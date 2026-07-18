#!/bin/sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
TENANT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
MIGRATION_SQL="$SCRIPT_DIR/V1.4.0__sync_tenant_schema.sql"
BASELINE_SQL="$TENANT_DIR/baseline/thinglinks_base_1.sql"
RUNTIME_BASELINE_SQL="$TENANT_DIR/../../../../thinglinks-public/thinglinks-tenant-datasource-init/src/main/resources/schema/mysql/thinglinks_base.sql"
DEFAULT_BASELINE_SQL="$TENANT_DIR/../default/baseline/thinglinks_ds_c_defaults.sql"
VIDEO_ENTITY_DIR="$TENANT_DIR/../../../../thinglinks-video/thinglinks-video-entity/src/main/java/com/mqttsnet/thinglinks/video/entity"
VIDEO_SERVICE_DIR="$TENANT_DIR/../../../../thinglinks-video/thinglinks-video-biz/src/main/java/com/mqttsnet/thinglinks/video/service"

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
  if grep -Eiq -- "$pattern" "$file"; then
    fail "$message"
  fi
}

line_number() {
  pattern=$1
  file=$2
  grep -Fn -- "$pattern" "$file" | sed -n '1s/:.*//p'
}

reject_text 'ALTER[[:space:]]+TABLE[[:space:]]+`product`[[:space:]]+DROP[[:space:]]+COLUMN[[:space:]]+`product_version`' "$MIGRATION_SQL" '迁移不得删除 product.product_version'
reject_text 'ALTER[[:space:]]+TABLE[[:space:]]+`rule_groovy_script`[[:space:]]+DROP[[:space:]]+COLUMN' "$MIGRATION_SQL" '迁移不得删除规则脚本旧字段'
reject_text 'ALTER[[:space:]]+TABLE[[:space:]]+`video_media_server`[[:space:]]+DROP[[:space:]]+COLUMN' "$MIGRATION_SQL" '迁移不得删除媒体服务器旧地址字段'
reject_text 'DROP[[:space:]]+TABLE([^;]*)(`empowerment_record`|`video_device_channel`|`video_device_info`)' "$MIGRATION_SQL" '迁移不得删除三张旧业务表'
reject_text 'SET[[:space:]]+FOREIGN_KEY_CHECKS' "$MIGRATION_SQL" '迁移不得关闭外键检查'

require_text 'SET `active_version_no` = `product_version`' "$MIGRATION_SQL" '缺少产品版本回填'
require_text 'SET `script_type` = CASE' "$MIGRATION_SQL" '缺少规则脚本可证明字段回填'
require_text 'SET `host` = CASE' "$MIGRATION_SQL" '缺少媒体服务器地址回填'
require_text 'rule_script_duplicate_group_count_should_be_0' "$MIGRATION_SQL" '缺少规则脚本重复预检'
require_text 'media_server_duplicate_group_count_should_be_0' "$MIGRATION_SQL" '缺少媒体服务器重复预检'
require_text 'legacy_table_count_should_be_3' "$MIGRATION_SQL" '缺少旧业务表保留验证'
require_text 'video_encrypted_credential_column_count_should_be_3' "$MIGRATION_SQL" '缺少视频凭据字段类型验证'
require_text 'video_plaintext_credential_count_manual_migration' "$MIGRATION_SQL" '缺少历史明文凭据统计'
require_text 'video_group_relation_normalized_duplicate_group_count_should_be_0' "$MIGRATION_SQL" '缺少视频分组关联归一重复预检'
require_text 'video_gateway_mapping_normalized_duplicate_group_count_should_be_0' "$MIGRATION_SQL" '缺少视频网关映射归一重复预检'
require_text 'rule_trace_normalized_duplicate_group_count_should_be_0' "$MIGRATION_SQL" '缺少规则轨迹归一重复预检'
require_text 'video_nullable_identity_key_column_count_should_be_2' "$MIGRATION_SQL" '缺少视频可空标识生成列验证'
require_text 'video_normalized_unique_index_count_should_be_2_when_prechecks_pass' "$MIGRATION_SQL" '缺少视频归一唯一索引验证'
require_text 'video_legacy_nullable_unique_index_count_should_be_0_when_prechecks_pass' "$MIGRATION_SQL" '缺少视频旧可空唯一索引验证'
require_text 'rule_trace_identity_key_column_count_should_be_1' "$MIGRATION_SQL" '缺少规则轨迹归一生成列验证'
require_text 'rule_trace_normalized_unique_index_count_should_be_1_when_precheck_passes' "$MIGRATION_SQL" '缺少规则轨迹归一唯一索引验证'
require_text 'rule_trace_legacy_nullable_unique_index_count_should_be_0_when_precheck_passes' "$MIGRATION_SQL" '缺少规则轨迹旧可空唯一索引验证'
require_text 'MODIFY COLUMN `password` text' "$MIGRATION_SQL" '缺少视频密码字段加密容量扩充'
require_text 'MODIFY COLUMN `channel_config` longtext' "$MIGRATION_SQL" '缺少通知渠道凭据字段加密容量扩充'
require_text 'ADD COLUMN `channel_identification_key` varchar(52)' "$MIGRATION_SQL" '缺少视频分组通道标识归一生成列迁移'
require_text 'ADD COLUMN `src_channel_identification_key` varchar(66)' "$MIGRATION_SQL" '缺少视频网关源通道标识归一生成列迁移'
require_text 'ADD COLUMN `bridge_rule_id_key` varchar(22)' "$MIGRATION_SQL" '缺少规则轨迹规则ID归一生成列迁移'
require_text 'group_id,device_identification,channel_identification_key' "$MIGRATION_SQL" '视频分组归一索引缺少精确结构校验'
require_text 'src_protocol,src_device_identification,src_channel_identification_key' "$MIGRATION_SQL" '视频网关归一索引缺少精确结构校验'
require_text 'trace_id,bridge_rule_id_key' "$MIGRATION_SQL" '规则轨迹归一索引缺少精确结构校验'
require_text 'casewhenbridge_rule_idisnullthen' "$MIGRATION_SQL" '规则轨迹归一生成列表达式缺少精确校验'
require_text 'casewhendeleted<>0thenconcat' "$MIGRATION_SQL" '视频归一生成列未隔离逻辑删除记录'
require_text 'COUNT(`sub_part`) = 0' "$MIGRATION_SQL" '归一唯一索引缺少完整列索引校验'
require_text 'STORED GENERATED' "$MIGRATION_SQL" '视频可空标识迁移缺少生成列属性校验'

