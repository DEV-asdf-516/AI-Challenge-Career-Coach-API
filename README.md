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
- **서비스 컨테이너 제공**: Docker compose를 제공하여 개발 환경 설정 불필요

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
## 📄 응답 예제

> 시스템 및 사용자 프롬프트 v1, qwen2.5:7b-instruct 모델을 사용한 면접 질문 생성 예시

### 0. 사용자 이력서 생성
- 요청 JSON
```json
{
  "careerSummary": "3년차 백엔드 개발자, Spring Boot/MSA 기반 커머스 서비스 개발, AWS EC2 운영 경험",
  "jobExperience": "대규모 이커머스 플랫폼 백엔드 개발, 마이크로서비스 아키텍처 설계 및 구현",
  "skills": "Java, Spring Boot, MySQL, AWS, Docker, Kubernetes",
  "desiredPosition": "B2B 플랫폼 백엔드 개발자",
  "yearsOfExperience": 3,
  "industry": "IT/소프트웨어"
}
```

- 응답 JSON
```json
{
  "id": "85205c00-01de-4b6b-9410-1911fc892de4",
  "careerSummary": "3년차 백엔드 개발자, Spring Boot/MSA 기반 커머스 서비스 개발, AWS EC2 운영 경험",
  "jobExperience": "대규모 이커머스 플랫폼 백엔드 개발, 마이크로서비스 아키텍처 설계 및 구현",
  "skills": "Java, Spring Boot, MySQL, AWS, Docker, Kubernetes",
  "desiredPosition": "B2B 플랫폼 백엔드 개발자",
  "yearsOfExperience": 3,
  "industry": "IT/소프트웨어"
}
```

### 1. 면접 질문 생성 
- 요청 URL
```
http://localhost:9070/api/resumes/85205c00-01de-4b6b-9410-1911fc892de4/mock-interview?deltas=false
```

- 응답
```
event: keepalive
: start

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: final
data: {
    "resumeId": "85205c00-01de-4b6b-9410-1911fc892de4",
    "questions": [
        {
            "question": "당신이 맡은 프로젝트에서 Spring Boot를 사용하여 웹 서비스를 개발했지만, 어떤 상황에서는 이를 MSA로 전환하는 것이 효율적일 수 있다는 의견에 동의합니다. 이를 구현할 때 가장 중요한 고려사항과 이를 적용한 사례를 설명해 주시겠습니까?",
            "category": "기술면접|문제해결|시스템설계",
            "expectedAnswerDirection": "MSA(Microservices Architecture) 전환을 위한 중요 고려사항 및 실무 경험을 중심으로 답변해 주십시오. [개인화 근거: 당신이 Spring Boot로 개발한 서비스의 성능과 확장성을 고려할 때, MSA를 적용하는 것이 어떤 장단점을 가지고 있는지 구체적으로 설명하는 것이 중요합니다.]",
            "difficulty": "중급"
        }, {
            "question": "AWS EC2와 Kubernetes를 함께 사용하면서 운영 경험을 설명해 주시겠습니까? 특히 Kubernetes를 사용한 컨테이너 관리와 자동화된 배포 프로세스에 대해 상세히 설명해 주십시오.",
            "category": "기술면접|최신기술",
            "expectedAnswerDirection": "AWS EC2와 Kubernetes의 협업 방식 및 이를 통해 얻은 효율성 향상을 중심으로 답변해 주십시오. [개인화 근거: 당신이 AWS EC2와 Kubernetes를 활용하여 운영한 실제 사례와 그 과정에서의 배움을 공유해 보세요.]",
            "difficulty": "중급"
        }, {
            "question": "당신이 개발한 서비스에 MySQL을 사용했지만, 향후 성능 문제를 해결하기 위해 NoSQL 데이터베이스 도입을 검토해야 한다는 의견도 있습니다. 이에 대한 입장을 설명하고, 실제로 이를 구현하려면 어떤 단계들을 거쳐야 하는지 답변해 주십시오.",
            "category": "기술면접|문제해결",
            "expectedAnswerDirection": "MySQL의 한계점과 NoSQL 데이터베이스 도입을 위한 고려사항 및 실무 경험을 중심으로 설명해 주십시오. [개인화 근거: 당신이 직면한 성능 문제와 이를 해결하기 위해 NoSQL 데이터베이스를 고려한 과정과 선택의 이유를 설명해 보세요.]",
            "difficulty": "중급"
        }, {
            "question": "Spring Boot 프레임워크에서 새로운 기능 추가나 버그 수정 작업을 진행할 때, 테스트 단계에 어떤 절차들을 포함시키는지 설명해주실 수 있습니까? 특히 이러한 과정에서 중요한 고려사항은 무엇이었는지를 공유해 주십시오.",
            "category": "기술면접|문제해결",
            "expectedAnswerDirection": "Spring Boot 프레임워크의 테스트 절차와 그 중요성에 대해 설명해 주십시오. [개인화 근거: 당신이 Spring Boot를 개발할 때 적용하는 테스트 방법과 이를 통해 얻은 혜택을 공유해 보세요.]",
            "difficulty": "초급"
        }, {
            "question": "당신의 프로젝트에서 Docker를 사용하여 컨테이너화 작업을 진행했지만, 실무에서는 Docker Swarm이나 Kubernetes와 같은 클러스터 관리를 위한 도구들이 더 효과적일 수 있다는 의견도 있습니다. 이를 구현하려면 어떤 단계들을 거쳐야 하는지 상세히 설명해 주십시오.",
            "category": "기술면접|최신기술",
            "expectedAnswerDirection": "Docker와 Kubernetes의 차이점 및 클러스터 관리 도구를 활용한 실제 사례를 중심으로 답변해 주십시오. [개인화 근거: 당신이 Docker를 사용하여 컨테이너화 작업을 진행하면서 발생한 문제와 이를 해결하기 위해 Kubernetes를 고려한 배경과 과정을 설명해 보세요.]",
            "difficulty": "중급"
        }
    ],
    "difficulty": "중급",
    "focusArea": "기술 스킬, 경험, 실무 사례 [전략: 이력서에서의 주요 기술 스킬과 경험을 바탕으로 문제 해결 능력 및 실무 경험을 심도 있게 파악할 수 있도록 질문을 구성하였습니다.]",
    "errorMessage": ""
}
```

