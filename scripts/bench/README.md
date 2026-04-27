# PairShot performance benchmarks

## Quick start

```bash
# 1. 디바이스 연결 (Android 10+, USB 디버깅)
# 2. 시드 데이터: PAIRED 페어 1 건 이상 있어야 PairPreviewBenchmark 동작

# 측정 실행
scripts/bench/run.sh all      # micro + macro
scripts/bench/run.sh micro    # PairImageComposer µs only
scripts/bench/run.sh macro    # Startup/Scroll/PairPreview only
scripts/bench/run.sh baseline # baseline-prof.txt 생성 only

# 결과 분석 (markdown 표 출력)
scripts/bench/analyze.sh
scripts/bench/analyze.sh --update-baseline  # 첫 측정을 baseline 으로 저장
```

## Modules

- `:microbenchmark:rendering` — `PairImageComposerBenchmark` (4MP/8MP/16MP × PREVIEW + 4MP FULL)
- `:benchmark` — `StartupBenchmark` · `HomeScrollBenchmark` · `PairPreviewBenchmark` · `BaselineProfileGenerator`

## TraceSectionMetric labels

- `ps.compose` — 합성 전체
- `ps.load-before` — Before 비트맵 디코딩
- `ps.load-after` — After 비트맵 디코딩
- `ps.compose-internal` — Canvas 합성·다운스케일

## Outputs

```
benchmark/build/outputs/connected_android_test_additional_output/<device>/
  *-benchmarkData.json   # 수치 (median, percentile)
  *.perfetto-trace       # 시각화용 (ui.perfetto.dev 에서 열기)

microbenchmark/rendering/build/outputs/connected_android_test_additional_output/
  *-benchmarkData.json
```

## CI

`.github/workflows/perf.yml` 참고 (PR 마다 microbenchmark, 야간 macrobenchmark 권장).
