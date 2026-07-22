#!/usr/bin/env bash

set -euo pipefail

SOURCE_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
TEST_ROOT="$(mktemp -d /tmp/bifromq-product-test.XXXXXX)"
FIXTURE_ROOT="$TEST_ROOT/repository"
TEST_TMP="$TEST_ROOT/tmp"
COMMAND_LOG="$TEST_ROOT/command.log"
BASELINE_DIR="$TEST_ROOT/baseline"
MAVEN_BIN="${MAVEN_CMD:-mvn}"
EVENT_PROVIDER_REL='bifromq-event-collector-plugin/event-provider/src/main/java/com/mqttsnet/thinglinks/BifromqEventCollectorPluginEventProvider.java'
FILES=(
  ".thinglinks-product.env"
  "pom.xml"
  "bifromq-auth-provider-plugin/pom.xml"
  "bifromq-auth-provider-plugin/auth-provider/pom.xml"
  "bifromq-event-collector-plugin/pom.xml"
  "bifromq-resource-throttler-plugin/pom.xml"
  "bifromq-setting-provider-plugin/pom.xml"
  "$EVENT_PROVIDER_REL"
)

fail_test() {
  printf '测试失败：%s\n' "$1" >&2
  exit 1
}

cleanup() {
  rm -rf "$TEST_ROOT"
}
trap cleanup EXIT HUP INT TERM

source_fingerprint() {
  (
    cd "$SOURCE_ROOT"
    git status --porcelain=v1 --untracked-files=all
    git diff --binary
    git diff --cached --binary
    while IFS= read -r -d '' file; do
      if [ -f "$file" ]; then
        printf '%s\n' "$file"
        shasum -a 256 "$file"
      elif [ -L "$file" ]; then
        printf '%s -> %s\n' "$file" "$(readlink "$file")"
      fi
    done < <(git ls-files -co --exclude-standard -z)
  ) | shasum -a 256 | awk '{print $1}'
}

copy_fixture() {
  local file
  mkdir -p "$FIXTURE_ROOT" "$TEST_TMP" "$BASELINE_DIR"
  while IFS= read -r -d '' file; do
    mkdir -p "$FIXTURE_ROOT/$(dirname "$file")"
    if [ -L "$SOURCE_ROOT/$file" ]; then
      cp -P "$SOURCE_ROOT/$file" "$FIXTURE_ROOT/$file"
    elif [ -f "$SOURCE_ROOT/$file" ]; then
      cp -p "$SOURCE_ROOT/$file" "$FIXTURE_ROOT/$file"
    fi
  done < <(git -C "$SOURCE_ROOT" ls-files -co --exclude-standard -z)
  git -C "$FIXTURE_ROOT" init -q
  git -C "$FIXTURE_ROOT" add -f -- .
}

SOURCE_STATE_BEFORE="$(source_fingerprint)"
copy_fixture

PRODUCT_SCRIPT="$FIXTURE_ROOT/scripts/product-config.sh"
MANIFEST="$FIXTURE_ROOT/.thinglinks-product.env"
EVENT_PROVIDER="$FIXTURE_ROOT/$EVENT_PROVIDER_REL"
LOCK_REF='refs/thinglinks/product-config-lock/bifromq-plugin'

for file in "${FILES[@]}"; do
  mkdir -p "$BASELINE_DIR/$(dirname "$file")"
  cp -p "$FIXTURE_ROOT/$file" "$BASELINE_DIR/$file"
done

fingerprint() {
  local file
  for file in "${FILES[@]}"; do
    printf '%s\n' "$file"
    shasum -a 256 "$FIXTURE_ROOT/$file"
  done
}

restore_file() {
  cp -p "$BASELINE_DIR/$1" "$FIXTURE_ROOT/$1"
}

set_manifest_value() {
  local key="$1" value="$2"
  TEST_KEY="$key" TEST_VALUE="$value" perl -0pi -e \
    's/^\Q$ENV{TEST_KEY}\E=.*$/$ENV{TEST_KEY}."=".$ENV{TEST_VALUE}/me' "$MANIFEST"
}

