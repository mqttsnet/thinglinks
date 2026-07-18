#!/bin/sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DEFAULT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
BASELINE_SQL="$DEFAULT_DIR/baseline/thinglinks_ds_c_defaults.sql"
SANITIZE_SQL="$SCRIPT_DIR/V1.4.0__sanitize_seed_data.sql"

fail() {
  printf '校验失败：%s\n' "$1" >&2
  exit 1
}

require_text() {
  pattern=$1
  file=$2
  message=$3
  grep -Fq -- "$pattern" "$file" || fail "$message"
}

reject_text() {
  pattern=$1
  file=$2
  message=$3
  if grep -Eq -- "$pattern" "$file"; then
    fail "$message"
  fi
}

[ -f "$BASELINE_SQL" ] || fail "默认库基线不存在：$BASELINE_SQL"
[ -f "$SANITIZE_SQL" ] || fail "存量种子清理迁移不存在：$SANITIZE_SQL"

# 新基线只包含可重复初始化的静态配置，不包含运行时数据或固定私钥。
reject_text '^INSERT INTO `(com_appendix|com_file|def_login_log|def_tenant_application_record|def_tenant_ds_c_rel|sop_doc_app|sop_doc_content|sop_doc_info|sop_help_doc|sop_isv_info|sop_isv_keys|sop_notify_info|sop_perm_isv_group|worker_node)`' "$BASELINE_SQL" '基线仍包含运行时、演示或密钥数据'
reject_text '483305815051076198|442312581298783074|633773629138778488|251763346439667712|291271427745644580|291271427745644606|20720901786330726[4-7]|179582070228516870|524352044412200344' "$BASELINE_SQL" '基线仍包含已确认的演示或测试种子主键'
reject_text 'TEST_ADD_DICT|DefGenTest(Simple|Tree)|DATA_SCOPE_TEST|测试通过脚本执行接口逻辑|测试树结构|test1' "$BASELINE_SQL" '基线仍包含运行时测试元数据'
reject_text 'thinglinks\.mqttsnet\.com|https?://[^[:space:]\x27]*mqttsnet\.com' "$BASELINE_SQL" '基线仍包含私有环境地址'
reject_text '-----BEGIN[[:space:]].*PRIVATE KEY-----|AKIA[0-9A-Z]{16}|sk-[A-Za-z0-9_-]{20,}' "$BASELINE_SQL" '基线疑似包含私钥或访问密钥'

require_text '__ADMIN_PASSWORD_SHA256__' "$BASELINE_SQL" '管理员密码占位符缺失'
require_text '__ADMIN_SALT__' "$BASELINE_SQL" '管理员盐占位符缺失'
require_text '__THINGLINKS_MYSQL_PASSWORD__' "$BASELINE_SQL" 'MySQL 密码占位符缺失'
require_text '__THINGLINKS_TDENGINE_PASSWORD__' "$BASELINE_SQL" 'TDengine 密码占位符缺失'
require_text '__SOP_OPEN_PRODUCTION_URL__' "$BASELINE_SQL" '开放平台生产地址占位符缺失'
require_text '__SOP_OPEN_SANDBOX_URL__' "$BASELINE_SQL" '开放平台沙箱地址占位符缺失'
require_text '__TORNA_SERVER_URL__' "$BASELINE_SQL" 'Torna 地址占位符缺失'
require_text '__NACOS_CONSOLE_URL__' "$BASELINE_SQL" 'Nacos 控制台地址占位符缺失'
require_text '__SKYWALKING_CONSOLE_URL__' "$BASELINE_SQL" 'SkyWalking 控制台地址占位符缺失'
require_text '__MINIO_CONSOLE_URL__' "$BASELINE_SQL" 'MinIO 控制台地址占位符缺失'
require_text '__JOB_ADMIN_URL__' "$BASELINE_SQL" 'Job 控制台地址占位符缺失'
require_text '__FILE_PREVIEW_URL__' "$BASELINE_SQL" '文件预览地址占位符缺失'
require_text '__JENKINS_URL__' "$BASELINE_SQL" 'Jenkins 地址占位符缺失'
require_text '__SCADA_URL__' "$BASELINE_SQL" 'SCADA 地址占位符缺失'
require_text '__NODERED_MANAGER_URL__' "$BASELINE_SQL" 'Node-RED 管理地址占位符缺失'

# 内置应用密钥必须全部是用途明确的占位符。
application_secret_count=0
while IFS= read -r secret; do
  application_secret_count=$((application_secret_count + 1))
  case "$secret" in
    __*_APP_SECRET__) ;;
    *) fail "内置应用仍包含非占位密钥：$secret" ;;
  esac
done <<EOF
$(awk -F"'" '/^INSERT INTO `def_application`/ { print $4 }' "$BASELINE_SQL")
EOF
[ "$application_secret_count" -eq 7 ] || fail "内置应用数量应为 7，实际为 $application_secret_count"

