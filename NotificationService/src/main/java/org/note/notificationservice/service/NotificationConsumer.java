package org.note.notificationservice.service;

import org.note.notificationservice.model.EmailRequest;
import org.note.notificationservice.model.SmsRequest;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    @Autowired
    private AllNotificationService notificationType;

    @RabbitListener(queues = "Email-notification")
    public void handleEmailNotification(EmailRequest emailRequest) {
        try {
            notificationType.sendEmail(emailRequest.getToEmail(), emailRequest.getSubject(), emailRequest.getBody());
        } catch (Exception e) {
            // Log error and potentially retry
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "Email-notification-with-docs")
    public void handleEmailNotificationWithDocs(EmailRequest emailRequest) {
        try {
            notificationType.sendWithDocs (
                emailRequest.getToEmail(),
                emailRequest.getSubject(),
                emailRequest.getBody(),
                emailRequest.getContent(),
                emailRequest.getFileName(),
                emailRequest.getMimeType()
            );
        } catch (Exception e) {
            // Log error and potentially retry
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "SMS-notification")
    public void handleSmsNotification(SmsRequest smsRequest) {
        try {
            notificationType.sendTextMessage(smsRequest.getToNumber(), smsRequest.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }
}