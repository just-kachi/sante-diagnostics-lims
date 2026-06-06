package lims.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lims.database.DatabaseConnection;

public class DatabaseSeeder {

    public static void main(String[] args) {
        try {
            if (DatabaseConnection.testConnection()) {
                System.out.println("Database connection successful.");
            } else {
                System.out.println("Database connection failed.");
                return;
            }

            createSuperAdminIfNotExists();

        } catch (SQLException e) {
            System.err.println("Seeder failed: " + e.getMessage());
        }
    }

    private static void createSuperAdminIfNotExists() throws SQLException {
        String email = "admin@sante.com";
        String password = "admin123";
        String fullName = "System Super Admin";

        if (userExists(email)) {
            System.out.println("Super Admin already exists: " + email);
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);

        String sql = """
                INSERT INTO users (
                    full_name,
                    email,
                    password_hash,
                    role,
                    email_verified,
                    force_password_change
                )
                VALUES (?, ?, ?, 'SUPER_ADMIN', true, false)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, hashedPassword);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Super Admin created successfully.");
                System.out.println("Email: " + email);
                System.out.println("Temporary Password: " + password);
            } else {
                System.out.println("Super Admin was not created.");
            }
        }
    }

    private static boolean userExists(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}