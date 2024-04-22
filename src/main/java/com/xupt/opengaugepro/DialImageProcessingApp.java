package com.xupt.opengaugepro;

import com.xupt.opengaugepro.entity.Status;
import com.xupt.opengaugepro.factory.ButtonBinding;
import com.xupt.opengaugepro.factory.ButtonFactory;
import com.xupt.opengaugepro.imageprocessing.ImageProcessingService;
import com.xupt.opengaugepro.util.ImageConverter;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Size;

import java.io.File;

import static com.xupt.opengaugepro.entity.Params.*;
import static org.opencv.imgproc.Imgproc.*;

public class DialImageProcessingApp extends Application {
    public static Status status;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        status = new Status();
    }
    public static ScrollPane originalImageScrollPane; // 用于显示原始图像的ScrollPane
    public static ScrollPane processedImageScrollPane; // 用于显示处理后的图像的ScrollPane
    private ButtonBinding buttonBinding; // 用于处理按钮点击事件的绑定类
    public static File currentImageFile; // 当前处理的图像文件
    public static ImageView originalImageView; // 用于显示原始图像的ImageView
    public static ImageView processedImageView; // 用于显示处理后的图像的ImageView
    public static ImageProcessingService imageProcessingService; // 图像处理服务
    public static ImageConverter imageConverter; // 图像转换服务
    public static Label statusLabel; // 用于显示状态的标签
    private TextField magnificationField; // 用于放大倍数的输入框
    private TextField minificationField; // 用于缩小倍数的输入框
    private TextField claheClipLimitField; // 用于CLAHE对比度限制的输入框
    private TextField claheTileGridSizeField; // 用于CLAHE网格尺寸的输入框
    private TextField sharpenStrengthField; // 用于锐化强度的输入框
    private TextField morphologySizeField; // 用于形态学尺寸的输入框
    private ComboBox<String> thresholdTypeComboBox; // 用于选择阈值类型的下拉框
    private ComboBox<String> denoiseComboBox; // 用于选择去噪类型的下拉框

     @Override
     public void start(Stage primaryStage) {
         imageProcessingService = new ImageProcessingService();
         imageConverter = new ImageConverter();

         // 初始化ImageView并设置保持图片宽高比
         originalImageView = new ImageView();
         processedImageView = new ImageView();
         originalImageView.setPreserveRatio(true);
         processedImageView.setPreserveRatio(true);

         // 初始化状态标签
         statusLabel = new Label("状态: 未开始");

         // 创建ButtonBinding实例，传入必要的参数
         ButtonBinding buttonBinding = new ButtonBinding(originalImageView, processedImageView, statusLabel);

         // 假设ButtonFactory已经修改为接受ButtonBinding实例
         ButtonFactory buttonFactory = new ButtonFactory(buttonBinding, primaryStage);
         Button[] buttons = buttonFactory.createButton(primaryStage);

         // 设置控制面板
         FlowPane controlPanel = new FlowPane(10, 10, buttons);
         controlPanel.setPadding(new Insets(10));
         controlPanel.setAlignment(Pos.CENTER);

         // 设置状态栏
         VBox statusBar = new VBox(statusLabel);
         statusBar.setPadding(new Insets(10));

         // 创建包含ImageView的ScrollPane
         originalImageScrollPane = new ScrollPane(originalImageView);
         processedImageScrollPane = new ScrollPane(processedImageView);

         // 设置ScrollPane政策
         originalImageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
         originalImageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
         processedImageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
         processedImageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

         // 动态调整ImageView大小以适应ScrollPane
         bindImageViewSizeToScrollPane(originalImageView, originalImageScrollPane);
         bindImageViewSizeToScrollPane(processedImageView, processedImageScrollPane);

         // 设置图像面板
         GridPane imagePanel = setupImagePanel();

         // 构建根面板
         BorderPane root = new BorderPane();
         root.setTop(controlPanel);
         root.setCenter(imagePanel);
         root.setBottom(statusBar);

         // 创建参数输入框并添加到界面
         initializeComboBox();
         createParameterInputs(root);
         updateParameters();

         // 创建场景并设置舞台
         Scene scene = new Scene(root, 1500, 700);
         primaryStage.setTitle("压力表盘图像信息处理系统");
         primaryStage.setScene(scene);
         primaryStage.show();
     }
    // 创建参数输入框和按钮
    private void createParameterInputs(BorderPane root) {
        // 创建参数输入框并设置默认值
        claheClipLimitField = new TextField(String.valueOf(claheClipLimit));
        claheTileGridSizeField = new TextField(String.valueOf(claheTileGridSize.width));
        sharpenStrengthField = new TextField(String.valueOf(sharpenStrength));
        morphologySizeField = new TextField(String.valueOf(morphologySize));
        magnificationField = new TextField(String.valueOf(Magnification));
        minificationField = new TextField(String.valueOf(Minification));
        // 创建更新参数的按钮
        Button updateParametersButton = new Button("更新参数");
        updateParametersButton.setOnAction(event -> updateParameters());

        // 创建参数输入布局容器
        VBox parametersBox = new VBox(
                new Label("放大倍数:"),
                magnificationField,
                new Label("缩小倍数:"),
                minificationField,
                new Label("模糊类型"),
                denoiseComboBox,
                new Label("CLAHE 对比度限制:"),
                claheClipLimitField,
                // new Label("CLAHE 网格尺寸:"),
                // claheTileGridSizeField,
                // new Label("锐化强度:"),
                // sharpenStrengthField,
                new Label("二值化类型:"),
                thresholdTypeComboBox,
                // new Label("形态学尺寸:"),
                // morphologySizeField,
                updateParametersButton
        );

        parametersBox.setPadding(new Insets(10));
        parametersBox.setSpacing(10);

        // 将参数输入布局容器添加到界面的右侧
        root.setRight(parametersBox);
    }

    private void initializeComboBox() {
        thresholdTypeComboBox = new ComboBox<>();
        thresholdTypeComboBox.getItems().addAll(
                "THRESH_BINARY (二值化)",
                "THRESH_BINARY_INV (二值化反转)",
                "THRESH_TRUNC (截断)",
                "THRESH_TOZERO (超过阈值置零)",
                "THRESH_TOZERO_INV (低于阈值置零)",
                "THRESH_OTSU (OTSU算法)", // 单独的OTSU, 通常与THRESH_BINARY组合使用
                "THRESH_BINARY + THRESH_OTSU (二值化 + OTSU算法)",
                "THRESH_BINARY_INV + THRESH_OTSU (二值化反转 + OTSU算法)"
        );
        thresholdTypeComboBox.setValue("THRESH_BINARY (二值化)"); // 设置默认选项
        denoiseComboBox = new ComboBox<>();
        denoiseComboBox.getItems().addAll(
                "高斯去噪",
                "中值去噪",
                "均值去噪"

        );
        denoiseComboBox.setValue("高斯去噪");
    }



    private void updateParameters() {
        double gridSize = 0;
        try {
            Magnification = Double.parseDouble(magnificationField.getText());
            Minification = Double.parseDouble(minificationField.getText());
            claheClipLimit = Double.parseDouble(claheClipLimitField.getText());
            gridSize = Double.parseDouble(claheTileGridSizeField.getText());
            claheTileGridSize = new Size(gridSize, gridSize);
            sharpenStrength = Float.parseFloat(sharpenStrengthField.getText());
        } catch (NumberFormatException e) {
            buttonBinding.showAlert("错误","请输入小数");
        }
        thresholdType = getThresholdType(thresholdTypeComboBox.getValue()); // 添加一个方法转换字符串为相应的阈值
        doniseType = getDoniseType(denoiseComboBox.getValue()); // 添加一个方法转换字符串为相应的去噪类型
        try {
            morphologySize = Integer.parseInt(morphologySizeField.getText());
        } catch (NumberFormatException e) {
            buttonBinding.showAlert("错误","请输入一个整数");
        }

    }

    private String getDoniseType(String value) {
        switch (value)
            {
                case "高斯去噪":
                    return "gaussian";
                case "中值去噪":
                    return "median";
                case "双边滤波":
                    return "bilateral";
                default:
                    return "gaussian";
            }
    }
    // 选取二值化
    private int getThresholdType(String value) {
        return switch (value) {
            case "THRESH_BINARY (二值化)" -> THRESH_BINARY;
            case "THRESH_BINARY_INV (二值化反转)" -> THRESH_BINARY_INV;
            case "THRESH_TRUNC (截断)" -> THRESH_TRUNC;
            case "THRESH_TOZERO (超过阈值置零)" -> THRESH_TOZERO;
            case "THRESH_TOZERO_INV (低于阈值置零)" -> THRESH_TOZERO_INV;
            case "THRESH_OTSU (OTSU算法)" -> THRESH_OTSU;
            case "THRESH_BINARY + THRESH_OTSU (二值化 + OTSU算法)" -> THRESH_BINARY + THRESH_OTSU;
            case "THRESH_BINARY_INV + THRESH_OTSU (二值化反转 + OTSU算法)" -> THRESH_BINARY_INV + THRESH_OTSU;
            default -> THRESH_BINARY; // 默认返回
        };
    }



    // 设置图像面板
    private GridPane setupImagePanel() {
        GridPane imagePanel = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        imagePanel.getColumnConstraints().addAll(column1, column2);

        Label originalImageLabel = new Label("原图");
        Label processedImageLabel = new Label("处理图");

        imagePanel.add(originalImageScrollPane, 0, 0);
        imagePanel.add(processedImageScrollPane, 1, 0);
        imagePanel.add(originalImageLabel, 0, 1);
        imagePanel.add(processedImageLabel, 1, 1);
        GridPane.setHalignment(originalImageLabel, HPos.CENTER);
        GridPane.setHalignment(processedImageLabel, HPos.CENTER);

        return imagePanel;
    }
    // 设置图像滚动面板
    private void bindImageViewSizeToScrollPane(ImageView imageView, ScrollPane scrollPane) {
        imageView.fitWidthProperty().bind(scrollPane.widthProperty());
    }




    public static void main(String[] args) {
        launch(args);
    }
}

