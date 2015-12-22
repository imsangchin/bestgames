
package com.miui.antivirus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class WhiteListHelper {

    private static WhiteListHelper INST;

    private AntiVirusDBHelper mDBHelper;

    private WhiteListHelper(Context context) {
        mDBHelper = new AntiVirusDBHelper(context);
    }

    public static WhiteListHelper getInstance(Context context) {
        if (INST == null) {
            INST = new WhiteListHelper(context.getApplicationContext());
        }
        return INST;
    }

    public void insertAppsWhiteList(String pkgName) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AntiVirusColumns.COLUMN_PACKAGE_NAME, pkgName);
        db.insert(AntiVirusColumns.TABLE_APPS_WHITE_LIST, null, values);
    }

    public void clearWhiteList() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.delete(AntiVirusColumns.TABLE_APPS_WHITE_LIST, null, null);
        db.delete(AntiVirusColumns.TABLE_APK_WHITE_LIST, null, null);
    }

    public boolean inAppsWhiteList(String pkgName) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            String selection = AntiVirusColumns.COLUMN_PACKAGE_NAME + " = ? ";
            String[] selectionArgs = {
                    pkgName
            };
            cursor = db.query(AntiVirusColumns.TABLE_APPS_WHITE_LIST, null, selection,
                    selectionArgs, null,
                    null, null);
            if (cursor == null) {
                return false;
            }
            return cursor.getCount() != 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public void insertApkWhiteList(String apkPath) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AntiVirusColumns.COLUMN_APK_PATH, apkPath);
        db.insert(AntiVirusColumns.TABLE_APK_WHITE_LIST, null, values);
    }

    public void clearApkWhiteList() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.delete(AntiVirusColumns.TABLE_APK_WHITE_LIST, null, null);
    }

    public boolean inApkWhiteList(String apkPath) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            String selection = AntiVirusColumns.COLUMN_APK_PATH + " = ? ";
            String[] selectionArgs = {
                    apkPath
            };
            cursor = db.query(AntiVirusColumns.TABLE_APK_WHITE_LIST, null, selection,
                    selectionArgs, null,
                    null, null);
            if (cursor == null) {
                return false;
            }
            return cursor.getCount() != 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }
}

class AntiVirusColumns implements BaseColumns {

    public static final String DB_NAME = "anti_virus.db";

    public static final int DB_VERSION = 1;

    // 已安装的
    public static final String TABLE_APPS_WHITE_LIST = "t_apps_white_list";
    public static final String COLUMN_PACKAGE_NAME = "package_name";

    // 未安装的
    public static final String TABLE_APK_WHITE_LIST = "t_apk_white_list";
    public static final String COLUMN_APK_PATH = "apk_path";
}

class AntiVirusDBHelper extends SQLiteOpenHelper {

    public AntiVirusDBHelper(Context context) {
        super(context, AntiVirusColumns.DB_NAME, null, AntiVirusColumns.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("create table ");
        sb.append(AntiVirusColumns.TABLE_APPS_WHITE_LIST);
        sb.append(" (");
        sb.append(AntiVirusColumns._ID + " integer primary key autoincrement, ");
        sb.append(AntiVirusColumns.COLUMN_PACKAGE_NAME + " TEXT ");
        sb.append(");");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("create table ");
        sb.append(AntiVirusColumns.TABLE_APK_WHITE_LIST);
        sb.append(" (");
        sb.append(AntiVirusColumns._ID + " integer primary key autoincrement, ");
        sb.append(AntiVirusColumns.COLUMN_APK_PATH + " TEXT ");
        sb.append(");");
        db.execSQL(sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ignore
    }
}
