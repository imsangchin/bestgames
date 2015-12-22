
package com.miui.powercenter.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Debug;
import android.util.Log;

import com.miui.powercenter.provider.PowerData.PowerMode;

public class PowerDataProvider extends ContentProvider {
    private PowerDBHelper mDBHelper;
    private static final String TAG = "PowerDataProvider";

    @Override
    public boolean onCreate() {
        Log.d(TAG, "PDEBUG-- 数据库初始化2");
        mDBHelper = new PowerDBHelper(getContext(), PowerDBHelper.DATABASE_NAME);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        PowerDBHelper database = mDBHelper;
        if (database == null) {
            throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getWritableDatabase();

        String limit = uri.getQueryParameter("limit");

        MatchResult result = match(uri, selection);

        Cursor cursor = db.query(result.mTable, projection, result.mSelection, selectionArgs, null,
                null,
                sortOrder, limit);
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case POWER_MODE_ID:
                return PowerData.CONTENT_ITEM_TYPE;

            case POWER_MODE:
                return PowerData.CONTENT_TYPE;

        }
        throw new IllegalStateException("Unknown URL");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        PowerDBHelper database = mDBHelper;
        if (database == null) {
            throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getWritableDatabase();

        MatchResult result = match(uri, null);

        long rowId = db.insert(result.mTable, PowerMode.Columns.MODE_NAME, values);
        Uri newUri = (rowId > 0 ? ContentUris.withAppendedId(uri, rowId) : null);
        if (newUri != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        PowerDBHelper database = mDBHelper;
        if (database == null) {
            throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getWritableDatabase();

        MatchResult result = match(uri, selection);

        int count = db.delete(result.mTable, result.mSelection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        PowerDBHelper database = mDBHelper;
        if (database == null) {
            throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getWritableDatabase();

        MatchResult result = match(uri, selection);

        int count = db.update(result.mTable, values, result.mSelection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    private static final class MatchResult {
        public String mTable;
        public String mSelection;
    }

    private MatchResult match(Uri uri, String selection) {
        int tableId = URI_MATCHER.match(uri);
        MatchResult result = new MatchResult();
        switch (tableId) {
            case POWER_MODE:
                result.mTable = PowerMode.TABLE_NAME;
                result.mSelection = selection;
                break;
            case POWER_MODE_ID:
                result.mTable = PowerMode.TABLE_NAME;
                result.mSelection = (selection == null ? null : selection + " AND ");
                result.mSelection += PowerMode.Columns.ID + " = " + uri.getPathSegments().get(1);
                break;
            default:
                throw new IllegalStateException("Unknown URL: " + uri.toString());
        }
        return result;
    }

    private static final int POWER_MODE = 104;
    private static final int POWER_MODE_ID = 105;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(PowerData.AUTHORITY, PowerMode.TABLE_NAME, POWER_MODE);
        URI_MATCHER
                .addURI(PowerData.AUTHORITY, PowerMode.TABLE_NAME + "/#", POWER_MODE_ID);
    }
}
