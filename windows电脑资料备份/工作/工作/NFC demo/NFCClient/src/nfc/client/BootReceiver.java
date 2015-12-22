package nfc.client;

import android.content.BroadcastReceiver;

import android.content.Context;

import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//receive phone boot_completed broadcast,start service to update help information in settd cycle
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Intent i = new Intent(context, ConnectService.class);
			context.startService(i);
		}
	}
}