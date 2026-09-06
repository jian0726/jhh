package com.delta.rental.repository;

import com.delta.rental.entity.PingJia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PingJiaRepository extends JpaRepository<PingJia, Long> {
    List<PingJia> findByShangPinId(Long shangPinId);
    List<PingJia> findByPingJiaRenId(Long pingJiaRenId);
}
