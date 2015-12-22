
package com.miui.securitycenter.manualitem;

import android.accounts.AccountManager;
import miui.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.Log;
import android.util.Xml.Encoding;

import miui.text.ExtraTextUtils;
import miui.os.Build;
import miui.cloud.external.CloudSysHelper;
import miui.accounts.ExtraAccountManager;

import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.common.FormatBytesUtil;
import com.miui.optimizecenter.whitelist.InternalWhiteList;
import com.miui.permcenter.PermissionUtils;
import com.lbe.security.bean.AppPermissionConfig;
import com.lbe.security.service.provider.Permissions;
import com.miui.powercenter.provider.DataManager;
import com.miui.securitycenter.ActionConstant;
import com.miui.securitycenter.AutoUpdateManualListService;
import com.miui.securitycenter.MainActivity;
import com.miui.securitycenter.R;
import com.miui.securitycenter.handlebar.HandleHeaderType;
import com.miui.securitycenter.handlebar.HandleItem;
import com.miui.securitycenter.handlebar.HandleItemModel;
import com.miui.securitycenter.handlebar.WeightConstants;
import com.miui.securitycenter.manualitem.ExaminationResult.ExaminationDataItem;
import com.miui.securitycenter.settings.SettingsActivity;

import com.miui.securitycenter.Preferences;

