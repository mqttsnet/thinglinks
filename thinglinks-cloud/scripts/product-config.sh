#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MANIFEST="$PROJECT_ROOT/.thinglinks-product.env"
ROOT_POM="$PROJECT_ROOT/pom.xml"
DEPENDENCIES_POM="$PROJECT_ROOT/thinglinks-dependencies-parent/pom.xml"
SDK_POM="$PROJECT_ROOT/thinglinks-sdk/pom.xml"
MQ_CONSTANT_FILE="$PROJECT_ROOT/thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/mq/ConsumerGroupConstant.java"
MQ_ROUTE_FILE="$PROJECT_ROOT/thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/mq/BizMqRouteConstant.java"
PRODUCT_LOCK_REF='refs/thinglinks/product-config-lock'
PRODUCT_LOCK_OWNER=''
PRODUCT_LOCK_TOKEN=''
PRODUCT_LOCK_OID=''
PRODUCT_LOCK_HELD=false

# Kafka 消费组业务后缀清单，与 ConsumerGroupConstant 中的前缀组合使用。
KAFKA_CONSUMER_GROUP_BINDINGS=(
  'thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/MqttKafkaInboundConsumer.java|BUS_MQTT'
  'thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/TcpKafkaInboundConsumer.java|BUS_TCP'
  'thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/WsKafkaInboundConsumer.java|BUS_WEBSOCKET'
  'thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/audit/MqttSessionStartAuditConsumer.java|AUDIT_SESSION_START'
  'thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/audit/MqttSessionStopAuditConsumer.java|AUDIT_SESSION_STOP'
  'thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/audit/NotAuthorizedClientAuditConsumer.java|AUDIT_NOT_AUTHORIZED'
)

MANIFEST_KEYS=(
  THINGLINKS_PRODUCT_CODE
  THINGLINKS_PRODUCT_NAME
  THINGLINKS_PRODUCT_NAME_ZH
  THINGLINKS_COMPONENT_CODE
  THINGLINKS_COMPONENT_NAME
  THINGLINKS_COMPONENT_VERSION
  THINGLINKS_UTIL_VERSION
  THINGLINKS_MAVEN_ARTIFACT_ID
  THINGLINKS_EDITION_CODE
  THINGLINKS_EDITION_NAME_ZH
  THINGLINKS_EDITION_NAME_EN
  THINGLINKS_LICENSE_MODEL
  THINGLINKS_LICENSE_FILE
  THINGLINKS_MQ_NAMESPACE
  THINGLINKS_PRODUCT_MANIFEST_VERSION
  THINGLINKS_SYNC_PROTECTED_PATHS
)

STATE_FILES=(
  "$MANIFEST"
  "$ROOT_POM"
  "$DEPENDENCIES_POM"
  "$SDK_POM"
  "$MQ_CONSTANT_FILE"
)

fail() {
  printf '错误：%s\n' "$1" >&2
  exit 1
}

release_product_lock() {
  [ "$PRODUCT_LOCK_HELD" = true ] || return 0

  local current_oid=''
  current_oid="$(git -C "$PROJECT_ROOT" rev-parse --verify "$PRODUCT_LOCK_REF" 2>/dev/null)" || current_oid=''
  if [ "$current_oid" != "$PRODUCT_LOCK_OID" ]; then
    printf '错误：产品配置写锁归属发生变化，保留当前锁引用。\n' >&2
    PRODUCT_LOCK_HELD=false
    return 1
  fi
  if ! git -C "$PROJECT_ROOT" update-ref -d "$PRODUCT_LOCK_REF" "$PRODUCT_LOCK_OID" 2>/dev/null; then
    printf '错误：产品配置写锁释放失败，当前锁引用未被删除。\n' >&2
    PRODUCT_LOCK_HELD=false
    return 1
  fi
  PRODUCT_LOCK_HELD=false
}

cleanup_product_lock() {
  local status=$?
  trap - EXIT HUP INT TERM
  if ! release_product_lock && [ "$status" -eq 0 ]; then
    status=1
  fi
  exit "$status"
}

