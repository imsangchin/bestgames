
package com.miui.permcenter.root;

public class RootHeaderModel {

    public RootHeaderModel() {
        // ignore
    }

    private String mHeaderTitle;
    private RootHeaderType mHeaderType;

    public String getHeaderTitle() {
        return mHeaderTitle;
    }

    public void setHeaderTitle(String title) {
        mHeaderTitle = title;
    }

    public void setRootHeaderType(RootHeaderType headerType) {
        mHeaderType = headerType;
    }

    public RootHeaderType getRootHeaderType() {
        return mHeaderType;
    }
}
