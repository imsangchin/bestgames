
package com.miui.optimizecenter.whitelist;


public class WhiteListHeaderModel {

    public WhiteListHeaderModel() {
        // ignore
    }

    private WhiteListType mWhiteListType;

    private CharSequence mHeaderTitle;

    private boolean mIsChecked;

    public WhiteListType getWhiteListType() {
        return mWhiteListType;
    }

    public void setWhiteListType(WhiteListType whiteListType) {
        mWhiteListType = whiteListType;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
    }

    public CharSequence getHeaderTitle() {
        return mHeaderTitle;
    }

    public void setHeaderTitle(CharSequence title) {
        mHeaderTitle = title;
    }

    @Override
    public String toString() {
        return "WhiteListType mWhiteListType = " + mWhiteListType
                + " mDirPath = " + " mIsChecked = " + mIsChecked;
    }
}
