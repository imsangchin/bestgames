package com.miui.securitycenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Preferences.setNeedUpdateAppCount(intent.getIntExtra(ExtraIntent.EXTRA_NEED_UPDATE_APP_COUNT, 0));
    }

}
