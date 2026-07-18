#!/usr/bin/env bash

set -euo pipefail

SOURCE_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/thinglinks-job-product-test.XXXXXX")"
FIXTURE="$TEST_ROOT/repo"

cleanup() {
  rm -rf "$TEST_ROOT"
}
trap cleanup EXIT HUP INT TERM

fail_test() {
  printf '测试失败：%s\n' "$1" >&2
  exit 1
}

mkdir -p "$FIXTURE/scripts/tests" "$FIXTURE/thinglinks-job-admin/src/main/resources"
FIXTURE="$(cd -P "$FIXTURE" && pwd)"
for file in .thinglinks-product.env .gitignore LICENSE \
  README.md README.zh-CN.md README.ja.md README.ko.md pom.xml; do
  cp "$SOURCE_ROOT/$file" "$FIXTURE/$file"
done
cp "$SOURCE_ROOT/thinglinks-job-admin/pom.xml" "$FIXTURE/thinglinks-job-admin/pom.xml"
cp "$SOURCE_ROOT/thinglinks-job-admin/src/main/resources/application.yml" \
  "$FIXTURE/thinglinks-job-admin/src/main/resources/application.yml"
cp "$SOURCE_ROOT/scripts/product-config.sh" "$FIXTURE/scripts/product-config.sh"
cp "$SOURCE_ROOT/scripts/tests/product-config-test.sh" "$FIXTURE/scripts/tests/product-config-test.sh"
chmod +x "$FIXTURE/scripts/product-config.sh" "$FIXTURE/scripts/tests/product-config-test.sh"

git -C "$FIXTURE" init --quiet
git -C "$FIXTURE" add .

PRODUCT="$FIXTURE/scripts/product-config.sh"
LOCK_PATH="$FIXTURE/.thinglinks-product.lock"
fingerprint() {
  shasum -a 256 "$FIXTURE/.thinglinks-product.env" "$FIXTURE/pom.xml" \
    "$FIXTURE/thinglinks-job-admin/pom.xml" \
    "$FIXTURE/thinglinks-job-admin/src/main/resources/application.yml"
}

expect_failure() {
  local label="$1"
  shift
  if "$@" >/dev/null 2>&1; then
    fail_test "$label"
  fi
}

replace_manifest_value() {
  local key="$1" value="$2"
  THINGLINKS_TEST_KEY="$key" THINGLINKS_TEST_VALUE="$value" perl -0pi -e \
    's/^\Q$ENV{THINGLINKS_TEST_KEY}\E=.*$/$ENV{THINGLINKS_TEST_KEY}."=".$ENV{THINGLINKS_TEST_VALUE}/me' \
    "$FIXTURE/.thinglinks-product.env"
}

"$PRODUCT" check >/dev/null
before="$(fingerprint)"
expect_failure '非法组件版本未被拒绝' "$PRODUCT" set-version 'bad/version'
[ "$before" = "$(fingerprint)" ] || fail_test '非法版本修改了受管文件'

"$PRODUCT" set-version 9.9.9 >/dev/null
grep -Fq 'THINGLINKS_COMPONENT_VERSION=9.9.9' "$FIXTURE/.thinglinks-product.env" || fail_test '组件版本未写入清单'
grep -Fq '<revision>9.9.9</revision>' "$FIXTURE/pom.xml" || fail_test '组件版本未写入根 POM'

"$PRODUCT" set-util-version 9.9.8 >/dev/null
grep -Fq '<version>9.9.8</version>' "$FIXTURE/pom.xml" || fail_test '父工程版本未更新'
grep -Fq '<thinglinks-util.version>9.9.8</thinglinks-util.version>' "$FIXTURE/pom.xml" || fail_test 'Util 版本未更新'

"$PRODUCT" set-java-version 21 >/dev/null
grep -Fq '<maven.compiler.source>21</maven.compiler.source>' "$FIXTURE/pom.xml" || fail_test 'Java source 未更新'
grep -Fq '<maven.compiler.target>21</maven.compiler.target>' "$FIXTURE/pom.xml" || fail_test 'Java target 未更新'

first_render="$(fingerprint)"
"$PRODUCT" render >/dev/null
[ "$first_render" = "$(fingerprint)" ] || fail_test 'render 不是幂等操作'

