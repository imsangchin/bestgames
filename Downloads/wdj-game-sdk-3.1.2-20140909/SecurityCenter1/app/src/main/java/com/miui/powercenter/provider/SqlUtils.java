
package com.miui.powercenter.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.miui.powercenter.provider.PowerData.PowerMode;

import java.util.ArrayList;

/**
 * 这个类是从 content provider 里面读取数据
 *
 */
public final class SqlUtils {
    private static final String TAG = "SqlUtils";

    public static ArrayList<PowerMode> getModeList(Context context) {
        String[] projection = new String[] {
                PowerMode.Columns.ID,
                PowerMode.Columns.MODE_TITLE,
                PowerMode.Columns.MODE_SUMMARY,
                PowerMode.Columns.MODE_NAME,
                PowerMode.Columns.CPU_STATE,
                PowerMode.Columns.AUTO_CLEAN_MEMERY,
                PowerMode.Columns.BRIGHTNESS,
                PowerMode.Columns.SLEEP,
                PowerMode.Columns.AIRPLANE_MODE,
                PowerMode.Columns.WIFI,
                PowerMode.Columns.INTERNET,
                PowerMode.Columns.VIBRATION,
                PowerMode.Columns.BLUETOOTH,
                PowerMode.Columns.SYNCHRONIZATION,
                PowerMode.Columns.GPS,
                PowerMode.Columns.TOUCH_WITH_VIBRATION,
                PowerMode.Columns.TOUCH_WITH_RING,
        };
        String selection = PowerMode.Columns.ID + " > 0";
        Cursor cursor = context.getContentResolver().query(PowerMode.EXTERNAL_URI, projection,
                selection, null, null);
        ArrayList<PowerMode> modeList = null;
        if (cursor != null) {
            modeList = new ArrayList<PowerMode>();
            try {
                while (cursor.moveToNext()) {
                    PowerMode mode = new PowerMode();
                    mode.mDBValue[0] = cursor.getInt(0);
                    mode.mDBValue[1] = cursor.getString(1);
                    mode.mDBValue[2] = cursor.getString(2);
                    mode.mDBValue[3] = cursor.getString(3);
                    for (int i = 4; i<17; i++) {
                        mode.mDBValue[i] = cursor.getInt(i);
                    }
                    modeList.add(mode);
                }
            } finally {
                cursor.close();
            }
        }
        return modeList;
    }

    // 注意：数据库中的id从1开始
    public static PowerMode getModeById(Context context, int id) {
        String selection = PowerMode.Columns.ID + " = ?";
        String[] args = new String[] {
                String.valueOf(id)
        };
        Cursor cursor = context.getContentResolver().query(PowerMode.EXTERNAL_URI,
                PowerMode.DB_KEY, selection, args, null);
        PowerMode mode = null;
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    mode = new PowerMode();
                    mode.mDBValue[0] = cursor.getInt(0);
                    for (int i = 1; i < 4; ++i) {
                        mode.mDBValue[i] = cursor.getString(i);
                    }

                    for (int i = 4; i < PowerMode.size(); ++i) {
                        mode.mDBValue[i] = cursor.getInt(i);
                    }
                }
            } finally {
                cursor.close();
            }
        }

        //在OTA 升级的时候，可能发生mode ID 错乱的情况， 此时使用默认的模式
        if (mode == null) {
            PowerMode[] modeArray = PowerData.getDefaultModeArray(context);
            mode = modeArray[0];
        }
        return mode;
    }

    public static void insertMode(Context context, PowerMode mode) {
        Uri url = PowerMode.EXTERNAL_URI;
        ContentValues values = new ContentValues();
        for (int i = 1; i < 4; ++i) {
            values.put(PowerMode.DB_KEY[i], String.valueOf(mode.mDBValue[i]));
        }
        for (int i = 4; i < PowerMode.DB_KEY.length; ++i) {
            values.put(PowerMode.DB_KEY[i], Integer.parseInt(String.valueOf(mode.mDBValue[i])));
        }
        context.getContentResolver().insert(url, values);
    }

    public static void updateMode(Context context, PowerMode mode) {
        Uri uri = PowerMode.EXTERNAL_URI;
        ContentValues values = new ContentValues();
        for (int i = 1; i < 4; ++i) {
            values.put(PowerMode.DB_KEY[i], String.valueOf(mode.mDBValue[i]));
        }
        for (int i = 4; i < PowerMode.DB_KEY.length; ++i) {
            values.put(PowerMode.DB_KEY[i], Integer.parseInt(String.valueOf(mode.mDBValue[i])));
        }
        String where = PowerMode.Columns.ID + " = "
                + Integer.parseInt(String.valueOf(mode.mDBValue[PowerMode.INDEX_ID]));
        context.getContentResolver().update(uri, values, where, null);
    }

    public static void deleteModeById(Context context, int id) {
        Uri url = PowerMode.EXTERNAL_URI;
        String where = PowerMode.Columns.ID + " = ?";
        String[] args = new String[] {
                String.valueOf(id)
        };
        context.getContentResolver().delete(url, where, args);
    }

    public static ArrayList<PowerMode> getAllModes(Context context) {
        String selection = PowerMode.Columns.ID + " > 0";
        Cursor cursor = context.getContentResolver().query(PowerMode.EXTERNAL_URI,
                PowerMode.DB_KEY, selection, null, null);
        ArrayList<PowerMode> modeList = new ArrayList<PowerMode>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    PowerMode mode = new PowerMode();
                    mode.mDBValue[0] = cursor.getInt(0);
                    for (int i = 1; i < 4; ++i) {
                        mode.mDBValue[i] = cursor.getString(i);
                    }
                    for (int i = 4; i < PowerMode.size(); ++i) {
                        mode.mDBValue[i] = cursor.getInt(i);
                    }
                    modeList.add(mode);
                }
            } finally {
                cursor.close();
            }
        }
        return modeList;
    }

    public static ArrayList<String> getAllModeNames(Context context) {
        ArrayList<String> nameList = null;
        String[] projection = new String[] {
                PowerMode.Columns.MODE_NAME
        };
        String selection = PowerMode.Columns.ID + " > 0";
        Cursor cursor = context.getContentResolver().query(PowerMode.EXTERNAL_URI, projection,
                selection, null, null);
        if (cursor != null) {
            nameList = new ArrayList<String>();
            try {
                while (cursor.moveToNext()) {
                    nameList.add(cursor.getString(0));
                }
            } finally {
                cursor.close();
            }
        }
        return nameList;
    }
}
