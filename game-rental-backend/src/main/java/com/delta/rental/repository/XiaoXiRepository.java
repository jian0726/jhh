package com.delta.rental.repository;

import com.delta.rental.entity.XiaoXi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface XiaoXiRepository extends JpaRepository<XiaoXi, Long> {
    List<XiaoXi> findByYongHuId(Long yongHuId);
    List<XiaoXi> findByYongHuIdAndShiFouYiDu(Long yongHuId, Integer shiFouYiDu);
    long countByYongHuIdAndShiFouYiDu(Long yongHuId, Integer shiFouYiDu);
}
