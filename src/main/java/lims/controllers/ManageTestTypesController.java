package lims.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lims.models.TestType;
import lims.services.TestTypeService;
import lims.utils.SceneManager;

public class ManageTestTypesController {

    @FXML
    private TextField testNameField;

    @FXML
    private TextField testCategoryField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField tatHoursField;

    @FXML
    private ComboBox<String> resultFormatComboBox;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField searchTestTypeField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private TableView<TestType> testTypesTable;

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
    private TableColumn<TestType, String> descriptionColumn;

    @FXML
    private TableColumn<TestType, Boolean> activeColumn;

    private final TestTypeService testTypeService = new TestTypeService();
    private final ObservableList<TestType> testTypeList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupComboBox();
        setupTable();
        setupTableSelection();
        setupSearch();
        loadTestTypes();
    }

    private void setupComboBox() {
        resultFormatComboBox.setItems(FXCollections.observableArrayList(
                "NUMERIC",
                "TEXT",
                "PDF",
                "IMAGE"
        ));
    }

    private void setupTable() {
        testTypeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        testCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        tatHoursColumn.setCellValueFactory(new PropertyValueFactory<>("tatHours"));
        resultFormatColumn.setCellValueFactory(new PropertyValueFactory<>("resultFormat"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        if (activeColumn != null) {
            activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        }

        testTypesTable.setItems(testTypeList);
    }

    private void setupTableSelection() {
        testTypesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedTestType) -> {
                    if (selectedTestType != null) {
                        fillForm(selectedTestType);
                    }
                }
        );
    }

    private void setupSearch() {
        searchTestTypeField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTestTypes(newValue);
        });
    }

    private void loadTestTypes() {
        try {
            testTypeList.setAll(testTypeService.getAllTestTypes());
        } catch (SQLException e) {
            showError("Could not load test types: " + e.getMessage());
        }
    }

    private void filterTestTypes(String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) {
                testTypeList.setAll(testTypeService.getAllTestTypes());
                return;
            }

            String lowerKeyword = keyword.toLowerCase();

            testTypeList.setAll(
                    testTypeService.getAllTestTypes()
                            .stream()
                            .filter(testType ->
                                    testType.getName().toLowerCase().contains(lowerKeyword)
                                            || testType.getCategory().toLowerCase().contains(lowerKeyword)
                                            || testType.getResultFormat().toLowerCase().contains(lowerKeyword)
                                            || String.valueOf(testType.getPrice()).contains(lowerKeyword)
                            )
                            .toList()
            );

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddTestType() {
        clearMessages();

        try {
            TestType testType = readForm();

            boolean created = testTypeService.createTestType(testType);

            if (created) {
                showSuccess("Test type added successfully.");
                handleClearForm();
                loadTestTypes();
            } else {
                showError("Test type could not be added.");
            }

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateTestType() {
        clearMessages();

        TestType selected = testTypesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a test type to update.");
            return;
        }

        try {
            TestType testType = readForm();
            testType.setId(selected.getId());
            testType.setActive(selected.isActive());

            boolean updated = testTypeService.updateTestType(testType);

            if (updated) {
                showSuccess("Test type updated successfully.");
                handleClearForm();
                loadTestTypes();
            } else {
                showError("Test type could not be updated.");
            }

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTestType() {
        clearMessages();

        TestType selected = testTypesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a test type to deactivate.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Deactivate Test Type");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to deactivate this test type?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            boolean deactivated = testTypeService.deactivateTestType(selected.getId());

            if (deactivated) {
                showSuccess("Test type deactivated successfully.");
                handleClearForm();
                loadTestTypes();
            } else {
                showError("Test type could not be deactivated.");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        testTypesTable.getSelectionModel().clearSelection();
        testNameField.clear();
        testCategoryField.clear();
        priceField.clear();
        tatHoursField.clear();
        resultFormatComboBox.getSelectionModel().clearSelection();
        descriptionArea.clear();
        clearMessages();
    }

    @FXML
    private void handleRefresh() {
        clearMessages();
        searchTestTypeField.clear();
        loadTestTypes();
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.switchTo("/views/super-admin-dashboard.fxml", "Super Admin Dashboard");
        } catch (IOException e) {
            showError("Navigation error: " + e.getMessage());
        }
    }

    private TestType readForm() {
        String name = testNameField.getText().trim();
        String category = testCategoryField.getText().trim();
        String priceText = priceField.getText().trim();
        String tatText = tatHoursField.getText().trim();
        String resultFormat = resultFormatComboBox.getValue();
        String description = descriptionArea.getText().trim();

        if (name.isBlank()) {
            throw new IllegalArgumentException("Test name is required.");
        }

        if (category.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }

        if (priceText.isBlank()) {
            throw new IllegalArgumentException("Price is required.");
        }

        if (tatText.isBlank()) {
            throw new IllegalArgumentException("Turnaround time is required.");
        }

        if (resultFormat == null || resultFormat.isBlank()) {
            throw new IllegalArgumentException("Result format is required.");
        }

        BigDecimal price;

        try {
            price = new BigDecimal(priceText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Price must be a valid number.");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }

        int tatHours;

        try {
            tatHours = Integer.parseInt(tatText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("TAT hours must be a whole number.");
        }

        if (tatHours <= 0) {
            throw new IllegalArgumentException("TAT hours must be greater than zero.");
        }

        return new TestType(name, category, price, tatHours, resultFormat, description);
    }

    private void fillForm(TestType testType) {
        testNameField.setText(testType.getName());
        testCategoryField.setText(testType.getCategory());
        priceField.setText(testType.getPrice().toString());
        tatHoursField.setText(String.valueOf(testType.getTatHours()));
        resultFormatComboBox.setValue(testType.getResultFormat());
        descriptionArea.setText(testType.getDescription());
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