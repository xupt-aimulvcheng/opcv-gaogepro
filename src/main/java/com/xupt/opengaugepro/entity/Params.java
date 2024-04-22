package com.xupt.opengaugepro.entity;

import org.opencv.core.Size;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;

public class Params {
    public static double claheClipLimit = 0.2; // CLAHE 对比度限制
    public  static Size claheTileGridSize = new Size(8, 8); // CLAHE 网格大小
    public  static float sharpenStrength = 8.0f; // 锐化强度
    public  static int thresholdType = THRESH_BINARY + THRESH_OTSU; // 二值化类型
    public  static int morphologySize = 2; // 形态学操作的核大小
    public static double Magnification = 1.2;  // 放大倍数
    public static double Minification = 0.8;  // 缩小倍数
    public static String doniseType = "gaussian"; // 去噪算法
}
