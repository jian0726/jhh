package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.service.ShangPinService;
import com.delta.rental.service.ShouCangService;
import com.delta.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 收藏接口。
 *
 * <p>返回的商品是「前端格式」：metadata 会被展开成 coverImageUrl / rank / 区服等字段，
 * 前端个人中心的收藏卡片可直接渲染，不需要再解析 JSON 字符串。
 */
@Tag(name = "收藏", description = "商品收藏的增删查")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class ShouCangController {

    private final ShouCangService shouCangService;
    private final ShangPinService shangPinService;

    @Operation(summary = "收藏商品")
    @PostMapping
    public ApiResponse<String> addFavorite(
            @RequestParam Long shangPinId) {

        Long userId = SecurityUtils.getCurrentUserId();
        shouCangService.addShouCang(userId, shangPinId);
        return ApiResponse.success("收藏成功");
    }

    @Operation(summary = "获取我的收藏")
    @GetMapping
    public ApiResponse<Map<String, Object>> getFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<ShangPin> favorites = shouCangService.getShouCangList(userId, pageable);

        List<Map<String, Object>> content = favorites.getContent().stream()
                .filter(Objects::nonNull)
                .map(shangPinService::toFrontendFormat)
                .collect(Collectors.toList());

        // 兼容前端两种取法：favRes.content / favRes.data
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("data", content);
        result.put("total", favorites.getTotalElements());
        result.put("totalElements", favorites.getTotalElements());
        result.put("page", favorites.getNumber());
        result.put("size", favorites.getSize());
        return ApiResponse.success(result);
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{shangPinId}")
    public ApiResponse<String> removeFavorite(
            @PathVariable Long shangPinId) {

        Long userId = SecurityUtils.getCurrentUserId();
        shouCangService.removeShouCang(userId, shangPinId);
        return ApiResponse.success("取消收藏成功");
    }

}