### 2. 학습 경로 추천
- 요청 URL
```
http://localhost:9070/api/resumes/85205c00-01de-4b6b-9410-1911fc892de4/learning-path?deltas=true
```

- 응답
```
event: keepalive
: start

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

event: keepalive
: ping

data: `` `

data:json
data:{
data:  "currentLevel

data:": "중급

data:",
data:  "targetLevel

data:": "고급",
data:

data:  "learningSteps":

data: [
data:    {
data:      "

data:title": "Spring Boot

data: 및 MSA 심

data:화 학습",
data:

data:      "description": "

data:Spring Boot와 마이

data:크로서비스 아키텍

data:처(MSA)에

data: 대한 심층 학

data:습을 진행하여 기

data:술 스택을

data: 더욱 확장하고

data: 최적화합니다

data:.",
data:      "category":

data: "기술역량|프

data:로젝트경험",
data:

data:      "priority": 1

data:,
data:      "estimatedDuration

data:": "3개월

data:",
data:      "resources": [
data:

data:        "Spring Boot in

data: Action",
data:        "Micro

data:services in Action"
data:

data: ],
data:      "personalizationReason

data:": "현재의

data: 경험은 Spring

data: Boot와 MSA에

data: 대한 기본적인 이해

data:를 가지고 있지만,

data: 더 깊이

data: 있는 지식과 실

data:무 적용을 위한 추가

data: 학습이 필요

data:합니다."
data:

data: },
data:    {
data:

data:      "title":

data: "AWS 서비스 활용 및

data: 운영 최적화",
data:

data: "description": "AWS 서비스

data:의 다양한 기능을

data: 활용하고, 성능

data: 최적화와 안

data:전성을 위한 운영

data: 전략을 학

data:습합니다.",
data:      "

data:category": "기술역

data:량|프로

data:젝트경험|

data:자격증",
data:

data: "priority":

data:2,
data:      "estimated

data:Duration": "2개

data:월",
data:      "

data:resources": [
data:        "

data:AWS Certified Solutions Architect -

data: Associate Study Guide",
data:

data: "Building Scalable and

data: Secure Applications with AWS"
data:

data:      ],
data:      "personal

data:izationReason": "AWS

data: EC2 운영 경험은

data: 있으나, AWS 서비스

data:의 전체적인 활용과

data: 최적화, 그리고

data: 자격증 취득

data:을 통해 전문성을

data: 높일 수

data: 있습니다."
data:    }
data:

data: ],
data:  "estimatedTime

data:frame": "5개

data:월",
data:  "success

data:Metrics": [
data:    "

data:Spring Boot와 MSA

data:에 대한 깊은

data: 이해",
data:    "AWS

data: 서비스 활용 및 운영

data: 최적화 능

data:력",
data:    "

data:AWS 자격증 취

data:득"
data:  ],
data:

data: "learningStrategy": "

data:기술 스택과

