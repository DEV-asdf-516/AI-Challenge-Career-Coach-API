package com.example.resume_coach.service;

import com.example.resume_coach.component.SSEHeartBeatManager;
import com.example.resume_coach.handler.OllamaResponseHandler;
import com.example.resume_coach.handler.SSEOllamaStreamHandler;
import com.example.resume_coach.handler.StreamHandler;
import com.example.resume_coach.model.ResumeDto;
import com.example.resume_coach.repository.ResumeRepository;
import com.example.resume_coach.repository.entity.Resume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final AICareerCoachService aiService;

    public ResumeDto.Response createResume(ResumeDto.CreateRequest request) {
        log.info("새로운 이력서 생성 요청: {}", request.getCareerSummary());

        Resume resume = new Resume();
        resume.setCareerSummary(request.getCareerSummary());
        resume.setJobExperience(request.getJobExperience());
        resume.setSkills(request.getSkills());
        resume.setDesiredPosition(request.getDesiredPosition());
        resume.setYearsOfExperience(request.getYearsOfExperience());
        resume.setIndustry(request.getIndustry());

        Resume savedResume = resumeRepository.save(resume);
        log.info("이력서 생성 완료: ID = {}", savedResume.getId());

        return convertToResponse(savedResume);
    }

    @Transactional(readOnly = true)
    public Optional<ResumeDto.Response> getResume(String id) {
        log.info("이력서 조회 요청: ID = {}", id);

        return resumeRepository.findById(id)
                .map(this::convertToResponse);
    }


    public ResumeDto.Response updateResume(String id, ResumeDto.CreateRequest request) {
        log.info("이력서 수정 요청: ID = {}", id);

        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다: " + id));

        resume.setCareerSummary(request.getCareerSummary());
        resume.setJobExperience(request.getJobExperience());
        resume.setSkills(request.getSkills());
        resume.setDesiredPosition(request.getDesiredPosition());
        resume.setYearsOfExperience(request.getYearsOfExperience());
        resume.setIndustry(request.getIndustry());

        Resume updatedResume = resumeRepository.save(resume);
        log.info("이력서 수정 완료: ID = {}", updatedResume.getId());

        return convertToResponse(updatedResume);
    }

    public void deleteResume(String id) {
        log.info("이력서 삭제 요청: ID = {}", id);

        if (!resumeRepository.existsById(id)) {
            throw new RuntimeException("이력서를 찾을 수 없습니다: " + id);
        }

        resumeRepository.deleteById(id);
        log.info("이력서 삭제 완료: ID = {}", id);
    }


    @Async("aiServiceExecutor")
    public void generateMockInterviewStream(String resumeId, boolean emitDeltas, SseEmitter emitter, SSEHeartBeatManager heartBeat) throws IOException {
        log.info("🎯 개인 맞춤형 면접 질문 생성 요청 : 이력서 ID = {}", resumeId);
        try {
            Resume resume = resumeRepository.findById(resumeId).orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다: " + resumeId));
            Function<String, ResumeDto.MockInterviewResponse> parser = full -> aiService.parseInterviewResponse(full, resumeId);
            StreamHandler handler = new SSEOllamaStreamHandler<>(emitter, parser, emitDeltas, heartBeat);
            OllamaResponseHandler responseHandler = aiService.generateMockInterviewQuestionsStream(resume, handler);
            emitter.onTimeout(() -> {
                responseHandler.cancel();
                emitter.completeWithError(new java.util.concurrent.TimeoutException("SSE timeout"));
            });
            emitter.onError(e -> {
                responseHandler.cancel();
            });
        } catch (Exception e) {
            log.error("면접 질문 생성 중 오류 발생", e);
            emitter.send(SseEmitter.event().name("error").data("면접 질문 생성 중 오류: " + e.getMessage()));
            emitter.completeWithError(e);
        }
    }


    @Async("aiServiceExecutor")
    public void generateLearningPathStream(String resumeId, boolean emitDeltas, SseEmitter emitter, SSEHeartBeatManager heartBeat) throws IOException {
        log.info("🎯 개인 맞춤형 학습 경로 생성 요청: 이력서 ID = {}", resumeId);
        try {
            Resume resume = resumeRepository.findById(resumeId).orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다: " + resumeId));
            Function<String, ResumeDto.LearningPathResponse> parser = full -> aiService.parseLearningPathResponse(full, resumeId);
            StreamHandler handler = new SSEOllamaStreamHandler<>(emitter, parser, emitDeltas, heartBeat);
            OllamaResponseHandler responseHandler = aiService.generateLearningPathStream(resume, handler);
            emitter.onTimeout(() -> {
                responseHandler.cancel();
                emitter.completeWithError(new java.util.concurrent.TimeoutException("SSE timeout"));
            });
            emitter.onError(e -> {
                responseHandler.cancel();
            });
        } catch (Exception e) {
            log.error("맞춤형 학습 경로 생성 중 오류 발생", e);
            emitter.send(SseEmitter.event().name("error").data("면접 질문 생성 중 오류: " + e.getMessage()));
            emitter.completeWithError(e);
        }
    }

    private ResumeDto.Response convertToResponse(Resume resume) {
        return new ResumeDto.Response(
                resume.getId(),
                resume.getCareerSummary(),
                resume.getJobExperience(),
                resume.getSkills(),
                resume.getDesiredPosition(),
                resume.getYearsOfExperience(),
                resume.getIndustry()
        );
    }


}