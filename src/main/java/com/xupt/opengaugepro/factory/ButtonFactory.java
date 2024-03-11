package com.xupt.opengaugepro.factory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;

public class ButtonFactory {
    private ButtonBinding buttonBinding;
    private Stage stage;
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ButtonFactory.class);

    public ButtonFactory(ButtonBinding buttonBinding, Stage stage) {
        this.buttonBinding = buttonBinding;
        this.stage = stage;
    }

    public Button[] createButton(Stage primaryStage) {
        // 创建按钮数组
        Button[] buttons = {
                createButton("上传图像", event -> buttonBinding.uploadImage(primaryStage)),
                createButton("去噪", event -> buttonBinding.processImage("denoise",stage)),
                createButton("调整亮度/对比度", event -> buttonBinding.processImage("adjustBrightness",stage)),
                createButton("转换为灰度图", event -> buttonBinding.processImage("toGrayScale",stage)),
                createButton("区域分割",event -> buttonBinding.processImage("segment",stage)),
                createButton("提取表盘图像", event -> buttonBinding.processImage("extractDial",stage)),
                createButton("边缘强化", event -> buttonBinding.processImage("edgeEnhancement",stage)),
                createButton("刻度突出显示", event -> buttonBinding.processImage("scaleHighlight",stage)),
                createButton("数字增强", event -> buttonBinding.processImage("digitEnhancement",stage)),
                createButton("放大原图", event -> buttonBinding.zoomImage(true, true)),
                createButton("缩小原图", event -> buttonBinding.zoomImage(false, true)),
                createButton("放大处理图", event -> buttonBinding.zoomImage(true, false)),
                createButton("缩小处理图", event -> buttonBinding.zoomImage(false, false))
        };

        // 应用通用样式
        applyCommonStyle(buttons);
        return buttons;
    }

    private Button createButton(String text, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setOnAction(event -> {
            try {
                action.handle(event);
            } catch (Exception e) {
                logger.error("图片处理出错" + e.getMessage());
                // 这里可以添加错误处理逻辑，例如显示一个错误对话框
                buttonBinding.showAlert("错误","图片处理出错");
            }
        });
        return button;
    }

    private void applyCommonStyle(Button[] buttons) {
        for (Button button : buttons) {
            button.setMinWidth(120);
            // 在此处添加更多样式设置，例如按钮颜色、字体等
        }
    }
}
