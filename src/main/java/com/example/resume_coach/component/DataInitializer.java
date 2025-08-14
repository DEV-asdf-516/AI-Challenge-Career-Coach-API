package com.example.resume_coach.component;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final PromptTemplateLoader promptTemplateLoader;

    @Getter
    private String interviewSystemPrompt;
    @Getter
    private String learningSystemPrompt;
    @Getter
    private String interviewUserPrompt;
    @Getter
    private String learningUserPrompt;

    @Override
    public void run(String... args) throws Exception {
        log.info("직무 데이터 확인...");
        try {
            Map<String, List<String>> jsonData = loadJobsFromJsonFile();

            String prettyJobDataJson = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonData);

            interviewSystemPrompt = promptTemplateLoader.loadSystemPrompt("interview_system_v1").replace("${jobs}", prettyJobDataJson);
            learningSystemPrompt = promptTemplateLoader.loadSystemPrompt("learning_system_v1").replace("${jobs}", prettyJobDataJson);
            interviewUserPrompt = promptTemplateLoader.loadUserPromptTemplate("interview_user_v1");
            learningUserPrompt = promptTemplateLoader.loadUserPromptTemplate("learning_user_v1");

            log.info(interviewSystemPrompt);
            log.info(learningSystemPrompt);

        } catch (Exception e) {
            throw e;
        }
    }


    private Map<String, List<String>> loadJobsFromJsonFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("jobs.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, List<String>>>() {
                    }
            );
        }
    }
}
