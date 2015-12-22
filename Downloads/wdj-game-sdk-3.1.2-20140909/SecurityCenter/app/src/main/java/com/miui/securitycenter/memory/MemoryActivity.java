
package com.miui.securitycenter.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miui.text.ExtraTextUtils;
import miui.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.securitycenter.ScoreConstants;
import com.miui.securitycenter.event.CleanupListEvent;
import com.miui.securitycenter.event.EventType;
import com.miui.securitycenter.event.RefreshListEvent;

import com.miui.securitycenter.R;

public class MemoryActivity extends Activity {

    private IMemoryCleanupCallback.Stub mMemoryCleanupCallback = new IMemoryCleanupCallback.Stub() {

        @Override
        public void onStartCleanup() {
            // ignore
        }

        @Override
        public boolean onCleanupItem(String descx) {
            // ignore
            return false;
        }

        @Override
        public void onFinishCleanup() {
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_LIST, RefreshListEvent.create());
        }

    };

    private class CleanupTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                mMemoryCheck.startCleanup(mMemoryCleanupCallback);
            } catch (RemoteException e) {
                // ignore
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_LIST, RefreshListEvent.create());
        }
    }

    private EventHandler mEventHandler = new EventHandler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EventType.EVENT_ON_MEMORY_LIST_ITEM_CHECKED:
                    onPocessListItemChecked((MemoryListItemCheckedEvent) msg.obj);
                    break;
                case EventType.EVENT_REFRESH_LIST:
                    refreshProcessList((RefreshListEvent) msg.obj);
                    break;
                case EventType.EVENT_CLEANUP_LIST_EVENT:
                    cleanupMemoryList((CleanupListEvent) msg.obj);
                    break;
                default:
                    break;
            }

        }

        private void cleanupMemoryList(CleanupListEvent event) {
            new CleanupTask().execute();
        }

        public void onPocessListItemChecked(MemoryListItemCheckedEvent event) {

            MemoryModel model = event.getProcessModel();
            model.setLocked(!event.isChecked());
            String pkgName = model.getPackageName();
            try {
                mMemoryCheck.setAppLockState(pkgName,
                        event.isChecked() ? ScoreConstants.LOCK_STATE_UNLOCK
                                : ScoreConstants.LOCK_STATE_LOCK);
            } catch (RemoteException e) {
                // ignore
            }
        }

        public void refreshProcessList(RefreshListEvent event) {
            loadMemoryList();
        }
    };

    private IMemoryCheck mMemoryCheck;

    private MemoryActivityView mMemoryView;
    private ProcessListAdapter mProcessListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m_activity_memory);

        mMemoryCheck = MemoryCheckManager.getInstance(this).getMemoryCheck();

        mMemoryView = (MemoryActivityView) findViewById(R.id.memory_view);
        mMemoryView.setEventHandler(mEventHandler);

        mProcessListAdapter = new ProcessListAdapter(this, mEventHandler);
        mMemoryView.setMemoryListAdapter(mProcessListAdapter);

        loadMemoryList();
    }

    private void loadMemoryList() {
        try {
            PackageManager pm = getPackageManager();
            Map<String, MemoryModel> processList = new HashMap<String, MemoryModel>();

            List<String> runningApps = mMemoryCheck.getRunningApps();

            for (String pkg : runningApps) {
                MemoryModel model = new MemoryModel();
                CharSequence label = null;
                try {
                    label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0));
                } catch (NameNotFoundException e) {
                    // ignore
                }
                model.setAppName(label == null ? pkg : label.toString());
                model.setPackageName(pkg);
                model.setMemorySize(mMemoryCheck.getMemoryUsed(pkg));
                if (mMemoryCheck.getAppLockState(pkg) == ScoreConstants.LOCK_STATE_LOCK) {
                    model.setLocked(true);
                } else {
                    model.setLocked(false);
                }
                processList.put(pkg, model);
            }

            // update process list
            updateProcessList(processList);
        } catch (RemoteException e) {
            // ignore
        }
    }

    private void updateProcessList(Map<String, MemoryModel> processList) {
        // update title bar
        updateTitleBar(processList);

        mProcessListAdapter.updateData(processList);
        mProcessListAdapter.notifyDataSetChanged();
        mMemoryView.setCleanupButtonEnabled(!processList.isEmpty());
        mMemoryView.setEmptyViewShown(processList.isEmpty());
    }

    private void updateTitleBar(Map<String, MemoryModel> data) {
        int count = data.size();
        long totalSize = 0;

        Set<String> keys = data.keySet();
        for (String key : keys) {
            totalSize += data.get(key).getMemorySize();
        }

        String strCount = String.valueOf(count);
        String strMemory = ExtraTextUtils.formatFileSize(this, totalSize);

        String message = getString(R.string.system_header_title);
        String title = String.format(message, count, strMemory);
        int color = getResources().getColor(R.color.high_light_green);
        mMemoryView.setHeaderTitle(AndroidUtils
                .getHighLightString(title, color, strCount, strMemory));
        mMemoryView.setHeaderBarShown(!data.isEmpty());
    }

    public class ProcessListAdapter extends BaseAdapter {

        private EventHandler mEventHandler;

        private List<MemoryModel> mProcessData = new ArrayList<MemoryModel>();
        private LayoutInflater mInflater;

        public ProcessListAdapter(Context context, EventHandler handler) {
            mEventHandler = handler;
            mInflater = LayoutInflater.from(context);
        }

        public void updateData(Map<String, MemoryModel> data) {
            mProcessData.clear();
            Set<String> keys = data.keySet();
            for (String key : keys) {
                mProcessData.add(data.get(key));
            }
        }

        @Override
        public int getCount() {
            return mProcessData.size();
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
                convertView = mInflater.inflate(R.layout.m_memory_list_item_view, null);
            }

            MemoryListItemView view = (MemoryListItemView) convertView;
            view.setEventHandler(mEventHandler);
            MemoryModel model = mProcessData.get(position);
            view.fillData(model);
            return view;
        }
    }

}
