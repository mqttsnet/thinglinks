package com.mqttsnet.thinglinks.productversion.diff;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.productversionchangelog.vo.DiffIgnore;
import com.mqttsnet.thinglinks.productversionchangelog.vo.FieldChange;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ReflectionDiffBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;

/**
 * 单对象字段级差异器,给变更记录用 —— 直接比对事件携带的 before / after(ResultVO),不反推快照:
 * 编辑取真正变化的字段,新增列 after 非空业务字段,删除列 before 非空业务字段。
 * 两层互补排除给 {@link ReflectionDiffBuilder#setExcludeFieldNames}:FRAMEWORK_EXCLUDE 兜底外部 jar /
 * 公共 AuditableResultVO 里拿不到注解的非业务字段,{@link DiffIgnore} 标注本仓 ResultVO 的结构外键 / 回显容器字段;
 * 排除集按类 cache 避免高频重复反射扫描。
 *
 * @author mqttsnet
 */
@Component
public class EntityFieldDiffer {

    /**
     * 跨模块公共字段兜底排除集 ── 这些字段在父类(外部 jar 或公共 AuditableResultVO)里,
     * 而 {@link DiffIgnore} 注解放在 link-entity 模块,上层公共模块不依赖它无法直接打注解。
     * 审计字段(createdBy / updatedBy / createdOrgId)有意不排除 ── "谁改的"有价值,噪音可接受。
     * 本仓 link-entity 内的 ResultVO 字段请用 {@link DiffIgnore} 注解,不要加到这里。
     */
    private static final Set<String> FRAMEWORK_EXCLUDE = Set.of(
        "id",          // SuperEntity.id
        "createdTime", // SuperEntity.createdTime
        "updatedTime", // Entity.updatedTime(自动更新,不排除会让 isNoopUpdate 检测永久失效)
        "echoMap"      // AuditableResultVO.echoMap(回显容器,非业务字段)
    );

    /**
     * 按 ResultVO 类型 cache 排除字段名数组 ── differ 高频调用(每次物模型 CRUD),加 cache 后单类型只扫一次。
     */
    private static final Map<Class<?>, String[]> EXCLUDE_CACHE = new ConcurrentHashMap<>();

    /**
     * 计算 before → after 的字段级变更(无变更返回空列表)。before 新增时为 null,after 删除时为 null。
     *
     * @param before 变更前对象(新增时为 null)
     * @param after 变更后对象(删除时为 null)
     * @return 字段级变更列表;无变更返回空列表
     */
    public List<FieldChange> diff(Object before, Object after) {
        if (before != null && after != null) {
            return editDiff(before, after);
        }
        return Optional.ofNullable(after)
            .map(a -> describe(a, true))
            .or(() -> Optional.ofNullable(before).map(b -> describe(b, false)))
            .orElseGet(List::of);
    }

    /**
     * 编辑:commons-lang3 builder + 排除字段一次性过滤,仅保留真正变化的字段。排除字段在 diff 阶段就跳过,
     * 不会进入 {@link DiffResult},无需后续手动 filter。
     *
     * @param before 变更前对象
     * @param after 变更后对象
     * @return 真正变化的字段变更列表
     */
    private List<FieldChange> editDiff(Object before, Object after) {
        Class<?> type = before.getClass();
        String[] excludeNames = resolveExcludeFieldNames(type);

        ReflectionDiffBuilder.Builder<Object> rdbBuilder = ReflectionDiffBuilder.builder()
            .setDiffBuilder(DiffBuilder.builder()
                .setLeft(before)
                .setRight(after)
                .setStyle(ToStringStyle.SHORT_PREFIX_STYLE)
                .build())
            .setExcludeFieldNames(excludeNames);
        DiffResult<Object> result = rdbBuilder.build().build();

        List<FieldChange> fields = new ArrayList<>(result.getNumberOfDiffs());
        for (Diff<?> d : result.getDiffs()) {
            fields.add(FieldChange.builder()
                .field(d.getFieldName())
                .label(label(type, d.getFieldName()))
                .before(d.getLeft())
                .after(d.getRight())
                .build());
        }
        return fields;
    }

