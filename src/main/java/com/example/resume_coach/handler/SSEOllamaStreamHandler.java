package com.example.resume_coach.handler;

import com.example.resume_coach.component.SSEHeartBeatManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.function.Function;


@RequiredArgsConstructor
public class SSEOllamaStreamHandler<T> implements StreamHandler {
    private final SseEmitter emitter;
    private final Function<String, T> parser;
    private final boolean emitDeltas;
    private final SSEHeartBeatManager heartbeat;
    private final StringBuilder sb = new StringBuilder();

    @Override
    public void onToken(String token) {
        sb.append(token);
        if (!emitDeltas) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().data(token));
            if (heartbeat != null) {
                heartbeat.touch(emitter); // 활동 갱신
            }
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    @Override
    public void onError(Throwable t) {
        try {
            emitter.send(SseEmitter.event().name("error").data(t.getMessage()));
        } catch (Exception e) {
            emitter.completeWithError(t);
        }
    }

    @SneakyThrows
    @Override
    public void onComplete() {
        String full = sb.toString();
        try {
            if (parser == null) {
                emitter.send(SseEmitter.event().name("final").data(full));
            } else {
                T dto = parser.apply(full); // DTO 파싱
                emitter.send(SseEmitter.event().name("final").data(dto));
            }
            emitter.complete();
        } catch (Exception ex) {
            onError(ex);
        }
    }
}
