package agivdel.laksia;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import java.awt.*;
import java.io.File;
import java.util.Map;

public class View {
    static Stage stage;
    static Scene scene;

    static final Desktop desktop = Desktop.getDesktop();
    static double faceProportion;
    static Map<Integer, String> names;


    Mat originImageMat;//матрица исходного изображения (1-, 3- или 4-канальная)
    Mat grayImageMat;//матрица изображения в оттенках серого
    MatOfRect faces;//матрица с координатами обнаруженных лиц
    Map<Rect, String> facess;
    File file;//файл изображения
//    double widthScaleFactor, heightScaleFactor;//коэфф.показывает, во сколько раз панель больше файла

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

}