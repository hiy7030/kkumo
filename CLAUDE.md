# KKUMO (꾸모) 프로젝트 가이드라인

이 파일은 Claude Code가 KKUMO 저장소에서 작업할 때 반드시 준수해야 할 규칙과 맥락을 정의합니다.

## ⛔ 보호된 파일 (Protected Files - Do Not Modify)
**아래 파일들은 인프라 및 배포 안정화를 위해 "동결(Freeze)" 상태입니다.**
사용자가 명시적으로 "수정해줘"라고 요청하기 전까지는, 어떤 기능 구현 중에도 **절대 임의로 수정하거나 삭제하지 마세요.**

1. **`.github/workflows/deploy.yml`** (CI/CD 파이프라인)
2. **`CLAUDE.md`** (프로젝트 규칙)

## 1. 프로젝트 개요 (Overview)
- **프로젝트명:** KKUMO (꾸모)
- **목표:** 1일 1회 사진 기록 및 Streak(왕관) 시스템을 통한 습관 형성 소셜 서비스.
- **개발 방식:** "바이브 코딩" (AI를 활용한 빠른 프로토타이핑 + 실무급 아키텍처).
- **기술 스택:**
    - **Language:** Kotlin (JDK 21)
    - **Framework:** Spring Boot 3.5.8, Spring Data JPA, Spring Security
    - **Database:** MySQL/MariaDB (CloudType)
    - **Frontend:** Thymeleaf (SSR) + Tailwind CSS (CDN) + Fetch API
    - **Build:** Gradle (Kotlin DSL)
    - **Database:**
      - MariaDB
  - **Deployment:** GitHub Actions -> CloudType

## 2. 아키텍처 및 패키지 구조
**도메인 중심 아키텍처 (Domain-oriented / Package-by-feature)** 를 엄격히 준수합니다.
*계층형 구조(root에 controller/service 패키지 배치)를 사용하지 마세요.*

- **Base Package:** `com.kkumo`
- **Structure:**
    - `global/` (Config, Security, Error, Utils, BaseTimeEntity)
    - `domain/`
        - `member/` (Controller, Service, Repository, Entity, DTO 등)
        - `post/` (게시글 관련 로직)
        - `reaction/` (반응 관련 로직)
        - `report/` (리포트/통계 로직)
    - `KKumoApplication.kt`

## 3. 핵심 비즈니스 규칙 (Strict Rules)

### 3.1 도메인 로직
1. **Member (`members` 테이블)**
    - `myEmoji`: 유니크(Unique). '👑' (왕관)은 시스템 전용이므로 선택 불가.
    - `hasCrown`: Streak 달성 여부 (Boolean).
2. **Post (`posts` 테이블)**
    - **1일 1회 제한:** DB Unique Index (`member_id` + `postedDate`) 설정 필수.
    - **정책:** **수정 가능** (`isEdited` 라벨링) / **삭제 절대 불가** (Streak 어뷰징 방지).
    - `postedDate`: 비즈니스 로직용 `LocalDate` 필드 (`createdAt`과 별도 운영).
3. **Streak 시스템**
    - **부여:** 글 작성 즉시 체크 (7일 연속 달성 시 왕관 부여).
    - **회수:** 매일 자정(00:00 KST) 스케줄러가 전날 미작성자의 Streak 초기화.
4. **Report & UX**
    - **MY Report:** 달력 뷰 (작성한 날짜에 내 사진 배경).
    - **ALL Report:** 생존 매트릭스 (Row: 유저 / Col: 날짜).
    - **Main UI:** "오늘 기록 마감까지 00:00:00 남음" 타이머 표시.

### 3.2 인프라 및 DevOps (Infra)
- **Timezone:** App & DB 모두 `Asia/Seoul` (KST) 강제 설정.
- **Image Processing:** Cloudflare R2 업로드 전 `Thumbnailator`로 리사이징(너비 800px, WebP/JPG) 필수.
- **CI/CD:**
    - GitHub Actions (`.github/workflows/deploy.yml`) 작성.
    - `main` 브랜치 Push/PR 시 Build & Test 자동 실행.
- **Environment:**
    - `deploy.yml`을 통한 자동 배포 (설정 건드리지 말 것).

## 4. 코드 작성 원칙 (Conventions)

### 품질 및 안정성
1. **No Pseudo-code:** 주석으로 로직을 생략하지 말고, 반드시 **실제 동작하는 완성된 코드**를 작성할 것.
2. **Null Safety:** Kotlin의 강점을 살려 `!!` 사용을 지양하고, `?.` (Safe Call) 사용.
3. **No Logic in Controller:** 비즈니스 로직은 반드시 `Service`에 위임.

