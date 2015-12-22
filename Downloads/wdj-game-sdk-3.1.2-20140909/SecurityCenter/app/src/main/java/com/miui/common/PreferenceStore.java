
package com.miui.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class PreferenceStore {

    private static SharedPreferences sPref;
    private static Resources mResources;

    public static void init(Context context) {
        if (sPref == null || mResources == null) {
            mResources = context.getResources();
            sPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }
    }

    public static Resources getResources() {
        return mResources;
    }

    public static void setPreferenceBoolean(String key, boolean value) {
        sPref.edit().putBoolean(key, value).commit();
    }

    public static boolean getPreferenceBoolean(String key, boolean defValue) {
        return sPref.getBoolean(key, defValue);
    }

    public static void setPreferenceString(String key, String value) {
        sPref.edit().putString(key, value).commit();
    }

    public static String getPreferenceString(String key, String defValue) {
        return sPref.getString(key, defValue);
    }

    public static void setPreferenceInt(String key, int value) {
        sPref.edit().putInt(key, value).commit();
    }

    public static int getPreferenceInt(String key, int defValue) {
        return sPref.getInt(key, defValue);
    }

    public static void setPreferenceLong(String key, long value) {
        sPref.edit().putLong(key, value).commit();
    }

    public static long getPreferenceLong(String key, long defValue) {
        return sPref.getLong(key, defValue);
    }

    public static void setPreferenceFloat(String key, float value) {
        sPref.edit().putFloat(key, value).commit();
    }

    public static float getPreferenceFloat(String key, float defValue) {
        return sPref.getFloat(key, defValue);
    }
}
