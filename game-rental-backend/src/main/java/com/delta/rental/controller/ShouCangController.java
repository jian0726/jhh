package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.service.ShouCangService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class ShouCangController {

    private final ShouCangService shouCangService;

    @PostMapping
    public ApiResponse<String> addFavorite(
            @RequestHeader("Authorization") String token,
            @RequestParam Long shangPinId) {
        
        Long userId = tiQuYongHuId(token);
        shouCangService.addShouCang(userId, shangPinId);
        return ApiResponse.success("收藏成功");
    }

    @GetMapping
    public ApiResponse<Page<ShangPin>> getFavorites(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = tiQuYongHuId(token);
        Pageable pageable = PageRequest.of(page, size);
        Page<ShangPin> favorites = shouCangService.getShouCangList(userId, pageable);
        return ApiResponse.success(favorites);
    }

    @DeleteMapping("/{shangPinId}")
    public ApiResponse<String> removeFavorite(
            @RequestHeader("Authorization") String token,
            @PathVariable Long shangPinId) {
        
        Long userId = tiQuYongHuId(token);
        shouCangService.removeShouCang(userId, shangPinId);
        return ApiResponse.success("取消收藏成功");
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