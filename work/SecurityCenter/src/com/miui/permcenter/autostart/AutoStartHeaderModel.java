
package com.miui.permcenter.autostart;

public class AutoStartHeaderModel {

    public AutoStartHeaderModel() {
        // ignore
    }

    private String mHeaderTitle;
    private AutoStartHeaderType mHeaderType;

    public String getHeaderTitle() {
        return mHeaderTitle;
    }

    public void setHeaderTitle(String title) {
        mHeaderTitle = title;
    }

    public void setAutoStartHeaderType(AutoStartHeaderType headerType) {
        mHeaderType = headerType;
    }

    public AutoStartHeaderType getAutoStartHeaderType() {
        return mHeaderType;
    }
}
