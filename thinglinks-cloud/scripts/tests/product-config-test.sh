#!/usr/bin/env bash

set -euo pipefail

SOURCE_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
FIXTURE_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/thinglinks-product-config-test.XXXXXX")"
trap 'rm -rf "$FIXTURE_ROOT"' EXIT

copy_file() {
  local relative_path="$1"
  mkdir -p "$FIXTURE_ROOT/$(dirname "$relative_path")"
  cp "$SOURCE_ROOT/$relative_path" "$FIXTURE_ROOT/$relative_path"
}

fail_test() {
  printf '测试失败：%s\n' "$1" >&2
  exit 1
}

managed_fingerprint() {
  shasum -a 256 \
    "$FIXTURE_ROOT/.thinglinks-product.env" \
    "$FIXTURE_ROOT/pom.xml" \
    "$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml" \
    "$FIXTURE_ROOT/thinglinks-sdk/pom.xml" \
    "$FIXTURE_ROOT/thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/mq/ConsumerGroupConstant.java"
}

file_mode() {
  if [ "$(uname -s)" = "Darwin" ]; then
    stat -f '%Lp' "$1"
  else
    stat -c '%a' "$1"
  fi
}

replace_manifest_value() {
  local key="$1"
  local value="$2"
  THINGLINKS_TEST_KEY="$key" THINGLINKS_TEST_VALUE="$value" perl -0pi -e '
    s/^\Q$ENV{THINGLINKS_TEST_KEY}\E=.*$/$ENV{THINGLINKS_TEST_KEY}."=".$ENV{THINGLINKS_TEST_VALUE}/me
  ' "$FIXTURE_ROOT/.thinglinks-product.env"
}

read_manifest_value() {
  local key="$1"
  THINGLINKS_TEST_KEY="$key" perl -ne '
    if (/^\Q$ENV{THINGLINKS_TEST_KEY}\E=(.*)$/) {
      $value = $1;
      $value =~ s/^"//;
      $value =~ s/"$//;
      print $value;
      $count++;
    }
    END { exit($count == 1 ? 0 : 1); }
  ' "$FIXTURE_ROOT/.thinglinks-product.env"
}

for relative_path in \
  .thinglinks-product.env \
  pom.xml \
  scripts/product-config.sh \
  thinglinks-dependencies-parent/pom.xml \
  thinglinks-sdk/pom.xml \
  thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/mq/ConsumerGroupConstant.java \
  thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/mq/BizMqRouteConstant.java \
  thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/MqttKafkaInboundConsumer.java \
  thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/TcpKafkaInboundConsumer.java \
  thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/WsKafkaInboundConsumer.java \
  thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/audit/MqttSessionStartAuditConsumer.java \
  thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/audit/MqttSessionStopAuditConsumer.java \
  thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/audit/NotAuthorizedClientAuditConsumer.java; do
  copy_file "$relative_path"
done
base_license_source="$SOURCE_ROOT/LICENSE"
if [ ! -f "$base_license_source" ]; then
  base_license_source="$SOURCE_ROOT/../LICENSE"
fi
cp "$base_license_source" "$FIXTURE_ROOT/LICENSE"
printf '%s\n' '测试夹具商业授权文件。' > "$FIXTURE_ROOT/LICENSE-COMMERCIAL"
printf '%s\n' '# 测试夹具变更记录' > "$FIXTURE_ROOT/CHANGELOG.md"
mkdir -p "$FIXTURE_ROOT/changelogs"
cp -R "$SOURCE_ROOT/changelogs/." "$FIXTURE_ROOT/changelogs/"
replace_manifest_value THINGLINKS_LICENSE_FILE LICENSE-COMMERCIAL
replace_manifest_value THINGLINKS_SYNC_PROTECTED_PATHS \
  '.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL,CHANGELOG.md,changelogs'
replace_manifest_value THINGLINKS_MARKER_SCAN_EXCLUDED_PATHS \
  'changelogs/product-config-migration.md'
chmod +x "$FIXTURE_ROOT/scripts/product-config.sh"

git -C "$FIXTURE_ROOT" init -q
git -C "$FIXTURE_ROOT" config user.name ProductConfigTest
git -C "$FIXTURE_ROOT" config user.email product-config-test@localhost
git -C "$FIXTURE_ROOT" add -A
git -C "$FIXTURE_ROOT" commit -qm 'fixture'

"$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null

