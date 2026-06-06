package lims.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lims.models.User;
import lims.utils.SceneManager;
import lims.utils.Session;

public class SuperAdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private void initialize() {
        User user = Session.getInstance().getCurrentUser();

        if (user != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + " (Super Admin)");
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