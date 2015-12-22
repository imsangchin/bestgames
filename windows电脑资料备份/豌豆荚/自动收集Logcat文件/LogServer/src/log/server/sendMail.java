package log.server;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

public class sendMail {
	//发信人地址
	private static final String FromAddress = "huwei.nwu@gmail.com"; 
	//邮箱密码
	private static final String PWD = "nwuNFC123";
	//收件人地址
	private static final String ToAddress = "nwucomputer2005@163.com";
	
	public static void send(String filepath) {

		EmailAttachment ea = new EmailAttachment();// 附件
		ea.setPath(filepath);// 本地附件文件

		MultiPartEmail email = new MultiPartEmail();
		email.setHostName("smtp.gmail.com");
		email.setAuthentication(FromAddress, PWD);
		email.setCharset("UTF-8");
		email.setTLS(true);

		try {
			email.setFrom(FromAddress);
			email.setSubject("log日志");
			email.setMsg("测试机器上的Log文件");
			email.addTo(ToAddress);
			email.attach(ea);
			//注意gmail采用465的端口
			email.setSslSmtpPort("465");
			email.send();
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}
}