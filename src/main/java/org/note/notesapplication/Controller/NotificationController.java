package org.note.notesapplication.Controller;

import lombok.extern.slf4j.Slf4j;
import org.note.notesapplication.Service.AutomatedReportSending;
import org.note.notesapplication.Service.NotificationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationProducer notification;

    @Autowired
    private AutomatedReportSending scheduledReportService;

    @PostMapping("/send-email")
    public ResponseEntity<String> sendMail(@RequestParam String toEmail, @RequestBody String body,
                                           @RequestParam String subject) {

        try {
            notification.sendEmail(toEmail, subject, body);
            log.info("Email sent via notesApplication service Producer {} ", toEmail);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }

    }


    @PostMapping("/send-sms")
    public ResponseEntity<String> sendText(@RequestParam String toNumber, @RequestParam String message) {
        try {
            notification.sendTextMessage(toNumber, message);
            return ResponseEntity.ok("SMS sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send SMS: " + e.getMessage());
        }
    }

    @PostMapping("/generate-and-send")
    public ResponseEntity<String> manuallyTriggerReportGeneration() {
        try {
            scheduledReportService.sendReportWithNotification();
            return ResponseEntity.ok("Reports generated and sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error generating and sending reports: " + e.getMessage());
        }
    }

}