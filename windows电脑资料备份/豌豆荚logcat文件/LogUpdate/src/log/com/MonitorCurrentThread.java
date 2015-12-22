package log.com;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MonitorCurrentThread extends Service {
	public static final String PackageName = "com.wandoujia.phoenix2";// �Զ��嶯��
	private ActivityManager am = null;

	private String LogPath = "/data/data/log.com/";

	@Override
	public IBinder onBind(Intent arg0) {
		initVar();
		StartMonitor();
		return null;
	}

	private void initVar() {
		am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();
		if (avaiableMedia()) {
			LogPath = android.os.Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + imei + "Log.txt";
		} else
			LogPath = LogPath + imei + "Log.txt";
	}

	@Override
	public void onCreate() {
		initVar();
		Log.i("service----->", "start");
	}

	private void StartMonitor() {
		Thread th_monitor = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					/**
					 * ��ȡ�������г�����������б�
					 */
					List<RunningAppProcessInfo> runningapps = am
							.getRunningAppProcesses();// ��ȡ���еĳ���
					// List<String>runningappsprocessnameList=new
					// ArrayList<String>();//���������������г���Ľ�������
					for (RunningAppProcessInfo app : runningapps) {
						// oldrunningappsprocessnameList.add(old.processName);
						// if(app.processName)
						// Log.i("old", old.processName);
						if (app.processName.contains("wandoujia")) {
							LogManagement.getInstance().Log(LogPath);
							return;
						}
					}
					try {
						Thread.sleep(1000);// ����һ��ʱ��
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}
			}
		});

		th_monitor.start();// ��������߳�
	}

	@Override
	public void onStart(Intent intent, int startId) {
		StartMonitor();
	}

	// �ж��Ƿ����SD��
	public boolean avaiableMedia() {
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

}