package com.course.calculator.db;

import com.course.calculator.model.CalculationRecord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** calculation_history 增删查，全部预编译。 */
public final class HistoryRepository {
    private final DatabaseManager databaseManager;

    public HistoryRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public synchronized long insert(String expression, String result, String createdAt)
            throws SQLException {
        String sql = "INSERT INTO calculation_history (expression, result, created_at) "
                + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = databaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, expression);
            ps.setString(2, result);
            ps.setString(3, createdAt);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new SQLException("插入记录后未获取到 id");
        }
    }

    public synchronized List<CalculationRecord> findAll() throws SQLException {
        String sql = "SELECT id, expression, result, created_at "
                + "FROM calculation_history ORDER BY id DESC";
        List<CalculationRecord> records = new ArrayList<>();
        try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                records.add(new CalculationRecord(
                        rs.getLong("id"),
                        rs.getString("expression"),
                        rs.getString("result"),
                        rs.getString("created_at")));
            }
        }
        return records;
    }

    public synchronized boolean deleteById(long id) throws SQLException {
        String sql = "DELETE FROM calculation_history WHERE id = ?";
        try (PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public synchronized void deleteAll() throws SQLException {
        try (Statement statement = databaseManager.getConnection().createStatement()) {
            statement.executeUpdate("DELETE FROM calculation_history");
        }
    }
}
