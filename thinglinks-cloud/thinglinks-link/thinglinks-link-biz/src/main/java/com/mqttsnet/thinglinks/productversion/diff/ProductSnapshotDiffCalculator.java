package com.mqttsnet.thinglinks.productversion.diff;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.productversion.vo.diff.ProductVersionDiffNode;
import com.mqttsnet.thinglinks.productversion.vo.diff.ProductVersionDiffSummaryVO;
import com.mqttsnet.thinglinks.productversion.vo.diff.ProductVersionDiffVO;
import com.mqttsnet.thinglinks.productversion.vo.diff.ProductVersionFieldDiffVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotCommandParameterVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotCommandVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotPropertyVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotServiceVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotVO;
import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductVersionChangeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ReflectionDiffBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;

/**
 * 产品快照差异计算器。给定 source / target 两份 {@link ProductSnapshotVO},产出与物模型层级一致的差异树
 * {@link ProductVersionDiffNode}:产品 → 服务 → 属性 / 命令 → 命令参数;空快照视为「全部新增」支持首次发布。
 * 结构遍历(按 code 配对各层级集合)在本类内完成;字段级值比对统一委托 {@link ReflectionDiffBuilder} 反射,
 * 快照 VO 新增字段会被自动纳入无需改动本类。
 *
 * @author mqttsnet
 */
@Component
public class ProductSnapshotDiffCalculator {

    /**
     * 层级标识。
     */
    private static final String LEVEL_PRODUCT = "PRODUCT";
    private static final String LEVEL_SERVICE = "SERVICE";
    private static final String LEVEL_PROPERTY = "PROPERTY";
    private static final String LEVEL_COMMAND = "COMMAND";
    private static final String LEVEL_COMMAND_PARAM = "COMMAND_PARAM";

    /**
     * 命令参数方向。
     */
    private static final String PARAM_REQUEST = "REQUEST";
    private static final String PARAM_RESPONSE = "RESPONSE";

    /**
     * 变更类型。
     */
    private static final String TYPE_ADDED = "ADDED";
    private static final String TYPE_REMOVED = "REMOVED";
    private static final String TYPE_MODIFIED = "MODIFIED";

    /**
     * 各层级反射比对时排除的「子集合 / 快照元信息」字段(子集合递归另行处理)。
     */
    private static final Set<String> PRODUCT_EXCLUDE = Set.of(
        "services", "versionNo", "activeVersionNo",
        "publishTime", "publishStrategy", "productIdentification");
    private static final Set<String> SERVICE_EXCLUDE = Set.of("properties", "commands");
    private static final Set<String> COMMAND_EXCLUDE = Set.of("requests", "responses");
    private static final Set<String> NO_EXCLUDE = Collections.emptySet();

    /**
     * 计算两份快照差异。source 可为 null(首次发布,视为全部新增),target 不可为 null。
     *
     * @param source 源快照,null 表示首次发布
     * @param target 目标快照,不可为 null
     * @return 差异结果
     */
    public ProductVersionDiffVO diff(ProductSnapshotVO source, ProductSnapshotVO target) {
        Objects.requireNonNull(target, "target snapshot must not be null");

        List<ProductVersionDiffNode> nodes = new ArrayList<>();
        Optional.ofNullable(buildProductNode(source, target)).ifPresent(nodes::add);
        nodes.addAll(diffServices(services(source), services(target)));

        ProductVersionDiffSummaryVO summary = summarize(nodes);
        return ProductVersionDiffVO.builder()
            .sourceVersion(Optional.ofNullable(source).map(ProductSnapshotVO::getVersionNo).orElse(null))
            .targetVersion(target.getVersionNo())
            .summary(summary)
            .nodes(nodes)
            .changeSummaryText(buildSummaryText(summary))
            .changeType(resolveChangeType(summary))
            .build();
    }

    // ────────────── 各层级节点构建 ──────────────

