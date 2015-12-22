package irdeto.software;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

	private long N = 3600000;
	private String file_path = "/data/data/irdeto.software/Time.txt";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub

		return null;
	}

	private void Read_Time() {
		DataInputStream datain = null;
		try {
			File f = new File(file_path);
			if (!f.exists()) {
				f.createNewFile();
				DataOutputStream dataout = new DataOutputStream(
						new FileOutputStream(file_path));
				dataout.writeInt(3);
				N = N * 3;
				dataout.close();
			}

			else {
				datain = new DataInputStream(new FileInputStream(file_path));
				if (datain != null) {
					int hour = datain.readInt();
					N = hour * N;
					Log.i("Time", N + "");
					Log.i("HOUR", hour + "");
				}
				datain.close();

			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	// get the current activity and judge if ourself
	// if our application is running return true,else return false
	private boolean GetActivityInformation() {
		try {

			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

			if (cn.getPackageName().endsWith("irdeto.software")) {
				return true;
			}
			return false;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);

		// long start_time = System.currentTimeMillis();
		new Thread() {
			public void run() {
				Log.i("Myservice", "1");
				try {
					Read_Time();
					while (true) {
						Thread.sleep(N);
						Read_Time();
						// if ourself application is running,stop modify the
						// file
						if (GetActivityInformation()) {
							Log.i("OurSelf", "Our");
							continue;
						}
						Operate_File.packs = getPackageManager()
								.getInstalledPackages(0);
						Operate_File file = new Operate_File();
						file.Decrypt_File(true, null);
					}
				} catch (Exception e) {
					Log.i("Exception", "Sleep Exception");
				} finally {

				}
			}
		}.start();
	}

}