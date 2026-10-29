package com.university.grademanagement.service;

import com.university.grademanagement.dao.*;
import com.university.grademanagement.exception.ResourceNotFoundException;
import com.university.grademanagement.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Centralized, configurable grading service responsible for calculating grades,
 * grade points, SGPA, CGPA, pass/fail status, and semester grade reports.
 */
public class GradeService {

    private final MarksDao marksDao;
    private final StudentDao studentDao;
    private final SubjectDao subjectDao;
    private final AcademicYearDao academicYearDao;

    public GradeService() {
        this(new MarksDaoImpl(), new StudentDaoImpl(), new SubjectDaoImpl(), new AcademicYearDaoImpl());
    }

    public GradeService(MarksDao marksDao, StudentDao studentDao, SubjectDao subjectDao, AcademicYearDao academicYearDao) {
        this.marksDao = marksDao;
        this.studentDao = studentDao;
        this.subjectDao = subjectDao;
        this.academicYearDao = academicYearDao;
    }

    /**
     * Determines letter grade from percentage according to university grading scale.
     */
    public String calculateGrade(double percentage, boolean isPassed, boolean isAbsent) {
        if (isAbsent || !isPassed) {
            return "F";
        }
        if (percentage >= 90.0) return "A+";
        if (percentage >= 80.0) return "A";
        if (percentage >= 70.0) return "B";
        if (percentage >= 60.0) return "C";
        if (percentage >= 50.0) return "D";
        if (percentage >= 40.0) return "E";
        return "F";
    }

    /**
     * Determines 10-point scale grade point from grade.
     */
    public double calculateGradePoint(String grade) {
        if (grade == null) return 0.0;
        return switch (grade.toUpperCase().trim()) {
            case "A+" -> 10.0;
            case "A" -> 9.0;
            case "B" -> 8.0;
            case "C" -> 7.0;
            case "D" -> 6.0;
            case "E" -> 5.0;
            default -> 0.0;
        };
    }

    /**
     * Computes subject-level passing status based on total marks and passing threshold.
     */
    public boolean checkSubjectPassed(double totalMarks, double passMarks, boolean isAbsent) {
        if (isAbsent) return false;
        return totalMarks >= passMarks;
    }

    /**
     * Evaluates full semester academic results for a student.
     * Accurately distinguishes between PASSED, FAILED, and INCOMPLETE results.
     */
    public GradeResult calculateSemesterResult(int studentId, int academicYearId, int semester) {
        Student student = studentDao.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        AcademicYear year = academicYearDao.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        // Get all active subjects required for this department and semester
        List<Subject> requiredSubjects = subjectDao.findByDepartmentAndSemester(student.getDepartment(), semester);
        List<Marks> recordedMarks = marksDao.findByStudentAndSemester(studentId, academicYearId, semester);

        GradeResult result = new GradeResult();
        result.setStudentId(studentId);
        result.setRollNumber(student.getRollNumber());
        result.setStudentName(student.getFullName());
        result.setDepartment(student.getDepartment());
        result.setSemester(semester);
        result.setAcademicYearCode(year.getYearCode());

        double totalObtained = 0.0;
        double totalMax = 0.0;
        int totalCredits = 0;
        double totalWeightedPoints = 0.0;
        int failedSubjectCount = 0;
        boolean hasMissingSubjectMarks = false;

        for (Subject sub : requiredSubjects) {
            Optional<Marks> markOpt = recordedMarks.stream()
                    .filter(m -> m.getSubjectId() == sub.getId())
                    .findFirst();

            if (markOpt.isEmpty()) {
                hasMissingSubjectMarks = true;
                // Represent missing assessment explicitly
                SubjectResult sr = new SubjectResult(
                        sub.getSubjectCode(), sub.getSubjectName(), sub.getCredits(),
                        0.0, sub.getMaxInternalMarks(),
                        0.0, sub.getMaxExternalMarks(),
                        0.0, sub.getMaxMarks(),
                        0.0, "INCOMPLETE", 0.0,
                        false, false, "Pending marks evaluation"
                );
                result.addSubjectResult(sr);
                totalMax += sub.getMaxMarks();
                totalCredits += sub.getCredits();
            } else {
                Marks m = markOpt.get();
                double subTotal = m.getTotalMarks();
                double subMax = sub.getMaxMarks();
                double percentage = subMax > 0 ? (subTotal / subMax) * 100.0 : 0.0;
                boolean passed = m.isPassed();
                if (!passed) {
                    failedSubjectCount++;
                }

                SubjectResult sr = new SubjectResult(
                        sub.getSubjectCode(), sub.getSubjectName(), sub.getCredits(),
                        m.getInternalMarks(), sub.getMaxInternalMarks(),
                        m.getExternalMarks(), sub.getMaxExternalMarks(),
                        subTotal, subMax, percentage,
                        m.getGrade(), m.getGradePoint(),
                        passed, m.isAbsent(), m.getRemarks()
                );
                result.addSubjectResult(sr);

                totalObtained += subTotal;
                totalMax += subMax;
                totalCredits += sub.getCredits();
                totalWeightedPoints += (m.getGradePoint() * sub.getCredits());
            }
        }

        result.setTotalObtainedMarks(totalObtained);
        result.setTotalMaxMarks(totalMax);
        result.setTotalCredits(totalCredits);

        double overallPercentage = totalMax > 0 ? (totalObtained / totalMax) * 100.0 : 0.0;
        result.setPercentage(overallPercentage);

        double sgpa = totalCredits > 0 ? (totalWeightedPoints / totalCredits) : 0.0;
        result.setSgpa(Math.round(sgpa * 100.0) / 100.0);

        // Academic standing determinations
        if (hasMissingSubjectMarks) {
            result.setStatus("INCOMPLETE");
            result.setIncomplete(true);
            result.setPassed(false);
            result.setOverallGrade("I");
            result.setRemarks("Semester assessment is incomplete. Some subject marks are pending.");
        } else if (failedSubjectCount > 0) {
            result.setStatus("FAILED (" + failedSubjectCount + " Backlog)");
            result.setIncomplete(false);
            result.setPassed(false);
            result.setOverallGrade("F");
            result.setRemarks("Candidate has failed in " + failedSubjectCount + " subject(s). Eligible for supplementary examination.");
        } else {
            result.setStatus("PASSED");
            result.setIncomplete(false);
            result.setPassed(true);
            result.setOverallGrade(calculateGrade(overallPercentage, true, false));

            if (overallPercentage >= 75.0) {
                result.setRemarks("Passed in First Class with Distinction");
            } else if (overallPercentage >= 60.0) {
                result.setRemarks("Passed in First Class");
            } else if (overallPercentage >= 50.0) {
                result.setRemarks("Passed in Second Class");
            } else {
                result.setRemarks("Passed in Pass Class");
            }
        }

        return result;
    }
}
