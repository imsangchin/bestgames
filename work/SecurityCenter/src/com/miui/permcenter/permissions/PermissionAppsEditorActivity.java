
package com.miui.permcenter.permissions;

import miui.app.Activity;
import miui.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.lbe.security.bean.AppPermissionConfig;
import com.lbe.security.service.provider.Permissions;
import com.miui.analytics.AnalyticsUtil;
import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.NotifyLoadingShownEvent;
import com.miui.permcenter.PermissionUtils;
import com.miui.permcenter.Preferences;
import com.miui.permcenter.event.EventType;
import com.miui.permcenter.event.OnPermAppsItemClickEvent;

import com.miui.securitycenter.R;

import java.util.HashMap;
import java.util.Map;

public class PermissionAppsEditorActivity extends Activity {

    public static final String EXTRA_PERMISSION_ID = "extra_permission_id";
    public static final String EXTRA_PERMISSION_NAME = "extra_permission_name";

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mData.clear();
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
        }

        @Override
        protected Void doInBackground(Void... params) {
            mData.putAll(PermissionUtils.loadPermissionApps(PermissionAppsEditorActivity.this,
                    mPermissionId));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
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
                case EventType.EVENT_ON_PERM_APPS_ITEM_CLICK:
                    onAppsItemClick((OnPermAppsItemClickEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        private void onAppsItemClick(OnPermAppsItemClickEvent event) {
            String pkgName = event.getPkgName();
            AppPermissionConfig config = mData.get(pkgName);
            if (mPermissionId == Permissions.PERM_ID_SYSTEMALERT) {
                showSystemAlertDialog(config);
            } else {
                showPermissionActionDialog(config);
            }
        }

        private void notifyListUpdate(NotifyListUpdateEvent event) {
            mAppsEditorListAdapter.updateData(mData);
            mAppsEditorListAdapter.setItemEnable(PermissionUtils.isAppPermissionControlOpen(getApplicationContext()));
            mAppsEditorListAdapter.notifyDataSetChanged();

            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
        }

        private void notifyLoadingShown(NotifyLoadingShownEvent event) {
            mAppsEditorView.setLoadingViewShown(event.isShown());
        }
    };

    private long mPermissionId;

    private Map<String, AppPermissionConfig> mData = new HashMap<String, AppPermissionConfig>();

    private PermissionAppsEditorView mAppsEditorView;
    private PermissionAppsEditorListAdapter mAppsEditorListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pm_activity_permission_apps);

        mAppsEditorView = (PermissionAppsEditorView) findViewById(R.id.apps_editor_view);

        mPermissionId = getIntent().getLongExtra(EXTRA_PERMISSION_ID, -1);
        if (mPermissionId == -1) {
            finish();
            return;
        }

        String permissionName = getIntent().getStringExtra(EXTRA_PERMISSION_NAME);
        setTitle(permissionName);

        mAppsEditorListAdapter = new PermissionAppsEditorListAdapter(this, mEventHandler,
                mPermissionId);
        mAppsEditorView.setAppsEditorListAdapter(mAppsEditorListAdapter);

        new LoadDataTask().execute();
    }

    private void showPermissionActionDialog(final AppPermissionConfig config) {

        final String permissionName = AppPermissionConfig.getPermissionName(mPermissionId);
        final int permissionAction = config.getEffectivePermissionConfig(mPermissionId);

        String accept = getString(R.string.permission_action_accept);
        String prompt = getString(R.string.permission_action_prompt);
        String reject = getString(R.string.permission_action_reject);

        final String[] items = {
                accept, prompt, reject
        };

        int selectItem = -1;
        switch (permissionAction) {
            case AppPermissionConfig.ACTION_ACCEPT:
                selectItem = 0;
                break;
            case AppPermissionConfig.ACTION_PROMPT:
                selectItem = 1;
                break;
            case AppPermissionConfig.ACTION_REJECT:
                selectItem = 2;
                break;
            default:
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle(permissionName)
                .setSingleChoiceItems(items, selectItem,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int action = permissionAction;
                                switch (which) {
                                    case 0:
                                        action = AppPermissionConfig.ACTION_ACCEPT;
                                        break;
                                    case 1:
                                        action = AppPermissionConfig.ACTION_PROMPT;
                                        break;
                                    case 2:
                                        action = AppPermissionConfig.ACTION_REJECT;
                                        break;
                                    default:
                                        break;
                                }

                                // app 权限设置打点(2)
                                int status = which + 1;
                                String pkgName = config.getPackageName();
                                String AppName = AndroidUtils.loadAppLabel(
                                        PermissionAppsEditorActivity.this, pkgName).toString();
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_APP_PACKAGE, pkgName);
                                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_APP_NAME, AppName);
                                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_NAME, mPermissionId + "");
                                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_STATUS, status + "");

                                AnalyticsUtil.track(PermissionAppsEditorActivity.this,
                                        AnalyticsUtil.TRACK_ID_PERMISSION_CHANGE, map);

                                PermissionUtils.setPermissionAction(
                                        PermissionAppsEditorActivity.this,
                                        config, mPermissionId, action);

                                dialog.dismiss();
                                mAppsEditorListAdapter.notifyDataSetChanged();
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showSystemAlertDialog(final AppPermissionConfig config) {

        final String permissionName = AppPermissionConfig.getPermissionName(mPermissionId);
        final int permissionAction = config.getEffectivePermissionConfig(mPermissionId);

        String accept = getString(R.string.permission_action_accept);
        String reject = getString(R.string.permission_action_reject);

        final String[] items = {
                accept, reject
        };

        int selectItem = -1;
        switch (permissionAction) {
            case AppPermissionConfig.ACTION_ACCEPT:
                selectItem = 0;
                break;
            case AppPermissionConfig.ACTION_REJECT:
                selectItem = 1;
                break;
            default:
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle(permissionName)
                .setSingleChoiceItems(items, selectItem,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int action = permissionAction;
                                switch (which) {
                                    case 0:
                                        action = AppPermissionConfig.ACTION_ACCEPT;
                                        break;
                                    case 1:
                                        action = AppPermissionConfig.ACTION_REJECT;
                                        break;
                                    default:
                                        break;
                                }

                                PermissionUtils.setPermissionAction(
                                        PermissionAppsEditorActivity.this,
                                        config, mPermissionId, action);

                                dialog.dismiss();
                                mAppsEditorListAdapter.notifyDataSetChanged();
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
