#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MANIFEST="$PROJECT_ROOT/.thinglinks-product.env"
ROOT_POM="$PROJECT_ROOT/pom.xml"
PLUGIN_POMS=(
  "$PROJECT_ROOT/bifromq-auth-provider-plugin/pom.xml"
  "$PROJECT_ROOT/bifromq-event-collector-plugin/pom.xml"
  "$PROJECT_ROOT/bifromq-resource-throttler-plugin/pom.xml"
  "$PROJECT_ROOT/bifromq-setting-provider-plugin/pom.xml"
)
AUTH_POM="${PLUGIN_POMS[0]}"
EVENT_PROVIDER="$PROJECT_ROOT/bifromq-event-collector-plugin/event-provider/src/main/java/com/mqttsnet/thinglinks/BifromqEventCollectorPluginEventProvider.java"
FORBIDDEN_PATTERN='thinglinks[-_ ]+[[:alnum:]_ -]+[-_ ]+pro([^[:alnum:]]|$)|thinglinks[-_ ]+pro([^[:alnum:]]|$)|thinglinks[[:alnum:]_]*pro([^[:alnum:]]|$)|bifromq[-_ ]+plugin[-_ ]+pro([^[:alnum:]]|$)|bifromq[[:alnum:]_]*pro([^[:alnum:]]|$)|webpro([^[:alnum:]]|$)|(util|cloud|web|plugin)[-_ ]+pro([^[:alnum:]]|$)|(util|cloud|web|plugin)pro([^[:alnum:]]|$)|旗舰版|商业版|社区版|开源版|(pro|professional|enterprise|commercial|community|open[-_ ]*source)[-_ ]+edition([^[:alnum:]]|$)'
CAMEL_FORBIDDEN_PATTERN='(ThingLinks|thinglinks)[[:alnum:]_]*Pro([[:upper:]][[:alnum:]_]*|[0-9][[:alnum:]_]*)|(Bifromq|BifroMQ|bifromq)[[:alnum:]_]*Pro([[:upper:]][[:alnum:]_]*|[0-9][[:alnum:]_]*)|(Cloud|cloud|Util|util|Web|web|Plugin|plugin)[[:alnum:]_]*Pro([[:upper:]][[:alnum:]_]*|[0-9][[:alnum:]_]*)|(ThingLinks|thinglinks|Bifromq|BifroMQ|bifromq|Cloud|cloud|Util|util|Web|web|Plugin|plugin)[[:alnum:]_]*pro[[:upper:]][[:alnum:]_]*'
IDENTITY_EDITION_PATTERN='(^|[^[:alnum:]])(pro|community|commercial|enterprise)([^[:alnum:]]|$)|thinglinks[[:alnum:]_]*pro($|[^[:alnum:]])|bifromq[[:alnum:]_]*pro($|[^[:alnum:]])|(util|cloud|web|plugin)pro($|[^[:alnum:]])|(professional|open[-_ ]*source)[-_ ]+edition([^[:alnum:]]|$)|旗舰版|商业版|社区版|开源版'
CAMEL_IDENTITY_EDITION_PATTERN="$CAMEL_FORBIDDEN_PATTERN"
LOCK_REF='refs/thinglinks/product-config-lock/bifromq-plugin'
LOCK_OWNED=0
LOCK_TOKEN=''
LOCK_OID=''
PROTECTED_PATHS=()

TRANSACTION_ACTIVE=0
TRANSACTION_BACKUP_DIR=''
TRANSACTION_TARGETS=()
TRANSACTION_TEMP_FILES=()
OPERATION_TEMP_DIRS=()

fail() {
  printf '错误：%s\n' "$1" >&2
  exit 1
}

is_allowed_manifest_key() {
  case "$1" in
    THINGLINKS_PRODUCT_CODE|THINGLINKS_PRODUCT_NAME|THINGLINKS_PRODUCT_NAME_ZH|\
    THINGLINKS_COMPONENT_CODE|THINGLINKS_COMPONENT_NAME|THINGLINKS_COMPONENT_VERSION|\
    THINGLINKS_MAVEN_ARTIFACT_ID|THINGLINKS_MAVEN_GROUP_ID|THINGLINKS_BIFROMQ_VERSION|\
    THINGLINKS_UTIL_VERSION|THINGLINKS_JAVA_VERSION|THINGLINKS_JAVA_PACKAGE_PREFIX|\
    THINGLINKS_EDITION_CODE|THINGLINKS_EDITION_NAME_ZH|THINGLINKS_EDITION_NAME_EN|\
    THINGLINKS_LICENSE_MODEL|THINGLINKS_LICENSE_FILE|THINGLINKS_PRODUCT_MANIFEST_VERSION|\
    THINGLINKS_SYNC_PROTECTED_PATHS)
      return 0
      ;;
    *)
      return 1
      ;;
  esac
}

