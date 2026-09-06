package com.delta.rental.repository;

import com.delta.rental.entity.FengKongRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FengKongRuleRepository extends JpaRepository<FengKongRule, Long> {
    List<FengKongRule> findByEnabled(Boolean enabled);
    Optional<FengKongRule> findByRuleCode(String ruleCode);
    List<FengKongRule> findByRuleType(String ruleType);
}
