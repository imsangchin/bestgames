package com.wandoujia.tools.blr.task;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class MailTask extends SimpleTask {

    protected static void sendMail(String recipient, String subject, String content, boolean hasCC) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        final String account = "noreply@wandoujia.com";
        final String password = "8Raq3%DE";
        
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(account, password);
                    }
                });
        try {
            
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(account));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            if (hasCC) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse("huwei@wandoujia.com"));
            }
            message.setSubject(subject, "UTF-8");
            message.setContent(content, "text/html;charset=UTF-8");
            Transport.send(message);
            System.out.println("SendMail to " + recipient + " Done.");
 
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