manifest_value() {
  local key="$1" line value count
  count="$(grep -c "^${key}=" "$MANIFEST" || true)"
  [ "$count" -eq 1 ] || fail_test "测试清单中的 $key 必须且只能出现一次"
  line="$(grep "^${key}=" "$MANIFEST")"
  value="${line#*=}"
  if [[ "$value" == \"*\" && "$value" == *\" ]]; then
    value="${value:1:${#value}-2}"
  fi
  printf '%s' "$value"
}

run_product() {
  TMPDIR="$TEST_TMP" "$PRODUCT_SCRIPT" "$@"
}

create_test_lock_blob() {
  local owner_pid="$1" token="$2" created_at=1
  printf 'pid=%s\ntoken=%s\ncreatedAt=%s\n' \
    "$owner_pid" "$token" "$created_at" | \
    git -C "$FIXTURE_ROOT" hash-object -w --stdin
}

create_test_lock_ref() {
  local owner_pid="$1" token="$2" oid
  oid="$(create_test_lock_blob "$owner_pid" "$token")"
  git -C "$FIXTURE_ROOT" update-ref "$LOCK_REF" "$oid" ''
  printf '%s' "$oid"
}

wait_for_file() {
  local file="$1" attempts=0
  while [ ! -e "$file" ]; do
    attempts=$((attempts + 1))
    [ "$attempts" -lt 1000 ] || fail_test "等待测试同步文件超时：$file"
    sleep 0.01
  done
}

assert_fails_without_changes() {
  local before after
  before="$(fingerprint)"
  if run_product "$@" >"$COMMAND_LOG" 2>&1; then
    fail_test "命令本应失败：$*"
  fi
  after="$(fingerprint)"
  [ "$before" = "$after" ] || fail_test "失败命令污染了产品清单、POM 或运行协议：$*"
}

assert_identity_rejected() {
  local key="$1" value="$2"
  set_manifest_value "$key" "$value"
  if run_product check >"$COMMAND_LOG" 2>&1; then
    fail_test "稳定身份中的发行标识没有被拒绝：$key=$value"
  fi
  grep -Fq "稳定身份配置项 $key" "$COMMAND_LOG" || \
    fail_test "稳定身份校验没有在格式校验之前拦截：$key=$value"
  restore_file .thinglinks-product.env
}

assert_manifest_value_rejected() {
  local key="$1" value="$2"
  set_manifest_value "$key" "$value"
  if run_product check >"$COMMAND_LOG" 2>&1; then
    fail_test "非法清单值没有被拒绝：$key=$value"
  fi
  restore_file .thinglinks-product.env
}

assert_edition_license_rejected() {
  local edition="$1" license="$2"
  set_manifest_value THINGLINKS_EDITION_CODE "$edition"
  set_manifest_value THINGLINKS_LICENSE_MODEL "$license"
  if run_product check >"$COMMAND_LOG" 2>&1; then
    fail_test "发行版本与授权模型冲突没有被拒绝：$edition/$license"
  fi
  restore_file .thinglinks-product.env
}

assert_edition_license_accepted() {
  local edition="$1" license="$2"
  set_manifest_value THINGLINKS_EDITION_CODE "$edition"
  set_manifest_value THINGLINKS_LICENSE_MODEL "$license"
  run_product check >"$COMMAND_LOG" 2>&1 || \
    fail_test "合法的发行版本与授权模型组合被拒绝：$edition/$license"
  restore_file .thinglinks-product.env
}

assert_scanner_rejects_content() {
  local marker="$1" probe="$FIXTURE_ROOT/scanner-probe.txt"
  printf '%s\n' "$marker" > "$probe"
  if run_product check >"$COMMAND_LOG" 2>&1; then
    fail_test "扫描器没有拦截发行标识：$marker"
  fi
  rm -f "$probe"
}

assert_scanner_rejects_path() {
  local probe="$FIXTURE_ROOT/$1"
  printf '%s\n' 'neutral' > "$probe"
  if run_product check >"$COMMAND_LOG" 2>&1; then
    fail_test "扫描器没有拦截发行标识路径：$1"
  fi
  rm -f "$probe"
}

assert_current_document_version_rejected() {
  local label="$1" content="$2" probe="$FIXTURE_ROOT/current-version-probe.md"
  printf '%s\n' "$content" > "$probe"
  if run_product check >"$COMMAND_LOG" 2>&1; then
    fail_test "文档中的当前版本硬编码没有被拒绝：$label"
  fi
  grep -Fq '文档包含当前版本硬编码' "$COMMAND_LOG" || \
    fail_test "文档当前版本校验未返回明确错误：$label"
  rm -f "$probe"
}

assert_pom_comment_spoof_rejected() {
  local file="$1" tag="$2" expected="$3" drift="$4"
  TEST_TAG="$tag" TEST_EXPECTED="$expected" TEST_DRIFT="$drift" perl -0pi -e '
    my $tag = quotemeta($ENV{TEST_TAG});
    my $expected = quotemeta($ENV{TEST_EXPECTED});
    my $replacement = "<!-- <$ENV{TEST_TAG}>$ENV{TEST_EXPECTED}</$ENV{TEST_TAG}> -->\n" .
      "<$ENV{TEST_TAG}>$ENV{TEST_DRIFT}</$ENV{TEST_TAG}>";
    s{<$tag>$expected</$tag>}{$replacement} or die "target tag not found\n";
  ' "$FIXTURE_ROOT/$file"
  if run_product check >"$COMMAND_LOG" 2>&1; then
    fail_test "POM 注释中的期望值掩盖了实际漂移：$file/$tag"
  fi
  restore_file "$file"
}

assert_pom_duplicate_tag_rejected() {
  local file="$1" tag="$2" expected="$3" drift="$4"
  TEST_TAG="$tag" TEST_EXPECTED="$expected" TEST_DRIFT="$drift" perl -0pi -e '
    my $tag = quotemeta($ENV{TEST_TAG});
    my $expected = quotemeta($ENV{TEST_EXPECTED});
    my $replacement = "<$ENV{TEST_TAG}>$ENV{TEST_EXPECTED}</$ENV{TEST_TAG}>\n" .
      "<$ENV{TEST_TAG}>$ENV{TEST_DRIFT}</$ENV{TEST_TAG}>";
    s{<$tag>$expected</$tag>}{$replacement} or die "target tag not found\n";
  ' "$FIXTURE_ROOT/$file"
  if run_product check >"$COMMAND_LOG" 2>&1; then
    fail_test "POM 重复标签掩盖了实际漂移：$file/$tag"
  fi
  restore_file "$file"
}

assert_no_operation_residue() {
  local residue=''
  if git -C "$FIXTURE_ROOT" rev-parse --verify --quiet "$LOCK_REF" >/dev/null; then
    fail_test "Git 元数据中残留产品配置锁引用"
  fi
  [ ! -e "$FIXTURE_ROOT/.product-config-untracked-residual.tmp" ] || \
    fail_test "工作树残留旧版测试 marker"
  residue="$(find "$FIXTURE_ROOT" -path "$FIXTURE_ROOT/.git" -prune -o \
    \( -name '*.thinglinks-product.[0-9]*' -o -name '.product-config-untracked-residual.tmp' \) \
    -print -quit)"
  [ -z "$residue" ] || fail_test "工作树残留产品配置临时文件：$residue"
  residue="$(find "$TEST_TMP" -mindepth 1 \
    \( -name 'bifromq-product-stage.*' -o -name 'bifromq-product-backup.*' \) \
    -print -quit)"
  [ -z "$residue" ] || fail_test "临时目录残留未清理：$residue"
  if git -C "$FIXTURE_ROOT" status --porcelain=v1 --untracked-files=all | \
    grep -Eq 'product-config-untracked-residual'; then
    fail_test "Git 状态中出现测试 marker"
  fi
}

BASE_COMPONENT_VERSION="$(manifest_value THINGLINKS_COMPONENT_VERSION)"
BASE_BIFROMQ_VERSION="$(manifest_value THINGLINKS_BIFROMQ_VERSION)"
BASE_UTIL_VERSION="$(manifest_value THINGLINKS_UTIL_VERSION)"
TEST_NEW_VERSION='9.9.9-product-test'

run_product check >"$COMMAND_LOG"

# 非保护文档通过清单键引用当前版本；历史迁移数字不作为当前版本配置处理。
assert_current_document_version_rejected component \
  "当前组件版本为 ${BASE_COMPONENT_VERSION}。"
assert_current_document_version_rejected bifromq \
  "当前 BifroMQ 兼容版本为 ${BASE_BIFROMQ_VERSION}。"
assert_current_document_version_rejected util \
  "当前 ThingLinks Util 依赖版本为 ${BASE_UTIL_VERSION}。"
assert_current_document_version_rejected java \
  "当前 Java 版本为 $(manifest_value THINGLINKS_JAVA_VERSION)。"
assert_current_document_version_rejected drift-component \
  '当前组件版本为 1.0.7。'
assert_current_document_version_rejected drift-bifromq \
  '当前 BifroMQ 兼容版本为 3.3.4。'
assert_current_document_version_rejected drift-util \
  '当前 ThingLinks Util 依赖版本为 1.0.7。'
assert_current_document_version_rejected drift-java \
  '当前 Java 版本为 21。'
assert_current_document_version_rejected drift-v-prefix \
  'currently requires v3.3.4'
assert_current_document_version_rejected requirements-section \
  $'## Requirements\n- BifroMQ 3.3.4\n- JDK 21+'
printf '## Requirements\n- Maven 3.8.5\n- Maven Compiler Plugin 3.13.0\n' \
  > "$FIXTURE_ROOT/tooling-requirements-probe.md"
run_product check >"$COMMAND_LOG" || fail_test "构建工具版本被误判为产品兼容版本"
rm -f "$FIXTURE_ROOT/tooling-requirements-probe.md"
printf '## %s -> 4.0 历史迁移对照\n| %s | 4.0 |\n' \
  "$BASE_BIFROMQ_VERSION" "$BASE_BIFROMQ_VERSION" > "$FIXTURE_ROOT/version-history-probe.md"
run_product check >"$COMMAND_LOG" || fail_test "历史迁移版本被误判为当前版本配置"
rm -f "$FIXTURE_ROOT/version-history-probe.md"

# 失败 setter 的清单、POM 与固定运行协议原状保护。
assert_fails_without_changes set-version '1.0/invalid'
assert_fails_without_changes set-bifromq-version '3.x'
assert_fails_without_changes set-util-version '1.0/invalid'
assert_fails_without_changes set-java-version seventeen

# BifroMQ 4.x 包名迁移前的版本拒绝场景。
before_bifromq4="$(fingerprint)"
if run_product set-bifromq-version 4.0.0 >"$COMMAND_LOG" 2>&1; then
  fail_test "BifroMQ 4.x 在包名迁移前不应被接受"
fi
grep -Fq 'org.apache.bifromq' "$COMMAND_LOG" || fail_test "4.x 拒绝信息没有给出包名迁移指引"
[ "$before_bifromq4" = "$(fingerprint)" ] || fail_test "拒绝 4.x 时修改了仓库"

# 授权模型枚举校验。
assert_manifest_value_rejected THINGLINKS_LICENSE_MODEL unsupported

# 发行版本与授权模型必须构成语义一致的组合。
for invalid_pair in \
  'community commercial' \
  'community dual-license' \
  'commercial open-source' \
  'enterprise open-source'; do
  read -r edition license <<< "$invalid_pair"
  assert_edition_license_rejected "$edition" "$license"
done
for valid_pair in \
  'community open-source' \
  'commercial commercial' \
  'commercial dual-license' \
  'enterprise commercial' \
  'enterprise dual-license'; do
  read -r edition license <<< "$valid_pair"
  assert_edition_license_accepted "$edition" "$license"
done

# 保护路径的数组解析以及 glob、空项、越界和非规范化写法校验。
PROTECTED_PREFIX='.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL'
for invalid_paths in \
  "$PROTECTED_PREFIX,*" \
  "$PROTECTED_PREFIX,?" \
  "$PROTECTED_PREFIX,[x]" \
  "$PROTECTED_PREFIX," \
  ",$PROTECTED_PREFIX" \
  "$PROTECTED_PREFIX,../secret" \
  "$PROTECTED_PREFIX,./local" \
  "$PROTECTED_PREFIX,/absolute" \
  "$PROTECTED_PREFIX,a//b" \
  "$PROTECTED_PREFIX,LICENSE"; do
  assert_manifest_value_rejected THINGLINKS_SYNC_PROTECTED_PATHS "$invalid_paths"
done
assert_manifest_value_rejected THINGLINKS_SYNC_PROTECTED_PATHS \
  "$PROTECTED_PREFIX,does-not-exist"

# 稳定身份字段的发行版本信息校验。
assert_identity_rejected THINGLINKS_PRODUCT_CODE thinglinks-pro
assert_identity_rejected THINGLINKS_PRODUCT_NAME '"ThingLinks Pro"'
assert_identity_rejected THINGLINKS_PRODUCT_NAME_ZH ThingLinks旗舰版
assert_identity_rejected THINGLINKS_COMPONENT_CODE bifromq-plugin-pro
assert_identity_rejected THINGLINKS_COMPONENT_NAME '"BifroMQ Community"'
assert_identity_rejected THINGLINKS_MAVEN_ARTIFACT_ID bifromq-plugin-pro
assert_identity_rejected THINGLINKS_MAVEN_GROUP_ID com.mqttsnet.pro
assert_identity_rejected THINGLINKS_JAVA_PACKAGE_PREFIX com.mqttsnet.enterprise
for marker in \
  ThingLinksPro thinglinkspro ThingLinksJobPro ThingLinksJobProFeature \
  thinglinksproConfig webproConfig cloudproConfig utilproConfig pluginproConfig \
  cloudpro utilpro pluginpro CloudPro UtilPro PluginPro CloudPro-config \
  BifromqPluginPro BifromqPluginProAuth WebProService CloudProService UtilProStarter \
  ThingLinksUtilProStarter \
  '"Professional Edition"' '"Open Source Edition"' \
  '"ThingLinks Commercial"' \
  '"ThingLinks Enterprise"' ThingLinks商业版 ThingLinks社区版 ThingLinks开源版; do
  assert_identity_rejected THINGLINKS_COMPONENT_NAME "$marker"
done

# protocol、product、professional 等相似业务词的非发行标识场景。
for allowed_identity in \
  '"Professional Platform"' Product Protocol Project Process Properties \
  ThingLinksProfessional ThingLinksProduct ThingLinksProtocol thinglinks-protocol \
  thinglinksProfessional thinglinksProduct thinglinksProtocol thinglinksProject \
  thinglinksProcess thinglinksProperties \
  ThingLinksProject ThingLinksProcess ThingLinksProperties BifromqPluginProperties; do
  set_manifest_value THINGLINKS_COMPONENT_NAME "$allowed_identity"
  run_product check >"$COMMAND_LOG" || fail_test "稳定身份误报：$allowed_identity"
  restore_file .thinglinks-product.env
done

# 路径与内容扫描对 kebab、camel、中文及英文版本名的覆盖。
for marker in \
  thinglinks-random-pro ThingLinksJobProConfig ThingLinksJobProFeature \
  thinglinksproConfig webproConfig cloudproConfig utilproConfig pluginproConfig \
  bifromq-plugin-pro BifromqPluginProAuth WebPro WebProConfig WebProService \
  webProDatasource CloudPro UtilPro PluginPro 'CloudPro,enabled' UtilPro-config \
  PluginPro/config CloudProService UtilProStarter ThingLinksUtilProStarter \
  BifromqPluginPro BifromqPluginProConfig 旗舰版 商业版 社区版 开源版 \
  'Pro Edition' 'Professional Edition' 'Enterprise Edition' 'Commercial Edition' \
  'Community Edition' 'Open Source Edition'; do
  assert_scanner_rejects_content "$marker"
done
for marker_path in CloudPro.txt UtilPro.yaml PluginPro-config.txt; do
  assert_scanner_rejects_path "$marker_path"
done
printf '%s\n' 'neutral' > "$FIXTURE_ROOT/BifromqPluginProConfig.txt"
if run_product check >"$COMMAND_LOG" 2>&1; then
  fail_test "扫描器没有拦截 camel 发行标识文件名"
fi
rm -f "$FIXTURE_ROOT/BifromqPluginProConfig.txt"

printf '%s\n' \
  'Professional Platform' Product Protocol Project Process Properties \
  CloudProduct CloudProfessional CloudProtocol UtilProduct PluginProperties \
  ThingLinksProfessional ThingLinksProduct ThingLinksProtocol thinglinks-protocol-starter \
  thinglinksProfessional thinglinksProduct thinglinksProtocol thinglinksProject \
  thinglinksProcess thinglinksProperties \
  ThingLinksProject ThingLinksProcess ThingLinksProperties BifromqPluginProperties \
  > "$FIXTURE_ROOT/scanner-negative-probe.txt"
run_product check >"$COMMAND_LOG" || fail_test "扫描器误报 Professional/Product/Protocol"
rm -f "$FIXTURE_ROOT/scanner-negative-probe.txt"

# POM 坐标与版本必须读取实际 XML 值，注释不能伪装成期望配置。
assert_pom_comment_spoof_rejected pom.xml artifactId bifromq-plugin bifromq-plugin-drift
assert_pom_comment_spoof_rejected bifromq-auth-provider-plugin/pom.xml \
  artifactId bifromq-auth-provider-plugin bifromq-auth-provider-plugin-drift
assert_pom_comment_spoof_rejected bifromq-auth-provider-plugin/auth-provider/pom.xml \
  artifactId bifromq-auth-provider-plugin bifromq-auth-provider-plugin-drift
assert_pom_comment_spoof_rejected pom.xml revision \
  "$BASE_COMPONENT_VERSION" "${BASE_COMPONENT_VERSION}-drift"
assert_pom_comment_spoof_rejected bifromq-auth-provider-plugin/pom.xml \
  bifromq.version "$BASE_BIFROMQ_VERSION" "${BASE_BIFROMQ_VERSION}-drift"
assert_pom_comment_spoof_rejected bifromq-auth-provider-plugin/pom.xml \
  thinglinks-util.version "$BASE_UTIL_VERSION" "${BASE_UTIL_VERSION}-drift"
assert_pom_duplicate_tag_rejected pom.xml artifactId bifromq-plugin bifromq-plugin-drift
assert_pom_duplicate_tag_rejected pom.xml revision \
  "$BASE_COMPONENT_VERSION" "${BASE_COMPONENT_VERSION}-drift"

# 事件 Topic 固定 mqtt 跨服务协议及 topic(suffix) 映射结构。
grep -Fq 'private static final String KAFKA_TOPIC_PREFIX = "mqtt";' "$EVENT_PROVIDER" || \
  fail_test "事件 Topic 固定 mqtt 协议缺失"
topic_count="$(perl -ne 'while (/TOPIC_MAP\.put\([^,]+,\s*topic\("[^"]+"\)\)/g) { $count++ } END { print $count || 0 }' "$EVENT_PROVIDER")"
[ "$topic_count" -gt 0 ] || fail_test "事件 Topic 映射没有统一使用 topic(suffix)"

