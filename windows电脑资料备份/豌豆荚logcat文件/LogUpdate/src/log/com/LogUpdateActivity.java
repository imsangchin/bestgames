package log.com;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogUpdateActivity extends Activity {
	/** Called when the activity is first created. */
	Button ClickButton;
	Button StartButton;
	EditText server;
	private String LogPath = "/data/data/log.com/";

	UpLoadFile loadfile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 获取手机IMEI作为日志文件名称
		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();

		server = (EditText) findViewById(R.id.serverip);

		if (avaiableMedia()) {
			LogPath = android.os.Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + imei + "Log.txt";
		} else
			LogPath = LogPath + imei + "Log.txt";

		StartButton = (Button) findViewById(R.id.startLog);
		StartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					Intent i = new Intent(LogUpdateActivity.this,
							MonitorCurrentThread.class);
					bindService(i, mConnection, Context.BIND_AUTO_CREATE);
				} catch (Exception e) {

				}
			}
		});

		ClickButton = (Button) findViewById(R.id.sendLog);
		ClickButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					if(loadfile == null)
					{
						LogManagement.getInstance().StopLog();
						loadfile = new UpLoadFile();
						if (!loadfile.Connect_Socket(server.getText().toString())) {
							Toast.makeText(LogUpdateActivity.this, "连接服务器失败",
								Toast.LENGTH_LONG).show();
						} else {
							new Thread() {
								public void run() {
									loadfile.uploadFile(LogPath);
									loadfile = null;
								}
							}.start();

							Toast.makeText(LogUpdateActivity.this, "发送日志成功",
								Toast.LENGTH_LONG).show();
						}

						unbindService(mConnection);
					}

				} catch (Exception e) {

				}
			}
		});
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
		}

		public void onServiceDisconnected(ComponentName className) {
		}
	};

	/**
	 * 判断手机是否有SD卡存在
	 * 
	 * @return
	 */
	public boolean avaiableMedia() {
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
}