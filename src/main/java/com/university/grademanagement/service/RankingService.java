package com.university.grademanagement.service;

import com.university.grademanagement.dao.MarksDao;
import com.university.grademanagement.dao.MarksDaoImpl;
import com.university.grademanagement.dao.StudentDao;
import com.university.grademanagement.dao.StudentDaoImpl;
import com.university.grademanagement.exception.AuthorizationException;
import com.university.grademanagement.model.Role;
import com.university.grademanagement.model.Student;
import com.university.grademanagement.model.StudentRank;
import com.university.grademanagement.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service managing student merit rankings, eligibility filtering, and at-risk student detection.
 */
public class RankingService {

    private final MarksDao marksDao;
    private final StudentDao studentDao;

    public RankingService() {
        this(new MarksDaoImpl(), new StudentDaoImpl());
    }

    public RankingService(MarksDao marksDao, StudentDao studentDao) {
        this.marksDao = marksDao;
        this.studentDao = studentDao;
    }

    /**
     * Calculates cohort rankings for a given academic year and semester.
     * Enforces university ranking policy: failed subjects disqualify from merit rank numbering.
     */
    public List<StudentRank> getClassRankings(int academicYearId, int semester, String department, User currentUser) {
        if (currentUser == null) {
            throw new AuthorizationException("Authentication required.");
        }

        List<StudentRank> rankings = marksDao.getRankings(academicYearId, semester, department);

        // If logged-in user is a student, verify if policy allows them to see the cohort or their own rank
        if (currentUser.getRole() == Role.STUDENT) {
            // Find student's own record
            Optional<Student> studentOpt = studentDao.findByUserId(currentUser.getId());
            if (studentOpt.isPresent()) {
                String myRoll = studentOpt.get().getRollNumber();
                // Annotate or ensure student can inspect their own standing
                rankings.forEach(r -> {
                    if (r.getRollNumber().equalsIgnoreCase(myRoll)) {
                        r.setFullName(r.getFullName() + " (You)");
                    }
                });
            }
        }

        return rankings;
    }

    /**
     * Identifies students requiring academic intervention and support (failed courses or low percentage).
     */
    public List<StudentRank> getAtRiskStudents(Integer academicYearId, Integer semester, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN, Role.FACULTY);
        return marksDao.getAtRiskStudents(academicYearId, semester, 50.0);
    }
}