BASELINE_COMPONENT_VERSION="$(read_manifest_value THINGLINKS_COMPONENT_VERSION)"
BASELINE_UTIL_VERSION="$(read_manifest_value THINGLINKS_UTIL_VERSION)"
BASELINE_ARTIFACT_ID="$(read_manifest_value THINGLINKS_MAVEN_ARTIFACT_ID)"
BASELINE_COMPONENT_NAME="$(read_manifest_value THINGLINKS_COMPONENT_NAME)"
TEST_NEW_COMPONENT_VERSION='9.8.7.6'
TEST_ROLLBACK_COMPONENT_VERSION='9.8.7.7'
TEST_LOCK_LOSS_COMPONENT_VERSION='9.8.7.8'
TEST_MID_LOCK_LOSS_COMPONENT_VERSION='9.8.7.9'

PRODUCT_LOCK_REF='refs/thinglinks/product-config-lock/thinglinks-cloud'

create_product_lock_owner() {
  printf '%s\n' "$1" | git -C "$FIXTURE_ROOT" hash-object -w --stdin
}

set_product_lock_ref() {
  local owner_oid="$1"
  git -C "$FIXTURE_ROOT" update-ref "$PRODUCT_LOCK_REF" "$owner_oid" ''
}

product_lock_ref_oid() {
  git -C "$FIXTURE_ROOT" rev-parse --verify "$PRODUCT_LOCK_REF"
}

product_lock_ref_exists() {
  git -C "$FIXTURE_ROOT" show-ref --verify --quiet "$PRODUCT_LOCK_REF"
}

manifest_backup="$FIXTURE_ROOT/changelogs/.thinglinks-product.env.test-backup"
lock_error="$FIXTURE_ROOT/changelogs/.thinglinks-product-lock.test-error"
cp "$FIXTURE_ROOT/.thinglinks-product.env" "$manifest_backup"
replace_manifest_value THINGLINKS_COMPONENT_VERSION invalid-version
live_lock_oid="$(create_product_lock_owner "$$ live-test $(date +%s)")"
set_product_lock_ref "$live_lock_oid"
if "$FIXTURE_ROOT/scripts/product-config.sh" render >"$lock_error" 2>&1; then
  fail_test "已有写操作时 render 必须被拒绝"
fi
grep -Fq '正在执行' "$lock_error" \
  || fail_test "render 必须先检查写锁，再读取产品清单"
[ "$(product_lock_ref_oid)" = "$live_lock_oid" ] \
  || fail_test "当前进程持有的写锁不得被其他进程删除"
if "$FIXTURE_ROOT/scripts/product-config.sh" set-component-version "$TEST_NEW_COMPONENT_VERSION" >"$lock_error" 2>&1; then
  fail_test "已有写操作时 set-component-version 必须被拒绝"
fi
grep -Fq '正在执行' "$lock_error" \
  || fail_test "set-component-version 必须先检查写锁，再读取产品清单"
[ "$(product_lock_ref_oid)" = "$live_lock_oid" ] \
  || fail_test "版本写操作不得删除其他进程持有的写锁"
git -C "$FIXTURE_ROOT" update-ref -d "$PRODUCT_LOCK_REF" "$live_lock_oid"
rm -f "$lock_error"
mv "$manifest_backup" "$FIXTURE_ROOT/.thinglinks-product.env"

incomplete_lock_oid="$(create_product_lock_owner "$$ incomplete-owner")"
set_product_lock_ref "$incomplete_lock_oid"
if "$FIXTURE_ROOT/scripts/product-config.sh" render >/dev/null 2>&1; then
  fail_test "格式不完整的锁所有者信息不得被当作失效锁删除"
fi
[ "$(product_lock_ref_oid)" = "$incomplete_lock_oid" ] \
  || fail_test "格式不完整的锁所有者信息必须保留供人工检查"
git -C "$FIXTURE_ROOT" update-ref -d "$PRODUCT_LOCK_REF" "$incomplete_lock_oid"

stale_lock_oid="$(create_product_lock_owner '99999999 stale-test 0')"
set_product_lock_ref "$stale_lock_oid"
"$FIXTURE_ROOT/scripts/product-config.sh" render >/dev/null \
  || fail_test "失效写锁应被安全回收"
if product_lock_ref_exists; then
  fail_test "成功 render 后不得残留写锁引用"
fi

stale_lock_owner='99999999 stale-before-owner-change 0'
stale_lock_oid="$(create_product_lock_owner "$stale_lock_owner")"
replacement_lock_owner="$$ replacement-live $(date +%s)"
replacement_lock_oid="$(create_product_lock_owner "$replacement_lock_owner")"
set_product_lock_ref "$stale_lock_oid"
if (
  source "$FIXTURE_ROOT/scripts/product-config.sh"
  read_product_lock_owner() {
    local owner_oid="$1"
    if [ "$owner_oid" = "$stale_lock_oid" ]; then
      git -C "$PROJECT_ROOT" update-ref "$PRODUCT_LOCK_REF" "$replacement_lock_oid" "$stale_lock_oid"
      printf '%s\n' "$stale_lock_owner"
    else
      git -C "$PROJECT_ROOT" cat-file blob "$owner_oid"
    fi
  }
  acquire_product_lock
) >"$lock_error" 2>&1; then
  fail_test "回收检查期间锁归属变化时写操作必须被拒绝"
