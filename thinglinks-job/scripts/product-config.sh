#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd -P "$(dirname "$0")/.." && pwd)"
MANIFEST="$PROJECT_ROOT/.thinglinks-product.env"
ROOT_POM="$PROJECT_ROOT/pom.xml"
ADMIN_POM="$PROJECT_ROOT/thinglinks-job-admin/pom.xml"
APPLICATION_YML="$PROJECT_ROOT/thinglinks-job-admin/src/main/resources/application.yml"
LOCK_DIR="$PROJECT_ROOT/.thinglinks-product.lock"
LOCK_OWNED=0
LOCK_INITIALIZING=0
LOCK_OWNER_TOKEN=''
LOCK_OWNER_TEMP=''
PROTECTED_PATHS=()
CLEANUP_PATHS=()
PENDING_CLEANUP_PATH=''
DEFERRED_SIGNAL_STATUS=0
TRANSACTION_ACTIVE=0
TRANSACTION_TARGETS=()
TRANSACTION_BACKUPS=()
TRANSACTION_STAGED=()
PENDING_TRANSACTION_FILE=''

fail() {
  printf '错误：%s\n' "$1" >&2
  exit 1
}

assert_relative_repo_path() {
  local path="$1" label="$2"
  [ -n "$path" ] || fail "$label 不能为空"
  case "/$path/" in
    /*/../*|/*/./*|*//* ) fail "$label 不能越出仓库或包含空路径段：$path" ;;
  esac
  case "$path" in
    /*|./*) fail "$label 必须是仓库相对路径：$path" ;;
  esac
  [[ "$path" =~ ^[A-Za-z0-9._/-]+$ ]] || fail "$label 格式不正确：$path"
}

assert_no_symlink_components() {
  local relative="$1" label="$2" remaining="$1" component current="$PROJECT_ROOT"
  while [ -n "$remaining" ]; do
    component="${remaining%%/*}"
    if [ "$remaining" = "$component" ]; then
      remaining=''
    else
      remaining="${remaining#*/}"
    fi
    current="$current/$component"
    [ ! -L "$current" ] || fail "$label 不允许使用符号链接：$relative"
  done
}

assert_safe_existing_repo_path() {
  local relative="$1" label="$2"
  assert_relative_repo_path "$relative" "$label"
  assert_no_symlink_components "$relative" "$label"
  [ -e "$PROJECT_ROOT/$relative" ] || fail "$label 不存在：$relative"
}

