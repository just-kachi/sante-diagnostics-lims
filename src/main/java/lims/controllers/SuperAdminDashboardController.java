package lims.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lims.database.DatabaseConnection;
import lims.models.User;
import lims.utils.SceneManager;
import lims.utils.Session;

public class SuperAdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label pendingRequestsLabel;

    @FXML
    private Label paidRequestsLabel;

    @FXML
    private Label validatedResultsLabel;

    @FXML
    private void initialize() {
        User user = Session.getInstance().getCurrentUser();

        if (user != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + " (Super Admin)");
        }

        loadDashboardStats();
    }

    private void loadDashboardStats() {
        setCountLabel(totalUsersLabel, """
                SELECT COUNT(*) AS total
                FROM users
                """);

        setCountLabel(pendingRequestsLabel, """
                SELECT COUNT(*) AS total
                FROM test_requests
                WHERE result_status <> 'VALIDATED'
                """);

        setCountLabel(paidRequestsLabel, """
                SELECT COUNT(*) AS total
                FROM test_requests
                WHERE payment_status = 'PAID'
                """);

        setCountLabel(validatedResultsLabel, """
                SELECT COUNT(*) AS total
                FROM test_requests
                WHERE result_status = 'VALIDATED'
                """);
    }

    private void setCountLabel(Label label, String sql) {
        if (label == null) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                label.setText(String.valueOf(resultSet.getInt("total")));
            }

        } catch (SQLException e) {
            label.setText("0");
        }
    }

    @FXML
    private void goToManageTestTypes() {
        try {
            SceneManager.switchTo("/views/manage-test-types.fxml", "Manage Test Types");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAdminRequestQueue() {
        try {
            SceneManager.switchTo("/views/admin-test-request-queue.fxml", "Test Request Queue");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToManageUsers() {
        try {
            SceneManager.switchTo("/views/manage-users.fxml", "Manage Users");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAuditTrail() {
        try {
            SceneManager.switchTo("/views/audit-trail.fxml", "Audit Trail");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Session.getInstance().clear();
            SceneManager.switchTo("/views/login.fxml", "Sante Diagnostics LIMS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}