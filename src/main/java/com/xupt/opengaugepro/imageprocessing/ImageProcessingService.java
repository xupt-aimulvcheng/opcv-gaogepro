package com.xupt.opengaugepro.imageprocessing;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageProcessingService {


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


    // 增强处理表盘图像
    public Mat enhanceImage(Mat image) {
        // 在这里添加增强处理的逻辑
        // ...
        return image;
    }
    // 检测表盘。使用高斯模糊和霍夫圆变换来检测圆形（表盘），然后使用掩模提取圆形区域。
    public Mat detectDial(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(9, 9), 2, 2);

        Mat circles = new Mat();
        Imgproc.HoughCircles(grayImage, circles, Imgproc.HOUGH_GRADIENT, 1, grayImage.rows() / 8, 100, 20, 0, 0);

        // 创建一个与原图大小相同的白色背景图像
        Mat whiteBackground = new Mat(image.size(), image.type(), new Scalar(255,255,255));

        // 如果检测到圆形，则将表盘区域复制到白色背景图像上
        if (circles.cols() > 0) {
            double[] c = circles.get(0, 0);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            int radius = (int) Math.round(c[2]);

            // 创建一个掩模，其中圆形区域是白色，其他区域是黑色
            Mat mask = Mat.zeros(image.size(), CvType.CV_8U);
            Imgproc.circle(mask, center, radius, new Scalar(255,255,255), -1);

            // 将原图中的表盘区域复制到白色背景上
            image.copyTo(whiteBackground, mask);
        }
        return resizeImage(whiteBackground,640,480);
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
    // 边缘强化
    public Mat enhanceEdges(Mat image) {
        // 首先使用 detectDial 方法定位表盘
        Mat dial = detectDial(image);

        // 然后在表盘区域进行边缘强化
        Mat edges = new Mat();
        Imgproc.cvtColor(dial, edges, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(edges, edges, new Size(3, 3), 0);
        Imgproc.Canny(edges, edges, 50, 150, 3, false);

        // 创建一个与原始图像同样大小的空白图像
        Mat enhancedImage = Mat.zeros(image.size(), image.type());

        // 将边缘强化后的表盘图像复制到空白图像的对应区域
        dial.copyTo(enhancedImage, edges);

        return enhancedImage;
    }
    // 刻度突出显示
    public Mat highlightScales(Mat image) {

        return image;
    }
    // 数字增强
    public Mat enhanceDigits(Mat image) {

        return image;
    }
    // 对比度调整
    public Mat adjustContrast(Mat image) {

        return image;
    }

    // 对图片进行宽高的设置
    public Mat resizeImage(Mat image, int width, int height) {
        Mat resizedImage = new Mat();
        Size size = new Size(width, height);
        Imgproc.resize(image, resizedImage, size);
        return resizedImage;
    }

    // 其他图像处理方法...
}

