
package com.miui.antivirus;

import java.util.ArrayList;
import java.util.List;

import com.miui.antivirus.VirusCheckManager.VirusCleanupCallback;
import com.miui.antivirus.event.EventType;
import com.miui.antivirus.event.OnCleanupVirusItemEvent;
import com.miui.antivirus.event.OnFinishCleanupVirusEvent;
import com.miui.antivirus.event.OnRiskListItemClickEvent;
import com.miui.antivirus.event.OnStartCleanupVirusEvent;
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

public class RiskListActivity extends Activity {

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

    private class CleanupRiskListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            List<VirusModel> riskList = new ArrayList<VirusModel>(
                    mVirusCheckManager.getRiskList());

            for (VirusModel model : riskList) {
                mVirusCheckManager.cleanupVirus(mIFileProxy, model);
                mVirusCheckManager.removeRisk(model);
            }

            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_LIST, RefreshListEvent.create());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mVirusCheckManager.clearRiskList();
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_LIST, RefreshListEvent.create());
        }
    }

    private EventHandler mEventHandler = new EventHandler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EventType.EVENT_CLEANUP_LIST_EVENT:
                    cleanupRiskList((CleanupListEvent) msg.obj);
                    break;
                case EventType.EVENT_REFRESH_LIST:
                    refreshList((RefreshListEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_RISK_LIST_ITEM_CLICK:
                    onRiskItemClick((OnRiskListItemClickEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        private void onRiskItemClick(OnRiskListItemClickEvent event) {
            Intent intent = new Intent(RiskListActivity.this, RiskDetailActivity.class);
            intent.putExtra(RiskDetailActivity.EXTRA_DATA, event.getData());
            startActivity(intent);
        }

        private void refreshList(RefreshListEvent event) {
            refreshData();
        }

        private void cleanupRiskList(CleanupListEvent event) {
            new CleanupRiskListTask().execute();
        }
    };

    private RiskListActivityView mRiskListView;
    private RiskListAdapter mRiskListAdapter;

    private VirusCheckManager mVirusCheckManager;
    private IFileProxy mIFileProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v_activity_risk_list);
        mVirusCheckManager = VirusCheckManager.getInstance(this);
        mVirusCheckManager.setNeedRefreshVirusInfo(true);

        mRiskListView = (RiskListActivityView) findViewById(R.id.risk_list_view);
        mRiskListView.setEventHandler(mEventHandler);

        mRiskListAdapter = new RiskListAdapter(this, mEventHandler);
        mRiskListView.setRiskListAdapter(mRiskListAdapter);

        AidlProxyHelper.getInstance().bindFileProxy(this, mFileConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        List<VirusModel> riskList = new ArrayList<VirusModel>(mVirusCheckManager.getRiskList());
        mRiskListAdapter.updateData(riskList);
        mRiskListAdapter.notifyDataSetChanged();

        String strVirusCount = String.valueOf(riskList.size());
        String headerTitle = getString(R.string.risk_list_header_title, riskList.size());
        int color = getResources().getColor(R.color.high_light_red);
        mRiskListView.setHeaderTitle(AndroidUtils.getHighLightString(
                headerTitle, color, strVirusCount));

        mRiskListView.setHeaderBarShown(!riskList.isEmpty());
        mRiskListView.setCleanupButtonEnabled(!riskList.isEmpty());
        mRiskListView.setEmptyViewShown(riskList.isEmpty());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIFileProxy != null) {
            AidlProxyHelper.getInstance().unbindProxy(this, mFileConnection);
        }
    }

    public class RiskListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private EventHandler mHandler;

        private List<VirusModel> mDataList = new ArrayList<VirusModel>();

        public RiskListAdapter(Context context, EventHandler handler) {
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
                convertView = mInflater.inflate(R.layout.v_risk_list_item_view, null);
            }

            RiskListItemView itemView = (RiskListItemView) convertView;
            itemView.setEventHandler(mHandler);
            itemView.fillData(mDataList.get(position));
            return convertView;
        }

    }
}
