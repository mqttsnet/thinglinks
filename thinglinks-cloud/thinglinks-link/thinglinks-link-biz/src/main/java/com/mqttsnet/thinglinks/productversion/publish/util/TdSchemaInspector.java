package com.mqttsnet.thinglinks.productversion.publish.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mqttsnet.thinglinks.productpublishrecord.vo.ddl.DdlFieldVO;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * TDengine 表结构反查结果分析工具 ── 把 {@link SuperTableDescribeVO} 列表转成 {@link DdlFieldVO} 集合并按
 * schema / tag 分组,同时计算行级字节数合计(跟 TDengine 内部存储一致,单条 row 字段字节总和 ≤ 65531)。
 *
 * @author mqttsnet
 */
public final class TdSchemaInspector {

    /**
     * TDengine 单行所有字段(不含 tag)字节合计上限。
     */
    public static final int TD_ROW_MAX_BYTES = 65531;

    /**
     * NCHAR UTF-32 编码,每字符 4 字节。
     */
    private static final int NCHAR_BYTES_PER_CHAR = 4;

    /**
     * TDengine JSON 字段固定字节数。
     */
    private static final int JSON_FIXED_BYTES = 4096;

    /**
     * 定长类型字节数表(大写归一化后查询)。变长类型(NCHAR/BINARY/VARCHAR/VARBINARY)由 computeBytes 单独按 length 计算。
     */
    private static final Map<String, Integer> FIXED_TYPE_BYTES = Map.of(
        "TIMESTAMP", 8,
        "BIGINT", 8,
        "DOUBLE", 8,
        "INT", 4,
        "FLOAT", 4,
        "SMALLINT", 2,
        "TINYINT", 1,
        "BOOL", 1
    );

    /**
     * TDengine {@code DESCRIBE} 返回 tag 字段的 {@code note} 列固定值。
     */
    private static final String NOTE_TAG = "TAG";

    /**
     * 隐藏构造器 ── 纯工具类。
     */
    private TdSchemaInspector() {
    }

    /**
     * 分析 describe 返回值,产出按 schema / tag 分组的字段集合 + 行级字节合计。describeRows 允许 null/empty;
     * 返回始终非 null,为空时返回各字段空集合 + rowBytes=0 的实例。
     *
     * @param describeRows describe 返回行,允许 null/empty
     * @return 表结构快照,始终非 null
     */
    public static SchemaSnapshot inspect(List<SuperTableDescribeVO> describeRows) {
        List<DdlFieldVO> schema = new java.util.ArrayList<>();
        List<DdlFieldVO> tags = new java.util.ArrayList<>();
        int rowBytes = 0;
        for (SuperTableDescribeVO row : Optional.ofNullable(describeRows).orElse(Collections.emptyList())) {
            DdlFieldVO field = toDdlField(row);
            if (NOTE_TAG.equalsIgnoreCase(row.getNote())) {
                tags.add(field);
            } else {
                schema.add(field);
                rowBytes += Optional.ofNullable(field.getBytes()).orElse(0);
            }
        }
        return SchemaSnapshot.builder()
            .schemaFields(schema)
            .tagsFields(tags)
            .rowBytes(rowBytes)
            .build();
    }

    /**
     * 单行 describe 结果 → 简化 {@link DdlFieldVO}。
     *
     * @param row 单行 describe 结果
     * @return 简化字段 VO
     */
    private static DdlFieldVO toDdlField(SuperTableDescribeVO row) {
        Integer bytes = computeBytes(row.getType(), row.getLength());
        return DdlFieldVO.builder()
            .field(row.getField())
            .type(row.getType())
            .length(row.getLength())
            .bytes(bytes)
            .build();
    }

    /**
     * 按 TDengine 数据类型(case-insensitive)+ 长度计算实际字节数。length 为变长类型字符数(定长可 null);
     * type 为 null 返 0。
     *
     * @param type   TDengine 数据类型
     * @param length 变长类型字符数,定长可 null
     * @return 字节数;type 为 null 或未知类型返 0
     */
    public static Integer computeBytes(String type, Integer length) {
        if (type == null) {
            return 0;
        }
        String upper = type.trim().toUpperCase();
        Integer fixed = FIXED_TYPE_BYTES.get(upper);
        if (fixed != null) {
            return fixed;
        }
        if ("JSON".equals(upper)) {
            return JSON_FIXED_BYTES;
        }
        // 变长类型:NCHAR = length×4,BINARY/VARCHAR/VARBINARY = length×1
        int len = Optional.ofNullable(length).orElse(0);
        if ("NCHAR".equals(upper)) {
            return len * NCHAR_BYTES_PER_CHAR;
        }
        if ("BINARY".equals(upper) || "VARCHAR".equals(upper) || "VARBINARY".equals(upper)) {
            return len;
        }
        // 未知类型,按 0 计避免 NPE(同时不污染 rowBytes 合计)
        return 0;
    }

    /**
     * 表结构快照 ── inspect 的返回值,聚合三件套:schema 字段 / tag 字段 / 行级字节合计。
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SchemaSnapshot {
        private List<DdlFieldVO> schemaFields;
        private List<DdlFieldVO> tagsFields;
        private Integer rowBytes;

        // Lombok @Builder 不直接配合 @NoArgsConstructor + 字段非 final 时:需要手写带参 ctor 让 builder 生成 build()
        SchemaSnapshot(List<DdlFieldVO> schemaFields, List<DdlFieldVO> tagsFields, Integer rowBytes) {
            this.schemaFields = schemaFields;
            this.tagsFields = tagsFields;
            this.rowBytes = rowBytes;
        }
    }
}
