
package com.miui.securitycenter.system;


public class SystemItemModel {

    private SystemItem mItem;
    private SystemType mSystemType;
    private CharSequence mTitle;
    private CharSequence mSummary;

    public SystemItem getItem() {
        return mItem;
    }

    public void setItem(SystemItem item) {
        this.mItem = item;
    }

    public SystemType getProtectionType() {
        return mSystemType;
    }

    public void setSystemType(SystemType protectionType) {
        this.mSystemType = protectionType;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
    }

    public CharSequence getSummary() {
        return mSummary;
    }

    public void setSummary(CharSequence summary) {
        this.mSummary = summary;
    }

    public SystemItemModel() {

    }

    @Override
    public String toString() {
        return "SystemItemModel Item = " + mItem + " Title = " + mTitle + " Summary = " + mSummary
                + " mSystemType = " + mSystemType;
    }
}