# Git ref CAS 的活锁保护、失效 PID 回收和工作树隔离。
live_lock_oid="$(create_test_lock_ref "$$" live-test)"
printf '%s\n' 'INVALID_MANIFEST_SYNTAX' >> "$MANIFEST"
assert_fails_without_changes set-version "$TEST_NEW_VERSION"
grep -Fq "产品配置正在被进程 $$ 修改" "$COMMAND_LOG" || \
  fail_test "写命令在获取活锁前读取了产品清单"
[ "$(git -C "$FIXTURE_ROOT" rev-parse --verify "$LOCK_REF")" = "$live_lock_oid" ] || \
  fail_test "活锁 owner 被其他进程修改"
restore_file .thinglinks-product.env
git -C "$FIXTURE_ROOT" update-ref -d "$LOCK_REF" "$live_lock_oid"

# 两个 contender 并发 claim 时，仅 create-only CAS 的胜者可以进入写操作。
LOCK_RACE="$TEST_ROOT/lock-race"
mkdir -p "$LOCK_RACE"
THINGLINKS_PRODUCT_TEST_PAUSE_BEFORE_LOCK_CLAIM="$LOCK_RACE/first" \
  run_product render >"$LOCK_RACE/first.log" 2>&1 &
first_lock_pid=$!
wait_for_file "$LOCK_RACE/first.ready"
THINGLINKS_PRODUCT_TEST_PAUSE_AFTER_LOCK_CLAIM="$LOCK_RACE/second" \
  run_product render >"$LOCK_RACE/second.log" 2>&1 &
