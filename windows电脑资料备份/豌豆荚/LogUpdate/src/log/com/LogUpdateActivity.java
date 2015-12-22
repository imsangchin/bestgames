package log.com;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class LogUpdateActivity extends Activity implements
		Button.OnClickListener {
	/** Called when the activity is first created. */

	Button StartButton;

	EditText userName;
	EditText server;
	TextView hintArea;
	Spinner mailList;

	TextView copyright;

	private String mailName = "huwei@wandoujia.com";

	private List<String> list = new ArrayList<String>();
	private List<String> mail_list = new ArrayList<String>();
	private ArrayAdapter<String> adapter;

	private String LogPath = "/data/data/log.com/";

	UpLoadFile loadfile;

	private static final int UPLOADSUCCESS = 100;
	private static final int UPLOADFAILURE = 101;

	private String name;

	private String hintString = "1. ������Ͱ�ť\n2. ���������������޸ķ�������ַ";

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		try {
			init();
		} catch (Exception e) {

		}
	}

	private void init() {
		loadfile = new UpLoadFile();

		copyright = (TextView) findViewById(R.id.copyright);
		copyright.setGravity(Gravity.CENTER);

		server = (EditText) findViewById(R.id.serverip);

		StartButton = (Button) findViewById(R.id.sendLog);
		StartButton.setOnClickListener(this);

		hintArea = (TextView) findViewById(R.id.hintarea);
		// hintArea.setBackgroundColor(android.R.color.white);
		hintArea.setText(hintString);

		userName = (EditText) findViewById(R.id.username);

		SharedPreferences preferences = getSharedPreferences("LOGUPLOAD", 0);
		name = preferences.getString("USERNAME", "");
		String serverip = preferences.getString("SERVER",
				getString(R.string.server_ip));
		server.setText(serverip);
		userName.setText(name);

		mailList = (Spinner) findViewById(R.id.spinner_mail);

		list.add("Yaxin Liu");
		list.add("Xiaobo Zhang");
		list.add("Nan Zhang");
		list.add("Qiao Sun");
		list.add("Yilin Yu");
		list.add("Diao Liu");
		list.add("Wei Hu");

		mail_list.add("yaxin.liu@wandoujia.com");
		mail_list.add("xiaobo@wandoujia.com");
		mail_list.add("zhangnan@wandoujia.com");
		mail_list.add("sunqiao@wandoujia.com");
		mail_list.add("yuyilin@wandoujia.com");
		mail_list.add("match@wandoujia.com");
		mail_list.add("huwei@wandoujia.com");

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);

		mailList.setAdapter(adapter);
		// ���岽��Ϊ�����б����ø����¼�����Ӧ���������Ӧ�˵���ѡ��
		mailList.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@SuppressWarnings("unchecked")
			public void onItemSelected(AdapterView arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				mailName = mail_list.get(arg2);
				// System.out.print(mailName);
			}

			@SuppressWarnings("unchecked")
			public void onNothingSelected(AdapterView arg0) {
				// TODO Auto-generated method stub
			}
		});
	}
	/**
	 * Write information into file
	 */
	private void writeFile() {

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();

		try {
			
			name = name.toLowerCase().trim().replace(' ', '_');
			String phoneName =  Build.MODEL.toLowerCase().trim().replace(' ', '_');

			List<PackageInfo> packages = getPackageManager()
					.getInstalledPackages(
							PackageManager.GET_PERMISSIONS
									| PackageManager.GET_SIGNATURES
									| PackageManager.GET_SERVICES);
			for (int i = 0; i < packages.size(); i++) {
				PackageInfo packageInfo = packages.get(i);
				if (packageInfo.packageName.contains("com.wandoujia.phoenix")) {
					if (avaiableMedia()) {
						LogPath = android.os.Environment.getExternalStorageDirectory()
								.getAbsolutePath() + "/" + name + "_" + phoneName  + "_" + Build.VERSION.RELEASE  + 
								 "_" + packageInfo.versionName + "." + packageInfo.versionCode + "_" + imei + ".txt";
					} else
						LogPath = LogPath + name + "_" + phoneName  + "_" + Build.VERSION.RELEASE  +
						 "_" + packageInfo.versionName + "." + packageInfo.versionCode + "_" + imei + ".txt";
					
					File f = new File(LogPath);
					if (!f.exists())
						f.createNewFile();
					BufferedWriter output = new BufferedWriter(new FileWriter(f));
					output.write("�ֻ�Model: " + Build.MODEL + "\n");
					output.write("�ֻ�SDK: " + Build.VERSION.SDK + "\n");
					output.write("�汾��: " + Build.VERSION.RELEASE + "\n");
					output.write("�㶹��: "
							+ packageInfo.applicationInfo.processName + "\n");
					output.write("�㶹�԰汾: " + packageInfo.versionName + "\n");
					output.write("�㶹�԰汾��: " + packageInfo.versionCode + "\n");
					output.close();
					break;
				}
			}
			
		} catch (Exception e) {

		}
	}

	private Handler mHanler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPLOADSUCCESS:
				hintArea.setText("�ϴ���־�ɹ�");
				StartButton.setEnabled(true);
				break;
			case UPLOADFAILURE:
				hintArea.setText("�ϴ���־ʧ��");
				break;
			}
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

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		try {
			switch (v.getId()) {
			case R.id.sendLog:
				name = userName.getText().toString();
				if (name.equals("")) {
					Toast.makeText(LogUpdateActivity.this, "�������������",
							Toast.LENGTH_LONG).show();
					return;
				}

				SharedPreferences preferences = getSharedPreferences(
						"LOGUPLOAD", 0);
				preferences.edit().putString("USERNAME", name).commit();

				preferences.edit()
						.putString("SERVER", server.getText().toString())
						.commit();

				writeFile();

				StartButton.setEnabled(false);

				LogManagement.getInstance().getLog(LogPath);

				if (!loadfile.Connect_Socket(server.getText().toString())) {
					Toast.makeText(LogUpdateActivity.this, "���ӷ�����ʧ��",
							Toast.LENGTH_LONG).show();
					StartButton.setEnabled(true);
					return;
				} else {
					hintArea.setText("�����ϴ���־...");
					new Thread() {
						public void run() {
							try {
								Thread.sleep(2000);
							} catch (Exception e) {
								System.out.print(e.getMessage());
							}
							LogManagement.getInstance().StopLog();
							loadfile.uploadFile(LogPath, mailName);
							Message msg = mHanler.obtainMessage(UPLOADSUCCESS);
							mHanler.sendMessage(msg);
						}
					}.start();
				}
			}
		} catch (Exception e) {

		}
	}

}