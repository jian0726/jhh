package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.dto.PingJiaRequest;
import com.delta.rental.entity.PingJia;
import com.delta.rental.service.PingJiaService;
import com.delta.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "商品评价", description = "评价发布、商品评价列表与平均评分")
@RestController
@RequestMapping("/api/pingjia")
@RequiredArgsConstructor
public class PingJiaController {

    private final PingJiaService pingJiaService;

    @Operation(summary = "发布商品评价")
    @PostMapping
    public ApiResponse<PingJia> faBuPingJia(
            @RequestBody PingJiaRequest request) {
        Long pingJiaRenId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(pingJiaService.createPingJia(pingJiaRenId, request));
    }

    @Operation(summary = "获取商品评价列表")
    @GetMapping("/shangpin/{shangPinId}")
    public ApiResponse<List<PingJia>> getShangPinPingJia(@PathVariable Long shangPinId) {
        return ApiResponse.success(pingJiaService.getShangPinPingJia(shangPinId));
    }

    @Operation(summary = "获取我发表的评价")
    @GetMapping("/yonghu")
    public ApiResponse<List<PingJia>> getYongHuPingJia() {
        Long yongHuId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(pingJiaService.getYongHuPingJia(yongHuId));
    }

    @Operation(summary = "获取商品平均评分")
    @GetMapping("/shangpin/{shangPinId}/pingfen")
    public ApiResponse<Double> getPingJunPingFen(@PathVariable Long shangPinId) {
        return ApiResponse.success(pingJiaService.getAveragePingFen(shangPinId));
    }

}
