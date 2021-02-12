package agivdel.laksia;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class View {
    static Stage stage;
    static Scene scene;
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