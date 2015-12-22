package log.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;

class MyThread extends Thread{ 
	private Socket socket_client;
	private DataInputStream din = null;
	private OutputStream dout = null;
	private String filename ="log.txt";
	private static final String logDir = "e:\\LogDir\\";
	
	public MyThread(Socket socket)
	{
		socket_client = socket;
	}
	
	public void run()
	{
		try
		{
             din = new DataInputStream(new BufferedInputStream(socket_client  
                     .getInputStream()));// 使用缓存进行包装，提示读取速度  

             filename = din.readUTF();
             
             File file = new File(logDir + filename); 
             
            // SendEmail.sendmail(logDir + filename);
             if(file.exists()){
            	 file.delete();
            	 file.createNewFile();
             }
             System.out.println("文件名称:" + filename); // 显示接收文件名称
             //file.createNewFile();
             FileOutputStream fos = new FileOutputStream(file);  
             dout = new DataOutputStream(new BufferedOutputStream(  
                     fos));  

             byte[] buffer = new byte[1024];  
             int len = 0;  
             while ((len = din.read(buffer)) != 1024) {  
                 dout.write(buffer, 0, len);  
             }  
             System.out.println("Read End");
             dout.flush();  
             dout.close();
             fos.close();
             
             SendEmail.sendmail(logDir + filename);
		}
		catch(Exception e)
		{
			System.out.print(e.getMessage());
			 SendEmail.sendmail(logDir + filename);
		}
	  }
}