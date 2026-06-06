package lims.controllers;

import lims.services.EmailService;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lims.models.Result;
import lims.models.User;
import lims.services.ResultService;
import lims.utils.SceneManager;
import lims.utils.Session;

public class ResultValidationController {

    @FXML
    private TextField searchResultField;

    @FXML
    private TableView<Result> resultsTable;

    @FXML
    private TableColumn<Result, Integer> resultIdColumn;

    @FXML
    private TableColumn<Result, Integer> requestIdColumn;

    @FXML
    private TableColumn<Result, String> customerNameColumn;

    @FXML
    private TableColumn<Result, String> testNameColumn;

    @FXML
    private TableColumn<Result, String> resultFormatColumn;

    @FXML
    private TableColumn<Result, String> paymentStatusColumn;

    @FXML
    private TableColumn<Result, String> sampleStatusColumn;

    @FXML
    private TableColumn<Result, String> resultStatusColumn;

    @FXML
    private TableColumn<Result, String> uploadedAtColumn;

    @FXML
    private TableColumn<Result, String> validatedAtColumn;

    @FXML
    private TextArea resultPreviewArea;

    @FXML
    private TextArea rejectionReasonArea;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final ResultService resultService = new ResultService();
    private final EmailService emailService = new EmailService();
    private final ObservableList<Result> resultList = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        setupTable();
        setupSelection();
        setupSearch();
        loadResults();
    }

    private void setupTable() {
        resultIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        resultFormatColumn.setCellValueFactory(new PropertyValueFactory<>("resultFormat"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        sampleStatusColumn.setCellValueFactory(new PropertyValueFactory<>("sampleStatus"));
        resultStatusColumn.setCellValueFactory(new PropertyValueFactory<>("resultStatus"));

        uploadedAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUploadedAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }

            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getUploadedAt().format(formatter)
            );
        });

        validatedAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getValidatedAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }

            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getValidatedAt().format(formatter)
            );
        });

        resultsTable.setItems(resultList);
    }

    private void setupSelection() {
        resultsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedResult) -> {
                    if (selectedResult != null) {
                        showResultPreview(selectedResult);
                    }
                }
        );
    }

    private void setupSearch() {
        searchResultField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResults();
        });
    }

    private void loadResults() {
        clearMessages();

        try {
            resultList.setAll(resultService.getAllResults());
        } catch (SQLException e) {
            showError("Could not load results: " + e.getMessage());
        }
    }

    private void filterResults() {
        try {
            String keyword = searchResultField.getText() == null
                    ? ""
                    : searchResultField.getText().toLowerCase().trim();

            resultList.setAll(
                    resultService.getAllResults()
                            .stream()
                            .filter(result ->
                                    keyword.isBlank()
                                            || result.getCustomerName().toLowerCase().contains(keyword)
                                            || result.getTestName().toLowerCase().contains(keyword)
                                            || result.getResultFormat().toLowerCase().contains(keyword)
                                            || result.getPaymentStatus().toLowerCase().contains(keyword)
                                            || result.getSampleStatus().toLowerCase().contains(keyword)
                                            || result.getResultStatus().toLowerCase().contains(keyword)
                                            || String.valueOf(result.getRequestId()).contains(keyword)
                            )
                            .toList()
            );

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void showResultPreview(Result result) {
        resultPreviewArea.setText(
                "Result ID: " + result.getId() + "\n"
                        + "Request ID: " + result.getRequestId() + "\n"
                        + "Customer: " + result.getCustomerName() + "\n"
                        + "Test: " + result.getTestName() + "\n"
                        + "Format: " + result.getResultFormat() + "\n"
                        + "Payment: " + result.getPaymentStatus() + "\n"
                        + "Sample: " + result.getSampleStatus() + "\n"
                        + "Status: " + result.getResultStatus() + "\n"
                        + "Uploaded By: " + (
                                result.getUploadedByName() == null
                                        ? "Not uploaded yet"
                                        : result.getUploadedByName()
                        ) + "\n"
                        + "Validated By: " + (
                                result.getValidatedByName() == null
                                        ? "Not validated yet"
                                        : result.getValidatedByName()
                        ) + "\n"
                        + "Uploaded At: " + (
                                result.getUploadedAt() == null
                                        ? "Not uploaded yet"
                                        : result.getUploadedAt().format(formatter)
                        ) + "\n"
                        + "Validated At: " + (
                                result.getValidatedAt() == null
                                        ? "Not validated yet"
                                        : result.getValidatedAt().format(formatter)
                        ) + "\n\n"
                        + "Result Value / Notes:\n"
                        + (
                                result.getResultValue() == null || result.getResultValue().isBlank()
                                        ? "No result value entered."
                                        : result.getResultValue()
                        )
        );
    }

    @FXML
    private void handleValidateResult() {
        clearMessages();

        Result selected = resultsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a result first.");
            return;
        }

        if (!"UPLOADED".equalsIgnoreCase(selected.getResultStatus())
                && !"REJECTED".equalsIgnoreCase(selected.getResultStatus())) {
            showError("Only uploaded or rejected results can be validated.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in lab attendant found.");
            return;
        }

        try {
            boolean validated = resultService.validateResult(selected.getId(), currentUser.getId());

            if (validated) {
                try {
                    emailService.sendResultReadyEmailForResult(selected.getId());
                    showSuccess("Result validated successfully. Customer can now view it. Email notification processed.");
                } catch (SQLException emailError) {
                    showSuccess("Result validated successfully, but email notification failed: " + emailError.getMessage());
                }

                rejectionReasonArea.clear();
                loadResults();
            } else {
                showError("Result could not be validated.");
            }
        
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRejectResult() {
        clearMessages();

        Result selected = resultsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a result first.");
            return;
        }

        if (!"UPLOADED".equalsIgnoreCase(selected.getResultStatus())) {
            showError("Only uploaded results can be rejected.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in lab attendant found.");
            return;
        }

        String reason = rejectionReasonArea.getText();

        if (reason == null || reason.isBlank()) {
            showError("Please enter a rejection reason.");
            return;
        }

        try {
            boolean rejected = resultService.rejectResult(selected.getId(), currentUser.getId(), reason);

            if (rejected) {
                showSuccess("Result rejected successfully.");
                rejectionReasonArea.clear();
                loadResults();
            } else {
                showError("Result could not be rejected.");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewResult() {
        clearMessages();

        Result selected = resultsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a result first.");
            return;
        }

        showResultPreview(selected);
    }

    @FXML
    private void handleRefresh() {
        searchResultField.clear();
        resultPreviewArea.clear();
        rejectionReasonArea.clear();
        loadResults();
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.switchTo("/views/lab-attendant-dashboard.fxml", "Lab Attendant Dashboard");
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