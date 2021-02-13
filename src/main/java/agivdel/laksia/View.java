package agivdel.laksia;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.Map;

public class View {
    static Stage stage;
    static Scene scene;

    static Map<Integer, String> names;

    @FXML
    StackPane mainPane;
    @FXML
    ImageView imageView;
    @FXML
    Pane facesPane;


    /**
     * Метод для передачи объекта главного окна в класс контроллера.
     *
     * @param stage объект главного окна
     */
    public void setStage(Stage stage) {
        View.stage = stage;
        View.scene = stage.getScene();
    }
}