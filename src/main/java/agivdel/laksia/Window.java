package agivdel.laksia;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Открытие файла изображения в новом окне.
 */

public class Window {
    public static void showImage(Image image, String title, StackPane pane, Stage stage) {

        Stage newWindow = new Stage();
        pane.widthProperty().addListener((obj, oV, nV) -> {
            newWindow.setWidth(nV.doubleValue());
        });
        pane.heightProperty().addListener((obj, oV, nV) -> {
            newWindow.setHeight(nV.doubleValue());
        });
        stage.xProperty().addListener((o, oV, nV) -> {
            newWindow.setX(nV.doubleValue() + pane.getLayoutX());
        });
        stage.yProperty().addListener((o, oV, nV) -> {
            newWindow.setY(nV.doubleValue() + /*pane.getLayoutY()*/ + 70);
        });

        Pane pane2 = new Pane();
        ImageView imageView2 = new ImageView();
        if (image != null) {
            imageView2.setImage(image);
            if (image.getWidth() > pane2.getWidth() || image.getHeight() > pane2.getHeight()) {
                imageView2.setFitWidth(pane2.getWidth());
                imageView2.setFitHeight(pane2.getHeight());
            } else {
                //маленькие изображения сохраняют исходные размеры
                imageView2.setFitWidth(0);
                imageView2.setFitHeight(0);
            }
            pane2.getChildren().add(imageView2);
        }

        BorderPane box = new BorderPane();
        box.setCenter(pane2);

        Scene windowScene = new Scene(box);
        newWindow.setScene(windowScene);
        newWindow.setTitle(title);

//        newWindow.toFront();
        newWindow.setAlwaysOnTop(true);

        newWindow.show();
    }
}