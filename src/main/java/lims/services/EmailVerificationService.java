package lims.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import lims.database.DatabaseConnection;

public class EmailVerificationService {

    private final EmailService emailService = new EmailService();
    private final AuditLogService auditLogService = new AuditLogService();

    public String createVerificationToken(int userId, String email, String fullName) throws SQLException {
        String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String expireOldTokensSql = """
                UPDATE email_verification_tokens
                SET used = true
                WHERE user_id = ? AND used = false
                """;

        String insertTokenSql = """
                INSERT INTO email_verification_tokens (user_id, token, expires_at, used)
                VALUES (?, ?, ?, false)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement expireStatement = connection.prepareStatement(expireOldTokensSql)) {
                    expireStatement.setInt(1, userId);
                    expireStatement.executeUpdate();
                }

                try (PreparedStatement insertStatement = connection.prepareStatement(insertTokenSql)) {
                    insertStatement.setInt(1, userId);
                    insertStatement.setString(2, token);
                    insertStatement.setObject(3, LocalDateTime.now().plusHours(24));
                    insertStatement.executeUpdate();
                }

                connection.commit();

                sendVerificationEmail(email, fullName, token);

                auditLogService.log(
                        userId,
                        "EMAIL_VERIFICATION_TOKEN_CREATED",
                        "USER",
                        userId,
                        "Email verification token created for: " + email
                );

                return token;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean verifyEmail(int userId, String token) throws SQLException {
        String findTokenSql = """
                SELECT id
                FROM email_verification_tokens
                WHERE user_id = ?
                  AND token = ?
                  AND used = false
                  AND expires_at > CURRENT_TIMESTAMP
                """;

        String updateTokenSql = """
                UPDATE email_verification_tokens
                SET used = true
                WHERE user_id = ? AND token = ?
                """;

        String updateUserSql = """
                UPDATE users
                SET email_verified = true
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                boolean validToken = false;

                try (PreparedStatement findStatement = connection.prepareStatement(findTokenSql)) {
                    findStatement.setInt(1, userId);
                    findStatement.setString(2, token.trim().toUpperCase());

                    try (ResultSet resultSet = findStatement.executeQuery()) {
                        validToken = resultSet.next();
                    }
                }

                if (!validToken) {
                    connection.rollback();
                    return false;
                }

                try (PreparedStatement tokenStatement = connection.prepareStatement(updateTokenSql)) {
                    tokenStatement.setInt(1, userId);
                    tokenStatement.setString(2, token.trim().toUpperCase());
                    tokenStatement.executeUpdate();
                }

                try (PreparedStatement userStatement = connection.prepareStatement(updateUserSql)) {
                    userStatement.setInt(1, userId);
                    userStatement.executeUpdate();
                }

                connection.commit();

                auditLogService.log(
                        userId,
                        "EMAIL_VERIFIED",
                        "USER",
                        userId,
                        "User verified email successfully."
                );

                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void sendVerificationEmail(String email, String fullName, String token) {
        String subject = "Verify Your Sante Diagnostics Account";

        String body = """
                Hello %s,

                Welcome to Sante Diagnostics LIMS.

                Your email verification code is:

                %s

                This code expires in 24 hours.

                Enter this code in the application to verify your account.

                Regards,
                Sante Diagnostics Ltd
                """.formatted(fullName, token);

        emailService.sendEmail(email, subject, body);
    }
}