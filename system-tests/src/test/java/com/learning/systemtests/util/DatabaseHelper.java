package com.learning.systemtests.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

import static com.learning.systemtests.config.TestConfig.*;

/**
 * Utility class for database operations in system tests.
 * Provides methods for verification and cleanup of test data.
 * Updated for Lite (Single-Tenant) Architecture.
 */
public class DatabaseHelper {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHelper.class);

    private DatabaseHelper() {
        // Utility class
    }

    // ============================================================
    // Connection Methods
    // ============================================================

    /**
     * Get a connection to the main database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(PLATFORM_DB_URL, DB_USER, DB_PASSWORD);
    }

    // ============================================================
    // User Verification
    // ============================================================

    /**
     * Check if a user exists in the database.
     */
    public static boolean userExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Error checking user existence: {}", e.getMessage());
            return false;
        }
    }

    // ============================================================
    // Entry Verification
    // ============================================================

    /**
     * Check if an entry exists.
     */
    public static boolean entryExists(String key) {
        String sql = "SELECT COUNT(*) FROM entries WHERE entry_key = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Error checking entry existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get row count from a table.
     */
    public static int getRowCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("Error getting row count for table {}: {}", tableName, e.getMessage());
            return -1;
        }
    }

    // ============================================================
    // Cleanup Methods
    // ============================================================

    /**
     * Clean up test user.
     */
    public static void cleanupUser(String email) {
        String sql = "DELETE FROM users WHERE email = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                log.info("Cleaned up user: {}", email);
            }
        } catch (SQLException e) {
            log.error("Error cleaning up user: {}", e.getMessage());
        }
    }

    /**
     * Clean up entries table.
     * Use with caution!
     */
    public static void cleanupEntries() {
        String sql = "TRUNCATE TABLE entries";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            log.info("Truncated entries table");
        } catch (SQLException e) {
            log.error("Error truncating entries table: {}", e.getMessage());
        }
    }
}
