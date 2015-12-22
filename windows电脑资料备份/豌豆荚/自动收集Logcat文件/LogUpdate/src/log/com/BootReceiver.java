package log.com;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		// 当手机开始的是启动服务，检测豌豆荚进程是否启动
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Intent i = new Intent(context, MonitorCurrentThread.class);
			context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
		}

		public void onServiceDisconnected(ComponentName className) {
		}
	};
}