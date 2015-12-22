
package com.miui.permcenter.permissions;

import java.io.File;

import android.net.Uri;

import com.lbe.security.service.provider.PermissionManager;

public class PermissionSwitch {

    /**
     * Content URI
     */
    public static final Uri CONTENT_URI = Uri.withAppendedPath(PermissionManager.CONTENT_URI,
            "switch");

    private PermissionSwitch() {
    }
}
