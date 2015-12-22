package log.server;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class MailSend {

	/**
	 * 发件方式枚举
	 */
	public enum SendTypeEnum {
		TO(0, "发件方式 - 普通发送"), CS(1, "发件方式 - 抄送"), MCS(2, "发件方式 - 密件抄送");
		private int type;
		private String description;

		SendTypeEnum(int type, String desc) {
			this.type = type;
			this.description = desc;
		}

		public int intValue() {
			return this.type;
		}

		public String getDescription() {
			return this.description;
		}
	}

	/** 邮件相关信息 - SMTP 服务器 */
	private String smtpHost = "smtp.gmail.com";
	/** 邮件相关信息 - 邮件用户名 */
	private String username = "huwei.nwu@gmail.com";
	/** 邮件相关信息 - 密码 */
	private String password = "nwuNFC123";
	/** 邮件相关信息 - 发件人邮件地址 */
	private String fromAddress = "huwei.nwu@gmail.com";
	/** 邮件相关信息 - 邮件主题 */
	private String subject = "日志文件";
	/** 邮件相关信息 - 邮件发送地址 */
	private Address[] toAddress = null;
	/** 邮件相关信息 - 邮件抄送地址 */
	private Address[] csAddress = null;
	/** 邮件相关信息 - 邮件密件抄送地址 */
	private Address[] mcsAddress = null;
	/** 邮件相关信息 - 邮件正文(复合结构) */
	private MimeMultipart context = null;

	/** 发送单个邮件-收件人地址 */
	private String toUserAddress = "nwucomputer2005@163.com";

	public MailSend() {
		context = new MimeMultipart();
	}

	/**
	 * 设置 SMTP 服务器
	 * 
	 * @param strSMTPHost
	 *            邮件服务器名称或 IP
	 * @param strUser
	 *            邮件用户名
	 * @param strPassword
	 *            密码
	 */
	public void setSMTP(String strSMTPHost, String strUser, String strPassword) {
		this.smtpHost = strSMTPHost;
		this.username = strUser;
		this.password = strPassword;
	}

	/**
	 * 设置邮件发送地址
	 * 
	 * @param strFromAddress
	 *            邮件发送地址
	 */
	public void setFromAddress(String strFromAddress) {
		this.fromAddress = strFromAddress;
	}

	/**
	 * 设置邮件目的地址
	 * 
	 * @param strAddress
	 *            邮件目的地址列表, 不同的地址可用;号分隔
	 * @param iAddressType
	 *            邮件发送方式 (SendTypeEnum.TO 0, SendTypeEnum.CS 1, SendTypeEnum.MCS
	 *            2) 枚举已在本类定义
	 * @throws AddressException
	 */
	public void setAddress(String strAddress, SendTypeEnum iAddressType)
			throws AddressException {
		if (iAddressType == SendTypeEnum.TO) {
			ArrayList alAddress = splitStr(strAddress, ';');
			toAddress = new Address[alAddress.size()];
			for (int i = 0; i < alAddress.size(); i++) {
				toAddress[i] = new InternetAddress((String) alAddress.get(i));
			}
		} else if (iAddressType == SendTypeEnum.CS) {
			ArrayList alAddress = splitStr(strAddress, ';');
			csAddress = new Address[alAddress.size()];
			for (int i = 0; i < alAddress.size(); i++) {
				csAddress[i] = new InternetAddress((String) alAddress.get(i));
			}
		} else if (iAddressType == SendTypeEnum.MCS) {
			ArrayList alAddress = splitStr(strAddress, ';');
			mcsAddress = new Address[alAddress.size()];
			for (int i = 0; i < alAddress.size(); i++) {
				mcsAddress[i] = new InternetAddress((String) alAddress.get(i));
			}
		}
	}

	/**
	 * 设置邮件主题
	 * 
	 * @param strSubject
	 *            邮件主题
	 */
	public void setSubject(String strSubject) {
		this.subject = strSubject;
	}

	/**
	 * 设置邮件文本正文
	 * 
	 * @param strTextBody
	 *            邮件文本正文
	 * @throws MessagingException
	 */
	public void setContext(String strTextBody) throws MessagingException {
		MimeBodyPart mimebodypart = new MimeBodyPart();
		mimebodypart.setText(strTextBody, "GBK");
		context.addBodyPart(mimebodypart);
	}

	/**
	 * 设置邮件超文本正文
	 * 
	 * @param strHtmlBody
	 *            邮件超文本正文
	 * @throws MessagingException
	 */
	public void setHtmlContext(String strHtmlBody) throws MessagingException {
		MimeBodyPart mimebodypart = new MimeBodyPart();
		mimebodypart.setDataHandler(new DataHandler(strHtmlBody,
				"text/html;charset=GBK"));
		context.addBodyPart(mimebodypart);
	}

	/**
	 * 设置邮件正文外部链接 URL, 信体中将包含链接所指向的内容
	 * 
	 * @param strURLAttachment
	 *            邮件正文外部链接 URL
	 * @throws MessagingException
	 * @throws MalformedURLException
	 */
	public void setURLAttachment(String strURLAttachment)
			throws MessagingException, MalformedURLException {
		MimeBodyPart mimebodypart = new MimeBodyPart();
		mimebodypart.setDataHandler(new DataHandler(new URL(strURLAttachment)));
		context.addBodyPart(mimebodypart);
	}

	/**
	 * 设置邮件附件
	 * 
	 * @param strFileAttachment
	 *            文件的全路径
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public void setFileAttachment(String strFileAttachment)
			throws MessagingException, UnsupportedEncodingException {
		File path = new File(strFileAttachment);
		if (!path.exists() || path.isDirectory()) {
			return;
		}
		String strFileName = path.getName();
		MimeBodyPart mimebodypart = new MimeBodyPart();
		mimebodypart.setDataHandler(new DataHandler(new FileDataSource(
				strFileAttachment)));
		// modified by zord @ 2003/6/16 to support Chinese File Name
		// mimebodypart.setFileName(strFileName);
		mimebodypart.setFileName(MimeUtility.encodeText(strFileName));
		// end of modify
		context.addBodyPart(mimebodypart);
	}

	/**
	 * 主方法：邮件发送 setSMTP(); setFromAddress(); setAddress(); setSubject();
	 * set*Context(); set*Attachment(); }
	 * 
	 * @throws MessagingException
	 */

	public void sendBatch() throws MessagingException {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", this.smtpHost);
		properties.put("mail.smtp.auth", "true");// 必须
		Session session = Session.getInstance(properties, new Authenticator() {
			protected javax.mail.PasswordAuthentication getPasswordAuthentication() {// 这个很重要，当今mail服务器大多都是通过认证才能发信的
				return new javax.mail.PasswordAuthentication(
						MailSend.this.username, MailSend.this.password);
			}
		});
		MimeMessage mimemessage = new MimeMessage(session);
		mimemessage.setFrom(new InternetAddress(this.fromAddress));
		if (toAddress != null) {
			mimemessage.addRecipients(javax.mail.Message.RecipientType.TO,
					this.toAddress);
		}
		if (csAddress != null) {
			mimemessage.addRecipients(javax.mail.Message.RecipientType.CC,
					this.csAddress);
		}
		if (mcsAddress != null) {
			mimemessage.addRecipients(javax.mail.Message.RecipientType.BCC,
					this.mcsAddress);
		}
		mimemessage.setSubject(this.subject);
		mimemessage.setContent(this.context);
		mimemessage.setSentDate(new Date());

		Transport.send(mimemessage);

		System.out.println("已向下列邮箱发送了邮件");
		showAddress(toAddress, csAddress, mcsAddress);
	}

	/**
	 * 显示所有的收件人地址信息
	 * 
	 * @param addresses
	 */
	private static void showAddress(Address[]... addresses) {
		if (addresses != null) {
			for (int i = 0; i < addresses.length; i++) {
				if (addresses[i] != null) {
					for (int j = 0; j < addresses[i].length; j++) {
						System.out.println(addresses[i][j]);
					}
				}
			}
		}
	}

	/**
	 * 将字符串分割成字符串集合
	 * 
	 * @param str
	 *            字符串
	 * @param sp
	 *            分隔符
	 * @return
	 */
	private static ArrayList<String> splitStr(String str, char sp) {
		ArrayList<String> retu = new ArrayList<String>();
		String[] res = str.split("" + sp);
		for (String s : res) {
			retu.add(s);
		}
		return retu;
	}

	/**
	 * 发送带附件的邮件
	 * 
	 * @param filename
	 *            附件的路径
	 */
	public static void sendmail(String filename) {
		try {
			MailSend mail = new MailSend();
			mail.setFromAddress(mail.fromAddress);
			mail.setSMTP(mail.smtpHost, mail.username, mail.password);

			mail.setAddress(mail.toUserAddress, SendTypeEnum.TO);

			mail.setSubject("邮件发送测试");// 标题

			try {
				mail.setFileAttachment(filename);// 本地附件
			} catch (Exception e) {
				System.out.println("附件发送异常");
			}
			mail.sendBatch();
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}