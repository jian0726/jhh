package com.delta.rental.service;

import com.delta.rental.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用文件存储服务。
 *
 * <p>统一约定：<b>图片文件存放在后端服务器磁盘上，数据库里只保存这个文件的访问 URL</b>。
 * 前端通过 &lt;img :src="url"&gt; 直接引用，前端工程内不存放任何业务图片。
 * 商品封面、轮播图、以及后续管理员页/IM 的图片上传都复用本服务，避免每个业务各写一份。
 *
 * <p>存储目录默认取运行目录下的 {@code uploads/}，可通过 {@code app.upload-dir} 配置覆盖
 * （见 application.yml）。该目录同时被 {@code WebConfig} 映射为 {@code /uploads/**} 静态资源。
 */
@Service
public class FileStorageService {

    /** 单张图片大小上限：10MB，与 spring.servlet.multipart.max-file-size 保持一致 */
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;

    /** 图片访问路径前缀，与 WebConfig 的静态资源映射保持一致 */
    public static final String URL_PREFIX = "/uploads/";

    /** 图片保存目录（统一使用正斜杠并以 / 结尾，Windows / Linux 通用） */
    private final String uploadDir;

    public FileStorageService(@Value("${app.upload-dir:}") String configuredDir) {
        String dir = (configuredDir == null || configuredDir.isBlank())
                ? System.getProperty("user.dir") + "/uploads"
                : configuredDir;
        this.uploadDir = normalize(dir);
    }

    /**
     * 上传目录（正斜杠、以 / 结尾），供 WebConfig 注册静态资源映射使用。
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * 保存图片并返回可直接访问的绝对 URL。
     *
     * @param file 前端 multipart 上传的图片
     * @return 形如 http://host:port/uploads/xxx.png 的绝对地址
     */
    public String storeImage(MultipartFile file) {
        validateImage(file);
        try {
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new BusinessException(500, "上传目录创建失败：" + uploadDir);
            }
            String filename = UUID.randomUUID() + resolveExtension(file.getOriginalFilename());
            file.transferTo(new File(uploadDir + filename));
            return buildAccessUrl(filename);
        } catch (IOException e) {
            throw new BusinessException(500, "图片上传失败：" + e.getMessage());
        }
    }

    /**
     * 保存图片并返回结果 Map，同时给出多个字段别名，
     * 兼容不同前端（商品用 coverImageUrl、轮播用 tuPianUrl、通用用 url）。
     */
    public Map<String, String> storeImageAsMap(MultipartFile file) {
        String url = storeImage(file);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("coverImageUrl", url);
        result.put("tuPianUrl", url);
        return result;
    }

    /**
     * 删除已上传的文件（传绝对 URL 或文件名均可）。
     * 删除失败不影响主流程，仅记录日志由调用方决定是否忽略。
     */
    public boolean deleteByUrl(String urlOrFilename) {
        if (urlOrFilename == null || urlOrFilename.isBlank()) {
            return false;
        }
        String filename = urlOrFilename;
        int idx = filename.lastIndexOf(URL_PREFIX);
        if (idx >= 0) {
            filename = filename.substring(idx + URL_PREFIX.length());
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            // 防止路径穿越，只允许纯文件名
            return false;
        }
        File target = new File(uploadDir + filename);
        return target.isFile() && target.delete();
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择图片文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "仅支持图片文件");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(400, "图片大小不能超过10MB");
        }
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 只保留常规图片后缀，避免带上恶意内容
            if (ext.matches("(?i)\\.(jpg|jpeg|png|gif|webp|bmp|svg)")) {
                return ext.toLowerCase();
            }
        }
        return ".jpg";
    }

    private String buildAccessUrl(String filename) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(URL_PREFIX)
                .path(filename)
                .toUriString();
    }

    private String normalize(String dir) {
        String normalized = dir.replace('\\', '/');
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }
}