fi
grep -Fq '正在执行' "$lock_error" \
  || fail_test "锁归属变化后必须识别后来出现的活动写锁"
[ "$(product_lock_ref_oid)" = "$replacement_lock_oid" ] \
  || fail_test "CAS 回收不得删除后来出现的活动写锁"
git -C "$FIXTURE_ROOT" update-ref -d "$PRODUCT_LOCK_REF" "$replacement_lock_oid"
rm -f "$lock_error"

lock_loss_tmp="$FIXTURE_ROOT/lock-loss-tmp"
mkdir -p "$lock_loss_tmp"
root_lock_loss_backup="$FIXTURE_ROOT/changelogs/.root-pom.lock-loss-backup"
dependencies_lock_loss_backup="$FIXTURE_ROOT/changelogs/.dependencies-pom.lock-loss-backup"
sdk_lock_loss_backup="$FIXTURE_ROOT/changelogs/.sdk-pom.lock-loss-backup"
for lock_loss_after_write in 1 2; do
  cp -p "$FIXTURE_ROOT/pom.xml" "$root_lock_loss_backup"
  cp -p "$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml" "$dependencies_lock_loss_backup"
  cp -p "$FIXTURE_ROOT/thinglinks-sdk/pom.xml" "$sdk_lock_loss_backup"
  replacement_lock_owner="$$ replacement-mid-transaction-$lock_loss_after_write $(date +%s)"
  replacement_lock_oid="$(create_product_lock_owner "$replacement_lock_owner")"
  if [ "$lock_loss_after_write" -eq 1 ]; then
    lock_loss_sentinel_target="$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml"
  else
    lock_loss_sentinel_target="$FIXTURE_ROOT/thinglinks-sdk/pom.xml"
  fi
  lock_loss_sentinel="new-owner-sentinel-after-write-$lock_loss_after_write"

  if (
    export TMPDIR="$lock_loss_tmp"
    source "$FIXTURE_ROOT/scripts/product-config.sh"
    acquire_product_lock
    load_manifest
    check_required_values
    THINGLINKS_COMPONENT_VERSION="$TEST_MID_LOCK_LOSS_COMPONENT_VERSION"
    THINGLINKS_UTIL_VERSION="$TEST_MID_LOCK_LOSS_COMPONENT_VERSION"
    managed_write_count=0
    after_managed_file_replaced() {
      managed_write_count=$((managed_write_count + 1))
      if [ "$managed_write_count" -eq "$lock_loss_after_write" ]; then
        git -C "$PROJECT_ROOT" update-ref "$PRODUCT_LOCK_REF" "$replacement_lock_oid" "$PRODUCT_LOCK_OID"
        printf '%s\n' "$lock_loss_sentinel" > "$lock_loss_sentinel_target"
      fi
    }
    transactional_render
  ) >"$lock_error" 2>&1; then
    fail_test "第 $lock_loss_after_write 个受管文件写入后失锁时旧事务必须失败"
  fi

  grep -Fq "$lock_loss_sentinel" "$lock_loss_sentinel_target" \
    || fail_test "失锁后的旧事务不得覆盖新 owner 写入的 sentinel：$lock_loss_after_write"
  grep -Fq '安全停止' "$lock_error" \
    || fail_test "中途失锁必须返回安全停止语义：$lock_loss_after_write"
  [ "$(product_lock_ref_oid)" = "$replacement_lock_oid" ] \
    || fail_test "中途失锁后必须保留新的活动写锁：$lock_loss_after_write"
  if find "$lock_loss_tmp" -mindepth 1 -print -quit | grep -q .; then
    fail_test "中途失锁后不得残留 render 或 backup 临时目录：$lock_loss_after_write"
  fi
  if find "$FIXTURE_ROOT" -type f \
    \( -name '.thinglinks-product.*' ! -name '.thinglinks-product.env' -o -name '.thinglinks-manifest.*' \) \
    -print -quit | grep -q .; then
    fail_test "中途失锁后不得残留受管文件旁的原子临时文件：$lock_loss_after_write"
  fi

  git -C "$FIXTURE_ROOT" update-ref -d "$PRODUCT_LOCK_REF" "$replacement_lock_oid"
  cp -p "$root_lock_loss_backup" "$FIXTURE_ROOT/pom.xml"
  cp -p "$dependencies_lock_loss_backup" "$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml"
  cp -p "$sdk_lock_loss_backup" "$FIXTURE_ROOT/thinglinks-sdk/pom.xml"
  rm -f "$root_lock_loss_backup" "$dependencies_lock_loss_backup" "$sdk_lock_loss_backup" "$lock_error"
