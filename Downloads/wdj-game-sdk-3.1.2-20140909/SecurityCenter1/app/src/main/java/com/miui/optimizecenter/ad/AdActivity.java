
package com.miui.optimizecenter.ad;

import miui.app.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.miui.common.AndroidUtils;
import com.miui.common.ApkIconHelper;
import com.miui.common.EventHandler;
import com.miui.common.MediaScannerUtil;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.optimizecenter.CleanDataManager;
import com.miui.optimizecenter.Preferences;
import com.miui.optimizecenter.event.AddToWhiteListEvent;
import com.miui.optimizecenter.event.CleanAdItemEvent;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.NotifyButtonEnabledEvent;
import com.miui.optimizecenter.event.NotifyDataSetChangedEvent;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.NotifyLoadingShownEvent;
import com.miui.optimizecenter.event.PerformItemClickEvent;
import com.miui.optimizecenter.event.ViewAdDetailsEvent;
import com.miui.optimizecenter.event.OnFinishScanAdEvent;
import com.miui.optimizecenter.event.OnScanningItemEvent;
import com.miui.optimizecenter.event.OnStartScanAdEvent;
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.optimizecenter.tools.FileHelper;
import com.miui.optimizecenter.tools.ScanADsThread;
import com.miui.optimizecenter.whitelist.WhiteListManager;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.ExtraIntent;

import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;
import com.cleanmaster.sdk.IAdDirCallback;
import com.cleanmaster.sdk.IKSCleaner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miui.text.ExtraTextUtils;

public class AdActivity extends Activity {

    private IAdDirCallback.Stub mADCallback = new IAdDirCallback.Stub() {

        @Override
        public void onStartScan(int nTotalScanItem) throws RemoteException {
            mIsAdScanned = false;
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
        public void onFindAdDir(String name, String dirPath) throws RemoteException {
            if (!mWhiteListManager.inAdWhiteList(dirPath)) {
                AdModel ad = new AdModel();
                ad.setName(name);
                ad.setDirectoryPath(dirPath);
                ad.setFileSize(mIKSCleaner.pathCalcSize(dirPath));
                ad.setAdviseDelete(true);
                mDataManger.addAdModel(dirPath, ad);

                mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                        NotifyListUpdateEvent.create(false));
            }

        }

        @Override
        public void onAdDirScanFinish() throws RemoteException {
            mIsAdScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }
    };

    private class ClearAdTask extends AsyncTask<Void, Void, Void> {
        private List<String> mPathList = new ArrayList<String>();

        public ClearAdTask(List<String> paths) {
            mPathList.clear();
            mPathList.addAll(paths);
        }