assert_safe_managed_file() {
  local file="$1" label="$2" relative
  case "$file" in
    "$PROJECT_ROOT"/*) relative="${file#"$PROJECT_ROOT"/}" ;;
    *) fail "$label 不在仓库内：$file" ;;
  esac
  assert_safe_existing_repo_path "$relative" "$label"
  [ -f "$file" ] || fail "$label 必须是普通文件：$relative"
}

validate_managed_paths() {
  assert_safe_managed_file "$MANIFEST" '产品配置文件'
  assert_safe_managed_file "$ROOT_POM" '根 POM'
  assert_safe_managed_file "$ADMIN_POM" 'Admin POM'
  assert_safe_managed_file "$APPLICATION_YML" 'Spring 配置文件'
}

register_cleanup_path() {
  CLEANUP_PATHS+=("$1")
}

is_allowed_manifest_key() {
  case "$1" in
    THINGLINKS_PRODUCT_CODE|THINGLINKS_PRODUCT_NAME|THINGLINKS_PRODUCT_NAME_ZH|\
    THINGLINKS_COMPONENT_CODE|THINGLINKS_COMPONENT_NAME|THINGLINKS_COMPONENT_VERSION|\
    THINGLINKS_MAVEN_ARTIFACT_ID|THINGLINKS_MAVEN_GROUP_ID|THINGLINKS_UTIL_VERSION|\
    THINGLINKS_JAVA_VERSION|THINGLINKS_EDITION_CODE|THINGLINKS_EDITION_NAME_ZH|\
    THINGLINKS_EDITION_NAME_EN|THINGLINKS_LICENSE_MODEL|THINGLINKS_LICENSE_FILE|\
    THINGLINKS_SERVICE_NAME|THINGLINKS_PRODUCT_MANIFEST_VERSION|\
    THINGLINKS_SYNC_PROTECTED_PATHS)
      return 0
      ;;
    *) return 1 ;;
  esac
}

load_manifest() {
  local file="${1:-$MANIFEST}" line key raw value seen=' '
  [ -f "$file" ] || fail "缺少产品配置文件：$file"
  unset THINGLINKS_PRODUCT_CODE THINGLINKS_PRODUCT_NAME THINGLINKS_PRODUCT_NAME_ZH \
    THINGLINKS_COMPONENT_CODE THINGLINKS_COMPONENT_NAME THINGLINKS_COMPONENT_VERSION \
    THINGLINKS_MAVEN_ARTIFACT_ID THINGLINKS_MAVEN_GROUP_ID THINGLINKS_UTIL_VERSION \
    THINGLINKS_JAVA_VERSION THINGLINKS_EDITION_CODE THINGLINKS_EDITION_NAME_ZH \
    THINGLINKS_EDITION_NAME_EN THINGLINKS_LICENSE_MODEL THINGLINKS_LICENSE_FILE \
    THINGLINKS_SERVICE_NAME THINGLINKS_PRODUCT_MANIFEST_VERSION \
    THINGLINKS_SYNC_PROTECTED_PATHS
  while IFS= read -r line || [ -n "$line" ]; do
    line="${line%$'\r'}"
    case "$line" in ''|'#'*) continue ;; esac
    [[ "$line" =~ ^([A-Z][A-Z0-9_]*)=(.*)$ ]] || fail "产品配置存在不受支持的语法：$line"
    key="${BASH_REMATCH[1]}"
    raw="${BASH_REMATCH[2]}"
    is_allowed_manifest_key "$key" || fail "产品配置包含未授权配置项：$key"
    case "$seen" in *" $key "*) fail "产品配置项重复：$key" ;; esac
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
  done < "$file"
}

check_manifest_comments() {
  perl -ne '
    utf8::decode($_);
    if (/^([A-Z][A-Z0-9_]*)=/ && (!defined($previous) || $previous !~ /^#[^#]/ || $previous !~ /\p{Han}/)) {
      print STDERR "配置项 $1 前必须紧邻一行中文注释\n";
      $invalid = 1;
    }
    $previous = $_ if /\S/;
    END { exit($invalid ? 1 : 0); }
  ' "$1" || fail "产品配置中的每个配置项都必须紧邻一行中文注释"
}

parse_protected_paths() {
  local remaining="${THINGLINKS_SYNC_PROTECTED_PATHS}," item existing
  PROTECTED_PATHS=()
  while [ -n "$remaining" ]; do
    item="${remaining%%,*}"
    remaining="${remaining#*,}"
    [ -n "$item" ] || fail "同步保护路径包含空项"
    assert_relative_repo_path "$item" '同步保护路径'
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

validate_manifest() {
  local key
  local required=(
    THINGLINKS_PRODUCT_CODE THINGLINKS_PRODUCT_NAME THINGLINKS_PRODUCT_NAME_ZH
    THINGLINKS_COMPONENT_CODE THINGLINKS_COMPONENT_NAME THINGLINKS_COMPONENT_VERSION
    THINGLINKS_MAVEN_ARTIFACT_ID THINGLINKS_MAVEN_GROUP_ID THINGLINKS_UTIL_VERSION
    THINGLINKS_JAVA_VERSION THINGLINKS_EDITION_CODE THINGLINKS_EDITION_NAME_ZH
    THINGLINKS_EDITION_NAME_EN THINGLINKS_LICENSE_MODEL THINGLINKS_LICENSE_FILE
    THINGLINKS_SERVICE_NAME THINGLINKS_PRODUCT_MANIFEST_VERSION
    THINGLINKS_SYNC_PROTECTED_PATHS
  )
  for key in "${required[@]}"; do
    [ -n "${!key:-}" ] || fail "产品配置项 $key 不能为空"
  done
  [[ "$THINGLINKS_PRODUCT_CODE" =~ ^[a-z0-9]+([.-][a-z0-9]+)*$ ]] || fail "产品编码格式不正确"
  [[ "$THINGLINKS_COMPONENT_CODE" =~ ^[a-z0-9]+([.-][a-z0-9]+)*$ ]] || fail "组件编码格式不正确"
  [ "$THINGLINKS_COMPONENT_CODE" = "$THINGLINKS_MAVEN_ARTIFACT_ID" ] || fail "组件编码必须与 Maven artifactId 一致"
  [[ "$THINGLINKS_COMPONENT_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+)?(-[0-9A-Za-z.-]+)?$ ]] || fail "Job 组件版本号格式不正确"
  [[ "$THINGLINKS_UTIL_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+)?(-[0-9A-Za-z.-]+)?$ ]] || fail "Util 版本号格式不正确"
  [[ "$THINGLINKS_JAVA_VERSION" =~ ^[0-9]+$ ]] && [ "$THINGLINKS_JAVA_VERSION" -ge 17 ] || fail "Java 版本必须是不小于 17 的整数"
  [[ "$THINGLINKS_MAVEN_GROUP_ID" =~ ^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)+$ ]] || fail "Maven groupId 格式不正确"
  [[ "$THINGLINKS_SERVICE_NAME" =~ ^[a-z0-9]+([.-][a-z0-9]+)*$ ]] || fail "服务名称格式不正确"
  [[ "$THINGLINKS_EDITION_CODE" =~ ^(community|commercial|enterprise)$ ]] || fail "发行版本编码不在允许范围内"
  [[ "$THINGLINKS_LICENSE_MODEL" =~ ^(open-source|commercial|dual-license)$ ]] || fail "授权模型编码不在允许范围内"
  case "$THINGLINKS_EDITION_CODE:$THINGLINKS_LICENSE_MODEL" in
    community:open-source|commercial:commercial|commercial:dual-license|enterprise:commercial|enterprise:dual-license) ;;
    *) fail "发行版本与授权模型不匹配：$THINGLINKS_EDITION_CODE/$THINGLINKS_LICENSE_MODEL" ;;
  esac
  [ "$THINGLINKS_PRODUCT_MANIFEST_VERSION" = 1 ] || fail "不支持的产品清单格式版本"
  assert_relative_repo_path "$THINGLINKS_LICENSE_FILE" '授权文件路径'
  for key in THINGLINKS_PRODUCT_CODE THINGLINKS_PRODUCT_NAME THINGLINKS_PRODUCT_NAME_ZH \
    THINGLINKS_COMPONENT_CODE THINGLINKS_COMPONENT_NAME THINGLINKS_MAVEN_ARTIFACT_ID \
    THINGLINKS_MAVEN_GROUP_ID THINGLINKS_SERVICE_NAME; do
    if printf '%s\n' "${!key}" | grep -Eiq 'thinglinks([[:space:]_.-]*job)?[[:space:]_.-]*(pro(fessional)?|community|commercial|enterprise)([^[:alnum:]]|$)|(^|[^[:alnum:]])(pro|community|commercial|enterprise)([^[:alnum:]]|$)|旗舰版|商业版|社区版|开源版'; then
      fail "稳定身份配置项 $key 不得包含发行版本标识：${!key}"
    fi
  done
  parse_protected_paths
  protected_path_contains .thinglinks-product.env || fail "同步保护路径必须包含 .thinglinks-product.env"
  protected_path_contains LICENSE || fail "同步保护路径必须包含 LICENSE"
  protected_path_contains "$THINGLINKS_LICENSE_FILE" || fail "同步保护路径必须包含 $THINGLINKS_LICENSE_FILE"
  for key in "${PROTECTED_PATHS[@]}"; do
    assert_safe_existing_repo_path "$key" '同步保护路径'
  done
  [ -f "$PROJECT_ROOT/$THINGLINKS_LICENSE_FILE" ] || fail "授权文件必须是普通文件：$THINGLINKS_LICENSE_FILE"
}

xml_path_values() {
  local file="$1" path="$2"
  XML_PATH="$path" perl -0777 -e '
    my $xml = <>;
    $xml =~ s{<!--.*?-->}{}sg;
    $xml =~ s{<\?.*?\?>}{}sg;
    my (@stack, @values);
    my ($capture_depth, $value) = (0, q{});
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
          ($capture_depth, $value) = (0, q{});
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
        push @values, q{} if $capture_depth && @stack == $capture_depth;
        $capture_depth = 0 if $capture_depth && @stack == $capture_depth;
        pop @stack;
      }
    }
    exit 2 if @stack;
    print join("\n", @values);
    print "\n" if @values;
  ' "$file"
}

assert_xml_value() {
  local file="$1" path="$2" expected="$3" values
  values="$(xml_path_values "$file" "$path")" || fail "$file 的 XML 结构无效"
  [[ "$values" != *$'\n'* ]] || fail "$file 的 $path 配置重复"
  [ "$values" = "$expected" ] || fail "$file 的 $path 与产品清单不一致：$values"
}

check_poms() {
  local root="${1:-$ROOT_POM}" admin="${2:-$ADMIN_POM}"
  assert_xml_value "$root" project/parent/groupId "$THINGLINKS_MAVEN_GROUP_ID"
  assert_xml_value "$root" project/parent/artifactId thinglinks-parent
  assert_xml_value "$root" project/parent/version "$THINGLINKS_UTIL_VERSION"
  assert_xml_value "$root" project/artifactId "$THINGLINKS_MAVEN_ARTIFACT_ID"
  assert_xml_value "$root" project/version '${revision}'
  assert_xml_value "$root" project/properties/revision "$THINGLINKS_COMPONENT_VERSION"
  assert_xml_value "$root" project/properties/thinglinks-util.version "$THINGLINKS_UTIL_VERSION"
  assert_xml_value "$root" project/properties/maven.compiler.source "$THINGLINKS_JAVA_VERSION"
  assert_xml_value "$root" project/properties/maven.compiler.target "$THINGLINKS_JAVA_VERSION"
  assert_xml_value "$admin" project/parent/groupId "$THINGLINKS_MAVEN_GROUP_ID"
  assert_xml_value "$admin" project/parent/artifactId "$THINGLINKS_MAVEN_ARTIFACT_ID"
  assert_xml_value "$admin" project/parent/version '${revision}'
}

check_community_metadata() {
  [ "$THINGLINKS_EDITION_CODE" = community ] || return 0
  [ "$THINGLINKS_EDITION_NAME_ZH" = '社区版' ] || \
    fail "社区发行版本的中文名称必须是：社区版"
  [ "$THINGLINKS_EDITION_NAME_EN" = Community ] || \
    fail "社区发行版本的英文名称必须是：Community"
  [ "$THINGLINKS_LICENSE_MODEL" = open-source ] || \
    fail "社区发行版本的授权模型必须是：open-source"
  [ "$THINGLINKS_LICENSE_FILE" = LICENSE ] || \
    fail "社区发行版本的授权文件必须是仓库根目录 LICENSE"
  grep -Fq 'GNU GENERAL PUBLIC LICENSE' "$PROJECT_ROOT/$THINGLINKS_LICENSE_FILE" && \
    grep -Eq 'Version[[:space:]]+3' "$PROJECT_ROOT/$THINGLINKS_LICENSE_FILE" || \
    fail "社区发行版本的 LICENSE 必须包含 GNU GPL v3 正文"
  assert_xml_value "$ROOT_POM" project/licenses/license/name 'GNU General Public License version 3'
  assert_xml_value "$ROOT_POM" project/licenses/license/url 'https://opensource.org/licenses/GPL-3.0'
}

yaml_path_values() {
  local file="$1" path="$2"
  YAML_PATH="$path" perl -e '
    use strict;
    use warnings;
    my @wanted = split m{/}, $ENV{YAML_PATH};
    my (@indents, @keys, @values);
    while (my $line = <>) {
      $line =~ s/\r?\n$//;
      next if $line =~ /^\s*(?:#.*)?$/ || $line =~ /^\s*---\s*$/;
      die "tab indentation is unsupported\n" if $line =~ /^\t/;
      next unless $line =~ /^( *)([A-Za-z_][A-Za-z0-9_.-]*):(?:[ ]*(.*?))?[ ]*$/;
      my ($indent, $key, $raw) = (length($1), $2, defined($3) ? $3 : q{});
      pop @indents, pop @keys while @indents && $indent <= $indents[-1];
      my @path = (@keys, $key);
      if (join(q{/}, @path) eq join(q{/}, @wanted)) {
        my $value = $raw;
        $value =~ s/[ ]+#.*$//;
        $value =~ s/^(["\x27])(.*)\1$/$2/;
        push @values, $value;
      }
      if ($raw eq q{} || $raw =~ /^#/) {
        push @indents, $indent;
        push @keys, $key;
      }
    }
    print join("\n", @values);
    print "\n" if @values;
  ' "$file"
}

check_runtime_identifier() {
  local file="${1:-$APPLICATION_YML}" values
  values="$(yaml_path_values "$file" spring/application/name)" || \
    fail "$file 的 YAML 结构无法解析"
  [[ "$values" != *$'\n'* ]] || fail "$file 的 spring.application.name 配置重复"
  [ "$values" = "$THINGLINKS_SERVICE_NAME" ] || \
    fail "Spring 应用名称与产品清单不一致：$values"
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

contains_forbidden_marker() {
  grep -Eiq \
    'thinglinks([[:space:]_.-]*job)?[[:space:]_.-]*(pro(fessional)?|commercial|enterprise|community)([^[:alnum:]]|$)|(^|[^[:alnum:]])(pro(fessional)?|commercial|enterprise|community|open[[:space:]_.-]*source)[[:space:]_.-]*edition([^[:alnum:]]|$)|旗舰版|商业版|社区版|开源版'
}

is_transaction_artifact() {
  local file="$PROJECT_ROOT/${1#./}" candidate
  for candidate in ${TRANSACTION_STAGED[@]+"${TRANSACTION_STAGED[@]}"} \
    ${TRANSACTION_BACKUPS[@]+"${TRANSACTION_BACKUPS[@]}"}; do
    [ -n "$candidate" ] && [ "$file" = "$candidate" ] && return 0
  done
  return 1
}

check_forbidden_markers() {
  local file matches='' marker_file file_list grep_status
  marker_file="$(mktemp "${TMPDIR:-/tmp}/thinglinks-job-marker.XXXXXX")"
  file_list="$(mktemp "${TMPDIR:-/tmp}/thinglinks-job-files.XXXXXX")"
  if ! git -C "$PROJECT_ROOT" ls-files -co --exclude-standard -z > "$file_list"; then
    rm -f "$marker_file" "$file_list"
    fail "Git 文件枚举失败，无法确认发行边界"
  fi
  while IFS= read -r -d '' file; do
    is_transaction_artifact "$file" && continue
    is_protected_path "$file" && continue
    if printf '%s\n' "$file" | contains_forbidden_marker; then
      rm -f "$marker_file" "$file_list"
      fail "文件路径包含不应散落维护的发行版本标识：$file"
    fi
    case "$file" in
      scripts/product-config.sh|scripts/tests/product-config-test.sh) continue ;;
    esac
    [ -f "$PROJECT_ROOT/$file" ] || continue
    if grep -IHEni \
      'thinglinks([[:space:]_.-]*job)?[[:space:]_.-]*(pro(fessional)?|commercial|enterprise|community)([^[:alnum:]]|$)|(^|[^[:alnum:]])(pro(fessional)?|commercial|enterprise|community|open[[:space:]_.-]*source)[[:space:]_.-]*edition([^[:alnum:]]|$)|旗舰版|商业版|社区版|开源版' \
      "$PROJECT_ROOT/$file" >"$marker_file" 2>/dev/null; then
      matches+="$(cat "$marker_file")"$'\n'
    else
      grep_status="$?"
      if [ "$grep_status" -ne 1 ]; then
        rm -f "$marker_file" "$file_list"
        fail "无法扫描文件中的发行版本标识：$file"
      fi
    fi
  done < "$file_list"
  rm -f "$marker_file" "$file_list"
  if [ -n "$matches" ]; then
    fail "发现不应散落维护的发行版本标识：\n${matches%$'\n'}"
  fi
}

check() {
  validate_managed_paths
  check_manifest_comments "$MANIFEST"
  validate_manifest
  check_poms
  check_runtime_identifier
  check_community_metadata
  check_forbidden_markers
  printf '产品配置、Maven 坐标、版本引用、运行标识和发行边界检查通过。\n'
}

replace_poms() {
  local root="$1" admin="$2"
  THINGLINKS_COMPONENT_VALUE="$THINGLINKS_COMPONENT_VERSION" \
  THINGLINKS_ARTIFACT_VALUE="$THINGLINKS_MAVEN_ARTIFACT_ID" \
  THINGLINKS_GROUP_VALUE="$THINGLINKS_MAVEN_GROUP_ID" \
  THINGLINKS_UTIL_VALUE="$THINGLINKS_UTIL_VERSION" \
  THINGLINKS_JAVA_VALUE="$THINGLINKS_JAVA_VERSION" perl -0pi -e '
    my ($parent, $tail) = split m{</parent>}, $_, 2;
    die "root parent block missing\n" unless defined $tail;
    my $parent_group = ($parent =~ s{(<parent>\s*<groupId>)[^<]+(</groupId>)}{$1.$ENV{THINGLINKS_GROUP_VALUE}.$2}ge);
    my $parent_version = ($parent =~ s{(<artifactId>thinglinks-parent</artifactId>\s*<version>)[^<]+(</version>)}{$1.$ENV{THINGLINKS_UTIL_VALUE}.$2}ge);
    my $artifact = ($tail =~ s{(<modelVersion>4\.0\.0</modelVersion>\s*<artifactId>)[^<]+(</artifactId>)}{$1.$ENV{THINGLINKS_ARTIFACT_VALUE}.$2}ge);
    my $revision = ($tail =~ s{(<revision>)[^<]+(</revision>)}{$1.$ENV{THINGLINKS_COMPONENT_VALUE}.$2}ge);
    my $util = ($tail =~ s{(<thinglinks-util\.version>)[^<]+(</thinglinks-util\.version>)}{$1.$ENV{THINGLINKS_UTIL_VALUE}.$2}ge);
    my $source = ($tail =~ s{(<maven\.compiler\.source>)[^<]+(</maven\.compiler\.source>)}{$1.$ENV{THINGLINKS_JAVA_VALUE}.$2}ge);
    my $target = ($tail =~ s{(<maven\.compiler\.target>)[^<]+(</maven\.compiler\.target>)}{$1.$ENV{THINGLINKS_JAVA_VALUE}.$2}ge);
    die "managed root POM value count invalid\n" unless $parent_group == 1 && $parent_version == 1 && $artifact == 1 && $revision == 1 && $util == 1 && $source == 1 && $target == 1;
    $_ = $parent . "</parent>" . $tail;
  ' "$root" || fail "根 POM 受管配置结构不正确"
  THINGLINKS_ARTIFACT_VALUE="$THINGLINKS_MAVEN_ARTIFACT_ID" \
  THINGLINKS_GROUP_VALUE="$THINGLINKS_MAVEN_GROUP_ID" perl -0pi -e '
    my ($parent, $tail) = split m{</parent>}, $_, 2;
    die "admin parent block missing\n" unless defined $tail;
    my $group = ($parent =~ s{(<parent>\s*<groupId>)[^<]+(</groupId>)}{$1.$ENV{THINGLINKS_GROUP_VALUE}.$2}ge);
    my $artifact = ($parent =~ s{(<artifactId>)[^<]+(</artifactId>\s*<version>\$\{revision\}</version>)}{$1.$ENV{THINGLINKS_ARTIFACT_VALUE}.$2}ge);
    die "admin parent managed value count invalid\n" unless $group == 1 && $artifact == 1;
    $_ = $parent . "</parent>" . $tail;
  ' "$admin" || fail "Admin POM 父坐标结构不正确"
}

replace_runtime_identifier() {
  local file="$1"
  THINGLINKS_SERVICE_VALUE="$THINGLINKS_SERVICE_NAME" perl -i -e '
    use strict;
    use warnings;
    my (@indents, @keys);
    my $count = 0;
    while (my $line = <>) {
      if ($line =~ /^( *)([A-Za-z_][A-Za-z0-9_.-]*):(?:[ ]*(.*?))?[ ]*(\r?\n)?$/) {
        my ($spaces, $key, $raw, $ending) = ($1, $2, defined($3) ? $3 : q{}, defined($4) ? $4 : q{});
        my $indent = length($spaces);
        pop @indents, pop @keys while @indents && $indent <= $indents[-1];
        my @path = (@keys, $key);
        if (join(q{/}, @path) eq q{spring/application/name}) {
          $line = $spaces . $key . q{: } . $ENV{THINGLINKS_SERVICE_VALUE} . ($ending || "\n");
          ++$count;
          $raw = $ENV{THINGLINKS_SERVICE_VALUE};
        }
        if ($raw eq q{} || $raw =~ /^#/) {
          push @indents, $indent;
          push @keys, $key;
        }
      }
      print $line;
    }
    die "spring.application.name count invalid: $count\n" unless $count == 1;
  ' "$file" || fail "Spring 应用名称受管配置结构不正确"
}

set_manifest_value() {
  local file="$1" key="$2" value="$3" count
  count="$(grep -c "^${key}=" "$file" || true)"
  [ "$count" -eq 1 ] || fail "产品配置项 $key 必须且只能出现一次"
  THINGLINKS_KEY="$key" THINGLINKS_VALUE="$value" perl -0pi -e 's/^\Q$ENV{THINGLINKS_KEY}\E=.*$/$ENV{THINGLINKS_KEY}."=".$ENV{THINGLINKS_VALUE}/me' "$file"
}

acquire_lock() {
  local owner owner_pid
  LOCK_OWNER_TOKEN="$$.$RANDOM.$(date +%s)"
  if ! mkdir "$LOCK_DIR" 2>/dev/null; then
    [ ! -L "$LOCK_DIR" ] || fail "产品配置写锁路径不能是符号链接"
    if [ -f "$LOCK_DIR/owner" ] && [ ! -L "$LOCK_DIR/owner" ]; then
      owner="$(cat "$LOCK_DIR/owner" 2>/dev/null || true)"
      owner_pid="${owner%% *}"
      if [[ "$owner_pid" =~ ^[0-9]+$ ]] && kill -0 "$owner_pid" 2>/dev/null; then
        fail "产品配置正在被进程 $owner_pid 修改"
      fi
      fail "发现未释放的产品配置写锁；确认没有配置命令运行后再手工删除：$LOCK_DIR"
    fi
    fail "产品配置写锁正在初始化或状态不完整：$LOCK_DIR"
  fi
  LOCK_INITIALIZING=1
  LOCK_OWNER_TEMP="$(mktemp "$LOCK_DIR/.owner.XXXXXX")" || {
    rmdir "$LOCK_DIR" 2>/dev/null || true
    fail "无法初始化产品配置写锁"
  }
  if ! printf '%s %s\n' "$$" "$LOCK_OWNER_TOKEN" > "$LOCK_OWNER_TEMP" || \
    ! mv -f "$LOCK_OWNER_TEMP" "$LOCK_DIR/owner"; then
    rm -f "$LOCK_OWNER_TEMP"
    rmdir "$LOCK_DIR" 2>/dev/null || true
    fail "无法写入产品配置锁所有者"
  fi
  LOCK_OWNER_TEMP=''
  LOCK_OWNED=1
  LOCK_INITIALIZING=0
}

release_lock() {
  local current expected status=0
  if [ "$LOCK_INITIALIZING" -eq 1 ]; then
    [ -n "$LOCK_OWNER_TEMP" ] && rm -f "$LOCK_OWNER_TEMP"
    if [ -f "$LOCK_DIR/owner" ] && [ ! -L "$LOCK_DIR/owner" ]; then
      current="$(cat "$LOCK_DIR/owner" 2>/dev/null || true)"
      expected="$$ $LOCK_OWNER_TOKEN"
      if [ "$current" = "$expected" ]; then
        rm -f "$LOCK_DIR/owner" || status=1
      else
        status=1
      fi
    fi
    rmdir "$LOCK_DIR" 2>/dev/null || status=1
    LOCK_INITIALIZING=0
    LOCK_OWNER_TEMP=''
    LOCK_OWNER_TOKEN=''
    return "$status"
  fi
  [ "$LOCK_OWNED" -eq 1 ] || return 0
  expected="$$ $LOCK_OWNER_TOKEN"
  if [ -L "$LOCK_DIR" ] || [ -L "$LOCK_DIR/owner" ] || [ ! -f "$LOCK_DIR/owner" ]; then
    printf '错误：产品配置写锁所有者文件缺失或不安全，未释放锁。\n' >&2
    return 1
  fi
  current="$(cat "$LOCK_DIR/owner" 2>/dev/null || true)"
  if [ "$current" != "$expected" ]; then
    printf '错误：产品配置写锁所有者已变化，未释放其他进程的锁。\n' >&2
    return 1
  fi
  if ! rm -f "$LOCK_DIR/owner" || ! rmdir "$LOCK_DIR"; then
    printf '错误：产品配置写锁包含未知内容，未完整释放锁。\n' >&2
    return 1
  fi
  LOCK_OWNED=0
  LOCK_OWNER_TOKEN=''
}

prepare_stage() {
  local manifest_source="$1" stage="$2"
  mkdir -p "$stage/thinglinks-job-admin/src/main/resources"
  cp "$manifest_source" "$stage/.thinglinks-product.env"
  cp "$ROOT_POM" "$stage/pom.xml"
  cp "$ADMIN_POM" "$stage/thinglinks-job-admin/pom.xml"
  cp "$APPLICATION_YML" "$stage/thinglinks-job-admin/src/main/resources/application.yml"
  load_manifest "$stage/.thinglinks-product.env"
  check_manifest_comments "$stage/.thinglinks-product.env"
  validate_manifest
  replace_poms "$stage/pom.xml" "$stage/thinglinks-job-admin/pom.xml"
  replace_runtime_identifier "$stage/thinglinks-job-admin/src/main/resources/application.yml"
  check_poms "$stage/pom.xml" "$stage/thinglinks-job-admin/pom.xml"
  check_runtime_identifier "$stage/thinglinks-job-admin/src/main/resources/application.yml"
}

cleanup_transaction_artifacts() {
  local file status=0
  if [ -n "$PENDING_TRANSACTION_FILE" ] && \
    { [ -e "$PENDING_TRANSACTION_FILE" ] || [ -L "$PENDING_TRANSACTION_FILE" ]; }; then
    rm -f "$PENDING_TRANSACTION_FILE" || status=1
  fi
  PENDING_TRANSACTION_FILE=''
  for file in ${TRANSACTION_STAGED[@]+"${TRANSACTION_STAGED[@]}"}; do
    [ -n "$file" ] || continue
    if [ -e "$file" ] || [ -L "$file" ]; then
      rm -f "$file" || status=1
    fi
  done
  for file in ${TRANSACTION_BACKUPS[@]+"${TRANSACTION_BACKUPS[@]}"}; do
    [ -n "$file" ] || continue
    if [ -e "$file" ] || [ -L "$file" ]; then
      rm -f "$file" || status=1
    fi
  done
  TRANSACTION_STAGED=()
  TRANSACTION_BACKUPS=()
  TRANSACTION_TARGETS=()
  return "$status"
}

rollback_transaction() {
  local index backup target status=0
  if [ "$TRANSACTION_ACTIVE" -eq 1 ]; then
    for ((index=0; index<${#TRANSACTION_TARGETS[@]}; index++)); do
      backup="${TRANSACTION_BACKUPS[$index]}"
      target="${TRANSACTION_TARGETS[$index]}"
      if [ -f "$backup" ] && [ ! -L "$backup" ]; then
        mv -f "$backup" "$target" || status=1
        TRANSACTION_BACKUPS[$index]=''
      else
        printf '错误：无法恢复受管文件，备份缺失：%s\n' "$target" >&2
        status=1
      fi
    done
  fi
  TRANSACTION_ACTIVE=0
  cleanup_transaction_artifacts || status=1
  return "$status"
}

finish_transaction() {
  TRANSACTION_ACTIVE=0
  cleanup_transaction_artifacts
}

commit_stage() {
  local stage="$1" include_manifest="$2" index source target directory basename backup staged
  local sources=() targets=()
  if [ "$include_manifest" = true ]; then
    sources+=("$stage/.thinglinks-product.env")
    targets+=("$MANIFEST")
  fi
  sources+=(
    "$stage/pom.xml"
    "$stage/thinglinks-job-admin/pom.xml"
    "$stage/thinglinks-job-admin/src/main/resources/application.yml"
  )
  targets+=("$ROOT_POM" "$ADMIN_POM" "$APPLICATION_YML")

  TRANSACTION_TARGETS=()
  TRANSACTION_BACKUPS=()
  TRANSACTION_STAGED=()
  for ((index=0; index<${#targets[@]}; index++)); do
    source="${sources[$index]}"
    target="${targets[$index]}"
    assert_safe_managed_file "$target" '受管目标文件'
    directory="${target%/*}"
    basename="${target##*/}"
    PENDING_TRANSACTION_FILE="$(mktemp "$directory/.${basename}.product-backup.XXXXXX")" || \
      fail "无法创建受管文件同目录备份：$target"
    backup="$PENDING_TRANSACTION_FILE"
    TRANSACTION_TARGETS+=("$target")
    TRANSACTION_BACKUPS+=("$backup")
    TRANSACTION_STAGED+=('')
    PENDING_TRANSACTION_FILE=''
    PENDING_TRANSACTION_FILE="$(mktemp "$directory/.${basename}.product-stage.XXXXXX")" || \
      fail "无法创建受管文件同目录暂存：$target"
    staged="$PENDING_TRANSACTION_FILE"
    TRANSACTION_STAGED[$index]="$staged"
    PENDING_TRANSACTION_FILE=''
    cp -p "$target" "$backup" || fail "无法备份受管文件：$target"
    cp -p "$source" "$staged" || fail "无法准备受管文件：$target"
  done

  TRANSACTION_ACTIVE=1
  for ((index=0; index<${#TRANSACTION_TARGETS[@]}; index++)); do
    mv -f "${TRANSACTION_STAGED[$index]}" "${TRANSACTION_TARGETS[$index]}" || \
      fail "无法原子替换受管文件：${TRANSACTION_TARGETS[$index]}"
    TRANSACTION_STAGED[$index]=''
  done
  if ! (load_manifest "$MANIFEST" && check); then
    rollback_transaction || fail "产品配置写入失败，且未能完整恢复原文件"
    fail "产品配置写入失败，已恢复原文件"
  fi
  finish_transaction || fail "产品配置写入完成，但临时备份清理失败"
}

render_with_manifest() {
  local manifest_source="$1" include_manifest="$2" stage
  validate_managed_paths
  PENDING_CLEANUP_PATH="$(mktemp -d "${TMPDIR:-/tmp}/thinglinks-job-product-stage.XXXXXX")"
  stage="$PENDING_CLEANUP_PATH"
  register_cleanup_path "$stage"
  PENDING_CLEANUP_PATH=''
  prepare_stage "$manifest_source" "$stage"
  commit_stage "$stage" "$include_manifest"
  rm -rf "$stage"
}

render_current() {
  render_with_manifest "$MANIFEST" false
  printf '已根据 .thinglinks-product.env 刷新 Job Maven 配置。\n'
}

set_version_field() {
  local key="$1" label="$2" value="$3" stage_manifest
  case "$key" in
    THINGLINKS_COMPONENT_VERSION|THINGLINKS_UTIL_VERSION)
      [[ "$value" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+)?(-[0-9A-Za-z.-]+)?$ ]] || fail "$label 格式不正确：$value"
      ;;
    THINGLINKS_JAVA_VERSION)
      [[ "$value" =~ ^[0-9]+$ ]] && [ "$value" -ge 17 ] || fail "$label 必须是不小于 17 的整数"
      ;;
  esac
  PENDING_CLEANUP_PATH="$(mktemp "${TMPDIR:-/tmp}/thinglinks-job-manifest.XXXXXX")"
  stage_manifest="$PENDING_CLEANUP_PATH"
  register_cleanup_path "$stage_manifest"
  PENDING_CLEANUP_PATH=''
  cp "$MANIFEST" "$stage_manifest"
  set_manifest_value "$stage_manifest" "$key" "$value"
  render_with_manifest "$stage_manifest" true
  rm -f "$stage_manifest"
  printf '%s已更新为 %s。\n' "$label" "$value"
}

