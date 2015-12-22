
package com.miui.optimizecenter.cleandb;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class CacheEntity {

    // json key
    public static final String KEY_CACHE_TYPE = "cache_type";
    public static final String KEY_ALERT_INFO = "alert_info";
    public static final String KEY_DESCRIPTION = "description";

    private String mPkgName;
    private String mDirPath;
    private String mRootDir;
    private String mCacheType;
    private String mAlertInfo;
    private String mDescx;
    private boolean mAdviseDel;

    public String getPkgName() {
        return mPkgName;
    }

    public void setPkgName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public String getDirPath() {
        return mDirPath;
    }

    public void setDirPath(String dirPath) {
        this.mDirPath = dirPath;
    }

    public String getRootDir() {
        return mRootDir;
    }

    public void setRootDir(String rootDir) {
        mRootDir = rootDir;
    }

    public String getCacheType() {
        return mCacheType;
    }

    void setCacheType(String cacheType) {
        this.mCacheType = cacheType;
    }

    public String getAlertInfo() {
        return mAlertInfo;
    }

    void setAlertInfo(String alertInfo) {
        this.mAlertInfo = alertInfo;
    }

    public String getDescx() {
        return mDescx;
    }

    void setDescx(String descx) {
        this.mDescx = descx;
    }

    public boolean isAdviseDel() {
        return mAdviseDel;
    }

    public void setAdviseDel(boolean adviseDel) {
        mAdviseDel = adviseDel;
    }

    public void parseResFromJsonString(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            return;
        }

        try {
            JSONObject json = new JSONObject(jsonString);
            mCacheType = json.optString(KEY_CACHE_TYPE);
            mAlertInfo = json.optString(KEY_ALERT_INFO);
            mDescx = json.optString(KEY_DESCRIPTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
