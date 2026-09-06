package com.delta.rental.service;

import com.delta.rental.entity.DingDan;
import com.delta.rental.entity.QianBao;
import com.delta.rental.entity.TuiKuanShenQing;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.DingDanRepository;
import com.delta.rental.repository.QianBaoRepository;
import com.delta.rental.repository.TuiKuanShenQingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TuiKuanService {

    private final TuiKuanShenQingRepository tuiKuanShenQingRepository;
    private final DingDanRepository dingDanRepository;
    private final QianBaoRepository qianBaoRepository;

    public TuiKuanShenQing applyRefund(Long yongHuId, String dingDanHao, String reason, String evidenceUrl) {
        DingDan order = dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        if (!order.getMaiJiaId2().equals(yongHuId)) {
            throw new BusinessException(403, "无权申请退款");
        }

        if (order.getZhuangTai() != 1) {
            throw new BusinessException(400, "订单状态不允许申请退款");
        }

        if (tuiKuanShenQingRepository.findByDingDanHao(dingDanHao).isPresent()) {
            throw new BusinessException(400, "该订单已申请退款");
        }

        TuiKuanShenQing shenQing = TuiKuanShenQing.builder()
                .dingDanHao(dingDanHao)
                .yongHuId(yongHuId)
                .tuiKuanYuanYin(reason)
                .pingZhengUrl(evidenceUrl)
                .tuiKuanJinE(order.getZongJinE())
                .zhuangTai(0)
                .build();

        return tuiKuanShenQingRepository.save(shenQing);
    }

    public TuiKuanShenQing getRefundStatus(String dingDanHao) {
        return tuiKuanShenQingRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "退款申请不存在"));
    }

    public Page<TuiKuanShenQing> getRefundList(Long yongHuId, Pageable pageable) {
        return tuiKuanShenQingRepository.findByYongHuIdOrderByShenQingShiJianDesc(yongHuId, pageable);
    }

    @Transactional
    public void sellerHandleRefund(Long sellerId, String dingDanHao, boolean agree) {
        TuiKuanShenQing shenQing = tuiKuanShenQingRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "退款申请不存在"));

        DingDan order = dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        if (!order.getMaiJiaId().equals(sellerId)) {
            throw new BusinessException(403, "无权处理该退款");
        }

        shenQing.setMaiJiaFuKuan(agree ? 1 : 2);

        if (!agree) {
            shenQing.setZhuangTai(2);
            shenQing.setChuLiShiJian(LocalDateTime.now());
        }

        tuiKuanShenQingRepository.save(shenQing);
    }

    @Transactional
    public void adminHandleRefund(Long adminId, String dingDanHao, boolean agree, String remark) {
        TuiKuanShenQing shenQing = tuiKuanShenQingRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "退款申请不存在"));

        DingDan order = dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        shenQing.setZhuangTai(agree ? 1 : 3);
        shenQing.setChuLiShiJian(LocalDateTime.now());
        shenQing.setGuanLiYuanBeiZhu(remark);

        tuiKuanShenQingRepository.save(shenQing);

        if (agree) {
            QianBao buyerWallet = qianBaoRepository.findByYongHuId(order.getMaiJiaId2())
                    .orElseGet(() -> QianBao.builder().yongHuId(order.getMaiJiaId2()).build());
            buyerWallet.setYuE(buyerWallet.getYuE().add(order.getZongJinE()));
            buyerWallet.setKeTiXianJinE(buyerWallet.getKeTiXianJinE().add(order.getZongJinE()));
            qianBaoRepository.save(buyerWallet);

            order.setZhuangTai(4);
            dingDanRepository.save(order);
        }
    }
}