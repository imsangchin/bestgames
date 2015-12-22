
package com.miui.optimizecenter.event;

public class ExpandListGroupEvent {

    private int mGroupPos;
    private String mPkgName;
    private boolean mIsExpand;

    private ExpandListGroupEvent() {

    }

    public static ExpandListGroupEvent create(int groupPos, String pkgName, boolean isExpand) {
        ExpandListGroupEvent res = new ExpandListGroupEvent();
        res.mGroupPos = groupPos;
        res.mPkgName = pkgName;
        res.mIsExpand = isExpand;
        return res;
    }

    public int getGroupPos() {
        return mGroupPos;
    }

    public String getPkgName() {
        return mPkgName;
    }

    public boolean isExpand() {
        return mIsExpand;
    }

    @Override
    public String toString() {
        return "ExpandListGroupEvent PkgName = " + mPkgName + " GroupPos = " + mGroupPos
                + " isExpand = " + mIsExpand;
    }
}
