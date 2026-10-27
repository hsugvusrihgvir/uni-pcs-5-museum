package ru.mirea.project.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class VisitorDialogController {

    // связывают поля контроллера с элементами формы
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField patronymicField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordHashField;
    @FXML
    private Label errorLabel;

    // открывает форму добавления посетителя
    public static Optional<VisitorInput> show(Window owner) {
        try {
            // загружает форму и получает её контроллер
            FXMLLoader loader = new FXMLLoader(
                    VisitorDialogController.class.getResource("/ru/mirea/project/ui/visitor-dialog.fxml")
            );
            Parent content = loader.load();
            VisitorDialogController controller = loader.getController();

            // создаёт окно для ввода данных
            Dialog<VisitorInput> dialog = new Dialog<>();
            dialog.initOwner(owner);
            dialog.setTitle("Новый посетитель");
            dialog.setHeaderText("Заполните данные посетителя");

            ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(520);
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
            dialog.setResultConverter(new Callback<ButtonType, VisitorInput>() {
                @Override
                public VisitorInput call(ButtonType button) {
                    return button == saveButtonType ? controller.getInput() : null;
                }
            });
            return dialog.showAndWait();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось открыть форму посетителя.", e);
        }
    }

    // проверяет поля и возвращает текст ошибки
    private String validate() {
        if (lastNameField.getText().isBlank()) {
            return "Фамилия не может быть пустой.";
        }
        if (nameField.getText().isBlank()) {
            return "Имя не может быть пустым.";
        }
        // проверяет длину полного имени
        if (lastNameField.getText().trim().length() > 255
                || nameField.getText().trim().length() > 255
                || patronymicField.getText().trim().length() > 255) {
            return "ФИО не должно быть длиннее 255 символов.";
        }
        // запрещает дату рождения из будущего
        if (birthDatePicker.getValue() != null && birthDatePicker.getValue().isAfter(LocalDate.now())) {
            return "Дата рождения не может быть в будущем.";
        }
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            return "Email не может быть пустым.";
        }
        // проверяет длину и запись почтового адреса
        if (email.length() > 255 || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "Введите корректный email длиной не более 255 символов.";
        }
        if (passwordHashField.getText().isBlank()) {
            return "Хеш пароля не может быть пустым.";
        }
        if (passwordHashField.getText().length() > 255) {
            return "Хеш пароля не должен быть длиннее 255 символов.";
        }
        return null;
    }

    // собирает введённые данные в один объект
    private VisitorInput getInput() {
        return new VisitorInput(
                nameField.getText().trim(),
                lastNameField.getText().trim(),
                emptyToNull(patronymicField.getText()),
                birthDatePicker.getValue(),
                emailField.getText().trim(),
                passwordHashField.getText()
        );
    }

    // заменяет пустое отчество отсутствующим значением
    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    // хранит данные заполненной формы
    public record VisitorInput(
            String name,
            String lastName,
            String patronymic,
            LocalDate birthDate,
            String email,
            String passwordHash
    ) {
    }
}
