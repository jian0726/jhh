package com.delta.rental.repository;

import com.delta.rental.entity.QianBao;
import com.delta.rental.entity.TiXianShenQing;
import com.delta.rental.entity.TuiKuanShenQing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TuiKuanShenQingRepository extends JpaRepository<TuiKuanShenQing, Long> {

    Optional<TuiKuanShenQing> findByDingDanHao(String dingDanHao);

    Page<TuiKuanShenQing> findByYongHuIdOrderByShenQingShiJianDesc(Long yongHuId, Pageable pageable);

    Page<TuiKuanShenQing> findByZhuangTaiOrderByShenQingShiJianDesc(Integer zhuangTai, Pageable pageable);
}