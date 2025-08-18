# 🎯 AI Challenge - 커리어 코치 API

> **[2025 AI Challenge Back-End 개발 과제](https://xg18kywe.ninehire.site/job_posting/HdJL2ScC)** 참여 작품  
> 생성형 AI를 활용한 개인 맞춤형 커리어 코치 챗봇 API

## 📝 프로젝트 개요

구직자의 이력서 정보(경력, 직무, 기술 스킬)를 바탕으로 **생성형 AI**가 개인 맞춤형 면접 모의질문을 생성하고, 자기계발 학습 경로를 제안하여 구직자의 합격률 향상을 돕는 실시간 스트리밍 백엔드 API입니다.

### 🌟 핵심 특징
- **실시간 스트리밍**: SSE(Server-Sent Events) 기반 실시간 AI 응답 전송
- **전 직업군 대응**: 잡코리아 직무 카테고리 내 모든 직업군 지원
- **개인 맞춤형**: 사용자의 경력 수준과 목표 직무에 최적화된 질문과 학습 경로 제공
- **실무 중심**: 실제 면접에서 활용 가능한 심화 질문과 구체적인 학습 방안 제시
- **완전 무료**: API Key 없이 누구나 사용 가능한 완벽 무료 서비스

## 🛠️ 기술 스택

- **Backend**: Java 17, Spring Boot 3.x
- **AI Engine**: Ollama (Local LLM) - 무료
- **Database**: H2 Database (In-Memory)
- **Streaming**: Server-Sent Events (SSE)
- **Documentation**: Swagger/OpenAPI 3
- **Containerization**: Docker, Docker Compose

## 🎯 주요 기능

### 1. 이력서 핵심 정보 관리 API
```
POST   /api/resumes        # 이력서 생성 ⭐
GET    /api/resumes/{id}   # 이력서 조회
PUT    /api/resumes/{id}   # 이력서 수정
DELETE /api/resumes/{id}   # 이력서 삭제
```
- 경력 요약, 직무 경험, 기술 스킬 등 이력서 핵심 정보 입력
- 희망 직무, 경력 년수, 업계 정보 포함

### 2. 맞춤 면접 모의질문 생성 ⭐
```
GET /api/resumes/{id}/mock-interview
Content-Type: text/event-stream
```
- 입력된 이력서 정보 기반 **개인 맞춤형 면접 질문 5개** 생성
- 질문 카테고리: 기술면접, 경험면접, 문제해결, 시스템설계, 최신기술, 상황대응
- 난이도 조절 및 기대 답변 방향 제시
- `deltas=true`: 토큰 단위 실시간 스트리밍
- `deltas=false`: 완성된 결과만 전송

### 3. 개인 맞춤형 학습 경로 추천 ⭐
```
GET /api/resumes/{id}/learning-path
Content-Type: text/event-stream
```
- 구직자 역량 분석 후 **단계별 학습 경로** 제안
- 기술역량, 프로젝트경험, 소프트스킬, 자격증, 네트워킹 카테고리
- 구체적인 학습 리소스 및 예상 소요 기간 포함
- `deltas=true`: 토큰 단위 실시간 스트리밍
- `deltas=false`: 완성된 결과만 전송

## 🔧 핵심 컴포넌트

### SSE 스트리밍 아키텍처
- **SseEmitter**: Spring의 Server-Sent Events 지원
- **StreamHandler**: 스트리밍 이벤트 처리 인터페이스
- **OllamaLineSubscriber**: Flow.Subscriber 기반 실시간 응답 처리

### AI Career Coach Service
- **프롬프트 엔지니어링**: 직무별 맞춤형 시스템 프롬프트 설계
- **JSON 응답 파싱**: 구조화된 AI 응답 처리
- **Fallback 메커니즘**: AI 서비스 장애 시 대체 응답 제공
- **실시간 스트리밍**: SSE를 통한 실시간 AI 응답 전송

### Ollama API Client
- **스트리밍 처리**: HttpClient.BodyHandlers.fromLineSubscriber 활용
- **비동기 처리**: CompletableFuture 기반 비동기 AI 호출
- **옵션 최적화**: 창의적/분석적 작업별 파라미터 조정
- **타임아웃 관리**: 연결 및 읽기 타임아웃 설정

## 🎨 프롬프트 엔지니어링 전략

### 1. 역할 기반 프롬프트 설계
```
당신은 베테랑 면접관이자 HR 전문가다.

## 당신의 역할
- 다양한 업계/직무의 베테랑 면접관
- 지원자별 맞춤형 면접 질문 설계 전문가
- 실무 중심 기술 평가 및 시나리오 작성 능력 보유
- 경력 수준별(신입/주니어/미들/시니어/리드) 난이도 조정 능력
```

### 2. 개인화 전략
- 경력 수준별 난이도 조정 (신입 ↔ 시니어)
- 직무별 특화 질문 (개발자, 디자이너, 마케터, 간호사 등)
- 부족한 영역 파악 및 성장 가능성 평가

### 3. 구조화된 JSON 응답
```json
{
  "questions": [
    {
      "question": "구체적 면접 질문",
      "category": "기술면접|경험면접|문제해결|시스템설계|최신기술|상황대응",
      "expectedAnswerDirection": "기대 답변 방향",
      "difficulty": "기초|초급|중급|고급|전문가",
      "personalizationReason": "이 질문이 지원자에게 특화된 이유"
    }
  ],
  "overallDifficulty": "기초|초급|중급|고급|전문가",
  "focusArea": "핵심 평가 영역",
  "interviewStrategy": "면접 전략"
}
```
## 🎯 AI Challenge 평가 포인트 대응

### 1. 생성형 AI 활용의 창의성 및 정교함 ✅
- **다단계 프롬프트 설계**: 시스템 프롬프트 + 사용자 프롬프트 조합
- **실시간 스트리밍**: SSE를 통한 AI 생성 과정 실시간 가시화
- **개인화 알고리즘**: 경력, 직무, 스킬에 따른 차별화된 질문 생성
- **프롬프트 보안**: 인젝션 공격 방지 메커니즘 구현

### 2. 백엔드 아키텍처 및 구현 ✅
- **확장 가능한 설계**: 모듈화된 컴포넌트 구조
- **비동기 스트리밍**: SSE + CompletableFuture를 활용한 고성능 처리
- **에러 핸들링**: Fallback 메커니즘 / 장애 대응 로직

### 3. 기능의 유용성 및 실용성 ✅
- **실무 중심 질문**: 실제 면접에서 활용 가능한 심화 질문
- **구체적 학습 경로**: 실행 가능한 리소스와 예상 기간 제시
- **전 직업군 지원**: 잡코리아 내 전 직무 분야 대응
- **실시간 UX**: 스트리밍을 통한 즉각적인 사용자 피드백

## 🏆 프로젝트 하이라이트

1. **🔄 실시간 스트리밍**: SSE 기반 AI 응답 실시간 전송
2. **📚 체계적인 직무 분류**: 27개 카테고리, 300+ 직업 데이터베이스
3. **🎯 개인화 엔진**: 경력 수준과 목표에 따른 맞춤형 추천
4. **⚡ 고성능 AI 통합**: 비동기 스트리밍 기반 실시간 응답 처리
5. **🔒 안전한 프롬프트**: 인젝션 방지 및 보안 메커니즘
6. **📖 완벽한 문서화**: Swagger 기반 API 문서 자동 생성

---

## 🚀 실행 방법

### 🐳 Docker를 이용한 간편 실행
Docker Compose를 사용하면 Ollama와 애플리케이션을 한 번에 실행할 수 있습니다.

```bash
# 1. 프로젝트 클론
git clone https://github.com/DEV-asdf-516/AI-Challenge-Career-Coach-API.git
cd resume-coach-api

# 2. 환경 설정 파일 생성
cat > .env << EOF
PORT=9070

DB_NAME=resume
DB_USERNAME=admin
DB_PASSWORD=1234

OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_MODEL=benedict/linkbricks-llama3.1-korean:8b

# GPU가 없는 환경이라면 아래 두 모델을 추천합니다.
# 별도의 설정 없이 곧바로 확인할 수 있습니다. 모델 성능이 조금 떨어질 수 있습니다.
# OLLAMA_MODEL=llama3.2:3b

# 다음 모델 사용 시 HF_TOKEN 환경변수 설정이 필요합니다. 
# https://huggingface.co/settings/tokens 에 접속해 토큰을 발급 받으세요. 
# OLLAMA_MODEL=llama3-instruct-kor-8b-q4km  
# HF_TOKEN=<your_huggingface_token_here>

SWAGGER_SERVER_URL=http://localhost
EOF

# 3. Docker Compose로 전체 서비스 실행
docker-compose up -d

# 4. 모델 다운로드 완료 확인 (첫 다운로드 시 5-10분의 시간이 소요됩니다.)
docker-compose logs -f model-setup

# 5. API 서비스 확인
curl -N -H "Accept: text/event-stream" http://localhost:9070/api/ollama/greeting
```

**서비스 URL:**
- API 문서 (Swagger): http://localhost:9070/
- H2 Database Console: http://localhost:9070/h2-console
