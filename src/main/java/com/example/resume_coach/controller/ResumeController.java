package com.example.resume_coach.controller;


import com.example.resume_coach.component.SSEHeartBeatManager;
import com.example.resume_coach.model.ResumeDto;
import com.example.resume_coach.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Resume Coach API", description = "AI 기반 개인 맞춤형 커리어 코치 API")
public class ResumeController {

    private final ResumeService resumeService;
    private final SSEHeartBeatManager sseHeartbeatManager;
    /**
     * 이력서 핵심 정보 입력 API
     */
    @Operation(summary = "이력서 생성", description = "사용자의 경력, 직무 경험, 기술 스킬 등 핵심 정보를 입력받아 이력서를 생성합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "이력서 생성 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResumeDto.Response.class))), @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(mediaType = "application/json"))})
    @PostMapping("/resumes")
    public ResponseEntity<ResumeDto.Response> createResume(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "이력서 생성 요청 정보",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResumeDto.CreateRequest.class),
                            examples = @ExampleObject(name = "이력서 생성 예시", value = """
            {
              "careerSummary": "3년차 백엔드 개발자, Spring Boot/MSA 기반 커머스 서비스 개발, AWS EC2 운영 경험",
              "jobExperience": "대규모 이커머스 플랫폼 백엔드 개발, 마이크로서비스 아키텍처 설계 및 구현",
              "skills": "Java, Spring Boot, MySQL, AWS, Docker, Kubernetes",
              "desiredPosition": "B2B 플랫폼 백엔드 개발자",
              "yearsOfExperience": 3,
              "industry": "IT/소프트웨어"
            }
            """)))
            @Valid @RequestBody ResumeDto.CreateRequest request
    )
    {
        log.info("이력서 생성 API 호출: {}", request.getCareerSummary());

        try {
            ResumeDto.Response response = resumeService.createResume(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("이력서 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 이력서 조회 API
     */
    @Operation(summary = "이력서 조회")
    @GetMapping("/resumes/{id}")
    public ResponseEntity<ResumeDto.Response> getResume(@PathVariable String id) {
        log.info("이력서 조회 API 호출: ID = {}", id);
        return resumeService.getResume(id).map(resume -> ResponseEntity.ok(resume)).orElse(ResponseEntity.notFound().build());
    }


    /**
     * 이력서 삭제 API
     */
    @Operation(summary = "이력서 삭제")
    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable String id) {
        log.info("이력서 삭제 API 호출: ID = {}", id);
        try {
            resumeService.deleteResume(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("이력서 삭제 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("이력서 삭제 중 예기치 않은 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "이력서 수정")
    @PutMapping("/resumes/{id}")
    public ResponseEntity<ResumeDto.Response> updateResume(
            @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "이력서 수정 요청 정보",
                    required = true,
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResumeDto.CreateRequest.class)))
            @Valid @RequestBody ResumeDto.CreateRequest request)
    {
        log.info("이력서 수정 API 호출: ID = {}, 요청 내용 = {}", id, request.getCareerSummary());
        try {
            ResumeDto.Response updatedResume = resumeService.updateResume(id, request);
            return ResponseEntity.ok(updatedResume);
        } catch (RuntimeException e) {
            log.error("이력서 수정 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("이력서 수정 중 예기치 않은 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "맞춤 면접 질문 생성 ⭐", description = "입력된 이력서 정보를 바탕으로 AI가 개인 맞춤형 면접 모의질문 5개를 생성합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "면접 질문 생성 성공",
            content = @Content(mediaType = "text/event-stream;charset=UTF-8",
                    schema = @Schema(implementation = ResumeDto.MockInterviewResponse.class),
                    examples = @ExampleObject(name = "면접 질문 생성 결과", value = """
                    {
                      "resumeId": "d0ae03af-789e-4ad9-bd2d-956c8077349e",
                      "questions": [
                        {
                          "question": "Spring Boot에서 마이크로서비스 간 통신을 구현할 때 어떤 방식을 선택하고, 그 이유는 무엇인가요?",
                          "category": "기술면접",
                          "expectedAnswerDirection": "HTTP/REST, gRPC, 메시지 큐 등의 방식과 각각의 장단점 설명",
                          "difficulty": "중급"
                        }
                      ],
                      "difficulty": "중급",
                      "focusArea": "백엔드 아키텍처 및 클라우드 운영"
                    }
            """))),
            @ApiResponse(responseCode = "404", description = "이력서를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "AI 서비스 오류")})
    @GetMapping(path = "/resumes/{id}/mock-interview", produces =  "text/event-stream;charset=UTF-8")
    public SseEmitter streamMockInterview(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean deltas) throws IOException {
        SseEmitter emitter = new SseEmitter();
        emitter.send(SseEmitter.event().name("keepalive").comment("start"));
        sseHeartbeatManager.register(emitter);
        resumeService.generateMockInterviewStream(id, deltas, emitter, sseHeartbeatManager);
        return emitter;
    }

    @Operation(summary = "개인 맞춤형 학습 경로 추천 ⭐", description = "이력서 정보를 분석하여 구직자의 합격률을 높이고 역량을 강화할 수 있는 " + "개인 맞춤형 학습 경로를 단계별로 제안합니다. 기술 스택 심화, 프로젝트 경험, " + "커뮤니케이션 스킬 강화 등 구체적인 방안을 포함합니다.")
    @ApiResponses(value = {@ApiResponse(
            responseCode = "200",
            description = "학습 경로 생성 성공",
            content = @Content(mediaType ="text/event-stream;charset=UTF-8",
                    schema = @Schema(implementation = ResumeDto.LearningPathResponse.class),
                    examples = @ExampleObject(name = "학습 경로 추천 결과", value = """
                    {
                      "resumeId": "d0ae03af-789e-4ad9-bd2d-956c8077349e",
                      "currentLevel": "중급 백엔드 개발자",
                      "targetLevel": "고급 백엔드 개발자",
                      "learningSteps": [
                        {
                          "title": "분산 시스템 설계 심화",
                          "description": "대규모 트래픽 처리를 위한 시스템 아키텍처 설계 능력 강화",
                          "category": "기술역량",
                          "priority": 1,
                          "estimatedDuration": "3-4개월",
                          "resources": ["Designing Data-Intensive Applications 서적", "AWS Solutions Architect 자격증"]
                        }
                      ],
                      "estimatedTimeframe": "8-12개월"
                    }
            """))),
            @ApiResponse(responseCode = "404", description = "이력서를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "AI 서비스 오류")})
    @GetMapping(path = "/resumes/{id}/learning-path", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamLearningPath(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean deltas) throws IOException {
        SseEmitter emitter = new SseEmitter();
        emitter.send(SseEmitter.event().name("keepalive").comment("start"));
        sseHeartbeatManager.register(emitter);
        resumeService.generateLearningPathStream(id, deltas, emitter, sseHeartbeatManager);
        return emitter;
    }
}