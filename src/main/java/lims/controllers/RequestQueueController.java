package lims.controllers;

import java.io.IOException;
import java.math.BigDecimal;
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
import lims.models.TestRequest;
import lims.models.User;
import lims.services.PaymentService;
import lims.services.TestRequestService;
import lims.utils.SceneManager;
import lims.utils.Session;

public class RequestQueueController {

    @FXML
    private Label pageTitleLabel;

    @FXML
    private TextField searchRequestField;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> paymentFilterComboBox;

    @FXML
    private TableView<TestRequest> requestsTable;

    @FXML
    private TableColumn<TestRequest, Integer> requestIdColumn;

    @FXML
    private TableColumn<TestRequest, String> customerNameColumn;

    @FXML
    private TableColumn<TestRequest, String> testNameColumn;

    @FXML
    private TableColumn<TestRequest, BigDecimal> priceColumn;

    @FXML
    private TableColumn<TestRequest, String> requestDateColumn;

    @FXML
    private TableColumn<TestRequest, String> sampleStatusColumn;

    @FXML
    private TableColumn<TestRequest, String> paymentStatusColumn;

    @FXML
    private TableColumn<TestRequest, String> resultStatusColumn;

    @FXML
    private TextArea requestDetailsArea;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final TestRequestService testRequestService = new TestRequestService();
    private final PaymentService paymentService = new PaymentService();
    private final ObservableList<TestRequest> requestList = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        setupFilters();
        setupTable();
        setupSelection();
        setupSearch();
        setupPageTitle();
        loadRequests();
    }

    private void setupPageTitle() {
        User user = Session.getInstance().getCurrentUser();

        if (user == null || pageTitleLabel == null) {
            return;
        }

        if ("SUPER_ADMIN".equalsIgnoreCase(user.getRole())) {
            pageTitleLabel.setText("Super Admin Test Request Queue");
        } else if ("LAB_ATTENDANT".equalsIgnoreCase(user.getRole())) {
            pageTitleLabel.setText("Lab Attendant Test Request Queue");
        }
    }

    private void setupFilters() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL",
                "ACTIVE",
                "COMPLETED",
                "CANCELLED",
                "REQUESTED",
                "COLLECTED",
                "PROCESSING",
                "VALIDATED",
                "READY",
                "PENDING",
                "UPLOADED",
                "REJECTED"
        ));
        statusFilterComboBox.setValue("ALL");

        paymentFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL",
                "UNPAID",
                "PAID"
        ));
        paymentFilterComboBox.setValue("ALL");
    }

    private void setupTable() {
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        sampleStatusColumn.setCellValueFactory(new PropertyValueFactory<>("sampleStatus"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        resultStatusColumn.setCellValueFactory(new PropertyValueFactory<>("resultStatus"));

        requestDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getRequestedAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }

            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getRequestedAt().format(formatter)
            );
        });

        requestsTable.setItems(requestList);
    }

    private void setupSelection() {
        requestsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedRequest) -> {
                    if (selectedRequest != null) {
                        showRequestDetails(selectedRequest);
                    }
                }
        );
    }

    private void setupSearch() {
        searchRequestField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterRequests();
        });

        statusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterRequests();
        });

        paymentFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterRequests();
        });
    }

    private void loadRequests() {
        clearMessages();

        try {
            requestList.setAll(testRequestService.getAllRequests());
        } catch (SQLException e) {
            showError("Could not load requests: " + e.getMessage());
        }
    }

    private void filterRequests() {
        try {
            String keyword = searchRequestField.getText() == null
                    ? ""
                    : searchRequestField.getText().toLowerCase().trim();

            String statusFilter = statusFilterComboBox.getValue();
            String paymentFilter = paymentFilterComboBox.getValue();

            requestList.setAll(
                    testRequestService.getAllRequests()
                            .stream()
                            .filter(request -> {
                                boolean matchesKeyword =
                                        keyword.isBlank()
                                                || request.getCustomerName().toLowerCase().contains(keyword)
                                                || request.getTestName().toLowerCase().contains(keyword)
                                                || request.getTestCategory().toLowerCase().contains(keyword)
                                                || request.getPaymentStatus().toLowerCase().contains(keyword)
                                                || request.getSampleStatus().toLowerCase().contains(keyword)
                                                || request.getResultStatus().toLowerCase().contains(keyword);

                                boolean matchesStatus =
                                        statusFilter == null
                                                || statusFilter.equals("ALL")
                                                || request.getRequestStatus().equalsIgnoreCase(statusFilter)
                                                || request.getSampleStatus().equalsIgnoreCase(statusFilter)
                                                || request.getResultStatus().equalsIgnoreCase(statusFilter);

                                boolean matchesPayment =
                                        paymentFilter == null
                                                || paymentFilter.equals("ALL")
                                                || request.getPaymentStatus().equalsIgnoreCase(paymentFilter);

                                return matchesKeyword && matchesStatus && matchesPayment;
                            })
                            .toList()
            );

        } catch (SQLException e) {
            showError("Filter failed: " + e.getMessage());
        }
    }

    private void showRequestDetails(TestRequest request) {
        requestDetailsArea.setText(
                "Request ID: " + request.getId() + "\n"
                        + "Customer: " + request.getCustomerName() + "\n"
                        + "Test: " + request.getTestName() + "\n"
                        + "Category: " + request.getTestCategory() + "\n"
                        + "Price: ₦" + request.getPrice() + "\n"
                        + "Result Format: " + request.getResultFormat() + "\n"
                        + "Payment Status: " + request.getPaymentStatus() + "\n"
                        + "Sample Status: " + request.getSampleStatus() + "\n"
                        + "Result Status: " + request.getResultStatus() + "\n"
                        + "Requested At: " + (
                                request.getRequestedAt() == null
                                        ? "Not available"
                                        : request.getRequestedAt().format(formatter)
                        ) + "\n"
                        + "Estimated Ready At: " + (
                                request.getEstimatedReadyAt() == null
                                        ? "Not available"
                                        : request.getEstimatedReadyAt().format(formatter)
                        )
        );
    }

    @FXML
    private void handleMarkPaid() {
        clearMessages();

        TestRequest selected = requestsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a request first.");
            return;
        }

        if ("PAID".equalsIgnoreCase(selected.getPaymentStatus())) {
            showError("This request is already marked as PAID.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in staff user found.");
            return;
        }

        try {
            boolean marked = paymentService.markRequestAsPaid(selected.getId(), currentUser.getId());

            if (marked) {
                showSuccess("Payment marked as PAID successfully.");
                loadRequests();
            } else {
                showError("Payment could not be marked as PAID.");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchRequestField.clear();
        statusFilterComboBox.setValue("ALL");
        paymentFilterComboBox.setValue("ALL");
        requestDetailsArea.clear();
        loadRequests();
    }

    @FXML
    private void handleViewDetails() {
        clearMessages();

        TestRequest selected = requestsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a request first.");
            return;
        }

        showRequestDetails(selected);
    }

    @FXML
    private void handleBack() {
        User user = Session.getInstance().getCurrentUser();

        try {
            if (user != null && "SUPER_ADMIN".equalsIgnoreCase(user.getRole())) {
                SceneManager.switchTo("/views/super-admin-dashboard.fxml", "Super Admin Dashboard");
            } else if (user != null && "LAB_ATTENDANT".equalsIgnoreCase(user.getRole())) {
                SceneManager.switchTo("/views/lab-attendant-dashboard.fxml", "Lab Attendant Dashboard");
            } else {
                SceneManager.switchTo("/views/login.fxml", "Sante Diagnostics LIMS");
            }
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