package com.delta.rental.repository;

import com.delta.rental.entity.GuanLiYuan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GuanLiYuanRepository extends JpaRepository<GuanLiYuan, Long> {
    List<GuanLiYuan> findByGuanLiYuanId(Long guanLiYuanId);
    List<GuanLiYuan> findByMuBiaoLeiXingAndMuBiaoId(String muBiaoLeiXing, Long muBiaoId);

    /** 后台「操作日志」列表：按时间倒序。 */
    List<GuanLiYuan> findAllByOrderByChuangJianShiJianDesc();
}
