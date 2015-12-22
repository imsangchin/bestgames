
package com.miui.securitycenter.event;

import com.miui.securitycenter.MenuBar.MenuItem;

public class OnMenuItemClickEvent {

    private OnMenuItemClickEvent() {
        // ingore
    }

    private MenuItem mMenuItem;

    public static OnMenuItemClickEvent create(MenuItem item) {
        OnMenuItemClickEvent res = new OnMenuItemClickEvent();
        res.mMenuItem = item;
        return res;
    }

    public MenuItem getMenuItem() {
        return mMenuItem;
    }

}
