package com.delta.rental.service;

import com.delta.rental.dto.PingJiaRequest;
import com.delta.rental.entity.DingDan;
import com.delta.rental.entity.PingJia;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.DingDanRepository;
import com.delta.rental.repository.PingJiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PingJiaService {
    
    private final PingJiaRepository pingJiaRepository;
    private final DingDanRepository dingDanRepository;
    
    public PingJia createPingJia(Long pingJiaRenId, PingJiaRequest request) {
        DingDan dingDan = dingDanRepository.findById(request.getDingDanId())
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        
        if (!dingDan.getMaiJiaId2().equals(pingJiaRenId)) {
            throw new BusinessException(403, "无权评价此订单");
        }
        
        if (dingDan.getZhuangTai() != 2) {
            throw new BusinessException(400, "订单未完成，无法评价");
        }
        
        PingJia pingJia = new PingJia();
        pingJia.setDingDanId(request.getDingDanId());
        pingJia.setShangPinId(request.getShangPinId());
        pingJia.setPingJiaRenId(pingJiaRenId);
        pingJia.setPingFen(request.getPingFen());
        pingJia.setPingJiaNeiRong(request.getPingJiaNeiRong());
        
        return pingJiaRepository.save(pingJia);
    }
    
    public List<PingJia> getShangPinPingJia(Long shangPinId) {
        return pingJiaRepository.findByShangPinId(shangPinId);
    }
    
    public List<PingJia> getYongHuPingJia(Long yongHuId) {
        return pingJiaRepository.findByPingJiaRenId(yongHuId);
    }
    
    public Double getAveragePingFen(Long shangPinId) {
        List<PingJia> pingJias = pingJiaRepository.findByShangPinId(shangPinId);
        if (pingJias.isEmpty()) {
            return 0.0;
        }
        return pingJias.stream()
                .mapToInt(PingJia::getPingFen)
                .average()
                .orElse(0.0);
    }
}
