package lims.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
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
import lims.models.TestType;
import lims.models.User;
import lims.services.TestRequestService;
import lims.services.TestTypeService;
import lims.utils.PaymentContext;
import lims.utils.SceneManager;
import lims.utils.Session;

public class BrowseTestsController {

    @FXML
    private TextField searchTestField;

    @FXML
    private ComboBox<String> categoryFilterComboBox;

    @FXML
    private TableView<TestType> testsTable;

    @FXML
    private TableColumn<TestType, Integer> testTypeIdColumn;

    @FXML
    private TableColumn<TestType, String> testNameColumn;

    @FXML
    private TableColumn<TestType, String> testCategoryColumn;

    @FXML
    private TableColumn<TestType, BigDecimal> priceColumn;

    @FXML
    private TableColumn<TestType, Integer> tatHoursColumn;

    @FXML
    private TableColumn<TestType, String> resultFormatColumn;

    @FXML
    private TextArea selectedTestDetailsArea;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final TestTypeService testTypeService = new TestTypeService();
    private final TestRequestService testRequestService = new TestRequestService();
    private final ObservableList<TestType> testList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTable();
        setupSelection();
        setupSearch();
        loadTests();
    }

    private void setupTable() {
        testTypeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        testCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        tatHoursColumn.setCellValueFactory(new PropertyValueFactory<>("tatHours"));
        resultFormatColumn.setCellValueFactory(new PropertyValueFactory<>("resultFormat"));

        testsTable.setItems(testList);
    }

    private void setupSelection() {
        testsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> {
                    if (selected != null) {
                        selectedTestDetailsArea.setText(
                                "Test: " + selected.getName() + "\n"
                                        + "Category: " + selected.getCategory() + "\n"
                                        + "Price: ₦" + selected.getPrice() + "\n"
                                        + "TAT: " + selected.getTatHours() + " hours\n"
                                        + "Result Format: " + selected.getResultFormat() + "\n\n"
                                        + "Description:\n" + selected.getDescription()
                        );
                    }
                }
        );
    }

    private void setupSearch() {
        searchTestField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTests();
        });

        categoryFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterTests();
        });
    }

    private void loadTests() {
        try {
            testList.setAll(testTypeService.getActiveTestTypes());
            setupCategoryFilter();
        } catch (SQLException e) {
            showError("Could not load tests: " + e.getMessage());
        }
    }

    private void setupCategoryFilter() throws SQLException {
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add("ALL");

        for (TestType testType : testTypeService.getActiveTestTypes()) {
            if (!categories.contains(testType.getCategory())) {
                categories.add(testType.getCategory());
            }
        }

        categoryFilterComboBox.setItems(categories);
        categoryFilterComboBox.setValue("ALL");
    }

    private void filterTests() {
        try {
            String keyword = searchTestField.getText() == null
                    ? ""
                    : searchTestField.getText().toLowerCase().trim();

            String selectedCategory = categoryFilterComboBox.getValue();

            testList.setAll(
                    testTypeService.getActiveTestTypes()
                            .stream()
                            .filter(testType -> {
                                boolean matchesKeyword =
                                        keyword.isBlank()
                                                || testType.getName().toLowerCase().contains(keyword)
                                                || testType.getCategory().toLowerCase().contains(keyword)
                                                || testType.getResultFormat().toLowerCase().contains(keyword);

                                boolean matchesCategory =
                                        selectedCategory == null
                                                || selectedCategory.equals("ALL")
                                                || testType.getCategory().equals(selectedCategory);

                                return matchesKeyword && matchesCategory;
                            })
                            .toList()
            );

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRequestSelectedTest() {
        clearMessages();

        TestType selected = testsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a test first.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in customer found.");
            return;
        }

        try {
            int requestId = testRequestService.createRequest(currentUser.getId(), selected);
            PaymentContext.setCurrentRequestId(requestId);
            SceneManager.switchTo("/views/payment-details.fxml", "Payment Details");

        } catch (SQLException e) {
            showError("Could not create request: " + e.getMessage());
        } catch (IOException e) {
            showError("Could not open payment screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        clearMessages();
        searchTestField.clear();
        categoryFilterComboBox.setValue("ALL");
        selectedTestDetailsArea.clear();
        loadTests();
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