package agivdel.laksia;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class MatConvert {
    public static final Scalar COLOR_BLACK = colorRGB(0, 0, 0);
    public static final Scalar COLOR_WHITE = colorRGB(255, 255, 255);
    public static final Scalar COLOR_RED = colorRGB(255, 0, 0);
    public static final Scalar COLOR_BLUE = colorRGB(0, 0, 255);
    public static final Scalar COLOR_GREEN = colorRGB(0, 128, 0);
    public static final Scalar COLOR_YELLOW = colorRGB(255, 255, 0);
    public static final Scalar COLOR_GRAY = colorRGB(128, 128, 128);


    private static Scalar colorRGB(double red, double green, double blue) {
        return new Scalar(blue, green, red);
    }

    /**
     * Преобразует исходную матрицу изображения (цветного или в оттенках серого, 1-, 3- или 4-канального)
     * в одноканальную матрицу изображения в оттенках серого
     * @param originMat исходная матрица
     * @return матрицу с изображением в оттенках серого и одним каналом
     */
    public static Mat matToGrayMat(Mat originMat) {
        if (originMat.channels() == 1) {
            return originMat;
        }
        Mat grayMat = new Mat();
        int code = 0;
        switch (originMat.channels()) {
            case 3 -> code = Imgproc.COLOR_BGR2GRAY;
            case 4 -> code = Imgproc.COLOR_BGRA2GRAY;
        }
        Imgproc.cvtColor(originMat, grayMat, code);
        return grayMat;
    }

    /**
     * Загружает изображение и преобразует его в объект класса Mat
     * @param fileName полное имя файла
     * @param flag константа для описания типа преобразования
     * @return объект класса Mat
     */
    public static Mat fileNameToMat(String fileName, int flag) {
        Mat image = Imgcodecs.imread(fileName, flag);//flag=0 для GRAYSCALE
        if (image.empty()) {
            System.out.println("не удалось загрузить изображение");
        }
        return image;
    }

    /**
     * Преобрпазование объекта класса Mat в объект класса Image и показ его в отдельном окне
     * @param imageMat матрица с изображением
     * @param title заголовок окна
     */
    public static void showImage(Mat imageMat, String title) {
        Image image = matToWritableImageFX(imageMat);

        Stage window = new Stage();
        ScrollPane sp = new ScrollPane();
        ImageView imageView = new ImageView();
        if (image != null) {
            imageView.setImage(image);
            if (image.getWidth() < 1000) {
                sp.setPrefWidth(image.getWidth() + 5);
            } else {
                sp.setPrefWidth(1000);
            }
            if (image.getHeight() < 700) {
                sp.setPrefViewportHeight(image.getHeight() + 10);
            } else {
                sp.setPrefHeight(700);
            }
        }
        sp.setContent(imageView);
        sp.setPannable(true);

        BorderPane box = new BorderPane();
        box.setCenter(sp);

        Scene windowScene = new Scene(box);
        window.setScene(windowScene);
        window.setTitle(title);
        window.show();
    }

    /**
     * Преобразование объекта класса Mat в объект класса WritableImage
     * @param imageMat
     * @return
     */
    public static WritableImage matToWritableImageFX(Mat imageMat) {
        if (imageMat == null || imageMat.empty()) {
            return null;
        }
        if (imageMat.depth() == CvType.CV_8U) {
            ;
        } else if (imageMat.depth() == CvType.CV_16U) {
            Mat m_16 = new Mat();
            imageMat.convertTo(m_16, CvType.CV_8U, 255.0 / 65535);
            imageMat = m_16;
        } else if (imageMat.depth() == CvType.CV_32F) {
            Mat m_32 = new Mat();
            imageMat.convertTo(m_32, CvType.CV_8U, 255);
            imageMat = m_32;
        } else {
            return null;
        }

        if (imageMat.channels() == 1) {
            Mat m_bgra = new Mat();
            Imgproc.cvtColor(imageMat, m_bgra, Imgproc.COLOR_GRAY2BGRA);
            imageMat = m_bgra;
        } else if (imageMat.channels() == 3) {
            Mat m_bgra = new Mat();
            Imgproc.cvtColor(imageMat, m_bgra, Imgproc.COLOR_BGR2BGRA);
            imageMat = m_bgra;
        } else if (imageMat.channels() == 4) {
            ;
        } else {
            return  null;
        }

        byte[] buf = new byte[imageMat.channels() * imageMat.cols() * imageMat.rows()];
        imageMat.get(0, 0, buf);

        WritableImage wim = new WritableImage(imageMat.cols(), imageMat.rows());
        PixelWriter pw = wim.getPixelWriter();
        pw.setPixels(0, 0, imageMat.cols(), imageMat.rows(), WritablePixelFormat.getByteBgraInstance(), buf, 0, imageMat.cols() * 4);

        return wim;
    }

    /**
     * преобразование объекта класса Image в объект класса Mat
     * @param image
     * @return
     */
    public static Mat imageToMat(Image image) {
        if (image == null) {
            return new Mat();
        }
        PixelReader pr = image.getPixelReader();
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        byte[] buf = new byte[4 * w * h];
        pr.getPixels(0, 0, w, h, WritablePixelFormat.getByteBgraInstance(), buf, 0, w * 4);
        Mat m = new Mat(h, w, CvType.CV_8UC4);
        m.put(0, 0, buf);
        return m;
    }
}