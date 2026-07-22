#!/usr/bin/env bash

# ThingLinks 五组件版本升级编排器。
# 组件版本只从各自的 .thinglinks-product.env 读取，并由各组件产品配置工具写入。

set -u
set -o pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

COMPONENT_NAMES=(
  "Cloud"
  "Job"
  "BifroMQ 插件"
  "Web"
  "可视化大屏"
)

COMPONENT_DIRS=(
  "thinglinks-cloud"
  "thinglinks-job"
  "bifromq-plugin"
  "thinglinks-web"
  "thinglinks-web-visualize"
)

PRODUCT_TOOLS=(
  "thinglinks-cloud/scripts/product-config.sh"
  "thinglinks-job/scripts/product-config.sh"
  "bifromq-plugin/scripts/product-config.sh"
  "thinglinks-web/scripts/product-config.mjs"
  "thinglinks-web-visualize/scripts/product-config.mjs"
)

SET_VERSION_COMMANDS=(
  "set-component-version"
  "set-version"
  "set-version"
  "set-version"
  "set-version"
)

ORIGINAL_VERSIONS=()
TARGET_VERSION=""
ROLLBACK_LAST_INDEX=-1
TRANSACTION_ACTIVE=0

usage() {
  printf '用法：%s check\n' "$0"
  printf '      %s <新版本>\n' "$0"
  printf '示例：%s 1.4.0\n' "$0"
}

validate_version() {
  local version="$1"
  [[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+)?(-[0-9A-Za-z.-]+)?$ ]]
}

manifest_path() {
  local index="$1"
  printf '%s/%s/.thinglinks-product.env\n' "$PROJECT_ROOT" "${COMPONENT_DIRS[$index]}"
}

read_manifest_version() {
  local index="$1"
  local manifest
  local definition_count
  local version

  manifest="$(manifest_path "$index")"
  [ -f "$manifest" ] || {
    printf '错误：缺少 %s 产品清单：%s\n' "${COMPONENT_NAMES[$index]}" "$manifest" >&2
    return 1
  }

  definition_count="$(awk '$0 ~ /^THINGLINKS_COMPONENT_VERSION=/ { count += 1 } END { print count + 0 }' "$manifest")" || return 1
  [ "$definition_count" -eq 1 ] || {
    printf '错误：%s 产品清单必须且只能定义一次 THINGLINKS_COMPONENT_VERSION。\n' "${COMPONENT_NAMES[$index]}" >&2
    return 1
  }

  version="$(awk -F= '$1 == "THINGLINKS_COMPONENT_VERSION" { print substr($0, index($0, "=") + 1) }' "$manifest")" || return 1
  validate_version "$version" || {
    printf '错误：%s 产品清单中的组件版本格式不正确：%s\n' "${COMPONENT_NAMES[$index]}" "$version" >&2
    return 1
  }
  printf '%s\n' "$version"
}

run_product_tool() {
  local index="$1"
  shift
  local component_root="$PROJECT_ROOT/${COMPONENT_DIRS[$index]}"
  local tool="$PROJECT_ROOT/${PRODUCT_TOOLS[$index]}"

  (
    cd "$component_root" || exit 1
    "$tool" "$@"
  )
}

preflight_component() {
  local index="$1"
  local manifest
  local tool="$PROJECT_ROOT/${PRODUCT_TOOLS[$index]}"
  local version

  manifest="$(manifest_path "$index")"
  [ -f "$manifest" ] || {
    printf '错误：缺少 %s 产品清单：%s\n' "${COMPONENT_NAMES[$index]}" "$manifest" >&2
    return 1
  }
  [ ! -L "$manifest" ] || {
    printf '错误：%s 产品清单不能是符号链接：%s\n' "${COMPONENT_NAMES[$index]}" "$manifest" >&2
    return 1
  }
  [ -f "$tool" ] || {
    printf '错误：缺少 %s 产品配置工具：%s\n' "${COMPONENT_NAMES[$index]}" "$tool" >&2
    return 1
  }
  [ -x "$tool" ] || {
    printf '错误：%s 产品配置工具不可执行：%s\n' "${COMPONENT_NAMES[$index]}" "$tool" >&2
    return 1
  }

  version="$(read_manifest_version "$index")" || return 1
  ORIGINAL_VERSIONS[$index]="$version"
  printf '  ✓ %-14s 清单版本 %s\n' "${COMPONENT_NAMES[$index]}" "$version"
}

