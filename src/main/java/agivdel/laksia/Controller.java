package agivdel.laksia;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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

public class Controller extends View implements Initializable {

    static Stage stage;
    static Scene scene;

    static final Desktop desktop = Desktop.getDesktop();
    static double faceProportion;
    static Map<Integer, String> names;

    File file;//файл изображения
    MatOfRect faces;//матрица с координатами обнаруженных лиц
    Map<Rect, String> facess;
    double widthScaleFactor, heightScaleFactor;//коэфф.показывает, во сколько раз панель больше файла

    @FXML
    StackPane pane;
    @FXML
    ImageView imageView;
    @FXML
    Pane facesPane;
    @FXML
    TextField parentAddressField;
    @FXML
    Label getNameLabel;
    @FXML
    Label getMatProfileLabel;
    @FXML
    Label foundFacesNumberLabel;
    @FXML
    CheckMenuItem autoFaceDetectMenu;
    @FXML
    RadioMenuItem maskRectangleMenu;
    @FXML
    RadioMenuItem showRectangleMenu;
    @FXML
    CheckMenuItem autoFaceRecognizeMenu;

    @FXML
    Slider faceProportionSlider;
    @FXML
    Label faceMinSizeLabel;
    @FXML
    Label foundFacesNumberLabel2;


    /**
     * Метод для передачи объекта главного окна в класс контроллера.
     *
     * @param stage объект главного окна
     */
    public void setStage(Stage stage) {
        Controller.stage = stage;
        Controller.scene = stage.getScene();
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
        ToggleGroup showOrMaskGroup = new ToggleGroup();
        maskRectangleMenu.setToggleGroup(showOrMaskGroup);
        showRectangleMenu.setToggleGroup(showOrMaskGroup);
        foundFacesNumberLabel2.textProperty().bind(foundFacesNumberLabel.textProperty());//временно, потом убрать
    }

    /**
     * Минимальный размер искомого лица. Объекты размером меньше будут игнорироваться.
     */
    public void setFaceDetectAdjustment() {
        faceProportion = 0.2f;
        faceProportionSlider.valueProperty().addListener((obj, oldValue, newValue) -> {
            faceProportion = newValue.doubleValue();
        });
    }

