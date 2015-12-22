
package com.miui.securitycenter.manualitem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import miui.os.Build;
import com.miui.common.IOUtils;
import com.miui.securitycenter.Preferences;
import com.miui.securitycenter.handlebar.HandleItem;

import java.util.ArrayList;
import java.util.List;

public class WhiteListManager {

    private static WhiteListManager INST;
    private List<String> mWhiteList = new ArrayList<String>();
    private ItemListDBHandler mDBHandler;

    public WhiteListManager(Context context) {
        mDBHandler = new ItemListDBHandler(context);
    }

    public static synchronized WhiteListManager getInstance(Context context) {
        if (INST == null) {
            INST = new WhiteListManager(context.getApplicationContext());
        }
        return INST;
    }

    public long insertToWhiteList(HandleItem type, CharSequence title, CharSequence summary,
            int weight) {
        if (type == HandleItem.MIUI_UPDATE) {
            Preferences.setWhiteListMiuiVersion(Preferences.getNewestMiuiVersion());
        }
        SQLiteDatabase db = mDBHandler.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ManualItemDbConstant.WhiteList.COLUMN_ITEM, type.toString());
        cv.put(ManualItemDbConstant.WhiteList.COLUMN_TITLE, title.toString());
        cv.put(ManualItemDbConstant.WhiteList.COLUMN_SUMMARY, summary.toString());
        cv.put(ManualItemDbConstant.WhiteList.COLUMN_WEIGHT, weight);

        long id = db.insert(ManualItemDbConstant.WHITE_LIST_TABLE_NAME, null, cv);
        return id;
    }

    public void loadWhiteList() {
        mWhiteList.clear();

        SQLiteDatabase db = mDBHandler.getReadableDatabase();
        Cursor cursor = null;
        try {
            String[] columns = {
                    ManualItemDbConstant.WhiteList.COLUMN_ITEM
            };
            cursor = db.query(ManualItemDbConstant.WHITE_LIST_TABLE_NAME, columns, null, null, null, null, null);
            if (cursor == null) {
                return;
            }
            if (!cursor.moveToFirst()) {
                return;
            }
            do {
                mWhiteList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // ignore
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    public boolean inWhiteList(HandleItem type) {
        return mWhiteList.contains(type.toString());
    }

    public List<WhiteListItemModel> getItemModelWhiteList() {
        SQLiteDatabase db = mDBHandler.getReadableDatabase();
        List<WhiteListItemModel> whiteList = new ArrayList<WhiteListItemModel>();
        Cursor cursor = null;
        try {
            cursor = db.query(ManualItemDbConstant.WHITE_LIST_TABLE_NAME,
                    null, null, null, null, null, null);
            if (cursor == null) {
                return whiteList;
            }

            if (!cursor.moveToFirst()) {
                return whiteList;
            }

            do {
                WhiteListItemModel item = new WhiteListItemModel();
                item.setType(cursor.getString(cursor
                        .getColumnIndex(ManualItemDbConstant.WhiteList.COLUMN_ITEM)));
                item.setTitle(cursor.getString(cursor
                        .getColumnIndex(ManualItemDbConstant.WhiteList.COLUMN_TITLE)));
                item.setSummary(cursor.getString(cursor
                        .getColumnIndex(ManualItemDbConstant.WhiteList.COLUMN_SUMMARY)));
                item.setWeight(cursor.getInt(cursor
                        .getColumnIndex(ManualItemDbConstant.WhiteList.COLUMN_WEIGHT)));
                item.setChecked(false);
                whiteList.add(item);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // ignore
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return whiteList;
    }

    public int deleteModelFromWhiteList(String type) {
        SQLiteDatabase db = mDBHandler.getWritableDatabase();
        String whereClause = ManualItemDbConstant.WhiteList.COLUMN_ITEM + " = ? ";
        String[] whereArgs = {
                type
        };
        return db.delete(ManualItemDbConstant.WHITE_LIST_TABLE_NAME, whereClause, whereArgs);
    }
}
