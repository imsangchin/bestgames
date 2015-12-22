
package com.miui.securitycenter.manualitem;

public class WhiteListItemModel {

    public WhiteListItemModel() {
        // ignore
    }

    private String mType;
    private String mTitle;
    private String mSummary;
    private boolean mIsCheck;
    private int mWeight;

    public String getType() {
        return mType;
    }
    public void setType(String type) {
        mType = type;
    }
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public boolean isChecked() {
        return mIsCheck;
    }

    public void setChecked(boolean checked) {
        mIsCheck = checked;
    }

    public int getWeight() {
        return mWeight;
    }

    public void setWeight(int weight) {
        mWeight = weight;
    }
}
