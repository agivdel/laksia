package agivdel.laksia;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Controller extends View implements Initializable {

    @FXML
    private TextField getParentField;
    @FXML
    private Label getNameLabel;

    @FXML
    private RadioMenuItem maskRectangleMenu;
    @FXML
    private RadioMenuItem showRectangleMenu;
    @FXML
    private CheckMenuItem autoFaceRecognizeMenu;


    /**
     * Инициализация объектов и полей.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        names = Recognizer.trainModel("src/main/resources/trainingSet_facerec/");
        facess = new HashMap<>();
        ToggleGroup showOrMaskGroup = new ToggleGroup();
        maskRectangleMenu.setToggleGroup(showOrMaskGroup);
        showRectangleMenu.setToggleGroup(showOrMaskGroup);

        faceProportion = 0.2f;
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
        file = fileChooserConfigure().showOpenDialog(stage);
        //TODO вызывать метод обработки из отдельного класса обработки ошибок?
        if (file == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "no file selected");
            alert.showAndWait();
        }
        displayImageFile(file);
    }

    /**
     * Загруженный файл откроется программой ОС, настроенной по умолчанию для этого типа файлов.
     * @throws IOException
     */
    @FXML
    private void openFileWithExternalProgram() throws IOException {
        if (file == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "no file selected");
            alert.showAndWait();
            return;
        }
        desktop.open(file);
    }

    /**
     * Закрытие программы.
     */
    @FXML
    private void programExit() {
        stage.close();
        Platform.exit();
    }

    /**
     * отображение выбранного файла изображения
     *
     * @param file файл изображения
     */
    private void displayImageFile(File file) {
        Image imageFile = new Image(file.toURI().toString());

        getNameLabel.setText(file.getName());
        facesPane.getChildren().clear();

        imageView.setImage(imageFile);
        //изображения больше панели подгоняются под ее размер и остаются такими до конца
        if (imageFile.getWidth() > pane.getWidth() || imageFile.getHeight() > pane.getHeight()) {
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
        if (file == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "no file selected");
            alert.showAndWait();
            return;
        }
        originImageMat = MatConvert.fileNameToMat(file.getPath(), -1);
        grayImageMat = MatConvert.matToGrayMat(originImageMat);
        Imgproc.equalizeHist(grayImageMat, grayImageMat);

        int minSize = 0;
        int height = grayImageMat.rows();
        if (Math.round(height * (float) faceProportion) > 0) {//минимальный размер лица, который можно будет найти
            minSize = Math.round((height * (float) faceProportion));
        }
        faces = new MatOfRect();
        faces = Detector.findFaces(grayImageMat, minSize);

        facesPane.getChildren().clear();

        for (Rect rect : faces.toList()) {
            //когда реальный размер изображения больше окна, выделение уходит в сторону - ИСПРАВИТЬ!
            Mat face = grayImageMat.submat(rect);
            Mat resizeMat = new Mat();
            Imgproc.resize(grayImageMat, resizeMat, new Size(100, 100));
            System.out.println(rect.width + ", " + rect.height);//
            facess.put(rect, String.valueOf(0));
            new Drawer().drawSingleFace(rect);
            if (autoFaceRecognizeMenu.isSelected()) {
                double[] result = faceRecognize(resizeMat);
                double predictedLabel = result[0];
                double confidenceValue = result[1];
            }
        }
    }


    /**
     * Все зоны выделения найденных лиц после сдвига отрисовываются заново (возвращаются на место).
     */
    @FXML
    private void putRectangleBack() {
        new Drawer().drawAllFaces(faces);
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
     * @throws MalformedURLException
     */
    @FXML
    private void showRectangle() throws MalformedURLException {
        if (file == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "select the image file");
            alert.showAndWait();
        }
        if (faces.empty()) {
            faceDetect();
        } else {
            facesPane.setOpacity(0.4);
        }
    }

    /**
     * Распознаем найденные лица.
     * @param currentFace матрица с изображением.
     * @return The predicted label for the given image and associated confidence (e.g. distance) for the predicted label.
     */
    @FXML
    private double[] faceRecognize(Mat currentFace) {
        return Recognizer.faceRecognize(currentFace);
    }
}
