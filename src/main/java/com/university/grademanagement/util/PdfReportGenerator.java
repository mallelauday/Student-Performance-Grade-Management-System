package com.university.grademanagement.util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.university.grademanagement.config.AppConfig;
import com.university.grademanagement.model.GradeResult;
import com.university.grademanagement.model.StudentRank;
import com.university.grademanagement.model.SubjectResult;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Enterprise PDF Report and Marksheet Generator using OpenPDF.
 */
public class PdfReportGenerator {

    private static final Color PRIMARY_NAVY = new Color(30, 41, 59);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);
    private static final Color HEADER_BG = new Color(241, 245, 249);
    private static final Color PASS_GREEN = new Color(22, 163, 74);
    private static final Color FAIL_RED = new Color(220, 38, 38);

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_NAVY);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
    private static final Font FONT_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, ACCENT_BLUE);
    private static final Font FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
    private static final Font FONT_BODY_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

    private PdfReportGenerator() {
    }

    /**
     * Generates a formal, high-resolution student marksheet PDF.
     */
    public static void generateMarksheetPdf(File outputFile, GradeResult result) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));

        // Add page footer event
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                String footerText = "Generated on " + new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date()) +
                        " | Page " + writer.getPageNumber() + " | Official University Grade Document";
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(footerText, FONT_FOOTER),
                        (document.right() - document.left()) / 2 + document.leftMargin(), document.bottom() - 15, 0);
            }
        });

        document.open();

        // 1. University Header
        Paragraph univTitle = new Paragraph(AppConfig.UNIVERSITY_NAME, FONT_TITLE);
        univTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(univTitle);

        Paragraph univSubtitle = new Paragraph(AppConfig.ACCREDITATION, FONT_SUBTITLE);
        univSubtitle.setAlignment(Element.ALIGN_CENTER);
        univSubtitle.setSpacingAfter(8);
        document.add(univSubtitle);

        Paragraph reportTitle = new Paragraph("OFFICIAL SEMESTER GRADE REPORT", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, ACCENT_BLUE));
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        reportTitle.setSpacingAfter(15);
        document.add(reportTitle);

        // 2. Student Info Card Table
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{20f, 30f, 20f, 30f});
        infoTable.setSpacingAfter(15);

        addInfoCell(infoTable, "Student Name:", result.getStudentName());
        addInfoCell(infoTable, "Roll Number:", result.getRollNumber());
        addInfoCell(infoTable, "Department:", result.getDepartment());
        addInfoCell(infoTable, "Semester:", "Semester " + result.getSemester());
        addInfoCell(infoTable, "Academic Year:", result.getAcademicYearCode() != null ? result.getAcademicYearCode() : "Current");
        addInfoCell(infoTable, "Evaluation Date:", new SimpleDateFormat("dd MMMM yyyy").format(new Date()));

        document.add(infoTable);

        // 3. Subject Evaluation Table
        document.add(new Paragraph("ACADEMIC PERFORMANCE SUMMARY", FONT_SECTION));
        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(4);
        document.add(spacer);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{12f, 26f, 8f, 9f, 9f, 9f, 9f, 8f, 10f});
        table.setSpacingAfter(15);

        // Header cells
        String[] headers = {"Code", "Course Title", "Credits", "Internal", "External", "Total", "Max", "Grade", "Status"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_TABLE_HEADER));
            cell.setBackgroundColor(PRIMARY_NAVY);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        boolean stripe = false;
        for (SubjectResult sr : result.getSubjectResults()) {
            Color rowBg = stripe ? HEADER_BG : Color.WHITE;
            stripe = !stripe;

            addBodyCell(table, sr.getSubjectCode(), Element.ALIGN_CENTER, rowBg);
            addBodyCell(table, sr.getSubjectName(), Element.ALIGN_LEFT, rowBg);
            addBodyCell(table, String.valueOf(sr.getCredits()), Element.ALIGN_CENTER, rowBg);
            addBodyCell(table, String.format("%.1f", sr.getInternalMarks()), Element.ALIGN_RIGHT, rowBg);
            addBodyCell(table, String.format("%.1f", sr.getExternalMarks()), Element.ALIGN_RIGHT, rowBg);
            addBodyCell(table, String.format("%.1f", sr.getTotalMarks()), Element.ALIGN_RIGHT, rowBg);
            addBodyCell(table, String.format("%.0f", sr.getMaxMarks()), Element.ALIGN_RIGHT, rowBg);
            addBodyCell(table, sr.getGrade(), Element.ALIGN_CENTER, rowBg);

            Font statusFont = sr.isPassed() ?
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PASS_GREEN) :
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, FAIL_RED);
            PdfPCell statusCell = new PdfPCell(new Phrase(sr.isPassed() ? "PASS" : "FAIL", statusFont));
            statusCell.setBackgroundColor(rowBg);
            statusCell.setPadding(5);
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(statusCell);
        }

        document.add(table);

        // 4. Overall Semester Summary Card
        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{25f, 25f, 25f, 25f});
        summaryTable.setSpacingAfter(25);

        addSummaryCardCell(summaryTable, "Total Marks", String.format("%.1f / %.0f", result.getTotalObtainedMarks(), result.getTotalMaxMarks()));
        addSummaryCardCell(summaryTable, "Overall Percentage", String.format("%.2f%%", result.getPercentage()));
        addSummaryCardCell(summaryTable, "Semester SGPA", String.format("%.2f / 10.0", result.getSgpa()));
        addSummaryCardCell(summaryTable, "Final Result", result.getStatus());

        document.add(summaryTable);

        // 5. Grading Policy Legend
        Paragraph legendTitle = new Paragraph("GRADING SYSTEM & POINT SCALE", FONT_SUBTITLE);
        legendTitle.setSpacingAfter(4);
        document.add(legendTitle);

        PdfPTable legendTable = new PdfPTable(7);
        legendTable.setWidthPercentage(100);
        legendTable.setSpacingAfter(30);

        String[] grades = {"A+ (90-100%): 10", "A (80-89%): 9", "B (70-79%): 8", "C (60-69%): 7", "D (50-59%): 6", "E (40-49%): 5", "F (<40%): 0"};
        for (String g : grades) {
            PdfPCell c = new PdfPCell(new Phrase(g, FONT_FOOTER));
            c.setBackgroundColor(HEADER_BG);
            c.setPadding(4);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            legendTable.addCell(c);
        }
        document.add(legendTable);

        // 6. Signatures
        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);

        PdfPCell sign1 = new PdfPCell(new Phrase("\n\n___________________________\nDean / Head of Department", FONT_BODY_BOLD));
        sign1.setBorder(Rectangle.NO_BORDER);
        sign1.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell sign2 = new PdfPCell(new Phrase("\n\n___________________________\nController of Examinations", FONT_BODY_BOLD));
        sign2.setBorder(Rectangle.NO_BORDER);
        sign2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        signTable.addCell(sign1);
        signTable.addCell(sign2);
        document.add(signTable);

        document.close();
    }

    /**
     * Generates a formal Class Ranking & Merit List PDF.
     */
    public static void generateClassRankingsPdf(File outputFile, List<StudentRank> rankings, String dept, int sem, String year) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));

        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                String footerText = "Generated on " + new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date()) +
                        " | Page " + writer.getPageNumber() + " | University Academic Merit List";
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(footerText, FONT_FOOTER),
                        (document.right() - document.left()) / 2 + document.leftMargin(), document.bottom() - 15, 0);
            }
        });

        document.open();

        Paragraph univTitle = new Paragraph(AppConfig.UNIVERSITY_NAME, FONT_TITLE);
        univTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(univTitle);

        Paragraph reportTitle = new Paragraph("OFFICIAL ACADEMIC MERIT & RANKING LIST", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, ACCENT_BLUE));
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(reportTitle);

        Paragraph meta = new Paragraph(String.format("Department: %s | Semester: %d | Academic Period: %s", dept, sem, year), FONT_SUBTITLE);
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.setSpacingAfter(15);
        document.add(meta);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10f, 18f, 28f, 14f, 15f, 12f, 15f});

        String[] headers = {"Rank", "Roll No", "Student Name", "Total Marks", "Percentage", "SGPA", "Status"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_TABLE_HEADER));
            cell.setBackgroundColor(PRIMARY_NAVY);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        boolean stripe = false;
        for (StudentRank sr : rankings) {
            Color rowBg = stripe ? HEADER_BG : Color.WHITE;
            stripe = !stripe;

            String rankText = sr.getRank() > 0 ? String.valueOf(sr.getRank()) : "-";
            addBodyCell(table, rankText, Element.ALIGN_CENTER, rowBg);
            addBodyCell(table, sr.getRollNumber(), Element.ALIGN_CENTER, rowBg);
            addBodyCell(table, sr.getFullName(), Element.ALIGN_LEFT, rowBg);
            addBodyCell(table, String.format("%.1f", sr.getTotalMarks()), Element.ALIGN_RIGHT, rowBg);
            addBodyCell(table, String.format("%.2f%%", sr.getPercentage()), Element.ALIGN_RIGHT, rowBg);
            addBodyCell(table, String.format("%.2f", sr.getSgpa()), Element.ALIGN_CENTER, rowBg);

            boolean isPass = sr.getStatus() != null && sr.getStatus().startsWith("PASS");
            Font statusFont = isPass ?
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, PASS_GREEN) :
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, FAIL_RED);
            PdfPCell statusCell = new PdfPCell(new Phrase(sr.getStatus(), statusFont));
            statusCell.setBackgroundColor(rowBg);
            statusCell.setPadding(5);
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(statusCell);
        }

        document.add(table);
        document.close();
    }

    private static void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, FONT_BODY_BOLD));
        cLabel.setBackgroundColor(HEADER_BG);
        cLabel.setPadding(4);
        cLabel.setBorderColor(new Color(226, 232, 240));

        PdfPCell cValue = new PdfPCell(new Phrase(value != null ? value : "-", FONT_BODY));
        cValue.setPadding(4);
        cValue.setBorderColor(new Color(226, 232, 240));

        table.addCell(cLabel);
        table.addCell(cValue);
    }

    private static void addBodyCell(PdfPTable table, String text, int align, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", FONT_BODY));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }

    private static void addSummaryCardCell(PdfPTable table, String title, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(8);
        cell.setBorderColor(new Color(203, 213, 225));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pTitle = new Paragraph(title, FONT_SUBTITLE);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        Paragraph pVal = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_NAVY));
        pVal.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(pTitle);
        cell.addElement(pVal);
        table.addCell(cell);
    }
}
