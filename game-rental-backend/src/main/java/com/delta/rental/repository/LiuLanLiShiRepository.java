package com.delta.rental.repository;

import com.delta.rental.entity.LiuLanLiShi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiuLanLiShiRepository extends JpaRepository<LiuLanLiShi, Long> {

    Page<LiuLanLiShi> findByYongHuIdOrderByLiuLanShiJianDesc(Long yongHuId, Pageable pageable);

    void deleteByYongHuId(Long yongHuId);

    void deleteByYongHuIdAndShangPinId(Long yongHuId, Long shangPinId);

    @Modifying
    @Query("DELETE FROM LiuLanLiShi l WHERE l.yongHuId = :yongHuId AND l.liuLanShiJian < :time")
    void deleteOldRecords(@Param("yongHuId") Long yongHuId, @Param("time") LocalDateTime time);
}