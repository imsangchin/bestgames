
package com.miui.optimizecenter.deepclean;

import miui.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import miui.app.ActionBar;
import miui.text.ExtraTextUtils;

import com.cleanmaster.sdk.CMCleanConst;
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
import com.miui.optimizecenter.cache.CacheActivity;
import com.miui.optimizecenter.cache.CacheActivityView;
import com.miui.optimizecenter.cache.CacheExpandableListAdater;
import com.miui.optimizecenter.cache.CacheModel;
import com.miui.optimizecenter.cache.StateButton.State;
import com.miui.optimizecenter.cache.CacheGroupComparator;
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
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.optimizecenter.tools.CacheUtils;
import com.miui.optimizecenter.tools.FileHelper;
import com.miui.optimizecenter.tools.ScanCachesThread;
import com.miui.optimizecenter.tools.ScanSystemCacheThread;
import com.miui.optimizecenter.whitelist.InternalWhiteList;
import com.miui.optimizecenter.whitelist.WhiteListManager;
import com.miui.optimizecenter.enums.CacheGroupSortType;
import com.miui.optimizecenter.enums.LargeFileSortType;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.ExtraIntent;
import com.miui.securitycenter.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CacheFragment extends BaseDeepCleanFragment {

    public static final String TAG = CacheFragment.class.getSimpleName();

    private ICacheCallback.Stub mCacheCallback = new ICacheCallback.Stub() {

        // apk包个数
        @Override
        public void onStartScan(int nTotalScanItem) throws RemoteException {
            mCacheScanned = false;
            mDataManager.clearDeepCleanCacheMaps();
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
        }

        @Override
        public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
            // ignore
            Log.d("miui", "============onScanCacheItem = desc = " + desc);
            return mForceStopped;
        }

        @Override
        public void onFindCacheItem(String cacheType, String dirPath, String pkgName,
                boolean bAdviseDel, String alertInfo, String descx) throws RemoteException {
            Log.d("miui", "===========onFindCacheItem cacheType = " + cacheType + " dirPath = "
                    + dirPath + " bAdviseDel = " + bAdviseDel + " alertInfo = " + alertInfo
                    + " descx = " + descx);

            if (!mWhiteListManager.inCacheWhiteList(dirPath)
                    && !InternalWhiteList.inInternalCacheWhiteList(dirPath)) {
                CacheModel cache = new CacheModel();
                cache.setDirectoryPath(dirPath);

                cache.setCacheType(cacheType);
                cache.setPackageName(pkgName);
                cache.setAdviseDelete(bAdviseDel);
                cache.setAlertInfo(alertInfo);
                cache.setDescription(descx);
                cache.setFileSize(mIKSCleaner.pathCalcSize(dirPath));
                mDataManager.addDeepCleanCacheModel(dirPath, cache);

                mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                        NotifyListUpdateEvent.create(false));
            }
        }

        @Override
        public void onCacheScanFinish() throws RemoteException {
            mCacheScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_EXPAND_LIST_GROUPS,
                    NotifyExpandListGroupsEvent.create(true));
        }
    };

    private class ClearCacheTask extends AsyncTask<Void, Void, Void> {

        private List<String> mPathList = new ArrayList<String>();

        public ClearCacheTask(List<String> paths) {
            mPathList.clear();
            mPathList.addAll(paths);
        }

        @Override
        protected void onPreExecute() {
            for (String path : mPathList) {
                mDataManager.removeDeepCleanCacheModel(path);
            }
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (String path : mPathList) {
                try {
                    mIFileProxy.deleteFileByPath(path);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (getActivity() != null) {
                Toast.makeText(getActivity(), R.string.toast_garbage_cleanup_success,
                        Toast.LENGTH_SHORT).show();
                MediaScannerUtil.scanWholeExternalStorage(getActivity());
            }
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
                        FileHelper.openFile(getActivity(), info.getAbsolutePath());
                    } else {
                        FileHelper.openFile(getActivity(), info.getParent());
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
            mDataManager.removeDeepCleanCacheModel(cache.getDirectoryPath());
            mCacheView.collapseAllItem(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));

            Toast.makeText(getActivity(), R.string.toast_add_white_list_success,
                    Toast.LENGTH_SHORT).show();
        }

        private void cleanupListItems(CleanupListItemsEvent event) {
            if (!mCacheScanned) {
                return;
            }

            List<String> pathList = new ArrayList<String>();

            Map<String, CacheModel> cacheMaps = new HashMap<String, CacheModel>(
                    mDataManager.getDeepCleanCacheMaps());
            Set<String> keys = cacheMaps.keySet();
            for (String key : keys) {
                CacheModel cache = cacheMaps.get(key);
                if (cache.adviseDelete()) {
                    pathList.add(cache.getDirectoryPath());
                }
            }
            if (!pathList.isEmpty()) {
                new ClearCacheTask(pathList).execute();
            }
        }

        private void onListGroupChecked(ListGroupStateChangedEvent event) {
            if (event.getState() == State.CHECKED) {
                showGroupStateChangeDialog(event);
            } else {
                Map<String, CacheModel> cacheMaps = new HashMap<String, CacheModel>(
                        mDataManager.getDeepCleanCacheMaps());
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
                    mDataManager.getDeepCleanCacheMaps());
            Set<String> keys = cacheMaps.keySet();
            for (String key : keys) {
                CacheModel cache = cacheMaps.get(key);
                if (TextUtils.equals(event.getPkgName(), cache.getPackageName())) {
                    targetCaches.add(cache);
                }
            }

            String msg = getString(R.string.dialog_msg_check_all, event.getAppName(),
                    targetCaches.size());

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
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
            if (!mCacheScanned) {
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
                        mDataManager.getDeepCleanCacheMaps());
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

                mCacheAdater.updateData(cacheFiles, mDefaultComparator);
                mCacheAdater.notifyDataSetChanged();
                mCacheView.setCleanupButtonEnabled(!cacheFiles.isEmpty());

                String leftText = getString(R.string.deepclean_cache_header_left);
                int color = getResources().getColor(R.color.high_light_green);
                String leftContent = String.format(leftText, cacheMaps.size());
                String rightContent = ExtraTextUtils.formatFileSize(getActivity(), totalSize);
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
            // mCacheView.setLoadingShown(event.isShown());
        }

        private void notifyCleanupButtonEnabled(NotifyButtonEnabledEvent event) {
            mCacheView.setCleanupButtonEnabled(event.isEnabled());
        }
    };

    private class LoadCacheWhiteListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mWhiteListManager.loadCacheWhiteList();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // 缓存垃圾
            new ScanCachesThread(getActivity(), mIKSCleaner, CMCleanConst.MASK_SCAN_ADVANCED,
                    mCacheCallback).start();
        }
    }

    private CacheGroupComparator mDefaultComparator = new CacheGroupComparator();
    private CacheGroupComparator SIZE_COMPARATOR = new CacheGroupComparator(CacheGroupSortType.SIZE);
    private CacheGroupComparator NAME_COMPARATOR = new CacheGroupComparator(CacheGroupSortType.NAME);

    private boolean mCacheScanned = false;
    private boolean mForceStopped = false;

    private boolean mIsActivityCreated = false;
    private boolean mIsServiceBinded = false;

    private WhiteListManager mWhiteListManager;
    private CleanMaster mCleanMaster;
    private CleanDataManager mDataManager;

    private IFileProxy mIFileProxy;
    private IKSCleaner mIKSCleaner;

    private CacheActivityView mCacheView;
    private CacheExpandableListAdater mCacheAdater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForceStopped = false;

        if (Preferences.getCacheGroupSortType() == CacheGroupSortType.SIZE) {
            mDefaultComparator = SIZE_COMPARATOR;
        } else {
            mDefaultComparator = NAME_COMPARATOR;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.op_activity_cache, null);
        mCacheView = (CacheActivityView) view;
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWhiteListManager = WhiteListManager.getInstance(getActivity());
        mCleanMaster = CleanMaster.getInstance(getActivity());
        mDataManager = CleanDataManager.getInstance(getActivity());

        mCacheView.setEventHandler(mEventHandler);

        mCacheAdater = new CacheExpandableListAdater(mEventHandler);
        mCacheView.setCacheListAdapter(mCacheAdater);

        mIsActivityCreated = true;
        checkSatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                NotifyListUpdateEvent.create(true));
    }

    private void checkSatus() {
        new LoadCacheWhiteListTask().execute();
    }

    @Override
    public void onServiceBinded(Context context, IKSCleaner cleaner, IFileProxy fileProxy) {
        mIKSCleaner = cleaner;
        mIFileProxy = fileProxy;

        mIsServiceBinded = true;
        checkSatus();
    }

    @Override
    public void onFragmentSortTypeSelected(Context context, int sortTypeId) {
        if (sortTypeId == 1) {
            mDefaultComparator = SIZE_COMPARATOR;
            Preferences.setCacheGroupSortType(CacheGroupSortType.SIZE);
        }
        else if (sortTypeId == 2) {
            mDefaultComparator = NAME_COMPARATOR;
            Preferences.setCacheGroupSortType(CacheGroupSortType.NAME);
        }
        mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                NotifyListUpdateEvent.create(true));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mForceStopped = true;
    }
}
