
package com.miui.analytics;

import java.util.HashMap;
import java.util.Map;

import miui.analytics.Analytics;
import android.content.Context;
import android.os.Build;

import com.miui.securitycenter.AppPackageInfo;
import com.miui.securitycenter.DateTimeUtils;

public class AnalyticsUtil {

    /* Common track constant */
    public static final String TRACK_ID_COMMON_MIUI_VERSION = "system_version";
    public static final String TRACK_ID_COMMON_APP_VERSION = "app_version";
    public static final String TRACK_ID_COMMON_DATA_TIME = "data_time";

    public static final String TRACK_VALUE_COMMON_MIUI_VERSION = Build.VERSION.INCREMENTAL;

    /* Enter security center */
    public static final String TRACK_ID_SECURITY_CENTER_ENTER = "security_center_enter";

    /* Security center main menu */
    public static final String TRACK_ID_MAIN_MENU_CLICK = "main_menu_click";

    public static final int TRACK_VALUE_MAIN_MENU_CLICK_TJ = 0;
    public static final int TRACK_VALUE_MAIN_MENU_CLICK_LJ = 1;
    public static final int TRACK_VALUE_MAIN_MENU_CLICK_WL = 2;
    public static final int TRACK_VALUE_MAIN_MENU_CLICK_SR = 3;
    public static final int TRACK_VALUE_MAIN_MENU_CLICK_SD = 4;
    public static final int TRACK_VALUE_MAIN_MENU_CLICK_BD = 5;
    public static final int TRACK_VALUE_MAIN_MENU_CLICK_SQ = 6;

    public static final String TRACK_ID_CENTER_CHECK_CANCEL = "center_check_cancel";
    public static final String TRACK_ID_CENTER_CHECK_ONE_OPT_CLICKED = "center_check_one_opt_clicked";
    public static final String TRACK_ID_CENTER_CHECK_SCORE = "center_check_score";

    /* Security center menu after scan */
    public static final String TRACK_ID_CENTER_ENTER_SYSTEM = "center_enter_system";
    public static final String TRACK_ID_CENTER_ENTER_MEMORY = "center_enter_memory";
    public static final String TRACK_ID_CENTER_ENTER_CACHE = "center_enter_cache";

    /* Cleaner main menu */
    public static final String TRACK_ID_CLEANER_ENTER_CACHE = "cleaner_enter_cache";
    public static final String TRACK_ID_CLEANER_ENTER_AD = "cleaner_enter_ad";
    public static final String TRACK_ID_CLEANER_ENTER_APK = "cleaner_enter_apk";
    public static final String TRACK_ID_CLEANER_ENTER_UNINSTALL = "cleaner_enter_uninstall";

    public static final String TRACK_ID_CLEANER_OPERATION_ONE_KEY_CLICKED = "cleaner_operation_one_key_clicked";
    public static final String TRACK_ID_CLEANER_OPERATION_DEEP_CLICKED = "cleaner_operation_deep_clicked";

    /* Battery preference switch */
    public static final String TRACK_ID_BATTERY_CHANGE_SAVE_MODE = "battery_change_save_mode";
    public static final String TRACK_ID_BATTERY_CHANGE_POWER_LOW_SAVE_MODE = "battery_change_power_low_save_mode";
    public static final String TRACK_ID_BATTERY_CHANGE_TIMER_SAVE_MODE = "battery_change_timer_save_mode";

    public static final int TRACK_VALUE_SWITCH_OPEN = 1;
    public static final int TRACK_VALUE_SWITCH_CLOSE = 0;

    /* Virus scan */
    public static final String TRACK_ID_VIRUS_SCAN_DONE = "virus_scan_done";
    public static final String TRACK_ID_VIRUS_SCAN_DONE_VIRUS_NUM = "virus_app_num";
    public static final String TRACK_ID_VIRUS_SCAN_DONE_RISK_NUM = "risk_app_num";

    public static final String TRACK_ID_VIRUS_SCAN_VIRUS_RESULT = "virus_scan_virus_result";
    public static final String TRACK_ID_VIRUS_SCAN_VIRUS_PACKAGE = "virus_app_package";
    public static final String TRACK_ID_VIRUS_SCAN_VIRUS_APP_NAME = "virus_app_name";
    public static final String TRACK_ID_VIRUS_SCAN_VIRUS_NAME = "virus_name";

    public static final String TRACK_ID_VIRUS_SCAN_RISK_RESULT = "virus_scan_risk_result";
    public static final String TRACK_ID_VIRUS_SCAN_RISK_PACKAGE = "risk_app_package";
    public static final String TRACK_ID_VIRUS_SCAN_RISK_APP_NAME = "risk_app_name";

    /* Settings */
    public static final String TRACK_ID_ENTER_SECURITYCENTER_SETTINGS = "enter_securitycenter_settings";
    public static final String TRACK_ID_ENTER_GARBAGE_CLEANUP_SETTINGS = "enter_garbage_cleanup_settings";
    public static final String TRACK_ID_ENTER_POWER_MANAGER_SETTINGS = "enter_power_manager_settings";
    public static final String TRACK_ID_ENTER_ANTISPAM_SETTINGS = "enter_antispam_settings";
    public static final String TRACK_ID_ENTER_ANTIVIRUS_SETTINGS = "enter_antivirus_settings";

    /* guard provider */
    public static final String TRACK_ID_GUARD_PROVIDER_DAMAGED = "guard_provider_damaged";
    public static final String TRACK_ID_CLEAN_MATSER_DAMAGED = "clean_matser_damaged";

