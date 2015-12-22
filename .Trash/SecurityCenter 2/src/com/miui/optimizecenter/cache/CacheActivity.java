
package com.miui.optimizecenter.cache;

import miui.app.Activity;
import miui.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.miui.securitycenter.R;
import com.cleanmaster.sdk.ICacheCallback;
import com.cleanmaster.sdk.IKSCleaner;
import com.miui.common.AndroidUtils;
import com.miui.common.ApkIconHelper;
import com.miui.common.EventHandler;
import com.miui.common.MediaScannerUtil;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.optimizecenter.CleanDataManager;
import com.miui.optimizecenter.Preferences;
import com.miui.optimizecenter.cache.StateButton.State;
import com.miui.optimizecenter.cleandb.CacheEntity;
import com.miui.optimizecenter.cleandb.CleanMaster;
import com.miui.optimizecenter.event.AddToWhiteListEvent;
import com.miui.optimizecenter.event.CleanCacheItemEvent;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.ExpandListGroupEvent;
import com.miui.optimizecenter.event.ListGroupStateChangedEvent;
import com.miui.optimizecenter.event.NotifyButtonEnabledEvent;
import com.miui.optimizecenter.event.NotifyExpandListGroupsEvent;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.NotifyLoadingShownEvent;
import com.miui.optimizecenter.event.ViewCacheDetailsEvent;
import com.miui.optimizecenter.event.OnFinishScanCacheEvent;
import com.miui.optimizecenter.event.OnScanningItemEvent;
import com.miui.optimizecenter.event.OnStartScanCacheEvent;
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.optimizecenter.tools.CacheUtils;
import com.miui.optimizecenter.tools.FileHelper;
import com.miui.optimizecenter.tools.ScanCachesThread;
import com.miui.optimizecenter.tools.ScanSystemCacheCallback;
import com.miui.optimizecenter.tools.ScanSystemCacheThread;
import com.miui.optimizecenter.whitelist.InternalWhiteList;
import com.miui.optimizecenter.whitelist.WhiteListManager;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.ExtraIntent;
import com.miui.analytics.AnalyticsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miui.text.ExtraTextUtils;

public class CacheActivity extends Activity {

    private ICacheCallback.Stub mCacheCallback = new ICacheCallback.Stub() {

        // apk包个数
        @Override
        public void onStartScan(int nTotalScanItem) throws RemoteException {
            mCacheScanned = false;
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
        }

        @Override
        public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
            // ignore
            return mForceStopped;
        }

