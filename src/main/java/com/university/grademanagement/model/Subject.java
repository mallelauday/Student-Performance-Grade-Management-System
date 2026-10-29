package com.university.grademanagement.model;

import java.sql.Timestamp;

/**
 * Domain model representing an academic subject/course.
 */
public class Subject {
    private int id;
    private String subjectCode;
    private String subjectName;
    private String department;
    private int semester;
    private int credits;
    private double maxInternalMarks;
    private double maxExternalMarks;
    private double maxMarks;
    private double passMarks;
    private boolean elective;
    private boolean active;
    private Timestamp createdAt;

    public Subject() {
        this.credits = 4;
        this.maxInternalMarks = 40.0;
        this.maxExternalMarks = 60.0;
        this.maxMarks = 100.0;
        this.passMarks = 40.0;
        this.elective = false;
        this.active = true;
    }

    public Subject(int id, String subjectCode, String subjectName, String department, int semester, int credits,
                   double maxInternalMarks, double maxExternalMarks, double maxMarks, double passMarks, boolean elective, boolean active) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.department = department;
        this.semester = semester;
        this.credits = credits;
        this.maxInternalMarks = maxInternalMarks;
        this.maxExternalMarks = maxExternalMarks;
        this.maxMarks = maxMarks;
        this.passMarks = passMarks;
        this.elective = elective;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        this.maxMarks = this.maxInternalMarks + this.maxExternalMarks;
    }

    public double getMaxExternalMarks() {
        return maxExternalMarks;
    }

    public void setMaxExternalMarks(double maxExternalMarks) {
        this.maxExternalMarks = maxExternalMarks;
        this.maxMarks = this.maxInternalMarks + this.maxExternalMarks;
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

    public boolean isElective() {
        return elective;
    }

    public void setElective(boolean elective) {
        this.elective = elective;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return subjectCode + " - " + subjectName + " (" + credits + " Credits)";
    }
}
