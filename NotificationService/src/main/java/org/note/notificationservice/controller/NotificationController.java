package org.note.notificationservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.note.notificationservice.model.EmailRequest;
import org.note.notificationservice.model.SmsRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "notification-exchange";
    private static final String EMAIL_ROUTING_KEY = "notification.email";
    private static final String SMS_ROUTING_KEY = "notification.sms";

    @PostMapping("/send-email")
    public ResponseEntity<String> sendMail(@RequestParam String toEmail,
                                          @RequestParam String subject,
                                          @RequestBody(required = false) String body) {
        log.info("Email via notification-service ");
        try {
            EmailRequest request = new EmailRequest();
            request.setToEmail(toEmail);
            request.setSubject(subject);
            request.setBody(body != null ? body : "");

            rabbitTemplate.convertAndSend(EXCHANGE, EMAIL_ROUTING_KEY, request);
            log.info("Email notification sent " +
                    "{}" +
                    "{}", toEmail, body);

            return ResponseEntity.ok("Email sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to queue email: " + e.getMessage());
        }
    }

    @PostMapping("/send-sms")
    public ResponseEntity<String> sendText(@RequestParam String toNumber, @RequestParam String message) {
        try {
            SmsRequest request = new SmsRequest();
            request.setToNumber(toNumber);
            request.setMessage(message);

            rabbitTemplate.convertAndSend(EXCHANGE, SMS_ROUTING_KEY, request);
            return ResponseEntity.ok("SMS sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to queue SMS: " + e.getMessage());
        }
    }
}