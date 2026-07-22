#!/usr/bin/env bash

set -euo pipefail

TEST_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SCRIPT_UNDER_TEST="$TEST_ROOT/scripts/bump-version.sh"
TEMP_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/thinglinks-bump-version-test.XXXXXX")"

cleanup() {
  rm -rf "$TEMP_ROOT"
}
trap cleanup EXIT

fail() {
  printf '测试失败：%s\n' "$1" >&2
  exit 1
}

assert_equals() {
  local expected="$1"
  local actual="$2"
  local label="$3"
  [ "$actual" = "$expected" ] || fail "${label}；期望 ${expected}，实际 ${actual}"
}

read_version() {
  awk -F= '$1 == "THINGLINKS_COMPONENT_VERSION" { print substr($0, index($0, "=") + 1) }' "$1"
}

write_fake_tool() {
  local target="$1"
  mkdir -p "$(dirname "$target")"
  cp "$TEMP_ROOT/fake-product-config.sh" "$target"
  chmod +x "$target"
}

make_fixture() {
  local name="$1"
  local fixture="$TEMP_ROOT/$name"
  local component

  mkdir -p "$fixture/scripts"
  cp "$SCRIPT_UNDER_TEST" "$fixture/scripts/bump-version.sh"
  chmod +x "$fixture/scripts/bump-version.sh"

  for component in thinglinks-cloud thinglinks-job bifromq-plugin thinglinks-web thinglinks-web-visualize; do
    mkdir -p "$fixture/$component"
    printf '# 组件发布版本。\nTHINGLINKS_COMPONENT_VERSION=1.4.0\n' > "$fixture/$component/.thinglinks-product.env"
  done

  write_fake_tool "$fixture/thinglinks-cloud/scripts/product-config.sh"
  write_fake_tool "$fixture/thinglinks-job/scripts/product-config.sh"
  write_fake_tool "$fixture/bifromq-plugin/scripts/product-config.sh"
  write_fake_tool "$fixture/thinglinks-web/scripts/product-config.mjs"
  write_fake_tool "$fixture/thinglinks-web-visualize/scripts/product-config.mjs"
  printf '%s\n' "$fixture"
}

assert_all_versions() {
  local fixture="$1"
  local expected="$2"
  local component
  local actual

  for component in thinglinks-cloud thinglinks-job bifromq-plugin thinglinks-web thinglinks-web-visualize; do
    actual="$(read_version "$fixture/$component/.thinglinks-product.env")"
    assert_equals "$expected" "$actual" "$component 版本"
  done
}

cat > "$TEMP_ROOT/fake-product-config.sh" <<'FAKE_TOOL'
#!/usr/bin/env bash

set -euo pipefail

COMPONENT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPONENT="$(basename "$COMPONENT_ROOT")"
MANIFEST="$COMPONENT_ROOT/.thinglinks-product.env"
COMMAND="${1:-check}"
LOG_FILE="${BUMP_VERSION_TEST_LOG:?缺少测试日志路径}"

read_version() {
  awk -F= '$1 == "THINGLINKS_COMPONENT_VERSION" { print substr($0, index($0, "=") + 1) }' "$MANIFEST"
}

write_version() {
  local next_version="$1"
  local temporary="$MANIFEST.tmp.$$"
  awk -v value="$next_version" '
    $0 ~ /^THINGLINKS_COMPONENT_VERSION=/ {
      print "THINGLINKS_COMPONENT_VERSION=" value
      next
    }
    { print }
  ' "$MANIFEST" > "$temporary"
  mv "$temporary" "$MANIFEST"
}

