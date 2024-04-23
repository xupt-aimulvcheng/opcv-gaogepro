package com.xupt.opengaugepro.factory;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.dialog.ProgressDialog;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.xupt.opengaugepro.DialImageProcessingApp.*;
import static com.xupt.opengaugepro.entity.Params.*;

//按钮绑定的行为
public class ButtonBinding {
    private ImageView originalImageView;
    private ImageView processedImageView;
    private Label statusLabel;
    private double lastZoomFactorOriginal = 1.0;
    private double lastZoomFactorProcessed = 1.0;
    private Logger logger = LoggerFactory.getLogger(ButtonBinding.class);

    public ButtonBinding(ImageView originalImageView, ImageView processedImageView, Label statusLabel) {
        this.originalImageView = originalImageView;
        this.processedImageView = processedImageView;
        this.statusLabel = statusLabel;
    }
    //上传图片
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
            originalImageView.setImage(fxImage);
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

    /**
     * 处理图像
     * @param processType 处理图像的类型（例如，去噪、亮度调整等）。
     * @param stage 主应用程序的舞台，可能用于显示进度信息或其他对话框。
     */
    public void processImage(String processType, Stage stage) {
        if (currentImageFile == null) {
            showAlert("提示", "请先上传一张图像。");
            return;
        }

        status.setStatus("处理中");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String filePath = currentImageFile.getAbsolutePath();
                Mat image = imageProcessingService.loadImage(filePath);

                updateMessage("正在处理图像...");
                // 这里模拟图像处理过程
                Thread.sleep(1000); // 假设耗时操作
                // 根据处理类型调用不同的图像处理方法
                switch (processType) {
                    case "denoise" ->
                        // 使用高斯滤波进行去噪
                            image = imageProcessingService.denoise(image);
                    case "adjustBrightness" ->
                        // 调整图像的亮度和对比度
                            image = imageProcessingService.adjustBrightnessContrast(image, alphaValue, betaValue);
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
                            image = imageProcessingService.enhanceDigits(image, claheClipLimit, claheTileGridSize, sharpenStrength, thresholdType, morphologySize);
                    case "enhance" ->
                        // 对图像进行总体增强处理
                            image = imageProcessingService.enhanceImage(image);
                    case "segment" ->
                        // 区域分割
                            image = imageProcessingService.segmentDial(image);
                    default -> {
                        // 处理未知的处理类型
                        showAlert("错误", "未知的处理类型：" + processType);
                    }
                }

                Image fxImage = imageConverter.convertMatToJavaFXImage(image);

                // 因为我们正在后台线程中更新UI组件，所以需要在JavaFX应用线程中执行
                Platform.runLater(() -> {
                    processedImageView.setImage(fxImage);
                    status.setStatus("已完成");
                    statusLabel.setText(status.getStatus());
                    lastZoomFactorProcessed = lastZoomFactorOriginal;
                });

                return null;
            }
        };

        task.setOnFailed(e -> {
            Throwable problem = task.getException();
            showAlert("错误", "处理图像时发生错误: " + problem.getMessage());
            logger.error("处理图像时发生错误: {}", problem.getMessage());
        });

        showProgressDialog(stage, task);
    }

    /**
     * 显示进度对话框
     * @param stage 主应用程序的舞台，用于关联进度对话框。
     * @param task 执行的后台任务，其进度信息将显示在进度对话框中。
     */
    private void showProgressDialog(Stage stage, Task<?> task) {
        // 创建一个进度条对话框
        ProgressDialog progressDialog = new ProgressDialog(task);
        progressDialog.initOwner(stage);
        progressDialog.setTitle("处理中");

        // 启动任务
        Thread thread = new Thread(task);
        thread.start();
    }


    /**
     * 显示消息对话框
     * @param title 对话框的标题。
     * @param message 显示在对话框中的消息内容。
     */
    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 放大或缩小图像
     * @param zoomIn 如果为true，则放大图像；如果为false，则缩小图像。
     * @param isOriginal  如果为true，则对原始图像进行缩放；如果为false，则对处理过的图像进行缩放。
     */
    public void zoomImage(boolean zoomIn, boolean isOriginal) {
        if (currentImageFile == null) {
            showAlert("原图像不存在", "请先上传一张图像，然后再尝试放大或缩小。");
            return;
        }

        ImageView imageView = isOriginal ? originalImageView : processedImageView;

        // 仅当对处理过的图像进行操作时，检查是否存在处理过的图像
        if (!isOriginal && processedImageView.getImage() == null) {
            showAlert("处理图像不存在", "请先处理一张图像，然后再尝试放大或缩小。");
            return;
        }

        double scaleFactor = zoomIn ? Magnification : Minification; // 放大或缩小20%

        // 更新放大缩小比例
        double newZoomFactor = isOriginal ? lastZoomFactorOriginal * scaleFactor : lastZoomFactorProcessed * scaleFactor;
        if (isOriginal) {
            lastZoomFactorOriginal = newZoomFactor;
        } else {
            lastZoomFactorProcessed = newZoomFactor;
        }
        imageView.setScaleX(newZoomFactor);
        imageView.setScaleY(newZoomFactor);
    }

}
