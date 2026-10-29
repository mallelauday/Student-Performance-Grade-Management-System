package com.university.grademanagement.model;

/**
 * Domain model representing a student's ranking position in a class/semester cohort.
 */
public class StudentRank {
    private int rank;
    private int studentId;
    private String rollNumber;
    private String fullName;
    private String department;
    private int semester;
    private double totalMarks;
    private double maxMarks;
    private double percentage;
    private double sgpa;
    private String status; // PASSED, FAILED, INCOMPLETE

    public StudentRank() {
    }

    public StudentRank(int rank, int studentId, String rollNumber, String fullName, String department,
                       int semester, double totalMarks, double maxMarks, double percentage, double sgpa, String status) {
        this.rank = rank;
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.department = department;
        this.semester = semester;
        this.totalMarks = totalMarks;
        this.maxMarks = maxMarks;
        this.percentage = percentage;
        this.sgpa = sgpa;
        this.status = status;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public double getSgpa() {
        return sgpa;
    }

    public void setSgpa(double sgpa) {
        this.sgpa = sgpa;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