assert_product_lock_owned() {
  if [ "$PRODUCT_LOCK_HELD" != true ]; then
    printf '错误：写入产品配置前必须持有仓库写锁。\n' >&2
    return 1
  fi
  local current_oid=''
  current_oid="$(git -C "$PROJECT_ROOT" rev-parse --verify "$PRODUCT_LOCK_REF" 2>/dev/null)" || current_oid=''
  if [ "$current_oid" != "$PRODUCT_LOCK_OID" ]; then
    printf '错误：产品配置写锁归属发生变化，拒绝写入受管文件。\n' >&2
    return 1
  fi
}

product_lock_is_owned() {
  [ "$PRODUCT_LOCK_HELD" = true ] || return 1
  local current_oid=''
  current_oid="$(git -C "$PROJECT_ROOT" rev-parse --verify "$PRODUCT_LOCK_REF" 2>/dev/null)" || return 1
  [ "$current_oid" = "$PRODUCT_LOCK_OID" ]
}

after_managed_file_replaced() {
  :
}

read_product_lock_owner() {
  local owner_oid="$1"
  local owner_line=''
  owner_line="$(git -C "$PROJECT_ROOT" cat-file blob "$owner_oid" 2>/dev/null)" || return 1
  [[ "$owner_line" =~ ^[1-9][0-9]*[[:space:]][A-Za-z0-9._-]+[[:space:]][0-9]+$ ]] || return 2
  printf '%s\n' "$owner_line"
}

prepare_product_lock_owner() {
  local created_at owner_pid
  created_at="$(date +%s)"
  owner_pid="${BASHPID:-$$}"
  PRODUCT_LOCK_TOKEN="${BASHPID:-$$}-${created_at}-$RANDOM-$RANDOM"
  PRODUCT_LOCK_OWNER="$owner_pid $PRODUCT_LOCK_TOKEN $created_at"
  PRODUCT_LOCK_OID="$(printf '%s\n' "$PRODUCT_LOCK_OWNER" | git -C "$PROJECT_ROOT" hash-object -w --stdin 2>/dev/null)" \
    || fail "无法创建产品配置写锁所有者对象"
  [[ "$PRODUCT_LOCK_OID" =~ ^[0-9a-f]{40}([0-9a-f]{24})?$ ]] \
    || fail "产品配置写锁所有者对象格式不正确"

  trap cleanup_product_lock EXIT
  trap 'exit 129' HUP
  trap 'exit 130' INT
  trap 'exit 143' TERM
}

acquire_product_lock() {
  local observed_oid='' observed_owner='' owner_pid='' attempt
  git -C "$PROJECT_ROOT" rev-parse --is-inside-work-tree >/dev/null 2>&1 \
    || fail "产品配置写操作必须在 Git 工作区内执行"
  prepare_product_lock_owner

  for attempt in 1 2 3 4 5 6 7 8; do
    if git -C "$PROJECT_ROOT" update-ref "$PRODUCT_LOCK_REF" "$PRODUCT_LOCK_OID" '' 2>/dev/null; then
      PRODUCT_LOCK_HELD=true
      return 0
    fi

    observed_oid="$(git -C "$PROJECT_ROOT" rev-parse --verify "$PRODUCT_LOCK_REF" 2>/dev/null)" || {
      sleep 0.02
      continue
    }
    observed_owner=''
    if ! observed_owner="$(read_product_lock_owner "$observed_oid")"; then
      fail "产品配置写锁所有者信息不完整"
    fi
    owner_pid="${observed_owner%% *}"
    if kill -0 "$owner_pid" 2>/dev/null; then
      fail "已有产品配置写操作正在执行（PID：${owner_pid}）"
    fi

    if git -C "$PROJECT_ROOT" update-ref -d "$PRODUCT_LOCK_REF" "$observed_oid" 2>/dev/null; then
      continue
    fi
    sleep 0.02
  done

  fail "无法获得产品配置写锁，请稍后重试"
}

is_allowed_key() {
  local candidate="$1"
  local allowed
  for allowed in "${MANIFEST_KEYS[@]}"; do
    [ "$candidate" = "$allowed" ] && return 0
  done
  return 1
}

