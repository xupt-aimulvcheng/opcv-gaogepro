package com.xupt.opengaugepro.imageprocessing;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.xupt.opengaugepro.entity.Params.doniseType;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.imgproc.Imgproc.*;

public class ImageProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);

    // 加载图像
    public Mat loadImage(String filePath) {
        return Imgcodecs.imread(filePath);
    }
    // 去噪声
    public Mat denoise(Mat image) {
        Mat denoisedImage = new Mat();

        switch (doniseType) {
            // 使用不同的去噪方法
            case "gaussian" -> Imgproc.GaussianBlur(image, denoisedImage, new Size(5, 5), 0); // 高斯模糊
            case "median" -> Imgproc.medianBlur(image, denoisedImage, 5); // 中值滤波
            case "bilateral" -> Imgproc.bilateralFilter(image, denoisedImage, 9, 75, 75); // 双边滤波
            default -> image.copyTo(denoisedImage); // 默认不做处理
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
        cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return resizeImage(grayImage,640,480);
    }
    // 概率霍夫变换来检测直线（如刻度线和指针）
    public Mat detectLines(Mat image) {
        Mat grayImage = new Mat();
        cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Canny(grayImage, grayImage, 50, 200, 3, false);

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
        // 初始化灰度图像
        Mat grayImage = new Mat();
        cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.apply(grayImage, grayImage); // 现在grayImage是适用于CLAHE的灰度图

        // 应用高斯模糊
        Mat resizedImage = new Mat();
        Imgproc.resize(grayImage, resizedImage, new Size(), 0.5, 0.5, Imgproc.INTER_LINEAR);
        GaussianBlur(resizedImage, resizedImage, new Size(9, 9), 2, 2);

        // 霍夫圆变换参数调整
        Mat circles = new Mat();
        // 使用霍夫圆变换算法检测图像中的圆形。这里调整了算法的几个关键参数：
        // 1: dp - 累加器分辨率与图像分辨率的反比，这里设置为1表示分辨率相同。
        // resizedImage.rows() / 8: 最小圆心距，即检测到的圆心之间的最小距离。
        // 100: Canny边缘检测器的高阈值。
        // 20: 圆心累加器的阈值，值越高，检出的圆越少但质量越高。
        // 0, 0: 最小半径和最大半径，两个设置为0表示不限制圆的大小。
        Imgproc.HoughCircles(resizedImage, circles, Imgproc.HOUGH_GRADIENT, 1, resizedImage.rows() / 8, 100, 20, 0, 0);

        // 创建白色背景
        Mat whiteBackground = new Mat(image.size(), image.type(), new Scalar(255,255,255));

        // 处理圆
        if (circles.cols() > 0) {
            // 获取第一个检测到的圆的信息
            double[] c = circles.get(0, 0);
            // 中心位置坐标调整，因为前面对图像进行了缩放，这里需要将圆心坐标调整回原始图像的比例
            Point center = new Point(Math.round(c[0] * 2), Math.round(c[1] * 2));
            // 半径调整，同样因为图像缩放的原因，将半径调整回原始图像的比例
            int radius = (int) Math.round(c[2] * 2);

            // 创建一个遮罩，用于从原图中提取圆形区域
            Mat mask = Mat.zeros(image.size(), CvType.CV_8U);
            // 在遮罩上绘制圆形，白色区域表示将要提取的部分
            Imgproc.circle(mask, center, radius, new Scalar(255,255,255), -1);
            // 使用遮罩将圆形区域从原始图像中提取出来，存放在白色背景上
            image.copyTo(whiteBackground, mask);
        }


        return resizeImage(whiteBackground, 640, 480);
    }

    // 区域分割
    public Mat segmentDial(Mat image) {
        try {
            // 预处理：对图像进行强光环境下的预处理
            Mat preprocessedImage = preprocessForStrongLight(image);

            Mat grayImage = preprocessedImage;
            if (preprocessedImage.channels() > 1) {
                // 如果预处理后的图像是彩色的，则将其转换为灰度图，因为灰度图更适合进行阈值分割
                grayImage = new Mat();
                cvtColor(preprocessedImage, grayImage, Imgproc.COLOR_BGR2GRAY);
            }

            // 应用阈值分割：使用OTSU算法自动计算最优阈值并进行二值化处理
            Mat binaryImage = new Mat();
            Imgproc.threshold(grayImage, binaryImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

            // 返回调整大小后的二值化图像，通常用于进一步的图像分析或处理
            return resizeImage(binaryImage, 640, 480);
        } catch (Exception e) {
            logger.error("区域分割失败", e);
        }

        return null;
    }

    private Mat preprocessForStrongLight(Mat image) {
        // 应用对比度限制的自适应直方图均衡化（CLAHE）
        Mat grayImage = new Mat();
        // 首先将原图像转换为灰度图，因为CLAHE处理是在灰度图上进行的
        cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        // 创建CLAHE对象
        CLAHE clahe = Imgproc.createCLAHE();
        // 设置对比度限制，较高的值可以改善图像的对比度，特别是在强光条件下
        clahe.setClipLimit(2.0);
        Mat enhancedImage = new Mat();
        // 应用CLAHE算法，增强图像的局部对比度
        clahe.apply(grayImage, enhancedImage);

        // 返回调整大小后的图像，通常是为了保持一致的处理输出或减少计算量
        return resizeImage(enhancedImage, 640, 480);
    }

    // 边缘强化方法
    public Mat enhanceEdges(Mat image) {
        // 首先检测表盘
        Mat dial = detectDial(image);
        if (dial == null || dial.empty()) { // 检测失败或返回空图像
            logger.error("detectDial返回了一个空图像");
            return null;
        }

        // 如果图像不是灰度图，转换为灰度图
        Mat edges = new Mat();
        if (dial.channels() > 1) {
            Imgproc.cvtColor(dial, edges, Imgproc.COLOR_BGR2GRAY);
        } else {
            edges = dial.clone(); // 如果已经是灰度图，直接复制
        }

        // 应用高斯模糊
        Imgproc.GaussianBlur(edges, edges, new Size(3, 3), 0.5);

        // 执行Canny边缘检测
        Imgproc.Canny(edges, edges, 60, 200);

        // 创建一个与原图同大小、同类型的新图像，来保存边缘强化后的结果
        Mat enhancedImage = new Mat(image.size(), CvType.CV_8UC1);
        enhancedImage.setTo(Scalar.all(0)); // 初始为全黑

        // 将检测到的边缘复制到新图像上
        edges.copyTo(enhancedImage);

        // 如有必要，调整图像大小
        Mat resizedImage = new Mat();
        Size newSize = new Size(640, 480); // 新的尺寸
        Imgproc.resize(enhancedImage, resizedImage, newSize);

        // 调整图像的亮度和对比度
        return adjustBrightnessContrast(resizedImage, 1.2, 50);
    }




    // 增强处理表盘图像
    public Mat enhanceImage(Mat image) {
        return image;
    }

   //  // 边缘强化
   // public Mat enhanceEdges(Mat image) {
   //     Mat grayImage = detectDial(image);
   //     // 首先转换图像到灰度，因为边缘检测通常在灰度图上执行
   //     Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
   //
   //     // 应用高斯模糊来减少图像噪声
   //     Imgproc.GaussianBlur(grayImage, grayImage, new Size(3, 3), 0);
   //
   //     // 使用Canny算法检测边缘
   //     Mat edges = new Mat();
   //     Imgproc.Canny(grayImage, edges, 50, 150);
   //
   //     return edges; // 返回边缘检测结果，类型为CV_8UC1
   // }
    // 刻度线检测
    public Mat highlightScales(Mat image) {
        // 边缘增强
        Mat enhancedEdgesImage = enhanceEdges(image);
        if (enhancedEdgesImage == null || enhancedEdgesImage.empty()) {
            logger.error("边缘强化失败或返回空图像");
            return null;
        }

        // 转换为灰度图
        if (enhancedEdgesImage.channels() > 1) {
            cvtColor(enhancedEdgesImage, enhancedEdgesImage, Imgproc.COLOR_BGR2GRAY);
        }

        // 应用二值化
        Imgproc.threshold(enhancedEdgesImage, enhancedEdgesImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        // 霍夫线变换以检测刻度线
        Mat lines = new Mat();
        double rho = 1; // 像素精度
        double theta = Math.PI / 180; // 角度精度
        int threshold = 50; // 累加器阈值
        int minLineLength = 10; // 最小线长度
        int maxLineGap = 10; // 最大线间隙

        Imgproc.HoughLinesP(enhancedEdgesImage, lines, rho, theta, threshold, minLineLength, maxLineGap);

        // 检测表盘中心和半径
        double[] radiusOutput = new double[1];
        Point dialCenter = findDialCenterAndRadius(enhancedEdgesImage, radiusOutput);
        double dialRadius = radiusOutput[0];
        logger.info("表盘中心坐标: ({}, {})", dialCenter.x, dialCenter.y);

        // 绘制直线
        for (int i = 0; i < lines.rows(); i++) {
            double[] val = lines.get(i, 0);
            Point start = new Point(val[0], val[1]);
            Point end = new Point(val[2], val[3]);

            // 过滤条件：长度和距离中心的相对位置
            double length = Math.hypot(end.x - start.x, end.y - start.y);
            double distanceFromCenterStart = Math.hypot(start.x - dialCenter.x, start.y - dialCenter.y);
            double distanceFromCenterEnd = Math.hypot(end.x - dialCenter.x, end.y - dialCenter.y);

            // 保留长度合适且一端靠近表盘边缘的线条
            if (length < dialRadius * 1.2 && length > dialRadius * 0.8) {
                if (Math.abs(distanceFromCenterStart - dialRadius) < dialRadius * 0.1 ||
                        Math.abs(distanceFromCenterEnd - dialRadius) < dialRadius * 0.1) {
                    // 使用细线绘制刻度线
                    Imgproc.line(enhancedEdgesImage, start, end, new Scalar(0, 255, 0), 1);
                }
            }
        }
        return enhancedEdgesImage;
    }



    public Point findDialCenterAndRadius(Mat image, double[] radiusOutput) {
        Mat grayImage = image;
        // 检查图像是否已经是灰度图
        if (image.channels() > 1) {
            grayImage = new Mat();
            cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        }

        // 应用高斯模糊减少噪声，改善霍夫圆变换的效果
        GaussianBlur(grayImage, grayImage, new Size(9, 9), 2, 2);

        Mat circles = new Mat();
        // 进行霍夫圆变换以检测圆
        Imgproc.HoughCircles(grayImage, circles, Imgproc.HOUGH_GRADIENT, 1, grayImage.rows()/8, 100, 30, 0, 0);

        Point dialCenter = new Point();
        double maxRadius = 0;

        // 寻找最大的圆
        for (int i = 0; i < circles.cols(); i++) {
            double[] circle = circles.get(0, i);
            Point center = new Point(Math.round(circle[0]), Math.round(circle[1]));
            double radius = circle[2];

            if (radius > maxRadius) {
                maxRadius = radius;
                dialCenter = center;
            }
        }

        if (radiusOutput != null && radiusOutput.length > 0) {
            radiusOutput[0] = maxRadius; // 通过引用返回找到的半径
        }

        return dialCenter; // 返回找到的表盘中心点
    }

    /**
     * 增强图像中的数字识别度。
     * @param image 输入的原始BGR图像。
     * @return 处理后的图像，增强了数字的可读性。
     */
    public Mat enhanceDigits(Mat image) {
        // 转换为灰度图像
        Mat grayImage = new Mat();
        cvtColor(image, grayImage, COLOR_BGR2GRAY);

        // 应用CLAHE
        CLAHE clahe = createCLAHE(2.0, new Size(8, 8));
        Mat claheResult = new Mat();
        clahe.apply(grayImage, claheResult);

        // 创建并应用锐化核
        Mat sharpenKernel = new Mat(3, 3, CV_32FC1, new Scalar(-1));
        sharpenKernel.put(1, 1, 8); // 核心点强化
        Mat enhancedImage = enhanceImage(image);
        filter2D(claheResult, enhancedImage, claheResult.depth(), sharpenKernel);

        // 二值化处理
        threshold(enhancedImage, enhancedImage, 0, 255, THRESH_BINARY + THRESH_OTSU);

        // 形态学操作以清晰数字的边缘
        Mat morphKernel = getStructuringElement(MORPH_RECT, new Size(2, 2));
        morphologyEx(enhancedImage, enhancedImage, MORPH_CLOSE, morphKernel);

        // 找出轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(enhancedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        // 在原图上绘制轮廓
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(0, 255, 0);
            Imgproc.drawContours(image, contours, i, color, 2, Imgproc.LINE_8, hierarchy, 0, new Point());
        }

        return image;
    }
    public Mat enhanceDigits(Mat image, double claheClipLimit, Size claheTileGridSize, float sharpenStrength, int thresholdType, int morphologySize) {
        // 转换为灰度图像
        Mat grayImage = new Mat();
        cvtColor(image, grayImage, COLOR_BGR2GRAY);

        // 应用CLAHE算法增强图像对比度
        CLAHE clahe = createCLAHE(claheClipLimit, claheTileGridSize);
        Mat claheResult = new Mat();
        clahe.apply(grayImage, claheResult);

        // 应用高斯模糊去噪
        Mat blurredImage = new Mat();
        GaussianBlur(claheResult, blurredImage, new Size(5, 5), 0);

        // 创建并锐化图像，以增强图像中的细节
        Mat sharpenKernel = new Mat(3, 3, CvType.CV_32F, new Scalar(0));
        sharpenKernel.put(1, 1, sharpenStrength);
        float[] surround = {-1, -1, -1, -1, 9, -1, -1, -1, -1}; // 旁边点的负值强化核心点
        sharpenKernel.put(0, 0, surround);
        Mat sharpenedImage = enhanceImage(image);
        // 应用二维线性滤波器（filter2D）来锐化图像
        // blurredImage: 输入图像
        // sharpenedImage: 输出图像
        // blurredImage.depth(): 输入图像的深度
        // sharpenKernel: 自定义的锐化核，用于增强图像的局部对比度和边缘
        filter2D(blurredImage, sharpenedImage, blurredImage.depth(), sharpenKernel);


        // 使用阈值化方法将图像转换为二值图像。
        // 0: 阈值 - 该参数设置为0，表示使用OTSU方法自动计算最佳阈值。
        // 255: 最大值 - 当像素值超过（或等于）阈值时应赋予的新值。
        // thresholdType: 阈值类型
        threshold(sharpenedImage, sharpenedImage, 0, 255, thresholdType);


        // 创建结构元素，用于形态学操作。这里调整了算法的关键参数：
        // MORPH_RECT: 结构元素的形状，使用矩形用于闭运算处理边缘连接问题。
        // new Size(morphologySize, morphologySize): 定义结构元素的大小。
        Mat morphKernel = getStructuringElement(MORPH_RECT, new Size(morphologySize, morphologySize));

        // 应用形态学闭运算。闭运算用于关闭前景物体内部的小孔，或前景物体上的小黑点。
        // sharpenedImage: 输入图像。
        // sharpenedImage: 输出图像。
        // MORPH_CLOSE: 指定进行闭运算。
        // morphKernel: 使用的结构元素，决定了操作的精确性和效果。
        morphologyEx(sharpenedImage, sharpenedImage, MORPH_CLOSE, morphKernel);


        // 找出轮廓并绘制
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        // 使用`findContours`函数来检测图像中的轮廓。这里调整了算法的几个关键参数：
        // sharpenedImage: 输入图像。
        // contours: 输出轮廓，检测到的轮廓将存储为一个向量的向量，每个向量表示一个轮廓的所有点。
        // hierarchy: 轮廓的层次结构，包含关于图像拓扑的信息（例如，一个轮廓与其他轮廓的父子关系）。
        // RETR_EXTERNAL: 检测模式，这里只检测最外层的轮廓，忽略内部嵌套轮廓。
        // CHAIN_APPROX_SIMPLE: 轮廓近似方法，它只保留轮廓的拐点信息，减少存储空间。
        findContours(sharpenedImage, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(0, 255, 0);
            // 在图像上绘制轮廓，用于标示和可视化检测到的形状或对象。
            // image: 要绘制轮廓的目标图像。
            // contours: 包含所有检测到的轮廓的向量，每个轮廓由点的向量表示。
            // i: 指定要绘制的轮廓的索引，这里循环变量i表示绘制每一个轮廓。
            // color: 轮廓的颜色，这里使用绿色(0, 255, 0)标示轮廓。
            // 2: 轮廓线的粗细，这里设置为2像素。
            // LINE_8: 线条类型，LINE_8是8-connected line，表示线条边缘平滑。
            // hierarchy: 轮廓的层次结构，这里用于可能的层次绘制控制。
            // 0: 最大层数，这里设置为0表示只绘制指定轮廓，不进入更深层次。
            // new Point(): 偏移量，这里不进行偏移，即从原点开始绘制。
            drawContours(image, contours, i, color, 2, LINE_8, hierarchy, 0, new Point());

        }

        return image; // 返回增强后的原图，显示清晰的数字轮廓
    }}

