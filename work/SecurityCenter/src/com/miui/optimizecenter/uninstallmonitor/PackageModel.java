
package com.miui.optimizecenter.uninstallmonitor;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.miui.common.AndroidUtils;

public class PackageModel {
    static final String TAG = PackageModel.class.getSimpleName();

    public static class FromCursorFactory {

        private static final String[] PACKAGE_PROJECTION = {
                PackageColumns._ID, // 0
                PackageColumns.COLUMN_IC_LAUNCHER, // 1
                PackageColumns.COLUMN_APPLICATION_LABEL, // 2
                PackageColumns.COLUMN_PACKAGE_NAME, // 3
                PackageColumns.COLUMN_VERSION_NAME, // 4
                PackageColumns.COLUMN_VERSION_CODE, // 5
        };

        private static final int COLUMN_ID = 0;

        private static final int COLUMN_IC_LAUNCHER = 1;

        private static final int COLUMN_APPLICATION_LABEL = 2;

        private static final int COLUMN_PACKAGE_NAME = 3;

        private static final int COLUMN_VERSION_NAME = 4;

        private static final int COLUMN_VERSION_CODE = 5;

        public static String[] getProjection() {
            int len = PACKAGE_PROJECTION.length;
            String[] res = new String[len];
            System.arraycopy(PACKAGE_PROJECTION, 0, res, 0, len);
            return res;
        }

        public static PackageModel create(Cursor cursor) {
            PackageModel res = new PackageModel();
            res.mId = cursor.getLong(COLUMN_ID);
            res.mApplicationLabel = cursor.getString(COLUMN_APPLICATION_LABEL);
            res.mPackageName = cursor.getString(COLUMN_PACKAGE_NAME);
            res.mVersionName = cursor.getString(COLUMN_VERSION_NAME);
            res.mVersionCode = cursor.getInt(COLUMN_VERSION_CODE);
            byte[] bytes = cursor.getBlob(COLUMN_IC_LAUNCHER);
            res.mLauncher = AndroidUtils.bytesToDrawable(bytes);

            Log.d(TAG, res.toString());
            return res;
        }
    }

    public static PackageModel create(Cursor cursor) {
        return FromCursorFactory.create(cursor);
    }

    public static PackageModel create(Drawable launcher, String appLabel, String pkgName,
            String versionName, int versionCode) {
        PackageModel res = new PackageModel();
        res.mLauncher = launcher;
        res.mApplicationLabel = appLabel;
        res.mPackageName = pkgName;
        res.mVersionName = versionName;
        res.mVersionCode = versionCode;

        Log.d(TAG, res.toString());
        return res;
    }

    private long mId;
    private Drawable mLauncher;
    private String mApplicationLabel;
    private String mPackageName;
    private String mVersionName;
    private int mVersionCode;

    private PackageModel() {
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Drawable getLauncher() {
        return mLauncher;
    }

    public void setLauncher(Drawable launcher) {
        mLauncher = launcher;
    }

    public String getApplicationLabel() {
        return mApplicationLabel;
    }

    public void setApplicationLabel(String label) {
        mApplicationLabel = label;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public String getVersionName() {
        return mVersionName;
    }

    public void setVersionName(String versionName) {
        mVersionName = versionName;
    }

    public int getVersionCode() {
        return mVersionCode;
    }

    public void setVersionCode(int versionCode) {
        mVersionCode = versionCode;
    }

    @Override
    public String toString() {
        return "PackageModel : appLabel = " + mApplicationLabel + " packageName = " + mPackageName
                + " versionName = " + mVersionName + " versionCode = " + mVersionCode
                + " launcher = " + mLauncher;
    }

}
