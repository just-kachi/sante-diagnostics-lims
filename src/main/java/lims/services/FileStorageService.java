package lims.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileStorageService {

    private static final String UPLOAD_DIRECTORY = "uploads/results";

    public String saveResultFile(File sourceFile) throws IOException {
        if (sourceFile == null) {
            return null;
        }

        Path uploadPath = Path.of(UPLOAD_DIRECTORY);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileName = sourceFile.getName();
        String safeFileName = System.currentTimeMillis() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        Path destination = uploadPath.resolve(safeFileName);

        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return destination.toAbsolutePath().toString();
    }

    public String getFileType(File file) {
        if (file == null) {
            return null;
        }

        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return "PDF";
        }

        if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "IMAGE";
        }

        return "UNKNOWN";
    }

    public boolean isAllowedResultFile(File file) {
        String fileType = getFileType(file);
        return "PDF".equals(fileType) || "IMAGE".equals(fileType);
    }
}