    /**
     * 产品基础信息节点(无子级);无字段变更时返回 null。
     *
     * @param source 源快照
     * @param target 目标快照
     * @return 产品节点;无字段变更返 null
     */
    private ProductVersionDiffNode buildProductNode(ProductSnapshotVO source, ProductSnapshotVO target) {
        List<ProductVersionFieldDiffVO> fields =
            compareFields(source, target, ProductSnapshotVO.class, PRODUCT_EXCLUDE);
        if (fields.isEmpty()) {
            return null;
        }
        return ProductVersionDiffNode.builder()
            .level(LEVEL_PRODUCT)
            .changeType(source == null ? TYPE_ADDED : TYPE_MODIFIED)
            .name(pick(source, target, ProductSnapshotVO::getProductName))
            .fields(fields)
            .children(Collections.emptyList())
            .build();
    }

    private List<ProductVersionDiffNode> diffServices(List<ProductSnapshotServiceVO> source,
                                                      List<ProductSnapshotServiceVO> target) {
        return diffKeyedList(source, target, ProductSnapshotServiceVO::getServiceCode, this::buildServiceNode);
    }

    /**
     * 服务节点:自身字段 + 属性 / 命令子级。src 或 tgt 为 null 即新增 / 删除。
     *
     * @param src 源服务
     * @param tgt 目标服务
     * @return 服务节点
     */
    private ProductVersionDiffNode buildServiceNode(ProductSnapshotServiceVO src, ProductSnapshotServiceVO tgt) {
        List<ProductVersionFieldDiffVO> fields =
            compareFields(src, tgt, ProductSnapshotServiceVO.class, SERVICE_EXCLUDE);
        List<ProductVersionDiffNode> children = new ArrayList<>();
        children.addAll(diffProperties(properties(src), properties(tgt)));
        children.addAll(diffCommands(commands(src), commands(tgt)));
        return assemble(LEVEL_SERVICE, null,
            pick(src, tgt, ProductSnapshotServiceVO::getServiceCode),
            pick(src, tgt, ProductSnapshotServiceVO::getServiceName),
            changeType(src, tgt), fields, children);
    }

    private List<ProductVersionDiffNode> diffProperties(List<ProductSnapshotPropertyVO> source,
                                                        List<ProductSnapshotPropertyVO> target) {
        return diffKeyedList(source, target, ProductSnapshotPropertyVO::getPropertyCode, this::buildPropertyNode);
    }

    /**
     * 属性节点(叶子)。
     *
     * @param src 源属性
     * @param tgt 目标属性
     * @return 属性节点
     */
    private ProductVersionDiffNode buildPropertyNode(ProductSnapshotPropertyVO src, ProductSnapshotPropertyVO tgt) {
        List<ProductVersionFieldDiffVO> fields =
            compareFields(src, tgt, ProductSnapshotPropertyVO.class, NO_EXCLUDE);
        return assemble(LEVEL_PROPERTY, null,
            pick(src, tgt, ProductSnapshotPropertyVO::getPropertyCode),
            pick(src, tgt, ProductSnapshotPropertyVO::getPropertyName),
            changeType(src, tgt), fields, Collections.emptyList());
    }

    private List<ProductVersionDiffNode> diffCommands(List<ProductSnapshotCommandVO> source,
                                                      List<ProductSnapshotCommandVO> target) {
        return diffKeyedList(source, target, ProductSnapshotCommandVO::getCommandCode, this::buildCommandNode);
    }

    /**
     * 命令节点:自身字段 + 请求 / 响应参数子级。
     *
     * @param src 源命令
     * @param tgt 目标命令
     * @return 命令节点
     */
    private ProductVersionDiffNode buildCommandNode(ProductSnapshotCommandVO src, ProductSnapshotCommandVO tgt) {
        List<ProductVersionFieldDiffVO> fields =
            compareFields(src, tgt, ProductSnapshotCommandVO.class, COMMAND_EXCLUDE);
        List<ProductVersionDiffNode> children = new ArrayList<>();
        children.addAll(diffParams(requests(src), requests(tgt), PARAM_REQUEST));
        children.addAll(diffParams(responses(src), responses(tgt), PARAM_RESPONSE));
        return assemble(LEVEL_COMMAND, null,
            pick(src, tgt, ProductSnapshotCommandVO::getCommandCode),
            pick(src, tgt, ProductSnapshotCommandVO::getCommandName),
            changeType(src, tgt), fields, children);
    }

