package com.delta.rental.service;

import com.delta.rental.entity.XiaoXi;
import com.delta.rental.repository.XiaoXiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class XiaoXiService {

    private final XiaoXiRepository xiaoXiRepository;

    public XiaoXi createXiaoXi(Long yongHuId, String biaoTi, String neiRong, Integer leiXing) {
        XiaoXi xiaoXi = new XiaoXi();
        xiaoXi.setYongHuId(yongHuId);
        xiaoXi.setBiaoTi(biaoTi);
        xiaoXi.setNeiRong(neiRong);
        xiaoXi.setLeiXing(leiXing);
        xiaoXi.setShiFouYiDu(0);
        return xiaoXiRepository.save(xiaoXi);
    }

    public List<XiaoXi> getYongHuXiaoXi(Long yongHuId) {
        return xiaoXiRepository.findByYongHuId(yongHuId);
    }

    public List<XiaoXi> getUnreadXiaoXi(Long yongHuId) {
        return xiaoXiRepository.findByYongHuIdAndShiFouYiDu(yongHuId, 0);
    }

    public List<XiaoXi> getWeiDuXiaoXi(Long yongHuId) {
        return getUnreadXiaoXi(yongHuId);
    }

    public XiaoXi markAsRead(Long xiaoXiId) {
        XiaoXi xiaoXi = xiaoXiRepository.findById(xiaoXiId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        xiaoXi.setShiFouYiDu(1);
        return xiaoXiRepository.save(xiaoXi);
    }

    public void markAllAsRead(Long yongHuId) {
        List<XiaoXi> xiaoXis = xiaoXiRepository.findByYongHuIdAndShiFouYiDu(yongHuId, 0);
        xiaoXis.forEach(m -> m.setShiFouYiDu(1));
        xiaoXiRepository.saveAll(xiaoXis);
    }

    public long getWeiDuCount(Long yongHuId) {
        return xiaoXiRepository.countByYongHuIdAndShiFouYiDu(yongHuId, 0);
    }
}
