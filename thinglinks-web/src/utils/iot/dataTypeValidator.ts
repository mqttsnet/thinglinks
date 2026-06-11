/**
 * 物模型数据类型校验 / 提示 / 控件配置 统一工具。
 *
 * <p><b>设计动机</b>:</p>
 * <ul>
 *   <li>物模型属性 / 命令参数的 {@code datatype} 决定了"允许填什么数值范围、需要哪些约束字段、
 *       用什么 antd 控件",过去这些散落在多个 data.tsx 里,新增字段时容易遗漏校验</li>
 *   <li>设备调试时用户按命令模板填参数,同样需要按 {@code datatype} 校验输入,
 *       逻辑应与"物模型定义编辑"完全一致 ── 这里统一收口</li>
 * </ul>
 *
 * <p><b>典型用法</b>:</p>
 * <pre>{@code
 * // 1. BasicForm schema 里按 datatype 动态切控件 + 校验:
 * componentProps: ({ formModel }) => buildComponentProps(formModel.datatype, { width: '100%' }),
 * rules: ({ values }) => buildRules(values.datatype, { required: true }),
 *
 * // 2. 设备调试参数面板按命令参数 datatype 校验用户输入:
 * const errMsg = validateByDatatype(parameter.datatype, userInput, { min, max, enumlist });
 * if (errMsg) showError(errMsg);
 * }</pre>
 *
 * <p><b>时序库行业上限(强约束,超出后端写表会拒)</b>:</p>
 * <ul>
 *   <li>{@code int} → JS 安全整数范围(2^53)内,但建议落到 {@link #TD_INT_RANGE}</li>
 *   <li>{@code string} → NCHAR 字段最大 {@link #TD_NCHAR_MAX} 字节</li>
 * </ul>
 */
import type { Rule } from 'ant-design-vue/es/form';
import { useI18n } from '/@/hooks/web/useI18n';

/**
 * 延迟拿 t —— 模块 init 时 i18n 可能还没 setup 完(不同入口 import 顺序不固定),
 * 提前 destructure 会拿到兜底的 echo fn(永远返 raw key,locale 切换也不生效)。
 * 所有导出方法在调用时再 useI18n(),那时 i18n.global 必已就绪。
 */
function tt(): (key: string, ...args: any[]) => string {
    return useI18n().t as unknown as (key: string, ...args: any[]) => string;
}

/** 物模型支持的 datatype 枚举(与后端 {@code ProductPropertyDataTypeEnum} 字典 LINK_PRODUCT_SERVICE_PROPERTY_DATA_TYPE 对齐)。 */
export type ModelDatatype =
    | 'int'
    | 'decimal'
    | 'string'
    | 'bool'
    | 'DateTime'
    | 'jsonObject';

/** 时序库单字段 NCHAR/BINARY 最大字节数(行级总和上限 65517,本工具不做行级校验)。 */
export const TD_NCHAR_MAX = 16374;

/** int 类型推荐范围(避开 JS Number 精度损失,实际时序库 INT 上限为 2^31-1)。 */
export const TD_INT_RANGE = { min: -2147483648, max: 2147483647 };

/** decimal(对应时序库 DOUBLE)推荐范围 ── 用户填超出时仅 warn,不阻断。 */
export const TD_DECIMAL_RANGE = { min: -1.7976931348623157e308, max: 1.7976931348623157e308 };

/** 是否需要 maxlength 字段(变长类型才需要)。 */
export function needMaxLength(datatype?: string): boolean {
    return datatype === 'string' || datatype === 'DateTime' || datatype === 'jsonObject';
}

/** 是否需要 min/max/step 数值约束字段。 */
export function needNumericConstraints(datatype?: string): boolean {
    return datatype === 'int' || datatype === 'decimal';
}

/** 是否需要 enumlist 枚举值字段(目前只对 string 开放,未来 int 也可能加)。 */
export function needEnumList(datatype?: string): boolean {
    return datatype === 'string';
}

