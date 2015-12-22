
package com.miui.optimizecenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import miui.provider.ExtraGuardVirusInfoEntity;
import miui.provider.ExtraSettings;
import miui.text.ExtraTextUtils;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.util.Log;

import com.cleanmaster.sdk.CMCleanConst;
import com.cleanmaster.sdk.IAdDirCallback;
import com.cleanmaster.sdk.ICacheCallback;
import com.cleanmaster.sdk.IResidualCallback;
import com.cleanmaster.sdk.IKSCleaner;
import com.miui.antivirus.ExtraGuardHelper;
import com.miui.common.ApkIconHelper;
import com.miui.common.NotificationHelper;
import com.miui.common.NotificationHelper.NotificationKey;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.optimizecenter.ad.AdModel;
import com.miui.optimizecenter.apk.ApkModel;
import com.miui.optimizecenter.cache.CacheModel;
import com.miui.optimizecenter.enums.ApkStatus;
import com.miui.optimizecenter.enums.GarbageCleanupSize;
import com.miui.optimizecenter.enums.GarbageCleanupTimes;
import com.miui.optimizecenter.residual.ResidualModel;
import com.miui.optimizecenter.tools.ApkUtils;
import com.miui.optimizecenter.tools.CacheUtils;
import com.miui.optimizecenter.whitelist.InternalWhiteList;
import com.miui.optimizecenter.whitelist.WhiteListManager;
import com.miui.optimizecenter.whitelist.WhiteListColumns;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.DateTimeUtils;
import com.miui.securitycenter.ExtraIntent;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.IFileScanCallback;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageStats;

import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;

import com.miui.optimizecenter.cleandb.CleanMaster;
import com.miui.optimizecenter.cleandb.CacheEntity;

