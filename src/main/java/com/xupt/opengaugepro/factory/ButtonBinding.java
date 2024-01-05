package com.xupt.opengaugepro.factory;

import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

import static com.xupt.opengaugepro.DialImageProcessingApp.*;

//按钮绑定的行为
public class ButtonBinding {
    private double lastZoomFactorOriginal = 1.0;
    private double lastZoomFactorProcessed = 1.0;

    public void uploadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图像文件");
        String userDir = System.getProperty("user.dir");
        File resourceDir = new File(userDir, "src/main/resources/image");
        if (resourceDir.exists() && resourceDir.isDirectory()) {
            fileChooser.setInitialDirectory(resourceDir);
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图像文件", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            // 将文件路径转换为Mat对象
            Mat image = Imgcodecs.imread(file.getAbsolutePath());

            // 调整图片大小到640x480
            Mat resizedImage = imageProcessingService.resizeImage(image, 640, 480);
            // 将Mat对象转换回Image对象以在JavaFX中显示
            Image fxImage = imageConverter.convertMatToJavaFXImage(resizedImage);

            currentImageFile = file;
            originalImageView.setImage(fxImage);  // 注意这里使用的是fxImage
            processedImageView.setImage(null);

            // 不需要调整ImageView的大小，因为图片已经调整到了640x480
            // originalImageView.setFitWidth(width);
            // processedImageView.setFitWidth(width);

            // 更新状态
            status.setStatus("已上传");
            statusLabel.setText(status.getStatus());
        } else {
            showAlert("错误", "未选择任何文件");
        }
    }


    public void processImage(String processType) {
        if (currentImageFile == null) {
            showAlert("提示", "请先上传一张图像。");
            return;
        }

        String filePath = currentImageFile.getAbsolutePath();
        Mat image = imageProcessingService.loadImage(filePath);

        switch (processType) {
            case "denoise" ->
                // 使用高斯滤波进行去噪
                    image = imageProcessingService.denoise(image, "gaussian");
            case "adjustBrightness" ->
                // 调整图像的亮度和对比度
                    image = imageProcessingService.adjustBrightnessContrast(image, 1.2, 50);
            case "toGrayScale" ->
                // 将图像转换为灰度图
                    image = imageProcessingService.toGrayScale(image);
            case "extractDial" ->
                // 检测并提取表盘区域
                    image = imageProcessingService.detectDial(image);
            case "edgeEnhancement" ->
                // 对图像进行边缘强化处理
                    image = imageProcessingService.enhanceEdges(image);
            case "scaleHighlight" ->
                // 突出显示表盘上的刻度
                    image = imageProcessingService.highlightScales(image);
            case "digitEnhancement" ->
                // 增强表盘上的数字可读性
                    image = imageProcessingService.enhanceDigits(image);
            case "enhance" ->
                // 对图像进行总体增强处理
                    image = imageProcessingService.enhanceImage(image);
            case "segment" ->
                // 区域分割
                    image = imageProcessingService.segmentDial(image);
            default -> {
                // 处理未知的处理类型
                showAlert("错误", "未知的处理类型：" + processType);
                return;
            }
        }

        Image fxImage = imageConverter.convertMatToJavaFXImage(image);
        processedImageView.setImage(fxImage);
        status.setStatus("已完成");
        statusLabel.setText(status.getStatus());

        // 重设图像缩放
        processedImageView.setFitWidth(originalImageView.getFitWidth());
        processedImageView.setFitHeight(originalImageView.getFitHeight());
        lastZoomFactorProcessed = lastZoomFactorOriginal;
    }




    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void zoomImage(boolean zoomIn, boolean isOriginal) {
        ImageView imageView = isOriginal ? originalImageView : processedImageView;
        double scaleFactor = zoomIn ? 1.2 : 0.8; // 放大或缩小20%

        // 更新放大缩小比例
        if (isOriginal) {
            lastZoomFactorOriginal *= scaleFactor;
        } else {
            lastZoomFactorProcessed *= scaleFactor;
        }

        // 计算新的图像尺寸
        double newWidth = imageView.getImage().getWidth() * lastZoomFactorOriginal;
        double newHeight = imageView.getImage().getHeight() * lastZoomFactorOriginal;

        imageView.setFitWidth(newWidth);
        imageView.setFitHeight(newHeight);

        // 如果ImageView被放置在ScrollPane中
        ScrollPane scrollPane = isOriginal ? originalImageScrollPane : processedImageScrollPane;
        if (scrollPane != null) {
            // 如果图像放大到超出ScrollPane的尺寸，则需要设置为不保持比例
            // 以允许用户通过滚动条查看图像的不同部分
            imageView.setPreserveRatio(newWidth <= scrollPane.getWidth() && newHeight <= scrollPane.getHeight());
        }
    }

}
