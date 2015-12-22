
package com.miui.securitycenter;

import android.content.ContentResolver;
import android.content.MiuiIntent;
import android.os.SystemProperties;
import android.provider.MiuiSettings;

import com.miui.common.PreferenceStore;

public class Preferences {

    static final String pref_key_latest_optimize_date = "latest_optimize_date";

    public static void setLatestOptimizeDate(long date) {
        PreferenceStore.setPreferenceLong(pref_key_latest_optimize_date, date);
    }

    public static long getLatestOptimizeDate(long defValue) {
        return PreferenceStore.getPreferenceLong(pref_key_latest_optimize_date, defValue);
    }

    static final String pref_key_last_check_canceled = "last_check_canceled";

    public static void setLastCheckCanceled(boolean canceled) {
        PreferenceStore.setPreferenceBoolean(pref_key_last_check_canceled, canceled);
    }

    public static boolean isLastCheckCanceled() {
        return PreferenceStore.getPreferenceBoolean(pref_key_last_check_canceled, false);
    }

    public static void setShowPermanentNotification(ContentResolver cr, boolean shown) {
        MiuiSettings.System.putBoolean(cr, MiuiIntent.EXTRA_SHOW_SECURITY_NOTIFICATION, shown);
    }

    public static boolean isShowPermanentNotification(ContentResolver cr) {
        return MiuiSettings.System.getBoolean(cr, MiuiIntent.EXTRA_SHOW_SECURITY_NOTIFICATION,
                MiuiIntent.DEFAULT_SHOW_SECURITY_NOTIFICATION);
    }

    static final String pref_key_need_update_app_count = "need_update_app_count";

    public static void setNeedUpdateAppCount(int count) {
        PreferenceStore.setPreferenceInt(pref_key_need_update_app_count, count);
    }

    public static int getNeedUpdateAppCount() {
        return PreferenceStore.getPreferenceInt(pref_key_need_update_app_count, 0);
    }

    static final String pref_key_has_new_block_phone = "has_new_block_phone";

    public static void setHasNewBlockPhone(boolean hasNew) {
        PreferenceStore.setPreferenceBoolean(pref_key_has_new_block_phone, hasNew);
    }

    public static boolean hasNewBlockPhone() {
        return PreferenceStore.getPreferenceBoolean(pref_key_has_new_block_phone, false);
    }

    static final String pref_key_has_new_block_mms = "has_new_block_mms";

    public static void setHasNewBlockMms(boolean hasNew) {
        PreferenceStore.setPreferenceBoolean(pref_key_has_new_block_mms, hasNew);
    }

    public static boolean hasNewBlockMms() {
        return PreferenceStore.getPreferenceBoolean(pref_key_has_new_block_mms, false);
    }

    public static void setConnectNetworkAlow(boolean alow) {
        if (alow) {
            SystemProperties.set(MiuiSettings.System.KEY_SECURITY_CENTER_ALLOW_CONNECT_NETWORK, "true");
        }else {
            SystemProperties.set(MiuiSettings.System.KEY_SECURITY_CENTER_ALLOW_CONNECT_NETWORK, "false");
        }
    }

    public static boolean isConnectNetworkAlow() {
        return SystemProperties.getBoolean(MiuiSettings.System.KEY_SECURITY_CENTER_ALLOW_CONNECT_NETWORK, false);
    }

    static final String pref_key_cta_checkbox_checked = "key_cta_checkbox_checked";

    public static void setCtaCheckboxChecked(boolean checked) {
        PreferenceStore.setPreferenceBoolean(pref_key_cta_checkbox_checked, checked);
    }

    public static boolean isCtaCheckboxChecked() {
        return PreferenceStore.getPreferenceBoolean(pref_key_cta_checkbox_checked, false);
    }

    // cta end

    static final String pref_key_newest_miui_version = "key_set_newest_miui_version";

    public static void setNewestMiuiVersion(String version) {
        PreferenceStore.setPreferenceString(pref_key_newest_miui_version, version);
    }

    public static String getNewestMiuiVersion() {
        return PreferenceStore.getPreferenceString(pref_key_newest_miui_version, "");
    }

    static final String pref_key_white_list_miui_version = "key_white_list_miui_version";

    public static void setWhiteListMiuiVersion(String version) {
        PreferenceStore.setPreferenceString(pref_key_white_list_miui_version, version);
    }

    public static String getWhiteListMiuiVersion() {
        return PreferenceStore.getPreferenceString(pref_key_white_list_miui_version, "");
    }
}
