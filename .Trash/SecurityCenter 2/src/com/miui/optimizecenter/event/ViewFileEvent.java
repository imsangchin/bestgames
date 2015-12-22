
package com.miui.optimizecenter.event;

public class ViewFileEvent {

    private String mPath;

    private ViewFileEvent() {

    }

    public static ViewFileEvent create(String path) {
        ViewFileEvent res = new ViewFileEvent();
        res.mPath = path;
        return res;
    }

    public String getPath() {
        return mPath;
    }
}
