package org.note.notesapplication.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.TextAdjustEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import org.note.notesapplication.DTO.userResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportService {

    @Autowired
    private NotesService notesService;

    @Autowired
    private NotificationProducer sendNotification;

    @Autowired
    private Environment env;

    private boolean isDockerEnvironment = false;

    @PostConstruct
    public void init() {
        // Check if running in Docker environment
        isDockerEnvironment = Arrays.asList(env.getActiveProfiles()).contains("docker");
        if (isDockerEnvironment) {
            // Configure JasperReports for Docker
            System.setProperty("net.sf.jasperreports.compiler.temp.dir", "/tmp");
            System.setProperty("net.sf.jasperreports.compiler.xml.validation", "false");
            log.info("Configured JasperReports for Docker environment");

            // Log all system properties for debugging
            System.getProperties().forEach((k, v) -> {
                if (k.toString().contains("jasper") || k.toString().contains("report")) {
                    log.info("Property: {} = {}", k, v);
                }
            });
        }
    }

    @Cacheable(value = "notesReport", key = "#username + '-' + #reportFormat")
    public byte[] generateNotesReport(String username, String reportFormat) throws JRException {
        try {
            log.info("Starting report generation for user {} in format {}", username, reportFormat);
            long startTime = System.currentTimeMillis();

            // Get notes for the user
            List<userResponse> notes = notesService.getAllNotesByUser(username);
            log.info("Retrieved {} notes for user {}", notes.size(), username);

            JasperPrint jasperPrint;

            // Create data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(notes);

            // Add parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("username", username);
            parameters.put("createdBy", "Notes Application");

            // Create a dynamically generated report design
            JasperDesign jasperDesign = createReportDesign();

            // Compile the report design - with better error handling
            try {
                // Compile the report design
                log.info("Compiling report design...");
                JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
                log.info("Successfully compiled report template");

                // Fill report
                jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            } catch (JRException e) {
                log.error("Error compiling or filling report: {}", e.getMessage(), e);

                // If in Docker and failing, use a simpler approach
                if (isDockerEnvironment) {
                    log.info("Using simplified report generation for Docker environment");
                    // Use a simpler approach that might work better in Docker
                    jasperPrint = createSimplifiedReport(notes, username);
                } else {
                    throw e; // Re-throw if not in Docker
                }
            }

            // Export based on format
            byte[] reportBytes;
            switch (reportFormat.toLowerCase()) {
                case "pdf":
                    reportBytes = JasperExportManager.exportReportToPdf(jasperPrint);
                    break;
                case "html":
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    HtmlExporter exporter = new HtmlExporter();
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
                    exporter.exportReport();
                    reportBytes = outputStream.toByteArray();
                    break;
                default:
                    throw new IllegalArgumentException("Format not supported: " + reportFormat);
            }
            log.info("Report generation time for {}: {} ms", reportFormat, System.currentTimeMillis() - startTime);
            return reportBytes;
        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void shareYourNotes(String toEmail, String fromUsername, String title) {
        try {
            // Get the note content
            userResponse note = notesService.getNoteByTitleAndUser(fromUsername, title);
            if (note == null) {
                throw new RuntimeException("Note not found: " + title);
            }

            // Format the email body to include who is sharing it
            String emailBody = String.format(
                    """
                            Note Title: %s
                            
                            Shared by: %s
                            
                            Content:
                            %s
                            
                            This note was shared from Notes Application.""",
                    title, fromUsername, note.getContent());

            // Send the email using the fixed sender address configured in SendGrid
            sendNotification.sendEmail(toEmail, "Note Shared: " + title, emailBody);
            System.out.println(note.getContent());
            // Log the successful share
            System.out.println("Note '" + title + "' shared by " + fromUsername + " to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to share note: " + e.getMessage());
            throw new RuntimeException("Failed to share note: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a simplified JasperPrint for Docker environment where complex
     * compilation may fail
     */
    private JasperPrint createSimplifiedReport(List<userResponse> notes, String username) throws JRException {
        // Create a very simple JasperDesign that should compile even in restricted
        // environments
        JasperDesign design = new JasperDesign();
        design.setName("Simple_Notes_Report");
        design.setPageWidth(595);
        design.setPageHeight(842);
        design.setColumnWidth(555);

        // Define fields
        JRDesignField titleField = new JRDesignField();
        titleField.setName("title");
        titleField.setValueClass(String.class);
        design.addField(titleField);

        JRDesignField contentField = new JRDesignField();
        contentField.setName("content");
        contentField.setValueClass(String.class);
        design.addField(contentField);

        // Create title band
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(30);

        // Add a simple title text
        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setText("Notes Report for " + username);
        titleText.setX(0);
        titleText.setY(0);
        titleText.setWidth(555);
        titleText.setHeight(30);
        titleBand.addElement(titleText);

        design.setTitle(titleBand);

        // Create detail band with just the note title
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(20);

        JRDesignTextField titleField1 = new JRDesignTextField();
        titleField1.setX(0);
        titleField1.setY(0);
        titleField1.setWidth(555);
        titleField1.setHeight(20);

        JRDesignExpression titleExpression = new JRDesignExpression();
        titleExpression.setText("$F{title}");
        titleField1.setExpression(titleExpression);
        detailBand.addElement(titleField1);

        ((JRDesignSection) design.getDetailSection()).addBand(detailBand);

        // Compile this simpler design
        JasperReport report = JasperCompileManager.compileReport(design);

        // Create data source and fill
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(notes);
        return JasperFillManager.fillReport(report, new HashMap<>(), dataSource);
    }

    private JasperDesign createReportDesign() throws JRException {
        // Define the cream color
        java.awt.Color headerColor = new java.awt.Color(242, 238, 225); // Slightly darker for headers

        // Create a new JasperDesign object
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("Simple_Notes_Report");
        jasperDesign.setPageWidth(595);
        jasperDesign.setPageHeight(842);
        jasperDesign.setColumnWidth(555);
        jasperDesign.setLeftMargin(20);
        jasperDesign.setRightMargin(20);
        jasperDesign.setTopMargin(20);
        jasperDesign.setBottomMargin(20);

        // Define fields
        JRDesignField titleField = new JRDesignField();
        titleField.setName("title");
        titleField.setValueClass(String.class);
        jasperDesign.addField(titleField);

        JRDesignField contentField = new JRDesignField();
        contentField.setName("content");
        contentField.setValueClass(String.class);
        jasperDesign.addField(contentField);

        JRDesignField createdDateField = new JRDesignField();
        createdDateField.setName("createdDate");
        createdDateField.setValueClass(java.time.LocalDateTime.class);
        jasperDesign.addField(createdDateField);

        // Define parameters
        JRDesignParameter usernameParam = new JRDesignParameter();
        usernameParam.setName("username");
        usernameParam.setValueClass(String.class);
        jasperDesign.addParameter(usernameParam);

        // Create title band
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(50);

        // Create rectangle for background color in title band
        JRDesignRectangle titleBackground = new JRDesignRectangle();
        titleBackground.setX(0);
        titleBackground.setY(0);
        titleBackground.setWidth(555);
        titleBackground.setHeight(50);
        titleBackground.setBackcolor(headerColor);
        // Use the proper enum from JasperReports
        titleBackground.setMode(net.sf.jasperreports.engine.type.ModeEnum.OPAQUE);
        titleBand.addElement(titleBackground);

        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setText("Notes Report");
        titleText.setX(0);
        titleText.setY(0);
        titleText.setWidth(555);
        titleText.setHeight(30);
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleText.setFontSize(16f);
        titleText.setBold(true);
        titleBand.addElement(titleText);

        // Add title band to design
        jasperDesign.setTitle(titleBand);

        // Create column header band
        JRDesignBand columnHeaderBand = new JRDesignBand();
        columnHeaderBand.setHeight(20);

        // Header background
        JRDesignRectangle headerBackground = new JRDesignRectangle();
        headerBackground.setX(0);
        headerBackground.setY(0);
        headerBackground.setWidth(555);
        headerBackground.setHeight(20);
        headerBackground.setBackcolor(headerColor);
        headerBackground.setMode(net.sf.jasperreports.engine.type.ModeEnum.OPAQUE);
        columnHeaderBand.addElement(headerBackground);

        JRDesignStaticText titleHeader = new JRDesignStaticText();
        titleHeader.setText("Title");
        titleHeader.setX(0);
        titleHeader.setY(0);
        titleHeader.setWidth(185);
        titleHeader.setHeight(20);
        titleHeader.setBold(true);
        titleHeader.setMode(net.sf.jasperreports.engine.type.ModeEnum.TRANSPARENT);
        columnHeaderBand.addElement(titleHeader);

        JRDesignStaticText dateHeader = new JRDesignStaticText();
        dateHeader.setText("Date");
        dateHeader.setX(185);
        dateHeader.setY(0);
        dateHeader.setWidth(185);
        dateHeader.setHeight(20);
        dateHeader.setBold(true);
        dateHeader.setMode(net.sf.jasperreports.engine.type.ModeEnum.TRANSPARENT);
        columnHeaderBand.addElement(dateHeader);

        JRDesignStaticText contentHeader = new JRDesignStaticText();
        contentHeader.setText("Content");
        contentHeader.setX(370);
        contentHeader.setY(0);
        contentHeader.setWidth(185);
        contentHeader.setHeight(20);
        contentHeader.setBold(true);
        contentHeader.setMode(net.sf.jasperreports.engine.type.ModeEnum.TRANSPARENT);
        columnHeaderBand.addElement(contentHeader);

        // Add column header band to design
        jasperDesign.setColumnHeader(columnHeaderBand);

        // Create detail band
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(30);

        JRDesignTextField titleField1 = new JRDesignTextField();
        titleField1.setX(0);
        titleField1.setY(0);
        titleField1.setWidth(185);
        titleField1.setHeight(30);
        titleField1.setBlankWhenNull(true);
        // Use textAdjust instead of stretchWithOverflow in newer versions
        titleField1.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);

        JRDesignExpression titleExpression = new JRDesignExpression();
        titleExpression.setText("$F{title}");
        titleField1.setExpression(titleExpression);
        detailBand.addElement(titleField1);

        JRDesignTextField dateField = new JRDesignTextField();
        dateField.setX(185);
        dateField.setY(0);
        dateField.setWidth(185);
        dateField.setHeight(30);
        dateField.setBlankWhenNull(true);

        JRDesignExpression dateExpression = new JRDesignExpression();
        dateExpression.setText("$F{createdDate} != null ? $F{createdDate}.toString() : \"\"");
        dateField.setExpression(dateExpression);
        detailBand.addElement(dateField);

        JRDesignTextField contentField1 = new JRDesignTextField();
        contentField1.setX(370);
        contentField1.setY(0);
        contentField1.setWidth(185);
        contentField1.setHeight(30);
        contentField1.setBlankWhenNull(true);
        // Use textAdjust instead of stretchWithOverflow in newer versions
        contentField1.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);

        JRDesignExpression contentExpression = new JRDesignExpression();
        contentExpression.setText("$F{content}");
        contentField1.setExpression(contentExpression);
        detailBand.addElement(contentField1);

        // Add detail band to design
        ((JRDesignSection) jasperDesign.getDetailSection()).addBand(detailBand);

        // Create page footer band
        JRDesignBand pageFooterBand = new JRDesignBand();
        pageFooterBand.setHeight(20);

        JRDesignTextField pageNumberField = new JRDesignTextField();
        pageNumberField.setX(0);
        pageNumberField.setY(0);
        pageNumberField.setWidth(555);
        pageNumberField.setHeight(20);
        pageNumberField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);

        JRDesignExpression pageNumberExpression = new JRDesignExpression();
        pageNumberExpression.setText("\"Page \" + $V{PAGE_NUMBER}");
        pageNumberField.setExpression(pageNumberExpression);
        pageFooterBand.addElement(pageNumberField);

        // Add page footer band to design
        jasperDesign.setPageFooter(pageFooterBand);

        return jasperDesign;
    }
}