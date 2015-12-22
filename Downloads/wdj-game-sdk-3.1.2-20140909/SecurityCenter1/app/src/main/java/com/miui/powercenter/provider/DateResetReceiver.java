package com.miui.powercenter.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class DateResetReceiver extends BroadcastReceiver {
  public void onReceive(Context context, Intent intent) {

    if (intent == null) {
      return;
    }

    final String action = intent.getAction();

    if (TextUtils.isEmpty(action)) {
      return;
    }
    if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)
        || action.equals(Intent.ACTION_TIME_CHANGED)
        || action.equals(Intent.ACTION_BOOT_COMPLETED)) {
      startAlarmService(context);
    }
  }

  private void startAlarmService(Context context) {
    Intent shutdownIntent = new Intent(ShutdownAlarmIntentService.ACTION_RESET_SHUTDOWNTIME);
    context.startService(shutdownIntent);
    Intent bootIntent = new Intent(BootAlarmIntentService.ACTION_PHONE_START);
    context.startService(bootIntent);
  }

}
