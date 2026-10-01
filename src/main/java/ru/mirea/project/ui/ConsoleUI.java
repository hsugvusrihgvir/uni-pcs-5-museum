package ru.mirea.project.ui;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatus;
import ru.mirea.project.model.Visitor;
import ru.mirea.project.service.BookingService;
import ru.mirea.project.service.ExhibitionService;
import ru.mirea.project.service.VisitorService;
import ru.mirea.project.repository.BookingRepository;
import ru.mirea.project.repository.ExhibitionRepository;
import ru.mirea.project.repository.VisitorRepository;
import ru.mirea.project.util.ExcelExporter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    // чтобы только один объект мог быть
    private static final ConsoleUI INSTANCE = new ConsoleUI();
    // System.in - это стандартный поток ввода (из консоли)
    private final Scanner scanner = new Scanner(System.in);

    private final BookingRepository bookingRepository = new BookingRepository();
    private final ExhibitionRepository exhibitionRepository = new ExhibitionRepository();
    private final VisitorRepository visitorRepository = new VisitorRepository();

    // СЕРВИСЫ
    private final ExhibitionService exhibitionService = new ExhibitionService(exhibitionRepository);
    private final VisitorService visitorService = new VisitorService(visitorRepository);
    private final BookingService bookingService = new BookingService(bookingRepository, visitorService, exhibitionService);

    // КОНСТРУКТОР
    // чтобы только один объект мог быть
    private ConsoleUI(){}

    public static ConsoleUI getInstance() {
        return INSTANCE;
    }

    // старт работы приложения
    public void start(){
        showMenu();
    }

    // МЕТОДЫ ДЛЯ ВЫВОДА
    // вывод как в питоне чтобы #evil
    // args - массив
    // Object - общий родитель почти всех объектов в Java
    // поэтому туда можно передать значения разных типов
    private void print(String str) {
        System.out.println(str);
    }

    private void print(String str, Object... args) {
        System.out.printf(str, args);
        System.out.println();
    }

    // МЕТОДЫ ДЛЯ ЧТЕНИЯ
    // метод для норм чтения чисел менюшных
    private int choiceReader(int minNumber, int maxNumber){
        while (true) {
            // nextInt() это метод кот. читает целое число из ввода
            // лучше использовать scanner.nextLine(), т.к. могут быть проблемы
            // с переводом строки из-за nextInt() при посл. исп. nextLine()
            // лучше посмотерть подробнее
            try {
                int c = Integer.parseInt(scanner.nextLine().trim());
                if (c < minNumber || c > maxNumber) {
                    print("Пожалуйста, введите целое число в диапазоне %d - %d. :(", minNumber, maxNumber);
                    continue;
                }
                return c;

            } catch (NumberFormatException e) {
                print("Пожалуйста, введите целое число! :(");
            }
        }
    }

    private long idReader(String objectName){
        while (true) {
            try {
                print("Пожалуйста, введите id нужного вам " + objectName + ":");
                long id = Long.parseLong(scanner.nextLine().trim());
                return id;
            } catch (NumberFormatException e) {
                print("id " + objectName + " это целое число.");
            }
        }
    }

    private String textReader(String message) {
        print(message);
        return scanner.nextLine().trim();
    }

    private LocalDate dateReader(String message) {
        while (true) {
            try {
                print(message + " (ГГГГ-ММ-ДД):");
                return LocalDate.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException e) {
                print("Пожалуйста, введите дату в формате ГГГГ-ММ-ДД.");
            }
        }
    }

    private LocalDate nullableDateReader(String message) {
        while (true) {
            try {
                print(message + " (ГГГГ-ММ-ДД, Enter — не указывать):");
                String value = scanner.nextLine().trim();
                return value.isEmpty() ? null : LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                print("Пожалуйста, введите дату в формате ГГГГ-ММ-ДД.");
            }
        }
    }

    private Double nullablePriceReader() {
        while (true) {
            try {
                print("Введите цену (Enter — не указывать):");
                String value = scanner.nextLine().trim();
                return value.isEmpty() ? null : Double.parseDouble(value.replace(',', '.'));
            } catch (NumberFormatException e) {
                print("Пожалуйста, введите число или оставьте строку пустой.");
            }
        }
    }

    private BookingStatus bookingStatusReader() {
        while (true) {
            print("Введите статус (CREATED, CONFIRMED, COMPLETED, CANCELLED):");
            String value = scanner.nextLine().trim().toUpperCase();
            try {
                return BookingStatus.valueOf(value);
            } catch (IllegalArgumentException e) {
                print("Такого статуса нет.");
            }
        }
    }

    private void printList(List<?> items, String emptyMessage) {
        if (items.isEmpty()) {
            print(emptyMessage);
            return;
        }

        for (Object item : items) {
            print(item.toString());
        }
    }

    // МЕТОДЫ ДЛЯ МЕНЮ
    // вывод меню
    private void showMenu(){
        while (true) {
            print("""
                1. Посетители
                2. Управление бронированиями
                3. Поиск бронирований
                4. Фильтрация бронирований
                5. Сортировка бронирований
                6. Статистика
                7. Экспорт данных
                8. Вывести таблицы базы данных
                0. Выход
                
                Выберите действие:
                """);

            int c = choiceReader(0, 8);
            switch (c) {
                case 1 -> visitorMenu();
                case 2 -> bookingMenu();
                case 3 -> searchMenu();
                case 4 -> filterMenu();
                case 5 -> sortingMenu();
                case 6 -> statisticsMenu();
                case 7 -> {
                    try {
                        Path exportFile = ExcelExporter.exportAll(
                                visitorService.getAllVisitors(),
                                exhibitionService.getAllExhibitions(),
                                bookingService.getSortedBookings(1)
                        );
                        print("Данные экспортированы в файл:");
                        print(exportFile.toString());
                    } catch (IllegalStateException e) {
                        print(e.getMessage());
                    }
                }
                case 8 -> bdMenu();
                case 0 -> {
                    print("Выход...");
                    return;
                }

            }
        }

    }

    private void visitorMenu() {
        while (true) {
            print("""
                1. Создать посетителя (регистрация)
                2. Просмотр всех клиентов
                0. Назад
                
                Выберите действие:""");

            int c = choiceReader(0, 2);
            switch (c) {
                case 1 -> {
                    try {
                        String name = textReader("Введите имя:");
                        String lastName = textReader("Введите фамилию:");
                        String patronymic = textReader("Введите отчество (Enter — не указывать):");
                        if (patronymic.isEmpty()) {
                            patronymic = null;
                        }
                        LocalDate birthDate = nullableDateReader("Введите дату рождения");
                        String email = textReader("Введите email:");
                        String passwordHash = textReader("Введите хеш пароля:");

                        Visitor visitor = visitorService.createVisitor(
                                name,
                                lastName,
                                patronymic,
                                birthDate,
                                email,
                                passwordHash
                        );
                        print("Посетитель создан:");
                        print(visitor.toString());
                    } catch (BusinessException e) {
                        print(e.getMessage());
                    } catch (IllegalStateException e) {
                        print(e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        printList(visitorService.getAllVisitors(), "Посетители не найдены.");
                    } catch (IllegalStateException e) {
                        print(e.getMessage());
                    }
                }
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }

    }

    private void bookingMenu() {
        while (true) {
            print("""
                1. Создание бронирования - статус CREATED если цена NULL, иначе CONFIRMED
                2. Вывод всех бронирований
                3. Получение бронирования по ID
                4. Изменение бронирования (по ID) — статуса или добавляем цену (переход в CONFIRMED)
                5. Удаление бронирования
                0. Назад

                Выберите действие:""");

            int c = choiceReader(0, 5);
            switch (c) {
                case 1 -> {
                    try {
                        long visitorId = idReader("посетителя");
                        long exhibitionId = idReader("выставки");
                        LocalDate visitDate = dateReader("Введите дату посещения");
                        Double price = nullablePriceReader();

                        Booking booking = bookingService.createBooking(
                                visitorId,
                                exhibitionId,
                                visitDate,
                                price
                        );
                        print("Бронирование создано:");
                        print(booking.toString());
                    } catch (BusinessException | EntityNotFoundException e) {
                        print(e.getMessage());
                    }
                }
                case 2 -> {
                    printList(
                            bookingService.getSortedBookings(1),
                            "Бронирования не найдены."
                    );
                }

                case 3 -> {
                    try {
                        long bookingId = idReader("бронирования");
                        Booking booking = bookingService.getBookingById(bookingId);
                        print(booking.toString());
                    } catch (EntityNotFoundException e) {
                        print(e.getMessage());
                    }
                }
                case 4 -> {
                    try {
                        long bookingId = idReader("бронирования");
                        BookingStatus status = bookingStatusReader();
                        Double price = nullablePriceReader();

                        Booking booking = bookingService.updateBooking(bookingId, status, price);
                        print("Бронирование изменено:");
                        print(booking.toString());
                    } catch (BusinessException | EntityNotFoundException e) {
                        print(e.getMessage());
                    }
                }
                case 5 -> {
                    try {
                        long bookingId = idReader("бронирования");
                        bookingService.deleteBooking(bookingId);
                        print("Бронирование удалено.");
                    } catch (EntityNotFoundException e) {
                        print(e.getMessage());
                    }
                }
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }

    private void searchMenu() {
        while (true) {
            print("""
                1. Поиск по ID посетителя
                2. Поиск по названию выставки
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 2);

            switch (c) {
                case 1 -> {
                    try {
                        long visitorId = idReader("посетителя");
                        printList(
                                bookingService.getBookingsByVisitorId(visitorId),
                                "Бронирования не найдены."
                        );
                    } catch (BusinessException e) {
                        print(e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        String title = textReader("Введите название выставки:");
                        printList(
                                bookingService.getBookingsByExhibitionTitle(title),
                                "Бронирования не найдены."
                        );
                    } catch (BusinessException e) {
                        print(e.getMessage());
                    }
                }
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }


    private void filterMenu() {
        while (true) {
            print("""
                1. Фильтр по статусу
                2. Фильтр по диапазону дат
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 2);

            switch (c) {
                case 1 -> {
                    BookingStatus status = bookingStatusReader();
                    printList(
                            bookingService.filterBookingsByStatus(status),
                            "Бронирования не найдены."
                    );
                }
                case 2 -> {
                    try {
                        LocalDate start = dateReader("Введите начало диапазона");
                        LocalDate end = dateReader("Введите конец диапазона");
                        printList(
                                bookingService.filterBookingsByDateRange(start, end),
                                "Бронирования не найдены."
                        );
                    } catch (BusinessException e) {
                        print(e.getMessage());
                    }
                }
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }


    private void sortingMenu() {
        while (true) {
            print("""
                1. Сортировка по дате (сначала новые)
                2. Сортировка по цене (сначала дорогие)
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 2);

            switch (c) {
                case 1 -> {
                    printList(
                            bookingService.getSortedBookings(1),
                            "Бронирования не найдены."
                    );
                }
                case 2 -> {
                    printList(
                            bookingService.getSortedBookings(2),
                            "Бронирования не найдены."
                    );
                }
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }


    private void statisticsMenu() {
        try {
            print("Всего клиентов в системе: %d", visitorService.getAllVisitors().size());
        } catch (IllegalStateException e) {
            print(e.getMessage());
        }
        bookingService.printStatistics();
    }


    private void bdMenu() {
        while (true) {
            print("""
                1. Выставки
                2. Посетители
                3. Бронирования
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 3);

            switch (c) {
                case 1 -> {
                    printList(
                            exhibitionService.getAllExhibitions(),
                            "Выставки не найдены."
                    );
                }
                case 2 -> {
                    try {
                        printList(
                                visitorService.getAllVisitors(),
                                "Посетители не найдены."
                        );
                    } catch (IllegalStateException e) {
                        print(e.getMessage());
                    }
                }
                case 3 -> {
                    printList(
                            bookingService.getSortedBookings(1),
                            "Бронирования не найдены."
                    );
                }
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }

}

