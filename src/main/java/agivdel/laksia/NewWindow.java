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

public class NewWindow {
    public static void showImage(Image image, String title, StackPane mainPane, Stage mainWindow) {

        Stage newWindow = new Stage();
        mainPane.widthProperty().addListener((obj, oV, nV) -> {
            newWindow.setWidth(nV.doubleValue());
        });
        mainPane.heightProperty().addListener((obj, oV, nV) -> {
            newWindow.setHeight(nV.doubleValue());
        });
        mainWindow.xProperty().addListener((o, oV, nV) -> {
            newWindow.setX(nV.doubleValue() + mainPane.getLayoutX());
        });
        mainWindow.yProperty().addListener((o, oV, nV) -> {
            newWindow.setY(nV.doubleValue() + /*mainPane.getLayoutY()*/ + 70);
        });

        Pane newPane = new Pane();
        ImageView imageView = getImageView(newPane, image);
        newPane.getChildren().add(imageView);

        BorderPane box = new BorderPane();
        box.setCenter(newPane);

        Scene windowScene = new Scene(box);
        newWindow.setScene(windowScene);
        newWindow.setTitle(title);

//        newWindow.toFront();
        newWindow.setAlwaysOnTop(true);

        newWindow.show();
    }

    public static ImageView getImageView(Pane pane, Image image) {
        if (image == null) {
            System.out.println("image is null");
            return new ImageView();
        }
        ImageView imageView = new ImageView(image);
        //изображения больше панели подгоняются под ее размер и остаются такими до конца
        if (image.getWidth() > pane.getWidth() || image.getHeight() > pane.getHeight()) {
            imageView.setFitWidth(pane.getWidth());
            imageView.setFitHeight(pane.getHeight());
        } else {
            //маленькие изображения сохраняют исходные размеры
            imageView.setFitWidth(0);
            imageView.setFitHeight(0);
        }
        return imageView;
    }
}