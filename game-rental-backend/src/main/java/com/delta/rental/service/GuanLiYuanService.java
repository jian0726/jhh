package com.delta.rental.service;

import com.delta.rental.entity.GuanLiYuan;
import com.delta.rental.repository.GuanLiYuanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuanLiYuanService {
    
    private final GuanLiYuanRepository guanLiYuanRepository;
    
    public GuanLiYuan logAction(Long guanLiYuanId, String action, String targetType, Long targetId, String detail) {
        GuanLiYuan log = new GuanLiYuan();
        log.setGuanLiYuanId(guanLiYuanId);
        log.setCaoZuo(action);
        log.setMuBiaoLeiXing(targetType);
        log.setMuBiaoId(targetId);
        log.setXiQing(detail);
        return guanLiYuanRepository.save(log);
    }
    
    public List<GuanLiYuan> getGuanLiYuanLogs(Long guanLiYuanId) {
        return guanLiYuanRepository.findByGuanLiYuanId(guanLiYuanId);
    }
    
    public List<GuanLiYuan> getLogsByTarget(String targetType, Long targetId) {
        return guanLiYuanRepository.findByMuBiaoLeiXingAndMuBiaoId(targetType, targetId);
    }
}
