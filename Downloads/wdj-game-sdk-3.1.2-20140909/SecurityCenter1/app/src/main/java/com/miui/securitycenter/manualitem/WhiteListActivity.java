
package com.miui.securitycenter.manualitem;

import miui.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.miui.securitycenter.event.EventType;
import com.miui.common.EventHandler;
import com.miui.securitycenter.event.CleanupListItemsEvent;
import com.miui.securitycenter.event.NotifyButtonEnabledEvent;
import com.miui.securitycenter.event.NotifyListUpdateEvent;
import com.miui.securitycenter.event.NotifyLoadingShownEvent;
import com.miui.securitycenter.event.NotifyWhiteListHeader;
import com.miui.securitycenter.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WhiteListActivity extends Activity {

    private class LoadWhiteListTask extends AsyncTask<Void, Void, Void> {

        public LoadWhiteListTask() {
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(false));
            mWhiteList.clear();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            mWhiteList = mWhiteListManager.getItemModelWhiteList();
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_WHITE_LIST_HEADER,
                    NotifyWhiteListHeader.create(!mWhiteList.isEmpty()));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }
    }

    private WhiteListActivityView mMainView;
    private WhiteListAdapter mWhiteListAdater;
    private WhiteListManager mWhiteListManager;
    private List<WhiteListItemModel> mWhiteList;

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
                case EventType.EVENT_NOTIFY_WHITE_LIST_HEADER:
                    notifyWhiteListHeader((NotifyWhiteListHeader) msg.obj);
                default:
                    break;
            }
        }

        private void notifyWhiteListHeader(NotifyWhiteListHeader event) {
            // TODO Auto-generated method stub
            if(event.isShown()){
                String headerTitle = getString(R.string.manual_white_list_header, mWhiteList.size());
                mMainView.setHeaderVisibility(true);
                mMainView.setHeaderTitle(headerTitle, String.valueOf(mWhiteList.size()));
            }
            else {
                mMainView.setHeaderVisibility(false);
            }
        }

        private void removeFromWhiteList(CleanupListItemsEvent event) {
            // TODO Auto-generated method stub
            if (mWhiteList.isEmpty()) {
                return;
            }
            List<WhiteListItemModel> targetList = new ArrayList<WhiteListItemModel>();
            for (WhiteListItemModel item : mWhiteList) {
                if (item.isChecked()) {
                    targetList.add(item);
                }
            }
            if (targetList.isEmpty()) {
                return;
            }
            for (WhiteListItemModel target : targetList) {
                if (target.isChecked()) {
                    mWhiteListManager.deleteModelFromWhiteList(target.getType());
                }
            }
            new LoadWhiteListTask().execute();
        }

        private void notifyWhiteListUpdate(NotifyListUpdateEvent event) {
            // TODO Auto-generated method stub
            mWhiteListAdater.updateData(mWhiteList);
            mWhiteListAdater.notifyDataSetChanged();
        }

        private void notifyButtonEnabled(NotifyButtonEnabledEvent event) {
            // TODO Auto-generated method stub
            mMainView.setCleanupButtonEnabled(event.isEnabled());
        }

        private void notifyLoadingViewShown(NotifyLoadingShownEvent event) {
            // TODO Auto-generated method stub
            mMainView.setLoadingShown(event.isShown());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ma_activity_white_list);

        mWhiteList = new ArrayList<WhiteListItemModel>();
        mWhiteListManager = WhiteListManager.getInstance(getApplicationContext());

        mMainView = (WhiteListActivityView) findViewById(R.id.white_list_view);
        mMainView.setEventHandler(mEventHandler);

        mWhiteListAdater = new WhiteListAdapter(this, mEventHandler);
        mMainView.setListAdapter(mWhiteListAdater);

        new LoadWhiteListTask().execute();
    }

    private class WhiteListAdapter extends BaseAdapter {
        private EventHandler mEventHandler;
        private LayoutInflater mInflater;
        private List<WhiteListItemModel> mWhiteListData = new ArrayList<WhiteListItemModel>();
        private WhiteListItemComparator mWhiteListItemComparator = new WhiteListItemComparator();

        public WhiteListAdapter(Context context, EventHandler handler) {
            mEventHandler = handler;
            mInflater = LayoutInflater.from(context);
        }

        public void updateData(List<WhiteListItemModel> data) {
            mWhiteListData.clear();
            mWhiteListData.addAll(data);
            Collections.sort(mWhiteList, mWhiteListItemComparator);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mWhiteListData.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            WhiteListItemModel item = mWhiteListData.get(position);
            WhiteListItemView itemView = null;
            if (convertView != null) {
                itemView = (WhiteListItemView) convertView;
            }
            else {
                itemView = (WhiteListItemView) mInflater.inflate(R.layout.ma_white_list_item_view,
                        null);
            }
            itemView.setEventHandler(mEventHandler);
            itemView.fillData(item);
            return itemView;
        }

    }
    public class WhiteListItemComparator implements Comparator<WhiteListItemModel> {

        @Override
        public int compare(WhiteListItemModel lhs, WhiteListItemModel rhs) {
            // TODO Auto-generated method stub
            if(lhs.getWeight() < rhs.getWeight()) {
                return -1;
            }
            else if(lhs.getWeight() > rhs.getWeight()){
                return 1;
            }
            return 0;
        }

    }
}