### Entity 및 JPA 규칙
1. **Auditing:** 모든 Entity는 `BaseTimeEntity`를 상속받아 `createdAt`/`updatedAt`을 자동 관리한다.
2. **Class Type:** JPA Entity에는 `data class` 사용을 금지한다. (Lazy Loading 이슈 방지, 일반 `class` 사용).

### 네이밍 규칙
- **Class:** PascalCase (예: `MemberService`)
- **Variable/Function:** camelCase (예: `findMemberById`)
- **DTO:** 의도를 명확히 표현 (예: `SignUpRequest`, `MemberResponse`)

### 테스트 전략
- **커버리지:** 전체 커버리지보다 **핵심 로직(Streak, 제약조건)** 검증에 집중.
- **도구:** JUnit5 + Mockk.

### Controller Annotation Strategy (Strict Rule)
모든 컨트롤러는 표준 Spring 어노테이션(`@RestController`, `@Controller`) 대신 **프로젝트 전용 커스텀 어노테이션**을 사용해야 한다.
모든 요청은 기본적으로 `/kkumo/v1` 경로를 상속받는다.

#### A. REST API (JSON Response)
- **Annotation:** `@KKumoRestController`
- **Usage:** 데이터 통신, API 엔드포인트 구현 시 사용.
- **Definition:**
  ```kotlin
  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.RUNTIME)
  @RestController
  @RequestMapping("/kkumo/v1")
  annotation class KKumoRestController
  
#### B. Web View (HTML/Thymeleaf Response)
- **Annotation:** `@KKumoWebController`
- **Usage:** 화면(View)을 반환하는 페이지 컨트롤러 구현 시 사용.
- **Definition:**
  ```kotlin
  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.RUNTIME)
  @Controller
  @RequestMapping("/kkumo/v1")
  annotation class KKumoWebController
  
#### C. Implementation Example
- Case 1: API (Member Logic)
- 
- 
   ```kotlin
   @KKumoRestController
   class MemberApiController(private val memberService: MemberService) {
   @PostMapping("/members")
   fun signup(@RequestBody request: SignupRequest) { ... }
   }
- Case 2: View (Page Rendering)
   ```kotlin
  @KKumoWebController
  class MemberViewController {
  @GetMapping("/login")
  fun loginPage(): String = "login"
  }

## 5. UI/UX Design Guidelines (KKUMO Design System)
**Core Concept:** "Retro, Warm, Soft, Cute" (Main Symbol: 👑 Crown)

### Color Palette (Custom Hex Codes)
*Tailwind의 기본 컬러 대신 아래의 Hex Code를 Arbitrary Value(`[]`)로 반드시 사용할 것.*
- **Background:** `bg-[#FCF6D9]` (Page Main - Warm Cream), `bg-white` (Card/Container).
- **Primary Action (CTA):** `bg-[#CF4B00]` (Burnt Orange), `hover:bg-[#B03E00]`, `text-white`.
- **Main Accent:** `text-[#9CC6DB]` (Sky Blue), `focus:ring-[#9CC6DB]` (Input Focus Ring).
- **Sub/Support:** `text-[#DDBA7D]` (Muted Gold) for secondary borders or subtitles.
- **Text:** `text-gray-800` (Headings), `text-gray-600` (Body), `text-[#CF4B00]` (Error/Highlight).

### Component Style
- **Shape:** All UI elements must use **Rounded Corners**.
    - Containers/Cards: `rounded-3xl`, `shadow-lg`.
    - Inputs/Buttons: `rounded-xl`.
- **Logo:** Simple Crown Emoji (`👑`) centered at the top (`text-6xl`).
- **Input Focus:** `focus:ring-2 focus:ring-[#9CC6DB]` (Blue Ring), `border-gray-300`.

### Interaction Patterns (Hybrid Architecture)
- **View Navigation:** Standard `GET` requests returning Thymeleaf templates.
- **Data Operations:** Forms (Login/Signup/Post) use `fetch` API (JSON payload).
- **Error Handling:**
    - **Validation/Business Error:** Show `alert(message)` (Do NOT redirect).
    - **System Error (500):** Redirect to `error/errorPage`.

## 6. 빌드 및 실행 명령어

```bash
# 빌드 (테스트 제외하고 빠르게)
./gradlew clean build -x test

# 앱 실행
./gradlew bootRun

# 테스트 실행
./gradlew test