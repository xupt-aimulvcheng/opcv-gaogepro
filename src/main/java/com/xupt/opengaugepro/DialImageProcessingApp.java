package com.xupt.opengaugepro;

import com.xupt.opengaugepro.entity.Status;
import com.xupt.opengaugepro.imageprocessing.ImageProcessingService;
import com.xupt.opengaugepro.util.ImageConverter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import java.io.File;

public class DialImageProcessingApp extends Application {
    private static Status status;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        status = new Status();
    }
    private Mat processedImage; // 添加这行代码，用于存储处理后的图像

    private ImageView originalImageView;
    private ImageView processedImageView;
    private ImageProcessingService imageProcessingService;
    private ImageConverter imageConverter;
    private File currentImageFile;
    private Label statusLabel;
    private double lastZoomFactorOriginal = 1.0;
    private double lastZoomFactorProcessed = 1.0;

    @Override
    public void start(Stage primaryStage) {
        imageProcessingService = new ImageProcessingService();
        imageConverter = new ImageConverter();

        originalImageView = new ImageView();
        originalImageView.setFitWidth(200);
        originalImageView.setPreserveRatio(true);

        processedImageView = new ImageView();
        processedImageView.setFitWidth(200);
        processedImageView.setPreserveRatio(true);

        Button btnUpload = new Button("上传图像");
        Button btnDenoise = new Button("去噪");
        Button btnAdjustBrightness = new Button("调整亮度/对比度");
        Button btnToGrayScale = new Button("转换为灰度图");
        Button btnExtractDial = new Button("提取表盘图像");
        Button btnEnhance = new Button("增强处理表盘图像");
        Button btnZoomInOriginal = new Button("放大原图");
        Button btnZoomOutOriginal = new Button("缩小原图");
        Button btnZoomInProcessed = new Button("放大处理图");
        Button btnZoomOutProcessed = new Button("缩小处理图");
        // 为按钮添加事件处理
        btnUpload.setOnAction(event -> uploadImage(primaryStage));
        btnDenoise.setOnAction(event -> processImage("denoise"));
        btnAdjustBrightness.setOnAction(event -> processImage("adjustBrightness"));
        btnToGrayScale.setOnAction(event -> processImage("toGrayScale"));
        btnExtractDial.setOnAction(event -> processImage("extractDial"));
        btnEnhance.setOnAction(event -> processImage("enhance"));
        btnZoomInOriginal.setOnAction(event -> zoomImage(true, true));
        btnZoomOutOriginal.setOnAction(event -> zoomImage(false, true));
        btnZoomInProcessed.setOnAction(event -> zoomImage(true, false));
        btnZoomOutProcessed.setOnAction(event -> zoomImage(false, false));

        HBox controlPanel = new HBox(10, btnUpload, btnZoomInOriginal, btnZoomOutOriginal, btnZoomInProcessed, btnZoomOutProcessed, btnDenoise, btnAdjustBrightness, btnToGrayScale, btnExtractDial, btnEnhance);
        controlPanel.setPadding(new javafx.geometry.Insets(10));

        statusLabel = new Label(status.getStatus());

        VBox statusBar = new VBox(statusLabel);
        statusBar.setPadding(new javafx.geometry.Insets(10));

        HBox imagePanel = new HBox(10, originalImageView, processedImageView);

        BorderPane root = new BorderPane();
        root.setTop(controlPanel);
        root.setCenter(imagePanel);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 800, 450);
        primaryStage.setTitle("压力表盘图像信息处理系统");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void uploadImage(Stage stage) {
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
            lastZoomFactorOriginal = 1.0;
            originalImageView.setFitWidth(200); // 重置为初始宽度
            originalImageView.setFitHeight(200); // 根据需要设置初始高度
            currentImageFile = file;
            Image image = new Image("file:" + file.getAbsolutePath());
            originalImageView.setImage(image);
            processedImageView.setImage(null);
            status.setStatus("已上传");
            statusLabel.setText(status.getStatus());
        }
        else {
            showAlert("错误", "未选择任何文件");
        }
    }

    private void processImage(String processType) {
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
    private void zoomImage(boolean zoomIn, boolean isOriginal) {
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



    public static void main(String[] args) {
        launch(args);
    }
}