    /* optimize center */
    public static final String TRACK_ID_TOTAL_TRASH_SIZE = "total_trash_size";
    public static final String TRACK_ID_TRASH_CACHE_SIZE = "trash_cache_size";
    public static final String TRACK_ID_TRASH_AD_SIZE = "trash_ad_size";
    public static final String TRACK_ID_TRASH_APK_SIZE = "trash_apk_size";
    public static final String TRACK_ID_TRASH_RESIDUAL_SIZE = "trash_residual_size";

    public static final String TRACK_ID_OPTIMIZE_CEBTER_TRASH_COUNT = "optimize_cebter_trash_count";
    public static final String TRACK_ID_TOTAL_TRASH_COUNT = "total_trash_count";
    public static final String TRACK_ID_TRASH_CACHE_COUNT = "trash_cache_count";
    public static final String TRACK_ID_TRASH_AD_COUNT = "trash_ad_count";
    public static final String TRACK_ID_TRASH_APK_COUNT = "trash_apk_count";
    public static final String TRACK_ID_TRASH_RESIDUAL_COUNT = "trash_residual_count";

    /* active */
    public static final String TRACK_ID_ACTIVE_MAIN = "active_main";
    public static final String TRACK_ID_ACTIVE_TRASH = "active_trash";
    public static final String TRACK_ID_ACTIVE_NETWORK = "active_network";
    public static final String TRACK_ID_ACTIVE_ANTISPAM = "active_antispam";
    public static final String TRACK_ID_ACTIVE_BATTERY = "active_battery";
    public static final String TRACK_ID_ACTIVE_VIRUS = "active_virus";
    public static final String TRACK_ID_ACTIVE_PERMISSION = "active_permission";

    /* ROOT */
    public static final String TRACK_ID_STABLE_ROOT_ACCESS = "stable_root_access";

    /* permission */
    public static final String TRACK_ID_PERMISSION_CHANGE = "permission_change";
    public static final String TRACK_ID_PERMISSION_APP_PACKAGE = "permission_app_package";
    public static final String TRACK_ID_PERMISSION_APP_NAME = "permission_app_name";
    public static final String TRACK_ID_PERMISSION_NAME = "permission_name";
    public static final String TRACK_ID_PERMISSION_STATUS = "permission_status";

    public static void trackMainMenuClick(Context context, int value) {
        track(context, TRACK_ID_MAIN_MENU_CLICK, value);
    }

    public static void trackMainCheckScore(Context context, int score) {
        track(context, TRACK_ID_CENTER_CHECK_SCORE, score);
    }

    public static void trackVirusScanResult(Context context, int virus, int risk) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TRACK_ID_VIRUS_SCAN_DONE_VIRUS_NUM, String.valueOf(virus));
        map.put(TRACK_ID_VIRUS_SCAN_DONE_RISK_NUM, String.valueOf(risk));
        track(context, TRACK_ID_VIRUS_SCAN_DONE, map);
    }

    public static void trackVirusScanVirusDetail(Context context,
            String pkgName, String appName, String virusName) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TRACK_ID_VIRUS_SCAN_VIRUS_PACKAGE, pkgName);
        map.put(TRACK_ID_VIRUS_SCAN_VIRUS_APP_NAME, appName);
        map.put(TRACK_ID_VIRUS_SCAN_VIRUS_NAME, virusName);
        track(context, TRACK_ID_VIRUS_SCAN_VIRUS_RESULT, map);
    }

    public static void trackCleanupTrashCount(Context context, String trashKey, int trashValue) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(trashKey, String.valueOf(trashValue));
        track(context, TRACK_ID_OPTIMIZE_CEBTER_TRASH_COUNT, map);
    }

    public static void trackVirusScanRiskDetail(Context context,
            String pkgName, String appName) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TRACK_ID_VIRUS_SCAN_RISK_PACKAGE, pkgName);
        map.put(TRACK_ID_VIRUS_SCAN_RISK_APP_NAME, appName);
        track(context, TRACK_ID_VIRUS_SCAN_RISK_RESULT, map);
    }

    public static void track(Context context, String eventId) {
        track(context, eventId, null);
    }

    public static void track(Context context, String eventId, long value) {
        track(context, eventId, value, null);
    }

    public static void track(Context context, String eventId,
            Map<String, String> parameters) {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        parameters.putAll(getPresetParam(context));
        Analytics tracker = Analytics.getInstance();
        tracker.startSession(context);
        tracker.trackEvent(eventId, parameters);
        tracker.endSession();
    }

    public static void track(Context context, String eventId, long value,
            Map<String, String> parameters) {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        parameters.putAll(getPresetParam(context));
        Analytics tracker = Analytics.getInstance();
        tracker.startSession(context);
        tracker.trackEvent(eventId, parameters, value);
        tracker.endSession();
    }

    private static HashMap<String, String> getPresetParam(Context context) {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(TRACK_ID_COMMON_MIUI_VERSION,
                TRACK_VALUE_COMMON_MIUI_VERSION);
        parameters
                .put(TRACK_ID_COMMON_APP_VERSION, AppPackageInfo.sVersionName);
        parameters.put(TRACK_ID_COMMON_DATA_TIME, DateTimeUtils.formatDataTime(
                System.currentTimeMillis(), DateTimeUtils.DATE_FORMAT_DAY));
        return parameters;
    }
}
