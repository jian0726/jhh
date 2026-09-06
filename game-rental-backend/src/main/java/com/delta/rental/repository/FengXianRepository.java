package com.delta.rental.repository;

import com.delta.rental.entity.FengXian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FengXianRepository extends JpaRepository<FengXian, Long> {
    List<FengXian> findByYongHuId(Long yongHuId);
    List<FengXian> findByFengXianLeiXing(String fengXianLeiXing);
}
