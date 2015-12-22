package com.miui.securitycenter;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager.NameNotFoundException;

public class DevModeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Context mSettingsContext = null;
        try {
            mSettingsContext = context.createPackageContext(SETTINGS_PACKAGE_NAME,
                    Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (mSettingsContext != null) {
            mSettingsContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit()
                    .putBoolean(PREF_SHOW, intent.getBooleanExtra(PREF_SHOW, false)).commit();
        }
    }

    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String PREF_SHOW = "show";
    private static final String PREF_FILE = "development";
}