case "$COMMAND" in
  check)
    CURRENT_VERSION="$(read_version)"
    printf 'check:%s:%s\n' "$COMPONENT" "$CURRENT_VERSION" >> "$LOG_FILE"
    if [ "${FAIL_CHECK_COMPONENT:-}" = "$COMPONENT" ]; then
      exit 71
    fi
    if [ "${FAIL_FINAL_CHECK_COMPONENT:-}" = "$COMPONENT" ] && [ "$CURRENT_VERSION" != "1.4.0" ]; then
      if [ -n "${MUTATE_COMPONENT_ON_FINAL_FAILURE:-}" ]; then
        OTHER_MANIFEST="$(dirname "$COMPONENT_ROOT")/${MUTATE_COMPONENT_ON_FINAL_FAILURE}/.thinglinks-product.env"
        OTHER_TEMPORARY="$OTHER_MANIFEST.tmp.$$"
        awk '
          $0 ~ /^THINGLINKS_COMPONENT_VERSION=/ {
            print "THINGLINKS_COMPONENT_VERSION=9.9.9"
            next
          }
          { print }
        ' "$OTHER_MANIFEST" > "$OTHER_TEMPORARY"
        mv "$OTHER_TEMPORARY" "$OTHER_MANIFEST"
      fi
      exit 75
    fi
    ;;
  set-component-version|set-version)
    [ "$#" -eq 2 ] || exit 72
    printf 'set:%s:%s\n' "$COMPONENT" "$2" >> "$LOG_FILE"
    write_version "$2"
    if [ "${FAIL_SET_AFTER_WRITE_COMPONENT:-}" = "$COMPONENT" ] && [ "$2" != "1.4.0" ]; then
      exit 73
    fi
    ;;
  *)
    exit 74
    ;;
esac
FAKE_TOOL
chmod +x "$TEMP_ROOT/fake-product-config.sh"

SUCCESS_FIXTURE="$(make_fixture success)"
: > "$SUCCESS_FIXTURE/operations.log"
BUMP_VERSION_TEST_LOG="$SUCCESS_FIXTURE/operations.log" \
  "$SUCCESS_FIXTURE/scripts/bump-version.sh" 1.5.0 > "$SUCCESS_FIXTURE/output.log"
assert_all_versions "$SUCCESS_FIXTURE" 1.5.0
assert_equals 5 "$(grep -c '^set:' "$SUCCESS_FIXTURE/operations.log")" '成功流程写入次数'
assert_equals 10 "$(grep -c '^check:' "$SUCCESS_FIXTURE/operations.log")" '成功流程检查次数'
FIRST_SET_LINE="$(grep -n '^set:' "$SUCCESS_FIXTURE/operations.log" | head -1 | cut -d: -f1)"
CHECKS_BEFORE_FIRST_SET="$(sed -n "1,$((FIRST_SET_LINE - 1))p" "$SUCCESS_FIXTURE/operations.log" | grep -c '^check:')"
assert_equals 5 "$CHECKS_BEFORE_FIRST_SET" '首次写入前检查次数'

CHECK_ONLY_FIXTURE="$(make_fixture check-only)"
: > "$CHECK_ONLY_FIXTURE/operations.log"
BUMP_VERSION_TEST_LOG="$CHECK_ONLY_FIXTURE/operations.log" \
  "$CHECK_ONLY_FIXTURE/scripts/bump-version.sh" check > "$CHECK_ONLY_FIXTURE/output.log"
assert_all_versions "$CHECK_ONLY_FIXTURE" 1.4.0
assert_equals 5 "$(grep -c '^check:' "$CHECK_ONLY_FIXTURE/operations.log")" '只读检查次数'
if grep -q '^set:' "$CHECK_ONLY_FIXTURE/operations.log"; then
  fail '只读检查发生了写入'
fi
grep -q '五个组件的产品配置均通过检查' "$CHECK_ONLY_FIXTURE/output.log" || fail '只读检查缺少成功提示'

IDEMPOTENT_FIXTURE="$(make_fixture idempotent)"
: > "$IDEMPOTENT_FIXTURE/operations.log"
BUMP_VERSION_TEST_LOG="$IDEMPOTENT_FIXTURE/operations.log" \
  "$IDEMPOTENT_FIXTURE/scripts/bump-version.sh" 1.4.0 > "$IDEMPOTENT_FIXTURE/output.log"
assert_all_versions "$IDEMPOTENT_FIXTURE" 1.4.0