cp "$FIXTURE/pom.xml" "$TEST_ROOT/pom.backup"
perl -0pi -e 's{<revision>9\.9\.9</revision>}{<revision>0.0.0</revision>}' "$FIXTURE/pom.xml"
if "$PRODUCT" check >/dev/null 2>&1; then
  fail_test 'POM 版本漂移未被拒绝'
fi
cp "$TEST_ROOT/pom.backup" "$FIXTURE/pom.xml"

printf '%s\n' 'ThingLinksJobPro' > "$FIXTURE/neutral-marker.txt"
expect_failure '散落的发行版本标识未被拒绝' "$PRODUCT" check
rm -f "$FIXTURE/neutral-marker.txt"

cp "$FIXTURE/.thinglinks-product.env" "$TEST_ROOT/manifest.protected-marker"
mkdir -p "$FIXTURE/release/ThingLinksJobCommercial"
printf '%s\n' 'CommercialEdition' > "$FIXTURE/release/ThingLinksJobCommercial/LICENSE-COMMERCIAL"
replace_manifest_value THINGLINKS_SYNC_PROTECTED_PATHS \
  '.thinglinks-product.env,LICENSE,release/ThingLinksJobCommercial'
"$PRODUCT" check >/dev/null || fail_test '受保护的发行授权路径被散落标识扫描误拦截'
cp "$TEST_ROOT/manifest.protected-marker" "$FIXTURE/.thinglinks-product.env"
rm -rf "$FIXTURE/release"

mkdir -p "$FIXTURE/release/ThingLinksJobCommercial"
printf '%s\n' 'neutral' > "$FIXTURE/release/ThingLinksJobCommercial/notes.txt"
expect_failure '路径中的紧凑发行版本标识未被拒绝' "$PRODUCT" check
rm -rf "$FIXTURE/release"

printf '%s\n' 'CommercialEdition' > "$FIXTURE/compact-marker.txt"
expect_failure '内容中的紧凑发行版本标识未被拒绝' "$PRODUCT" check
rm -f "$FIXTURE/compact-marker.txt"

mkdir "$LOCK_PATH"
printf '%s\n' 'initializing' > "$LOCK_PATH/owner-not-ready"
lock_before="$(fingerprint)"
expect_failure '没有 pid 的初始化锁被并发进程偷取' "$PRODUCT" set-version 9.9.7
[ -e "$LOCK_PATH/owner-not-ready" ] || fail_test '并发进程删除了不属于自己的初始化锁'
[ "$lock_before" = "$(fingerprint)" ] || fail_test '获取锁失败时修改了受管文件'
rm -rf "$LOCK_PATH"

FAKE_BIN="$TEST_ROOT/fake-bin"
mkdir -p "$FAKE_BIN"
REAL_CP="$(command -v cp)"
REAL_MV="$(command -v mv)"
printf '%s\n' \
  '#!/usr/bin/env bash' \
  'set -euo pipefail' \
  'command_name="${0##*/}"' \
  'last_argument="${!#}"' \
  'if [ "$command_name" = cp ]; then' \
  '  "$REAL_CP" "$@"' \
  'else' \
  '  "$REAL_MV" "$@"' \
  'fi' \
  'if [ -n "${TEST_INTERRUPT_TARGET:-}" ] && [ "$last_argument" = "$TEST_INTERRUPT_TARGET" ] && [ ! -e "$TEST_INTERRUPT_ONCE" ]; then' \
  '  : > "$TEST_INTERRUPT_ONCE"' \
  '  kill -TERM "$PPID"' \
  'fi' > "$FAKE_BIN/managed-write"
chmod +x "$FAKE_BIN/managed-write"
ln -s managed-write "$FAKE_BIN/cp"
ln -s managed-write "$FAKE_BIN/mv"

interrupt_before="$(fingerprint)"
if PATH="$FAKE_BIN:$PATH" REAL_CP="$REAL_CP" REAL_MV="$REAL_MV" \
  TEST_INTERRUPT_TARGET="$FIXTURE/.thinglinks-product.env" \
  TEST_INTERRUPT_ONCE="$TEST_ROOT/interrupt.once" \
  "$PRODUCT" set-version 8.8.8 >/dev/null 2>&1; then
  fail_test '写入期间收到 TERM 后脚本仍以成功状态继续执行'
