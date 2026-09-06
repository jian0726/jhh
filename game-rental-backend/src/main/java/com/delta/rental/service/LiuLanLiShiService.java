package com.delta.rental.service;

import com.delta.rental.entity.LiuLanLiShi;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.repository.LiuLanLiShiRepository;
import com.delta.rental.repository.ShangPinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiuLanLiShiService {

    private final LiuLanLiShiRepository liuLanLiShiRepository;
    private final ShangPinRepository shangPinRepository;

    public void addLiuLan(Long yongHuId, Long shangPinId) {
        LiuLanLiShi liShi = LiuLanLiShi.builder()
                .yongHuId(yongHuId)
                .shangPinId(shangPinId)
                .liuLanShiJian(LocalDateTime.now())
                .build();

        liuLanLiShiRepository.save(liShi);
    }

    public Page<ShangPin> getLiuLanList(Long yongHuId, Pageable pageable) {
        Page<LiuLanLiShi> liShiPage = liuLanLiShiRepository.findByYongHuIdOrderByLiuLanShiJianDesc(yongHuId, pageable);
        
        List<Long> shangPinIds = liShiPage.getContent().stream()
                .map(LiuLanLiShi::getShangPinId)
                .distinct()
                .collect(Collectors.toList());

        List<ShangPin> shangPinList = shangPinRepository.findAllById(shangPinIds);

        return liShiPage.map(ls -> 
            shangPinList.stream()
                .filter(sp -> sp.getId().equals(ls.getShangPinId()))
                .findFirst()
                .orElse(null)
        );
    }

    @Transactional
    public void clearLiuLan(Long yongHuId) {
        liuLanLiShiRepository.deleteByYongHuId(yongHuId);
    }

    @Transactional
    public void removeLiuLan(Long yongHuId, Long shangPinId) {
        liuLanLiShiRepository.deleteByYongHuIdAndShangPinId(yongHuId, shangPinId);
    }

    @Transactional
    public void cleanOldRecords(Long yongHuId, int days) {
        LocalDateTime thresholdTime = LocalDateTime.now().minusDays(days);
        liuLanLiShiRepository.deleteOldRecords(yongHuId, thresholdTime);
    }
}