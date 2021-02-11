package agivdel.laksia;

import org.opencv.core.*;
import org.opencv.face.Face;
import org.opencv.face.Facemark;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.List;

public class Detector {

    /**
     * Ищет лица (по умолчанию - каскадом Хаара),
     * записывает в матрицу faces координаты ограничивающих эти лица прямоугольников,
     * рисует эти прямоугольники на матрице исходного изображения.
     * @param grayImageMat матрица с исходным изображением для поиска лиц
     * @param minNeighbors
     * @param minSize
     * @return
     */

    public static MatOfRect findFaces(Mat grayImageMat, int minNeighbors, int minSize) {
        CascadeClassifier faceDetector = new CascadeClassifier(
                "src/main/resources/trainedModel/haarcascades/" +
                        "haarcascade_frontalface_alt.xml");
        if (faceDetector.empty()) {
            System.out.println("Не удалось загрузить классификатор лиц");
        }
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(grayImageMat,
                faces,
                1.1,
                minNeighbors,//встречал значение "2"
                Objdetect.CASCADE_DO_CANNY_PRUNING,//встречал значние "CASCADE_SCALE_IMAGE"
                new Size(minSize, minSize),
                grayImageMat.size());//вторым параметром указана матрица, куда будет записан результат
        return faces;
    }

    /**
     * Ищет ориентирные точки на найденных лицах,
     * записывает координаты этих точек в матрицу landmarks,
     * рисует эти точки на матрице исходного изображения.
     * Пока этот инструмент не использую.
     * @param grayImageMat матрица с исходным изображением для поиска ориентирных точек
     * @param faces матрица координат найденных лиц
     * @param flag
     * @return
     */
    public static Mat findLandmarks(Mat grayImageMat, MatOfRect faces, int flag) {
        Facemark facemark;
        switch (flag) {
            case 0 -> facemark = Face.createFacemarkKazemi();
            case 1 -> facemark = Face.createFacemarkLBF();
            case 2 -> facemark = Face.createFacemarkAAM();
            default -> facemark = Face.createFacemarkKazemi();
        }
        //загружаем обученную модель (своей у нас пока нет) для поиска ориентирных точек
        facemark.loadModel("src/main/resources/trainedModel/face_landmark_model.dat");
        //создаем матрицу для хранения ориентирных точек
        List<MatOfPoint2f> landmarks = new ArrayList<>();
        //берем изображение grayImageMat, по координатам найденных лиц faces ищем точки и пишем их в landmarks
        facemark.fit(grayImageMat, faces, landmarks);
        for (MatOfPoint2f landmark : landmarks) {
            for (int j = 0; j < landmark.rows(); j++) {
                double[] coordinates = landmark.get(j, 0);
                Point point = new Point(coordinates[0], coordinates[1]);
                Imgproc.circle(grayImageMat, point, 4, new Scalar(222), 1);
            }
        }
        return grayImageMat;
    }
}