done
rmdir "$lock_loss_tmp"
"$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null \
  || fail_test "中途失锁测试恢复后产品配置必须保持一致"

replacement_lock_owner="$$ replacement-after-claim $(date +%s)"
replacement_lock_oid="$(create_product_lock_owner "$replacement_lock_owner")"
before_lock_loss="$(managed_fingerprint)"
if (
  source "$FIXTURE_ROOT/scripts/product-config.sh"
  acquire_product_lock
  git -C "$PROJECT_ROOT" update-ref "$PRODUCT_LOCK_REF" "$replacement_lock_oid" "$PRODUCT_LOCK_OID"
  transactional_set_version THINGLINKS_COMPONENT_VERSION Cloud "$TEST_LOCK_LOSS_COMPONENT_VERSION"
) >"$lock_error" 2>&1; then
  fail_test "获得写锁后归属被替换时旧写操作必须失败"
fi
after_lock_loss="$(managed_fingerprint)"
[ "$before_lock_loss" = "$after_lock_loss" ] \
  || fail_test "失去写锁的进程不得修改产品清单或受管文件"
grep -Fq '归属发生变化' "$lock_error" \
  || fail_test "写锁归属变化必须返回明确错误"
[ "$(product_lock_ref_oid)" = "$replacement_lock_oid" ] \
  || fail_test "旧写操作释放锁时不得删除新的活动写锁"
git -C "$FIXTURE_ROOT" update-ref -d "$PRODUCT_LOCK_REF" "$replacement_lock_oid"
rm -f "$lock_error"

before_read="$(managed_fingerprint)"
expected_component_version="$(perl -0ne 'print $1 if /<revision>([^<]+)<\/revision>/' \
  "$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml")"
component_version="$("$FIXTURE_ROOT/scripts/product-config.sh" get-component-version)" \
  || fail_test "组件版本读取命令执行失败"
[ -n "$expected_component_version" ] || fail_test "测试夹具中缺少组件版本"
[ "$component_version" = "$expected_component_version" ] || fail_test "组件版本读取命令返回值不正确"
after_read="$(managed_fingerprint)"
[ "$before_read" = "$after_read" ] || fail_test "组件版本读取命令不得修改受管文件"

before_invalid="$(managed_fingerprint)"
if "$FIXTURE_ROOT/scripts/product-config.sh" set-component-version '1.0.bad version' >/dev/null 2>&1; then
  fail_test "非法版本号必须被拒绝"
fi
after_invalid="$(managed_fingerprint)"
[ "$before_invalid" = "$after_invalid" ] || fail_test "非法版本号污染了清单或生成文件"

marker_suffix='p''ro'
camel_marker_suffix='P''ro'
mkdir -p "$FIXTURE_ROOT/notes"
untracked_marker="$FIXTURE_ROOT/notes/version-marker.md"
camel_marker="Web${camel_marker_suffix}Config"
printf 'temporary %s marker\n' "$camel_marker" > "$untracked_marker"
if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
  fail_test "未跟踪文件中的驼峰发行标识必须被识别"
fi

before_failed_render="$(managed_fingerprint)"
if "$FIXTURE_ROOT/scripts/product-config.sh" set-component-version "$TEST_ROLLBACK_COMPONENT_VERSION" >/dev/null 2>&1; then
  fail_test "发行边界检查失败时版本更新必须失败"
fi
after_failed_render="$(managed_fingerprint)"
[ "$before_failed_render" = "$after_failed_render" ] || fail_test "渲染后检查失败时没有完整回滚"
if product_lock_ref_exists; then
  fail_test "失败的版本写操作不得残留写锁引用"
fi
rm -f "$untracked_marker"

tracked_marker="$FIXTURE_ROOT/notes/tracked-version-marker.md"
flagship_marker='旗''舰版'
commercial_marker='商''业版'
community_marker='社''区版'
open_source_marker='开''源版'
printf '%s\n' \
  "thinglinks-job-${marker_suffix}" \
  "getWeb${camel_marker_suffix}Config" \
  "cloud${camel_marker_suffix}Datasource" \
  "$flagship_marker" \
  "$commercial_marker" \
  "$community_marker" \
  "$open_source_marker" \
  "Commercial Ed"'ition' \
  "Enterprise Ed"'ition' \
  "Community Ed"'ition' \
  "Pro Ed"'ition' \
  "Professional Ed"'ition' \
  "Open Source Ed"'ition' > "$tracked_marker"
git -C "$FIXTURE_ROOT" add "$tracked_marker"
if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
  fail_test "已跟踪文件中的完整发行标识集合必须被识别"
fi
git -C "$FIXTURE_ROOT" rm -q -f "$tracked_marker"

