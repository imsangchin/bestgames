
package com.miui.permcenter.permissions;

public class PermissionModel {

    public PermissionModel() {
        // ignore
    }

    private int mDefaultAction;
    private String mDescx;
    private String mName;
    private long mId;
    private int mUsedAppsCount;

    public int getDefaultAction() {
        return mDefaultAction;
    }

    public void setDefaultAction(int defaultAction) {
        mDefaultAction = defaultAction;
    }

    public String getDescx() {
        return mDescx;
    }

    public void setDescx(String descx) {
        mDescx = descx;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public int getUsedAppsCount() {
        return mUsedAppsCount;
    }

    public void setUsedAppsCount(int count) {
        mUsedAppsCount = count;
    }

    @Override
    public String toString() {
        return "PermissonModel mDefaultAction = " + mDefaultAction + " mDescx = " + mDescx
                + " mName = " + mName + " mId = " + mId + " mUsedAppsCount = " + mUsedAppsCount;
    }
}
