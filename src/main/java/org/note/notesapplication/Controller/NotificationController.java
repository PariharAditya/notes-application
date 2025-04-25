package org.note.notesapplication.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.note.notesapplication.Service.AutomatedReportSending;
import org.note.notesapplication.Service.NotificationProducer;
import org.note.notesapplication.Service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notification")
@Tag(name = "Notification", description = "Notification Management endpoints email or phone number")
public class NotificationController {

        @Autowired
        private NotificationProducer notification;

        @Autowired
        private AutomatedReportSending scheduledReportService;

        @Autowired
        private ReportService reportService;

        @PostMapping("/send-email")
        @Operation(summary = "Send email notification", description = "Sends an email notification to the specified email address with custom subject and body", responses = {
                        @ApiResponse(responseCode = "200", description = "Email sent successfully"),
                        @ApiResponse(responseCode = "400", description = "Failed to send email"),
                        @ApiResponse(responseCode = "500", description = "Internal Server Error")
        })
        @Parameters({
                        @Parameter(name = "toEmail", description = "Email address of the recipient", required = true),
                        @Parameter(name = "body", description = "Body of the email", required = true),
                        @Parameter(name = "subject", description = "Subject of the email", required = true)
        })
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
        @Operation(summary = "Send SMS notification", description = "Sends an SMS notification to the specified phone number", responses = {
                        @ApiResponse(responseCode = "200", description = "SMS sent successfully"),
                        @ApiResponse(responseCode = "400", description = "Failed to send SMS"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @Parameters({
                        @Parameter(name = "toNumber", description = "Phone number of the recipient", required = true),
                        @Parameter(name = "message", description = "Message to be sent in the SMS", required = true)
        })
        public ResponseEntity<String> sendText(@RequestParam String toNumber, @RequestParam String message) {
                try {
                        notification.sendTextMessage(toNumber, message);
                        return ResponseEntity.ok("SMS sent successfully");
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body("Failed to send SMS: " + e.getMessage());
                }
        }

        @Operation(summary = "Manually trigger report generation and sending", description = "CRON Job " +
                        "This endpoint allows you to manually trigger the report generation and sending process.", responses = {
                                        @ApiResponse(responseCode = "200", description = "Reports generated and sent successfully"),
                                        @ApiResponse(responseCode = "500", description = "Error generating and sending reports")
                        })
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

        @PostMapping("/share/{title}")
        @Operation(summary = "Share a note", description = "Shares a specific note with another user",

                        responses = {
                                        @ApiResponse(responseCode = "200", description = "Note shared successfully"),
                                        @ApiResponse(responseCode = "404", description = "Note not found or user not found")

                        })
        public ResponseEntity<String> shareNote(
                        @PathVariable String title,
                        @RequestParam String toEmail) {
                try {
                        reportService.shareYourNotes(toEmail,  title);
                        return ResponseEntity.ok("Note shared successfully");
                } catch (Exception e) {
                        return ResponseEntity.notFound().build();
                }
        }

}