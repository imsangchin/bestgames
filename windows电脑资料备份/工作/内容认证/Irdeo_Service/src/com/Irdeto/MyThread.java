package com.Irdeto;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class MyThread extends Thread{ 
	private Socket socket_client;
	private static String Init_Android = "e:\\ctodemo\\demo\\Android.txt";
	private DataInputStream in = null;
	private OutputStream out = null;
	private FileOutputStream outSTr = null;
	private BufferedOutputStream Buff = null;
	private String line =null;
	private byte[] temp ;
	private Rsa decrypt_rsa;
	
	public MyThread(Socket socket)
	{
		socket_client = socket;
		decrypt_rsa = new Rsa();
	}
	
	public void run()
	{
		 
		try
		{
			 in=new DataInputStream(socket_client.getInputStream()); 

			 //PrintWriter out=new PrintWriter(socket_client.getOutputStream(),true); 
			 out = socket_client.getOutputStream();
			 outSTr = new FileOutputStream(new File(Init_Android));
			 
			 Buff = new BufferedOutputStream(outSTr);
			
			 int length = in.readInt();
			 while(length != 0)
			 {
				 int index = 0;
				 byte[] content = new byte[length];
				 in.read(content);
				 //decrypt the content has some error,there are have to many 0 in byte array
				 byte[] decrypt_content = decrypt_rsa.decrypt(content);
				 for(int i=0;i<decrypt_content.length;i++)
				 {
					 if(decrypt_content[i] != 0)
					 {
						 index = i;
						 break;
					 }
				 }				 
				 Buff.write(decrypt_content, index, decrypt_content.length - index);
				 Buff.write('\r');
				 length = in.readInt();
			 }

			 Buff.close();
				 
			 File dir_f = new File("e:\\ctodemo\\server");
			 File[] dir_list = dir_f.listFiles();
			 
			 int i = 0;
			 int j = 0;
			 temp = new byte[512];
			 out.write('1');
			 out.flush();
			 for(i = 0;i<dir_list.length;i++)
			 {
				 byte[] filename = dir_list[i].getName().getBytes();
				 for(j = 0 ;j<filename.length;j++)
					 temp[j] = filename[j];
				 temp[j] = '\0';
				 out.write(temp);
				 out.flush();
			 }
			
			 //should flush the buffer after use the buffer
			 temp[0] = '\0';
			 out.write(temp);
			 out.flush();
			 //read file name from client
			// new Thread()
			 //{
				// public void run()
				// {
					 //while(true)
					// {
			Read_File_Client();
			 
					// }
				 //}
			// }.start();

		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
	  }
	
	private void Read_File_Client()
	{
		try
		{
			int length = in.readInt();
			 while(length == -1)
			 {
				 length = in.readInt();
			 }
			 
			 byte[] file_name = new byte[length];
			 in.read(file_name);
			 line = new String(file_name);
			 //encrypt the file which client selected
			 new Encrypt("e:\\ctodemo\\server\\" + line);
			 out.flush();
			
			 String file = line;
			 byte[] filename = file.getBytes();
			 int i =0;
			 for(i = 0 ;i<file.getBytes().length;i++)
				 temp[i] = filename[i];
			temp[i] = '\0';
			out.write(temp);
			out.flush();
		
			String Encryptfile = "e:\\ctodemo\\demo\\"+ line;
			
			File f = new File(Encryptfile);
		
			InputStream ein = new FileInputStream(Encryptfile);
			DataInputStream datain = new DataInputStream(ein);
		
			 while(datain.read(temp) != -1)
			 {
				 out.write(temp);
				 out.flush();
			 }
			
			 ein.close();
			 datain.close();
			 for(i = 0;i<10;i++)
				 temp[i] = '\0';
			 out.write(temp);
			 out.flush();
			// Thread.currentThread().destroy();
			 in.close();
			 out.close();
		
			 //delete the file after output the file to client
			 //f.delete();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}