public class CheckGarbageCleanupService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ServiceConnection mCleanerConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCleaner = null;
            mCleanerConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCleaner = IKSCleaner.Stub.asInterface(service);
            mCleanerConnected = true;
            checkServiceConnection();
        }
    };

    private ServiceConnection mFileConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mFileProxy = null;
            mFileProxyConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mFileProxy = IFileProxy.Stub.asInterface(service);
            mFileProxyConnected = true;
            checkServiceConnection();
        }
    };

    private boolean mCacheScanned = false;
    private boolean mAdScanned = false;
    private boolean mApkScanned = false;
    private boolean mResidualScanned = false;

    private boolean mCleanerConnected = false;
    private boolean mFileProxyConnected = false;

    private long mGarbageSize = 0;

    private WhiteListManager mWhiteListManager;
    protected IKSCleaner mCleaner;
    private IFileProxy mFileProxy;

    private CleanMaster mCleanMaster;

    private List<String> mCacheDirPaths = new ArrayList<String>();

    private ICacheCallback.Stub mCacheCallbStub = new ICacheCallback.Stub() {

        // apk包个数
        @Override
        public void onStartScan(int nTotalScanItem) throws RemoteException {
            mCacheScanned = false;
            mCacheDirPaths.clear();
        }

        @Override
        public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
            return false;
        }

        @Override
        public void onFindCacheItem(String cacheType, String dirPath, String pkgName,
                boolean bAdviseDel, String alertInfo, String descx) throws RemoteException {

            if (!mWhiteListManager.inCacheWhiteList(dirPath)
                    && !InternalWhiteList.inInternalCacheWhiteList(dirPath)) {
                CacheEntity cEntity = mCleanMaster.queryCacheByDirPath(dirPath);
                if (cEntity != null){
                    bAdviseDel = cEntity.isAdviseDel();
                }
                if (bAdviseDel && !mCacheDirPaths.contains(dirPath)) {
                    mCacheDirPaths.add(dirPath);
                    mGarbageSize += mCleaner.pathCalcSize(dirPath);
                }
            }
        }

        @Override
        public void onCacheScanFinish() throws RemoteException {
            mCacheScanned = true;
            checkGarbageScanned();
        }
    };

    private IResidualCallback.Stub mResidualCallbStub = new IResidualCallback.Stub() {

        @Override
        public void onStartScan(int nTotalScanItem) throws RemoteException {
            mResidualScanned = false;
        }

        @Override
        public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
            return false;
        }

        @Override
        public void onFindResidualItem(String dirPath, String descName, boolean bAdviseDel,
                String alertInfo) throws RemoteException {
            if (!mWhiteListManager.inResidualWhiteList(dirPath)) {
                if (bAdviseDel) {
                    mGarbageSize += mCleaner.pathCalcSize(dirPath);
                }
            }
        }

        @Override
        public void onResidualScanFinish() throws RemoteException {
            mResidualScanned = true;
            checkGarbageScanned();
        }

    };

    private IAdDirCallback.Stub mADCallStub = new IAdDirCallback.Stub() {

        @Override
        public void onStartScan(int nTotalScanItem) throws RemoteException {
            mAdScanned = false;
        }

        @Override
        public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
            return false;
        }

        @Override
        public void onFindAdDir(String name, String dirPath) throws RemoteException {
            if (!mWhiteListManager.inAdWhiteList(dirPath)) {
                mGarbageSize += mCleaner.pathCalcSize(dirPath);
            }
        }

        @Override
        public void onAdDirScanFinish() throws RemoteException {
            mAdScanned = true;
            checkGarbageScanned();
        }
    };

    private IFileScanCallback.Stub mApkCallStub = new IFileScanCallback.Stub() {

        @Override
        public void onScanStart() {
            mApkScanned = false;
        }

        // Stop scanning when return true
        @Override
        public boolean onFindFile(ProxyFileInfo info) {
            try {
                String dirPath = info.getAbsolutePath();
                if (!mWhiteListManager.inApkWhiteList(dirPath)) {
                    ApkModel apk = new ApkModel();
                    ApkUtils.checkApkStatus(getApplicationContext(), apk, dirPath, mFileProxy);

                    boolean bAdviseDel = apk.getStatus() != ApkStatus.UNINSTALLED
                            && apk.getStatus() != ApkStatus.INSTALLED;
                    if (bAdviseDel) {
                        mGarbageSize += mCleaner.pathCalcSize(dirPath);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        public void onScanFinish() {
            mApkScanned = true;
            checkGarbageScanned();
        }

        @Override
        public void onError() {
            mApkScanned = true;
            checkGarbageScanned();
        }
    };

    private IPackageStatsObserver mStatsObserver = new IPackageStatsObserver.Stub() {
        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                throws RemoteException {
            mGarbageSize += pStats.cacheSize + pStats.externalCacheSize;

            if (++mSystemCacheLoadCount == mSystemCacheCount) {
                doScanExternalGarbage();
            }
        }
    };

    private int mSystemCacheCount = 0;
    private int mSystemCacheLoadCount = 0;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        if (valideRootAnalytics()) {
            AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_STABLE_ROOT_ACCESS);
        }
        mWhiteListManager = WhiteListManager.getInstance(this);

        AidlProxyHelper.getInstance().bindFileProxy(this, mFileConnection);
        AidlProxyHelper.getInstance().bindCleanProxy(this, mCleanerConnection);
    };

    private boolean valideRootAnalytics() {
        File f = null;
        final String kSuSearchPaths[] = {
                "/system/bin/", "/system/xbin/",
                "/system/sbin/", "/sbin/", "/vendor/bin/"
        };
        try {
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f != null && f.exists()) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private void checkServiceConnection() {
        if (mCleanerConnected && mFileProxyConnected) {

            GarbageCleanupTimes time = Preferences.getGarbageCleanupTime();
            if (time == GarbageCleanupTimes.NEVER) {
                stopSelf();
                return;
            }

            long lastCheckTime = Preferences.getLastGarbageCleanupTime();
            int realInterval = DateTimeUtils.getFromNowDayInterval(lastCheckTime);
            int targetInterval = time.getValue(getResources());
            if (lastCheckTime != 0 && realInterval < targetInterval) {
                stopSelf();
                return;
            }

            Preferences.setLastGarbageCleanupTime(System.currentTimeMillis());

            mCleanMaster = CleanMaster.getInstance(this);
            WhiteListManager wlm = WhiteListManager.getInstance(this);
            wlm.loadAdWhiteList();
            wlm.loadApkWhiteList();
            wlm.loadCacheWhiteList();
            wlm.loadResidualWhiteList();

            mGarbageSize = 0;
            doScanSystemCache();
        }
    }

    private void doScanSystemCache() {
        if (mWhiteListManager.inCacheWhiteList(ApkIconHelper.PKG_SYSTEM_CACHE)) {
            doScanExternalGarbage();
        } else {
            mSystemCacheCount = 0;
            mSystemCacheLoadCount = 0;

            PackageManager pm = getPackageManager();
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            List<PackageInfo> apps = pm.getInstalledPackages(0);
            for (PackageInfo info : apps) {
                if (CacheUtils.isRunningProcess(processes, info.packageName)) {
                    continue;
                }
                if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    ++mSystemCacheCount;
                    CacheUtils.getPackageCacheSize(this, mStatsObserver, info.packageName);
                }
            }
        }
    }

    private void doScanExternalGarbage() {
        mAdScanned = false;
        mCacheScanned = false;
        mResidualScanned = false;
        mApkScanned = false;

        try {

            List<String> mEmptyFolderPaths = mFileProxy.getEmptyDirList();
            if (!mEmptyFolderPaths.isEmpty()) {
                mGarbageSize += mEmptyFolderPaths.size() * 4 * ExtraTextUtils.KB;
            }

            Locale locale = getResources().getConfiguration().locale;
            mCleaner.init(locale.getLanguage(), locale.getCountry());

            // 广告
            mCleaner.scanAdDir(mADCallStub);
            // 缓存
            mCleaner.scanCache(CMCleanConst.MASK_SCAN_COMMON, mCacheCallbStub);
            // 卸载残留
            mCleaner.scanResidual(CMCleanConst.MASK_SCAN_COMMON
                    | CMCleanConst.MASK_SCAN_ADVANCED, mResidualCallbStub);

            // 安装包
            String volumeName = "external";
            String[] columns = new String[] {
                    FileColumns.DATA
            };
            String selection = FileColumns.DATA + " LIKE '%.apk'";

            mFileProxy.scanFilesByUri(mApkCallStub, Files.getContentUri(volumeName), columns,
                    selection, null, null);
        } catch (Exception e) {
            // ignore
        }
    }

    private void checkGarbageScanned() {
        if (mAdScanned && mCacheScanned && mResidualScanned && mApkScanned) {
            Preferences.setScannedGarbageSize(mGarbageSize);
            GarbageCleanupSize size = Preferences.getGarbageCleanupSize();
            if (mGarbageSize >= size.getValue(getResources())) {
                showNotification(this, mGarbageSize);
            } else {
                stopSelf();
            }
        }
    }

    public static void showNotification(Context context, long garbageSize) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ExtraIntent.EXTRA_AUTO_START_SCAN, true);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder builder = new Notification.Builder(context);

        builder.setSmallIcon(R.drawable.stat_notify_garbage_cleanup);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_launcher_rubbish_clean));

        String title = context.getString(R.string.notification_title_of_garbage_cleanup);
        String summary = context.getString(R.string.notification_summary_of_garbage_cleanup,
                ExtraTextUtils.formatFileSize(context, garbageSize));

        builder.setContentTitle(title);
        builder.setContentText(summary);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pi);
        builder.setAutoCancel(true);

        Notification notification = builder.build();

        notification.tickerText = title + ":" + summary;
        notification.extraNotification.customizedIcon = true;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NotificationHelper.getInstance(context).getNotificationIdByKey(
                NotificationKey.KEY_TIME_GARBAGE_CLEANUP), notification);
        Preferences.setGarbageIndanger(true);
    }

    public static void cancelNotification(Context context) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NotificationHelper.getInstance(context).getNotificationIdByKey(
                NotificationKey.KEY_TIME_GARBAGE_CLEANUP));
        Preferences.setGarbageIndanger(false);
    }

    @Override
    public void onDestroy() {
        AidlProxyHelper.getInstance().unbindProxy(this, mFileConnection);
        AidlProxyHelper.getInstance().unbindProxy(this, mCleanerConnection);
        super.onDestroy();
    }

}
