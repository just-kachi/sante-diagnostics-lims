package lims.controllers;

import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import lims.models.User;
import lims.services.AuthService;
import lims.utils.SceneManager;
import lims.utils.Session;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = showPasswordCheckBox != null && showPasswordCheckBox.isSelected()
                ? visiblePasswordField.getText()
                : passwordField.getText();

        clearMessages();

        if (email.isBlank() || password.isBlank()) {
            showError("Please enter both email and password.");
            return;
        }

        try {
            User user = authService.login(email, password);

            if (user == null) {
                showError("Invalid email or password.");
                return;
            }

           Session.getInstance().setCurrentUser(user);

            if (user.isForcePasswordChange()) {
                SceneManager.switchTo("/views/force-password-change.fxml", "Change Password");
                return;
            }

            if ("CUSTOMER".equalsIgnoreCase(user.getRole()) && !user.isEmailVerified()) {
                SceneManager.switchTo("/views/verify-email.fxml", "Verify Email");
                return;
            }

            routeUserByRole(user);

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (IOException e) {
            showError("Screen loading error: " + e.getMessage());
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        if (showPasswordCheckBox.isSelected()) {
            visiblePasswordField.setText(passwordField.getText());

            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(visiblePasswordField.getText());

            passwordField.setVisible(true);
            passwordField.setManaged(true);

            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
        }
    }


    @FXML
    private void goToRegister() {
        try {
            SceneManager.switchTo("/views/register.fxml", "Customer Registration");
        } catch (IOException e) {
            showAlert("Navigation Error", e.getMessage(), Alert.AlertType.ERROR);
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

    private void clearMessages() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        } else {
            showAlert("Error", message, Alert.AlertType.ERROR);
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