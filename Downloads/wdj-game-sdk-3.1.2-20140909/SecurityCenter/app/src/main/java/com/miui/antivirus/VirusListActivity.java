
package com.miui.antivirus;

import java.util.ArrayList;
import java.util.List;

import com.miui.antivirus.VirusCheckManager.VirusCleanupCallback;
import com.miui.antivirus.event.EventType;
import com.miui.antivirus.event.OnCleanupVirusItemEvent;
import com.miui.antivirus.event.OnFinishCleanupVirusEvent;
import com.miui.antivirus.event.OnStartCleanupVirusEvent;
import com.miui.antivirus.event.OnVirusListItemClickEvent;
import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.event.CleanupListEvent;
import com.miui.securitycenter.event.RefreshListEvent;

import miui.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.miui.guardprovider.service.IFileProxy;

import com.miui.securitycenter.R;

public class VirusListActivity extends Activity {

    private ServiceConnection mFileConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIFileProxy = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIFileProxy = IFileProxy.Stub.asInterface(service);
            AidlProxyHelper.getInstance().setIFileProxy(mIFileProxy);
        }
    };

    private class CleanupVirusListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            List<VirusModel> virusList = new ArrayList<VirusModel>(
                    mVirusCheckManager.getVirusList());

            for (VirusModel model : virusList) {
                mVirusCheckManager.cleanupVirus(mIFileProxy, model);
                mVirusCheckManager.removeVirus(model);
            }

            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_LIST, RefreshListEvent.create());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mVirusCheckManager.clearVirusList();
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_LIST, RefreshListEvent.create());
        }
    }

    private EventHandler mEventHandler = new EventHandler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EventType.EVENT_CLEANUP_LIST_EVENT:
                    cleanupVirusList((CleanupListEvent) msg.obj);
                    break;
                case EventType.EVENT_REFRESH_LIST:
                    refreshList((RefreshListEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_VIRUS_LIST_ITEM_CLICK:
                    onVirusListItemClick((OnVirusListItemClickEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        private void onVirusListItemClick(OnVirusListItemClickEvent event) {
            Intent intent = new Intent(VirusListActivity.this, VirusDetailActivity.class);
            intent.putExtra(VirusDetailActivity.EXTRA_DATA, event.getData());
            startActivity(intent);
        }

        private void refreshList(RefreshListEvent event) {
            refreshData();
        }

        private void cleanupVirusList(CleanupListEvent event) {
            new CleanupVirusListTask().execute();
        }
    };

    private VirusListActivityView mVirusListView;
    private VirusListAdapter mVirusListAdapter;

    private VirusCheckManager mVirusCheckManager;
    private IFileProxy mIFileProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v_activity_virus_list);
        mVirusCheckManager = VirusCheckManager.getInstance(this);
        mVirusCheckManager.setNeedRefreshVirusInfo(true);

        mVirusListView = (VirusListActivityView) findViewById(R.id.virus_list_view);
        mVirusListView.setEventHandler(mEventHandler);

        mVirusListAdapter = new VirusListAdapter(this, mEventHandler);
        mVirusListView.setVirusListAdapter(mVirusListAdapter);

        AidlProxyHelper.getInstance().bindFileProxy(this, mFileConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        List<VirusModel> virusList = new ArrayList<VirusModel>(mVirusCheckManager.getVirusList());
        mVirusListAdapter.updateData(virusList);
        mVirusListAdapter.notifyDataSetChanged();

        String strVirusCount = String.valueOf(virusList.size());
        String headerTitle = getString(R.string.virus_list_header_title, virusList.size());
        int color = getResources().getColor(R.color.high_light_red);
        mVirusListView.setHeaderTitle(AndroidUtils
                .getHighLightString(headerTitle, color, strVirusCount));

        mVirusListView.setHeaderBarShown(!virusList.isEmpty());
        mVirusListView.setCleanupButtonEnabled(!virusList.isEmpty());
        mVirusListView.setEmptyViewShown(virusList.isEmpty());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIFileProxy != null) {
            AidlProxyHelper.getInstance().unbindProxy(this, mFileConnection);
        }
    }

    public class VirusListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private EventHandler mHandler;

        private List<VirusModel> mDataList = new ArrayList<VirusModel>();

        public VirusListAdapter(Context context, EventHandler handler) {
            mInflater = LayoutInflater.from(context);
            mHandler = handler;
        }

        public void updateData(List<VirusModel> data) {
            mDataList.clear();
            mDataList.addAll(data);
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.v_virus_list_item_view, null);
            }

            VirusListItemView itemView = (VirusListItemView) convertView;
            itemView.setEventHandler(mHandler);
            itemView.fillData(mDataList.get(position));
            return convertView;
        }

    }
}
