
package com.miui.permcenter.permissions;

import miui.app.AlertDialog;
import miui.app.ProgressDialog;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import miui.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;

import com.miui.securitycenter.R;
import com.lbe.security.bean.AppPermissionConfig;
import com.lbe.security.service.provider.Permissions;
import com.miui.analytics.AnalyticsUtil;
import com.miui.common.AndroidUtils;
import com.miui.permcenter.PermissionUtils;
import com.miui.permcenter.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppPermissionsEditorActivity extends PreferenceActivity {

    public static final String EXTRA_PKGNAME = "extra_pkgname";

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            if (mLoadingDialog == null) {
                mLoadingDialog = ProgressDialog.show(AppPermissionsEditorActivity.this, null,
                        getResources().getString(R.string.hints_loading_text), true, false);
            }
            mData.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mData = PermissionUtils.loadGroupedAppPermissions(AppPermissionsEditorActivity.this,
                    mAppPermissionConfig);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mLoadingDialog != null) {
                mLoadingDialog.cancel();
                mLoadingDialog = null;
            }
            loadPermissionAction();
        }
    }

    private ProgressDialog mLoadingDialog;

    private AppPermissionConfig mAppPermissionConfig;

    private static final PermissionGroupComparator GROUP_COMPARATOR = new PermissionGroupComparator();

    private Map<Long, List<PermissionModel>> mData = new HashMap<Long, List<PermissionModel>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pm_activity_app_permissions_editor);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_PERMISSION);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_MAIN);

        String pkgName = getIntent().getStringExtra(EXTRA_PKGNAME);
        if (TextUtils.isEmpty(pkgName)) {
            finish();
            return;
        }

        setTitle(AndroidUtils.loadAppLabel(this, pkgName));

        mAppPermissionConfig = PermissionUtils.loadAppPermissionConfig(this, pkgName);
        if (mAppPermissionConfig == null) {
            finish();
            return;
        }

        new LoadDataTask().execute();
    }

    private void loadPermissionAction() {
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        Set<Long> groupIds = mData.keySet();
        List<Long> groupIdList = new ArrayList<Long>(groupIds);
        if (groupIdList.size() >= 2) {
            Collections.sort(groupIdList, GROUP_COMPARATOR);
        }

        boolean isPermissionControlOpen = PermissionUtils.isAppPermissionControlOpen(getApplicationContext());

        for (long groupId : groupIdList) {
            List<PermissionModel> permissionList = mData.get(groupId);
            if (permissionList != null && !permissionList.isEmpty()) {
                GroupModel group = PermissionUtils.getGroupById(this, groupId);
                if (group != null) {
                    PreferenceCategory category = new PreferenceCategory(this);
                    category.setTitle(group.getName());
                    screen.addPreference(category);

                    for (PermissionModel permission : permissionList) {
                        final long permissionId = permission.getId();
                        final String permissionName = permission.getName();
                        final int permissionAction = PermissionUtils.getPermissionAction(
                                mAppPermissionConfig, permissionId);

                        AppPermsEditorPreference preference = new AppPermsEditorPreference(this);
                        preference.setTitle(permissionName);
                        preference.setSummary(permission.getDescx());
                        preference.setPermissionAction(permissionAction);

                        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                if (permissionId == Permissions.PERM_ID_SYSTEMALERT) {
                                    showSystemAlertDialog(permissionName, permissionId,
                                            permissionAction);
                                } else {
                                    showPermissionActionDialog(permissionName, permissionId,
                                            permissionAction);
                                }
                                return true;
                            }
                        });

                        if (!isPermissionControlOpen) {
                            preference.setEnabled(false);
                        }

                        category.addPreference(preference);
                    }

                }
            }
        }
    }

    private void showPermissionActionDialog(final String permissionName, final long permissionId,
            final int permissionAction) {
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

                                // app 权限设置打点(1)
                                int status = which + 1;
                                String pkgName = getIntent().getStringExtra(EXTRA_PKGNAME);
                                String AppName = AndroidUtils.loadAppLabel(
                                        AppPermissionsEditorActivity.this, pkgName).toString();
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_APP_PACKAGE, pkgName);
                                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_APP_NAME, AppName);
                                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_NAME, permissionId + "");
                                map.put(AnalyticsUtil.TRACK_ID_PERMISSION_STATUS, status + "");

                                AnalyticsUtil.track(AppPermissionsEditorActivity.this,
                                        AnalyticsUtil.TRACK_ID_PERMISSION_CHANGE, map);

                                PermissionUtils.setPermissionAction(
                                        AppPermissionsEditorActivity.this,
                                        mAppPermissionConfig, permissionId, action);

                                dialog.dismiss();
                                loadPermissionAction();
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showSystemAlertDialog(final String permissionName, final long permissionId,
            final int permissionAction) {
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
                                        AppPermissionsEditorActivity.this,
                                        mAppPermissionConfig, permissionId, action);

                                dialog.dismiss();
                                loadPermissionAction();
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
