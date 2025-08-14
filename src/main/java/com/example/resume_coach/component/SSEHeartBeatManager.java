package com.example.resume_coach.component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class SSEHeartBeatManager {
    private final ThreadPoolTaskScheduler sseKeepAliveScheduler;
    private final Map<SseEmitter, Long> lastActivity = new ConcurrentHashMap<>();
    private final Duration idleThreshold = Duration.ofSeconds(59);
    private final Duration checkEvery = Duration.ofSeconds(10);
    private ScheduledFuture<?> task;

    @PostConstruct
    void start() {
        task = sseKeepAliveScheduler.scheduleAtFixedRate(this::tick, checkEvery);
    }

    @PreDestroy
    void stop() {
        if (task != null) task.cancel(true);
    }

    public void register(SseEmitter emitter) {
        touch(emitter); // now
        emitter.onCompletion(() -> lastActivity.remove(emitter));
        emitter.onTimeout(() -> lastActivity.remove(emitter));
    }

    /**
     * 토큰 전송 등 활동 시 매번 호출해서 타임스탬프 갱신
     */
    public void touch(SseEmitter emitter) {
        lastActivity.put(emitter, System.currentTimeMillis());
    }

    private void tick() {
        long now = System.currentTimeMillis();
        List<SseEmitter> toRemove = new ArrayList<>();

        for (Map.Entry<SseEmitter, Long> entry : lastActivity.entrySet()) {
            SseEmitter emitter = entry.getKey();
            long ts = entry.getValue();
            if (now - ts < idleThreshold.toMillis()) {
                continue;
            }
            try {
                emitter.send(SseEmitter.event().name("keepalive").comment("ping"));
                entry.setValue(now);
            } catch (Exception e) {
                toRemove.add(emitter); // 루프 끝난 뒤 제거
                completeQuietly(emitter, e);
            }
        }

        for (SseEmitter emitter : toRemove) {
            lastActivity.remove(emitter);
        }
    }

    private void completeQuietly(SseEmitter emitter, Exception cause) {
        try {
            emitter.completeWithError(cause);
        } catch (Exception ignore) {}
    }
}
