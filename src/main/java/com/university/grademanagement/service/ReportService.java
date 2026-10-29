package com.university.grademanagement.service;

import com.university.grademanagement.dao.AuditLogDao;
import com.university.grademanagement.dao.AuditLogDaoImpl;
import com.university.grademanagement.dao.MarksDao;
import com.university.grademanagement.dao.MarksDaoImpl;
import com.university.grademanagement.dao.StudentDao;
import com.university.grademanagement.dao.StudentDaoImpl;
import com.university.grademanagement.exception.AuthorizationException;
import com.university.grademanagement.exception.ResourceNotFoundException;
import com.university.grademanagement.model.*;
import com.university.grademanagement.util.CsvExporter;
import com.university.grademanagement.util.PdfReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Service managing authorized export of marksheets, transcripts, rankings, and marks in CSV and PDF formats.
 */
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final GradeService gradeService;
    private final RankingService rankingService;
    private final MarksDao marksDao;
    private final StudentDao studentDao;
    private final AuditLogDao auditLogDao;

    public ReportService() {
        this(new GradeService(), new RankingService(), new MarksDaoImpl(), new StudentDaoImpl(), new AuditLogDaoImpl());
    }

    public ReportService(GradeService gradeService, RankingService rankingService, MarksDao marksDao, StudentDao studentDao, AuditLogDao auditLogDao) {
        this.gradeService = gradeService;
        this.rankingService = rankingService;
        this.marksDao = marksDao;
        this.studentDao = studentDao;
        this.auditLogDao = auditLogDao;
    }

    /**
     * Exports a student semester marksheet in PDF or CSV format.
     */
    public void exportStudentMarksheet(int studentId, int academicYearId, int semester, File destFile, String format, User currentUser) throws Exception {
        if (currentUser == null) {
            throw new AuthorizationException("Authentication required.");
        }

        // Student authorization check
        if (currentUser.getRole() == Role.STUDENT) {
            Student s = studentDao.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            if (s.getUserId() != currentUser.getId()) {
                throw new AuthorizationException("Students are only authorized to export their own marksheets.");
            }
        }

        GradeResult result = gradeService.calculateSemesterResult(studentId, academicYearId, semester);

        if ("PDF".equalsIgnoreCase(format)) {
            PdfReportGenerator.generateMarksheetPdf(destFile, result);
        } else {
            CsvExporter.exportStudentMarksheet(destFile, result);
        }

        auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "EXPORT_MARKSHEET",
                "MARKSHEET", "Student:" + result.getRollNumber(),
                "Exported " + format.toUpperCase() + " marksheet for semester " + semester));
    }

    /**
     * Exports class rankings in PDF or CSV format.
     */
    public void exportClassRankings(int academicYearId, int semester, String dept, String yearStr, File destFile, String format, User currentUser) throws Exception {
        AuthService.requireRole(currentUser, Role.ADMIN, Role.FACULTY);

        List<StudentRank> rankings = rankingService.getClassRankings(academicYearId, semester, dept, currentUser);

        if ("PDF".equalsIgnoreCase(format)) {
            PdfReportGenerator.generateClassRankingsPdf(destFile, rankings, dept != null ? dept : "All Departments", semester, yearStr);
        } else {
            CsvExporter.exportClassRankings(destFile, rankings, String.valueOf(semester), yearStr);
        }

        auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "EXPORT_RANKINGS",
                "RANKINGS", "Sem:" + semester,
                "Exported " + format.toUpperCase() + " class rankings report"));
    }

    /**
     * Exports subject marks entry sheet to CSV.
     */
    public void exportSubjectMarks(int subjectId, int academicYearId, int semester, String subjectCode, File destFile, User currentUser) throws Exception {
        AuthService.requireRole(currentUser, Role.ADMIN, Role.FACULTY);

        List<Marks> marksList = marksDao.findBySubjectAndSemester(subjectId, academicYearId, semester);
        CsvExporter.exportSubjectMarks(destFile, marksList, subjectCode);

        auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "EXPORT_SUBJECT_MARKS",
                "MARKS", "Subject:" + subjectCode, "Exported marks list to CSV"));
    }
}