    private List<ProductVersionDiffNode> diffParams(List<ProductSnapshotCommandParameterVO> source,
                                                    List<ProductSnapshotCommandParameterVO> target,
                                                    String paramKind) {
        return diffKeyedList(source, target, ProductSnapshotCommandParameterVO::getParameterCode,
            (src, tgt) -> buildParamNode(src, tgt, paramKind));
    }

    /**
     * 命令参数节点(叶子),携带请求 / 响应方向。
     *
     * @param src       源参数
     * @param tgt       目标参数
     * @param paramKind 参数方向(请求 / 响应)
     * @return 命令参数节点
     */
    private ProductVersionDiffNode buildParamNode(ProductSnapshotCommandParameterVO src,
                                                  ProductSnapshotCommandParameterVO tgt, String paramKind) {
        List<ProductVersionFieldDiffVO> fields =
            compareFields(src, tgt, ProductSnapshotCommandParameterVO.class, NO_EXCLUDE);
        return assemble(LEVEL_COMMAND_PARAM, paramKind,
            pick(src, tgt, ProductSnapshotCommandParameterVO::getParameterCode),
            pick(src, tgt, ProductSnapshotCommandParameterVO::getParameterName),
            changeType(src, tgt), fields, Collections.emptyList());
    }

    // ────────────── 通用工具 ──────────────

    /**
     * 通用层级 diff:按 key 配对 source / target 列表逐项交 nodeBuilder 构建节点。仅 target 有→新增、仅 source
     * 有→删除、两侧都有→配对比对(nodeBuilder 据入参 null 自判变更类型,无实质变更返 null)。
     * 返回顺序:target 现序在前、删除项补末尾。
     *
     * @param source      源列表
     * @param target      目标列表
     * @param keyFn       取配对 key 的函数
     * @param nodeBuilder 节点构建器
     * @param <T>         列表元素类型
     * @return 差异节点列表
     */
    private static <T> List<ProductVersionDiffNode> diffKeyedList(
        List<T> source, List<T> target, Function<T, String> keyFn,
        BiFunction<T, T, ProductVersionDiffNode> nodeBuilder) {
        Map<String, T> srcMap = indexBy(source, keyFn);
        Map<String, T> tgtMap = indexBy(target, keyFn);
        List<ProductVersionDiffNode> nodes = new ArrayList<>();
        tgtMap.forEach((code, t) -> {
            ProductVersionDiffNode node = nodeBuilder.apply(srcMap.get(code), t);
            if (node != null) {
                nodes.add(node);
            }
        });
        srcMap.forEach((code, s) -> {
            if (!tgtMap.containsKey(code)) {
                ProductVersionDiffNode node = nodeBuilder.apply(s, null);
                if (node != null) {
                    nodes.add(node);
                }
            }
        });
        return nodes;
    }

    /**
     * 组装节点:MODIFIED 且自身无字段变更、子级也无变更时返回 null(无实质变更不产出)。
     *
     * @param level      层级标识
     * @param paramKind  参数方向(非命令参数层级传 null)
     * @param code       节点编码
     * @param name       节点名称
     * @param changeType 变更类型
     * @param fields     字段级差异
     * @param children   子节点
     * @return 节点;无实质变更返 null
     */
    private static ProductVersionDiffNode assemble(String level, String paramKind, String code, String name,
                                                   String changeType, List<ProductVersionFieldDiffVO> fields,
                                                   List<ProductVersionDiffNode> children) {
        if (TYPE_MODIFIED.equals(changeType) && fields.isEmpty() && children.isEmpty()) {
            return null;
        }
        return ProductVersionDiffNode.builder()
            .level(level)
            .paramKind(paramKind)
            .code(code)
            .name(name)
            .changeType(changeType)
            .fields(fields)
            .children(children)
            .build();
    }

