# ☁️ KKUMO (꾸모)
> **매일 한 장의 사진으로 쌓는 습관, 게이미피케이션 기반 소셜 기록 플랫폼**

<br>

## 📜 서비스 개요
**KKUMO(꾸모)** 는 메신저(카카오톡 오픈채팅) 기반 기록 모임의 한계인  
- 참여자들이 기록을 꾸준히 수행하고 있는지 한눈에 확인하기 어려운 문제  
- '하루 한 장'과 같은 규칙이 실제로 지켜지고 있는지 검증하기 어려운 문제  

을 해결하기 위해 기획된 소셜 기록 플랫폼입니다.

2026년 1월부터 실제 사용자 기반으로 운영되었으며, 사용자의 기록 생성 및 조회 기능을 중심으로 서비스를 설계·구현했습니다.

단순 기록 저장을 넘어 **연속 달성(Streak)** 개념을 도입하여 사용자의 습관 형성을 유도하고, 개인 및 전체 기록을 한눈에 확인할 수 있는 대시보드를 제공합니다.

- **개발 및 운영 기간**: 2025.12 (1개월 개발) / 2026.01 ~ 2026.04 (현재 운영 종료)
- **개발 인원**: 1인 (기획, 설계, 개발, 배포 전 과정 수행)

<br>

## 🛠 기술 스택

### Backend
- **Language**: Kotlin
- **Framework**: Spring Boot 3.5.8
- **Data**: Spring Data JPA, QueryDSL(복잡한 동적 쿼리 처리 및 성능 최적화를 위해 사용)
- **Database**: MariaDB
- **Template Engine**: Thymeleaf

### DevOps & Storage
- **Infrastructure**: CloudType, Cloudflare R2 (Object Storage)
- **CI/CD**: GitHub Actions

### Tools
- **AI Assist**: Gemini, Claude <br>(단순 코드 생성을 넘어, AI가 제안한 로직의 비효율 및 오류를 검증하고 리팩토링하는 등 주도적인 페어 프로그래밍 도구로 활용)

<br>

## ✨ 주요 기능 

- **🔥 연속 기록(Streak) 및 보상 시스템**
  - 매일 1장씩 사진을 업로드하여 기록을 유지합니다.
  - 7일 연속 기록 달성 시 **왕관(👑) 뱃지**가 자동 부여됩니다.
  - 자정(00:00)까지 미기록 시 Streak이 초기화되어 지속적인 참여(Retention)를 유도합니다.

- **🚫 1일 1회 기록 제한 및 어뷰징 방지**
  - '하루 한 장'이라는 핵심 가치를 지키기 위해 당일 중복 업로드를 방지합니다.
  - 한 번 등록된 기록은 **수정 및 삭제가 전면 불가**하도록 엄격한 정책을 설정하여 Streak 어뷰징을 원천 차단합니다.

- **📊 직관적인 기록 대시보드 (Report)**
  - **MY Report**: 달력(Calendar) 뷰를 통해 개인의 월간 기록 히스토리를 사진 배경으로 시각화합니다.
  - **ALL Report**: 전체 회원(Row) × 날짜(Col) 형태의 매트릭스 뷰로 커뮤니티 전체의 기록 현황을 한눈에 파악할 수 있습니다.

- **⏳ 마감 타이머 기능**
  - 자정까지 남은 시간(HH:MM:SS)을 실시간 타이머로 제공하여 기록 작성을 독려합니다.

<br>

## 🚀 트러블 슈팅

### 1. N+1 문제 해결 및 대시보드 조회 성능 최적화
- **문제**: ALL Report(전체 현황판) 조회 시, 회원 목록과 해당 회원의 일별 기록을 가져오는 과정에서 심각한 N+1 문제가 발생.
- **해결**: `Fetch Join`과 `DTO Projection`을 활용하여 쿼리 수를 최소화하고, 무한 스크롤 환경에 맞춰 `Slice` 페이징을 적용하여 불필요한 Count 쿼리 제거.

### 2. 대용량 배치 처리 성능 최적화 (Streak 초기화)
- **문제**: 매일 자정에 미기록자의 Streak을 0으로 초기화해야 하는데, 사용자 수가 늘어날수록 단건 Update 쿼리(Dirty Checking) 방식은 DB 부하 및 병목을 유발할 위험이 있음.
- **해결**: Cron 스케줄러와 **JPQL Bulk Update** 방식을 결합하여, 대상 데이터를 단 한 번의 쿼리로 일괄 변경하도록 설계해 DB I/O 부하를 최소화함.

### 3. 데이터 무결성 보장 및 어뷰징 차단
- **문제**: 동시 요청 상황에서 '1일 1회 기록' 정책이 깨질 수 있는 동시성 이슈 존재.
- **해결**: DB 테이블에 `Unique Index (member_id + posted_date)`를 설정하여 데이터베이스 레벨에서 원천 차단하고, 애플리케이션 서비스 계층에서 예외 처리를 수행하는 **이중 검증 로직**을 구현하여 데이터 정합성을 확보함.

### 4. 리액션 기능 성능 최적화 (비동기 처리)
- **문제**: 사용자들이 게시글에 다건의 리액션(이모지)을 남길 때마다 단건 DB I/O가 발생하여, 사용자가 몰리는 특정 시간대에 DB 커넥션 부하가 발생할 우려가 있음.
- **해결**: 리액션 요청을 즉시 DB에 반영하지 않고, 일정 단위로 묶어 처리하는 **비동기 배치 구조**로 개선하여 DB I/O를 감소시킴

<br>

## 🏗 시스템 아키텍처
<img width="800" alt="kkumo-system-architecture" src="https://github.com/user-attachments/assets/52a6b15e-a052-441d-a3a7-822b925e5a05" />

<br>

## 🗄 ERD 다이어그램
<img width="600" alt="kkumo-erd" src="https://github.com/user-attachments/assets/7e93b03e-92c5-4a38-a84e-7972a36b7470" />


<br>

## 💻 뷰 템플릿 (Screenshots)
| 로그인 & 회원가입 | 메인 화면 (연속 기록 전/후) | 비밀글 & 리액션 |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/4cc74f56-b8f9-4936-bf35-2db9507d025c" width="120"> <img src="https://github.com/user-attachments/assets/81712792-4752-4cec-b19f-610d3762c66d" width="120"> | <img src="https://github.com/user-attachments/assets/406745dd-8e91-4cef-831b-32eed3e1de00" width="250"> | <img src="https://github.com/user-attachments/assets/664ee996-012d-4bfe-b432-b6a40d53684c" width="250"> |

<br>

| **MY Report (개인 캘린더)** | **ALL Report (전체 대시보드)** | **마이페이지** |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/69bb295c-27cb-4a2c-aae2-73ebf86c2ec3" width="250"> | <img src="https://github.com/user-attachments/assets/66b71f05-5939-4b9c-9a8f-0f844b2e3a9a" width="250"> | <img src="https://github.com/user-attachments/assets/88b3d104-82ee-480b-8a15-450e5c1323d9" width="250"> |
