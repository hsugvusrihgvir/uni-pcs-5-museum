package ru.mirea.project.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.mirea.project.repository.BookingRepository;
import ru.mirea.project.repository.ExhibitionRepository;
import ru.mirea.project.repository.VisitorRepository;
import ru.mirea.project.service.BookingService;
import ru.mirea.project.service.ExhibitionService;
import ru.mirea.project.service.VisitorService;

import java.io.IOException;

public class MuseumApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MuseumApplication.class.getResource("/ru/mirea/project/ui/main-view.fxml")
        );
        Parent root = loader.load();

        // зависимости создаются в одном месте и передаются контроллеру после загрузки fxml
        VisitorService visitorService = new VisitorService(new VisitorRepository());
        ExhibitionService exhibitionService = new ExhibitionService(new ExhibitionRepository());
        BookingService bookingService = new BookingService(
                new BookingRepository(),
                visitorService,
                exhibitionService
        );

        MainController controller = loader.getController();
        controller.setServices(bookingService, visitorService, exhibitionService);

        Scene scene = new Scene(root, 1280, 760);
        scene.getStylesheets().add(
                MuseumApplication.class.getResource("/ru/mirea/project/ui/app.css").toExternalForm()
        );

        stage.setTitle("Музей — система бронирования");
        // иконка Stage отдельна от картинки, которая показывается внутри fxml
        stage.getIcons().add(new Image(
                MuseumApplication.class.getResourceAsStream("/ru/mirea/project/ui/museum-logo.png")
        ));
        stage.setMinWidth(980);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }
}
