
package com.miui.securitycenter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import miui.provider.ExtraTelephony;

import com.miui.antivirus.RestoreAntiVirusWhiteListService;
import com.miui.antivirus.VirusLibAutoUpdateService;
import com.miui.common.NotificationHelper;
import com.miui.common.PreferenceStore;
import com.miui.common.NotificationHelper.NotificationKey;
import com.miui.optimizecenter.AutoUpdateCLeanupDBService;
import com.miui.optimizecenter.CheckGarbageCleanupService;
import com.miui.optimizecenter.cleandb.CleanDbHelper;
import com.miui.optimizecenter.uninstallmonitor.PackageSyncService;

import java.io.IOException;
import java.util.Calendar;

public class SecurityCenterService extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private Handler mHandler = new Handler();

    private final ContentObserver mMmsSpamObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        };

        public void onChange(boolean selfChange, Uri uri) {
            updateNotification();
        };
    };
    private final ContentObserver mPhoneSpamObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        };

        public void onChange(boolean selfChange, Uri uri) {
            updateNotification();
        };
    };

    private void updateNotification() {
        int phoneCount = NotificationService.getNewBlockedPhoneCount(this);
        int mmsCount = NotificationService.getNewBlockedMmsCount(this);
        Preferences.setHasNewBlockPhone(phoneCount != 0);
        Preferences.setHasNewBlockMms(mmsCount != 0);

        Intent intent = new Intent(NotificationService.ACTION_UPDATE_NOTIFICATION);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化SharedPreferences
        PreferenceStore.init(this);

        // 同步系统已安装的应用信息，在提示卸载残留时使用
        startService(new Intent(this, PackageSyncService.class));

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // 自动定时更新清理库
        setAutoUpdateCleanupDBAlarm(am, this);
        // 垃圾清理定时提醒
        setDailyGarbageCleanupAlarm(am, this);

        // 定时更新手动优化项
        setAutoUpdateManualItemAlarm(am, this);

        // 开启通知栏
        if (Preferences.isShowPermanentNotification(getContentResolver())) {
            startService(new Intent(this, NotificationService.class));
        }

        // 病毒库更新
        if (com.miui.antivirus.Preferences.isVirusLibAutoUpdateEnabled()) {
            setVriusLibAutoUpdateAlarm(this);
        }

        // 重置Anti Virus white list
        setRestoreAntiVirusWhiteListAlarm(am, this);

        try {
            CleanDbHelper dbHelper = new CleanDbHelper(this);
            dbHelper.copyDbFromAssert(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContentResolver cr = getContentResolver();
        cr.registerContentObserver(ExtraTelephony.FirewallLog.CONTENT_URI, false,
                mPhoneSpamObserver);
        cr.registerContentObserver(ExtraTelephony.MmsSms.BLOCKED_CONVERSATION_CONTENT_URI, false,
                mMmsSpamObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ContentResolver cr = getContentResolver();
        cr.unregisterContentObserver(mPhoneSpamObserver);
        cr.unregisterContentObserver(mMmsSpamObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * 每天15:00-17:00检查一次
     */
    private void setAutoUpdateManualItemAlarm(AlarmManager am, Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 15 + (int) (Math.random() * 2));
        calendar.set(Calendar.MINUTE, (int) (Math.random() * 60));

        Intent intent = new Intent(context, AutoUpdateManualListService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    /**
     * 每天8:00-10:00检查一次
     */
    private void setAutoUpdateCleanupDBAlarm(AlarmManager am, Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8 + (int) (Math.random() * 2));
        calendar.set(Calendar.MINUTE, (int) (Math.random() * 60));

        Intent intent = new Intent(context, AutoUpdateCLeanupDBService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    /**
     * 每天11:00-15:00检查一次
     */
    private void setDailyGarbageCleanupAlarm(AlarmManager am, Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 11 + (int) (Math.random() * 4));
        calendar.set(Calendar.MINUTE, (int) (Math.random() * 60));

        Intent intent = new Intent(context, CheckGarbageCleanupService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    /**
     * 病毒库升级 每天6:00-8:00检查一次
     */
    public static void setVriusLibAutoUpdateAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6 + (int) (Math.random() * 2));
        calendar.set(Calendar.MINUTE, (int) (Math.random() * 60));

        int requestCode = NotificationHelper.getInstance(context).getNotificationIdByKey(
                NotificationKey.KEY_VIRUS_LIB_AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, new Intent(
                context, VirusLibAutoUpdateService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    /**
     * 关闭病毒库自动更新
     */
    public static void cancelVirusLibAutoUpdateAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int requestCode = NotificationHelper.getInstance(context).getNotificationIdByKey(
                NotificationKey.KEY_VIRUS_LIB_AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, new Intent(
                context, VirusLibAutoUpdateService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pendingIntent);
    }

    /**
     * 每周六恢复一次全量病毒扫描
     */
    private void setRestoreAntiVirusWhiteListAlarm(AlarmManager am, Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 1 + (int) (Math.random() * 2));
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

        int requestCode = NotificationHelper.getInstance(context).getNotificationIdByKey(
                NotificationKey.KEY_CLEAR_VIRUS_WHITE_LIST);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, new Intent(
                context, RestoreAntiVirusWhiteListService.class),
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

}
