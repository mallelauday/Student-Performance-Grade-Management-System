package com.university.grademanagement.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model representing a student's evaluated semester grade report card and statistics.
 */
public class GradeResult {
    private int studentId;
    private String rollNumber;
    private String studentName;
    private String department;
    private int semester;
    private String academicYearCode;

    private List<SubjectResult> subjectResults;

    private double totalObtainedMarks;
    private double totalMaxMarks;
    private double percentage;
    private int totalCredits;
    private double sgpa;
    private double cgpa;
    private String overallGrade;
    private boolean passed;
    private boolean incomplete;
    private String status; // PASSED, FAILED, INCOMPLETE
    private int rank;
    private String remarks;

    public GradeResult() {
        this.subjectResults = new ArrayList<>();
        this.status = "INCOMPLETE";
        this.rank = 0;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getAcademicYearCode() {
        return academicYearCode;
    }

    public void setAcademicYearCode(String academicYearCode) {
        this.academicYearCode = academicYearCode;
    }

    public List<SubjectResult> getSubjectResults() {
        return subjectResults;
    }

    public void setSubjectResults(List<SubjectResult> subjectResults) {
        this.subjectResults = subjectResults;
    }

    public void addSubjectResult(SubjectResult result) {
        this.subjectResults.add(result);
    }

    public double getTotalObtainedMarks() {
        return totalObtainedMarks;
    }

    public void setTotalObtainedMarks(double totalObtainedMarks) {
        this.totalObtainedMarks = totalObtainedMarks;
    }

    public double getTotalMaxMarks() {
        return totalMaxMarks;
    }

    public void setTotalMaxMarks(double totalMaxMarks) {
        this.totalMaxMarks = totalMaxMarks;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }

    public double getSgpa() {
        return sgpa;
    }

    public void setSgpa(double sgpa) {
        this.sgpa = sgpa;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public String getOverallGrade() {
        return overallGrade;
    }

    public void setOverallGrade(String overallGrade) {
        this.overallGrade = overallGrade;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public void setIncomplete(boolean incomplete) {
        this.incomplete = incomplete;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
