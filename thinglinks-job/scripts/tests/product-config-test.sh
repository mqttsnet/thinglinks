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
fingerprint() {
  shasum -a 256 "$FIXTURE/.thinglinks-product.env" "$FIXTURE/pom.xml" \
    "$FIXTURE/thinglinks-job-admin/pom.xml"
}

"$PRODUCT" check >/dev/null
before="$(fingerprint)"
if "$PRODUCT" set-version 'bad/version' >/dev/null 2>&1; then
  fail_test '非法组件版本未被拒绝'
fi
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

printf '%s\n' 'ThingLinksJobPro' > "$FIXTURE/ThingLinksJobPro.txt"
if "$PRODUCT" check >/dev/null 2>&1; then
  fail_test '散落的发行版本标识未被拒绝'
fi
rm -f "$FIXTURE/ThingLinksJobPro.txt"

"$PRODUCT" check >/dev/null
[ ! -e "$FIXTURE/.thinglinks-product.lock" ] || fail_test '产品配置写锁未清理'

rollback_before="$(fingerprint)"
printf '%s\n' 'ThingLinksJobPro' > "$FIXTURE/rollback-marker.txt"
if "$PRODUCT" set-version 8.8.8 >/dev/null 2>&1; then
  fail_test '写入后检查失败未被拒绝'
fi
[ "$rollback_before" = "$(fingerprint)" ] || fail_test '写入后检查失败未恢复受管文件'
[ ! -e "$FIXTURE/.thinglinks-product.lock" ] || fail_test '回滚后产品配置写锁未清理'
rm -f "$FIXTURE/rollback-marker.txt"
"$PRODUCT" check >/dev/null

printf 'Job 产品配置隔离回归测试通过。\n'
