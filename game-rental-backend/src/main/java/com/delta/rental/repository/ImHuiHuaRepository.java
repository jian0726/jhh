package com.delta.rental.repository;

import com.delta.rental.entity.ImHuiHua;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImHuiHuaRepository extends JpaRepository<ImHuiHua, Long> {

    Optional<ImHuiHua> findByHuiHuaHao(String huiHuaHao);

    @Query("SELECT h FROM ImHuiHua h WHERE h.yongHuId1 = :userId OR h.yongHuId2 = :userId ORDER BY h.zuiHouXiaoXiShiJian DESC")
    Page<ImHuiHua> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT h FROM ImHuiHua h WHERE (h.yongHuId1 = :userId1 AND h.yongHuId2 = :userId2) OR (h.yongHuId1 = :userId2 AND h.yongHuId2 = :userId1)")
    Optional<ImHuiHua> findByTwoUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    Optional<ImHuiHua> findByHuiHuaHaoAndYongHuId1OrHuiHuaHaoAndYongHuId2(String huiHuaHao, Long userId1,
            String huiHuaHao2, Long userId2);

    Page<ImHuiHua> findAllByOrderByZuiHouXiaoXiShiJianDesc(Pageable pageable);

    @Query("SELECT COUNT(DISTINCT h.yongHuId1) + COUNT(DISTINCT h.yongHuId2) FROM ImHuiHua h")
    Long countDistinctUsers();
}