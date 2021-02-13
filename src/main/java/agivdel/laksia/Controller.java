package agivdel.laksia;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.opencv.core.*;

import java.io.File;
import java.net.URL;
import java.util.*;

public class Controller extends View implements Initializable {

    File file;//файл изображения
    MatOfRect facesMat;//матрица с координатами обнаруженных лиц
    Map<Rect, String> faces;//карта лиц и имен


    @FXML
    private Label getNameLabel;
    @FXML
    private RadioMenuItem maskRectangleMenu;
    @FXML
    private RadioMenuItem showRectangleMenu;


    /**
     * Инициализация объектов и полей.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        names = Recognizer.trainModel("src/main/resources/trainingSet_facerec/");
        this.faces = new HashMap<>();
        ToggleGroup showOrMaskGroup = new ToggleGroup();
        maskRectangleMenu.setToggleGroup(showOrMaskGroup);
        showRectangleMenu.setToggleGroup(showOrMaskGroup);
    }

    /**
     * Настройка выбора файла изображения.
     */
    private FileChooser fileChooserConfigure() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("select a single image");

        File initialFile = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(initialFile);

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg", "*.jpe"),//читается FX/openCV, без альфа
                new FileChooser.ExtensionFilter("All files", "*.*"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp"),//читается FX/openCV, без альфа
                new FileChooser.ExtensionFilter("GIF", "*.gif"),//читается FX
                new FileChooser.ExtensionFilter("JPEG2000", "*.jp2", "*.j2k"),//читается openCV, без альфа
                new FileChooser.ExtensionFilter("PNG", "*.png"),//читается FX, openCV
                new FileChooser.ExtensionFilter("TIFF", "*.tif", "*.tiff"),//должен читаться, читается openCV
                new FileChooser.ExtensionFilter("PGM", "*.pgm")//Portable Gray Map Image, читается OpenCV
        );
        return fileChooser;
    }

    /**
     * Открытие файла изображения с помощью объекта выбора файлов.
     */
    @FXML
    private void openSingleFile() {
        //получить файл от объекта файловыборщика
        //создать объект изображения на основе файла
        //очистить слой лиц от старых данных
        //создать объект imageView на основе изображения
        this.file = fileChooserConfigure().showOpenDialog(stage);
        //TODO вызывать метод обработки из отдельного класса обработки ошибок?
        if (this.file == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "no file selected");
            alert.showAndWait();
        }
        Image image = new Image(this.file.toURI().toString());

        getNameLabel.setText(file.getName());
        facesPane.getChildren().clear();

        imageView.setImage(image);
        //изображения больше панели подгоняются под ее размер и остаются такими до конца
        if (image.getWidth() > pane.getWidth() || image.getHeight() > pane.getHeight()) {
            imageView.setFitWidth(pane.getWidth());
            imageView.setFitHeight(pane.getHeight());
        } else {
            //маленькие изображения сохраняют исходные размеры
            imageView.setFitWidth(0);
            imageView.setFitHeight(0);
        }
    }

    /**
     * Ищем лица на изображении и распознаем их.
     */
    @FXML
    private void faceDetect() {
        if (this.file == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "no file selected");
            alert.showAndWait();
            return;
        }
        facesPane.getChildren().clear();
        this.facesMat = Detector.findFaces(this.file);
        this.facesMat.toList().forEach(faceFrame -> this.faces.put(faceFrame, "0"));
        drawAllFaces(this.facesMat);
    }

    /**
     * Распознаем найденные лица.
     * @param currentFace матрица с изображением.
     * @return The predicted label for the given image and associated confidence (e.g. distance) for the predicted label.
     */
    @FXML
    private double[] faceRecognize() {
        //получить список прямоугольников-лиц
        //пройтись по списку
        //распознать каждое лицо
        for (Rect faceFrame : this.facesMat.toList()) {
            Mat face =
        }
        return Recognizer.faceRecognize();
    }


    /**
     * Все зоны выделения найденных лиц после сдвига отрисовываются заново (возвращаются на место).
     */
    @FXML
    private void putRectangleBack() {
        drawAllFaces(this.facesMat);
    }

    /**
     * Делаем слой с лицами полностью прозрачным.
     */
    @FXML
    private void maskRectangle() {
        facesPane.setOpacity(0);
    }

    /**
     * Если лица еще не найдены, ищем их.
     * Если лица уже найдены, возвращаем слою с лицами частичную непрозрачность.
     */
    @FXML
    private void showRectangle() {
        if (this.file == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "select the image file");
            alert.showAndWait();
        }
        if (this.facesMat.empty()) {
            faceDetect();
        } else {
            facesPane.setOpacity(0.4);
        }
    }

    public void drawAllFaces(MatOfRect facesMat) {
        facesPane.getChildren().clear();
        for (Rect faceFrame : facesMat.toList()) {
            drawSingleFace(faceFrame);
        }
    }

    public void drawSingleFace(Rect faceFrame) {
        double topLeftX = (faceFrame.x /** widthScaleFactor*/ + imageView.getLayoutX());// widthScaleFactor;
        double topLeftY = (faceFrame.y /** heightScaleFactor*/ + imageView.getLayoutY());// heightScaleFactor;
        Shape faceRectangle = new Rectangle(topLeftX, topLeftY, faceFrame.width, faceFrame.height);

        Text text = new Text(topLeftX, topLeftY, "another shchi");
        text.setTextOrigin(VPos.TOP);//начало координат в левом верхнем углу узла text. Доступен только для класса Text
        text.setFill(Color.WHITE);
        text.setFont(Font.font(16.0));
        text.setOpacity(1.0);
        if (!this.faces.get(faceFrame).equals("0")) {
            text.setText(this.faces.get(faceFrame));
        }

        text.setOnMousePressed(me -> {
            if (me.isSecondaryButtonDown()) {
                TextField textField = new TextField();
                facesPane.getChildren().add(textField);
                textField.setLayoutX(topLeftX);
                textField.setLayoutY(topLeftY + 20);
                textField.setPromptText("уточните имя");
                textField.setOnKeyReleased(keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.ENTER) {
                        String newName = textField.getText().trim();
                        if (!newName.equals("") & newName.length() > 0) {
                            text.setText(newName);
                            this.faces.put(faceFrame, newName);
                        }
                        facesPane.getChildren().remove(textField);
                    }
                });
            }
        });

        NodeEffects.makeDraggable(faceRectangle);
        NodeEffects.makeBindUp(text, faceRectangle);
        NodeEffects.makeHighlighted(faceRectangle);
        facesPane.getChildren().addAll(faceRectangle, text);
    }
}