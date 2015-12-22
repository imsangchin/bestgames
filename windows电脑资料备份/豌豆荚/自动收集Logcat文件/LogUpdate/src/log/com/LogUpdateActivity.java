package log.com;

import java.io.File;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LogUpdateActivity extends Activity {
	/** Called when the activity is first created. */
	Button ClickButton;
	Button StartButton;
	Button ClearButton;
	EditText server;
	TextView hintArea;
	private String LogPath = "/data/data/log.com/";

	UpLoadFile loadfile;

	private static final int UPLOADSUCCESS = 100;
	private static final int UPLOADFAILURE = 101;
	private boolean upload_success = false;

	private String hintString = "1. �״�ʹ�õ�ʱ��������ʼ��ذ�ť\n2. �޸ķ�������ַ";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		loadfile = new UpLoadFile();
		
		// ��ȡ�ֻ�IMEI��Ϊ��־�ļ����ƣ���֤ÿ̨��������־�ļ����Ʋ���ͬ
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

			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					Intent i = new Intent(LogUpdateActivity.this,
							MonitorCurrentThread.class);
					bindService(i, mConnection, Context.BIND_AUTO_CREATE);

					StartButton.setEnabled(false);

					SharedPreferences preferences = getSharedPreferences(
							"LOGUPLOAD", 0);
					preferences.edit().putBoolean("START", true).commit();
				} catch (Exception e) {

				}
			}
		});

		ClickButton = (Button) findViewById(R.id.sendLog);
		ClickButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					//���ӱ�־λ����ֹ�û����������η��Ͱ�ť
					if (!upload_success) {
						upload_success = true;
						LogManagement.getInstance().StopLog();
						
						if (!loadfile.Connect_Socket(server.getText()
								.toString())) {
							Toast.makeText(LogUpdateActivity.this, "���ӷ�����ʧ��",
									Toast.LENGTH_LONG).show();
							return;
						} else {
							new Thread() {
								public void run() {
									loadfile.uploadFile(LogPath);
									upload_success = false;
									Message msg = mHanler
											.obtainMessage(UPLOADSUCCESS);
									mHanler.sendMessage(msg);
									
								}
							}.start();
						}

						resetButton();
					}

				} catch (Exception e) {

				}
			}
		});

		ClearButton = (Button)findViewById(R.id.ClearLog);
		ClearButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					LogManagement.getInstance().StopLog();	
					resetButton();
				}
				catch(Exception e)
				{
					
				}
			}
		});
		
		hintArea = (TextView) findViewById(R.id.hintarea);
		// hintArea.setBackgroundColor(android.R.color.white);
		hintArea.setText(hintString);

		SharedPreferences preferences = getSharedPreferences("LOGUPLOAD", 0);
		boolean IsStart = preferences.getBoolean("START", false);
		if (IsStart) {
			StartButton.setEnabled(false);
		}
	}

	/**
	 * 
	 */
	private void resetButton()
	{
		SharedPreferences preferences = getSharedPreferences(
				"LOGUPLOAD", 0);
		preferences.edit().putBoolean("START", false).commit();
		StartButton.setEnabled(true);
		unbindService(mConnection);
		
		File file = new File(LogPath); // �����ļ�
		if(file.exists())
		{
			file.delete();
		}
	}
	
	private Handler mHanler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPLOADSUCCESS:
				hintArea.setText("�ϴ���־�ɹ�");
				break;
			case UPLOADFAILURE:
				break;
			}
		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			hintArea.setText("���Ӻ�̨����ɹ�");
		}

		public void onServiceDisconnected(ComponentName className) {
		}
	};

	/**
	 * �ж��ֻ��Ƿ���SD������
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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		//unbindService(mConnection);
	}

	
}