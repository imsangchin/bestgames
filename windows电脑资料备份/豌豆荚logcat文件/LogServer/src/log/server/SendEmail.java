package log.server;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;

public class SendEmail {

    public static void sendmail(String filename){

//     创建一个Email附件
    	try
    	{
    		EmailAttachment emailattachment = new EmailAttachment();

       		emailattachment.setPath(filename);

       		emailattachment.setDisposition(EmailAttachment.ATTACHMENT);

       		emailattachment.setDescription("This is Smile picture");

       		emailattachment.setName("bulktree");

       		//创建一个email

       		MultiPartEmail multipartemail = new MultiPartEmail();

       		multipartemail.setHostName("smtp.gmail.com");

       		multipartemail.addTo("nwucomputer2005@163.com", "Hu Wei");

       		multipartemail.setFrom("huwei.nwu@gmail.com", "Hu Wei");

       		multipartemail.setAuthentication("huwei.nwu@gmail.com", "nwuNFC123");

       		multipartemail.setSubject("This is a attachment Email");

       		multipartemail.setMsg("this a attachment Eamil Test");

       		//添加附件

       		multipartemail.attach(emailattachment);

       		//发送邮件

       		multipartemail.send();

       		System.out.println("The attachmentEmail send sucessful!!!");
    	}
    	catch(Exception e)
    	{
    		System.out.println(e.getMessage());
    	}
    }
}