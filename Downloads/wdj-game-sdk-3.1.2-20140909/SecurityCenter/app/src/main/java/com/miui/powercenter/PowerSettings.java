
package com.miui.powercenter;

import android.content.Intent;
import android.content.MiuiIntent;;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;

import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;

import miui.preference.PreferenceActivity;
import miui.preference.RadioButtonPreference;
import miui.preference.RadioButtonPreferenceCategory;

public class PowerSettings extends PreferenceActivity {
    private static final String TAG = "PowerSettings";

    private static final String KEY_RADIO_GROUP = "key_radio_group";
    private static final String KEY_GRAPHIC = "key_shape";
    private static final String KEY_NUMBER = "key_number";
    private static final String KEY_TOP = "key_top";

    private RadioButtonPreferenceCategory mRadioGroup;
    private RadioButtonPreference mGraphic;
    private RadioButtonPreference mNumber;
    private RadioButtonPreference mTop;

    private OnPreferenceClickListener mListener = new OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            int value = 0;
            if (TextUtils.equals(key, KEY_GRAPHIC)) {
                value = 0;
            } else if (TextUtils.equals(key, KEY_NUMBER)) {
                value = 1;
            } else if (TextUtils.equals(key, KEY_TOP)) {
                value = 2;
            }
            Settings.System.putInt(PowerSettings.this.getContentResolver(),
                    MiuiSettings.System.BATTERY_INDICATOR_STYLE, value);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String title = null;
        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getStringExtra(MiuiIntent.EXTRA_SETTINGS_TITLE);
            if (!TextUtils.isEmpty(title)) {
                setTitle(title);
            }
        }

        if (TextUtils.isEmpty(title)) {
            AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ENTER_POWER_MANAGER_SETTINGS);
        }

        addPreferencesFromResource(R.xml.pc_power_settings);

        init();
    }

    @Override
    protected void onResume() {
        refresh();
        super.onResume();
    }

    private void init() {
        mRadioGroup = (RadioButtonPreferenceCategory) findPreference(KEY_RADIO_GROUP);
        mGraphic = (RadioButtonPreference) findPreference(KEY_GRAPHIC);
        mNumber = (RadioButtonPreference) findPreference(KEY_NUMBER);
        mTop = (RadioButtonPreference) findPreference(KEY_TOP);
        mGraphic.setOnPreferenceClickListener(mListener);
        mNumber.setOnPreferenceClickListener(mListener);
        mTop.setOnPreferenceClickListener(mListener);

        refresh();
    }

    private void refresh() {
        int value = Settings.System.getInt(getContentResolver(),
                MiuiSettings.System.BATTERY_INDICATOR_STYLE, 0);
        mRadioGroup.setCheckedPosition(value);
    }
}
