
package com.miui.securitycenter.settings;

import miui.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView.FindListener;

import miui.preference.PreferenceActivity;

import com.miui.analytics.AnalyticsUtil;
import com.miui.securitycenter.AppPackageInfo;
import com.miui.securitycenter.CtaDialogActivity;
import com.miui.securitycenter.NotificationService;
import com.miui.securitycenter.Preferences;
import android.content.MiuiIntent;

import com.miui.securitycenter.manualitem.WhiteListActivity;

import com.miui.securitycenter.R;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener,
        OnPreferenceClickListener {
    private final String APP_SETTINGS_INTENT = "miui.intent.action.APP_SETTINGS";
    private final String PREFERENCE_KEY_MANUAL_WHITE_LIST = "preference_key_manual_item_white_list";

    private Preference mCreateShortcutPreference;
    private Preference mAboutVersionPreference;
    private Preference mManualWhiteListPreference;
    private CheckBoxPreference mNotificationPreference;
    private CheckBoxPreference mCtaPreference;

    private Preference mGarbageCleanupSettings;
    private Preference mNetworkAssistantSettings;
    private Preference mAntiSpamSettings;
    private Preference mPowerManagerSettings;
    private Preference mAntiVirusSettings;
    private Preference mPermissionsSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.m_settings);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_MAIN);

        // 没有在Manifest中直接指定title是因为要在Settings中提供统一的系统应用设置入口
        Intent intent = getIntent();
        if (!TextUtils.equals(APP_SETTINGS_INTENT, intent.getAction())) {
            setTitle(R.string.activity_title_settings);
        }

        mCreateShortcutPreference = findPreference(getString(R.string.preference_key_create_shortcut));
        mAboutVersionPreference = findPreference(getString(R.string.preference_key_about_version));
        mManualWhiteListPreference = findPreference(PREFERENCE_KEY_MANUAL_WHITE_LIST);
        mAboutVersionPreference.setSummary(getString(R.string.menu_item_about_summary,
                AppPackageInfo.sVersionName));

        mManualWhiteListPreference.setIntent(new Intent(this, WhiteListActivity.class));
        mCreateShortcutPreference.setIntent(new Intent(this, ShortcutActivity.class));
        mAboutVersionPreference.setOnPreferenceClickListener(this);

        mNotificationPreference = (CheckBoxPreference) findPreference(getString(R.string.preference_key_create_permanent_notification));
        mNotificationPreference.setOnPreferenceChangeListener(this);
        mNotificationPreference.setChecked(Preferences
                .isShowPermanentNotification(getContentResolver()));

        mCtaPreference = (CheckBoxPreference) findPreference(getString(R.string.preference_key_cta_settings));
        mCtaPreference.setOnPreferenceChangeListener(this);

        mGarbageCleanupSettings = findPreference(getString(R.string.preference_key_module_garbage_cleanup));
        Intent garbageCleanupIntent = new Intent(MiuiIntent.ACTION_GARBAGE_CLEANUP_SETTINGS);
        garbageCleanupIntent.putExtra(MiuiIntent.EXTRA_SETTINGS_TITLE,
                getString(R.string.Settings_title_garbage_cleanup));

        mGarbageCleanupSettings.setIntent(garbageCleanupIntent);

        mNetworkAssistantSettings = findPreference(getString(R.string.preference_key_module_network_assistant));
        Intent networkAssistantIntent = new Intent(MiuiIntent.ACTION_NETWORKASSISTANT_SETTINGS);
        networkAssistantIntent.putExtra(MiuiIntent.EXTRA_SETTINGS_TITLE,
                getString(R.string.Settings_title_network_assistants));
        mNetworkAssistantSettings.setIntent(networkAssistantIntent);

        mAntiSpamSettings = findPreference(getString(R.string.preference_key_module_antipam));
        Intent antiSpamIntent = new Intent(MiuiIntent.ACTION_ANTISPAM_SETTINGS);
        antiSpamIntent.putExtra(MiuiIntent.EXTRA_SETTINGS_TITLE,
                getString(R.string.Settings_title_anti_spam));
        mAntiSpamSettings.setIntent(antiSpamIntent);

        mPowerManagerSettings = findPreference(getString(R.string.preference_key_module_power_center));
        Intent powerIntent = new Intent(MiuiIntent.ACTION_POWER_SETTINGS);
        powerIntent.putExtra(MiuiIntent.EXTRA_SETTINGS_TITLE,
                getString(R.string.Settings_title_power_center));
        mPowerManagerSettings.setIntent(powerIntent);

        mAntiVirusSettings = findPreference(getString(R.string.preference_key_module_antivirus));
        Intent antiVirusIntent = new Intent(MiuiIntent.ACTION_ANTIVIRUS_SETTINGS);
        antiVirusIntent.putExtra(MiuiIntent.EXTRA_SETTINGS_TITLE,
                getString(R.string.Settings_title_anti_virus));
        mAntiVirusSettings.setIntent(antiVirusIntent);

        mPermissionsSettings = findPreference(getString(R.string.preference_key_module_permissions));
        Intent permissionIntent = new Intent(MiuiIntent.ACTION_PERMISSION_SETTINGS);
        permissionIntent.putExtra(MiuiIntent.EXTRA_SETTINGS_TITLE,
                getString(R.string.settings_title_permission));
        mPermissionsSettings.setIntent(permissionIntent);

        // 如果不是一个海外版本， 权限管理的设置项需要隐藏。 实际需要隐藏的是全局的权限开关， 现在只有这一个设置项。
        if (!miui.os.Build.IS_INTERNATIONAL_BUILD && !miui.os.Build.IS_CTS_BUILD) {
            PreferenceCategory moduleSettingCategory = (PreferenceCategory) findPreference(getString(R.string.preference_key_category_title_module_settings));
            moduleSettingCategory.removePreference(mPermissionsSettings);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCtaPreference.setChecked(Preferences.isConnectNetworkAlow());
    }

    private void showMenuAboutDialog() {
        String message = getString(R.string.menu_item_about_content,
                AppPackageInfo.sVersionName);
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.menu_item_about_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(
                getString(R.string.preference_key_create_permanent_notification))) {
            boolean shown = (Boolean) newValue;
            Preferences.setShowPermanentNotification(getContentResolver(),
                    shown);

            if (shown) {
                startService(new Intent(SettingsActivity.this, NotificationService.class));
            } else {
                stopService(new Intent(SettingsActivity.this, NotificationService.class));
            }
            return true;
        } else if (preference.getKey().equals(
                getString(R.string.preference_key_cta_settings))) {
            boolean checked = (Boolean) newValue;
            if (checked) {
                startActivity(new Intent(SettingsActivity.this, CtaDialogActivity.class));
            } else {
                Preferences.setConnectNetworkAlow(false);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.preference_key_about_version))) {
            showMenuAboutDialog();
            return true;
        }
        return false;
    }

}