bare_web_marker="Web${camel_marker_suffix}"
thinglinks_camel_marker="ThingLinksJob${camel_marker_suffix}"
bifromq_camel_marker="BifromqPlugin${camel_marker_suffix}"
camel_positive_markers=(
  "Web${camel_marker_suffix}Feature"
  "Web${camel_marker_suffix}2"
  "Cloud${camel_marker_suffix}Service"
  "Util${camel_marker_suffix}Starter"
  "ThingLinks${camel_marker_suffix}Edition"
  "ThingLinksJob${camel_marker_suffix}Feature"
  "BifromqPlugin${camel_marker_suffix}Auth"
  "$bare_web_marker"
  "$thinglinks_camel_marker"
  "$bifromq_camel_marker"
)
for boundary_marker in "${camel_positive_markers[@]}"; do
  boundary_file="$FIXTURE_ROOT/notes/camel-boundary-marker.md"
  mkdir -p "$(dirname "$boundary_file")"
  printf '%s\n' "$boundary_marker" > "$boundary_file"
  git -C "$FIXTURE_ROOT" add "$boundary_file"
  if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
    fail_test "驼峰发行标识边界必须被识别：$boundary_marker"
  fi
  git -C "$FIXTURE_ROOT" rm -q -f "$boundary_file"
done

lower_marker_suffix='p''ro'
compact_positive_markers=(
  "web${lower_marker_suffix}"
  "cloud${lower_marker_suffix}"
  "util${lower_marker_suffix}"
  "thinglinkscloud${lower_marker_suffix}"
  "Web${camel_marker_suffix}_config"
  "Cloud${camel_marker_suffix}_service"
  "thinglinks${camel_marker_suffix}Config"
  "thinglinks${lower_marker_suffix}Config"
  "thinglinksJob${camel_marker_suffix}Feature"
  "web${lower_marker_suffix}Config"
  "cloud${lower_marker_suffix}Config"
  "util${lower_marker_suffix}Config"
  "bifromqplugin${lower_marker_suffix}"
  "bifromqplugin${lower_marker_suffix}Config"
)
for compact_marker in "${compact_positive_markers[@]}"; do
  compact_marker_file="$FIXTURE_ROOT/notes/compact-boundary-marker.md"
  mkdir -p "$(dirname "$compact_marker_file")"
  printf '%s\n' "$compact_marker" > "$compact_marker_file"
  git -C "$FIXTURE_ROOT" add "$compact_marker_file"
  if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
    fail_test "紧凑发行标识必须被识别：$compact_marker"
  fi
  git -C "$FIXTURE_ROOT" rm -q -f "$compact_marker_file"

  compact_marker_path="$FIXTURE_ROOT/notes/${compact_marker}.md"
  mkdir -p "$(dirname "$compact_marker_path")"
  printf '%s\n' 'neutral marker path fixture' > "$compact_marker_path"
  git -C "$FIXTURE_ROOT" add "$compact_marker_path"
  if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
    fail_test "文件路径中的紧凑发行标识必须被识别：$compact_marker"
  fi
  git -C "$FIXTURE_ROOT" rm -q -f "$compact_marker_path"
done

edition_marker_one="Pro Ed"'ition'
edition_marker_two="Professional Ed"'ition'
edition_marker_three="Open Source Ed"'ition'
edition_positive_markers=(
  "$edition_marker_one"
  "$edition_marker_two"
  "$edition_marker_three"
)
for edition_marker in "${edition_positive_markers[@]}"; do
  edition_file="$FIXTURE_ROOT/notes/edition-boundary-marker.md"
  mkdir -p "$(dirname "$edition_file")"
  printf '%s\n' "$edition_marker" > "$edition_file"
  git -C "$FIXTURE_ROOT" add "$edition_file"
  if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
    fail_test "英文发行版本名必须被识别：$edition_marker"
  fi
  git -C "$FIXTURE_ROOT" rm -q -f "$edition_file"
done

printf '\n%s\n' "$edition_marker_two" >> "$FIXTURE_ROOT/LICENSE-COMMERCIAL"
"$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null \
  || fail_test "同步保护路径中的授权文本不得触发发行标识扫描"

