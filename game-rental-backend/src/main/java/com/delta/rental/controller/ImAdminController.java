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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "管理后台-IM 会话", description = "会话查询、消息检索、会话删除与统计")
@RestController
@RequestMapping("/api/admin/im")
@RequiredArgsConstructor
public class ImAdminController {

    private final ImService imService;

    @Operation(summary = "获取全部会话（分页）")
    @GetMapping("/sessions")
    public ApiResponse<Page<ImHuiHua>> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ImHuiHua> sessions = imService.getAllSessions(pageable);
        return ApiResponse.success(sessions);
    }

    @Operation(summary = "查看指定会话的消息")
    @GetMapping("/messages/{sessionId}")
    public ApiResponse<Page<ImXiaoXi>> getSessionMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ImXiaoXi> messages = imService.getMessages(sessionId, 0L, pageable);
        return ApiResponse.success(messages);
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<String> deleteSession(
            @PathVariable String sessionId) {
        
        imService.deleteSession(sessionId);
        return ApiResponse.success("会话删除成功");
    }

    @Operation(summary = "按关键词搜索消息")
    @GetMapping("/search")
    public ApiResponse<Page<ImXiaoXi>> searchMessages(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ImXiaoXi> messages = imService.searchMessages(keyword, pageable);
        return ApiResponse.success(messages);
    }

    @Operation(summary = "获取 IM 统计数据")
    @GetMapping("/statistics")
    public ApiResponse<java.util.Map<String, Object>> getStatistics() {
        
        java.util.Map<String, Object> stats = imService.getStatistics();
        return ApiResponse.success(stats);
    }
}