second_lock_pid=$!
wait_for_file "$LOCK_RACE/second.ready"
printf '%s\n' continue > "$LOCK_RACE/first.continue"
if wait "$first_lock_pid"; then
  fail_test "CAS 竞争失败者不应进入产品配置写操作"
fi
grep -Fq '产品配置正在被进程' "$LOCK_RACE/first.log" || \
  fail_test "CAS 竞争失败者没有识别当前 live owner"
printf '%s\n' continue > "$LOCK_RACE/second.continue"
wait "$second_lock_pid" || fail_test "新 owner 未能完成产品配置写操作"
assert_no_operation_residue

# claim 后 ref 被 CAS 切换时，旧 writer 必须在替换任何目标文件前退出。
LOCK_STEAL="$TEST_ROOT/lock-steal"
before_lock_steal="$(fingerprint)"
THINGLINKS_PRODUCT_TEST_PAUSE_AFTER_LOCK_CLAIM="$LOCK_STEAL" \
  run_product set-version "$TEST_NEW_VERSION" >"$TEST_ROOT/lock-steal.log" 2>&1 &
stale_writer_pid=$!
wait_for_file "$LOCK_STEAL.ready"
stale_owner_oid="$(git -C "$FIXTURE_ROOT" rev-parse --verify "$LOCK_REF")"
replacement_owner_oid="$(create_test_lock_blob "$$" replacement-live)"
git -C "$FIXTURE_ROOT" update-ref "$LOCK_REF" "$replacement_owner_oid" "$stale_owner_oid"
printf '%s\n' continue > "$LOCK_STEAL.continue"
if wait "$stale_writer_pid"; then
  fail_test "锁归属变化后的旧 writer 不应成功"
