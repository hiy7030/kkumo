# 🎨 KKUMO(꾸모) Design System

이 문서는 꾸모 프로젝트의 UI/UX 디자인 원칙과 Tailwind CSS 스타일 가이드를 정의합니다.
모든 View(HTML) 개발 시 이 규칙을 준수하여 일관성 있는 사용자 경험을 제공해야 합니다.

## 1. Design Concept
- **Keywords:** `Simplicity`, `Pastel`, `Soft`, `Cute`
- **Main Symbol:** 👑 (Crown) - 심플하고 명확한 왕관 아이콘 사용.
- **Vibe:** 자극적이지 않고 은은한 파스텔톤 배경에, 둥글둥글한 UI 요소로 포근한 느낌을 줌.

## 2. Color Palette (Tailwind CSS)
### Primary (Background)
- **Main Background:** `bg-sky-50` (아주 연한 하늘색) ~ `bg-sky-100`
- **Card Background:** `bg-white` (깨끗한 흰색)

### Secondary (Point & Action)
- **Point Color (Button/Highlight):** `bg-yellow-300` (파스텔 노란색)
- **Hover Action:** `hover:bg-yellow-400`
- **Text on Yellow:** `text-yellow-900` (가독성을 위해 진한 노란/갈색 텍스트)

### Text & Neutral
- **Title:** `text-gray-800`
- **Body:** `text-gray-600`
- **Placeholder:** `text-gray-400`
- **Border:** `border-gray-200`

### Error
- **Text:** `text-red-500`
- **Background:** `bg-red-50`

## 3. Typography & Shape
### Font Style
- 기본적으로 Sans-serif를 사용하되, 부드러운 느낌을 주기 위해 폰트 굵기를 적절히 조절.
- **Title:** `font-bold`
- **Body:** `font-medium` or `font-normal`

### Borders (Rounded)
꾸모의 모든 UI 요소는 **둥근 모서리**를 가집니다. 각진 디자인(`rounded-none`)은 지양합니다.
- **Card Container:** `rounded-3xl` (매우 둥글게)
- **Button:** `rounded-xl` or `rounded-2xl`
- **Input Field:** `rounded-xl`

### Shadows
- **Card:** `shadow-lg` (부드럽고 넓게 퍼지는 그림자)
- **Input Focus:** `focus:ring-2 focus:ring-yellow-300` (포커스 시 노란색 링)

## 4. UI Components Guide

### A. Card Layout (Login/Signup Box)
화면 정중앙에 위치하며, 흰색 배경에 그림자가 있는 카드 형태.
```html
<div class="bg-white p-8 rounded-3xl shadow-lg w-full max-w-md">
    </div>

