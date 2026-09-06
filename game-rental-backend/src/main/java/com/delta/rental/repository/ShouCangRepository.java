package com.delta.rental.repository;

import com.delta.rental.entity.ShouCang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShouCangRepository extends JpaRepository<ShouCang, Long> {

    Optional<ShouCang> findByYongHuIdAndShangPinId(Long yongHuId, Long shangPinId);

    Page<ShouCang> findByYongHuIdOrderByChuangJianShiJianDesc(Long yongHuId, Pageable pageable);

    List<ShouCang> findByYongHuId(Long yongHuId);

    void deleteByYongHuIdAndShangPinId(Long yongHuId, Long shangPinId);

    boolean existsByYongHuIdAndShangPinId(Long yongHuId, Long shangPinId);
}