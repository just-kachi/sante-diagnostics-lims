package lims.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import lims.models.TestRequest;
import lims.models.User;
import lims.services.TestRequestService;
import lims.utils.PaymentContext;
import lims.utils.SceneManager;
import lims.utils.Session;

public class MyRequestsController {

    @FXML
    private TextField searchRequestField;

    @FXML
    private ComboBox<String> requestStatusFilterComboBox;

    @FXML
    private TableView<TestRequest> myRequestsTable;

    @FXML
    private TableColumn<TestRequest, Integer> requestIdColumn;

    @FXML
    private TableColumn<TestRequest, String> testNameColumn;

    @FXML
    private TableColumn<TestRequest, String> requestDateColumn;

    @FXML
    private TableColumn<TestRequest, String> paymentStatusColumn;

    @FXML
    private TableColumn<TestRequest, String> sampleStatusColumn;

    @FXML
    private TableColumn<TestRequest, String> resultStatusColumn;

    @FXML
    private TableColumn<TestRequest, String> estimatedReadyAtColumn;

    @FXML
    private TextArea requestDetailsArea;

    @FXML
    private Label countdownLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final TestRequestService testRequestService = new TestRequestService();
    private final ObservableList<TestRequest> requestList = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Drives the live countdown so the label refreshes every second
    // without requiring user interaction.
    private Timeline countdownTimeline;

    @FXML
    private void initialize() {
        setupFilters();
        setupTable();
        setupSelection();
        setupSearch();
        loadRequests();
        startCountdownTimer();
    }

    /**
     * Starts a JavaFX Timeline that refreshes the countdown label every second
     * for the currently selected request. The timeline is also responsible for
     * tearing itself down when the scene window is closed so it does not leak.
     */
    private void startCountdownTimer() {
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            TestRequest selected = myRequestsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateCountdown(selected);
            }
        }));
        countdownTimeline.setCycleCount(Animation.INDEFINITE);
        countdownTimeline.play();

        // Attach a one-shot listener that stops the timer cleanly when the
        // window hosting this view is closed, so the screen exits without
        // leaving a background task behind.
        if (countdownLabel != null) {
            countdownLabel.sceneProperty().addListener((sceneObs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((winObs, oldWin, newWin) -> {
                        if (newWin != null) {
                            newWin.setOnHidden(e -> stopCountdownTimer());
                        }
                    });
                }
            });
        }
    }

    private void stopCountdownTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
    }

    private void setupFilters() {
        requestStatusFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL",
                "ACTIVE",
                "COMPLETED",
                "CANCELLED",
                "UNPAID",
                "PAID"
        ));

        requestStatusFilterComboBox.setValue("ALL");
    }

    private void setupTable() {
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        sampleStatusColumn.setCellValueFactory(new PropertyValueFactory<>("sampleStatus"));
        resultStatusColumn.setCellValueFactory(new PropertyValueFactory<>("resultStatus"));

        requestDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getRequestedAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }

            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getRequestedAt().format(formatter)
            );
        });

        estimatedReadyAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEstimatedReadyAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("Not available");
            }

            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getEstimatedReadyAt().format(formatter)
            );
        });

        myRequestsTable.setItems(requestList);
    }

    private void setupSelection() {
        myRequestsTable.getSelectionModel().selectedItemProperty().addListener(
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

        requestStatusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterRequests();
        });
    }

    private void loadRequests() {
        clearMessages();

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in customer found.");
            return;
        }

        try {
            requestList.setAll(testRequestService.getCustomerRequests(currentUser.getId()));
        } catch (SQLException e) {
            showError("Could not load requests: " + e.getMessage());
        }
    }

    private void filterRequests() {
        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            return;
        }

        try {
            String keyword = searchRequestField.getText() == null
                    ? ""
                    : searchRequestField.getText().toLowerCase().trim();

            String selectedFilter = requestStatusFilterComboBox.getValue();

            requestList.setAll(
                    testRequestService.getCustomerRequests(currentUser.getId())
                            .stream()
                            .filter(request -> {
                                boolean matchesKeyword =
                                        keyword.isBlank()
                                                || request.getTestName().toLowerCase().contains(keyword)
                                                || request.getTestCategory().toLowerCase().contains(keyword)
                                                || request.getPaymentStatus().toLowerCase().contains(keyword)
                                                || request.getSampleStatus().toLowerCase().contains(keyword)
                                                || request.getResultStatus().toLowerCase().contains(keyword);

                                boolean matchesFilter =
                                        selectedFilter == null
                                                || selectedFilter.equals("ALL")
                                                || request.getRequestStatus().equalsIgnoreCase(selectedFilter)
                                                || request.getPaymentStatus().equalsIgnoreCase(selectedFilter)
                                                || request.getSampleStatus().equalsIgnoreCase(selectedFilter)
                                                || request.getResultStatus().equalsIgnoreCase(selectedFilter);

                                return matchesKeyword && matchesFilter;
                            })
                            .toList()
            );

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void showRequestDetails(TestRequest request) {
        requestDetailsArea.setText(
                "Request ID: " + request.getId() + "\n"
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

        updateCountdown(request);
    }

    private void updateCountdown(TestRequest request) {
        if (request.getEstimatedReadyAt() == null) {
            countdownLabel.setText("Countdown: Not available");
            return;
        }

        java.time.Duration duration = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                request.getEstimatedReadyAt()
        );

        if (duration.isNegative() || duration.isZero()) {
            countdownLabel.setText("Countdown: Result should be ready soon.");
            return;
        }

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        countdownLabel.setText(
                "Countdown: " + days + " day(s), " + hours + " hour(s), " + minutes + " minute(s) remaining"
        );
    }

    @FXML
    private void handleViewPaymentDetails() {
        clearMessages();

        TestRequest selected = myRequestsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a request first.");
            return;
        }

        try {
            PaymentContext.setCurrentRequestId(selected.getId());
            SceneManager.switchTo("/views/payment-details.fxml", "Payment Details");
        } catch (IOException e) {
            showError("Could not open payment details: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchRequestField.clear();
        requestStatusFilterComboBox.setValue("ALL");
        requestDetailsArea.clear();
        countdownLabel.setText("Countdown: Select a request to view time remaining.");
        loadRequests();
    }

    @FXML
    private void handleViewDetails() {
        clearMessages();

        TestRequest selected = myRequestsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a request first.");
            return;
        }

        showRequestDetails(selected);
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.switchTo("/views/customer-dashboard.fxml", "Customer Dashboard");
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
}