/**
 * 设备上行前置转换脚本 ── 按脚本类型(渠道)可扩展的模板注册表(一个渠道可多模板)。
 *
 * <p>以后新增渠道/模板只需往 {@link SCRIPT_TEMPLATES} 对应渠道的数组里加一条,无需改 UI;
 * 单模板渲染为按钮,多模板自动渲染为下拉。
 * key 与渠道字典 {@code RULE_GROOVY_SCRIPT_CHANNEL_CODE} 的 channelCode 对齐(小写)。
 *
 * @author mqttsnet
 */

/** 单个脚本模板项 */
export interface ScriptTemplateItem {
  /** 模板唯一键(同渠道内唯一) */
  key: string;
  /** 下拉/按钮显示名 */
  label: string;
  /** 模板内容(Groovy) */
  content: string;
}

/** 数据上报转换脚本模板(完整示例:解析 + 字段映射 + 平台标准 ThingLinks 信封) */
const DATA_REPORT_TEMPLATE = `/**
 * 设备上行前置转换脚本模板 —— 数据上报(产出平台标准 ThingLinks 信封)
 *
 * 引擎自动注入的绑定变量(可直接使用):
 *   originTopic / originBody       源设备上报的 topic 与原始报文(originBody 为字符串)
 *   originBodyHex                  原始报文十六进制(二进制 / 非 JSON 无损取字节)
 *   deviceIdentification / productIdentification / clientId
 *   device.*       设备基础信息,如 device.signKey(签名密钥) / device.encryptMethod(0明文 1SM4 2AES)
 *                  / device.fwVersion(固件版本) / device.boundProductVersionNo(绑定的产品版本)
 *   product.*      产品基础信息,如 product.protocolType / product.dataFormat / product.model
 *   productModel   本版本物模型(运行时=设备绑定版本 / 调试=选中版本):productModel.services[].properties[]
 *   config.*       脚本扩展参数(extend_params),如 config.SERVICE_CODE
 *   log            调试日志:log.info / log.warn / log.debug / log.error(...) → 调试面板「执行日志」展示,运行时进服务日志
 *
 * 出参约定:返回 [topic, payload]。
 *   topic    : 平台标准数据上报主题 /v1/devices/{deviceId}/datas
 *   payload  : 平台标准 ThingLinks 信封 ── 用 JSON.toJSONString(...) 序列化成 JSON 串再返回!
 *              这样 head.mid / head.timeStamp 等数值字段才会按"数值"送达;直接返回 Map 会被 rule 的
 *              JSON 序列化把 Long 转成字符串,导致下游协议解析报类型错(String cannot be cast to Long)。
 *   head     : {mid 消息ID(数值,SnowflakeIdUtil.nextLong()), cipherFlag 取 device.encryptMethod, timeStamp 13位毫秒(数值)}
 *   dataBody : {devices:[{deviceId, services:[{serviceCode, data, eventTime}]}]}
 *   dataSign : 明文(cipherFlag=0)留空;需加密时 SHA256(timeStamp:device.signKey) 全小写
 */
import com.alibaba.fastjson2.JSON
import com.mqttsnet.basic.utils.SnowflakeIdUtil
import org.apache.commons.codec.digest.DigestUtils

// 1) 解析源厂商报文(纯文本 / 二进制见「非JSON」模板,从 originBodyHex 取字节)
def origin = JSON.parseObject(originBody)

// 2) 设备 / 配置上的动态值(密钥、加密方式都来自注入的 device.*)
def deviceId   = deviceIdentification ?: device?.deviceIdentification
def signKey    = device?.signKey ?: ""
def cipherFlag = (device?.encryptMethod ?: 0) as int
def ts         = System.currentTimeMillis()

// 调试日志:log.xxx(...) 显示在调试面板「执行日志」,运行时进 rule 服务日志(logger: groovy.script)
log.info("[transform] deviceId=" + deviceId + " origin=" + origin)

// 3) 厂商字段 → 平台物模型字段映射(serviceCode 取 config.SERVICE_CODE;按你的报文 + 产品物模型改)
def service = [
    serviceCode: (config?.SERVICE_CODE ?: "default"),
    eventTime  : (origin?.ts ?: ts),                    // 设备带时间戳就用它,否则用当前毫秒
    data       : [
        battery        : origin?.battery,                // 直接透传
        signal_strength: origin?.rssi,                   // 字段改名
        firmware       : origin?.fw ?: device?.fwVersion, // 缺省回退设备档案
        // TODO 按产品物模型补全其余属性(可做单位换算 / 类型转换 / 枚举映射)
    ].findAll { k, v -> v != null }
]
// 多服务上报:再建 def service2 = [serviceCode: "...", eventTime: ts, data: [...]];下面 services 用 [service, service2]

// (可选)用本版本物模型校验 / 动态映射:只上报物模型里声明过的属性
// def modelSvc = productModel?.services?.find { it.serviceCode == service.serviceCode }
// def declared = (modelSvc?.properties*.propertyCode ?: []) as Set
// service.data = service.data.findAll { k, v -> declared.contains(k) }

// 4) 平台标准 ThingLinks 信封(mid 用数值型 nextLong;timeStamp 为毫秒数值)
def payload = [
    head    : [mid: SnowflakeIdUtil.nextLong(), cipherFlag: cipherFlag, timeStamp: ts],
    dataBody: [devices: [[deviceId: deviceId, services: [service]]]],
    dataSign: cipherFlag == 0 ? "" : DigestUtils.sha256Hex(ts + ":" + signKey).toLowerCase()
]

// 5) payload 序列化成 JSON 串再返回 ── 数值字段(mid/timeStamp/eventTime)才能原样送达
return [topic: "/v1/devices/" + deviceId + "/datas", payload: JSON.toJSONString(payload)]
`;

