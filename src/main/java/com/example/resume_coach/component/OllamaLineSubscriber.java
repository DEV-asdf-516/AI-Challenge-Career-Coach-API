package com.example.resume_coach.component;

import com.example.resume_coach.handler.StreamHandler;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Flow;

@RequiredArgsConstructor
@Slf4j
public class OllamaLineSubscriber implements Flow.Subscriber<String> {

    private static final long REQUEST_BATCH = 32;
    private static final long MAX_LATENCY_MS = 1000;
    private final StreamHandler handler;
    private final ObjectReader chunkReader;
    private final StringBuilder tokenBuffer = new StringBuilder(2048);
    private int consumedSinceRequest = 0;
    private Flow.Subscription subscription;
    private long lastFlushAt = System.currentTimeMillis();

    public OllamaLineSubscriber(ObjectMapper objectMapper, StreamHandler handler) {
        this.handler = handler;
        this.chunkReader = objectMapper.readerFor(Chunk.class);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(REQUEST_BATCH);
    }

    @Override
    public void onNext(String line) {
        try {
            if (line == null || line.isBlank()) {
                return;
            }
            Chunk c = chunkReader.readValue(line);
            String token = (c.message != null && c.message.content != null) ? c.message.content : null;

            if (token != null && !token.isEmpty()) {
                tokenBuffer.append(token);
            }
            boolean timeReached = System.currentTimeMillis() - lastFlushAt > MAX_LATENCY_MS;

            if (timeReached) {
                flushBatch();
            }

            // 백프레셔 호출 줄이기
            if (++consumedSinceRequest >= REQUEST_BATCH) {
                subscription.request(REQUEST_BATCH);
                consumedSinceRequest = 0;
            }
        } catch (Exception ignore) {
            log.warn("Error processing line: " + line, ignore);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        try {
            flushBatch();
            handler.onError(throwable);
        } finally {
            cancel();
            onComplete();
        }
    }

    @Override
    public void onComplete() {
        flushBatch();
        cancel();
        handler.onComplete();
    }


    public void cancel() {
        try {
            if (subscription != null) subscription.cancel();
        } catch (Throwable ignore) {
        }
    }

    private void flushBatch() {
        if (tokenBuffer.length() == 0) return;
        String batched = tokenBuffer.toString();
        tokenBuffer.setLength(0);
        lastFlushAt = System.currentTimeMillis();
        try {
            handler.onToken(batched);
        } catch (Exception e) {
            handler.onError(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Chunk {
        public Message message;

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Message {
            public String content;
        }
    }
}
