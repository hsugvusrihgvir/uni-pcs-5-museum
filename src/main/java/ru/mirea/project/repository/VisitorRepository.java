package ru.mirea.project.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.mirea.project.model.Visitor;
import ru.mirea.project.util.DatabaseManager;

public class VisitorRepository {

    private static final Logger LOGGER = Logger.getLogger(VisitorRepository.class.getName());

    // --- Маппинг ---
    private void mapVisitorToStatement(PreparedStatement statement, Visitor visitor) throws SQLException {
        statement.setString(1, visitor.getName());
        statement.setString(2, visitor.getLastName());
        statement.setString(3, visitor.getPatronymic());

        if (visitor.getBirthDate() == null) {
            statement.setNull(4, Types.DATE);
        } else {
            statement.setDate(4, Date.valueOf(visitor.getBirthDate()));
        }

        statement.setString(5, visitor.getEmail());
        statement.setString(6, visitor.getPasswordHash());
    }

    private Visitor mapResultSetToVisitor(ResultSet resultSet) throws SQLException {
        Visitor visitor = new Visitor();
        visitor.setId(resultSet.getLong("id"));
        visitor.setName(resultSet.getString("name"));
        visitor.setLastName(resultSet.getString("last_name"));
        visitor.setPatronymic(resultSet.getString("patronymic"));

        Date birthDate = resultSet.getDate("birth_date");
        visitor.setBirthDate(birthDate == null ? null : birthDate.toLocalDate());

        visitor.setEmail(resultSet.getString("email"));
        visitor.setPasswordHash(resultSet.getString("password_hash"));
        return visitor;
    }

    // --- CRUD ---

    public Visitor save(Visitor visitor) {
        String sql = "INSERT INTO visitor "
                   + "(name, last_name, patronymic, birth_date, email, password_hash) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            mapVisitorToStatement(statement, visitor);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    visitor.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save visitor", e);
        }
        return visitor;
    }

    public List<Visitor> findAll() {
        List<Visitor> visitors = new ArrayList<>();
        String sql = "SELECT id, name, last_name, patronymic, birth_date, email, password_hash "
                   + "FROM visitor ORDER BY id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                visitors.add(mapResultSetToVisitor(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find all visitors", e);
        }
        return visitors;
    }

    public boolean existsById(long id) {
        String sql = "SELECT 1 FROM visitor WHERE id = ? LIMIT 1";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to check existence of visitor by id", e);
            return false;
        }
    }
}
