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

public class LabAttendantDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label pendingSamplesLabel;

    @FXML
    private Label processingSamplesLabel;

    @FXML
    private Label readyForValidationLabel;

    @FXML
    private Label validatedResultsLabel;

    @FXML
    private void initialize() {
        User user = Session.getInstance().getCurrentUser();

        if (user != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + " (Lab Attendant)");
        }

        loadDashboardStats();
    }

    private void loadDashboardStats() {
        setCountLabel(pendingSamplesLabel, """
                SELECT COUNT(*) AS total
                FROM samples
                WHERE sample_status IN ('REQUESTED', 'COLLECTED')
                """);

        setCountLabel(processingSamplesLabel, """
                SELECT COUNT(*) AS total
                FROM samples
                WHERE sample_status = 'PROCESSING'
                """);

        setCountLabel(readyForValidationLabel, """
                SELECT COUNT(*) AS total
                FROM results
                WHERE result_status = 'UPLOADED'
                """);

        setCountLabel(validatedResultsLabel, """
                SELECT COUNT(*) AS total
                FROM results
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
            System.out.println("Lab dashboard stat error: " + e.getMessage());
        }
    }

    @FXML
    private void goToLabRequestQueue() {
        try {
            SceneManager.switchTo("/views/lab-test-request-queue.fxml", "Lab Request Queue");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSampleTracking() {
        try {
            SceneManager.switchTo("/views/sample-tracking.fxml", "Sample Tracking");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToResultUpload() {
        try {
            SceneManager.switchTo("/views/result-upload.fxml", "Result Upload");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToResultValidation() {
        try {
            SceneManager.switchTo("/views/result-validation.fxml", "Result Validation");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToManageUsers() {
        try {
            SceneManager.switchTo("/views/manage-users.fxml", "Create Customer Account");
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