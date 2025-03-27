package org.note.notesapplication.Service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.TextAdjustEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import org.note.notesapplication.model.userResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private notesService notesService;

    @Cacheable(value = "notesReport", key = "#username + '-' + #reportFormat")
    public byte[] generateNotesReport(String username, String reportFormat) throws JRException {
        // Get notes for the user
        List<userResponse> notes = notesService.getAllNotesByUser(username);

        /*
        we need dataSource, params(DB columns) and data-field to write these things

        When you generate a report with JasperReports, the following steps occur:
        Data Collection: You gather the data (e.g., from a database).
        Report Design: Either load a pre-made .jrxml template or create it dynamically in code.
        Filling the Report: Combine the data and design to produce a JasperPrint object.
        Exporting the Report: Convert the JasperPrint into a specific format (PDF, HTML, etc.).
        This is where byte[] comes in – the report is exported as a byte[].
        */

        try {
            // Create a dynamically generated report design
            JasperDesign jasperDesign = createReportDesign();

            // Compile the report design
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            System.out.println("Successfully compiled report template");

            // Create data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(notes);

            // Add parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("username", username);
            parameters.put("createdBy", "Notes Application");

            // Fill report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export based on format
            switch (reportFormat.toLowerCase()) {
                case "pdf":
                    return JasperExportManager.exportReportToPdf(jasperPrint);
                case "html":
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    HtmlExporter exporter = new HtmlExporter();
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
                    exporter.exportReport();
                    return outputStream.toByteArray();
            }
            throw new IllegalArgumentException("Format not supported: " + reportFormat);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
        ((JRDesignSection)jasperDesign.getDetailSection()).addBand(detailBand);

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