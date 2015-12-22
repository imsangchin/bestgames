
package com.miui.securitycenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.miui.securitycenter.handlebar.HandleItem;
import com.miui.securitycenter.manualitem.WhiteListManager;

public class MiuiUpdateReceiver extends BroadcastReceiver {

    private static final String EXTRA_NEWEST_VERSION = "newVersion";
    private WhiteListManager mWhiteListManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String newestVersion = intent.getStringExtra(EXTRA_NEWEST_VERSION);
        if (newestVersion != null) {
            Preferences.setNewestMiuiVersion(newestVersion);
            String whiteListMiuiVersion = Preferences.getWhiteListMiuiVersion();
            if (!whiteListMiuiVersion.isEmpty() && !whiteListMiuiVersion.equals(newestVersion)) {
                mWhiteListManager = WhiteListManager.getInstance(context);
                mWhiteListManager.deleteModelFromWhiteList(HandleItem.MIUI_UPDATE.toString());
            }

        }
    }

}