fi
[ "$before_lock_steal" = "$(fingerprint)" ] || fail_test "旧 writer 在锁归属变化后修改了目标文件"
grep -Fq '产品配置锁归属已变化' "$TEST_ROOT/lock-steal.log" || \
  fail_test "锁归属变化没有返回明确错误"
[ "$(git -C "$FIXTURE_ROOT" rev-parse --verify "$LOCK_REF")" = "$replacement_owner_oid" ] || \
  fail_test "旧 writer 删除了新的 owner 引用"
git -C "$FIXTURE_ROOT" update-ref -d "$LOCK_REF" "$replacement_owner_oid"
assert_no_operation_residue

# 首个目标替换后归属丢失时，旧 writer 不得继续写入或无锁回滚。
LOCK_AFTER_REPLACE="$TEST_ROOT/lock-after-replace"
before_first_replace="$(fingerprint)"
THINGLINKS_PRODUCT_TEST_PAUSE_AFTER_FIRST_REPLACE="$LOCK_AFTER_REPLACE" \
  run_product set-version "$TEST_NEW_VERSION" >"$TEST_ROOT/lock-after-replace.log" 2>&1 &
partial_writer_pid=$!
wait_for_file "$LOCK_AFTER_REPLACE.ready"
after_first_replace="$(fingerprint)"
[ "$before_first_replace" != "$after_first_replace" ] || fail_test "首个替换测试未修改目标文件"
partial_owner_oid="$(git -C "$FIXTURE_ROOT" rev-parse --verify "$LOCK_REF")"
replacement_owner_oid="$(create_test_lock_blob "$$" replacement-after-first)"
git -C "$FIXTURE_ROOT" update-ref "$LOCK_REF" "$replacement_owner_oid" "$partial_owner_oid"
printf '%s\n' continue > "$LOCK_AFTER_REPLACE.continue"
if wait "$partial_writer_pid"; then
  fail_test "首个替换后丢失锁的旧 writer 不应成功"