protocol_marker="$FIXTURE_ROOT/notes/product-professional-protocol-project-process-properties.md"
mkdir -p "$(dirname "$protocol_marker")"
printf '%s\n' \
  'thinglinks-protocol' \
  'WebProduct' \
  'WebProfessional' \
  'WebProtocol' \
  'WebProject' \
  'WebProcess' \
  'WebProperties' \
  'CloudProduct' \
  'CloudProfessional' \
  'CloudProtocol' \
  'CloudProject' \
  'CloudProcess' \
  'CloudProperties' \
  'UtilProduct' \
  'UtilProfessional' \
  'UtilProtocol' \
  'UtilProject' \
  'UtilProcess' \
  'UtilProperties' \
  'ThingLinksProduct' \
  'ThingLinksProfessional' \
  'ThingLinksProtocol' \
  'ThingLinksProject' \
  'ThingLinksProcess' \
  'ThingLinksProperties' \
  'ThingLinksJobProduct' \
  'ThingLinksJobProfessional' \
  'ThingLinksJobProtocol' \
  'ThingLinksJobProject' \
  'ThingLinksJobProcess' \
  'ThingLinksJobProperties' \
  'BifromqPluginProduct' \
  'BifromqPluginProfessional' \
  'BifromqPluginProtocol' \
  'BifromqPluginProject' \
  'BifromqPluginProcess' \
  'BifromqPluginProperties' > "$protocol_marker"
git -C "$FIXTURE_ROOT" add "$protocol_marker"
"$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null \
  || fail_test "Product、Professional、Protocol、Project、Process、Properties 不得被误判为发行标识"

allowed_identity_names=(
  'ThingLinks Professional Platform'
  'ThingLinks Product Platform'
  'ThingLinks Protocol Platform'
)
for allowed_identity_name in "${allowed_identity_names[@]}"; do
  cp "$FIXTURE_ROOT/.thinglinks-product.env" "$manifest_backup"
  replace_manifest_value THINGLINKS_PRODUCT_NAME "\"$allowed_identity_name\""
  "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null \
    || fail_test "普通产品身份词不得被误判为发行标识：$allowed_identity_name"
  mv "$manifest_backup" "$FIXTURE_ROOT/.thinglinks-product.env"
done

compact_identity_values=(
  "THINGLINKS_PRODUCT_CODE:thinglinks${lower_marker_suffix}"
  'THINGLINKS_MQ_NAMESPACE:cloudcommunity'
)
for compact_identity_entry in "${compact_identity_values[@]}"; do
  compact_identity_key="${compact_identity_entry%%:*}"
  compact_identity_value="${compact_identity_entry#*:}"
  cp "$FIXTURE_ROOT/.thinglinks-product.env" "$manifest_backup"
  replace_manifest_value "$compact_identity_key" "$compact_identity_value"
  if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
    fail_test "紧凑发行标识必须被识别：$compact_identity_value"
  fi
  mv "$manifest_backup" "$FIXTURE_ROOT/.thinglinks-product.env"
done

protected_path_invalid_values=(
  'LICENSE,LICENSE-COMMERCIAL,CHANGELOG.md,changelogs'
  '.thinglinks-product.env,LICENSE-COMMERCIAL,CHANGELOG.md,changelogs'
  '.thinglinks-product.env,LICENSE,CHANGELOG.md,changelogs'
  '.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL,CHANGELOG.md,changelogs,missing-path'
  '.thinglinks-product.env,LICENSE,LICENSE,LICENSE-COMMERCIAL,CHANGELOG.md,changelogs'
  '.thinglinks-product.env,./LICENSE,LICENSE-COMMERCIAL,CHANGELOG.md,changelogs'
  '.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL,changelogs/../CHANGELOG.md,changelogs'
  '.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL,/CHANGELOG.md,changelogs'
  '.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL,CHANGELOG*.md,changelogs'
  '.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL,CHANGELOG.md,changelogs/'
  '.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL,CHANGELOG.md,changelogs,'
)
for protected_path_value in "${protected_path_invalid_values[@]}"; do
  cp "$FIXTURE_ROOT/.thinglinks-product.env" "$manifest_backup"
  replace_manifest_value THINGLINKS_SYNC_PROTECTED_PATHS "$protected_path_value"
  if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
    fail_test "非法同步保护路径必须被拒绝：$protected_path_value"
  fi
  mv "$manifest_backup" "$FIXTURE_ROOT/.thinglinks-product.env"
done
"$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null \
  || fail_test "存在的文件和目录应可作为同步保护路径"

valid_edition_license_pairs=(
  'community:open-source'
  'commercial:commercial'
  'commercial:dual-license'
  'enterprise:commercial'
  'enterprise:dual-license'
)
for edition_license_pair in "${valid_edition_license_pairs[@]}"; do
  cp "$FIXTURE_ROOT/.thinglinks-product.env" "$manifest_backup"
  replace_manifest_value THINGLINKS_EDITION_CODE "${edition_license_pair%%:*}"
  replace_manifest_value THINGLINKS_LICENSE_MODEL "${edition_license_pair#*:}"
  "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null \
    || fail_test "合法发行版本与授权模型组合应被接受：$edition_license_pair"
  mv "$manifest_backup" "$FIXTURE_ROOT/.thinglinks-product.env"
done

