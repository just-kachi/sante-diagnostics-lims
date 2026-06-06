package lims.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lims.database.DatabaseConnection;

public class PaymentService {
    private final AuditLogService auditLogService = new AuditLogService();

    public boolean markRequestAsPaid(int requestId, int markedByUserId) throws SQLException {
        String updateRequestSql = """
                UPDATE test_requests
                SET payment_status = 'PAID'
                WHERE id = ?
                """;

        String updatePaymentSql = """
                UPDATE payments
                SET payment_status = 'PAID',
                    marked_paid_by = ?,
                    marked_paid_at = CURRENT_TIMESTAMP
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int requestRows;

                try (PreparedStatement requestStatement = connection.prepareStatement(updateRequestSql)) {
                    requestStatement.setInt(1, requestId);
                    requestRows = requestStatement.executeUpdate();
                }

                int paymentRows;

                try (PreparedStatement paymentStatement = connection.prepareStatement(updatePaymentSql)) {
                    paymentStatement.setInt(1, markedByUserId);
                    paymentStatement.setInt(2, requestId);
                    paymentRows = paymentStatement.executeUpdate();
                }

                if (requestRows > 0 && paymentRows > 0) {
                    connection.commit();

                    auditLogService.log(
                            markedByUserId,
                            "PAYMENT_MARKED_PAID",
                            "TEST_REQUEST",
                            requestId,
                            "Payment marked as PAID for request ID: " + requestId
                    );

                    return true;
                }

                connection.rollback();
                return false;

            } catch (SQLException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}