    /**
     * 反射比对一对对象的标量字段。任一侧为 null 时以空实例兜底 ── 新增→全部字段判 ADDED、删除→全部 REMOVED。
     * excludes 内字段(子集合 / 元信息)在 {@link ReflectionDiffBuilder#setExcludeFieldNames} diff 阶段直接跳过。
     *
     * @param source   源对象,可为 null
     * @param target   目标对象,可为 null
     * @param type     对象类型
     * @param excludes 排除比对的字段名
     * @param <T>      对象类型
     * @return 字段级差异列表
     */
    private static <T> List<ProductVersionFieldDiffVO> compareFields(T source, T target,
                                                                     Class<T> type, Set<String> excludes) {
        T src = source != null ? source : newEmpty(type);
        T tgt = target != null ? target : newEmpty(type);
        // 跟 EntityFieldDiffer 同模式:中间变量类型显式,避免 IDE 把外部类的 deprecation 误传染到
        // 内部 Builder.setExcludeFieldNames(non-deprecated)
        ReflectionDiffBuilder.Builder<T> rdbBuilder = ReflectionDiffBuilder.<T>builder()
            .setDiffBuilder(DiffBuilder.<T>builder()
                .setLeft(src)
                .setRight(tgt)
                .setStyle(ToStringStyle.SHORT_PREFIX_STYLE)
                .build())
            .setExcludeFieldNames(excludes.toArray(new String[0]));
        DiffResult<T> result = rdbBuilder.build().build();

        List<ProductVersionFieldDiffVO> fields = new ArrayList<>(result.getNumberOfDiffs());
        for (Diff<?> d : result.getDiffs()) {
            Object before = d.getLeft();
            Object after = d.getRight();
            // ReflectionDiffBuilder 把 null 和 "" 当不同值,会产出一条 diff;但业务上两者等价
            // (用户既看不出区别,前端 fmt 也都显示为 "—")—— 这种"两侧都为空"的噪声丢掉
            if (isBlank(before) && isBlank(after)) {
                continue;
            }
            fields.add(ProductVersionFieldDiffVO.builder()
                .field(d.getFieldName())
                .label(fieldLabel(type, d.getFieldName()))
                .changeType(primitiveChangeType(before, after))
                .before(before)
                .after(after)
                .dictType(fieldDictType(type, d.getFieldName()))
                .build());
        }
        return fields;
    }

    /**
     * 字段值业务意义上的"空":null 与 "" 等价处理。
     *
     * @param v 字段值
     * @return 是否为空
     */
    private static boolean isBlank(Object v) {
        return v == null || "".equals(v);
    }

    /**
     * 取字段 @Echo 注解的 dictType,无则 null。前端按此字典 key 把原始整数/编码值翻译成中文展示
     * (如 productType=1 → "普通产品"),避免前端硬编码字典映射。用 hutool {@link ReflectUtil#getField}
     * 支持父类字段查找。
     *
     * @param type      字段所属类型
     * @param fieldName 字段名
     * @return 字典类型 key;无则 null
     */
    private static String fieldDictType(Class<?> type, String fieldName) {
        Field f = ReflectUtil.getField(type, fieldName);
        if (f != null) {
            Echo echo = f.getAnnotation(Echo.class);
            if (echo != null && StrUtil.isNotBlank(echo.dictType())) {
                return echo.dictType();
            }
        }
        return null;
    }

    /**
     * 取字段 @Schema(description) 作中文标签,缺失则回退字段名。用 hutool {@link ReflectUtil#getField}
     * 支持父类字段查找。
     *
     * @param type      字段所属类型
     * @param fieldName 字段名
     * @return 中文标签;缺失回退字段名
     */
    private static String fieldLabel(Class<?> type, String fieldName) {
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
     * 反射创建空实例(快照 VO 均带无参构造)。
     *
     * @param type 目标类型
     * @param <T>  目标类型
     * @return 空实例
     */
    private static <T> T newEmpty(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("snapshot VO must have a no-arg constructor: " + type, e);
        }
    }

