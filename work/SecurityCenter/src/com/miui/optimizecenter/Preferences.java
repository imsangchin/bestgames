
package com.miui.optimizecenter;

import android.content.res.Resources;

import com.miui.common.PreferenceStore;
import com.miui.optimizecenter.enums.CacheGroupSortType;
import com.miui.optimizecenter.enums.GarbageCleanupSize;
import com.miui.optimizecenter.enums.GarbageCleanupTimes;
import com.miui.optimizecenter.enums.InstalledAppsSortType;
import com.miui.optimizecenter.enums.LargeFileSortType;

public class Preferences {

    static final String pref_key_latest_garbage_cleanup_date = "key_latest_garbage_cleanup_date";

    public static void setLatestGarbageCleanupDate(long date) {
        PreferenceStore.setPreferenceLong(pref_key_latest_garbage_cleanup_date, date);
    }

    public static long getLatestGarbageCleanupDate(long defValue) {
        return PreferenceStore.getPreferenceLong(pref_key_latest_garbage_cleanup_date, defValue);
    }

    static final String pref_key_default_expand_cache_groups = "key_default_expand_cache_groups";

    public static void setDefaultExpandCacheGroups(boolean expand) {
        PreferenceStore.setPreferenceBoolean(pref_key_default_expand_cache_groups, expand);
    }

    public static boolean isDefaultExpandCacheGroups() {
        return PreferenceStore.getPreferenceBoolean(pref_key_default_expand_cache_groups, false);
    }

    static final String pref_key_cleanup_db_auto_update_enabled = "key_cleanup_db_auto_update_enabled";

    public static void setAutoUpdateCLeanupDBEnabled(boolean enabled) {
        PreferenceStore.setPreferenceBoolean(pref_key_cleanup_db_auto_update_enabled, enabled);
    }

    public static boolean isAutoUpdateCLeanupDBEnabled() {
        return PreferenceStore.getPreferenceBoolean(pref_key_cleanup_db_auto_update_enabled, true);
    }

    static final String pref_key_cleanup_db_auto_update_time = "key_cleanup_db_auto_update_time";

    public static void setAutoUpdateCLeanupDBTime(long time) {
        PreferenceStore.setPreferenceLong(pref_key_cleanup_db_auto_update_time, time);
    }

    public static long getAutoUpdateCLeanupDBTime() {
        return PreferenceStore.getPreferenceLong(pref_key_cleanup_db_auto_update_time, 0);
    }

    static final String pref_key_garbage_cleanup_time = "key_garbage_cleanup_time";

    public static GarbageCleanupTimes getGarbageCleanupTime() {
        Resources res = PreferenceStore.getResources();
        int defValue = GarbageCleanupTimes.getDefault(res).getValue(res);
        int pref = PreferenceStore.getPreferenceInt(pref_key_garbage_cleanup_time, defValue);
        return GarbageCleanupTimes.fromValue(res, pref);
    }

    public static void setGarbageCleanupTime(GarbageCleanupTimes time) {
        Resources res = PreferenceStore.getResources();
        int value = time.getValue(res);
        PreferenceStore.setPreferenceInt(pref_key_garbage_cleanup_time, value);
    }

    static final String pref_key_garbage_cleanup_size = "key_garbage_cleanup_size";

    public static GarbageCleanupSize getGarbageCleanupSize() {
        Resources res = PreferenceStore.getResources();
        int defValue = GarbageCleanupSize.getDefault(res).getValue(res);
        int pref = PreferenceStore.getPreferenceInt(pref_key_garbage_cleanup_size, defValue);
        return GarbageCleanupSize.fromValue(res, pref);
    }

    public static void setGarbageCleanupSize(GarbageCleanupSize size) {
        Resources res = PreferenceStore.getResources();
        int value = size.getValue(res);
        PreferenceStore.setPreferenceInt(pref_key_garbage_cleanup_size, value);
    }

    static final String pref_key_last_scaning_canceled = "key_last_scaning_canceled";

    public static void setLastScanningCanceled(boolean canceled) {
        PreferenceStore.setPreferenceBoolean(pref_key_last_scaning_canceled, canceled);
    }

    public static boolean isLastScanningCanceled() {
        return PreferenceStore.getPreferenceBoolean(pref_key_last_scaning_canceled, false);
    }

    static final String pref_key_last_cache_scaning_canceled = "key_last_cache_scaning_canceled";

    public static void setLastCacheScanningCanceled(boolean canceled) {
        PreferenceStore.setPreferenceBoolean(pref_key_last_cache_scaning_canceled, canceled);
    }

    public static boolean isLastCacheScanningCanceled() {
        return PreferenceStore.getPreferenceBoolean(pref_key_last_cache_scaning_canceled, false);
    }

