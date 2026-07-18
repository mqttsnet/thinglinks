<template>
  <!--
    属性详情抽屉(Flexy 风格)。

    产品视角的信息层次:
      1. Hero ── 用户最关心:"现在值是多少?"(大字号 + 单位 + 趋势)+ "这属性是什么?"(icon + name + code)
      2. Schema 卡 ── 元数据定义(数据类型/读写方法/范围/单位),配色按语义分组
      3. 描述卡 ── 业务文本说明
      4. 历史卡 ── 时间窗筛选 + 数据表(越界值高亮警告色)

    交互:
      - propertyCode 可复制(运维查日志 / 配告警常用)
      - 越界数值表格高亮 ── min/max 边界一眼可见
      - 空态友好(无数据时显式提示)
  -->
  <BasicDrawer
    v-bind="$attrs"
    width="780px"
    @register="register"
    :title="t('common.title.details')"
    class="property-detail-drawer"
  >
    <!-- ===== Hero 卡 ===== -->
    <div class="hero-card">
      <div class="hero-icon">
        <img v-if="iconUrl" :src="iconUrl" :alt="meta.propertyName" />
        <DatabaseOutlined v-else />
      </div>
      <div class="hero-body">
        <div class="hero-name">
          <span class="text">{{ meta.propertyName || '-' }}</span>
          <a-tag v-if="meta.datatype" color="blue" class="dt-tag">{{ meta.datatype }}</a-tag>
          <!-- 读写方法 ── 走通用 PropertyMethodBadge,语义化图标 + 颜色 + tooltip -->
          <PropertyMethodBadge v-if="meta.method" :method="meta.method" />
        </div>
        <div class="hero-code">
          <span class="label">propertyCode</span>
          <span class="code">{{ meta.propertyCode || '-' }}</span>
          <a-tooltip v-if="meta.propertyCode" :title="t('common.title.copy')">
            <CopyOutlined class="copy-icon" @click="copyCode" />
          </a-tooltip>
        </div>
      </div>
      <!-- 当前值 ── 有值时大字号高亮,空值显示友好提示而非孤零零的 `-` -->
      <div class="hero-value" v-if="hasCurrentValue">
        <span class="num">{{ currentValueText }}</span>
        <span v-if="meta.unit" class="unit">{{ meta.unit }}</span>
      </div>
      <div class="hero-value-empty" v-else>
        <DatabaseOutlined class="empty-icon" />
        <span>{{ t('iot.link.device.device.running.noValueYet') || '暂无上报' }}</span>
      </div>
    </div>

    <!-- ===== 元数据卡 ===== -->
    <div class="meta-section">
      <div class="section-title">
        <span class="title-bar" style="background: #5d87ff" />
        {{ t('iot.link.device.device.running.basicMeta') }}
      </div>
      <div class="meta-grid">
        <div class="meta-item">
          <div class="label">{{ t('iot.link.device.device.running.propertyName') }}</div>
          <div class="value">{{ meta.propertyName || '-' }}</div>
        </div>
        <div class="meta-item">
          <div class="label">{{ t('iot.link.device.device.running.unit') }}</div>
          <div class="value">{{ meta.unit || '-' }}</div>
        </div>
        <div v-if="meta.min != null && meta.min !== ''" class="meta-item">
          <div class="label">{{ t('iot.link.device.device.running.min') }}</div>
          <div class="value mono">{{ meta.min }}</div>
        </div>
        <div v-if="meta.max != null && meta.max !== ''" class="meta-item">
          <div class="label">{{ t('iot.link.device.device.running.max') }}</div>
          <div class="value mono">{{ meta.max }}</div>
        </div>
        <div v-if="meta.step != null && meta.step !== ''" class="meta-item">
          <div class="label">{{ t('iot.link.device.device.running.step') }}</div>
          <div class="value mono">{{ meta.step }}</div>
        </div>
        <div v-if="meta.maxlength != null && meta.maxlength !== ''" class="meta-item">
          <div class="label">{{ t('iot.link.device.device.running.maxLength') }}</div>
          <div class="value mono">{{ meta.maxlength }}</div>
        </div>
        <div v-if="meta.enumlist" class="meta-item is-full">
          <div class="label">{{ t('iot.link.device.device.running.enumList') }}</div>
          <div class="value mono">{{ meta.enumlist }}</div>
        </div>
      </div>
    </div>

    <!-- ===== 描述卡(有内容才显示) ===== -->
    <div v-if="meta.description || meta.remark" class="meta-section">
      <div class="section-title">
        <span class="title-bar" style="background: #8c97a5" />
        {{ t('iot.link.device.device.running.description') }}
      </div>
      <div v-if="meta.description" class="desc-text">{{ meta.description }}</div>
      <div v-if="meta.remark" class="desc-text remark">{{ meta.remark }}</div>
    </div>

    <!-- ===== 趋势图卡(仅数值型属性显示;string / 标识符等非数值不画趋势,避免对字符串做无意义的数值统计) ===== -->
    <div v-if="isNumericProperty && echoList.length" class="meta-section">
      <div class="section-title">
        <span class="title-bar" style="background: #fa896b" />
        {{ t('iot.link.device.device.running.trend') }}
      </div>
      <PropertyTrendChart
        :data="echoList"
        :propertyCode="meta.propertyCode"
        :propertyName="meta.propertyName"
        :unit="meta.unit"
        :min="meta.min"
        :max="meta.max"
        :height="260"
      />
    </div>

    <!-- ===== 历史数据卡 ===== -->
    <div class="meta-section history-section">
      <div class="section-title">
        <span class="title-bar" style="background: #13c2c2" />
        {{ t('iot.link.device.device.running.historyData') }}
        <a-tag v-if="echoList.length" class="count-tag">{{ echoList.length }}</a-tag>
        <!-- 实时模式开关 ── parent 未 provide WS 时禁用 -->
        <div class="realtime-toggle" v-if="stream">
          <span class="live-dot" :class="{ on: isLive }" />
          <span class="live-label">
            {{ realtimeMode
              ? t('iot.link.device.device.running.live')
              : t('iot.link.device.device.running.realtimeOff') }}
          </span>
          <a-tooltip :title="t('iot.link.device.device.running.realtimeTip')">
            <a-switch
              v-model:checked="realtimeMode"
              size="small"
              @change="onRealtimeChange"
            />
          </a-tooltip>
        </div>
      </div>
      <div class="history-toolbar" ref="rangePickerWrap">
        <a-range-picker
          v-model:value="timeValue"
          show-time
          format="YYYY/MM/DD HH:mm:ss"
          valueFormat="YYYY/MM/DD HH:mm:ss"
          :presets="rangePresets"
          :getPopupContainer="getPopupContainerRef"
          :disabled="realtimeMode"
          style="flex: 1"
        />
        <a-button type="primary" :disabled="realtimeMode" @click="onRangeSearch">
          <template #icon><SearchOutlined /></template>
          {{ t('common.title.query') }}
        </a-button>
        <a-button :disabled="realtimeMode" @click="resetTime">
          <template #icon><RedoOutlined /></template>
          {{ t('iot.link.device.device.running.resetLast10') }}
        </a-button>
      </div>

      <div class="history-table">
        <a-empty
          v-if="!loading && !echoList.length"
          :description="t('iot.link.device.device.running.emptyHistory')"
          :image="emptyImage"
        />
        <!-- 分页 ── 数据量大时(几百~几千)防表格卡顿;BasicTable 分页是前端切片,数据已全拉 -->
        <!-- canResize 关闭:表格按自然高度展开,抽屉 body 作唯一滚动容器;否则表格内层滚动 +
             ResizeObserver 会在父级每 5s 刷新时重算高度,把滚动位置弹回顶部 -->
        <BasicTable
          v-else
          :columns="columns"
          :dataSource="echoList"
          :canResize="false"
          :loading="loading"
          :striped="true"
          :bordered="true"
          :pagination="{ pageSize: 20, showSizeChanger: true, showQuickJumper: true, pageSizeOptions: ['10','20','50','100'] }"
          showTableSetting
        />
      </div>
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { useI18n } from '/@/hooks/web/useI18n';
  import { computed, h, inject, ref, watch, type Ref } from 'vue';
  import dayjs from 'dayjs';
  import { queryDeviceShadow } from '/@/api/iot/link/deviceShadow/deviceShadow';
  import { findUrlById } from '/@/api/thinglinks/file/upload';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import {
    SearchOutlined,
    DatabaseOutlined,
    CopyOutlined,
    RedoOutlined,
  } from '@ant-design/icons-vue';
  import { Empty, Switch, Tag, Tooltip } from 'ant-design-vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTable } from '/@/components/Table';
  import { PropertyMethodBadge, PropertyTrendChart } from '/@/components/iot';
  import { getRunningDetailColumns } from './runningDetailSchema';

  const { t } = useI18n();

  /** 属性元数据 + 上下文(deviceIdentification / serviceCode / versionNo / icon fileId) */
  const meta = ref<any>({});
  const echoList = ref<any[]>([]);
  const loading = ref(false);
  const timeValue = ref<string[]>([]);
  /** icon 真实 URL ── propertyMetadata.icon 是 fileId,通过 findUrlById 拿 url(可能没有) */
  const iconUrl = ref<string>('');

  /**
   * 实时滚动:开启后从 parent 的 WS 流(provide('deviceShadowStream'))拿到最新一帧,
   * 提取本属性当前值 → append 到 echoList 头部,趋势组件 watch points 自动重绘。
   *
   * 设计要点:
   *  - 不重连 WS;复用 parent 已有连接,避免双倍 connection / 多端竞态
   *  - parent 未 provide(组件被独立挂载)时 stream 为 null,开关直接禁用
   *  - 按 ts 去重防快照重复;maxBufferPoints 防内存增长
   *  - 开启时禁用时间选择器,提示"实时模式"
   *  - 关闭时回到普通模式,需手动 onRangeSearch 或 resetTime 刷一次,把 buffer 还原成真实历史
   */
  const stream = inject<{
    latestShadowMessage: Ref<any>;
    socketIsSuccess: Ref<boolean>;
  } | null>('deviceShadowStream', null);
  const realtimeMode = ref(false);
  const MAX_REALTIME_POINTS = 200;
  const isLive = computed(() => realtimeMode.value && !!stream?.socketIsSuccess.value);

  watch(
    () => stream?.latestShadowMessage.value,
    (msg) => {
      if (!realtimeMode.value || !msg) return;
      const ctx = meta.value;
      if (!ctx?.propertyCode) return;
      // 找当前 service / 当前 property,严格匹配避免脏数据
      const svc = msg.services?.find?.((s: any) => s.serviceCode === ctx.serviceCode);
      const prop = svc?.properties?.find?.((p: any) => p.propertyCode === ctx.propertyCode);
      if (!prop) return;
      const ts = prop.createdTime || dayjs().format('YYYY-MM-DD HH:mm:ss.SSS');
      // 头部 ts 一致视为重复(后端连续推同一帧不应再 append)
      if (echoList.value[0]?.ts === ts) return;
      echoList.value = [
        { [ctx.propertyCode]: prop.propertyValue, ts },
        ...echoList.value,
      ].slice(0, MAX_REALTIME_POINTS);
    },
  );

  /** 切实时模式开关 ── 关掉时显式 reset 回普通模式,把 buffer 换回真历史 */
  function onRealtimeChange(checked: boolean) {
    if (!checked) {
      // 关闭实时 → 回到"最近 10 分钟"基线
      resetTime();
    }
    // 开启时不立刻清空,保留已有历史给用户做"过去 + 实时"无缝衔接
  }

  const emptyImage = Empty.PRESENTED_IMAGE_SIMPLE;

  // 通过 Description / Tag / Tooltip / Switch 显式注册避免 TabPane 之类的全局注册依赖
  const _components = { Tag, Tooltip, Switch };
  void _components;

  const [register] = useDrawerInner(async (data) => {
    meta.value = data || {};
    iconUrl.value = '';
    realtimeMode.value = false; // 切属性默认回到非实时,避免上一个属性的 buffer 污染
    resetTime();
    // icon fileId → url(若属性配了 icon)
    if (meta.value?.icon) {
      try {
        const res = await findUrlById([meta.value.icon]);
        iconUrl.value = res?.[meta.value.icon] || '';
      } catch {
        iconUrl.value = '';
      }
    }
  });

  /** 是否有上报值 ── Hero 区据此决定走大字号 / 空态提示 */
  const hasCurrentValue = computed(() => {
    const v = meta.value?.propertyValue;
    return v != null && v !== '';
  });

  /**
   * 是否数值型属性 ── 仅数值型才显示趋势图 / 数值统计。
   * 白名单与物模型 datatype 定义对齐(数值范围 min/max/step 仅对 int/decimal 生效);
   * 大小写不敏感 + 容错常见别名,避免把 string(如设备ID,可能含前导 0)当数值做 min/max/平均。
   */
  const NUMERIC_DATATYPES = ['int', 'integer', 'decimal', 'float', 'double', 'number'];
  const isNumericProperty = computed(() => {
    const dt = String(meta.value?.datatype || '').toLowerCase();
    return NUMERIC_DATATYPES.includes(dt);
  });

  /** 当前属性值文本 ── 优先 propertyValue,空时 '-' */
  const currentValueText = computed(() => {
    const v = meta.value?.propertyValue;
    if (v == null || v === '') return '-';
    if (typeof v === 'object') {
      try {
        return JSON.stringify(v);
      } catch {
        return String(v);
      }
    }
    return String(v);
  });

  /**
   * 表格列 ── 主列加越界高亮(数值型属性且配了 min/max,值越界时 cell 红字提示)。
   * 历史 getRunningDetailColumns 已带"propertyCode 复制 + tooltip",在此基础上扩展。
   */
  const columns = computed(() => {
    const propertyCode = meta.value?.propertyCode || '';
    const propertyName = meta.value?.propertyName || '';
    const unit = meta.value?.unit;
    const min = parseFloat(meta.value?.min);
    const max = parseFloat(meta.value?.max);
    const hasRange = !isNaN(min) || !isNaN(max);

    const base = getRunningDetailColumns({
      title: `${propertyName}${unit ? `(${unit})` : ''}`,
      dataIndex: propertyCode,
    });
    if (!hasRange) return base;
    // 包装主列 customRender 加越界判断:数值越 min/max 时红色高亮
    return base.map((col, idx) => {
      if (idx !== 0) return col;
      const inner = col.customRender;
      return {
        ...col,
        customRender: (params: any) => {
          const node = typeof inner === 'function' ? inner(params) : params.value;
          const num = parseFloat(params?.value);
          const out =
            !isNaN(num) &&
            ((!isNaN(min) && num < min) || (!isNaN(max) && num > max));
          if (!out) return node;
          return h(
            'span',
            {
              style: { color: '#d03b5b', fontWeight: 600 },
              title: t('iot.link.device.device.running.outOfRange'),
            },
            [node],
          );
        },
      };
    });
  });

  const rangePickerWrap = ref(null);
  const getPopupContainerRef = () => rangePickerWrap.value;

  const rangePresets = ref([
    { label: t('iot.link.device.device.running.last10Min'), value: [dayjs().add(-10, 'm'), dayjs()] },
    { label: t('iot.link.device.device.running.last1Hour'), value: [dayjs().add(-1, 'h'), dayjs()] },
    { label: t('iot.link.device.device.running.last1Day'), value: [dayjs().add(-1, 'd'), dayjs()] },
    { label: t('iot.link.device.device.running.last7Day'), value: [dayjs().add(-7, 'd'), dayjs()] },
  ]);

  const getLastTenMinutes = () => {
    const now = Date.now();
    return { startTime: now - 10 * 60 * 1000, endTime: now };
  };

  /**
   * 拉历史数据 ── queryDeviceShadow 带时间窗 + serviceCode,从 res.services 严格匹配
   * 当前 serviceCode 的 echoList(防 race 取错)。
   */
  const getDetailInfo = async () => {
    loading.value = true;
    try {
      const { deviceIdentification, serviceCode, versionNo } = meta.value || {};
      const params: any = {
        deviceIdentification,
        serviceCode,
        versionNo,
      };
      if (timeValue.value?.length === 2) {
        params.startTime = dayjs(timeValue.value[0]).valueOf() * 1_000_000;
        params.endTime = dayjs(timeValue.value[1]).valueOf() * 1_000_000;
      }
      const res = await queryDeviceShadow(params);
      const matched = (res as any)?.services?.find?.((i: any) => i.serviceCode === serviceCode);
      echoList.value = matched?.echoList || [];
    } catch (e) {
      console.warn('[runningDetail] 历史数据加载失败', e);
      echoList.value = [];
    } finally {
      loading.value = false;
    }
  };

  const onRangeSearch = () => {
    if (timeValue.value?.length === 2) {
      getDetailInfo();
    }
  };

  const resetTime = () => {
    const { startTime, endTime } = getLastTenMinutes();
    timeValue.value = [
      dayjs(startTime).format('YYYY/MM/DD HH:mm:ss'),
      dayjs(endTime).format('YYYY/MM/DD HH:mm:ss'),
    ];
    getDetailInfo();
  };

  /**
   * 复制 propertyCode ── handleCopyTextV2 内部已经弹 message.success,
   * 这里只 await 即可,不要再调 createMessage.success,否则会弹两个 toast。
   */
  async function copyCode() {
    if (!meta.value?.propertyCode) return;
    await handleCopyTextV2(meta.value.propertyCode);
  }
