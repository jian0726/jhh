package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.service.AlipayCreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "第三方信用-芝麻信用", description = "芝麻信用分查询与授权")
@RestController
@RequestMapping("/api/alipay-credit")
@RequiredArgsConstructor
public class AlipayCreditController {

    private final AlipayCreditService alipayCreditService;

    @Operation(summary = "查询芝麻信用分")
    @GetMapping("/score")
    public ApiResponse<Map<String, Object>> getCreditScore(
            @RequestParam(required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            userId = "default_user";
        }
        
        Map<String, Object> result = alipayCreditService.queryCreditScore(userId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "发起芝麻信用授权")
    @PostMapping("/authorize")
    public ApiResponse<Map<String, Object>> authorize(
            @RequestParam String authCode) {
        
        String userId = "default_user";
        Map<String, Object> result = alipayCreditService.authorize(userId, authCode);
        return ApiResponse.success(result);
    }
}