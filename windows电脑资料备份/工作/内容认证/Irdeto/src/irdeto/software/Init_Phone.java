package irdeto.software;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.R.bool;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Init_Phone extends Activity {

	private List<PackageInfo> packs;

	public Map<String, Object> Application_Information = null;
	public Vector<String> Apk_Name = null;
	public Vector<String> Sort_ApkName = null;

	private String HOST = "10.86.8.234";
	// private String HOST = "10.86.14.11";
	private int port = 15001;
	// private PrintWriter out = null;

	private Button register_button;
	private Button cancel_button;
	private InputStream in = null;
	private DataOutputStream out = null;

	private EditText Ip = null;
	private ProgressDialog myDialog = null;
	private ProgressDialog waitDialog = null;
	private final int DOWNLOAD_COMPLETE = 2;
	private final int DOWNLOAD_FILELIST = 4;
	private final int DOWNLOAD_FILE = 3;
	private final int CONNECT_ERROR = 1;
	private final int SOCKET_ERROR = 5;
	private final int SOCKER_READ_ERROR = 6;
	private ListView listView = null;
	private ArrayList<Map<String, Object>> coll = null;
	private String[] picture = { ".jpg", ".bmp", ".png" };
	private static String ip_address = null;

	private byte[] temp = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init);

		register_button = (Button) findViewById(R.id.register);
		cancel_button = (Button) findViewById(R.id.Cancel);
		Ip = (EditText) findViewById(R.id.IP_Address);

		if(ip_address != null)
			Ip.setText(ip_address);
		register_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try
				{
				// setup a progreesDialog ,wait for a moment
				myDialog = ProgressDialog.show(Init_Phone.this, "Loading",
						"Connect to the Server", true);

				new Thread() {
					public void run() {
						if(!Connect_Socket())
							return;
						GetApkInformation();

						Sort_Map();

						Send_Data_To_Server();

					}
				}.start();
				}
				catch(Exception e)
				{
					
				}
			}
		});

		cancel_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent runintent = new Intent();
				runintent.setClass(Init_Phone.this, Initpage.class);
				startActivity(runintent);
				finish();
			}
		});

	}

	private void GetApkInformation() {
		packs = getPackageManager().getInstalledPackages(0);

		Application_Information = new HashMap<String, Object>(packs.size()
				+ packs.size() / 10);
		Apk_Name = new Vector<String>(packs.size());

		for (int i = 0; i < packs.size(); i++) {
			PackageInfo p = packs.get(i);
			String source = p.applicationInfo.sourceDir;
			FileInfo file = Irdeto.Find_FileInfo(source);
			Application_Information.put(source.hashCode() + "", file);
			Apk_Name.addElement(source.hashCode() + "");
		}

	}

	private Handler mhandler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case DOWNLOAD_COMPLETE:
				waitDialog.dismiss();
				new AlertDialog.Builder(Init_Phone.this)
						.setTitle("Sucess")
						.setMessage("Irdeto Protect Softwre")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

										Intent home = new Intent();
										home.setClass(Init_Phone.this,
												Initpage.class);
										startActivity(home);

										finish();
									}
								}).show();
				break;

			case DOWNLOAD_FILELIST:
				setContentView(listView);
				break;

			case DOWNLOAD_FILE:
				waitDialog = ProgressDialog.show(Init_Phone.this, "Loading",
						"Download the file", true);
				break;
			case CONNECT_ERROR:
				myDialog.dismiss();
				new AlertDialog.Builder(Init_Phone.this)
						.setTitle("Error")
						.setMessage("Can't connect to the server")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

									}
								}).show();

				break;
			case SOCKET_ERROR:
				new AlertDialog.Builder(Init_Phone.this)
						.setTitle("Error")
						.setMessage("Can't read information from server")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

									}
								}).show();
				break;

			case SOCKER_READ_ERROR:
				new AlertDialog.Builder(Init_Phone.this)
						.setTitle("Error")
						.setMessage("Unkown socket error")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

									}
								}).show();
				waitDialog.dismiss();
				break;
			}
		};

	};

	public boolean avaiableMedia() {
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean Judege_end() {
		for (int i = 0; i < 10; i++)
			if (temp[i] != '\0')
				return false;
		return true;
	}

	private void Download_file() {
		try {
			// read file name form server
			temp = new byte[512];
			// int test = in.read(temp);
			// in.
			// if can't read number in 5 seconds, there are maybe some error in
			// socket connection
			long start = System.currentTimeMillis();
			while (in.read(temp) == -1) {
				// test = in.read(temp);
				long end = System.currentTimeMillis();

				if (end - start > 5000) {
					Message message = mhandler.obtainMessage(SOCKER_READ_ERROR);
					mhandler.sendMessage(message);
					return;
				}
			}
			int length = 0;
			for (length = 0; length < 512; length++)
				if (temp[length] == '\0')
					break;

			byte[] file = new byte[length];

			for (int i = 0; i < length; i++)
				file[i] = temp[i];

			// String file_encrypt_name = "/data/data/irdeto.software/" + new
			// String(file);
			String file_encrypt_name = null;
			if (avaiableMedia()) {
				String root = android.os.Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ File.separator + "encrypt" + File.separator;

				File f = new File(root);
				if (!f.exists())
					f.mkdir();
				file_encrypt_name = root + new String(file);
			} else
				file_encrypt_name = "/data/data/irdeto.software/"
						+ new String(file);

			File f = new File(file_encrypt_name);
			if (!f.exists())
				f.createNewFile();

			DataOutputStream outdata_encrypt = null;
			OutputStream out_encrypt = null;
			out_encrypt = new FileOutputStream(file_encrypt_name);
			outdata_encrypt = new DataOutputStream(out_encrypt);

			// must send the end flags
			while (in.read(temp) != -1) {
				if (!Judege_end())
					outdata_encrypt.write(temp);
				// receive end
				else {
					// close the file
					out_encrypt.close();
					outdata_encrypt.close();
					// close the socket
					in.close();
					out.close();
					Message message = mhandler.obtainMessage(DOWNLOAD_COMPLETE);
					mhandler.sendMessage(message);
					return;
				}
			}
			// maybe some error in socket connection
			// delete the new file
			f.delete();
			Message message = mhandler.obtainMessage(SOCKER_READ_ERROR);
			mhandler.sendMessage(message);

		} catch (SocketTimeoutException e1) {

			Message message = mhandler.obtainMessage(SOCKER_READ_ERROR);
			mhandler.sendMessage(message);
		} catch (Exception e) {

			e.printStackTrace();
			Message message = mhandler.obtainMessage(SOCKER_READ_ERROR);
			mhandler.sendMessage(message);
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

	// read the server file list
	private void Read_File_List() {
		try {
			temp = new byte[512];
			long start = System.currentTimeMillis();
			while (in.read() == -1) {
				long end = System.currentTimeMillis();
				if (end - start > 5000) {
					Message message = mhandler.obtainMessage(SOCKET_ERROR);
					mhandler.sendMessage(message);
					myDialog.dismiss();
				}
			}
			Vector<String> file_list = new Vector<String>(5);
			while (in.read(temp) != -1) {
				int length = 0;
				for (length = 0; length < 512; length++)
					if (temp[length] == '\0')
						break;
				if (length == 0)
					break;
				byte[] file = new byte[length];
				for (int i = 0; i < length; i++)
					file[i] = temp[i];
				file_list.add(new String(file));
			}

			coll = new ArrayList<Map<String, Object>>();

			Map<String, Object> item;

			for (int i = 0; i < file_list.size(); i++) {
				if (IsPicture(file_list.elementAt(i))) {
					item = new HashMap<String, Object>();
					item.put("Title", file_list.elementAt(i));
					item.put("Icon", R.drawable.pho);
					item.put("Des", "Irdeto protected");
					coll.add(item);
				}
			}
			// add the music file to list
			for (int i = 0; i < file_list.size(); i++) {
				if (!IsPicture(file_list.elementAt(i))) {
					item = new HashMap<String, Object>();
					item.put("Title", file_list.elementAt(i));
					item.put("Icon", R.drawable.music);
					item.put("Des", "Irdeto protected");
					coll.add(item);
				}
			}

			SimpleAdapter adapter = new SimpleAdapter(this, coll,
					R.layout.mylist, new String[] { "Title", "Icon", "Des" },
					new int[] { R.id.filename, R.id.image, R.id.TextView01 });

			listView = new ListView(Init_Phone.this);
			listView.setAdapter(adapter);
			Message message = mhandler.obtainMessage(DOWNLOAD_FILELIST);
			mhandler.sendMessage(message);
			myDialog.dismiss();
		} catch (Exception e) {
			Message message = mhandler.obtainMessage(SOCKET_ERROR);
			mhandler.sendMessage(message);
			myDialog.dismiss();
			e.printStackTrace();
		}
	}

	private void Send_socket(byte[] encrypt_content) {
		try {
			out.writeInt(encrypt_content.length);
			out.flush();
			out.write(encrypt_content);
			out.flush();
		} catch (Exception e) {
			Message message = mhandler.obtainMessage(SOCKET_ERROR);
			mhandler.sendMessage(message);
		}
	}

	// send all apkinformation to service use socket
	private void Send_Data_To_Server() {
		try {
			Rsa rsa_encrypt = new Rsa();
			// test.encrypt(null);
			String total = Sort_ApkName.size() + "";
			// byte[] test = total.getBytes();
			byte[] encrypt_content = rsa_encrypt.encrypt(total.getBytes());
			Send_socket(encrypt_content);

			for (int i = 0; i < Sort_ApkName.size(); i++) {
				FileInfo file = (FileInfo) Application_Information
						.get(Sort_ApkName.elementAt(i));

				// String time_String =
				// Fill_Binary(Long.toBinaryString(file.st_ctime));
				// encrypt_content =
				// rsa_encrypt.encrypt(time_String.getBytes());
				// Send_socket(encrypt_content);
				// out.println(new String(encrypt_content));

				// String inode_String =
				// Fill_Binary(Long.toBinaryString(file.st_ino));
				// encrypt_content =
				// rsa_encrypt.encrypt(inode_String.getBytes());

				String info = Long.toBinaryString(file.st_ctime)
						+ Long.toBinaryString(file.st_ino);
				java.security.MessageDigest md = java.security.MessageDigest
						.getInstance("MD5");
				md.update(info.getBytes());

				byte[] out = md.digest();
				for (int k = 0; k < 16; k++) {
					if (out[k] < 0) {
						out[k] = (byte) (128 + out[k]);
						if (out[k] == 0)
							out[k] = 1;
					} else if (out[k] == 0)
						out[k] = 1;
					else if (out[k] == 0x0A || out[k] == 0x0D)
						out[k] = (byte) (out[k] + 1);
				}
				encrypt_content = rsa_encrypt.encrypt(out);
				Send_socket(encrypt_content);
			}
			// write filename into storage
			for (int i = 0; i < Sort_ApkName.size(); i++) {
				byte[] encrypt_apkname = rsa_encrypt.encrypt(Sort_ApkName
						.elementAt(i).getBytes());
				Send_socket(encrypt_apkname);
			}
			// send the length 0 to tell server the file has sent complete
			out.writeInt(0);
			out.flush();

			Read_File_List();

			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					final String File_path = coll.get(position).get("Title")
							.toString();
					Send_socket(File_path.getBytes());

					new AlertDialog.Builder(Init_Phone.this)
							.setTitle("Irdeto")
							.setMessage(
									coll.get(position).get("Title").toString())
							.setPositiveButton("Download",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											Message message = mhandler
													.obtainMessage(DOWNLOAD_FILE);
											mhandler.sendMessage(message);
											new Thread() {
												public void run() {
													Download_file();
												}
											}.start();
										}

									})
							.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub

										}
									}).show();
				}

			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Init Socket Information,Client to connect the server
	private boolean Connect_Socket() {
		try {
			HOST = Ip.getText().toString();
			ip_address = HOST;
			InetAddress serverAddr = InetAddress.getByName(HOST);// TCPServer.SERVERIP
			Socket socket = new Socket();
			SocketAddress serverAddress = new InetSocketAddress(serverAddr,
					port);
			
			// set socket read timeout,about 5 seconds
			socket.setSoTimeout(5000);
			// set timeout when connect the server,set 2 seconds
			socket.connect(serverAddress, 2000);
			
			out = new DataOutputStream(socket.getOutputStream());
			in = socket.getInputStream();
			return true;
			// DataOutputStream outtest = new DataOutputStream(out);
		} catch (SocketTimeoutException e1) {
			Message message = mhandler.obtainMessage(CONNECT_ERROR);
			mhandler.sendMessage(message);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			Message message = mhandler.obtainMessage(CONNECT_ERROR);
			mhandler.sendMessage(message);
			return false;
		}
	}

	// fill string with zero when the length is not times of 8
	private String Fill_Binary(String number) {
		int length = number.length();
		int idiv = length % 8;
		String result = "";
		if (idiv != 0) {
			for (int i = 0; i < 8 - idiv; i++)
				result += 0;
		}
		result += number;
		return result;
	}

	// sort map by create_time from low to big
	private void Sort_Map() {
		Sort_ApkName = new Vector<String>(Apk_Name.size());
		// select algorithm
		// every time select the min number
		for (int i = 0; i < Apk_Name.size(); i++) {
			int index = i;
			FileInfo f = (FileInfo) Application_Information.get(Apk_Name
					.elementAt(i));
			long time = f.st_ctime;
			for (int j = i + 1; j < Apk_Name.size(); j++) {
				FileInfo temp = (FileInfo) Application_Information.get(Apk_Name
						.elementAt(j));
				if (temp.st_ctime < time) {
					index = j;
					time = temp.st_ctime;
				} else if (temp.st_ctime == time) {

					if (Apk_Name.elementAt(index).compareTo(
							Apk_Name.elementAt(j)) > 0) {
						index = j;
					}
				}
			}
			Sort_ApkName.add(Apk_Name.elementAt(index));
			if (index != i) {
				String path = Apk_Name.elementAt(index);
				// Apk_Name.remove(index);
				Apk_Name.set(index, Apk_Name.elementAt(i));
				Apk_Name.set(i, path);
			}
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		try {
			in.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}