require_text '`password` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci' "$BASELINE_SQL" '视频密码基线字段容量不满足加密存储'
require_text '设备口令（EncryptTypeHandler 加密落盘）' "$BASELINE_SQL" '视频通道口令基线定义未启用加密存储'
require_text '`channel_config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci' "$BASELINE_SQL" '通知渠道凭证基线字段容量不满足加密存储'
require_text '渠道凭证JSON（EncryptTypeHandler 整体加密落盘）' "$BASELINE_SQL" '通知渠道凭证基线定义未启用加密存储'
require_text 'typeHandler = EncryptTypeHandler.class' "$VIDEO_ENTITY_DIR/device/VideoChannel.java" '视频通道实体缺少加密处理器'
require_text 'typeHandler = EncryptTypeHandler.class' "$VIDEO_ENTITY_DIR/device/VideoNotifySubscription.java" '通知订阅实体缺少加密处理器'
require_text 'typeHandler = EncryptTypeHandler.class' "$VIDEO_ENTITY_DIR/platform/VideoPlatform.java" '级联平台实体缺少加密处理器'
require_text '`channel_identification_key` varchar(52)' "$BASELINE_SQL" '视频分组通道标识归一生成列缺失'
require_text '`src_channel_identification_key` varchar(66)' "$BASELINE_SQL" '视频网关源通道标识归一生成列缺失'
require_text 'UNIQUE KEY `uk_group_device_channel_key`' "$BASELINE_SQL" '视频分组归一唯一索引缺失'
require_text 'UNIQUE KEY `uk_src_device_channel_key`' "$BASELINE_SQL" '视频网关归一唯一索引缺失'
require_text '`bridge_rule_id_key` varchar(22)' "$BASELINE_SQL" '规则轨迹规则ID归一生成列缺失'
require_text 'UNIQUE KEY `uk_trace_rule_key`' "$BASELINE_SQL" '规则轨迹归一唯一索引缺失'
require_text 'when (`deleted` <> 0) then concat' "$BASELINE_SQL" '视频基线生成列未按主键隔离逻辑删除记录'
require_text 'case when (`bridge_rule_id` is null)' "$BASELINE_SQL" '规则轨迹基线生成列未隔离空值与真实规则ID'
reject_text 'UNIQUE[[:space:]]+KEY[[:space:]]+`uk_group_device_channel`[[:space:]]*\(' "$BASELINE_SQL" '视频分组基线仍使用可被空值绕过的旧唯一索引'
reject_text 'UNIQUE[[:space:]]+KEY[[:space:]]+`uk_src_device_channel`[[:space:]]*\(' "$BASELINE_SQL" '视频网关基线仍使用可被空值绕过的旧唯一索引'
reject_text 'UNIQUE[[:space:]]+KEY[[:space:]]+`uk_trace_rule`[[:space:]]*\(' "$BASELINE_SQL" '规则轨迹基线仍使用可被空值绕过的旧唯一索引'
require_text '`channel_identification_key` varchar(52)' "$RUNTIME_BASELINE_SQL" '运行时租户初始化缺少视频分组归一生成列'
require_text '`src_channel_identification_key` varchar(66)' "$RUNTIME_BASELINE_SQL" '运行时租户初始化缺少视频网关归一生成列'
require_text '`bridge_rule_id_key` varchar(22)' "$RUNTIME_BASELINE_SQL" '运行时租户初始化缺少规则轨迹归一生成列'
require_text 'UNIQUE KEY `uk_group_device_channel_key`' "$RUNTIME_BASELINE_SQL" '运行时租户初始化缺少视频分组归一唯一索引'
require_text 'UNIQUE KEY `uk_src_device_channel_key`' "$RUNTIME_BASELINE_SQL" '运行时租户初始化缺少视频网关归一唯一索引'
require_text 'UNIQUE KEY `uk_trace_rule_key`' "$RUNTIME_BASELINE_SQL" '运行时租户初始化缺少规则轨迹归一唯一索引'
require_text 'when (`deleted` <> 0) then concat' "$RUNTIME_BASELINE_SQL" '运行时租户初始化未按主键隔离视频逻辑删除记录'
require_text 'case when (`bridge_rule_id` is null)' "$RUNTIME_BASELINE_SQL" '运行时租户初始化未隔离空规则ID与真实规则ID'
require_text '`password` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci' "$RUNTIME_BASELINE_SQL" '运行时租户初始化的视频密码字段容量不满足加密存储'
require_text '`channel_config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci' "$RUNTIME_BASELINE_SQL" '运行时租户初始化的通知凭据字段容量不满足加密存储'
require_text '设备口令（EncryptTypeHandler 加密落盘）' "$RUNTIME_BASELINE_SQL" '运行时租户初始化的视频通道口令未标记加密存储'
require_text '渠道凭证JSON（EncryptTypeHandler 整体加密落盘）' "$RUNTIME_BASELINE_SQL" '运行时租户初始化的通知凭据未标记加密存储'
require_text '认证密码（EncryptTypeHandler 加密落盘）' "$RUNTIME_BASELINE_SQL" '运行时租户初始化的级联密码未标记加密存储'
reject_text 'UNIQUE[[:space:]]+KEY[[:space:]]+`uk_group_device_channel`[[:space:]]*\(' "$RUNTIME_BASELINE_SQL" '运行时租户初始化仍使用视频分组旧可空唯一索引'
reject_text 'UNIQUE[[:space:]]+KEY[[:space:]]+`uk_src_device_channel`[[:space:]]*\(' "$RUNTIME_BASELINE_SQL" '运行时租户初始化仍使用视频网关旧可空唯一索引'
reject_text 'UNIQUE[[:space:]]+KEY[[:space:]]+`uk_trace_rule`[[:space:]]*\(' "$RUNTIME_BASELINE_SQL" '运行时租户初始化仍使用规则轨迹旧可空唯一索引'
require_text 'VideoIdentityNormalizer.trimAsciiSpaceToNull(channelIdentification)' "$VIDEO_SERVICE_DIR/group/impl/VideoDeviceGroupRelationServiceImpl.java" '视频分组绑定未按数据库规则归一通道标识'
require_text 'VideoIdentityNormalizer.trimAsciiSpaceToNull' "$VIDEO_SERVICE_DIR/gateway/impl/VideoGatewayMappingServiceImpl.java" '视频网关映射未按数据库规则归一源通道标识'

