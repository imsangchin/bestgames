
package com.miui.optimizecenter.uninstallmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.miui.common.AndroidUtils;

import java.util.ArrayList;
import java.util.List;

public class PackagesManager {

    private static PackagesManager INST;
    private PackageDataBaseHelper mDBHelper;

    private PackagesManager(Context context) {
        mDBHelper = new PackageDataBaseHelper(context);
    }

    public static synchronized PackagesManager getInstance(Context context) {
        if (INST == null) {
            INST = new PackagesManager(context.getApplicationContext());
        }
        return INST;
    }

    public void addPackages(List<PackageModel> packages) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.beginTransaction();

        for (PackageModel model : packages) {
            ContentValues values = new ContentValues();
            values.put(PackageColumns.COLUMN_APPLICATION_LABEL, model.getApplicationLabel());
            values.put(PackageColumns.COLUMN_PACKAGE_NAME, model.getPackageName());
            values.put(PackageColumns.COLUMN_VERSION_NAME, model.getVersionName());
            values.put(PackageColumns.COLUMN_VERSION_CODE, model.getVersionCode());
            byte[] bytes = AndroidUtils.drawableToBytes(model.getLauncher());
            if (bytes != null) {
                values.put(PackageColumns.COLUMN_IC_LAUNCHER, bytes);
            }
            db.insert(PackageColumns.TABLE_PACKAGE, null, values);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addPackage(PackageModel model) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PackageColumns.COLUMN_APPLICATION_LABEL, model.getApplicationLabel());
        values.put(PackageColumns.COLUMN_PACKAGE_NAME, model.getPackageName());
        values.put(PackageColumns.COLUMN_VERSION_NAME, model.getVersionName());
        values.put(PackageColumns.COLUMN_VERSION_CODE, model.getVersionCode());
        byte[] bytes = AndroidUtils.drawableToBytes(model.getLauncher());
        if (bytes != null) {
            values.put(PackageColumns.COLUMN_IC_LAUNCHER, bytes);
        }
        db.insert(PackageColumns.TABLE_PACKAGE, null, values);
    }

    public void updatePackage(PackageModel model) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String packageName = model.getPackageName();

        ContentValues values = new ContentValues();
        values.put(PackageColumns.COLUMN_APPLICATION_LABEL, model.getApplicationLabel());
        values.put(PackageColumns.COLUMN_VERSION_NAME, model.getVersionName());
        values.put(PackageColumns.COLUMN_VERSION_CODE, model.getVersionCode());
        byte[] bytes = AndroidUtils.drawableToBytes(model.getLauncher());
        if (bytes != null) {
            values.put(PackageColumns.COLUMN_IC_LAUNCHER, bytes);
        }

        String whereClause = PackageColumns.COLUMN_PACKAGE_NAME + " = ?";
        String[] whereArgs = {
                packageName
        };
        db.update(PackageColumns.TABLE_PACKAGE, values, whereClause, whereArgs);
    }

    public boolean isPackageExist(String packageName) {
        return getPackageByName(packageName) != null;
    }

    public PackageModel getPackageByName(String packageName) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            String table = PackageColumns.TABLE_PACKAGE;
            String[] columns = PackageModel.FromCursorFactory.getProjection();
            String selection = PackageColumns.COLUMN_PACKAGE_NAME + " = ?";
            String[] selectionArgs = {
                    packageName
            };
            cursor = db.query(table, columns, selection, selectionArgs, null, null, null);

            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                return PackageModel.create(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    public List<PackageModel> getAllPackages() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            String table = PackageColumns.TABLE_PACKAGE;
            String[] columns = PackageModel.FromCursorFactory.getProjection();
            cursor = db.query(table, columns, null, null, null, null, null);

            if (cursor == null) {
                return null;
            }
            if (!cursor.moveToFirst()) {
                return null;
            }

            List<PackageModel> models = new ArrayList<PackageModel>();
            do {
                models.add(PackageModel.create(cursor));
            } while (cursor.moveToNext());
            return models;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    public boolean isPackageEmpty() {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            String table = PackageColumns.TABLE_PACKAGE;
            String[] columns = PackageModel.FromCursorFactory.getProjection();
            cursor = db.query(table, columns, null, null, null, null, null);

            if (cursor == null) {
                return true;
            }
            if (!cursor.moveToFirst()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }

    public void deletePackageByName(String packageName) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String table = PackageColumns.TABLE_PACKAGE;
        String whereClause = PackageColumns.COLUMN_PACKAGE_NAME + " = ?";
        String[] whereArgs = {
                packageName
        };
        db.delete(table, whereClause, whereArgs);
    }

    public void clearPackages() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.delete(PackageColumns.TABLE_PACKAGE, null, null);
    }
}
