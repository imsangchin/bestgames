
package com.miui.optimizecenter.uninstallmonitor;

import java.util.ArrayList;
import java.util.Arrays;

import miui.content.res.IconCustomizer;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MiuiSettings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.cleanmaster.sdk.CMCleanConst;
import com.miui.common.AndroidUtils;
import com.miui.common.NotificationHelper;
import com.miui.common.NotificationHelper.NotificationKey;
import com.miui.guardprovider.service.StorageInfo;
import com.miui.optimizecenter.MainActivity;
import com.miui.securitycenter.ExtraIntent;

import com.cleanmaster.sdk.IKSCleaner;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.securitycenter.R;

public class PackageListener extends BroadcastReceiver {
    static final String TAG = PackageListener.class.getSimpleName();

    private String mPkgName;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        /**
        if (MiuiSettings.System.getBoolean(context.getContentResolver(),
                MiuiSettings.System.PACKAGE_DELETE_BY_RESTORE_PHONE, false)) {
            return;
        }

        try {
            mPkgName = getPackageName(intent);
            if (TextUtils.isEmpty(mPkgName)) {
                return;
            }

            mContext = context.getApplicationContext();
            boolean pkgUpdating = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            String action = intent.getAction();

            Log.d(TAG, "Package name = " + mPkgName + " updating = " + pkgUpdating);
            Log.d(TAG, "Action = " + action);

            PackagesManager manager = PackagesManager.getInstance(context);

            PackageManager pm = context.getPackageManager();

            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {

                PackageInfo info = AndroidUtils.getPackageInfoByName(context, mPkgName);
                if (info == null || info.applicationInfo == null) {
                    return;
                }
                CharSequence label = pm.getApplicationLabel(info.applicationInfo);
                String appLabel = label == null ? mPkgName : label.toString();
                Drawable launcher = info.applicationInfo.loadIcon(pm);
                String versionName = info.versionName;
                int versionCode = info.versionCode;

                PackageModel model = PackageModel.create(launcher, appLabel, mPkgName, versionName,
                        versionCode);

                if (manager.isPackageExist(mPkgName)) {
                    manager.updatePackage(model);
                } else {
                    manager.addPackage(model);
                }

                bindFileService(mContext);
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!pkgUpdating) {
                    bindCleanerService(mContext);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        String pkg = uri != null ? uri.getSchemeSpecificPart() : null;
        return pkg;
    }

    private IFileProxy mIFileProxy;

    private ServiceConnection mFileConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIFileProxy = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIFileProxy = IFileProxy.Stub.asInterface(service);

            checkLowMemory();
        }
    };

    private void checkLowMemory() {
        try {
            StorageInfo internalInfo = mIFileProxy.getInternalStroageInfo();
            long totalSize = internalInfo.total;
            long freeSize = internalInfo.free;
            long usedSize = totalSize - freeSize;
            float percent = (usedSize * 1.0f) / (totalSize * 1.0f);
            if (percent >= 0.9f) {
                showLowMemoryNotification(mContext, String.valueOf((int) (percent * 100)) + "%");
            }
        } catch (Exception e) {
            // ingore
        }
        unbindFileService(mContext);
    }

    public static void showLowMemoryNotification(Context context, String percent) {
        CharSequence title = context.getString(R.string.notification_title_of_low_memory, percent);
        CharSequence summary = context.getString(R.string.notification_summary_of_low_memory);

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.op_low_memory_notification_remoteview);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        views.setImageViewBitmap(R.id.icon,
                IconCustomizer.generateIconStyleDrawable(context.getResources().getDrawable(
                        R.drawable.ic_launcher_rubbish_clean)).getBitmap());

        views.setTextViewText(R.id.primary_text, title);
        views.setTextViewText(R.id.secondary_text, summary);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.stat_notify_garbage_cleanup);
        builder.setWhen(System.currentTimeMillis());
        builder.setContent(views);
        builder.setContentIntent(pi);

        Notification notification = builder.build();

        notification.tickerText = title + ":" + summary;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NotificationHelper.getInstance(context).getNotificationIdByKey(
                NotificationKey.KEY_LOW_SYSTEM_MEMORY), notification);
    }

    public static void cancelLowMemoryNotification(Context context) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NotificationHelper.getInstance(context).getNotificationIdByKey(
                NotificationKey.KEY_LOW_SYSTEM_MEMORY));
    }

    private void bindFileService(Context context) {
        // 访问sdcard文件使用
        Intent fileIntent = new Intent(ExtraIntent.ACTION_FILE_PROXY_SERVICE);
        context.bindService(fileIntent, mFileConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFileService(Context context) {
        context.unbindService(mFileConnection);
    }

    private ServiceConnection mCleanerConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // ignore
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            checkAppResiduals(IKSCleaner.Stub.asInterface(service));
        }
    };

    private void bindCleanerService(Context context) {
        Intent intent = new Intent(CMCleanConst.ACTION_CLEAN_SERVICE);
        context.bindService(intent, mCleanerConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindCleanerService(Context context) {
        context.unbindService(mCleanerConnection);
    }

    private void checkAppResiduals(IKSCleaner cleaner) {
        try {
            String[] residualPaths = cleaner.getResidualFilePaths(mPkgName);
            if (residualPaths != null && residualPaths.length > 0) {
                Log.d(TAG, "package name : " + mPkgName + " Residual path = "
                        + Arrays.toString(residualPaths));

                ArrayList<String> pathList = new ArrayList<String>();

                for (int i = 0; i < residualPaths.length; i++) {
                    pathList.add(residualPaths[i]);
                }

                long residualSize = 0;
                String largePath = null;
                long largeSize = 0;
                for (String path : pathList) {
                    long size = cleaner.pathCalcSize(path);
                    residualSize += size;
                    if (size > largeSize) {
                        largeSize = size;
                        largePath = path;
                    }
                }
                Log.d(TAG, "TotalSize = " + residualSize + " LargeSize = " + largeSize);

                if (residualSize > 0) {
                    Intent intent = new Intent(mContext, PackageActivity.class);
                    intent.putExtra(PackageActivity.EXTRA_PKG_NAME, mPkgName);
                    intent.putExtra(PackageActivity.EXTRA_RESIDUAL_SIZE, residualSize);
                    intent.putExtra(PackageActivity.EXTRA_VIEW_PATH, largePath);
                    intent.putStringArrayListExtra(PackageActivity.EXTRA_RESIDUAL_PATHS, pathList);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    mContext.startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        unbindCleanerService(mContext);
    }
}