    /**
     * 字段变更类型:空 → 有值 = ADDED,有值 → 空 = REMOVED,两侧都有值 = MODIFIED。
     *
     * @param before 变更前值
     * @param after  变更后值
     * @return 变更类型
     */
    private static String primitiveChangeType(Object before, Object after) {
        boolean beforeBlank = isBlank(before);
        boolean afterBlank = isBlank(after);
        if (beforeBlank && !afterBlank) {
            return TYPE_ADDED;
        }
        if (!beforeBlank && afterBlank) {
            return TYPE_REMOVED;
        }
        return TYPE_MODIFIED;
    }

    /**
     * 节点变更类型:src 缺 = 新增,tgt 缺 = 删除,两者都在 = 修改。
     *
     * @param src 源对象
     * @param tgt 目标对象
     * @return 变更类型
     */
    private static String changeType(Object src, Object tgt) {
        if (src == null) {
            return TYPE_ADDED;
        }
        if (tgt == null) {
            return TYPE_REMOVED;
        }
        return TYPE_MODIFIED;
    }

    /**
     * 取节点显示值:优先 target、回退 source。
     *
     * @param src    源对象
     * @param tgt    目标对象
     * @param getter 取值函数
     * @param <T>    对象类型
     * @return 显示值
     */
    private static <T> String pick(T src, T tgt, Function<T, String> getter) {
        if (tgt != null && getter.apply(tgt) != null) {
            return getter.apply(tgt);
        }
        return src == null ? null : getter.apply(src);
    }

    private static <T> Map<String, T> indexBy(List<T> items, Function<T, String> keyFn) {
        Map<String, T> result = new LinkedHashMap<>();
        Optional.ofNullable(items).orElse(Collections.emptyList())
            .forEach(item -> Optional.ofNullable(keyFn.apply(item))
                .ifPresent(key -> result.put(key, item)));
        return result;
    }

    // 子集合安全取值(null / 空统一成空列表)
    private static List<ProductSnapshotServiceVO> services(ProductSnapshotVO vo) {
        return vo == null ? Collections.emptyList()
            : Optional.ofNullable(vo.getServices()).orElse(Collections.emptyList());
    }

    private static List<ProductSnapshotPropertyVO> properties(ProductSnapshotServiceVO vo) {
        return vo == null ? Collections.emptyList()
            : Optional.ofNullable(vo.getProperties()).orElse(Collections.emptyList());
    }

    private static List<ProductSnapshotCommandVO> commands(ProductSnapshotServiceVO vo) {
        return vo == null ? Collections.emptyList()
            : Optional.ofNullable(vo.getCommands()).orElse(Collections.emptyList());
    }

    private static List<ProductSnapshotCommandParameterVO> requests(ProductSnapshotCommandVO vo) {
        return vo == null ? Collections.emptyList()
            : Optional.ofNullable(vo.getRequests()).orElse(Collections.emptyList());
    }

    private static List<ProductSnapshotCommandParameterVO> responses(ProductSnapshotCommandVO vo) {
        return vo == null ? Collections.emptyList()
            : Optional.ofNullable(vo.getResponses()).orElse(Collections.emptyList());
    }

    // ────────────── 摘要汇总 ──────────────

    /**
     * 遍历差异树汇总轻量计数。
     *
     * @param nodes 差异节点列表
     * @return 差异摘要
     */
    private ProductVersionDiffSummaryVO summarize(List<ProductVersionDiffNode> nodes) {
        ProductVersionDiffSummaryVO s = ProductVersionDiffSummaryVO.builder().build();
        accumulate(nodes, s);
        return s;
    }

