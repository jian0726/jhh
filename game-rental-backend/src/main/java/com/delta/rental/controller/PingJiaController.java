package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.dto.PingJiaRequest;
import com.delta.rental.entity.PingJia;
import com.delta.rental.service.PingJiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pingjia")
@RequiredArgsConstructor
public class PingJiaController {

    private final PingJiaService pingJiaService;

    @PostMapping
    public ApiResponse<PingJia> faBuPingJia(
            @RequestHeader("Authorization") String token,
            @RequestBody PingJiaRequest request) {
        Long pingJiaRenId = tiQuYongHuId(token);
        return ApiResponse.success(pingJiaService.createPingJia(pingJiaRenId, request));
    }

    @GetMapping("/shangpin/{shangPinId}")
    public ApiResponse<List<PingJia>> getShangPinPingJia(@PathVariable Long shangPinId) {
        return ApiResponse.success(pingJiaService.getShangPinPingJia(shangPinId));
    }

    @GetMapping("/yonghu")
    public ApiResponse<List<PingJia>> getYongHuPingJia(@RequestHeader("Authorization") String token) {
        Long yongHuId = tiQuYongHuId(token);
        return ApiResponse.success(pingJiaService.getYongHuPingJia(yongHuId));
    }

    @GetMapping("/shangpin/{shangPinId}/pingfen")
    public ApiResponse<Double> getPingJunPingFen(@PathVariable Long shangPinId) {
        return ApiResponse.success(pingJiaService.getAveragePingFen(shangPinId));
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
