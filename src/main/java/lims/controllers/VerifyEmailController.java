package lims.controllers;

import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lims.models.User;
import lims.services.EmailVerificationService;
import lims.utils.SceneManager;
import lims.utils.Session;

public class VerifyEmailController {

    @FXML
    private Label instructionLabel;

    @FXML
    private TextField tokenField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final EmailVerificationService emailVerificationService = new EmailVerificationService();

    @FXML
    private void initialize() {
        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser != null && instructionLabel != null) {
            instructionLabel.setText(
                    "A verification code was sent/printed for: " + currentUser.getEmail()
            );
        }
    }

    @FXML
    private void handleVerifyEmail() {
        clearMessages();

        String token = tokenField.getText().trim();

        if (token.isBlank()) {
            showError("Please enter your verification code.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in user found.");
            return;
        }

        try {
            boolean verified = emailVerificationService.verifyEmail(currentUser.getId(), token);

            if (verified) {
                currentUser.setEmailVerified(true);
                showSuccess("Email verified successfully.");

                SceneManager.switchTo("/views/customer-dashboard.fxml", "Customer Dashboard");
            } else {
                showError("Invalid or expired verification code.");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (IOException e) {
            showError("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void handleResendCode() {
        clearMessages();

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in user found.");
            return;
        }

        try {
            emailVerificationService.createVerificationToken(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getFullName()
            );

            showSuccess("A new verification code has been sent/printed.");

        } catch (SQLException e) {
            showError("Could not resend verification code: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Session.getInstance().clear();
            SceneManager.switchTo("/views/login.fxml", "Sante Diagnostics LIMS");
        } catch (IOException e) {
            showError("Navigation error: " + e.getMessage());
        }
    }

    private void clearMessages() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }

        if (successLabel != null) {
            successLabel.setText("");
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    private void showSuccess(String message) {
        if (successLabel != null) {
            successLabel.setText(message);
        }
    }
}