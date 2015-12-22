
package com.miui.powercenter.provider;


import android.content.Context;
import android.net.Uri;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.miui.securitycenter.R;
import com.miui.powercenter.PowerCenter;

public final class PowerData {
    public static final String AUTHORITY = "com.miui.powercenter";
    public static final String MIUI_CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.miui";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.miui";



    // android系统内部变量
    public static final String[] SYSTEM_POWER_MODE_CPU_STATE = MiuiSettings.System.POWER_MODE_VALUES;

    public static class PowerMode {

        private static final String TAG = "PowerMode";

        public static final String TABLE_NAME = "custom_mode";
        public static final Uri EXTERNAL_URI = getContentUri(TABLE_NAME);

        public static interface Columns {
            public static final String ID = "_id";
            public static final String MODE_TITLE = "mode_title";
            public static final String MODE_SUMMARY = "mode_summary";
            public static final String MODE_NAME = "mode_name";
            public static final String CPU_STATE = "cpu_state";
            public static final String AUTO_CLEAN_MEMERY = "auto_clean_memory";
            public static final String BRIGHTNESS = "brightness";
            public static final String SLEEP = "sleep";
            public static final String WIFI = "wifi";
            public static final String INTERNET = "internet";
            public static final String VIBRATION = "vibration";
            public static final String BLUETOOTH = "bluetooth";
            public static final String SYNCHRONIZATION = "synchronization";
            public static final String GPS = "gps";
            public static final String TOUCH_WITH_VIBRATION = "touch_with_vibration";
            public static final String TOUCH_WITH_RING = "touch_with_ring";
            public static final String AIRPLANE_MODE = "airplane_mode";
        }

        public static final String DB_KEY[] = new String[] {
                Columns.ID,
                Columns.MODE_TITLE,
                Columns.MODE_SUMMARY,
                Columns.MODE_NAME,
                Columns.CPU_STATE,
                Columns.AUTO_CLEAN_MEMERY,
                Columns.BRIGHTNESS,
                Columns.SLEEP,
                Columns.AIRPLANE_MODE,
                Columns.WIFI,
                Columns.INTERNET,
                Columns.VIBRATION,
                Columns.BLUETOOTH,
                Columns.SYNCHRONIZATION,
                Columns.GPS,
                Columns.TOUCH_WITH_VIBRATION,
                Columns.TOUCH_WITH_RING
        };

        public static final int CPU_STATE_PERFORMANCE = 0;
        public static final int CPU_STATE_BALANCE = 1;
        public static final int CPU_STATE_SAVE = 2;

        public static final int AUTO_CLEAN_MEMORY_NEVER = -1;
        public static final int SLEEP_NEVER = 0;
        public static final int BRIGHTNESS_MODE_MANUAL = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        public static final int BRIGHTNESS_MODE_AUTOMATIC = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;

        public static final int SWITCH_OFF = 0;
        public static final int SWITCH_ON = 1;

        public static final int VIBRATION_NO = 0;
        public static final int VIBRATION_SLIENCE = 1;
        public static final int VIBRATION_YES = 2;

        // 用作Bundle的key, 如果是-1 的话，那么说明是需要新建一个mode
        public static final String KEY_POWER_MODE_ID = "key_power_mode_id";
        public static final String KEY_POWER_MODE_ID_USING = "key_power_mode_id_using";

        // 用作Preference的key
        public static final String KEY_ID = "0";
        public static final String KEY_TITLE = "1";
        public static final String KEY_SUMMARY = "2";
        public static final String KEY_NAME = "3";
        public static final String KEY_CPU_STATE = "4";
        public static final String KEY_AUTO_CLEAN_MEMORY = "5";
        public static final String KEY_BRIGHTNESS = "6";
        public static final String KEY_MODE_SLEEP = "7";
        public static final String KEY_AIRPLANE_MODE = "8";
        public static final String KEY_WIFI = "9";
        public static final String KEY_INTERNET = "10";
        public static final String KEY_VIBRATION = "11";
        public static final String KEY_BLUETOOTH = "12";
        public static final String KEY_SYNCHRONIZATION = "13";
        public static final String KEY_GPS = "14";
        public static final String KEY_TOUCH_VIBRATON = "15";
        public static final String KEY_TOUCH_RING = "16";

