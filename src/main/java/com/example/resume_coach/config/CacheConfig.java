package com.example.resume_coach.config;


import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(
                "interviewQuestions",    // 인터뷰 캐싱
                "learningPaths",         // 학습 경로 캐시
                "jobCategories",         // 직무 카테고리 캐시
                "promptTemplates"       // 프롬프트 템플릿 캐시
        ));
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }

    @Bean("resumeContentKeyGenerator")
    public KeyGenerator resumeContentKeyGenerator() {
        return (target, method, params) -> {
            if (params.length > 0 && params[0] != null) {
                String content = params[0].toString();
                return generateContentHash(content);
            }
            return "default";
        };
    }

    @Bean("promptKeyGenerator")
    public KeyGenerator promptKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder keyBuilder = new StringBuilder();
            for (Object param : params) {
                if (param != null) {
                    keyBuilder.append(param.toString().hashCode()).append("_");
                }
            }
            return keyBuilder.toString();
        };
    }

    private String generateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(content.hashCode());
        }
    }

}
