package com.university.grademanagement.dao;

import com.university.grademanagement.model.Marks;
import com.university.grademanagement.model.StudentRank;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object interface for Marks and Academic Performance evaluation.
 */
public interface MarksDao {
    Optional<Marks> findById(int id);
    Optional<Marks> findByStudentAndSubject(int studentId, int subjectId, int academicYearId, int semester);
    List<Marks> findByStudentAndSemester(int studentId, int academicYearId, int semester);
    List<Marks> findBySubjectAndSemester(int subjectId, int academicYearId, int semester);
    Marks saveOrUpdate(Marks marks, Connection conn);
    boolean saveBatch(List<Marks> marksList, int userId);
    int countByStudentAndSemester(int studentId, int academicYearId, int semester);
    int countFailedSubjects(int studentId, int academicYearId, int semester);
    double getSubjectAverage(int subjectId, int academicYearId, int semester);
    double getOverallAveragePercentage(Integer academicYearId, Integer semester);
    double getOverallPassRate(Integer academicYearId, Integer semester);
    double getHighestMarksPercentage(Integer academicYearId, Integer semester);
    double getLowestMarksPercentage(Integer academicYearId, Integer semester);
    Map<String, Integer> getGradeDistribution(Integer academicYearId, Integer semester);
    Map<String, Double> getSubjectWiseAverages(Integer academicYearId, Integer semester);
    Map<Integer, Double> getSemesterPassRates(Integer academicYearId);
    List<StudentRank> getRankings(int academicYearId, int semester, String department);
    List<StudentRank> getAtRiskStudents(Integer academicYearId, Integer semester, double passThreshold);
    int countTotalMarks();
}
