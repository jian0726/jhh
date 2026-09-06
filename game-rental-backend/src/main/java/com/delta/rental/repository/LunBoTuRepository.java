package com.delta.rental.repository;

import com.delta.rental.entity.LunBoTu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LunBoTuRepository extends JpaRepository<LunBoTu, Long> {
    List<LunBoTu> findByZhuangTaiOrderByShunXuAsc(Integer zhuangTai);
}