package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.ImHuiHua;
import com.delta.rental.entity.ImXiaoXi;
import com.delta.rental.service.ImService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/im")
@RequiredArgsConstructor
public class ImController {

    private final ImService imService;

    @PostMapping("/session/create")
    public ApiResponse<ImHuiHua> createSession(
            @RequestHeader("Authorization") String token,
            @RequestParam Long targetUserId) {
        
        Long userId = tiQuYongHuId(token);
        ImHuiHua session = imService.createSession(userId, targetUserId);
        return ApiResponse.success(session);
    }

    @GetMapping("/sessions")
    public ApiResponse<Page<ImHuiHua>> getSessions(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = tiQuYongHuId(token);
        Pageable pageable = PageRequest.of(page, size);
        Page<ImHuiHua> sessions = imService.getSessions(userId, pageable);
        return ApiResponse.success(sessions);
    }

    @GetMapping("/messages/{sessionId}")
    public ApiResponse<Page<ImXiaoXi>> getMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = tiQuYongHuId(token);
        Pageable pageable = PageRequest.of(page, size);
        Page<ImXiaoXi> messages = imService.getMessages(sessionId, userId, pageable);
        return ApiResponse.success(messages);
    }

    @PostMapping("/message/send")
    public ApiResponse<ImXiaoXi> sendMessage(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        
        Long userId = tiQuYongHuId(token);
        String huiHuaHao = (String) request.get("sessionId");
        Long shouDaoRenId = ((Number) request.get("targetUserId")).longValue();
        String neiRong = (String) request.get("content");

        ImXiaoXi message = imService.sendMessage(huiHuaHao, userId, shouDaoRenId, neiRong);
        return ApiResponse.success(message);
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