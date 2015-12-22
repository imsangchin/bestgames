
package com.miui.optimizecenter;

import miui.app.Activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.content.MiuiIntent;

import com.miui.securitycenter.R;

import com.cleanmaster.sdk.CMCleanConst;
import com.cleanmaster.sdk.IAdDirCallback;
import com.cleanmaster.sdk.ICacheCallback;
import com.cleanmaster.sdk.IResidualCallback;
import com.miui.analytics.AnalyticsUtil;
import com.miui.antivirus.ExtraGuardHelper;
import com.miui.antivirus.VirusCheckManager;
import com.miui.antivirus.event.OnActionButtonClickEvent;
import com.miui.common.AndroidUtils;
import com.miui.common.ApkIconHelper;
import com.miui.common.EventHandler;
import com.miui.common.MediaScannerUtil;
import com.miui.guardprovider.service.IFileScanCallback;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.guardprovider.service.StorageInfo;
import com.miui.optimizecenter.MainContentFrame.BackgroundStatus;
import com.miui.optimizecenter.MainHandleBar.HandleItem;
import com.miui.optimizecenter.ad.AdActivity;
import com.miui.optimizecenter.ad.AdModel;
import com.miui.optimizecenter.apk.ApkActivity;
import com.miui.optimizecenter.apk.ApkModel;
import com.miui.optimizecenter.cache.CacheActivity;
import com.miui.optimizecenter.cache.CacheModel;
import com.miui.optimizecenter.cleandb.CacheEntity;
import com.miui.optimizecenter.cleandb.CleanMaster;
import com.miui.optimizecenter.deepclean.DeepCleanActivity;
import com.miui.optimizecenter.enums.ApkStatus;
import com.miui.optimizecenter.enums.GarbageCleanStatus;
import com.miui.optimizecenter.enums.SecurityStatus;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.HandleEmptyListEvent;
import com.miui.optimizecenter.event.OnCleanupGarbageItemEvent;
import com.miui.optimizecenter.event.RefreshGarbageSizeEvent;
import com.miui.optimizecenter.event.OnFinishCleanupGarbageEvent;
import com.miui.optimizecenter.event.OnFinishScanAdEvent;
import com.miui.optimizecenter.event.OnFinishScanApkEvent;
import com.miui.optimizecenter.event.OnFinishScanCacheEvent;
import com.miui.optimizecenter.event.OnFinishScanGarbageEvent;
import com.miui.optimizecenter.event.OnFinishScanResidualEvent;
import com.miui.optimizecenter.event.OnGarbageHandleItemClickEvent;
import com.miui.optimizecenter.event.OnScanningItemEvent;
import com.miui.optimizecenter.event.OnStartCleanupGarbageEvent;
import com.miui.optimizecenter.event.OnStartScanAdEvent;
import com.miui.optimizecenter.event.OnStartScanApkEvent;
import com.miui.optimizecenter.event.OnStartScanCacheEvent;
import com.miui.optimizecenter.event.OnStartScanResidualEvent;
import com.miui.optimizecenter.event.RefreshFreeMemoryEvent;
import com.miui.optimizecenter.event.SetContentSummaryVisibilityEvent;
import com.miui.optimizecenter.event.SetContentTitleEvent;
import com.miui.optimizecenter.event.SetDeepCleanVisibilityEvent;
import com.miui.optimizecenter.event.SetHandleItemContentEvent;
import com.miui.optimizecenter.event.ViewDeepCleanEvent;
import com.miui.optimizecenter.residual.ResidualActivity;
import com.miui.optimizecenter.residual.ResidualModel;
import com.miui.optimizecenter.tools.ApkUtils;
import com.miui.optimizecenter.tools.CacheUtils;
import com.miui.optimizecenter.tools.ScanADsThread;
import com.miui.optimizecenter.tools.ScanAPKsThread;
import com.miui.optimizecenter.tools.ScanCachesThread;
import com.miui.optimizecenter.tools.ScanResidualsThread;
import com.miui.optimizecenter.tools.ScanSystemCacheCallback;
import com.miui.optimizecenter.tools.ScanSystemCacheThread;
import com.miui.optimizecenter.whitelist.InternalWhiteList;
import com.miui.optimizecenter.whitelist.WhiteListManager;
import com.miui.securitycenter.DateTimeUtils;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.ExtraIntent;
import com.miui.securitycenter.event.OnBackPressedEvent;
import com.miui.securitycenter.event.ViewSettingsEvent;