cleanup_registered_paths() {
  local path status=0
  if [ -n "$PENDING_CLEANUP_PATH" ] && \
    { [ -e "$PENDING_CLEANUP_PATH" ] || [ -L "$PENDING_CLEANUP_PATH" ]; }; then
    rm -rf "$PENDING_CLEANUP_PATH" || status=1
  fi
  PENDING_CLEANUP_PATH=''
  for path in ${CLEANUP_PATHS[@]+"${CLEANUP_PATHS[@]}"}; do
    [ -n "$path" ] || continue
    if [ -e "$path" ] || [ -L "$path" ]; then
      rm -rf "$path" || status=1
    fi
  done
  CLEANUP_PATHS=()
  return "$status"
}

cleanup_runtime_state() {
  local status=0
  rollback_transaction || status=1
  cleanup_registered_paths || status=1
  release_lock || status=1
  return "$status"
}

run_exit_cleanup() {
  local command_status="$?" cleanup_status=0
  trap - EXIT HUP INT TERM
  cleanup_runtime_state || cleanup_status=1
  if [ "$command_status" -eq 0 ] && [ "$cleanup_status" -ne 0 ]; then
    command_status=1
  fi
  exit "$command_status"
}

run_locked() {
  local cleanup_status=0
  DEFERRED_SIGNAL_STATUS=0
  trap 'DEFERRED_SIGNAL_STATUS=129' HUP
  trap 'DEFERRED_SIGNAL_STATUS=130' INT
  trap 'DEFERRED_SIGNAL_STATUS=143' TERM
  trap 'run_exit_cleanup' EXIT
  acquire_lock
  trap 'exit 129' HUP
  trap 'exit 130' INT
  trap 'exit 143' TERM
  [ "$DEFERRED_SIGNAL_STATUS" -eq 0 ] || exit "$DEFERRED_SIGNAL_STATUS"
  "$@"
  if ! cleanup_runtime_state; then
    cleanup_status=1
  fi
  trap - EXIT HUP INT TERM
  return "$cleanup_status"
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
      run_locked render_current
      ;;
    get-version)
      load_manifest "$MANIFEST"; printf '%s\n' "$THINGLINKS_COMPONENT_VERSION"
      ;;
    get-util-version)
      load_manifest "$MANIFEST"; printf '%s\n' "$THINGLINKS_UTIL_VERSION"
      ;;
    get-java-version)
      load_manifest "$MANIFEST"; printf '%s\n' "$THINGLINKS_JAVA_VERSION"
      ;;
    set-version)
      [ "$#" -eq 2 ] || fail "用法：$0 set-version <Job 新版本>"
      run_locked set_version_field THINGLINKS_COMPONENT_VERSION 'Job 组件版本' "$2"
      ;;
    set-util-version)
      [ "$#" -eq 2 ] || fail "用法：$0 set-util-version <Util 新版本>"
      run_locked set_version_field THINGLINKS_UTIL_VERSION 'Util 依赖版本' "$2"
      ;;
    set-java-version)
      [ "$#" -eq 2 ] || fail "用法：$0 set-java-version <Java 主版本>"
      run_locked set_version_field THINGLINKS_JAVA_VERSION 'Java 编译版本' "$2"
      ;;
    *)
      fail "未知命令：${command}；可用命令：check、render、get-version、get-util-version、get-java-version、set-version、set-util-version、set-java-version"
      ;;
  esac
}

main "$@"
