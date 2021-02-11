package agivdel.laksia;

import javafx.scene.control.Alert;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * Тренировка модели распознавания лиц на основе пользовательского набора фотографий.
 * Весь обучающий набор лиц преобразуется в одну общую матрицу данных,
 * где каждая строка представляет собой один экземпляр изображения лица, разложенного в строку.
 * Все лица обучающего набора должны быть приведены к одному размеру и иметь нормированные гистограммы.
 * Если постоянно обновлять обученную модель новыми фотографиями, можно не хранить снимки.
 */

public class Recognizer {
    public static Map<Integer, String> trainModel(String pathName) {
        Map<Integer, String> names = new HashMap<>();
        //проверяем, есть ли фотографии в обучающем наборе
        File trainingSetDirectory = new File(pathName);
        if (!trainingSetDirectory.exists()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "check for directory presence " +
                    "src/main/resources/trainingSet_facerec/" +
                    " with training image set .png");
            alert.showAndWait();
        }
        FilenameFilter imageFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".png");
            }
        };
        File[] imageFiles = trainingSetDirectory.listFiles(imageFilter);
        Objects.requireNonNull(imageFiles);//ИЗМЕНИТЬ!
        if (imageFiles.length <= 1) {
            //сейчас диалоговое окно появляется ранее главного окна программы
            //сменить показ окна диалога на другой способ оповещения
//            Alert alert = new Alert(Alert.AlertType.INFORMATION, "too few images to train the face recognition model");
//            alert.showAndWait();
            return new HashMap<>();
        }
        List<Mat> images = new ArrayList<>();
        Mat labels = new Mat(imageFiles.length, 1, CvType.CV_32SC1);
        for (int row = 0; row < imageFiles.length; row++) {
            Mat imageMat = MatConvert.fileNameToMat(imageFiles[row].getAbsolutePath(), 0);//загружаем изображение в матрицу сразу же в серых тонах
            Imgproc.equalizeHist(imageMat, imageMat);
            //example of image name: "1-nameOfPerson_0.png"
            //"1" - number of person, "0" - number of image
            String labelAndName = imageFiles[row].getName().split("\\_")[0];
            int label = Integer.parseInt(labelAndName.split("\\-")[0]);
            String name = labelAndName.split("\\-")[1];
            names.put(label, name);
            images.add(imageMat);
            labels.put(row, 0, label);
        }
        FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
        faceRecognizer.train(images, labels);
        faceRecognizer.save("src/main/resources/trainedModel/");

        return names;
    }

    public static double[] faceRecognize(Mat currentFace) {
        Path LBPHTrainedModelPath = Paths.get("src/main/resources/trainedModel/" +
                "lbph_faceRecognition_model.xml");
        if (!Files.exists(LBPHTrainedModelPath, LinkOption.NOFOLLOW_LINKS)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "no face recognition model\nface recognition is not possible");
            alert.showAndWait();
            return new double[]{-1,-1};
        }
        int[] predictedLabel = new int[1];
        double[] confidenceValue = new double[1];
        FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
        faceRecognizer.read(String.valueOf(LBPHTrainedModelPath));
        if (faceRecognizer.empty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "failed to load model");
            alert.showAndWait();
            return new double[]{-1,-1};
        }
        faceRecognizer.predict(currentFace, predictedLabel, confidenceValue);

        return new double[]{predictedLabel[0], confidenceValue[0]};
    }
}