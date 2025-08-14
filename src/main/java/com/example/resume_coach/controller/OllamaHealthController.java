package com.example.resume_coach.controller;

import com.example.resume_coach.component.OllamaApiClient;
import com.example.resume_coach.handler.SSEOllamaStreamHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/ollama")
@RequiredArgsConstructor
@Slf4j
public class OllamaHealthController {
    private final OllamaApiClient ollamaApiClient;

    @GetMapping(path="/greeting",produces="text/event-stream;charset=UTF-8")
    public SseEmitter testOllamaGeneration() {
        SseEmitter emitter = new SseEmitter();
        ollamaApiClient.callOllamaStream(
                "안녕하세요! 2줄 이내의 간단한 자기소개를 해주세요.",
                Map.of("num_predict", 100, "temperature", 0.2),
                new SSEOllamaStreamHandler(emitter,null,true,null)
        );
        return emitter;
    }

}