load_manifest() {
  [ -f "$MANIFEST" ] || fail "缺少产品配置文件：$MANIFEST"

  local allowed
  for allowed in "${MANIFEST_KEYS[@]}"; do
    printf -v "$allowed" '%s' ''
  done

  local line key value last_character seen_keys
  seen_keys=$'\n'
  while IFS= read -r line || [ -n "$line" ]; do
    line="${line%$'\r'}"
    case "$line" in
      ''|'#'*) continue ;;
      *=*)
        key="${line%%=*}"
        value="${line#*=}"
        ;;
      *) fail "产品配置存在无法解析的行：$line" ;;
    esac

    [[ "$key" =~ ^[A-Z][A-Z0-9_]*$ ]] || fail "产品配置键格式不正确：$key"
    is_allowed_key "$key" || fail "产品配置包含未登记的键：$key"
    case "$seen_keys" in
      *$'\n'"$key"$'\n'*) fail "产品配置键重复：$key" ;;
    esac

    if [ "${value#\"}" != "$value" ]; then
      last_character="${value#${value%?}}"
      [ "$last_character" = '"' ] || fail "产品配置项 $key 的双引号未闭合"
      value="${value#\"}"
      value="${value%\"}"
      case "$value" in
        *'"'*) fail "产品配置项 $key 不支持转义双引号" ;;
      esac
    else
      case "$value" in
        *[[:space:]]*) fail "产品配置项 $key 含空格时必须使用双引号" ;;
      esac
    fi

    case "$value" in
      *'$('*|*'${'*|*'`'*|*';'*|*'|'*|*'&'*|*'<'*|*'>'*)
        fail "产品配置项 $key 含有不允许的表达式或控制字符"
        ;;
    esac

    printf -v "$key" '%s' "$value"
    seen_keys="${seen_keys}${key}"$'\n'
  done < "$MANIFEST"
}

check_manifest_comments() {
  perl -ne '
    utf8::decode($_);
    if (/^([A-Z][A-Z0-9_]*)=/) {
      if (!defined($previous) || $previous !~ /^#[^#]/ || $previous !~ /\p{Han}/) {
        print STDERR "配置项 $1 前必须紧邻一行中文注释\n";
        $invalid = 1;
      }
    }
    $previous = $_ if /\S/;
    END { exit($invalid ? 1 : 0); }
  ' "$MANIFEST" || fail "产品配置中的每个配置项都必须紧邻一行中文注释"
}

validate_version() {
  local label="$1"
  local value="$2"
  [[ "$value" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+)?(-[0-9A-Za-z.-]+)?$ ]] || fail "$label 版本号格式不正确：$value"
}

validate_slug() {
  local label="$1"
  local value="$2"
  [[ "$value" =~ ^[a-z][a-z0-9-]*$ ]] || fail "$label 必须是小写字母开头的稳定编码：$value"
}

validate_stable_slug() {
  local label="$1"
  local value="$2"
  validate_slug "$label" "$value"
  case "-$value-" in
    *-pro-*|*-community-*|*-commercial-*|*-enterprise-*)
      fail "$label 不得包含发行版本标识：$value"
      ;;
  esac
  case "$value" in
    *pro|*community|*commercial|*enterprise)
      fail "$label 不得包含紧凑发行版本标识：$value"
      ;;
  esac
}

validate_display_value() {
  local key="$1"
  local value="$2"
  [ -n "$value" ] || fail "产品配置项 $key 不能为空"
  case "$value" in
    *$'\n'*|*$'\r'*|*$'\t'*) fail "产品配置项 $key 不得包含控制字符" ;;
  esac
}

validate_stable_display_value() {
  local key="$1"
  local value="$2"
  local release_word flagship_word commercial_word community_word open_source_word enterprise_word pattern
  validate_display_value "$key" "$value"
  release_word='p''ro'
  flagship_word='旗''舰版'
  commercial_word='商''业版'
  community_word='社''区版'
  open_source_word='开''源版'
  enterprise_word='企''业版'
  pattern="(^|[^[:alnum:]])(${release_word}|community|commercial|enterprise)([^[:alnum:]]|$)|(^|[^[:alnum:]])(professional|open[[:space:]_-]+source)[[:space:]_-]+edition([^[:alnum:]]|$)|${flagship_word}|${commercial_word}|${community_word}|${open_source_word}|${enterprise_word}"
  if printf '%s\n' "$value" | grep -iqE "$pattern"; then
    fail "产品配置项 $key 不得包含发行版本标识"
  fi
}

