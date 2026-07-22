/**
 * ProductTopicPicker - 产品 Topic 主题选择器组件类型定义。
 *
 * <p>支持两种模式:
 * <ul>
 *   <li>basic - 从产品已配置的 ProductTopic 列表里选(列表页);依赖 productIdentification 拉数据</li>
 *   <li>custom - 用户手动输入 MQTT 通配符 topic(如 device/+/data 或 home/room/#)</li>
 * </ul>
 *
 * <p>组件 v-model 是字符串(最终 topic 值);内部记录 mode 决定 UI。
 *
 * @author mqttsnet
 */

/** 选择模式 */
export type ProductTopicPickerMode = 'basic' | 'custom';

/**
 * 基础 topic 来源数据(通常是 ProductTopic 表的一行)。
 * 字段名按 ProductTopicResultVO 对齐,但仅依赖 topic + 几个展示字段。
 */
export interface ProductTopicRecord {
  topic: string;
  remark?: string;
  echoMap?: {
    functionType?: string;
    publisher?: string;
    subscriber?: string;
  };
  [k: string]: any;
}

/** 选择器触发器形态 */
export type ProductTopicPickerTriggerStyle = 'input' | 'tag';
