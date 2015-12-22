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
	 * 判断是否可以连接服务器
	 * 
	 * @param server_ip
	 *            服务器的Ip地址
	 * @return false 连接不成功 true 连接成功
	 */
	public boolean Connect_Socket(String server_ip) {
		try {
			// ip_address = HOST;
			InetAddress serverAddr = InetAddress.getByName(server_ip);// TCPServer.SERVERIP
			Socket socket = new Socket();
			SocketAddress serverAddress = new InetSocketAddress(serverAddr,
					port);

			// 设置Socket 5秒超时
			socket.setSoTimeout(5000);
			// 设置连接 2秒超时
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
	 * 上传文件
	 * 
	 * @param filename
	 */
	public void uploadFile(String filename) {
		try {
			File file = new File(filename); // 定义文件
			// 文件不存在的时候返回
			if (!file.exists()) {
				return;
			}
			FileInputStream fis = new FileInputStream(file); // 定义文件输入流
			din = new DataInputStream(new BufferedInputStream(fis)); // 用缓存流包装文件输入流（提高读取速度），然后再包装成数据输入流

			filename = filename.substring(filename.lastIndexOf('/') + 1);

			dout.writeUTF(filename);

			byte[] buffer = new byte[1024]; // 定义缓存
			int len = 0;
			while ((len = din.read(buffer)) != -1) {
				dout.write(buffer, 0, len); // 向服务器发送数据
			}

			dout.flush();
			dout.close();
			// 日志文件发送完毕后，进行删除（日志文件过大，占空间）
			file.delete();
		} catch (Exception e) {

		}
	}
}