fi
[ "$after_first_replace" = "$(fingerprint)" ] || \
  fail_test "旧 writer 在丢失锁后继续写入或执行了无锁回滚"
[ "$(git -C "$FIXTURE_ROOT" rev-parse --verify "$LOCK_REF")" = "$replacement_owner_oid" ] || \
  fail_test "旧 writer 删除了首个替换后的新 owner 引用"
grep -Fq '跳过无锁回滚' "$TEST_ROOT/lock-after-replace.log" || \
  fail_test "首个替换后的锁归属变化没有明确跳过回滚"
git -C "$FIXTURE_ROOT" update-ref -d "$LOCK_REF" "$replacement_owner_oid"
restore_file .thinglinks-product.env
run_product check >"$COMMAND_LOG"
assert_no_operation_residue

# 未知格式 owner 不按死锁删除，保留 ref 供人工确认。
malformed_owner_oid="$(printf '%s\n' malformed-owner | \
  git -C "$FIXTURE_ROOT" hash-object -w --stdin)"
git -C "$FIXTURE_ROOT" update-ref "$LOCK_REF" "$malformed_owner_oid" ''
assert_fails_without_changes render
grep -Fq 'owner 对象格式无效' "$COMMAND_LOG" || fail_test "未知 owner 格式没有被保守拒绝"
[ "$(git -C "$FIXTURE_ROOT" rev-parse --verify "$LOCK_REF")" = "$malformed_owner_oid" ] || \
  fail_test "未知格式 owner 引用被错误删除"
