package ru.mirea.project.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatistics;
import ru.mirea.project.model.BookingStatus;
import ru.mirea.project.model.Exhibition;
import ru.mirea.project.model.Visitor;
import ru.mirea.project.service.BookingService;
import ru.mirea.project.service.ExhibitionService;
import ru.mirea.project.service.VisitorService;
import ru.mirea.project.util.ExcelExporter;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class MainController {

    private static final String ALL_STATUSES = "Все статусы";

    // связывают поля контроллера с элементами разметки
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private DatePicker startDateFilter;
    @FXML
    private DatePicker endDateFilter;
    @FXML
    private TableView<Booking> bookingTable;
    @FXML
    private TableColumn<Booking, Number> bookingIdColumn;
    @FXML
    private TableColumn<Booking, String> bookingVisitorColumn;
    @FXML
    private TableColumn<Booking, String> bookingExhibitionColumn;
    @FXML
    private TableColumn<Booking, LocalDate> bookingDateColumn;
    @FXML
    private TableColumn<Booking, BookingStatus> bookingStatusColumn;
    @FXML
    private TableColumn<Booking, Double> bookingPriceColumn;
    @FXML
    private Button editBookingButton;
    @FXML
    private Button deleteBookingButton;

    @FXML
    private TableView<Visitor> visitorTable;
    @FXML
    private TableColumn<Visitor, Number> visitorIdColumn;
    @FXML
    private TableColumn<Visitor, String> visitorNameColumn;
    @FXML
    private TableColumn<Visitor, LocalDate> visitorBirthDateColumn;
    @FXML
    private TableColumn<Visitor, String> visitorEmailColumn;

    @FXML
    private TableView<Exhibition> exhibitionTable;
    @FXML
    private TableColumn<Exhibition, Number> exhibitionIdColumn;
    @FXML
    private TableColumn<Exhibition, String> exhibitionTitleColumn;
    @FXML
    private TableColumn<Exhibition, LocalDate> exhibitionStartColumn;
    @FXML
    private TableColumn<Exhibition, LocalDate> exhibitionEndColumn;
    @FXML
    private TableColumn<Exhibition, Number> exhibitionHallColumn;

    @FXML
    private Label visitorsCountLabel;
    @FXML
    private Label bookingsCountLabel;
    @FXML
    private Label completedCountLabel;
    @FXML
    private Label cancelledCountLabel;
    @FXML
    private Label revenueLabel;
    @FXML
    private Label statusLabel;

    // дают доступ к данным и проверкам
    private BookingService bookingService;
    private VisitorService visitorService;
    private ExhibitionService exhibitionService;

    // хранят данные для таблиц
    private final ObservableList<Booking> bookingItems = FXCollections.observableArrayList();
    private final ObservableList<Visitor> visitorItems = FXCollections.observableArrayList();
    private final ObservableList<Exhibition> exhibitionItems = FXCollections.observableArrayList();
    // находят посетителя или выставку по номеру
    private final Map<Long, Visitor> visitorsById = new HashMap<>();
    private final Map<Long, Exhibition> exhibitionsById = new HashMap<>();

    // хранит бронирования после поиска и фильтрации
    private FilteredList<Booking> filteredBookings;

    // настраивает таблицы и действия после загрузки разметки
    @FXML
    private void initialize() {
        configureBookingTable();
        configureVisitorTable();
        configureExhibitionTable();

        // добавляет в список все статусы
        statusFilterComboBox.getItems().add(ALL_STATUSES);
        for (BookingStatus status : BookingStatus.values()) {
            statusFilterComboBox.getItems().add(status.name());
        }
        statusFilterComboBox.setValue(ALL_STATUSES);

        // оставляет только подходящие бронирования
        filteredBookings = new FilteredList<>(bookingItems);
        // сортирует бронирования после фильтрации
        SortedList<Booking> sortedBookings = new SortedList<>(filteredBookings);
        // связывает сортировку таблицы со списком
        sortedBookings.comparatorProperty().bind(bookingTable.comparatorProperty());
        bookingTable.setItems(sortedBookings);

        visitorTable.setItems(visitorItems);
        exhibitionTable.setItems(exhibitionItems);

        // обновляет поиск при вводе текста
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                applyFilters(false);
            }
        });
        // включает кнопки изменения и удаления при выборе строки
        bookingTable.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<Booking>() {
                    @Override
                    public void changed(ObservableValue<? extends Booking> observable, Booking oldValue, Booking newValue) {
                        updateBookingButtons(newValue);
                    }
                });
        // открывает изменение бронирования двойным щелчком по строке
        bookingTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    handleEditBooking();
                }
            }
        });
        updateBookingButtons(null);
    }

    // сохраняет сервисы и загружает данные
    public void setServices(
            BookingService bookingService,
            VisitorService visitorService,
            ExhibitionService exhibitionService
    ) {
        this.bookingService = bookingService;
        this.visitorService = visitorService;
        this.exhibitionService = exhibitionService;
        refreshAll();
    }

    // задаёт содержимое столбцов бронирований
    private void configureBookingTable() {
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<Booking, Number>("id"));
        // находит имя посетителя по его номеру
        bookingVisitorColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Booking, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Booking, String> data) {
                return new ReadOnlyStringWrapper(visitorName(data.getValue().getVisitorId()));
            }
        });
        // находит название выставки по её номеру
        bookingExhibitionColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Booking, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Booking, String> data) {
                return new ReadOnlyStringWrapper(exhibitionTitle(data.getValue().getExhibitionId()));
            }
        });
        bookingDateColumn.setCellValueFactory(new PropertyValueFactory<Booking, LocalDate>("visitDate"));
        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<Booking, BookingStatus>("status"));
        bookingPriceColumn.setCellValueFactory(new PropertyValueFactory<Booking, Double>("price"));
        // показывает цену с двумя знаками после точки или ставит прочерк
        bookingPriceColumn.setCellFactory(new Callback<TableColumn<Booking, Double>, TableCell<Booking, Double>>() {
            @Override
            public TableCell<Booking, Double> call(TableColumn<Booking, Double> column) {
                return new TableCell<Booking, Double>() {
                    @Override
                    protected void updateItem(Double price, boolean empty) {
                        super.updateItem(price, empty);
                        setText(empty || price == null ? "—" : String.format(Locale.US, "%.2f ₽", price));
                    }
                };
            }
        });
        bookingTable.setPlaceholder(new Label("Бронирования не найдены"));
    }

    // задаёт содержимое таблицы посетителей
    private void configureVisitorTable() {
        visitorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        visitorIdColumn.setCellValueFactory(new PropertyValueFactory<Visitor, Number>("id"));
        visitorNameColumn.setCellValueFactory(new PropertyValueFactory<Visitor, String>("fullName"));
        visitorBirthDateColumn.setCellValueFactory(new PropertyValueFactory<Visitor, LocalDate>("birthDate"));
        visitorEmailColumn.setCellValueFactory(new PropertyValueFactory<Visitor, String>("email"));
        visitorTable.setPlaceholder(new Label("Посетители не найдены"));
    }

    // задаёт содержимое таблицы выставок
    private void configureExhibitionTable() {
        exhibitionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        exhibitionIdColumn.setCellValueFactory(new PropertyValueFactory<Exhibition, Number>("id"));
        exhibitionTitleColumn.setCellValueFactory(new PropertyValueFactory<Exhibition, String>("title"));
        exhibitionStartColumn.setCellValueFactory(new PropertyValueFactory<Exhibition, LocalDate>("startDate"));
        exhibitionEndColumn.setCellValueFactory(new PropertyValueFactory<Exhibition, LocalDate>("endDate"));
        exhibitionHallColumn.setCellValueFactory(new PropertyValueFactory<Exhibition, Number>("hallNumber"));
        exhibitionTable.setPlaceholder(new Label("Выставки не найдены"));
    }

    // обновляет данные после нажатия кнопки
    @FXML
    private void handleRefresh() {
        refreshAll();
    }

    // открывает форму и создаёт бронирование после сохранения
    @FXML
    private void handleAddBooking() {
        if (visitorItems.isEmpty() || exhibitionItems.isEmpty()) {
            UiDialogs.error(owner(), "Для бронирования нужен хотя бы один посетитель и одна выставка.");
            return;
        }

        try {
            Optional<BookingDialogController.BookingInput> result =
                    BookingDialogController.show(owner(), visitorItems, exhibitionItems, null);
            // пропускает создание при закрытии формы без сохранения
            if (result.isPresent()) {
                BookingDialogController.BookingInput input = result.get();
                try {
                    bookingService.createBooking(
                            input.visitor().getId(),
                            input.exhibition().getId(),
                            input.visitDate(),
                            input.price()
                    );
                    refreshAll();
                    statusLabel.setText("Бронирование создано.");
                } catch (RuntimeException e) {
                    showError(e);
                }
            }
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    // меняет статус и цену выбранного бронирования
    @FXML
    private void handleEditBooking() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        // запрещает изменение завершённого или отменённого бронирования
        if (isFinalStatus(selected)) {
            UiDialogs.error(owner(), "Бронирование в финальном статусе нельзя изменить.");
            return;
        }

        try {
            Optional<BookingDialogController.BookingInput> result =
                    BookingDialogController.show(owner(), visitorItems, exhibitionItems, selected);
            if (result.isPresent()) {
                BookingDialogController.BookingInput input = result.get();
                try {
                    bookingService.updateBooking(selected.getId(), input.status(), input.price());
                    refreshAll();
                    statusLabel.setText("Бронирование изменено.");
                } catch (RuntimeException e) {
                    showError(e);
                }
            }
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    // удаляет выбранное бронирование после подтверждения
    @FXML
    private void handleDeleteBooking() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!UiDialogs.confirm(owner(), "Удалить бронирование № " + selected.getId() + "?")) {
            return;
        }

        try {
            bookingService.deleteBooking(selected.getId());
            refreshAll();
            statusLabel.setText("Бронирование удалено.");
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    // открывает форму и добавляет нового посетителя
    @FXML
    private void handleAddVisitor() {
        try {
            Optional<VisitorDialogController.VisitorInput> result = VisitorDialogController.show(owner());
            if (result.isPresent()) {
                VisitorDialogController.VisitorInput input = result.get();
                try {
                    visitorService.createVisitor(
                            input.name(),
                            input.lastName(),
                            input.patronymic(),
                            input.birthDate(),
                            input.email(),
                            input.passwordHash()
                    );
                    refreshAll();
                    statusLabel.setText("Посетитель создан.");
                } catch (RuntimeException e) {
                    showError(e);
                }
            }
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    // применяет выбранные условия поиска
    @FXML
    private void handleApplyFilters() {
        applyFilters(true);
    }

    // сбрасывает условия поиска и снова показывает список
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilterComboBox.setValue(ALL_STATUSES);
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);
        applyFilters(false);
        statusLabel.setText("Фильтры сброшены.");
    }

    // показывает сначала бронирования с поздней датой
    @FXML
    private void handleSortByDate() {
        bookingDateColumn.setSortType(TableColumn.SortType.DESCENDING);
        bookingTable.getSortOrder().setAll(List.of(bookingDateColumn));
        bookingTable.sort();
        statusLabel.setText("Сортировка: сначала новые даты.");
    }

    // показывает сначала дорогие бронирования
    @FXML
    private void handleSortByPrice() {
        bookingPriceColumn.setSortType(TableColumn.SortType.DESCENDING);
        bookingTable.getSortOrder().setAll(List.of(bookingPriceColumn));
        bookingTable.sort();
        statusLabel.setText("Сортировка: сначала дорогие бронирования.");
    }

    // сохраняет показанные бронирования в файл с таблицей
    @FXML
    private void handleExport() {
        if (bookingTable.getItems().isEmpty()) {
            UiDialogs.error(owner(), "В текущем наборе нет бронирований для экспорта.");
            return;
        }

        // открывает выбор места и имени файла
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить бронирования");
        chooser.setInitialFileName("museum_bookings_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        File selectedFile = chooser.showSaveDialog(owner());
        if (selectedFile == null) {
            return;
        }

        // добавляет расширение при его отсутствии
        File exportFile = selectedFile.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")
                ? selectedFile
                : new File(selectedFile.getParentFile(), selectedFile.getName() + ".xlsx");

        try {
            ExcelExporter.exportBookings(new ArrayList<>(bookingTable.getItems()), exportFile.toPath());
            UiDialogs.info(owner(), "Файл сохранён:\n" + exportFile.getAbsolutePath());
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    // загружает данные и обновляет экран
    private void refreshAll() {
        if (bookingService == null || visitorService == null || exhibitionService == null) {
            return;
        }

        try {
            // получает все данные до обновления таблиц
            List<Visitor> visitors = visitorService.getAllVisitors();
            List<Exhibition> exhibitions = exhibitionService.getAllExhibitions();
            List<Booking> bookings = bookingService.getAllBookings();

            visitorItems.setAll(visitors);
            exhibitionItems.setAll(exhibitions);
            bookingItems.setAll(bookings);

            // обновляет поиск по номерам
            visitorsById.clear();
            for (Visitor visitor : visitors) {
                visitorsById.put(visitor.getId(), visitor);
            }
            exhibitionsById.clear();
            for (Exhibition exhibition : exhibitions) {
                exhibitionsById.put(exhibition.getId(), exhibition);
            }

            // перерисовывает таблицу после обновления имён и названий
            bookingTable.refresh();
            applyFilters(false);
            refreshStatistics();
            statusLabel.setText("Данные обновлены. Бронирований: " + bookingItems.size() + ".");
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    // показывает новые числа в блоке статистики
    private void refreshStatistics() {
        BookingStatistics statistics = bookingService.getStatistics();
        visitorsCountLabel.setText(Long.toString(statistics.visitors()));
        bookingsCountLabel.setText(Long.toString(statistics.bookings()));
        completedCountLabel.setText(Long.toString(statistics.completed()));
        cancelledCountLabel.setText(Long.toString(statistics.cancelled()));
        revenueLabel.setText(String.format(Locale.US, "%.2f ₽", statistics.completedRevenue()));
    }

    // применяет поиск и фильтры к бронированиям
    private void applyFilters(boolean showValidationError) {
        if (filteredBookings == null) {
            return;
        }

        LocalDate start = startDateFilter.getValue();
        LocalDate end = endDateFilter.getValue();
        // сохраняет текущий список при неверном порядке дат
        if (start != null && end != null && start.isAfter(end)) {
            if (showValidationError) {
                UiDialogs.error(owner(), "Дата начала фильтра не может быть позже даты окончания.");
            }
            return;
        }

        String query = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = statusFilterComboBox.getValue();

        // проверяет выбранные условия для каждой строки
        filteredBookings.setPredicate(new Predicate<Booking>() {
            @Override
            public boolean test(Booking booking) {
                boolean matchesSearch = query.isEmpty() || matchesSearch(booking, query);
                boolean matchesStatus = selectedStatus == null
                        || ALL_STATUSES.equals(selectedStatus)
                        || booking.getStatus().name().equals(selectedStatus);
                boolean matchesStart = start == null || !booking.getVisitDate().isBefore(start);
                boolean matchesEnd = end == null || !booking.getVisitDate().isAfter(end);
                return matchesSearch && matchesStatus && matchesStart && matchesEnd;
            }
        });

        statusLabel.setText("Показано бронирований: " + filteredBookings.size() + ".");
    }

    // ищет текст в данных бронирования и связанных записях
    private boolean matchesSearch(Booking booking, String query) {
        Visitor visitor = visitorsById.get(booking.getVisitorId());
        Exhibition exhibition = exhibitionsById.get(booking.getExhibitionId());
        String visitorText = visitor == null
                ? Long.toString(booking.getVisitorId())
                : visitor.getFullName() + " " + visitor.getEmail();
        String exhibitionText = exhibition == null
                ? Long.toString(booking.getExhibitionId())
                : exhibition.getTitle();

        return Long.toString(booking.getId()).contains(query)
                || visitorText.toLowerCase(Locale.ROOT).contains(query)
                || exhibitionText.toLowerCase(Locale.ROOT).contains(query)
                || booking.getStatus().name().toLowerCase(Locale.ROOT).contains(query)
                || booking.getVisitDate().toString().contains(query);
    }

    // отключает кнопки изменения и удаления без выбранной строки
    private void updateBookingButtons(Booking selected) {
        boolean nothingSelected = selected == null;
        editBookingButton.setDisable(nothingSelected);
        deleteBookingButton.setDisable(nothingSelected);
    }

    // проверяет завершение или отмену бронирования
    private boolean isFinalStatus(Booking booking) {
        return booking != null
                && (booking.getStatus() == BookingStatus.COMPLETED
                || booking.getStatus() == BookingStatus.CANCELLED);
    }

    // показывает имя посетителя или его номер
    private String visitorName(long id) {
        Visitor visitor = visitorsById.get(id);
        return visitor == null ? "ID " + id : visitor.getFullName();
    }

    // показывает название выставки или её номер
    private String exhibitionTitle(long id) {
        Exhibition exhibition = exhibitionsById.get(id);
        return exhibition == null ? "ID " + id : exhibition.getTitle();
    }

    // получает текущее окно для форм и сообщений
    private Window owner() {
        return bookingTable.getScene() == null ? null : bookingTable.getScene().getWindow();
    }

    // показывает ошибку и отмечает неудачную операцию
    private void showError(RuntimeException error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            message = "Произошла неизвестная ошибка.";
        }
        UiDialogs.error(owner(), message);
        statusLabel.setText("Операция не выполнена.");
    }
}
