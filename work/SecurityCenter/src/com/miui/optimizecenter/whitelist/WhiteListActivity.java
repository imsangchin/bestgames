
package com.miui.optimizecenter.whitelist;

import miui.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.common.PinnedBaseAdapter;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.NotifyButtonEnabledEvent;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.NotifyLoadingShownEvent;
import com.miui.optimizecenter.whitelist.WhiteListManager.AdWhiteInfo;
import com.miui.optimizecenter.whitelist.WhiteListManager.ApkWhiteInfo;
import com.miui.optimizecenter.whitelist.WhiteListManager.CacheWhiteInfo;
import com.miui.optimizecenter.whitelist.WhiteListManager.LargeFileWhiteInfo;
import com.miui.optimizecenter.whitelist.WhiteListManager.ResidualWhiteInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.miui.securitycenter.R;

public class WhiteListActivity extends Activity {

    private class LoadWhiteListTask extends AsyncTask<Void, Void, Void> {

        private boolean mShowToast = false;

        public LoadWhiteListTask(boolean showToast) {
            mShowToast = showToast;
        }

        @Override
        protected void onPreExecute() {
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(false));
            mWhiteList.clear();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            int color = getResources().getColor(R.color.high_light_green);

            List<CacheWhiteInfo> cacheWhiteList = mWhiteListManager.getCacheWhiteList();
            if (!cacheWhiteList.isEmpty()) {

                String cacheHeaderTitle = getString(R.string.white_list_cache_header,
                        cacheWhiteList.size());

                WhiteListHeaderModel cacheHeader = new WhiteListHeaderModel();
                cacheHeader.setChecked(false);
                cacheHeader.setHeaderTitle(AndroidUtils.getHighLightString(cacheHeaderTitle, color,
                        String.valueOf(cacheWhiteList.size())));
                cacheHeader.setWhiteListType(WhiteListType.CACHE);

                for (CacheWhiteInfo cache : cacheWhiteList) {
                    List<WhiteListItemModel> items = mWhiteList.get(cacheHeader);
                    if (items == null) {
                        items = new ArrayList<WhiteListItemModel>();
                        mWhiteList.put(cacheHeader, items);
                    }
                    items.add(WhiteListItemModel.createFromCache(cache));
                }
            }

            List<AdWhiteInfo> adWhiteList = mWhiteListManager.getAdWhiteList();
            if (!adWhiteList.isEmpty()) {
                String adHeaderTitle = getString(R.string.white_list_ad_header, adWhiteList.size());

                WhiteListHeaderModel adHeader = new WhiteListHeaderModel();
                adHeader.setChecked(false);
                adHeader.setHeaderTitle(AndroidUtils.getHighLightString(adHeaderTitle, color,
                        String.valueOf(adWhiteList.size())));
                adHeader.setWhiteListType(WhiteListType.AD);

                for (AdWhiteInfo ad : adWhiteList) {
                    List<WhiteListItemModel> items = mWhiteList.get(adHeader);
                    if (items == null) {
                        items = new ArrayList<WhiteListItemModel>();
                        mWhiteList.put(adHeader, items);
                    }
                    items.add(WhiteListItemModel.createFromAd(ad));
                }
            }

            List<ApkWhiteInfo> apkWhiteList = mWhiteListManager.getApkWhiteList();
            if (!apkWhiteList.isEmpty()) {
                String apkHeaddrTitle = getString(R.string.white_list_apk_header,
                        apkWhiteList.size());

                WhiteListHeaderModel apkHeader = new WhiteListHeaderModel();
                apkHeader.setChecked(false);
                apkHeader.setHeaderTitle(AndroidUtils.getHighLightString(apkHeaddrTitle, color,
                        String.valueOf(apkWhiteList.size())));
                apkHeader.setWhiteListType(WhiteListType.APK);

                for (ApkWhiteInfo apk : apkWhiteList) {
                    List<WhiteListItemModel> items = mWhiteList.get(apkHeader);
                    if (items == null) {
                        items = new ArrayList<WhiteListItemModel>();
                        mWhiteList.put(apkHeader, items);
                    }
                    items.add(WhiteListItemModel.createFromApk(apk));
                }
            }

