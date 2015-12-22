
package com.miui.securitycenter.manualitem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.miui.securitycenter.manualitem.ManualItemDbConstant;

public class ItemListDBHandler extends SQLiteOpenHelper {

    public ItemListDBHandler(Context context) {
        super(context, ManualItemDbConstant.DB_NAME, null, ManualItemDbConstant.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        createNewVersion(db);
    }

    private void createNewVersion(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        sb.append("create table ");
        sb.append(ManualItemDbConstant.ITEM_LIST_TABLE_NAME);
        sb.append(" (");
        sb.append(ManualItemDbConstant.ItemList.COLUMN_ITEM + " TEXT NOT NULL PRIMARY KEY,");
        sb.append(ManualItemDbConstant.ItemList.COLUMN_SCORE + " INTEGER, ");
        sb.append(ManualItemDbConstant.ItemList.COLUMN_WEIGHT + " INTEGER, ");
        sb.append(ManualItemDbConstant.ItemList.COLUMN_CHECKED + " INTEGER");
        sb.append(");");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("create table ");
        sb.append(ManualItemDbConstant.WHITE_LIST_TABLE_NAME);
        sb.append(" (");
        sb.append(ManualItemDbConstant.WhiteList._ID + " integer primary key autoincrement, ");
        sb.append(ManualItemDbConstant.WhiteList.COLUMN_ITEM + " TEXT, ");
        sb.append(ManualItemDbConstant.WhiteList.COLUMN_TITLE + " TEXT, ");
        sb.append(ManualItemDbConstant.WhiteList.COLUMN_SUMMARY + " TEXT, ");
        sb.append(ManualItemDbConstant.WhiteList.COLUMN_WEIGHT + " INTEGER");
        sb.append(");");
        db.execSQL(sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }

}
