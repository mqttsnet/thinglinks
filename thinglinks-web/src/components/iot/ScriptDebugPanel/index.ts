/**
 * ScriptDebugPanel ── 规则脚本在线调试面板(IoT 应用内通用组件)。
 *
 * <p>把"设备上行前置转换脚本"的在线调试 UI(三 Tab 输入/输出/变量 + 历史记录、设备选择、
 * 源 topic / 源报文录入、运行、输出与异常展示、binding 变量检视)整体封装为自包含组件:
 * 调用方只通过 props 传入待调试的脚本上下文,组件内部自管状态、自调 transformDebug API。
 *
 * <p>使用样例:
 * <pre>
 *   import { ScriptDebugPanel } from '/@/components/iot/ScriptDebugPanel';
 *
 *   &lt;ScriptDebugPanel
 *     :script-content="detailData.scriptContent"
 *     :product-identification="detailData.productIdentification"
 *     :topic-pattern="detailData.topicPattern"
 *     :history-key="id"
 *   /&gt;
 * </pre>
 *
 * <p>权限点 {@code rule:groovy:ruleGroovyScript:mockDebug} 控制"运行"按钮可见性。
 *
 * @author mqttsnet
 */
import ScriptDebugPanel from './index.vue';
import BindingKvTable from './BindingKvTable.vue';

export { ScriptDebugPanel, BindingKvTable };
