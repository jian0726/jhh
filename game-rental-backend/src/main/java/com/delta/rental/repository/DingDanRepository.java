package com.delta.rental.repository;

import com.delta.rental.entity.DingDan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DingDanRepository extends JpaRepository<DingDan, Long> {
    Optional<DingDan> findByDingDanHao(String dingDanHao);
    List<DingDan> findByMaiJiaId2(Long maiJiaId2);
    List<DingDan> findByMaiJiaId(Long maiJiaId);
    List<DingDan> findByShangPinId(Long shangPinId);
    List<DingDan> findByZhuangTai(Integer zhuangTai);
    List<DingDan> findByFuKuanShiJianAfter(LocalDateTime startTime);
    List<DingDan> findByMaiJiaId2AndFuKuanShiJianAfter(Long userId, LocalDateTime startTime);
    List<DingDan> findByMaiJiaId2AndShangPinIdAndFuKuanShiJianAfter(Long userId, Long shangPinId, LocalDateTime startTime);
    
    @Query("SELECT d FROM DingDan d WHERE d.fuKuanShiJian > :startTime")
    List<DingDan> findRecentOrders(@Param("startTime") LocalDateTime startTime);
}
