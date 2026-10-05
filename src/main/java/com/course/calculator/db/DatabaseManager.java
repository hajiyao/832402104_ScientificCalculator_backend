package com.course.calculator.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** SQLite 连接，启动时建表。 */
public final class DatabaseManager {
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS calculation_history ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "expression TEXT NOT NULL, "
                    + "result TEXT NOT NULL, "
                    + "created_at TEXT NOT NULL)";

    private final Connection connection;

    public DatabaseManager(String dbPath) {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("找不到 SQLite JDBC 驱动", ex);
        }
        File dbFile = new File(dbPath);
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(CREATE_TABLE_SQL);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("数据库初始化失败", ex);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
