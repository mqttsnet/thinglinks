<template>
  <!--
    属性读写方法徽章 ── 把物模型 method(r/w/rw)语义可视化。
    用户场景:在属性 chip / 列表里一眼区分"只读监控值"、"可写控制值"、"双向值"。
    设计:不依赖业务上下文,纯展示组件;空值不渲染。

    用法:
      - 详情/弹窗等空间充裕场景: size="md"(默认),带文字
      - 卡片/表格密集场景:       size="sm" + iconOnly,只展示一个 icon
  -->
  <a-tooltip v-if="info" :title="info.tooltip">
    <span class="prop-method-badge" :class="[info.cls, `size-${size}`, { 'icon-only': iconOnly }]">
      <component :is="info.icon" class="icon" />
      <span v-if="!iconOnly" class="text">{{ info.text }}</span>
    </span>
  </a-tooltip>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { Tooltip } from 'ant-design-vue';
  import {
    EyeOutlined,
    EditOutlined,
    SwapOutlined,
    QuestionOutlined,
  } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';

  defineOptions({ name: 'PropertyMethodBadge' });

  // a-tooltip 显式持有避免 tree-shake 警告
  const _components = { Tooltip };
  void _components;

  const props = withDefaults(
    defineProps<{
      /** 物模型 method:r=只读 / w=只写 / rw=读写;大小写无关,空值不渲染 */
      method?: string | null;
      /** 只展示 icon 不带文字(用在密集场景如 chip 内) */
      iconOnly?: boolean;
      /** 尺寸 sm(密集卡片) / md(默认 详情弹窗) */
      size?: 'sm' | 'md';
    }>(),
    {
      method: '',
      iconOnly: false,
      size: 'md',
    },
  );

  const { t } = useI18n();

  /**
   * method 归一化 + 映射 ── 容错处理:
   *  - 兼容后端常见简写:r / re / read / ro / r/o → READ;w / wr / write / wo / w/o → WRITE;
   *    rw / wr (有时也代表 read+write) / readwrite / read_write / read/write / r/w → R/W
   *  - 大小写无关,会去掉 / _ 空格
   *  - 空值返回 null(组件不渲染),未识别值用 QuestionOutlined 占位避免静默
   *
   *  注:'wr' 在不同后端语义不一(可能写,也可能读写)。采用社区惯例 wr=write。
   *  如果某些产品语义把 wr 当作 read-write,可以在后端归一化或这里再调整。
   */
  const info = computed(() => {
    const raw = (props.method || '').toString().trim().toLowerCase().replace(/[\s/_-]/g, '');
    if (!raw) return null;
    const READ_SET = new Set(['r', 're', 'read', 'ro']);
    const WRITE_SET = new Set(['w', 'wr', 'write', 'wo']);
    const RW_SET = new Set(['rw', 'readwrite', 'rdwr', 'readandwrite']);

    // 能力位串归一:method 本质是 r(读)/w(写)/e(事件) 的能力位组合,
    // 平台默认值即 'RWE'(见 productProperty 表单 defaultValue + ACCESS_MODE 字典)。
    // 纯 [rwe] 组合(如 rwe / rew / we / re)按是否含 r、w 归到 读写/只读/只写,
    // e(event)维度并入主能力展示,不单独造类型 → 避免 rwe 这类合法值被判"未识别"。
    let normalized = raw;
    if (/^[rwe]+$/.test(raw) && !READ_SET.has(raw) && !WRITE_SET.has(raw) && !RW_SET.has(raw)) {
      const hasR = raw.includes('r');
      const hasW = raw.includes('w');
      normalized = hasR && hasW ? 'rw' : hasW ? 'w' : 'r';
    }

    if (READ_SET.has(normalized)) {
      return {
        cls: 'm-r',
        icon: EyeOutlined,
        text: t('component.iotProp.methodRead') || '只读',
        tooltip: t('component.iotProp.methodReadTip') || '只读 ── 设备上报 / 平台查询',
      };
    }
    if (WRITE_SET.has(normalized)) {
      return {
        cls: 'm-w',
        icon: EditOutlined,
        text: t('component.iotProp.methodWrite') || '只写',
        tooltip: t('component.iotProp.methodWriteTip') || '只写 ── 平台下发 / 设备执行',
      };
    }
    if (RW_SET.has(normalized)) {
      return {
        cls: 'm-rw',
        icon: SwapOutlined,
        text: t('component.iotProp.methodReadWrite') || '读写',
        tooltip: t('component.iotProp.methodReadWriteTip') || '读写 ── 双向交互',
      };
    }
    return {
      cls: 'm-unknown',
      icon: QuestionOutlined,
      text: raw,
      tooltip: t('component.iotProp.methodUnknownTip') || '未识别的 method 值',
    };
  });
</script>

<style lang="less" scoped>
  .prop-method-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    border-radius: 10px;
    font-weight: 500;
    border: 1px solid transparent;
    line-height: 1;

    /* md 默认 ── 详情弹窗用 */
    &.size-md {
      padding: 2px 8px;
      font-size: 12px;
      line-height: 18px;

      .icon {
        font-size: 11px;
      }
    }

    /* sm 小号 ── 卡片 / 表格密集场景用 */
    &.size-sm {
      padding: 1px 6px;
      font-size: 11px;
      line-height: 16px;

      .icon {
        font-size: 10px;
      }
    }

    /* icon-only 圆形,挤进卡片头部 ── 默认走当前 size 的 padding */
    &.icon-only {
      gap: 0;
      padding: 2px;
      border-radius: 50%;

      &.size-sm {
        width: 16px;
        height: 16px;
        justify-content: center;
      }
      &.size-md {
        width: 20px;
        height: 20px;
        justify-content: center;
      }
    }

    /* r 蓝 / w 橙 / rw 紫 / 未知灰 ── 高对比 + Flexy 色系 */
    &.m-r {
      color: #1a4ce0;
      background: #eef3ff;
      border-color: #d6e4ff;
    }
    &.m-w {
      color: #b4661d;
      background: #fff4e3;
      border-color: #ffe0b2;
    }
    &.m-rw {
      color: #6a3fb8;
      background: #f3ecff;
      border-color: #e0d0ff;
    }
    &.m-unknown {
      color: #97a1b0;
      background: #f4f6fb;
      border-color: #e6ebf3;
    }
  }
</style>
