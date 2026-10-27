package com.delta.rental.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 开启定时任务。可通过 {@code SCHEDULED_ENABLED=false} 关闭（测试环境或单实例调试时有用）。
 *
 * <p>具体任务见 {@link ScheduledTasks}。
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
