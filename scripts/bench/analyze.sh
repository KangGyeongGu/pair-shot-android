#!/usr/bin/env bash
# Aggregate Macro/Microbenchmark JSON output into a single Markdown table.
# Compares to scripts/bench/baseline.json if present (% delta highlighting).
#
# Usage:
#   scripts/bench/analyze.sh [--update-baseline]
#
# Dependencies: jq, python3 (>=3.8)

set -euo pipefail

update_baseline=false
if [[ "${1:-}" == "--update-baseline" ]]; then
    update_baseline=true
fi

if ! command -v jq >/dev/null 2>&1; then
    echo "ERR: jq required (brew install jq)" >&2
    exit 1
fi

aggregated=$(mktemp)
tmp_lines=$(mktemp)
trap "rm -f $aggregated $tmp_lines" EXIT

found=0
while IFS= read -r -d '' f; do
    found=1
    jq -c '.benchmarks // [] | map({
        key: ((.className // "?") + "#" + (.name // "?")),
        metrics: (.metrics // {} | to_entries | map({ name: .key, median: (.value.median // .value.medianMs // null) }))
    }) | .[]' "$f" >> "$tmp_lines" || true
done < <(find . -path '*/connected_android_test_additional_output/*-benchmarkData.json' -print0 2>/dev/null)

if [[ $found -eq 0 ]]; then
    echo "No benchmark results found. Run scripts/bench/run.sh first." >&2
    exit 1
fi

jq -s '.' "$tmp_lines" > "$aggregated"

baseline_file="scripts/bench/baseline.json"
baseline_json="{}"
if [[ -f "$baseline_file" ]]; then
    baseline_json=$(cat "$baseline_file")
fi

python3 - "$aggregated" "$baseline_file" "$update_baseline" <<'PY'
import json, sys, os

agg_path, baseline_path, update_str = sys.argv[1], sys.argv[2], sys.argv[3]
update = update_str == "true"

with open(agg_path) as f:
    rows = json.load(f)

baseline = {}
if os.path.exists(baseline_path):
    with open(baseline_path) as f:
        try:
            baseline = json.load(f)
        except Exception:
            baseline = {}

new_baseline = {}
print("# PairShot Benchmark Report\n")
print("| Test | Metric | Median | Baseline | Δ |")
print("|---|---|---:|---:|---:|")

for r in rows:
    key = r["key"]
    for m in r["metrics"]:
        metric = m["name"]
        median = m["median"]
        if median is None:
            continue
        b_key = f"{key}#{metric}"
        new_baseline[b_key] = median
        bv = baseline.get(b_key)
        if bv is None or bv == 0:
            delta = "—"
        else:
            pct = (median - bv) / bv * 100
            sign = "+" if pct >= 0 else ""
            mark = ""
            if pct >= 10:
                mark = " ⚠️"
            elif pct <= -5:
                mark = " ✅"
            delta = f"{sign}{pct:.1f}%{mark}"
        print(f"| {key} | {metric} | {median:.3f} | {bv if bv else '—'} | {delta} |")

if update:
    with open(baseline_path, "w") as f:
        json.dump(new_baseline, f, indent=2, sort_keys=True)
    print(f"\n→ Baseline updated: {baseline_path}")
PY
