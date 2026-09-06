package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.service.LiuLanLiShiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class LiuLanLiShiController {

    private final LiuLanLiShiService liuLanLiShiService;

    @GetMapping("/browse")
    public ApiResponse<Page<ShangPin>> getBrowseHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = tiQuYongHuId(token);
        Pageable pageable = PageRequest.of(page, size);
        Page<ShangPin> history = liuLanLiShiService.getLiuLanList(userId, pageable);
        return ApiResponse.success(history);
    }

    @DeleteMapping("/browse")
    public ApiResponse<String> clearBrowseHistory(@RequestHeader("Authorization") String token) {
        Long userId = tiQuYongHuId(token);
        liuLanLiShiService.clearLiuLan(userId);
        return ApiResponse.success("清空浏览历史成功");
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