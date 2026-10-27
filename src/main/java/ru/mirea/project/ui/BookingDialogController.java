package ru.mirea.project.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import javafx.util.StringConverter;
import javafx.util.Callback;
import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatus;
import ru.mirea.project.model.Exhibition;
import ru.mirea.project.model.Visitor;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BookingDialogController {

    // связывают поля контроллера с элементами формы
    @FXML
    private ComboBox<Visitor> visitorComboBox;
    @FXML
    private ComboBox<Exhibition> exhibitionComboBox;
    @FXML
    private DatePicker visitDatePicker;
    @FXML
    private ComboBox<BookingStatus> statusComboBox;
    @FXML
    private TextField priceField;
    @FXML
    private Label statusHintLabel;
    @FXML
    private Label errorLabel;

    // хранит признак изменения существующего бронирования
    private boolean editing;

    // открывает форму создания или изменения бронирования
    public static Optional<BookingInput> show(
            Window owner,
            List<Visitor> visitors,
            List<Exhibition> exhibitions,
            Booking booking
    ) {
        try {
            // загружает форму и получает её контроллер
            FXMLLoader loader = new FXMLLoader(
                    BookingDialogController.class.getResource("/ru/mirea/project/ui/booking-dialog.fxml")
            );
            Parent content = loader.load();
            BookingDialogController controller = loader.getController();
            controller.configure(visitors, exhibitions, booking);

            // создаёт окно и задаёт его заголовок
            Dialog<BookingInput> dialog = new Dialog<>();
            dialog.initOwner(owner);
            dialog.setTitle(booking == null ? "Новое бронирование" : "Изменение бронирования");
            dialog.setHeaderText(booking == null
                    ? "Выберите посетителя, выставку и дату"
                    : "Можно изменить статус или цену");

            ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(560);
            UiDialogs.applyStyles(dialog.getDialogPane());

            // проверяет поля перед сохранением и оставляет окно открытым при ошибке
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.addEventFilter(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String error = controller.validate();
                    if (error != null) {
                        controller.errorLabel.setText(error);
                        event.consume();
                    }
                }
            });

            // возвращает данные только после нажатия кнопки сохранения
            dialog.setResultConverter(new Callback<ButtonType, BookingInput>() {
                @Override
                public BookingInput call(ButtonType button) {
                    return button == saveButtonType ? controller.getInput() : null;
                }
            });
            return dialog.showAndWait();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось открыть форму бронирования.", e);
        }
    }

    // заполняет поля формы перед показом
    private void configure(List<Visitor> visitors, List<Exhibition> exhibitions, Booking booking) {
        visitorComboBox.getItems().setAll(visitors);
        // показывает имя и почту посетителя в списке
        visitorComboBox.setConverter(visitorConverter());

        exhibitionComboBox.getItems().setAll(exhibitions);
        // показывает название выставки в списке
        exhibitionComboBox.setConverter(exhibitionConverter());

        statusComboBox.getItems().setAll(BookingStatus.values());
        // выбирает режим изменения при наличии бронирования
        editing = booking != null;

        if (editing) {
            // находит выбранного посетителя и выставку по номеру
            for (Visitor visitor : visitors) {
                if (visitor.getId() == booking.getVisitorId()) {
                    visitorComboBox.setValue(visitor);
                    break;
                }
            }
            for (Exhibition exhibition : exhibitions) {
                if (exhibition.getId() == booking.getExhibitionId()) {
                    exhibitionComboBox.setValue(exhibition);
                    break;
                }
            }
            visitDatePicker.setValue(booking.getVisitDate());
            statusComboBox.setValue(booking.getStatus());
            priceField.setText(booking.getPrice() == null ? "" : booking.getPrice().toString());

            // оставляет доступными только статус и цену
            visitorComboBox.setDisable(true);
            exhibitionComboBox.setDisable(true);
            visitDatePicker.setDisable(true);
            statusHintLabel.setText("Финальные статусы COMPLETED и CANCELLED больше не изменяются.");
        } else {
            // задаёт начальные значения для новой брони
            statusComboBox.setValue(BookingStatus.CREATED);
            statusComboBox.setDisable(true);
            visitDatePicker.setValue(LocalDate.now());
            statusHintLabel.setText("Без цены будет CREATED, с ценой — CONFIRMED.");

            // обновляет статус при вводе цены
            priceField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    statusComboBox.setValue(newValue == null || newValue.isBlank()
                            ? BookingStatus.CREATED
                            : BookingStatus.CONFIRMED);
                }
            });
        }
    }

    // проверяет поля и возвращает текст ошибки
    private String validate() {
        if (visitorComboBox.getValue() == null) {
            return "Выберите посетителя.";
        }
        if (exhibitionComboBox.getValue() == null) {
            return "Выберите выставку.";
        }
        if (visitDatePicker.getValue() == null) {
            return "Укажите дату посещения.";
        }

        // проверяет попадание даты в период работы выставки
        Exhibition exhibition = exhibitionComboBox.getValue();
        LocalDate visitDate = visitDatePicker.getValue();
        if (visitDate.isBefore(exhibition.getStartDate()) || visitDate.isAfter(exhibition.getEndDate())) {
            return "Дата посещения должна быть в период работы выставки: "
                    + exhibition.getStartDate() + " — " + exhibition.getEndDate() + ".";
        }

        // проверяет число и правила для выбранного статуса
        Double price;
        try {
            price = parsePrice();
        } catch (NumberFormatException e) {
            return "Цена должна быть числом.";
        }
        if (price != null && price <= 0) {
            return "Цена должна быть больше нуля.";
        }
        if (editing && statusComboBox.getValue() == BookingStatus.CONFIRMED && price == null) {
            return "Для статуса CONFIRMED нужно указать цену.";
        }
        if (editing && statusComboBox.getValue() == BookingStatus.CREATED && price != null) {
            return "При добавлении цены выберите статус CONFIRMED.";
        }
        return null;
    }

    // собирает введённые данные в один объект
    private BookingInput getInput() {
        return new BookingInput(
                visitorComboBox.getValue(),
                exhibitionComboBox.getValue(),
                visitDatePicker.getValue(),
                statusComboBox.getValue(),
                parsePrice()
        );
    }

    // превращает текст с запятой или точкой в число
    private Double parsePrice() {
        String text = priceField.getText();
        return text == null || text.isBlank()
                ? null
                : Double.parseDouble(text.trim().replace(',', '.'));
    }

    // задаёт подпись посетителя в выпадающем списке
    private static StringConverter<Visitor> visitorConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Visitor visitor) {
                return visitor == null ? "" : visitor.getFullName() + " — " + visitor.getEmail();
            }

            // возвращает пустое значение потому что ввод текста не используется
            @Override
            public Visitor fromString(String string) {
                return null;
            }
        };
    }

    // задаёт подпись выставки в выпадающем списке
    private static StringConverter<Exhibition> exhibitionConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Exhibition exhibition) {
                return exhibition == null ? "" : exhibition.getTitle();
            }

            // возвращает пустое значение потому что ввод текста не используется
            @Override
            public Exhibition fromString(String string) {
                return null;
            }
        };
    }

    // хранит данные заполненной формы
    public record BookingInput(
            Visitor visitor,
            Exhibition exhibition,
            LocalDate visitDate,
            BookingStatus status,
            Double price
    ) {
    }
}
