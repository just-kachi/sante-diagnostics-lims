package lims.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lims.models.User;
import lims.utils.SceneManager;
import lims.utils.Session;

public class LabAttendantDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private void initialize() {
        User user = Session.getInstance().getCurrentUser();

        if (user != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + " (Lab Attendant)");
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