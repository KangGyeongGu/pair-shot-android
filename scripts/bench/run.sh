#!/usr/bin/env bash
# PairShot performance benchmark runner.
#
# Usage:
#   scripts/bench/run.sh [micro|macro|baseline|all]
#
# - micro    : :microbenchmark:rendering connectedReleaseAndroidTest (PairImageComposer µs)
# - macro    : :benchmark connectedBenchmarkAndroidTest (Startup/Scroll/PairPreview)
# - baseline : Macrobenchmark BaselineProfileGenerator only -> baseline-prof.txt
# - all      : micro + macro
#
# Requires:
#   - USB-connected physical device (Android 10+, USB debugging enabled)
#   - Pre-seeded data: at least 1 PAIRED pair on the device for PairPreviewBenchmark
#
# Outputs:
#   benchmark/build/outputs/connected_android_test_additional_output/
#   microbenchmark/rendering/build/outputs/connected_android_test_additional_output/

set -euo pipefail

target="${1:-all}"
package="com.pairshot"

if ! command -v adb >/dev/null 2>&1; then
    echo "ERR: adb not found in PATH" >&2
    exit 1
fi

devices=$(adb devices | awk 'NR>1 && /device$/ { print $1 }')
if [[ -z "$devices" ]]; then
    echo "ERR: no connected device. Plug in a physical Android 10+ device with USB debugging." >&2
    exit 1
fi
echo "==> Target device(s): $devices"

echo "==> Granting permissions and waking device"
adb shell pm grant "$package" android.permission.CAMERA 2>/dev/null || true
adb shell pm grant "$package" android.permission.READ_MEDIA_IMAGES 2>/dev/null || true
adb shell pm grant "$package" android.permission.WRITE_EXTERNAL_STORAGE 2>/dev/null || true
adb shell input keyevent KEYCODE_WAKEUP || true
adb shell wm dismiss-keyguard || true
adb shell svc power stayon usb || true
adb shell settings put global zen_mode 1 || true

run_micro() {
    echo "==> Microbenchmark: PairImageComposer"
    ./gradlew :microbenchmark:rendering:connectedReleaseAndroidTest
}

run_macro() {
    echo "==> Macrobenchmark: Startup / Scroll / PairPreview"
    ./gradlew :benchmark:connectedBenchmarkAndroidTest
}

run_baseline() {
    echo "==> Baseline Profile generator"
    ./gradlew :benchmark:connectedBenchmarkAndroidTest \
        -P "android.testInstrumentationRunnerArguments.class=com.pairshot.benchmark.BaselineProfileGenerator"
}

case "$target" in
    micro)    run_micro ;;
    macro)    run_macro ;;
    baseline) run_baseline ;;
    all)      run_micro; run_macro ;;
    *)        echo "Unknown target: $target. Use micro|macro|baseline|all." >&2; exit 1 ;;
esac

echo
echo "==> Done. Restoring zen mode."
adb shell settings put global zen_mode 0 || true

echo
echo "Results:"
find . -path '*/connected_android_test_additional_output/*-benchmarkData.json' 2>/dev/null \
    | head -50
