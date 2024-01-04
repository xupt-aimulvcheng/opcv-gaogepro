package com.xupt.opengaugepro.factory;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;

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
            Image image = new Image("file:" + file.getAbsolutePath());
            currentImageFile = file;
            originalImageView.setImage(image);
            processedImageView.setImage(null);

            // 调整 ImageView 的大小以填充一半的窗口
            double width = stage.getWidth() / 2;
            originalImageView.setFitWidth(width);
            processedImageView.setFitWidth(width);

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
            case "denoise" -> image = imageProcessingService.denoise(image);
            case "adjustBrightness" -> image = imageProcessingService.adjustBrightnessContrast(image, 1.2, 50);
            case "toGrayScale" -> image = imageProcessingService.toGrayScale(image);
            case "extractDial" -> image = imageProcessingService.detectDial(image); // 使用detectDial替代extractDial
            case "enhance" -> image = imageProcessingService.enhanceImage(image);
            default -> {
                showAlert("错误", "未知的处理类型：" + processType);
                return;
            }
        }

        processedImage = image;

        Image fxImage = imageConverter.convertMatToJavaFXImage(image);
        processedImageView.setImage(fxImage);
        status.setStatus("已完成");
        statusLabel.setText(status.getStatus());
        // 处理图像后重设放大缩小的比例
        processedImageView.setFitWidth(originalImageView.getFitWidth());
        processedImageView.setFitHeight(originalImageView.getFitHeight());
        lastZoomFactorProcessed = lastZoomFactorOriginal; // 使用原图的放大比例
    }


    private void showAlert(String title, String message) {
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

        imageView.setFitWidth(imageView.getFitWidth() * scaleFactor);
        imageView.setFitHeight(imageView.getFitHeight() * scaleFactor);
    }
}
