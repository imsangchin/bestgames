
package com.miui.powercenter.provider;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.miui.powercenter.PowerAdapterUtils;
import com.miui.powercenter.provider.PowerData.PowerMode;

import miui.util.AudioManagerHelper;

public class PowerTaskManager { // 如何消除交叉引用？？？防止manager为空？？？
    private static final String TAG = "PowerTaskManager";

    private Context mContext;
    private static PowerTaskManager mInstance;

    public static PowerTaskManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PowerTaskManager(context.getApplicationContext());
        }
        return mInstance;
    }

    private PowerTaskManager(Context context) {
        mContext = context;
    }

    public void setCpuState(int cpuIndex) {
        // 在v6 上面，可能会删除掉某些值
        if (cpuIndex >= MiuiSettings.System.POWER_MODE_VALUES.length) {
            cpuIndex = MiuiSettings.System.POWER_MODE_VALUES.length - 1;
            if (cpuIndex < 0)
                return;
        }
        if (SystemProperties.get(MiuiSettings.System.POWER_MODE_KEY_PROPERTY,
                MiuiSettings.System.POWER_MODE_VALUE_DEFAULT) == null) return;

        String cpuValue = MiuiSettings.System.POWER_MODE_VALUES[cpuIndex];
        SystemProperties.set(MiuiSettings.System.POWER_MODE_KEY_PROPERTY, cpuValue);
        System.putString(mContext.getContentResolver(), MiuiSettings.System.POWER_MODE, cpuValue);
    }

    public int getCpuStateIndex() {
        String cpuValue = SystemProperties.get(MiuiSettings.System.POWER_MODE_KEY_PROPERTY,
                MiuiSettings.System.POWER_MODE_VALUE_DEFAULT);
        if (cpuValue == null) return -1;
        String[] values = MiuiSettings.System.POWER_MODE_VALUES;
        for (int i = 0; i < values.length; ++i) {
            if (TextUtils.equals(cpuValue, values[i])) {
                return i;
            }
        }
        Log.e(TAG, "error: cannot get cpu value index right!");
        return 0;
    }

    /***
     * 锁屏后自动清理内存 1.检测到锁屏信号 2.保存锁屏后清理内存的时间，使用SharedPreference
     */
    public void setAutoCleanMemorySeconds(int seconds) {
        DataManager manager = DataManager.getInstance(mContext);
        manager.putInt(DataManager.KEY_AUTO_CLEAN_MEMORY_DELAYED_TIME, seconds);
    }

    public int getAutoCleanMemorySeconds() {
        DataManager manager = DataManager.getInstance(mContext);
        int seconds = manager.getInt(DataManager.KEY_AUTO_CLEAN_MEMORY_DELAYED_TIME,
                DataManager.AUTO_CLEAN_MEMORY_DELAYED_TIME_DEFAULT);
        return seconds;
    }

    /***
     * 屏幕亮度设置 自动调整亮度 开启{0，100} 关闭{-100,0}
     */

    private static final int BRIGHTNESS_RANGE = 100;
    private static final int MAXIMUM_BACKLIGHT = android.os.PowerManager.BRIGHTNESS_ON;

    public void setBrightness(int brightness) {
        final ContentResolver resolver = mContext.getContentResolver();
        int mode;
        if (brightness > 0) {
            mode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
            float value = (2.0f * brightness) / BRIGHTNESS_RANGE - 1.0f;
            Settings.System.putFloat(resolver, Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, value);

            IPowerManager power = IPowerManager.Stub
                    .asInterface(ServiceManager.getService("power"));
            if (power != null) {
                PowerAdapterUtils.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(power,
                        value);
            }

        } else {
            mode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
            brightness = 0 - brightness;
            --brightness;
            int brightnessDim = mContext.getResources().getInteger(
                    android.miui.R.integer.android_config_screenBrightnessDim);
            int value = (MAXIMUM_BACKLIGHT - brightnessDim) * brightness / BRIGHTNESS_RANGE
                    + brightnessDim;
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, value);

            IPowerManager power = IPowerManager.Stub
                    .asInterface(ServiceManager.getService("power"));
            if (power != null) {
                PowerAdapterUtils.setTemporaryScreenBrightnessSettingOverride(power, value);
            }
        }
    }

    public int getBrightness() {
        int mode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        mode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);

        float brightness = 0;
        int brightnessDim = mContext.getResources().getInteger(
                android.miui.R.integer.android_config_screenBrightnessDim);
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            brightness = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, 0);
            brightness = (brightness + 1) / 2;
            return (int) (brightness * BRIGHTNESS_RANGE);
        } else {
            brightness = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, 100);
            brightness = (brightness - brightnessDim)
                    / (MAXIMUM_BACKLIGHT - brightnessDim);
            return (int) (0 - (brightness * BRIGHTNESS_RANGE));
        }
    }

    // 无操作后休眠时间设置
    public void setSleepSeconds(int seconds) {
        long mm = seconds * DateUtils.SECOND_IN_MILLIS;
        if (mm == 0) {
            mm = Integer.MAX_VALUE;
        }
        Settings.System.putLong(mContext.getContentResolver(), SCREEN_OFF_TIMEOUT, mm);
    }

    public int getSleepSeconds() {
        long mm = Settings.System.getLong(mContext.getContentResolver(), SCREEN_OFF_TIMEOUT,
                Integer.MAX_VALUE);
        if (mm == Integer.MAX_VALUE) {
            return PowerMode.SLEEP_NEVER;
        }
        return (int) (mm / DateUtils.SECOND_IN_MILLIS);
    }

    // WIFI开关设置
    public void setWifi(boolean isEnabled) {
        Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.WIFI_ON,
                isEnabled ? 1 : 0);
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(isEnabled);
    }

    public boolean getWifi() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    // 2G/3G上网设置
    public void setInternet(boolean isEnabled) {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.setMobileDataEnabled(isEnabled);
    }

    public boolean getInternet() {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getMobileDataEnabled();
    }

    // 振动设置
    public void setVibration(int value) {
        switch (value) {
            case 0:
                AudioManagerHelper.setVibrateSetting(mContext, false, true);
                AudioManagerHelper.setVibrateSetting(mContext, false, false);
                break;
            case 1:
                AudioManagerHelper.setVibrateSetting(mContext, true, true);
                AudioManagerHelper.setVibrateSetting(mContext, false, false);
                break;
            case 2:
                AudioManagerHelper.setVibrateSetting(mContext, true, true);
                AudioManagerHelper.setVibrateSetting(mContext, true, false);
                break;
            default:
                break;
        }
    }

    public int getVibration() {
        boolean vibrateWhenSilent = MiuiSettings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.VIBRATE_IN_SILENT, true);
        boolean vibrateWhenRing = MiuiSettings.System.getBoolean(mContext.getContentResolver(),
                MiuiSettings.System.VIBRATE_IN_NORMAL,
                MiuiSettings.System.VIBRATE_IN_NORMAL_DEFAULT);
        // 仅在静音时振动
        if (vibrateWhenSilent && !vibrateWhenRing) {
            return 1;
        }
        // 振动（静音时振动 && 响铃时振动）
        if (vibrateWhenSilent && vibrateWhenRing) {
            return 2;
        }
        // 仅在响铃时振动，目前UI上暂不支持
        // if (!vibrateWhenSilent && vibrateWhenRing) {
        // return 3;
        // }
        return 0;
    }

    // 蓝牙设置
    public void setBluetooth(boolean isEnabled) {
        Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.BLUETOOTH_ON,
                isEnabled ? 1 : 0);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (isEnabled) {
            if (!adapter.isEnabled()) {
                adapter.enable();
            }
        } else {
            if (adapter.isEnabled()) {
                adapter.disable();
            }
        }
    }

    public boolean getBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.isEnabled();
    }

    // 同步设置
    public void setSynchronization(boolean isEnabled) {
        ContentResolver.setMasterSyncAutomatically(isEnabled);
    }

    public boolean getSynchronization() {
        return ContentResolver.getMasterSyncAutomatically();
    }

    // GPS设置
    public void setGPS(boolean isEnabled) {
        Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(),
                LocationManager.GPS_PROVIDER, isEnabled);
    }

    public boolean getGPS() {
        String str = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        Log.d(TAG, "gps: " + str);
        if (str != null) {
            return str.contains("gps");
        }
        return false;
    }

    // 触摸时振动
    public void setTouchVibration(boolean isEnabled) {
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, isEnabled ? 1 : 0);
    }

    public boolean getTouchVibration() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0;
    }

    // 触摸时提示音
    public void setTouchRing(boolean isEnabled) {
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.SOUND_EFFECTS_ENABLED,
                isEnabled ? 1 : 0);
        AudioManager mAudioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        if (isEnabled) {
            mAudioManager.loadSoundEffects();
        } else {
            mAudioManager.unloadSoundEffects();
        }
    }

    public boolean getTouchRing() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SOUND_EFFECTS_ENABLED, 0) != 0;
    }

    public void setAirplaneMode(boolean isEnabled) {
        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                isEnabled ? 1 : 0);

        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", isEnabled);
        mContext.sendBroadcast(intent);
    }

    public boolean getAirplaneMode() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

}