load_manifest() {
  local manifest_file="${1:-$MANIFEST}"
  local line key raw value seen=' '
  [ -f "$manifest_file" ] || fail "缺少产品配置文件：$manifest_file"

  unset THINGLINKS_PRODUCT_CODE THINGLINKS_PRODUCT_NAME THINGLINKS_PRODUCT_NAME_ZH \
    THINGLINKS_COMPONENT_CODE THINGLINKS_COMPONENT_NAME THINGLINKS_COMPONENT_VERSION \
    THINGLINKS_MAVEN_ARTIFACT_ID THINGLINKS_MAVEN_GROUP_ID THINGLINKS_BIFROMQ_VERSION \
    THINGLINKS_UTIL_VERSION THINGLINKS_JAVA_VERSION THINGLINKS_JAVA_PACKAGE_PREFIX \
    THINGLINKS_EDITION_CODE THINGLINKS_EDITION_NAME_ZH THINGLINKS_EDITION_NAME_EN \
    THINGLINKS_LICENSE_MODEL THINGLINKS_LICENSE_FILE THINGLINKS_PRODUCT_MANIFEST_VERSION \
    THINGLINKS_SYNC_PROTECTED_PATHS

  while IFS= read -r line || [ -n "$line" ]; do
    line="${line%$'\r'}"
    case "$line" in
      ''|'#'*) continue ;;
    esac
    if [[ ! "$line" =~ ^([A-Z][A-Z0-9_]*)=(.*)$ ]]; then
      fail "产品配置存在不受支持的语法：$line"
    fi
    key="${BASH_REMATCH[1]}"
    raw="${BASH_REMATCH[2]}"
    is_allowed_manifest_key "$key" || fail "产品配置包含未授权配置项：$key"
    case "$seen" in
      *" $key "*) fail "产品配置项重复：$key" ;;
    esac
    seen+="$key "

    if [[ "$raw" == \"* ]]; then
      [[ "$raw" == *\" && ${#raw} -ge 2 ]] || fail "产品配置项 $key 的双引号未闭合"
      value="${raw:1:${#raw}-2}"
      [[ "$value" != *\"* ]] || fail "产品配置项 $key 不支持嵌套双引号"
    else
      [[ "$raw" != *[[:space:]]* ]] || fail "产品配置项 $key 含空白时必须使用双引号"
      value="$raw"
    fi
    printf -v "$key" '%s' "$value"
  done < "$manifest_file"
}

check_manifest_comments() {
  local manifest_file="${1:-$MANIFEST}"
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
  ' "$manifest_file" || fail "产品配置中的每个配置项都必须紧邻一行中文注释"
}

parse_protected_paths() {
  local remaining="${THINGLINKS_SYNC_PROTECTED_PATHS}," item existing
  PROTECTED_PATHS=()
  while [ -n "$remaining" ]; do
    item="${remaining%%,*}"
    remaining="${remaining#*,}"
    [ -n "$item" ] || fail "同步保护路径包含空项"
    case "$item" in
      *'*'*|*'?'*|*'['*|*']'*) fail "同步保护路径禁止使用 glob 元字符：$item" ;;
    esac
    [ "${item#/}" = "$item" ] || fail "同步保护路径不得使用绝对路径：$item"
    [[ "$item" != ./* ]] || fail "同步保护路径必须使用规范化相对路径：$item"
    [[ "$item" =~ ^[A-Za-z0-9._/-]+$ ]] || fail "同步保护路径包含不安全字符：$item"
    case "/$item/" in
      *'/../'*|*'/./'*|*'//'*) fail "同步保护路径禁止越界或非规范化片段：$item" ;;
    esac
    [[ "$item" != */ ]] || fail "同步保护目录不得以斜杠结尾：$item"
    for existing in ${PROTECTED_PATHS[@]+"${PROTECTED_PATHS[@]}"}; do
      [ "$existing" != "$item" ] || fail "同步保护路径重复：$item"
    done
    PROTECTED_PATHS+=("$item")
  done
}

protected_path_contains() {
  local expected="$1" item
  for item in "${PROTECTED_PATHS[@]}"; do
    [ "$item" = "$expected" ] && return 0
  done
  return 1
}

validate_protected_paths() {
  local item
  parse_protected_paths
  protected_path_contains '.thinglinks-product.env' || fail "同步保护路径必须包含 .thinglinks-product.env"
  protected_path_contains 'LICENSE' || fail "同步保护路径必须包含 LICENSE"
  protected_path_contains "$THINGLINKS_LICENSE_FILE" || fail "同步保护路径必须包含 $THINGLINKS_LICENSE_FILE"
  for item in "${PROTECTED_PATHS[@]}"; do
    [ -e "$PROJECT_ROOT/$item" ] || fail "同步保护路径不存在：$item"
  done
}

reject_stable_identity_marker() {
  local key="$1" value="$2"
  if printf '%s\n' "$value" | grep -Eiq -- "$IDENTITY_EDITION_PATTERN" || \
    printf '%s\n' "$value" | grep -Eq -- "$CAMEL_IDENTITY_EDITION_PATTERN"; then
    fail "稳定身份配置项 $key 不得包含发行版本标识：$value"
  fi
}

check_required_values() {
  local keys=(
    THINGLINKS_PRODUCT_CODE THINGLINKS_PRODUCT_NAME THINGLINKS_PRODUCT_NAME_ZH
    THINGLINKS_COMPONENT_CODE THINGLINKS_COMPONENT_NAME THINGLINKS_COMPONENT_VERSION
    THINGLINKS_MAVEN_ARTIFACT_ID THINGLINKS_MAVEN_GROUP_ID THINGLINKS_BIFROMQ_VERSION
    THINGLINKS_UTIL_VERSION THINGLINKS_JAVA_VERSION THINGLINKS_JAVA_PACKAGE_PREFIX
    THINGLINKS_EDITION_CODE THINGLINKS_EDITION_NAME_ZH THINGLINKS_EDITION_NAME_EN
    THINGLINKS_LICENSE_MODEL THINGLINKS_LICENSE_FILE THINGLINKS_PRODUCT_MANIFEST_VERSION
    THINGLINKS_SYNC_PROTECTED_PATHS
  )
  local key
  for key in "${keys[@]}"; do
    [ -n "${!key:-}" ] || fail "产品配置项 $key 不能为空"
  done
  local identity_keys=(
    THINGLINKS_PRODUCT_CODE THINGLINKS_PRODUCT_NAME THINGLINKS_PRODUCT_NAME_ZH
    THINGLINKS_COMPONENT_CODE THINGLINKS_COMPONENT_NAME THINGLINKS_MAVEN_ARTIFACT_ID
    THINGLINKS_MAVEN_GROUP_ID THINGLINKS_JAVA_PACKAGE_PREFIX
  )
  for key in "${identity_keys[@]}"; do
    reject_stable_identity_marker "$key" "${!key}"
  done

  [[ "$THINGLINKS_PRODUCT_CODE" =~ ^[a-z0-9]+([.-][a-z0-9]+)*$ ]] || fail "产品编码格式不正确"
  [[ "$THINGLINKS_COMPONENT_CODE" =~ ^[a-z0-9]+([.-][a-z0-9]+)*$ ]] || fail "组件编码格式不正确"
  [ "$THINGLINKS_COMPONENT_CODE" = "$THINGLINKS_MAVEN_ARTIFACT_ID" ] || fail "组件编码必须与 Maven artifactId 一致"
  [[ "$THINGLINKS_COMPONENT_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+)?(-[0-9A-Za-z.-]+)?$ ]] || fail "插件版本号格式不正确"
  [[ "$THINGLINKS_BIFROMQ_VERSION" =~ ^3\.[0-9]+\.[0-9]+(-[0-9A-Za-z.-]+)?$ ]] || fail "当前插件仅支持 BifroMQ 3.x；升级 4.x 前必须先迁移到 org.apache.bifromq 包"
  [[ "$THINGLINKS_UTIL_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+)?(-[0-9A-Za-z.-]+)?$ ]] || fail "Util 版本号格式不正确"
  [[ "$THINGLINKS_JAVA_VERSION" =~ ^[0-9]+$ ]] && [ "$THINGLINKS_JAVA_VERSION" -ge 17 ] || fail "Java 版本必须是不小于 17 的整数"
  [[ "$THINGLINKS_MAVEN_ARTIFACT_ID" =~ ^[a-z0-9]+([.-][a-z0-9]+)*$ ]] || fail "Maven artifactId 格式不正确"
  [[ "$THINGLINKS_MAVEN_GROUP_ID" =~ ^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)+$ ]] || fail "Maven groupId 格式不正确"
  [[ "$THINGLINKS_JAVA_PACKAGE_PREFIX" =~ ^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)+$ ]] || fail "Java 包前缀格式不正确"
  [ "$THINGLINKS_JAVA_PACKAGE_PREFIX" = "$THINGLINKS_MAVEN_GROUP_ID" ] || fail "插件 Java 包前缀必须与 Maven groupId 一致"
  [[ "$THINGLINKS_EDITION_CODE" =~ ^(community|commercial|enterprise)$ ]] || fail "发行版本编码不在允许范围内"
  [[ "$THINGLINKS_LICENSE_MODEL" =~ ^(open-source|commercial|dual-license)$ ]] || fail "授权模型编码仅允许 open-source、commercial、dual-license"
  case "$THINGLINKS_EDITION_CODE:$THINGLINKS_LICENSE_MODEL" in
    community:open-source|commercial:commercial|commercial:dual-license|\
    enterprise:commercial|enterprise:dual-license) ;;
    *) fail "发行版本与授权模型不匹配：$THINGLINKS_EDITION_CODE/$THINGLINKS_LICENSE_MODEL" ;;
  esac
  [[ "$THINGLINKS_LICENSE_FILE" =~ ^[A-Za-z0-9._/-]+$ ]] || fail "授权文件路径格式不正确"
  [[ "$THINGLINKS_LICENSE_FILE" != /* && "$THINGLINKS_LICENSE_FILE" != *..* ]] || fail "授权文件必须位于仓库内"
  [ -f "$PROJECT_ROOT/$THINGLINKS_LICENSE_FILE" ] || fail "授权文件不存在：$THINGLINKS_LICENSE_FILE"
  [ "$THINGLINKS_PRODUCT_MANIFEST_VERSION" = '1' ] || fail "不支持的产品清单格式版本"
  validate_protected_paths
}

xml_path_values() {
  local file="$1" path="$2"
  XML_PATH="$path" perl -0777 -e '
    my $xml = <>;
    $xml =~ s{<!--.*?-->}{}sg;
    $xml =~ s{<\?.*?\?>}{}sg;
    my @stack;
    my @values;
    my $capture_depth = 0;
    my $value = q{};
    while ($xml =~ m{([^<]*)<([^>]+)>}sg) {
      my ($text, $tag) = ($1, $2);
      $value .= $text if $capture_depth && @stack == $capture_depth;
      next if $tag =~ /^\s*[!?]/;
      if ($tag =~ s{^\s*/\s*}{}) {
        $tag =~ s{\s+$}{};
        exit 2 unless @stack && $stack[-1] eq $tag;
        if ($capture_depth && @stack == $capture_depth) {
          $value =~ s/^\s+|\s+$//g;
          push @values, $value;
          $capture_depth = 0;
          $value = q{};
        }
        pop @stack;
        next;
      }
      my $self_closing = $tag =~ s{/\s*$}{};
      $tag =~ s/^\s+|\s+$//g;
      my ($name) = $tag =~ /^([A-Za-z_][A-Za-z0-9_.:-]*)(?:\s|$)/;
      exit 2 unless defined $name;
      push @stack, $name;
      if (join(q{/}, @stack) eq $ENV{XML_PATH}) {
        $capture_depth = scalar @stack;
        $value = q{};
      }
      if ($self_closing) {
        if ($capture_depth && @stack == $capture_depth) {
          push @values, q{};
          $capture_depth = 0;
        }
        pop @stack;
      }
    }
    exit 2 if @stack;
    print join("\n", @values);
    print "\n" if @values;
  ' "$file"
}

read_xml_path_value() {
  local file="$1" path="$2" values
  values="$(xml_path_values "$file" "$path")" || return 1
  [[ "$values" != *$'\n'* ]] || return 1
  printf '%s' "$values"
}

assert_xml_path_value() {
  local file="$1" path="$2" expected="$3" actual
  actual="$(read_xml_path_value "$file" "$path")" || \
    fail "$file 的 $path 配置重复或 XML 结构无效"
  [ "$actual" = "$expected" ] || fail "$file 的 $path 与产品清单不一致"
}

assert_project_coordinate() {
  local file="$1" expected_group="$2" expected_artifact="$3"
  assert_xml_path_value "$file" project/groupId "$expected_group"
  assert_xml_path_value "$file" project/artifactId "$expected_artifact"
  assert_xml_path_value "$file" project/version '${revision}'
}

assert_parent_coordinate() {
  local file="$1" expected_group="$2" expected_artifact="$3"
  assert_xml_path_value "$file" project/parent/groupId "$expected_group"
  assert_xml_path_value "$file" project/parent/artifactId "$expected_artifact"
  assert_xml_path_value "$file" project/parent/version '${revision}'
}

check_pom_coordinates() {
  local pom expected_parent_artifact package_path package_hits=0
  assert_project_coordinate "$ROOT_POM" "$THINGLINKS_MAVEN_GROUP_ID" "$THINGLINKS_MAVEN_ARTIFACT_ID"
  assert_xml_path_value "$ROOT_POM" project/name "$THINGLINKS_MAVEN_ARTIFACT_ID"
  for pom in "${PLUGIN_POMS[@]}"; do
    assert_project_coordinate "$pom" "$THINGLINKS_MAVEN_GROUP_ID" "$(basename "$(dirname "$pom")")"
  done

  while IFS= read -r pom; do
    case "$pom" in
      bifromq-*-plugin/*/pom.xml)
        expected_parent_artifact="${pom%%/*}"
        assert_parent_coordinate "$PROJECT_ROOT/$pom" \
          "$THINGLINKS_MAVEN_GROUP_ID" "$expected_parent_artifact"
        ;;
    esac
  done < <(git -C "$PROJECT_ROOT" ls-files -co --exclude-standard '*pom.xml')

  package_path="${THINGLINKS_JAVA_PACKAGE_PREFIX//.//}"
  [ -d "$PROJECT_ROOT/bifromq-auth-provider-plugin/auth-provider/src/main/java/$package_path" ] || fail "Java 包前缀未同步到源码目录：$THINGLINKS_JAVA_PACKAGE_PREFIX"
  package_hits="$(grep -RIl --include='*.java' "package ${THINGLINKS_JAVA_PACKAGE_PREFIX}" "$PROJECT_ROOT" 2>/dev/null | wc -l | tr -d ' ')"
  [ "$package_hits" -gt 0 ] || fail "源码中不存在产品清单声明的 Java 包前缀"
}

check_versions() {
  local root_file="${1:-$ROOT_POM}" index pom
  shift || true
  local plugin_files=()
  if [ "$#" -gt 0 ]; then
    plugin_files=("$@")
  else
    plugin_files=("${PLUGIN_POMS[@]}")
  fi
  assert_xml_path_value "$root_file" project/properties/revision "$THINGLINKS_COMPONENT_VERSION"
  assert_xml_path_value "$root_file" project/properties/java.source.version "$THINGLINKS_JAVA_VERSION"
  assert_xml_path_value "$root_file" project/properties/java.target.version "$THINGLINKS_JAVA_VERSION"
  assert_xml_path_value "$root_file" project/properties/maven.compiler.source "$THINGLINKS_JAVA_VERSION"
  assert_xml_path_value "$root_file" project/properties/maven.compiler.target "$THINGLINKS_JAVA_VERSION"
  for index in "${!plugin_files[@]}"; do
    pom="${plugin_files[$index]}"
    assert_xml_path_value "$pom" project/properties/revision "$THINGLINKS_COMPONENT_VERSION"
    assert_xml_path_value "$pom" project/properties/bifromq.version "$THINGLINKS_BIFROMQ_VERSION"
    assert_xml_path_value "$pom" project/properties/java.source.version "$THINGLINKS_JAVA_VERSION"
    assert_xml_path_value "$pom" project/properties/java.target.version "$THINGLINKS_JAVA_VERSION"
  done
  assert_xml_path_value "${plugin_files[0]}" project/properties/thinglinks-util.version "$THINGLINKS_UTIL_VERSION"
}

check_runtime_identifiers() {
  local event_provider="${1:-$EVENT_PROVIDER}" topic_count
  grep -Fq 'private static final String KAFKA_TOPIC_PREFIX = "mqtt";' "$event_provider" || \
    fail "$event_provider 未同步固定的 Kafka Topic 前缀"
  topic_count="$(perl -ne '
    next if /^\s*\/\//;
    while (/TOPIC_MAP\.put\([^,]+,\s*topic\("([^"]+)"\)\)/g) { $count++ }
    if (/TOPIC_MAP\.put\(/ && !/TOPIC_MAP\.put\([^,]+,\s*topic\("[^"]+"\)\)/) {
      print STDERR "TOPIC_MAP 必须通过 topic(suffix) 生成 Topic：$_";
      $invalid = 1;
    }
    END { print $count || 0; exit($invalid ? 1 : 0); }
  ' "$event_provider")" || fail "Kafka Topic 路由没有统一使用固定的 topic(suffix) 协议"
  [ "$topic_count" -gt 0 ] || fail "未在事件路由表中发现 Kafka Topic"
}

is_protected_path() {
  local file="${1#./}" item
  for item in "${PROTECTED_PATHS[@]}"; do
    if [ "$file" = "$item" ] || [[ "$file" == "$item/"* ]]; then
      return 0
    fi
  done
  return 1
}

is_internal_scan_file() {
  case "$1" in
    scripts/product-config.sh|scripts/tests/product-config-test.sh) return 0 ;;
    *) return 1 ;;
  esac
}

check_forbidden_markers() {
  local file path_list='' path_matches='' camel_path_matches=''
  local content_matches='' camel_content_matches='' matches=''
  local scan_files=()
  while IFS= read -r -d '' file; do
    is_protected_path "$file" && continue
    is_internal_scan_file "$file" && continue
    path_list+="$file"$'\n'
    [ -f "$PROJECT_ROOT/$file" ] || continue
    scan_files+=("$PROJECT_ROOT/$file")
  done < <(git -C "$PROJECT_ROOT" ls-files -co --exclude-standard -z)

  path_matches="$(printf '%s' "$path_list" | grep -Eni -- "$FORBIDDEN_PATTERN" || true)"
  [ -z "$path_matches" ] || matches+="文件路径包含发行版本标识：\n$path_matches"$'\n'
  camel_path_matches="$(printf '%s' "$path_list" | grep -En -- "$CAMEL_FORBIDDEN_PATTERN" || true)"
  [ -z "$camel_path_matches" ] || matches+="文件路径包含 CamelCase 发行版本标识：\n$camel_path_matches"$'\n'
  if [ "${#scan_files[@]}" -gt 0 ]; then
    content_matches="$(grep -IHEni -- "$FORBIDDEN_PATTERN" "${scan_files[@]}" || true)"
    [ -z "$content_matches" ] || matches+="$content_matches"$'\n'
    camel_content_matches="$(grep -IHEn -- "$CAMEL_FORBIDDEN_PATTERN" "${scan_files[@]}" || true)"
    [ -z "$camel_content_matches" ] || matches+="$camel_content_matches"$'\n'
  fi
  [ -z "$matches" ] || fail "发现不应散落维护的发行版本标识：\n${matches%$'\n'}"
}

check_documented_versions() {
  local file matches=''
  local markdown_files=()
  while IFS= read -r -d '' file; do
    is_protected_path "$file" && continue
    [ -f "$PROJECT_ROOT/$file" ] || continue
    markdown_files+=("$PROJECT_ROOT/$file")
  done < <(git -C "$PROJECT_ROOT" ls-files -co --exclude-standard -z '*.md')
  [ "${#markdown_files[@]}" -gt 0 ] || return 0

  matches="$(THINGLINKS_DOCUMENT_PRODUCT_NAME="$THINGLINKS_PRODUCT_NAME" \
    THINGLINKS_DOCUMENT_COMPONENT_CODE="$THINGLINKS_COMPONENT_CODE" \
    THINGLINKS_DOCUMENT_COMPONENT_NAME="$THINGLINKS_COMPONENT_NAME" \
    THINGLINKS_DOCUMENT_ARTIFACT_ID="$THINGLINKS_MAVEN_ARTIFACT_ID" perl -ne '
      use utf8;
      BEGIN { binmode STDOUT, q{:encoding(UTF-8)}; }
      utf8::decode($_);
      if (!defined($document) || $document ne $ARGV) {
        $document = $ARGV;
        $document_line = 0;
        $section_version_context = 0;
        $section_requirements = 0;
      }
      $document_line++;
      my $line_context_pattern = qr{
        当前|目前|现行|现用|正在使用|依赖版本|兼容版本|
        current(?:ly)?|in[ ]+use|requires?|requirements?|dependency[ ]+version|compatibility[ ]+version|
        現在|依存バージョン|互換バージョン|현재|의존[ ]*버전|호환[ ]*버전
      }ix;
      my $section_version_pattern = qr{
        当前[^\n]*版本|兼容(?:版本|性)|依赖版本|
        current[^\n]*version|compatibility|dependency[ ]+version|
        現在[^\n]*バージョン|互換バージョン|依存バージョン|
        현재[^\n]*버전|호환[ ]*버전|의존[ ]*버전
      }ix;
      my $requirements_pattern = qr{
        requirements?|prerequisites?|环境要求|运行要求|動作環境|要件|요구[ ]*사항
      }ix;
      my @product_names = grep { defined($_) && length($_) } @ENV{
        qw(THINGLINKS_DOCUMENT_PRODUCT_NAME THINGLINKS_DOCUMENT_COMPONENT_CODE
          THINGLINKS_DOCUMENT_COMPONENT_NAME THINGLINKS_DOCUMENT_ARTIFACT_ID)
      };
      my $product_names = join q{|}, map { quotemeta($_) } sort { length($b) <=> length($a) } @product_names;
      my $product_term = qr{BifroMQ|ThingLinks(?:[ _-]+Util)?|(?:$product_names)}i;
      if (/^\s{0,3}#{1,6}\s+(.+?)\s*$/) {
        my $heading = $1;
        $section_version_context = $heading =~ /$section_version_pattern/ ? 1 : 0;
        $section_requirements = $heading =~ /$requirements_pattern/ ? 1 : 0;
      }
      my $semver = qr{(?<![0-9A-Za-z.])(?:[vV])?[0-9]+\.[0-9]+\.[0-9]+(?:[.+-][0-9A-Za-z.-]+)?(?![0-9A-Za-z.])};
      my $java_major = qr{(?:Java|JDK)[^0-9\n]{0,40}(?<![0-9])[1-9][0-9]?(?![0-9.])}i;
      my $has_semver = /$semver/;
      my $has_java_major = /$java_major/;
      my $has_line_context = /$line_context_pattern/;
      my $is_hardcoded = $has_line_context && ($has_semver || $has_java_major);
      $is_hardcoded ||= $section_version_context && ($has_semver || $has_java_major);
      $is_hardcoded ||= $section_requirements && ($has_java_major || ($has_semver && /$product_term/));
      if ($is_hardcoded) {
        print "$ARGV:$document_line:$_";
      }
    ' "${markdown_files[@]}")"
  [ -z "$matches" ] || fail "文档包含当前版本硬编码，请引用 .thinglinks-product.env：\n${matches%$'\n'}"
}

check() {
  check_manifest_comments "$MANIFEST"
  check_required_values
  check_pom_coordinates
  check_versions
  check_runtime_identifiers
  check_forbidden_markers
  check_documented_versions
  printf '产品配置、Maven 坐标、版本引用和发行边界检查通过。\n'
}

replace_tag() {
  local file="$1" tag="$2" value="$3"
  THINGLINKS_TAG="$tag" THINGLINKS_VALUE="$value" perl -0pi -e '
    my @parts = split /(<!--.*?-->)/s, $_;
    my $quoted_tag = quotemeta($ENV{THINGLINKS_TAG});
    my $count = 0;
    for (my $index = 0; $index < @parts; $index += 2) {
      $count += ($parts[$index] =~ s{<$quoted_tag>[^<]*</$quoted_tag>}{<$ENV{THINGLINKS_TAG}>$ENV{THINGLINKS_VALUE}</$ENV{THINGLINKS_TAG}>}g);
    }
    die "$ENV{THINGLINKS_TAG} count: $count\n" unless $count == 1;
    $_ = join q{}, @parts;
  ' "$file" || fail "$file 必须且只能包含一个有效的 $tag 配置"
}

set_manifest_value() {
  local manifest_file="$1" key="$2" value="$3" count
  count="$(grep -c "^${key}=" "$manifest_file" || true)"
  [ "$count" -eq 1 ] || fail "产品配置项 $key 必须且只能出现一次"
  THINGLINKS_KEY="$key" THINGLINKS_VALUE="$value" perl -0pi -e 's/^\Q$ENV{THINGLINKS_KEY}\E=.*$/$ENV{THINGLINKS_KEY}."=".$ENV{THINGLINKS_VALUE}/me' "$manifest_file"
}

rollback_transaction() {
  local index temporary
  [ "$TRANSACTION_ACTIVE" -eq 1 ] || return 0
  for temporary in ${TRANSACTION_TEMP_FILES[@]+"${TRANSACTION_TEMP_FILES[@]}"}; do
    rm -f "$temporary"
  done
  for index in "${!TRANSACTION_TARGETS[@]}"; do
    cp "$TRANSACTION_BACKUP_DIR/$index" "${TRANSACTION_TARGETS[$index]}" || true
  done
  rm -rf "$TRANSACTION_BACKUP_DIR"
  TRANSACTION_ACTIVE=0
  TRANSACTION_BACKUP_DIR=''
  TRANSACTION_TARGETS=()
  TRANSACTION_TEMP_FILES=()
}

discard_transaction_without_restore() {
  local temporary
  [ "$TRANSACTION_ACTIVE" -eq 1 ] || return 0
  for temporary in ${TRANSACTION_TEMP_FILES[@]+"${TRANSACTION_TEMP_FILES[@]}"}; do
    rm -f "$temporary"
  done
  rm -rf "$TRANSACTION_BACKUP_DIR"
  TRANSACTION_ACTIVE=0
  TRANSACTION_BACKUP_DIR=''
  TRANSACTION_TARGETS=()
  TRANSACTION_TEMP_FILES=()
}

cleanup_temp_dirs() {
  local directory
  for directory in ${OPERATION_TEMP_DIRS[@]+"${OPERATION_TEMP_DIRS[@]}"}; do
    rm -rf "$directory"
  done
  OPERATION_TEMP_DIRS=()
}

release_lock() {
  local current_oid='' status=0
  [ "$LOCK_OWNED" -eq 1 ] || return 0
  if ! git -C "$PROJECT_ROOT" update-ref -d "$LOCK_REF" "$LOCK_OID" 2>/dev/null; then
    current_oid="$(git -C "$PROJECT_ROOT" rev-parse --verify "$LOCK_REF" 2>/dev/null || true)"
    if [ -n "$current_oid" ] && [ "$current_oid" != "$LOCK_OID" ]; then
      printf '错误：产品配置锁归属已变化，保留当前 owner 引用。\n' >&2
    else
      printf '错误：无法按 owner OID 释放产品配置锁。\n' >&2
    fi
    status=1
  fi
  LOCK_OWNED=0
  LOCK_TOKEN=''
  LOCK_OID=''
  return "$status"
}

assert_lock_owned() {
  local current_oid=''
  [ "$LOCK_OWNED" -eq 1 ] || fail "写入产品配置前必须持有仓库锁"
  current_oid="$(git -C "$PROJECT_ROOT" rev-parse --verify "$LOCK_REF" 2>/dev/null || true)"
  [ "$current_oid" = "$LOCK_OID" ] || fail "产品配置锁归属已变化，终止文件写入"
}

lock_ref_is_owned() {
  local current_oid=''
  [ "$LOCK_OWNED" -eq 1 ] || return 1
  current_oid="$(git -C "$PROJECT_ROOT" rev-parse --verify "$LOCK_REF" 2>/dev/null || true)"
  [ "$current_oid" = "$LOCK_OID" ]
}

cleanup_operation() {
  local status=$?
  trap - EXIT HUP INT TERM
  if [ "$TRANSACTION_ACTIVE" -eq 1 ]; then
    if lock_ref_is_owned; then
      rollback_transaction
    else
      printf '错误：产品配置锁归属已变化，跳过无锁回滚。\n' >&2
      discard_transaction_without_restore
      status=1
    fi
  fi
  cleanup_temp_dirs
  release_lock || status=1
  exit "$status"
}

interrupt_operation() {
  trap - HUP INT TERM
  exit 130
}

wait_for_test_lock_hook() {
  local hook="$1" attempts=0
  [ -n "$hook" ] || return 0
  printf '%s\n' "$$" > "${hook}.ready"
  while [ ! -e "${hook}.continue" ]; do
    attempts=$((attempts + 1))
    [ "$attempts" -lt 1000 ] || fail "产品配置锁测试钩子等待超时：$hook"
    sleep 0.01
  done
}

acquire_lock() {
  local owner_pid='' owner_token='' owner_created_at='' owner_record='' observed_oid='' candidate_oid='' attempt=0 created_at
  created_at="$(date +%s)"
  LOCK_TOKEN="$$-${RANDOM:-0}-${RANDOM:-0}-$created_at"
  candidate_oid="$(printf 'pid=%s\ntoken=%s\ncreatedAt=%s\n' \
    "$$" "$LOCK_TOKEN" "$created_at" | git -C "$PROJECT_ROOT" hash-object -w --stdin)" || \
    fail "无法创建产品配置锁 owner 对象"
  wait_for_test_lock_hook "${THINGLINKS_PRODUCT_TEST_PAUSE_BEFORE_LOCK_CLAIM:-}"
  while [ "$attempt" -lt 100 ]; do
    attempt=$((attempt + 1))
    if git -C "$PROJECT_ROOT" update-ref "$LOCK_REF" "$candidate_oid" '' 2>/dev/null; then
      LOCK_OID="$candidate_oid"
      LOCK_OWNED=1
      trap cleanup_operation EXIT
      trap interrupt_operation HUP INT TERM
      observed_oid="$(git -C "$PROJECT_ROOT" rev-parse --verify "$LOCK_REF" 2>/dev/null || true)"
      [ "$observed_oid" = "$LOCK_OID" ] || fail "产品配置锁归属校验失败"
      wait_for_test_lock_hook "${THINGLINKS_PRODUCT_TEST_PAUSE_AFTER_LOCK_CLAIM:-}"
      return 0
    fi
    observed_oid="$(git -C "$PROJECT_ROOT" rev-parse --verify "$LOCK_REF" 2>/dev/null || true)"
    if [ -z "$observed_oid" ]; then
      sleep 0.01
      continue
    fi
    owner_record="$(git -C "$PROJECT_ROOT" cat-file blob "$observed_oid" 2>/dev/null || true)"
    owner_pid="$(printf '%s\n' "$owner_record" | sed -n 's/^pid=\([0-9][0-9]*\)$/\1/p')"
    owner_token="$(printf '%s\n' "$owner_record" | sed -n 's/^token=\([A-Za-z0-9._-][A-Za-z0-9._-]*\)$/\1/p')"
    owner_created_at="$(printf '%s\n' "$owner_record" | sed -n 's/^createdAt=\([0-9][0-9]*\)$/\1/p')"
    if [[ ! "$owner_pid" =~ ^[0-9]+$ ]] || \
      [[ ! "$owner_token" =~ ^[A-Za-z0-9._-]+$ ]] || \
      [[ ! "$owner_created_at" =~ ^[0-9]+$ ]] || \
      [ "$owner_record" != "$(printf 'pid=%s\ntoken=%s\ncreatedAt=%s' \
        "$owner_pid" "$owner_token" "$owner_created_at")" ]; then
      fail "产品配置锁 owner 对象格式无效，已保留当前引用"
    fi
    if [[ "$owner_pid" =~ ^[0-9]+$ ]] && kill -0 "$owner_pid" 2>/dev/null; then
      fail "产品配置正在被进程 $owner_pid 修改"
    fi
    git -C "$PROJECT_ROOT" update-ref -d "$LOCK_REF" "$observed_oid" 2>/dev/null || true
  done
  fail "无法在限定时间内取得产品配置锁"
}

run_locked() {
  acquire_lock
  "$@"
  cleanup_temp_dirs
  release_lock
  trap - EXIT HUP INT TERM
}

commit_stage() {
  local stage_dir="$1" include_manifest="$2" index target relative temporary
  local replacement_count=0 fail_after="${THINGLINKS_PRODUCT_TEST_FAIL_AFTER_REPLACE:-0}"
  assert_lock_owned
  [[ "$fail_after" =~ ^[0-9]+$ ]] || fail "失败注入参数必须是非负整数"
  TRANSACTION_TARGETS=("$ROOT_POM" "${PLUGIN_POMS[@]}")
  TRANSACTION_TEMP_FILES=()
  [ "$include_manifest" = 'true' ] && TRANSACTION_TARGETS=("$MANIFEST" "${TRANSACTION_TARGETS[@]}")
  TRANSACTION_BACKUP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/bifromq-product-backup.XXXXXX")"
  OPERATION_TEMP_DIRS+=("$TRANSACTION_BACKUP_DIR")
  for index in "${!TRANSACTION_TARGETS[@]}"; do
    cp "${TRANSACTION_TARGETS[$index]}" "$TRANSACTION_BACKUP_DIR/$index"
  done
  TRANSACTION_ACTIVE=1

  for target in "${TRANSACTION_TARGETS[@]}"; do
    assert_lock_owned
    relative="${target#$PROJECT_ROOT/}"
    temporary="${target}.thinglinks-product.$$"
    TRANSACTION_TEMP_FILES+=("$temporary")
    cp "$stage_dir/$relative" "$temporary"
    mv "$temporary" "$target"
    replacement_count=$((replacement_count + 1))
    if [ "$replacement_count" -eq 1 ]; then
      wait_for_test_lock_hook "${THINGLINKS_PRODUCT_TEST_PAUSE_AFTER_FIRST_REPLACE:-}"
    fi
    assert_lock_owned
    if [ "$fail_after" -gt 0 ] && [ "$replacement_count" -eq "$fail_after" ]; then
      fail "测试注入：第 $replacement_count 个文件替换后中止"
    fi
  done

  load_manifest "$MANIFEST"
  check
  assert_lock_owned
  TRANSACTION_ACTIVE=0
  rm -rf "$TRANSACTION_BACKUP_DIR"
  TRANSACTION_BACKUP_DIR=''
  TRANSACTION_TARGETS=()
  TRANSACTION_TEMP_FILES=()
}

prepare_stage() {
  local manifest_source="$1" stage_dir="$2"
  local pom stage_pom
  local stage_plugin_poms=()
  mkdir -p "$stage_dir"
  if [ "$manifest_source" != "$stage_dir/.thinglinks-product.env" ]; then
    cp "$manifest_source" "$stage_dir/.thinglinks-product.env"
  fi
  cp "$ROOT_POM" "$stage_dir/pom.xml"
  for pom in "${PLUGIN_POMS[@]}"; do
    stage_pom="$stage_dir/${pom#$PROJECT_ROOT/}"
    mkdir -p "$(dirname "$stage_pom")"
    cp "$pom" "$stage_pom"
    stage_plugin_poms+=("$stage_pom")
  done
  load_manifest "$stage_dir/.thinglinks-product.env"
  check_manifest_comments "$stage_dir/.thinglinks-product.env"
  check_required_values
  replace_tag "$stage_dir/pom.xml" revision "$THINGLINKS_COMPONENT_VERSION"
  replace_tag "$stage_dir/pom.xml" java.source.version "$THINGLINKS_JAVA_VERSION"
  replace_tag "$stage_dir/pom.xml" java.target.version "$THINGLINKS_JAVA_VERSION"
  replace_tag "$stage_dir/pom.xml" maven.compiler.source "$THINGLINKS_JAVA_VERSION"
  replace_tag "$stage_dir/pom.xml" maven.compiler.target "$THINGLINKS_JAVA_VERSION"
  for stage_pom in "${stage_plugin_poms[@]}"; do
    replace_tag "$stage_pom" revision "$THINGLINKS_COMPONENT_VERSION"
    replace_tag "$stage_pom" bifromq.version "$THINGLINKS_BIFROMQ_VERSION"
    replace_tag "$stage_pom" java.source.version "$THINGLINKS_JAVA_VERSION"
    replace_tag "$stage_pom" java.target.version "$THINGLINKS_JAVA_VERSION"
  done
  replace_tag "${stage_plugin_poms[0]}" thinglinks-util.version "$THINGLINKS_UTIL_VERSION"
  check_versions "$stage_dir/pom.xml" "${stage_plugin_poms[@]}"
}

render_current_manifest() {
  local stage_dir
  stage_dir="$(mktemp -d "${TMPDIR:-/tmp}/bifromq-product-stage.XXXXXX")"
  OPERATION_TEMP_DIRS+=("$stage_dir")
  prepare_stage "$MANIFEST" "$stage_dir"
  check_pom_coordinates
  check_forbidden_markers
  commit_stage "$stage_dir" false
  rm -rf "$stage_dir"
  printf '已根据 %s 原子刷新插件、BifroMQ、Util 与 Java 版本。\n' "$MANIFEST"
}

set_and_render() {
  local key="$1" value="$2" label="$3" stage_dir
  stage_dir="$(mktemp -d "${TMPDIR:-/tmp}/bifromq-product-stage.XXXXXX")"
  OPERATION_TEMP_DIRS+=("$stage_dir")
  mkdir -p "$stage_dir"
  cp "$MANIFEST" "$stage_dir/.thinglinks-product.env"
  set_manifest_value "$stage_dir/.thinglinks-product.env" "$key" "$value"
  prepare_stage "$stage_dir/.thinglinks-product.env" "$stage_dir"
  check_pom_coordinates
  check_forbidden_markers
  commit_stage "$stage_dir" true
  rm -rf "$stage_dir"
  printf '%s已原子更新为 %s。\n' "$label" "$value"
}

set_version() {
  [[ "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+)?(-[0-9A-Za-z.-]+)?$ ]] || fail "插件版本号格式不正确"
  set_and_render THINGLINKS_COMPONENT_VERSION "$1" '插件版本'
}

set_bifromq_version() {
  [[ "$1" =~ ^3\.[0-9]+\.[0-9]+(-[0-9A-Za-z.-]+)?$ ]] || fail "当前插件仅支持 BifroMQ 3.x；升级 4.x 前必须先迁移到 org.apache.bifromq 包"
  set_and_render THINGLINKS_BIFROMQ_VERSION "$1" 'BifroMQ 版本'
}

set_util_version() {
  [[ "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+)?(-[0-9A-Za-z.-]+)?$ ]] || fail "Util 版本号格式不正确"
  set_and_render THINGLINKS_UTIL_VERSION "$1" 'Util 版本'
}

set_java_version() {
  [[ "$1" =~ ^[0-9]+$ ]] && [ "$1" -ge 17 ] || fail "Java 版本必须是不小于 17 的整数"
  set_and_render THINGLINKS_JAVA_VERSION "$1" 'Java 版本'
}

main() {
  local command="${1:-check}"
  case "$command" in
    check)
      [ "$#" -eq 1 ] || fail "用法：$0 check"
      load_manifest "$MANIFEST"
      check
      ;;
    render)
      [ "$#" -eq 1 ] || fail "用法：$0 render"
      run_locked render_current_manifest
      ;;
    set-version)
      [ "$#" -eq 2 ] || fail "用法：$0 set-version <新版本>"
      run_locked set_version "$2"
      ;;
    set-bifromq-version)
      [ "$#" -eq 2 ] || fail "用法：$0 set-bifromq-version <BifroMQ 新版本>"
      run_locked set_bifromq_version "$2"
      ;;
    set-util-version)
      [ "$#" -eq 2 ] || fail "用法：$0 set-util-version <Util 新版本>"
      run_locked set_util_version "$2"
      ;;
    set-java-version)
      [ "$#" -eq 2 ] || fail "用法：$0 set-java-version <Java 主版本>"
      run_locked set_java_version "$2"
      ;;
    *)
      fail "未知命令：${command}；可用命令：check、render、set-version、set-bifromq-version、set-util-version、set-java-version"
      ;;
  esac
}

main "$@"
