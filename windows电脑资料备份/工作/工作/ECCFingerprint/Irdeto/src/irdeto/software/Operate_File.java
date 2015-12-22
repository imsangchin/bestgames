package irdeto.software;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.content.pm.PackageInfo;
import android.os.Environment;
import android.util.Log;

public class Operate_File {
	public static List<PackageInfo> packs;
	private Map<String, Object> Application_Information = null;
	private Vector<String> Apk_Name = null;
	private Vector<String> Sort_ApkName = null;
	private Vector<String> Sort_End_ApkName = null;
	private String[] path_from_file;
	private int[] file_data;
	private int[] original_data;
	private int count;

	private byte[] key_aes = new byte[16];
	private int err;

	private String root = "/data/data/irdeto.software/";

	public static String file_decrypt = null;

	private String activity_file = "/data/data/irdeto.software/irdeto.mp3";

	private final int Buffer_Size = 4096;

	// private byte[] decrypt_content = new byte[Buffer_Size];
	public static Vector<byte[]> audio_data = new Vector<byte[]>(10);

	public static boolean IsStop = false;

	public static boolean IsPicture = false;

	private String[] picture = { ".jpg", ".bmp", ".png" };

	private DataInputStream datain = null;
	private InputStream in = null;

	private DataOutputStream re_outdata = null;
	private OutputStream re_out = null;

	private File f_temp = null;

	public static Vector<byte[]> pic_decrypt_content = null;

	public boolean Decrypt_File(boolean dir, String filename) {

		if (avaiableMedia()) {
			root = android.os.Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ "encrypt"
					+ File.separator;
		}
		File f = new File(root);
		if (dir) {
			// FilenameFilter filter = new FilenameFilter()
			File[] list = f.listFiles(new MyFilenameFilter());
			for (int i = 0; i < list.length; i++) {
				count = packs.size();
				Memory(packs.size());
				Get_Apkinformation();
				readFile_decrypt(list[i].getAbsolutePath(), false);
			}
		} else {
			count = packs.size();
			Memory(packs.size());
			Get_Apkinformation();

			return readFile_decrypt(filename, true);
		}
		return false;
	}

