
package com.miui.securitycenter.handlebar;

public class HandleItemModel {

    private HandleHeaderType mType;
    private HandleItem mItem;
    private CharSequence mTitle;
    private CharSequence mSummary;
    private int mScore;
    private int mWeight;

    public HandleHeaderType getType() {
        return mType;
    }

    public void setType(HandleHeaderType type) {
        mType = type;
    }

    public HandleItem getItem() {
        return mItem;
    }

    public void setItem(HandleItem item) {
        mItem = item;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    public CharSequence getSummary() {
        return mSummary;
    }

    public void setSummary(CharSequence summary) {
        mSummary = summary;
    }

    public int getWeight() {
        return mWeight;
    }

    public void setWeight(int weight) {
        mWeight = weight;
    }

    public int getScore() {
        return mScore;
    }

    public void setScore(int score) {
        mScore = score;
    }
}
