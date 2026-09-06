package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.*;
import com.delta.rental.repository.*;
import com.delta.rental.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class GuanLiController {
    
    private final YongHuRepository yongHuRepository;
    private final ShangPinRepository shangPinRepository;
    private final DingDanRepository dingDanRepository;
    private final YongHuService yongHuService;
    private final ShangPinService shangPinService;
    private final GuanLiYuanService guanLiYuanService;
    
    @GetMapping("/tongji")
    public ApiResponse<Map<String, Object>> getTongJi() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("zongYongHuShu", yongHuRepository.count());
        stats.put("zongShangPinShu", shangPinRepository.count());
        stats.put("zongDingDanShu", dingDanRepository.count());
        stats.put("daiShenHeShangPinShu", shangPinRepository.findByZhuangTai(0).size());
        return ApiResponse.success(stats);
    }
    
    @GetMapping("/yonghu")
    public ApiResponse<List<YongHu>> getAllYongHu() {
        return ApiResponse.success(yongHuRepository.findAll());
    }
    
    @GetMapping("/shangpin/pending")
    public ApiResponse<List<ShangPin>> getPendingShangPin() {
        return ApiResponse.success(shangPinRepository.findByZhuangTai(0));
    }
    
    @PutMapping("/yonghu/{id}/fengjin")
    public ApiResponse<YongHu> fengJinYongHu(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long adminId = tiQuYongHuId(token);
        YongHu yongHu = yongHuService.fengJinYongHu(id);
        guanLiYuanService.logAction(adminId, "FENGJIN_YONGHU", "YongHu", id, "封禁用户");
        return ApiResponse.success(yongHu);
    }
    
    @PutMapping("/yonghu/{id}/jiefeng")
    public ApiResponse<YongHu> jieFengYongHu(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long adminId = tiQuYongHuId(token);
        YongHu yongHu = yongHuService.jieFengYongHu(id);
        guanLiYuanService.logAction(adminId, "JIEFENG_YONGHU", "YongHu", id, "解封用户");
        return ApiResponse.success(yongHu);
    }
    
    @PutMapping("/yonghu/{id}/jueses")
    public ApiResponse<YongHu> updateYongHuJueSe(
            @PathVariable Long id,
            @RequestParam Integer jueSe,
            @RequestHeader("Authorization") String token) {
        Long adminId = tiQuYongHuId(token);
        YongHu yongHu = yongHuService.updateYongHuJueSe(id, jueSe);
        guanLiYuanService.logAction(adminId, "UPDATE_JUESE", "YongHu", id, "修改用户角色为" + jueSe);
        return ApiResponse.success(yongHu);
    }
    
    private Long tiQuYongHuId(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String[] parts = token.split("\\.");
        String payload = parts[1];
        String decoded = new String(java.util.Base64.getUrlDecoder().decode(payload));
        return Long.parseLong(com.google.gson.JsonParser.parseString(decoded).getAsJsonObject().get("userId").getAsString());
    }
}