rule_update_line=$(line_number 'SET `script_type` = CASE' "$MIGRATION_SQL")
rule_index_line=$(line_number 'ADD UNIQUE INDEX `uk_rule_groovy_script_identity`' "$MIGRATION_SQL")
media_update_line=$(line_number 'SET `host` = CASE' "$MIGRATION_SQL")
media_index_line=$(line_number 'ADD UNIQUE INDEX `uk_media_server_unique_host_http_port`' "$MIGRATION_SQL")
group_precheck_line=$(line_number 'video_group_relation_normalized_duplicate_group_count_should_be_0' "$MIGRATION_SQL")
group_index_line=$(line_number 'ADD UNIQUE INDEX `uk_group_device_channel_key`' "$MIGRATION_SQL")
group_drop_line=$(line_number 'DROP INDEX `uk_group_device_channel`' "$MIGRATION_SQL")
gateway_precheck_line=$(line_number 'video_gateway_mapping_normalized_duplicate_group_count_should_be_0' "$MIGRATION_SQL")
gateway_index_line=$(line_number 'ADD UNIQUE INDEX `uk_src_device_channel_key`' "$MIGRATION_SQL")
gateway_drop_line=$(line_number 'DROP INDEX `uk_src_device_channel`' "$MIGRATION_SQL")
trace_precheck_line=$(line_number 'rule_trace_normalized_duplicate_group_count_should_be_0' "$MIGRATION_SQL")
trace_index_line=$(line_number 'ADD UNIQUE INDEX `uk_trace_rule_key`' "$MIGRATION_SQL")
trace_drop_line=$(line_number 'DROP INDEX `uk_trace_rule`' "$MIGRATION_SQL")

