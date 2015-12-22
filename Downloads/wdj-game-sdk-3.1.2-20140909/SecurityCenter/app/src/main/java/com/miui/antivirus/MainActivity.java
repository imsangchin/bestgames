
package com.miui.antivirus;

import java.util.ArrayList;
import java.util.List;

import miui.app.Activity;
import miui.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;
import com.miui.antivirus.MainHandleBar.HandleItem;
import com.miui.antivirus.VirusCheckManager.ScanItemType;
import com.miui.antivirus.VirusCheckManager.ScanResultType;
import com.miui.antivirus.VirusCheckManager.VirusCleanupCallback;
import com.miui.antivirus.VirusCheckManager.VirusScanCallback;
import com.miui.antivirus.event.EventType;
import com.miui.antivirus.event.OnActionButtonClickEvent;
import com.miui.antivirus.event.OnCancelScanVirusEvent;
import com.miui.antivirus.event.OnCleanupVirusItemEvent;
import com.miui.antivirus.event.OnFindVirusItemEvent;
import com.miui.antivirus.event.OnFinishCleanupVirusEvent;
import com.miui.antivirus.event.OnFinishCloudScanVirusEvent;
import com.miui.antivirus.event.OnFinishScanVirusEvent;
import com.miui.antivirus.event.OnScanVirusItemEvent;
import com.miui.antivirus.event.OnStartCleanupVirusEvent;
import com.miui.antivirus.event.OnStartCloudScanVirusEvent;
import com.miui.antivirus.event.OnStartScanVirusEvent;
import com.miui.antivirus.event.OnVirusHandleItemClickEvent;
import com.miui.common.AndroidUtils;
import com.miui.common.ApkIconHelper;
import com.miui.common.EventHandler;
import com.miui.securitycenter.DateTimeUtils;
import com.miui.securitycenter.ExtraIntent;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.event.OnBackPressedEvent;
import com.miui.securitycenter.event.ViewSettingsEvent;

import com.miui.guardprovider.service.IFileProxy;

public class MainActivity extends Activity {

    public enum VirusScanStatus {
        NORMAL, SCANNING, SCANNED, CLEANNING, CLEANNED
    }

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

    private VirusScanCallback mVirusScanCallback = new VirusScanCallback() {

        @Override
        public void onStartScan() {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_START_SCAN_VIRUS,
                    OnStartScanVirusEvent.create());
        }

