package com.miui.powercenter.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DateResetReceiver extends BroadcastReceiver {
    private final static String ACTION_RESET_SHUTDOWNTIME="com.miui.powercenter.provider.RESET_SHUTDOWNTIME";
    private final static String ACTION_RESET_BOOTTIME="com.miui.powercenter.provider.RESET_BOOTTIME";

    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
            Intent shutdownintent = new Intent(ACTION_RESET_SHUTDOWNTIME);
            context.startService(shutdownintent);
            Intent bootintent = new Intent(ACTION_RESET_BOOTTIME);
            context.startService(bootintent);
        }

         //Handle TIME_SET: reset base of Stopwatch to ensure it to be correct
        if (action.equals(Intent.ACTION_TIME_CHANGED)) {
            Intent shutdownintent = new Intent(ACTION_RESET_SHUTDOWNTIME);
            context.startService(shutdownintent);

            Intent bootintent = new Intent(ACTION_RESET_BOOTTIME);
            context.startService(bootintent);
        }

        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent shutdownintent = new Intent(ACTION_RESET_SHUTDOWNTIME);
            context.startService(shutdownintent);

            Intent bootintent = new Intent(ACTION_RESET_BOOTTIME);
            context.startService(bootintent);
        }

    }

}