fi
[ -e "$TEST_ROOT/interrupt.once" ] || fail_test '未命中受管文件写入中断测试点'
[ "$interrupt_before" = "$(fingerprint)" ] || fail_test '写入中断后留下了半写状态'
[ ! -e "$LOCK_PATH" ] || fail_test '写入中断后产品配置写锁未清理'

REAL_PERL="$(command -v perl)"
printf '%s\n' \
  '#!/usr/bin/env bash' \
  'set -euo pipefail' \
  'if [ ! -e "$TEST_PAUSE_ONCE" ]; then' \
  '  : > "$TEST_PAUSE_ONCE"' \
  '  : > "$TEST_PAUSE_READY"' \
  '  while [ ! -e "$TEST_PAUSE_CONTINUE" ]; do sleep 0.02; done' \
  'fi' \
  'exec "$REAL_PERL" "$@"' > "$FAKE_BIN/perl"
chmod +x "$FAKE_BIN/perl"

PATH="$FAKE_BIN:$PATH" REAL_CP="$REAL_CP" REAL_MV="$REAL_MV" REAL_PERL="$REAL_PERL" \
  TEST_PAUSE_ONCE="$TEST_ROOT/pause.once" TEST_PAUSE_READY="$TEST_ROOT/pause.ready" \
  TEST_PAUSE_CONTINUE="$TEST_ROOT/pause.continue" \
  "$PRODUCT" render >"$TEST_ROOT/owner-check.out" 2>&1 &
owner_check_pid=$!
for _ in $(seq 1 250); do
  [ -e "$TEST_ROOT/pause.ready" ] && break
  sleep 0.02
done
[ -e "$TEST_ROOT/pause.ready" ] || fail_test '未能暂停持锁进程'
if [ -d "$LOCK_PATH" ]; then
  if [ -f "$LOCK_PATH/owner" ]; then
    printf '%s\n' '999999 foreign-owner-token' > "$LOCK_PATH/owner"
  else
    printf '%s\n' '999999' > "$LOCK_PATH/pid"
  fi
else
  printf '%s\n' '999999 foreign-owner-token' > "$LOCK_PATH"
fi
: > "$TEST_ROOT/pause.continue"
if wait "$owner_check_pid"; then
  fail_test '锁所有者令牌被替换后脚本仍报告成功'
fi
[ -e "$LOCK_PATH" ] || fail_test '释放锁时删除了其他所有者的锁'
rm -rf "$LOCK_PATH"

external_pom="$TEST_ROOT/external-pom.xml"
cp "$FIXTURE/pom.xml" "$external_pom"
rm "$FIXTURE/pom.xml"
ln -s "$external_pom" "$FIXTURE/pom.xml"
external_before="$(shasum -a 256 "$external_pom")"
expect_failure '受管 POM 符号链接未被拒绝' "$PRODUCT" render
[ "$external_before" = "$(shasum -a 256 "$external_pom")" ] || fail_test 'render 通过符号链接修改了仓库外文件'
rm "$FIXTURE/pom.xml"
cp "$external_pom" "$FIXTURE/pom.xml"

cp "$FIXTURE/.thinglinks-product.env" "$TEST_ROOT/manifest.safe"
mkdir -p "$TEST_ROOT/protected-outside"
printf '%s\n' 'protected' > "$TEST_ROOT/protected-outside/value.txt"
ln -s "$TEST_ROOT/protected-outside" "$FIXTURE/protected-link"
replace_manifest_value THINGLINKS_SYNC_PROTECTED_PATHS \
  '.thinglinks-product.env,LICENSE,protected-link/value.txt'
expect_failure '同步保护路径中的符号链接目录未被拒绝' "$PRODUCT" check
cp "$TEST_ROOT/manifest.safe" "$FIXTURE/.thinglinks-product.env"
rm "$FIXTURE/protected-link"

replace_manifest_value THINGLINKS_MAVEN_GROUP_ID org.example.thinglinks
replace_manifest_value THINGLINKS_SERVICE_NAME thinglinks-job-control
"$PRODUCT" render >/dev/null
grep -Fq '<groupId>org.example.thinglinks</groupId>' "$FIXTURE/pom.xml" || fail_test 'render 未更新根 POM groupId'
grep -Fq '<groupId>org.example.thinglinks</groupId>' "$FIXTURE/thinglinks-job-admin/pom.xml" || fail_test 'render 未更新 Admin POM groupId'
grep -Fq '    name: thinglinks-job-control' \
  "$FIXTURE/thinglinks-job-admin/src/main/resources/application.yml" || fail_test 'render 未更新 Spring 应用名称'
