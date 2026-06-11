/**
 * ScriptTemplatePicker ── 规则脚本「按类型一键填充模板」按钮(IoT 应用内通用组件)。
 *
 * <p>按 channelCode 从模板注册表({@code ./templates})取脚本模板;无对应模板则按钮禁用。
 * 已有脚本内容时填充前二次确认覆盖,确认后 {@code emit('fill', 模板内容)} 由调用方写入脚本字段。
 * 新增渠道/类型只需往 {@code SCRIPT_TEMPLATES} 增加一条,无需改组件。
 *
 * <p>使用样例:
 * <pre>
 *   import { ScriptTemplatePicker } from '/@/components/iot/ScriptTemplatePicker';
 *
 *   &lt;ScriptTemplatePicker
 *     :channel-code="model.channelCode"
 *     :current-value="model[field]"
 *     @fill="(content) =&gt; (model[field] = content)"
 *   /&gt;
 * </pre>
 *
 * @author mqttsnet
 */
import ScriptTemplatePicker from './index.vue';

export { ScriptTemplatePicker };
export { getScriptTemplates, SCRIPT_TEMPLATES } from './templates';
export type { ScriptTemplateItem } from './templates';
