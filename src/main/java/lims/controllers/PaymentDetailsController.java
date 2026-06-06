package lims.controllers;

import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lims.models.TestRequest;
import lims.models.User;
import lims.services.PaymentService;
import lims.services.TestRequestService;
import lims.utils.PaymentContext;
import lims.utils.SceneManager;
import lims.utils.Session;

public class PaymentDetailsController {

    @FXML
    private Label requestIdLabel;

    @FXML
    private Label testNameLabel;

    @FXML
    private Label amountLabel;

    @FXML
    private Label bankNameLabel;

    @FXML
    private Label accountNumberLabel;

    @FXML
    private Label accountNameLabel;

    @FXML
    private Label paymentStatusLabel;

    @FXML
    private Label instructionLabel;

    @FXML
    private Label messageLabel;

    private final TestRequestService testRequestService = new TestRequestService();
    private final PaymentService paymentService = new PaymentService();

    private TestRequest currentRequest;

    @FXML
    private void initialize() {
        loadPaymentDetails();
    }

    private void loadPaymentDetails() {
        int requestId = PaymentContext.getCurrentRequestId();

        if (requestId <= 0) {
            requestIdLabel.setText("Request ID: Not available");
            instructionLabel.setText("No request was selected.");
            return;
        }

        try {
            currentRequest = testRequestService.getRequestById(requestId);

            if (currentRequest == null) {
                instructionLabel.setText("Request could not be found.");
                return;
            }

            requestIdLabel.setText("Request ID: " + currentRequest.getId());
            testNameLabel.setText("Test: " + currentRequest.getTestName());
            amountLabel.setText("Amount: ₦" + currentRequest.getPrice());
            paymentStatusLabel.setText("Payment Status: " + currentRequest.getPaymentStatus());

            bankNameLabel.setText("Bank Name: PAU Demo Bank");
            accountNumberLabel.setText("Account Number: 0123456789");
            accountNameLabel.setText("Account Name: Sante Diagnostics Ltd");

            instructionLabel.setText(
                    "You can pay now for demo purposes, or pay later from your requests page. "
                            + "In a real system, this would connect to a bank/payment gateway or staff verification."
            );

            clearMessage();

        } catch (SQLException e) {
            instructionLabel.setText("Could not load payment details: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayNow() {
        clearMessage();

        if (currentRequest == null) {
            showMessage("No request is loaded.");
            return;
        }

        if ("PAID".equalsIgnoreCase(currentRequest.getPaymentStatus())) {
            showMessage("This request has already been paid.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showMessage("No logged-in user found.");
            return;
        }

        try {
            boolean paid = paymentService.markRequestAsPaid(currentRequest.getId(), currentUser.getId());

            if (paid) {
                showMessage("Payment successful. Your request is now marked as PAID.");
                loadPaymentDetails();
            } else {
                showMessage("Payment could not be completed.");
            }

        } catch (SQLException e) {
            showMessage("Payment error: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayLater() {
        try {
            PaymentContext.clear();
            SceneManager.switchTo("/views/customer-dashboard.fxml", "Customer Dashboard");
        } catch (IOException e) {
            showMessage("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void goToDashboard() {
        try {
            PaymentContext.clear();
            SceneManager.switchTo("/views/customer-dashboard.fxml", "Customer Dashboard");
        } catch (IOException e) {
            showMessage("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void goToMyRequests() {
        try {
            PaymentContext.clear();
            SceneManager.switchTo("/views/my-requests.fxml", "My Requests");
        } catch (IOException e) {
            showMessage("My Requests screen has not been created yet.");
        }
    }

    private void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        } else {
            instructionLabel.setText(message);
        }
    }

    private void clearMessage() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }
}