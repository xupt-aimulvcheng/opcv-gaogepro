package com.xupt.opengaugepro.factory;

import com.xupt.opengaugepro.DialImageProcessingApp;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ButtonFactory {
    private ButtonBinding buttonBinding;
    private Stage stage;
    private static Logger logger = LoggerFactory.getLogger(ButtonFactory.class);
    private DialImageProcessingApp app;  // 添加对主应用的引用

    public ButtonFactory(ButtonBinding buttonBinding, Stage stage, DialImageProcessingApp app) {
        this.buttonBinding = buttonBinding;
        this.stage = stage;
        this.app = app;
    }

    /**
     * 创建分组的按钮菜单
     * @param primaryStage 主舞台
     * @return 控件数组
     */
    public Node[] createMenuButtons(Stage primaryStage) {
        // 文件上传按钮
        Button uploadButton = createButton("上传图像", event -> buttonBinding.uploadImage(primaryStage));

        // 预处理按钮
        MenuButton preprocessMenu = new MenuButton("预处理");
        preprocessMenu.getItems().addAll(
                createMenuItem("去噪", event -> buttonBinding.processImage("denoise", stage)),
                createMenuItem("调整亮度/对比度", event -> buttonBinding.processImage("adjustBrightness", stage)),
                createMenuItem("转换为灰度图", event -> buttonBinding.processImage("toGrayScale", stage)),
                createMenuItem("区域分割", event -> buttonBinding.processImage("segment", stage))
        );

        // 增强处理按钮
        MenuButton enhancementMenu = new MenuButton("增强处理");
        enhancementMenu.getItems().addAll(
                createMenuItem("提取表盘图像", event -> buttonBinding.processImage("extractDial", stage)),
                createMenuItem("边缘强化",event -> buttonBinding.processImage("enhanceEdges",stage)),
                createMenuItem("数字增强", event -> buttonBinding.processImage("digitEnhancement", stage))
        );

        // 放缩图片按钮
        MenuButton zoomMenu = new MenuButton("放缩图片");
        zoomMenu.getItems().addAll(
                createMenuItem("放大原图", event -> buttonBinding.zoomImage(true, true)),
                createMenuItem("缩小原图", event -> buttonBinding.zoomImage(false, true)),
                createMenuItem("放大处理图", event -> buttonBinding.zoomImage(true, false)),
                createMenuItem("缩小处理图", event -> buttonBinding.zoomImage(false, false))
        );

        // 应用通用样式
        applyCommonStyle(new Control[]{uploadButton, preprocessMenu, enhancementMenu, zoomMenu});


        // 返回包含所有按钮和菜单的数组
        return new Node[]{uploadButton, preprocessMenu, enhancementMenu, zoomMenu};
    }

    /**
     * 创建一个按钮
     * @param text 按钮文本
     * @param action 按钮点击事件
     * @return 按钮
     */
    private Button createButton(String text, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setOnAction(getActionEventEventHandler(text, action));
        return button;
    }

    private EventHandler<ActionEvent> getActionEventEventHandler(String text, EventHandler<ActionEvent> action) {
        return event -> {
            try {
                app.createParameterInputs(app.root,text);
                logger.info(text + "按钮被点击");
                action.handle(event);
            } catch (Exception e) {
                logger.error("图片处理出错" + e.getMessage());
                buttonBinding.showAlert("错误", "图片处理出错");
            }
        };
    }

    /**
     * 创建一个菜单项
     * @param text 菜单项文本
     * @param action 菜单项点击事件
     * @return 菜单项
     */
    private MenuItem createMenuItem(String text, EventHandler<ActionEvent> action) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.setOnAction(getActionEventEventHandler(text, action));
        return menuItem;
    }

    /**
     * 应用通用样式到一组控件
     * @param controls 控件数组
     */
    private void applyCommonStyle(Control[] controls) {
        for (Control control : controls) {
            // 设置最小宽度
            control.setMinWidth(120);
            // 设置控件的背景色、文字颜色、字体大小和内边距
            control.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");
            // 添加鼠标悬停效果
            control.setOnMouseEntered(e -> control.setStyle("-fx-background-color: #357ABD; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;"));
            control.setOnMouseExited(e -> control.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;"));
        }
    }
}