data: 아키텍처

data:에 대한 심화

data: 학습을 통해

data: 기술적

data: 능력을 강화

data:하고, AWS 서비스의

data: 활용을 통해 실무

data: 적용 능력을

data: 향

data:상시킵

data:니다.

data: 이를 통해 B2B

data: 플랫폼 백

data:엔드 개발자의

data: 역량을 고급

data: 수준으로 끌

data:어올릴 수 있습니다."
data:

data:}
data:` ``

event: final
data: {
    "resumeId": "85205c00-01de-4b6b-9410-1911fc892de4",
    "currentLevel": "중급",
    "targetLevel": "고급",
    "learningSteps": [
        {
            "title": "Spring Boot 및 MSA 심화 학습",
            "description": "Spring Boot와 마이크로서비스 아키텍처(MSA)에 대한 심층 학습을 진행하여 기술 스택을 더욱 확장하고 최적화합니다.\n\n\uD83D\uDCA1 개인 맞춤 포인트: 현재의 경험은 Spring Boot와 MSA에 대한 기본적인 이해를 가지고 있지만, 더 깊이 있는 지식과 실무 적용을 위한 추가 학습이 필요합니다.",
            "category": "기술역량|프로젝트경험",
            "priority": 1,
            "estimatedDuration": "3개월",
            "resources": ["Spring Boot in Action", "Microservices in Action"]
        }, 
        {
            "title": "AWS 서비스 활용 및 운영 최적화",
            "description": "AWS 서비스의 다양한 기능을 활용하고, 성능 최적화와 안전성을 위한 운영 전략을 학습합니다.\n\n\uD83D\uDCA1 개인 맞춤 포인트: AWS EC2 운영 경험은 있으나, AWS 서비스의 전체적인 활용과 최적화, 그리고 자격증 취득을 통해 전문성을 높일 수 있습니다.",
            "category": "기술역량|프로젝트경험|자격증",
            "priority": 2,
            "estimatedDuration": "2개월",
            "resources": ["AWS Certified Solutions Architect - Associate Study Guide", "Building Scalable and Secure Applications with AWS"]
        }
    ],
    "estimatedTimeframe": "5개월 [전략: 기술 스택과 아키텍처에 대한 심화 학습을 통해 기술적 능력을 강화하고, AWS 서비스의 활용을 통해 실무 적용 능력을 향상시킵니다. 이를 통해 B2B 플랫폼 백엔드 개발자의 역량을 고급 수준으로 끌어올릴 수 있습니다.]",
    "errorMessage": ""
}
```


## 🚀 실행 방법

### 🐳 Docker를 이용한 간편 실행
Docker Compose를 사용하면 Ollama와 애플리케이션을 한 번에 실행할 수 있습니다.

```bash
# 1. 프로젝트 클론
git clone https://github.com/DEV-asdf-516/AI-Challenge-Career-Coach-API.git
cd AI-Challenge-Career-Coach-API

# 2. 환경 설정 파일 생성
cat > .env << EOF
PORT=9070

DB_NAME=resume
DB_USERNAME=admin
DB_PASSWORD=1234

OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_MODEL=benedict/linkbricks-llama3.1-korean:8b
# 또는
# OLLAMA_MODEL=gemma2:9b

## GPU가 없는 환경이라면 아래 세 모델을 추천합니다.
## - 최소 사양 기준: RAM 8GB, CPU 4Core 
## 느린 응답(10 ~ 12분), 정확도 높음
# OLLAMA_MODEL=qwen2.5:7b-instruct 
## 빠른 응답(2 ~ 3분), 정확도 낮음
# OLLAMA_MODEL=llama3.2:3b 
## 다음 모델 사용 시 HF_TOKEN 환경변수 설정이 필요합니다. 
## https://huggingface.co/settings/tokens 에 접속해 토큰을 발급 받으세요. 
## 빠른 응답(4 ~ 5분), 정확도 중간
# OLLAMA_MODEL=llama3-instruct-kor-8b-q4km  
# HF_TOKEN=<your_huggingface_token_here>

SWAGGER_SERVER_URL=http://localhost
EOF

# 3. Docker Compose로 전체 서비스 실행
docker-compose up -d

# 4. 모델 다운로드 완료 확인 (첫 다운로드 시 5-10분 정도의 시간이 소요됩니다.)
docker-compose logs -f model-setup

# 5. API 서비스 확인
curl -N -H "Accept: text/event-stream" http://localhost:9070/api/ollama/greeting
```

**서비스 URL:**
- API 문서 (Swagger): http://localhost:9070/
- H2 Database Console: http://localhost:9070/h2-console
