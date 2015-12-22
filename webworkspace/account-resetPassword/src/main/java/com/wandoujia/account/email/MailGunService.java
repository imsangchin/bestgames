package com.wandoujia.account.email;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class MailGunService implements IMailService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MailGunService.class);

    private final ExecutorService pool = Executors.newCachedThreadPool();

    @Override
    public void sendMail(final MailContent mailContent) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                MultivaluedMapImpl formData = new MultivaluedMapImpl();
                formData.add("from", "豌豆荚 " + mailContent.getFrom());
                formData.add("subject", mailContent.getSubject());
                formData.add("to", mailContent.getTo());
                formData.add("html", mailContent.getContent());

                Client client = Client.create();
                client.addFilter(new HTTPBasicAuthFilter("api",
                        "key-5br6rwrim18qcnavw7vfxrud2d9sg5r2"));
                WebResource webResource = client
                        .resource("https://api.mailgun.net/v2/mail.wandoujia.com/messages");

                try {
                    ClientResponse response = webResource.type(
                            MediaType.APPLICATION_FORM_URLENCODED).post(
                            ClientResponse.class, formData);
                    if(response.getStatus() == HttpStatus.SC_OK){
                        LOG.info("send email to {} success", mailContent.getTo());
                    } else {
                        String errorReason = String.valueOf(response.getStatus()) + "  " + IOUtils.toString(response.getEntityInputStream());
                        LOG.warn("send email to {} failure  reason {}", mailContent.getTo(), errorReason);
                    }
                } catch (ClientHandlerException e) {
                    LOG.warn("send email to {} failure ,reason {}", mailContent.getTo(),e.getMessage());
                } catch (Exception e) {
                    LOG.warn("send email to {} failure,reason{} ", mailContent.getTo(),e.getMessage());
                }
            }
        });
    }

}
