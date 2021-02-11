package agivdel.laksia;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    private static Stage stage;
    private static Scene scene;
    private static final Desktop desktop = Desktop.getDesktop();

    private static double faceProportion;

    private static Map<Integer, String> names;

    private File file;//файл изображения
    private Mat originImageMat;//матрица исходного изображения (1-, 3- или 4-канальная)
    private Mat grayImageMat;//матрица изображения в оттенках серого
    private MatOfRect faces;//матрица с координатами обнаруженных лиц
    private Map<Rect, String> facess;
    private double widthScaleFactor, heightScaleFactor;//коэфф.показывает, во сколько раз панель больше файла

    @FXML
    private TextField getParentField;
    @FXML
    private StackPane pane;
    @FXML
    private ImageView imageView;
    @FXML
    private Pane facesPane;
    @FXML
    private Label getNameLabel;
    @FXML
    private Label getMatProfileLabel;
    @FXML
    private Label foundFacesNumberLabel;

    @FXML
    private CheckMenuItem autoFaceDetectMenu;
    @FXML
    private RadioMenuItem maskRectangleMenu;
    @FXML
    private RadioMenuItem showRectangleMenu;
    @FXML
    private CheckMenuItem autoFaceRecognizeMenu;

    @FXML
    private Slider faceProportionSlider;
    @FXML
    private Label faceMinSizeLabel;
    @FXML
    private Label foundFacesNumberLabel2;


    /**
     * Метод для передачи объекта главного окна в класс контроллера.
     *
     * @param stage объект главного окна
     */
    public void setStage(Stage stage) {
        Controller.stage = stage;
        scene = stage.getScene();
    }

    /**
     * Инициализация объектов и полей.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getParentDirectoryFieldInit();//инициализация текстового поля директории выбранного файла
        setFaceDetectAdjustment();

        names = Recognizer.trainModel("src/main/resources/trainingSet_facerec/");
        facess = new HashMap<>();
        maskiShowGroupInit();
        foundFacesNumberLabel2.textProperty().bind(foundFacesNumberLabel.textProperty());//временно, потом убрать
    }

    /**
     * Минимальный размер искомого лица. Объекты размером меньше будут игнорироваться.
     */
    private void setFaceDetectAdjustment() {
        faceProportion = 0.2f;
        faceProportionSlider.valueProperty().addListener((obj, oldValue, newValue) -> {
            faceProportion = newValue.doubleValue();
        });
    }

    /**
     * Открытие файла изображения с помощью объекта выбора файлов.
     */
    @FXML
    private void openSingleFile() {
        file = fileChooserConfigure().showOpenDialog(stage);
        if (file == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "no file selected");
            alert.showAndWait();
        }
        displayImageFile(file);
    }

    /**
     * Отображение директории с выбранным файлом изображения в поле getParentField.
     * Поле getParentField может принимать путь к файлу. Если файл существует, он будет открыт.
     */
    private void getParentDirectoryFieldInit() {
        getParentField.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                File newFile = new File(getParentField.getText().trim());
                if (newFile.exists()) {
                    file = newFile;
                    displayImageFile(file);
                } else if (file != null) {
                    getParentField.setText(file.getParent());
                }
            }
        });
    }

    /**
     * Загруженный файл откроется программой ОС, настроенной по умолчанию для этого типа файлов.
     *
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


    private void maskiShowGroupInit() {
        ToggleGroup showOrMaskGroup = new ToggleGroup();
        maskRectangleMenu.setToggleGroup(showOrMaskGroup);
        showRectangleMenu.setToggleGroup(showOrMaskGroup);
    }


    //по умолчанию все видео преобразуются в матрицу

    //если пользователь выбрал файл видео с диска,
    //открывают его через new videoCapture().read(), получаю матрицу как есть
    //и преобразую ее в матрицу изображения matToBufferedImage() (это быстрее, чем в WritableImage)

    //если пользователь захватывает кадры с веб-камеры,
    //открывают его через new videoCapture().read(), получаю матрицу как есть
    //и преобразую ее в матрицу изображения matToBufferedImage() (это быстрее, чем в WritableImage)

    //если пользователь выбрал файл фото с диска, преобразую его в матрицу как есть (константа -1)

    /**
     * отображение выбранного файла изображения
     *
     * @param file файл изображения
     */
    private void displayImageFile(File file) {
        try {
            String localUrl = file.toURI().toURL().toString();
            Image imageFile = new Image(localUrl);

            getParentField.setText(file.getParent() + "\\");
            getParentField.positionCaret(getParentField.getText().length());
            getNameLabel.setText(file.getName());
            foundFacesNumberLabel.setText("");
            facesPane.getChildren().clear();

            imageView.setImage(imageFile);

            //дублирование файла изображения в отдельном новом окне
//            Window.showImage(imageFile, "file image", pane, stage);

            //изображения больше панели подгоняются под ее размер и остаются такими до конца
            if (imageFile.getWidth() > pane.getWidth() || imageFile.getHeight() > pane.getHeight()) {
                imageView.setFitWidth(pane.getWidth());
                imageView.setFitHeight(pane.getHeight());
            } else {
                //изображения меньше панели сохраняют исходные размеры
                imageView.setFitWidth(0);
                imageView.setFitHeight(0);
            }
            widthScaleFactor = (pane.getWidth() / imageFile.getWidth());
            heightScaleFactor = (pane.getHeight() / imageFile.getHeight());

            //переписать код, сделать гибкую подгонку размеров изображений под размеры панели

            //при уменьшении высоты окна изображение наползает на панель меню - найти причину и исправить!

            if (autoFaceDetectMenu.isSelected() || showRectangleMenu.isSelected()) {
                faceDetect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ищем лица на изображении
     */
    @FXML
    private void faceDetect() throws MalformedURLException {
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
        faceMinSizeLabel.setText(String.valueOf(minSize));//временно, потом убрать
        faces = new MatOfRect();
        faces = Detector.findFaces(grayImageMat, minSize);

        String matProfile = String.format("  %dx%d    %s    %dch  ", originImageMat.width(), originImageMat.height(),
                CvType.typeToString(originImageMat.type()), originImageMat.channels());
        getMatProfileLabel.setText(matProfile);
        foundFacesNumberLabel.setText(String.valueOf(faces.toList().size()));
        facesPane.getChildren().clear();

        for (Rect rect : faces.toList()) {
            //когда реальный размер изображения больше окна, выделение уходит в сторону - ИСПРАВИТЬ!
            Mat face = grayImageMat.submat(rect);
            Mat resizeMat = new Mat();
            Imgproc.resize(grayImageMat, resizeMat, new Size(100, 100));
            System.out.println(rect.width + ", " + rect.height);//
            facess.put(rect, String.valueOf(0));
            Drawer.drawSingleFace(rect, facesPane, pane, facess, imageView, widthScaleFactor, heightScaleFactor, file);
            if (autoFaceRecognizeMenu.isSelected()) {
                double[] result = faceRecognize(resizeMat);
                double predictedLabel = result[0];
                double confidenceValue = result[1];
            }
        }
    }


    //90. если пользователь нажал "новый человек", сохраняю это имя
    //95. и экстракт лица помещаю в модель FR, используя имя в имени (или номер?)
    //110. показываю на панели лиц отметку с ближайшим вектором для данного лица (т.е. предсказание модели)
    //120. возможно, ближайшего вектора не найдется, тогда обозначить лицо "неизвестный"


    @FXML
    private void putRectangleBack() throws MalformedURLException {
        Drawer.drawAllFaces(facesPane, faces, pane, facess, imageView, widthScaleFactor, heightScaleFactor, file);
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

/*    public void showSimplePopup(String message) {
        Label messageLabel = new Label(message);
        Button cancelButton = new Button("пнятненько");
        VBox vbox = new VBox();
        vbox.getChildren().addAll(messageLabel, cancelButton);
        vbox.setMinWidth(200);
        vbox.setMinHeight(100);
        Popup popup = new Popup();
        cancelButton.setOnAction(event -> popup.hide());
        popup.getScene().setRoot(vbox);
        popup.show(stage);
    }*/
}
