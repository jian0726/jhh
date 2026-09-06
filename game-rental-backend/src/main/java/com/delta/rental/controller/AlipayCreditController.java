package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.service.AlipayCreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alipay-credit")
@RequiredArgsConstructor
public class AlipayCreditController {

    private final AlipayCreditService alipayCreditService;

    @GetMapping("/score")
    public ApiResponse<Map<String, Object>> getCreditScore(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            userId = "default_user";
        }
        
        Map<String, Object> result = alipayCreditService.queryCreditScore(userId);
        return ApiResponse.success(result);
    }

    @PostMapping("/authorize")
    public ApiResponse<Map<String, Object>> authorize(
            @RequestHeader("Authorization") String token,
            @RequestParam String authCode) {
        
        String userId = "default_user";
        Map<String, Object> result = alipayCreditService.authorize(userId, authCode);
        return ApiResponse.success(result);
    }
}