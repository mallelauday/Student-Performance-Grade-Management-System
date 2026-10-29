package com.university.grademanagement.dao;

import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.exception.DatabaseException;
import com.university.grademanagement.model.Marks;
import com.university.grademanagement.model.StudentRank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * JDBC PreparedStatement implementation of MarksDao with analytics and ranking queries.
 */
public class MarksDaoImpl implements MarksDao {

    private static final Logger logger = LoggerFactory.getLogger(MarksDaoImpl.class);

    private Marks mapRow(ResultSet rs) throws SQLException {
        Marks m = new Marks();
        m.setId(rs.getInt("id"));
        m.setStudentId(rs.getInt("student_id"));
        m.setSubjectId(rs.getInt("subject_id"));
        m.setAcademicYearId(rs.getInt("academic_year_id"));
        m.setSemester(rs.getInt("semester"));
        m.setInternalMarks(rs.getDouble("internal_marks"));
        m.setExternalMarks(rs.getDouble("external_marks"));
        m.setTotalMarks(rs.getDouble("total_marks"));
        m.setGrade(rs.getString("grade"));
        m.setGradePoint(rs.getDouble("grade_point"));
        m.setPassed(rs.getBoolean("is_passed"));
        m.setAbsent(rs.getBoolean("is_absent"));
        m.setRemarks(rs.getString("remarks"));
        m.setEnteredByUserId((Integer) rs.getObject("entered_by_user_id"));
        m.setEnteredAt(rs.getTimestamp("entered_at"));
        m.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            m.setStudentRollNumber(rs.getString("roll_number"));
            m.setStudentName(rs.getString("student_name"));
            m.setSubjectCode(rs.getString("subject_code"));
            m.setSubjectName(rs.getString("subject_name"));
            m.setCredits(rs.getInt("credits"));
            m.setMaxInternalMarks(rs.getDouble("max_internal_marks"));
            m.setMaxExternalMarks(rs.getDouble("max_external_marks"));
            m.setMaxMarks(rs.getDouble("max_marks"));
            m.setPassMarks(rs.getDouble("pass_marks"));
            m.setAcademicYearCode(rs.getString("year_code"));
        } catch (SQLException ignored) {}

