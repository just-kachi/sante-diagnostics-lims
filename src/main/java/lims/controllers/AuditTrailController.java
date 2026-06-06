package lims.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lims.models.AuditLog;
import lims.services.AuditLogService;
import lims.utils.SceneManager;

public class AuditTrailController {

    @FXML
    private TextField searchAuditField;

    @FXML
    private ComboBox<String> actionFilterComboBox;

    @FXML
    private TableView<AuditLog> auditTable;

    @FXML
    private TableColumn<AuditLog, Integer> auditIdColumn;

    @FXML
    private TableColumn<AuditLog, String> userEmailColumn;

    @FXML
    private TableColumn<AuditLog, String> roleColumn;

    @FXML
    private TableColumn<AuditLog, String> actionColumn;

    @FXML
    private TableColumn<AuditLog, String> entityTypeColumn;

    @FXML
    private TableColumn<AuditLog, Integer> entityIdColumn;

    @FXML
    private TableColumn<AuditLog, String> createdAtColumn;

    @FXML
    private TextArea detailsArea;

    @FXML
    private Label errorLabel;

    private final AuditLogService auditLogService = new AuditLogService();
    private final ObservableList<AuditLog> auditList = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private void initialize() {
        setupFilters();
        setupTable();
        setupSelection();
        setupSearch();
        loadLogs();
    }

    private void setupFilters() {
        actionFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL",
                "LOGIN_SUCCESS",
                "CUSTOMER_REGISTERED",
                "TEST_TYPE_CREATED",
                "TEST_TYPE_UPDATED",
                "TEST_TYPE_DEACTIVATED",
                "TEST_REQUEST_CREATED",
                "PAYMENT_MARKED_PAID",
                "SAMPLE_STATUS_UPDATED",
                "RESULT_UPLOADED",
                "RESULT_VALIDATED",
                "RESULT_REJECTED"
        ));

        actionFilterComboBox.setValue("ALL");
    }

    private void setupTable() {
        auditIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("userRole"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        entityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        entityIdColumn.setCellValueFactory(new PropertyValueFactory<>("entityId"));

        createdAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }

            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getCreatedAt().format(formatter)
            );
        });

        auditTable.setItems(auditList);
    }

    private void setupSelection() {
        auditTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedLog) -> {
                    if (selectedLog != null) {
                        showDetails(selectedLog);
                    }
                }
        );
    }

    private void setupSearch() {
        searchAuditField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterLogs();
        });

        actionFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterLogs();
        });
    }

    private void loadLogs() {
        clearError();

        try {
            auditList.setAll(auditLogService.getAllLogs());
        } catch (SQLException e) {
            showError("Could not load audit logs: " + e.getMessage());
        }
    }

    private void filterLogs() {
        try {
            String keyword = searchAuditField.getText() == null
                    ? ""
                    : searchAuditField.getText().toLowerCase().trim();

            String selectedAction = actionFilterComboBox.getValue();

            auditList.setAll(
                    auditLogService.getAllLogs()
                            .stream()
                            .filter(log -> {
                                boolean matchesKeyword =
                                        keyword.isBlank()
                                                || log.getUserEmail().toLowerCase().contains(keyword)
                                                || log.getUserRole().toLowerCase().contains(keyword)
                                                || log.getAction().toLowerCase().contains(keyword)
                                                || log.getEntityType().toLowerCase().contains(keyword)
                                                || log.getDescription().toLowerCase().contains(keyword)
                                                || String.valueOf(log.getEntityId()).contains(keyword);

                                boolean matchesAction =
                                        selectedAction == null
                                                || selectedAction.equals("ALL")
                                                || log.getAction().equalsIgnoreCase(selectedAction);

                                return matchesKeyword && matchesAction;
                            })
                            .toList()
            );

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void showDetails(AuditLog log) {
        detailsArea.setText(
                "Audit ID: " + log.getId() + "\n"
                        + "User: " + log.getUserEmail() + "\n"
                        + "Role: " + log.getUserRole() + "\n"
                        + "Action: " + log.getAction() + "\n"
                        + "Entity Type: " + log.getEntityType() + "\n"
                        + "Entity ID: " + log.getEntityId() + "\n"
                        + "Time: " + (
                                log.getCreatedAt() == null
                                        ? "Not available"
                                        : log.getCreatedAt().format(formatter)
                        ) + "\n\n"
                        + "Description:\n" + log.getDescription()
        );
    }

    @FXML
    private void handleRefresh() {
        searchAuditField.clear();
        actionFilterComboBox.setValue("ALL");
        detailsArea.clear();
        loadLogs();
    }

    @FXML
    private void handleViewDetails() {
        clearError();

        AuditLog selected = auditTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select an audit log first.");
            return;
        }

        showDetails(selected);
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.switchTo("/views/super-admin-dashboard.fxml", "Super Admin Dashboard");
        } catch (IOException e) {
            showError("Navigation error: " + e.getMessage());
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
}