tenant_count=$(grep -c '^INSERT INTO `def_tenant` ' "$BASELINE_SQL" || true)
user_count=$(grep -c '^INSERT INTO `def_user` ' "$BASELINE_SQL" || true)
user_tenant_count=$(grep -c '^INSERT INTO `def_user_tenant_rel` ' "$BASELINE_SQL" || true)
[ "$tenant_count" -eq 1 ] || fail "基线应只包含 1 个内置租户，实际为 $tenant_count"
[ "$user_count" -eq 1 ] || fail "基线应只包含 1 个内置管理员，实际为 $user_count"
[ "$user_tenant_count" -eq 1 ] || fail "基线应只包含 1 条管理员租户关系，实际为 $user_tenant_count"

# 清理迁移不得重新保存旧敏感原文，且必须具备摘要匹配和严格身份门禁。
require_text 'SHA2(`token`, 256)' "$SANITIZE_SQL" 'Torna token 未使用摘要识别'
require_text 'SHA2(CONCAT_WS(' "$SANITIZE_SQL" '测试用户未使用组合摘要识别'
require_text 'SHA2(`app_secret`, 256)' "$SANITIZE_SQL" '测试应用密钥未使用摘要识别'
require_text '`id` = 483305815051076198' "$SANITIZE_SQL" '演示租户主键门禁缺失'
require_text '`name` = '\''MqttsNet演示企业'\''' "$SANITIZE_SQL" '演示租户业务身份门禁缺失'
require_text 'legacy_runtime_test_asset_count_should_be_0' "$SANITIZE_SQL" '迁移末尾缺少测试资产验证'
reject_text '(`token`|`app_secret`)[[:space:]]*=|-----BEGIN[[:space:]].*PRIVATE KEY-----|MIIEv[A-Za-z0-9+/=]{100,}' "$SANITIZE_SQL" '清理迁移仍使用敏感原文匹配，或保存了私钥原文'

# 解析基线的核心关系，防止清理后留下孤儿引用。
applications_file=$(mktemp "${TMPDIR:-/tmp}/thinglinks-default-applications.XXXXXX")
tenants_file=$(mktemp "${TMPDIR:-/tmp}/thinglinks-default-tenants.XXXXXX")
users_file=$(mktemp "${TMPDIR:-/tmp}/thinglinks-default-users.XXXXXX")
resources_file=$(mktemp "${TMPDIR:-/tmp}/thinglinks-default-resources.XXXXXX")
trap 'rm -f "$applications_file" "$tenants_file" "$users_file" "$resources_file"' EXIT HUP INT TERM

sed -nE 's/^INSERT INTO `def_application`.*VALUES \(([0-9]+),.*/\1/p' "$BASELINE_SQL" | sort -u > "$applications_file"
sed -nE 's/^INSERT INTO `def_tenant`.*VALUES \(([0-9]+),.*/\1/p' "$BASELINE_SQL" | sort -u > "$tenants_file"
sed -nE 's/^INSERT INTO `def_user`.*VALUES \(([0-9]+),.*/\1/p' "$BASELINE_SQL" | sort -u > "$users_file"
sed -nE 's/^INSERT INTO `def_resource`.*VALUES \(([0-9]+), ([0-9]+),.*/\1 \2/p' "$BASELINE_SQL" | sort -u > "$resources_file"

sed -nE 's/^INSERT INTO `def_tenant_application_rel`.*VALUES \([^,]+, ([0-9]+), ([0-9]+),.*/\1 \2/p' "$BASELINE_SQL" |
while read -r tenant_id application_id; do
  grep -Fqx -- "$tenant_id" "$tenants_file" || fail "租户应用关系引用不存在的租户：$tenant_id"
  grep -Fqx -- "$application_id" "$applications_file" || fail "租户应用关系引用不存在的应用：$application_id"
done

sed -nE 's/^INSERT INTO `def_user_tenant_rel`.*VALUES \([^,]+, [^,]+, ([0-9]+), [^,]+, ([0-9]+),.*/\1 \2/p' "$BASELINE_SQL" |
while read -r user_id tenant_id; do
  grep -Fqx -- "$user_id" "$users_file" || fail "用户租户关系引用不存在的用户：$user_id"
  grep -Fqx -- "$tenant_id" "$tenants_file" || fail "用户租户关系引用不存在的租户：$tenant_id"
done

sed -nE 's/^INSERT INTO `def_tenant_resource_rel`.*VALUES \([^,]+, ([0-9]+), ([0-9]+), ([0-9]+),.*/\1 \2 \3/p' "$BASELINE_SQL" |
while read -r resource_id application_id tenant_id; do
  grep -Fqx -- "$resource_id $application_id" "$resources_file" || fail "租户资源关系引用不存在或应用不匹配的资源：$resource_id/$application_id"
  grep -Fqx -- "$tenant_id" "$tenants_file" || fail "租户资源关系引用不存在的租户：$tenant_id"
done

printf '默认库基线与存量清理迁移静态校验通过。\n'
