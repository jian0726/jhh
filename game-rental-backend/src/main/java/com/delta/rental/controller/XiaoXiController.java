package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.XiaoXi;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.service.XiaoXiService;
import com.delta.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 消息中心接口。
 *
 * <p>注意：数据库/实体里的字段是 {@code shiFouYiDu}、{@code chuangJianShiJian}，
 * 而前端契约使用 {@code shiFouDuGuo}、{@code faSongShiJian}。
 * 这里统一在 Controller 层做转换（两个名字都返回，同时兼容新旧前端）。
 */
@Tag(name = "站内消息", description = "消息列表、未读数与已读标记")
@RestController
@RequestMapping("/api/xiaoxi")
@RequiredArgsConstructor
public class XiaoXiController {

    private final XiaoXiService xiaoXiService;

    @Operation(summary = "获取我的消息列表")
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getYongHuXiaoXi() {
        Long yongHuId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(toVoList(xiaoXiService.getYongHuXiaoXi(yongHuId)));
    }

    @Operation(summary = "获取未读消息")
    @GetMapping("/weidu")
    public ApiResponse<List<Map<String, Object>>> getWeiDuXiaoXi() {
        Long yongHuId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(toVoList(xiaoXiService.getWeiDuXiaoXi(yongHuId)));
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/weidu/count")
    public ApiResponse<Map<String, Object>> getWeiDuCount() {
        Long yongHuId = SecurityUtils.getCurrentUserId();
        long count = xiaoXiService.getWeiDuCount(yongHuId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", count);
        return ApiResponse.success(result);
    }

    @Operation(summary = "标记单条消息已读")
    @PutMapping("/{id}/dushu")
    public ApiResponse<Map<String, Object>> jiLuWeiDu(@PathVariable Long id) {
        return ApiResponse.success(toVo(xiaoXiService.markAsRead(id)));
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("/quanbudushu")
    public ApiResponse<Void> quanBuDuShu() {
        Long yongHuId = SecurityUtils.getCurrentUserId();
        xiaoXiService.markAllAsRead(yongHuId);
        return ApiResponse.success("标记已读成功", null);
    }

    private List<Map<String, Object>> toVoList(List<XiaoXi> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    private Map<String, Object> toVo(XiaoXi m) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", m.getId());
        vo.put("yongHuId", m.getYongHuId());
        vo.put("biaoTi", m.getBiaoTi());
        vo.put("neiRong", m.getNeiRong());
        vo.put("leiXing", m.getLeiXing());
        // 前端契约字段名
        vo.put("shiFouDuGuo", m.getShiFouYiDu());
        vo.put("faSongShiJian", m.getChuangJianShiJian());
        // 兼容实体原字段名
        vo.put("shiFouYiDu", m.getShiFouYiDu());
        vo.put("chuangJianShiJian", m.getChuangJianShiJian());
        return vo;
    }

}
