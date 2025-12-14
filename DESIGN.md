# 🎨 KKUMO (꾸모) Design System

이 문서는 꾸모 프로젝트의 UI/UX 디자인 원칙과 Tailwind CSS 스타일 가이드를 정의합니다.
개발 시 이 규칙을 준수하여 통일감 있는 "레트로 & 웜(Retro & Warm)" 무드를 유지해야 합니다.

## 1. Design Concept
- **Keywords:** `Retro`, `Warm`, `Soft`, `Cute`
- **Main Symbol:** 👑 (Crown) - 심플하고 명확한 왕관 아이콘 사용.
- **Vibe:** 따뜻한 크림색 배경에 차분한 블루와 강렬한 오렌지 포인트를 더해, 감성적이고 포근한 느낌을 줌.

---

## 2. Color Palette
Tailwind CSS 설정(`tailwind.config.js`)에 추가하거나 Arbitrary Value(`[]`)를 사용하여 적용합니다.

| Role | Color Name | Hex Code | Usage |
| :--- | :--- | :--- | :--- |
| **Main** | **Sky Blue** | `#9CC6DB` | 헤더, 주요 테두리, 아이콘 배경, 활성 탭 |
| **Base** | **Warm Cream** | `#FCF6D9` | **웹페이지 전체 배경(`body`)**, 부드러운 분위기 조성 |
| **Point** | **Burnt Orange** | `#CF4B00` | **주요 버튼(CTA)**, 강조 텍스트, 에러 메시지, 클릭 유도 |
| **Sub** | **Muted Gold** | `#DDBA7D` | 보조 버튼, 구분선, 서브 텍스트, 왕관 아이콘 매칭 |

### Tailwind Usage Guide
- **Backgrounds:**
    - Page Body: `bg-[#FCF6D9]`
    - Card/Container: `bg-white`
- **Actions (Buttons):**
    - Primary: `bg-[#CF4B00]` (Text: `text-white`)
    - Hover: `hover:bg-[#B03E00]`
- **Text:**
    - Headings: `text-gray-800`
    - Highlights/Error: `text-[#CF4B00]`
    - Subtitles: `text-[#DDBA7D]` or `text-gray-500`

---

## 3. Typography & Shape

### Font Style
- 기본적으로 Sans-serif를 사용하되, 가독성을 위해 굵기를 조절합니다.
- **Title:** `font-bold` (강조가 필요할 때)
- **Body:** `font-medium` or `font-normal`

### Borders & Radius (Rounded)
꾸모의 모든 UI 요소는 **둥근 모서리**를 가집니다. 각진 디자인(`rounded-none`)은 지양합니다.
- **Card Container:** `rounded-3xl` (매우 둥글게, 부드러운 느낌)
- **Button:** `rounded-xl`
- **Input Field:** `rounded-lg`

### Shadows & Effects
- **Card:** `shadow-lg` (부드럽고 넓게 퍼지는 그림자)
- **Input Focus:** `focus:ring-2 focus:ring-[#9CC6DB]` (포커스 시 메인 블루 컬러 링)
- **Button Hover:** 색상 변경과 함께 약간의 `transform` 효과 권장.

---

## 4. UI Components Guide (Code Snippets)

### A. Page Layout (Background)
따뜻한 크림색 배경이 전체를 감싸고, 중앙에 흰색 카드가 위치합니다.
```html
<body class="bg-[#FCF6D9] min-h-screen flex items-center justify-center font-sans">
    <div class="bg-white p-8 rounded-3xl shadow-lg w-full max-w-md">
        </div>
</body>