    private void accumulate(List<ProductVersionDiffNode> nodes, ProductVersionDiffSummaryVO s) {
        if (nodes == null) {
            return;
        }
        for (ProductVersionDiffNode n : nodes) {
            switch (n.getLevel()) {
                case LEVEL_PRODUCT -> s.setProductInfoChanged(
                    s.getProductInfoChanged() + (n.getFields() == null ? 0 : n.getFields().size()));
                case LEVEL_SERVICE -> accumulateService(s, n.getChangeType());
                case LEVEL_PROPERTY -> accumulateProperty(s, n.getChangeType());
                case LEVEL_COMMAND -> accumulateCommand(s, n.getChangeType());
                default -> {
                    // COMMAND_PARAM:参数变更已体现为所属命令的 MODIFIED,不单独计数
                }
            }
            accumulate(n.getChildren(), s);
        }
    }

    private void accumulateService(ProductVersionDiffSummaryVO s, String type) {
        switch (type) {
            case TYPE_ADDED -> s.setServiceAdded(s.getServiceAdded() + 1);
            case TYPE_REMOVED -> s.setServiceRemoved(s.getServiceRemoved() + 1);
            case TYPE_MODIFIED -> s.setServiceModified(s.getServiceModified() + 1);
            default -> {
            }
        }
    }

    private void accumulateProperty(ProductVersionDiffSummaryVO s, String type) {
        switch (type) {
            case TYPE_ADDED -> s.setPropertyAdded(s.getPropertyAdded() + 1);
            case TYPE_REMOVED -> s.setPropertyRemoved(s.getPropertyRemoved() + 1);
            case TYPE_MODIFIED -> s.setPropertyModified(s.getPropertyModified() + 1);
            default -> {
            }
        }
    }

    private void accumulateCommand(ProductVersionDiffSummaryVO s, String type) {
        switch (type) {
            case TYPE_ADDED -> s.setCommandAdded(s.getCommandAdded() + 1);
            case TYPE_REMOVED -> s.setCommandRemoved(s.getCommandRemoved() + 1);
            case TYPE_MODIFIED -> s.setCommandModified(s.getCommandModified() + 1);
            default -> {
            }
        }
    }

    /**
     * 差异摘要 → 人类可读文案;无任何变更返回 null(调用方据此判断是否值得记一条流水)。
     *
     * @param s 差异摘要
     * @return 文案;无变更返 null
     */
    private String buildSummaryText(ProductVersionDiffSummaryVO s) {
        List<String> parts = new ArrayList<>();
        if (s.getProductInfoChanged() > 0) {
            parts.add("产品信息变更");
        }
        int service = s.getServiceAdded() + s.getServiceRemoved() + s.getServiceModified();
        if (service > 0) {
            parts.add("服务变更 " + service + " 项");
        }
        int property = s.getPropertyAdded() + s.getPropertyRemoved() + s.getPropertyModified();
        if (property > 0) {
            parts.add("属性变更 " + property + " 项");
        }
        int command = s.getCommandAdded() + s.getCommandRemoved() + s.getCommandModified();
        if (command > 0) {
            parts.add("命令变更 " + command + " 项");
        }
        return parts.isEmpty() ? null : String.join("、", parts);
    }

    /**
     * 由差异摘要反推主变更类型:全为新增 → 新增,全为删除 → 删除,其余 → 编辑。
     *
     * @param s 差异摘要
     * @return 主变更类型值
     */
    private Integer resolveChangeType(ProductVersionDiffSummaryVO s) {
        int added = s.getServiceAdded() + s.getPropertyAdded() + s.getCommandAdded();
        int removed = s.getServiceRemoved() + s.getPropertyRemoved() + s.getCommandRemoved();
        int modified = s.getProductInfoChanged() + s.getServiceModified()
            + s.getPropertyModified() + s.getCommandModified();
        if (added > 0 && removed == 0 && modified == 0) {
            return ProductVersionChangeTypeEnum.CREATE.getValue();
        }
        if (removed > 0 && added == 0 && modified == 0) {
            return ProductVersionChangeTypeEnum.DELETE.getValue();
        }
        return ProductVersionChangeTypeEnum.UPDATE.getValue();
    }
}
