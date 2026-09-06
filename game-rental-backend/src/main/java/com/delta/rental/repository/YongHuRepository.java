package com.delta.rental.repository;

import com.delta.rental.entity.YongHu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface YongHuRepository extends JpaRepository<YongHu, Long> {
    Optional<YongHu> findByYongHuMing(String yongHuMing);
    Optional<YongHu> findByShouJiHao(String shouJiHao);
    Optional<YongHu> findByOpenIdAndOpenIdType(String openId, String openIdType);
    boolean existsByYongHuMing(String yongHuMing);
    boolean existsByShouJiHao(String shouJiHao);
    Optional<YongHu> findByEmail(String email);
    boolean existsByEmail(String email);
}
