package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.XiaoXi;
import com.delta.rental.service.XiaoXiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/xiaoxi")
@RequiredArgsConstructor
public class XiaoXiController {

    private final XiaoXiService xiaoXiService;

    @GetMapping
    public ApiResponse<List<XiaoXi>> getYongHuXiaoXi(@RequestHeader("Authorization") String token) {
        Long yongHuId = tiQuYongHuId(token);
        return ApiResponse.success(xiaoXiService.getYongHuXiaoXi(yongHuId));
    }

    @GetMapping("/weidu")
    public ApiResponse<List<XiaoXi>> getWeiDuXiaoXi(@RequestHeader("Authorization") String token) {
        Long yongHuId = tiQuYongHuId(token);
        return ApiResponse.success(xiaoXiService.getWeiDuXiaoXi(yongHuId));
    }

    @GetMapping("/weidu/count")
    public ApiResponse<java.util.Map<String, Object>> getWeiDuCount(@RequestHeader("Authorization") String token) {
        Long yongHuId = tiQuYongHuId(token);
        long count = xiaoXiService.getWeiDuCount(yongHuId);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("count", count);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}/dushu")
    public ApiResponse<XiaoXi> jiLuWeiDu(@PathVariable Long id) {
        return ApiResponse.success(xiaoXiService.markAsRead(id));
    }

    @PutMapping("/quanbudushu")
    public ApiResponse<Void> quanBuDuShu(@RequestHeader("Authorization") String token) {
        Long yongHuId = tiQuYongHuId(token);
        xiaoXiService.markAllAsRead(yongHuId);
        return ApiResponse.success("标记已读成功", null);
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
