package com.example.resume_coach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

public class ResumeDto {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "이력서 생성 요청 DTO")
    public static class CreateRequest {
        
        @Schema(description = "경력 요약", 
                example = "3년차 백엔드 개발자, Spring Boot/MSA/Python 기반 커머스 서비스 개발, AWS EC2 운영 경험",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "경력 요약은 필수입니다")
        private String careerSummary;
        
        @Schema(description = "직무 경험", 
                example = "대규모 이커머스 플랫폼 백엔드 개발, 마이크로서비스 아키텍처 설계 및 구현, RESTful API 개발",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "직무 경험은 필수입니다")
        private String jobExperience;
        
        @Schema(description = "업무 스킬",
                example = "Java, Spring Boot, Spring Data JPA, MySQL, PostgreSQL, Redis, AWS EC2/RDS/S3, Docker, Kubernetes",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "업무 스킬은 필수입니다")
        private String skills;
        
        @Schema(description = "희망 직무", example = "시니어 백엔드 개발자")
        private String desiredPosition;
        
        @Schema(description = "경력 년수", example = "3", minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "경력 년수는 필수입니다")
        @Min(value = 0, message = "경력 년수는 0년 이상이어야 합니다")
        private Integer yearsOfExperience;
        
        @Schema(description = "업계", example = "IT/소프트웨어")
        @NotNull(message = "경력 년수는 필수입니다")
        private String industry;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "이력서 응답 DTO")
    public static class Response {
        @Schema(description = "이력서 ID", example = "abc123-def456-ghi789")
        private String id;

        @Schema(description = "경력 요약")
        private String careerSummary;

        @Schema(description = "직무 경험")
        private String jobExperience;

        @Schema(description = "업무 스킬")
        private String skills;

        @Schema(description = "희망 직무")
        private String desiredPosition;

        @Schema(description = "경력 년수")
        private Integer yearsOfExperience;

        @Schema(description = "업계")
        private String industry;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "맞춤 면접 질문 응답 DTO")
    public static class MockInterviewResponse {
        @Schema(description = "이력서 ID", example = "abc123-def456-ghi789")
        private String resumeId;

        @Schema(description = "면접 질문 목록")
        private List<InterviewQuestion> questions;

        @Schema(description = "전체 난이도", example = "중급")
        private String difficulty;

        @Schema(description = "주요 포커스 영역", example = "백엔드 기술스택")
        private String focusArea;

        private String errorMessage;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "면접 질문 DTO")
        public static class InterviewQuestion {
            @Schema(description = "면접 질문",
                    example = "Spring Boot에서 마이크로서비스 간 통신을 구현할 때 어떤 방식을 선택하고, 그 이유는 무엇인가요?")
            private String question;

            @Schema(description = "질문 카테고리",
                    example = "기술면접",
                    allowableValues = {"기술면접", "경험면접", "문제해결", "시스템설계", "최신기술"})
            private String category;

            @Schema(description = "기대하는 답변 방향",
                    example = "HTTP/REST, gRPC, 메시지 큐 등의 방식과 각각의 장단점 설명")
            private String expectedAnswerDirection;

            @Schema(description = "질문 난이도",
                    example = "중급",
                    allowableValues = {"초급", "중급", "고급"})
            private String difficulty;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "학습 경로 추천 응답 DTO")
    public static class LearningPathResponse {
        @Schema(description = "이력서 ID", example = "abc123-def456-ghi789")
        private String resumeId;
        
        @Schema(description = "현재 수준", example = "중급 백엔드 개발자")
        private String currentLevel;
        
        @Schema(description = "목표 수준", example = "시니어 백엔드 개발자")
        private String targetLevel;
        
        @Schema(description = "학습 단계 목록")
        private List<LearningStep> learningSteps;
        
        @Schema(description = "전체 예상 기간", example = "8-12개월")
        private String estimatedTimeframe;

        private String errorMessage;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "학습 단계 DTO")
        public static class LearningStep {
            @Schema(description = "학습 단계 제목", example = "분산 시스템 설계 심화")
            private String title;
            
            @Schema(description = "학습 내용 설명", 
                    example = "대규모 트래픽 처리를 위한 시스템 아키텍처 설계 능력 강화")
            private String description;
            
            @Schema(description = "학습 카테고리", 
                    example = "기술역량",
                    allowableValues = {"기술역량", "프로젝트경험", "소프트스킬", "자격증", "네트워킹"})
            private String category;
            
            @Schema(description = "우선순위", example = "1", minimum = "1")
            private Integer priority;
            
            @Schema(description = "예상 소요 기간", example = "3-4개월")
            private String estimatedDuration;
            
            @Schema(description = "학습 리소스 목록", 
                    example = "[\"Designing Data-Intensive Applications 서적\", \"AWS Solutions Architect 자격증 준비\"]")
            private List<String> resources;
        }
    }
}