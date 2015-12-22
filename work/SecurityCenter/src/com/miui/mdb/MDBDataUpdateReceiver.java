
package com.miui.mdb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import miui.util.DataUpdateUtils;

public class MDBDataUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        Log.v(MDBUpdateUtil.LOG_TAG, "MDBDataUpdateReceiver receive action: " + action);
        if (DataUpdateUtils.DATA_UPDATE_RECEIVE.equals(action)) {
            Intent i = new Intent(intent);
            i.setClass(context, MDBUpdateService.class);
            context.startService(i);
        }
    }
}
