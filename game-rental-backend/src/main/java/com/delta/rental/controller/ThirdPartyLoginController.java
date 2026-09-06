package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.enums.ThirdPartyLoginType;
import com.delta.rental.service.ThirdPartyLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class ThirdPartyLoginController {

    private final ThirdPartyLoginService thirdPartyLoginService;

    @GetMapping("/auth-url")
    public ApiResponse<Map<String, String>> getAuthUrl(
            @RequestParam String type,
            @RequestParam(required = false) String state) {
        
        ThirdPartyLoginType loginType = ThirdPartyLoginType.fromCode(type);
        String authUrl = thirdPartyLoginService.getAuthUrl(loginType, state);
        
        Map<String, String> result = Map.of("authUrl", authUrl);
        return ApiResponse.success(result);
    }

    @PostMapping("/callback")
    public ApiResponse<Map<String, Object>> callback(
            @RequestParam String type,
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        
        ThirdPartyLoginType loginType = ThirdPartyLoginType.fromCode(type);
        Map<String, Object> result = thirdPartyLoginService.login(loginType, code, state);
        
        return ApiResponse.success(result);
    }
}