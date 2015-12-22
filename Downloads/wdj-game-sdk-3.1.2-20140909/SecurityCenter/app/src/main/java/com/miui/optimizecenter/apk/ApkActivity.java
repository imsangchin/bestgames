
package com.miui.optimizecenter.apk;

import miui.app.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.miui.antivirus.ExtraGuardHelper;
import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.common.MediaScannerUtil;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.IFileScanCallback;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.optimizecenter.CleanDataManager;
import com.miui.optimizecenter.Preferences;
import com.miui.optimizecenter.cache.CacheActivity;
import com.miui.optimizecenter.cache.CacheModel;
import com.miui.optimizecenter.enums.ApkStatus;
import com.miui.optimizecenter.enums.SecurityStatus;
import com.miui.optimizecenter.event.AddToWhiteListEvent;
import com.miui.optimizecenter.event.CleanApkItemEvent;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.InstallApkEvent;
import com.miui.optimizecenter.event.NotifyButtonEnabledEvent;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.NotifyLoadingShownEvent;
import com.miui.optimizecenter.event.PerformItemClickEvent;
import com.miui.optimizecenter.event.ViewAdDetailsEvent;
import com.miui.optimizecenter.event.ViewApkDetailsEvent;
import com.miui.optimizecenter.event.OnFinishScanAdEvent;
import com.miui.optimizecenter.event.OnScanningItemEvent;
import com.miui.optimizecenter.event.OnStartScanAdEvent;
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.optimizecenter.tools.ApkUtils;
import com.miui.optimizecenter.tools.FileHelper;
import com.miui.optimizecenter.tools.ScanADsThread;
import com.miui.optimizecenter.tools.ScanAPKsThread;
import com.miui.optimizecenter.whitelist.WhiteListManager;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.ExtraIntent;

import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;
import com.cleanmaster.sdk.IKSCleaner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miui.provider.ExtraGuardVirusInfoEntity;
import miui.text.ExtraTextUtils;