</script>

<style lang="less" scoped>
  .property-detail-drawer {
    :deep(.ant-drawer-body) {
      padding: 0;
      background: linear-gradient(180deg, #f7f9ff 0%, #ffffff 60%);
      /* 关键:锁滚动在 drawer 内 ── 否则内容未溢出时 wheel 事件穿透 mask 到底层页面 */
      overflow-y: auto;
      overscroll-behavior: contain;
    }
  }

  /* ===== Hero 卡 ===== */
  .hero-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px 24px;
    background: linear-gradient(135deg, #eef3ff 0%, #e6efff 100%);
    border-bottom: 1px solid #eef1f7;

    .hero-icon {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 26px;
      flex-shrink: 0;
      box-shadow: 0 6px 18px rgba(93, 135, 255, 0.32);
      overflow: hidden;
      padding: 6px;

      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }

    .hero-body {
      flex: 1;
      min-width: 0;

      .hero-name {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;

        .text {
          font-size: 17px;
          font-weight: 700;
          color: #2a3547;
        }

        .dt-tag {
          margin-right: 0;
          font-weight: 500;
        }
      }

      .hero-code {
        margin-top: 6px;
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: #97a1b0;

        .label {
          color: #97a1b0;
        }
        .code {
          color: #2a3547;
          font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
          font-weight: 600;
        }
        .copy-icon {
          color: #97a1b0;
          cursor: pointer;
          transition: color 0.18s ease;

          &:hover {
            color: #5d87ff;
          }
        }
      }
    }

    .hero-value {
      text-align: right;
      display: flex;
      align-items: baseline;
      gap: 6px;
      flex-shrink: 0;

      .num {
        font-size: 30px;
        font-weight: 700;
        color: #2a3547;
        font-variant-numeric: tabular-nums;
        word-break: break-all;
        max-width: 240px;
        line-height: 1.1;
      }
      .unit {
        font-size: 14px;
        color: #97a1b0;
      }
    }

    /* 空值态 ── 不再用大字号孤零零的 `-`,改为带 icon 的小灰提示 */
    .hero-value-empty {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      flex-shrink: 0;
      padding: 6px 12px;
      border-radius: 10px;
      background: rgba(151, 161, 176, 0.12);
      color: #97a1b0;
      font-size: 13px;

      .empty-icon {
        font-size: 14px;
      }
    }
  }

  /* ===== 公共 section 卡 ===== */
  .meta-section {
    margin: 16px 24px;
    padding: 14px 18px;
    background: #fff;
    border: 1px solid #eef1f7;
    border-radius: 12px;

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      font-weight: 600;
      color: #2a3547;
      margin-bottom: 12px;

      .title-bar {
        width: 3px;
        height: 14px;
        border-radius: 2px;
      }

      .count-tag {
        margin-left: auto;
        margin-right: 0;
      }
    }
  }

  .meta-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 14px 22px;

    .meta-item {
      min-width: 0;

      &.is-full {
        grid-column: 1 / -1;
      }

      .label {
        font-size: 12px;
        color: #97a1b0;
        margin-bottom: 4px;
      }

      .value {
        font-size: 14px;
        font-weight: 500;
        color: #2a3547;
        word-break: break-all;

        &.mono {
          font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        }
      }
    }
  }

  .desc-text {
    font-size: 13px;
    color: #4a5568;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-all;

    &.remark {
      margin-top: 8px;
      padding-top: 8px;
      border-top: 1px dashed #eef1f7;
      color: #6b7280;
    }
  }

  /* ===== 历史卡 ===== */
  .history-section {
    .history-toolbar {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
    }

    .history-table {
      :deep(.ant-empty) {
        padding: 28px 0;
      }
    }

    /* 实时模式开关 ── 靠右挤进 section-title */
    .realtime-toggle {
      margin-left: auto;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #97a1b0;

      .live-label {
        line-height: 1;
      }

      .live-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #d4d8e0;
        transition: background 0.2s ease;

        &.on {
          background: #20c997;
          box-shadow: 0 0 0 0 rgba(32, 201, 151, 0.55);
          animation: livePulse 1.6s ease-out infinite;
        }
      }
    }
  }

  @keyframes livePulse {
    0% {
      box-shadow: 0 0 0 0 rgba(32, 201, 151, 0.55);
    }
    70% {
      box-shadow: 0 0 0 8px rgba(32, 201, 151, 0);
    }
    100% {
      box-shadow: 0 0 0 0 rgba(32, 201, 151, 0);
    }
  }
</style>
