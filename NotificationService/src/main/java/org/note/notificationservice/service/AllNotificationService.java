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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Service
public class AllNotificationService {

    @Autowired
    private SendGrid sendGrid;

    public void sendEmail(String toEmail, String subject, String body) throws IOException {
        Mail mail = createBasicMail(toEmail, subject, body);
        sendMail(mail);
    }

    public void sendWithDocs(String toEmail, String subject,
                             String body, byte[] attachment,
                             String fileName, String mime) throws IOException {
        // Create basic mail
        Mail mail = createBasicMail(toEmail, subject, body);

        String correctFileName = fileName.replaceAll("[\\r\\n\\t\\f\\v]", "")
                .replaceAll("\\p{Cntrl}", "")
                .trim();

        // Add attachment
        Attachments attachments = new Attachments();
        attachments.setContent(Base64.getEncoder().encodeToString(attachment));
        attachments.setType(mime);
        attachments.setFilename(correctFileName);
        attachments.setDisposition("attachment");
        attachments.setContentId("Report");
        mail.addAttachments(attachments);

        System.out.println("to " + toEmail + "Attachment added: " + correctFileName);

        // Send mail
        sendMail(mail);
    }

    private Mail createBasicMail(String toEmail, String subject, String body) {
        String fromEmail = "manish.moryani@hashstudioz.com";
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
        String twilioAuthToken = "0163fb181d076b6988f4398c303d9a5a";
        String twilioAccount = "AC32563a7ca2487f847152170f377da876";
        Twilio.init(twilioAccount, twilioAuthToken);

        toNumber = toNumber.startsWith("+") ? toNumber : "+91" + toNumber;

        String twilioFromNumber = "+12513877564";
        Message msg = Message.creator(
                new PhoneNumber(toNumber),
                new PhoneNumber(twilioFromNumber),
                message
        ).create();

        System.out.println("Message Sent: " + msg.getSid() + " to " + toNumber);

    }

}
