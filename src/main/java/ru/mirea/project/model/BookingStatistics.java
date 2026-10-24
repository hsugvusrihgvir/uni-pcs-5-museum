package ru.mirea.project.model;

// неизменяемый набор значений для вкладки со статистикой
public record BookingStatistics(
        long visitors,
        long bookings,
        long completed,
        long cancelled,
        double completedRevenue
) {
}
