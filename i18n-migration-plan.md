# 문자열 리소스 중앙집중화 및 다국어(i18n) 대응 계획서

> 작성일: 2026-04-23
>
> **목표**: 전 화면·모달·알림바의 하드코딩된 한국어 문구 132개를 Android 표준 `strings.xml` 리소스로 이전하여 다국어(i18n) 지원 기반 확보. Snackbar 개선은 이 작업 완료 후 진행.

---

## 1. 범위 요약

- **대상 문구**: 132개 (8개 모듈)
- **대상 카테고리**: 버튼, 설정 항목, 다이얼로그, 접근성(contentDescription), 섹션 라벨, 안내문구, 스낵바, Empty state, TextField placeholder
- **기본 언어**: 한국어 (`values/strings.xml`)
- **추가 예정 언어**: 영어 (`values-en/strings.xml`) — 번역 작업은 별도 Gate

---

## 2. 아키텍처 결정

### 2.1 리소스 시스템: Android 표준 `strings.xml`

**채택 이유**:
- Android 플랫폼 1순위 권장 방식
- 시스템 언어 자동 감지 및 전환 (추가 코드 0)
- `<plurals>` 복수형, `%1$s` 포맷, 이스케이프 규칙이 표준화됨
- 번역 플랫폼(Weblate, Lokalise, Crowdin) 모두 이 포맷 지원
- Android Studio의 Translations Editor로 직관적 편집
- 빌드 시점에 누락·참조 오류 감지

### 2.2 모듈별 분산 배치

각 feature 모듈이 자기 문구를 소유하고, 공통 문구만 `core/ui`에 둠.

```
core/ui/src/main/res/values/strings.xml               ← 공통 버튼·다이얼로그 패턴
app/src/main/res/values/strings.xml                   ← 앱명, MainActivity
feature/home/src/main/res/values/strings.xml
feature/camera/src/main/res/values/strings.xml
feature/album/src/main/res/values/strings.xml
feature/pairpreview/src/main/res/values/strings.xml
feature/settings/src/main/res/values/strings.xml
feature/exportsettings/src/main/res/values/strings.xml
```

**장점**:
- 모듈 독립성 유지 (기존 아키텍처 규칙 부합)
- 파일 크기 관리 용이
- 공통 리소스만 재사용

**단점**:
- "공통 vs feature 어디에 둘지" 판단 필요 (명확한 규칙 §3에서 정의)

### 2.3 ViewModel이 Context 의존 없이 문자열 전달하는 방법 — `UiText` 래퍼

**문제**: ViewModel은 도메인 계층에 가까워 `Context`·`@StringRes` 의존을 피하는 게 Clean Architecture 원칙.

**해결**: `UiText` sealed class로 "해석 가능한 문자열"을 추상화.

```kotlin
// core/ui/src/main/java/com/pairshot/core/ui/text/UiText.kt
sealed class UiText {
    data class Resource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText()

    data class Plural(
        @PluralsRes val resId: Int,
        val count: Int,
        val args: List<Any> = emptyList(),
    ) : UiText()

    data class Dynamic(val value: String) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is Resource -> stringResource(resId, *args.toTypedArray())
        is Plural -> pluralStringResource(resId, count, *args.toTypedArray())
        is Dynamic -> value
    }

    fun asString(context: Context): String = when (this) {
        is Resource -> context.getString(resId, *args.toTypedArray())
        is Plural -> context.resources.getQuantityString(resId, count, *args.toTypedArray())
        is Dynamic -> value
    }
}
```

**ViewModel 사용 예**:
```kotlin
_snackbarMessage.emit(
    SnackbarEvent(
        message = UiText.Resource(R.string.snackbar_capture_error),
        variant = SnackbarVariant.ERROR,
    )
)

// 매개변수 있는 문구
UiText.Resource(R.string.home_selection_count, listOf(selectedCount))

// 복수형
UiText.Plural(R.plurals.pair_delete_confirm, pairCount, listOf(pairCount))
```

**UI 사용 예**:
```kotlin
Text(text = event.message.asString())
```

**장점**:
- ViewModel이 `Context` 의존 안 함
- 테스트 용이 (`UiText.Resource(R.string.xxx)` 비교만)
- 동적 문자열(예: 서버 응답 메시지)도 `UiText.Dynamic`으로 포용

