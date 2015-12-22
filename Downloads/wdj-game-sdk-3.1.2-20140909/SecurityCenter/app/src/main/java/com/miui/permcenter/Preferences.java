
package com.miui.permcenter;

import android.content.ContentResolver;
import android.content.MiuiIntent;
import android.provider.MiuiSettings;

import com.miui.common.PreferenceStore;

public class Preferences {

    static final String pref_key_has_shown_auto_start_declare = "key_has_shown_auto_start_declare";

    public static void setHasShownAutoStartDeclare(boolean shown) {
        PreferenceStore.setPreferenceBoolean(pref_key_has_shown_auto_start_declare, shown);
    }

    public static boolean hasShownAutoStartDeclare() {
        return PreferenceStore.getPreferenceBoolean(pref_key_has_shown_auto_start_declare, false);
    }


}
