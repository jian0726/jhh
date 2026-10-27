package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.ImHuiHua;
import com.delta.rental.entity.ImXiaoXi;
import com.delta.rental.service.ImService;
import com.delta.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "IM 即时通讯", description = "会话创建、会话列表、消息收发")
@RestController
@RequestMapping("/api/im")
@RequiredArgsConstructor
public class ImController {

    private final ImService imService;

    @Operation(summary = "创建（或复用）会话")
    @PostMapping("/session/create")
    public ApiResponse<ImHuiHua> createSession(
            @RequestParam Long targetUserId) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        ImHuiHua session = imService.createSession(userId, targetUserId);
        return ApiResponse.success(session);
    }

    @Operation(summary = "获取我的会话列表")
    @GetMapping("/sessions")
    public ApiResponse<Page<ImHuiHua>> getSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<ImHuiHua> sessions = imService.getSessions(userId, pageable);
        return ApiResponse.success(sessions);
    }

    @Operation(summary = "获取会话消息记录")
    @GetMapping("/messages/{sessionId}")
    public ApiResponse<Page<ImXiaoXi>> getMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<ImXiaoXi> messages = imService.getMessages(sessionId, userId, pageable);
        return ApiResponse.success(messages);
    }

    @Operation(summary = "发送消息")
    @PostMapping("/message/send")
    public ApiResponse<ImXiaoXi> sendMessage(
            @RequestBody Map<String, Object> request) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        String huiHuaHao = (String) request.get("sessionId");
        Long shouDaoRenId = ((Number) request.get("targetUserId")).longValue();
        String neiRong = (String) request.get("content");

        ImXiaoXi message = imService.sendMessage(huiHuaHao, userId, shouDaoRenId, neiRong);
        return ApiResponse.success(message);
    }

}