package lims.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lims.database.DatabaseConnection;
import lims.models.User;
import lims.utils.SceneManager;
import lims.utils.Session;

public class CustomerDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label activeRequestsLabel;

    @FXML
    private Label readyResultsLabel;

    @FXML
    private Label pastResultsLabel;

    @FXML
    private Label nextResultCountdownLabel;

    @FXML
    private void initialize() {
        User user = Session.getInstance().getCurrentUser();

        if (user != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + " (Customer)");
            loadDashboardStats(user.getId());
        }
    }

    private void loadDashboardStats(int customerId) {
        loadActiveRequests(customerId);
        loadReadyResults(customerId);
        loadPastResults(customerId);
        loadNextResultCountdown(customerId);
    }

    private void loadActiveRequests(int customerId) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM test_requests
                WHERE customer_id = ?
                  AND result_status <> 'VALIDATED'
                """;

        setCountLabel(activeRequestsLabel, sql, customerId);
    }

    private void loadReadyResults(int customerId) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM test_requests
                WHERE customer_id = ?
                  AND result_status = 'VALIDATED'
                """;

        setCountLabel(readyResultsLabel, sql, customerId);
    }

    private void loadPastResults(int customerId) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM test_requests
                WHERE customer_id = ?
                  AND result_status = 'VALIDATED'
                """;

        setCountLabel(pastResultsLabel, sql, customerId);
    }

    private void loadNextResultCountdown(int customerId) {
        String sql = """
                SELECT estimated_ready_at
                FROM test_requests
                WHERE customer_id = ?
                  AND result_status <> 'VALIDATED'
                  AND estimated_ready_at IS NOT NULL
                ORDER BY estimated_ready_at ASC
                LIMIT 1
                """;

        if (nextResultCountdownLabel == null) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    LocalDateTime estimatedReadyAt = resultSet.getTimestamp("estimated_ready_at").toLocalDateTime();
                    LocalDateTime now = LocalDateTime.now();

                    if (estimatedReadyAt.isBefore(now)) {
                        nextResultCountdownLabel.setText("A result may be ready soon. Check My Results.");
                        return;
                    }

                    Duration duration = Duration.between(now, estimatedReadyAt);
                    long hours = duration.toHours();
                    long minutes = duration.toMinutesPart();

                    nextResultCountdownLabel.setText(hours + "h " + minutes + "m remaining");
                } else {
                    nextResultCountdownLabel.setText("No active request");
                }
            }

        } catch (SQLException e) {
            nextResultCountdownLabel.setText("Could not load countdown");
        }
    }

    private void setCountLabel(Label label, String sql, int customerId) {
        if (label == null) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    label.setText(String.valueOf(resultSet.getInt("total")));
                }
            }

        } catch (SQLException e) {
            label.setText("0");
        }
    }

    @FXML
    private void goToBrowseTests() {
        try {
            SceneManager.switchTo("/views/browse-tests.fxml", "Browse Tests");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToMyRequests() {
        try {
            SceneManager.switchTo("/views/my-requests.fxml", "My Requests");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCustomerResults() {
        try {
            SceneManager.switchTo("/views/customer-results.fxml", "Result Vault");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToProfile() {
        try {
            SceneManager.switchTo("/views/profile.fxml", "Customer Profile");
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