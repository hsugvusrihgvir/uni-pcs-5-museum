package ru.mirea.project.ui;

import java.util.Scanner;

public class ConsoleUI {
    // чтобы только один объект мог быть
    private static final ConsoleUI INSTANCE = new ConsoleUI();

    // final - как const почти
    // System.in - это стандартный поток ввода (из консоли)
    private final Scanner scanner = new Scanner(System.in);

    public void start(){
        showMenu();
    }

    // чтобы только один объект мог быть
    public static ConsoleUI getInstance() {
        return INSTANCE;
    }

    private ConsoleUI(){}


    // вывод как в питоне хаха
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

    // вывод меню
    private void showMenu(){
        while (true) {
            print("""
                1. Произведения искусства
                2. Авторы
                3. Выставки
                4. Посетители
                5. Сотрудники
                6. Поиск
                7. Фильтрация и сортировка
                8. Статистика
                9. Экспорт данных
                10. Вывести таблицы базы данных
                0. Выход
                
                Выберите действие:
                """);

            int c = choiceReader(0, 10);
            switch (c) {
                case 1 -> artworkMenu();
                case 2 -> authorMenu();
                case 3 -> exhibitionMenu();
                case 4 -> visitorMenu();
                case 5 -> employeeMenu();
                case 6 -> searchMenu();
                case 7 -> sortingMenu();
                case 8 -> statisticsMenu();
                case 9 -> exportMenu();
                case 10 -> bdMenu();
                case 0 -> {
                    print("Выход...");
                    return;
                }

            }
        }

    }