invalid_edition_license_pairs=(
  'community:commercial'
  'community:dual-license'
  'commercial:open-source'
  'enterprise:open-source'
)
for edition_license_pair in "${invalid_edition_license_pairs[@]}"; do
  cp "$FIXTURE_ROOT/.thinglinks-product.env" "$manifest_backup"
  replace_manifest_value THINGLINKS_EDITION_CODE "${edition_license_pair%%:*}"
  replace_manifest_value THINGLINKS_LICENSE_MODEL "${edition_license_pair#*:}"
  if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
    fail_test "不兼容的发行版本与授权模型组合必须被拒绝：$edition_license_pair"
  fi
  mv "$manifest_backup" "$FIXTURE_ROOT/.thinglinks-product.env"
done

for stable_edition_marker in "$edition_marker_two" "$edition_marker_three"; do
  cp "$FIXTURE_ROOT/.thinglinks-product.env" "$manifest_backup"
  replace_manifest_value THINGLINKS_PRODUCT_NAME "\"ThingLinks $stable_edition_marker\""
  if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
    fail_test "稳定产品名称不得包含英文发行版本名：$stable_edition_marker"
  fi
  mv "$manifest_backup" "$FIXTURE_ROOT/.thinglinks-product.env"
done

cp "$FIXTURE_ROOT/.thinglinks-product.env" "$manifest_backup"
replace_manifest_value THINGLINKS_LICENSE_MODEL dual
if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
  fail_test "非标准授权模型 dual 必须被拒绝"
fi
replace_manifest_value THINGLINKS_LICENSE_MODEL dual-license
replace_manifest_value THINGLINKS_EDITION_CODE commercial
"$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null \
  || fail_test "标准授权模型 dual-license 应被接受"
mv "$manifest_backup" "$FIXTURE_ROOT/.thinglinks-product.env"

root_pom_backup="$FIXTURE_ROOT/changelogs/.root-pom.test-backup"
cp "$FIXTURE_ROOT/pom.xml" "$root_pom_backup"
THINGLINKS_EXPECTED_ARTIFACT_ID="$BASELINE_ARTIFACT_ID" \
  THINGLINKS_EXPECTED_COMPONENT_NAME="$BASELINE_COMPONENT_NAME" perl -0pi -e '
  s{(<artifactId>)\Q$ENV{THINGLINKS_EXPECTED_ARTIFACT_ID}\E(</artifactId>)}{$1."wrong-cloud".$2}e;
  s{(<description>)\Q$ENV{THINGLINKS_EXPECTED_COMPONENT_NAME}\E(</description>)}{$1."Wrong Cloud".$2}e;
  s{</project>}{"<!-- <artifactId>".$ENV{THINGLINKS_EXPECTED_ARTIFACT_ID}."</artifactId> -->\n".
    "<!-- <description>".$ENV{THINGLINKS_EXPECTED_COMPONENT_NAME}."</description> -->\n</project>"}e;
' "$FIXTURE_ROOT/pom.xml"
if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
  fail_test "根 POM 的注释标签不得伪装组件坐标或名称"
fi
mv "$root_pom_backup" "$FIXTURE_ROOT/pom.xml"

dependencies_pom_backup="$FIXTURE_ROOT/changelogs/.dependencies-pom.test-backup"
cp "$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml" "$dependencies_pom_backup"
THINGLINKS_EXPECTED_UTIL_VERSION="$BASELINE_UTIL_VERSION" perl -0pi -e '
  s{(<artifactId>thinglinks-parent</artifactId>\s*<version>)[^<]+(</version>)}{$1."0.0.0".$2}e;
  s{</project>}{"<!-- <artifactId>thinglinks-parent</artifactId><version>".
    $ENV{THINGLINKS_EXPECTED_UTIL_VERSION}."</version> -->\n</project>"}e;
' "$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml"
if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
  fail_test "依赖父 POM 的注释标签不得伪装 parent 版本"
fi
mv "$dependencies_pom_backup" "$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml"

cp "$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml" "$dependencies_pom_backup"
THINGLINKS_EXPECTED_COMPONENT_VERSION="$BASELINE_COMPONENT_VERSION" \
  THINGLINKS_EXPECTED_UTIL_VERSION="$BASELINE_UTIL_VERSION" perl -0pi -e '
  s{(<revision>)\Q$ENV{THINGLINKS_EXPECTED_COMPONENT_VERSION}\E(</revision>)}{$1."0.0.0".$2}e;
  s{(<thinglinks-util\.version>)\Q$ENV{THINGLINKS_EXPECTED_UTIL_VERSION}\E(</thinglinks-util\.version>)}{$1."0.0.0".$2}e;
  s{</project>}{"<!-- <revision>".$ENV{THINGLINKS_EXPECTED_COMPONENT_VERSION}."</revision> -->\n".
    "<!-- <thinglinks-util.version>".$ENV{THINGLINKS_EXPECTED_UTIL_VERSION}."</thinglinks-util.version> -->\n</project>"}e;
