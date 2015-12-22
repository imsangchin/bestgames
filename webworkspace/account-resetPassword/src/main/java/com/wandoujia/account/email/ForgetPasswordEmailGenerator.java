package com.wandoujia.account.email;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForgetPasswordEmailGenerator {

    private static final Logger LOG = LoggerFactory
            .getLogger(ForgetPasswordEmailGenerator.class);

    private String forgetPasswordUrl = "https://account.wandoujia.com/web/checkcode?passcode=%s&username=%s";

    private String subject;

    private String templateFileName;

    public MailContent getMailContent(String to, String activeCode,
            String username) {
        MailContent mailContent = new MailContent();
        try {
            mailContent.setTo(to);
            mailContent.setFrom("noreply@wandoujia.com");
            mailContent.setSubject(subject);

            Map<String, Object> context = new HashMap<String, Object>();
            context.put(
                    "wandoujiaurl",
                    String.format(forgetPasswordUrl, activeCode,
                            URLEncoder.encode(username, "utf8")));

            context.put("wandoujiatime", DateFormatUtils.format(new Date(),
                    "yyyy年MM月dd日"));
            mailContent.setContent(new VelocityHelper().getContent(
                    templateFileName, context));
        } catch (UnsupportedEncodingException e) {
            LOG.warn(e.getMessage());
            return null;
        }
        return mailContent;
    }

    public void setForgetPasswordUrl(String forgetPasswordUrl) {
        this.forgetPasswordUrl = forgetPasswordUrl;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }

}