    private void artworkMenu(){
        while (true) {
            print("""
                1. Добавить произведение
                2. Показать все произведения
                3. Найти произведение по ID
                4. Изменить произведение
                5. Удалить произведение
                0. Назад
                
                Выберите действие:""");

            int c = choiceReader(0, 5);
            switch (c) {
                case 1 -> print("шо та добавлем");
                case 2 -> print("ТИПО ПОКАЗЫВАЕМ");
                case 3 -> print("ищем произведение по ID");
                case 4 -> print("изменяем");
                case 5 -> print("удаляем");
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }

    }

    private void authorMenu(){
        while (true) {
            print("""
                    1. Добавить автора
                    2. Показать всех авторов
                    3. Найти автора по ID
                    4. Изменить автора
                    5. Удалить автора
                    0. Назад

                    Выберите действие:""");

            int c = choiceReader(0, 5);
            switch (c){
                case 1 -> print("добавляем");
                case 2 -> print("показываем");
                case 3 -> print("ищем");
                case 4 -> print("меняем");
                case 5 -> print("удаляем");
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }

    private void exhibitionMenu() {
        while (true) {
            print("""
                1. Добавить выставку
                2. Показать все выставки
                3. Найти выставку по ID
                4. Изменить выставку
                5. Удалить выставку
                6. Показать произведения на выставке
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 6);

            switch (c) {
                case 1 -> print("добавляем выставку");
                case 2 -> print("показываем все выставки");
                case 3 -> print("ищем выставку по ID");
                case 4 -> print("изменяем выставку");
                case 5 -> print("удаляем выставку");
                case 6 -> print("показываем произведения на выставке");
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }


    private void visitorMenu() {
        while (true) {
            print("""
                1. Добавить посетителя
                2. Показать всех посетителей
                3. Найти посетителя по ID
                4. Изменить посетителя
                5. Удалить посетителя
                6. Зарегистрировать посещение выставки
                7. Показать историю посещений
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 7);

            switch (c) {
                case 1 -> print("добавляем посетителя");
                case 2 -> print("показываем всех посетителей");
                case 3 -> print("ищем посетителя по ID");
                case 4 -> print("изменяем посетителя");
                case 5 -> print("удаляем посетителя");
                case 6 -> print("регистрируем посещение выставки");
                case 7 -> print("показываем историю посещений");
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }


    private void employeeMenu() {
        while (true) {
            print("""
                1. Добавить сотрудника
                2. Показать всех сотрудников
                3. Найти сотрудника по ID
                4. Изменить сотрудника
                5. Удалить сотрудника
                6. Назначить сотрудника на выставку
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 6);

            switch (c) {
                case 1 -> print("добавляем сотрудника");
                case 2 -> print("показываем всех сотрудников");
                case 3 -> print("ищем сотрудника по ID");
                case 4 -> print("изменяем сотрудника");
                case 5 -> print("удаляем сотрудника");
                case 6 -> print("назначаем сотрудника на выставку");
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
                1. Поиск произведения по названию
                2. Поиск произведений по автору
                3. Поиск произведений по материалу
                4. Поиск произведений по году создания
                5. Поиск выставки по названию
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 5);

            switch (c) {
                case 1 -> print("ищем произведение по названию");
                case 2 -> print("ищем произведения по автору");
                case 3 -> print("ищем произведения по материалу");
                case 4 -> print("ищем произведения по году создания");
                case 5 -> print("ищем выставку по названию");
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
                1. Фильтр произведений по статусу
                2. Фильтр произведений по типу
                3. Фильтр произведений по диапазону годов
                4. Фильтр произведений по выставке
                5. Сортировка произведений по названию
                6. Сортировка произведений по году создания
                7. Сортировка произведений по размеру
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 7);

            switch (c) {
                case 1 -> print("фильтруем по статусу");
                case 2 -> print("фильтруем по типу");
                case 3 -> print("фильтруем по диапазону годов");
                case 4 -> print("фильтруем по выставке");
                case 5 -> print("сортируем по названию");
                case 6 -> print("сортируем по году создания");
                case 7 -> print("сортируем по размеру");
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }


    private void statisticsMenu() {
        while (true) {
            print("""
                1. Показать общую статистику
                2. Статистика по произведениям
                3. Статистика по выставкам
                4. Статистика по посещениям
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 4);

            switch (c) {
                case 1 -> print("показываем общую статистику");
                case 2 -> print("показываем статистику по произведениям");
                case 3 -> print("показываем статистику по выставкам");
                case 4 -> print("показываем статистику по посещениям");
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }


    private void exportMenu() {
        while (true) {
            print("""
                1. Экспорт произведений в Excel
                2. Экспорт авторов в Excel
                3. Экспорт выставок в Excel
                4. Экспорт посетителей и посещений в Excel
                5. Экспорт всех данных в Excel
                6. Экспорт произведений в CSV
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 6);

            switch (c) {
                case 1 -> print("экспортируем произведения в Excel");
                case 2 -> print("экспортируем авторов в Excel");
                case 3 -> print("экспортируем выставки в Excel");
                case 4 -> print("экспортируем посетителей и посещения в Excel");
                case 5 -> print("экспортируем все данные в Excel");
                case 6 -> print("экспортируем произведения в CSV");
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }


    private void bdMenu() {
        while (true) {
            print("""
                1. Произведения
                2. Авторы
                3. Материалы
                4. Фотографии
                5. Выставки
                6. Посетители
                7. Посещения
                8. Сотрудники
                9. Связи произведений и авторов
                10. Связи произведений и материалов
                11. Связи произведений и выставок
                12. Связи сотрудников и выставок
                13. Вывести все таблицы
                0. Назад

                Выберите действие:
                """);

            int c = choiceReader(0, 13);

            switch (c) {
                case 1 -> print("выводим таблицу произведений");
                case 2 -> print("выводим таблицу авторов");
                case 3 -> print("выводим таблицу материалов");
                case 4 -> print("выводим таблицу фотографий");
                case 5 -> print("выводим таблицу выставок");
                case 6 -> print("выводим таблицу посетителей");
                case 7 -> print("выводим таблицу посещений");
                case 8 -> print("выводим таблицу сотрудников");
                case 9 -> print("выводим связи произведений и авторов");
                case 10 -> print("выводим связи произведений и материалов");
                case 11 -> print("выводим связи произведений и выставок");
                case 12 -> print("выводим связи сотрудников и выставок");
                case 13 -> print("выводим все таблицы");
                case 0 -> {
                    print("Возвращаемся...");
                    return;
                }
            }
        }
    }

}

