package com.university.grademanagement.util;

import com.university.grademanagement.model.GradeResult;
import com.university.grademanagement.model.Marks;
import com.university.grademanagement.model.StudentRank;
import com.university.grademanagement.model.SubjectResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust RFC-4180 compliant CSV generator with CSV formula-injection protection and UTF-8 BOM.
 */
public class CsvExporter {

    private CsvExporter() {
    }

    /**
     * Escapes a single CSV value, mitigating CSV/Spreadsheet formula injection attacks.
     */
    public static String escapeCell(String value) {
        if (value == null) {
            return "";
        }

        String str = value;
        // Formula injection mitigation: if value starts with =, +, -, @, tab or return, prepend with single quote
        if (!str.isEmpty()) {
            char firstChar = str.charAt(0);
            if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@' || firstChar == '\t' || firstChar == '\r') {
                str = "'" + str;
            }
        }

        // If contains comma, quote, or newline, escape double quotes and wrap in quotes
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            str = "\"" + str.replace("\"", "\"\"") + "\"";
        }

        return str;
    }

    /**
     * Writes headers and rows to a CSV file using UTF-8 with BOM for Excel compatibility.
     */
    public static void exportToFile(File file, List<String> headers, List<List<String>> rows) throws IOException {
        try (OutputStream fos = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(writer)) {

            // UTF-8 BOM for Microsoft Excel
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            // Write header
            if (headers != null && !headers.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < headers.size(); i++) {
                    sb.append(escapeCell(headers.get(i)));
                    if (i < headers.size() - 1) sb.append(",");
                }
                bw.write(sb.toString());
                bw.newLine();
            }

            // Write rows
            if (rows != null) {
                for (List<String> row : rows) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < row.size(); i++) {
                        sb.append(escapeCell(row.get(i)));
                        if (i < row.size() - 1) sb.append(",");
                    }
                    bw.write(sb.toString());
                    bw.newLine();
                }
            }
            bw.flush();
        }
    }

    /**
     * Exports a student semester marksheet to CSV.
     */
    public static void exportStudentMarksheet(File file, GradeResult result) throws IOException {
        List<String> headers = List.of(
                "Course Code", "Course Name", "Credits", "Internal Marks", "Max Internal",
                "External Marks", "Max External", "Total Marks", "Max Marks", "Percentage",
                "Grade", "Grade Point", "Status", "Remarks"
        );

        List<List<String>> rows = new ArrayList<>();
        for (SubjectResult sr : result.getSubjectResults()) {
            rows.add(List.of(
                    sr.getSubjectCode(),
                    sr.getSubjectName(),
                    String.valueOf(sr.getCredits()),
                    String.format("%.2f", sr.getInternalMarks()),
                    String.format("%.2f", sr.getMaxInternalMarks()),
                    String.format("%.2f", sr.getExternalMarks()),
                    String.format("%.2f", sr.getMaxExternalMarks()),
                    String.format("%.2f", sr.getTotalMarks()),
                    String.format("%.2f", sr.getMaxMarks()),
                    String.format("%.2f%%", sr.getPercentage()),
                    sr.getGrade(),
                    String.format("%.2f", sr.getGradePoint()),
                    sr.isPassed() ? "PASS" : "FAIL",
                    sr.getRemarks() != null ? sr.getRemarks() : ""
            ));
        }

        // Summary row
        rows.add(List.of(
                "SUMMARY",
                "Total Credits: " + result.getTotalCredits(),
                "",
                "",
                "",
                "",
                "",
                String.format("%.2f", result.getTotalObtainedMarks()),
                String.format("%.2f", result.getTotalMaxMarks()),
                String.format("%.2f%%", result.getPercentage()),
                result.getOverallGrade(),
                String.format("SGPA: %.2f", result.getSgpa()),
                result.getStatus(),
                result.getRank() > 0 ? "Class Rank: " + result.getRank() : ""
        ));

        exportToFile(file, headers, rows);
    }

    /**
     * Exports class rankings cohort to CSV.
     */
    public static void exportClassRankings(File file, List<StudentRank> ranks, String semesterStr, String yearStr) throws IOException {
        List<String> headers = List.of(
                "Rank", "Roll Number", "Student Name", "Department", "Semester",
                "Total Marks", "Max Marks", "Percentage", "SGPA", "Academic Status"
        );

        List<List<String>> rows = new ArrayList<>();
        for (StudentRank sr : ranks) {
            rows.add(List.of(
                    sr.getRank() > 0 ? String.valueOf(sr.getRank()) : "N/A",
                    sr.getRollNumber(),
                    sr.getFullName(),
                    sr.getDepartment(),
                    String.valueOf(sr.getSemester()),
                    String.format("%.2f", sr.getTotalMarks()),
                    String.format("%.2f", sr.getMaxMarks()),
                    String.format("%.2f%%", sr.getPercentage()),
                    String.format("%.2f", sr.getSgpa()),
                    sr.getStatus()
            ));
        }

        exportToFile(file, headers, rows);
    }

    /**
     * Exports subject marks entry list to CSV.
     */
    public static void exportSubjectMarks(File file, List<Marks> marksList, String subjectInfo) throws IOException {
        List<String> headers = List.of(
                "Roll Number", "Student Name", "Subject Code", "Subject Name",
                "Internal Marks", "External Marks", "Total Marks", "Grade",
                "Grade Point", "Result", "Remarks"
        );

        List<List<String>> rows = new ArrayList<>();
        for (Marks m : marksList) {
            rows.add(List.of(
                    m.getStudentRollNumber() != null ? m.getStudentRollNumber() : "",
                    m.getStudentName() != null ? m.getStudentName() : "",
                    m.getSubjectCode() != null ? m.getSubjectCode() : "",
                    m.getSubjectName() != null ? m.getSubjectName() : "",
                    String.format("%.2f", m.getInternalMarks()),
                    String.format("%.2f", m.getExternalMarks()),
                    String.format("%.2f", m.getTotalMarks()),
                    m.getGrade(),
                    String.format("%.2f", m.getGradePoint()),
                    m.isPassed() ? "PASSED" : "FAILED",
                    m.getRemarks() != null ? m.getRemarks() : ""
            ));
        }

        exportToFile(file, headers, rows);
    }
}
