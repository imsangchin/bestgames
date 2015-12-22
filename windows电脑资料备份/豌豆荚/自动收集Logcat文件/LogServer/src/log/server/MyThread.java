package log.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;

class MyThread extends Thread {
	private Socket socket_client;
	private DataInputStream din = null;
	private OutputStream dout = null;
	private String filename = "log.txt";
	private static final String logDir = "e:\\LogDir\\";

	public MyThread(Socket socket) {
		socket_client = socket;
	}

	public void run() {
		try {
			din = new DataInputStream(new BufferedInputStream(
					socket_client.getInputStream()));

			filename = din.readUTF();

			File file = new File(logDir + filename);

			// SendEmail.sendmail(logDir + filename);
			if (file.exists()) {
				file.delete();
				file.createNewFile();
			}

			// file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			dout = new DataOutputStream(new BufferedOutputStream(fos));

			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = din.read(buffer)) != -1) {
				dout.write(buffer, 0, len);
			}
			System.out.println("Read End");
			dout.flush();
			dout.close();
			fos.close();
			
			LogServer.ta.append("用户退出,日志上传成功\n");
			LogServer.count --;
			
			sendMail.send(logDir + filename);
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
	}
}