package com.delta.rental.repository;

import com.delta.rental.entity.ShangPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShangPinRepository extends JpaRepository<ShangPin, Long> {
    List<ShangPin> findByMaiJiaId(Long maiJiaId);
    List<ShangPin> findByZhuangTai(Integer zhuangTai);
    List<ShangPin> findByZhuangTaiIn(List<Integer> zhuangTai);

    @Query(value = "SELECT * FROM shangpin WHERE MATCH(biao_ti, miao_shu, you_xi_zhang_hao) AGAINST(:keyword IN NATURAL LANGUAGE MODE) AND zhuang_tai = 1", nativeQuery = true)
    List<ShangPin> searchByKeyword(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM shangpin WHERE MATCH(biao_ti, miao_shu, you_xi_zhang_hao) AGAINST(:keyword IN BOOLEAN MODE) AND zhuang_tai = 1", nativeQuery = true)
    List<ShangPin> searchByKeywordBoolean(@Param("keyword") String keyword);

    @Query("SELECT s FROM ShangPin s WHERE s.zhuangTai = 1 " +
           "AND (:minPrice IS NULL OR s.riZuJiaGe >= :minPrice) " +
           "AND (:maxPrice IS NULL OR s.riZuJiaGe <= :maxPrice) " +
           "AND (:zuiShaoZuJieTianShu IS NULL OR s.zuiShaoZuJieTianShu <= :zuiShaoZuJieTianShu)")
    List<ShangPin> filterShangPin(
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("zuiShaoZuJieTianShu") Integer zuiShaoZuJieTianShu);
}