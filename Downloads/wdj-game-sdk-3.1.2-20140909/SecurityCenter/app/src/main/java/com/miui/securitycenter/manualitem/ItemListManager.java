
package com.miui.securitycenter.manualitem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.miui.securitycenter.handlebar.HandleItem;
import com.miui.securitycenter.handlebar.HandleItemModel;
import com.miui.securitycenter.manualitem.ExaminationResult.ExaminationDataItem;

import java.util.ArrayList;
import java.util.List;

public class ItemListManager {

    private static ItemListManager INST;
    private ItemListDBHandler mDBHandler;

    public ItemListManager(Context context) {
        mDBHandler = new ItemListDBHandler(context.getApplicationContext());
    }

    public static synchronized ItemListManager getInstance(Context context) {
        if (INST == null) {
            INST = new ItemListManager(context);
        }
        return INST;
    }

    public void initialDataBaseIfNoData() {
        SQLiteDatabase db = mDBHandler.getWritableDatabase();
        StringBuilder sb = new StringBuilder();
        for (HandleItem item : HandleItem.values()) {
            if (item.mWeight != 0) {
                sb = new StringBuilder();
                sb.append("insert into " + ManualItemDbConstant.ITEM_LIST_TABLE_NAME + " values(");
                sb.append("'" + item.toString() + "', '" + item.mScore + "', '" + item.mWeight
                        + "', '" + item.mChecked + "')");
                db.execSQL(sb.toString());
            }
        }
    }

    public void insertAllToItemList(ExaminationResult result) {
        deleteAllFromItemList();
        List<ExaminationDataItem> resList = result.getmExaminationDataList();
        for (ExaminationDataItem item : resList) {
            if (insertToItemList(item.getCategory().toUpperCase(), item.getScore(),
                    item.getOrder(), item.isChecked()) == -1) {
                return;
            }
        }

    }

    private long insertToItemList(String item, int score, int weight, boolean checked) {
        SQLiteDatabase db = mDBHandler.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ManualItemDbConstant.ItemList.COLUMN_ITEM, item.toUpperCase());
        cv.put(ManualItemDbConstant.ItemList.COLUMN_SCORE, score);
        cv.put(ManualItemDbConstant.ItemList.COLUMN_WEIGHT, weight);
        cv.put(ManualItemDbConstant.ItemList.COLUMN_CHECKED, checked);
        long id = db.insert(ManualItemDbConstant.ITEM_LIST_TABLE_NAME, null, cv);
        return id;
    }

    public List<HandleItemModel> getItemList() {
        SQLiteDatabase db = mDBHandler.getReadableDatabase();
        List<HandleItemModel> itemList = new ArrayList<HandleItemModel>();
        Cursor cursor = null;
        try {
            cursor = db.query(ManualItemDbConstant.ITEM_LIST_TABLE_NAME, null, null, null, null,
                    null, null);
            if (cursor == null) {
                return itemList;
            }
            if (!cursor.moveToFirst()) {
                return itemList;
            }

            do {
                int checked = cursor.getInt(cursor
                        .getColumnIndex(ManualItemDbConstant.ItemList.COLUMN_CHECKED));
                if (checked != 0) {
                    HandleItemModel model = new HandleItemModel();
                    HandleItem item = HandleItem.valueOf(cursor.getString(
                            cursor.getColumnIndex(ManualItemDbConstant.ItemList.COLUMN_ITEM))
                            .toUpperCase());
                    int score = cursor.getInt(cursor
                            .getColumnIndex(ManualItemDbConstant.ItemList.COLUMN_SCORE));
                    int weight = cursor.getInt(cursor
                            .getColumnIndex(ManualItemDbConstant.ItemList.COLUMN_WEIGHT));
                    model.setItem(item);
                    model.setScore(score);
                    model.setWeight(weight);
                    itemList.add(model);
                }
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // ignore
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return itemList;
    }

    public int deleteAllFromItemList() {
        SQLiteDatabase db = mDBHandler.getWritableDatabase();
        return db.delete(ManualItemDbConstant.ITEM_LIST_TABLE_NAME, null, null);
    }

    public int deleteModelFromItemList(String item) {
        SQLiteDatabase db = mDBHandler.getWritableDatabase();
        String whereClause = ManualItemDbConstant.ItemList.COLUMN_ITEM + " = ? ";
        String[] whereArgs = {
                item
        };
        return db.delete(ManualItemDbConstant.ITEM_LIST_TABLE_NAME, whereClause, whereArgs);
    }
}