/**
 * 按 datatype 推荐的 antd 控件类型(用于 BasicForm schema 的 component 字段)。
 *
 * <ul>
 *   <li>{@code int / decimal} → InputNumber</li>
 *   <li>{@code bool} → Switch</li>
 *   <li>{@code DateTime} → DatePicker(带时间)</li>
 *   <li>{@code string / jsonObject} → Input / InputTextArea</li>
 * </ul>
 */
export function pickComponent(datatype?: string): string {
    switch (datatype) {
        case 'int':
        case 'decimal':
            return 'InputNumber';
        case 'bool':
            return 'Switch';
        case 'DateTime':
            return 'DatePicker';
        case 'jsonObject':
            return 'InputTextArea';
        default:
            return 'Input';
    }
}

/**
 * 按 datatype 生成 BasicForm 的 componentProps。
 *
 * <p>例如 int 会自动设 {@code precision: 0} 防小数;decimal 不限制小数位;
 * DateTime 自动开 showTime + 标准 format。</p>
 */
export function buildComponentProps(datatype?: string, extra: Record<string, unknown> = {}): Record<string, unknown> {
    const t = tt();
    const base: Record<string, unknown> = { style: { width: '100%' }, ...extra };
    switch (datatype) {
        case 'int':
            return {
                ...base,
                placeholder: t('common.inputText'),
                precision: 0,
                min: TD_INT_RANGE.min,
                max: TD_INT_RANGE.max,
            };
        case 'decimal':
            return {
                ...base,
                placeholder: t('common.inputText'),
            };
        case 'bool':
            return { ...base };
        case 'DateTime':
            return {
                ...base,
                showTime: true,
                format: 'YYYY-MM-DD HH:mm:ss',
                valueFormat: 'YYYY-MM-DD HH:mm:ss',
            };
        case 'jsonObject':
            return { ...base, rows: 4, placeholder: '{ "key": "value" }' };
        case 'string':
        default:
            return { ...base, placeholder: t('common.inputText') };
    }
}

/**
 * 按 datatype 构建 antd Form Rule 列表(用于物模型定义编辑 / 设备调试参数填写)。
 *
 * @param datatype 物模型 datatype
 * @param opts.required 是否必填
 * @param opts.min / opts.max 物模型定义的范围(decimal/int 用)
 * @param opts.maxlength 物模型定义的字符长度(string 用)
 * @param opts.enumlist 物模型定义的枚举值(逗号分隔字符串,string 用)
 */