split_protected_paths() {
  local old_ifs="$IFS"
  local had_noglob=false
  case "$THINGLINKS_SYNC_PROTECTED_PATHS" in
    ,*|*,|*,,*) fail "同步保护路径不能包含空项" ;;
  esac
  case "$-" in
    *f*) had_noglob=true ;;
  esac
  set -f
  IFS=','
  read -r -a PROTECTED_PATHS <<< "$THINGLINKS_SYNC_PROTECTED_PATHS" || fail "同步保护路径无法解析"
  IFS="$old_ifs"
  [ "$had_noglob" = true ] || set +f
}

has_exact_protected_path() {
  local relative_path="$1"
  local protected
  for protected in "${PROTECTED_PATHS[@]}"; do
    [ "$protected" = "$relative_path" ] && return 0
  done
  return 1
}

check_protected_paths() {
  split_protected_paths
  [ "${#PROTECTED_PATHS[@]}" -gt 0 ] || fail "同步保护路径不能为空"

  local protected seen_paths
  seen_paths=$'\n'
  for protected in "${PROTECTED_PATHS[@]}"; do
    [ -n "$protected" ] || fail "同步保护路径不能包含空项"
    case "$protected" in
      ../LICENSE) ;;
      /*|.|..|./*|*/.|*/./*|../*|*/..|*'/../'*|*//*|*/|*'*'*|*'?'*|*'['*|*']'*|*'\'*|*[[:space:]]*)
        fail "同步保护路径必须是明确的仓库相对路径：$protected"
        ;;
    esac
    [[ "$protected" =~ ^[A-Za-z0-9._/-]+$ ]] \
      || fail "同步保护路径包含不安全字符：$protected"
    case "$seen_paths" in
      *$'\n'"$protected"$'\n'*) fail "同步保护路径重复：$protected" ;;
    esac
    seen_paths="${seen_paths}${protected}"$'\n'
    [ -e "$PROJECT_ROOT/$protected" ] || fail "同步保护路径不存在：$protected"
  done

  has_exact_protected_path '.thinglinks-product.env' \
    || fail "同步保护路径必须包含产品清单：.thinglinks-product.env"
  if [ -e "$PROJECT_ROOT/LICENSE" ]; then
    has_exact_protected_path 'LICENSE' \
      || fail "同步保护路径必须包含基础授权文件：LICENSE"
  fi
  has_exact_protected_path "$THINGLINKS_LICENSE_FILE" \
    || fail "同步保护路径必须包含附加授权文件：$THINGLINKS_LICENSE_FILE"
}

check_required_values() {
  local key
  for key in "${MANIFEST_KEYS[@]}"; do
    [ -n "${!key:-}" ] || fail "产品配置项 $key 不能为空"
  done

  validate_stable_slug "产品编码" "$THINGLINKS_PRODUCT_CODE"
  validate_stable_display_value THINGLINKS_PRODUCT_NAME "$THINGLINKS_PRODUCT_NAME"
  validate_stable_display_value THINGLINKS_PRODUCT_NAME_ZH "$THINGLINKS_PRODUCT_NAME_ZH"
  validate_stable_slug "组件编码" "$THINGLINKS_COMPONENT_CODE"
  validate_stable_display_value THINGLINKS_COMPONENT_NAME "$THINGLINKS_COMPONENT_NAME"
  validate_version "Cloud" "$THINGLINKS_COMPONENT_VERSION"
  validate_version "Util" "$THINGLINKS_UTIL_VERSION"
  validate_stable_slug "Maven artifactId" "$THINGLINKS_MAVEN_ARTIFACT_ID"
  [ "$THINGLINKS_COMPONENT_CODE" = "$THINGLINKS_MAVEN_ARTIFACT_ID" ] || fail "组件编码必须与 Maven artifactId 保持一致"
  [[ "$THINGLINKS_EDITION_CODE" =~ ^(community|commercial|enterprise)$ ]] || fail "发行版本编码不在允许范围内"
  validate_display_value THINGLINKS_EDITION_NAME_ZH "$THINGLINKS_EDITION_NAME_ZH"
  validate_display_value THINGLINKS_EDITION_NAME_EN "$THINGLINKS_EDITION_NAME_EN"
  [[ "$THINGLINKS_LICENSE_MODEL" =~ ^(open-source|commercial|dual-license)$ ]] \
    || fail "授权模型编码仅允许 open-source、commercial、dual-license：$THINGLINKS_LICENSE_MODEL"
  case "${THINGLINKS_EDITION_CODE}:${THINGLINKS_LICENSE_MODEL}" in
    community:open-source|commercial:commercial|commercial:dual-license|enterprise:commercial|enterprise:dual-license) ;;
    *) fail "发行版本与授权模型不匹配：${THINGLINKS_EDITION_CODE}/${THINGLINKS_LICENSE_MODEL}" ;;
  esac
  [ "$THINGLINKS_LICENSE_FILE" = '../LICENSE' ] \
    || [[ "$THINGLINKS_LICENSE_FILE" =~ ^[A-Za-z0-9._/-]+$ ]] \
    || fail "授权文件路径格式不正确"
  [ -f "$PROJECT_ROOT/$THINGLINKS_LICENSE_FILE" ] || fail "授权文件不存在：$THINGLINKS_LICENSE_FILE"
  validate_stable_slug "消息队列命名空间" "$THINGLINKS_MQ_NAMESPACE"
  [ "$THINGLINKS_PRODUCT_MANIFEST_VERSION" = "1" ] || fail "当前脚本仅支持清单格式版本 1"
  check_protected_paths
}