public class ApkActivity extends Activity {
    private IFileScanCallback.Stub mApkCallback = new IFileScanCallback.Stub() {

        @Override
        public void onScanStart() {
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
            mIsApkScanned = false;
        }

        // Stop scanning when return true
        @Override
        public boolean onFindFile(ProxyFileInfo info) {
            try {
                String dirPath = info.getAbsolutePath();

                if (!mWhiteListManager.inApkWhiteList(dirPath)) {
                    ApkModel apk = new ApkModel();
                    ApkUtils.checkApkStatus(ApkActivity.this, apk, dirPath, mIFileProxy);
                    apk.setSecurityStatus(SecurityStatus.SAFE);

                    boolean adviseDelete = apk.getStatus() != ApkStatus.UNINSTALLED
                            && apk.getStatus() != ApkStatus.INSTALLED;
                    apk.setAdviseDelete(adviseDelete);
                    apk.setFileSize(mIKSCleaner.pathCalcSize(dirPath));
                    mDataManger.addApkModel(dirPath, apk);

                    mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                            NotifyListUpdateEvent.create(false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return mForceStopped;
        }

        @Override
        public void onScanFinish() {
            mIsApkScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }

        @Override
        public void onError() {
            mIsApkScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }
    };

    private class ClearApkTask extends AsyncTask<Void, Void, Void> {
        private List<String> mPathList = new ArrayList<String>();

        public ClearApkTask(List<String> paths) {
            mPathList.clear();
            mPathList.addAll(paths);
        }

        @Override
        protected void onPreExecute() {
            for (String path : mPathList) {
                mDataManger.removeApkModel(path);
            }
            mApkView.collapseAllItems(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (String path : mPathList) {
                AidlProxyHelper.getInstance().deleteDirectory(path);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(ApkActivity.this, R.string.toast_garbage_cleanup_success,
                    Toast.LENGTH_SHORT).show();
            MediaScannerUtil.scanWholeExternalStorage(ApkActivity.this);
        }
    }

    private EventHandler mEventHandler = new EventHandler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EventType.EVENT_NOTIFY_BUTTON_ENABLED:
                    notifyButtonEnabled((NotifyButtonEnabledEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_LOADING_SHOWN:
                    notifyLoadingShown((NotifyLoadingShownEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_LIST_UPDATE:
                    notifyListUpdate((NotifyListUpdateEvent) msg.obj);
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
                case EventType.EVENT_CLEAN_APK_ITEM:
                    cleanApkItem((CleanApkItemEvent) msg.obj);
                    break;
                case EventType.EVENT_INSTALL_APK:
                    installApk((InstallApkEvent) msg.obj);
                    break;
                case EventType.EVENT_PERFORM_ITEM_CLICK:
                    performItemClick((PerformItemClickEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        public void performItemClick(PerformItemClickEvent event) {
            mApkView.performItemClick(event.getView(), event.getPosition(), event.getPosition());
        }

        private void installApk(InstallApkEvent event) {
            try {
                ProxyFileInfo info = mIFileProxy.getFileInfo(event.getPath());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName("com.android.packageinstaller",
                        "com.android.packageinstaller.PackageInstallerActivity");
                intent.setDataAndType(Uri.parse(info.getFileUri()),
                        "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private void cleanApkItem(CleanApkItemEvent event) {
            List<String> pathList = new ArrayList<String>();
            pathList.add(event.getData().getAbsolutePath());
            new ClearApkTask(pathList).execute();
        }

        private void viewFile(ViewFileEvent event) {
            try {
                String path = null;
                ProxyFileInfo info = mIFileProxy.getFileInfo(event.getPath());
                if (info.isDirectory()) {
                    path = info.getAbsolutePath();
                } else {
                    path = info.getParent();
                }
                FileHelper.openFile(ApkActivity.this, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void addToWhiteList(AddToWhiteListEvent event) {
            ApkModel apk = (ApkModel) event.getData();
            mWhiteListManager
                    .insertApkToWhiteList(apk.getAbsolutePath(), apk.getApplicationLabel());
            mDataManger.removeApkModel(apk.getAbsolutePath());
            mApkView.collapseAllItems(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));

            Toast.makeText(ApkActivity.this, R.string.toast_add_white_list_success,
                    Toast.LENGTH_SHORT).show();
        }

        private void cleanupListItems(CleanupListItemsEvent event) {
            List<String> pathList = new ArrayList<String>();
            Map<String, ApkModel> apkMap = new HashMap<String, ApkModel>(
                    mDataManger.getApkMaps());
            Set<String> keys = apkMap.keySet();
            for (String key : keys) {
                ApkModel apk = apkMap.get(key);
                if (apk.adviseDelete()) {
                    pathList.add(apk.getAbsolutePath());
                }
            }
            if (!pathList.isEmpty()) {
                int apkCount = pathList.size();
                final long trashApk = mDataManger.getAdviseDeleteApkSize();
                AnalyticsUtil.track(ApkActivity.this, AnalyticsUtil.TRACK_ID_TRASH_APK_SIZE,
                        trashApk);
                AnalyticsUtil.track(ApkActivity.this, AnalyticsUtil.TRACK_ID_TRASH_APK_COUNT,
                        apkCount);
                new ClearApkTask(pathList).execute();
            }
        }

        private void notifyListUpdate(NotifyListUpdateEvent event) {
            try {
                List<ApkModel> apkList = new ArrayList<ApkModel>();
                Map<String, ApkModel> adMaps = new HashMap<String, ApkModel>(
                        mDataManger.getApkMaps());
                Set<String> keys = adMaps.keySet();
                for (String key : keys) {
                    apkList.add(adMaps.get(key));
                }

                if (event.isNeedSort() && apkList.size() >= 2) {
                    Collections.sort(apkList, mApkComparator);
                }

                mApkAdapter.updateData(apkList);
                mApkAdapter.notifyDataSetChanged();
                mApkView.setCleanupButtonEnabled(!apkList.isEmpty());

                String leftText = getText(R.string.hints_apk_header_left).toString();
                long totalSize = 0;
                for (ApkModel model : apkList) {
                    totalSize += model.getFileSize();
                }

                int color = getResources().getColor(R.color.high_light_green);
                String leftContent = String.format(leftText, apkList.size());
                String rightContent = ExtraTextUtils.formatFileSize(ApkActivity.this, totalSize);
                mApkView.setHeaderLeftTitle(AndroidUtils.getHighLightString(leftContent,
                        color, String.valueOf(apkList.size())));
                mApkView.setHeaderRightTitle(AndroidUtils.getHighLightString(rightContent, color,
                        rightContent));
                mApkView.setHeaderBarShown(!apkList.isEmpty());
            } catch (Exception e) {
                // ignore
            }
        }

        private void notifyLoadingShown(NotifyLoadingShownEvent event) {
            mApkView.setLoadingShown(event.isShown());
        }

        private void notifyButtonEnabled(NotifyButtonEnabledEvent event) {
            mApkView.setCleanupButtonEnabled(event.isEnabled());
        }
    };

    private class LoadApkWhiteListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mDataManger.clearApkMaps();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mWhiteListManager.loadApkWhiteList();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            new ScanAPKsThread(mIFileProxy, mApkCallback).start();
        }
    }

    private ApkComparator mApkComparator = new ApkComparator();

    private CleanDataManager mDataManger;
    private WhiteListManager mWhiteListManager;

    private ApkActivityView mApkView;
    private ApkListAdapter mApkAdapter;

    private boolean mIsApkScanned = false;
    private boolean mForceStopped = false;

    private IFileProxy mIFileProxy;
    private IKSCleaner mIKSCleaner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_activity_apk);

        mIFileProxy = AidlProxyHelper.getInstance().getIFileProxy();
        mIKSCleaner = AidlProxyHelper.getInstance().getIKSCleaner();

        mDataManger = CleanDataManager.getInstance(this);
        mWhiteListManager = WhiteListManager.getInstance(this);

        mApkView = (ApkActivityView) findViewById(R.id.apk_view);
        mApkView.setEventHandler(mEventHandler);
        mApkAdapter = new ApkListAdapter(mEventHandler);
        mApkView.setApkListAdapter(mApkAdapter);

        // 开始扫描
        if (mDataManger.getApkMaps().isEmpty() || Preferences.isLastScanningCanceled()
                || Preferences.isLastApkScanningCanceled()) {
            new LoadApkWhiteListTask().execute();
        } else {
            mIsApkScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                NotifyListUpdateEvent.create(true));
    }

    @Override
    public void onPause() {
        super.onPause();
        mDataManger.setHasUpdateApkMaps(true);
        Preferences.setLastApkScanningCanceled(!mIsApkScanned);
    }
}
