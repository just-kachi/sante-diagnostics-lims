package lims.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lims.models.Result;
import lims.models.ResultFile;
import lims.models.User;
import lims.services.ResultService;
import lims.utils.SceneManager;
import lims.utils.Session;

public class CustomerResultsController {

    @FXML
    private TextField searchResultField;

    @FXML
    private TableView<Result> resultsTable;

    @FXML
    private TableColumn<Result, Integer> resultIdColumn;

    @FXML
    private TableColumn<Result, Integer> requestIdColumn;

    @FXML
    private TableColumn<Result, String> testNameColumn;

    @FXML
    private TableColumn<Result, String> resultFormatColumn;

    @FXML
    private TableColumn<Result, String> resultStatusColumn;

    @FXML
    private TableColumn<Result, String> validatedAtColumn;

    @FXML
    private TextArea resultSummaryArea;

    @FXML
    private ComboBox<ResultFile> resultFileComboBox;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final ResultService resultService = new ResultService();
    private final ObservableList<Result> resultList = FXCollections.observableArrayList();
    private final ObservableList<ResultFile> fileList = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        setupTable();
        setupSelection();
        setupSearch();
        setupFileComboBox();
        loadResults();
    }

    private void setupTable() {
        resultIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        resultFormatColumn.setCellValueFactory(new PropertyValueFactory<>("resultFormat"));
        resultStatusColumn.setCellValueFactory(new PropertyValueFactory<>("resultStatus"));

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
                        showResultDetails(selectedResult);
                        loadFilesForResult(selectedResult.getId());
                    }
                }
        );
    }

    private void setupSearch() {
        searchResultField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResults();
        });
    }

    private void setupFileComboBox() {
        resultFileComboBox.setItems(fileList);

        resultFileComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ResultFile file) {
                if (file == null) {
                    return "";
                }

                return file.getFileName() + " (" + file.getFileType() + ")";
            }

            @Override
            public ResultFile fromString(String string) {
                return null;
            }
        });
    }

    private void loadResults() {
        clearMessages();

        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("No logged-in customer found.");
            return;
        }

        try {
            resultList.setAll(resultService.getValidatedResultsForCustomer(currentUser.getId()));
        } catch (SQLException e) {
            showError("Could not load results: " + e.getMessage());
        }
    }

    private void filterResults() {
        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            return;
        }

        try {
            String keyword = searchResultField.getText() == null
                    ? ""
                    : searchResultField.getText().toLowerCase().trim();

            resultList.setAll(
                    resultService.getValidatedResultsForCustomer(currentUser.getId())
                            .stream()
                            .filter(result ->
                                    keyword.isBlank()
                                            || result.getTestName().toLowerCase().contains(keyword)
                                            || result.getResultFormat().toLowerCase().contains(keyword)
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
        resultSummaryArea.setText(
                "Result ID: " + result.getId() + "\n"
                        + "Request ID: " + result.getRequestId() + "\n"
                        + "Test: " + result.getTestName() + "\n"
                        + "Format: " + result.getResultFormat() + "\n"
                        + "Status: " + result.getResultStatus() + "\n"
                        + "Uploaded At: " + (
                                result.getUploadedAt() == null
                                        ? "Not available"
                                        : result.getUploadedAt().format(formatter)
                        ) + "\n"
                        + "Validated At: " + (
                                result.getValidatedAt() == null
                                        ? "Not available"
                                        : result.getValidatedAt().format(formatter)
                        ) + "\n\n"
                        + "Result Value / Notes:\n"
                        + (
                                result.getResultValue() == null || result.getResultValue().isBlank()
                                        ? "No written result value."
                                        : result.getResultValue()
                        )
        );
    }

    private void loadFilesForResult(int resultId) {
        try {
            List<ResultFile> files = resultService.getFilesForResult(resultId);
            fileList.setAll(files);

            if (!files.isEmpty()) {
                resultFileComboBox.getSelectionModel().selectFirst();
            } else {
                resultFileComboBox.getSelectionModel().clearSelection();
            }

        } catch (SQLException e) {
            showError("Could not load result files: " + e.getMessage());
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

        showResultDetails(selected);
        loadFilesForResult(selected.getId());
    }

    @FXML
    private void handleOpenFile() {
        clearMessages();

        ResultFile selectedFile = resultFileComboBox.getSelectionModel().getSelectedItem();

        if (selectedFile == null) {
            showError("No PDF/image file is attached to this result.");
            return;
        }

        try {
            resultService.openResultFile(selectedFile);
            showSuccess("Opening file: " + selectedFile.getFileName());
        } catch (IOException e) {
            showError("Could not open file: " + e.getMessage());
        }
    }

    /**
     * Allows the customer to download (save a copy of) the selected attached
     * result file to a destination of their choice using a JavaFX FileChooser.
     * Reuses existing ResultFile metadata and on-disk storage — the file is
     * simply copied from its stored path to the user-selected location.
     */
    @FXML
    private void handleDownloadFile() {
        clearMessages();

        ResultFile selectedFile = resultFileComboBox.getSelectionModel().getSelectedItem();

        if (selectedFile == null) {
            showError("No PDF/image file is attached to this result.");
            return;
        }

        // Defensive guard: a null or blank file path would cause Path.of(...)
        // to throw NullPointerException / InvalidPathException, which is not
        // caught by the IOException handler below. Fail gracefully instead.
        if (selectedFile.getFilePath() == null || selectedFile.getFilePath().isBlank()) {
            showError("File path missing for this attachment.");
            return;
        }

        Path sourcePath = Path.of(selectedFile.getFilePath());

        if (!Files.exists(sourcePath)) {
            showError("File does not exist on the server: " + selectedFile.getFilePath());
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Download Result File");
        fileChooser.setInitialFileName(selectedFile.getFileName());

        Window window = resultFileComboBox.getScene() == null
                ? null
                : resultFileComboBox.getScene().getWindow();

        File destination = fileChooser.showSaveDialog(window);

        if (destination == null) {
            // User cancelled the save dialog — nothing to do.
            return;
        }

        try {
            Files.copy(sourcePath, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            showSuccess("Downloaded to: " + destination.getAbsolutePath());
        } catch (IOException e) {
            showError("Could not download file: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchResultField.clear();
        resultSummaryArea.clear();
        fileList.clear();
        loadResults();
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

    private void showSuccess(String message) {
        if (successLabel != null) {
            successLabel.setText(message);
        }
    }
}