<template>
  <span
    class="ors-badge"
    :class="`ors-badge--${statusKey}`"
    :style="{ width: `${size}px`, height: `${size}px`, borderRadius: `${radius}px` }"
  >
    <component :is="icon" :style="{ fontSize: `${iconSize}px` }" />
  </span>
</template>

<script lang="ts" setup>
  /**
   * OtaRecordStatusBadge ── OTA 升级记录状态徽标(项目通用)
   *
   * <p>与「设备详情指标卡」同款 flexy 图标:圆角方块 + 渐变底 + 白色 Ant 线性图标,
   * 配色取 flexy 调性(蓝 / 琥珀 / 青 / 绿 / 红),用于升级记录的汇总卡、卡片列表与详情页,语义一致。</p>
   *
   * @author mqttsnet
   */
  import { computed, type Component } from 'vue';
  import {
    AppstoreOutlined,
    ClockCircleOutlined,
    SyncOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
  } from '@ant-design/icons-vue';

  defineOptions({ name: 'OtaRecordStatusBadge' });

  const props = withDefaults(
    defineProps<{
      /** 升级记录状态:0 待升级 / 1 升级中 / 2 升级成功 / 3 升级失败;或传 'total' 表示汇总总数 */
      status?: number | string | null;
      /** 徽标边长(px) */
      size?: number;
    }>(),
    { status: 'total', size: 48 },
  );

  const STATUS_KEY: Record<string, string> = {
    '0': 'pending',
    '1': 'upgrading',
    '2': 'success',
    '3': 'failed',
    total: 'total',
    pending: 'pending',
    upgrading: 'upgrading',
    success: 'success',
    failed: 'failed',
  };

  const ICONS: Record<string, Component> = {
    total: AppstoreOutlined,
    pending: ClockCircleOutlined,
    upgrading: SyncOutlined,
    success: CheckCircleOutlined,
    failed: CloseCircleOutlined,
  };

  const statusKey = computed(() => STATUS_KEY[String(props.status ?? 'total')] || 'total');
  const icon = computed(() => ICONS[statusKey.value]);
  const radius = computed(() => Math.round(props.size * 0.28));
  const iconSize = computed(() => Math.round(props.size * 0.52));
</script>

<style scoped lang="less">
  .ors-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  // 配色与设备详情指标卡对齐(flexy 调性,渐变 + 同色软阴影)
  .ors-badge--total {
    background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
    box-shadow: 0 6px 14px rgba(93, 135, 255, 0.3);
  }

  .ors-badge--pending {
    background: linear-gradient(135deg, #ffae1f 0%, #ffc94a 100%);
    box-shadow: 0 6px 14px rgba(255, 174, 31, 0.3);
  }

  .ors-badge--upgrading {
    background: linear-gradient(135deg, #13c2c2 0%, #48d6f0 100%);
    box-shadow: 0 6px 14px rgba(19, 194, 194, 0.3);
  }

  .ors-badge--success {
    background: linear-gradient(135deg, #13deb9 0%, #36e6c3 100%);
    box-shadow: 0 6px 14px rgba(19, 222, 185, 0.3);
  }

  .ors-badge--failed {
    background: linear-gradient(135deg, #fa5252 0%, #ff8080 100%);
    box-shadow: 0 6px 14px rgba(250, 82, 82, 0.28);
  }

  // 进行中的状态才加动效(语义即动效):升级中 = 持续旋转,待升级 = 呼吸闪动;
  // 总数 / 成功 / 失败属于"已定状态",保持静止,避免列表里一堆图标乱动。
  .ors-badge--upgrading :deep(.anticon) {
    animation: ors-spin 1.6s linear infinite;
    transform-origin: center;
  }

  .ors-badge--pending :deep(.anticon) {
    animation: ors-breathe 1.8s ease-in-out infinite;
  }

  @keyframes ors-spin {
    to {
      transform: rotate(360deg);
    }
  }

  @keyframes ors-breathe {
    0%,
    100% {
      opacity: 1;
    }
    50% {
      opacity: 0.5;
    }
  }

  // 尊重系统"减少动态效果"偏好
  @media (prefers-reduced-motion: reduce) {
    .ors-badge--upgrading :deep(.anticon),
    .ors-badge--pending :deep(.anticon) {
      animation: none;
    }
  }
</style>
