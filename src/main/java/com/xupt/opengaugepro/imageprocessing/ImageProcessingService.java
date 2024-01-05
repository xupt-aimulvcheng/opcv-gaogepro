package com.xupt.opengaugepro.imageprocessing;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);

    // 加载图像
    public Mat loadImage(String filePath) {
        return Imgcodecs.imread(filePath);
    }
    // 去噪声
    public Mat denoise(Mat image, String method) {
        Mat denoisedImage = new Mat();

        switch (method) {
            //
            case "gaussian" -> Imgproc.GaussianBlur(image, denoisedImage, new Size(5, 5), 0);
            case "median" -> Imgproc.medianBlur(image, denoisedImage, 5);
            case "bilateral" -> Imgproc.bilateralFilter(image, denoisedImage, 9, 75, 75);
            default -> image.copyTo(denoisedImage);
        }

        return resizeImage(denoisedImage,640,480);
    }
    // 调整亮度和对比度
    public Mat adjustBrightnessContrast(Mat image, double alpha, double beta) {
        Mat newImage = new Mat();
        image.convertTo(newImage, -1, alpha, beta);
        return resizeImage(newImage,630,480);
    }

    // 转换为灰度图
    public Mat toGrayScale(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return resizeImage(grayImage,640,480);
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

        return resizeImage(image,640,480);
    }
    // 对图片进行宽高的设置
    public Mat resizeImage(Mat image, int targetWidth, int targetHeight) {
        // 计算缩放比例
        double scale = Math.min((double)targetWidth / image.width(), (double)targetHeight / image.height());

        // 新的图像尺寸
        int newWidth = (int)(image.width() * scale);
        int newHeight = (int)(image.height() * scale);

        // 等比例缩放图像
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new Size(newWidth, newHeight), 0, 0, Imgproc.INTER_LINEAR);

        // 创建一个目标大小的白色背景
        Mat finalImage = new Mat(targetHeight, targetWidth, image.type(), new Scalar(255,255,255));

        // 计算缩放后图像应该放置的位置
        int offsetX = (targetWidth - newWidth) / 2;
        int offsetY = (targetHeight - newHeight) / 2;

        // 在白色背景上居中放置缩放后的图像
        Mat roi = finalImage.submat(offsetY, offsetY + newHeight, offsetX, offsetX + newWidth);
        resizedImage.copyTo(roi);

        return finalImage;
    }




    // 检测表盘。使用高斯模糊和霍夫圆变换来检测圆形（表盘），然后使用掩模提取圆形区域。
    public Mat detectDial(Mat image) {
        // 对比度限制直方图均衡化（CLAHE）
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.apply(grayImage, grayImage);

        // 降低分辨率
        Mat resizedImage = new Mat();
        Imgproc.resize(grayImage, resizedImage, new Size(), 0.5, 0.5, Imgproc.INTER_LINEAR);
        Imgproc.GaussianBlur(resizedImage, resizedImage, new Size(9, 9), 2, 2);

        // 霍夫圆变换参数调整
        Mat circles = new Mat();
        Imgproc.HoughCircles(resizedImage, circles, Imgproc.HOUGH_GRADIENT, 1, resizedImage.rows() / 8, 100, 20, 0, 0);

        // 创建白色背景
        Mat whiteBackground = new Mat(image.size(), image.type(), new Scalar(255,255,255));

        // 处理圆
        if (circles.cols() > 0) {
            double[] c = circles.get(0, 0);
            Point center = new Point(Math.round(c[0] * 2), Math.round(c[1] * 2)); // 调整中心位置
            int radius = (int) Math.round(c[2] * 2); // 调整半径

            Mat mask = Mat.zeros(image.size(), CvType.CV_8U);
            Imgproc.circle(mask, center, radius, new Scalar(255,255,255), -1);
            image.copyTo(whiteBackground, mask);
        }

        return resizeImage(whiteBackground, 640, 480);
    }
    // 区域分割
    public Mat segmentDial(Mat image) {
        try {
            // 预处理
            Mat preprocessedImage = preprocessForStrongLight(image);

            Mat grayImage = preprocessedImage;
            if (preprocessedImage.channels() > 1) {
                // 如果预处理后的图像是彩色的，则转换为灰度图
                grayImage = new Mat();
                Imgproc.cvtColor(preprocessedImage, grayImage, Imgproc.COLOR_BGR2GRAY);
            }

            // 应用阈值分割
            Mat binaryImage = new Mat();
            Imgproc.threshold(grayImage, binaryImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            return resizeImage(binaryImage,640,480);
        } catch (Exception e) {
            logger.error("Error during dial segmentation", e);
        }

        return null;
    }
    private Mat preprocessForStrongLight(Mat image) {
        // 应用对比度限制的自适应直方图均衡化（CLAHE）
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(2.0);
        Mat enhancedImage = new Mat();
        clahe.apply(grayImage, enhancedImage);

        return resizeImage(enhancedImage,640,480);
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

        return resizeImage(enhancedImage,640,480);
    }


    // 增强处理表盘图像
    public Mat enhanceImage(Mat image) {
        // 在这里添加增强处理的逻辑
        // ...
        return image;
    }

    // 刻度突出显示
    public Mat highlightScales(Mat image) {

        return image;
    }
    // 数字增强
    public Mat enhanceDigits(Mat image) {
        try {
            // 数字检测
            Mat digits = detectDigits(image);
            Mat pointers = detectPointers(image);
            Mat circleCenter = detectCircleCenter(image);

            // 创建掩模
            Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);
            Core.bitwise_or(digits, mask, mask);
            Core.bitwise_or(pointers, mask, mask);
            Core.bitwise_or(circleCenter, mask, mask);

            // 转换原图为灰度图
            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

            // 应用掩模
            Mat result = new Mat();
            grayImage.copyTo(result, mask);

            // 应用CLAHE
            CLAHE clahe = Imgproc.createCLAHE();
            clahe.setClipLimit(4);
            clahe.apply(result, result);

            // 应用锐化
            Mat kernel = new Mat(3, 3, CvType.CV_32F, new Scalar(0));
            kernel.put(1, 1, 5);
            kernel.put(0, 1, -1);
            kernel.put(1, 0, -1);
            kernel.put(1, 2, -1);
            kernel.put(2, 1, -1);
            Imgproc.filter2D(result, result, -1, kernel);

            // 边缘强化
            result = enhanceEdges(result);

            return result;
        } catch (Exception e) {
            System.err.println("Error enhancing digits: " + e.getMessage());
            return null;
        }
    }




    // 使用Canny边缘检测来识别指针
    public Mat detectPointers(Mat image) {
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 50, 150);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 50, 30, 10);
        Mat pointers = Mat.zeros(image.size(), CvType.CV_8UC1);
        for (int i = 0; i < lines.rows(); i++) {
            double[] val = lines.get(i, 0);
            Imgproc.line(pointers, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(255), 2);
        }

        return pointers;
    }

    // 使用霍夫圆变换来检测圆心
    public Mat detectCircleCenter(Mat image) {
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, gray, new Size(9, 9), 2, 2);
        Mat circles = new Mat();
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1, gray.rows() / 8, 200, 100, 0, 0);

        Mat circleCenters = Mat.zeros(image.size(), CvType.CV_8UC1);
        for (int i = 0; i < circles.cols(); i++) {
            double[] c = circles.get(0, i);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            Imgproc.circle(circleCenters, center, 1, new Scalar(255), 3, 8, 0);
        }

        return circleCenters;
    }
    public Mat detectDigits(Mat image) {
        // 转换到灰度图像
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // 应用阈值处理来提取可能的数字区域
        Mat thresh = new Mat();
        Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        // 应用形态学操作来分离数字
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Mat morph = new Mat();
        Imgproc.morphologyEx(thresh, morph, Imgproc.MORPH_CLOSE, kernel);

        // 找到轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(morph, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 创建一个与原图大小相同的空白图像
        Mat digits = Mat.zeros(image.size(), CvType.CV_8UC1);

        // 绘制检测到的数字轮廓
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            // 可以在这里添加进一步的检查来过滤掉非数字的轮廓
            Imgproc.rectangle(digits, rect.tl(), rect.br(), new Scalar(255), -1);
        }

        return digits;
    }









    // 其他图像处理方法...
}

