package org.note.notesapplication.Service;

import lombok.extern.slf4j.Slf4j;
import org.note.notesapplication.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class AutomatedReportSending {
    /*
    our task is to create a scheduler that will send the report to the user every 5 min
    this is for testing purpose only
    1. create a scheduler that will run every 5 min
    2. get all the users
    3. get the report of each user
    4. send the report to the user(Via mail)
    */

    @Autowired
    private ReportService reportService;

    @Autowired
    private NotificationProducer sendNotification;

    @Autowired
    private MongoTemplate mongoTemplate;

//    @Scheduled(cron = "0 */5 * * * *")
    public void sendReportWithNotification() {
        List<User> users = mongoTemplate.findAll(User.class);

        for (User user : users) {

            try {
                byte[] reportContent = reportService.generateNotesReport(user.getUsername(), "pdf");

                String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
                String subject = "Your Notes Report - " + currentDate;
                String body = "Hello " + user + ",\n\n"
                        + "Please find attached your latest notes report generated on "
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        + ".\n\n" + "This is an automated message. Please do not reply to this email.\n\n";

                // Send email with attachment
                sendNotification.sendWithDocs(user.getUsername(), subject, body,  reportContent,"notes-report-" + user + currentDate + ".pdf", "application/pdf");
                System.out.println("Report sent to " + user.getUsername());

            } catch (Exception e) {
                System.out.println("Failed to send report to " + user.getUsername() + ": " + e.getMessage());
                log.info(e.getMessage());
            }
        }

    }
}