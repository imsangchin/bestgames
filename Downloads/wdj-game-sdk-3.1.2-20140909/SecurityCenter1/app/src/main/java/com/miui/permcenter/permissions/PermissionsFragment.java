
package com.miui.permcenter.permissions;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.miui.securitycenter.R;
import com.miui.common.EventHandler;
import com.miui.optimizecenter.CustomPreference;
import com.miui.permcenter.PermissionUtils;
import com.miui.permcenter.event.EventType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermissionsFragment extends PreferenceFragment {

    public static final String TAG = PermissionsFragment.class.getSimpleName();

    private static final PermissionGroupComparator GROUP_COMPARATOR = new PermissionGroupComparator();

    private class LoadPermissionsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mPermissions = PermissionUtils.loadGroupedAllPermissions(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            PreferenceScreen screen = getPreferenceScreen();
            screen.removeAll();

            Set<Long> groupIds = mPermissions.keySet();
            List<Long> groupIdList = new ArrayList<Long>(groupIds);
            if (groupIdList.size() >= 2) {
                Collections.sort(groupIdList, GROUP_COMPARATOR);
            }

            for (long groupId : groupIdList) {
                if (getActivity() == null) {
                    return;
                }
                List<PermissionModel> permissionList = mPermissions.get(groupId);
                if (permissionList != null && !permissionList.isEmpty()) {
                    GroupModel group = PermissionUtils.getGroupById(getActivity(), groupId);
                    if (group != null) {
                        PreferenceCategory category = new PreferenceCategory(getActivity());
                        category.setTitle(group.getName());
                        screen.addPreference(category);

                        String appsCountHints = getString(R.string.hints_permission_apps_count);
                        for (PermissionModel permission : permissionList) {
                            CustomPreference preference = new CustomPreference(getActivity());
                            preference.setTitle(permission.getName());
                            preference.setSummary(permission.getDescx());
                            preference.setContent(String.format(appsCountHints,
                                    permission.getUsedAppsCount()));

                            Intent intent = new Intent(getActivity(),
                                    PermissionAppsEditorActivity.class);
                            intent.putExtra(PermissionAppsEditorActivity.EXTRA_PERMISSION_ID,
                                    permission.getId());
                            intent.putExtra(PermissionAppsEditorActivity.EXTRA_PERMISSION_NAME,
                                    permission.getName());
                            preference.setIntent(intent);

                            category.addPreference(preference);
                        }

                    }
                }
            }
        }
    }

    private Map<Long, List<PermissionModel>> mPermissions = new HashMap<Long, List<PermissionModel>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pm_fragment_permissions);
    }

    @Override
    public void onResume() {
        super.onResume();
        new LoadPermissionsTask().execute();
    }
}
