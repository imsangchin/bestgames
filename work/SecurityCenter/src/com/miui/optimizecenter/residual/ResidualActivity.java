
package com.miui.optimizecenter.residual;

import miui.app.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.common.MediaScannerUtil;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.optimizecenter.CleanDataManager;
import com.miui.optimizecenter.Preferences;
import com.miui.optimizecenter.cache.CacheActivity;
import com.miui.optimizecenter.cache.CacheModel;
import com.miui.optimizecenter.event.AddToWhiteListEvent;
import com.miui.optimizecenter.event.CleanResidualEvent;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.NotifyButtonEnabledEvent;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.NotifyLoadingShownEvent;
import com.miui.optimizecenter.event.PerformItemClickEvent;
import com.miui.optimizecenter.event.ViewApkDetailsEvent;
import com.miui.optimizecenter.event.OnFinishScanAdEvent;
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.optimizecenter.event.ViewResidualDetailsEvent;
import com.miui.optimizecenter.event.OnScanningItemEvent;
import com.miui.optimizecenter.event.OnStartScanAdEvent;
import com.miui.optimizecenter.tools.FileHelper;
import com.miui.optimizecenter.tools.ScanADsThread;
import com.miui.optimizecenter.tools.ScanResidualsThread;
import com.miui.optimizecenter.whitelist.WhiteListManager;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.ExtraIntent;

import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;
import com.cleanmaster.sdk.IResidualCallback;
import com.cleanmaster.sdk.IKSCleaner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miui.text.ExtraTextUtils;

public class ResidualActivity extends Activity {

    private IResidualCallback.Stub mResidualCallback = new IResidualCallback.Stub() {

        @Override
        public void onStartScan(int nTotalScanItem) throws RemoteException {
            mIsResidualScanned = false;
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
        }

        @Override
        public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
            // TODO
            return mForceStopped;
        }

