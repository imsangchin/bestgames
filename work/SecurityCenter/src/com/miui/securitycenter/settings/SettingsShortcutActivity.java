
package com.miui.securitycenter.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import miui.preference.PreferenceActivity;

import com.miui.securitycenter.settings.ShortcutHelper.Shortcut;

import com.miui.securitycenter.R;

public class SettingsShortcutActivity extends PreferenceActivity implements
        OnPreferenceChangeListener {

    private CheckBoxPreference mQuickCleanUp;
    private CheckBoxPreference mOptimizeCenter;
    private CheckBoxPreference mNetworkAssistant;
    private CheckBoxPreference mAntiSpam;
    private CheckBoxPreference mPowerCenter;
    private CheckBoxPreference mVirusCenter;
    private CheckBoxPreference mPermCenter;

    private ShortcutHelper mShortcutHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.m_settings_shortcuts);

        mShortcutHelper = ShortcutHelper.getInstance(this);

        mQuickCleanUp = (CheckBoxPreference) findPreference(getString(R.string.preference_key_shortcut_quick_cleanup));
        mOptimizeCenter = (CheckBoxPreference) findPreference(getString(R.string.preference_key_shortcut_optimize_center));
        mNetworkAssistant = (CheckBoxPreference) findPreference(getString(R.string.preference_key_shortcut_network_assistant));
        mAntiSpam = (CheckBoxPreference) findPreference(getString(R.string.preference_key_shortcut_antispam));
        mPowerCenter = (CheckBoxPreference) findPreference(getString(R.string.preference_key_shortcut_power_center));
        mVirusCenter = (CheckBoxPreference) findPreference(getString(R.string.preference_key_shortcut_virus_center));
        mPermCenter = (CheckBoxPreference) findPreference(getString(R.string.preference_key_shortcut_perm_center));

        mQuickCleanUp.setOnPreferenceChangeListener(this);
        mQuickCleanUp.setIcon(R.drawable.ic_launcher_quick_clean);
        mOptimizeCenter.setOnPreferenceChangeListener(this);
        mOptimizeCenter.setIcon(R.drawable.ic_launcher_rubbish_clean);
        mNetworkAssistant.setOnPreferenceChangeListener(this);
        mNetworkAssistant.setIcon(R.drawable.ic_launcher_network_assistant);
        mAntiSpam.setOnPreferenceChangeListener(this);
        mAntiSpam.setIcon(R.drawable.ic_launcher_anti_spam);
        mPowerCenter.setOnPreferenceChangeListener(this);
        mPowerCenter.setIcon(R.drawable.ic_launcher_power_optimize);
        mVirusCenter.setOnPreferenceChangeListener(this);
        mVirusCenter.setIcon(R.drawable.ic_launcher_virus_scan);
        mPermCenter.setOnPreferenceChangeListener(this);
        mPermCenter.setIcon(R.drawable.ic_launcher_license_manage);
    }

    @Override
    public void onResume() {
        super.onResume();
        mQuickCleanUp.setChecked(mShortcutHelper.queryShortcut(Shortcut.QUICk_CLEANUP));
        mOptimizeCenter.setChecked(mShortcutHelper.queryShortcut(Shortcut.OPTIMIZE_CENTER));
        mNetworkAssistant.setChecked(mShortcutHelper.queryShortcut(Shortcut.NETWORK_ASSISTANT));
        mAntiSpam.setChecked(mShortcutHelper.queryShortcut(Shortcut.ANTISPAM));
        mPowerCenter.setChecked(mShortcutHelper.queryShortcut(Shortcut.POWER_CENTER));
        mVirusCenter.setChecked(mShortcutHelper.queryShortcut(Shortcut.VIRUS_CENTER));
        mPermCenter.setChecked(mShortcutHelper.queryShortcut(Shortcut.PERM_CENTER));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean checked = (Boolean) newValue;
        String prefKey = preference.getKey();
        Shortcut shortcut = null;
        if (prefKey.equals(getString(R.string.preference_key_shortcut_quick_cleanup))) {
            shortcut = Shortcut.QUICk_CLEANUP;
        } else if (prefKey.equals(getString(R.string.preference_key_shortcut_optimize_center))) {
            shortcut = Shortcut.OPTIMIZE_CENTER;
        } else if (prefKey.equals(getString(R.string.preference_key_shortcut_network_assistant))) {
            shortcut = Shortcut.NETWORK_ASSISTANT;
        } else if (prefKey.equals(getString(R.string.preference_key_shortcut_antispam))) {
            shortcut = Shortcut.ANTISPAM;
        } else if (prefKey.equals(getString(R.string.preference_key_shortcut_power_center))) {
            shortcut = Shortcut.POWER_CENTER;
        } else if (prefKey.equals(getString(R.string.preference_key_shortcut_virus_center))) {
            shortcut = Shortcut.VIRUS_CENTER;
        } else if (prefKey.equals(getString(R.string.preference_key_shortcut_perm_center))) {
            shortcut = Shortcut.PERM_CENTER;
        }
        if (shortcut == null) {
            return false;
        }
        if (checked) {
            mShortcutHelper.createShortcut(shortcut);
        } else {
            mShortcutHelper.removeShortcut(shortcut);
        }
        return true;
    }
}
