package lims.controllers;

import java.io.File;
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
import javafx.stage.FileChooser;
import lims.models.Result;
import lims.models.User;
import lims.services.ResultService;
import lims.utils.SceneManager;
import lims.utils.Session;

public class ResultUploadController {

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
    private TextArea resultValueArea;

    @FXML
    private Label selectedFileLabel;

    @FXML
    private Label resultDetailsLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final ResultService resultService = new ResultService();
    private final ObservableList<Result> resultList = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private File selectedFile;
    private Result selectedResult;

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

        resultsTable.setItems(resultList);
    }

    private void setupSelection() {
        resultsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> {
                    if (selected != null) {
                        selectedResult = selected;
                        showResultDetails(selected);

                        resultValueArea.setText(
                                selected.getResultValue() == null
                                        ? ""
                                        : selected.getResultValue()
                        );
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

    private void showResultDetails(Result result) {
        resultDetailsLabel.setText(
                "Result ID: " + result.getId()
                        + "\nRequest ID: " + result.getRequestId()
                        + "\nCustomer: " + result.getCustomerName()
                        + "\nTest: " + result.getTestName()
                        + "\nFormat: " + result.getResultFormat()
                        + "\nPayment: " + result.getPaymentStatus()
                        + "\nSample: " + result.getSampleStatus()
                        + "\nStatus: " + result.getResultStatus()
        );
    }

    @FXML
    private void handleChooseFile() {
        clearMessages();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Result File");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(SceneManager.getPrimaryStage());

        if (file != null) {
            selectedFile = file;
            selectedFileLabel.setText("Selected: " + file.getName());

            if (selectedResult != null) {
                showSuccess("File selected. It will attach to Result ID " + selectedResult.getId() + " when you click Upload Result.");
            } else {
                showSuccess("File selected. Now select the result row you want to attach it to.");
            }
        }
    }

    @FXML
    private void handleUploadResult() {
        clearMessages();

        Result tableSelected = resultsTable.getSelectionModel().getSelectedItem();

        if (tableSelected != null) {
            selectedResult = tableSelected;
        }

        Result selected = selectedResult;

        if (selected == null) {
            showError("Please select the result row you want this file/result value to attach to.");
            return;
        }

        if ("UNPAID".equalsIgnoreCase(selected.getPaymentStatus())) {
            showError("This request is unpaid. Payment must be confirmed before result upload.");
            return;
        }

        if (!"READY".equalsIgnoreCase(selected.getSampleStatus())
                && !"VALIDATED".equalsIgnoreCase(selected.getSampleStatus())) {
            showError("Sample must be READY or VALIDATED before uploading result.");
            return;
        }

        String resultValue = resultValueArea.getText().trim();

        if (resultValue.isBlank() && selectedFile == null) {
            showError("Enter a result value or choose a PDF/image file.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in lab attendant found.");
            return;
        }

        try {
            boolean uploaded = resultService.uploadResult(
                    selected.getId(),
                    resultValue,
                    selectedFile,
                    currentUser.getId()
            );

            if (uploaded) {
                showSuccess("Result uploaded successfully. It is now awaiting validation.");
                handleClear();
                loadResults();
            } else {
                showError("Result could not be uploaded.");
            }

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (IOException e) {
            showError("File upload error: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        resultValueArea.clear();
        selectedFile = null;
        selectedResult = null;
        selectedFileLabel.setText("No file selected");
        resultDetailsLabel.setText("Select a result to view details.");
        resultsTable.getSelectionModel().clearSelection();
        clearMessages();
    }

    @FXML
    private void handleRefresh() {
        searchResultField.clear();
        handleClear();
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