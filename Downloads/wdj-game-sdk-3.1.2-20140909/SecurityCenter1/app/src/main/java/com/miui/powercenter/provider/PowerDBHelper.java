
package com.miui.powercenter.provider;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.miui.powercenter.PowerCenter;
import com.miui.powercenter.provider.PowerData.PowerMode;

/**
 * Wrapper class for a specific database (associated with one particular
 * external card, or with internal storage). Can open the actual database on
 * demand, create and upgrade the schema, etc.
 */
public final class PowerDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "PowerDBHelper";

    public static final String DATABASE_NAME = "com_miui_powercenter.db";
    //3: 需要把原先的默认模式添加到数据库里面， 并且保留原先老版本的用户模式
    private static final int DATABASE_VERSION = 3;

    public PowerDBHelper(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    private void updateDatabase(SQLiteDatabase db, int fromVersion,
            int toVersion) {

        Log.i(TAG, "Database update: from " + fromVersion + " to " + toVersion);

        // sanity checks
        if (toVersion != DATABASE_VERSION) {
            Log.e(TAG, "Illegal update request. Got " + toVersion + ", expected "
                    + DATABASE_VERSION);
            throw new IllegalArgumentException();
        } else if (fromVersion > toVersion) {
            Log.e(TAG, "Illegal update request: can't downgrade from " + fromVersion + " to "
                    + toVersion + ". Did you forget to wipe data?");
            throw new IllegalArgumentException();
        }

        if (fromVersion < 1) {
            // Drop everything and start over.
            Log.i(TAG, "Upgrading media database from version " + fromVersion + " to " + toVersion
                    + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS " + PowerMode.TABLE_NAME);

            db.execSQL("CREATE TABLE IF NOT EXISTS " + PowerMode.TABLE_NAME + " ( "
                    + PowerMode.Columns.ID + "  INTEGER PRIMARY KEY,"
                    + PowerMode.Columns.MODE_TITLE + " TEXT, "
                    + PowerMode.Columns.MODE_SUMMARY + " TEXT, "
                    + PowerMode.Columns.MODE_NAME + " TEXT, "
                    + PowerMode.Columns.CPU_STATE + " INTEGER, "
                    + PowerMode.Columns.AUTO_CLEAN_MEMERY + " INTEGER, "
                    + PowerMode.Columns.BRIGHTNESS + " INTEGER, "
                    + PowerMode.Columns.SLEEP + " INTEGER, "
                    + PowerMode.Columns.WIFI + " INTEGER, "
                    + PowerMode.Columns.INTERNET + " INTEGER, "
                    + PowerMode.Columns.VIBRATION + " INTEGER, "
                    + PowerMode.Columns.BLUETOOTH + " INTEGER, "
                    + PowerMode.Columns.SYNCHRONIZATION + " INTEGER, "
                    + PowerMode.Columns.GPS + " INTEGER, "
                    + PowerMode.Columns.TOUCH_WITH_VIBRATION + " INTEGER, "
                    + PowerMode.Columns.TOUCH_WITH_RING + " INTEGER"
                    + ");");
            // createDefaultPowerModeRecords(db);
        }

        // 增加飞行模式字段
        if (fromVersion < 2) {
            Log.i(TAG, "update db from 1 to 2");
            db.execSQL("ALTER TABLE custom_mode ADD airplane_mode INTEGER DEFAULT 0");
        }

//        if (fromVersion < 3) {
//            Log.d(PowerCenter.DEBUG_TAG, "update db from 2 to 3");
//
//            Cursor cursor = db.query(PowerMode.TABLE_NAME, null, null, null, null,
//                    null,
//                    null, null);
//
//            ArrayList<ArrayList<String>> oldValues = new ArrayList<ArrayList<String>>();
//
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    ArrayList<String> currentRow = new ArrayList<String>();
//                    String title = cursor.getString(cursor.getColumnIndex(PowerMode.Columns.MODE_TITLE));
//                    String summary = cursor.getString(cursor.getColumnIndex(PowerMode.Columns.MODE_SUMMARY));
//                    String name = cursor.getString(cursor.getColumnIndex(PowerMode.Columns.MODE_NAME));
//
//                    String cpuState = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.CPU_STATE)));
//                    String autoCleanMemory = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.AUTO_CLEAN_MEMERY)));
//                    String brightNess = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.BRIGHTNESS)));
//                    String sleep = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.SLEEP)));
//                    String wifi = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.WIFI)));
//                    String internet = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.INTERNET)));
//                    String bluetooth = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.BLUETOOTH)));
//                    String synchronize = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.SYNCHRONIZATION)));
//                    String gps = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.GPS)));
//                    String touchWithVibration = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.TOUCH_WITH_VIBRATION)));
//                    String touchWithRing = String.valueOf(cursor.getInt(cursor.getColumnIndex(PowerMode.Columns.TOUCH_WITH_RING)));
//
//                    currentRow.add("");
//                    currentRow.add(title);
//                    currentRow.add(summary);
//                    currentRow.add(name);
//                    currentRow.add(cpuState);
//                    currentRow.add(autoCleanMemory);
//                    currentRow.add(brightNess);
//                    currentRow.add(sleep);
//                    currentRow.add(wifi);
//                    currentRow.add(internet);
//                    currentRow.add(bluetooth);
//                    currentRow.add(synchronize);
//                    currentRow.add(gps);
//                    currentRow.add(touchWithVibration);
//                    currentRow.add(touchWithRing);
//                }
//            }
//
//            //插入原有的所有的数据
//            Log.d(PowerCenter.DEBUG_TAG, "INSERT ALL data");
//            for (int i=0; i<oldValues.size(); i++) {
//                ArrayList<String> oneRow = oldValues.get(i);
//                ContentValues values = new ContentValues();
//
//                for (int j = 1; j < 4; ++j) {
//                    values.put(PowerMode.DB_KEY[j], String.valueOf(oneRow.get(j)));
//                }
//
//                for (int j = 4; j < PowerMode.DB_KEY.length; ++j) {
//                    values.put(PowerMode.DB_KEY[j], Integer.parseInt(String.valueOf(oneRow.get(j))));
//                }
//
//                db.insert(PowerMode.TABLE_NAME, PowerMode.Columns.MODE_NAME, values);
//            }
//        }
    }
}