    static final String pref_key_last_ad_scaning_canceled = "key_last_ad_scaning_canceled";

    public static void setLastAdScanningCanceled(boolean canceled) {
        PreferenceStore.setPreferenceBoolean(pref_key_last_ad_scaning_canceled, canceled);
    }

    public static boolean isLastAdScanningCanceled() {
        return PreferenceStore.getPreferenceBoolean(pref_key_last_ad_scaning_canceled, false);
    }

    static final String pref_key_last_apk_scaning_canceled = "key_last_apk_scaning_canceled";

    public static void setLastApkScanningCanceled(boolean canceled) {
        PreferenceStore.setPreferenceBoolean(pref_key_last_apk_scaning_canceled, canceled);
    }

    public static boolean isLastApkScanningCanceled() {
        return PreferenceStore.getPreferenceBoolean(pref_key_last_apk_scaning_canceled, false);
    }

    static final String pref_key_last_residual_scaning_canceled = "key_last_residual_scaning_canceled";

    public static void setLastResidualScanningCanceled(boolean canceled) {
        PreferenceStore.setPreferenceBoolean(pref_key_last_residual_scaning_canceled, canceled);
    }

    public static boolean isLastResidualScanningCanceled() {
        return PreferenceStore.getPreferenceBoolean(pref_key_last_residual_scaning_canceled, false);
    }

    static final String pref_key_last_garbage_cleaup_time = "key_last_garbage_cleaup_time";

    public static void setLastGarbageCleanupTime(long time) {
        PreferenceStore.setPreferenceLong(pref_key_last_garbage_cleaup_time, time);
    }

    public static long getLastGarbageCleanupTime() {
        return PreferenceStore.getPreferenceLong(pref_key_last_garbage_cleaup_time, 0);
    }

    static final String pref_key_installed_apps_sort_type = "key_installed_apps_sort_type";

    public static InstalledAppsSortType getInstalledAppSortType() {
        Resources res = PreferenceStore.getResources();
        int defValue = InstalledAppsSortType.getDefault(res).getValue(res);
        int pref = PreferenceStore.getPreferenceInt(pref_key_installed_apps_sort_type, defValue);
        return InstalledAppsSortType.fromValue(res, pref);
    }

    public static void setInstalledAppSortType(InstalledAppsSortType sortType) {
        Resources res = PreferenceStore.getResources();
        int value = sortType.getValue(res);
        PreferenceStore.setPreferenceInt(pref_key_installed_apps_sort_type, value);
    }

    static final String pref_key_large_file_sort_type = "key_large_file_sort_type";

    public static LargeFileSortType getLargeFileSortType() {
        Resources res = PreferenceStore.getResources();
        int defValue = LargeFileSortType.getDefault(res).getValue(res);
        int pref = PreferenceStore.getPreferenceInt(pref_key_large_file_sort_type,
                defValue);
        return LargeFileSortType.fromValue(res, pref);
    }

    public static void setLargeFileSortType(LargeFileSortType sortType) {
        Resources res = PreferenceStore.getResources();
        int value = sortType.getValue(res);
        PreferenceStore.setPreferenceInt(pref_key_large_file_sort_type, value);
    }

    static final String pref_key_cache_data_sort_type = "key_cache_data_sort_type";

    public static CacheGroupSortType getCacheGroupSortType() {
        Resources res = PreferenceStore.getResources();
        int defValue = CacheGroupSortType.getDefault(res).getValue(res);
        int pref = PreferenceStore.getPreferenceInt(pref_key_cache_data_sort_type, defValue);
        return CacheGroupSortType.fromValue(res, pref);
    }

    public static void setCacheGroupSortType(CacheGroupSortType sortType) {
        Resources res = PreferenceStore.getResources();
        int value = sortType.getValue(res);
        PreferenceStore.setPreferenceInt(pref_key_cache_data_sort_type, value);
    }

    static final String pref_key_scanned_garbage_size = "key_scanned_garbage_size";

    public static long getScannedGarbageSize() {
        return PreferenceStore.getPreferenceLong(pref_key_scanned_garbage_size, 0);
    }

    public static void setScannedGarbageSize(long size) {
        PreferenceStore.setPreferenceLong(pref_key_scanned_garbage_size, size);
    }

    static final String pref_key_garbage_in_danger = "key_garbage_danger_in_flag";

    public static boolean isGarbageInDanger() {
        return PreferenceStore.getPreferenceBoolean(pref_key_garbage_in_danger, false);
    }

    public static void setGarbageIndanger(boolean danger) {
        PreferenceStore.setPreferenceBoolean(pref_key_garbage_in_danger, danger);
    }
}
