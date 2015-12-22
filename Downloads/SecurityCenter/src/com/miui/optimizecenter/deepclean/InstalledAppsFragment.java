
package com.miui.optimizecenter.deepclean;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.MiuiIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.miui.antivirus.ExtraGuardHelper;
import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.backup.proxy.IBackupProxy;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.optimizecenter.CleanDataManager;
import com.miui.optimizecenter.PkgSizeStats;
import com.miui.optimizecenter.Preferences;
import com.miui.optimizecenter.enums.InstalledAppsSortType;
import com.miui.optimizecenter.enums.SecurityStatus;
import com.miui.optimizecenter.event.BackupUninstallAppEvent;
import com.miui.optimizecenter.event.BackupUninstallAppsEvent;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.NotifyButtonEnabledEvent;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.NotifyLoadingShownEvent;
import com.miui.optimizecenter.event.PerformItemClickEvent;
import com.miui.optimizecenter.event.UninstallAppEvent;
import com.miui.optimizecenter.event.UninstallAppsEvent;
import com.miui.optimizecenter.event.ViewAppDetailsEvent;
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.optimizecenter.event.ViewInstalledAppDetailsEvent;
import com.miui.optimizecenter.event.ViewLargeFileDetailsEvent;
import com.miui.optimizecenter.tools.FileHelper;
import com.miui.optimizecenter.tools.ScanInstalledAppThread;
import com.miui.optimizecenter.tools.ScanInstalledAppsCallback;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.ExtraIntent;
import com.cleanmaster.sdk.IKSCleaner;

import android.content.pm.IPackageDeleteObserver;
import com.android.internal.os.PkgUsageStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miui.app.AlertDialog;
import miui.app.ActionBar;
import miui.provider.ExtraGuardVirusInfoEntity;
import miui.text.ExtraTextUtils;

import com.miui.securitycenter.R;

public class InstalledAppsFragment extends BaseDeepCleanFragment {

    public static final String TAG = InstalledAppsFragment.class.getSimpleName();

