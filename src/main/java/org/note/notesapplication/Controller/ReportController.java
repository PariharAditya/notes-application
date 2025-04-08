package org.note.notesapplication.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Report", description = "Report Management endpoints")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Operation(
            summary = "Generate notes report",
            description = "Generates a report of notes for a specific user in the specified format (PDF or HTML).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Report generated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or user not found"),
                    @ApiResponse(responseCode = "500", description = "Error generating report")
            }
    )
    @Parameters({
            @Parameter(
                    name = "username",
                    description = "Username of the user for whom the report is generated",
                    required = true
            ),
            @Parameter(
                    name = "format",
                    description = "Format of the report (PDF or HTML)",
                    required = true
            )
    })
    @GetMapping("/notes/{format}")
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