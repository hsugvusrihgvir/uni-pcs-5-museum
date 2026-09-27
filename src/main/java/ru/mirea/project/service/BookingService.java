package ru.mirea.project.service;

import java.time.LocalDate;
import java.util.List;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatus;
import ru.mirea.project.repository.BookingRepository;

public class BookingService {

    private final BookingRepository bookingRepository;

    // сервис принимает репозиторий для работы с базой данных
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // 2.1: Создание бронирования
    // Правила 1, 2 и 5
    public Booking createBooking(long visitorId, long exhibitionId, LocalDate visitDate) {
        
        // Правило 1 (Существование сущности)
        // потом здесь добавятся проверки через visitorRepository и exhibitionRepository
        if (visitorId <= 0 || exhibitionId <= 0) {
            throw new BusinessException("Ошибка: Указан несуществующий ID посетителя или выставки!");
        }

        // Правило 2 (Логика дат работы выставки)
        // временные рамки выставки на 2026 год. Позже даты будут запрашиваться из бд
        LocalDate exhibitionStart = LocalDate.of(2026, 1, 1);
        LocalDate exhibitionEnd = LocalDate.of(2026, 12, 31);
        
        if (visitDate.isBefore(exhibitionStart) || visitDate.isAfter(exhibitionEnd)) {
            throw new BusinessException("Ошибка: Дата сеанса (" + visitDate + ") выходит за рамки работы выставки!");
        }

        // Правило 5 (Авто-статус при создании)
        Booking booking = new Booking();
        booking.setVisitorId(visitorId);
        booking.setExhibitionId(exhibitionId);
        booking.setVisitDate(visitDate);
        booking.setStatus(BookingStatus.CREATED); // строго CREATED
        booking.setPrice(null); // NULL 

        // Отправляем репозиторию команду на сохранение новой записи в PostgreSQL
        // ЗАМЕТКА БЭКЕНДЕРУ: В BookingRepository нужно будет дописать метод save()
        // return bookingRepository.save(booking); 
        return booking; 
    }

    // 2.3: Получение бронирования по ID
    public Booking getBookingById(long id) {
        // метод findById из репозитория
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ошибка: Бронирование с ID " + id + " не найдено в базе данных!"));
    }

    //2.4: Изменение бронирования (статуса и цены)
    // Правила 3 и 4
    public Booking updateBooking(long id, BookingStatus newStatus, Double newPrice) {
        // достаем текущую запись, чтобы проверить её статус
        Booking booking = getBookingById(id);
        BookingStatus oldStatus = booking.getStatus();

        // --- Правило 4 (Финальные статусы и заморозка данных) ---
        if (oldStatus == BookingStatus.COMPLETED || oldStatus == BookingStatus.CANCELLED) {
            throw new BusinessException("Ошибка: Бронирование находится в конечном статусе (" + oldStatus + "). Изменение любых данных запрещено!");
        }

        // --- Правило 3 (Запрет шага назад) ---
        if ((oldStatus == BookingStatus.CONFIRMED) && newStatus == BookingStatus.CREATED) {
            throw new BusinessException("Ошибка: Нельзя вернуть билет из статуса CONFIRMED обратно в CREATED!");
        }

        // переход в CONFIRMED (2.4: добавляем цену)
        if (newStatus == BookingStatus.CONFIRMED) {
            if (newPrice == null || newPrice <= 0) {
                throw new BusinessException("Ошибка: Для подтверждения бронирования необходимо указать цену билета!");
            }
            booking.setPrice(newPrice);
        }

        booking.setStatus(newStatus);
        
        return bookingRepository.save(booking);
    }

    // 2.5: Удаление бронирования по ID
    public void deleteBooking(long id) {
        // проверяем, существует ли запись
        getBookingById(id); 

        bookingRepository.deleteById(id);
    }

    // 3.1
    public List<Booking> getBookingsByVisitorId(long visitorId) {
        if (visitorId <= 0) throw new BusinessException("Некорректный ID посетителя");
        return bookingRepository.findByVisitorId(visitorId);
    }

    // 3.2
    public List<Booking> getBookingsByExhibitionTitle(String title) {
        if (title == null || title.trim().isEmpty()) throw new BusinessException("Название выставки не может быть пустым");
        return bookingRepository.searchByExhibitionTitle(title);
    }

    // 4.1
    public List<Booking> filterBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    // 4.2
    public List<Booking> filterBookingsByDateRange(LocalDate start, LocalDate end) {
        // логика дат (Правило 2)
        if (start.isAfter(end)) {
            throw new BusinessException("Ошибка: Дата начала не может быть позже даты окончания фильтра!");
        }
        return bookingRepository.findByDateRange(start, end);
    }

    // 5.1 и 5.2
    public List<Booking> getSortedBookings(int option) {
        if (option == 1) return bookingRepository.findAllSortedByDateNewestFirst();
        else return bookingRepository.findAllSortedByPriceExpensiveFirst();
    }

    // Пункт 6: Сборка статистики
    public void printStatistics() {
        // нужен вызов еще из визитора все посетители типа
        long totalBookings = bookingRepository.countAll();
        long completed = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long cancelled = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        double money = bookingRepository.sumPriceForCompleted();

        // ...
    }
}
