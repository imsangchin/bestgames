
package com.miui.securitycenter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.AnimatorListenerAdapter;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.MiuiIntent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.BatteryManager;
import miui.os.Environment;
import miui.provider.ExtraTelephony;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.RemoteViews;

import com.miui.common.AndroidUtils;
import com.miui.common.NotificationHelper;
import com.miui.common.NotificationHelper.NotificationKey;
import com.miui.securitycenter.RecentTaskMonitor.RecentTaskListener;
import com.miui.securitycenter.RecentTaskMonitor.TopTask;

import android.provider.MiuiSettings;
import miui.content.res.IconCustomizer;

import com.miui.securitycenter.R;

public class NotificationService extends Service implements RecentTaskListener {

    public static final int INVALIDE_ANGLE = -1;

    public static final String ACTION_UPDATE_NOTIFICATION = "com.miui.securitycenter.action.UPDATE_NOTIFICATION";

    private boolean mHasNewAntiSpam = false;
    private boolean mNeedShowTickerText = false;

    private BroadcastReceiver mAntiSpamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateAntiSpamStatus();
        }
    };

    private void updateAntiSpamStatus() {
        if (Preferences.hasNewBlockMms() || Preferences.hasNewBlockPhone()) {
            mHasNewAntiSpam = true;
            mNeedShowTickerText = true;
        } else {
            mHasNewAntiSpam = false;
            mNeedShowTickerText = false;
        }

        sendAntiSpamUpdateBroadcast(this);
    }

    public static void sendAntiSpamUpdateBroadcast(Context context) {
        Intent intent = new Intent("miui.intent.action.ANTISPAM_UPDATE");
        boolean hasNew = (Preferences.hasNewBlockPhone() || Preferences.hasNewBlockMms())
                && !MiuiSettings.AntiSpam.hasViewAntispam(context);
        intent.putExtra("has_intercept", hasNew ? 1 : 0);
        context.sendStickyBroadcast(intent);
    }

    public static void sendBatteryConfigChangeBroadcast(Context context) {
        // TODO
    }

    private int mBatteryPercent = 0;

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryStatus(intent);
        }
    };

    private void updateBatteryStatus(Intent intent) {
        if (intent == null) {
            return;
        }

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);

        if (scale != 0) {
            mBatteryPercent = (level * 100) / scale;
        }
    }

    private ClearMemoryReceiver mClearMemoryReceiver = new ClearMemoryReceiver();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private RecentTaskMonitor mRecentTaskMonitor;

    private NotificationHelper mNotificationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mClearMemoryReceiver, new IntentFilter(
                ClearMemoryReceiver.ACTION_CLEAR_MEMORY));
        registerReceiver(mAntiSpamReceiver, new IntentFilter(ACTION_UPDATE_NOTIFICATION));

        Intent intent = registerReceiver(mBatteryReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        updateBatteryStatus(intent);

        mNotificationHelper = NotificationHelper.getInstance(this);

        // 监听RecentTask
        mRecentTaskMonitor = RecentTaskMonitor.getInstance(getApplicationContext(), this);
        mRecentTaskMonitor.start();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mClearMemoryReceiver);
        unregisterReceiver(mAntiSpamReceiver);
        unregisterReceiver(mBatteryReceiver);
        mRecentTaskMonitor.stop();
        cancelNotification();
        super.onDestroy();
    }

    private void showNotification(int animAngle) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.m_notification_remoteview);

        // 防止整个status bar被点击
        views.setOnClickPendingIntent(R.id.ll_frame, null);

        // 安全中心
        Bitmap icLauncher = null;
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(getPackageName(), 0);
            icLauncher = IconCustomizer.generateIconStyleDrawable(appInfo.loadIcon(pm)).getBitmap();
        } catch (Exception e) {
            icLauncher = IconCustomizer.generateIconStyleDrawable(
                    getResources().getDrawable(R.drawable.app_icon_securitycenter)).getBitmap();
        }
        views.setImageViewBitmap(R.id.iv_launcher, icLauncher);

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent iv_launcher_pi = PendingIntent.getActivity(this, 0, new Intent(this,
                MainActivity.class), 0);
        views.setOnClickPendingIntent(R.id.iv_launcher, iv_launcher_pi);

        // 一键清理
        final long totalMemory = Environment.getTotalPhysicalMemory() / 1024;
        long freeMemory = Environment.getFreeMemory() / 1024;
        long usedMemory = totalMemory - freeMemory;

        views.setTextViewText(R.id.iv_garbage_text,
                getString(R.string.menu_item_notification_garbage_text,
                        getFormatedMemory(freeMemory, true)));

        if (animAngle == INVALIDE_ANGLE) {
            animAngle = (int) (((usedMemory * 1.0f) / (totalMemory * 1.0f)) * 360);
        }

        views.setImageViewResource(R.id.iv_garbage, getGarbageDrawableId(animAngle));

        Intent clearMemory = new Intent(ClearMemoryReceiver.ACTION_CLEAR_MEMORY);
        PendingIntent iv_garbage_pi = PendingIntent.getBroadcast(this, 0, clearMemory, 0);
        views.setOnClickPendingIntent(R.id.ll_barbage, iv_garbage_pi);

        // 省电优化
        views.setImageViewResource(R.id.iv_power, getPowerDrawableId(mBatteryPercent));

        PendingIntent iv_power_pi = PendingIntent.getActivity(this, 0, new Intent(
                ExtraIntent.ACTION_POWER_MODE_CHOOSER), 0);
        views.setOnClickPendingIntent(R.id.ll_power, iv_power_pi);

        views.setTextViewText(R.id.iv_power_text,
                getString(R.string.menu_text_power_percent, mBatteryPercent + "%"));

        // 骚扰拦截
        if (mHasNewAntiSpam && !MiuiSettings.AntiSpam.hasViewAntispam(this)) {
            views.setImageViewResource(R.id.iv_antispam,
                    R.drawable.icon_antispam_notification_avail);
            views.setTextViewText(R.id.iv_antispam_text,
                    getString(R.string.menu_item_notification_antispam_text_avail));
        } else {
            views.setImageViewResource(R.id.iv_antispam,
                    R.drawable.icon_antispam_notification_normal);
            views.setTextViewText(R.id.iv_antispam_text,
                    getString(R.string.menu_item_notification_antispam_text_normal));
        }

        PendingIntent iv_antispam_pi = PendingIntent.getActivity(this, 0, new Intent(
                "android.intent.action.SET_FIREWALL"), 0);
        views.setOnClickPendingIntent(R.id.ll_antispam, iv_antispam_pi);

        // Notification
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        if (mHasNewAntiSpam && !MiuiSettings.AntiSpam.hasViewAntispam(this)) {
            builder.setSmallIcon(R.drawable.icon_stat_notify_firewall);
        } else {
            builder.setSmallIcon(R.drawable.icon_stat_safe);
        }
        builder.setWhen(System.currentTimeMillis());
        builder.setContent(views);
        builder.setContentIntent(pi);
        builder.setOngoing(true);

        Notification notification = builder.build();

        if (mNeedShowTickerText && MiuiSettings.AntiSpam.isShowBlockNotification(this)
                && !MiuiSettings.AntiSpam.hasViewAntispam(this)) {
            notification.tickerText = getString(R.string.notification_title_firewall_blocked);
            mNeedShowTickerText = false;
        }

        notification.priority = Notification.PRIORITY_MAX;

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(mNotificationHelper
                .getNotificationIdByKey(NotificationKey.KEY_PERM_NOTIFICATION_BAR), notification);
    }

    private void cancelNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(mNotificationHelper
                .getNotificationIdByKey(NotificationKey.KEY_PERM_NOTIFICATION_BAR));
    }

    private int getGarbageDrawableId(int angle) {
        if (angle == 0) {
            return R.drawable.icon_garbage_0;
        } else if (angle <= 15) {
            return R.drawable.icon_garbage_1;
        } else if (angle <= 30) {
            return R.drawable.icon_garbage_2;
        } else if (angle <= 45) {
            return R.drawable.icon_garbage_3;
        } else if (angle <= 60) {
            return R.drawable.icon_garbage_4;
        } else if (angle <= 75) {
            return R.drawable.icon_garbage_5;
        } else if (angle <= 90) {
            return R.drawable.icon_garbage_6;
        } else if (angle <= 105) {
            return R.drawable.icon_garbage_7;
        } else if (angle <= 120) {
            return R.drawable.icon_garbage_8;
        } else if (angle <= 135) {
            return R.drawable.icon_garbage_9;
        } else if (angle <= 150) {
            return R.drawable.icon_garbage_10;
        } else if (angle <= 165) {
            return R.drawable.icon_garbage_11;
        } else if (angle <= 180) {
            return R.drawable.icon_garbage_12;
        } else if (angle <= 195) {
            return R.drawable.icon_garbage_13;
        } else if (angle <= 210) {
            return R.drawable.icon_garbage_14;
        } else if (angle <= 225) {
            return R.drawable.icon_garbage_15;
        } else if (angle <= 240) {
            return R.drawable.icon_garbage_16;
        } else if (angle <= 255) {
            return R.drawable.icon_garbage_17;
        } else if (angle <= 270) {
            return R.drawable.icon_garbage_18;
        } else if (angle <= 285) {
            return R.drawable.icon_garbage_19;
        } else if (angle <= 300) {
            return R.drawable.icon_garbage_20;
        } else if (angle <= 315) {
            return R.drawable.icon_garbage_21;
        } else if (angle <= 330) {
            return R.drawable.icon_garbage_22;
        } else if (angle <= 345) {
            return R.drawable.icon_garbage_23;
        } else {
            return R.drawable.icon_garbage_24;
        }
    }

    private int getPowerDrawableId(int percent) {
        if (percent > 10) {
            return R.drawable.icon_power_normal;
        } else {
            return R.drawable.icon_power_low;
        }
    }

    public static int getNewBlockedPhoneCount(Context context) {
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[] {
                "count(*)"
        };
        String selection = "type=? AND read=?";
        // ExtraTelephony.FirewallLog.TYPE_CALL
        String[] selectionArgs = new String[] {
                String.valueOf(1), "0"
        };

        Cursor cursor = cr.query(ExtraTelephony.FirewallLog.CONTENT_URI, projection, selection,
                selectionArgs, null);

        if (cursor == null) {
            return 0;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            cursor = null;
            return 0;
        }

        int phoneCount = cursor.getInt(0);
        cursor.close();
        cursor = null;
        return phoneCount;
    }

    public static int getNewBlockedMmsCount(Context context) {
        // ExtraTelephony.Threads.UNREAD_COUNT
        String[] projection = new String[] {
                "unread_count"
        };
        String selection = "unread_count" + ">0";
        Cursor cursor = context.getContentResolver().query(
                ExtraTelephony.MmsSms.BLOCKED_CONVERSATION_CONTENT_URI, projection, selection,
                null, null);

        if (cursor == null) {
            return 0;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            cursor = null;
            return 0;
        }

        int mmsCount = 0;

        do {
            mmsCount += cursor.getInt(0);
        } while (cursor.moveToNext());

        cursor.close();
        cursor = null;
        return mmsCount;
    }

    public static String getFormatedMemory(long memoryK, boolean onlyM) {
        memoryK /= 1024;
        if (onlyM || memoryK < 1024) {
            return memoryK + "M";
        }
        memoryK /= 1024;
        return memoryK + "G";
    }

    // private TopTask mPreTopTask = TopTask.UNKNOWN;

    @Override
    public void onTopTaskChanged(TopTask topTask) {
        // if (mPreTopTask == topTask) {
        // return;
        // }
        // mPreTopTask = topTask;

        switch (topTask) {
            case SELF:
                cancelNotification();
                break;
            case OTHER:
                showNotification(INVALIDE_ANGLE);
                break;
            case UNKNOWN:
                // ignore
                break;

            default:
                break;
        }
    }

    public class ClearMemoryReceiver extends BroadcastReceiver {
        public static final String ACTION_CLEAR_MEMORY = "com.miui.securitycenter.action.CLEAR_MEMORY";

        private static final int ROTATE_VELOCITY = 600;

        private ObjectAnimator mCircleStartAnimator;
        private int mAngle = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            // 调用MiuiSystemUI
            context.sendBroadcast(new Intent(MiuiIntent.ACTION_SYSTEMUI_TASK_MANAGER_CLEAR));

            // 做动画
            if (mCircleStartAnimator != null) {
                mCircleStartAnimator.cancel();
                mCircleStartAnimator = null;
            }
            // 一键清理
            final long totalMemory = Environment.getTotalPhysicalMemory() / 1024;
            long freeMemory = Environment.getFreeMemory() / 1024;
            long usedMemory = totalMemory - freeMemory;

            int animAngle = (int) (((usedMemory * 1.0f) / (totalMemory * 1.0f)) * 360);

            mCircleStartAnimator = ObjectAnimator.ofInt(this, "angle", animAngle, 0);
            mCircleStartAnimator.setDuration(animAngle * 1000 / ROTATE_VELOCITY);
            mCircleStartAnimator.setInterpolator(new LinearInterpolator());
            mCircleStartAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animator) {
                    mRecentTaskMonitor.pause();
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mRecentTaskMonitor.resume();
                    showNotification(INVALIDE_ANGLE);
                }
            });
            mCircleStartAnimator.start();
        }

        public void setAngle(int angle) {
            mAngle = angle;
            showNotification(angle);
        }

        public int getAngle() {
            return mAngle;
        }
    }
}
