package lims.controllers;

import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import lims.models.User;
import lims.services.AuthService;
import lims.utils.SceneManager;
import lims.utils.Session;

public class ForcePasswordChangeController {

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleChangePassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        clearError();

        if (newPassword.isBlank() || confirmPassword.isBlank()) {
            showError("Please fill in both password fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (newPassword.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in user found.");
            return;
        }

        try {
            boolean changed = authService.changeForcedPassword(currentUser.getId(), newPassword);

            if (changed) {
                currentUser.setForcePasswordChange(false);
                routeUserByRole(currentUser);
            } else {
                showError("Password could not be changed.");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (IOException e) {
            showError("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Session.getInstance().clear();
            SceneManager.switchTo("/views/login.fxml", "Sante Diagnostics LIMS");
        } catch (IOException e) {
            showAlert("Logout Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void routeUserByRole(User user) throws IOException {
        String role = user.getRole();

        switch (role) {
            case "SUPER_ADMIN" ->
                SceneManager.switchTo("/views/super-admin-dashboard.fxml", "Super Admin Dashboard");

            case "LAB_ATTENDANT" ->
                SceneManager.switchTo("/views/lab-attendant-dashboard.fxml", "Lab Attendant Dashboard");

            case "CUSTOMER" ->
                SceneManager.switchTo("/views/customer-dashboard.fxml", "Customer Dashboard");

            default ->
                showError("Unknown user role: " + role);
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}