ROLLBACK_FIXTURE="$(make_fixture rollback)"
: > "$ROLLBACK_FIXTURE/operations.log"
if BUMP_VERSION_TEST_LOG="$ROLLBACK_FIXTURE/operations.log" \
  FAIL_SET_AFTER_WRITE_COMPONENT=thinglinks-job \
  "$ROLLBACK_FIXTURE/scripts/bump-version.sh" 1.5.0 > "$ROLLBACK_FIXTURE/output.log" 2>&1; then
  fail '组件写入后失败时根脚本仍返回成功'
fi
assert_all_versions "$ROLLBACK_FIXTURE" 1.4.0
if grep -q '^set:bifromq-plugin:1.5.0$' "$ROLLBACK_FIXTURE/operations.log"; then
  fail '失败后仍继续更新后续组件'
fi
grep -q '^set:thinglinks-job:1.4.0$' "$ROLLBACK_FIXTURE/operations.log" || fail '未回滚当前失败组件'
grep -q '^set:thinglinks-cloud:1.4.0$' "$ROLLBACK_FIXTURE/operations.log" || fail '未回滚先前成功组件'

FINAL_CHECK_FIXTURE="$(make_fixture final-check)"
: > "$FINAL_CHECK_FIXTURE/operations.log"
if BUMP_VERSION_TEST_LOG="$FINAL_CHECK_FIXTURE/operations.log" \
  FAIL_FINAL_CHECK_COMPONENT=thinglinks-web \
  "$FINAL_CHECK_FIXTURE/scripts/bump-version.sh" 1.5.0 > "$FINAL_CHECK_FIXTURE/output.log" 2>&1; then
  fail '最终检查失败时根脚本仍返回成功'
fi
assert_all_versions "$FINAL_CHECK_FIXTURE" 1.4.0
assert_equals 5 "$(grep -c '^set:.*:1.4.0$' "$FINAL_CHECK_FIXTURE/operations.log")" '最终检查失败后的回滚次数'

CONCURRENT_FIXTURE="$(make_fixture concurrent-change)"
: > "$CONCURRENT_FIXTURE/operations.log"
if BUMP_VERSION_TEST_LOG="$CONCURRENT_FIXTURE/operations.log" \
  FAIL_FINAL_CHECK_COMPONENT=thinglinks-web \
  MUTATE_COMPONENT_ON_FINAL_FAILURE=thinglinks-cloud \
  "$CONCURRENT_FIXTURE/scripts/bump-version.sh" 1.5.0 > "$CONCURRENT_FIXTURE/output.log" 2>&1; then
  fail '并发修改场景下根脚本仍返回成功'
fi
assert_equals 9.9.9 "$(read_version "$CONCURRENT_FIXTURE/thinglinks-cloud/.thinglinks-product.env")" '并发修改后的 Cloud 版本'
for component in thinglinks-job bifromq-plugin thinglinks-web thinglinks-web-visualize; do
  assert_equals 1.4.0 "$(read_version "$CONCURRENT_FIXTURE/$component/.thinglinks-product.env")" "$component 回滚版本"
done
grep -q '未覆盖事务外的并发修改' "$CONCURRENT_FIXTURE/output.log" || fail '并发修改未给出保护提示'

PREFLIGHT_FIXTURE="$(make_fixture preflight)"
: > "$PREFLIGHT_FIXTURE/operations.log"
if BUMP_VERSION_TEST_LOG="$PREFLIGHT_FIXTURE/operations.log" \
  FAIL_CHECK_COMPONENT=bifromq-plugin \
  "$PREFLIGHT_FIXTURE/scripts/bump-version.sh" 1.5.0 > "$PREFLIGHT_FIXTURE/output.log" 2>&1; then
  fail '组件预检失败时根脚本仍返回成功'
fi
assert_all_versions "$PREFLIGHT_FIXTURE" 1.4.0
if grep -q '^set:' "$PREFLIGHT_FIXTURE/operations.log"; then
  fail '全部组件预检通过前发生了写入'
fi

printf '根版本编排测试通过。\n'
