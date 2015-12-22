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

	/**
	 * ��ʼ������
	 */
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

	/**
	 * ��ʼ����㶹�Խ���
	 */
	private void StartMonitor() {
		Thread th_monitor = new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				/**
				 * ��ȡ�������г�����������б�
				 */
				List<RunningAppProcessInfo> runningapps = am
						.getRunningAppProcesses();// ��ȡ���еĳ���
				// �ж��㶹�ԵĽ����Ƿ��Ѿ����������������ʼ������־������ļ���
				for (RunningAppProcessInfo app : runningapps) {
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
		});
		th_monitor.start();// ��������߳�
	}

	@Override
	public void onStart(Intent intent, int startId) {
		StartMonitor();
	}

	/**
	 * �ж�SD���Ƿ����
	 * 
	 * @return
	 */
	private boolean avaiableMedia() {
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

}