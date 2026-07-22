#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MANIFEST="$PROJECT_ROOT/.thinglinks-product.env"
ROOT_POM="$PROJECT_ROOT/pom.xml"
ADMIN_POM="$PROJECT_ROOT/thinglinks-job-admin/pom.xml"
APPLICATION_YML="$PROJECT_ROOT/thinglinks-job-admin/src/main/resources/application.yml"
LOCK_DIR="$PROJECT_ROOT/.thinglinks-product.lock"
LOCK_OWNED=0
PROTECTED_PATHS=()

fail() {
  printf '错误：%s\n' "$1" >&2
  exit 1
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
    [[ "$item" =~ ^[A-Za-z0-9._/-]+$ ]] || fail "同步保护路径格式不正确：$item"
    [[ "$item" != /* && "$item" != ./* && "$item" != *..* && "$item" != *//* ]] || \
      fail "同步保护路径不安全：$item"
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
  [ -f "$PROJECT_ROOT/$THINGLINKS_LICENSE_FILE" ] || fail "授权文件不存在：$THINGLINKS_LICENSE_FILE"
  for key in THINGLINKS_PRODUCT_CODE THINGLINKS_PRODUCT_NAME THINGLINKS_PRODUCT_NAME_ZH \
    THINGLINKS_COMPONENT_CODE THINGLINKS_COMPONENT_NAME THINGLINKS_MAVEN_ARTIFACT_ID \
    THINGLINKS_MAVEN_GROUP_ID THINGLINKS_SERVICE_NAME; do
    if printf '%s\n' "${!key}" | grep -Eiq '(^|[^[:alnum:]])(pro|community|commercial|enterprise)([^[:alnum:]]|$)|旗舰版|商业版|社区版|开源版'; then
      fail "稳定身份配置项 $key 不得包含发行版本标识：${!key}"
    fi
  done
  parse_protected_paths
  protected_path_contains .thinglinks-product.env || fail "同步保护路径必须包含 .thinglinks-product.env"
  protected_path_contains LICENSE || fail "同步保护路径必须包含 LICENSE"
  protected_path_contains "$THINGLINKS_LICENSE_FILE" || fail "同步保护路径必须包含 $THINGLINKS_LICENSE_FILE"
  for key in "${PROTECTED_PATHS[@]}"; do
    [ -e "$PROJECT_ROOT/$key" ] || fail "同步保护路径不存在：$key"
  done
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

check_runtime_identifier() {
  grep -Eq "^[[:space:]]+name:[[:space:]]*${THINGLINKS_SERVICE_NAME}[[:space:]]*$" "$APPLICATION_YML" || \
    fail "Spring 应用名称与产品清单不一致：$THINGLINKS_SERVICE_NAME"
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

check_forbidden_markers() {
  local file matches='' marker_file
  marker_file="$(mktemp "${TMPDIR:-/tmp}/thinglinks-job-marker.XXXXXX")"
  while IFS= read -r -d '' file; do
    is_protected_path "$file" && continue
    case "$file" in
      scripts/product-config.sh|scripts/tests/product-config-test.sh|thinglinks-job-admin/src/main/resources/application.yml) continue ;;
    esac
    [ -f "$PROJECT_ROOT/$file" ] || continue
    if grep -IHEni 'thinglinks[-_ ]*job[-_ ]*pro([^[:alnum:]]|$)|thinglinksjobpro([^[:alnum:]]|$)|ThingLinksJobPro|旗舰版|商业版|社区版|开源版|(Pro|Professional|Enterprise|Commercial|Community|Open Source)[-_ ]+Edition' "$PROJECT_ROOT/$file" >"$marker_file" 2>/dev/null; then
      matches+="$(cat "$marker_file")"$'\n'
    fi
  done < <(git -C "$PROJECT_ROOT" ls-files -co --exclude-standard -z)
  rm -f "$marker_file"
  if [ -n "$matches" ]; then
    fail "发现不应散落维护的发行版本标识：\n${matches%$'\n'}"
  fi
}

check() {
  check_manifest_comments "$MANIFEST"
  validate_manifest
  check_poms
  check_runtime_identifier
  check_forbidden_markers
  printf '产品配置、Maven 坐标、版本引用、运行标识和发行边界检查通过。\n'
}

replace_poms() {
  local root="$1" admin="$2"
  THINGLINKS_COMPONENT_VALUE="$THINGLINKS_COMPONENT_VERSION" \
  THINGLINKS_ARTIFACT_VALUE="$THINGLINKS_MAVEN_ARTIFACT_ID" \
  THINGLINKS_UTIL_VALUE="$THINGLINKS_UTIL_VERSION" \
  THINGLINKS_JAVA_VALUE="$THINGLINKS_JAVA_VERSION" perl -0pi -e '
    my ($parent, $tail) = split m{</parent>}, $_, 2;
    die "root parent block missing\n" unless defined $tail;
    my $parent_version = ($parent =~ s{(<artifactId>thinglinks-parent</artifactId>\s*<version>)[^<]+(</version>)}{$1.$ENV{THINGLINKS_UTIL_VALUE}.$2}ge);
    my $artifact = ($tail =~ s{(<modelVersion>4\.0\.0</modelVersion>\s*<artifactId>)[^<]+(</artifactId>)}{$1.$ENV{THINGLINKS_ARTIFACT_VALUE}.$2}ge);
    my $revision = ($tail =~ s{(<revision>)[^<]+(</revision>)}{$1.$ENV{THINGLINKS_COMPONENT_VALUE}.$2}ge);
    my $util = ($tail =~ s{(<thinglinks-util\.version>)[^<]+(</thinglinks-util\.version>)}{$1.$ENV{THINGLINKS_UTIL_VALUE}.$2}ge);
    my $source = ($tail =~ s{(<maven\.compiler\.source>)[^<]+(</maven\.compiler\.source>)}{$1.$ENV{THINGLINKS_JAVA_VALUE}.$2}ge);
    my $target = ($tail =~ s{(<maven\.compiler\.target>)[^<]+(</maven\.compiler\.target>)}{$1.$ENV{THINGLINKS_JAVA_VALUE}.$2}ge);
    die "managed root POM value count invalid\n" unless $parent_version == 1 && $artifact == 1 && $revision == 1 && $util == 1 && $source == 1 && $target == 1;
    $_ = $parent . "</parent>" . $tail;
  ' "$root" || fail "根 POM 受管配置结构不正确"
  THINGLINKS_ARTIFACT_VALUE="$THINGLINKS_MAVEN_ARTIFACT_ID" perl -0pi -e '
    my ($parent, $tail) = split m{</parent>}, $_, 2;
    die "admin parent block missing\n" unless defined $tail;
    my $count = ($parent =~ s{(<artifactId>)[^<]+(</artifactId>\s*<version>\$\{revision\}</version>)}{$1.$ENV{THINGLINKS_ARTIFACT_VALUE}.$2}ge);
    die "admin parent artifact count: $count\n" unless $count == 1;
    $_ = $parent . "</parent>" . $tail;
  ' "$admin" || fail "Admin POM 父坐标结构不正确"
}

set_manifest_value() {
  local file="$1" key="$2" value="$3" count
  count="$(grep -c "^${key}=" "$file" || true)"
  [ "$count" -eq 1 ] || fail "产品配置项 $key 必须且只能出现一次"
  THINGLINKS_KEY="$key" THINGLINKS_VALUE="$value" perl -0pi -e 's/^\Q$ENV{THINGLINKS_KEY}\E=.*$/$ENV{THINGLINKS_KEY}."=".$ENV{THINGLINKS_VALUE}/me' "$file"
}

acquire_lock() {
  local owner
  if mkdir "$LOCK_DIR" 2>/dev/null; then
    printf '%s\n' "$$" > "$LOCK_DIR/pid"
    LOCK_OWNED=1
    return
  fi
  owner="$(cat "$LOCK_DIR/pid" 2>/dev/null || true)"
  if [[ "$owner" =~ ^[0-9]+$ ]] && kill -0 "$owner" 2>/dev/null; then
    fail "产品配置正在被进程 $owner 修改"
  fi
  rm -rf "$LOCK_DIR"
  mkdir "$LOCK_DIR" || fail "无法获取产品配置写锁"
  printf '%s\n' "$$" > "$LOCK_DIR/pid"
  LOCK_OWNED=1
}

release_lock() {
  [ "$LOCK_OWNED" -eq 1 ] || return 0
  rm -rf "$LOCK_DIR"
  LOCK_OWNED=0
}

prepare_stage() {
  local manifest_source="$1" stage="$2"
  mkdir -p "$stage/thinglinks-job-admin"
  cp "$manifest_source" "$stage/.thinglinks-product.env"
  cp "$ROOT_POM" "$stage/pom.xml"
  cp "$ADMIN_POM" "$stage/thinglinks-job-admin/pom.xml"
  load_manifest "$stage/.thinglinks-product.env"
  check_manifest_comments "$stage/.thinglinks-product.env"
  validate_manifest
  replace_poms "$stage/pom.xml" "$stage/thinglinks-job-admin/pom.xml"
  check_poms "$stage/pom.xml" "$stage/thinglinks-job-admin/pom.xml"
}

commit_stage() {
  local stage="$1" include_manifest="$2" backup
  backup="$(mktemp -d "${TMPDIR:-/tmp}/thinglinks-job-product-backup.XXXXXX")"
  cp "$MANIFEST" "$backup/manifest"
  cp "$ROOT_POM" "$backup/root-pom"
  cp "$ADMIN_POM" "$backup/admin-pom"
  if [ "$include_manifest" = true ]; then cp "$stage/.thinglinks-product.env" "$MANIFEST"; fi
  cp "$stage/pom.xml" "$ROOT_POM"
  cp "$stage/thinglinks-job-admin/pom.xml" "$ADMIN_POM"
  if ! (load_manifest "$MANIFEST" && check); then
    cp "$backup/manifest" "$MANIFEST"
    cp "$backup/root-pom" "$ROOT_POM"
    cp "$backup/admin-pom" "$ADMIN_POM"
    rm -rf "$backup"
    fail "产品配置写入失败，已恢复原文件"
  fi
  rm -rf "$backup"
}

render_with_manifest() {
  local manifest_source="$1" include_manifest="$2" stage
  stage="$(mktemp -d "${TMPDIR:-/tmp}/thinglinks-job-product-stage.XXXXXX")"
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
  stage_manifest="$(mktemp "${TMPDIR:-/tmp}/thinglinks-job-manifest.XXXXXX")"
  cp "$MANIFEST" "$stage_manifest"
  set_manifest_value "$stage_manifest" "$key" "$value"
  render_with_manifest "$stage_manifest" true
  rm -f "$stage_manifest"
  printf '%s已更新为 %s。\n' "$label" "$value"
}

run_locked() {
  acquire_lock
  trap release_lock EXIT HUP INT TERM
  "$@"
  release_lock
  trap - EXIT HUP INT TERM
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
      fail "未知命令：$command；可用命令：check、render、get-version、get-util-version、get-java-version、set-version、set-util-version、set-java-version"
      ;;
  esac
}

main "$@"
