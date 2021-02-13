package agivdel.laksia;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import java.io.File;
import java.util.Map;

public class View {
    static Stage stage;
    static Scene scene;

    static Map<Integer, String> names;

    @FXML
    StackPane pane;
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

    public StackPane getPane() {
        return pane;
    }

    public void setPane(StackPane pane) {
        this.pane = pane;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public Pane getFacesPane() {
        return facesPane;
    }

    public void setFacesPane(Pane facesPane) {
        this.facesPane = facesPane;
    }
}