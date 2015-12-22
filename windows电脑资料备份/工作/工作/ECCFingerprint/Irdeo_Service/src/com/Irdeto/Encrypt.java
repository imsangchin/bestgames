package com.Irdeto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Encrypt
{ 
	private  int[] file_data;
	private  String[] filePath;
	private  int max_length;
	private  int[] Rand_number;
	private  int code_number;
	
	private  String file_encrypt_path = "E:\\ctodemo\\demo\\";
	
	private  String Init_Android = "E:\\ctodemo\\demo\\Android.txt";
	
	private String[] picture= {".jpg",".bmp",".png"};
	private DataInputStream datain = null;
	private InputStream in = null;
	 
	private DataOutputStream outdata = null;
	private BufferedInputStream bufferout = null;
	private OutputStream out= null;
	
	//read file by byte,and write a new encrypted file 
	private  void readFileByBytes(String fileName,byte[] key) {
	     try 
	     {
	    	in = new FileInputStream(fileName);
	        //Use Buffer to improve efficiency
	    	bufferout = new  BufferedInputStream(in);
	   
	        datain = new DataInputStream(bufferout);
	        
	        write_help_information(fileName);
	    	
	    	for(int i = 0 ;i<picture.length;i++)
	    	{
	    		if(fileName.endsWith(picture[i]))
	    		{
	    			File f = new File(fileName);
	    			long length = f.length();
	    			encrypt_picture(length,key);
	    			return;
	    		}
	    	}
	        
	       encrypt_video(key);    
	      
	   } catch (Exception e) 
	   {
	        e.printStackTrace();
	   } 
	 }

	 
	//this function use to encrypt video information
	private void encrypt_video(byte[] key) throws IOException
	{
		//read content from file and encrypt them ,write  into new file
		byte[] tempbytes = new byte[4096];
        while (datain.read(tempbytes) != -1) 
        {
        	byte[] result = encrypt(tempbytes,key);
        	outdata.write(result);
         }
        //close all the stream
        outdata.flush();
        
        in.close();
        out.close();
        datain.close();
        outdata.close();
	}
	
	private void encrypt_picture(long length,byte[] key) throws IOException
	{
		int count = 0;
		//divide the big picture to many parts,which has 80KB 
		while(length > 80920)
		{
			length = length - 80920;
			byte[] tempbytes = new byte[80920];
			datain.read(tempbytes);
			byte[] result = encrypt_pic(tempbytes,key);
			outdata.writeInt(result.length);
	        outdata.write(result);
			count ++;
		}
		
		byte[] tempbytes = new byte[(int)length];
	
		datain.read(tempbytes);
		byte[] result = encrypt_pic(tempbytes,key);
		outdata.writeInt(result.length);
        outdata.write(result);
        
        //Client write 0 tell client file has sent over
        outdata.writeInt(0);
	
        //close all the stream
        outdata.flush();
		in.close();
		datain.close();
		out.close();
		outdata.close();
	}
	
	//write help information in file head
	private void write_help_information(String fileName)
	{
		try
		{
		 	String file_name = file_encrypt_path + fileName.substring(fileName.lastIndexOf('\\') + 1);
	        File f = new File(file_name);
	        //if file doesn't exist,first to create the new file and then output the stream to new file
	        if(!f.exists())
	        	f.createNewFile();
	        
	        out = new FileOutputStream(file_name);
	        outdata = new DataOutputStream(new BufferedOutputStream(out));
	            
	        outdata.writeInt(code_number);
	        for(int i =0;i<code_number;i++)
	            outdata.writeInt(Rand_number[i]);
	        
	        //write the Rscode's  file data length ,in order to preserve the same Rscode
	        // Rscode must have same NN,KK
	        outdata.writeInt(max_length);
	        outdata.writeInt(filePath.length);
	         
	        //write filepath into file
	        for(int i = 0;i<filePath.length;i++)
	        {
	            outdata.write(filePath[i].getBytes());
	            outdata.write('\r');
	        }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	            
	}
	
	//AES encrypt algorithm,use aes defaule mode,which will fill the content
	private   byte[] encrypt_pic(byte[] byteContent,byte[] raw) 
	{   
        try 
        {                
        	SecretKeySpec key = new SecretKeySpec(raw, "AES");   
            Cipher cipher = Cipher.getInstance("AES");
        	//Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key); 
            byte[] result = cipher.doFinal(byteContent);            
            return result; 
        } catch (NoSuchAlgorithmException e) {   
                e.printStackTrace();   
        } catch (NoSuchPaddingException e) {   
                e.printStackTrace();   
        } catch (InvalidKeyException e) {   
                e.printStackTrace();   
        } catch (IllegalBlockSizeException e) {   
                e.printStackTrace();   
        } catch (BadPaddingException e) {   
                e.printStackTrace();   
        }   
        return null;   
	}  

	
	//AES encrypt algorithm,encrypt video use NoPadding,which dosen't pad the content when
	//information doesn't has 16bytes.
	//In this module all the content's size must be the multiple of 16 bytes 
	private   byte[] encrypt(byte[] byteContent,byte[] raw) 
	{   
        try 
        {                
        	SecretKeySpec key = new SecretKeySpec(raw, "AES");   
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key); 
            byte[] result = cipher.doFinal(byteContent);            
            return result; 
        } catch (NoSuchAlgorithmException e) {   
                e.printStackTrace();   
        } catch (NoSuchPaddingException e) {   
                e.printStackTrace();   
        } catch (InvalidKeyException e) {   
                e.printStackTrace();   
        } catch (IllegalBlockSizeException e) {   
                e.printStackTrace();   
        } catch (BadPaddingException e) {   
                e.printStackTrace();   
        }   
        return null;   
	}  



	//AES decrypt algorithm
 	public  byte[] decrypt(byte[] content,byte[] raw) {   
		try 
		{   
			SecretKeySpec key = new SecretKeySpec(raw, "AES");               
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] result = cipher.doFinal(content);   
			return result; 
	        
		} catch (NoSuchAlgorithmException e) {   
	                e.printStackTrace();   
	        
		} catch (NoSuchPaddingException e) {   
	                e.printStackTrace();   
	       
		} catch (InvalidKeyException e) {   
	                e.printStackTrace();   
	        
		} catch (IllegalBlockSizeException e) {   
	                e.printStackTrace();   
	       
		} catch (BadPaddingException e) {   
	                e.printStackTrace();        
		}   
	        
		return null;   
	}  
	
	//create the original information by reading android.txt
 	//delete the file at last
 	public  void Read_Init_File(String fileName) 
 	{
       // File file = new File(fileName);
      //  BufferedReader reader = null;
 		DataInputStream reader;
        try 
        {  	
        	//reader = new BufferedReader(new FileReader(file));
        	reader = new DataInputStream(new FileInputStream(fileName));
            String tempString = null;
            String total = reader.readLine();
            
            int total_number = Integer.valueOf(total);
            int count = 0;
            max_length = total_number*16 + (total_number/10)*16;
            file_data = new int[max_length];
            filePath = new String[total_number];
            int index = 0;
            
            //every file data was divided into two lines,first is time,next is inode number
            int all_length = total_number * 16 + total_number - 1;
            //reader.r
            
            int tempbyte = -1;
            //tempbyte = reader.read();
            while(count < all_length)
            {
            	tempbyte = reader.read();
            	count ++;
            	if(count % 17 == 0)
            		continue;
            	file_data[index] = tempbyte;
            	index ++;	
            }
            
            int count_index = 0;
            while ((tempString = reader.readLine()) != null) {
            	if(tempString.equals(""))
            		continue;
            	//all the file name was memory at last
            		filePath[count_index++] = tempString; 
       
            }
            	//first storage is time number, which length has 32 byte
          
            	/*
            	if(count % 2 != 0)
            	{
            		for(int i = 0;i<4;i++)
            		{
            			file_data[index] = Integer.valueOf(tempString.substring(0 + 8*i, 8*(i + 1)),2);
            			index ++;
            		}     
            	}
            	else
            	{
            		for(int i = 0;i<2;i++)
            		{
            			file_data[index] = Integer.valueOf(tempString.substring(0 + 8*i, 8*(i + 1)),2);
            			index ++;
            		}
            			
            	}
            	count++;  */
            //}
            reader.close();
           // file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        } 

    }
	
	//create random number in 0 -255,which can store in one byte
	private  void Random_number(int max)
	{
		Rand_number = new int[max + 1];
		Random rd = new Random(); 
		int length = max - max_length / 10;
		if(length % 2 == 0)
		{
			length++;
		}
		for(int i = 0;i<length;i++)
			Rand_number[i] = rd.nextInt(255);
			
	}
	 
	//compute the index of RsCode
	private  int Computer_Power()
	{
		int i = 1;
		while(i != 0)
		{
			if(Math.pow(2.0, i) > max_length)
				break;
			i++;
		}
		return i;
	}
	
	
	private  void Encrypt_File(String File_Name)
	{
		Read_Init_File(Init_Android);
		//generate RSCODE object,which use to create rscode
		Rscode rs_code = new Rscode();
		 
		int index = Computer_Power();
		
		// the code total number for Rscode
		code_number =(int)Math.pow(2.0, (double)index) - 1;
		//set min information length as 511
		if(code_number <2047)
		{
			code_number = 2047;
			index = 11;
		}
		//Initialize the rscoding 
		rs_code.init(code_number,index,max_length);
		 
		Random_number(code_number);
	
		rs_code.encode(Rand_number);
	
		//select 16 bytes as key form the long information
		int key_len = code_number / 16;
	    byte[] key_aes = new byte[16];
	    for(int i = 0;i<16;i++)
	    {
	    	key_aes[i] = (byte)Rand_number[key_len * i];
	    }
	    
		//use random number ^ apkfiledata ,send this information to client
	    //client use this information to decrypt file
		for(int i = 0;i<max_length;i++)
		{
			 Rand_number[i] = Rand_number[i]^file_data[i];
		}
		
		
		
		readFileByBytes(File_Name,key_aes);
	}

	public Encrypt(String File_Name)
	{
		Encrypt_File(File_Name);
	}
	
}