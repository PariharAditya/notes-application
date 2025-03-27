package org.note.notificationservice.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Service
public class AllNotificationService {

    @Value("${sendgrid.from.email:manish.moryani@hashstudioz.com}")
    private String fromEmail;

    @Autowired
    private SendGrid sendGrid;

    @Value("${twilio.account.sid:AC32563a7ca2487f847152170f377da876}")
    private String twilioAccount;

    @Value("${twilio.auth.token:4a91f0d15e1c741605f56083b394db7e}")
    private String twilioAuthToken;

    @Value("${twilio.from.number:+12513877564}")
    private String twilioFromNumber;

    public void sendEmail(String toEmail, String subject, String body) throws IOException {
        Mail mail = createBasicMail(toEmail, subject, body);
        sendMail(mail);
    }

    public void sendWithDocs(String toEmail, String subject,
                             String body, byte[] attachment,
                             String fileName, String mime) throws IOException {
        // Create basic mail
        Mail mail = createBasicMail(toEmail, subject, body);

        // Add attachment
        Attachments attachments = new Attachments();
        attachments.setContent(Base64.getEncoder().encodeToString(attachment));
        attachments.setType(mime);
        attachments.setFilename(fileName);
        attachments.setDisposition("attachment");
        attachments.setContentId("Report");
        mail.addAttachments(attachments);

        // Send mail
        sendMail(mail);
    }

    private Mail createBasicMail(String toEmail, String subject, String body) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/Plain", body);
        return new Mail(from, subject, to, content);
    }

    private void sendMail(Mail mail) throws IOException {
        Request request = new Request();
        request.setMethod(Method.POST);

        request.setEndpoint("mail/send");

        try {
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            // Check response
            if (response.getStatusCode() >= 400) {
                System.err.println("SendGrid API Error: " + response.getStatusCode() + " - " + response.getBody());
                throw new RuntimeException("Failed to send email: " + response.getBody());
            }
        } catch (IOException e) {
            System.err.println("SendGrid API IOException: " + e.getMessage());
            throw e;
        }
    }

    public void sendTextMessage(String toNumber, String message) {
        Twilio.init(twilioAccount, twilioAuthToken);

        toNumber = toNumber.startsWith("+") ? toNumber : "+91" + toNumber;

        Message msg = Message.creator(
                new PhoneNumber(toNumber),
                new PhoneNumber(twilioFromNumber),
                message
        ).create();

        System.out.println("Message Sent: " + msg.getSid());

    }

}
