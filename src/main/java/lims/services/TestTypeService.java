package lims.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lims.database.DatabaseConnection;
import lims.models.TestType;

public class TestTypeService {

    public List<TestType> getAllTestTypes() throws SQLException {
        String sql = """
                SELECT id, name, category, price, tat_hours, result_format,
                       description, active, created_at
                FROM test_types
                ORDER BY id DESC
                """;

        List<TestType> testTypes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                testTypes.add(mapTestType(resultSet));
            }
        }

        return testTypes;
    }

    public List<TestType> getActiveTestTypes() throws SQLException {
        String sql = """
                SELECT id, name, category, price, tat_hours, result_format,
                       description, active, created_at
                FROM test_types
                WHERE active = true
                ORDER BY category, name
                """;

        List<TestType> testTypes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                testTypes.add(mapTestType(resultSet));
            }
        }

        return testTypes;
    }

    public boolean createTestType(TestType testType) throws SQLException {
        String sql = """
                INSERT INTO test_types
                (name, category, price, tat_hours, result_format, description, active)
                VALUES (?, ?, ?, ?, ?, ?, true)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, testType.getName());
            statement.setString(2, testType.getCategory());
            statement.setBigDecimal(3, testType.getPrice());
            statement.setInt(4, testType.getTatHours());
            statement.setString(5, testType.getResultFormat());
            statement.setString(6, testType.getDescription());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateTestType(TestType testType) throws SQLException {
        String sql = """
                UPDATE test_types
                SET name = ?, category = ?, price = ?, tat_hours = ?,
                    result_format = ?, description = ?, active = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, testType.getName());
            statement.setString(2, testType.getCategory());
            statement.setBigDecimal(3, testType.getPrice());
            statement.setInt(4, testType.getTatHours());
            statement.setString(5, testType.getResultFormat());
            statement.setString(6, testType.getDescription());
            statement.setBoolean(7, testType.isActive());
            statement.setInt(8, testType.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deactivateTestType(int testTypeId) throws SQLException {
        String sql = "UPDATE test_types SET active = false WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, testTypeId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteTestType(int testTypeId) throws SQLException {
        String sql = "DELETE FROM test_types WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, testTypeId);
            return statement.executeUpdate() > 0;
        }
    }

    private TestType mapTestType(ResultSet resultSet) throws SQLException {
        return new TestType(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("tat_hours"),
                resultSet.getString("result_format"),
                resultSet.getString("description"),
                resultSet.getBoolean("active"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }
}