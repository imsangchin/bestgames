
package com.miui.securitycenter.cache;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.securitycenter.ScoreConstants;
import com.miui.securitycenter.cache.CacheCheckManager.CacheCleanupCallback;
import com.miui.securitycenter.event.CleanupListEvent;
import com.miui.securitycenter.event.EventType;
import com.miui.securitycenter.event.RefreshListEvent;

import com.miui.securitycenter.R;

public class CacheActivity extends Activity {

    private CacheCleanupCallback mCacheCleanupCallback = new CacheCleanupCallback() {

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
            mCacheCheckManager.startCleanup(mCacheCleanupCallback);
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
                case EventType.EVENT_ON_CACHE_LIST_ITEM_CHECKED:
                    onCacheListItemChecked((CacheListItemCheckedEvent) msg.obj);
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

        public void onCacheListItemChecked(CacheListItemCheckedEvent event) {
            CacheModel model = event.getCacheModel();
            model.setLocked(!event.isChecked());
            String pkgName = model.getPackageName();
            mCacheCheckManager.setAppLockState(pkgName,
                    event.isChecked() ? ScoreConstants.LOCK_STATE_UNLOCK
                            : ScoreConstants.LOCK_STATE_LOCK);
        }

        public void refreshProcessList(RefreshListEvent event) {
            loadCacheList();
        }
    };

    private CacheCheckManager mCacheCheckManager;

    private CacheActivityView mCacheView;
    private CacheListAdapter mCacheListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m_activity_cache);

        mCacheCheckManager = CacheCheckManager.getInstance(this);

        mCacheView = (CacheActivityView) findViewById(R.id.cache_view);
        mCacheView.setEventHandler(mEventHandler);

        mCacheListAdapter = new CacheListAdapter(this, mEventHandler);
        mCacheView.setCacheListAdapter(mCacheListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCacheList();
    }

    private void loadCacheList() {
        PackageManager pm = getPackageManager();
        Map<String, CacheModel> cacheMap = new HashMap<String, CacheModel>();

        List<String> cacheApps = mCacheCheckManager.getCacheApps();

        for (String pkg : cacheApps) {
            CacheModel model = new CacheModel();
            CharSequence label = null;
            try {
                label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0));
            } catch (NameNotFoundException e) {
                // ignore
            }
            model.setAppName(label == null ? pkg : label.toString());
            model.setPackageName(pkg);
            model.setCacheSize(mCacheCheckManager.getMemoryUsed(pkg));
            if (mCacheCheckManager.getAppLockState(pkg) == ScoreConstants.LOCK_STATE_LOCK) {
                model.setLocked(true);
            } else {
                model.setLocked(false);
            }
            cacheMap.put(pkg, model);
        }

        // update process list
        updateProcessList(cacheMap);
    }

    private void updateProcessList(Map<String, CacheModel> cacheList) {
        // update title bar
        updateTitleBar(cacheList);

        mCacheListAdapter.updateData(cacheList);
        mCacheListAdapter.notifyDataSetChanged();
        mCacheView.setCleanupButtonEnabled(!cacheList.isEmpty());
        mCacheView.setEmptyViewShown(cacheList.isEmpty());
    }

    private void updateTitleBar(Map<String, CacheModel> data) {
        int count = data.size();
        long totalSize = 0;

        Set<String> keys = data.keySet();
        for (String key : keys) {
            totalSize += data.get(key).getCacheSize();
        }

        String strCount = String.valueOf(count);
        String strMemory = ExtraTextUtils.formatFileSize(this, totalSize);

        String message = getText(R.string.cache_header_title).toString();
        String title = String.format(message, count, strMemory);
        int color = getResources().getColor(R.color.high_light_green);
        mCacheView.setHeaderTitle(AndroidUtils
                .getHighLightString(title, color, strCount, strMemory));
        mCacheView.setHeaderBarShown(!data.isEmpty());
    }

    public class CacheListAdapter extends BaseAdapter {

        private EventHandler mEventHandler;

        private List<CacheModel> mCacheData = new ArrayList<CacheModel>();
        private LayoutInflater mInflater;

        public CacheListAdapter(Context context, EventHandler handler) {
            mEventHandler = handler;
            mInflater = LayoutInflater.from(context);
        }

        public void updateData(Map<String, CacheModel> data) {
            mCacheData.clear();
            Set<String> keys = data.keySet();
            for (String key : keys) {
                mCacheData.add(data.get(key));
            }
        }

        @Override
        public int getCount() {
            return mCacheData.size();
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
                convertView = mInflater.inflate(R.layout.m_cache_list_item_view, null);
            }

            CacheListItemView view = (CacheListItemView) convertView;
            view.setEventHandler(mEventHandler);
            CacheModel model = mCacheData.get(position);
            view.fillData(model);
            return view;
        }
    }

}
