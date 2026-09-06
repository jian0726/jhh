package com.delta.rental.repository;

import com.delta.rental.entity.QianBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QianBaoRepository extends JpaRepository<QianBao, Long> {

    Optional<QianBao> findByYongHuId(Long yongHuId);

    boolean existsByYongHuId(Long yongHuId);
}