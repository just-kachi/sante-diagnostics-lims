package lims.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lims.database.DatabaseConnection;
import lims.models.TestRequest;
import lims.models.TestType;

public class TestRequestService {

    public int createRequest(int customerId, TestType testType) throws SQLException {
        String insertRequestSql = """
                INSERT INTO test_requests (
                    customer_id,
                    test_type_id,
                    request_status,
                    payment_status,
                    estimated_ready_at
                )
                VALUES (?, ?, 'ACTIVE', 'UNPAID', CURRENT_TIMESTAMP + (? * INTERVAL '1 hour'))
                RETURNING id
                """;

        String insertSampleSql = """
                INSERT INTO samples (request_id, sample_status)
                VALUES (?, 'REQUESTED')
                """;

        String insertResultSql = """
                INSERT INTO results (request_id, result_status)
                VALUES (?, 'PENDING')
                """;

        String insertPaymentSql = """
                INSERT INTO payments (request_id, amount, payment_status)
                VALUES (?, ?, 'UNPAID')
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int requestId;

                try (PreparedStatement requestStatement = connection.prepareStatement(insertRequestSql)) {
                    requestStatement.setInt(1, customerId);
                    requestStatement.setInt(2, testType.getId());
                    requestStatement.setInt(3, testType.getTatHours());

                    try (ResultSet resultSet = requestStatement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("Could not create test request.");
                        }

                        requestId = resultSet.getInt("id");
                    }
                }

                try (PreparedStatement sampleStatement = connection.prepareStatement(insertSampleSql)) {
                    sampleStatement.setInt(1, requestId);
                    sampleStatement.executeUpdate();
                }

                try (PreparedStatement resultStatement = connection.prepareStatement(insertResultSql)) {
                    resultStatement.setInt(1, requestId);
                    resultStatement.executeUpdate();
                }

                try (PreparedStatement paymentStatement = connection.prepareStatement(insertPaymentSql)) {
                    paymentStatement.setInt(1, requestId);
                    paymentStatement.setBigDecimal(2, testType.getPrice());
                    paymentStatement.executeUpdate();
                }

                connection.commit();
                return requestId;

            } catch (SQLException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public TestRequest getRequestById(int requestId) throws SQLException {
        String sql = """
                SELECT
                    tr.id,
                    tr.customer_id,
                    u.full_name AS customer_name,
                    tr.test_type_id,
                    tt.name AS test_name,
                    tt.category AS test_category,
                    tt.price,
                    tt.tat_hours,
                    tt.result_format,
                    tr.request_status,
                    tr.payment_status,
                    s.sample_status,
                    r.result_status,
                    tr.requested_at,
                    tr.estimated_ready_at
                FROM test_requests tr
                JOIN users u ON tr.customer_id = u.id
                JOIN test_types tt ON tr.test_type_id = tt.id
                LEFT JOIN samples s ON s.request_id = tr.id
                LEFT JOIN results r ON r.request_id = tr.id
                WHERE tr.id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, requestId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRequest(resultSet);
                }
            }
        }

        return null;
    }
    
    public java.util.List<TestRequest> getCustomerRequests(int customerId) throws SQLException {
    String sql = """
            SELECT
                tr.id,
                tr.customer_id,
                u.full_name AS customer_name,
                tr.test_type_id,
                tt.name AS test_name,
                tt.category AS test_category,
                tt.price,
                tt.tat_hours,
                tt.result_format,
                tr.request_status,
                tr.payment_status,
                s.sample_status,
                r.result_status,
                tr.requested_at,
                tr.estimated_ready_at
            FROM test_requests tr
            JOIN users u ON tr.customer_id = u.id
            JOIN test_types tt ON tr.test_type_id = tt.id
            LEFT JOIN samples s ON s.request_id = tr.id
            LEFT JOIN results r ON r.request_id = tr.id
            WHERE tr.customer_id = ?
            ORDER BY tr.requested_at DESC
            """;

        java.util.List<TestRequest> requests = new java.util.ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRequest(resultSet));
                }
            }
        }

        return requests;
    }
    
    
    public java.util.List<TestRequest> getAllRequests() throws SQLException {
    String sql = """
            SELECT
                tr.id,
                tr.customer_id,
                u.full_name AS customer_name,
                tr.test_type_id,
                tt.name AS test_name,
                tt.category AS test_category,
                tt.price,
                tt.tat_hours,
                tt.result_format,
                tr.request_status,
                tr.payment_status,
                s.sample_status,
                r.result_status,
                tr.requested_at,
                tr.estimated_ready_at
            FROM test_requests tr
            JOIN users u ON tr.customer_id = u.id
            JOIN test_types tt ON tr.test_type_id = tt.id
            LEFT JOIN samples s ON s.request_id = tr.id
            LEFT JOIN results r ON r.request_id = tr.id
            ORDER BY tr.requested_at DESC
            """;

        java.util.List<TestRequest> requests = new java.util.ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
            requests.add(mapRequest(resultSet));
            }
        }

        return requests;
    }
    

    private TestRequest mapRequest(ResultSet resultSet) throws SQLException {
        return new TestRequest(
                resultSet.getInt("id"),
                resultSet.getInt("customer_id"),
                resultSet.getString("customer_name"),
                resultSet.getInt("test_type_id"),
                resultSet.getString("test_name"),
                resultSet.getString("test_category"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("tat_hours"),
                resultSet.getString("result_format"),
                resultSet.getString("request_status"),
                resultSet.getString("payment_status"),
                resultSet.getString("sample_status"),
                resultSet.getString("result_status"),
                resultSet.getTimestamp("requested_at").toLocalDateTime(),
                resultSet.getTimestamp("estimated_ready_at") == null
                        ? null
                        : resultSet.getTimestamp("estimated_ready_at").toLocalDateTime()
        );
    }
}