### 2.4 `SnackbarEvent` 타입 변경

```kotlin
// before
data class SnackbarEvent(
    val message: String,
    val variant: SnackbarVariant = SnackbarVariant.INFO,
    val actionLabel: String? = null,
)

// after
data class SnackbarEvent(
    val message: UiText,
    val variant: SnackbarVariant = SnackbarVariant.INFO,
    val actionLabel: UiText? = null,
)
```

---

## 3. 네이밍 규칙

### 3.1 키 구조: `{scope}_{category}_{semantic}`

| 부분 | 값 |
|---|---|
| scope | `common`, `home`, `camera`, `album`, `pair_preview`, `settings`, `watermark`, `combine`, `export`, `snackbar`, `dialog` |
| category | `title`, `label`, `button`, `message`, `hint`, `placeholder`, `desc`, `error`, `info`, `success` |
| semantic | 의미를 짧게 (camelCase 말고 snake_case) |

### 3.2 예시

| 하드코딩 | 리소스 키 | 위치 |
|---|---|---|
| "취소" | `common_button_cancel` | core/ui |
| "삭제" | `common_button_delete` | core/ui |
| "확인" | `common_button_confirm` | core/ui |
| "뒤로가기" | `common_desc_back` | core/ui |
| "설정" (화면 제목) | `settings_title` | feature/settings |
| "촬영 시작" | `home_button_start_capture` | feature/home |
| "앨범 생성" | `home_button_create_album` | feature/home |
| "N개 선택됨" | `home_topbar_selection_count` | feature/home |
| "카메라 권한 필요" | `camera_permission_title` | feature/camera |
| "촬영에 실패했습니다. 다시 시도해주세요" | `snackbar_error_capture_failed` | core/ui |
| "저장에 실패했습니다. 다시 시도해주세요" | `snackbar_error_save_failed` | core/ui |
| "N개 페어를 삭제하시겠어요?" | `dialog_delete_pair_confirm` | core/ui |

### 3.3 복수형 처리 규칙

**한국어는 복수 변화가 없지만** 영어 대응을 위해 `<plurals>`로 선언.

```xml
<!-- values/strings.xml (Korean) -->
<plurals name="pair_count_selected">
    <item quantity="other">%1$d개 선택됨</item>
</plurals>

<!-- values-en/strings.xml (English) -->
<plurals name="pair_count_selected">
    <item quantity="one">%1$d item selected</item>
    <item quantity="other">%1$d items selected</item>
</plurals>
```

**복수형 대상 (현재 코드 기준 8곳)**:
- `%1$d개 선택됨` (HomeTopBar)
- `%1$d개 페어를 삭제하시겠어요?` (DeletePairConfirmDialog)
- `%1$d개 앨범을 삭제하시겠어요?` (HomeScreen)
- `%1$d개 페어를 앨범에서 제거하시겠습니까?` (DeletePairsDialog)
- `%1$d개 선택됨, %2$d개 합성본` (DeletePairConfirmDialog)
- `Before 썸네일 %1$d` (BeforePreviewStrip)
- `%1$d / %2$d 완료` (ExportSettingsScreen)
- `%1$s · %2$d개` (MainActivity)

### 3.4 포맷 규칙

| 구분 | 표기 |
|---|---|
| 정수 매개변수 | `%1$d`, `%2$d` |
| 문자열 매개변수 | `%1$s`, `%2$s` |
| 소수점 | `%1$.2f` |
| 퍼센트 문자 자체 | `%%` |
| 개행 | `\n` |
| 작은따옴표 | `\'` (이스케이프 필수) |
| 큰따옴표 | `\"` (이스케이프 필수) |

---

## 4. 공통 vs Feature 구분 규칙

### 4.1 `core/ui`에 두는 기준

다음 중 **하나라도** 해당하면 공통:

1. **여러 feature 모듈에서 2회 이상 사용** (예: "취소", "삭제", "공유")
2. **Dialog/Snackbar/접근성 레이블처럼 범용 컴포넌트와 1:1 대응** (예: `PairShotSnackbar`에서 쓰는 에러 메시지)
3. **용어집 수준의 개념어** (예: "페어", "합성본" — 단 실제 문자열이 짧으면 굳이 키 분리 안 해도 됨)