        public static final int INDEX_ID = 0;
        public static final int INDEX_TITLE = 1;
        public static final int INDEX_SUMMARY = 2;
        public static final int INDEX_NAME = 3;
        public static final int INDEX_CPU_STATE = 4;
        public static final int INDEX_AUTO_CLEAN_MEMORY = 5;
        public static final int INDEX_BRIGHTNESS = 6;
        public static final int INDEX_SLEEP = 7;
        public static final int INDEX_AIRPLANE_MODE = 8;
        public static final int INDEX_WIFI = 9;
        public static final int INDEX_INTERNET = 10;
        public static final int INDEX_VIBRATION = 11;
        public static final int INDEX_BLUETOOTH = 12;
        public static final int INDEX_SYNCHRONIZATION = 13;
        public static final int INDEX_GPS = 14;
        public static final int INDEX_TOUCH_VIBRATION = 15;
        public static final int INDEX_TOUCH_RING = 16;

        public static final String[] PreferenceKey = new String[] {
                null, // _id字段占位，不实际使用
                null, // mode_title字段占位，不实际使用
                null, // mode_summary字段占位，不实际使用
                KEY_NAME,
                KEY_CPU_STATE,
                KEY_AUTO_CLEAN_MEMORY,
                KEY_BRIGHTNESS,
                KEY_MODE_SLEEP,
                KEY_AIRPLANE_MODE,
                KEY_WIFI,
                KEY_INTERNET,
                KEY_VIBRATION,
                KEY_BLUETOOTH,
                KEY_SYNCHRONIZATION,
                KEY_GPS,
                KEY_TOUCH_VIBRATON,
                KEY_TOUCH_RING
        };

        public static final int[] PreferenceTitleId = new int[] {
                0, // _id字段占位，不实际使用
                0, // mode_title字段占位，不实际使用
                0, // mode_summary字段占位，不实际使用
                0, // mode_name字段占位，不实际使用
                R.string.power_mode_cpu_state,
                R.string.power_mode_auto_clean_memory,
                R.string.power_mode_screen_brightness,
                R.string.power_mode_sleep,
                R.string.power_mode_airplane_mode,
                R.string.power_mode_wifi,
                R.string.power_mode_internet,
                R.string.power_mode_vibration,
                R.string.power_mode_bluetooth,
                R.string.power_mode_synchronization,
                R.string.power_mode_gps,
                R.string.power_mode_touch_with_vibration,
                R.string.power_mode_touch_with_ring
        };

        public Object[] mDBValue = new Object[DB_KEY.length];

        public PowerMode() {
            mDBValue[0] = 0;
            for (int i = 4; i < mDBValue.length; ++i) {
                mDBValue[i] = 0;
            }
        }

        public PowerMode(PowerMode mode) {
            for (int i = 4; i < mDBValue.length; ++i) {
                mDBValue[i] = mode.mDBValue[i];
            }
        }

        public static int size() {
            return DB_KEY.length;
        }

        //把mode 设置成为当前的模式
        public static void setCurrentMode(PowerMode mode) {
            for (int i = 4; i < mode.mDBValue.length; ++i) {
                sCurrentMode.mDBValue[i] = mode.mDBValue[i];
            }
        }

        //这个用于当前应用状态的模式查询
        public static PowerMode sCurrentMode = new PowerMode();

        public static boolean isModeEqual(PowerMode mode1, PowerMode mode2) {
            boolean same = true;

            same = same && TextUtils.equals((String)mode1.mDBValue[1], (String)mode2.mDBValue[1]);
            same = same && TextUtils.equals((String)mode1.mDBValue[2], (String)mode2.mDBValue[2]);
            same = same && TextUtils.equals((String)mode1.mDBValue[3], (String)mode2.mDBValue[3]);

            same = same && (((Integer)mode1.mDBValue[4]).intValue() == ((Integer)mode2.mDBValue[4]).intValue());
            same = same && (((Integer)mode1.mDBValue[5]).intValue() == ((Integer)mode2.mDBValue[5]).intValue());
            same = same && (((Integer)mode1.mDBValue[6]).intValue() == ((Integer)mode2.mDBValue[6]).intValue());
            same = same && (((Integer)mode1.mDBValue[7]).intValue() == ((Integer)mode2.mDBValue[7]).intValue());
            same = same && (((Integer)mode1.mDBValue[8]).intValue() == ((Integer)mode2.mDBValue[8]).intValue());
            same = same && (((Integer)mode1.mDBValue[9]).intValue() == ((Integer)mode2.mDBValue[9]).intValue());
            same = same && (((Integer)mode1.mDBValue[10]).intValue() == ((Integer)mode2.mDBValue[10]).intValue());
            same = same && (((Integer)mode1.mDBValue[11]).intValue() == ((Integer)mode2.mDBValue[11]).intValue());
            same = same && (((Integer)mode1.mDBValue[12]).intValue() == ((Integer)mode2.mDBValue[12]).intValue());
            same = same && (((Integer)mode1.mDBValue[13]).intValue() == ((Integer)mode2.mDBValue[13]).intValue());
            same = same && (((Integer)mode1.mDBValue[14]).intValue() == ((Integer)mode2.mDBValue[14]).intValue());
            same = same && (((Integer)mode1.mDBValue[15]).intValue() == ((Integer)mode2.mDBValue[15]).intValue());
            same = same && (((Integer)mode1.mDBValue[16]).intValue() == ((Integer)mode2.mDBValue[16]).intValue());

            Log.d(TAG, "电源中心--POWERDATA 两个模式是否相等: " + same);
            return same;
        }

