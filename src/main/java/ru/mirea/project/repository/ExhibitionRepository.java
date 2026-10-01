package ru.mirea.project.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.mirea.project.model.Exhibition;
import ru.mirea.project.util.DatabaseManager;

public class ExhibitionRepository {

    private static final Logger LOGGER = Logger.getLogger(ExhibitionRepository.class.getName());

    // --- Маппинг ---
    private void mapExhibitionToStatement(PreparedStatement statement, Exhibition exhibition) throws SQLException {
        statement.setString(1, exhibition.getTitle());
        statement.setString(2, exhibition.getDescription());
        statement.setString(3, exhibition.getPhotoUrl());
        statement.setDate(4, Date.valueOf(exhibition.getStartDate()));
        statement.setDate(5, Date.valueOf(exhibition.getEndDate()));
        statement.setInt(6, exhibition.getHallNumber());
    }

    private Exhibition mapResultSetToExhibition(ResultSet rs) throws SQLException {
        Exhibition exhibition = new Exhibition();
        exhibition.setId(rs.getLong("id"));
        exhibition.setTitle(rs.getString("title"));
        exhibition.setDescription(rs.getString("description"));
        exhibition.setPhotoUrl(rs.getString("photo_url"));
        exhibition.setStartDate(rs.getDate("start_date").toLocalDate());
        exhibition.setEndDate(rs.getDate("end_date").toLocalDate());
        exhibition.setHallNumber(rs.getInt("hall_number"));
        return exhibition;
    }

    // --- CRUD ---

    public Exhibition save(Exhibition exhibition) {
        boolean isNew = (exhibition.getId() == 0);
        String sql = isNew
                ? "INSERT INTO exhibitions (title, description, photo_url, start_date, end_date, hall_number) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE exhibitions SET title = ?, description = ?, photo_url = ?, start_date = ?, end_date = ?, hall_number = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, isNew ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {

            mapExhibitionToStatement(statement, exhibition);

            if (!isNew) {
                statement.setLong(7, exhibition.getId());
            }

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalStateException("Выставка не была сохранена.");
            }

            if (isNew) {
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        exhibition.setId(keys.getLong(1));
                    } else {
                        throw new IllegalStateException("База данных не вернула ID созданной выставки.");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save/update exhibition", e);
            throw new IllegalStateException("Не удалось сохранить выставку в базе данных.", e);
        }
        return exhibition;
    }

    public Optional<Exhibition> findById(long id) {
        String sql = "SELECT id, title, description, photo_url, start_date, end_date, hall_number FROM exhibitions WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToExhibition(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find exhibition by id", e);
            throw new IllegalStateException("Не удалось получить выставку из базы данных.", e);
        }
        return Optional.empty();
    }

    public List<Exhibition> findAll() {
        List<Exhibition> list = new ArrayList<>();
        String sql = "SELECT id, title, description, photo_url, start_date, end_date, hall_number FROM exhibitions ORDER BY start_date DESC";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToExhibition(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find all exhibitions", e);
            throw new IllegalStateException("Не удалось получить список выставок из базы данных.", e);
        }
        return list;
    }

    public boolean deleteById(long id) {
        String sql = "DELETE FROM exhibitions WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete exhibition by id", e);
            throw new IllegalStateException("Не удалось удалить выставку из базы данных.", e);
        }
    }


    public boolean existsById(long id) {
        String sql = "SELECT 1 FROM exhibitions WHERE id = ? LIMIT 1";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next(); 
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to check existence of exhibition by id", e);
            throw new IllegalStateException("Не удалось проверить существование выставки в базе данных.", e);
        }
    }
}
