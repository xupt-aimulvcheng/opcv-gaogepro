package com.xupt.opengaugepro.util;

import okhttp3.*;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

public class ImageUtils {
    public static final String API_KEY = "40dWAdlcJoCMjzXbtfgytRr4";
    public static final String SECRET_KEY = "3TJOnYdFqpaM8JG5iYzLKnIaTXxIG16Z";
    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    /**
     * 图片修复
     * @param mat 待修复的图片
     * @return 修复后的图片
     * @throws IOException io异常
     */
    public static Mat InpainteMat(Mat mat) throws IOException {
        String base64Image = matToBase64(mat);
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"rectangle\":[],\"image\":\"" + base64Image + "\"}");
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/image-process/v1/inpainting?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String imageJSON = new JSONObject(response.body().string()).getString("image");
            base64ToFile(imageJSON, "inpainting_");
            return base64ToMat(imageJSON);
        }
    }

    /**
     * 图片清晰度增强
     * @param mat 待增强的图片
     * @return 增强后的图片
     * @throws IOException io异常
     */
    public static Mat enhanceMat(Mat mat) throws IOException {
        String base64Image = matToBase64(mat);
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "image="+base64Image);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/image-process/v1/image_definition_enhance?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String imageJSON = new JSONObject(response.body().string()).getString("image");
        base64ToFile(imageJSON, "enhance_");
        return base64ToMat(imageJSON);
    }

    /**
     * 拉伸图像恢复
     * @param mat 待修复的图片
     * @return 修复后的图片
     * @throws IOException io异常
     */
    public static Mat StretchRestoreMat(Mat mat) throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String base64 = matToBase64(mat);
        // image 可以通过 getFileContentAsBase64("C:\fakepath\OIP.jpg") 方法获取,如果Content-Type是application/x-www-form-urlencoded时,第二个参数传true
        RequestBody body = RequestBody.create(mediaType, "image="+ base64);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/image-process/v1/stretch_restore?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String resStr = response.body().string();
        String imageJSON = new JSONObject(resStr).getString("image");
        base64ToFile(imageJSON, "stretch_restore_");
        return base64ToMat(imageJSON);
    }

    /**
     * 图片对比度增强
     * @param mat 待增强的图片
     * @return 增强后的图片
     * @throws IOException io异常
     */
    public static Mat ContrastEnhanceMat(Mat mat) throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        // image 可以通过 getFileContentAsBase64("C:\fakepath\R.jpg") 方法获取,如果Content-Type是application/x-www-form-urlencoded时,第二个参数传true
        RequestBody body = RequestBody.create(mediaType, "image="+matToBase64(mat));
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/image-process/v1/contrast_enhance?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String resStr = response.body().string();
        String imageJSON = new JSONObject(resStr).getString("image");
        base64ToFile(imageJSON, "contrast_enhance_");
        return base64ToMat(imageJSON);
    }

    /**
     * 图像色彩增强
     * @param mat 待增强的图片
     * @return 增强后的图片
     * @throws IOException io异常
     */
    public static Mat color_enhanceMat(Mat mat) throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        // image 可以通过 getFileContentAsBase64("C:\fakepath\R.jpg") 方法获取,如果Content-Type是application/x-www-form-urlencoded时,第二个参数传true
        RequestBody body = RequestBody.create(mediaType, "image="+matToBase64(mat));
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/image-process/v1/color_enhance?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String resSr = response.body().string();
        String imageJSON = new JSONObject(resSr).getString("image");
        base64ToFile(imageJSON, "color_enhance_");
        return base64ToMat(imageJSON);
    }

    /**
     * 图像去噪
     * @param mat 待去噪的图片
     * @return 去噪后的图片
     * @throws IOException io异常
     */
    public static Mat DonoiseMat(Mat mat) throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        // image 可以通过 getFileContentAsBase64("C:\fakepath\R (1).jpg") 方法获取,如果Content-Type是application/x-www-form-urlencoded时,第二个参数传true
        RequestBody body = RequestBody.create(mediaType, "image="+matToBase64(mat));
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/image-process/v1/denoise?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String string = response.body().string();
        String imageJSON = new JSONObject(string).getString("image");
        base64ToFile(imageJSON, "denoise_");
        return base64ToMat(imageJSON);
    }

    public static Mat Quality(Mat mat) throws IOException{
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        // image 可以通过 getFileContentAsBase64("C:\fakepath\src0.jpg") 方法获取,如果Content-Type是application/x-www-form-urlencoded时,第二个参数传true
        RequestBody body = RequestBody.create(mediaType, "image=" + matToBase64(mat));
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/image-process/v1/image_quality_enhance?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String string = response.body().string();
        String imageJSON = new JSONObject(string).getString("image");
        base64ToFile(imageJSON, "quality");
        return base64ToMat(imageJSON);

    }

    static String matToBase64(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        // 这里使用PNG格式编码图像，你也可以选择其他格式，例如JPEG
        if (Imgcodecs.imencode(".png", mat, buffer)) {
            byte[] byteArray = buffer.toArray();
            String base64String = Base64.getEncoder().encodeToString(byteArray);
            try {
                // 对Base64字符串进行URL编码
                return URLEncoder.encode(base64String, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.err.println("Error encoding base64 string: " + e.getMessage());
                return "";  // 在出现编码错误时返回空字符串
            }
        }
        return "";
    }

    static String getFileContentAsBase64(String path, boolean urlEncode) throws IOException {
        byte[] b = Files.readAllBytes(Paths.get(path));
        String base64 = Base64.getEncoder().encodeToString(b);
        if (urlEncode) {
            base64 = URLEncoder.encode(base64, "UTF-8");
        }
        return base64;
    }

    static String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String string = response.body().string();
            return new JSONObject(string).getString("access_token");
        }
    }
    /**
     * 将Base64编码的图像字符串转换为Mat对象
     *
     * @param base64Str 图像的Base64编码字符串
     * @return 转换后的Mat对象
     */
    public static Mat base64ToMat(String base64Str) {
        try {
            // 解码Base64字符串为字节数组
            byte[] imageData = Base64.getDecoder().decode(base64Str);
            MatOfByte matOfByte = new MatOfByte(imageData);
            // 使用imdecode方法将字节数组转换为Mat对象
            return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);
        } catch (IllegalArgumentException e) {
            // 处理Base64解码错误
            System.err.println("base64转Mat出错: " + e.getMessage());
            return new Mat();  // 返回一个空的Mat对象
        }
    }

    /**
     * 将Mat对象转换为文件
     * @param base64String Base64编码的图像字符串
     */
    public static void base64ToFile(String base64String, String type) {
        try {
            // 清洗Base64字符串，移除所有非Base64字符
            base64String = base64String.replaceAll("[^A-Za-z0-9+/=]", "");
            String randomFileName = type + UUID.randomUUID().toString() + ".png";
            String outputPath = "C:\\Users\\HONOR\\Downloads\\" + randomFileName;
            // 添加Base64填充
            switch (base64String.length() % 4) {
                case 2: base64String += "=="; break;
                case 3: base64String += "="; break;
            }

            // 解码Base64字符串到字节数组
            byte[] imageBytes = Base64.getDecoder().decode(base64String);

            // 使用ByteArrayInputStream尝试读取图像
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                throw new IllegalArgumentException("无法将解码的字节数组转换为图像，请检查Base64字符串是否正确或完整。");
            }
            File file = new File(outputPath);
            // 如果文件不存在
            if (!file.exists()) {
                // 创建文件
                if (!file.createNewFile()) {
                    throw new IOException("无法创建文件：" + outputPath);
                }
            }
            // 将图像保存到文件
            ImageIO.write(image, "png", file);
            System.out.println("图片已成功保存到：" + outputPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        String randomFileName = "random_image_" + UUID.randomUUID().toString() + ".png";
        String resourceDir = "C:\\Users\\HONOR\\Downloads\\" + randomFileName;
        String filePath = "C:\\Users\\HONOR\\Downloads\\txt.txt"; // 文本文件路径
        String base64String = new String(Files.readAllBytes(Paths.get(filePath)));
        // base64ToFile(base64String);
    }
}
