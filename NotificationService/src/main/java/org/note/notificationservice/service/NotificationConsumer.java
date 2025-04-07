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
            System.out.println("Email sent to: " + emailRequest.getToEmail() + " with subject: " + emailRequest.getSubject());
        } catch (Exception e) {
            // Log error and potentially retry
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "Email-notification-with-docs")
    public void handleEmailNotificationWithDocs(EmailRequest emailRequest) {
        try {
            // Validate filename before processing
            if (emailRequest.getFileName() != null) {
                String sanitizedFileName = emailRequest.getFileName().replaceAll("[\\r\\n\\t\\f\\v]", "")
                        .replaceAll("[\\p{Cntrl}]", "")
                        .trim();
                emailRequest.setFileName(sanitizedFileName);
            }

            assert emailRequest.getFileName() != null;
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