            List<ResidualWhiteInfo> residualWhiteList = mWhiteListManager.getResidualWhiteList();
            if (!residualWhiteList.isEmpty()) {
                String residualHeaderTitle = getString(R.string.white_list_residual_header,
                        residualWhiteList.size());

                WhiteListHeaderModel residualHeader = new WhiteListHeaderModel();
                residualHeader.setChecked(false);
                residualHeader.setHeaderTitle(AndroidUtils.getHighLightString(residualHeaderTitle,
                        color, String.valueOf(residualWhiteList.size())));
                residualHeader.setWhiteListType(WhiteListType.RESIDUAL);

                for (ResidualWhiteInfo residual : residualWhiteList) {
                    List<WhiteListItemModel> items = mWhiteList.get(residualHeader);
                    if (items == null) {
                        items = new ArrayList<WhiteListItemModel>();
                        mWhiteList.put(residualHeader, items);
                    }
                    items.add(WhiteListItemModel.createFromResidual(residual));
                }
            }

            List<LargeFileWhiteInfo> largeFileWhiteList = mWhiteListManager.getLargeFileWhiteList();
            if (!largeFileWhiteList.isEmpty()) {
                String largeFileheaderTitle = getString(R.string.white_list_large_file_header,
                        largeFileWhiteList.size());

                WhiteListHeaderModel largeFileHeader = new WhiteListHeaderModel();
                largeFileHeader.setChecked(false);
                largeFileHeader.setHeaderTitle(AndroidUtils.getHighLightString(
                        largeFileheaderTitle, color, String.valueOf(largeFileWhiteList.size())));
                largeFileHeader.setWhiteListType(WhiteListType.LARGE_FILE);

                for (LargeFileWhiteInfo largeFile : largeFileWhiteList) {
                    List<WhiteListItemModel> items = mWhiteList.get(largeFileHeader);
                    if (items == null) {
                        items = new ArrayList<WhiteListItemModel>();
                        mWhiteList.put(largeFileHeader, items);
                    }
                    items.add(WhiteListItemModel.createFromLargeFile(largeFile));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
            if (mShowToast) {
                Toast.makeText(WhiteListActivity.this,
                        R.string.toast_removed_from_white_list, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private EventHandler mEventHandler = new EventHandler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EventType.EVENT_NOTIFY_LOADING_SHOWN:
                    notifyLoadingViewShown((NotifyLoadingShownEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_BUTTON_ENABLED:
                    notifyButtonEnabled((NotifyButtonEnabledEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_LIST_UPDATE:
                    notifyWhiteListUpdate((NotifyListUpdateEvent) msg.obj);
                    break;
                case EventType.EVENT_CLEANUP_LIST_ITEMS:
                    removeFromWhiteList((CleanupListItemsEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        private void removeFromWhiteList(CleanupListItemsEvent event) {
            if (mWhiteList.isEmpty()) {
                return;
            }

            List<WhiteListItemModel> targetList = new ArrayList<WhiteListItemModel>();

            Set<WhiteListHeaderModel> headers = mWhiteList.keySet();
            for (WhiteListHeaderModel header : headers) {
                List<WhiteListItemModel> items = mWhiteList.get(header);
                for (WhiteListItemModel item : items) {
                    if (item.isChecked()) {
                        targetList.add(item);
                    }
                }
            }

            if (targetList.isEmpty()) {
                return;
            }

            for (WhiteListItemModel model : targetList) {
                if (model.isChecked()) {
                    switch (model.getWhiteListType()) {
                        case CACHE:
                            mWhiteListManager.deleteCacheFromWhiteList(model.getDirPath());
                            break;
                        case AD:
                            mWhiteListManager.deleteAdFromWhiteList(model.getDirPath());
                            break;
                        case APK:
                            mWhiteListManager.deleteApkFromWhiteList(model.getDirPath());
                            break;
                        case RESIDUAL:
                            mWhiteListManager.deleteResidualFromWhiteList(model.getDirPath());
                            break;
                        case LARGE_FILE:
                            mWhiteListManager.deleteLargeFileFromWhiteList(model.getDirPath());
                            break;
                        default:
                            break;
                    }
                }
            }
            new LoadWhiteListTask(true).execute();
        }

        private void notifyWhiteListUpdate(NotifyListUpdateEvent event) {
            mWhiteListAdater.updateData(mWhiteList);
            mWhiteListAdater.notifyDataSetChanged();
        }

        private void notifyButtonEnabled(NotifyButtonEnabledEvent event) {
            mMainView.setCleanupButtonEnabled(event.isEnabled());
        }

        private void notifyLoadingViewShown(NotifyLoadingShownEvent event) {
            mMainView.setLoadingShown(event.isShown());
        }
    };

    private Map<WhiteListHeaderModel, List<WhiteListItemModel>> mWhiteList = new HashMap<WhiteListHeaderModel, List<WhiteListItemModel>>();

    private WhiteListActivityView mMainView;
    private PinnedWhiteListAdapter mWhiteListAdater;

    private WhiteListManager mWhiteListManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_activity_white_list);

        mWhiteListManager = WhiteListManager.getInstance(getApplicationContext());

        mMainView = (WhiteListActivityView) findViewById(R.id.white_list_view);
        mMainView.setEventHandler(mEventHandler);
        mWhiteListAdater = new PinnedWhiteListAdapter(this, mEventHandler);
        mMainView.setListAdapter(mWhiteListAdater);

        new LoadWhiteListTask(false).execute();
    }

    private class PinnedWhiteListAdapter extends PinnedBaseAdapter {

        private HeaderComparator mHeaderComparator = new HeaderComparator();

        private EventHandler mEventHandler;
        private LayoutInflater mInflater;

        public PinnedWhiteListAdapter(Context context, EventHandler handler) {
            mEventHandler = handler;
            mInflater = LayoutInflater.from(context);
        }

        private Map<WhiteListHeaderModel, List<WhiteListItemModel>> mWhiteListData = new HashMap<WhiteListHeaderModel, List<WhiteListItemModel>>();

        private List<WhiteListHeaderModel> mHeaders = new ArrayList<WhiteListHeaderModel>();;

        public void updateData(Map<WhiteListHeaderModel, List<WhiteListItemModel>> data) {
            mWhiteListData.clear();
            mWhiteListData.putAll(data);

            mHeaders.clear();
            Set<WhiteListHeaderModel> keys = mWhiteListData.keySet();
            for (WhiteListHeaderModel header : keys) {
                mHeaders.add(header);
            }
            if (mHeaders.size() >= 2) {
                Collections.sort(mHeaders, mHeaderComparator);
            }
        }

        @Override
        public Object getItem(int section, int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int section, int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getSectionCount() {
            return mHeaders.size();
        }

        @Override
        public int getCountForSection(int section) {
            return mWhiteListData.get(mHeaders.get(section)).size();
        }

        @Override
        public View getItemView(int section, int position, View convertView, ViewGroup parent) {
            WhiteListItemModel itemModel = mWhiteListData.get(mHeaders.get(section)).get(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.op_white_list_item_view, null);
            }
            WhiteListItemView itemView = (WhiteListItemView) convertView;
            itemView.setEventHandler(mEventHandler);
            itemView.fillData(itemModel);
            return itemView;
        }

        @Override
        public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
            WhiteListHeaderModel headerModel = mHeaders.get(section);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.op_white_list_header_view, null);
            }
            WhiteListHeaderView headerView = (WhiteListHeaderView) convertView;
            headerView.setEventHandler(mEventHandler);
            headerView.fillData(headerModel);
            return headerView;
        }

    }
}