### 4.2 Feature 모듈에 두는 기준

다음 중 **하나라도** 해당하면 해당 모듈:

1. 해당 기능·화면 고유 라벨 (예: "플래시", "야간모드" → camera 전용)
2. 해당 기능의 설정 항목 (예: "두께", "곡률" → settings.combine 전용)
3. 다른 모듈에서 참조할 이유가 없는 문구

### 4.3 경계 사례 판단

| 문구 | 판정 근거 | 최종 위치 |
|---|---|---|
| "설정" | Home AppBar 아이콘 desc + Settings 화면 제목 + 여러 화면 카드 링크 — 3곳 이상 | `core/ui: common_title_settings` |
| "공유" | Home·Album 두 모듈에서 동일 사용 | `core/ui: common_button_share` |
| "삭제" | 10곳 이상 범용 사용 | `core/ui: common_button_delete` |
| "뒤로가기" | 4개 모듈 접근성 desc | `core/ui: common_desc_back` |
| "더보기" | Camera만 사용 | `feature/camera: camera_desc_more` |
| "페어 추가" | Album 전용 | `feature/album: album_button_add_pair` |
| "촬영 시작" | Home·Album 두 모듈 — but wording nuance는 같음 | `core/ui: common_button_start_capture` |

---

## 5. 파일 구조 및 볼륨 예측

### 5.1 모듈별 예상 키 수

| 모듈 | 키 수 (근사) | 주요 카테고리 |
|---|---|---|
| `core/ui` | 약 30 | 공통 버튼, Dialog 패턴, Snackbar 메시지, 공통 접근성 |
| `app` | 약 3 | 앱명, MainActivity selection bar |
| `feature/home` | 약 15 | AppBar, 액션바, Dialog, Filter |
| `feature/camera` | 약 25 | 권한, 설정 시트, Strip, 접근성 |
| `feature/album` | 약 15 | Dialog, 액션바, Empty state |
| `feature/pairpreview` | 약 5 | 접근성, Dialog |
| `feature/settings` | 약 45 | 설정 전 항목·섹션 |
| `feature/exportsettings` | 약 12 | 섹션·옵션 |

**합계: 약 150개 키** (기존 132 + 중복 분리로 일부 증가)

### 5.2 실제 XML 예시 (핵심만 발췌)

**`core/ui/src/main/res/values/strings.xml`**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Common Buttons -->
    <string name="common_button_cancel">취소</string>
    <string name="common_button_confirm">확인</string>
    <string name="common_button_delete">삭제</string>
    <string name="common_button_save">저장</string>
    <string name="common_button_share">공유</string>
    <string name="common_button_start_capture">촬영 시작</string>
    <string name="common_button_save_to_device">기기저장</string>

    <!-- Common Accessibility -->
    <string name="common_desc_back">뒤로가기</string>
    <string name="common_desc_settings">설정</string>
    <string name="common_desc_more">더보기</string>

    <!-- Common Titles -->
    <string name="common_title_settings">설정</string>

    <!-- Snackbar: Error -->
    <string name="snackbar_error_capture_failed">촬영에 실패했습니다. 다시 시도해주세요</string>
    <string name="snackbar_error_save_failed">저장에 실패했습니다. 다시 시도해주세요</string>
    <string name="snackbar_error_unknown">오류가 발생했습니다. 다시 시도해주세요</string>
    <string name="snackbar_error_share_failed">공유 준비에 실패했습니다. 다시 시도해주세요</string>
    <string name="snackbar_error_file_load_failed">파일을 불러올 수 없습니다</string>

    <!-- Snackbar: Success -->
    <string name="snackbar_success_all_after_captured">모든 After 촬영이 완료되었습니다</string>
    <string name="snackbar_success_cache_cleared">캐시를 정리했습니다</string>

    <!-- Snackbar: Info -->
    <string name="snackbar_info_combined_exists">이미 합성본이 있습니다</string>
    <string name="snackbar_info_select_zip_location">압축파일 저장 위치를 선택해주세요</string>
    <string name="snackbar_info_save_cancelled">저장을 취소했습니다</string>
    <string name="snackbar_info_saved_to_device">기기에 저장했습니다</string>
    <string name="snackbar_info_saved_to_device_partial">기기에 저장했습니다 (일부 건너뜀)</string>

    <!-- Dialog: Delete Pair -->
    <plurals name="dialog_delete_pair_confirm">
        <item quantity="other">%1$d개 페어를 삭제하시겠어요?</item>
    </plurals>
    <string name="dialog_delete_pair_title">페어 삭제</string>
    <string name="dialog_delete_pair_method_title">삭제 방식 선택</string>
    <plurals name="dialog_delete_pair_summary">
        <item quantity="other">%1$d개 선택됨, %2$d개 합성본.</item>
    </plurals>
    <string name="dialog_delete_pair_button_all">일괄 삭제</string>
    <string name="dialog_delete_pair_button_combined_only">합성본만</string>
