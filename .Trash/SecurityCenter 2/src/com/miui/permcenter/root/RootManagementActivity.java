
package com.miui.permcenter.root;

import miui.app.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.lbe.security.bean.AppPermissionConfig;
import com.lbe.security.service.provider.Permissions;
import com.miui.analytics.AnalyticsUtil;
import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.NotifyLoadingShownEvent;
import com.miui.permcenter.PermissionUtils;
import com.miui.permcenter.event.EnableAppGetRootEvent;
import com.miui.permcenter.event.EventType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.miui.securitycenter.R;

public class RootManagementActivity extends Activity {

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mEnableMap.clear();
            mDisableMap.clear();
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
        }

        @Override
        protected Void doInBackground(Void... params) {

            Map<String, AppPermissionConfig> rootApps = PermissionUtils
                    .loadPermissionApps(RootManagementActivity.this, Permissions.PERM_ID_ROOT);

            Set<String> pkgNames = rootApps.keySet();
            for (String pkgName : pkgNames) {
                AppPermissionConfig config = rootApps.get(pkgName);

                RootModel model = new RootModel();
                model.setPkgName(pkgName);
                model.setAppLabel(AndroidUtils.loadAppLabel(RootManagementActivity.this, pkgName)
                        .toString());

                int permissionAction = config
                        .getEffectivePermissionConfig(Permissions.PERM_ID_ROOT);
                if (permissionAction == AppPermissionConfig.ACTION_ACCEPT) {
                    model.setRootEnabled(true);
                    mEnableMap.put(pkgName, model);
                } else {
                    model.setRootEnabled(false);
                    mDisableMap.put(pkgName, model);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));

            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }
    }

    private EventHandler mEventHandler = new EventHandler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EventType.EVENT_NOTIFY_LOADING_SHOWN:
                    notifyLoadingShown((NotifyLoadingShownEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_LIST_UPDATE:
                    notifyListUpdate((NotifyListUpdateEvent) msg.obj);
                    break;
                case EventType.EVENT_ENABLE_APP_GET_ROOT:
                    enableAppGetRoot((EnableAppGetRootEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        private void enableAppGetRoot(EnableAppGetRootEvent event) {
            String pkgName = event.getPkgName();
            boolean enable = event.isEnabled();

            if (enable) {
                Intent intent = new Intent(RootManagementActivity.this, RootApplyActivity.class);
                intent.putExtra(RootApplyActivity.EXTRA_PKGNAME, pkgName);
                startActivity(intent);
            } else {
                // app root权限(取消)打点
                String AppName = AndroidUtils.loadAppLabel(
                        RootManagementActivity.this, pkgName).toString();
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_APP_PACKAGE, pkgName);
                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_APP_NAME, AppName);
                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_NAME, Permissions.PERM_ID_ROOT + "");
                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_STATUS, 3 + "");

                AnalyticsUtil.track(RootManagementActivity.this,
                        AnalyticsUtil.TRACK_ID_PERMISSION_CHANGE, map);

                AppPermissionConfig config = PermissionUtils.loadAppPermissionConfig(
                        RootManagementActivity.this, pkgName);
                PermissionUtils.setPermissionAction(RootManagementActivity.this, config,
                        Permissions.PERM_ID_ROOT, AppPermissionConfig.ACTION_REJECT);

                RootModel disableModel = mEnableMap.get(pkgName);
                mDisableMap.put(pkgName, disableModel);
                mEnableMap.remove(pkgName);
                disableModel.setRootEnabled(false);

                mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                        NotifyListUpdateEvent.create(true));
            }

        }

        private void notifyListUpdate(NotifyListUpdateEvent event) {
            Map<RootHeaderModel, Map<String, RootModel>> rootData = new HashMap<RootHeaderModel, Map<String, RootModel>>();

            if (!mEnableMap.isEmpty()) {
                RootHeaderModel enableHeader = new RootHeaderModel();
                enableHeader.setRootHeaderType(RootHeaderType.ACCEPT);
                enableHeader.setHeaderTitle(getString(R.string.hints_get_root_enable_title,
                        mEnableMap.size()));

                rootData.put(enableHeader, mEnableMap);
            }

            if (!mDisableMap.isEmpty()) {
                RootHeaderModel disableHeader = new RootHeaderModel();
                disableHeader.setRootHeaderType(RootHeaderType.REJECT);
                disableHeader.setHeaderTitle(getString(R.string.hints_get_root_disable_title,
                        mDisableMap.size()));

                rootData.put(disableHeader, mDisableMap);
            }

            mRootListAdapter.updateData(rootData);
            mRootListAdapter.notifyDataSetChanged();
        }

        private void notifyLoadingShown(NotifyLoadingShownEvent event) {
            mRootView.setLoadingShown(event.isShown());
        }
    };

    private Map<String, RootModel> mEnableMap = new HashMap<String, RootModel>();
    private Map<String, RootModel> mDisableMap = new HashMap<String, RootModel>();

    private RootManagementView mRootView;
    private RootListAdapter mRootListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pm_activity_root_management);

        mRootView = (RootManagementView) findViewById(R.id.root_view);

        mRootListAdapter = new RootListAdapter(this, mEventHandler);
        mRootView.setRootListAdapter(mRootListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadDataTask().execute();
    }
}
