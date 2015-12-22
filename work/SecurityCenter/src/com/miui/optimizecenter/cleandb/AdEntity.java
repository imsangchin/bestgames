
package com.miui.optimizecenter.cleandb;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class AdEntity {

    // json key
    public static final String KEY_DESC_NAME = "desc_name";
    public static final String KEY_ALERT_INFO = "alert_info";

    private String mPkgName;
    private String mDirPath;
    private String mRootDir;
    private String mAlertInfo;
    private String mDescName;
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

    public String getAlertInfo() {
        return mAlertInfo;
    }

    void setAlertInfo(String alertInfo) {
        this.mAlertInfo = alertInfo;
    }

    public String getDescName() {
        return mDescName;
    }

    void setDescName(String descName) {
        this.mDescName = descName;
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
            mAlertInfo = json.optString(KEY_ALERT_INFO);
            mDescName = json.optString(KEY_DESC_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
