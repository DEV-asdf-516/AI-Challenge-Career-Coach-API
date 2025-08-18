package com.example.resume_coach.component;

import com.example.resume_coach.handler.OllamaResponseHandler;
import com.example.resume_coach.handler.StreamHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${ollama.api.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.api.end-point}")
    private String ollamaEndPoint;

    @Value("${ollama.model}")
    private String ollamaModel;

    @Value("${ollama.api.timeout.read:600}")
    private int readTimeoutSeconds;

    public OllamaResponseHandler callOllamaStream(String prompt, Map<String, Object> options, StreamHandler handler) {
        try {
            String requestBody = createChatRequestBodyJson(prompt, options, true);
            return callOllamaStreamInternal(requestBody, prompt, handler);
        } catch (JsonProcessingException e) {
            handler.onError(e);
            return new OllamaResponseHandler(CompletableFuture.failedFuture(e), null);
        }
    }


    public OllamaResponseHandler callOllamaStream(
            String systemPrompt,
            String prompt,
            Map<String, Object> options,
            StreamHandler handler
    ) {
        try {
            String requestBody = createChatRequestBodyJson(systemPrompt, prompt, options, true);
            return callOllamaStreamInternal(requestBody, prompt, handler);
        } catch (JsonProcessingException e) {
            handler.onError(e);
           return new OllamaResponseHandler(CompletableFuture.failedFuture(e), null);
        }
    }

    public OllamaResponseHandler callOllamaStreamInternal(
            String requestBody,
            String prompt,
            StreamHandler handler
    ) {
        try {
            String chatApiUrl = ollamaBaseUrl + ollamaEndPoint;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(chatApiUrl))
                    .header("Accept", "application/x-ndjson")
                    .header("Content-Type", MediaType.APPLICATION_JSON.toString())
                    .header("User-Agent", "Career-Coach-API/1.0")
                    .timeout(Duration.ofSeconds(readTimeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            log.info("Ollama API 스트리밍 호출 시작 - 프롬프트 길이: {}", prompt.length());
            OllamaLineSubscriber subscriber = new OllamaLineSubscriber(objectMapper, handler);
            CompletableFuture<Void> future =  httpClient.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.fromLineSubscriber(subscriber)
            ).thenAccept(
                    response -> {
                        if (response.statusCode() != HttpStatus.OK.value()) {
                            log.error("Ollama Chat API 호출 실패: {}", response.statusCode());
                            handler.onError(new RuntimeException("HTTP Status " + response.statusCode()));
                        }
                    });
            return new OllamaResponseHandler(future, subscriber);
        } catch (Exception e) {
            handler.onError(e);
            return new OllamaResponseHandler(CompletableFuture.failedFuture(e), null);
        }
    }

    private String createChatRequestBodyJson(
            String systemPrompt,
            String prompt,
            Map<String, Object> options,
            boolean streaming
    ) throws JsonProcessingException {
        Map<String, Object> requestBody = Map.of(
                "model", ollamaModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", prompt)
                ),
                "stream", streaming,
                "options", options
        );
        return objectMapper.writeValueAsString(requestBody);
    }

    private String createChatRequestBodyJson(
            String prompt,
            Map<String, Object> options,
            boolean streaming
    ) throws JsonProcessingException {
        Map<String, Object> requestBody = Map.of(
                "model", ollamaModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "stream", streaming,
                "options", options
        );
        return objectMapper.writeValueAsString(requestBody);
    }

    /**
     * 창의적 작업용 옵션 (높은 temperature)
     */
    public Map<String, Object> createCreativeOptions() {
        return Map.of(
                "temperature", 0.8,
                "top_p", 0.9,
                "num_predict", 2000,
                "repeat_penalty", 1.1
        );
    }

    public Map<String, Object> createAnalyticalOptions() {
        return Map.of(
                "temperature", 0.3,
                "top_p", 0.8,
                "num_predict", 2000,
                "repeat_penalty", 1.0
        );
    }

    /**
     * JSON 응답에서 JSON 부분만 추출하는 유틸리티 메서드
     */
    public String extractJsonFromResponse(String response) {
        // JSON 시작과 끝을 찾아서 추출
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");

        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1);
        }

        // JSON을 찾지 못한 경우 전체 응답 반환
        return response;
    }
}