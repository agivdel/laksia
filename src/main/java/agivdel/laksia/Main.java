package agivdel.laksia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.getIcons().add(new Image("/images/L.png"));
        stage.setTitle("Laksia");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/sample.fxml"));

        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 670);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        Controller controller = loader.getController();
        controller.setStage(stage);
    }
}