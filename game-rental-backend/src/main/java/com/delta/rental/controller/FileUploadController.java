package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 通用文件上传接口。
 *
 * <p>前端统一调用本接口上传图片，拿到返回的绝对 URL 后，再把它作为普通字符串字段
 * 提交到对应的业务接口（发布商品、新增轮播图……）。这样后端只需维护一份上传逻辑，
 * 数据库里存的是 URL，图片文件由后端托管。
 *
 * <p>字段名同时兼容 {@code file} 与 {@code image}，方便不同页面复用。
 */
@Tag(name = "文件上传", description = "通用图片上传")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * 上传单张图片。
     *
     * @return { url, coverImageUrl, tuPianUrl }（三个字段值相同，方便前端按各自命名取值）
     */
    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        MultipartFile target = file != null ? file : image;
        return ApiResponse.success(fileStorageService.storeImageAsMap(target));
    }
}
