package com.anzhuoshoudiantong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anzhuoshoudiantong.ads.AdController;


public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      AdController controller = AdController.getInstance(context);
      controller.showNotificationAd();
    }
  }
}
