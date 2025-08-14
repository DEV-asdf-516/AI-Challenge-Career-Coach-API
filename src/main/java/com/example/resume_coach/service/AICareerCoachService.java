package com.example.resume_coach.service;

import com.example.resume_coach.component.DataInitializer;
import com.example.resume_coach.component.OllamaApiClient;
import com.example.resume_coach.handler.StreamHandler;
import com.example.resume_coach.model.ResumeDto;
import com.example.resume_coach.repository.entity.Resume;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AICareerCoachService {

    private final DataInitializer dataInitializer;
    private final OllamaApiClient ollamaApiClient;
    private final ObjectMapper objectMapper;

    public CompletableFuture<Void> generateMockInterviewQuestionsStream(
            Resume resume,
            StreamHandler handler
    ) {
        log.info("개인 맞춤형 면접 질문 생성 시작: 이력서 ID = {}", resume.getId());
        String userPrompt = formatUserPrompt(dataInitializer.getInterviewUserPrompt(), resume);
        return ollamaApiClient.callOllamaStream(
                dataInitializer.getInterviewSystemPrompt(),
                userPrompt,
                ollamaApiClient.createCreativeOptions(),
                handler
        );
    }


    public CompletableFuture<Void> generateLearningPathStream(
            Resume resume,
            StreamHandler handler) {
        log.info("개인 맞춤형 학습 경로 생성 시작: 이력서 ID = {}", resume.getId());
        String userPrompt = formatUserPrompt(dataInitializer.getLearningUserPrompt(), resume);
        return ollamaApiClient.callOllamaStream(
                dataInitializer.getLearningSystemPrompt(),
                userPrompt,
                ollamaApiClient.createAnalyticalOptions(),
                handler
        );
    }

    public ResumeDto.MockInterviewResponse parseInterviewResponse(String aiResponse, String resumeId) {
        String jsonPart = ollamaApiClient.extractJsonFromResponse(aiResponse);
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonPart);

            List<ResumeDto.MockInterviewResponse.InterviewQuestion> questions = new ArrayList<>();
            JsonNode questionsArray = jsonNode.get("questions");

            if (questionsArray == null || !questionsArray.isArray()) {
                log.warn("면접 질문이 없거나 잘못된 형식입니다. AI 응답: {}", aiResponse);
                return createInterviewFallbackResponse(resumeId, jsonPart);
            }

            for (JsonNode questionNode : questionsArray) {
                ResumeDto.MockInterviewResponse.InterviewQuestion question =
                        new ResumeDto.MockInterviewResponse.InterviewQuestion();

                question.setQuestion(questionNode.get("question").asText());
                question.setCategory(questionNode.get("category").asText());
                question.setExpectedAnswerDirection(questionNode.get("expectedAnswerDirection").asText());
                question.setDifficulty(questionNode.get("difficulty").asText());

                if (questionNode.has("personalizationReason")) {
                    String enhanced = question.getExpectedAnswerDirection() +
                            " [개인화 근거: " + questionNode.get("personalizationReason").asText() + "]";
                    question.setExpectedAnswerDirection(enhanced);
                }

                questions.add(question);
            }

            String difficulty = jsonNode.get("overallDifficulty").asText();
            String focusArea = jsonNode.get("focusArea").asText();

            if (jsonNode.has("interviewStrategy")) {
                focusArea += " [전략: " + jsonNode.get("interviewStrategy").asText() + "]";
            }

            return new ResumeDto.MockInterviewResponse(
                    resumeId,
                    questions,
                    difficulty,
                    focusArea,
                    ""
            );

        } catch (Exception e) {
            log.error("면접 질문 응답 파싱 실패", e);
            return createInterviewFallbackResponse(resumeId, jsonPart);
        }
    }


    public ResumeDto.LearningPathResponse parseLearningPathResponse(String aiResponse, String resumeId) {
        String jsonPart = ollamaApiClient.extractJsonFromResponse(aiResponse);
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonPart);

            List<ResumeDto.LearningPathResponse.LearningStep> learningSteps = new ArrayList<>();
            JsonNode stepsArray = jsonNode.get("learningSteps");

            if (stepsArray == null || !stepsArray.isArray()) {
                log.warn("학습 단계가 없거나 잘못된 형식입니다. AI 응답: {}", aiResponse);
                return createLearningPathFallbackResponse(resumeId, jsonPart);
            }

            for (JsonNode stepNode : stepsArray) {
                ResumeDto.LearningPathResponse.LearningStep step =
                        new ResumeDto.LearningPathResponse.LearningStep();

                step.setTitle(stepNode.get("title").asText());

                // 개인화 근거를 설명에 통합
                String description = stepNode.get("description").asText();
                if (stepNode.has("personalizationReason")) {
                    description += "\n\n💡 개인 맞춤 포인트: " + stepNode.get("personalizationReason").asText();
                }
                step.setDescription(description);

                step.setCategory(stepNode.get("category").asText());
                step.setPriority(stepNode.get("priority").asInt());
                step.setEstimatedDuration(stepNode.get("estimatedDuration").asText());

                // 리소스 파싱
                List<String> resources = new ArrayList<>();
                JsonNode resourcesArray = stepNode.get("resources");
                if (resourcesArray != null && resourcesArray.isArray()) {
                    for (JsonNode resource : resourcesArray) {
                        resources.add(resource.asText());
                    }
                }
                step.setResources(resources);

                learningSteps.add(step);
            }

            String currentLevel = jsonNode.get("currentLevel").asText();
            String targetLevel = jsonNode.get("targetLevel").asText();
            String timeframe = jsonNode.get("estimatedTimeframe").asText();

            if (jsonNode.has("learningStrategy")) {
                timeframe += " [전략: " + jsonNode.get("learningStrategy").asText() + "]";
            }

            return new ResumeDto.LearningPathResponse(
                    resumeId,
                    currentLevel,
                    targetLevel,
                    learningSteps,
                    timeframe,
                    ""
            );

        } catch (Exception e) {
            log.error("학습 경로 응답 파싱 실패", e);
            return createLearningPathFallbackResponse(resumeId, jsonPart);
        }
    }

    private String formatUserPrompt(String template, Resume resume) {
        return template
                .replace("${industry}", resume.getIndustry())
                .replace("${desiredPosition}", resume.getDesiredPosition())
                .replace("${yearsOfExperience}", String.valueOf(resume.getYearsOfExperience()))
                .replace("${careerSummary}", resume.getCareerSummary())
                .replace("${jobExperience}", resume.getJobExperience())
                .replace("${skills}", resume.getSkills());
    }

    private ResumeDto.MockInterviewResponse createInterviewFallbackResponse(String resumeId, String result) {
        List<ResumeDto.MockInterviewResponse.InterviewQuestion> questions = List.of(
                new ResumeDto.MockInterviewResponse.InterviewQuestion(
                        "AI 서비스 장애가 발생하였습니다.",
                        "서비스장애",
                        "잠시 후 다시 시도해주세요.",
                        "알림"
                )
        );

        return new ResumeDto.MockInterviewResponse(
                resumeId,
                questions,
                "서비스 장애",
                "AI 서비스 일시 중단",
                result
        );
    }

    private ResumeDto.LearningPathResponse createLearningPathFallbackResponse(String resumeId, String result) {
        List<ResumeDto.LearningPathResponse.LearningStep> steps = List.of(
                new ResumeDto.LearningPathResponse.LearningStep(
                        "AI 서비스 장애",
                        "AI 서비스 장애가 발생하였습니다.\n잠시 후 다시 시도해주세요.",
                        "서비스장애",
                        1,
                        "잠시 후",
                        List.of("잠시 후 다시 시도")
                )
        );

        return new ResumeDto.LearningPathResponse(
                resumeId,
                "서비스 장애",
                "정상 서비스",
                steps,
                "잠시 후",
                result
        );
    }
}