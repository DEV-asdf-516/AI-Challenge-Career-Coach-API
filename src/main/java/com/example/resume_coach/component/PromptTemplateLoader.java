package com.example.resume_coach.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Slf4j
public class PromptTemplateLoader {

    public String loadSystemPrompt(String promptName) {
        return loadResourceFile("prompts/system/" + promptName + ".txt");
    }


    public String loadUserPromptTemplate(String promptName) {
        return loadResourceFile("prompts/user/" + promptName + ".txt");
    }


    private String loadResourceFile(String resourcePath) {
        try {
            Resource resource = new ClassPathResource(resourcePath);
            return Files.readString(Paths.get(resource.getURI()));
        } catch (IOException e) {
            log.error("프롬프트 파일 로드 실패: {}", resourcePath, e);
            throw new RuntimeException("프롬프트 파일을 찾을 수 없습니다: " + resourcePath);
        }
    }
}