export function buildRules(
    datatype: string | undefined,
    opts: {
        required?: boolean;
        min?: number | string;
        max?: number | string;
        maxlength?: number | string;
        enumlist?: string;
    } = {},
): Rule[] {
    const t = tt();
    const rules: Rule[] = [];
    if (opts.required) {
        rules.push({
            required: true,
            message: t('common.inputText'),
        });
    }
    switch (datatype) {
        case 'int':
            // 用 validator 而非 type:'integer',兼容字符串回显值(避免被类型校验误拦)
            rules.push({
                validator: (_: any, value: any) =>
                    value == null || value === '' || Number.isInteger(Number(value))
                        ? Promise.resolve()
                        : Promise.reject(t('iot.link.productProperty.productProperty.validator.intType')),
            });
            rules.push(rangeRule(opts.min, opts.max, TD_INT_RANGE.min, TD_INT_RANGE.max));
            break;
        case 'decimal':
            // 用 validator 而非 type:'number',兼容字符串回显值(避免被类型校验误拦)
            rules.push({
                validator: (_: any, value: any) =>
                    value == null || value === '' || !Number.isNaN(Number(value))
                        ? Promise.resolve()
                        : Promise.reject(t('iot.link.productProperty.productProperty.validator.numberType')),
            });
            rules.push(rangeRule(opts.min, opts.max, TD_DECIMAL_RANGE.min, TD_DECIMAL_RANGE.max));
            break;
        case 'string':
            if (opts.maxlength != null) {
                const cap = Math.min(Number(opts.maxlength) || TD_NCHAR_MAX, TD_NCHAR_MAX);
                rules.push({
                    type: 'string',
                    max: cap,
                    message: t('iot.link.productProperty.productProperty.validator.stringMax', { n: cap }),
                });
            }
            if (opts.enumlist) {
                const options = parseEnumList(opts.enumlist);
                if (options.length) {
                    rules.push({
                        validator: (_: any, value: any) => {
                            if (value == null || value === '') return Promise.resolve();
                            return options.includes(String(value))
                                ? Promise.resolve()
                                : Promise.reject(
                                      tt()('iot.link.productProperty.productProperty.validator.enumOnly', {
                                          options: options.join(' / '),
                                      }),
                                  );
                        },
                    });
                }
            }
            break;
        case 'bool':
            rules.push({
                validator: (_: any, value: any) => {
                    if (value == null || typeof value === 'boolean') return Promise.resolve();
                    return Promise.reject(tt()('iot.link.productProperty.productProperty.validator.boolType'));
                },
            });
            break;
        case 'DateTime':
            if (opts.maxlength != null) {
                rules.push({
                    type: 'string',
                    max: Number(opts.maxlength),
                    message: t('iot.link.productProperty.productProperty.validator.stringMax', { n: opts.maxlength }),
                });
            }
            break;
        case 'jsonObject':
            rules.push({
                validator: (_: any, value: any) => {
                    if (value == null || value === '') return Promise.resolve();
                    try {
                        JSON.parse(String(value));
                        return Promise.resolve();
                    } catch (e: any) {
                        return Promise.reject(
                            tt()('iot.link.productProperty.productProperty.validator.jsonInvalid', {
                                msg: e?.message ?? '',
                            }),
                        );
                    }
                },
            });
            break;
    }
    return rules;
}

/**
 * 同步校验单个输入值(用于设备调试场景:用户填参数立即返回错误信息)。
 *
 * @returns 错误文案(可直接 antd message.warn);通过返 {@code null}
 */
export function validateByDatatype(
    datatype: string | undefined,
    value: any,
    opts: {
        required?: boolean;
        min?: number | string;
        max?: number | string;
        maxlength?: number | string;
        enumlist?: string;
    } = {},
): string | null {
    const t = tt();
    if (value == null || value === '') {
        return opts.required ? t('common.inputText') : null;
    }
    switch (datatype) {
        case 'int': {
            const n = Number(value);
            if (!Number.isInteger(n)) {
                return t('iot.link.productProperty.productProperty.validator.intType');
            }
            return checkRange(n, opts.min, opts.max, TD_INT_RANGE.min, TD_INT_RANGE.max);
        }
        case 'decimal': {
            const n = Number(value);
            if (Number.isNaN(n)) {
                return t('iot.link.productProperty.productProperty.validator.numberType');
            }
            return checkRange(n, opts.min, opts.max, TD_DECIMAL_RANGE.min, TD_DECIMAL_RANGE.max);
        }
        case 'string': {
            const s = String(value);
            const cap = Math.min(Number(opts.maxlength) || TD_NCHAR_MAX, TD_NCHAR_MAX);
            if (s.length > cap) {
                return t('iot.link.productProperty.productProperty.validator.stringMax', { n: cap });
            }
            if (opts.enumlist) {
                const options = parseEnumList(opts.enumlist);
                if (options.length && !options.includes(s)) {
                    return t('iot.link.productProperty.productProperty.validator.enumOnly', {
                        options: options.join(' / '),
                    });
                }
            }
            return null;
        }
        case 'bool':
            return typeof value === 'boolean'
                ? null
                : t('iot.link.productProperty.productProperty.validator.boolType');
        case 'DateTime':
            return null;
        case 'jsonObject':
            try {
                JSON.parse(String(value));
                return null;
            } catch (e: any) {
                return t('iot.link.productProperty.productProperty.validator.jsonInvalid', {
                    msg: e?.message ?? '',
                });
            }
        default:
            return null;
    }
}

