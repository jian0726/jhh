package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.DingDan;
import com.delta.rental.entity.FengXian;
import com.delta.rental.service.DingDanService;
import com.delta.rental.repository.FengXianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DingDanService dingDanService;
    private final FengXianRepository fengXianRepository;

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyDisputes(@RequestHeader("Authorization") String token) {
        Long userId = tiQuYongHuId(token);

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

    @PostMapping
    public ApiResponse<DingDan> submitDispute(
            @RequestHeader("Authorization") String token,
            @RequestParam String dingDanHao,
            @RequestParam String reason) {
        Long userId = tiQuYongHuId(token);
        return ApiResponse.success(dingDanService.jiufengDingDan(dingDanHao, userId, reason));
    }

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
