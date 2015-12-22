
package com.miui.optimizecenter.whitelist;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.miui.common.IOUtils;

public class WhiteListManager {

    private static WhiteListManager INST;
    private WhiteListDBHelper mDbHelper;

    private List<String> mCachePathWhiteList = new ArrayList<String>();
    private List<String> mAdPathWhiteList = new ArrayList<String>();
    private List<String> mApkPathWhiteList = new ArrayList<String>();
    private List<String> mResidualPathWhiteList = new ArrayList<String>();

    private WhiteListManager(Context context) {
        mDbHelper = new WhiteListDBHelper(context);
    }

    public static synchronized WhiteListManager getInstance(Context context) {
        if (INST == null) {
            INST = new WhiteListManager(context.getApplicationContext());
        }
        return INST;
    }

    public long insertCacheToWhiteList(String cacheType, String dirPath, String pkgName,
            String alertInfo, String desc) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WhiteListColumns.Cache.COLUMN_CACHE_TYPE, cacheType);
        values.put(WhiteListColumns.Cache.COLUMN_DIR_PATH, dirPath);
        values.put(WhiteListColumns.Cache.COLUMN_PKG_NAME, pkgName);
        values.put(WhiteListColumns.Cache.COLUMN_ALERT_INFO, alertInfo);
        values.put(WhiteListColumns.Cache.COLUMN_DESC, desc);

        long id = db.insert(WhiteListColumns.TABLE_CACHE_WHITE_LIST, null, values);
        return id;
    }

    public void loadCacheWhiteList() {
        mCachePathWhiteList.clear();

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Cursor cursor = null;
        try {
            String[] columns = {
                    WhiteListColumns.Cache.COLUMN_DIR_PATH
            };
            cursor = db.query(WhiteListColumns.TABLE_CACHE_WHITE_LIST, columns, null,
                    null, null, null, null);
            if (cursor == null) {
                return;
            }

            if (!cursor.moveToFirst()) {
                return;
            }

            do {
                mCachePathWhiteList.add(cursor.getString(0));
            } while (cursor.moveToNext());

        } catch (Exception e) {
            // ignore
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    public boolean inCacheWhiteList(String dirPath) {
        return mCachePathWhiteList.contains(dirPath);
    }

    public class CacheWhiteInfo {
        public CacheWhiteInfo() {

        }

        public String cacheType;
        public String dirPath;
        public String pkgName;
        public String alertInfo;
        public String desc;
    }

    public List<CacheWhiteInfo> getCacheWhiteList() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        List<CacheWhiteInfo> cacheWhiteList = new ArrayList<CacheWhiteInfo>();
        Cursor cursor = null;
        try {
            cursor = db.query(WhiteListColumns.TABLE_CACHE_WHITE_LIST,
                    null, null, null, null, null, null);
            if (cursor == null) {
                return cacheWhiteList;
            }

            if (!cursor.moveToFirst()) {
                return cacheWhiteList;
            }

            do {
                CacheWhiteInfo cache = new CacheWhiteInfo();
                cache.cacheType = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Cache.COLUMN_CACHE_TYPE));
                cache.dirPath = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Cache.COLUMN_DIR_PATH));
                cache.pkgName = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Cache.COLUMN_PKG_NAME));
                cache.alertInfo = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Cache.COLUMN_ALERT_INFO));
                cache.desc = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Cache.COLUMN_DESC));
                cacheWhiteList.add(cache);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // ignore
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return cacheWhiteList;
    }

    public int deleteCacheFromWhiteList(String dirPath) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String whereClause = WhiteListColumns.Cache.COLUMN_DIR_PATH + " = ? ";
        String[] whereArgs = {
                dirPath
        };
        return db.delete(WhiteListColumns.TABLE_CACHE_WHITE_LIST, whereClause, whereArgs);
    }

    public long insertAdToWhiteList(String name, String dirPath) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WhiteListColumns.Ad.COLUMN_NAME, name);
        values.put(WhiteListColumns.Ad.COLUMN_DIR_PATH, dirPath);

        long id = db.insert(WhiteListColumns.TABLE_AD_WHITE_LIST, null, values);
        return id;
    }

    public void loadAdWhiteList() {
        mAdPathWhiteList.clear();

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            String[] columns = {
                    WhiteListColumns.Ad.COLUMN_DIR_PATH
            };
            cursor = db.query(WhiteListColumns.TABLE_AD_WHITE_LIST, columns, null, null, null,
                    null, null);
            if (cursor == null) {
                return;
            }

            if (!cursor.moveToFirst()) {
                return;
            }

            do {
                mAdPathWhiteList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // ignore
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    public boolean inAdWhiteList(String dirPath) {
        return mAdPathWhiteList.contains(dirPath);
    }

    public class AdWhiteInfo {
        public AdWhiteInfo() {

        }

        public String dirPath;
        public String name;
    }

    public List<AdWhiteInfo> getAdWhiteList() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        List<AdWhiteInfo> adWhiteList = new ArrayList<AdWhiteInfo>();
        Cursor cursor = null;
        try {
            cursor = db.query(WhiteListColumns.TABLE_AD_WHITE_LIST,
                    null, null, null, null, null, null);
            if (cursor == null) {
                return adWhiteList;
            }

            if (!cursor.moveToFirst()) {
                return adWhiteList;
            }

            do {
                AdWhiteInfo ad = new AdWhiteInfo();
                ad.name = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Ad.COLUMN_NAME));
                ad.dirPath = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Ad.COLUMN_DIR_PATH));
                adWhiteList.add(ad);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // ignore
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return adWhiteList;
    }

    public int deleteAdFromWhiteList(String dirPath) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String whereClause = WhiteListColumns.Ad.COLUMN_DIR_PATH + " = ? ";
        String[] whereArgs = {
                dirPath
        };
        return db.delete(WhiteListColumns.TABLE_AD_WHITE_LIST, whereClause, whereArgs);
    }

    public long insertResidualToWhiteList(String descName, String dirPath, String alertInfo) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WhiteListColumns.Residual.COLUMN_DESC_NAME, descName);
        values.put(WhiteListColumns.Residual.COLUMN_DIR_PATH, dirPath);
        values.put(WhiteListColumns.Residual.COLUMN_ALERT_INFO, alertInfo);

        long id = db.insert(WhiteListColumns.TABLE_RESIDUAL_WHITE_LIST, null, values);
        return id;
    }

    public void loadResidualWhiteList() {
        mResidualPathWhiteList.clear();

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Cursor cursor = null;
        try {
            String[] columns = {
                    WhiteListColumns.Residual.COLUMN_DIR_PATH
            };
            cursor = db.query(WhiteListColumns.TABLE_RESIDUAL_WHITE_LIST, columns, null,
                    null, null, null, null);
            if (cursor == null) {
                return;
            }

            if (!cursor.moveToFirst()) {
                return;
            }

            do {
                mResidualPathWhiteList.add(cursor.getString(0));
            } while (cursor.moveToNext());

        } catch (Exception e) {
            // ignore
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    public boolean inResidualWhiteList(String dirPath) {
        return mResidualPathWhiteList.contains(dirPath);
    }

    public class ResidualWhiteInfo {
        public ResidualWhiteInfo() {

        }

        public String descName;
        public String dirPath;
        public String alertInfo;
    }

    public List<ResidualWhiteInfo> getResidualWhiteList() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        List<ResidualWhiteInfo> residualWhiteList = new ArrayList<ResidualWhiteInfo>();
        Cursor cursor = null;
        try {
            cursor = db.query(WhiteListColumns.TABLE_RESIDUAL_WHITE_LIST,
                    null, null, null, null, null, null);
            if (cursor == null) {
                return residualWhiteList;
            }

            if (!cursor.moveToFirst()) {
                return residualWhiteList;
            }

            do {
                ResidualWhiteInfo residual = new ResidualWhiteInfo();
                residual.alertInfo = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Residual.COLUMN_ALERT_INFO));
                residual.dirPath = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Residual.COLUMN_DIR_PATH));
                residual.descName = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Residual.COLUMN_DESC_NAME));
                residualWhiteList.add(residual);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // ignore
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return residualWhiteList;
    }

    public int deleteResidualFromWhiteList(String dirPath) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String whereClause = WhiteListColumns.Residual.COLUMN_DIR_PATH + " = ? ";
        String[] whereArgs = {
                dirPath
        };
        return db.delete(WhiteListColumns.TABLE_RESIDUAL_WHITE_LIST, whereClause, whereArgs);
    }

    public long insertApkToWhiteList(String dirPath, String appName) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WhiteListColumns.Apk.COLUMN_DIR_PATH, dirPath);
        values.put(WhiteListColumns.Apk.COLUMN_APP_NAME, appName);

        long id = db.insert(WhiteListColumns.TABLE_APK_WHITE_LIST, null, values);
        return id;
    }

    public void loadApkWhiteList() {
        mApkPathWhiteList.clear();

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            String[] columns = {
                    WhiteListColumns.Apk.COLUMN_DIR_PATH
            };
            cursor = db.query(WhiteListColumns.TABLE_APK_WHITE_LIST, columns, null,
                    null, null, null, null);
            if (cursor == null) {
                return;
            }

            if (!cursor.moveToFirst()) {
                return;
            }

            do {
                mApkPathWhiteList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // ignore
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    public boolean inApkWhiteList(String dirPath) {
        return mApkPathWhiteList.contains(dirPath);
    }

    public class ApkWhiteInfo {
        public ApkWhiteInfo() {

        }

        public String dirPath;

        public String appName;
    }

    public List<ApkWhiteInfo> getApkWhiteList() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        List<ApkWhiteInfo> apkWhiteList = new ArrayList<ApkWhiteInfo>();
        Cursor cursor = null;
        try {
            cursor = db.query(WhiteListColumns.TABLE_APK_WHITE_LIST,
                    null, null, null, null, null, null);
            if (cursor == null) {
                return apkWhiteList;
            }

            if (!cursor.moveToFirst()) {
                return apkWhiteList;
            }

            do {
                ApkWhiteInfo apk = new ApkWhiteInfo();
                apk.dirPath = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Apk.COLUMN_DIR_PATH));
                apk.appName = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.Apk.COLUMN_APP_NAME));
                apkWhiteList.add(apk);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // ignore
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return apkWhiteList;
    }

    public int deleteApkFromWhiteList(String dirPath) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String whereClause = WhiteListColumns.Apk.COLUMN_DIR_PATH + " = ? ";
        String[] whereArgs = {
                dirPath
        };
        return db.delete(WhiteListColumns.TABLE_APK_WHITE_LIST, whereClause, whereArgs);
    }

    public long insertLargeFileToWhiteList(String dirPath) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WhiteListColumns.LargeFile.COLUMN_DIR_PATH, dirPath);

        long id = db.insert(WhiteListColumns.TABLE_LARGE_FILE_WHITE_LIST, null, values);
        return id;
    }

    public boolean inLargeFileWhiteList(String dirPath) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = WhiteListColumns.LargeFile.COLUMN_DIR_PATH + " = ? ";
        String[] selectionArgs = {
                dirPath
        };
        Cursor cursor = null;
        try {
            cursor = db.query(WhiteListColumns.TABLE_LARGE_FILE_WHITE_LIST, null, selection,
                    selectionArgs, null, null, null);
            if (cursor == null) {
                return false;
            }

            if (!cursor.moveToFirst()) {
                return false;
            }

            return cursor.getCount() != 0;
        } catch (Exception e) {
            // ignore
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }

    public class LargeFileWhiteInfo {
        public LargeFileWhiteInfo() {

        }

        public String dirPath;
    }

    public List<LargeFileWhiteInfo> getLargeFileWhiteList() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        List<LargeFileWhiteInfo> largeFileWhiteList = new ArrayList<LargeFileWhiteInfo>();
        Cursor cursor = null;
        try {
            cursor = db.query(WhiteListColumns.TABLE_LARGE_FILE_WHITE_LIST,
                    null, null, null, null, null, null);
            if (cursor == null) {
                return largeFileWhiteList;
            }

            if (!cursor.moveToFirst()) {
                return largeFileWhiteList;
            }

            do {
                LargeFileWhiteInfo largeFile = new LargeFileWhiteInfo();
                largeFile.dirPath = cursor.getString(cursor
                        .getColumnIndex(WhiteListColumns.LargeFile.COLUMN_DIR_PATH));
                largeFileWhiteList.add(largeFile);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // ignore
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return largeFileWhiteList;
    }

    public int deleteLargeFileFromWhiteList(String dirPath) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String whereClause = WhiteListColumns.LargeFile.COLUMN_DIR_PATH + " = ? ";
        String[] whereArgs = {
                dirPath
        };
        return db.delete(WhiteListColumns.TABLE_LARGE_FILE_WHITE_LIST, whereClause, whereArgs);
    }
}