        public void apply(Context context) {
            PowerTaskManager manager = PowerTaskManager.getInstance(context);

            int value;
            value = getInt(4);

            manager.setCpuState(value); //CONTENT_URI
            value = getInt(5);
            manager.setAutoCleanMemorySeconds(value);
            value = getInt(6);
            manager.setBrightness(value); //CONTENT_URI
            value = getInt(7);
            manager.setSleepSeconds(value); //CONTENT_URI
            value = getInt(8);
            manager.setAirplaneMode(value == 1 ? true : false); //CONTENT_URI
            value = getInt(9);
            manager.setWifi(value == 1 ? true : false); //CONTENT_URI
            value = getInt(10);
            manager.setInternet(value == 1 ? true : false); //XXXX
            value = getInt(11);
            manager.setVibration(value); //CONTENT_URI
            value = getInt(12);
            manager.setBluetooth(value == 1 ? true : false); //CONTENT_URI

            value = getInt(13);
            manager.setSynchronization(value == 1 ? true : false);
            value = getInt(14);
            manager.setGPS(value == 1 ? true : false);
            value = getInt(15);
            manager.setTouchVibration(value == 1 ? true : false);
            value = getInt(16);
            manager.setTouchRing(value == 1 ? true : false);

            setCurrentMode(this);
        }