check_component() {
  local index="$1"
  printf '  → 检查 %s\n' "${COMPONENT_NAMES[$index]}"
  run_product_tool "$index" check
}

set_component_version() {
  local index="$1"
  local version="$2"
  run_product_tool "$index" "${SET_VERSION_COMMANDS[$index]}" "$version"
}

rollback_components() {
  local index
  local rollback_failed=0
  local expected
  local actual

  [ "$TRANSACTION_ACTIVE" -eq 1 ] || return 0
  if [ "$ROLLBACK_LAST_INDEX" -lt 0 ]; then
    TRANSACTION_ACTIVE=0
    return 0
  fi

  printf '\n正在按相反顺序恢复已尝试更新的组件：\n' >&2
  index="$ROLLBACK_LAST_INDEX"
  while [ "$index" -ge 0 ]; do
    expected="${ORIGINAL_VERSIONS[$index]}"
    printf '  → 恢复 %s 到 %s\n' "${COMPONENT_NAMES[$index]}" "$expected" >&2
    actual="$(read_manifest_version "$index")" || actual=""
    if [ -n "$actual" ] && [ "$actual" != "$expected" ] && [ "$actual" != "$TARGET_VERSION" ]; then
      printf '  ✗ %s 当前版本已变为 %s，未覆盖事务外的并发修改\n' "${COMPONENT_NAMES[$index]}" "$actual" >&2
      rollback_failed=1
      index=$((index - 1))
      continue
    fi
    if ! set_component_version "$index" "$expected"; then
      printf '  ✗ %s 恢复命令执行失败\n' "${COMPONENT_NAMES[$index]}" >&2
      rollback_failed=1
      index=$((index - 1))
      continue
    fi
    actual="$(read_manifest_version "$index")" || actual=""
    if [ "$actual" != "$expected" ] || ! run_product_tool "$index" check; then
      printf '  ✗ %s 恢复后校验失败\n' "${COMPONENT_NAMES[$index]}" >&2
      rollback_failed=1
    else
      printf '  ✓ %s 已恢复\n' "${COMPONENT_NAMES[$index]}" >&2
    fi
    index=$((index - 1))
  done

  TRANSACTION_ACTIVE=0
  ROLLBACK_LAST_INDEX=-1
  return "$rollback_failed"
}

handle_unexpected_exit() {
  local status="$1"

  trap - EXIT HUP INT TERM
  if [ "$status" -ne 0 ] && [ "$TRANSACTION_ACTIVE" -eq 1 ]; then
    printf '\n版本升级意外中止，正在恢复已尝试更新的组件。\n' >&2
    if ! rollback_components; then
      printf '警告：意外中止后的自动恢复不完整，请人工核对。\n' >&2
    fi
  fi
  exit "$status"
}

abort_transaction() {
  local reason="$1"
  printf '\n错误：%s\n' "$reason" >&2
  trap - EXIT HUP INT TERM
  if rollback_components; then
    printf '已恢复到升级前的组件版本。\n' >&2
  else
    printf '警告：自动恢复不完整，请根据上方失败项人工核对。\n' >&2
  fi
  exit 1
}

handle_signal() {
  local status="$1"
  local signal_name="$2"
  printf '\n收到 %s，停止版本升级。\n' "$signal_name" >&2
  trap - EXIT HUP INT TERM
  if ! rollback_components; then
    printf '警告：中断后的自动恢复不完整，请人工核对。\n' >&2
  fi
  exit "$status"
}

