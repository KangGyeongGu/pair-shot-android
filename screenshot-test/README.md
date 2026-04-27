# PairShot Screenshot Tests (Roborazzi)

JVM(Robolectric)에서 Compose hierarchy 를 PNG 로 렌더해 시각 회귀를 자동 차단.

## 명령

```bash
# 첫 baseline 캡처 (또는 재기록)
./gradlew :screenshot-test:testDebugUnitTest -Proborazzi.test.record=true --rerun-tasks

# PR 검증 (baseline 과 픽셀 diff. 변동 시 fail)
./gradlew :screenshot-test:testDebugUnitTest -Proborazzi.test.verify=true --rerun-tasks

# 차이만 확인 (fail 안 하고 비교 이미지 출력)
./gradlew :screenshot-test:testDebugUnitTest -Proborazzi.test.compare=true --rerun-tasks
```

## 결과 위치

```
screenshot-test/src/test/snapshots/   ← git 추적, baseline PNG
screenshot-test/build/outputs/roborazzi/  ← diff/비교 이미지 (verify/compare 시)
```

## 새 테스트 추가 패턴

```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class MyScreenshotTest {
    @Test fun screen_state() {
        captureRoboImage(filePath = "src/test/snapshots/MyScreen_state.png") {
            PairShotTheme {
                MyScreen(...)
            }
        }
    }
}
```

## 디바이스 변형

`RobolectricDeviceQualifiers` 의 사전 정의 spec 사용:
- `Pixel4` (소형 5.7")
- `Pixel7` (표준 6.3")
- `MediumTablet` / `LargeTablet`
- `Pixel9` 등 최신

다중 디바이스 매트릭스가 필요하면 같은 컴포저블에 대해 별도 테스트 클래스 또는 `@ParameterizedRobolectricTestRunner` 활용.

## 주의

- AGP 9.x + roborazzi gradle plugin 호환 stable 미확인이라 plugin 미적용. dependency 만으로 동작 (record/verify/compare 모드는 -P*  flag → systemProperty).
- targetSdk=35 이상은 Robolectric 4.13 미지원 → `@Config(sdk = [34])` 명시 필수.
- Compose Compiler Metrics (`build/compose_metrics/*-composables.txt`) 와 함께 사용하면 stability 회귀 + 시각 회귀를 동시 가드.
