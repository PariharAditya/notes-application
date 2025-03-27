package org.note.notesapplication.Service;

import org.note.notesapplication.model.EmailRequest;
import org.note.notesapplication.model.SmsRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {
    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "notification-exchange";
    private static final String EMAIL_ROUTING_KEY = "notification.email";
    private static final String EMAIL_WITH_DOCS_ROUTING_KEY = "notification.email.docs";
    private static final String SMS_ROUTING_KEY = "notification.sms";

    // prevents the service from being instantiated without this dependency.
    @Autowired
    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEmail(String toEmail, String subject, String body) {
        EmailRequest request = new EmailRequest();
        request.setToEmail(toEmail);
        request.setSubject(subject);
        request.setBody(body);
        rabbitTemplate.convertAndSend(EXCHANGE, EMAIL_ROUTING_KEY, request);
    }

    public void sendWithDocs(String toEmail, String subject,
                           String body, byte[] attachment,
                           String fileName, String mime) {
        EmailRequest request = new EmailRequest();
        request.setToEmail(toEmail);
        request.setSubject(subject);
        request.setBody(body);
        request.setContent(attachment);
        request.setFileName(fileName);
        request.setMimeType(mime);
        rabbitTemplate.convertAndSend(EXCHANGE, EMAIL_WITH_DOCS_ROUTING_KEY, request);
    }

    public void sendTextMessage(String toNumber, String message) {
        SmsRequest request = new SmsRequest();
        request.setToNumber(toNumber);
        request.setMessage(message);
        rabbitTemplate.convertAndSend(EXCHANGE, SMS_ROUTING_KEY, request);
    }
}