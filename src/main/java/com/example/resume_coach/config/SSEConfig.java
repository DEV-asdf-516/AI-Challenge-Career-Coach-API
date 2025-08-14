package com.example.resume_coach.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SSEConfig {
    @Bean
    public ThreadPoolTaskScheduler sseKeepAliveScheduler() {
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(4);
        ts.setThreadNamePrefix("sse-keepalive-");
        ts.initialize();
        return ts;
    }
}