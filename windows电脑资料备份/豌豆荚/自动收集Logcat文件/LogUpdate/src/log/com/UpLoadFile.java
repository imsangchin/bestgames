package log.com;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class UpLoadFile {
	private int port = 15001;
	private DataInputStream din = null;
	private DataOutputStream dout = null;

	/**
	 * �ж��Ƿ�������ӷ�����
	 * 
	 * @param server_ip
	 *            ��������Ip��ַ
	 * @return false ���Ӳ��ɹ� true ���ӳɹ�
	 */
	public boolean Connect_Socket(String server_ip) {
		try {
			// ip_address = HOST;
			InetAddress serverAddr = InetAddress.getByName(server_ip);// TCPServer.SERVERIP
			Socket socket = new Socket();
			SocketAddress serverAddress = new InetSocketAddress(serverAddr,
					port);

			// ����Socket 5�볬ʱ
			socket.setSoTimeout(5000);
			// �������� 2�볬ʱ
			socket.connect(serverAddress, 2000);

			dout = new DataOutputStream(socket.getOutputStream());
			return true;
		} catch (SocketTimeoutException e1) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * �ϴ��ļ�
	 * 
	 * @param filename
	 */
	public void uploadFile(String filename) {
		try {
			File file = new File(filename); // �����ļ�
			// �ļ������ڵ�ʱ�򷵻�
			if (!file.exists()) {
				return;
			}
			FileInputStream fis = new FileInputStream(file); // �����ļ�������
			din = new DataInputStream(new BufferedInputStream(fis)); // �û�������װ�ļ�����������߶�ȡ�ٶȣ���Ȼ���ٰ�װ������������

			filename = filename.substring(filename.lastIndexOf('/') + 1);

			dout.writeUTF(filename);

			byte[] buffer = new byte[1024]; // ���建��
			int len = 0;
			while ((len = din.read(buffer)) != -1) {
				dout.write(buffer, 0, len); // ���������������
			}

			dout.flush();
			dout.close();
			// ��־�ļ�������Ϻ󣬽���ɾ������־�ļ�����ռ�ռ䣩
			file.delete();
		} catch (Exception e) {

		}
	}
}