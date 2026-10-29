package com.university.grademanagement.model;

import java.sql.Timestamp;

/**
 * Domain model representing a mapping between a faculty member and an assigned subject.
 */
public class FacultySubject {
    private int id;
    private int facultyId;
    private int subjectId;
    private int academicYearId;
    private int semester;
    private Timestamp assignedDate;

    // Hydrated fields for convenient display in tables and reports
    private String facultyEmployeeId;
    private String facultyName;
    private String facultyDepartment;
    private String subjectCode;
    private String subjectName;
    private String academicYearCode;

    public FacultySubject() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(int facultyId) {
        this.facultyId = facultyId;
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

    public Timestamp getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(Timestamp assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getFacultyEmployeeId() {
        return facultyEmployeeId;
    }

    public void setFacultyEmployeeId(String facultyEmployeeId) {
        this.facultyEmployeeId = facultyEmployeeId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getFacultyDepartment() {
        return facultyDepartment;
    }

    public void setFacultyDepartment(String facultyDepartment) {
        this.facultyDepartment = facultyDepartment;
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

    public String getAcademicYearCode() {
        return academicYearCode;
    }

    public void setAcademicYearCode(String academicYearCode) {
        this.academicYearCode = academicYearCode;
    }

    @Override
    public String toString() {
        return subjectCode + " (" + subjectName + ") - " + facultyName;
    }
}
