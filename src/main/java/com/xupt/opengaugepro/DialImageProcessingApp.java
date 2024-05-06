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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Size;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private TextField alphaField;
    private TextField betaField;
    private VBox parametersBox;
    private Button updateParametersButton;
    private Map<String, Control> controlMap;
    public BorderPane root;
    private List<String> operationConditions;
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
        buttonBinding = new ButtonBinding(originalImageView, processedImageView, statusLabel,this);

        // 假设ButtonFactory已经修改为接受ButtonBinding实例
        ButtonFactory buttonFactory = new ButtonFactory(buttonBinding, primaryStage,this);
        Node[] buttons = buttonFactory.createMenuButtons(primaryStage);

        VBox buttonPanel = new VBox(10, buttons);
        setupButtonPanel(buttonPanel);  // 调用设置按钮面板的方法

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

        // 设置根面板
        root = new BorderPane();
        root.setLeft(buttonPanel);
        root.setCenter(imagePanel);
        // 创建参数输入框并添加到界面
        initializeComboBox();
        createParameterInputs(root,""); // 调整参数输入区，添加至界面右侧
        root.setBottom(new VBox(statusLabel));
        // 设置舞台
        primaryStage.setMaximized(true);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        // hiddenInput();
        primaryStage.setTitle("压力表盘图像信息处理系统");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeControlMap(String str) {
        controlMap = new HashMap<>();
        // 使用参数名称作为键，控件对象作为值填充映射
        if (str.startsWith("放大")) controlMap.put("放大倍数", magnificationField);
        if (str.startsWith("缩小")) controlMap.put("缩小倍数", minificationField);
        if (str.equals("去噪")) controlMap.put("模糊类型", denoiseComboBox);
        if ("调整亮度/对比度".equals(str)) {
            controlMap.put("亮度调整系数", alphaField);
            controlMap.put("对比度调整系数", betaField);
        }
        if ("数字增强".equals(str)) {
            controlMap.put("CLAHE 对比度限制", claheClipLimitField);
            controlMap.put("CLAHE 网格尺寸", claheTileGridSizeField);
            controlMap.put("锐化强度", sharpenStrengthField);
            controlMap.put("二值化类型", thresholdTypeComboBox);
            controlMap.put("形态学尺寸", morphologySizeField);
        }
    }

    // 创建参数输入框和按钮
    public void createParameterInputs(BorderPane root, String str) {
        // 创建参数输入框并设置默认值
        claheClipLimitField = new TextField(String.valueOf(claheClipLimit));
        claheTileGridSizeField = new TextField(String.valueOf(claheTileGridSize.width));
        sharpenStrengthField = new TextField(String.valueOf(sharpenStrength));
        morphologySizeField = new TextField(String.valueOf(morphologySize));
        magnificationField = new TextField(String.valueOf(Magnification));
        minificationField = new TextField(String.valueOf(Minification));
        alphaField = new TextField(String.valueOf(alphaValue));
        betaField = new TextField(String.valueOf(betaValue));
        // 初始化 parametersBox
        parametersBox = new VBox();
        operationConditions = new ArrayList<>();
        operationConditions.add("放大倍数");
        operationConditions.add("缩小倍数");
        operationConditions.add("去噪");
        operationConditions.add("调整亮度/对比度");
        operationConditions.add("数字增强");
        controlMap = new HashMap<>();
        initializeControlMap(str);

        parametersBox.setPadding(new Insets(10));
        parametersBox.setSpacing(10);
        parametersBox.setAlignment(Pos.TOP_CENTER);

        // 创建参数输入布局容器
        // 遍历映射并为每个条目创建一个输入控件和标签
        for (Map.Entry<String, Control> entry : controlMap.entrySet()) {
            String key = entry.getKey();
            Control control = entry.getValue();

            // 为每个控件创建一个标签
            Label label = new Label(key);

            // 创建一个容器来包含标签和控件
            VBox container = new VBox(); // 你可以选择使用 HBox 如果你想让它们在同一行
            container.getChildren().add(label);
            container.getChildren().add(control);

            // 根据控件类型进行特定的初始化（如果需要的话）
            if (control instanceof TextField) {
                // 对 TextField 进行特定配置
            } else if (control instanceof ComboBox) {
                // 对 ComboBox 进行特定配置
            }

            // 将容器添加到 VBox 中
            parametersBox.getChildren().add(container);
        }
        // 创建更新参数的按钮
        if (operationConditions.contains(str)) {
            updateParametersButton = new Button("更新参数");
            updateParametersButton.setOnAction(event -> updateParameters());
            // 添加更新参数的按钮到 parametersBox
            parametersBox.getChildren().add(updateParametersButton);
        }
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



    public void updateParameters() {
        double gridSize = 0;
        try {
            Magnification = Double.parseDouble(magnificationField.getText());
            Minification = Double.parseDouble(minificationField.getText());
            claheClipLimit = Double.parseDouble(claheClipLimitField.getText());
            gridSize = Double.parseDouble(claheTileGridSizeField.getText());
            claheTileGridSize = new Size(gridSize, gridSize);
            sharpenStrength = Float.parseFloat(sharpenStrengthField.getText());
            alphaValue = Double.parseDouble(alphaField.getText());
            betaValue = Double.parseDouble(betaField.getText());
        } catch (NumberFormatException e) {
            buttonBinding.showAlert("错误","请输入小数");
        }
        thresholdType = getThresholdType(thresholdTypeComboBox.getValue()); // 一个方法转换字符串为相应的阈值
        doniseType = getDoniseType(denoiseComboBox.getValue()); // 一个方法转换字符串为相应的去噪类型
        try {
            morphologySize = Double.parseDouble(morphologySizeField.getText());
        } catch (NumberFormatException e) {
            buttonBinding.showAlert("错误","形态学尺寸请输入一个数字");
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
    private void setupButtonPanel(VBox buttonPanel) {
        // 设置填充和间距
        buttonPanel.setPadding(new Insets(10));
        buttonPanel.setSpacing(10);
        // 设置对齐方式
        buttonPanel.setAlignment(Pos.CENTER_LEFT);
        // 使VBox填满其区域
        buttonPanel.setMaxWidth(Double.MAX_VALUE);
        buttonPanel.setFillWidth(true);

        // 设置按钮的基础和悬停样式
        String baseStyle = "-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10px;";
        String hoverStyle = "-fx-background-color: #357ABD; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10px;";

        for (Node node : buttonPanel.getChildren()) {
            if (node instanceof Control) {
                Control control = (Control) node;
                control.setStyle(baseStyle);
                // 确保按钮的宽度充足
                control.setMinWidth(150);
                control.setMaxWidth(Double.MAX_VALUE);  // 允许按钮宽度填满VBox的宽度

                // 设置鼠标悬停效果
                control.setOnMouseEntered(e -> control.setStyle(hoverStyle));
                control.setOnMouseExited(e -> control.setStyle(baseStyle));
            }
        }
    }





    public static void main(String[] args) {
        launch(args);
    }
}