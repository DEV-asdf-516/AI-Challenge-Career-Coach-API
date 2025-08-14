package com.example.resume_coach.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SSEConfig {
    @Bean
    public ThreadPoolTaskScheduler sseKeepAliveScheduler() {
        ThreadPoolTaskScheduler pool = new ThreadPoolTaskScheduler();
        pool.setPoolSize(4);
        pool.setThreadNamePrefix("sse-keepalive-");
        pool.initialize();
        return pool;
    }
}