main() {
  local new_version
  local index
  local current_version

  if [ "$#" -ne 1 ]; then
    printf '错误：请提供 check 或一个目标版本。\n' >&2
    usage >&2
    return 2
  fi

  new_version="$1"
  if [ "$new_version" = "check" ]; then
    printf 'ThingLinks 五组件产品配置检查\n\n'
    printf '第一步：检查产品清单与工具\n'
    for ((index = 0; index < ${#COMPONENT_NAMES[@]}; index += 1)); do
      preflight_component "$index" || return 1
    done

    printf '\n第二步：执行全部组件的只读检查\n'
    for ((index = 0; index < ${#COMPONENT_NAMES[@]}; index += 1)); do
      if ! check_component "$index"; then
        printf '错误：%s 检查失败。\n' "${COMPONENT_NAMES[$index]}" >&2
        return 1
      fi
    done
    printf '\n✓ 五个组件的产品配置均通过检查。\n'
    return 0
  fi

  if ! validate_version "$new_version"; then
    printf '错误：版本号格式不正确：%s\n' "$new_version" >&2
    printf '支持 X.Y.Z、X.Y.Z.N 以及带预发布后缀的格式。\n' >&2
    return 2
  fi

  printf 'ThingLinks 五组件统一版本升级\n'
  printf '目标版本：%s\n\n' "$new_version"
  printf '第一步：检查产品清单与工具\n'
  for ((index = 0; index < ${#COMPONENT_NAMES[@]}; index += 1)); do
    preflight_component "$index" || return 1
  done

  printf '\n第二步：执行全部组件的只读检查\n'
  for ((index = 0; index < ${#COMPONENT_NAMES[@]}; index += 1)); do
    if ! check_component "$index"; then
      printf '错误：%s 预检失败，尚未写入任何组件。\n' "${COMPONENT_NAMES[$index]}" >&2
      return 1
    fi
  done

  TARGET_VERSION="$new_version"
  TRANSACTION_ACTIVE=1
  ROLLBACK_LAST_INDEX=-1
  trap 'handle_unexpected_exit $?' EXIT
  trap 'handle_signal 129 HUP' HUP
  trap 'handle_signal 130 INT' INT
  trap 'handle_signal 143 TERM' TERM

  printf '\n第三步：通过各组件产品配置工具更新版本\n'
  for ((index = 0; index < ${#COMPONENT_NAMES[@]}; index += 1)); do
    current_version="$(read_manifest_version "$index")" || abort_transaction "无法重新读取 ${COMPONENT_NAMES[$index]} 产品清单"
    if [ "$current_version" != "${ORIGINAL_VERSIONS[$index]}" ]; then
      abort_transaction "${COMPONENT_NAMES[$index]} 产品清单在预检后发生变化，已停止以避免覆盖并发修改"
    fi

    printf '  → %s：%s → %s\n' "${COMPONENT_NAMES[$index]}" "$current_version" "$new_version"
    ROLLBACK_LAST_INDEX="$index"
    if ! set_component_version "$index" "$new_version"; then
      abort_transaction "${COMPONENT_NAMES[$index]} 版本更新失败"
    fi
    current_version="$(read_manifest_version "$index")" || abort_transaction "无法读取更新后的 ${COMPONENT_NAMES[$index]} 产品清单"
    if [ "$current_version" != "$new_version" ]; then
      abort_transaction "${COMPONENT_NAMES[$index]} 产品清单未写入目标版本"
    fi
  done

  printf '\n第四步：逐项执行最终检查\n'
  for ((index = 0; index < ${#COMPONENT_NAMES[@]}; index += 1)); do
    if ! check_component "$index"; then
      abort_transaction "${COMPONENT_NAMES[$index]} 最终检查失败"
    fi
    current_version="$(read_manifest_version "$index")" || abort_transaction "无法读取最终的 ${COMPONENT_NAMES[$index]} 产品清单"
    if [ "$current_version" != "$new_version" ]; then
      abort_transaction "${COMPONENT_NAMES[$index]} 最终清单版本与目标版本不一致"
    fi
  done

  TRANSACTION_ACTIVE=0
  ROLLBACK_LAST_INDEX=-1
  trap - EXIT HUP INT TERM

  printf '\n✓ 五个组件均已更新并通过检查，当前版本为 %s。\n' "$new_version"
  printf 'ThingLinks Util 采用独立版本周期，本脚本不会修改各组件清单中的 Util 依赖版本。\n'
  printf '可使用 git diff -- scripts/bump-version.sh thinglinks-cloud thinglinks-job bifromq-plugin thinglinks-web thinglinks-web-visualize 查看变更。\n'
}

main "$@"
