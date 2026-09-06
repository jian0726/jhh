package com.delta.rental.repository;

import com.delta.rental.entity.ImXiaoXi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImXiaoXiRepository extends JpaRepository<ImXiaoXi, Long> {

    Page<ImXiaoXi> findByHuiHuaHaoOrderByFaSongShiJianAsc(String huiHuaHao, Pageable pageable);

    @Modifying
    @Query("UPDATE ImXiaoXi x SET x.shiFouYiDu = 1 WHERE x.huiHuaHao = :huiHuaHao AND x.shouDaoRenId = :userId AND x.shiFouYiDu = 0")
    void markAsRead(@Param("huiHuaHao") String huiHuaHao, @Param("userId") Long userId);

    @Query("SELECT COUNT(x) FROM ImXiaoXi x WHERE x.huiHuaHao = :huiHuaHao AND x.shouDaoRenId = :userId AND x.shiFouYiDu = 0")
    Long countUnread(@Param("huiHuaHao") String huiHuaHao, @Param("userId") Long userId);

    void deleteByHuiHuaHao(String huiHuaHao);

    @Query("SELECT x FROM ImXiaoXi x WHERE x.neiRong LIKE %:keyword% ORDER BY x.faSongShiJian DESC")
    Page<ImXiaoXi> searchByNeiRong(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(x) FROM ImXiaoXi x WHERE x.faSongShiJian >= CURRENT_DATE")
    Long countToday();
}