package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.service.LiuLanLiShiService;
import com.delta.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "浏览历史", description = "浏览足迹查询与清空")
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class LiuLanLiShiController {

    private final LiuLanLiShiService liuLanLiShiService;

    @Operation(summary = "获取我的浏览历史")
    @GetMapping("/browse")
    public ApiResponse<Page<ShangPin>> getBrowseHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<ShangPin> history = liuLanLiShiService.getLiuLanList(userId, pageable);
        return ApiResponse.success(history);
    }

    @Operation(summary = "清空浏览历史")
    @DeleteMapping("/browse")
    public ApiResponse<String> clearBrowseHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        liuLanLiShiService.clearLiuLan(userId);
        return ApiResponse.success("清空浏览历史成功");
    }

}