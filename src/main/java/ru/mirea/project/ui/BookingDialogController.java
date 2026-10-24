package ru.mirea.project.ui;

import javafx.event.ActionEvent;
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
import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatus;
import ru.mirea.project.model.Exhibition;
import ru.mirea.project.model.Visitor;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BookingDialogController {

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

    private boolean editing;

    public static Optional<BookingInput> show(
            Window owner,
            List<Visitor> visitors,
            List<Exhibition> exhibitions,
            Booking booking
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    BookingDialogController.class.getResource("/ru/mirea/project/ui/booking-dialog.fxml")
            );
            Parent content = loader.load();
            BookingDialogController controller = loader.getController();
            controller.configure(visitors, exhibitions, booking);

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

            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.addEventFilter(ActionEvent.ACTION, event -> {
                String error = controller.validate();
                if (error != null) {
                    controller.errorLabel.setText(error);
                    event.consume();
                }
            });

            dialog.setResultConverter(button -> button == saveButtonType ? controller.getInput() : null);
            return dialog.showAndWait();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось открыть форму бронирования.", e);
        }
    }

    private void configure(List<Visitor> visitors, List<Exhibition> exhibitions, Booking booking) {
        visitorComboBox.getItems().setAll(visitors);
        visitorComboBox.setConverter(visitorConverter());

        exhibitionComboBox.getItems().setAll(exhibitions);
        exhibitionComboBox.setConverter(exhibitionConverter());

        statusComboBox.getItems().setAll(BookingStatus.values());
        editing = booking != null;

        if (editing) {
            visitors.stream()
                    .filter(visitor -> visitor.getId() == booking.getVisitorId())
                    .findFirst()
                    .ifPresent(visitorComboBox::setValue);
            exhibitions.stream()
                    .filter(exhibition -> exhibition.getId() == booking.getExhibitionId())
                    .findFirst()
                    .ifPresent(exhibitionComboBox::setValue);
            visitDatePicker.setValue(booking.getVisitDate());
            statusComboBox.setValue(booking.getStatus());
            priceField.setText(booking.getPrice() == null ? "" : booking.getPrice().toString());

            // эти поля относятся к созданию брони, а изменение по правилам проекта меняет статус или цену
            visitorComboBox.setDisable(true);
            exhibitionComboBox.setDisable(true);
            visitDatePicker.setDisable(true);
            statusHintLabel.setText("Финальные статусы COMPLETED и CANCELLED больше не изменяются.");
        } else {
            statusComboBox.setValue(BookingStatus.CREATED);
            statusComboBox.setDisable(true);
            visitDatePicker.setValue(LocalDate.now());
            statusHintLabel.setText("Без цены будет CREATED, с ценой — CONFIRMED.");

            // при создании статус показывается сразу, но пользователь не выбирает его вручную
            priceField.textProperty().addListener((observable, oldValue, newValue) ->
                    statusComboBox.setValue(newValue == null || newValue.isBlank()
                            ? BookingStatus.CREATED
                            : BookingStatus.CONFIRMED)
            );
        }
    }

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

        Exhibition exhibition = exhibitionComboBox.getValue();
        LocalDate visitDate = visitDatePicker.getValue();
        if (visitDate.isBefore(exhibition.getStartDate()) || visitDate.isAfter(exhibition.getEndDate())) {
            return "Дата посещения должна быть в период работы выставки: "
                    + exhibition.getStartDate() + " — " + exhibition.getEndDate() + ".";
        }

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

    private BookingInput getInput() {
        return new BookingInput(
                visitorComboBox.getValue(),
                exhibitionComboBox.getValue(),
                visitDatePicker.getValue(),
                statusComboBox.getValue(),
                parsePrice()
        );
    }

    private Double parsePrice() {
        String text = priceField.getText();
        return text == null || text.isBlank()
                ? null
                : Double.parseDouble(text.trim().replace(',', '.'));
    }

    private static StringConverter<Visitor> visitorConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Visitor visitor) {
                return visitor == null ? "" : visitor.getFullName() + " — " + visitor.getEmail();
            }

            @Override
            public Visitor fromString(String string) {
                return null;
            }
        };
    }

    private static StringConverter<Exhibition> exhibitionConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Exhibition exhibition) {
                return exhibition == null ? "" : exhibition.getTitle();
            }

            @Override
            public Exhibition fromString(String string) {
                return null;
            }
        };
    }

    public record BookingInput(
            Visitor visitor,
            Exhibition exhibition,
            LocalDate visitDate,
            BookingStatus status,
            Double price
    ) {
    }
}
