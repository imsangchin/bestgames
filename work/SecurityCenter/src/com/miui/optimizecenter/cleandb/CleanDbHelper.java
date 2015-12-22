
package com.miui.optimizecenter.cleandb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.miui.common.IOUtils;

public class CleanDbHelper extends SQLiteOpenHelper {
    private static final String TAG = CleanDbHelper.class.getSimpleName();

    public static final String DB_NAME = "garbage_clean.db";
    public static final int DB_VERSION = 2;

    // cache
    public static final String T_CACHE = "t_cache";

    // ad
    public static final String T_AD_DIR = "t_ad";

    // residual
    public static final String T_RESIDUAL = "t_residual";

    public CleanDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ignore
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ignore
    }

    public void copyDbFromAssert(Context context) throws IOException {

        File file = context.getDatabasePath(DB_NAME);
        try {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(file, null);
            int curVersion = db.getVersion();
            if (curVersion >= DB_VERSION) {
                return;
            }
        } catch (Exception e) {
            Log.d(TAG, "open clean up garbage_clean.db failed ,error msg = " + e.getMessage());
        }

        file.deleteOnExit();
        // copy new
        OutputStream output = new FileOutputStream(file);
        InputStream input = context.getAssets().open(DB_NAME);
        IOUtils.copy(input, output);
    }
}
