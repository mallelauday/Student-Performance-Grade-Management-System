package com.university.grademanagement.model;

import java.sql.Timestamp;

/**
 * Domain model representing an academic year or period.
 */
public class AcademicYear {
    private int id;
    private String yearCode;
    private String yearName;
    private boolean current;
    private Timestamp createdAt;

    public AcademicYear() {
    }

    public AcademicYear(int id, String yearCode, String yearName, boolean current) {
        this.id = id;
        this.yearCode = yearCode;
        this.yearName = yearName;
        this.current = current;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getYearCode() {
        return yearCode;
    }

    public void setYearCode(String yearCode) {
        this.yearCode = yearCode;
    }

    public String getYearName() {
        return yearName;
    }

    public void setYearName(String yearName) {
        this.yearName = yearName;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return yearName + (current ? " [Current]" : "");
    }
}
