
package com.miui.antivirus;

import android.content.res.Resources;

import com.miui.common.PreferenceStore;

public class Preferences {

    static final String pref_key_anti_virus_status = "key_anti_virus_status";

    public static AntiVirusStatus getLastAntiVirusStatus() {
        Resources res = PreferenceStore.getResources();
        int defValue = AntiVirusStatus.getDefault(res).getValue(res);
        int pref = PreferenceStore.getPreferenceInt(pref_key_anti_virus_status, defValue);
        return AntiVirusStatus.fromValue(res, pref);
    }

    public static void setLastAntiVirusStatus(AntiVirusStatus status) {
        Resources res = PreferenceStore.getResources();
        int value = status.getValue(res);
        PreferenceStore.setPreferenceInt(pref_key_anti_virus_status, value);
    }

    static final String pref_key_latest_virus_scan_date = "key_latest_virus_scan_date";

    public static void setLatestVirusScanDate(long date) {
        PreferenceStore.setPreferenceLong(pref_key_latest_virus_scan_date, date);
    }

    public static long getLatestVirusScanDate(long defValue) {
        return PreferenceStore.getPreferenceLong(pref_key_latest_virus_scan_date, defValue);
    }

    static final String pref_key_virus_scan_cloud = "virus_scan_cloud";

    public static void setVirusScanCloudEnabled(boolean enabled) {
        PreferenceStore.setPreferenceBoolean(pref_key_virus_scan_cloud, enabled);
    }

    public static boolean isVirusScanCloudEnabled() {
        return PreferenceStore.getPreferenceBoolean(pref_key_virus_scan_cloud, true);
    }

    static final String pref_key_last_scan_risk_count = "key_last_scan_risk_count";

    public static void setLastVirusScanRiskCount(int count) {
        PreferenceStore.setPreferenceInt(pref_key_last_scan_risk_count, count);
    }

    public static int getLastVirusScanRiskCount() {
        return PreferenceStore.getPreferenceInt(pref_key_last_scan_risk_count, 0);
    }

    static final String pref_key_last_scan_virus_count = "key_last_scan_virus_count";

    public static void setLastVirusScanVirusCount(int count) {
        PreferenceStore.setPreferenceInt(pref_key_last_scan_virus_count, count);
    }

    public static int getLastVirusScanVirusCount() {
        return PreferenceStore.getPreferenceInt(pref_key_last_scan_virus_count, 0);
    }

    static final String pref_key_virus_lib_auto_update_enabled = "virus_auto_update";

    public static void setVirusLibAutoUpdateEnabled(boolean enabled) {
        PreferenceStore.setPreferenceBoolean(pref_key_virus_lib_auto_update_enabled, enabled);
    }

    public static boolean isVirusLibAutoUpdateEnabled() {
        return PreferenceStore.getPreferenceBoolean(pref_key_virus_lib_auto_update_enabled, true);
    }

    static final String pref_key_virus_cloud_scan_enabled = "virus_scan_cloud";

    public static void setVirusCloudScanEnabled(boolean enabled) {
        PreferenceStore.setPreferenceBoolean(pref_key_virus_cloud_scan_enabled, enabled);
    }

    public static boolean isVirusCloudScanEnabled() {
        return PreferenceStore.getPreferenceBoolean(pref_key_virus_cloud_scan_enabled, true);
    }

    static final String pref_key_latest_virus_lib_update_date = "virus_lib_version";

    public static void setLatestVirusLibUpdateDate(long date) {
        PreferenceStore.setPreferenceLong(pref_key_latest_virus_lib_update_date, date);
    }

    public static long getLatestVirusLibUpdateDate() {
        return PreferenceStore.getPreferenceLong(pref_key_latest_virus_lib_update_date, 0);
    }

    static final String pref_key_need_cleanup_white_list = "key_need_cleanup_white_list";

    public static void setNeedCleanupWhiteList(boolean need) {
        PreferenceStore.setPreferenceBoolean(pref_key_need_cleanup_white_list, need);
    }

    public static boolean isNeedCleanupWhiteList() {
        return PreferenceStore.getPreferenceBoolean(pref_key_need_cleanup_white_list, false);
    }
}