        @Override
        public void onFindResidualItem(String dirPath, String descName, boolean bAdviseDel,
                String alertInfo) throws RemoteException {
            if (!mWhiteListManager.inResidualWhiteList(dirPath)) {
                ResidualModel residual = new ResidualModel();
                residual.setDirectoryPath(dirPath);
                residual.setDescName(descName);
                residual.setAdviseDelete(bAdviseDel);
                residual.setAlertInfo(alertInfo);
                residual.setFileSize(mIKSCleaner.pathCalcSize(dirPath));
                mDataManger.addResidualModel(dirPath, residual);

                mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                        NotifyListUpdateEvent.create(false));
            }

        }

        @Override
        public void onResidualScanFinish() throws RemoteException {
            mIsResidualScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }
    };

    private class ClearResidualTask extends AsyncTask<Void, Void, Void> {
        private List<String> mPathList = new ArrayList<String>();

        public ClearResidualTask(List<String> paths) {
            mPathList.clear();
            mPathList.addAll(paths);
        }

        @Override
        protected void onPreExecute() {
            for (String path : mPathList) {
                mDataManger.removeResidualModel(path);
            }
            mResidualView.collapseAllItems(false);
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
            Toast.makeText(ResidualActivity.this, R.string.toast_garbage_cleanup_success,
                    Toast.LENGTH_SHORT).show();
            MediaScannerUtil.scanWholeExternalStorage(ResidualActivity.this);
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
                case EventType.EVENT_CLEAN_RESIDUAL_ITEM:
                    cleanResidualItem((CleanResidualEvent) msg.obj);
                    break;
                case EventType.EVENT_PERFORM_ITEM_CLICK:
                    performItemClick((PerformItemClickEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        public void performItemClick(PerformItemClickEvent event) {
            mResidualView.performItemClick(event.getView(), event.getPosition(),
                    event.getPosition());
        }

        private void cleanResidualItem(CleanResidualEvent event) {
            List<String> pathList = new ArrayList<String>();
            pathList.add(event.getData().getDirectoryPath());
            new ClearResidualTask(pathList).execute();
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
                FileHelper.openFile(ResidualActivity.this, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void addToWhiteList(AddToWhiteListEvent event) {
            ResidualModel residual = (ResidualModel) event.getData();
            mWhiteListManager
                    .insertResidualToWhiteList(residual.getDescName(), residual.getDirectoryPath(),
                            residual.getAlertInfo());
            mDataManger.removeResidualModel(residual.getDirectoryPath());
            mResidualView.collapseAllItems(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));

            Toast.makeText(ResidualActivity.this, R.string.toast_add_white_list_success,
                    Toast.LENGTH_SHORT).show();
        }

        private void cleanupListItems(CleanupListItemsEvent event) {
            List<String> pathList = new ArrayList<String>();

            Map<String, ResidualModel> residualMap = new HashMap<String, ResidualModel>(
                    mDataManger.getResidualMaps());

            Set<String> keys = residualMap.keySet();
            for (String key : keys) {
                ResidualModel residual = residualMap.get(key);
                if (residual.adviseDelete()) {
                    pathList.add(residual.getDirectoryPath());
                }
            }
            if (!pathList.isEmpty()) {
                int residualCount = pathList.size();
                final long trashResidual = mDataManger.getAdviseDeleteResidualSize();
                AnalyticsUtil.track(ResidualActivity.this, AnalyticsUtil.TRACK_ID_TRASH_RESIDUAL_SIZE,
                        trashResidual);
                AnalyticsUtil.track(ResidualActivity.this, AnalyticsUtil.TRACK_ID_TRASH_RESIDUAL_COUNT,
                        residualCount);
                new ClearResidualTask(pathList).execute();
            }
        }

        private void notifyListUpdate(NotifyListUpdateEvent event) {
            try {
                List<ResidualModel> residualList = new ArrayList<ResidualModel>();
                Map<String, ResidualModel> residualMap = new HashMap<String, ResidualModel>(
                        mDataManger.getResidualMaps());
                Set<String> keys = residualMap.keySet();
                for (String key : keys) {
                    residualList.add(residualMap.get(key));
                }
                mAdAdapter.updateData(residualList);
                mAdAdapter.notifyDataSetChanged();
                mResidualView.setCleanupButtonEnabled(!residualList.isEmpty());

                String leftText = getText(R.string.hints_residual_header_left).toString();
                long totalSize = 0;
                for (ResidualModel model : residualList) {
                    totalSize += model.getFileSize();
                }

                int color = getResources().getColor(R.color.high_light_green);
                String leftContent = String.format(leftText, residualList.size());
                String rightContent = ExtraTextUtils.formatFileSize(ResidualActivity.this,
                        totalSize);
                mResidualView.setHeaderLeftTitle(AndroidUtils.getHighLightString(leftContent,
                        color, String.valueOf(residualList.size())));
                mResidualView.setHeaderRightTitle(AndroidUtils.getHighLightString(rightContent,
                        color, rightContent));
                mResidualView.setHeaderBarShown(!residualList.isEmpty());
            } catch (Exception e) {
                // ignore
            }
        }

        private void notifyLoadingShown(NotifyLoadingShownEvent event) {
            mResidualView.setLoadingShown(event.isShown());
        }

        private void notifyButtonEnabled(NotifyButtonEnabledEvent event) {
            mResidualView.setCleanupButtonEnabled(event.isEnabled());
        }
    };

    private class LoadResidualWhiteListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mDataManger.clearResidualMaps();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mWhiteListManager.loadResidualWhiteList();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            new ScanResidualsThread(ResidualActivity.this, mIKSCleaner, mResidualCallback).start();
        }
    }

    private CleanDataManager mDataManger;
    private WhiteListManager mWhiteListManager;

    private ResidualActivityView mResidualView;
    private ResidualListAdapter mAdAdapter;

    private boolean mIsResidualScanned = false;
    private boolean mForceStopped = false;

    private IFileProxy mIFileProxy;
    private IKSCleaner mIKSCleaner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_activity_residual);

        mIFileProxy = AidlProxyHelper.getInstance().getIFileProxy();
        mIKSCleaner = AidlProxyHelper.getInstance().getIKSCleaner();

        mDataManger = CleanDataManager.getInstance(this);
        mWhiteListManager = WhiteListManager.getInstance(this);

        mResidualView = (ResidualActivityView) findViewById(R.id.residual_view);
        mResidualView.setEventHandler(mEventHandler);
        mAdAdapter = new ResidualListAdapter(mEventHandler);
        mResidualView.setResidualListAdapter(mAdAdapter);

        // 开始扫描
        if (mDataManger.getResidualMaps().isEmpty() || Preferences.isLastScanningCanceled()
                || Preferences.isLastResidualScanningCanceled()) {
            new LoadResidualWhiteListTask().execute();
        } else {
            mIsResidualScanned = true;

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
        mDataManger.setHasUpdateResidualMaps(true);
        Preferences.setLastResidualScanningCanceled(!mIsResidualScanned);
    }
}