import com.cleanmaster.sdk.IKSCleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miui.provider.ExtraGuardVirusInfoEntity;
import miui.text.ExtraTextUtils;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ServiceConnection mCleanerConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIKSCleaner = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIKSCleaner = IKSCleaner.Stub.asInterface(service);
            AidlProxyHelper.getInstance().setIKSCleaner(mIKSCleaner);
            checkServiceConnection();
        }
    };

    private ServiceConnection mFileConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIFileProxy = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIFileProxy = IFileProxy.Stub.asInterface(service);
            AidlProxyHelper.getInstance().setIFileProxy(mIFileProxy);
            checkServiceConnection();
        }
    };

    private ICacheCallback.Stub mCacheCallback = new ICacheCallback.Stub() {

        // apk包个数
        @Override
        public void onStartScan(int nTotalScanItem) throws RemoteException {
            mCacheScanned = false;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_START_SCAN_CACHE,
                    OnStartScanCacheEvent.create());
        }

        @Override
        public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_SCANNING_ITEM,
                    OnScanningItemEvent.create(desc));
            Log.d("miui", "===========onScanCacheItem = " + desc);
            return mForceStopped;
        }

        @Override
        public void onFindCacheItem(String cacheType, String dirPath, String pkgName,
                boolean bAdviseDel, String alertInfo, String descx) throws RemoteException {
            Log.d("miui", "===========onFindCacheItem cacheType cacheType = " + cacheType
                    + " dirPath = " + dirPath + " bAdviseDel = " + bAdviseDel + " alertInfo = "
                    + alertInfo + " descx = " + descx);

            if (!mWhiteListManager.inCacheWhiteList(dirPath)
                    && !InternalWhiteList.inInternalCacheWhiteList(dirPath)) {
                CacheModel cache = new CacheModel();
                cache.setDirectoryPath(dirPath);

                CacheEntity entity = mCleanMaster.queryCacheByDirPath(dirPath);
                if (entity != null) {
                    cache.setCacheType(entity.getCacheType());
                    cache.setPackageName(entity.getPkgName());
                    cache.setAdviseDelete(entity.isAdviseDel());
                    cache.setAlertInfo(entity.getAlertInfo());
                    cache.setDescription(entity.getDescx());
                } else {
                    cache.setCacheType(cacheType);
                    cache.setPackageName(pkgName);
                    cache.setAdviseDelete(bAdviseDel);
                    cache.setAlertInfo(alertInfo);
                    cache.setDescription(descx);
                }

                long fileSize = mIKSCleaner.pathCalcSize(dirPath);
                cache.setFileSize(fileSize);

                if (mDataManager.addCacheModel(dirPath, cache) && cache.adviseDelete()) {
                    mTotalGarbageSize += cache.getFileSize();
                    mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_GARBAGE_SIZE,
                            RefreshGarbageSizeEvent.create());
                }
            }
        }

        @Override
        public void onCacheScanFinish() throws RemoteException {
            mCacheScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_SCAN_CACHE,
                    OnFinishScanCacheEvent.create());
            checkScanningStatus();
        }
    };

    private IResidualCallback.Stub mResidualCallback = new IResidualCallback.Stub() {

        @Override
        public void onStartScan(int nTotalScanItem) throws RemoteException {
            mResidualScanned = false;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_START_SCAN_RESIDUAL,
                    OnStartScanResidualEvent.create());
        }

        @Override
        public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_SCANNING_ITEM,
                    OnScanningItemEvent.create(desc));
            return mForceStopped;
        }

        @Override
        public void onFindResidualItem(String dirPath, String descName, boolean bAdviseDel,
                String alertInfo) throws RemoteException {
            Log.d("miui", "=========onFindResidualItem dirPath = " + dirPath + " descName = "
                    + descName + " bAdviseDel = " + bAdviseDel + " alertInfo = " + alertInfo);
            if (!mWhiteListManager.inResidualWhiteList(dirPath)) {
                ResidualModel residual = new ResidualModel();
                residual.setDirectoryPath(dirPath);
                residual.setDescName(descName);
                residual.setAlertInfo(alertInfo);

                long fileSize = mIKSCleaner.pathCalcSize(dirPath);
                residual.setFileSize(fileSize);
                residual.setAdviseDelete(bAdviseDel);

                mDataManager.addResidualModel(dirPath, residual);

                if (residual.adviseDelete()) {
                    mTotalGarbageSize += residual.getFileSize();
                    mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_GARBAGE_SIZE,
                            RefreshGarbageSizeEvent.create());
                }
            }
        }

        @Override
        public void onResidualScanFinish() throws RemoteException {
            mResidualScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_SCAN_RESIDUAL,
                    OnFinishScanResidualEvent.create());
            checkScanningStatus();
        }

    };

    private IAdDirCallback.Stub mAdCallback = new IAdDirCallback.Stub() {

        @Override
        public void onStartScan(int nTotalScanItem) throws RemoteException {
            mAdScanned = false;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_START_SCAN_AD,
                    OnStartScanAdEvent.create());
        }

        @Override
        public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_SCANNING_ITEM,
                    OnScanningItemEvent.create(desc));
            return mForceStopped;
        }

        @Override
        public void onFindAdDir(String name, String dirPath) throws RemoteException {
            Log.d("miui", "==============alertInfo name = " + name + " dirPath = " + dirPath);

            if (!mWhiteListManager.inAdWhiteList(dirPath)) {
                AdModel ad = new AdModel();
                ad.setName(name);
                ad.setDirectoryPath(dirPath);

                long fileSize = mIKSCleaner.pathCalcSize(dirPath);
                ad.setFileSize(fileSize);
                ad.setAdviseDelete(true);

                mDataManager.addAdModel(dirPath, ad);

                if (ad.adviseDelete()) {
                    mTotalGarbageSize += ad.getFileSize();
                    mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_GARBAGE_SIZE,
                            RefreshGarbageSizeEvent.create());
                }
            }
        }

        @Override
        public void onAdDirScanFinish() throws RemoteException {
            mAdScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_SCAN_AD,
                    OnFinishScanAdEvent.create());
            checkScanningStatus();
        }
    };

    private IFileScanCallback.Stub mApkCallback = new IFileScanCallback.Stub() {

        @Override
        public void onScanStart() {
            mApkScanned = false;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_START_SCAN_APK,
                    OnStartScanApkEvent.create());
        }

        // Stop scanning when return true
        @Override
        public boolean onFindFile(ProxyFileInfo info) {
            try {
                String dirPath = info.getAbsolutePath();
                Log.d("miui", "==========onFindFile dirPath = " + dirPath);
                if (!mWhiteListManager.inApkWhiteList(dirPath)) {
                    ApkModel apk = new ApkModel();
                    ApkUtils.checkApkStatus(MainActivity.this, apk, dirPath, mIFileProxy);
                    apk.setSecurityStatus(SecurityStatus.SAFE);

                    boolean bAdviseDel = apk.getStatus() != ApkStatus.UNINSTALLED
                            && apk.getStatus() != ApkStatus.INSTALLED;
                    long fileSize = mIKSCleaner.pathCalcSize(dirPath);
                    apk.setFileSize(fileSize);
                    apk.setAdviseDelete(bAdviseDel);

                    mDataManager.addApkModel(dirPath, apk);

                    mEventHandler.sendEventMessage(EventType.EVENT_ON_SCANNING_ITEM,
                            OnScanningItemEvent.create(apk.getApplicationLabel()));

                    if (apk.adviseDelete()) {
                        mTotalGarbageSize += apk.getFileSize();
                        mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_GARBAGE_SIZE,
                                RefreshGarbageSizeEvent.create());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mForceStopped;
        }

        @Override
        public void onScanFinish() {
            mApkScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_SCAN_APK,
                    OnFinishScanApkEvent.create());
            checkScanningStatus();
        }

        @Override
        public void onError() {
            mApkScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_SCAN_APK,
                    OnFinishScanApkEvent.create());
            checkScanningStatus();
        }
    };

    private ScanSystemCacheCallback mSystemCacheCallback = new ScanSystemCacheCallback() {

        @Override
        public void onScanStart() {
            mSystemCacheSize = 0;
            mSystemCacheScanned = false;
        }

        @Override
        public boolean onScanItem(String pkgName, PackageStats usageStats) {
            mSystemCacheSize += usageStats.cacheSize + usageStats.externalCacheSize;
            Log.d("miui", "==========system cache = " + pkgName);
            return mForceStopped;
        }

        @Override
        public void onScanFinish() {
            if (mSystemCacheSize > 0) {
                if (!mWhiteListManager.inCacheWhiteList(ApkIconHelper.PKG_SYSTEM_CACHE)) {
                    CacheModel systemCache = new CacheModel();
                    PackageManager pm = getPackageManager();
                    String cacheType = getString(R.string.cache_title_system_cache);
                    systemCache.setCacheType(cacheType);
                    systemCache.setFileSize(mSystemCacheSize);
                    systemCache.setPackageName(ApkIconHelper.PKG_SYSTEM_CACHE);
                    systemCache.setDirectoryPath(ApkIconHelper.PKG_SYSTEM_CACHE);
                    systemCache.setAdviseDelete(true);
                    mDataManager.addCacheModel(ApkIconHelper.PKG_SYSTEM_CACHE,
                            systemCache);
                    mTotalGarbageSize += mSystemCacheSize;
                    mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_GARBAGE_SIZE,
                            RefreshGarbageSizeEvent.create());
                }
            }

            mSystemCacheScanned = true;
            checkScanningStatus();
        }
    };

    private class LoadEmptyFoldersTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mEmptyFoldersScanned = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // empty folders
            if (!mWhiteListManager.inCacheWhiteList(ApkIconHelper.PKG_EMPTY_FOLDER)) {
                List<String> emptyFolderPaths = null;
                try {
                    emptyFolderPaths = mIFileProxy.getEmptyDirList();
                } catch (RemoteException e) {
                    // ignore
                }
                if (emptyFolderPaths != null && !emptyFolderPaths.isEmpty()) {
                    CacheModel emptyFolder = new CacheModel();

                    emptyFolder.setPackageName(ApkIconHelper.PKG_EMPTY_FOLDER);
                    emptyFolder.setDirectoryPath(ApkIconHelper.PKG_EMPTY_FOLDER);
                    emptyFolder.setAdviseDelete(true);

                    long folderSize = emptyFolderPaths.size() * 4 * 1000;

                    emptyFolder.setFileSize(folderSize);
                    emptyFolder.setCacheType(getString(R.string.cache_title_empty_folder,
                            emptyFolderPaths.size()));

                    mDataManager.addCacheModel(ApkIconHelper.PKG_EMPTY_FOLDER, emptyFolder);
                    mDataManager.setEmptyFolderPaths(emptyFolderPaths);

                    if (emptyFolder.adviseDelete()) {
                        mTotalGarbageSize += emptyFolder.getFileSize();
                        mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_GARBAGE_SIZE,
                                RefreshGarbageSizeEvent.create());
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mEmptyFoldersScanned = true;
            checkScanningStatus();
        }
    }

    private class CleanUpTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_START_CLEANUP_GARBAGE,
                    OnStartCleanupGarbageEvent.create());
        }

        @Override
        protected Void doInBackground(Void... params) {
            Map<String, CacheModel> cacheMaps = mDataManager.getAdviseDeleteCacheMaps();
            Map<String, AdModel> adMaps = mDataManager.getAdviseDeleteAdMaps();
            Map<String, ApkModel> apkMaps = mDataManager.getAdviseDeleteApkMaps();
            Map<String, ResidualModel> residualMaps = mDataManager.getAdviseDeleteResidualMaps();

            final long trashCache = mDataManager.getAdviseDeleteCacheSize();
            final long trashAd = mDataManager.getAdviseDeleteAdSize();
            final long trashApk = mDataManager.getAdviseDeleteApkSize();
            final long trashResidual = mDataManager.getAdviseDeleteResidualSize();
            final int cacheCount = cacheMaps.size();
            final int adCount = adMaps.size();
            final int apkCount = apkMaps.size();
            final int residualCount = residualMaps.size();

            try {
                // ad
                Set<String> adKeys = adMaps.keySet();
                for (String key : adKeys) {
                    AdModel model = adMaps.get(key);
                    if (mForceStopped) {
                        return null;
                    }
                    String file = model.getDirectoryPath();
                    AidlProxyHelper.getInstance().deleteDirectory(file);
                    mDataManager.removeAdModel(file);

                    mEventHandler.sendEventMessage(EventType.EVENT_ON_CLEANUP_GARBAGE_ITEM,
                            OnCleanupGarbageItemEvent.create(model.getName()));
                }

                if (adCount > 0) {
                    AnalyticsUtil.track(MainActivity.this, AnalyticsUtil.TRACK_ID_TRASH_AD_SIZE,
                            trashAd);
                    AnalyticsUtil.track(MainActivity.this, AnalyticsUtil.TRACK_ID_TRASH_AD_COUNT,
                            adCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // apk
                Set<String> apkKeys = apkMaps.keySet();
                for (String key : apkKeys) {
                    ApkModel model = apkMaps.get(key);
                    if (mForceStopped) {
                        return null;
                    }
                    String file = model.getAbsolutePath();
                    AidlProxyHelper.getInstance().deleteDirectory(file);
                    mDataManager.removeApkModel(file);

                    mEventHandler.sendEventMessage(EventType.EVENT_ON_CLEANUP_GARBAGE_ITEM,
                            OnCleanupGarbageItemEvent.create(model.getApplicationLabel()));

                    if (apkCount > 0) {
                        AnalyticsUtil.track(MainActivity.this,
                                AnalyticsUtil.TRACK_ID_TRASH_APK_SIZE,
                                trashApk);
                        AnalyticsUtil
                                .track(MainActivity.this, AnalyticsUtil.TRACK_ID_TRASH_APK_COUNT,
                                        apkCount);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // cache
                Set<String> cacheKeys = cacheMaps.keySet();
                for (String key : cacheKeys) {
                    CacheModel model = cacheMaps.get(key);
                    if (mForceStopped) {
                        return null;
                    }
                    if (TextUtils.equals(ApkIconHelper.PKG_SYSTEM_CACHE, model.getPackageName())) {
                        // system cache
                        CacheUtils.clearSystemCache(getApplicationContext());
                    } else if (TextUtils.equals(ApkIconHelper.PKG_EMPTY_FOLDER,
                            model.getPackageName())) {
                        // empty folder
                        List<String> emptyFolderPaths = mDataManager.getEmptyFolderPaths();
                        for (String folder : emptyFolderPaths) {
                            if (mForceStopped) {
                                return null;
                            }
                            AidlProxyHelper.getInstance().deleteDirectory(folder);
                        }
                    } else {
                        String file = model.getDirectoryPath();
                        AidlProxyHelper.getInstance().deleteDirectory(file);
                    }
                    mDataManager.removeCacheModel(model.getDirectoryPath());

                    mEventHandler.sendEventMessage(EventType.EVENT_ON_CLEANUP_GARBAGE_ITEM,
                            OnCleanupGarbageItemEvent.create(model.getCacheType()));
                }
                if (cacheCount > 0) {
                    AnalyticsUtil.track(MainActivity.this, AnalyticsUtil.TRACK_ID_TRASH_CACHE_SIZE,
                            trashCache);
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_TRASH_CACHE_COUNT,
                            cacheCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // residual
                Set<String> residualKeys = residualMaps.keySet();
                for (String key : residualKeys) {
                    ResidualModel model = residualMaps.get(key);
                    if (mForceStopped) {
                        return null;
                    }
                    String file = model.getDirectoryPath();
                    AidlProxyHelper.getInstance().deleteDirectory(file);
                    mDataManager.removeResidualModel(file);

                    mEventHandler.sendEventMessage(EventType.EVENT_ON_CLEANUP_GARBAGE_ITEM,
                            OnCleanupGarbageItemEvent.create(model.getDescName()));
                }
                if (residualCount > 0) {
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_TRASH_RESIDUAL_SIZE,
                            trashResidual);
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_TRASH_RESIDUAL_COUNT,
                            residualCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (adCount + apkCount + cacheCount + residualCount > 0) {
                AnalyticsUtil.track(MainActivity.this, AnalyticsUtil.TRACK_ID_TOTAL_TRASH_SIZE,
                        (trashAd + trashApk + trashCache + trashResidual));
                AnalyticsUtil.track(MainActivity.this, AnalyticsUtil.TRACK_ID_TOTAL_TRASH_COUNT,
                        (adCount + apkCount + cacheCount + residualCount));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_CLEANUP_GARBAGE,
                    OnFinishCleanupGarbageEvent.create());
            MediaScannerUtil.scanWholeExternalStorage(MainActivity.this);
        }
    }

    private class LoadWhiteListDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mDataManager.clearData();
            mTotalGarbageSize = 0;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            mWhiteListManager.loadCacheWhiteList();
            mWhiteListManager.loadAdWhiteList();
            mWhiteListManager.loadApkWhiteList();
            mWhiteListManager.loadResidualWhiteList();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!mForceStopped) {
                // 空文件夹
                new LoadEmptyFoldersTask().execute();
                // 系统缓存
                new ScanSystemCacheThread(MainActivity.this, mSystemCacheCallback).start();
                // 缓存垃圾
                new ScanCachesThread(MainActivity.this, mIKSCleaner, mCacheCallback).start();
                // 广告
                new ScanADsThread(MainActivity.this, mIKSCleaner, mAdCallback).start();
                // 卸载残留
                new ScanResidualsThread(MainActivity.this, mIKSCleaner, mResidualCallback)
                        .start();
                // 安装包
                new ScanAPKsThread(mIFileProxy, mApkCallback).start();
            }
        }
    }

    private EventHandler mEventHandler = new EventHandler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EventType.EVENT_ON_ACTION_BUTTON_CLICK:
                    onActionButtonClick((OnActionButtonClickEvent) msg.obj);
                    break;
                case EventType.EVENT_REFRESH_GARBAGE_SIZE:
                    refreshGarbageSize((RefreshGarbageSizeEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_START_SCAN_CACHE:
                    onStartScanCache((OnStartScanCacheEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_SCANNING_ITEM:
                    onScanningItem((OnScanningItemEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_SCAN_CACHE:
                    onFinishScanCache((OnFinishScanCacheEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_START_SCAN_AD:
                    onStartScanAd((OnStartScanAdEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_SCAN_AD:
                    onFinishScanAd((OnFinishScanAdEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_START_SCAN_APK:
                    onStartScanApk((OnStartScanApkEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_SCAN_APK:
                    onFinishScanApk((OnFinishScanApkEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_START_SCAN_RESIDUAL:
                    onStartScanResidual((OnStartScanResidualEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_SCAN_RESIDUAL:
                    onFinishScanResidual((OnFinishScanResidualEvent) msg.obj);
                    break;
                case EventType.EVENT_REFRESH_FREE_MEMORY:
                    refreshFreeMemory((RefreshFreeMemoryEvent) msg.obj);
                    break;
                case EventType.EVENT_SET_DEEP_CLEAN_VISIBILITY:
                    setDeepCleanVisibility((SetDeepCleanVisibilityEvent) msg.obj);
                    break;
                case EventType.EVENT_SET_CONTENT_SUMMARY_VISIBILITY:
                    setContentSummaryVisibility((SetContentSummaryVisibilityEvent) msg.obj);
                    break;
                case EventType.EVENT_SET_CONTENT_TITLE:
                    setContentTitle((SetContentTitleEvent) msg.obj);
                    break;
                case EventType.EVENT_HANDLE_EMPTY_LIST:
                    handleGarbageEmpty((HandleEmptyListEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_SCAN_GARBAGE:
                    onFinishScanGarbage((OnFinishScanGarbageEvent) msg.obj);
                    break;
                case EventType.EVENT_SET_HANDLE_ITEM_CONTENT:
                    setHandleItemContent((SetHandleItemContentEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_START_CLEANUP_GARBAGE:
                    onStartCleanupGarbage((OnStartCleanupGarbageEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_CLEANUP_GARBAGE_ITEM:
                    onCleanupGarbageItem((OnCleanupGarbageItemEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_CLEANUP_GARBAGE:
                    onFinishCleanupGarbage((OnFinishCleanupGarbageEvent) msg.obj);
                    break;
                case EventType.EVENT_VIEW_SETTINGS:
                    viewSettings((ViewSettingsEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_garbage_HANDLE_ITEM_CLICK:
                    onHandleItemClick((OnGarbageHandleItemClickEvent) msg.obj);
                    break;
                case EventType.EVENT_VIEW_DEEPCLEAN:
                    viewDeepClean((ViewDeepCleanEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_BACK_PRESSED:
                    onBackPressed((OnBackPressedEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        private void onBackPressed(OnBackPressedEvent event) {
            finish();
        }

        private void viewDeepClean(ViewDeepCleanEvent event) {
            startActivity(new Intent(MainActivity.this, DeepCleanActivity.class));
            AnalyticsUtil.track(MainActivity.this,
                    AnalyticsUtil.TRACK_ID_CLEANER_OPERATION_DEEP_CLICKED);
        }

        private void onHandleItemClick(OnGarbageHandleItemClickEvent event) {
            switch (event.getHandleItem()) {
                case CACHE:
                    startActivity(new Intent(MainActivity.this, CacheActivity.class));
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_CLEANER_ENTER_CACHE);
                    break;
                case AD:
                    startActivity(new Intent(MainActivity.this, AdActivity.class));
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_CLEANER_ENTER_AD);
                    break;
                case APK:
                    startActivity(new Intent(MainActivity.this, ApkActivity.class));
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_CLEANER_ENTER_APK);
                    break;
                case RESIDUAL:
                    startActivity(new Intent(MainActivity.this, ResidualActivity.class));
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_CLEANER_ENTER_UNINSTALL);
                    break;
                default:
                    break;
            }
        }

        private void viewSettings(ViewSettingsEvent event) {
            startActivity(new Intent(MiuiIntent.ACTION_GARBAGE_CLEANUP_SETTINGS));
            AnalyticsUtil.track(MainActivity.this,
                    AnalyticsUtil.TRACK_ID_ENTER_GARBAGE_CLEANUP_SETTINGS);
        }

        private void onFinishCleanupGarbage(OnFinishCleanupGarbageEvent event) {
            Preferences.setLatestGarbageCleanupDate(System.currentTimeMillis());
            Preferences.setScannedGarbageSize(0);
            String hints = getString(R.string.hints_finish_quick_cleanup);
            AndroidUtils.showShortToast(MainActivity.this, hints);
            mCleanStatus = GarbageCleanStatus.CLEANED;

            mMainView.setContentTitleText(getString(R.string.hints_finish_quick_cleanup_title,
                    AndroidUtils.getFormatMaxM(mTotalGarbageSize)));
            mMainView.setActionButtonEnabled(true);
            mMainView.setActionButtonText(getString(R.string.btn_text_done));
            mMainView.setHandleItemContextText(HandleItem.CACHE, hints);
            mMainView.setHandleItemContextText(HandleItem.AD, hints);
            mMainView.setHandleItemContextText(HandleItem.APK, hints);
            mMainView.setHandleItemContextText(HandleItem.RESIDUAL, hints);
            mMainView.setDeepCleanButtonVisibility(View.VISIBLE);
            mMainView.setContentSummaryVisibility(View.GONE);
            mMainView.setUnitSuffixVisibility(View.VISIBLE);

            mTotalGarbageSize = 0;
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_FREE_MEMORY,
                    RefreshFreeMemoryEvent.create());
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_GARBAGE_SIZE,
                    RefreshGarbageSizeEvent.create());
        }

        private void onCleanupGarbageItem(OnCleanupGarbageItemEvent event) {
            mMainView.setContentSummaryText(event.getDescx());
        }

        private void onStartCleanupGarbage(OnStartCleanupGarbageEvent event) {
            mCleanStatus = GarbageCleanStatus.CLEANING;

            String hints = getString(R.string.hints_quick_cleanning);
            mMainView.setContentTitleText(hints);
            mMainView.setActionButtonEnabled(false);
            mMainView.setActionButtonText(getString(R.string.btn_text_stop));
            mMainView.setHandleItemContextText(HandleItem.CACHE, hints);
            mMainView.setHandleItemContextText(HandleItem.AD, hints);
            mMainView.setHandleItemContextText(HandleItem.APK, hints);
            mMainView.setHandleItemContextText(HandleItem.RESIDUAL, hints);
            mMainView.setDeepCleanButtonVisibility(View.GONE);
            mMainView.setContentSummaryVisibility(View.VISIBLE);
        }

        private void setHandleItemContent(SetHandleItemContentEvent event) {
            mMainView.setHandleItemContextText(event.getHandleItem(), event.getContent());
        }

        private void onFinishScanGarbage(OnFinishScanGarbageEvent event) {
            AndroidUtils.showShortToast(MainActivity.this, R.string.hints_finish_quick_scan);
            Preferences.setScannedGarbageSize(mTotalGarbageSize);
            mMainView.setActionButtonText(getString(R.string.btn_text_quick_cleanup));
            mMainView.setDeepCleanButtonVisibility(View.VISIBLE);
            mCleanStatus = GarbageCleanStatus.SCANNED;

            refreshGarbageSize(RefreshGarbageSizeEvent.create());
        }

        private void handleGarbageEmpty(HandleEmptyListEvent event) {
            AndroidUtils.showShortToast(MainActivity.this, R.string.toast_garbage_list_empty);
            Preferences.setLatestGarbageCleanupDate(System.currentTimeMillis());
            mCleanStatus = GarbageCleanStatus.CLEANED;

            mMainView.setActionButtonEnabled(true);
            mMainView.setActionButtonText(getString(R.string.btn_text_done));
            mMainView.setDeepCleanButtonVisibility(View.VISIBLE);
            mMainView.setContentSummaryVisibility(View.GONE);
            mMainView.setUnitSuffixVisibility(View.VISIBLE);

            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_FREE_MEMORY,
                    RefreshFreeMemoryEvent.create());
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_GARBAGE_SIZE,
                    RefreshGarbageSizeEvent.create());
        }

        private void setContentTitle(SetContentTitleEvent event) {
            mMainView.setContentTitleText(event.getTitle());
        }

        private void setContentSummaryVisibility(SetContentSummaryVisibilityEvent event) {
            mMainView.setContentSummaryVisibility(event.getVisibility());
        }

        private void setDeepCleanVisibility(SetDeepCleanVisibilityEvent event) {
            mMainView.setDeepCleanButtonVisibility(event.getVisibility());
        }

        private void refreshFreeMemory(RefreshFreeMemoryEvent event) {
            try {
                StorageInfo externalInfo = mIFileProxy.getTotalExternalStroageInfo();
                long totalSize = externalInfo.total;
                long availableSize = externalInfo.free;
                if (!mIFileProxy.isInternalAndExternalStorageSame()) {
                    StorageInfo internalInfo = mIFileProxy.getInternalStroageInfo();
                    totalSize += internalInfo.total;
                    availableSize += internalInfo.free;
                }

                mMainView.setContentTitleText(getString(R.string.hints_storage_free_size,
                        ExtraTextUtils.formatFileSize(MainActivity.this, availableSize)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void onFinishScanResidual(OnFinishScanResidualEvent event) {
            refreshHandleItemContent(HandleItem.RESIDUAL);
            mMainView.setHandleItemEnabled(HandleItem.RESIDUAL, true);
        }

        private void onStartScanResidual(OnStartScanResidualEvent event) {
            mMainView.setHandleItemContextText(HandleItem.RESIDUAL,
                    getString(R.string.hints_quick_scanning));
            mMainView.setHandleItemEnabled(HandleItem.RESIDUAL, false);
        }

        private void onFinishScanApk(OnFinishScanApkEvent event) {
            refreshHandleItemContent(HandleItem.APK);
            mMainView.setHandleItemEnabled(HandleItem.APK, true);
        }

        private void onStartScanApk(OnStartScanApkEvent event) {
            mMainView.setHandleItemContextText(HandleItem.APK,
                    getString(R.string.hints_quick_scanning));
            mMainView.setHandleItemEnabled(HandleItem.APK, false);
        }

        private void onFinishScanAd(OnFinishScanAdEvent event) {
            refreshHandleItemContent(HandleItem.AD);
            mMainView.setHandleItemEnabled(HandleItem.AD, true);
        }

        private void onStartScanAd(OnStartScanAdEvent event) {
            mMainView.setHandleItemContextText(HandleItem.AD,
                    getString(R.string.hints_quick_scanning));
            mMainView.setHandleItemEnabled(HandleItem.AD, false);
        }

        private void onFinishScanCache(OnFinishScanCacheEvent event) {
            refreshHandleItemContent(HandleItem.CACHE);
            mMainView.setHandleItemEnabled(HandleItem.CACHE, true);
        }

        private void onScanningItem(OnScanningItemEvent event) {
            mMainView.setContentSummaryText(event.getDescx());
        }

        private void onStartScanCache(OnStartScanCacheEvent event) {
            mMainView.setHandleItemContextText(HandleItem.CACHE,
                    getString(R.string.hints_quick_scanning));
            mMainView.setHandleItemEnabled(HandleItem.CACHE, false);
        }

        private void refreshGarbageSize(RefreshGarbageSizeEvent event) {
            mMainView.setGarbageSize(mTotalGarbageSize);

            if (mTotalGarbageSize >= 20 * 1000 * 1000) {
                mCurrentBackgroundStatus = BackgroundStatus.RED;
            } else if (mTotalGarbageSize >= 10 * 1000 * 1000) {
                mCurrentBackgroundStatus = BackgroundStatus.ORANGE;
            } else {
                mCurrentBackgroundStatus = BackgroundStatus.CYAN;
            }
            mMainView.updateForeground(mCurrentBackgroundStatus);
        }

        private void onActionButtonClick(OnActionButtonClickEvent event) {
            switch (mCleanStatus) {
                case NORMAL:
                    mForceStopped = false;
                    Preferences.setLastScanningCanceled(false);
                    mCleanStatus = GarbageCleanStatus.SCANNING;
                    mMainView.setActionButtonText(getString(R.string.btn_text_stop));
                    mMainView.setContentTitleText(getString(R.string.hints_quick_scanning));
                    mMainView.setDeepCleanButtonVisibility(View.GONE);
                    mMainView.setContentSummaryVisibility(View.VISIBLE);

                    mTotalGarbageSize = 0;
                    new LoadWhiteListDataTask().execute();

                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_CLEANER_OPERATION_ONE_KEY_CLICKED);
                    break;
                case SCANNING:
                    mForceStopped = true;
                    Preferences.setLastScanningCanceled(true);
                    mCleanStatus = GarbageCleanStatus.NORMAL;
                    mDataManager.clearData();
                    mMainView.setActionButtonText(getString(R.string.btn_text_quick_scan));
                    mEventHandler.sendEventMessage(EventType.EVENT_SET_CONTENT_SUMMARY_VISIBILITY,
                            SetContentSummaryVisibilityEvent.create(View.GONE));
                    mEventHandler.sendEventMessage(EventType.EVENT_SET_DEEP_CLEAN_VISIBILITY,
                            SetDeepCleanVisibilityEvent.create(View.VISIBLE));
                    refreshLatestCleanupInfo();
                    break;
                case SCANNED:
                    new CleanUpTask().execute();
                    break;
                case CLEANING:
                    // ignore
                    break;
                case CLEANED:
                    mCleanStatus = GarbageCleanStatus.NORMAL;
                    mMainView.setActionButtonText(getString(R.string.btn_text_quick_scan));
                    finish();
                    break;
                case DISABLED:
                    // ignore
                    break;
                default:
                    break;
            }
        }
    };

    private boolean mCacheScanned = false;
    private boolean mAdScanned = false;
    private boolean mApkScanned = false;
    private boolean mResidualScanned = false;
    private boolean mEmptyFoldersScanned = false;
    private boolean mSystemCacheScanned = false;

    private IKSCleaner mIKSCleaner;
    private IFileProxy mIFileProxy;
    private WhiteListManager mWhiteListManager;
    private CleanDataManager mDataManager;
    private AidlProxyHelper mProxyHelper;
    private CleanMaster mCleanMaster;

    private boolean mForceStopped = false;
    private long mTotalGarbageSize;
    private long mSystemCacheSize;

    private BackgroundStatus mCurrentBackgroundStatus = BackgroundStatus.CYAN;

    private GarbageCleanStatus mCleanStatus = GarbageCleanStatus.NORMAL;

    private MainActivityView mMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_activity_main);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_TRASH);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_MAIN);

        CheckGarbageCleanupService.cancelNotification(this);

        if (!checkCleanupService(this)) {
            showEoorDialog(this);
            AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_CLEAN_MATSER_DAMAGED);
            return;
        }

        if (!checkFileProxyService(this)) {
            showEoorDialog(this);
            AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_GUARD_PROVIDER_DAMAGED);
            return;
        }

        mForceStopped = false;

        mWhiteListManager = WhiteListManager.getInstance(this);
        mDataManager = CleanDataManager.getInstance(this);
        mProxyHelper = AidlProxyHelper.getInstance();
        mProxyHelper.bindFileProxy(this, mFileConnection);
        mProxyHelper.bindCleanProxy(this, mCleanerConnection);
        mCleanMaster = CleanMaster.getInstance(this);

        mMainView = (MainActivityView) findViewById(R.id.main_view);
        mMainView.setEventHandler(mEventHandler);

        refreshLatestCleanupInfo();
    }

    private boolean checkCleanupService(Context context) {
        Intent cleanerIntent = new Intent(CMCleanConst.ACTION_CLEAN_SERVICE);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentServices(cleanerIntent,
                PackageManager.GET_RESOLVED_FILTER);
        return infos != null && infos.size() != 0;
    }

    private boolean checkFileProxyService(Context context) {
        Intent fileIntent = new Intent(ExtraIntent.ACTION_FILE_PROXY_SERVICE);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentServices(fileIntent,
                PackageManager.GET_RESOLVED_FILTER);
        return infos != null && infos.size() != 0;
    }

    private void showEoorDialog(Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_load_cleanup_engine_error)
                .setMessage(R.string.dialog_msg_load_cleanup_engine_error)
                .setPositiveButton(R.string.ok, null)
                .create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCleanStatus == GarbageCleanStatus.SCANNING) {
            return;
        }

        if (mDataManager.hasUpdateCacheMaps()) {
            mDataManager.setHasUpdateCacheMaps(false);
            refreshHandleItemContent(HandleItem.CACHE);
            mCacheScanned = true;
            mEmptyFoldersScanned = true;
            mSystemCacheScanned = true;
        }
        if (mDataManager.hasUpdateAdMaps()) {
            mDataManager.setHasUpdateAdMaps(false);
            refreshHandleItemContent(HandleItem.AD);
            mAdScanned = true;
        }
        if (mDataManager.hasUpdateApkMaps()) {
            mDataManager.setHasUpdateApkMaps(false);
            refreshHandleItemContent(HandleItem.APK);
            mApkScanned = true;
        }
        if (mDataManager.hasUpdateResidualMaps()) {
            mDataManager.setHasUpdateResidualMaps(false);
            refreshHandleItemContent(HandleItem.RESIDUAL);
            mResidualScanned = true;
        }
        if (mCacheScanned && mAdScanned && mApkScanned && mResidualScanned) {
            checkScanningStatus();
        } else if ((mCacheScanned || mAdScanned || mApkScanned || mResidualScanned)
                && mTotalGarbageSize > 0) {

            refreshHandleItemContent(HandleItem.CACHE);
            refreshHandleItemContent(HandleItem.AD);
            refreshHandleItemContent(HandleItem.APK);
            refreshHandleItemContent(HandleItem.RESIDUAL);

            mTotalGarbageSize = mDataManager.getAdviseDeleteAdSize()
                    + mDataManager.getAdviseDeleteApkSize()
                    + mDataManager.getAdviseDeleteCacheSize()
                    + mDataManager.getAdviseDeleteResidualSize();

            refreshContentTitleByGarbageSize(mTotalGarbageSize);

            if (mTotalGarbageSize == 0) {
                mEventHandler.sendEventMessage(EventType.EVENT_HANDLE_EMPTY_LIST,
                        HandleEmptyListEvent.create());
            } else {
                mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_GARBAGE_SIZE,
                        RefreshGarbageSizeEvent.create());
            }
        }
    }

    private void refreshLatestCleanupInfo() {
        long latestCleanupDate = Preferences.getLatestGarbageCleanupDate(-1);
        if (latestCleanupDate == -1) {
            mEventHandler.sendEventMessage(EventType.EVENT_SET_CONTENT_TITLE,
                    SetContentTitleEvent
                            .create(getString(R.string.hints_latest_garbage_cleanup_never)));
        } else {
            mEventHandler.sendEventMessage(EventType.EVENT_SET_CONTENT_TITLE,
                    SetContentTitleEvent.create(DateTimeUtils.formatGarbageCleanupTime(this,
                            latestCleanupDate)));
        }
    }

    private void checkServiceConnection() {
        if (mIKSCleaner != null && mIFileProxy != null) {
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_FREE_MEMORY,
                    RefreshFreeMemoryEvent.create());

            mTotalGarbageSize = Preferences.getScannedGarbageSize();
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_GARBAGE_SIZE,
                    RefreshGarbageSizeEvent.create());
            boolean autoStartScan = getIntent().getBooleanExtra(ExtraIntent.EXTRA_AUTO_START_SCAN,
                    false);
            if (autoStartScan) {
                mMainView.performActionButtonClick();
            }
        }
    }

    private void checkScanningStatus() {
        if (mCacheScanned && mAdScanned && mApkScanned && mResidualScanned && mEmptyFoldersScanned
                && mSystemCacheScanned && !mForceStopped) {
            mCacheScanned = false;
            mAdScanned = false;
            mApkScanned = false;
            mResidualScanned = false;
            mEmptyFoldersScanned = false;
            mSystemCacheScanned = false;
            mForceStopped = false;

            Preferences.setLastScanningCanceled(false);

            mEventHandler.sendEventMessage(EventType.EVENT_SET_CONTENT_SUMMARY_VISIBILITY,
                    SetContentSummaryVisibilityEvent.create(View.GONE));
            mEventHandler.sendEventMessage(EventType.EVENT_SET_DEEP_CLEAN_VISIBILITY,
                    SetDeepCleanVisibilityEvent.create(View.VISIBLE));

            refreshHandleItemContent(HandleItem.CACHE);
            refreshHandleItemContent(HandleItem.AD);
            refreshHandleItemContent(HandleItem.APK);
            refreshHandleItemContent(HandleItem.RESIDUAL);

            mTotalGarbageSize = mDataManager.getAdviseDeleteAdSize()
                    + mDataManager.getAdviseDeleteApkSize()
                    + mDataManager.getAdviseDeleteCacheSize()
                    + mDataManager.getAdviseDeleteResidualSize();

            refreshContentTitleByGarbageSize(mTotalGarbageSize);

            if (mTotalGarbageSize == 0) {
                mEventHandler.sendEventMessage(EventType.EVENT_HANDLE_EMPTY_LIST,
                        HandleEmptyListEvent.create());
            } else {
                mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_SCAN_GARBAGE,
                        OnFinishScanGarbageEvent.create());
            }
        }
    }

    private void refreshContentTitleByGarbageSize(long garbageSize) {
        CharSequence mTitle = null;
        if (garbageSize >= 20 * 1000 * 1000) {
            mTitle = getString(R.string.hints_garbage_cleanup_result_danger);
        } else if (garbageSize >= 10 * 1000 * 1000) {
            mTitle = getString(R.string.hints_garbage_cleanup_result_risk);
        } else if (garbageSize == 0) {
            mTitle = getString(R.string.hints_garbage_cleanup_result_safe);
        } else {
            mTitle = getString(R.string.hints_garbage_cleanup_result_risk);
        }
        mEventHandler.sendEventMessage(EventType.EVENT_SET_CONTENT_TITLE,
                SetContentTitleEvent.create(mTitle));
    }

    private void refreshHandleItemContent(HandleItem item) {
        int count = 0;
        long size = 0;
        switch (item) {
            case CACHE:
                Map<String, CacheModel> cacheMap = mDataManager.getAdviseDeleteCacheMaps();
                count = cacheMap.size();
                Set<String> cacheKeys = cacheMap.keySet();
                for (String key : cacheKeys) {
                    size += cacheMap.get(key).getFileSize();
                }
                break;
            case AD:
                Map<String, AdModel> adMap = mDataManager.getAdviseDeleteAdMaps();
                count = adMap.size();
                Set<String> adKeys = adMap.keySet();
                for (String key : adKeys) {
                    size += adMap.get(key).getFileSize();
                }
                break;
            case APK:
                Map<String, ApkModel> apkMap = mDataManager.getAdviseDeleteApkMaps();
                count = apkMap.size();
                Set<String> apkKeys = apkMap.keySet();
                for (String key : apkKeys) {
                    size += apkMap.get(key).getFileSize();
                }
                break;
            case RESIDUAL:
                Map<String, ResidualModel> residualMap = mDataManager.getAdviseDeleteResidualMaps();
                count = residualMap.size();
                Set<String> residualKeys = residualMap.keySet();
                for (String key : residualKeys) {
                    size += residualMap.get(key).getFileSize();
                }
                break;
            default:
                break;
        }
        if (count == 0) {
            mEventHandler.sendEventMessage(EventType.EVENT_SET_HANDLE_ITEM_CONTENT,
                    SetHandleItemContentEvent.create(item,
                            getString(R.string.hints_garbage_handle_item_empty_content)));
        }
        else {
            mEventHandler.sendEventMessage(EventType.EVENT_SET_HANDLE_ITEM_CONTENT,
                    SetHandleItemContentEvent.create(item,
                            getString(R.string.hints_garbage_handle_item_content, count,
                                    ExtraTextUtils.formatFileSize(MainActivity.this, size))));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mProxyHelper.unbindProxy(this, mFileConnection);
            mProxyHelper.unbindProxy(this, mCleanerConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CleanDataManager.getInstance(this).clearData();
        ApkIconHelper.getInstance(this).clearCacheLaunchers();
    }
}
