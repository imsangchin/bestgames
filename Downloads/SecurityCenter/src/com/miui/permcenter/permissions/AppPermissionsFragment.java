
package com.miui.permcenter.permissions;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lbe.security.bean.AppPermissionConfig;
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

import miui.app.AlertDialog;

public class AppPermissionsFragment extends Fragment {

    public static final String TAG = AppPermissionsFragment.class.getSimpleName();

    private boolean isLoadingData;

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mAppsData.clear();
            if (!PermissionUtils.isAppPermissionControlOpen(getActivity().getApplicationContext())) {
                showPermissionControlClosedDialog();
            } else {
                mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                        NotifyLoadingShownEvent.create(true));
            }
            isLoadingData = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mAppsData = PermissionUtils.loadInstalledAppPermissionConfigs(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            isLoadingData = false;

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
            Intent intent = new Intent(getActivity(), AppPermissionsEditorActivity.class);
            intent.putExtra(AppPermissionsEditorActivity.EXTRA_PKGNAME, event.getPkgName());
            startActivity(intent);
        }

        private void notifyListUpdate(NotifyListUpdateEvent event) {
            mAppsListAdapter.updateData(mAppsData);
            mAppsListAdapter.notifyDataSetChanged();

            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
        }

        private void notifyLoadingShown(NotifyLoadingShownEvent event) {
            mAppsView.setLoadingViewShown(event.isShown());
        }
    };

    private Map<String, AppPermissionConfig> mAppsData = new HashMap<String, AppPermissionConfig>();

    private AppsFragmentView mAppsView;
    private AppsListAdapter mAppsListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAppsView = (AppsFragmentView) inflater.inflate(R.layout.pm_fragment_apps, null);
        return mAppsView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAppsListAdapter = new AppsListAdapter(getActivity(), mEventHandler);
        mAppsView.setAppsListAdapter(mAppsListAdapter);

        new LoadDataTask().execute();

    }

    private void showPermissionControlClosedDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.permission_permission_control_closed_desc_dialog_title)
                .setMessage(R.string.permission_permission_control_closed_desc_dialog_msg)
                .setPositiveButton(R.string.button_text_known, null)
                .setOnDismissListener(new OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (isLoadingData) {
                            mAppsView.setLoadingViewShown(true);
                        }
                    }
                })
                .show();
    }
}
