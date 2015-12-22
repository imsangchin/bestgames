
package com.miui.optimizecenter.whitelist;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WhiteListDBHelper extends SQLiteOpenHelper {

    public WhiteListDBHelper(Context context) {
        super(context, WhiteListColumns.DB_NAME, null, WhiteListColumns.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createNewVersion(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
        if (version == 1) {
            updateFromVersion1To2(db);
            version = 2;
        }

        if (version == 2) {
            updateFromVersion2To3(db);
        }
    }

    private void updateFromVersion1To2(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS table_cache");
        db.execSQL("DROP TABLE IF EXISTS table_ad");
        db.execSQL("DROP TABLE IF EXISTS table_apk");
        db.execSQL("DROP TABLE IF EXISTS table_residual");
        db.execSQL("DROP TABLE IF EXISTS table_large_file");

        StringBuilder sb = new StringBuilder();
        sb.append("create table ");
        sb.append(WhiteListColumns.TABLE_CACHE_WHITE_LIST);
        sb.append(" (");
        sb.append(WhiteListColumns.Cache._ID + " integer primary key autoincrement, ");
        sb.append(WhiteListColumns.Cache.COLUMN_CACHE_TYPE + " TEXT ,");
        sb.append(WhiteListColumns.Cache.COLUMN_DIR_PATH + " TEXT ,");
        sb.append(WhiteListColumns.Cache.COLUMN_PKG_NAME + " TEXT ,");
        sb.append(WhiteListColumns.Cache.COLUMN_ALERT_INFO + " TEXT ,");
        sb.append(WhiteListColumns.Cache.COLUMN_DESC + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("create table ");
        sb.append(WhiteListColumns.TABLE_AD_WHITE_LIST);
        sb.append(" (");
        sb.append(WhiteListColumns.Ad._ID + " integer primary key autoincrement, ");
        sb.append(WhiteListColumns.Ad.COLUMN_NAME + " TEXT ,");
        sb.append(WhiteListColumns.Ad.COLUMN_DIR_PATH + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("create table ");
        sb.append(WhiteListColumns.TABLE_RESIDUAL_WHITE_LIST);
        sb.append(" (");
        sb.append(WhiteListColumns.Residual._ID + " integer primary key autoincrement, ");
        sb.append(WhiteListColumns.Residual.COLUMN_DESC_NAME + " TEXT ,");
        sb.append(WhiteListColumns.Residual.COLUMN_DIR_PATH + " TEXT ,");
        sb.append(WhiteListColumns.Residual.COLUMN_ALERT_INFO + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("create table ");
        sb.append(WhiteListColumns.TABLE_APK_WHITE_LIST);
        sb.append(" (");
        sb.append(WhiteListColumns.Apk._ID + " integer primary key autoincrement, ");
        sb.append(WhiteListColumns.Apk.COLUMN_DIR_PATH + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("create table ");
        sb.append(WhiteListColumns.TABLE_LARGE_FILE_WHITE_LIST);
        sb.append(" (");
        sb.append(WhiteListColumns.LargeFile._ID + " integer primary key autoincrement, ");
        sb.append(WhiteListColumns.LargeFile.COLUMN_DIR_PATH + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());
    }

    private void updateFromVersion2To3(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + WhiteListColumns.TABLE_APK_WHITE_LIST
                + " ADD COLUMN " + WhiteListColumns.Apk.COLUMN_APP_NAME
                + " TEXT");
    }

    private void createNewVersion(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("create table ");
        sb.append(WhiteListColumns.TABLE_CACHE_WHITE_LIST);
        sb.append(" (");
        sb.append(WhiteListColumns.Cache._ID + " integer primary key autoincrement, ");
        sb.append(WhiteListColumns.Cache.COLUMN_CACHE_TYPE + " TEXT ,");
        sb.append(WhiteListColumns.Cache.COLUMN_DIR_PATH + " TEXT ,");
        sb.append(WhiteListColumns.Cache.COLUMN_PKG_NAME + " TEXT ,");
        sb.append(WhiteListColumns.Cache.COLUMN_ALERT_INFO + " TEXT ,");
        sb.append(WhiteListColumns.Cache.COLUMN_DESC + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("create table ");
        sb.append(WhiteListColumns.TABLE_AD_WHITE_LIST);
        sb.append(" (");
        sb.append(WhiteListColumns.Ad._ID + " integer primary key autoincrement, ");
        sb.append(WhiteListColumns.Ad.COLUMN_NAME + " TEXT ,");
        sb.append(WhiteListColumns.Ad.COLUMN_DIR_PATH + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("create table ");
        sb.append(WhiteListColumns.TABLE_RESIDUAL_WHITE_LIST);
        sb.append(" (");
        sb.append(WhiteListColumns.Residual._ID + " integer primary key autoincrement, ");
        sb.append(WhiteListColumns.Residual.COLUMN_DESC_NAME + " TEXT ,");
        sb.append(WhiteListColumns.Residual.COLUMN_DIR_PATH + " TEXT ,");
        sb.append(WhiteListColumns.Residual.COLUMN_ALERT_INFO + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("create table ");
        sb.append(WhiteListColumns.TABLE_APK_WHITE_LIST);
        sb.append(" (");
        sb.append(WhiteListColumns.Apk._ID + " integer primary key autoincrement, ");
        sb.append(WhiteListColumns.Apk.COLUMN_APP_NAME + " TEXT ,");
        sb.append(WhiteListColumns.Apk.COLUMN_DIR_PATH + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("create table ");
        sb.append(WhiteListColumns.TABLE_LARGE_FILE_WHITE_LIST);
        sb.append(" (");
        sb.append(WhiteListColumns.LargeFile._ID + " integer primary key autoincrement, ");
        sb.append(WhiteListColumns.LargeFile.COLUMN_DIR_PATH + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());
    }

}