    private ScanInstalledAppsCallback mInstalledAppsCallback = new ScanInstalledAppsCallback() {

        @Override
        public void onScanStart() {
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
            mDataManager.clearInstalledAppsMap();
        }

        @Override
        public boolean onScanItem(String pkgName, PackageInfo packageInfo, PkgUsageStats usageStats) {
            try {
                InstalledAppModel installedApp = new InstalledAppModel();
                installedApp.setPackageInfo(packageInfo);
                installedApp.setUsageStats(usageStats);
                installedApp.setAdviseDelete(false);

                installedApp.setSecurityStatus(SecurityStatus.SAFE);
                installedApp.setSecurityInfo(null);
                installedApp.setAdviseDelete(false);

                if (getActivity() != null) {
                    ExtraGuardHelper mExtraGuardHelper = ExtraGuardHelper
                            .getInstance(getActivity());
                    // 病毒扫描
                    ExtraGuardVirusInfoEntity virusInfo = mExtraGuardHelper
                            .incrementalCheckInstalledApp(
                                    packageInfo.applicationInfo, false);
                    if (virusInfo != null) {
                        int virusType = virusInfo.getType();
                        if (ExtraGuardHelper.VIRUS_BLACK == virusType) {
                            installedApp.setSecurityStatus(SecurityStatus.VIRUS);
                            installedApp.setSecurityInfo(virusInfo.getDescription());
                            installedApp.setAdviseDelete(true);
                        } else if (ExtraGuardHelper.VIRUS_GRAY == virusType) {
                            installedApp.setSecurityStatus(SecurityStatus.RISK);
                            installedApp.setSecurityInfo(virusInfo.getDescription());
                            installedApp.setAdviseDelete(true);
                        }
                    }
                }

                mDataManager.addInstalledAppModel(installedApp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return mForceStopped;
        }

        @Override
        public void onScanItemSizeChanged(String pkgName, PkgSizeStats sizeStats) {
            try {
                InstalledAppModel installedApp = mDataManager.getInstalledAppModel(pkgName);
                if (installedApp != null) {
                    installedApp.setSizeStats(sizeStats);

                    mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                            NotifyListUpdateEvent.create(false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onScanFinish() {
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }

    };

    private final IPackageDeleteObserver.Stub mPackageDeleteObserver = new IPackageDeleteObserver.Stub() {
        @Override
        public void packageDeleted(String packageName, int returnCode) throws RemoteException {
            // ignore
        }
    };

    private class UninstallAppsTask extends AsyncTask<Void, Void, Void> {
        private List<String> mSuccessList = new ArrayList<String>();
        private List<String> mFailedList = new ArrayList<String>();
        private boolean mIsBackup = false;

        public UninstallAppsTask(List<String> successList, List<String> failedList, boolean isBackup) {
            mSuccessList = successList;
            mFailedList = failedList;
            mIsBackup = isBackup;
        }

        @Override
        protected void onPreExecute() {
            Map<String, InstalledAppModel> appMaps = new HashMap<String, InstalledAppModel>(
                    mDataManager.getInstalledAppsMap());
            for (String pkgName : mSuccessList) {
                mDataManager.removeInstalledAppModel(pkgName);
            }

            mInstalledAppsView.collapseAllItems(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }

        @Override
        protected Void doInBackground(Void... params) {
            PackageManager pm = getActivity().getPackageManager();
            for (String pkg : mSuccessList) {
                pm.deletePackage(pkg, mPackageDeleteObserver, 0);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mIsBackup) {
                if (!mFailedList.isEmpty()) {
                    String msg = getString(R.string.toast_backup_uninstall_failed,
                            mFailedList.size());
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.toast_backup_uninstall_success,
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.toast_uninstall_success,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private EventHandler mEventHandler = new EventHandler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EventType.EVENT_NOTIFY_BUTTON_ENABLED:
                    notifyButtonEnabled((NotifyButtonEnabledEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_LOADING_SHOWN:
                    notifyLoadingShown((NotifyLoadingShownEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_LIST_UPDATE:
                    notifyListUpdate((NotifyListUpdateEvent) msg.obj);
                    break;
                case EventType.EVENT_UNINSTALL_APPS:
                    unInstallApps((UninstallAppsEvent) msg.obj);
                    break;
                case EventType.EVENT_BACKUP_UNINSTALL_APPS:
                    backupUninstallApps((BackupUninstallAppsEvent) msg.obj);
                    break;
                case EventType.EVENT_VIEW_FILE:
                    viewAppSdcardFiles((ViewFileEvent) msg.obj);
                    break;
                case EventType.EVENT_UNINSTALL_APP:
                    uninstallAppItem((UninstallAppEvent) msg.obj);
                    break;
                case EventType.EVENT_VIEW_APP_DETAILS:
                    viewAppDetails((ViewAppDetailsEvent) msg.obj);
                    break;
                case EventType.EVENT_BACKUP_UNINSTALL_APP:
                    backupAndUninstallApp((BackupUninstallAppEvent) msg.obj);
                    break;
                case EventType.EVENT_PERFORM_ITEM_CLICK:
                    performItemClick((PerformItemClickEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        public void performItemClick(PerformItemClickEvent event) {
            mInstalledAppsView.performItemClick(event.getView(), event.getPosition(),
                    event.getPosition());
        }

        private void backupAndUninstallApp(BackupUninstallAppEvent event) {
            final String pkgName = event.getPkgName();
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_backup_and_uninstall_apps)
                    .setMessage(R.string.dialog_msg_backup_and_uninstall_apps)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ArrayList<String> pkgs = new ArrayList<String>();
                                    pkgs.add(pkgName);
                                    startBackupActivity(pkgs);
                                }
                            })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        private static final String SCHEME = "package";

        private void viewAppDetails(ViewAppDetailsEvent event) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, event.getPkgName(), null);
            intent.setData(uri);
            startActivity(intent);
        }

        private void uninstallAppItem(UninstallAppEvent event) {
            final String pkgName = event.getPkgName();

            if (event.isSilent()) {
                doUninstallApp(pkgName);
                return;
            }

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_uninstall_apps)
                    .setMessage(R.string.dialog_msg_uninstall_apps)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    List<String> successList = new ArrayList<String>();
                                    successList.add(pkgName);
                                    new UninstallAppsTask(successList, new ArrayList<String>(),
                                            false).execute();
                                }
                            })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        private void viewAppSdcardFiles(ViewFileEvent event) {
            try {
                String[] residualPaths = AidlProxyHelper.getInstance().getIKSCleaner()
                        .getResidualFilePaths(event.getPath());
                if (residualPaths == null || residualPaths.length == 0) {
                    Toast.makeText(getActivity(),
                            R.string.toast_app_no_residual_on_sdcard,
                            Toast.LENGTH_SHORT).show();
                } else {
                    FileHelper.openFile(getActivity(), residualPaths[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void backupUninstallApps(BackupUninstallAppsEvent event) {
            ArrayList<String> pkgList = new ArrayList<String>();

            Map<String, InstalledAppModel> appMaps = new HashMap<String, InstalledAppModel>(
                    mDataManager.getInstalledAppsMap());
            Set<String> keys = appMaps.keySet();
            for (String key : keys) {
                InstalledAppModel installedApp = appMaps.get(key);
                if (installedApp.adviseDelete()) {
                    pkgList.add(installedApp.getPackageInfo().packageName);
                }
            }

            if (!pkgList.isEmpty()) {
                showBackupAndUninstallDialog(pkgList);
            }
        }

        private void unInstallApps(UninstallAppsEvent event) {
            Map<String, InstalledAppModel> appMaps = new HashMap<String, InstalledAppModel>(
                    mDataManager.getInstalledAppsMap());
            Set<String> keys = appMaps.keySet();
            for (String key : keys) {
                InstalledAppModel installedApp = appMaps.get(key);
                if (installedApp.adviseDelete()) {
                    showUninstallAllAppsConfirmDialog(getActivity());
                    return;
                }
            }
        }

        private void notifyListUpdate(NotifyListUpdateEvent event) {
            try {
                List<InstalledAppModel> appList = new ArrayList<InstalledAppModel>();
                Map<String, InstalledAppModel> appMaps = new HashMap<String, InstalledAppModel>(
                        mDataManager.getInstalledAppsMap());
                Set<String> keys = appMaps.keySet();
                for (String key : keys) {
                    InstalledAppModel installedApp = appMaps.get(key);
                    if (installedApp.getSizeStats() != null) {
                        appList.add(installedApp);
                    }
                }

                if (event.isNeedSort() && appList.size() >= 2) {
                    Collections.sort(appList, mDefaultComparator);
                }

                mInstalledAppsAdapter.updateData(appList);
                mInstalledAppsAdapter.notifyDataSetChanged();
                mInstalledAppsView.setUninstallButtonEnabled(!appList.isEmpty());

                String leftText = getString(R.string.hints_installed_apps_header_left);
                long totalSize = 0;
                for (InstalledAppModel model : appList) {
                    totalSize += (model.getSizeStats().internalSize + model.getSizeStats().externalSize);
                }

                int color = getResources().getColor(R.color.high_light_green);
                String leftContent = String.format(leftText, appList.size());
                String rightContent = ExtraTextUtils.formatFileSize(getActivity(), totalSize);
                mInstalledAppsView.setHeaderLeftTitle(AndroidUtils.getHighLightString(leftContent,
                        color, String.valueOf(appList.size())));
                mInstalledAppsView.setHeaderRightTitle(AndroidUtils.getHighLightString(
                        rightContent, color, rightContent));
                mInstalledAppsView.setHeaderBarShown(!appList.isEmpty());
            } catch (Exception e) {
                // ignore
            }
        }

        private void notifyLoadingShown(NotifyLoadingShownEvent event) {
            mInstalledAppsView.setLoadingShown(event.isShown());
        }

        private void notifyButtonEnabled(NotifyButtonEnabledEvent event) {
            mInstalledAppsView.setUninstallButtonEnabled(event.isEnabled());
        }
    };

    private InstalledAppComparator TIME_COMPARATOR = new InstalledAppComparator(
            InstalledAppsSortType.LUNCH_TIME);
    private InstalledAppComparator SIZE_COMPARATOR = new InstalledAppComparator(
            InstalledAppsSortType.APP_SIZE);

    private static final int REQUEST_CODE_BACKUP = 0;

    private InstalledAppComparator mDefaultComparator = TIME_COMPARATOR;

    private InstalledAppsFragmentView mInstalledAppsView;
    private InstalledAppsListAdapter mInstalledAppsAdapter;

    private CleanDataManager mDataManager;

    private boolean mForceStopped = false;

    private IFileProxy mIFileProxy;
    private IKSCleaner mIKSCleaner;
    private IBackupProxy mIBackupProxy;

    private boolean mIsActivityCreated = false;
    private boolean mIsServiceBinded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mForceStopped = false;

        if (Preferences.getInstalledAppSortType() == InstalledAppsSortType.LUNCH_TIME) {
            mDefaultComparator = TIME_COMPARATOR;
        } else {
            mDefaultComparator = SIZE_COMPARATOR;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.op_fragment_installed_apps, null);
        mInstalledAppsView = (InstalledAppsFragmentView) view;
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDataManager = CleanDataManager.getInstance(getActivity());

        mInstalledAppsView.setEventHandler(mEventHandler);
        mInstalledAppsAdapter = new InstalledAppsListAdapter(mEventHandler);
        mInstalledAppsView.setInstalledAppsListAdapter(mInstalledAppsAdapter);

        mIsActivityCreated = true;
        checkSatus();
    }

    @Override
    public void onServiceBinded(Context context, IKSCleaner cleaner, IFileProxy fileProxy) {
        mIFileProxy = fileProxy;
        mIKSCleaner = cleaner;
        mIsServiceBinded = true;
        checkSatus();
    };

    public void setIBackupProxy(IBackupProxy proxy) {
        mIBackupProxy = proxy;
    }

    private void checkSatus() {
        if (mIsActivityCreated && mIsServiceBinded) {
            new ScanInstalledAppThread(getActivity(), mInstalledAppsCallback).start();
        }
    }

    private void doUninstallApp(String pkgName) {
        try {
            PackageManager pm = getActivity().getPackageManager();
            pm.deletePackage(pkgName, mPackageDeleteObserver, 0);
            mDataManager.removeInstalledAppModel(pkgName);
            mInstalledAppsView.collapseAllItems(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        } catch (Exception e) {
            // ignore
        }
    }

    private void showBackupAndUninstallDialog(final ArrayList<String> pkgs) {
        if (pkgs == null || pkgs.isEmpty()) {
            return;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_title_backup_and_uninstall_apps)
                .setMessage(R.string.dialog_msg_backup_and_uninstall_apps)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startBackupActivity(pkgs);
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void startBackupActivity(ArrayList<String> pkgList) {
        try {
            if (mIBackupProxy.hasTask()) {
                Toast.makeText(getActivity(), R.string.toast_backup_is_running, Toast.LENGTH_SHORT)
                        .show();
            } else {
                Intent backupIntent = new Intent(MiuiIntent.ACTION_BACKUP_PROXY);
                backupIntent.putStringArrayListExtra(MiuiIntent.EXTRA_BACKUP_PKG_LIST, pkgList);
                startActivityForResult(backupIntent, REQUEST_CODE_BACKUP);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void showUninstallAllAppsConfirmDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_uninstall_apps)
                .setMessage(R.string.dialog_msg_uninstall_apps)
                .setPositiveButton(R.string.button_text_uninstall,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String, InstalledAppModel> appMaps = new HashMap<String, InstalledAppModel>(
                                        mDataManager.getInstalledAppsMap());

                                List<String> successList = new ArrayList<String>();
                                Set<String> keys = appMaps.keySet();
                                for (String key : keys) {
                                    if (appMaps.get(key).adviseDelete()) {
                                        successList.add(key);
                                    }
                                }

                                new UninstallAppsTask(successList, new ArrayList<String>(), false)
                                        .execute();
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BACKUP) {
            if (resultCode == Activity.RESULT_OK) {
                List<String> successList = data
                        .getStringArrayListExtra(MiuiIntent.EXTRA_BACKUP_SUCCESSED_PKG_LIST);
                List<String> failedList = data
                        .getStringArrayListExtra(MiuiIntent.EXTRA_BACKUP_FAILED_PKG_LIST);

                if (successList == null) {
                    successList = new ArrayList<String>();
                }
                if (failedList == null) {
                    failedList = new ArrayList<String>();
                }

                new UninstallAppsTask(successList, failedList, true).execute();
            } else {
                String msg = getString(R.string.toast_backup_uninstall_failed, "");
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                NotifyListUpdateEvent.create(true));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mForceStopped = true;
    }

    @Override
    public void onFragmentSortTypeSelected(Context context, int sortTypeId) {
        if (sortTypeId == 1) {
            mDefaultComparator = TIME_COMPARATOR;
            Preferences.setInstalledAppSortType(InstalledAppsSortType.LUNCH_TIME);
        } else if (sortTypeId == 2) {
            mDefaultComparator = SIZE_COMPARATOR;
            Preferences.setInstalledAppSortType(InstalledAppsSortType.APP_SIZE);
        }
        mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                NotifyListUpdateEvent.create(true));
    }
}
