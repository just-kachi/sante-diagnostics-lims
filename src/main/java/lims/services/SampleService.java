package lims.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lims.database.DatabaseConnection;
import lims.models.Sample;

public class SampleService {
    private final AuditLogService auditLogService = new AuditLogService();

    public List<Sample> getAllSamples() throws SQLException {
        String sql = """
                SELECT
                    s.id,
                    s.request_id,
                    u.full_name AS customer_name,
                    tt.name AS test_name,
                    s.sample_status,
                    tr.payment_status,
                    r.result_status,
                    staff.full_name AS updated_by_name,
                    s.updated_at
                FROM samples s
                JOIN test_requests tr ON s.request_id = tr.id
                JOIN users u ON tr.customer_id = u.id
                JOIN test_types tt ON tr.test_type_id = tt.id
                LEFT JOIN results r ON r.request_id = tr.id
                LEFT JOIN users staff ON s.updated_by = staff.id
                ORDER BY s.updated_at DESC
                """;

        List<Sample> samples = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                samples.add(mapSample(resultSet));
            }
        }

        return samples;
    }

    public boolean updateSampleStatus(int sampleId, String newStatus, int updatedByUserId) throws SQLException {
        String sql = """
                UPDATE samples
                SET sample_status = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newStatus);
            statement.setInt(2, updatedByUserId);
            statement.setInt(3, sampleId);

            boolean updated = statement.executeUpdate() > 0;

            if (updated) {
                auditLogService.log(
                        updatedByUserId,
                        "SAMPLE_STATUS_UPDATED",
                        "SAMPLE",
                        sampleId,
                        "Sample ID " + sampleId + " status updated to " + newStatus
                );
            }

            return updated;
        }
    }

    private Sample mapSample(ResultSet resultSet) throws SQLException {
        return new Sample(
                resultSet.getInt("id"),
                resultSet.getInt("request_id"),
                resultSet.getString("customer_name"),
                resultSet.getString("test_name"),
                resultSet.getString("sample_status"),
                resultSet.getString("payment_status"),
                resultSet.getString("result_status"),
                resultSet.getString("updated_by_name"),
                resultSet.getTimestamp("updated_at") == null
                        ? null
                        : resultSet.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}