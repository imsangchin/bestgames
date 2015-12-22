
package com.miui.optimizecenter.cleandb;

import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.miui.common.IOUtils;
import com.miui.common.Rc4;

public class CleanMaster {

    private static final String RC4_KEY = "miui_securitycenter";

    private static CleanMaster INST;
    private CleanDbHelper mDbHelper;
    private Locale mLocale;

    private CleanMaster(Context context) {
        mLocale = context.getResources().getConfiguration().locale;
        mDbHelper = new CleanDbHelper(context);
    }

    public static CleanMaster getInstance(Context context) {
        if (INST == null) {
            INST = new CleanMaster(context.getApplicationContext());
        }
        return INST;
    }

    public void setLocale(Locale locale) {
        mLocale = locale;
    }

    public long insertToCache(String table, String pkgName, String dirPath, int adviseDel,
            String res_zh_cn, String res_zh_tw, String res_def) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CleanColumns.PKG_NAME, Rc4.encry_RC4(pkgName, RC4_KEY));
        values.put(CleanColumns.DIR_PATH, Rc4.encry_RC4(dirPath, RC4_KEY));
        values.put(CleanColumns.ADVISE_DEL, Rc4.encry_RC4(String.valueOf(adviseDel), RC4_KEY));
        values.put(CleanColumns.RES_ZH_CN, Rc4.encry_RC4(res_zh_cn, RC4_KEY));
        values.put(CleanColumns.RES_ZH_TW, Rc4.encry_RC4(res_zh_tw, RC4_KEY));
        values.put(CleanColumns.RES_DEFAULT, Rc4.encry_RC4(res_def, RC4_KEY));
        return db.insert(table, null, values);
    }

    public CacheEntity queryCacheByDirPath(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return null;
        }

        String valideDirPath = valideDirPath(dirPath);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            String selection = CleanColumns.DIR_PATH + " = ?";
            String[] selectionArgs = {
                    Rc4.encry_RC4(valideDirPath, RC4_KEY)
            };
            cursor = db.query(CleanDbHelper.T_CACHE, null, selection, selectionArgs,
                    null, null, null);

            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }

            CacheEntity cache = new CacheEntity();

            String pkgName = cursor.getString(cursor.getColumnIndex(CleanColumns.PKG_NAME));
            cache.setPkgName(Rc4.decry_RC4(pkgName, RC4_KEY));

            cache.setDirPath(dirPath);

            String rootDir = cursor.getString(cursor.getColumnIndex(CleanColumns.ROOT_DIR));
            cache.setRootDir(Rc4.decry_RC4(rootDir, RC4_KEY));

            String adviseDel = cursor.getString(cursor.getColumnIndex(CleanColumns.ADVISE_DEL));
            cache.setAdviseDel(Integer.parseInt(adviseDel) == CleanColumns.ADVISE_DEL_YES);

            String jsonString = null;

            String localString = mLocale.toString();
            String simpString = Locale.SIMPLIFIED_CHINESE.toString();
            String twString = Locale.TRADITIONAL_CHINESE.toString();
            if (simpString.equals(localString)) {
                jsonString = cursor.getString(cursor.getColumnIndex(CleanColumns.RES_ZH_CN));
            } else if (twString.equals(localString)) {
                jsonString = cursor.getString(cursor.getColumnIndex(CleanColumns.RES_ZH_TW));
            } else {
                jsonString = cursor.getString(cursor.getColumnIndex(CleanColumns.RES_DEFAULT));
            }

            if (!TextUtils.isEmpty(jsonString)) {
                cache.parseResFromJsonString(Rc4.decry_RC4(jsonString, RC4_KEY));
            }
            return cache;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    private String valideDirPath(String dirPath) {
        String valomePath = Environment.getExternalStorageDirectory().getPath();
        if (dirPath.startsWith(valomePath)) {
            dirPath = dirPath.substring(valomePath.length());
        }
        return dirPath;
    }
}
