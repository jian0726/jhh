package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.dto.LunBoTuRequest;
import com.delta.rental.entity.LunBoTu;
import com.delta.rental.service.LunBoTuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 轮播图接口。
 *
 * <p>图片的存储方式：先调 {@code POST /api/upload/image} 上传图片得到绝对 URL，
 * 再调用下方的增删改接口把 URL 保存进数据库。前端首页只读取 {@code GET /api/lunbotu}，
 * 拿到 URL 后直接 &lt;img :src="item.tuPianUrl"&gt; 渲染。
 *
 * <p>注意：写接口（新增/修改/删除）与 {@code GET /api/lunbotu/all} 已由
 * {@link com.delta.rental.config.SecurityConfig} 限制为管理员（ROLE_ADMIN）可访问；
 * 首页公开接口 {@code GET /api/lunbotu} 匿名可读。
 */
@Tag(name = "轮播图", description = "首页轮播的增删改查")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LunBoTuController {

    private final LunBoTuService lunBoTuService;

    /** 首页：启用中的轮播，按顺序号升序 */
    @Operation(summary = "获取启用中的轮播图（首页）")
    @GetMapping("/lunbotu")
    public ApiResponse<List<LunBoTu>> getLunBoTu() {
        return ApiResponse.success(lunBoTuService.getActiveLunBoTu());
    }

    /** 管理页：全部轮播（含停用） */
    @Operation(summary = "获取全部轮播图（管理）")
    @GetMapping("/lunbotu/all")
    public ApiResponse<List<LunBoTu>> getAllLunBoTu() {
        return ApiResponse.success(lunBoTuService.getAllLunBoTu());
    }

    /** 新增轮播图 */
    @Operation(summary = "新增轮播图")
    @PostMapping("/lunbotu")
    public ApiResponse<LunBoTu> createLunBoTu(@RequestBody LunBoTuRequest request) {
        return ApiResponse.success("新增成功", lunBoTuService.createLunBoTu(request));
    }

    /** 修改轮播图（可按需只传部分字段） */
    @Operation(summary = "修改轮播图")
    @PutMapping("/lunbotu/{id}")
    public ApiResponse<LunBoTu> updateLunBoTu(@PathVariable Long id, @RequestBody LunBoTuRequest request) {
        return ApiResponse.success("修改成功", lunBoTuService.updateLunBoTu(id, request));
    }

    /** 删除轮播图（同时清理磁盘上的图片文件） */
    @Operation(summary = "删除轮播图")
    @DeleteMapping("/lunbotu/{id}")
    public ApiResponse<Void> deleteLunBoTu(@PathVariable Long id) {
        lunBoTuService.deleteLunBoTu(id);
        return ApiResponse.success("删除成功", null);
    }
}
