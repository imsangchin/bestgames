
package com.miui.powercenter.provider;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.BatteryManager;

public class BatteryInfo {
    private static final String TAG = "BatteryInfo";

    private static final String BATTERY_INFO = "battery_info_settings";
    public static final String BATTERY_INFO_PERCENT = "battery_info_settings_percent";
    public static final String BATTERY_INFO_STATE = "battery_info_settings_state";
    public static final String BATTERY_INFO_STANDBY_TIME = "battery_info_settings_standby_time";
    public static final String BATTERY_INFO_CHARGE_TIME = "battery_info_settings_charge_time";

    private static final long CHARGE_TIME_DEFAULT = 3 * 60 * 60 * 1000;
    private static final long STANDBY_TIME_DEFAULT = 2 * 24 * 60 * 60 * 1000;

    private static final long CHARGE_TIME_LIMIT_START = 1 * 60 * 60 * 1000;
    private static final long CHARGE_TIME_LIMIT_END = 12 * 60 * 60 * 1000;

    private static final long STANDBY_TIME_LIMIT_START = 6 * 60 * 60 * 1000;
    private static final long STANDBY_TIME_LIMIT_END = 3 * 24 * 60 * 60 * 1000;

    private SharedPreferences mBatteryInfo;

    private static BatteryInfo mInstance;

    private BatteryInfo(Context context) {
        mBatteryInfo = context.getSharedPreferences(BATTERY_INFO, 0);
    }

    public static BatteryInfo getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BatteryInfo(context.getApplicationContext());
        }
        return mInstance;
    }

    public void putBatteryPercent(int value) {
        Editor editor = mBatteryInfo.edit();
        editor.putInt(BATTERY_INFO_PERCENT, value);
        editor.commit();
    }

    public int getBatteryPercent(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = (int) (level * 100 / (float) scale);
        return batteryPct;
    }

    public void putBatteryState(int value) {
        Editor editor = mBatteryInfo.edit();
        editor.putInt(BATTERY_INFO_STATE, value);
        editor.commit();
    }

    public int getBatteryState(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status;
    }

    public void putBatteryStandbyTime(long value) {
        if (value < STANDBY_TIME_LIMIT_START) {
            value = STANDBY_TIME_LIMIT_START;
        } else if (value > STANDBY_TIME_LIMIT_END) {
            value = STANDBY_TIME_LIMIT_END;
        }
        Editor editor = mBatteryInfo.edit();
        editor.putLong(BATTERY_INFO_STANDBY_TIME, value);
        editor.commit();
    }

    public long getBatteryStandbyTime() {
        return mBatteryInfo.getLong(BATTERY_INFO_STANDBY_TIME, STANDBY_TIME_DEFAULT);
    }

    public long getBatteryStandbyTimeDefault() {
        return STANDBY_TIME_DEFAULT;
    }

    public void putBatteryChargeTime(long value) {
        if (value < CHARGE_TIME_LIMIT_START) {
            value = CHARGE_TIME_LIMIT_START;
        } else if (value > CHARGE_TIME_LIMIT_END) {
            value = CHARGE_TIME_LIMIT_END;
        }
        Editor editor = mBatteryInfo.edit();
        editor.putLong(BATTERY_INFO_CHARGE_TIME, value);
        editor.commit();
    }

    public long getBatteryChargeTime() {
        return mBatteryInfo.getLong(BATTERY_INFO_CHARGE_TIME, CHARGE_TIME_DEFAULT);
    }

    public long getBatteryChargeTimeDefault() {
        return CHARGE_TIME_DEFAULT;
    }

    public void registerOnBatteryInfoChanged(OnSharedPreferenceChangeListener l) {
        mBatteryInfo.registerOnSharedPreferenceChangeListener(l);
    }

    public void unregisterOnBatteryInfoChanged(OnSharedPreferenceChangeListener l) {
        mBatteryInfo.unregisterOnSharedPreferenceChangeListener(l);
    }
}
