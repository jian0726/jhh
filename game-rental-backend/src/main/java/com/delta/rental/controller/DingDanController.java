package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.dto.DingDanRequest;
import com.delta.rental.entity.DingDan;
import com.delta.rental.service.DingDanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dingdan")
@RequiredArgsConstructor
public class DingDanController {

    private final DingDanService dingDanService;

    @PostMapping
    public ApiResponse<DingDan> danJiaDingDan(
            @RequestHeader("Authorization") String token,
            @RequestBody DingDanRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
            @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint) {
        Long maiJiaId2 = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.createDingDan(maiJiaId2, request, ip, deviceFingerprint));
    }

    @GetMapping("/{dingDanHao}")
    public ApiResponse<DingDan> getDingDan(@PathVariable String dingDanHao) {
        return ApiResponse.success(dingDanService.getDingDanByHao(dingDanHao));
    }

    @PostMapping("/{dingDanHao}/fukuan")
    public ApiResponse<DingDan> fuKuanDingDan(
            @PathVariable String dingDanHao,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
            @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint) {
        return ApiResponse.success(dingDanService.fuKuanDingDan(dingDanHao, ip, deviceFingerprint));
    }

    @PostMapping("/{dingDanHao}/wancheng")
    public ApiResponse<DingDan> wanChengDingDan(@PathVariable String dingDanHao) {
        return ApiResponse.success(dingDanService.wanChengDingDan(dingDanHao));
    }

    @PostMapping("/{dingDanHao}/quxiao")
    public ApiResponse<DingDan> quXiaoDingDan(
            @PathVariable String dingDanHao,
            @RequestHeader("Authorization") String token) {
        Long yongHuId = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.quXiaoDingDan(dingDanHao, yongHuId));
    }

    @PostMapping("/{dingDanHao}/jiufeng")
    public ApiResponse<DingDan> jiuFengDingDan(
            @PathVariable String dingDanHao,
            @RequestHeader("Authorization") String token,
            @RequestParam String yuanYin) {
        Long yongHuId = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.jiufengDingDan(dingDanHao, yongHuId, yuanYin));
    }

    @PostMapping("/{dingDanHao}/chuli")
    public ApiResponse<DingDan> chuLiJiuFeng(@PathVariable String dingDanHao) {
        return ApiResponse.success(dingDanService.chuliJiufeng(dingDanHao));
    }

    @GetMapping("/maijia")
    public ApiResponse<List<DingDan>> getMaiJiaDingDan(@RequestHeader("Authorization") String token) {
        Long maiJiaId2 = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.getMaiJiaDingDan(maiJiaId2));
    }

    @GetMapping("/maimai/{maiJiaId}")
    public ApiResponse<List<DingDan>> getMaiJiaDingDanById(@PathVariable Long maiJiaId) {
        return ApiResponse.success(dingDanService.getMaiJiaDingDanById(maiJiaId));
    }

    @GetMapping("/shangpin/{shangPinId}")
    public ApiResponse<List<DingDan>> getShangPinDingDan(@PathVariable Long shangPinId) {
        return ApiResponse.success(dingDanService.getShangPinDingDan(shangPinId));
    }

    @GetMapping
    public ApiResponse<List<DingDan>> getMyOrders(@RequestHeader("Authorization") String token) {
        Long userId = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.getMyOrders(userId));
    }

    @GetMapping("/{dingDanHao}/credential")
    public ApiResponse<java.util.Map<String, Object>> getCredential(
            @PathVariable String dingDanHao,
            @RequestHeader("Authorization") String token) {
        Long userId = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.getCredential(dingDanHao, userId));
    }

    @PutMapping("/{dingDanHao}/return")
    public ApiResponse<DingDan> returnOrder(
            @PathVariable String dingDanHao,
            @RequestHeader("Authorization") String token) {
        Long userId = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.returnOrder(dingDanHao, userId));
    }

    @GetMapping("/{dingDanHao}/remaining")
    public ApiResponse<java.util.Map<String, Object>> getRemainingTime(@PathVariable String dingDanHao) {
        return ApiResponse.success(dingDanService.getRemainingTime(dingDanHao));
    }

    @PostMapping("/{dingDanHao}/inspect-start")
    public ApiResponse<DingDan> inspectStart(
            @PathVariable String dingDanHao,
            @RequestHeader("Authorization") String token) {
        Long userId = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.inspectStart(dingDanHao, userId));
    }

    @PutMapping("/{dingDanHao}/confirm")
    public ApiResponse<DingDan> confirmOrder(
            @PathVariable String dingDanHao,
            @RequestHeader("Authorization") String token) {
        Long userId = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.confirmOrder(dingDanHao, userId));
    }

    @PutMapping("/{dingDanHao}/reject")
    public ApiResponse<DingDan> rejectOrder(
            @PathVariable String dingDanHao,
            @RequestHeader("Authorization") String token,
            @RequestParam String reason) {
        Long userId = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.rejectOrder(dingDanHao, userId, reason));
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
