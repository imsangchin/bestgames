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
	public static final String PackageName = "com.wandoujia.phoenix2";// 自定义动作
	private ActivityManager am = null;

	private String LogPath = "/data/data/log.com/";

	@Override
	public IBinder onBind(Intent arg0) {
		initVar();
		StartMonitor();
		return null;
	}

	/**
	 * 初始化变量
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
	 * 开始监控豌豆荚进程
	 */
	private void StartMonitor() {
		Thread th_monitor = new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				/**
				 * 获取正在运行程序进程名称列表
				 */
				List<RunningAppProcessInfo> runningapps = am
						.getRunningAppProcesses();// 获取运行的程序
				// 判断豌豆荚的进程是否已经启动，如果启动则开始进行日志输出到文件中
				for (RunningAppProcessInfo app : runningapps) {
					if (app.processName.contains("wandoujia")) {
						LogManagement.getInstance().Log(LogPath);
						return;
					}
				}
				try {
					Thread.sleep(1000);// 休眠一段时间
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
		});
		th_monitor.start();// 启动监控线程
	}

	@Override
	public void onStart(Intent intent, int startId) {
		StartMonitor();
	}

	/**
	 * 判断SD卡是否存在
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