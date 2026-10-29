package com.university.grademanagement.model;

import java.sql.Timestamp;

/**
 * Domain model representing a student's marks record for a specific subject and academic period.
 */
public class Marks {
    private int id;
    private int studentId;
    private int subjectId;
    private int academicYearId;
    private int semester;
    private double internalMarks;
    private double externalMarks;
    private double totalMarks;
    private String grade;
    private double gradePoint;
    private boolean passed;
    private boolean absent;
    private String remarks;
    private Integer enteredByUserId;
    private Timestamp enteredAt;
    private Timestamp updatedAt;

    // Hydrated fields for convenient display in tables and reports
    private String studentRollNumber;
    private String studentName;
    private String subjectCode;
    private String subjectName;
    private int credits;
    private double maxInternalMarks;
    private double maxExternalMarks;
    private double maxMarks;
    private double passMarks;
    private String academicYearCode;

    public Marks() {
        this.grade = "F";
        this.gradePoint = 0.0;
        this.passed = false;
        this.absent = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(int academicYearId) {
        this.academicYearId = academicYearId;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public double getInternalMarks() {
        return internalMarks;
    }

    public void setInternalMarks(double internalMarks) {
        this.internalMarks = internalMarks;
        this.totalMarks = this.internalMarks + this.externalMarks;
    }

    public double getExternalMarks() {
        return externalMarks;
    }

    public void setExternalMarks(double externalMarks) {
        this.externalMarks = externalMarks;
        this.totalMarks = this.internalMarks + this.externalMarks;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public double getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(double gradePoint) {
        this.gradePoint = gradePoint;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public boolean isAbsent() {
        return absent;
    }

    public void setAbsent(boolean absent) {
        this.absent = absent;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getEnteredByUserId() {
        return enteredByUserId;
    }

    public void setEnteredByUserId(Integer enteredByUserId) {
        this.enteredByUserId = enteredByUserId;
    }

    public Timestamp getEnteredAt() {
        return enteredAt;
    }

    public void setEnteredAt(Timestamp enteredAt) {
        this.enteredAt = enteredAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public double getMaxInternalMarks() {
        return maxInternalMarks;
    }

    public void setMaxInternalMarks(double maxInternalMarks) {
        this.maxInternalMarks = maxInternalMarks;
    }

    public double getMaxExternalMarks() {
        return maxExternalMarks;
    }

    public void setMaxExternalMarks(double maxExternalMarks) {
        this.maxExternalMarks = maxExternalMarks;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public double getPassMarks() {
        return passMarks;
    }

    public void setPassMarks(double passMarks) {
        this.passMarks = passMarks;
    }

    public String getAcademicYearCode() {
        return academicYearCode;
    }

    public void setAcademicYearCode(String academicYearCode) {
        this.academicYearCode = academicYearCode;
    }

    /**
     * Calculates the percentage for this subject based on maximum marks.
     */
    public double getPercentage() {
        if (maxMarks <= 0) return 0.0;
        return (totalMarks / maxMarks) * 100.0;
    }

    @Override
    public String toString() {
        return studentRollNumber + " - " + subjectCode + ": " + totalMarks + " (" + grade + ")";
    }
}