/** 按 datatype 推荐的"问号 tooltip 提示文案"(供 BasicForm helpMessage 用)。 */
export function buildHelpMessage(datatype?: string): string {
    const t = tt();
    switch (datatype) {
        case 'int':
            return t('iot.link.productProperty.productProperty.help.int', TD_INT_RANGE);
        case 'decimal':
            return t('iot.link.productProperty.productProperty.help.decimal');
        case 'string':
            return t('iot.link.productProperty.productProperty.help.string', { max: TD_NCHAR_MAX });
        case 'bool':
            return t('iot.link.productProperty.productProperty.help.bool');
        case 'DateTime':
            return t('iot.link.productProperty.productProperty.help.dateTime');
        case 'jsonObject':
            return t('iot.link.productProperty.productProperty.help.jsonObject');
        default:
            return '';
    }
}

/**
 * 物模型编码(服务 / 属性 / 命令 / 参数)命名规范 —— 平台统一标准,与后端 ThingModelCodeRule 对齐。
 * 编码作为底层时序库表名 / 列名等数据标识,统一为小写字母开头的 snake_case(小写字母、数字、下划线,长度 2-50);
 * 系统仅做校验拦截、不做任何大小写转换,以保证存储值与用户输入原值一致。
 */
export const THING_MODEL_CODE_PATTERN = /^[a-z][a-z0-9_]{1,49}$/;

/**
 * 构建编码字段的 antd Form 校验规则:必填 + 小写 snake_case 格式(长度内含于正则)。
 * 空值只由 required 提示,有值才校验格式,避免重复报错。
 */
export function thingModelCodeRules(opts: { required?: boolean } = {}): Rule[] {
    const t = tt();
    const rules: Rule[] = [];
    if (opts.required !== false) {
        rules.push({ required: true, message: t('iot.link.product.thingModelCode.required') });
    }
    rules.push({
        validator: (_: any, value: any) => {
            if (value == null || value === '') return Promise.resolve();
            return THING_MODEL_CODE_PATTERN.test(String(value))
                ? Promise.resolve()
                : Promise.reject(t('iot.link.product.thingModelCode.pattern'));
        },
    });
    return rules;
}

// ─── 内部工具 ──────────────────────────────────────────────────

/** 把 "RED,GREEN,BLUE" / "RED|GREEN|BLUE" 拆成数组,自动 trim + 去空。 */
export function parseEnumList(enumlist: string): string[] {
    if (!enumlist) return [];
    return enumlist
        .split(/[,|]/)
        .map((s) => s.trim())
        .filter(Boolean);
}

/** 生成 antd 数值 rule(用户定义优先;无则走 datatype 系统上限)。 */
function rangeRule(
    userMin: number | string | undefined,
    userMax: number | string | undefined,
    sysMin: number,
    sysMax: number,
): Rule {
    const effMin = userMin != null && userMin !== '' ? Number(userMin) : sysMin;
    const effMax = userMax != null && userMax !== '' ? Number(userMax) : sysMax;
    return {
        validator: (_: any, value: any) => {
            if (value == null || value === '') return Promise.resolve();
            const n = Number(value);
            if (Number.isNaN(n)) return Promise.resolve();
            if (n < effMin || n > effMax) {
                return Promise.reject(
                    tt()('iot.link.productProperty.productProperty.validator.outOfRange', {
                        min: effMin,
                        max: effMax,
                    }),
                );
            }
            return Promise.resolve();
        },
    };
}

/** 同步范围检查(返错误文案 / null)。 */
function checkRange(
    n: number,
    userMin: number | string | undefined,
    userMax: number | string | undefined,
    sysMin: number,
    sysMax: number,
): string | null {
    const effMin = userMin != null && userMin !== '' ? Number(userMin) : sysMin;
    const effMax = userMax != null && userMax !== '' ? Number(userMax) : sysMax;
    if (n < effMin || n > effMax) {
        return tt()('iot.link.productProperty.productProperty.validator.outOfRange', {
            min: effMin,
            max: effMax,
        });
    }
    return null;
}
