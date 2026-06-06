package lims.services;

import java.awt.Desktop;
import java.nio.file.Path;
import lims.models.ResultFile;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lims.database.DatabaseConnection;
import lims.models.Result;

public class ResultService {
    private final AuditLogService auditLogService = new AuditLogService();

    private final FileStorageService fileStorageService = new FileStorageService();

    public List<Result> getAllResults() throws SQLException {
        String sql = """
                SELECT
                    r.id,
                    r.request_id,
                    u.full_name AS customer_name,
                    tt.name AS test_name,
                    tt.result_format,
                    tr.payment_status,
                    s.sample_status,
                    r.result_value,
                    r.result_status,
                    uploader.full_name AS uploaded_by_name,
                    validator.full_name AS validated_by_name,
                    r.uploaded_at,
                    r.validated_at
                FROM results r
                JOIN test_requests tr ON r.request_id = tr.id
                JOIN users u ON tr.customer_id = u.id
                JOIN test_types tt ON tr.test_type_id = tt.id
                LEFT JOIN samples s ON s.request_id = tr.id
                LEFT JOIN users uploader ON r.uploaded_by = uploader.id
                LEFT JOIN users validator ON r.validated_by = validator.id
                ORDER BY tr.requested_at DESC
                """;

        List<Result> results = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                results.add(mapResult(resultSet));
            }
        }

        return results;
    }

    public boolean uploadResult(int resultId, String resultValue, File selectedFile, int uploadedByUserId)
            throws SQLException, IOException {

        String filePath = null;
        String fileName = null;
        String fileType = null;

        if (selectedFile != null) {
            if (!fileStorageService.isAllowedResultFile(selectedFile)) {
                throw new IllegalArgumentException("Only PDF, PNG, JPG, and JPEG files are allowed.");
            }

            filePath = fileStorageService.saveResultFile(selectedFile);
            fileName = selectedFile.getName();
            fileType = fileStorageService.getFileType(selectedFile);
        }

        String updateResultSql = """
                UPDATE results
                SET result_value = ?,
                    result_status = 'UPLOADED',
                    uploaded_by = ?,
                    uploaded_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        String insertFileSql = """
                INSERT INTO result_files (result_id, file_name, file_path, file_type)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int resultRows;

                try (PreparedStatement resultStatement = connection.prepareStatement(updateResultSql)) {
                    resultStatement.setString(1, resultValue);
                    resultStatement.setInt(2, uploadedByUserId);
                    resultStatement.setInt(3, resultId);

                    resultRows = resultStatement.executeUpdate();
                }

                if (resultRows <= 0) {
                    connection.rollback();
                    return false;
                }

                if (selectedFile != null) {
                    try (PreparedStatement fileStatement = connection.prepareStatement(insertFileSql)) {
                        fileStatement.setInt(1, resultId);
                        fileStatement.setString(2, fileName);
                        fileStatement.setString(3, filePath);
                        fileStatement.setString(4, fileType);
                        fileStatement.executeUpdate();
                    }
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
    
    public boolean validateResult(int resultId, int validatedByUserId) throws SQLException {
    String updateResultSql = """
            UPDATE results
            SET result_status = 'VALIDATED',
                validated_by = ?,
                validated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    String updateSampleSql = """
            UPDATE samples
            SET sample_status = 'READY',
                updated_by = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE request_id = (
                SELECT request_id FROM results WHERE id = ?
            )
            """;

    String updateRequestSql = """
            UPDATE test_requests
            SET request_status = 'COMPLETED'
            WHERE id = (
                SELECT request_id FROM results WHERE id = ?
            )
            """;

    try (Connection connection = DatabaseConnection.getConnection()) {
        connection.setAutoCommit(false);

        try {
            int resultRows;

            try (PreparedStatement resultStatement = connection.prepareStatement(updateResultSql)) {
                resultStatement.setInt(1, validatedByUserId);
                resultStatement.setInt(2, resultId);
                resultRows = resultStatement.executeUpdate();
            }

            if (resultRows <= 0) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement sampleStatement = connection.prepareStatement(updateSampleSql)) {
                sampleStatement.setInt(1, validatedByUserId);
                sampleStatement.setInt(2, resultId);
                sampleStatement.executeUpdate();
            }

            try (PreparedStatement requestStatement = connection.prepareStatement(updateRequestSql)) {
                requestStatement.setInt(1, resultId);
                requestStatement.executeUpdate();
            }

            connection.commit();

            auditLogService.log(
                    validatedByUserId,
                    "RESULT_VALIDATED",
                    "RESULT",
                    resultId,
                    "Result ID " + resultId + " was validated."
            );

            return true;

        } catch (SQLException e) {
            connection.rollback();
            throw e;

        } finally {
            connection.setAutoCommit(true);
        }
    }
}

    public boolean rejectResult(int resultId, int rejectedByUserId, String reason) throws SQLException {
        String newValue = "REJECTED";
        if (reason != null && !reason.isBlank()) {
            newValue = "REJECTED - Reason: " + reason.trim();
        }

        String sql = """
                UPDATE results
                SET result_status = 'REJECTED',
                    result_value = COALESCE(result_value, '') || ?,
                    validated_by = ?,
                    validated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "\n" + newValue);
            statement.setInt(2, rejectedByUserId);
            statement.setInt(3, resultId);

            boolean rejected = statement.executeUpdate() > 0;

            if (rejected) {
                auditLogService.log(
                        rejectedByUserId,
                        "RESULT_REJECTED",
                        "RESULT",
                        resultId,
                        "Result ID " + resultId + " was rejected. Reason: " + reason
                );
            }

return rejected;
        }
    }
    
    public List<Result> getValidatedResultsForCustomer(int customerId) throws SQLException {
    String sql = """
            SELECT
                r.id,
                r.request_id,
                u.full_name AS customer_name,
                tt.name AS test_name,
                tt.result_format,
                tr.payment_status,
                s.sample_status,
                r.result_value,
                r.result_status,
                uploader.full_name AS uploaded_by_name,
                validator.full_name AS validated_by_name,
                r.uploaded_at,
                r.validated_at
            FROM results r
            JOIN test_requests tr ON r.request_id = tr.id
            JOIN users u ON tr.customer_id = u.id
            JOIN test_types tt ON tr.test_type_id = tt.id
            LEFT JOIN samples s ON s.request_id = tr.id
            LEFT JOIN users uploader ON r.uploaded_by = uploader.id
            LEFT JOIN users validator ON r.validated_by = validator.id
            WHERE tr.customer_id = ?
              AND r.result_status = 'VALIDATED'
            ORDER BY r.validated_at DESC
            """;

        List<Result> results = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

        try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapResult(resultSet));
                }
            }
        }

        return results;
    }

    public List<ResultFile> getFilesForResult(int resultId) throws SQLException {
        String sql = """
            SELECT id, result_id, file_name, file_path, file_type, uploaded_at
            FROM result_files
            WHERE result_id = ?
            ORDER BY uploaded_at DESC
            """;

        List<ResultFile> files = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, resultId);

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                files.add(new ResultFile(
                        resultSet.getInt("id"),
                        resultSet.getInt("result_id"),
                        resultSet.getString("file_name"),
                        resultSet.getString("file_path"),
                        resultSet.getString("file_type"),
                        resultSet.getTimestamp("uploaded_at") == null
                                ? null
                                : resultSet.getTimestamp("uploaded_at").toLocalDateTime()
                    ));
                }
            }
        }

        return files;
    }

    public void openResultFile(ResultFile resultFile) throws IOException {
        if (resultFile == null || resultFile.getFilePath() == null) {
            throw new IOException("No file selected.");
        }

        Path path = Path.of(resultFile.getFilePath());

        if (!java.nio.file.Files.exists(path)) {
            throw new IOException("File does not exist: " + resultFile.getFilePath());
        }

        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Opening files is not supported on this computer.");
        }

        Desktop.getDesktop().open(path.toFile());
    }

    private Result mapResult(ResultSet resultSet) throws SQLException {
        return new Result(
                resultSet.getInt("id"),
                resultSet.getInt("request_id"),
                resultSet.getString("customer_name"),
                resultSet.getString("test_name"),
                resultSet.getString("result_format"),
                resultSet.getString("payment_status"),
                resultSet.getString("sample_status"),
                resultSet.getString("result_value"),
                resultSet.getString("result_status"),
                resultSet.getString("uploaded_by_name"),
                resultSet.getString("validated_by_name"),
                resultSet.getTimestamp("uploaded_at") == null
                        ? null
                        : resultSet.getTimestamp("uploaded_at").toLocalDateTime(),
                resultSet.getTimestamp("validated_at") == null
                        ? null
                        : resultSet.getTimestamp("validated_at").toLocalDateTime()
        );
    }
}