    /**
     * Отображение директории с выбранным файлом изображения в поле getParentField.
     * Поле getParentField может принимать путь к файлу.
     * Если файл существует, он будет открыт.
     * Если такого файла нет, в поле будет показан старый (предыдущий) адрес.
     */
    public void getParentDirectoryFieldInit() {
        parentAddressField.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                File newFile = new File(parentAddressField.getText().trim());
                if (newFile.exists()) {
                    file = newFile;
                    displayImageFile();
                } else if (file != null) {
                    parentAddressField.setText(file.getParent());
                }
            }
        });
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
        displayImageFile();
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


    //по умолчанию все видео преобразуются в матрицу

    //если пользователь выбрал файл видео с диска,
    //открывают его через new videoCapture().read(), получаю матрицу как есть
    //и преобразую ее в матрицу изображения matToBufferedImage() (это быстрее, чем в WritableImage)

    //если пользователь захватывает кадры с веб-камеры,
    //открывают его через new videoCapture().read(), получаю матрицу как есть
    //и преобразую ее в матрицу изображения matToBufferedImage() (это быстрее, чем в WritableImage)

    //если пользователь выбрал файл фото с диска, преобразую его в матрицу как есть (константа -1)

    /**
     * Отображение выбранного файла изображения.
     */
    public void displayImageFile() {
        Image imageFile = new Image(file.toURI().toString());

        parentAddressField.setText(file.getParent() + "\\");
        parentAddressField.positionCaret(parentAddressField.getText().length());
        getNameLabel.setText(file.getName());
        foundFacesNumberLabel.setText("");
        facesPane.getChildren().clear();

        //дублирование файла изображения в отдельном новом окне
        NewWindow.showImage(imageFile, "file image", pane, stage);

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
        widthScaleFactor = (pane.getWidth() / imageFile.getWidth());
        heightScaleFactor = (pane.getHeight() / imageFile.getHeight());

        //переписать код, сделать гибкую подгонку размеров изображений под размеры панели

        //при уменьшении высоты окна изображение наползает на панель меню - найти причину и исправить!

        if (autoFaceDetectMenu.isSelected() || showRectangleMenu.isSelected()) {
            new Controller().faceDetect();
        }
    }

    /**
     * Ищем лица на изображении и распознаем их.
     */
    @FXML
    void faceDetect() {
        if (file == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "no file selected");
            alert.showAndWait();
            return;
        }
        Mat originImageMat = MatConvert.fileNameToMat(file.getPath(), -1);
        Mat originGrayImageMat = MatConvert.matToGrayMat(originImageMat);
        Imgproc.equalizeHist(originGrayImageMat, originGrayImageMat);

        int minSize = 0;
        int height = originGrayImageMat.rows();
        if (Math.round(height * (float) faceProportion) > 0) {//минимальный размер лица, который можно будет найти
            minSize = Math.round((height * (float) faceProportion));
        }
        faceMinSizeLabel.setText(String.valueOf(minSize));//временно, потом убрать
        faces = new MatOfRect();
        faces = Detector.findFaces(originGrayImageMat, minSize);

        String matProfile = String.format("  %dx%d    %s    %dch  ", originImageMat.width(), originImageMat.height(),
                CvType.typeToString(originImageMat.type()), originImageMat.channels());
        getMatProfileLabel.setText(matProfile);
        foundFacesNumberLabel.setText(String.valueOf(faces.toList().size()));
        facesPane.getChildren().clear();

        for (Rect rect : faces.toList()) {
            //когда реальный размер изображения больше окна, выделение уходит в сторону - ИСПРАВИТЬ!
            Mat face = originGrayImageMat.submat(rect);
            Mat resizeMat = new Mat();
            Imgproc.resize(originGrayImageMat, resizeMat, new Size(100, 100));
            System.out.println(rect.width + ", " + rect.height);//
            facess.put(rect, String.valueOf(0));
            drawSingleFace(rect);
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


    /**
     * Все зоны выделения найденных лиц после сдвига отрисовываются заново (возвращаются на место).
     */
    @FXML
    private void putRectangleBack() {
        drawAllFaces(faces);
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

    public void drawAllFaces(MatOfRect faces) {
        facesPane.getChildren().clear();
        for (Rect r : faces.toList()) {
            drawSingleFace(r);
        }
    }

    public void drawSingleFace(Rect r) {
        double topLeftX = (r.x * widthScaleFactor + imageView.getLayoutX());// widthScaleFactor;
        double topLeftY = (r.y * heightScaleFactor + imageView.getLayoutY());// heightScaleFactor;
        javafx.scene.shape.Shape faceRectangle = new Rectangle(topLeftX, topLeftY, r.width, r.height);
        System.out.println("x / y: " + r.x + ", " + r.y);
        System.out.println("imagePane w/h: " + pane.getWidth() + ", " + pane.getHeight());
        System.out.println("image w/h: " + new Image(file.toURI().toString()).getWidth() + ", " + new Image(file.toURI().toString()).getHeight());
        System.out.println("w: " + r.x * widthScaleFactor + ", h: " + r.y * heightScaleFactor);
        System.out.println("topLeftX/topLeftY: " + topLeftX + " / " + topLeftY);

        Text text = new Text(topLeftX, topLeftY, "another shchi");
        text.setTextOrigin(VPos.TOP);//начало координат в левом верхнем углу узла text. Доступен только для класса Text
        text.setFill(Color.WHITE);
        text.setFont(Font.font(16.0));
        text.setOpacity(1.0);
        if (!facess.get(r).equals(String.valueOf(0))) {
            text.setText(facess.get(r));
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
                            facess.put(r, newName);
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