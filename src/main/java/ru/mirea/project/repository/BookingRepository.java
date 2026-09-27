package ru.mirea.project.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatus;
import ru.mirea.project.util.DatabaseManager;


public class BookingRepository {

    //2.3: Получение бронирования по id из PostgreSQL
    public Optional<Booking> findById(long id) {
        String sql = "SELECT id, visitor_id, exhibition_id, visit_date, status, price FROM bookings WHERE id = ?";

        // try-with-resources автоматически закроет connection, statement и resultSet
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // подставляем ID вместо знака вопроса
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // сборка объекта Booking из строчки в базе данных
                    Booking booking = new Booking();
                    booking.setId(resultSet.getLong("id"));
                    booking.setVisitorId(resultSet.getLong("visitor_id"));
                    booking.setExhibitionId(resultSet.getLong("exhibition_id"));
                    
                    // Переводим SQL-дату в Java-дату
                    booking.setVisitDate(resultSet.getDate("visit_date").toLocalDate());
                    
                    // Превращаем строку из базы (строчный тип VARCHAR) обратно в наш Enum
                    booking.setStatus(BookingStatus.valueOf(resultSet.getString("status")));
                    
                    // Цена может быть NULL, обрабатываем это аккуратно
                    double price = resultSet.getDouble("price");
                    booking.setPrice(resultSet.wasNull() ? null : price);

                    return Optional.of(booking);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // в будущем тут пишется логирование
        }
        return Optional.empty();
    }

    /**
     * ПУНКТ 2.5: Удаление строки из таблицы bookings по ID
     */
    public boolean deleteById(long id) {
        String sql = "DELETE FROM bookings WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            int rowsDeleted = statement.executeUpdate();
            
            return rowsDeleted > 0; // true, если строчка реально удалилась
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