git -C "$FIXTURE_ROOT" update-ref -d "$LOCK_REF" "$malformed_owner_oid"
assert_no_operation_residue

create_test_lock_ref 99999999 dead-test >/dev/null
run_product render >"$COMMAND_LOG"
assert_no_operation_residue

# 首个文件替换后的异常回滚，以及锁、备份和目标旁临时文件清理。
before_injected_failure="$(fingerprint)"
if TMPDIR="$TEST_TMP" THINGLINKS_PRODUCT_TEST_FAIL_AFTER_REPLACE=1 \
  "$PRODUCT_SCRIPT" set-version "$TEST_NEW_VERSION" >"$COMMAND_LOG" 2>&1; then
  fail_test "中途失败注入本应导致 setter 失败"
fi
[ "$before_injected_failure" = "$(fingerprint)" ] || fail_test "中途失败没有完整回滚"
assert_no_operation_residue

# render 幂等性与固定运行协议的事务边界。
run_product render >"$COMMAND_LOG"
first_render="$(fingerprint)"
run_product render >"$COMMAND_LOG"
[ "$first_render" = "$(fingerprint)" ] || fail_test "连续 render 的结果不一致"
assert_no_operation_residue

# Maven validate 根工程只读门禁对坐标漂移的覆盖。
# 清单漂移场景保留 Maven 模型可解析状态，validate 阶段由产品门禁返回失败。
set_manifest_value THINGLINKS_MAVEN_GROUP_ID com.mqttsnet.mismatch
set_manifest_value THINGLINKS_JAVA_PACKAGE_PREFIX com.mqttsnet.mismatch
if TMPDIR="$TEST_TMP" "$MAVEN_BIN" --batch-mode -f "$FIXTURE_ROOT/pom.xml" -DskipTests validate \
  >"$COMMAND_LOG" 2>&1; then
  fail_test "Maven validate 没有拒绝根工程坐标漂移"
fi
if ! grep -Fq '与产品清单不一致' "$COMMAND_LOG"; then
  sed -n '1,160p' "$COMMAND_LOG" >&2
  fail_test "Maven validate 失败并非来自产品配置门禁"
fi
restore_file .thinglinks-product.env

run_product check >"$COMMAND_LOG"
assert_no_operation_residue
[ "$SOURCE_STATE_BEFORE" = "$(source_fingerprint)" ] || fail_test "回归测试修改了真实仓库"

REMOVED_TEST_ROOT="$TEST_ROOT"
cleanup
trap - EXIT HUP INT TERM
[ ! -e "$REMOVED_TEST_ROOT" ] || fail_test "测试 fixture 没有完整清理"
printf 'BifroMQ 产品配置隔离回归测试通过。\n'