	private boolean avaiableMedia() {
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	// malloc the new memory to storage the apk information
	private void Memory(int count) {
		if (Application_Information != null)
			Application_Information.clear();
		Application_Information = new HashMap<String, Object>(count + count
				/ 10);
		if (Apk_Name != null)
			Apk_Name.clear();
		Apk_Name = new Vector<String>(count);
		if (Sort_ApkName != null)
			Sort_ApkName.clear();
		Sort_ApkName = new Vector<String>(count);
		if (Sort_End_ApkName != null)
			Sort_End_ApkName.clear();

		Sort_End_ApkName = new Vector<String>(count);
	}

	private void Get_Apkinformation() {
		for (int i = 0; i < packs.size(); i++) {
			PackageInfo p = packs.get(i);
			FileInfo info = Irdeto.Find_FileInfo(p.applicationInfo.sourceDir);
			Apk_Name.add(p.applicationInfo.sourceDir.hashCode() + "");
			Application_Information.put(p.applicationInfo.sourceDir.hashCode()
					+ "", info);

		}
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

	// attain the original data
	private void attain_orginal() {
		int count_index = 0;
		for (int i = 0; i < Sort_End_ApkName.size(); i++) {
			FileInfo f = (FileInfo) Application_Information
					.get(Sort_End_ApkName.elementAt(i));
			String time_Binary = Long.toBinaryString(f.st_ctime);
			// Log.i("error",f.st_ctime + "");
			// String time_result = Fill_Binary(time_Binary);

			String inode_Binary = Long.toBinaryString(f.st_ino);
			// String inode_result = Fill_Binary(inode_Binary);
			String info = time_Binary + inode_Binary;
			try {
				java.security.MessageDigest md = java.security.MessageDigest
						.getInstance("MD5");
				md.update(info.getBytes());
				byte[] out = md.digest();
				for (int k = 0; k < 16; k++) {
					if (out[k] < 0) {
						out[k] = (byte) (128 + out[k]);
						if (out[k] == 0)
							out[k] = 1;
					}
					else if (out[k] == 0)
						out[k] = 1;
					else if (out[k] == 0x0A || out[k] == 0x0D)
						out[k] = (byte) (out[k] + 1);
					original_data[count_index++] = out[k];
				}
			} catch (Exception e) {

			}
			/*
			 * try { for (int j = 0; j < time_result.length() / 8; j++) {
			 * original_data[count_index] = Integer.valueOf(
			 * time_result.substring(8 * j, 8 * (j + 1)), 2); count_index++; }
			 * for (int j = 0; j < inode_result.length() / 8; j++) {
			 * original_data[count_index] = Integer.valueOf(
			 * inode_result.substring(8 * j, 8 * (j + 1)), 2); count_index++; }
			 * } catch (Exception e) { Log.i("Exception", count_index + ""); }
			 */
		}
	}

	// pretreatement the filedata,order the data by the original file sequence
	private int pretreatement(int path_number) throws NoSuchAlgorithmException {

		Sort_Map();
		// Sort_End_ApkName = Sort_ApkName;
		for (int i = 0; i < Sort_ApkName.size(); i++)
			Sort_End_ApkName.add(Sort_ApkName.elementAt(i));
		Log.i("Count", Sort_ApkName.size() + "");
		Apk_Name.clear();

		for (int i = 0; i < Sort_ApkName.size(); i++)
			Apk_Name.add(Sort_ApkName.elementAt(i));
		// Apk_Name.clear();
		// int min = count;
		// int max = path_number;
		original_data = new int[count * 16 + count / 10 * 16];
		if (path_number < count) {
			file_data = new int[count * 16 + count / 10 * 16];

			// min = path_number;
			// max = count;
		} else {
			file_data = new int[path_number * 16 + path_number / 10 * 16];
		}

		// first search if any application was been deleted
		// if delete input original path into storage
		for (int i = 0; i < path_from_file.length; i++) {
			if (!Sort_ApkName.contains(path_from_file[i])) {
				if (i > Apk_Name.size())
					Apk_Name.add(path_from_file[i]);
				else
					Apk_Name.insertElementAt(path_from_file[i], i);
			}
			// the application has updated, which maybe re-install after
			// uninstalling it
			else {
				int location = Apk_Name.indexOf(path_from_file[i]);
				if (location != i) {
					String temp_path = Apk_Name.elementAt(location);
					Apk_Name.remove(location);
					if (i > Apk_Name.size())
						Apk_Name.add(temp_path);
					else
						Apk_Name.insertElementAt(temp_path, i);
				}
			}
		}

		int count_index = 0;
		for (int i = 0; i < Apk_Name.size(); i++) {
			if (Application_Information.containsKey(Apk_Name.elementAt(i))) {
				FileInfo f = (FileInfo) Application_Information.get(Apk_Name
						.elementAt(i));
				String time_Binary = Long.toBinaryString(f.st_ctime);
				// String time_result = Fill_Binary(time_Binary);

				String inode_Binary = Long.toBinaryString(f.st_ino);
				// String inode_result = Fill_Binary(inode_Binary);
				String info = time_Binary + inode_Binary;
				java.security.MessageDigest md = java.security.MessageDigest
						.getInstance("MD5");
				md.update(info.getBytes());
				byte[] out = md.digest();
				// md5 output is 128 bit number, which has 16 bytes
				for (int j = 0; j < 16; j++) {
					if (out[j] < 0) {
						out[j] = (byte) (128 + out[j]);
						if (out[j] == 0)
							out[j] = 1;
					} else if (out[j] == 0)
						out[j] = 1;
					else if (out[j] == 0x0A || out[j] == 0x0D)
						out[j] = (byte) (out[j] + 1);
					file_data[count_index] = out[j];
					count_index++;
				}

			} else {
				count_index = count_index + 16;
			}
		}

		return count_index;

	}

	// decrypt the picture data by aes
	private byte[] decrypt_pic(byte[] content, byte[] raw) {
		try {
			SecretKeySpec key = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			// Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			// int length = content.length;
			try {
				// cipher.
				return cipher.doFinal(content);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();

		} catch (NoSuchPaddingException e) {
			e.printStackTrace();

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

		return null;
	}

	// decrypt the data by aes
	private byte[] decrypt(byte[] content, byte[] raw) {
		try {
			SecretKeySpec key = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			// Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			// int length = content.length;
			try {
				// cipher.
				return cipher.doFinal(content);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();

		} catch (NoSuchPaddingException e) {
			e.printStackTrace();

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

		return null;
	}

	// read apk file head ,resolve the result and calculate the key to decrypt
	// the file
	private boolean readFile_decrypt(String fileName, boolean Isdecrypt) {

		try {
			IsPicture = false;

			in = new FileInputStream(fileName);
			// datain = new DataInputStream(new BufferedInputStream(in));
			datain = new DataInputStream(new BufferedInputStream(in));

			int random_number = datain.readInt();
			Log.i("random_number", random_number + "");
			int[] Random_number = new int[random_number];
			for (int i = 0; i < random_number; i++) {
				Random_number[i] = datain.readInt();
			}

			int max_length = datain.readInt();

			int path_number = datain.readInt();

			path_from_file = new String[path_number];

			for (int i = 0; i < path_number; i++) {
				path_from_file[i] = datain.readLine();
			}

			pretreatement(path_number);

			for (int i = 0; i < file_data.length; i++) {
				Random_number[i] = Random_number[i] ^ file_data[i];
			}

			Rscode rs_code = new Rscode();
			int m = 0;
			while (true) {
				m++;
				if (Math.pow(2.0, m) > random_number)
					break;
			}

			rs_code.init(random_number, m, max_length);
			err = rs_code.decode(Random_number);

			// can't resolve the message ,It's a illegal user
			if (err == -1) {
				Log.i("file", fileName);
				return false;
			}

			if (Isdecrypt) {
				// select 16 byte from the whole number,because the aes key must
				// be
				// 128 bit
				int key_len = random_number / 16;

				for (int i = 0; i < 16; i++) {
					key_aes[i] = (byte) Random_number[key_len * i];
				}
			}

			// if there no change in user mobile phone,no need to modify the
			// data
			if (err != 0) {
				attain_orginal();
				// if has extern storage
				if (avaiableMedia())
					activity_file = android.os.Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ File.separator
							+ "irdeto"
							+ fileName.substring(fileName.lastIndexOf('.'));
				f_temp = new File(activity_file);
				if (!f_temp.exists())
					f_temp.createNewFile();
				re_out = new FileOutputStream(activity_file);
				re_outdata = new DataOutputStream(new BufferedOutputStream(
						re_out));

				re_outdata.writeInt(random_number);
				for (int i = 0; i < original_data.length; i++) {
					Random_number[i] = Random_number[i] ^ original_data[i];
				}

				for (int i = 0; i < random_number; i++)
					re_outdata.writeInt(Random_number[i]);

				re_outdata.writeInt(max_length);

				re_outdata.writeInt(Sort_End_ApkName.size());
				Log.i("New", Sort_End_ApkName.size() + "");

				for (int i = 0; i < Sort_End_ApkName.size(); i++) {
					re_outdata.write(Sort_End_ApkName.elementAt(i).getBytes());
					re_outdata.write('\r');
				}
			}

			for (int i = 0; i < picture.length; i++) {
				if (fileName.endsWith(picture[i])) {
					IsPicture = true;
					Decrypt_picture(fileName, Isdecrypt);
					return true;
				}
			}

			return Decrypt_Video(fileName, Isdecrypt);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	private boolean Decrypt_Video(String fileName, boolean Isdecrypt) {
		try {
			byte[] tempbytes = new byte[Buffer_Size];
			long start = System.currentTimeMillis();
			int count = 0;
			audio_data.clear();
			MyThread_Play.index = 0;
			// can decrypt the file
			IsStop = false;
			while (datain.read(tempbytes) != -1) {
				if (err != 0)
					re_outdata.write(tempbytes);
				// if has stopped the music and no error in user's phone, break
				// the cycle
				if (IsStop && err == 0)
					break;
				if (IsStop)
					continue;
				if (Isdecrypt) {
					byte[] decrypt_content = decrypt(tempbytes, key_aes);

					audio_data.add(decrypt_content);
					count++;
					if (count == 20) {
						Thread id = new Thread(new MyThread_Play());
						id.start();
					}
				}
			}
			// player.playSound(audio_data, 1);
			Log.i("mp3 = ", audio_data.size() + "");
			long end = System.currentTimeMillis();
			Log.i("test time", end - start + "");
			in.close();
			datain.close();

			// no need to change the old file
			if (err != 0) {
				File oldfile = new File(fileName);
				oldfile.delete();
				re_out.close();
				re_outdata.close();
				if (f_temp != null)
					f_temp.renameTo(oldfile);
			}
			return true;

		} catch (Exception e) {
			// Start_MyService();
			// Log.i("Exception", e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private void Decrypt_picture(String fileName, boolean Isdecrypt) throws IOException {
		try {
			if (pic_decrypt_content != null)
				pic_decrypt_content.clear();
			else {
				pic_decrypt_content = new Vector<byte[]>(10);
			}
			// int decrypt_count = datain.readInt();
			int length = datain.readInt();
			while (length != 0) {
				if (err != 0) {
					re_outdata.writeInt(length);
				}
				// Log.i("decrypt", decrypt_count + "");
				byte[] tempbytes = new byte[length];

				datain.read(tempbytes);
				if (err != 0)
					re_outdata.write(tempbytes);
				if (Isdecrypt) {
					pic_decrypt_content.add(decrypt_pic(tempbytes, key_aes));
				}
				length = datain.readInt();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			in.close();
			datain.close();

			// no need to change the old file
			if (err != 0) {
				File oldfile = new File(fileName);
				oldfile.delete();
				re_outdata.flush();
				re_out.close();
				re_outdata.close();
				if (f_temp != null)
					f_temp.renameTo(oldfile);
			}
		}
	}

}