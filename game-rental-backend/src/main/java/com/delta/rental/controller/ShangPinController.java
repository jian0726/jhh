package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.dto.ShangPinRequest;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.service.ShangPinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shangpin")
@RequiredArgsConstructor
public class ShangPinController {

    private final ShangPinService shangPinService;

    @PostMapping
    public ApiResponse<ShangPin> faBuShangPin(
            @RequestHeader("Authorization") String token,
            @RequestBody ShangPinRequest request) {
        Long maiJiaId = tiQuYongHuId(token);
        return ApiResponse.success(shangPinService.createShangPin(maiJiaId, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getShangPin(@PathVariable Long id) {
        ShangPin sp = shangPinService.getShangPinById(id);
        return ApiResponse.success(shangPinService.toFrontendFormat(sp));
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getShangPinList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        List<ShangPin> allProducts = shangPinService.getAllOnShelfShangPin();
        int total = allProducts.size();

        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);
        List<ShangPin> pageItems = (start < total) ? allProducts.subList(start, end) : List.of();

        List<Map<String, Object>> formattedList = pageItems.stream()
                .map(shangPinService::toFrontendFormat)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("data", formattedList);
        result.put("total", total);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/shangjia")
    public ApiResponse<List<ShangPin>> getShangJiaShangPin() {
        return ApiResponse.success(shangPinService.getAllOnShelfShangPin());
    }
    
    @GetMapping("/sousuo")
    public ApiResponse<List<ShangPin>> sousuoShangPin(@RequestParam String keyword) {
        return ApiResponse.success(shangPinService.searchShangPin(keyword));
    }
    
    @GetMapping("/search")
    public ApiResponse<Map<String, Object>> searchShangPin(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        List<ShangPin> allProducts = shangPinService.searchShangPin(keyword);
        int total = allProducts.size();

        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);
        List<ShangPin> pageItems = (start < total) ? allProducts.subList(start, end) : List.of();

        List<Map<String, Object>> formattedList = pageItems.stream()
                .map(shangPinService::toFrontendFormat)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("data", formattedList);
        result.put("total", total);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/search/boolean")
    public ApiResponse<List<ShangPin>> searchShangPinBoolean(@RequestParam String keyword) {
        return ApiResponse.success(shangPinService.searchShangPinBoolean(keyword));
    }

    @GetMapping("/filter")
    public ApiResponse<List<ShangPin>> filterShangPin(
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer zuiShaoZuJieTianShu) {
        return ApiResponse.success(shangPinService.filterShangPin(minPrice, maxPrice, zuiShaoZuJieTianShu));
    }

    @GetMapping("/maijia")
    public ApiResponse<List<ShangPin>> getMaiJiaShangPin(@RequestHeader("Authorization") String token) {
        Long maiJiaId = tiQuYongHuId(token);
        return ApiResponse.success(shangPinService.getMaiJiaShangPin(maiJiaId));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShangPin> gengXinShangPin(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestBody ShangPinRequest request) {
        Long maiJiaId = tiQuYongHuId(token);
        return ApiResponse.success(shangPinService.updateShangPin(id, maiJiaId, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> shanChuShangPin(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long maiJiaId = tiQuYongHuId(token);
        shangPinService.deleteShangPin(id, maiJiaId);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/xiajia")
    public ApiResponse<ShangPin> xiaJia(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long maiJiaId = tiQuYongHuId(token);
        return ApiResponse.success(shangPinService.xiaJia(id, maiJiaId));
    }

    @PutMapping("/{id}/tongguo")
    public ApiResponse<ShangPin> tongGuoShenHe(@PathVariable Long id) {
        return ApiResponse.success(shangPinService.tongGuoShenHe(id));
    }

    @PutMapping("/{id}/jujue")
    public ApiResponse<ShangPin> juJueShenHe(@PathVariable Long id) {
        return ApiResponse.success(shangPinService.juJueShenHe(id));
    }

    @PostMapping("/upload-image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error(400, "请选择图片文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.error(400, "仅支持图片文件");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return ApiResponse.error(400, "图片大小不能超过10MB");
        }

        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String savedFilename = UUID.randomUUID().toString() + extension;

            File dest = new File(uploadDir + savedFilename);
            file.transferTo(dest);

            Map<String, String> result = new HashMap<>();
            result.put("coverImageUrl", "/uploads/" + savedFilename);
            return ApiResponse.success(result);
        } catch (IOException e) {
            return ApiResponse.error(500, "图片上传失败");
        }
    }

    private Long tiQuYongHuId(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String[] parts = token.split("\\.");
        String payload = parts[1];
        String decoded = new String(java.util.Base64.getUrlDecoder().decode(payload));
        return Long.parseLong(
                com.google.gson.JsonParser.parseString(decoded).getAsJsonObject().get("userId").getAsString());
    }
}