        return m;
    }

    private String getBaseSelect() {
        return "SELECT m.*, s.roll_number, CONCAT(s.first_name, ' ', s.last_name) as student_name, " +
                "sub.subject_code, sub.subject_name, sub.credits, sub.max_internal_marks, sub.max_external_marks, " +
                "sub.max_marks, sub.pass_marks, y.year_code " +
                "FROM marks m " +
                "JOIN students s ON m.student_id = s.id " +
                "JOIN subjects sub ON m.subject_id = sub.id " +
                "JOIN academic_years y ON m.academic_year_id = y.id ";
    }

    @Override
    public Optional<Marks> findById(int id) {
        String sql = getBaseSelect() + "WHERE m.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding marks by id {}: {}", id, e.getMessage());
            throw new DatabaseException("Failed to find marks record", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Marks> findByStudentAndSubject(int studentId, int subjectId, int academicYearId, int semester) {
        String sql = getBaseSelect() + "WHERE m.student_id = ? AND m.subject_id = ? AND m.academic_year_id = ? AND m.semester = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, academicYearId);
            ps.setInt(4, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding marks for student {} subject {}: {}", studentId, subjectId, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Marks> findByStudentAndSemester(int studentId, int academicYearId, int semester) {
        List<Marks> list = new ArrayList<>();
        String sql = getBaseSelect() + "WHERE m.student_id = ? AND m.academic_year_id = ? AND m.semester = ? ORDER BY sub.subject_code ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYearId);
            ps.setInt(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding marks for student {} sem {}: {}", studentId, semester, e.getMessage());
            throw new DatabaseException("Failed to find marks by student", e);
        }
        return list;
    }

    @Override
    public List<Marks> findBySubjectAndSemester(int subjectId, int academicYearId, int semester) {
        List<Marks> list = new ArrayList<>();
        String sql = getBaseSelect() + "WHERE m.subject_id = ? AND m.academic_year_id = ? AND m.semester = ? ORDER BY s.roll_number ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, academicYearId);
            ps.setInt(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding marks for subject {} sem {}: {}", subjectId, semester, e.getMessage());
            throw new DatabaseException("Failed to find marks by subject", e);
        }
        return list;
    }

    @Override
    public Marks saveOrUpdate(Marks marks, Connection externalConn) {
        String sql = "INSERT INTO marks (student_id, subject_id, academic_year_id, semester, internal_marks, " +
                "external_marks, total_marks, grade, grade_point, is_passed, is_absent, remarks, entered_by_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE internal_marks = VALUES(internal_marks), " +
                "external_marks = VALUES(external_marks), total_marks = VALUES(total_marks), " +
                "grade = VALUES(grade), grade_point = VALUES(grade_point), is_passed = VALUES(is_passed), " +
                "is_absent = VALUES(is_absent), remarks = VALUES(remarks), entered_by_user_id = VALUES(entered_by_user_id), " +
                "updated_at = CURRENT_TIMESTAMP";

        boolean managedExternally = externalConn != null;
        Connection conn = null;
        try {
            conn = managedExternally ? externalConn : DatabaseConfig.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, marks.getStudentId());
                ps.setInt(2, marks.getSubjectId());
                ps.setInt(3, marks.getAcademicYearId());
                ps.setInt(4, marks.getSemester());
                ps.setDouble(5, marks.getInternalMarks());
                ps.setDouble(6, marks.getExternalMarks());
                ps.setDouble(7, marks.getTotalMarks());
                ps.setString(8, marks.getGrade());
                ps.setDouble(9, marks.getGradePoint());
                ps.setBoolean(10, marks.isPassed());
                ps.setBoolean(11, marks.isAbsent());
                ps.setString(12, marks.getRemarks());
                if (marks.getEnteredByUserId() != null) {
                    ps.setInt(13, marks.getEnteredByUserId());
                } else {
                    ps.setNull(13, Types.INTEGER);
                }

                ps.executeUpdate();
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        marks.setId(generatedKeys.getInt(1));
                    }
                }
                return marks;
            }
        } catch (SQLException e) {
            logger.error("Error saving marks for student {} subject {}: {}", marks.getStudentId(), marks.getSubjectId(), e.getMessage());
            throw new DatabaseException("Failed to save marks record", e);
        } finally {
            if (!managedExternally && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean saveBatch(List<Marks> marksList, int userId) {
        if (marksList == null || marksList.isEmpty()) return true;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Marks m : marksList) {
                    m.setEnteredByUserId(userId);
                    saveOrUpdate(m, conn);
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Error executing marks batch update: {}", e.getMessage());
                throw new DatabaseException("Failed to execute batch marks update", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Database connection error during batch update: {}", e.getMessage());
            throw new DatabaseException("Database error during marks batch update", e);
        }
    }

    @Override
    public int countByStudentAndSemester(int studentId, int academicYearId, int semester) {
        String sql = "SELECT COUNT(*) FROM marks WHERE student_id = ? AND academic_year_id = ? AND semester = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYearId);
            ps.setInt(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting marks for student: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public int countFailedSubjects(int studentId, int academicYearId, int semester) {
        String sql = "SELECT COUNT(*) FROM marks WHERE student_id = ? AND academic_year_id = ? AND semester = ? AND is_passed = FALSE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYearId);
            ps.setInt(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting failed subjects: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public double getSubjectAverage(int subjectId, int academicYearId, int semester) {
        String sql = "SELECT AVG(total_marks) FROM marks WHERE subject_id = ? AND academic_year_id = ? AND semester = ? AND is_absent = FALSE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, academicYearId);
            ps.setInt(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("Error calculating subject average: {}", e.getMessage());
        }
        return 0.0;
    }

    @Override
    public double getOverallAveragePercentage(Integer academicYearId, Integer semester) {
        StringBuilder sql = new StringBuilder("SELECT AVG((m.total_marks / sub.max_marks) * 100) " +
                "FROM marks m JOIN subjects sub ON m.subject_id = sub.id WHERE m.is_absent = FALSE ");
        List<Object> params = new ArrayList<>();
        if (academicYearId != null) {
            sql.append("AND m.academic_year_id = ? ");
            params.add(academicYearId);
        }
        if (semester != null && semester > 0) {
            sql.append("AND m.semester = ? ");
            params.add(semester);
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting class average percentage: {}", e.getMessage());
        }
        return 0.0;
    }

    @Override
    public double getOverallPassRate(Integer academicYearId, Integer semester) {
        StringBuilder sql = new StringBuilder("SELECT " +
                "(SUM(CASE WHEN is_passed = TRUE THEN 1 ELSE 0 END) * 100.0) / COUNT(*) " +
                "FROM marks WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (academicYearId != null) {
            sql.append("AND academic_year_id = ? ");
            params.add(academicYearId);
        }
        if (semester != null && semester > 0) {
            sql.append("AND semester = ? ");
            params.add(semester);
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting overall pass rate: {}", e.getMessage());
        }
        return 0.0;
    }

    @Override
    public double getHighestMarksPercentage(Integer academicYearId, Integer semester) {
        StringBuilder sql = new StringBuilder("SELECT MAX((m.total_marks / sub.max_marks) * 100) " +
                "FROM marks m JOIN subjects sub ON m.subject_id = sub.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (academicYearId != null) {
            sql.append("AND m.academic_year_id = ? ");
            params.add(academicYearId);
        }
        if (semester != null && semester > 0) {
            sql.append("AND m.semester = ? ");
            params.add(semester);
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting highest marks percentage: {}", e.getMessage());
        }
        return 0.0;
    }

    @Override
    public double getLowestMarksPercentage(Integer academicYearId, Integer semester) {
        StringBuilder sql = new StringBuilder("SELECT MIN((m.total_marks / sub.max_marks) * 100) " +
                "FROM marks m JOIN subjects sub ON m.subject_id = sub.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (academicYearId != null) {
            sql.append("AND m.academic_year_id = ? ");
            params.add(academicYearId);
        }
        if (semester != null && semester > 0) {
            sql.append("AND m.semester = ? ");
            params.add(semester);
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting lowest marks percentage: {}", e.getMessage());
        }
        return 0.0;
    }

    @Override
    public Map<String, Integer> getGradeDistribution(Integer academicYearId, Integer semester) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("A+", 0);
        map.put("A", 0);
        map.put("B", 0);
        map.put("C", 0);
        map.put("D", 0);
        map.put("E", 0);
        map.put("F", 0);

        StringBuilder sql = new StringBuilder("SELECT grade, COUNT(*) as cnt FROM marks WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (academicYearId != null) {
            sql.append("AND academic_year_id = ? ");
            params.add(academicYearId);
        }
        if (semester != null && semester > 0) {
            sql.append("AND semester = ? ");
            params.add(semester);
        }
        sql.append("GROUP BY grade");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String g = rs.getString("grade");
                    if (map.containsKey(g)) {
                        map.put(g, rs.getInt("cnt"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching grade distribution: {}", e.getMessage());
        }
        return map;
    }

    @Override
    public Map<String, Double> getSubjectWiseAverages(Integer academicYearId, Integer semester) {
        Map<String, Double> map = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder("SELECT sub.subject_code, sub.subject_name, AVG(m.total_marks) as avg_marks " +
                "FROM marks m " +
                "JOIN subjects sub ON m.subject_id = sub.id " +
                "WHERE m.is_absent = FALSE ");
        List<Object> params = new ArrayList<>();
        if (academicYearId != null) {
            sql.append("AND m.academic_year_id = ? ");
            params.add(academicYearId);
        }
        if (semester != null && semester > 0) {
            sql.append("AND m.semester = ? ");
            params.add(semester);
        }
        sql.append("GROUP BY sub.id, sub.subject_code, sub.subject_name ORDER BY sub.subject_code ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String label = rs.getString("subject_code") + " - " + rs.getString("subject_name");
                    map.put(label, rs.getDouble("avg_marks"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching subject-wise averages: {}", e.getMessage());
        }
        return map;
    }

    @Override
    public Map<Integer, Double> getSemesterPassRates(Integer academicYearId) {
        Map<Integer, Double> map = new TreeMap<>();
        StringBuilder sql = new StringBuilder("SELECT semester, " +
                "(SUM(CASE WHEN is_passed = TRUE THEN 1 ELSE 0 END) * 100.0) / COUNT(*) as pass_rate " +
                "FROM marks WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (academicYearId != null) {
            sql.append("AND academic_year_id = ? ");
            params.add(academicYearId);
        }
        sql.append("GROUP BY semester ORDER BY semester ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (academicYearId != null) {
                ps.setInt(1, academicYearId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("semester"), rs.getDouble("pass_rate"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching semester pass rates: {}", e.getMessage());
        }
        return map;
    }

    @Override
    public List<StudentRank> getRankings(int academicYearId, int semester, String department) {
        List<StudentRank> list = new ArrayList<>();

        // Aggregate by student: calculate total obtained, max possible, percentage, and check if any subject failed
        StringBuilder sql = new StringBuilder(
                "SELECT s.id as student_id, s.roll_number, CONCAT(s.first_name, ' ', s.last_name) as full_name, " +
                "s.department, m.semester, " +
                "SUM(m.total_marks) as total_obtained, " +
                "SUM(sub.max_marks) as total_max, " +
                "(SUM(m.total_marks) / SUM(sub.max_marks)) * 100.0 as percentage, " +
                "SUM(m.grade_point * sub.credits) / SUM(sub.credits) as sgpa, " +
                "SUM(CASE WHEN m.is_passed = FALSE THEN 1 ELSE 0 END) as failed_count, " +
                "COUNT(sub.id) as subjects_count " +
                "FROM marks m " +
                "JOIN students s ON m.student_id = s.id " +
                "JOIN subjects sub ON m.subject_id = sub.id " +
                "WHERE m.academic_year_id = ? AND m.semester = ? "
        );
        List<Object> params = new ArrayList<>();
        params.add(academicYearId);
        params.add(semester);

        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All")) {
            sql.append("AND s.department = ? ");
            params.add(department.trim());
        }

        sql.append("GROUP BY s.id, s.roll_number, s.first_name, s.last_name, s.department, m.semester ");
        // Ranking policy: Passed students ranked first by percentage DESC, sgpa DESC, then failed students, rollNumber ASC
        sql.append("ORDER BY failed_count ASC, percentage DESC, sgpa DESC, s.roll_number ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                int rankCounter = 1;
                while (rs.next()) {
                    int failedCount = rs.getInt("failed_count");
                    String status = failedCount == 0 ? "PASSED" : "FAILED (" + failedCount + " Backlog)";

                    StudentRank rank = new StudentRank();
                    // Students with failed subjects do not receive top rank honors
                    rank.setRank(failedCount == 0 ? rankCounter++ : 0);
                    rank.setStudentId(rs.getInt("student_id"));
                    rank.setRollNumber(rs.getString("roll_number"));
                    rank.setFullName(rs.getString("full_name"));
                    rank.setDepartment(rs.getString("department"));
                    rank.setSemester(rs.getInt("semester"));
                    rank.setTotalMarks(rs.getDouble("total_obtained"));
                    rank.setMaxMarks(rs.getDouble("total_max"));
                    rank.setPercentage(rs.getDouble("percentage"));
                    rank.setSgpa(rs.getDouble("sgpa"));
                    rank.setStatus(status);

                    list.add(rank);
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating rankings: {}", e.getMessage());
            throw new DatabaseException("Failed to calculate class rankings", e);
        }
        return list;
    }

    @Override
    public List<StudentRank> getAtRiskStudents(Integer academicYearId, Integer semester, double passThreshold) {
        List<StudentRank> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.id as student_id, s.roll_number, CONCAT(s.first_name, ' ', s.last_name) as full_name, " +
                "s.department, m.semester, " +
                "SUM(m.total_marks) as total_obtained, " +
                "SUM(sub.max_marks) as total_max, " +
                "(SUM(m.total_marks) / SUM(sub.max_marks)) * 100.0 as percentage, " +
                "SUM(m.grade_point * sub.credits) / SUM(sub.credits) as sgpa, " +
                "SUM(CASE WHEN m.is_passed = FALSE THEN 1 ELSE 0 END) as failed_count " +
                "FROM marks m " +
                "JOIN students s ON m.student_id = s.id " +
                "JOIN subjects sub ON m.subject_id = sub.id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (academicYearId != null) {
            sql.append("AND m.academic_year_id = ? ");
            params.add(academicYearId);
        }
        if (semester != null && semester > 0) {
            sql.append("AND m.semester = ? ");
            params.add(semester);
        }

        sql.append("GROUP BY s.id, s.roll_number, s.first_name, s.last_name, s.department, m.semester ");
        // Condition for at-risk: failed_count > 0 OR percentage < 50.0
        sql.append("HAVING failed_count > 0 OR percentage < 50.0 ");
        sql.append("ORDER BY failed_count DESC, percentage ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    int failedCount = rs.getInt("failed_count");
                    StudentRank sr = new StudentRank();
                    sr.setRank(index++);
                    sr.setStudentId(rs.getInt("student_id"));
                    sr.setRollNumber(rs.getString("roll_number"));
                    sr.setFullName(rs.getString("full_name"));
                    sr.setDepartment(rs.getString("department"));
                    sr.setSemester(rs.getInt("semester"));
                    sr.setTotalMarks(rs.getDouble("total_obtained"));
                    sr.setMaxMarks(rs.getDouble("total_max"));
                    sr.setPercentage(rs.getDouble("percentage"));
                    sr.setSgpa(rs.getDouble("sgpa"));
                    sr.setStatus(failedCount > 0 ? "Failed " + failedCount + " Subject(s)" : "Low Score (<50%)");
                    list.add(sr);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching at-risk students: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public int countTotalMarks() {
        String sql = "SELECT COUNT(*) FROM marks";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting total marks records: {}", e.getMessage());
        }
        return 0;
    }
}
