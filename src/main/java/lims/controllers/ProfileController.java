package lims.controllers;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lims.models.User;
import lims.utils.SceneManager;
import lims.utils.Session;

public class ProfileController {

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label emailVerifiedLabel;

    @FXML
    private Label createdAtLabel;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        User user = Session.getInstance().getCurrentUser();

        if (user == null) {
            setEmptyProfile();
            return;
        }

        fullNameLabel.setText(user.getFullName());
        emailLabel.setText(user.getEmail());
        roleLabel.setText(user.getRole());
        emailVerifiedLabel.setText(user.isEmailVerified() ? "Verified" : "Not verified");

        if (user.getCreatedAt() != null) {
            createdAtLabel.setText(user.getCreatedAt().format(formatter));
        } else {
            createdAtLabel.setText("—");
        }
    }

    @FXML
    private void handleBack() {
        User user = Session.getInstance().getCurrentUser();

        try {
            if (user != null && "CUSTOMER".equalsIgnoreCase(user.getRole())) {
                SceneManager.switchTo("/views/customer-dashboard.fxml", "Customer Dashboard");
            } else if (user != null && "SUPER_ADMIN".equalsIgnoreCase(user.getRole())) {
                SceneManager.switchTo("/views/super-admin-dashboard.fxml", "Super Admin Dashboard");
            } else if (user != null && "LAB_ATTENDANT".equalsIgnoreCase(user.getRole())) {
                SceneManager.switchTo("/views/lab-attendant-dashboard.fxml", "Lab Attendant Dashboard");
            } else {
                SceneManager.switchTo("/views/login.fxml", "Sante Diagnostics LIMS");
            }
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

    private void setEmptyProfile() {
        fullNameLabel.setText("—");
        emailLabel.setText("—");
        roleLabel.setText("—");
        emailVerifiedLabel.setText("—");
        createdAtLabel.setText("—");
    }
}