"$PRODUCT" check >/dev/null

cp "$FIXTURE/thinglinks-job-admin/src/main/resources/application.yml" "$TEST_ROOT/application.safe"
perl -0pi -e 's/^    name: thinglinks-job-control$/    name: wrong-service/m' \
  "$FIXTURE/thinglinks-job-admin/src/main/resources/application.yml"
printf '\ndecoy:\n  name: thinglinks-job-control\n' >> \
  "$FIXTURE/thinglinks-job-admin/src/main/resources/application.yml"
expect_failure 'check 未精确校验 spring.application.name 路径' "$PRODUCT" check
cp "$TEST_ROOT/application.safe" "$FIXTURE/thinglinks-job-admin/src/main/resources/application.yml"

cp "$FIXTURE/.thinglinks-product.env" "$TEST_ROOT/manifest.metadata"
replace_manifest_value THINGLINKS_EDITION_NAME_EN Open
expect_failure '社区版英文发行名称与 edition 编码不一致时未被拒绝' "$PRODUCT" check
cp "$TEST_ROOT/manifest.metadata" "$FIXTURE/.thinglinks-product.env"

replace_manifest_value THINGLINKS_COMPONENT_NAME '"ThingLinksJobEnterprise"'
expect_failure '稳定组件名称中的 CamelCase 发行标识未被拒绝' "$PRODUCT" check
cp "$TEST_ROOT/manifest.metadata" "$FIXTURE/.thinglinks-product.env"

cp "$FIXTURE/pom.xml" "$TEST_ROOT/pom.license"
perl -0pi -e 's{GNU General Public License version 3}{Apache License 2.0}' "$FIXTURE/pom.xml"
expect_failure '社区开源清单与 POM GPL 元数据不一致时未被拒绝' "$PRODUCT" check
cp "$TEST_ROOT/pom.license" "$FIXTURE/pom.xml"

cp "$FIXTURE/LICENSE" "$TEST_ROOT/license.safe"
printf '%s\n' 'not a GPL license' > "$FIXTURE/LICENSE"
expect_failure '社区开源清单与 LICENSE 内容不一致时未被拒绝' "$PRODUCT" check
cp "$TEST_ROOT/license.safe" "$FIXTURE/LICENSE"

REAL_GIT="$(command -v git)"
GIT_FAKE_BIN="$TEST_ROOT/git-fake-bin"
mkdir -p "$GIT_FAKE_BIN"
printf '%s\n' \
  '#!/usr/bin/env bash' \
  'set -euo pipefail' \
  'for argument in "$@"; do' \
  '  if [ "$argument" = ls-files ]; then exit 72; fi' \
  'done' \
  'exec "$REAL_GIT" "$@"' > "$GIT_FAKE_BIN/git"
chmod +x "$GIT_FAKE_BIN/git"
expect_failure 'Git 文件枚举失败时 check 没有按失败关闭' \
  env PATH="$GIT_FAKE_BIN:$PATH" REAL_GIT="$REAL_GIT" "$PRODUCT" check

"$PRODUCT" check >/dev/null
[ ! -e "$LOCK_PATH" ] || fail_test '产品配置写锁未清理'

rollback_before="$(fingerprint)"
printf '%s\n' 'ThingLinksJobPro' > "$FIXTURE/rollback-marker.txt"
if "$PRODUCT" set-version 8.8.8 >/dev/null 2>&1; then
  fail_test '写入后检查失败未被拒绝'
fi
[ "$rollback_before" = "$(fingerprint)" ] || fail_test '写入后检查失败未恢复受管文件'
[ ! -e "$LOCK_PATH" ] || fail_test '回滚后产品配置写锁未清理'
rm -f "$FIXTURE/rollback-marker.txt"
"$PRODUCT" check >/dev/null
if find "$FIXTURE" \( -name '.thinglinks-product.lock' -o -name '*.product-backup.*' \
  -o -name '*.product-stage.*' \) -print | grep -q .; then
  fail_test '回归完成后仍残留产品配置锁或事务临时文件'
fi

printf 'Job 产品配置隔离回归测试通过。\n'
