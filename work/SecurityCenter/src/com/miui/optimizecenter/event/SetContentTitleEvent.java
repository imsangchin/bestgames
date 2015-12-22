
package com.miui.optimizecenter.event;

public class SetContentTitleEvent {
    private CharSequence mTitle;

    private SetContentTitleEvent() {
        // ignore
    }

    public static SetContentTitleEvent create(CharSequence title) {
        SetContentTitleEvent res = new SetContentTitleEvent();
        res.mTitle = title;
        return res;
    }

    public CharSequence getTitle() {
        return mTitle;
    }
}
