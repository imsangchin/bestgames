
package com.miui.permcenter.autostart;

import miui.app.Activity;
import miui.app.ActionBar;
import miui.app.AlertDialog;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.miui.AppOpsUtils;

import com.lbe.security.service.provider.Permissions;
import com.miui.analytics.AnalyticsUtil;
import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.NotifyLoadingShownEvent;
import com.miui.permcenter.Preferences;
import com.miui.permcenter.event.EnableAppAutoStartEvent;
import com.miui.permcenter.event.EventType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.miui.securitycenter.R;

public class AutoStartManagementActivity extends Activity {

    private class LoadingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mAutoStartMap.clear();
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
        }

        @Override
        protected Void doInBackground(Void... params) {

            PackageManager pm = getPackageManager();

            List<ApplicationInfo> appInfos = pm.getInstalledApplications(0);
            for (ApplicationInfo appInfo : appInfos) {
                if (AndroidUtils.isThirdPartApp(appInfo)) {
                    AutoStartModel model = new AutoStartModel();
                    model.setPkgName(appInfo.packageName);
                    model.setAppLabel(appInfo.loadLabel(pm).toString());
                    model.setWarningInfo(getString(R.string.hints_auto_start_warning_info));

                    model.setAutoStartEnabled(AppOpsUtils.getApplicationAutoStart(AutoStartManagementActivity.this, 
                            appInfo.packageName) == AppOpsManager.MODE_ALLOWED);
                    mAutoStartMap.put(appInfo.packageName, model);
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
                case EventType.EVENT_ENABLE_APP_AUTO_START:
                    enableAppAutoStart((EnableAppAutoStartEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        private void enableAppAutoStart(EnableAppAutoStartEvent event) {
            String pkgName = event.getPkgName();
            boolean enabled = event.isEnabled();

            // app 自启动打点
            int status = enabled ? 1 : 3;
            String AppName = AndroidUtils.loadAppLabel(AutoStartManagementActivity.this, pkgName).toString();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(AnalyticsUtil.TRACK_ID_PERMISSION_APP_PACKAGE, pkgName);
            map.put(AnalyticsUtil.TRACK_ID_PERMISSION_APP_NAME, AppName);
            map.put(AnalyticsUtil.TRACK_ID_PERMISSION_NAME, Permissions.PERM_ID_AUTOSTART + "");
            map.put(AnalyticsUtil.TRACK_ID_PERMISSION_STATUS, status + "");
            AnalyticsUtil.track(AutoStartManagementActivity.this, AnalyticsUtil.TRACK_ID_PERMISSION_CHANGE, map);

            AppOpsUtils.setApplicationAutoStart(AutoStartManagementActivity.this, pkgName, enabled);
            if (!enabled) {
                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                am.forceStopPackage(pkgName);
            }

            AutoStartModel model = mAutoStartMap.get(pkgName);
            model.setAutoStartEnabled(enabled);

            // mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
            // NotifyListUpdateEvent.create(true));

            // 只是为了刷新group header title
            Map<String, AutoStartModel> mEnableMap = new HashMap<String, AutoStartModel>();
            Map<String, AutoStartModel> mDisableMap = new HashMap<String, AutoStartModel>();

            Set<String> pkgNames = mAutoStartMap.keySet();
            for (String pkg : pkgNames) {
                AutoStartModel autoStart = mAutoStartMap.get(pkg);
                if (autoStart.isAutoStartEnabled()) {
                    mEnableMap.put(pkg, autoStart);
                } else {
                    mDisableMap.put(pkg, autoStart);
                }
            }

            mAutoStartListAdapter.setHeaderTitle(AutoStartHeaderType.ENABLED,
                    getString(R.string.hints_auto_start_enable_title, mEnableMap.size()));
            mAutoStartListAdapter.setHeaderTitle(AutoStartHeaderType.DISABLED,
                    getString(R.string.hints_auto_start_disable_title, mDisableMap.size()));
            mAutoStartListAdapter.notifyDataSetChanged();
        }

        private void notifyListUpdate(NotifyListUpdateEvent event) {
            Map<AutoStartHeaderModel, Map<String, AutoStartModel>> autoStartData = new HashMap<AutoStartHeaderModel, Map<String, AutoStartModel>>();

            Map<String, AutoStartModel> mEnableMap = new HashMap<String, AutoStartModel>();
            Map<String, AutoStartModel> mDisableMap = new HashMap<String, AutoStartModel>();

            Set<String> pkgNames = mAutoStartMap.keySet();
            for (String pkgName : pkgNames) {
                AutoStartModel model = mAutoStartMap.get(pkgName);
                if (model.isAutoStartEnabled()) {
                    mEnableMap.put(pkgName, model);
                } else {
                    mDisableMap.put(pkgName, model);
                }
            }

            if (!mEnableMap.isEmpty()) {
                AutoStartHeaderModel enableHeader = new AutoStartHeaderModel();
                enableHeader.setAutoStartHeaderType(AutoStartHeaderType.ENABLED);
                enableHeader.setHeaderTitle(getString(R.string.hints_auto_start_enable_title,
                        mEnableMap.size()));

                autoStartData.put(enableHeader, mEnableMap);
            }

            if (!mDisableMap.isEmpty()) {
                AutoStartHeaderModel disableHeader = new AutoStartHeaderModel();
                disableHeader.setAutoStartHeaderType(AutoStartHeaderType.DISABLED);
                disableHeader.setHeaderTitle(getString(R.string.hints_auto_start_disable_title,
                        mDisableMap.size()));

                autoStartData.put(disableHeader, mDisableMap);
            }

            mAutoStartListAdapter.updateData(autoStartData);
            mAutoStartListAdapter.notifyDataSetChanged();
        }

        private void notifyLoadingShown(NotifyLoadingShownEvent event) {
            mAutoStartView.setLoadingShown(event.isShown());
        }
    };

    private Map<String, AutoStartModel> mAutoStartMap = new HashMap<String, AutoStartModel>();

    private AutoStartManagementView mAutoStartView;
    private AutoStartListAdapter mAutoStartListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pm_activity_auto_start_management);

        customActionBar(getActionBar());

        mAutoStartView = (AutoStartManagementView) findViewById(R.id.auto_start_view);

        mAutoStartListAdapter = new AutoStartListAdapter(this, mEventHandler);
        mAutoStartView.setAutoStartListAdapter(mAutoStartListAdapter);

        showDeclareDialog(false);
    }

    private void customActionBar(ActionBar actionBar) {
        Button tips = new Button(this);
        tips.setBackgroundResource(R.drawable.icon_info_selector);
        tips.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showDeclareDialog(true);
            }
        });

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        ActionBar.LayoutParams alp = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        actionBar.setCustomView(tips, alp);
    }

    private void showDeclareDialog(boolean force) {
        if (!force && Preferences.hasShownAutoStartDeclare()) {
            return;
        }

        Preferences.setHasShownAutoStartDeclare(true);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_auto_start_declare)
                .setMessage(R.string.dialog_msg_auto_start_declare)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadingTask().execute();
    }
}