[ -n "$rule_update_line" ] && [ -n "$rule_index_line" ] && [ "$rule_update_line" -lt "$rule_index_line" ] \
  || fail '规则脚本必须先回填再创建唯一索引'
[ -n "$media_update_line" ] && [ -n "$media_index_line" ] && [ "$media_update_line" -lt "$media_index_line" ] \
  || fail '媒体服务器必须先回填再创建唯一索引'
[ -n "$group_precheck_line" ] && [ -n "$group_index_line" ] && [ -n "$group_drop_line" ] \
  && [ "$group_precheck_line" -lt "$group_index_line" ] && [ "$group_index_line" -lt "$group_drop_line" ] \
  || fail '视频分组关联必须先重复预检、再创建归一索引、最后移除旧索引'
[ -n "$gateway_precheck_line" ] && [ -n "$gateway_index_line" ] && [ -n "$gateway_drop_line" ] \
  && [ "$gateway_precheck_line" -lt "$gateway_index_line" ] && [ "$gateway_index_line" -lt "$gateway_drop_line" ] \
  || fail '视频网关映射必须先重复预检、再创建归一索引、最后移除旧索引'
[ -n "$trace_precheck_line" ] && [ -n "$trace_index_line" ] && [ -n "$trace_drop_line" ] \
  && [ "$trace_precheck_line" -lt "$trace_index_line" ] && [ "$trace_index_line" -lt "$trace_drop_line" ] \
  || fail '规则轨迹必须先重复预检、再创建归一索引、最后移除旧索引'

reject_text '680740870356661376' "$BASELINE_SQL" '基线仍包含已确认的孤儿资源关系'
require_text 'base_role_resource_orphan_count_should_be_0' "$BASELINE_SQL" '基线缺少零孤儿验证'
require_text '__AMAP_JS_API_KEY__' "$BASELINE_SQL" '地图 JavaScript API 密钥占位符缺失'
require_text '__AMAP_SECURITY_JS_CODE__' "$BASELINE_SQL" '地图安全码占位符缺失'
reject_text '-----BEGIN[[:space:]].*PRIVATE KEY-----|AKIA[0-9A-Z]{16}|sk-[A-Za-z0-9_-]{20,}' "$BASELINE_SQL" '基线疑似包含私钥或访问密钥'

relations_file=$(mktemp "${TMPDIR:-/tmp}/thinglinks-relations.XXXXXX")
resources_file=$(mktemp "${TMPDIR:-/tmp}/thinglinks-resources.XXXXXX")
roles_file=$(mktemp "${TMPDIR:-/tmp}/thinglinks-roles.XXXXXX")
trap 'rm -f "$relations_file" "$resources_file" "$roles_file"' EXIT HUP INT TERM

sed -nE 's/^INSERT INTO `base_role_resource_rel`.*VALUES \([^,]+, ([0-9]+), ([0-9]+), ([0-9]+),.*/\1 \2 \3/p' "$BASELINE_SQL" | sort -u > "$relations_file"
sed -nE 's/^INSERT INTO `def_resource`.*VALUES \(([0-9]+), ([0-9]+),.*/\1 \2/p' "$DEFAULT_BASELINE_SQL" | sort -u > "$resources_file"
sed -nE 's/^INSERT INTO `base_role`.*VALUES \(([0-9]+),.*/\1/p' "$BASELINE_SQL" | sort -u > "$roles_file"

while read -r resource_id application_id role_id; do
  grep -Fqx -- "$resource_id $application_id" "$resources_file" \
    || fail "角色资源关系引用了不存在或应用不匹配的资源：$resource_id/$application_id"
  grep -Fqx -- "$role_id" "$roles_file" \
    || fail "角色资源关系引用了不存在的角色：$role_id"
done < "$relations_file"

printf '租户库迁移静态校验通过。\n'