        @Override
        public boolean onScanItem(String descx) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_SCAN_VIRUS_ITEM,
                    OnScanVirusItemEvent.create(descx));
            return mForceStopped;
        }

        @Override
        public void onFindItem(ScanResultType resultType, ScanItemType itemType, String pkgName,
                String appLabel, String sourceDir, String virusDescx, String virusName) {
            VirusModel model = new VirusModel();
            model.setScanResultType(resultType);
            model.setScanItemType(itemType);
            model.setPkgName(pkgName);
            model.setAppLabel(appLabel);
            model.setSourceDir(sourceDir);
            model.setVirusDescx(virusDescx);
            model.setVirusName(virusName);

            mEventHandler.sendEventMessage(EventType.EVENT_ON_FIND_VIRUS_ITEM,
                    OnFindVirusItemEvent.create(model));

            Log.d("miui", "================resultType = " + resultType + " itemType = " + itemType
                    + " virusDescx = " + virusDescx + " virusName = " + virusName);
        }

        @Override
        public void onCloudScanStart() {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_START_CLOUD_SCAN_VIRUS,
                    OnStartCloudScanVirusEvent.create());
        }

        @Override
        public void onFinishCloudScan() {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_CLOUD_SCAN_VIRUS,
                    OnFinishCloudScanVirusEvent.create());
        }

        @Override
        public void onFinishScan() {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_SCAN_VIRUS,
                    OnFinishScanVirusEvent.create());
        }

        @Override
        public void onCancelScan() {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_CANCEL_SCAN_VIRUS,
                    OnCancelScanVirusEvent.create());
        };
    };

    private VirusCleanupCallback mVirusCleanupCallback = new VirusCleanupCallback() {

        @Override
        public void onStartCleanup() {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_START_CLEANUP_VIRUS,
                    OnStartCleanupVirusEvent.create());
        }

        @Override
        public void onFinishCleanup() {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_CLEANUP_VIRUS,
                    OnFinishCleanupVirusEvent.create());
        }

        @Override
        public boolean onCleanupItem(String descx) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_CLEANUP_VIRUS_ITEM,
                    OnCleanupVirusItemEvent.create(descx));
            return mForceStopped;
        }

        @Override
        public void onCancelCleanup() {
            // TODO
        };
    };

    private class ScanTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            mVirusCheckManager.startScan(mIFileProxy, mVirusScanCallback);
            return null;
        }
    }

    private class CleanupTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mVirusCheckManager.startCleanup(mIFileProxy, mVirusCleanupCallback);
            return null;
        }
    }

    private EventHandler mEventHandler = new EventHandler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EventType.EVENT_ON_ACTION_BUTTON_CLICK:
                    onActionButtonClick((OnActionButtonClickEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_START_SCAN_VIRUS:
                    onStartScanVirus((OnStartScanVirusEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_SCAN_VIRUS_ITEM:
                    onScanVirusItem((OnScanVirusItemEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FIND_VIRUS_ITEM:
                    onFindVirusItem((OnFindVirusItemEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_START_CLOUD_SCAN_VIRUS:
                    onStartCloudScanVirus((OnStartCloudScanVirusEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_CLOUD_SCAN_VIRUS:
                    onFinishCloudScanVirus((OnFinishCloudScanVirusEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_SCAN_VIRUS:
                    onFinishScanVirus((OnFinishScanVirusEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_START_CLEANUP_VIRUS:
                    onStartCLenaupVirus((OnStartCleanupVirusEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_CLEANUP_VIRUS_ITEM:
                    onCleanupVirusItem((OnCleanupVirusItemEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_CLEANUP_VIRUS:
                    onFinishCleanupVirus((OnFinishCleanupVirusEvent) msg.obj);
                    break;
                case EventType.EVENT_VIEW_SETTINGS:
                    viewSettings((ViewSettingsEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_VIRUS_HANDLE_ITEM_CLICK:
                    onHandleItemClick((OnVirusHandleItemClickEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_BACK_PRESSED:
                    onBackPressed((OnBackPressedEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_CANCEL_SCAN_VIRUS:
                    onCancelScanVirus((OnCancelScanVirusEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        private void onCancelScanVirus(OnCancelScanVirusEvent event) {
            mMainView.setContentSummaryText(getString(R.string.descx_quick_scan_cancel));
        }

        private void onBackPressed(OnBackPressedEvent event) {
            finish();
        }

        private void onHandleItemClick(OnVirusHandleItemClickEvent event) {
            switch (event.getHandleItem()) {
                case VIRUS:
                    startActivity(new Intent(MainActivity.this, VirusListActivity.class));
                    break;
                case RISK:
                    startActivity(new Intent(MainActivity.this, RiskListActivity.class));
                    break;

                default:
                    break;
            }
        }

        private void viewSettings(ViewSettingsEvent event) {
            startActivity(new Intent(MainActivity.this, SettingsAcitivty.class));
            AnalyticsUtil.track(MainActivity.this,
                    AnalyticsUtil.TRACK_ID_ENTER_ANTIVIRUS_SETTINGS);
        }

        private void onFinishCleanupVirus(OnFinishCleanupVirusEvent event) {
            refreshVirusInfo();
        }

        private void onCleanupVirusItem(OnCleanupVirusItemEvent event) {
            mMainView.setContentSummaryText(event.getDescx());
        }

        private void onStartCLenaupVirus(OnStartCleanupVirusEvent event) {
            mMainView.setActionButtonText(getString(R.string.btn_text_stop));
            mMainView.setContentTitleText(getString(R.string.hints_cleanning_text));
            mVirusScanStatus = VirusScanStatus.CLEANNING;
        }

        private void onFinishScanVirus(OnFinishScanVirusEvent event) {
            mVirusScanStatus = VirusScanStatus.SCANNED;
            mMainView.stopScanningAnimation();
            mMainView.setHandleItemEnabled(HandleItem.RISK, true);
            mMainView.setHandleItemEnabled(HandleItem.VIRUS, true);
            Preferences.setNeedCleanupWhiteList(false);
            refreshVirusInfo();
        }

        private void onFinishCloudScanVirus(OnFinishCloudScanVirusEvent event) {
            mInCloudScanning = false;
            // TODO
        }

        private void onStartCloudScanVirus(OnStartCloudScanVirusEvent event) {
            mInCloudScanning = true;
            mMainView.setContentSummaryText(getString(R.string.hints_scanning_virus_by_cloud));
        }

        private void onFindVirusItem(OnFindVirusItemEvent event) {
            List<VirusModel> riskList = null;
            List<VirusModel> virusList = null;

            VirusModel model = event.getVirusModel();
            if (model.getScanResultType() == ScanResultType.RISK) {
                riskList = mVirusCheckManager.addRisk(model);
            } else if (model.getScanResultType() == ScanResultType.VIRUS) {
                virusList = mVirusCheckManager.addVirus(model);
            }

            if (riskList == null) {
                riskList = mVirusCheckManager.getRiskList();
            }
            if (virusList == null) {
                virusList = mVirusCheckManager.getVirusList();
            }

            AntiVirusStatus status = AntiVirusStatus.SAVE;
            if (!virusList.isEmpty()) {
                status = AntiVirusStatus.VIRUS;
                mMainView.setContentTitleText(getString(R.string.hints_scan_result_virus));
            } else if (!riskList.isEmpty()) {
                status = AntiVirusStatus.RISK;
                mMainView.setContentTitleText(getString(R.string.hints_scan_result_risk));
            }

            int riskSize = riskList.size();
            int virusSize = virusList.size();
            String strRiskCount = String.valueOf(riskSize);
            String riskContent = getString(R.string.hints_handle_item_text_risk, riskSize);
            String strVirusCount = String.valueOf(virusSize);
            String virusContent = getString(R.string.hints_handle_item_text_virus, virusSize);
            int color = getResources().getColor(R.color.high_light_red);

            mMainView.setHandleItemContentText(HandleItem.RISK, AndroidUtils
                    .getHighLightString(riskContent, color, strRiskCount));
            mMainView.setHandleItemContentText(HandleItem.VIRUS, AndroidUtils
                    .getHighLightString(virusContent, color, strVirusCount));

            mMainView.updateContentForeground(status, riskSize + virusSize);
        }

        private void onScanVirusItem(OnScanVirusItemEvent event) {
            mMainView.setContentSummaryText(event.getDescx());
        }

        private void onStartScanVirus(OnStartScanVirusEvent event) {
            mMainView.setActionButtonText(getString(R.string.btn_text_stop));
            mMainView.setContentTitleText(getString(R.string.hints_scan_result_safe));
            mMainView.setHandleItemEnabled(HandleItem.RISK, false);
            mMainView.setHandleItemEnabled(HandleItem.VIRUS, false);
            mVirusCheckManager.clearRiskList();
            mVirusCheckManager.clearVirusList();
            if (Preferences.isNeedCleanupWhiteList()) {
                WhiteListHelper.getInstance(MainActivity.this).clearWhiteList();
            }
            refreshVirusUI(AntiVirusStatus.SAVE, 0, 0);
        }

        private void onActionButtonClick(OnActionButtonClickEvent event) {
            switch (mVirusScanStatus) {
                case NORMAL:
                    mVirusScanStatus = VirusScanStatus.SCANNING;
                    mMainView.startScanningAnimation();
                    mForceStopped = false;
                    new ScanTask().execute();
                    Preferences.setLatestVirusScanDate(System.currentTimeMillis());
                    break;
                case SCANNING:
                    if (mInCloudScanning) {
                        Toast.makeText(MainActivity.this, R.string.toast_virus_in_cloud_scanning,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        showStopVirusScanDialog();
                    }
                    break;
                case SCANNED:
                    mForceStopped = false;
                    mVirusScanStatus = VirusScanStatus.CLEANNING;
                    new CleanupTask().execute();
                    break;
                case CLEANNING:
                    mForceStopped = true;
                    // TODO
                    break;
                case CLEANNED:
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    private IFileProxy mIFileProxy;

    private MainActivityView mMainView;
    private VirusCheckManager mVirusCheckManager;

    private VirusScanStatus mVirusScanStatus = VirusScanStatus.NORMAL;

    private boolean mForceStopped = false;
    private boolean mInCloudScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v_activity_main);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_VIRUS);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_MAIN);

        mVirusCheckManager = VirusCheckManager.getInstance(this);

        mMainView = (MainActivityView) findViewById(R.id.main_view);
        mMainView.setEventHandler(mEventHandler);

        AidlProxyHelper.getInstance().bindFileProxy(this, mFileConnection);

        handleLastScanInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVirusCheckManager.isNeedRefreshVirusInfo()) {
            mVirusCheckManager.setNeedRefreshVirusInfo(false);
            refreshVirusInfo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainView.stopScanningAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIFileProxy != null) {
            AidlProxyHelper.getInstance().unbindProxy(this, mFileConnection);
        }
        ApkIconHelper.getInstance(this).clearCacheLaunchers();
    }

    private void handleLastScanInfo() {
        mMainView.setContentSummaryText(getString(R.string.hints_need_quick_scan));
        long lastScanDate = Preferences.getLatestVirusScanDate(-1);
        if (lastScanDate == -1) {
            mMainView.setContentTitleText(getString(R.string.hints_last_virus_scan_none));
            mMainView.setHandleItemContentText(HandleItem.RISK,
                    getString(R.string.hints_last_virus_scan_none));
            mMainView.setHandleItemContentText(HandleItem.VIRUS,
                    getString(R.string.hints_last_virus_scan_none));
        } else {
            AntiVirusStatus status = Preferences.getLastAntiVirusStatus();
            mMainView.setContentTitleText(DateTimeUtils.formatVirusScanTime(status, this,
                    lastScanDate));

            int riskCount = Preferences.getLastVirusScanRiskCount();
            int virusCount = Preferences.getLastVirusScanVirusCount();

            refreshVirusUI(status, virusCount, riskCount);
        }
    }

    private void refreshVirusUI(AntiVirusStatus status, int virusCount, int riskCount) {
        mMainView.updateContentForeground(status, riskCount + virusCount);
        int color = getResources().getColor(R.color.high_light_red);

        if (virusCount == 0) {
            mMainView.setHandleItemContentText(HandleItem.VIRUS,
                    getString(R.string.hints_handle_item_text_safe));
        } else {
            String strVirusCount = String.valueOf(virusCount);
            String virusContent = getString(R.string.hints_handle_item_text_virus, virusCount);
            mMainView.setHandleItemContentText(HandleItem.VIRUS, AndroidUtils
                    .getHighLightString(virusContent, color, strVirusCount));
        }

        if (riskCount == 0) {
            mMainView.setHandleItemContentText(HandleItem.RISK,
                    getString(R.string.hints_handle_item_text_safe));
        } else {
            String strRiskCount = String.valueOf(riskCount);
            String riskContent = getString(R.string.hints_handle_item_text_risk, riskCount);
            mMainView.setHandleItemContentText(HandleItem.RISK, AndroidUtils
                    .getHighLightString(riskContent, color, strRiskCount));
        }
    }

    private void refreshVirusInfo() {
        mMainView.setActionButtonText(getString(R.string.btn_text_quick_cleanup));
        mMainView.setContentSummaryText(getString(R.string.descx_quick_scan_done));

        List<VirusModel> riskList = mVirusCheckManager.getRiskList();
        List<VirusModel> virusList = mVirusCheckManager.getVirusList();

        AntiVirusStatus status = AntiVirusStatus.SAVE;
        if (!virusList.isEmpty()) {
            status = AntiVirusStatus.VIRUS;
            mMainView.setContentTitleText(getString(R.string.hints_scan_result_virus));
        } else if (!riskList.isEmpty()) {
            status = AntiVirusStatus.RISK;
            mMainView.setContentTitleText(getString(R.string.hints_scan_result_risk));
        } else {
            mMainView.setContentTitleText(getString(R.string.hints_scan_result_safe));
            status = AntiVirusStatus.SAVE;
        }
        Preferences.setLastAntiVirusStatus(status);

        if (status == AntiVirusStatus.SAVE) {
            mVirusScanStatus = VirusScanStatus.CLEANNED;
            mMainView.setActionButtonText(getString(R.string.btn_text_done));
        }

        Preferences.setLastVirusScanRiskCount(riskList.size());
        Preferences.setLastVirusScanVirusCount(virusList.size());

        refreshVirusUI(status, virusList.size(), riskList.size());

        AnalyticsUtil.trackVirusScanResult(MainActivity.this, virusList.size(), riskList.size());
    }

    private void showStopVirusScanDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_stop_virus_scan)
                .setMessage(R.string.dialog_msg_stop_virus_scan)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mForceStopped = true;
                        mVirusScanStatus = VirusScanStatus.NORMAL;
                        mMainView.stopScanningAnimation();
                        mMainView.setActionButtonText(getString(R.string.btn_text_quick_scan));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
