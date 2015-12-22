package log.server;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

public class sendMail
{
	public static void send(String filepath)
	{
		
		EmailAttachment ea = new EmailAttachment();//附件
        ea.setPath(filepath);//本地附件文件

        MultiPartEmail email = new MultiPartEmail();
        email.setHostName("smtp.gmail.com");
       // email.setSslSmtpPort("465");
        email.setAuthentication("huwei.nwu@gmail.com", "nwuNFC123");
        email.setCharset("UTF-8");
        email.setTLS(true);

        try {
            email.setFrom("huwei.nwu@gmail.com");
            email.setSubject("commons email");
            email.setMsg("这是利用commons包发出的电子邮件");
            email.addTo("huwei.nwu@gmail.com");
            email.attach(ea);
            email.setSslSmtpPort("465");
            email.send();
        } catch (EmailException e) {
            e.printStackTrace();
        }
	}
}