
package com.miui.securitycenter.event;

import com.miui.securitycenter.MenuBar.MenuItem;

public class OnMenuItemLongClickEvent {

    private OnMenuItemLongClickEvent() {
        // ingore
    }

    private MenuItem mMenuItem;

    public static OnMenuItemLongClickEvent create(MenuItem item) {
        OnMenuItemLongClickEvent res = new OnMenuItemLongClickEvent();
        res.mMenuItem = item;
        return res;
    }

    public MenuItem getMenuItem() {
        return mMenuItem;
    }

}
