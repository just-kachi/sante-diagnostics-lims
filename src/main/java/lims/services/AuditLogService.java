package lims.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lims.database.DatabaseConnection;
import lims.models.AuditLog;

public class AuditLogService {

    public void log(int userId, String action, String entityType, int entityId, String description) {
        String sql = """
                INSERT INTO audit_logs (user_id, action, entity_type, entity_id, description)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (userId > 0) {
                statement.setInt(1, userId);
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }

            statement.setString(2, action);
            statement.setString(3, entityType);
            statement.setInt(4, entityId);
            statement.setString(5, description);

            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Audit log failed: " + e.getMessage());
        }
    }

    public List<AuditLog> getAllLogs() throws SQLException {
        String sql = """
                SELECT
                    al.id,
                    al.user_id,
                    COALESCE(u.email, 'SYSTEM') AS user_email,
                    COALESCE(u.role, 'SYSTEM') AS user_role,
                    al.action,
                    al.entity_type,
                    al.entity_id,
                    al.description,
                    al.created_at
                FROM audit_logs al
                LEFT JOIN users u ON al.user_id = u.id
                ORDER BY al.created_at DESC
                """;

        List<AuditLog> logs = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                logs.add(mapAuditLog(resultSet));
            }
        }

        return logs;
    }

    private AuditLog mapAuditLog(ResultSet resultSet) throws SQLException {
        return new AuditLog(
                resultSet.getInt("id"),
                resultSet.getInt("user_id"),
                resultSet.getString("user_email"),
                resultSet.getString("user_role"),
                resultSet.getString("action"),
                resultSet.getString("entity_type"),
                resultSet.getInt("entity_id"),
                resultSet.getString("description"),
                resultSet.getTimestamp("created_at") == null
                        ? null
                        : resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }
}