/** 最小骨架模板(快速起步:仅解析 + 改写 topic,payload 按需补全) */
const MINIMAL_TEMPLATE = `/**
 * 设备上行前置转换 —— 最小骨架(产出平台标准 ThingLinks 信封)
 * 绑定变量:originTopic / originBody / device.* / product.* / productModel / config.* / log(调试日志)
 * 出参:[topic, payload];payload 务必用 JSON.toJSONString(...) 返回,数值字段(mid/timeStamp)才不被转成字符串。
 */
import com.alibaba.fastjson2.JSON
import com.mqttsnet.basic.utils.SnowflakeIdUtil

def origin = JSON.parseObject(originBody)
def deviceId = deviceIdentification ?: device?.deviceIdentification
def ts = System.currentTimeMillis()

// log.xxx(...) 显示在调试「执行日志」,运行时进服务日志
log.info("[transform] deviceId=" + deviceId + " origin=" + origin)

def payload = [
    head    : [mid: SnowflakeIdUtil.nextLong(), cipherFlag: (device?.encryptMethod ?: 0) as int, timeStamp: ts],
    dataBody: [devices: [[
        deviceId: deviceId,
        services: [[
            serviceCode: (config?.SERVICE_CODE ?: "default"),
            data       : origin,   // TODO 按平台物模型字段映射改写
            eventTime  : ts
        ]]
    ]]],
    dataSign: ""
]
return [topic: "/v1/devices/" + deviceId + "/datas", payload: JSON.toJSONString(payload)]
`;

/** 非 JSON 报文转换模板(文本 / 十六进制 / 二进制:从 originBodyHex 拿无损原始字节) */
const NONJSON_TEMPLATE = `/**
 * 设备上行前置转换 —— 非 JSON 报文(文本 / 十六进制 / 二进制)
 *
 * 绑定:originBody(文本/JSON 串)、originBodyHex(原始报文十六进制,二进制无损)
 *      device.* / product.* / productModel / config.* / log(调试日志)
 * 出参:[topic, payload];payload 用 JSON.toJSONString(...) 返回(数值字段不被转成字符串)。
 */
import com.alibaba.fastjson2.JSON
import cn.hutool.core.util.HexUtil
import com.mqttsnet.basic.utils.SnowflakeIdUtil

def deviceId = deviceIdentification ?: device?.deviceIdentification
def ts       = System.currentTimeMillis()

// 1) 拿原始字节:二进制协议从 originBodyHex 解;纯文本可直接用 originBody
def bytes = originBodyHex ? HexUtil.decodeHex(originBodyHex) : (originBody ?: "").getBytes("UTF-8")

// 2) 按你的厂商二进制/文本协议解析字段(示例:第1字节=电量、第2字节=亮度)
def data = [
    battery   : bytes.length > 0 ? (bytes[0] & 0xFF) : null,
    brightness: bytes.length > 1 ? (bytes[1] & 0xFF) : null
    // 文本/分隔符协议示例:def parts = (originBody ?: "").split(","); parts[0] ...
].findAll { k, v -> v != null }

// log.xxx(...) 显示在调试「执行日志」,运行时进服务日志
log.info("[transform] bytes=" + bytes.length + " data=" + data)

// 3) 平台标准 ThingLinks 信封 → JSON 串返回
def payload = [
    head    : [mid: SnowflakeIdUtil.nextLong(), cipherFlag: (device?.encryptMethod ?: 0) as int, timeStamp: ts],
    dataBody: [devices: [[deviceId: deviceId, services: [[
        serviceCode: (config?.SERVICE_CODE ?: "default"), data: data, eventTime: ts
    ]]]]],
    dataSign: ""
]
return [topic: "/v1/devices/" + deviceId + "/datas", payload: JSON.toJSONString(payload)]
`;

/**
 * 通用模板列表 ── 上行转换契约与渠道无关(同样注入 originTopic / originBody / device / product /
 * productModel / config / log,同样产出平台标准 ThingLinks 信封),故 mqtt / webSocket 共用同一份骨架。
 */
const COMMON_TEMPLATES: ScriptTemplateItem[] = [
  { key: 'data_report', label: '数据上报(完整示例)', content: DATA_REPORT_TEMPLATE },
  { key: 'minimal', label: '最小骨架', content: MINIMAL_TEMPLATE },
  { key: 'nonjson', label: '非JSON(文本/十六进制)', content: NONJSON_TEMPLATE },
];

/**
 * 脚本模板注册表:channelCode(小写) → 该渠道的模板列表(可多个)。
 */
export const SCRIPT_TEMPLATES: Record<string, ScriptTemplateItem[]> = {
  mqtt: COMMON_TEMPLATES,
  websocket: COMMON_TEMPLATES,
};

/**
 * 取指定渠道的脚本模板列表;无对应渠道返回空数组。
 *
 * @param channelCode 渠道编码(大小写不敏感)
 */
export function getScriptTemplates(channelCode?: string | null): ScriptTemplateItem[] {
  if (!channelCode) return [];
  return SCRIPT_TEMPLATES[String(channelCode).toLowerCase()] ?? [];
}
