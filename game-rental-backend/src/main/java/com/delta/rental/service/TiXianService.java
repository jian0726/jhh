package com.delta.rental.service;

import com.delta.rental.entity.DingDan;
import com.delta.rental.entity.QianBao;
import com.delta.rental.entity.TiXianShenQing;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.DingDanRepository;
import com.delta.rental.repository.QianBaoRepository;
import com.delta.rental.repository.TiXianShenQingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TiXianService {

    private final TiXianShenQingRepository tiXianShenQingRepository;
    private final QianBaoRepository qianBaoRepository;

    public TiXianShenQing applyWithdraw(Long yongHuId, BigDecimal amount, String method, String account, String name) {
        QianBao qianBao = qianBaoRepository.findByYongHuId(yongHuId)
                .orElseThrow(() -> new BusinessException(400, "钱包不存在"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "提现金额必须大于0");
        }

        if (amount.compareTo(qianBao.getKeTiXianJinE()) > 0) {
            throw new BusinessException(400, "可提现余额不足");
        }

        String tiXianHao = "TX" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);

        TiXianShenQing shenQing = TiXianShenQing.builder()
                .tiXianHao(tiXianHao)
                .yongHuId(yongHuId)
                .tiXianJinE(amount)
                .shouKuanFangShi(method)
                .shouKuanZhangHao(account)
                .shouKuanRen(name)
                .zhuangTai(0)
                .build();

        qianBao.setKeTiXianJinE(qianBao.getKeTiXianJinE().subtract(amount));
        qianBaoRepository.save(qianBao);

        return tiXianShenQingRepository.save(shenQing);
    }

    public Page<TiXianShenQing> getWithdrawList(Long yongHuId, Pageable pageable) {
        return tiXianShenQingRepository.findByYongHuIdOrderByShenQingShiJianDesc(yongHuId, pageable);
    }

    public QianBao getBalance(Long yongHuId) {
        return qianBaoRepository.findByYongHuId(yongHuId)
                .orElseGet(() -> QianBao.builder().yongHuId(yongHuId).yuE(BigDecimal.ZERO).keTiXianJinE(BigDecimal.ZERO).build());
    }

    @Transactional
    public void confirmWithdraw(Long adminId, Long tiXianId) {
        TiXianShenQing shenQing = tiXianShenQingRepository.findById(tiXianId)
                .orElseThrow(() -> new BusinessException(404, "提现申请不存在"));

        shenQing.setZhuangTai(1);
        shenQing.setChuLiShiJian(LocalDateTime.now());
        tiXianShenQingRepository.save(shenQing);
    }

    @Transactional
    public void rejectWithdraw(Long adminId, Long tiXianId, String reason) {
        TiXianShenQing shenQing = tiXianShenQingRepository.findById(tiXianId)
                .orElseThrow(() -> new BusinessException(404, "提现申请不存在"));

        shenQing.setZhuangTai(2);
        shenQing.setChuLiShiJian(LocalDateTime.now());
        shenQing.setFailReason(reason);
        tiXianShenQingRepository.save(shenQing);

        QianBao qianBao = qianBaoRepository.findByYongHuId(shenQing.getYongHuId())
                .orElseThrow(() -> new BusinessException(404, "钱包不存在"));
        qianBao.setKeTiXianJinE(qianBao.getKeTiXianJinE().add(shenQing.getTiXianJinE()));
        qianBaoRepository.save(qianBao);
    }
}