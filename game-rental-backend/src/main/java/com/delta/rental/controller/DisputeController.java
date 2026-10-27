package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.DingDan;
import com.delta.rental.entity.FengXian;
import com.delta.rental.service.DingDanService;
import com.delta.rental.repository.FengXianRepository;
import com.delta.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "纠纷", description = "纠纷提交、证据上传与详情查询")
@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DingDanService dingDanService;
    private final FengXianRepository fengXianRepository;

    @Operation(summary = "获取我的纠纷列表")
    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyDisputes() {
        Long userId = SecurityUtils.getCurrentUserId();

        List<DingDan> orders = dingDanService.getMyOrders(userId);
        List<Map<String, Object>> disputes = orders.stream()
                .filter(order -> order.getZhuangTai() == 4)
                .map(order -> {
                    Map<String, Object> dispute = new HashMap<>();
                    dispute.put("id", order.getId());
                    dispute.put("dingDanHao", order.getDingDanHao());
                    dispute.put("shangPinId", order.getShangPinId());
                    dispute.put("zongJinE", order.getZongJinE());
                    dispute.put("chuangJianShiJian", order.getChuangJianShiJian());
                    return dispute;
                })
                .collect(Collectors.toList());

        return ApiResponse.success(disputes);
    }

    @Operation(summary = "查询纠纷详情")
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getDisputeDetail(@PathVariable Long id) {
        DingDan order = dingDanService.getDingDanByHao(id.toString());

        if (order.getZhuangTai() != 4) {
            throw new com.delta.rental.exception.BusinessException(400, "订单不是纠纷状态");
        }

        Map<String, Object> dispute = new HashMap<>();
        dispute.put("id", order.getId());
        dispute.put("dingDanHao", order.getDingDanHao());
        dispute.put("shangPinId", order.getShangPinId());
        dispute.put("maiJiaId", order.getMaiJiaId());
        dispute.put("maiJiaId2", order.getMaiJiaId2());
        dispute.put("zongJinE", order.getZongJinE());
        dispute.put("zuJieTianShu", order.getZuJieTianShu());
        dispute.put("chuangJianShiJian", order.getChuangJianShiJian());

        return ApiResponse.success(dispute);
    }

    @Operation(summary = "提交纠纷")
    @PostMapping
    public ApiResponse<DingDan> submitDispute(
            @RequestParam String dingDanHao,
            @RequestParam String reason) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.jiufengDingDan(dingDanHao, userId, reason));
    }

    @Operation(summary = "上传纠纷证据")
    @PostMapping("/{id}/evidence")
    public ApiResponse<Map<String, Object>> uploadEvidence(
            @PathVariable Long id,
            @RequestParam String evidenceUrl) {

        FengXian fengXian = new FengXian();
        fengXian.setYongHuId(id);
        fengXian.setFengXianLeiXing("EVIDENCE");
        fengXian.setMiaoShu("上传证据: " + evidenceUrl);
        fengXianRepository.save(fengXian);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "证据上传成功");

        return ApiResponse.success(result);
    }

}
