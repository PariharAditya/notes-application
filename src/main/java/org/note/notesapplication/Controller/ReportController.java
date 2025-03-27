package org.note.notesapplication.Controller;

import lombok.extern.slf4j.Slf4j;
import org.note.notesapplication.Service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{username}/reports")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/get-notes/{format}")
    public ResponseEntity<byte[]> generateReport(
            @PathVariable String username,
            @PathVariable String format) {

        try {
            byte[] reportContent = reportService.generateNotesReport(username, format);

            HttpHeaders headers = new HttpHeaders();
            String filename = "notes_report_" + username + "_" +
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

            if (format.equalsIgnoreCase("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDisposition(ContentDisposition.inline().filename(filename).build()); // tell browser to download or view file in browser
            } else if (format.equalsIgnoreCase("html")) {
                headers.setContentType(MediaType.TEXT_HTML);
                headers.setContentDisposition(ContentDisposition.inline().filename(filename).build());
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(reportContent);

        } catch (Exception e) {
            log.info("Error generating report: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        }
    }
}