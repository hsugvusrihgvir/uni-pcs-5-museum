package ru.mirea.project.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatus;
import ru.mirea.project.util.DatabaseManager;


public class BookingRepository {

    private static final Logger LOGGER = Logger.getLogger(BookingRepository.class.getName());

    // вспомогательные методы для маппинга Booking в PreparedStatement и ResultSet в Booking
    private void mapBookingToStatement(PreparedStatement statement, Booking booking) throws SQLException {
        statement.setLong(1, booking.getVisitorId());
        statement.setLong(2, booking.getExhibitionId());
        statement.setDate(3, Date.valueOf(booking.getVisitDate()));
        statement.setString(4, booking.getStatus().name());
        
        if (booking.getPrice() == null) {
            statement.setNull(5, Types.DOUBLE);
        } else {
            statement.setDouble(5, booking.getPrice());
        }
    }

    private Booking mapResultSetToBooking(ResultSet resultSet) throws SQLException {
        Booking booking = new Booking();
        booking.setId(resultSet.getLong("id"));
        booking.setVisitorId(resultSet.getLong("visitor_id"));
        booking.setExhibitionId(resultSet.getLong("exhibition_id"));
        booking.setVisitDate(resultSet.getDate("visit_date").toLocalDate());
        booking.setStatus(BookingStatus.valueOf(resultSet.getString("status")));
        
        double price = resultSet.getDouble("price");
        booking.setPrice(resultSet.wasNull() ? null : price);
        return booking;
    }

    // === CRUD методы для работы с bookings в постгресе ===

    // 2.1: сохранение нового бронирования 
    // 2.4: изменение существующего
    public Booking save(Booking booking) {
        boolean isNew = (booking.getId() == 0);
        String sql = isNew 
            ? "INSERT INTO bookings (visitor_id, exhibition_id, visit_date, status, price) VALUES (?, ?, ?, ?, ?)"
            : "UPDATE bookings SET visitor_id = ?, exhibition_id = ?, visit_date = ?, status = ?, price = ? WHERE id = ?";
            
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, isNew ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
            
            // Заполняем общие 5 параметров для INSERT и UPDATE
            mapBookingToStatement(statement, booking);
            
            if (!isNew) {
                // UPDATE, дописываем шестой параметр WHERE id = ?
                statement.setLong(6, booking.getId());
            }
            
            statement.executeUpdate();
            
            // запись новая, забираем созданный базой ид
            if (isNew) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        booking.setId(generatedKeys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save/update booking", e);
        }
        return booking;
    }

    // 2.2: вывод всех бронирований из bookings
    public List<Booking> findAll() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT id, visitor_id, exhibition_id, visit_date, status, price FROM bookings";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Booking booking = mapResultSetToBooking(resultSet);
                bookings.add(booking);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve all bookings", e);
        }
        return bookings;
    }

    //2.3: gолучение бронирования по id
    public Optional<Booking> findById(long id) {
        String sql = "SELECT id, visitor_id, exhibition_id, visit_date, status, price FROM bookings WHERE id = ?";

        // try-with-resources автоматически закроет connection, statement и resultSet
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // подставляем ИД вместо знака вопроса
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToBooking(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find booking by id", e);
        }
        return Optional.empty();
    }

    //2.5: удаление строки из bookings по id
    public boolean deleteById(long id) {
        String sql = "DELETE FROM bookings WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0; // true, если строчка реально удалилась
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete booking by id", e);
            return false;
        }
    }

    // 3. Поиск бронирований
    // 3.1 Поиск по ID посетителя
    public List<Booking> searchByVisitorId(long visitorId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT id, visitor_id, exhibition_id, visit_date, status, price "
                   + "FROM bookings WHERE visitor_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, visitorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(mapResultSetToBooking(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to search bookings by visitor id", e);
        }
        return bookings;
    }

    // 3.2 Поиск по названию выставки
    public List<Booking> searchByExhibitionTitle(String title) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.id, b.visitor_id, b.exhibition_id, b.visit_date, b.status, b.price "
                   + "FROM bookings b "
                   + "JOIN exhibitions e ON b.exhibition_id = e.id "
                   + "WHERE e.title LIKE ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + title + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(mapResultSetToBooking(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to search bookings by exhibition title", e);
        }
        return bookings;
    }

    // 4. Фильтрация
    // 4.1 Фильтр по статусу 
    public List<Booking> findByStatus(BookingStatus status) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT id, visitor_id, exhibition_id, visit_date, status, price "
                   + "FROM bookings WHERE status = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(mapResultSetToBooking(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to filter bookings by status", e);
        }
        return bookings;
    }

    // 4.2 Фильтр по диапазону дат
    public List<Booking> findByDateRange(LocalDate start, LocalDate end) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT id, visitor_id, exhibition_id, visit_date, status, price "
                   + "FROM bookings WHERE visit_date BETWEEN ? AND ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(start));
            statement.setDate(2, Date.valueOf(end));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(mapResultSetToBooking(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to filter bookings by date range", e);
        }
        return bookings;
    }

    // 5. Сортировка
    // 5.1 Сортировка по дате (сначала новые)
    public List<Booking> findAllSortedByDateNewestFirst() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT id, visitor_id, exhibition_id, visit_date, status, price "
                   + "FROM bookings ORDER BY visit_date DESC";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                bookings.add(mapResultSetToBooking(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve bookings sorted by date", e);
        }
        return bookings;
    }

    // 5.2 Сортировка по цене (сначала дорогие)
    public List<Booking> findAllSortedByPriceExpensiveFirst() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT id, visitor_id, exhibition_id, visit_date, status, price "
                   + "FROM bookings ORDER BY price DESC NULLS LAST";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                bookings.add(mapResultSetToBooking(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve bookings sorted by price", e);
        }
        return bookings;
    }

    // 6. Статистика: 

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM bookings";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to count bookings", e);
        }
        return 0;
    }

    public long countByStatus(BookingStatus status) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to count bookings by status", e);
        }
        return 0;
    }

    public double sumPriceForCompleted() {
        String sql = "SELECT SUM(price) FROM bookings WHERE status = 'COMPLETED'";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                double sum = resultSet.getDouble(1);
                return resultSet.wasNull() ? 0.0 : sum;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to sum price for completed bookings", e);
        }
        return 0.0;
    }
}
