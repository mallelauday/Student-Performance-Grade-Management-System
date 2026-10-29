package com.university.grademanagement.model;

/**
 * Encapsulates the evaluation result of a single subject within a student's semester report.
 */
public class SubjectResult {
    private String subjectCode;
    private String subjectName;
    private int credits;
    private double internalMarks;
    private double maxInternalMarks;
    private double externalMarks;
    private double maxExternalMarks;
    private double totalMarks;
    private double maxMarks;
    private double percentage;
    private String grade;
    private double gradePoint;
    private boolean passed;
    private boolean absent;
    private String remarks;

    public SubjectResult() {
    }

    public SubjectResult(String subjectCode, String subjectName, int credits,
                         double internalMarks, double maxInternalMarks,
                         double externalMarks, double maxExternalMarks,
                         double totalMarks, double maxMarks,
                         double percentage, String grade, double gradePoint,
                         boolean passed, boolean absent, String remarks) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.credits = credits;
        this.internalMarks = internalMarks;
        this.maxInternalMarks = maxInternalMarks;
        this.externalMarks = externalMarks;
        this.maxExternalMarks = maxExternalMarks;
        this.totalMarks = totalMarks;
        this.maxMarks = maxMarks;
        this.percentage = percentage;
        this.grade = grade;
        this.gradePoint = gradePoint;
        this.passed = passed;
        this.absent = absent;
        this.remarks = remarks;
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

    public double getInternalMarks() {
        return internalMarks;
    }

    public void setInternalMarks(double internalMarks) {
        this.internalMarks = internalMarks;
    }

    public double getMaxInternalMarks() {
        return maxInternalMarks;
    }

    public void setMaxInternalMarks(double maxInternalMarks) {
        this.maxInternalMarks = maxInternalMarks;
    }

    public double getExternalMarks() {
        return externalMarks;
    }

    public void setExternalMarks(double externalMarks) {
        this.externalMarks = externalMarks;
    }

    public double getMaxExternalMarks() {
        return maxExternalMarks;
    }

    public void setMaxExternalMarks(double maxExternalMarks) {
        this.maxExternalMarks = maxExternalMarks;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
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
}
