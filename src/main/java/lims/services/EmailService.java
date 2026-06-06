package lims.services;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import lims.database.DatabaseConnection;

public class EmailService {

    /*
     * For demo:
     * Leave SMTP_USERNAME and SMTP_PASSWORD empty and the app will print
     * the email message to the console instead of crashing.
     *
     * For real Gmail SMTP:
     * SMTP_HOST = "smtp.gmail.com"
     * SMTP_PORT = "587"
     * SMTP_USERNAME = your Gmail address
     * SMTP_PASSWORD = your Gmail App Password, not your normal Gmail password
     */
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = "";
    private static final String SMTP_PASSWORD = "";

    private static final String FROM_EMAIL = "no-reply@santediagnostics.com";
    private static final String FROM_NAME = "Sante Diagnostics Ltd";

    public void sendResultReadyEmailForResult(int resultId) throws SQLException {
        String sql = """
                SELECT
                    u.email AS customer_email,
                    u.full_name AS customer_name,
                    tt.name AS test_name,
                    tr.id AS request_id
                FROM results r
                JOIN test_requests tr ON r.request_id = tr.id
                JOIN users u ON tr.customer_id = u.id
                JOIN test_types tt ON tr.test_type_id = tt.id
                WHERE r.id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, resultId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String customerEmail = resultSet.getString("customer_email");
                    String customerName = resultSet.getString("customer_name");
                    String testName = resultSet.getString("test_name");
                    int requestId = resultSet.getInt("request_id");

                    String subject = "Your Sante Diagnostics Result is Ready";
                    String body = buildResultReadyEmail(customerName, testName, requestId);

                    sendEmail(customerEmail, subject, body);
                }
            }
        }
    }

    public void sendEmail(String recipientEmail, String subject, String body) {
        if (SMTP_USERNAME.isBlank() || SMTP_PASSWORD.isBlank()) {
            printDemoEmail(recipientEmail, subject, body);
            return;
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session mailSession = Session.getInstance(
                properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                    }
                }
        );

        try {
            Message message = new MimeMessage(mailSession);

            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail)
            );
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("Email sent successfully to " + recipientEmail);

        } catch (Exception e) {
            System.err.println("Email sending failed: " + e.getMessage());
            printDemoEmail(recipientEmail, subject, body);
        }
    }

    private String buildResultReadyEmail(String customerName, String testName, int requestId) {
        return """
                Hello %s,

                Your laboratory result is now ready.

                Test: %s
                Request ID: %d

                Please log in to the Sante Diagnostics LIMS application and open your Result Vault to view your validated result.

                Regards,
                Sante Diagnostics Ltd
                """.formatted(customerName, testName, requestId);
    }

    private void printDemoEmail(String recipientEmail, String subject, String body) {
        System.out.println("\n================ DEMO EMAIL NOTIFICATION ================");
        System.out.println("To: " + recipientEmail);
        System.out.println("Subject: " + subject);
        System.out.println();
        System.out.println(body);
        System.out.println("=========================================================\n");
    }
}