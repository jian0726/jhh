package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.*;
import com.delta.rental.repository.*;
import com.delta.rental.service.XiaoXiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final YongHuRepository yongHuRepository;
    private final ShangPinRepository shangPinRepository;
    private final DingDanRepository dingDanRepository;
    private final FengXianRepository fengXianRepository;
    private final PingJiaRepository pingJiaRepository;
    private final XiaoXiService xiaoXiService;

    @GetMapping("/users")
    public ApiResponse<List<YongHu>> getAllUsers() {
        return ApiResponse.success(yongHuRepository.findAll());
    }

    @PutMapping("/users/{id}/ban")
    public ApiResponse<YongHu> banUser(@PathVariable Long id, @RequestParam Integer status) {
        YongHu user = yongHuRepository.findById(id)
                .orElseThrow(() -> new com.delta.rental.exception.BusinessException(404, "用户不存在"));
        user.setZhuangTai(status);
        return ApiResponse.success(yongHuRepository.save(user));
    }

    @GetMapping("/products")
    public ApiResponse<List<ShangPin>> getPendingProducts() {
        return ApiResponse.success(shangPinRepository.findByZhuangTai(0));
    }

    @PutMapping("/products/{id}/audit")
    public ApiResponse<ShangPin> auditProduct(@PathVariable Long id, @RequestParam Integer result) {
        ShangPin product = shangPinRepository.findById(id)
                .orElseThrow(() -> new com.delta.rental.exception.BusinessException(404, "商品不存在"));
        
        if (result == 1) {
            product.setZhuangTai(1);
        } else {
            product.setZhuangTai(3);
        }
        
        return ApiResponse.success(shangPinRepository.save(product));
    }

    @GetMapping("/orders")
    public ApiResponse<List<DingDan>> getAllOrders() {
        return ApiResponse.success(dingDanRepository.findAll());
    }

    @GetMapping("/disputes")
    public ApiResponse<List<Map<String, Object>>> getAllDisputes() {
        List<DingDan> orders = dingDanRepository.findByZhuangTai(4);
        List<Map<String, Object>> disputes = orders.stream()
                .map(order -> {
                    Map<String, Object> dispute = new HashMap<>();
                    dispute.put("id", order.getId());
                    dispute.put("dingDanHao", order.getDingDanHao());
                    dispute.put("shangPinId", order.getShangPinId());
                    dispute.put("maiJiaId", order.getMaiJiaId());
                    dispute.put("maiJiaId2", order.getMaiJiaId2());
                    dispute.put("zongJinE", order.getZongJinE());
                    dispute.put("chuangJianShiJian", order.getChuangJianShiJian());
                    return dispute;
                })
                .collect(Collectors.toList());
        
        return ApiResponse.success(disputes);
    }

    @PutMapping("/disputes/{id}/judge")
    public ApiResponse<DingDan> judgeDispute(@PathVariable Long id) {
        DingDan order = dingDanRepository.findById(id)
                .orElseThrow(() -> new com.delta.rental.exception.BusinessException(404, "订单不存在"));
        
        order.setZhuangTai(2);
        
        ShangPin shangPin = shangPinRepository.findById(order.getShangPinId())
                .orElseThrow(() -> new com.delta.rental.exception.BusinessException(404, "商品不存在"));
        shangPin.setZhuangTai(1);
        shangPinRepository.save(shangPin);
        
        return ApiResponse.success(dingDanRepository.save(order));
    }

    @GetMapping("/risk/logs")
    public ApiResponse<List<FengXian>> getRiskLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String riskType) {
        List<FengXian> logs;
        if (userId != null) {
            logs = fengXianRepository.findByYongHuId(userId);
        } else if (riskType != null) {
            logs = fengXianRepository.findByFengXianLeiXing(riskType);
        } else {
            logs = fengXianRepository.findAll();
        }
        return ApiResponse.success(logs);
    }

    @GetMapping("/operate/logs")
    public ApiResponse<List<Map<String, Object>>> getOperateLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        
        Map<String, Object> log1 = new HashMap<>();
        log1.put("id", 1);
        log1.put("operator", "admin");
        log1.put("operation", "审核通过商品");
        log1.put("target", "商品ID: 1001");
        log1.put("time", LocalDateTime.now().minusHours(2));
        logs.add(log1);
        
        Map<String, Object> log2 = new HashMap<>();
        log2.put("id", 2);
        log2.put("operator", "admin");
        log2.put("operation", "封禁用户");
        log2.put("target", "用户ID: 2001");
        log2.put("time", LocalDateTime.now().minusHours(5));
        logs.add(log2);
        
        return ApiResponse.success(logs);
    }

    @PutMapping("/products/{id}/offline")
    public ApiResponse<ShangPin> forceOffline(@PathVariable Long id) {
        ShangPin product = shangPinRepository.findById(id)
                .orElseThrow(() -> new com.delta.rental.exception.BusinessException(404, "商品不存在"));
        product.setZhuangTai(3);
        return ApiResponse.success(shangPinRepository.save(product));
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        long totalUsers = yongHuRepository.count();
        long totalProducts = shangPinRepository.count();
        long totalOrders = dingDanRepository.count();
        long pendingProducts = shangPinRepository.findByZhuangTai(0).size();
        long pendingDisputes = dingDanRepository.findByZhuangTai(4).size();
        long todayOrders = dingDanRepository.findAll().stream()
                .filter(o -> o.getChuangJianShiJian() != null && 
                        o.getChuangJianShiJian().isAfter(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)))
                .count();
        
        dashboard.put("totalUsers", totalUsers);
        dashboard.put("totalProducts", totalProducts);
        dashboard.put("totalOrders", totalOrders);
        dashboard.put("pendingProducts", pendingProducts);
        dashboard.put("pendingDisputes", pendingDisputes);
        dashboard.put("todayOrders", todayOrders);
        
        List<Map<String, Object>> recentOrders = dingDanRepository.findAll().stream()
                .sorted((a, b) -> b.getChuangJianShiJian().compareTo(a.getChuangJianShiJian()))
                .limit(10)
                .map(order -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("dingDanHao", order.getDingDanHao());
                    item.put("zongJinE", order.getZongJinE());
                    item.put("zhuangTai", order.getZhuangTai());
                    item.put("chuangJianShiJian", order.getChuangJianShiJian());
                    return item;
                })
                .collect(Collectors.toList());
        dashboard.put("recentOrders", recentOrders);
        
        return ApiResponse.success(dashboard);
    }

    @PostMapping("/notice")
    public ApiResponse<XiaoXi> publishNotice(@RequestParam String title, @RequestParam String content) {
        List<YongHu> users = yongHuRepository.findAll();
        
        XiaoXi sample = null;
        for (YongHu user : users) {
            sample = xiaoXiService.createXiaoXi(user.getId(), title, content, 1);
        }
        
        return ApiResponse.success(sample);
    }
}
