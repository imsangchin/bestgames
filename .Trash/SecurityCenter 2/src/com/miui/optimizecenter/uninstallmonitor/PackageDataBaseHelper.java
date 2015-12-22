
package com.miui.optimizecenter.uninstallmonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PackageDataBaseHelper extends SQLiteOpenHelper {

    public PackageDataBaseHelper(Context context) {
        super(context, PackageColumns.DB_NAME, null, PackageColumns.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("create table ");
        sb.append(PackageColumns.TABLE_PACKAGE);
        sb.append(" (");
        sb.append(PackageColumns._ID + " integer primary key autoincrement, ");
        sb.append(PackageColumns.COLUMN_APPLICATION_LABEL + " TEXT ,");
        sb.append(PackageColumns.COLUMN_PACKAGE_NAME + " TEXT ,");
        sb.append(PackageColumns.COLUMN_VERSION_NAME + " TEXT ,");
        sb.append(PackageColumns.COLUMN_VERSION_CODE + " INTEGER ,");
        sb.append(PackageColumns.COLUMN_IC_LAUNCHER + " BOLD");
        sb.append(");");
        db.execSQL(sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ignore
    }

}
