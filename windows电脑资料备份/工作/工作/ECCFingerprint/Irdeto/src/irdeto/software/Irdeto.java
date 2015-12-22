package irdeto.software;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

class FileInfo {
	long st_mode; // 文件对应的模式，文件，目录等 　
	long st_ino; // i-node节点号 　　
	long st_dev; // 设备号码 　
	long st_rdev; // 特殊设备号码 　
	long st_nlink; // 文件的连接数 　
	long st_uid; // 文件所有者 　　
	long st_gid; // 文件所有者对应的组 　　
	long st_size; // 普通文件，对应的文件字节数 　　
	long st_atime; // 文件最后被访问的时间 　
	long st_mtime; // 文件内容最后被修改的时间 　　
	long st_ctime; // 文件状态（属性）改变时间 　　
	long st_blksize; // 文件内容对应的块大小 　　
	long st_blocks; // 文件内容对应的块数量 　

	public void init() {
	}
};

public class Irdeto extends Activity {

	public static native FileInfo Find_FileInfo(String path);

	private final int EVENT_TIME_TO_RUN_APP = 1;
	private final int EVENT_TIME_ILLEGAL_USER = 2;

	private final int EVENT_TIME_TEST = 3;
	private final int EVENT_TIME_TO_START = 4;

	private ProgressDialog myDialog;
	private PackageInfo app;

	private ListView listView = null;

	private String root = "/data/data/irdeto.software/";

	private MyPlayer player = null;

	private Thread Operate;
	private String[] picture = { ".jpg", ".bmp", ".png" };

	// private MediaPlayer player = null;

	public boolean avaiableMedia() {
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean IsPicture(String fileName) {
		for (int i = 0; i < picture.length; i++) {
			if (fileName.endsWith(picture[i])) {
				return true;
			}
		}
		return false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);

		listView = new ListView(this);
		if (avaiableMedia()) {
			root = android.os.Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ "encrypt"
					+ File.separator;
		}

		File f = new File(root);
		if (!f.exists()) {
			new AlertDialog.Builder(Irdeto.this)
					.setTitle("Irdeto")
					.setMessage("There is no protected software")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									finish();
								}
							}).show();

			f.mkdir();
		}

		// FilenameFilter filter = new FilenameFilter()
		File[] list = f.listFiles(new MyFilenameFilter());

		final ArrayList<Map<String, Object>> coll = new ArrayList<Map<String, Object>>();

		Map<String, Object> item;

		if (list.length != 0) {
			// first add the picture file to list
			for (int i = 0; i < list.length; i++) {
				if (IsPicture(list[i].getName())) {
					item = new HashMap<String, Object>();
					item.put("Title", list[i].getName());
					item.put("Icon", R.drawable.pho);
					item.put("Des", "Irdeto protected");
					coll.add(item);
				}
			}
			// add the music file to list
			for (int i = 0; i < list.length; i++) {
				if (!IsPicture(list[i].getName())) {
					item = new HashMap<String, Object>();
					item.put("Title", list[i].getName());
					item.put("Icon", R.drawable.music);
					item.put("Des", "Irdeto protected");
					coll.add(item);
				}
			}
		} else if (list.length == 0) {
			new AlertDialog.Builder(Irdeto.this)
					.setTitle("Irdeto")
					.setMessage("There is no protected software")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									finish();
								}
							}).show();

		}

		SimpleAdapter adapter = new SimpleAdapter(this, coll, R.layout.mylist,
				new String[] { "Title", "Icon", "Des" }, new int[] {
						R.id.filename, R.id.image, R.id.TextView01 });

		listView.setAdapter(adapter);
		// set the click listenr
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				final String File_path = root
						+ coll.get(position).get("Title").toString();

				new AlertDialog.Builder(Irdeto.this)
						.setTitle("Irdeto")
						.setMessage(coll.get(position).get("Title").toString())
						.setPositiveButton("Run",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										// listView.setClickable(false);

										MyThread_Play.play.stop_sound();

										Decrypt_Apk(File_path);
										// listView.setClickable(true);
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

									}
								}).show();
			}

		});

		setContentView(listView);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		MyThread_Play.play.stop_sound();
	}

	// create a new thread to decrypt file
	private void Decrypt_Apk(final String FileName) {
		// myDialog = ProgressDialog.show(Irdeto.this, "Loading",
		// "Irdeto protect software", true);
		try {
			Operate = new Thread() {
				public void run() {
					// Message message =
					// mHandler.obtainMessage(EVENT_TIME_TO_RUN_APP);
					// mHandler.sendMessage(message);
					if (Operate_File.packs != null)
						Operate_File.packs.clear();
					Operate_File.packs = getPackageManager()
							.getInstalledPackages(0);

					Operate_File decrypt = new Operate_File();

					if (IsPicture(FileName)) {

						Message message = mHandler
								.obtainMessage(EVENT_TIME_TO_START);
						mHandler.sendMessage(message);
					}

					if (decrypt.Decrypt_File(false, FileName)) {
						Message message = mHandler
								.obtainMessage(EVENT_TIME_TO_RUN_APP);
						mHandler.sendMessage(message);

					} else {
						Message message = mHandler
								.obtainMessage(EVENT_TIME_ILLEGAL_USER);
						mHandler.sendMessage(message);
					}
				}

			};

			Operate.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// use Hanlder to redraw the screen,because the sub thread can't redraw
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case EVENT_TIME_TO_RUN_APP:
				// count = packs.size();
				try {

					if (Operate_File.IsPicture) {
						try {
							myDialog.dismiss();
							Intent it = new Intent();
							it.setClass(Irdeto.this, ViewPicture.class);
							startActivity(it);
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (!Operate_File.IsStop)
						Toast.makeText(Irdeto.this, "Decrypt Sucess",
								Toast.LENGTH_LONG).show();
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}

			case EVENT_TIME_ILLEGAL_USER:
				// myDialog.dismiss();
				new AlertDialog.Builder(Irdeto.this)
						.setTitle("Failure")
						.setMessage("Illegal User")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										System.exit(0);
									}
								}).show();
				break;

			case EVENT_TIME_TO_START:
				myDialog = ProgressDialog.show(Irdeto.this, "Loading",
						"Decrypt Picture", true);
				break;
			}

		}
	};
	static {
		System.loadLibrary("Irdeto");
	}
}