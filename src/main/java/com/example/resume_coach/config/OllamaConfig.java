package com.example.resume_coach.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;


@Configuration
public class OllamaConfig {

    @Getter
    @Value("${ollama.model")
    private String ollamaModel;
    @Value("${ollama.api.timeout.connect:30}")
    private int connectTimeoutSeconds;

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "ollama", matchIfMissing = true)
    public HttpClient ollamaHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
}