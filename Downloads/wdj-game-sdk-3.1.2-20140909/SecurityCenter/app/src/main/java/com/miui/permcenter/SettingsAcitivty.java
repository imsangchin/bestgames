
package com.miui.permcenter;

import android.os.Bundle;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.MiuiIntent;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;

import miui.app.AlertDialog;
import miui.preference.PreferenceActivity;

import com.miui.securitycenter.R;


public class SettingsAcitivty extends PreferenceActivity implements OnPreferenceChangeListener {

    private CheckBoxPreference mAppPermissionControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pm_settings);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(MiuiIntent.EXTRA_SETTINGS_TITLE);
            if (!TextUtils.isEmpty(title)) {
                setTitle(title);
            }
        }

        mAppPermissionControl = (CheckBoxPreference) findPreference(getString(R.string.preference_key_app_permission_control));
        mAppPermissionControl.setOnPreferenceChangeListener(this);

    }

    private void setAppPermissionControlOpen(boolean isOpen) {
        if (isOpen) {
            PermissionUtils.setAppPermissionControlOpen(getApplicationContext(), true);
        } else {
            new AlertDialog.Builder(SettingsAcitivty.this)
                    .setTitle(R.string.permission_close_permission_control_dialog_title)
            .setMessage(R.string.permission_close_permission_control_dialog_msg)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PermissionUtils.setAppPermissionControlOpen(getApplicationContext(), false);
                                }
                            })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAppPermissionControl.setChecked(true);
                        }
                    })
                    .setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mAppPermissionControl.setChecked(true);
                        }
                    })
                    .show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAppPermissionControl.setChecked(PermissionUtils.isAppPermissionControlOpen(getApplicationContext()));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked = (Boolean) newValue;
        if (TextUtils.equals(preference.getKey(),
                getString(R.string.preference_key_app_permission_control))) {

            setAppPermissionControlOpen(isChecked);

            return true;
        } 
        return false;
    }
}
