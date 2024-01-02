package com.xupt.opengaugepro.imageprocessing;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageProcessingService {

    // 放大图像
    public Mat zoomIn(Mat image) {
        Mat zoomedImage = new Mat();
        Size size = new Size(image.width() * 2, image.height() * 2); // 放大尺寸
        Imgproc.resize(image, zoomedImage, size);
        return zoomedImage;
    }

    // 缩小图像
    public Mat zoomOut(Mat image) {
        Mat zoomedImage = new Mat();
        Size size = new Size(image.width() * 0.8, image.height() * 0.8); // 缩小尺寸
        Imgproc.resize(image, zoomedImage, size);
        return zoomedImage;
    }

    // 加载图像
    public Mat loadImage(String filePath) {
        return Imgcodecs.imread(filePath);
    }

    // 去噪声
    public Mat denoise(Mat image) {
        Mat denoisedImage = new Mat();
        Imgproc.GaussianBlur(image, denoisedImage, new Size(5, 5), 0);
        return denoisedImage;
    }

    // 调整亮度和对比度
    public Mat adjustBrightnessContrast(Mat image, double alpha, double beta) {
        Mat newImage = new Mat();
        image.convertTo(newImage, -1, alpha, beta);
        return newImage;
    }

    // 转换为灰度图
    public Mat toGrayScale(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }

    // 提取表盘图像（示例）
    public Mat extractDial(Mat image) {
        // 在这里添加提取表盘的逻辑
        // ...
        return image;
    }

    // 增强处理表盘图像
    public Mat enhanceImage(Mat image) {
        // 在这里添加增强处理的逻辑
        // ...
        return image;
    }
    public Mat detectDial(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(9, 9), 2, 2);

        Mat circles = new Mat();
        Imgproc.HoughCircles(grayImage, circles, Imgproc.HOUGH_GRADIENT, 1, grayImage.rows() / 8, 100, 20, 0, 0);

        Mat mask = Mat.zeros(image.size(), CvType.CV_8U);

        // 处理检测到的圆（此处只处理第一个检测到的圆）
        if (circles.cols() > 0) {
            double[] c = circles.get(0, 0);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            int radius = (int) Math.round(c[2]);

            // 创建一个只包含圆形区域的掩模
            Imgproc.circle(mask, center, radius, new Scalar(255,255,255), -1);
        }

        Mat dial = new Mat();
        image.copyTo(dial, mask); // 使用掩模提取圆形区域
        return dial;
    }


    // 概率霍夫变换来检测直线（如刻度线和指针）
    public Mat detectLines(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayImage, grayImage, 50, 200, 3, false);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(grayImage, lines, 1, Math.PI/180, 50, 30, 10);

        // 在原图上绘制检测到的线
        for (int x = 0; x < lines.rows(); x++) {
            double[] l = lines.get(x, 0);
            Imgproc.line(image, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0,0,255), 3, Imgproc.LINE_AA, 0);
        }

        return image;
    }

    // 其他图像处理方法...
}

