package com.workspacers.postbot;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Optional;

public final class Db implements AutoCloseable {
    private final Connection conn;

    public Db(String dbPath) {
        try {
            // SQLite will create the file if it doesn't exist
            this.conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            this.conn.setAutoCommit(true);
            initSchema();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open DB at: " + dbPath, e);
        }
    }

    private void initSchema() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS posts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at TEXT NOT NULL,
                    requested_by INTEGER NOT NULL,
                    group_message_id INTEGER,
                    content_hash TEXT NOT NULL
                )
            """);
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_posts_hash ON posts(content_hash)");
        }
    }

    public Optional<Long> findLastPostedMessageIdByHash(String contentHash) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT group_message_id FROM posts WHERE content_hash = ? ORDER BY id DESC LIMIT 1")) {
            ps.setString(1, contentHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    if (!rs.wasNull() && id > 0) return Optional.of(id);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertPost(long requestedBy, String contentHash, Long groupMessageId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO posts(created_at, requested_by, group_message_id, content_hash) VALUES (?,?,?,?)")) {
            ps.setString(1, OffsetDateTime.now().toString());
            ps.setLong(2, requestedBy);
            if (groupMessageId == null) ps.setNull(3, Types.INTEGER);
            else ps.setLong(3, groupMessageId);
            ps.setString(4, contentHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public void close() {
        try { conn.close(); } catch (Exception ignored) {}
    }
}