    /**
     * 新增 / 删除:遍历对象所有字段(含父类,via hutool {@link ReflectUtil#getFields}),排除框架字段 /
     * {@link DiffIgnore} / synthetic / static / transient,列非空业务字段。isAdd=true 值落 after,否则落 before。
     *
     * @param target 待描述对象
     * @param isAdd true 为新增(值落 after),false 为删除(值落 before)
     * @return 非空业务字段的变更列表
     */
    private List<FieldChange> describe(Object target, boolean isAdd) {
        Class<?> type = target.getClass();
        Set<String> exclude = Set.of(resolveExcludeFieldNames(type));
        List<FieldChange> fields = new ArrayList<>();
        for (Field f : ReflectUtil.getFields(type)) {
            String name = f.getName();
            int mods = f.getModifiers();
            if (exclude.contains(name)
                || f.isSynthetic()
                || Modifier.isStatic(mods)
                || Modifier.isTransient(mods)) {
                continue;
            }
            Object value = ReflectUtil.getFieldValue(target, f);
            if (ObjectUtil.isEmpty(value)) {
                continue;
            }
            fields.add(FieldChange.builder()
                .field(name)
                .label(label(type, name))
                .before(isAdd ? null : value)
                .after(isAdd ? value : null)
                .build());
        }
        return fields;
    }

    /**
     * 解析某 ResultVO 类型的完整排除字段名集合 ── FRAMEWORK_EXCLUDE ∪ {@link DiffIgnore} 标注字段(按类型 cache)。
     *
     * @param type ResultVO 类型
     * @return 完整排除字段名数组
     */
    private String[] resolveExcludeFieldNames(Class<?> type) {
        return EXCLUDE_CACHE.computeIfAbsent(type, this::scanExcludeFieldNames);
    }

    private String[] scanExcludeFieldNames(Class<?> type) {
        // LinkedHashSet 保证 FRAMEWORK_EXCLUDE 优先 + 注解字段去重
        Set<String> names = new LinkedHashSet<>(FRAMEWORK_EXCLUDE);
        for (Field f : ReflectUtil.getFields(type)) {
            if (f.isAnnotationPresent(DiffIgnore.class)) {
                names.add(f.getName());
            }
        }
        return names.toArray(new String[0]);
    }

    /**
     * 取字段 @Schema(description) 作中文标签,缺失则回退字段名。用 hutool {@link ReflectUtil#getField}
     * 支持父类字段查找(getDeclaredField 仅查当前类会漏掉父类的 @Schema)。
     *
     * @param type 目标类型
     * @param fieldName 字段名
     * @return 字段 @Schema 描述;缺失则返回字段名
     */
    private String label(Class<?> type, String fieldName) {
        Field f = ReflectUtil.getField(type, fieldName);
        if (f != null) {
            Schema schema = f.getAnnotation(Schema.class);
            if (schema != null && StrUtil.isNotBlank(schema.description())) {
                return schema.description();
            }
        }
        return fieldName;
    }

    /**
     * 测试钩子 ── 清掉某类的 cache,避免单元测试间相互影响(主流程不调用)。
     *
     * @param type 要清除 cache 的类型;为 null 时清空全部
     */
    static void clearCacheForTest(Class<?> type) {
        if (type == null) {
            EXCLUDE_CACHE.clear();
        } else {
            EXCLUDE_CACHE.remove(type);
        }
    }

    /**
     * 当前 cache 项数(单元测试断言用)。
     *
     * @return 当前 cache 项数
     */
    static int cacheSize() {
        return EXCLUDE_CACHE.size();
    }

    /**
     * 取某类型的 cache 拷贝(单元测试断言用,返回不可变快照)。
     *
     * @param type 目标类型
     * @return 该类型已 cache 的排除字段名集合;未 cache 返回空集合
     */
    static Set<String> cachedExcludeNames(Class<?> type) {
        String[] arr = EXCLUDE_CACHE.get(type);
        return arr == null ? Set.of() : new HashSet<>(Arrays.asList(arr));
    }
}
