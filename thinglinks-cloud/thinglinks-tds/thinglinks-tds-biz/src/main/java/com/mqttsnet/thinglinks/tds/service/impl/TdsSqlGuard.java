package com.mqttsnet.thinglinks.tds.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.tds.model.Fields;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;

import java.util.List;
import java.util.regex.Pattern;

/**
 * TDS SQL 入参守卫。
 * <p>
 * 对要拼进 TDengine SQL 的"标识符"(库名/表名/超级表名/列名/标签名)做严格白名单校验，
 * 防止 SQL 注入。值类字段(fieldValue)由 mapper 的 {@code #{}} 参数绑定负责转义，
 * 这里只兜底标识符——因为标识符在 SQL 里不能用占位符绑定，只能拼接。
 *
 * @author mqttsnet
 */
public final class TdsSqlGuard {

    /**
     * TDengine 标识符：字母或下划线起头，仅含字母/数字/下划线。
     * <p>
     * 长度上限取 TDengine 表名/超级表名/子表名的上限 192（库名、列名、标签名 TDengine 自身限 192，
     * 超出由 DB 报错兜底）。此处只做"字符集 + 合理长度"的防注入校验，不替 TDengine 做精确的分类长度限制，
     * 以免误杀系统生成的长表名（如 common_{雪花id}_{雪花id}_xxx 形态的超级表名）。
     */
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,191}$");

    private TdsSqlGuard() {
    }

    /**
     * 校验单个标识符，非法即抛 {@link BizException}。
     *
     * @param identifier 待校验标识符
     * @param fieldDesc  字段描述(用于报错定位)
     */
    public static void checkIdentifier(String identifier, String fieldDesc) {
        if (StrUtil.isBlank(identifier) || !IDENTIFIER.matcher(identifier).matches()) {
            throw BizException.wrap("非法的 TDengine 标识符[" + fieldDesc + "]: " + identifier);
        }
    }

    /**
     * 标识符非空时才校验(用于可能由服务端补齐的可选字段，如 dataBaseName)。
     */
    public static void checkIdentifierIfPresent(String identifier, String fieldDesc) {
        if (StrUtil.isNotBlank(identifier)) {
            checkIdentifier(identifier, fieldDesc);
        }
    }

    /**
     * 校验一组字段的字段名(列名/标签名)。
     */
    public static void checkFieldNames(List<Fields> fieldsList, String fieldDesc) {
        if (CollUtil.isEmpty(fieldsList)) {
            return;
        }
        for (Fields f : fieldsList) {
            checkIdentifier(f == null ? null : f.getFieldName(), fieldDesc + ".fieldName");
        }
    }

    /**
     * 校验单个字段对象的字段名(dataType 由枚举 {@code TdDataTypeEnum} 保证合法)。
     */
    public static void checkField(Fields fields, String fieldDesc) {
        if (fields == null) {
            return;
        }
        checkIdentifier(fields.getFieldName(), fieldDesc + ".fieldName");
    }

    /**
     * 子表/插入数据 DTO 校验：表名、超级表名、各列名/标签名。
     * 值(fieldValue)不在此校验，由 mapper {@code #{}} 绑定。
     */
    public static void checkTable(TableDTO dto) {
        if (dto == null) {
            throw BizException.wrap("TableDTO 不能为空");
        }
        checkIdentifierIfPresent(dto.getDataBaseName(), "dataBaseName");
        checkIdentifier(dto.getTableName(), "tableName");
        checkIdentifier(dto.getSuperTableName(), "superTableName");
        checkFieldNames(dto.getSchemaFieldValues(), "schemaFieldValues");
        checkFieldNames(dto.getTagsFieldValues(), "tagsFieldValues");
    }

    /**
     * 超级表 DTO 校验：库名、超级表名、schema/tag 列名、单字段对象。
     */
    public static void checkSuperTable(SuperTableDTO dto) {
        if (dto == null) {
            throw BizException.wrap("SuperTableDTO 不能为空");
        }
        checkIdentifierIfPresent(dto.getDataBaseName(), "dataBaseName");
        checkIdentifier(dto.getSuperTableName(), "superTableName");
        checkFieldNames(dto.getSchemaFields(), "schemaFields");
        checkFieldNames(dto.getTagsFields(), "tagsFields");
        checkField(dto.getFields(), "fields");
    }
}