import com.lbe.security.service.provider.Permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManualItemCheckManager {
    public interface ManualItemScanCallback {
        void onStartScan();

        /**
         * @param descx
         * @return true : force stop
         */
        boolean onScanItem(HandleItemModel model);

        void onFinishScan();
    }

    private final class ManualItemScore {

        ManualItemScore() {
            reset();
        }

        void reset() {
        }

        int getScore() {
            return 0;
        }
    }

    private Context mContext;
    private static ManualItemCheckManager INST;
    private List<HandleItemModel> mHandleItemList;
    private ManualItemScanCallback mCallback;
    private ManualItemScore mManualItemScore;
    private ItemListManager mItemListManager;

    // Network Assistant
    private static final String URI_NETWORK_TRAFFIC_INFO = "content://com.miui.networkassistant.provider/datausage_status";
    private static final String URI_NETWORK_SETTING_INFO = "content://com.miui.networkassistant.provider/na_settings_info";

    private static final int MILLSECONDOFONEDAY = 1000 * 3600 * 24;
    private static final int DAY_DIFF_FLOW_VERIFY = 3;

    private static final String TOTAL_LIMIT = "total_limit"; // 套餐总量
    private static final String MONTH_USED = "month_used";// 当月使用流量
    private static final String OPERATOR_SETTED = "operator_setted";// 是否设置运营商
                                                                    // 值类型boolean；true
                                                                    // 已经设置运营商
    private static final String CORRECTION_TIME = "correction_time";// 流量矫正时间
                                                                    // 值类型long；单位mills
    private static final String TRAFFIC_SAVING_STARTED = "traffic_saving_started";// 是否设置流量节省
                                                                                  // 值类型boolean；true
                                                                                  // 已经设置节省
    private static final String SHOW_STATUS_BAR_SETTED = "show_status_bar_setted";// 是否开启常驻通知栏信息
                                                                                  // 值类型int，只有0,1两种值，1表示开启
    private static final String NEEDED_TRAFFIC_PURCHASE = "needed_traffic_purchase";// 是否需要增加流量包
                                                                                    // 值类型Boolean，true表示需要增加流量包

    private boolean cloud_account;
    private boolean sim_card;
    private boolean data_flow;
    private boolean telecom_operator;

    public ManualItemCheckManager(Context context) {
        mContext = context;
        mItemListManager = ItemListManager.getInstance(context);
        mManualItemScore = new ManualItemScore();
    }

    public static ManualItemCheckManager getInstance(Context context) {
        if (INST == null) {
            INST = new ManualItemCheckManager(context.getApplicationContext());
        }
        return INST;
    }

    public int getManualScanCount() {
        return mItemListManager.getItemList().size();
    }

    public void startScanManulItem(ManualItemScanCallback callback) {
        mCallback = callback;

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        mHandleItemList = mItemListManager.getItemList();
        initBoolean();
        for (HandleItemModel model : mHandleItemList) {
            switch (model.getItem()) {
                case MIUI_UPDATE:
                    checkMiuiUpdate(model);
                    break;
                case CLOUD_ACCOUNT:
                    checkCloudAccount(model);
                    break;
                case CLOUD_SERVICE:
                    checkCoudService(model);
                    break;
                case POWER_OPTIMIZER:
                    checkPowerOptimizer(model);
                    break;
                case DATA_FLOW:
                    checkDataFlow(model);
                    break;
                case TELECOM_OPERAOTR:
                    checkTelecomOperator(model);
                    break;
                case FLOW_VERIFY:
                    checkFlowVerify(model);
                    break;
                case SAVING_SWITCH:
                    checkSavingSwitch(model);
                    break;
                case FLOW_NOTIFICATION:
                    checkFlowNotification(model);
                    break;
                case FLOW_PURCHASE:
                    checkFlowPurchase(model);
                    break;
                case PERMANENT_NOTIFICATIONBAR:
                    checkPermanentNotificationBar(model);
                    break;
                case GARBAGE_LIB:
                    checkGarbageLib(model);
                    break;
                case PERMISSION_ROOT:
                    checkPermissionRoot(model);
                    break;
                case APP_UPDATE:
                    checkAppUpdate(model);
                    break;
                default:
                    break;
            }
        }

        if (mCallback != null) {
            mCallback.onFinishScan();
        }
    }

    public void initBoolean() {
        cloud_account = false;
        data_flow = false;
        telecom_operator = false;
    }

    // MIUI_UPDATE
    private void checkMiuiUpdate(HandleItemModel model) {
        String curVersion = Build.VERSION.INCREMENTAL;
        String newestVersion = Preferences.getNewestMiuiVersion();
        if (!newestVersion.isEmpty() && !curVersion.equals(newestVersion)) {
            model.setTitle(mContext.getString(R.string.title_miui_update));
            model.setSummary(mContext.getString(R.string.summary_miui_update, newestVersion));
            updateScanProgress(model);
        }
    }

    // CLOUD_ACCOUNT
    private void checkCloudAccount(HandleItemModel model) {
        if (!CloudSysHelper.isXiaomiAccountPresent(mContext)) {
            model.setTitle(mContext.getString(R.string.title_cloud_account));
            model.setSummary(mContext.getString(R.string.summary_cloud_account));
            updateScanProgress(model);
            cloud_account = false;
        }
        else {
            cloud_account = true;
        }
    }

    // CLOUD_SERVICE
    private void checkCoudService(HandleItemModel model) {
        if (cloud_account) {
            if (CloudSysHelper.isAllMiCloudSyncOff(mContext)) {
                model.setTitle(mContext.getString(R.string.title_cloud_service));
                model.setSummary(mContext.getString(R.string.summary_cloud_service));
                updateScanProgress(model);
            }
        }
    }

    // Power optimizer
    private void checkPowerOptimizer(HandleItemModel model) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, filter);
        DataManager mDataManager = DataManager.getInstance(mContext);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;
        boolean isChecked = mDataManager.getBoolean(DataManager.KEY_LOW_BATTERY_ENABLED,
                DataManager.LOW_BATTERY_ENABLED_DEFAULT);
        if (batteryPct < 0.1f && !isChecked) {
            model.setTitle(mContext.getString(R.string.title_power_optimizer));
            model.setSummary(mContext.getString(R.string.summary_power_optimizer));
            updateScanProgress(model);
        }
    }

    // Data flow
    private void checkDataFlow(HandleItemModel model) {
        if (insertedSimCard()) {
            String res = getStringOfNetworkData(URI_NETWORK_TRAFFIC_INFO, TOTAL_LIMIT);
            if (!res.isEmpty() && Long.valueOf(res).longValue() <= 0) {
                model.setTitle(mContext.getString(R.string.title_data_flow));
                model.setSummary(mContext.getString(R.string.summary_data_flow));
                updateScanProgress(model);
                data_flow = false;
            }
            else {
                data_flow = true;
            }
        }
    }

    // TELECOM_OPERAOTR
    private void checkTelecomOperator(HandleItemModel model) {
        if (data_flow) {
            String res = getStringOfNetworkData(URI_NETWORK_SETTING_INFO, OPERATOR_SETTED);
            if (!res.isEmpty() && !Boolean.valueOf(res).booleanValue()) {
                model.setTitle(mContext.getString(R.string.title_telecom_operator));
                model.setSummary(mContext.getString(R.string.summary_telecom_operator));
                updateScanProgress(model);
                telecom_operator = false;
            }
            else {
                telecom_operator = true;
            }
        }
    }

    // FLOW_VERIFY
    private void checkFlowVerify(HandleItemModel model) {
        if (telecom_operator) {
            int day_diff = (int) Math.floor((getLongOfNetworkData(URI_NETWORK_SETTING_INFO,
                    CORRECTION_TIME) - System
                    .currentTimeMillis())
                    / (float) MILLSECONDOFONEDAY);
            if (day_diff > DAY_DIFF_FLOW_VERIFY) {
                model.setTitle(mContext.getString(R.string.title_flow_verify));
                model.setSummary(mContext.getString(R.string.summary_flow_verify, day_diff));
                updateScanProgress(model);
            }
        }
    }

    // SAVING_SWITCH
    private void checkSavingSwitch(HandleItemModel model) {
        String res = getStringOfNetworkData(URI_NETWORK_SETTING_INFO, TRAFFIC_SAVING_STARTED);
        if (!res.isEmpty() && !Boolean.valueOf(res).booleanValue()) {
            model.setTitle(mContext.getString(R.string.title_saving_switch));
            model.setSummary(mContext.getString(R.string.summary_saving_switch));
            updateScanProgress(model);
        }
    }

    // FLOW_NOTIFICATION
    private void checkFlowNotification(HandleItemModel model) {
        String res = getStringOfNetworkData(URI_NETWORK_SETTING_INFO, SHOW_STATUS_BAR_SETTED);
        if (!res.isEmpty() && Long.valueOf(res).longValue() != 1) {
            model.setTitle(mContext.getString(R.string.title_flow_notification));
            model.setSummary(mContext.getString(R.string.summary_flow_notification));
            updateScanProgress(model);
        }
    }

    // FLOW_PURCHASE
    private void checkFlowPurchase(HandleItemModel model) {
        String res = getStringOfNetworkData(URI_NETWORK_SETTING_INFO,
                NEEDED_TRAFFIC_PURCHASE);
        if (!res.isEmpty() && Boolean.valueOf(res).booleanValue()) {
            long flow_diff = getLongOfNetworkData(URI_NETWORK_TRAFFIC_INFO, MONTH_USED)
                    - getLongOfNetworkData(URI_NETWORK_TRAFFIC_INFO, TOTAL_LIMIT);
            if (flow_diff >= 0) {
                model.setTitle(mContext.getString(R.string.title_flow_purchase));
                model.setSummary(mContext.getString(R.string.summary_flow_purchase_beyond,
                        FormatBytesUtil.formatBytes(flow_diff, 0)));
                updateScanProgress(model);
            }
            else {
                model.setTitle(mContext.getString(R.string.title_flow_purchase));
                model.setSummary(mContext.getString(R.string.summary_flow_purchase_left,
                        FormatBytesUtil.formatBytes(-flow_diff, 0)));
                updateScanProgress(model);
            }
        }
    }

    // PERMANENT_NOTIFICATIONBAR
    private void checkPermanentNotificationBar(HandleItemModel model) {
        ContentResolver cr = mContext.getContentResolver();
        if (!com.miui.securitycenter.Preferences.isShowPermanentNotification(cr)) {
            model.setTitle(mContext
                    .getString(R.string.title_show_notification));
            model.setSummary(mContext.getString(R.string.summary_show_notification));
            updateScanProgress(model);
        }
    }

    // Garbage lib
    private void checkGarbageLib(HandleItemModel model) {
        if (!com.miui.optimizecenter.Preferences.isAutoUpdateCLeanupDBEnabled()) {
            model.setTitle(mContext.getString(R.string.title_garbage_lib));
            model.setSummary(mContext.getString(R.string.summary_garbage_lib));
            updateScanProgress(model);
        }
    }

    // PERMISSION_ROOT
    private void checkPermissionRoot(HandleItemModel model) {
        Map<String, AppPermissionConfig> rootApps = PermissionUtils.loadPermissionApps(mContext,
                Permissions.PERM_ID_ROOT);
        if (!Build.IS_STABLE_VERSION && !rootApps.isEmpty()) {
            Set<String> pkgNames = rootApps.keySet();
            boolean mRootTag = false;
            for (String pkgName : pkgNames) {
                AppPermissionConfig config = rootApps.get(pkgName);
                if (config.getEffectivePermissionConfig(Permissions.PERM_ID_ROOT) == AppPermissionConfig.ACTION_ACCEPT) {
                    mRootTag = true;
                    break;
                }
            }
            if (mRootTag) {
                model.setTitle(mContext.getString(R.string.title_permission_root));
                model.setSummary(mContext.getString(R.string.summary_permission_root));
                updateScanProgress(model);
            }
        }

    }

    // App Update
    private void checkAppUpdate(HandleItemModel model) {
        int count = Preferences.getNeedUpdateAppCount();
        if (count > 0) {
            model.setTitle(mContext.getString(R.string.title_app_update));
            model.setSummary(mContext.getString(R.string.summary_app_update, count));
            updateScanProgress(model);
        }
    }

    private void updateScanProgress(HandleItemModel model) {
        if (mCallback != null) {
            mCallback.onScanItem(model);
        }
    }

    public boolean insertedSimCard() {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(Uri.parse(URI_NETWORK_SETTING_INFO),
                    null, null, null, null);
            if (cursor == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public long getLongOfNetworkData(String URI, String Column) {
        Cursor cursor = null;
        long data = 0;
        try {
            cursor = mContext.getContentResolver().query(Uri.parse(URI),
                    null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                data = cursor.getLong(cursor.getColumnIndex(Column));
            }
        } catch (Exception e) {
            return 0;
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        return data;
    }

    public String getStringOfNetworkData(String URI, String Column) {
        Cursor cursor = null;
        String res = "";
        try {
            cursor = mContext.getContentResolver().query(Uri.parse(URI),
                    null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                res = cursor.getString(cursor.getColumnIndex(Column));
            }
        } catch (Exception e) {
            return res;
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        return res;
    }

    public boolean getBooleanOfNetworkData(String URI, String Column) {
        Cursor cursor = null;
        boolean flag = false;
        try {
            cursor = mContext.getContentResolver().query(Uri.parse(URI),
                    null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                flag = cursor.getInt(cursor.getColumnIndex(Column)) > 0;
            }
        } catch (Exception e) {
            return false;
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        return flag;
    }

    public void viewActionActivity(String action, boolean flag) {
        try {
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("high_light", flag);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewCloudAccount(MainActivity activity) {
        AccountManager am = AccountManager.get(mContext);
        am.addAccount("com.xiaomi", null, null, null, activity, null, new Handler());
    }

    public void viewCloudService() {
        CloudSysHelper.promptEnableAllMiCloudSync(mContext);
    }

    public void viewMiuiMarket() {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.xiaomi.market", "com.xiaomi.market.ui.MarketTabActivity");
            intent.putExtra("extra_tab", 3);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewUpdater() {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.android.updater", "com.android.updater.MainActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
