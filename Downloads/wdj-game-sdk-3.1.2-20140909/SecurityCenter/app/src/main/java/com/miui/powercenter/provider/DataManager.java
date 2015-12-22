
package com.miui.powercenter.provider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.miui.securitycenter.RestoreHelper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class DataManager {
    private static final String TAG = "SharedPreferencesManager";

    private static final String POWER_DATA_SETTINGS = "power_data_settings";

    public static final String KEY_LOW_BATTERY_ENABLED = "power_save_low_battery_enabled_2";
    public static final String KEY_LOW_BATTERY_PERCENTAGE = "power_save_low_battery_percentage_2";

    //当进入低电模式时， 我们选择的模式
    public static final String KEY_LOW_BATTERY_SELECTED = "power_save_low_battery_selected_2";
    //当出了低电模式时，我们选择的模式
    public static final String KEY_LOW_BATTERY_RECOVERY_SELECTED = "power_save_low_battery_recovery_selected_2";

    public static final String KEY_LOW_BATTERY_INUSE = "power_save_low_battery_inuse_2";
    //这里存储的是我们当前的应用模式的状态，可能是
    public static final String KEY_ON_TIME_ENABLED = "power_save_on_time_enabled_2";
    public static final String KEY_ON_TIME_START_HOUR = "power_save_on_time_start_hour_2";
    public static final String KEY_ON_TIME_START_MINUTE = "power_save_on_time_start_minute_2";
    public static final String KEY_ON_TIME_END_HOUR = "power_save_on_time_end_hour_2";
    public static final String KEY_ON_TIME_END_MINUTE = "power_save_on_time_end_minute_2";
    public static final String KEY_ON_TIME_SELECTED = "power_save_on_time_selected_2";
    //当on time 结束的时候，我们需要进入的模式
    public static final String KEY_ON_TIME_RECOVERY_SELECTED = "power_save_on_time_goout_selected_2";
    public static final String KEY_ON_TIME_INUSE    = "power_save_on_time_inuse_2";

    //这个是当前应用的状态，如果用户手工进行了更改，那么我们不管
    public static final String KEY_POWER_MODE_APPLIED = "power_mode_applied_2";
    public static final int POWER_MODE_APPLIED_DEFAULT = -1;

    //如果我们手工的选择了某一个模式，那么我们将会进入手工模式，就算这时低电可以，也不进入低电模式，需要充满电后再
    //进入低电时，进入到低电模式
    public static final String POWER_MODE_MANUAL_LOWBATTERY = "power_mode_manual_lowbattery_2";

    public static final String KEY_AUTO_CLEAN_MEMORY_DELAYED_TIME = "auto_clean_memory_delayed_time_2";

    //当我们第一次点击手工的模式的时候，我们会保存当前的状态， 并且创建一个我的模式
    public static final String KEY_FIRST_APPLY_MANUAL_MODE = "key_first_apply_manual_mode";
    public static final int AUTO_CLEAN_MEMORY_DELAYED_TIME_DEFAULT = -1;

    public static final boolean LOW_BATTERY_ENABLED_DEFAULT = false;
    public static final int LOW_BATTERY_PERCENTAGE_DEFAULT = 20;
    public static final int LOW_BATTERY_SELECTED_DEFAULT = 1;
    public static final int LOW_BATTERY_RECOVERY_DEFAULT = 0;
    public static final int BATTERY_SAVE_SELECTED_NONE = -2;

    public static final boolean ON_TIME_ENABLED_DEFAULT = false;
    public static final int ON_TIME_START_HOUR_DEFAULT = 0;
    public static final int ON_TIME_START_MINUTE_DEFAULT = 50;
    public static final int ON_TIME_END_HOUR_DEFAULT = 4;
    public static final int ON_TIME_END_MINUTE_DEFAULT = 50;
    public static final int ON_TIME_SELECTED_DEFAULT = 2;
    public static final int ON_TIME_RECOVERY_DEFAULT = 0;

    public static final int BOOT_TIME_DEFAULT=420;
    public static final int BOOT_REPEAT_DEFAULT=0x7f;
    public static final int SHUTDOWN_TIME_DEFAULT=1410;
    public static final int SHUTDOWN_REPEAT_DEFAULT=0x7f;

    //充电时退出低电状态
    public static final String KEY_EXIT_LOWBATTERY_WHENCHARGE = "power_exit_lowbattery_whencharge";

    //当前用户手工选定的省电模式
    public static final String KEY_MODE_WHICH_USER_CHOOSE = "power_mode_which_user_choose";

    private SharedPreferences mSharedPreferences;
    private static DataManager mInstance;
    private DataManager(Context context) {
        mSharedPreferences = context.getSharedPreferences(POWER_DATA_SETTINGS, 0);
    }

    public static DataManager getInstance(Context context) {
        if(mInstance == null) {
            Log.d(RestoreHelper.TAG, "Data manager is initilized");
            RestoreHelper.restoreData();
            Log.d(RestoreHelper.TAG, "After copying is finished");
            mInstance = new DataManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public String getString(String key, String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    public void putString(String key, String value) {
        Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public int getInt(String key, int defValue) {
        return mSharedPreferences.getInt(key, defValue);
    }

    public void putInt(String key, int value) {
        Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }

    public void putBoolean(String key, boolean value) {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void putLong(String key, long value) {
        Editor editor = mSharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public long getLong(String key, long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }

    public void putStringMap(HashMap<String,String> map) {
        Editor editor = mSharedPreferences.edit();
        Iterator<Entry<String,String>> itor = map.entrySet().iterator();
        while(itor.hasNext()) {
            Entry<String,String> entry = itor.next();
            editor.putString(entry.getKey(), entry.getValue());
        }

        editor.commit();
    }

    public String[] getValuesByKeys(String prefix, String[] keys) {
        String[] objs = new String[keys.length];
        for (int i=0; i<keys.length; i++) {
            objs[i] = mSharedPreferences.getString(prefix + keys[i], "");
        }

        return objs;
    }
}