        @Override
        protected void onPreExecute() {
            for (String path : mPathList) {
                mDataManger.removeAdModel(path);
            }
            mAdView.collapseAllItems(false);
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
            Toast.makeText(AdActivity.this, R.string.toast_garbage_cleanup_success,
                    Toast.LENGTH_SHORT).show();
            MediaScannerUtil.scanWholeExternalStorage(AdActivity.this);
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
                case EventType.EVENT_CLEAN_AD_ITEM:
                    cleanAdItem((CleanAdItemEvent) msg.obj);
                    break;
                case EventType.EVENT_PERFORM_ITEM_CLICK:
                    performItemClick((PerformItemClickEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        public void performItemClick(PerformItemClickEvent event) {
            mAdView.performItemClick(event.getView(), event.getPosition(), event.getPosition());
        }

        private void cleanAdItem(CleanAdItemEvent event) {
            List<String> pathList = new ArrayList<String>();
            pathList.add(event.getData().getDirectoryPath());
            new ClearAdTask(pathList).execute();
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
                FileHelper.openFile(AdActivity.this, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void addToWhiteList(AddToWhiteListEvent event) {
            AdModel ad = (AdModel) event.getData();
            mWhiteListManager.insertAdToWhiteList(ad.getName(), ad.getDirectoryPath());
            mDataManger.removeAdModel(ad.getDirectoryPath());
            mAdView.collapseAllItems(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));

            Toast.makeText(AdActivity.this, R.string.toast_add_white_list_success,
                    Toast.LENGTH_SHORT).show();
        }

        private void cleanupListItems(CleanupListItemsEvent event) {
            List<String> pathList = new ArrayList<String>();

            Map<String, AdModel> adMap = new HashMap<String, AdModel>(
                    mDataManger.getAdMaps());
            Set<String> keys = adMap.keySet();
            for (String key : keys) {
                AdModel ad = adMap.get(key);
                if (ad.adviseDelete()) {
                    pathList.add(ad.getDirectoryPath());
                }
            }
            if (!pathList.isEmpty()) {
                int adCount = pathList.size();
                final long trashAd = mDataManger.getAdviseDeleteAdSize();
                AnalyticsUtil.track(AdActivity.this, AnalyticsUtil.TRACK_ID_TRASH_AD_SIZE, trashAd);
                AnalyticsUtil
                        .track(AdActivity.this, AnalyticsUtil.TRACK_ID_TRASH_AD_COUNT, adCount);
                new ClearAdTask(pathList).execute();
            }
        }

        private void notifyListUpdate(NotifyListUpdateEvent event) {
            try {
                List<AdModel> adList = new ArrayList<AdModel>();
                Map<String, AdModel> adMaps = new HashMap<String, AdModel>(mDataManger.getAdMaps());
                Set<String> keys = adMaps.keySet();
                for (String key : keys) {
                    adList.add(adMaps.get(key));
                }
                mAdAdapter.updateData(adList);
                mAdAdapter.notifyDataSetChanged();
                mAdView.setCleanupButtonEnabled(!adList.isEmpty());

                String leftText = getText(R.string.hints_ad_header_left).toString();
                long totalSize = 0;
                for (AdModel model : adList) {
                    totalSize += model.getFileSize();
                }

                int color = getResources().getColor(R.color.high_light_green);
                String leftContent = String.format(leftText, adList.size());
                String rightContent = ExtraTextUtils.formatFileSize(AdActivity.this, totalSize);
                mAdView.setHeaderLeftTitle(AndroidUtils.getHighLightString(leftContent,
                        color, String.valueOf(adList.size())));
                mAdView.setHeaderRightTitle(AndroidUtils.getHighLightString(rightContent, color,
                        rightContent));
                mAdView.setHeaderBarShown(!adList.isEmpty());
            } catch (Exception e) {
                // ignore
            }
        }

        private void notifyLoadingShown(NotifyLoadingShownEvent event) {
            mAdView.setLoadingShown(event.isShown());
        }

        private void notifyButtonEnabled(NotifyButtonEnabledEvent event) {
            mAdView.setCleanupButtonEnabled(event.isEnabled());
        }
    };

    private class LoadAdWhiteListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mDataManger.clearAdMaps();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mWhiteListManager.loadAdWhiteList();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            new ScanADsThread(AdActivity.this, mIKSCleaner, mADCallback).start();
        }
    }

    private CleanDataManager mDataManger;
    private WhiteListManager mWhiteListManager;

    private AdActivityView mAdView;
    private AdListAdapter mAdAdapter;

    private boolean mIsAdScanned = false;
    private boolean mForceStopped = false;

    private IFileProxy mIFileProxy;
    private IKSCleaner mIKSCleaner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_activity_ad);

        mIFileProxy = AidlProxyHelper.getInstance().getIFileProxy();
        mIKSCleaner = AidlProxyHelper.getInstance().getIKSCleaner();

        mDataManger = CleanDataManager.getInstance(this);
        mWhiteListManager = WhiteListManager.getInstance(this);

        mAdView = (AdActivityView) findViewById(R.id.ad_view);
        mAdView.setEventHandler(mEventHandler);
        mAdAdapter = new AdListAdapter(mEventHandler);
        mAdView.setAdListAdapter(mAdAdapter);

        // 开始扫描
        if (mDataManger.getAdMaps().isEmpty() || Preferences.isLastScanningCanceled()
                || Preferences.isLastAdScanningCanceled()) {
            new LoadAdWhiteListTask().execute();
        } else {
            mIsAdScanned = true;

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
        mDataManger.setHasUpdateAdMaps(true);
        Preferences.setLastAdScanningCanceled(!mIsAdScanned);
    }

}
