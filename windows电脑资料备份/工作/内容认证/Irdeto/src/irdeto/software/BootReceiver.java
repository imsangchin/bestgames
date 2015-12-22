package irdeto.software;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		// receive phone boot_completed broadcast,start service to update help
		// information in settd cycle
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Intent i = new Intent(context, MyService.class);
			context.startService(i);
		}
		if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
			/*
			 * if(change_number == 2) { Operate_File.packs =
			 * context.getPackageManager().getInstalledPackages(0); Operate_File
			 * file = new Operate_File(); file.Decrypt_File(true, null);
			 * change_number = 0; } else { change_number ++; }
			 */
		}
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {

		}
	}
}
