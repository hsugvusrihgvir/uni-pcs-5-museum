package ru.mirea.project.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

public final class UiDialogs {

    private UiDialogs() {
    }

    // общие диалоги не дублируются в каждом контроллере
    public static void error(Window owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.initOwner(owner);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Операцию не удалось выполнить");
        applyStyles(alert.getDialogPane());
        alert.showAndWait();
    }

    public static void info(Window owner, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.initOwner(owner);
        alert.setTitle("Готово");
        alert.setHeaderText(null);
        applyStyles(alert.getDialogPane());
        alert.showAndWait();
    }

    public static boolean confirm(Window owner, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.initOwner(owner);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Подтвердите действие");
        applyStyles(alert.getDialogPane());
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    // диалоги имеют свою Scene, поэтому стили им добавляются отдельно
    public static void applyStyles(DialogPane dialogPane) {
        dialogPane.getStylesheets().add(
                UiDialogs.class.getResource("/ru/mirea/project/ui/app.css").toExternalForm()
        );
    }
}
