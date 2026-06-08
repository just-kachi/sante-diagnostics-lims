package lims.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import lims.utils.SceneManager;

public class ResultViewerController {

    @FXML
    private Label testNameLabel;

    @FXML
    private Label resultStatusLabel;

    @FXML
    private Label validatedAtLabel;

    @FXML
    private Label filePathLabel;

    @FXML
    private TextArea resultTextArea;

    @FXML
    private ImageView imagePreview;

    private static String currentTestName;
    private static String currentResultStatus;
    private static String currentValidatedAt;
    private static String currentResultText;
    private static String currentFilePath;

    public static void setResultData(String testName,
                                     String resultStatus,
                                     String validatedAt,
                                     String resultText,
                                     String filePath) {
        currentTestName = testName;
        currentResultStatus = resultStatus;
        currentValidatedAt = validatedAt;
        currentResultText = resultText;
        currentFilePath = filePath;
    }

    @FXML
    private void initialize() {
        if (testNameLabel != null) {
            testNameLabel.setText(valueOrDash(currentTestName));
        }

        if (resultStatusLabel != null) {
            resultStatusLabel.setText(valueOrDash(currentResultStatus));
        }

        if (validatedAtLabel != null) {
            validatedAtLabel.setText(valueOrDash(currentValidatedAt));
        }

        if (filePathLabel != null) {
            filePathLabel.setText(valueOrDash(currentFilePath));
        }

        if (resultTextArea != null) {
            resultTextArea.setText(valueOrDash(currentResultText));
        }

        loadImagePreviewIfPossible();
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.switchTo("/views/customer-results.fxml", "Result Vault");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenFile() {
        if (currentFilePath == null || currentFilePath.isBlank()) {
            return;
        }

        try {
            File file = new File(currentFilePath);

            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDownloadFile() {
        if (currentFilePath == null || currentFilePath.isBlank()) {
            return;
        }

        File sourceFile = new File(currentFilePath);

        if (!sourceFile.exists()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Result File");
        fileChooser.setInitialFileName(sourceFile.getName());

        File destinationFile = fileChooser.showSaveDialog(null);

        if (destinationFile == null) {
            return;
        }

        try {
            Files.copy(
                    sourceFile.toPath(),
                    destinationFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadImagePreviewIfPossible() {
        if (imagePreview == null || currentFilePath == null || currentFilePath.isBlank()) {
            return;
        }

        String lowerPath = currentFilePath.toLowerCase();

        boolean isImage = lowerPath.endsWith(".png")
                || lowerPath.endsWith(".jpg")
                || lowerPath.endsWith(".jpeg")
                || lowerPath.endsWith(".gif");

        if (!isImage) {
            return;
        }

        File imageFile = new File(currentFilePath);

        if (!imageFile.exists()) {
            return;
        }

        Image image = new Image(imageFile.toURI().toString());
        imagePreview.setImage(image);
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}