</resources>
```

**`feature/home/src/main/res/values/strings.xml`**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="home_topbar_title">PairShot</string>
    <plurals name="home_topbar_selection_count">
        <item quantity="other">%1$d개 선택됨</item>
    </plurals>

    <string name="home_filter_all">전체</string>
    <string name="home_filter_album">앨범</string>

    <string name="home_button_create_album">앨범 생성</string>
    <string name="home_button_select_all">전체선택</string>
    <string name="home_button_deselect_all">전체해제</string>
    <string name="home_button_export_settings">내보내기설정</string>
    <string name="home_button_album_rename">이름 수정</string>
    <string name="home_button_album_remove">앨범에서 제거</string>

    <string name="home_desc_deselect">선택 해제</string>
    <string name="home_desc_selection_mode">선택 모드</string>

    <string name="home_dialog_album_create_title">앨범 생성</string>
    <string name="home_dialog_album_create_hint">앨범 이름을 입력하세요.</string>
    <string name="home_dialog_album_create_placeholder">앨범 이름</string>
    <string name="home_dialog_album_rename_title">앨범 이름 수정</string>
    <string name="home_dialog_album_delete_title">앨범 삭제</string>
    <plurals name="home_dialog_album_delete_confirm">
        <item quantity="other">%1$d개 앨범을 삭제하시겠어요?\n앨범 내 모든 페어와 합성본도 함께 삭제됩니다.</item>
    </plurals>
</resources>
```

(나머지 모듈도 동일 패턴으로 구성)

---

## 6. ViewModel · Compose 사용 패턴

### 6.1 화면 내 고정 라벨 (가장 많음)

```kotlin
// before
Text(text = "촬영 시작")

// after
Text(text = stringResource(R.string.common_button_start_capture))
```

### 6.2 매개변수 있는 문구

```kotlin
// before
Text(text = "${selectedCount}개 선택됨")

// after (단수·복수 구분 없음)
Text(text = pluralStringResource(
    R.plurals.home_topbar_selection_count,
    selectedCount,
    selectedCount,
))
```

### 6.3 접근성

```kotlin
// before
Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "뒤로가기")

// after
Icon(
    imageVector = Icons.Filled.ArrowBack,
    contentDescription = stringResource(R.string.common_desc_back),
)
```

### 6.4 ViewModel에서 Snackbar 발동

```kotlin
// before
_snackbarMessage.emit(SnackbarEvent("저장에 실패했습니다.", SnackbarVariant.ERROR))

// after
_snackbarMessage.emit(
    SnackbarEvent(
        message = UiText.Resource(R.string.snackbar_error_save_failed),
        variant = SnackbarVariant.ERROR,
    )
)
```

UI 쪽:
```kotlin
// PairShotSnackbar 내부
Text(text = event.message.asString())
```

### 6.5 Context 기반 해석이 필요한 경우 (WorkManager, Service 등)

```kotlin
val text: String = uiText.asString(context)
```

---

## 7. 구현 Gate (단계별 실행)

### Gate 0 — 인프라 (선행 작업)

대상 파일 (신규):
- `core/ui/src/main/java/com/pairshot/core/ui/text/UiText.kt`
- `core/ui/src/main/res/values/strings.xml` (빈 파일 생성)

대상 파일 (수정):
- `core/ui/.../component/SnackbarEvent.kt` — `message: String` → `message: UiText`
- `core/ui/.../component/PairShotSnackbarController.kt` — 시그니처 반영
- `core/ui/.../component/PairShotSnackbar.kt` — `event.message.asString()` 사용

