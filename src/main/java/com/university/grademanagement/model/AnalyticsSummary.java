package com.university.grademanagement.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model aggregating university-wide and class-wide academic performance metrics.
 */
public class AnalyticsSummary {
    private int totalStudents;
    private int totalFaculty;
    private int totalSubjects;
    private int totalMarksRecorded;

    private double classAveragePercentage;
    private double overallPassRate;
    private double highestPercentage;
    private double lowestPercentage;
    private int atRiskStudentCount;

    private String topPerformerName;
    private String topPerformerRoll;
    private double topPerformerPercentage;

    private Map<String, Integer> gradeDistribution;
    private Map<String, Double> subjectAverages;
    private Map<Integer, Double> semesterPassRates;

    public AnalyticsSummary() {
        this.gradeDistribution = new HashMap<>();
        this.subjectAverages = new HashMap<>();
        this.semesterPassRates = new HashMap<>();
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalFaculty() {
        return totalFaculty;
    }

    public void setTotalFaculty(int totalFaculty) {
        this.totalFaculty = totalFaculty;
    }

    public int getTotalSubjects() {
        return totalSubjects;
    }

    public void setTotalSubjects(int totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public int getTotalMarksRecorded() {
        return totalMarksRecorded;
    }

    public void setTotalMarksRecorded(int totalMarksRecorded) {
        this.totalMarksRecorded = totalMarksRecorded;
    }

    public double getClassAveragePercentage() {
        return classAveragePercentage;
    }

    public void setClassAveragePercentage(double classAveragePercentage) {
        this.classAveragePercentage = classAveragePercentage;
    }

    public double getOverallPassRate() {
        return overallPassRate;
    }

    public void setOverallPassRate(double overallPassRate) {
        this.overallPassRate = overallPassRate;
    }

    public double getHighestPercentage() {
        return highestPercentage;
    }

    public void setHighestPercentage(double highestPercentage) {
        this.highestPercentage = highestPercentage;
    }

    public double getLowestPercentage() {
        return lowestPercentage;
    }

    public void setLowestPercentage(double lowestPercentage) {
        this.lowestPercentage = lowestPercentage;
    }

    public int getAtRiskStudentCount() {
        return atRiskStudentCount;
    }

    public void setAtRiskStudentCount(int atRiskStudentCount) {
        this.atRiskStudentCount = atRiskStudentCount;
    }

    public String getTopPerformerName() {
        return topPerformerName;
    }

    public void setTopPerformerName(String topPerformerName) {
        this.topPerformerName = topPerformerName;
    }

    public String getTopPerformerRoll() {
        return topPerformerRoll;
    }

    public void setTopPerformerRoll(String topPerformerRoll) {
        this.topPerformerRoll = topPerformerRoll;
    }

    public double getTopPerformerPercentage() {
        return topPerformerPercentage;
    }

    public void setTopPerformerPercentage(double topPerformerPercentage) {
        this.topPerformerPercentage = topPerformerPercentage;
    }

    public Map<String, Integer> getGradeDistribution() {
        return gradeDistribution;
    }

    public void setGradeDistribution(Map<String, Integer> gradeDistribution) {
        this.gradeDistribution = gradeDistribution;
    }

    public Map<String, Double> getSubjectAverages() {
        return subjectAverages;
    }

    public void setSubjectAverages(Map<String, Double> subjectAverages) {
        this.subjectAverages = subjectAverages;
    }

    public Map<Integer, Double> getSemesterPassRates() {
        return semesterPassRates;
    }

    public void setSemesterPassRates(Map<Integer, Double> semesterPassRates) {
        this.semesterPassRates = semesterPassRates;
    }
}