        public static Object[] retrieveCurrent(Context context) {
            Object[] objs = new Object[DB_KEY.length];
            PowerTaskManager manager = PowerTaskManager.getInstance(context);

            objs[4] = manager.getCpuStateIndex();
            objs[5] = manager.getAutoCleanMemorySeconds();
            objs[6] = manager.getBrightness();
            objs[7] = manager.getSleepSeconds();
            objs[8] = (manager.getAirplaneMode() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            objs[9] = (manager.getWifi() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            objs[10] = (manager.getInternet() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            objs[11] = manager.getVibration();
            objs[12] = (manager.getBluetooth() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            objs[13] = (manager.getSynchronization() ? PowerMode.SWITCH_ON
                    : PowerMode.SWITCH_OFF);
            objs[14] = (manager.getGPS() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            objs[15] = (manager.getTouchVibration() ? PowerMode.SWITCH_ON
                    : PowerMode.SWITCH_OFF);
            objs[16] = (manager.getTouchRing() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            return objs;
        }

        public void retrieve(Context context) {
            PowerTaskManager manager = PowerTaskManager.getInstance(context);

            mDBValue[4] = manager.getCpuStateIndex();
            mDBValue[5] = manager.getAutoCleanMemorySeconds();
            mDBValue[6] = manager.getBrightness();
            mDBValue[7] = manager.getSleepSeconds();
            mDBValue[8] = (manager.getAirplaneMode() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            mDBValue[9] = (manager.getWifi() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            mDBValue[10] = (manager.getInternet() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            mDBValue[11] = manager.getVibration();
            mDBValue[12] = (manager.getBluetooth() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            mDBValue[13] = (manager.getSynchronization() ? PowerMode.SWITCH_ON
                    : PowerMode.SWITCH_OFF);
            mDBValue[14] = (manager.getGPS() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
            mDBValue[15] = (manager.getTouchVibration() ? PowerMode.SWITCH_ON
                    : PowerMode.SWITCH_OFF);
            mDBValue[16] = (manager.getTouchRing() ? PowerMode.SWITCH_ON : PowerMode.SWITCH_OFF);
        }

        public int any_diffice_from_state(Context context) {
            Object[] tmp = retrieveCurrent(context);
            int p = -1;
            for (int i = 4; i<6; i++) {
                Log.i("lzftest", i+" "+tmp[i].toString()+" "+mDBValue[i].toString());
                if ((((Integer)tmp[i]).intValue()) != (((Integer)mDBValue[i]).intValue())) p = i;
            }
            int t6 =Math.abs((((Integer)tmp[6]).intValue()) - (((Integer)mDBValue[6]).intValue()));

            if (t6 > 1) {
                // Log.i("lzftest", 6+" "+tmp[6].toString()+" "+mDBValue[6].toString()+" 差别大");
                p = 6;
            } else {
                // Log.i("lzftest", 6+" "+tmp[6].toString()+" "+mDBValue[6].toString()+" 差别不大");
            }

            for (int i = 7; i < 17; i++) {
                // Log.i("lzftest", i+" "+tmp[i].toString()+" "+mDBValue[i].toString());
                if ((((Integer) tmp[i]).intValue()) != (((Integer) mDBValue[i]).intValue()))
                    p = i;
            }
            return p;
        }

        private int getInt(int index) {
            if (index < FIELD_START_INDEX) {
                Log.e(TAG, "error: cannot getInt(int index) for wrong index:" + index);
                return 0;
            }
            return Integer.parseInt(String.valueOf(mDBValue[index]));
        }

        private static int getInt(int index, Object[] objs) {
            if (index < FIELD_START_INDEX || index >= objs.length) {
                Log.e(TAG, "error: cannot getInt(int index) for wrong index:" + index);
                return 0;
            }

            if (TextUtils.isEmpty(String.valueOf(objs[index]))) {
                return 0;
            }

            return Integer.parseInt(String.valueOf(objs[index]));
        }
    }

    public static Uri getContentUri(String tableName) {
        return Uri.parse(MIUI_CONTENT_AUTHORITY_SLASH + tableName);
    }

    // 改成了4 个默认的省电模式
    private static final int DEFAULT_MODE_COUNT = 3;
    private static PowerMode[] defaultMode = null;

    public static int getDefaultModeCount() {
        return DEFAULT_MODE_COUNT;
    }

//    public static int getCustomModeIndex() {
//        return DEFAULT_MODE_COUNT - 1;
//    }

    // 每个模式详情页面的起始字段索引
    private static final int FIELD_START_INDEX = 3;

    public static int getFiledStartIndex() {
        return FIELD_START_INDEX;
    }

    public static PowerMode[] getDefaultModeArray(Context context) {
        if (defaultMode != null) {
            return refreshDefaultModeArray(context);
        }

        defaultMode = createDefaultModeArray(context);
        return defaultMode;
    }

//    public static PowerMode getCustomMode(Context context) {
//        if (defaultMode == null) {
//            defaultMode = createDefaultModeArray(context);
//        }
//        return defaultMode[DEFAULT_MODE_COUNT - 1];
//    }

    public static PowerMode[] createDefaultModeArray(Context context) {
        defaultMode = new PowerMode[DEFAULT_MODE_COUNT];

        // 智能省电模式
        defaultMode[0] = new PowerMode();
        defaultMode[0].mDBValue[0] = 0;
        defaultMode[0].mDBValue[1] = context.getString(R.string.power_chooser_smart_title);
        defaultMode[0].mDBValue[2] = context.getString(R.string.power_chooser_smart_summary);
        defaultMode[0].mDBValue[3] = context.getString(R.string.power_customizer_smart_mode);
        defaultMode[0].mDBValue[4] = PowerMode.CPU_STATE_BALANCE;
        defaultMode[0].mDBValue[5] = DataManager.AUTO_CLEAN_MEMORY_DELAYED_TIME_DEFAULT;
        defaultMode[0].mDBValue[6] = 50;
        defaultMode[0].mDBValue[7] = 30;
        defaultMode[0].mDBValue[8] = PowerMode.SWITCH_OFF;
        defaultMode[0].mDBValue[9] = PowerMode.SWITCH_ON;
        defaultMode[0].mDBValue[10] = PowerMode.SWITCH_ON;
        defaultMode[0].mDBValue[11] = PowerMode.VIBRATION_SLIENCE;
        defaultMode[0].mDBValue[12] = PowerMode.SWITCH_OFF;
        defaultMode[0].mDBValue[13] = PowerMode.SWITCH_ON;
        defaultMode[0].mDBValue[14] = PowerMode.SWITCH_OFF;
        defaultMode[0].mDBValue[15] = PowerMode.SWITCH_OFF;
        defaultMode[0].mDBValue[16] = PowerMode.SWITCH_OFF;


        // 超长待机模式
        defaultMode[1] = new PowerMode();
        defaultMode[1].mDBValue[0] = 1;
        defaultMode[1].mDBValue[1] = context.getString(R.string.power_chooser_standby_title);
        defaultMode[1].mDBValue[2] = context.getString(R.string.power_chooser_standby_summary);
        defaultMode[1].mDBValue[3] = context.getString(R.string.power_customizer_standby_mode);
        defaultMode[1].mDBValue[4] = PowerMode.CPU_STATE_BALANCE;
        defaultMode[1].mDBValue[5] = 300;
        defaultMode[1].mDBValue[6] = 10;
        defaultMode[1].mDBValue[7] = 30;
        defaultMode[1].mDBValue[8] = PowerMode.SWITCH_OFF;
        defaultMode[1].mDBValue[9] = PowerMode.SWITCH_OFF;
        defaultMode[1].mDBValue[10] = PowerMode.SWITCH_OFF;
        defaultMode[1].mDBValue[11] = PowerMode.VIBRATION_SLIENCE;
        defaultMode[1].mDBValue[12] = PowerMode.SWITCH_OFF;
        defaultMode[1].mDBValue[13] = PowerMode.SWITCH_ON;
        defaultMode[1].mDBValue[14] = PowerMode.SWITCH_OFF;
        defaultMode[1].mDBValue[15] = PowerMode.SWITCH_OFF;
        defaultMode[1].mDBValue[16] = PowerMode.SWITCH_OFF;


        // 睡眠模式
        defaultMode[2] = new PowerMode();
        defaultMode[2].mDBValue[0] = 2;
        defaultMode[2].mDBValue[1] = context.getString(R.string.power_chooser_sleep_title);
        defaultMode[2].mDBValue[2] = context.getString(R.string.power_chooser_sleep_summary);
        defaultMode[2].mDBValue[3] = context.getString(R.string.power_customizer_sleep_mode);
        defaultMode[2].mDBValue[4] = PowerMode.CPU_STATE_BALANCE;
        defaultMode[2].mDBValue[5] = 60;
        defaultMode[2].mDBValue[6] = 10;
        defaultMode[2].mDBValue[7] = 30;
        defaultMode[2].mDBValue[8] = PowerMode.SWITCH_ON;
        defaultMode[2].mDBValue[9] = PowerMode.SWITCH_OFF;
        defaultMode[2].mDBValue[10] = PowerMode.SWITCH_OFF;
        defaultMode[2].mDBValue[11] = PowerMode.VIBRATION_SLIENCE;
        defaultMode[2].mDBValue[12] = PowerMode.SWITCH_OFF;
        defaultMode[2].mDBValue[13] = PowerMode.SWITCH_ON;
        defaultMode[2].mDBValue[14] = PowerMode.SWITCH_OFF;
        defaultMode[2].mDBValue[15] = PowerMode.SWITCH_OFF;
        defaultMode[2].mDBValue[16] = PowerMode.SWITCH_OFF;

        for (int i = 0; i < defaultMode.length; ++i) {
            defaultMode[i].mDBValue[0] = i;
        }

        return defaultMode;
    }

    //防止简体/繁体/英文切换导致语言显示不正确
    public static PowerMode[] refreshDefaultModeArray(Context context) {
        // 智能省电模式
        defaultMode[0].mDBValue[0] = 0;
        defaultMode[0].mDBValue[1] = context.getString(R.string.power_chooser_smart_title);
        defaultMode[0].mDBValue[2] = context.getString(R.string.power_chooser_smart_summary);
        defaultMode[0].mDBValue[3] = context.getString(R.string.power_customizer_smart_mode);

        // 超长待机模式
        defaultMode[1].mDBValue[0] = 1;
        defaultMode[1].mDBValue[1] = context.getString(R.string.power_chooser_standby_title);
        defaultMode[1].mDBValue[2] = context.getString(R.string.power_chooser_standby_summary);
        defaultMode[1].mDBValue[3] = context.getString(R.string.power_customizer_standby_mode);

        // 睡眠模式
        defaultMode[2].mDBValue[0] = 2;
        defaultMode[2].mDBValue[1] = context.getString(R.string.power_chooser_sleep_title);
        defaultMode[2].mDBValue[2] = context.getString(R.string.power_chooser_sleep_summary);
        defaultMode[2].mDBValue[3] = context.getString(R.string.power_customizer_sleep_mode);

        return defaultMode;
    }
}