**검증**: 전체 컴파일 성공, 기존 하드코딩된 문자열은 `UiText.Dynamic("기존 문구")`로 일단 감쌈 — 점진 마이그레이션 가능.

### Gate 1 — 공통 리소스 (core/ui)

약 30개 키 추가:
- 공통 버튼 · 접근성 · 제목
- Snackbar 전체 15개 (§3 기준)
- 공통 Dialog (삭제 확인, 이름 입력)

마이그레이션:
- `core/ui/` 내부 사용처 전부 `stringResource` 전환
- Snackbar 발동 지점 `UiText.Resource` 전환

### Gate 2 — Feature: home (15개)

- `feature/home/src/main/res/values/strings.xml` 생성
- HomeScreen · HomeTopBar · HomeFilterRow · 각 BottomBar · Dialog 하드코딩 전환

### Gate 3 — Feature: camera (25개)

- 권한 안내, 설정 시트, 접근성 포함
- Snackbar는 이미 Gate 1에서 처리됨

### Gate 4 — Feature: album (15개)

- Dialog, 액션바, Empty state

### Gate 5 — Feature: pairpreview (5개)

- 접근성 중심

### Gate 6 — Feature: settings (45개, 가장 큼)

하위로 분할:
- 6-A: SettingsScreen 본체 · 섹션 라벨
- 6-B: 워터마크 설정
- 6-C: 합성 설정
- 6-D: 라이선스 · 기타

### Gate 7 — Feature: exportsettings (12개)

- 섹션 · 옵션

### Gate 8 — App 레벨 (3개)

- app 모듈 `strings.xml` 생성
- MainActivity SelectionBar 문구

### Gate 9 — 검증

- `./gradlew assembleDebug` 전체 빌드 성공
- 남은 하드코딩 문구 0 확인 (스크립트로 검증):
  ```
  grep -rn --include='*.kt' -E '"(?=.*[가-힣])' feature/ app/ core/
  ```
- Android Studio Translations Editor에서 누락 키 확인

### Gate 10 — 영어 번역 (선택, 별도 일정)

- `values-en/strings.xml` 각 모듈에 추가
- 번역 작업 (외주 또는 자체)
- 시스템 언어 전환 테스트 (에뮬레이터에서 `Settings > Language` 변경)

---

## 8. 실행 규모 및 리스크

### 8.1 변경 파일 수

- 신규: 각 모듈 `strings.xml` (8개) + `UiText.kt` = 9개
- 수정: 약 40~50개 Kotlin 파일 (Composable + ViewModel)

### 8.2 리스크와 대응

| 리스크 | 대응 |
|---|---|
| 빌드 중 참조 오류 (`R.string.xxx` 없음) | Gate별 완결 → 각 Gate 끝에 빌드 확인 |
| Preview에서 `stringResource` 호출 제약 | `@Composable` 컨텍스트에서만 호출 — 기존 코드도 대부분 이미 Composable 내 |
| ViewModel 변경 범위 확산 | `UiText` 도입으로 도메인 격리 유지. ViewModel 외 영역 영향 최소 |
| 번역 누락 감지 | Android Studio Translations Editor + Lint |
| 문자열 기호 이스케이프 (`'`, `"`, `@`, `?`, `%`) | §3.4 표 준수, Lint가 대부분 잡아줌 |

### 8.3 롤백 전략

- 각 Gate는 단일 커밋으로 완결 — 문제 발생 시 해당 커밋만 revert
- `UiText.Dynamic(literal)` fallback이 있어 점진 마이그레이션 중에도 앱 동작 보장

---

## 9. Snackbar 개선 계획과의 관계

`snackbar-improvement-plan.md`에서 정의한 크기·햅틱 개선은 **Gate 0~1 완료 후 진행**.

- 이 시점에 `SnackbarEvent.message`가 이미 `UiText`로 전환됨
- 문구 교체는 `UiText.Resource(R.string.snackbar_*)` 참조로 자연스럽게 반영
- Snackbar 개선 Gate F(문구 교체)는 자동으로 완료 상태

---

## 10. 다음 단계

1. 본 계획서 검토
2. Gate 0 착수 (`UiText` + `SnackbarEvent` 타입 변경)
3. Gate 1 ~ 9 순차 진행
