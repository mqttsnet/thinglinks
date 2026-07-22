/**
 * AllOrCustomPicker ── 通用"全部 vs 自定义"卡片选择器。
 *
 * <p>使用场景:任何"对某类资源,要么匹配全部,要么自定义多选"的语义。
 * <ul>
 *   <li>桥接规则匹配产品 / 设备</li>
 *   <li>规则联动应用范围</li>
 *   <li>授权范围 / 数据权限范围</li>
 *   <li>消息推送目标</li>
 * </ul>
 *
 * <p>能力:
 * <ul>
 *   <li>v-model 两态:'all'(对齐 BizConstant.ALL)或 (string|number)[] 自定义多选</li>
 *   <li>跨页保留 ── 选中态用 Set,翻页/搜索/过滤都不丢</li>
 *   <li>反查回显 ── detailApi 让 modelValue 已存值时仍能正确显示标签</li>
 *   <li>清空操作 ── 触发器右侧 ✕ + 弹窗内"清空全部" + tag 单删</li>
 *   <li>已选面板 ── 弹窗下方持久显示已选项,展开/折叠</li>
 *   <li>多形态触发器 ── tags / count / compact</li>
 *   <li>顶部过滤项 ── select / radio,可在搜索之外按维度过滤</li>
 *   <li>卡片插槽 ── #card / #cardIcon 完全自定义渲染</li>
 *   <li>单选 / 多选切换 ── multiple=false 时选中即关闭</li>
 *   <li>maxCount 限制 + isDisabled 函数式禁用项</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-05-08
 */
export { default as AllOrCustomPicker } from './AllOrCustomPicker.vue';
export { default as AllOrCustomPickerModal } from './PickerModal.vue';
export * from './types';
