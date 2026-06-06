package lims.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lims.database.DatabaseConnection;
import lims.models.User;
import lims.utils.PasswordUtil;

public class UserService {

    private final AuditLogService auditLogService = new AuditLogService();

    public List<User> getAllUsers() throws SQLException {
        String sql = """
                SELECT id, full_name, email, password_hash, role,
                       email_verified, force_password_change, created_at
                FROM users
                ORDER BY created_at DESC
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }

        return users;
    }

    public boolean createUserByStaff(String fullName, String email, String temporaryPassword,
                                     String role, int createdByUserId) throws SQLException {
        if (emailExists(email)) {
            return false;
        }

        String sql = """
                INSERT INTO users (
                    full_name,
                    email,
                    password_hash,
                    role,
                    email_verified,
                    force_password_change
                )
                VALUES (?, ?, ?, ?, true, true)
                """;

        String hashedPassword = PasswordUtil.hashPassword(temporaryPassword);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fullName.trim());
            statement.setString(2, email.trim().toLowerCase());
            statement.setString(3, hashedPassword);
            statement.setString(4, role);

            boolean created = statement.executeUpdate() > 0;

            if (created) {
                auditLogService.log(
                        createdByUserId,
                        "USER_CREATED",
                        "USER",
                        0,
                        "Staff-created user account. Email: " + email.trim().toLowerCase() + ", Role: " + role
                );
            }

            return created;
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim().toLowerCase());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getString("role"),
                resultSet.getBoolean("email_verified"),
                resultSet.getBoolean("force_password_change"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }
}