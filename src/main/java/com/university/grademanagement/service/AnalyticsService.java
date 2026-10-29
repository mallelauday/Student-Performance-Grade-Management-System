package com.university.grademanagement.service;

import com.university.grademanagement.dao.*;
import com.university.grademanagement.model.AnalyticsSummary;
import com.university.grademanagement.model.StudentRank;

import java.util.List;

/**
 * Service aggregating performance analytics, pass rates, distributions, and university metrics.
 */
public class AnalyticsService {

    private final MarksDao marksDao;
    private final StudentDao studentDao;
    private final FacultyDao facultyDao;
    private final SubjectDao subjectDao;

    public AnalyticsService() {
        this(new MarksDaoImpl(), new StudentDaoImpl(), new FacultyDaoImpl(), new SubjectDaoImpl());
    }

    public AnalyticsService(MarksDao marksDao, StudentDao studentDao, FacultyDao facultyDao, SubjectDao subjectDao) {
        this.marksDao = marksDao;
        this.studentDao = studentDao;
        this.facultyDao = facultyDao;
        this.subjectDao = subjectDao;
    }

    /**
     * Gathers unified dashboard summary data from persistent database records.
     */
    public AnalyticsSummary getAnalyticsSummary(Integer academicYearId, Integer semester) {
        AnalyticsSummary summary = new AnalyticsSummary();

        // Entity counts
        summary.setTotalStudents(studentDao.count(null, null, null, true));
        summary.setTotalFaculty(facultyDao.count());
        summary.setTotalSubjects(subjectDao.count());
        summary.setTotalMarksRecorded(marksDao.countTotalMarks());

        // Performance metrics
        double avg = marksDao.getOverallAveragePercentage(academicYearId, semester);
        summary.setClassAveragePercentage(Math.round(avg * 100.0) / 100.0);

        double passRate = marksDao.getOverallPassRate(academicYearId, semester);
        summary.setOverallPassRate(Math.round(passRate * 100.0) / 100.0);

        summary.setHighestPercentage(Math.round(marksDao.getHighestMarksPercentage(academicYearId, semester) * 100.0) / 100.0);
        summary.setLowestPercentage(Math.round(marksDao.getLowestMarksPercentage(academicYearId, semester) * 100.0) / 100.0);

        // Grade distribution & charts
        summary.setGradeDistribution(marksDao.getGradeDistribution(academicYearId, semester));
        summary.setSubjectAverages(marksDao.getSubjectWiseAverages(academicYearId, semester));
        summary.setSemesterPassRates(marksDao.getSemesterPassRates(academicYearId));

        // At risk count
        List<StudentRank> atRisk = marksDao.getAtRiskStudents(academicYearId, semester, 50.0);
        summary.setAtRiskStudentCount(atRisk.size());

        // Top performer
        if (academicYearId != null && semester != null && semester > 0) {
            List<StudentRank> ranks = marksDao.getRankings(academicYearId, semester, null);
            if (!ranks.isEmpty() && ranks.get(0).getRank() == 1) {
                StudentRank top = ranks.get(0);
                summary.setTopPerformerName(top.getFullName());
                summary.setTopPerformerRoll(top.getRollNumber());
                summary.setTopPerformerPercentage(Math.round(top.getPercentage() * 100.0) / 100.0);
            }
        }

        return summary;
    }
}
