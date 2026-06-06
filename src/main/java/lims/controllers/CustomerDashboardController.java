package lims.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lims.models.User;
import lims.utils.SceneManager;
import lims.utils.Session;

public class CustomerDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private void initialize() {
        User user = Session.getInstance().getCurrentUser();

        if (user != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + " (Customer)");
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
    private void handleLogout() {
        try {
            Session.getInstance().clear();
            SceneManager.switchTo("/views/login.fxml", "Sante Diagnostics LIMS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}