        @Override
        public void onFindCacheItem(String cacheType, String dirPath, String pkgName,
                boolean bAdviseDel, String alertInfo, String descx) throws RemoteException {
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

                cache.setFileSize(mIKSCleaner.pathCalcSize(dirPath));
                if (mDataManager.addCacheModel(dirPath, cache) && cache.adviseDelete()) {
                    mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                            NotifyListUpdateEvent.create(false));
                }
            }
        }

        @Override
        public void onCacheScanFinish() throws RemoteException {
            mCacheScanned = true;
            checkScanFinished();
        }
    };
    private ScanSystemCacheCallback mScanSystemCacheCallback = new ScanSystemCacheCallback() {

        @Override
        public void onScanStart() {
            mSystemCacheSize = 0;
            mSystemCacheScanned = false;
        }

        @Override
        public boolean onScanItem(String pkgName, PackageStats usageStats) {
            mSystemCacheSize += usageStats.cacheSize + usageStats.externalCacheSize;
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
                }
            }

            mSystemCacheScanned = true;
            checkScanFinished();
        }
    };

    private class LoadEmptyFoldersTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mEmptyFoldersScanned = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // empty folders
                if (!mWhiteListManager.inCacheWhiteList(ApkIconHelper.PKG_EMPTY_FOLDER)) {
                    List<String> emptyFolderPaths = mIFileProxy.getEmptyDirList();

                    if (emptyFolderPaths != null && !emptyFolderPaths.isEmpty()) {
                        CacheModel emptyFolder = new CacheModel();
                        emptyFolder.setFileSize(emptyFolderPaths.size() * 4 * 1000);
                        emptyFolder.setCacheType(getString(R.string.cache_title_empty_folder,
                                emptyFolderPaths.size()));
                        emptyFolder.setPackageName(ApkIconHelper.PKG_EMPTY_FOLDER);
                        emptyFolder.setDirectoryPath(ApkIconHelper.PKG_EMPTY_FOLDER);
                        emptyFolder.setAdviseDelete(true);
                        mDataManager.addCacheModel(ApkIconHelper.PKG_EMPTY_FOLDER,
                                emptyFolder);
                        mDataManager.setEmptyFolderPaths(emptyFolderPaths);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mEmptyFoldersScanned = true;
            checkScanFinished();
        }
    }

    private class ClearCacheTask extends AsyncTask<Void, Void, Void> {

        private List<String> mPathList = new ArrayList<String>();

        public ClearCacheTask(List<String> paths) {
            mPathList.clear();
            mPathList.addAll(paths);
        }

        @Override
        protected void onPreExecute() {
            for (String path : mPathList) {
                mDataManager.removeCacheModel(path);
            }

            mCacheView.collapseAllItem(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (String path : mPathList) {
                try {
                    if (TextUtils.equals(ApkIconHelper.PKG_SYSTEM_CACHE, path)) {
                        // system cache
                        CacheUtils.clearSystemCache(CacheActivity.this.getApplicationContext());
                    } else if (TextUtils.equals(ApkIconHelper.PKG_EMPTY_FOLDER, path)) {
                        // empty folder
                        List<String> emptyFolderPaths = mDataManager.getEmptyFolderPaths();
                        for (String folder : emptyFolderPaths) {
                            mIFileProxy.deleteFileByPath(folder);
                        }
                    } else {
                        mIFileProxy.deleteFileByPath(path);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(CacheActivity.this, R.string.toast_garbage_cleanup_success,
                    Toast.LENGTH_SHORT).show();
            MediaScannerUtil.scanWholeExternalStorage(CacheActivity.this);
        }
    }

    private EventHandler mEventHandler = new EventHandler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EventType.EVENT_NOTIFY_BUTTON_ENABLED:
                    notifyCleanupButtonEnabled((NotifyButtonEnabledEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_LOADING_SHOWN:
                    notifyCacheLoadingShown((NotifyLoadingShownEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_LIST_UPDATE:
                    notifyCacheListUpdate((NotifyListUpdateEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_EXPAND_LIST_GROUPS:
                    notifyExpandCacheGroups((NotifyExpandListGroupsEvent) msg.obj);
                    break;
                case EventType.EVENT_EXPAND_LIST_GROUP:
                    expandListGroup((ExpandListGroupEvent) msg.obj);
                    break;
                case EventType.EVENT_CLEAN_CACHE_ITEM:
                    cleanupCacheItem((CleanCacheItemEvent) msg.obj);
                    break;
                case EventType.EVENT_LIST_GROUP_STATE_CHANGED:
                    onListGroupChecked((ListGroupStateChangedEvent) msg.obj);
                    break;
                case EventType.EVENT_CLEANUP_LIST_ITEMS:
                    cleanupListItems((CleanupListItemsEvent) msg.obj);
                    break;
                case EventType.EVENT_ADD_TO_WHITE_LIST:
                    addToWhiteList((AddToWhiteListEvent) msg.obj);
                    break;
                case EventType.EVENT_VIEW_FILE:
                    viewFile((ViewFileEvent) msg.obj);
                    break;
                default:
                    break;
            }
        }

        private void viewFile(ViewFileEvent event) {
            try {
                if (TextUtils.equals(ApkIconHelper.PKG_SYSTEM_CACHE, event.getPath())) {
                    // system cache ignore
                } else if (TextUtils.equals(ApkIconHelper.PKG_EMPTY_FOLDER, event.getPath())) {
                    // empty folder ignore
                } else {
                    ProxyFileInfo info = mIFileProxy.getFileInfo(event.getPath());
                    if (info.isDirectory()) {
                        FileHelper.openFile(CacheActivity.this, info.getAbsolutePath());
                    } else {
                        FileHelper.openFile(CacheActivity.this, info.getParent());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void addToWhiteList(AddToWhiteListEvent event) {
            CacheModel cache = (CacheModel) event.getData();
            mWhiteListManager.insertCacheToWhiteList(cache.getCacheType(),
                    cache.getDirectoryPath(), cache.getPackageName(), cache.getAlertInfo(),
                    cache.getDescription());
            mDataManager.removeCacheModel(cache.getDirectoryPath());
            mCacheView.collapseAllItem(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));

            Toast.makeText(CacheActivity.this, R.string.toast_add_white_list_success,
                    Toast.LENGTH_SHORT).show();
        }

        private void cleanupListItems(CleanupListItemsEvent event) {
            if (!valideAction()) {
                return;
            }

            List<String> pathList = new ArrayList<String>();

            Map<String, CacheModel> cacheMaps = new HashMap<String, CacheModel>(
                    mDataManager.getCacheMaps());
            Set<String> keys = cacheMaps.keySet();
            for (String key : keys) {
                CacheModel cache = cacheMaps.get(key);
                if (cache.adviseDelete()) {
                    pathList.add(cache.getDirectoryPath());
                }
            }
            if (!pathList.isEmpty()) {
                final long trashCache = mDataManager.getAdviseDeleteCacheSize();
                int cacheCount = pathList.size();
                AnalyticsUtil.track(CacheActivity.this, AnalyticsUtil.TRACK_ID_TRASH_CACHE_SIZE,
                        trashCache);
                AnalyticsUtil.track(CacheActivity.this, AnalyticsUtil.TRACK_ID_TRASH_CACHE_COUNT,
                        cacheCount);
                new ClearCacheTask(pathList).execute();
            }
        }

        private void onListGroupChecked(ListGroupStateChangedEvent event) {
            if (event.getState() == State.CHECKED) {
                showGroupStateChangeDialog(event);
            } else {
                Map<String, CacheModel> cacheMaps = new HashMap<String, CacheModel>(
                        mDataManager.getCacheMaps());
                Set<String> keys = cacheMaps.keySet();
                for (String key : keys) {
                    CacheModel cache = cacheMaps.get(key);
                    if (TextUtils.equals(event.getPkgName(), cache.getPackageName())) {
                        cache.setAdviseDelete(event.getState() == State.CHECKED);
                    }
                }
                mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                        NotifyListUpdateEvent.create(true));
            }
        }

        private void showGroupStateChangeDialog(ListGroupStateChangedEvent event) {
            final List<CacheModel> targetCaches = new ArrayList<CacheModel>();

            Map<String, CacheModel> cacheMaps = new HashMap<String, CacheModel>(
                    mDataManager.getCacheMaps());
            Set<String> keys = cacheMaps.keySet();
            for (String key : keys) {
                CacheModel cache = cacheMaps.get(key);
                if (TextUtils.equals(event.getPkgName(), cache.getPackageName())) {
                    targetCaches.add(cache);
                }
            }

            String msg = getString(R.string.dialog_msg_check_all, event.getAppName(),
                    targetCaches.size());

            AlertDialog dialog = new AlertDialog.Builder(CacheActivity.this)
                    .setTitle(R.string.dialog_title_check_all)
                    .setMessage(msg)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (CacheModel cache : targetCaches) {
                                        cache.setAdviseDelete(true);
                                    }
                                }
                            })
                    .setNegativeButton(R.string.cancel, null)
                    .create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                            NotifyListUpdateEvent.create(true));
                }
            });
            dialog.show();
        }

        private void cleanupCacheItem(CleanCacheItemEvent event) {
            List<String> pathList = new ArrayList<String>();
            pathList.add(event.getData().getDirectoryPath());
            new ClearCacheTask(pathList).execute();
        }

        private void expandListGroup(ExpandListGroupEvent event) {
            if (!valideAction()) {
                return;
            }
            if (event.isExpand()) {
                mCacheView.expandListGroup(event.getGroupPos());
            } else {
                mCacheView.collapseListGroup(event.getGroupPos());
            }
        }

        private void notifyExpandCacheGroups(NotifyExpandListGroupsEvent event) {
            if (event.isExpand() && Preferences.isDefaultExpandCacheGroups()) {
                mCacheView.expandListAllGroups(mCacheAdater.getGroupCount());
            }
        }

        private void notifyCacheListUpdate(NotifyListUpdateEvent event) {
            try {
                Map<String, List<CacheModel>> cacheFiles = new HashMap<String, List<CacheModel>>();

                long totalSize = 0;
                Map<String, CacheModel> cacheMaps = new HashMap<String, CacheModel>(
                        mDataManager.getCacheMaps());
                Set<String> keys = cacheMaps.keySet();
                for (String key : keys) {
                    CacheModel cache = cacheMaps.get(key);
                    String pkgName = cache.getPackageName();

                    List<CacheModel> cacheList = cacheFiles.get(pkgName);
                    if (cacheList == null) {
                        cacheList = new ArrayList<CacheModel>();
                        cacheFiles.put(pkgName, cacheList);
                    }
                    cacheList.add(cache);
                    totalSize += cache.getFileSize();
                }

                mCacheAdater.updateData(cacheFiles, mComparator);
                mCacheAdater.notifyDataSetChanged();
                mCacheView.setCleanupButtonEnabled(!cacheFiles.isEmpty());

                String leftText = getString(R.string.hints_cache_header_left);
                int color = getResources().getColor(R.color.high_light_green);
                String leftContent = String.format(leftText, cacheMaps.size());
                String rightContent = ExtraTextUtils.formatFileSize(CacheActivity.this, totalSize);
                mCacheView.setHeaderLeftText(AndroidUtils.getHighLightString(leftContent,
                        color, String.valueOf(cacheMaps.size())));
                mCacheView.setHeaderRightText(AndroidUtils.getHighLightString(rightContent, color,
                        rightContent));
                mCacheView.setHeaderBarShown(!cacheFiles.isEmpty());
            } catch (Exception e) {
                // ignore
            }
        }

        private void notifyCacheLoadingShown(NotifyLoadingShownEvent event) {
            mCacheView.setLoadingShown(event.isShown());
        }

        private void notifyCleanupButtonEnabled(NotifyButtonEnabledEvent event) {
            mCacheView.setCleanupButtonEnabled(event.isEnabled());
        }
    };

    private class LoadCacheWhiteListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mDataManager.clearCacheMaps();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mWhiteListManager.loadCacheWhiteList();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // 系统缓存
            new ScanSystemCacheThread(CacheActivity.this, mScanSystemCacheCallback).start();
            // 缓存垃圾
            new ScanCachesThread(CacheActivity.this, mIKSCleaner, mCacheCallback).start();
            // 空文件夹
            new LoadEmptyFoldersTask().execute();
        }
    }

    private boolean mCacheScanned = false;
    private boolean mEmptyFoldersScanned = false;
    private boolean mSystemCacheScanned = false;
    private boolean mForceStopped = false;

    private long mSystemCacheSize;

    private CleanDataManager mDataManager;
    private WhiteListManager mWhiteListManager;
    private CleanMaster mCleanMaster;

    private IKSCleaner mIKSCleaner;
    private IFileProxy mIFileProxy;

    private CacheActivityView mCacheView;
    private CacheExpandableListAdater mCacheAdater;
    
    private CacheGroupComparator mComparator = new CacheGroupComparator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_activity_cache);
        mForceStopped = false;

        mDataManager = CleanDataManager.getInstance(this);
        mWhiteListManager = WhiteListManager.getInstance(this);
        mCleanMaster = CleanMaster.getInstance(this);

        mIKSCleaner = AidlProxyHelper.getInstance().getIKSCleaner();
        mIFileProxy = AidlProxyHelper.getInstance().getIFileProxy();

        mCacheView = (CacheActivityView) findViewById(R.id.cache_view);
        mCacheView.setEventHandler(mEventHandler);

        mCacheAdater = new CacheExpandableListAdater(mEventHandler);
        mCacheView.setCacheListAdapter(mCacheAdater);

        // start scan
        if (mDataManager.getCacheMaps().isEmpty() || Preferences.isLastScanningCanceled()
                || Preferences.isLastCacheScanningCanceled()) {
            new LoadCacheWhiteListTask().execute();
        } else {
            mSystemCacheScanned = true;
            mCacheScanned = true;
            mEmptyFoldersScanned = true;
            checkScanFinished();
        }
    }

    private void checkScanFinished() {
        if (mCacheScanned && mSystemCacheScanned && mEmptyFoldersScanned) {
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_EXPAND_LIST_GROUPS,
                    NotifyExpandListGroupsEvent.create(true));
        }
    }

    private boolean valideAction() {
        return mCacheScanned && mSystemCacheScanned && mEmptyFoldersScanned;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                NotifyListUpdateEvent.create(true));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataManager.setHasUpdateCacheMaps(true);
        Preferences.setLastCacheScanningCanceled(!valideAction());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mForceStopped = true;
    }

}
