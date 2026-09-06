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

@RestController
@RequestMapping("/api/admin/im")
@RequiredArgsConstructor
public class ImAdminController {

    private final ImService imService;

    @GetMapping("/sessions")
    public ApiResponse<Page<ImHuiHua>> getAllSessions(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ImHuiHua> sessions = imService.getAllSessions(pageable);
        return ApiResponse.success(sessions);
    }

    @GetMapping("/messages/{sessionId}")
    public ApiResponse<Page<ImXiaoXi>> getSessionMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ImXiaoXi> messages = imService.getMessages(sessionId, 0L, pageable);
        return ApiResponse.success(messages);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<String> deleteSession(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId) {
        
        imService.deleteSession(sessionId);
        return ApiResponse.success("会话删除成功");
    }

    @GetMapping("/search")
    public ApiResponse<Page<ImXiaoXi>> searchMessages(
            @RequestHeader("Authorization") String token,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ImXiaoXi> messages = imService.searchMessages(keyword, pageable);
        return ApiResponse.success(messages);
    }

    @GetMapping("/statistics")
    public ApiResponse<java.util.Map<String, Object>> getStatistics(
            @RequestHeader("Authorization") String token) {
        
        java.util.Map<String, Object> stats = imService.getStatistics();
        return ApiResponse.success(stats);
    }
}