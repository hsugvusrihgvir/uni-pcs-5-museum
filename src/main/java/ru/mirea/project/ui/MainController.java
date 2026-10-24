package ru.mirea.project.ui;

import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Window;
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

public class MainController {

    private static final String ALL_STATUSES = "Все статусы";

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

    private BookingService bookingService;
    private VisitorService visitorService;
    private ExhibitionService exhibitionService;

    private final ObservableList<Booking> bookingItems = FXCollections.observableArrayList();
    private final ObservableList<Visitor> visitorItems = FXCollections.observableArrayList();
    private final ObservableList<Exhibition> exhibitionItems = FXCollections.observableArrayList();
    private final Map<Long, Visitor> visitorsById = new HashMap<>();
    private final Map<Long, Exhibition> exhibitionsById = new HashMap<>();

    private FilteredList<Booking> filteredBookings;

    @FXML
    private void initialize() {
        configureBookingTable();
        configureVisitorTable();
        configureExhibitionTable();

        statusFilterComboBox.getItems().add(ALL_STATUSES);
        for (BookingStatus status : BookingStatus.values()) {
            statusFilterComboBox.getItems().add(status.name());
        }
        statusFilterComboBox.setValue(ALL_STATUSES);

        // FilteredList отвечает за поиск и фильтры, а SortedList сохраняет сортировку TableView
        filteredBookings = new FilteredList<>(bookingItems, booking -> true);
        SortedList<Booking> sortedBookings = new SortedList<>(filteredBookings);
        sortedBookings.comparatorProperty().bind(bookingTable.comparatorProperty());
        bookingTable.setItems(sortedBookings);

        visitorTable.setItems(visitorItems);
        exhibitionTable.setItems(exhibitionItems);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters(false));
        bookingTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> updateBookingButtons(newValue));
        bookingTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                handleEditBooking();
            }
        });
        updateBookingButtons(null);
    }

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

    private void configureBookingTable() {
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        bookingIdColumn.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getId()));
        bookingVisitorColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(visitorName(data.getValue().getVisitorId()))
        );
        bookingExhibitionColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(exhibitionTitle(data.getValue().getExhibitionId()))
        );
        bookingDateColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getVisitDate())
        );
        bookingStatusColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getStatus())
        );
        bookingPriceColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getPrice())
        );
        bookingPriceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "—" : String.format(Locale.US, "%.2f ₽", price));
            }
        });
        bookingTable.setPlaceholder(new Label("Бронирования не найдены"));
    }

    private void configureVisitorTable() {
        visitorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        visitorIdColumn.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getId()));
        visitorNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getFullName()));
        visitorBirthDateColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getBirthDate()));
        visitorEmailColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEmail()));
        visitorTable.setPlaceholder(new Label("Посетители не найдены"));
    }

    private void configureExhibitionTable() {
        exhibitionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        exhibitionIdColumn.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getId()));
        exhibitionTitleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitle()));
        exhibitionStartColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStartDate()));
        exhibitionEndColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEndDate()));
        exhibitionHallColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getHallNumber()));
        exhibitionTable.setPlaceholder(new Label("Выставки не найдены"));
    }

    @FXML
    private void handleRefresh() {
        refreshAll();
    }

    @FXML
    private void handleAddBooking() {
        if (visitorItems.isEmpty() || exhibitionItems.isEmpty()) {
            UiDialogs.error(owner(), "Для бронирования нужен хотя бы один посетитель и одна выставка.");
            return;
        }

        try {
            BookingDialogController.show(owner(), visitorItems, exhibitionItems, null)
                    .ifPresent(input -> {
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
                    });
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    @FXML
    private void handleEditBooking() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (isFinalStatus(selected)) {
            UiDialogs.error(owner(), "Бронирование в финальном статусе нельзя изменить.");
            return;
        }

        try {
            BookingDialogController.show(owner(), visitorItems, exhibitionItems, selected)
                    .ifPresent(input -> {
                        try {
                            bookingService.updateBooking(selected.getId(), input.status(), input.price());
                            refreshAll();
                            statusLabel.setText("Бронирование изменено.");
                        } catch (RuntimeException e) {
                            showError(e);
                        }
                    });
        } catch (RuntimeException e) {
            showError(e);
        }
    }

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

    @FXML
    private void handleAddVisitor() {
        try {
            VisitorDialogController.show(owner()).ifPresent(input -> {
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
            });
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    @FXML
    private void handleApplyFilters() {
        applyFilters(true);
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilterComboBox.setValue(ALL_STATUSES);
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);
        applyFilters(false);
        statusLabel.setText("Фильтры сброшены.");
    }

    @FXML
    private void handleSortByDate() {
        bookingDateColumn.setSortType(TableColumn.SortType.DESCENDING);
        bookingTable.getSortOrder().setAll(List.of(bookingDateColumn));
        bookingTable.sort();
        statusLabel.setText("Сортировка: сначала новые даты.");
    }

    @FXML
    private void handleSortByPrice() {
        bookingPriceColumn.setSortType(TableColumn.SortType.DESCENDING);
        bookingTable.getSortOrder().setAll(List.of(bookingPriceColumn));
        bookingTable.sort();
        statusLabel.setText("Сортировка: сначала дорогие бронирования.");
    }

    @FXML
    private void handleExport() {
        if (bookingTable.getItems().isEmpty()) {
            UiDialogs.error(owner(), "В текущем наборе нет бронирований для экспорта.");
            return;
        }

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

    private void refreshAll() {
        if (bookingService == null || visitorService == null || exhibitionService == null) {
            return;
        }

        try {
            // сначала получаем все списки, чтобы таблицы не остались обновлёнными наполовину
            List<Visitor> visitors = visitorService.getAllVisitors();
            List<Exhibition> exhibitions = exhibitionService.getAllExhibitions();
            List<Booking> bookings = bookingService.getAllBookings();

            visitorItems.setAll(visitors);
            exhibitionItems.setAll(exhibitions);
            bookingItems.setAll(bookings);

            visitorsById.clear();
            visitors.forEach(visitor -> visitorsById.put(visitor.getId(), visitor));
            exhibitionsById.clear();
            exhibitions.forEach(exhibition -> exhibitionsById.put(exhibition.getId(), exhibition));

            // имена связанных объектов зависят от словарей, поэтому таблицу нужно перерисовать
            bookingTable.refresh();
            applyFilters(false);
            refreshStatistics();
            statusLabel.setText("Данные обновлены. Бронирований: " + bookingItems.size() + ".");
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    private void refreshStatistics() {
        BookingStatistics statistics = bookingService.getStatistics();
        visitorsCountLabel.setText(Long.toString(statistics.visitors()));
        bookingsCountLabel.setText(Long.toString(statistics.bookings()));
        completedCountLabel.setText(Long.toString(statistics.completed()));
        cancelledCountLabel.setText(Long.toString(statistics.cancelled()));
        revenueLabel.setText(String.format(Locale.US, "%.2f ₽", statistics.completedRevenue()));
    }

    private void applyFilters(boolean showValidationError) {
        if (filteredBookings == null) {
            return;
        }

        LocalDate start = startDateFilter.getValue();
        LocalDate end = endDateFilter.getValue();
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

        filteredBookings.setPredicate(booking -> {
            boolean matchesSearch = query.isEmpty() || matchesSearch(booking, query);
            boolean matchesStatus = selectedStatus == null
                    || ALL_STATUSES.equals(selectedStatus)
                    || booking.getStatus().name().equals(selectedStatus);
            boolean matchesStart = start == null || !booking.getVisitDate().isBefore(start);
            boolean matchesEnd = end == null || !booking.getVisitDate().isAfter(end);
            return matchesSearch && matchesStatus && matchesStart && matchesEnd;
        });

        statusLabel.setText("Показано бронирований: " + filteredBookings.size() + ".");
    }

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

    private void updateBookingButtons(Booking selected) {
        boolean nothingSelected = selected == null;
        editBookingButton.setDisable(nothingSelected);
        deleteBookingButton.setDisable(nothingSelected);
    }

    private boolean isFinalStatus(Booking booking) {
        return booking != null
                && (booking.getStatus() == BookingStatus.COMPLETED
                || booking.getStatus() == BookingStatus.CANCELLED);
    }

    private String visitorName(long id) {
        Visitor visitor = visitorsById.get(id);
        return visitor == null ? "ID " + id : visitor.getFullName();
    }

    private String exhibitionTitle(long id) {
        Exhibition exhibition = exhibitionsById.get(id);
        return exhibition == null ? "ID " + id : exhibition.getTitle();
    }

    private Window owner() {
        return bookingTable.getScene() == null ? null : bookingTable.getScene().getWindow();
    }

    private void showError(RuntimeException error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            message = "Произошла неизвестная ошибка.";
        }
        UiDialogs.error(owner(), message);
        statusLabel.setText("Операция не выполнена.");
    }
}
