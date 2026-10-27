package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.dto.ShangPinRequest;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.service.FileStorageService;
import com.delta.rental.service.ShangPinService;
import com.delta.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "商品", description = "商品上架/搜索/筛选/审核与图片上传")
@RestController
@RequestMapping("/api/shangpin")
@RequiredArgsConstructor
public class ShangPinController {

    private final ShangPinService shangPinService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "上架商品")
    @PostMapping
    public ApiResponse<ShangPin> faBuShangPin(
            @RequestBody ShangPinRequest request) {
        Long maiJiaId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(shangPinService.createShangPin(maiJiaId, request));
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getShangPin(@PathVariable Long id) {
        ShangPin sp = shangPinService.getShangPinById(id);
        return ApiResponse.success(shangPinService.toFrontendFormat(sp));
    }

    @Operation(summary = "分页获取在售商品")
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
    
    @Operation(summary = "获取商家全部在售商品")
    @GetMapping("/shangjia")
    public ApiResponse<List<ShangPin>> getShangJiaShangPin() {
        return ApiResponse.success(shangPinService.getAllOnShelfShangPin());
    }
    
    @Operation(summary = "关键词搜索商品")
    @GetMapping("/sousuo")
    public ApiResponse<List<ShangPin>> sousuoShangPin(@RequestParam String keyword) {
        return ApiResponse.success(shangPinService.searchShangPin(keyword));
    }
    
    @Operation(summary = "分页搜索商品")
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
    
    @Operation(summary = "布尔模式全文搜索")
    @GetMapping("/search/boolean")
    public ApiResponse<List<ShangPin>> searchShangPinBoolean(@RequestParam String keyword) {
        return ApiResponse.success(shangPinService.searchShangPinBoolean(keyword));
    }

    @Operation(summary = "按价格 / 最短租期筛选商品")
    @GetMapping("/filter")
    public ApiResponse<List<ShangPin>> filterShangPin(
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer zuiShaoZuJieTianShu) {
        return ApiResponse.success(shangPinService.filterShangPin(minPrice, maxPrice, zuiShaoZuJieTianShu));
    }

    /**
     * 前端筛选结果页使用的富筛选接口。
     * 请求体：{ "filter": { accountType:[], accountLevel:"", ... }, "page": 1, "limit": 16 }
     * 响应：{ data: [商品列表], total: 总数 }
     */
    @Operation(summary = "高级筛选（多条件 + 分页）")
    @PostMapping("/filter")
    public ApiResponse<Map<String, Object>> filterShangPinAdvanced(
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> filter = null;
        int page = 1;
        int limit = 10;
        if (body != null) {
            Object rawFilter = body.get("filter");
            if (rawFilter instanceof Map<?, ?> raw) {
                Map<String, Object> parsed = new HashMap<>();
                raw.forEach((k, v) -> parsed.put(String.valueOf(k), v));
                filter = parsed;
            }
            Integer p = toInt(body.get("page"));
            if (p != null && p > 0) page = p;
            Integer l = toInt(body.get("limit"));
            if (l != null && l > 0) limit = l;
        }

        List<ShangPin> all = shangPinService.filterShangPinAdvanced(filter);
        int total = all.size();

        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);
        List<ShangPin> pageItems = (start >= 0 && start < total) ? all.subList(start, end) : List.of();

        List<Map<String, Object>> formattedList = pageItems.stream()
                .map(shangPinService::toFrontendFormat)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("data", formattedList);
        result.put("total", total);
        return ApiResponse.success(result);
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Operation(summary = "获取我上架的商品")
    @GetMapping("/maijia")
    public ApiResponse<List<ShangPin>> getMaiJiaShangPin() {
        Long maiJiaId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(shangPinService.getMaiJiaShangPin(maiJiaId));
    }

    /**
     * 当前登录用户上架的商品（个人中心「我的商品」使用）。
     *
     * <p>与 {@code /maijia} 数据来源一致，但返回前端格式（展开 metadata：
     * coverImageUrl / rank / 区服等），前端可直接渲染卡片。
     */
    @Operation(summary = "获取我的商品（前端格式）")
    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> getMyShangPin() {
        Long maiJiaId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> items = shangPinService.getMaiJiaShangPin(maiJiaId).stream()
                .map(shangPinService::toFrontendFormat)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("data", items);
        result.put("total", items.size());
        return ApiResponse.success(result);
    }

    @Operation(summary = "更新商品信息")
    @PutMapping("/{id}")
    public ApiResponse<ShangPin> gengXinShangPin(
            @PathVariable Long id,
            @RequestBody ShangPinRequest request) {
        Long maiJiaId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(shangPinService.updateShangPin(id, maiJiaId, request));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> shanChuShangPin(
            @PathVariable Long id) {
        Long maiJiaId = SecurityUtils.getCurrentUserId();
        shangPinService.deleteShangPin(id, maiJiaId);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "下架商品")
    @PutMapping("/{id}/xiajia")
    public ApiResponse<ShangPin> xiaJia(
            @PathVariable Long id) {
        Long maiJiaId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(shangPinService.xiaJia(id, maiJiaId));
    }

    @Operation(summary = "审核通过商品")
    @PutMapping("/{id}/tongguo")
    public ApiResponse<ShangPin> tongGuoShenHe(@PathVariable Long id) {
        return ApiResponse.success(shangPinService.tongGuoShenHe(id));
    }

    @Operation(summary = "审核拒绝商品")
    @PutMapping("/{id}/jujue")
    public ApiResponse<ShangPin> juJueShenHe(@PathVariable Long id) {
        return ApiResponse.success(shangPinService.juJueShenHe(id));
    }

    /**
     * 商品封面图上传（保留原路径，避免已对接的前端失效）。
     *
     * <p>落盘与 URL 生成逻辑已统一收敛到 {@link FileStorageService}，
     * 新页面建议直接使用通用接口 {@code POST /api/upload/image}。
     */
    @Operation(summary = "上传商品封面图")
    @PostMapping("/upload-image")
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        MultipartFile target = image != null ? image : file;
        return ApiResponse.success(fileStorageService.storeImageAsMap(target));
    }

}