' "$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml"
if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
  fail_test "依赖父 POM 的注释标签不得伪装属性版本"
fi
mv "$dependencies_pom_backup" "$FIXTURE_ROOT/thinglinks-dependencies-parent/pom.xml"

sdk_pom_backup="$FIXTURE_ROOT/changelogs/.sdk-pom.test-backup"
cp "$FIXTURE_ROOT/thinglinks-sdk/pom.xml" "$sdk_pom_backup"
THINGLINKS_EXPECTED_COMPONENT_VERSION="$BASELINE_COMPONENT_VERSION" perl -0pi -e '
  s{(<revision>)\Q$ENV{THINGLINKS_EXPECTED_COMPONENT_VERSION}\E(</revision>)}{$1."0.0.0".$2}e;
  s{</project>}{"<!-- <revision>".$ENV{THINGLINKS_EXPECTED_COMPONENT_VERSION}."</revision> -->\n</project>"}e;
' "$FIXTURE_ROOT/thinglinks-sdk/pom.xml"
if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
  fail_test "SDK POM 的注释标签不得伪装组件版本"
fi
mv "$sdk_pom_backup" "$FIXTURE_ROOT/thinglinks-sdk/pom.xml"

execution_marker="$FIXTURE_ROOT/manifest-must-not-execute"
cp "$FIXTURE_ROOT/.thinglinks-product.env" "$manifest_backup"
replace_manifest_value THINGLINKS_COMPONENT_NAME "\$(touch $execution_marker)"
if "$FIXTURE_ROOT/scripts/product-config.sh" check >/dev/null 2>&1; then
  fail_test "含命令表达式的产品名称必须被拒绝"
fi
[ ! -e "$execution_marker" ] || fail_test "产品清单被当作 Shell 脚本执行"
mv "$manifest_backup" "$FIXTURE_ROOT/.thinglinks-product.env"

manifest_mode_before="$(file_mode "$FIXTURE_ROOT/.thinglinks-product.env")"
"$FIXTURE_ROOT/scripts/product-config.sh" set-component-version "$TEST_NEW_COMPONENT_VERSION" >/dev/null
manifest_mode_after="$(file_mode "$FIXTURE_ROOT/.thinglinks-product.env")"
[ "$manifest_mode_before" = "$manifest_mode_after" ] || fail_test "原子更新不得改变产品清单文件权限"
if product_lock_ref_exists; then
  fail_test "成功的版本写操作不得残留写锁引用"
fi
before_render="$(managed_fingerprint)"
"$FIXTURE_ROOT/scripts/product-config.sh" render >/dev/null
after_render="$(managed_fingerprint)"
[ "$before_render" = "$after_render" ] || fail_test "连续 render 必须幂等"
if product_lock_ref_exists; then
  fail_test "成功 render 后不得残留写锁引用"
fi

replace_manifest_value THINGLINKS_MQ_NAMESPACE thinglinks-next
"$FIXTURE_ROOT/scripts/product-config.sh" render >/dev/null
grep -Fq 'String THINGLINKS_MQ_NAMESPACE = "thinglinks-next";' \
  "$FIXTURE_ROOT/thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/mq/ConsumerGroupConstant.java" \
  || fail_test "消息队列小写命名空间未由清单生成"
grep -Fq 'String THINGLINKS_MQ_NAMESPACE_UPPER = "THINGLINKS_NEXT";' \
  "$FIXTURE_ROOT/thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/mq/ConsumerGroupConstant.java" \
  || fail_test "消息队列大写命名空间未从清单派生"

for consumer_file in \
  "$FIXTURE_ROOT/thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/MqttKafkaInboundConsumer.java" \
  "$FIXTURE_ROOT/thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/TcpKafkaInboundConsumer.java" \
  "$FIXTURE_ROOT/thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/WsKafkaInboundConsumer.java" \
  "$FIXTURE_ROOT/thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/audit/MqttSessionStartAuditConsumer.java" \
  "$FIXTURE_ROOT/thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/audit/MqttSessionStopAuditConsumer.java" \
  "$FIXTURE_ROOT/thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/audit/NotAuthorizedClientAuditConsumer.java"; do
  grep -Fq 'ConsumerGroupConstant.THINGLINKS_CONSUMER_GROUP_PREFIX +' "$consumer_file" \
    || fail_test "Kafka 消费组未引用统一前缀：$consumer_file"
  if grep -q 'CID_THINGLINKS_' "$consumer_file"; then
    fail_test "修改消息队列命名空间后仍残留旧 Kafka 消费组：$consumer_file"
  fi
done

printf '产品配置脚本回归测试通过。\n'
