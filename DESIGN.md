# DESIGN.md

## 1. Color Palette (Blue & Cream Theme)
*Tailwind의 기본 컬러 대신 아래의 Hex Code를 Arbitrary Value(`[]`)로 사용하여 **감성적이고 차분한 블루 톤**을 구현할 것.*

### Background
- **Main Page:** `bg-white`
  - 트위터(X)나 최신 앱처럼 깔끔하고 확장성 있는 화이트 배경.
- **Sub / Border:** `bg-[#DBDFEA]`, `border-[#DBDFEA]` (Mist Blue)
  - 배경과 콘텐츠를 은은하게 구분하는 구분선이나 보조 배경색.

### Primary Action (CTA)
- **Base:** `bg-[#8294C4]` (Cool Blue)
  - 기록하기, 저장, 확인 등 가장 중요한 버튼.
- **Hover:** `hover:bg-[#6E80B0]`
- **Text:** `text-white`

### Main Accent & Focus
- **Active Icons/Links:** `text-[#8294C4]`
- **Input Focus Ring:** `focus:ring-[#ACB1D6]` (Soft Lavender)
  - 입력창 포커스 시 부드러운 라벤더 색상의 링 효과.
- **Active Border:** `focus:border-[#8294C4]`

### Sub / Support
- **Muted Text:** `text-[#ACB1D6]`
  - 비활성화된 버튼, 플레이스홀더, 보조 설명 텍스트.
- **Inactive Icons:** `text-[#ACB1D6]`

### Point (Highlight)
- **Accent Background:** `bg-[#FDF4E3]` (Warm Cream)
  - **프로필 이미지 배경**, 배지, 혹은 차가운 블루 톤 사이에서 따뜻함을 주고 싶은 포인트 요소에 사용.

### Typography Colors
- **Headings:** `text-slate-800` (가독성을 위한 진한 회색)
- **Body:** `text-slate-600` (본문)
- **Brand Text:** `text-[#8294C4]` (브랜드 컬러 강조 텍스트)

---

## 2. Component Style (Modern App-like)

### Shape & Radius
- **Primary Buttons:** `rounded-full` (Pill Shape)
  - 트위터 스타일의 둥근 알약 모양 버튼.
- **Containers / Cards:** `rounded-2xl`
  - 너무 과하지 않고 세련된 느낌의 라운드 처리.
- **Inputs / Images:** `rounded-xl`
  - 부드러운 모서리 마감.

### Logo
- **Style:** Sticky Header에 위치하며 심플함을 강조.
- **Size:** `text-4xl`
- **Icon:** Crown Emoji (`👑`)

### Input Fields
- **Default:** `border border-[#DBDFEA]`, `bg-white`, `rounded-xl`.
- **Focus State:**
  - `outline-none` (브라우저 기본 아웃라인 제거).
  - `ring-2 ring-[#ACB1D6]` (부드러운 포커스 링).
  - `border-[#8294C4]` (진한 블루 테두리).

### Shadows & Depth
- **Default Shadow:** `shadow-sm`
  - 플랫한 디자인을 유지하되, 아주 얕은 깊이감만 부여.
- **Floating Buttons (FAB):** `shadow-lg`
  - 화면 위에 떠 있는 버튼에만 강한 그림자 적용.