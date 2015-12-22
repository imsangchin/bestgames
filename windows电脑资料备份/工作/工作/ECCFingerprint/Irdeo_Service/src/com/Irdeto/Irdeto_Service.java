package com.Irdeto;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Irdeto_Service 
{
	private static final int port = 15001;
	private ServerSocket server;
	
	private JFrame f;
	private Button[] btn=new Button[2];
	
	public Irdeto_Service() 
	{
	    initComponent(); 
	}

	private void initComponent() 
	{
		final ImageIcon myimage=new ImageIcon("security.jpg");
		final Image image=myimage.getImage();
		f = new JFrame()
		{
			private static final long serialVersionUID = 1L;
			public void paint(Graphics g)
			{
				g.drawImage(image, 0, 0,this.getWidth(),this.getHeight(),null);  
			}
		  
		};
		  
      // f = new Frame("Irdeto Protect Service");
	   f.setTitle("Irdeto Protect Service");
       f.setLayout(new BorderLayout());   //The frame uses BorderLayout
       f.addWindowListener(new WindowAdapter() 
       {
           public void windowClosing(WindowEvent evt) 
           {
               System.exit(0);
           }
       });
       Panel centerPanel = new Panel();
       
       ButtonListener listen = new ButtonListener(); 
       centerPanel.setLayout(new GridLayout(1, 2));
       btn[0] = new Button("Start");
       btn[0].addActionListener(listen);
       btn[1] = new Button("End");
       btn[1].addActionListener(listen);
       btn[1].setEnabled(false);
       
       for(int i = 0 ;i<2;i++)
    	   centerPanel.add(btn[i]);
       
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
       //Dimension frameSize = frame.getSize(); 
       centerPanel.setBounds(screenSize.width / 2 - 250, screenSize.height / 2 - 250, 500, 500);
       f.setBounds(screenSize.width / 2 - 250, screenSize.height / 2 - 250, 500, 500);
       f.add(centerPanel,BorderLayout.SOUTH);      
       f.setVisible(true);

	}

	public static void main(String[] args) {
	
		File dirFile = new File("e:\\ctodemo\\demo");
	
		if(!dirFile.exists())
		 		dirFile.mkdir();
	       new Irdeto_Service();
	   }
	
	class ButtonListener implements ActionListener
	{
	       public void actionPerformed(ActionEvent e)
	       {
	    	   String op = e.getActionCommand();
	    	   if(op.compareTo("Start") == 0)
	    	   {
	    		   //set the button attribute
	    		   btn[0].setEnabled(false);
	    		   btn[1].setEnabled(true);
	    		   new Thread()
	    		   {
	    			   public void run()
	    			   {
	    				   try
	    				   {
	    					   server = new ServerSocket(port);
	    					   //wait for more client
	    					   while(true)
	    					   {
			   	    				Socket client = server.accept();
			   	    				MyThread client_accept = new MyThread(client);
			   	    				client_accept.start();
			   	    				Thread.sleep(100);
			   	    			}
		   	    			}
		   	    			catch(Exception ex)
		   	    			{
		   	    				ex.printStackTrace();
		   	    			}
	    			   }
	    		   }.start();
	    		  
	    	   }
	    	   else if(op.compareTo("End") == 0)
	    	   {
	    		   try
	    		   {   //close the socket
	    			   server.close();
	    			   //quit the application
	    			   System.exit(0);
	    		   }
	    		   catch(Exception ex)
	    		   {
	    			   ex.printStackTrace();
	    		   }
	    		   
	    	   }
	       }
	}


}