install_atomically() {
  local source_file="$1"
  local target_file="$2"
  local staged_file
  if ! assert_product_lock_owned; then
    return 1
  fi
  staged_file="$(mktemp "$(dirname "$target_file")/.thinglinks-product.XXXXXX")" || return 1
  if ! cp -p "$source_file" "$staged_file"; then
    rm -f "$staged_file"
    return 1
  fi
  if ! assert_product_lock_owned; then
    rm -f "$staged_file"
    return 1
  fi
  if ! mv -f "$staged_file" "$target_file"; then
    rm -f "$staged_file"
    return 1
  fi
  after_managed_file_replaced "$target_file"
}

backup_state() {
  local backup_dir="$1"
  local index
  for ((index = 0; index < ${#STATE_FILES[@]}; index++)); do
    cp -p "${STATE_FILES[$index]}" "$backup_dir/state-$index" || return 1
  done
}

restore_state() {
  local backup_dir="$1"
  local index
  for ((index = 0; index < ${#STATE_FILES[@]}; index++)); do
    install_atomically "$backup_dir/state-$index" "${STATE_FILES[$index]}" || return 1
  done
}

render_files() {
  local work_dir mq_namespace_upper index
  work_dir="$(mktemp -d "${TMPDIR:-/tmp}/thinglinks-product-render.XXXXXX")" || fail "无法创建产品配置渲染临时目录"
  mq_namespace_upper="$(printf '%s' "$THINGLINKS_MQ_NAMESPACE" | tr '[:lower:]-' '[:upper:]_')"

  for ((index = 1; index < ${#STATE_FILES[@]}; index++)); do
    if ! cp -p "${STATE_FILES[$index]}" "$work_dir/state-$index"; then
      rm -rf "$work_dir"
      fail "无法准备产品配置渲染文件：${STATE_FILES[$index]}"
    fi
  done

  if ! THINGLINKS_ARTIFACT_ID="$THINGLINKS_MAVEN_ARTIFACT_ID" THINGLINKS_COMPONENT_NAME_VALUE="$THINGLINKS_COMPONENT_NAME" perl -0pi -e '
    $artifact = s{(</parent>\s*<artifactId>)[^<]+(</artifactId>)}{$1.$ENV{THINGLINKS_ARTIFACT_ID}.$2}ge;
    $description = s{(<description>)[^<]+(</description>)}{$1.$ENV{THINGLINKS_COMPONENT_NAME_VALUE}.$2}ge;
    END { exit(($artifact == 1 && $description == 1) ? 0 : 1); }
  ' "$work_dir/state-1"; then
    rm -rf "$work_dir"
    fail "根 POM 的组件坐标或名称无法唯一定位"
  fi

  if ! THINGLINKS_COMPONENT_VALUE="$THINGLINKS_COMPONENT_VERSION" THINGLINKS_UTIL_VALUE="$THINGLINKS_UTIL_VERSION" perl -0pi -e '
    $parent = s{(<artifactId>thinglinks-parent</artifactId>\s*<version>)[^<]+(</version>)}{$1.$ENV{THINGLINKS_UTIL_VALUE}.$2}ge;
    $revision = s{(<revision>)[^<]+(</revision>)}{$1.$ENV{THINGLINKS_COMPONENT_VALUE}.$2}ge;
    $util = s{(<thinglinks-util\.version>)[^<]+(</thinglinks-util\.version>)}{$1.$ENV{THINGLINKS_UTIL_VALUE}.$2}ge;
    END { exit(($parent == 1 && $revision == 1 && $util == 1) ? 0 : 1); }
  ' "$work_dir/state-2"; then
    rm -rf "$work_dir"
    fail "依赖父 POM 的版本字段无法唯一定位"
  fi

  if ! THINGLINKS_COMPONENT_VALUE="$THINGLINKS_COMPONENT_VERSION" perl -0pi -e '
    $revision = s{(<revision>)[^<]+(</revision>)}{$1.$ENV{THINGLINKS_COMPONENT_VALUE}.$2}ge;
    END { exit($revision == 1 ? 0 : 1); }
  ' "$work_dir/state-3"; then
    rm -rf "$work_dir"
    fail "SDK POM 的版本字段无法唯一定位"
  fi

  if ! THINGLINKS_MQ_VALUE="$THINGLINKS_MQ_NAMESPACE" THINGLINKS_MQ_UPPER_VALUE="$mq_namespace_upper" perl -0pi -e '
    $lower = s{(String THINGLINKS_MQ_NAMESPACE = ")[^"]+(")}{$1.$ENV{THINGLINKS_MQ_VALUE}.$2}ge;
    $upper = s{(String THINGLINKS_MQ_NAMESPACE_UPPER = ")[^"]+(")}{$1.$ENV{THINGLINKS_MQ_UPPER_VALUE}.$2}ge;
    END { exit(($lower == 1 && $upper == 1) ? 0 : 1); }
  ' "$work_dir/state-4"; then
    rm -rf "$work_dir"
    fail "消息队列命名空间常量无法唯一定位"
  fi

  for ((index = 1; index < ${#STATE_FILES[@]}; index++)); do
    if ! install_atomically "$work_dir/state-$index" "${STATE_FILES[$index]}"; then
      rm -rf "$work_dir"
      return 1
    fi
  done
  if ! assert_product_lock_owned; then
    rm -rf "$work_dir"
    return 1
  fi
  rm -rf "$work_dir"
  printf '已根据 %s 刷新 Maven 坐标、版本与消息队列命名空间。\n' "$MANIFEST"
}

assert_contains() {
  local file="$1"
  local expected="$2"
  grep -Fq "$expected" "$file" || fail "$file 未同步：$expected"
}

assert_root_pom_identity() {
  THINGLINKS_EXPECTED_ARTIFACT_ID="$THINGLINKS_MAVEN_ARTIFACT_ID" \
    THINGLINKS_EXPECTED_COMPONENT_NAME="$THINGLINKS_COMPONENT_NAME" perl -0ne '
    s/<!--.*?-->//sg;
    $artifact = () = m{</parent>\s*<artifactId>\s*\Q$ENV{THINGLINKS_EXPECTED_ARTIFACT_ID}\E\s*</artifactId>}g;
    $description = () = m{<description>\s*\Q$ENV{THINGLINKS_EXPECTED_COMPONENT_NAME}\E\s*</description>}g;
    END { exit(($artifact == 1 && $description == 1) ? 0 : 1); }
  ' "$ROOT_POM" || fail "$ROOT_POM 的组件坐标或名称未与产品清单一致"
}

assert_xml_tag_value() {
  local file="$1"
  local tag="$2"
  local expected="$3"
  THINGLINKS_XML_TAG="$tag" THINGLINKS_EXPECTED_VALUE="$expected" perl -0ne '
    s/<!--.*?-->//sg;
    $tag = quotemeta($ENV{THINGLINKS_XML_TAG});
    $expected = $ENV{THINGLINKS_EXPECTED_VALUE};
    $count = () = /<$tag>\s*\Q$expected\E\s*<\/$tag>/g;
    END { exit($count == 1 ? 0 : 1); }
  ' "$file" || fail "$file 的 $tag 未精确同步为 $expected"
}

assert_dependencies_parent_version() {
  THINGLINKS_EXPECTED_VERSION="$THINGLINKS_UTIL_VERSION" perl -0ne '
    s/<!--.*?-->//sg;
    $count = () = /<artifactId>\s*thinglinks-parent\s*<\/artifactId>\s*<version>\s*\Q$ENV{THINGLINKS_EXPECTED_VERSION}\E\s*<\/version>/g;
    END { exit($count == 1 ? 0 : 1); }
  ' "$DEPENDENCIES_POM" || fail "$DEPENDENCIES_POM 的 thinglinks-parent 版本未精确同步为 $THINGLINKS_UTIL_VERSION"
}

check_kafka_consumer_groups() {
  local binding relative_path suffix file expected
  for binding in "${KAFKA_CONSUMER_GROUP_BINDINGS[@]}"; do
    relative_path="${binding%%|*}"
    suffix="${binding#*|}"
    file="$PROJECT_ROOT/$relative_path"
    [ -f "$file" ] || fail "缺少 Kafka 消费者文件：$relative_path"
    assert_contains "$file" 'import com.mqttsnet.thinglinks.common.mq.ConsumerGroupConstant;'
    expected="private static final String CONSUMER_GROUP = ConsumerGroupConstant.THINGLINKS_CONSUMER_GROUP_PREFIX + \"${suffix}\";"
    assert_contains "$file" "$expected"
    if grep -qE 'private static final String CONSUMER_GROUP = "CID_[A-Z0-9_]+";' "$file"; then
      fail "$relative_path 仍在硬编码 Kafka 消费组前缀"
    fi
  done
}

check() {
  local mq_namespace_upper
  mq_namespace_upper="$(printf '%s' "$THINGLINKS_MQ_NAMESPACE" | tr '[:lower:]-' '[:upper:]_')"

  check_manifest_comments
  check_required_values
  assert_root_pom_identity
  assert_dependencies_parent_version
  assert_xml_tag_value "$DEPENDENCIES_POM" revision "$THINGLINKS_COMPONENT_VERSION"
  assert_xml_tag_value "$DEPENDENCIES_POM" thinglinks-util.version "$THINGLINKS_UTIL_VERSION"
  assert_xml_tag_value "$SDK_POM" revision "$THINGLINKS_COMPONENT_VERSION"
  assert_contains "$MQ_CONSTANT_FILE" "String THINGLINKS_MQ_NAMESPACE = \"${THINGLINKS_MQ_NAMESPACE}\";"
  assert_contains "$MQ_CONSTANT_FILE" "String THINGLINKS_MQ_NAMESPACE_UPPER = \"${mq_namespace_upper}\";"
  assert_contains "$MQ_CONSTANT_FILE" 'String THINGLINKS_CONSUMER_GROUP_PREFIX = "CID_" + THINGLINKS_MQ_NAMESPACE_UPPER + "_";'
  assert_contains "$MQ_ROUTE_FILE" 'String TOPIC_PREFIX = ConsumerGroupConstant.THINGLINKS_MQ_NAMESPACE + "-";'
  assert_contains "$MQ_ROUTE_FILE" 'String CONSUMER_GROUP_PREFIX = ConsumerGroupConstant.THINGLINKS_CONSUMER_GROUP_PREFIX;'
  check_kafka_consumer_groups
  printf '产品配置和生成引用检查通过。\n'
}

set_manifest_value_atomically() {
  local key="$1"
  local value="$2"
  local staged_file
  if ! assert_product_lock_owned; then
    return 1
  fi
  staged_file="$(mktemp "$(dirname "$MANIFEST")/.thinglinks-manifest.XXXXXX")" || fail "无法创建产品清单临时文件"
  if ! cp -p "$MANIFEST" "$staged_file"; then
    rm -f "$staged_file"
    fail "无法准备产品清单临时文件"
  fi
  if ! THINGLINKS_KEY="$key" THINGLINKS_VALUE="$value" perl -pi -e '
    if (/^\Q$ENV{THINGLINKS_KEY}\E=/) {
      $_ = "$ENV{THINGLINKS_KEY}=$ENV{THINGLINKS_VALUE}\n";
      $count++;
    }
    END { exit($count == 1 ? 0 : 1); }
  ' "$staged_file"; then
    rm -f "$staged_file"
    fail "产品配置项无法唯一定位：$key"
  fi
  if ! assert_product_lock_owned; then
    rm -f "$staged_file"
    return 1
  fi
  if ! mv -f "$staged_file" "$MANIFEST"; then
    rm -f "$staged_file"
    printf '错误：产品清单原子更新失败。\n' >&2
    return 1
  fi
  after_managed_file_replaced "$MANIFEST"
}

stop_transaction_after_lock_loss() {
  local backup_dir="$1"
  rm -rf "$backup_dir"
  fail "产品配置写锁归属丢失，事务已安全停止；已写入的受管文件保持当前状态，未执行无锁回滚"
}

rollback_transaction_or_fail() {
  local backup_dir="$1"
  local failure_message="$2"
  if ! product_lock_is_owned; then
    stop_transaction_after_lock_loss "$backup_dir"
  fi
  if ! restore_state "$backup_dir"; then
    if ! product_lock_is_owned; then
      stop_transaction_after_lock_loss "$backup_dir"
    fi
    rm -rf "$backup_dir"
    fail "${failure_message}，且自动回滚未完成，请从 Git 工作区恢复"
  fi
  rm -rf "$backup_dir"
  fail "${failure_message}，已恢复修改前状态"
}

transactional_render() {
  local backup_dir
  backup_dir="$(mktemp -d "${TMPDIR:-/tmp}/thinglinks-product-backup.XXXXXX")" || fail "无法创建产品配置备份目录"
  if ! backup_state "$backup_dir"; then
    rm -rf "$backup_dir"
    fail "无法备份产品配置受管文件"
  fi
  if ! (render_files && load_manifest && check && assert_product_lock_owned); then
    rollback_transaction_or_fail "$backup_dir" "渲染失败"
  fi
  rm -rf "$backup_dir"
}

transactional_set_version() {
  local key="$1"
  local label="$2"
  local new_version="$3"
  local backup_dir

  validate_version "$label" "$new_version"
  backup_dir="$(mktemp -d "${TMPDIR:-/tmp}/thinglinks-product-backup.XXXXXX")" || fail "无法创建产品配置备份目录"
  if ! backup_state "$backup_dir"; then
    rm -rf "$backup_dir"
    fail "无法备份产品配置受管文件"
  fi
  if ! (
    set_manifest_value_atomically "$key" "$new_version" &&
    load_manifest &&
    check_required_values &&
    render_files &&
    check &&
    assert_product_lock_owned
  ); then
    rollback_transaction_or_fail "$backup_dir" "版本更新失败"
  fi
  rm -rf "$backup_dir"
}

main() {
  local command="${1:-check}"

  case "$command" in
    check)
      [ "$#" -eq 0 ] || [ "$#" -eq 1 ] || fail "用法：$0 check"
      load_manifest
      check_required_values
      check
      ;;
    render)
      [ "$#" -eq 1 ] || fail "用法：$0 render"
      acquire_product_lock
      load_manifest
      check_required_values
      transactional_render
      ;;
    set-component-version)
      [ "$#" -eq 2 ] || fail "用法：$0 set-component-version <新版本>"
      acquire_product_lock
      load_manifest
      check_required_values
      transactional_set_version THINGLINKS_COMPONENT_VERSION Cloud "$2"
      ;;
    set-util-version)
      [ "$#" -eq 2 ] || fail "用法：$0 set-util-version <新版本>"
      acquire_product_lock
      load_manifest
      check_required_values
      transactional_set_version THINGLINKS_UTIL_VERSION Util "$2"
      ;;
    get-component-version|print-component-version)
      [ "$#" -eq 1 ] || fail "用法：$0 get-component-version"
      load_manifest
      check_required_values
      check_manifest_comments
      printf '%s\n' "$THINGLINKS_COMPONENT_VERSION"
      ;;
    *)
      fail "未知命令：${command}；可用命令：check、render、set-component-version、set-util-version、get-component-version"
      ;;
  esac
}

if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  main "$@"
fi
