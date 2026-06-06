package lims.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lims.models.User;
import lims.services.UserService;
import lims.utils.SceneManager;
import lims.utils.Session;

public class ManageUsersController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField temporaryPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TextField searchUserField;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> userIdColumn;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, Boolean> emailVerifiedColumn;

    @FXML
    private TableColumn<User, Boolean> forcePasswordChangeColumn;

    @FXML
    private TableColumn<User, String> createdAtColumn;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final UserService userService = new UserService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        setupRoleOptions();
        setupTable();
        setupSearch();
        loadUsers();
    }

    private void setupRoleOptions() {
        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser != null && "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            roleComboBox.setItems(FXCollections.observableArrayList(
                    "LAB_ATTENDANT",
                    "CUSTOMER"
            ));
        } else {
            roleComboBox.setItems(FXCollections.observableArrayList(
                    "CUSTOMER"
            ));
            roleComboBox.setValue("CUSTOMER");
        }
    }

    private void setupTable() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        emailVerifiedColumn.setCellValueFactory(new PropertyValueFactory<>("emailVerified"));
        forcePasswordChangeColumn.setCellValueFactory(new PropertyValueFactory<>("forcePasswordChange"));

        createdAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }

            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getCreatedAt().format(formatter)
            );
        });

        usersTable.setItems(userList);
    }

    private void setupSearch() {
        searchUserField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers();
        });
    }

    private void loadUsers() {
        clearMessages();

        try {
            userList.setAll(userService.getAllUsers());
        } catch (SQLException e) {
            showError("Could not load users: " + e.getMessage());
        }
    }

    private void filterUsers() {
        try {
            String keyword = searchUserField.getText() == null
                    ? ""
                    : searchUserField.getText().toLowerCase().trim();

            userList.setAll(
                    userService.getAllUsers()
                            .stream()
                            .filter(user ->
                                    keyword.isBlank()
                                            || user.getFullName().toLowerCase().contains(keyword)
                                            || user.getEmail().toLowerCase().contains(keyword)
                                            || user.getRole().toLowerCase().contains(keyword)
                            )
                            .toList()
            );

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateUser() {
        clearMessages();

        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String temporaryPassword = temporaryPasswordField.getText();
        String role = roleComboBox.getValue();

        if (fullName.isBlank() || email.isBlank() || temporaryPassword.isBlank() || role == null) {
            showError("Please fill in all fields.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        if (temporaryPassword.length() < 6) {
            showError("Temporary password must be at least 6 characters.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in staff user found.");
            return;
        }

        if ("LAB_ATTENDANT".equalsIgnoreCase(currentUser.getRole())
                && !"CUSTOMER".equalsIgnoreCase(role)) {
            showError("Lab Attendants can only create customer accounts.");
            return;
        }

        try {
            boolean created = userService.createUserByStaff(
                    fullName,
                    email,
                    temporaryPassword,
                    role,
                    currentUser.getId()
            );

            if (created) {
                showSuccess("User created successfully. They must change password on first login.");
                clearForm();
                loadUsers();
            } else {
                showError("A user with this email already exists.");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchUserField.clear();
        clearForm();
        loadUsers();
    }

    @FXML
    private void handleBack() {
        User currentUser = Session.getInstance().getCurrentUser();

        try {
            if (currentUser != null && "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                SceneManager.switchTo("/views/super-admin-dashboard.fxml", "Super Admin Dashboard");
            } else if (currentUser != null && "LAB_ATTENDANT".equalsIgnoreCase(currentUser.getRole())) {
                SceneManager.switchTo("/views/lab-attendant-dashboard.fxml", "Lab Attendant Dashboard");
            } else {
                SceneManager.switchTo("/views/login.fxml", "Sante Diagnostics LIMS");
            }
        } catch (IOException e) {
            showError("Navigation error: " + e.getMessage());
        }
    }

    private void clearForm() {
        fullNameField.clear();
        emailField.clear();
        temporaryPasswordField.clear();

        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null && "LAB_ATTENDANT".equalsIgnoreCase(currentUser.getRole())) {
            roleComboBox.setValue("CUSTOMER");
        } else {
            roleComboBox.getSelectionModel().clearSelection();
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
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