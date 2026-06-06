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
import lims.models.Sample;
import lims.models.User;
import lims.services.SampleService;
import lims.utils.SceneManager;
import lims.utils.Session;

public class SampleTrackingController {

    @FXML
    private TextField searchSampleField;

    @FXML
    private ComboBox<String> sampleStatusComboBox;

    @FXML
    private ComboBox<String> filterStatusComboBox;

    @FXML
    private TableView<Sample> samplesTable;

    @FXML
    private TableColumn<Sample, Integer> sampleIdColumn;

    @FXML
    private TableColumn<Sample, Integer> requestIdColumn;

    @FXML
    private TableColumn<Sample, String> customerNameColumn;

    @FXML
    private TableColumn<Sample, String> testNameColumn;

    @FXML
    private TableColumn<Sample, String> paymentStatusColumn;

    @FXML
    private TableColumn<Sample, String> sampleStatusColumn;

    @FXML
    private TableColumn<Sample, String> resultStatusColumn;

    @FXML
    private TableColumn<Sample, String> updatedAtColumn;

    @FXML
    private TextArea sampleDetailsArea;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final SampleService sampleService = new SampleService();
    private final ObservableList<Sample> sampleList = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        setupComboBoxes();
        setupTable();
        setupSelection();
        setupSearch();
        loadSamples();
    }

    private void setupComboBoxes() {
        sampleStatusComboBox.setItems(FXCollections.observableArrayList(
                "REQUESTED",
                "COLLECTED",
                "PROCESSING",
                "VALIDATED",
                "READY"
        ));

        filterStatusComboBox.setItems(FXCollections.observableArrayList(
                "ALL",
                "REQUESTED",
                "COLLECTED",
                "PROCESSING",
                "VALIDATED",
                "READY"
        ));

        filterStatusComboBox.setValue("ALL");
    }

    private void setupTable() {
        sampleIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        sampleStatusColumn.setCellValueFactory(new PropertyValueFactory<>("sampleStatus"));
        resultStatusColumn.setCellValueFactory(new PropertyValueFactory<>("resultStatus"));

        updatedAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUpdatedAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }

            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getUpdatedAt().format(formatter)
            );
        });

        samplesTable.setItems(sampleList);
    }

    private void setupSelection() {
        samplesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedSample) -> {
                    if (selectedSample != null) {
                        sampleStatusComboBox.setValue(selectedSample.getSampleStatus());
                        showSampleDetails(selectedSample);
                    }
                }
        );
    }

    private void setupSearch() {
        searchSampleField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSamples();
        });

        filterStatusComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterSamples();
        });
    }

    private void loadSamples() {
        clearMessages();

        try {
            sampleList.setAll(sampleService.getAllSamples());
        } catch (SQLException e) {
            showError("Could not load samples: " + e.getMessage());
        }
    }

    private void filterSamples() {
        try {
            String keyword = searchSampleField.getText() == null
                    ? ""
                    : searchSampleField.getText().toLowerCase().trim();

            String statusFilter = filterStatusComboBox.getValue();

            sampleList.setAll(
                    sampleService.getAllSamples()
                            .stream()
                            .filter(sample -> {
                                boolean matchesKeyword =
                                        keyword.isBlank()
                                                || sample.getCustomerName().toLowerCase().contains(keyword)
                                                || sample.getTestName().toLowerCase().contains(keyword)
                                                || sample.getSampleStatus().toLowerCase().contains(keyword)
                                                || sample.getPaymentStatus().toLowerCase().contains(keyword)
                                                || sample.getResultStatus().toLowerCase().contains(keyword)
                                                || String.valueOf(sample.getRequestId()).contains(keyword);

                                boolean matchesStatus =
                                        statusFilter == null
                                                || statusFilter.equals("ALL")
                                                || sample.getSampleStatus().equalsIgnoreCase(statusFilter);

                                return matchesKeyword && matchesStatus;
                            })
                            .toList()
            );

        } catch (SQLException e) {
            showError("Filter failed: " + e.getMessage());
        }
    }

    private void showSampleDetails(Sample sample) {
        sampleDetailsArea.setText(
                "Sample ID: " + sample.getId() + "\n"
                        + "Request ID: " + sample.getRequestId() + "\n"
                        + "Customer: " + sample.getCustomerName() + "\n"
                        + "Test: " + sample.getTestName() + "\n"
                        + "Payment Status: " + sample.getPaymentStatus() + "\n"
                        + "Sample Status: " + sample.getSampleStatus() + "\n"
                        + "Result Status: " + sample.getResultStatus() + "\n"
                        + "Updated By: " + (
                                sample.getUpdatedByName() == null
                                        ? "Not updated yet"
                                        : sample.getUpdatedByName()
                        ) + "\n"
                        + "Updated At: " + (
                                sample.getUpdatedAt() == null
                                        ? "Not available"
                                        : sample.getUpdatedAt().format(formatter)
                        )
        );
    }

    @FXML
    private void handleUpdateStatus() {
        clearMessages();

        Sample selected = samplesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a sample first.");
            return;
        }

        String newStatus = sampleStatusComboBox.getValue();

        if (newStatus == null || newStatus.isBlank()) {
            showError("Please select a new sample status.");
            return;
        }

        if ("UNPAID".equalsIgnoreCase(selected.getPaymentStatus())) {
            showError("This request is still UNPAID. Confirm payment before processing the sample.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in lab attendant found.");
            return;
        }

        try {
            boolean updated = sampleService.updateSampleStatus(
                    selected.getId(),
                    newStatus,
                    currentUser.getId()
            );

            if (updated) {
                showSuccess("Sample status updated successfully.");
                loadSamples();
            } else {
                showError("Sample status could not be updated.");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchSampleField.clear();
        filterStatusComboBox.setValue("ALL");
        sampleStatusComboBox.getSelectionModel().clearSelection();
        sampleDetailsArea.clear();
        loadSamples();
    }

    @FXML
    private void handleViewDetails() {
        clearMessages();

        Sample selected = samplesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a sample first.");
            return;
        }

        showSampleDetails(selected);
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