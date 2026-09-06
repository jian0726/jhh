package com.delta.rental.service;

import com.delta.rental.entity.ShangPin;
import com.delta.rental.entity.ShouCang;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.ShangPinRepository;
import com.delta.rental.repository.ShouCangRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShouCangService {

    private final ShouCangRepository shouCangRepository;
    private final ShangPinRepository shangPinRepository;

    public ShouCang addShouCang(Long yongHuId, Long shangPinId) {
        if (shouCangRepository.existsByYongHuIdAndShangPinId(yongHuId, shangPinId)) {
            throw new BusinessException(400, "该商品已在收藏列表中");
        }

        ShangPin shangPin = shangPinRepository.findById(shangPinId)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));

        ShouCang shouCang = ShouCang.builder()
                .yongHuId(yongHuId)
                .shangPinId(shangPinId)
                .build();

        return shouCangRepository.save(shouCang);
    }

    public Page<ShangPin> getShouCangList(Long yongHuId, Pageable pageable) {
        Page<ShouCang> shouCangPage = shouCangRepository.findByYongHuIdOrderByChuangJianShiJianDesc(yongHuId, pageable);
        
        List<Long> shangPinIds = shouCangPage.getContent().stream()
                .map(ShouCang::getShangPinId)
                .collect(Collectors.toList());

        List<ShangPin> shangPinList = shangPinRepository.findAllById(shangPinIds);
        
        return shouCangPage.map(sc -> 
            shangPinList.stream()
                .filter(sp -> sp.getId().equals(sc.getShangPinId()))
                .findFirst()
                .orElse(null)
        );
    }

    @Transactional
    public void removeShouCang(Long yongHuId, Long shangPinId) {
        ShouCang shouCang = shouCangRepository.findByYongHuIdAndShangPinId(yongHuId, shangPinId)
                .orElseThrow(() -> new BusinessException(404, "收藏记录不存在"));
        
        shouCangRepository.delete(shouCang);
    }

    public boolean isShouCang(Long yongHuId, Long shangPinId) {
        return shouCangRepository.existsByYongHuIdAndShangPinId(yongHuId, shangPinId);
    }
}