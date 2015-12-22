
package com.miui.antivirus;

import android.os.Bundle;
import android.content.Intent;
import android.content.MiuiIntent;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.text.format.DateFormat;

import miui.preference.PreferenceActivity;
import com.miui.securitycenter.SecurityCenterService;
import com.miui.securitycenter.R;
import android.provider.MiuiSettings;

public class SettingsAcitivty extends PreferenceActivity implements OnPreferenceChangeListener {

    private CheckBoxPreference mAutoUpdateVirusLibPrefs;
    private CheckBoxPreference mOpenCloudScanPrefs;
    private CheckBoxPreference mOpenInstallMonitorPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.v_settings);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(MiuiIntent.EXTRA_SETTINGS_TITLE);
            if (!TextUtils.isEmpty(title)) {
                setTitle(title);
            }
        }

        mAutoUpdateVirusLibPrefs = (CheckBoxPreference) findPreference(getString(R.string.preference_key_virus_lib_auto_update));
        mAutoUpdateVirusLibPrefs.setOnPreferenceChangeListener(this);
        mOpenCloudScanPrefs = (CheckBoxPreference) findPreference(getString(R.string.preference_key_open_virus_cloud_scan));
        mOpenCloudScanPrefs.setOnPreferenceChangeListener(this);
        mOpenInstallMonitorPrefs = (CheckBoxPreference) findPreference(getString(R.string.preference_key_open_virus_install_monitor));
        mOpenInstallMonitorPrefs.setOnPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAutoUpdateVirusLibPrefs.setChecked(Preferences.isVirusLibAutoUpdateEnabled());
        mOpenCloudScanPrefs.setChecked(Preferences.isVirusCloudScanEnabled());
        mOpenInstallMonitorPrefs.setChecked(MiuiSettings.AntiVirus
                .isInstallMonitorEnabled(getApplicationContext()));

        long updateDate = Preferences.getLatestVirusLibUpdateDate();
        if (updateDate == 0) {
            mAutoUpdateVirusLibPrefs.setSummary(R.string.hints_virus_lib_update_default_summary);
        } else {
            mAutoUpdateVirusLibPrefs.setSummary(getString(
                    R.string.menu_item_virus_lib_auto_update_summary,
                    DateFormat.format("yyyy-MM-dd", updateDate)));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked = (Boolean) newValue;
        if (TextUtils.equals(preference.getKey(),
                getString(R.string.preference_key_virus_lib_auto_update))) {
            Preferences.setVirusLibAutoUpdateEnabled(isChecked);
            if (isChecked) {
                SecurityCenterService.setVriusLibAutoUpdateAlarm(getApplicationContext());
            } else {
                SecurityCenterService.cancelVirusLibAutoUpdateAlarm(getApplicationContext());
            }
            return true;
        } else if (TextUtils.equals(preference.getKey(),
                getString(R.string.preference_key_open_virus_cloud_scan))) {
            Preferences.setVirusCloudScanEnabled(isChecked);
            return true;
        } else if (TextUtils.equals(preference.getKey(),
                getString(R.string.preference_key_open_virus_install_monitor))) {
            MiuiSettings.AntiVirus.setInstallMonitorEnabled(getApplicationContext(), isChecked);
            return true;
        }
        return false;
    }
}
