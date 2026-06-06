package lims.services;

import lims.services.AuditLogService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lims.database.DatabaseConnection;
import lims.models.User;
import lims.utils.PasswordUtil;

public class AuthService {
    private final AuditLogService auditLogService = new AuditLogService();
    private final EmailVerificationService emailVerificationService = new EmailVerificationService();

    public User login(String email, String password) throws SQLException {
        String sql = """
                SELECT id, full_name, email, password_hash, role, email_verified,
                       force_password_change, created_at
                FROM users
                WHERE email = ?
                """;
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim().toLowerCase());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String storedHash = resultSet.getString("password_hash");

                    if (PasswordUtil.checkPassword(password, storedHash)) {
                        return mapUser(resultSet);
                    }
                }
            }
        }

        return null;
    }

public boolean registerCustomer(String fullName, String email, String password) throws SQLException {
    if (emailExists(email)) {
        return false;
    }

    String sql = """
            INSERT INTO users (full_name, email, password_hash, role, email_verified, force_password_change)
            VALUES (?, ?, ?, 'CUSTOMER', false, false)
            RETURNING id
            """;

    String hashedPassword = PasswordUtil.hashPassword(password);
    String cleanEmail = email.trim().toLowerCase();

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, fullName.trim());
        statement.setString(2, cleanEmail);
        statement.setString(3, hashedPassword);

        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                int newUserId = resultSet.getInt("id");

                auditLogService.log(
                        newUserId,
                        "CUSTOMER_REGISTERED",
                        "USER",
                        newUserId,
                        "Customer self-registered with email: " + cleanEmail
                );

                emailVerificationService.createVerificationToken(
                        newUserId,
                        cleanEmail,
                        fullName.trim()
                );

                return true;
            }
        }
    }

    return false;
}

    public boolean changeForcedPassword(int userId, String newPassword) throws SQLException {
        String sql = """
                UPDATE users
                SET password_hash = ?, force_password_change = false
                WHERE id = ?
                """;

        String hashedPassword = PasswordUtil.hashPassword(newPassword);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, hashedPassword);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;
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

