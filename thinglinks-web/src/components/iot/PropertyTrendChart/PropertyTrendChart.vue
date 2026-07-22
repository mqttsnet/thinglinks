<template>
  <!--
    属性时序趋势组件(基于 echarts 折线图)。

    设计目标:
      - 仅当数据可解析为数值时渲染图表;非数值(JSON / 文本)自动隐藏,父组件不需要预判
      - 顶部 stats bar:采样点数 / 最小 / 最大 / 平均 / 越界数 ── 用户对时段健康度一眼判断
      - min/max 阈值线 + 越界高亮(超出阈值的点变红),沿用 runningDetail 的告警语义
      - 内置 dataZoom 滑块,鼠标可拖动选时间段,密集数据也能放大查看
      - 智能 X 轴格式化:根据数据时间跨度自动决定显示精度
      - 自适应降采样:超过 maxPoints(默认 500)时按桶取 min/max,既保性能又保峰谷形态
      - 单位 / 属性名作为 tooltip 上下文,运维查看一目了然

    复用场景:
      - 设备影子属性详情弹窗(已接入)
      - 后续:仪表盘 / 大屏 / 告警详情 单属性时序回放
  -->
  <div v-if="visible" class="prop-trend-chart">
    <div v-if="!points.length" class="empty">
      {{ t('component.iotProp.trendEmpty') || '该时间窗内暂无数值型数据' }}
    </div>
    <template v-else>
      <!-- 统计 summary bar ── 时段健康度一览 -->
      <div class="stats-bar">
        <div class="stat-item">
          <span class="label">{{ t('component.iotProp.trendStatCount') || '采样' }}</span>
          <span class="num">{{ stats.count }}</span>
          <a-tooltip
            v-if="downsampled"
            :title="t('component.iotProp.trendSampledTip') || '原始点数较多已自动降采样,峰谷仍保留'"
          >
            <span class="sub">({{ t('component.iotProp.trendSampled') || '已降采样' }})</span>
          </a-tooltip>
        </div>
        <div class="stat-item">
          <span class="label">{{ t('component.iotProp.trendStatMin') || '最小' }}</span>
          <span class="num mono">{{ formatNum(stats.min) }}{{ unitSuffix }}</span>
        </div>
        <div class="stat-item">
          <span class="label">{{ t('component.iotProp.trendStatMax') || '最大' }}</span>
          <span class="num mono">{{ formatNum(stats.max) }}{{ unitSuffix }}</span>
        </div>
        <div class="stat-item">
          <span class="label">{{ t('component.iotProp.trendStatAvg') || '平均' }}</span>
          <span class="num mono">{{ formatNum(stats.avg) }}{{ unitSuffix }}</span>
        </div>
        <div v-if="stats.outOfRange > 0" class="stat-item out">
          <span class="label">{{ t('component.iotProp.trendStatOut') || '越界' }}</span>
          <span class="num">{{ stats.outOfRange }}</span>
        </div>
        <!-- 导出 ── PNG / CSV 二选一,放最右侧 -->
        <a-dropdown
          class="export-dropdown"
          :trigger="['click']"
          placement="bottomRight"
          :getPopupContainer="getPopupContainer"
        >
          <a-tooltip :title="t('component.iotProp.trendExportTip') || '导出趋势'">
            <a-button class="export-btn" size="small" type="text">
              <template #icon><DownloadOutlined /></template>
              {{ t('component.iotProp.trendExport') || '导出' }}
            </a-button>
          </a-tooltip>
          <template #overlay>
            <a-menu @click="onExport">
              <a-menu-item key="png">
                <FileImageOutlined /> {{ t('component.iotProp.trendExportPng') || '导出 PNG' }}
              </a-menu-item>
              <a-menu-item key="csv">
                <FileExcelOutlined /> {{ t('component.iotProp.trendExportCsv') || '导出 CSV' }}
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <div ref="chartRef" class="chart-canvas" :style="{ height: `${height}px` }"></div>
    </template>
  </div>
</template>

<script lang="ts" setup>
  import { computed, nextTick, onBeforeUnmount, ref, Ref, watch } from 'vue';
  import { Dropdown, Menu, Tooltip } from 'ant-design-vue';
  import { DownloadOutlined, FileExcelOutlined, FileImageOutlined } from '@ant-design/icons-vue';
  import dayjs from 'dayjs';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { useI18n } from '/@/hooks/web/useI18n';

  defineOptions({ name: 'PropertyTrendChart' });

  // a-tooltip / dropdown / menu 显式持有避免 tree-shake 警告
  const _components = { Tooltip, Dropdown, Menu, MenuItem: Menu.Item };
  void _components;

  const props = withDefaults(
    defineProps<{
      /** 原始数据:每项需含一个属性值字段(由 propertyCode 索引)+ ts 时间字段 */
      data?: any[];
      /** 主属性字段名(BasicTable dataIndex 同款) */
      propertyCode?: string;
      /** 时间戳字段名;默认 ts(TDengine echoList 标准) */
      tsField?: string;
      /** 单位(tooltip + y 轴后缀) */
      unit?: string;
      /** 属性中文名(tooltip header) */
      propertyName?: string;
      /** 最小阈值 ── 数字字符串或 number;有值则画虚线 */
      min?: string | number | null;
      /** 最大阈值 */
      max?: string | number | null;
      /** 容器高度 px */
      height?: number;
      /**
       * 判定为"数值型"的最低占比 ── 默认 80%。
       * 解析后有效数值 ÷ 总数 ≥ 该值才认为可绘图,否则 visible=false 隐藏整个组件
       * (例如 JSON 属性 / 文本属性自动隐藏,无需父组件预判)
       */
      numericRatioThreshold?: number;
      /**
       * 显示用最大点数,超过则降采样;默认 500。
       * 数据量大时(几千点)直接渲染会卡 ── 按桶取 min/max 保峰谷形态。
       */
      maxPoints?: number;
    }>(),
    {
      data: () => [],
      propertyCode: '',
      tsField: 'ts',
      unit: '',
      propertyName: '',
      min: null,
      max: null,
      height: 260,
      numericRatioThreshold: 0.8,
      maxPoints: 500,
    },
  );

  const { t } = useI18n();
  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions, resize, getInstance } = useECharts(
    chartRef as Ref<HTMLDivElement>,
    'default',
    {
      renderer: 'canvas',
    },
  );
  const getPopupContainer = (triggerNode?: HTMLElement) =>
    (document.fullscreenElement as HTMLElement | null) ||
    triggerNode?.ownerDocument?.body ||
    document.body;

  type Point = { ts: string | number; tsMs: number; value: number };

  /** 原始有效数据点(按时间升序),用于 stats 计算和降采样输入 */
  const rawPoints = computed<Point[]>(() => {
    if (!props.data?.length || !props.propertyCode) return [];
    const list = props.data
      .map((row) => {
        const raw = row?.[props.propertyCode];
        const num = parseFloat(raw);
        if (isNaN(num)) return null;
        const ts = row?.[props.tsField] ?? row?.event_time ?? '';
        const tsMs = new Date(ts).getTime() || 0;
        return { ts, tsMs, value: num };
      })
      .filter(Boolean) as Point[];
    return list.sort((a, b) => a.tsMs - b.tsMs);
  });

  /**
   * 是否启用了降采样 ── 用户能在 stats bar 看到提示。
   * 阈值用 props.maxPoints,默认 500 ── 经验值,既保流畅又保形态。
   */
  const downsampled = computed(() => rawPoints.value.length > props.maxPoints);

  /**
   * 桶降采样:把 N 个点压缩成最多 maxPoints 个,每桶保留 min/max 两端,
   * 这样峰谷不会丢失,趋势线仍能反映异常波动。
   *
   * 选这个朴素策略而非 LTTB:实现简单 + 越界点不会被"平滑掉"。
   */
  function bucketize(arr: Point[], maxPoints: number): Point[] {
    if (arr.length <= maxPoints) return arr;
    // 每桶留 2 点(min+max),所以桶数 = maxPoints/2
    const buckets = Math.max(1, Math.floor(maxPoints / 2));
    const bucketSize = arr.length / buckets;
    const out: Point[] = [];
    for (let i = 0; i < buckets; i++) {
      const start = Math.floor(i * bucketSize);
      const end = Math.min(arr.length, Math.floor((i + 1) * bucketSize));
      if (start >= end) continue;
      let lo = arr[start];
      let hi = arr[start];
      for (let j = start + 1; j < end; j++) {
        if (arr[j].value < lo.value) lo = arr[j];
        if (arr[j].value > hi.value) hi = arr[j];
      }
      // 按时间先后顺序输出,避免连线乱
      if (lo.tsMs === hi.tsMs) out.push(lo);
      else if (lo.tsMs < hi.tsMs) out.push(lo, hi);
      else out.push(hi, lo);
    }
    return out;
  }

  /** 图表实际渲染用的点(可能已降采样) */
  const points = computed<Point[]>(() =>
    downsampled.value ? bucketize(rawPoints.value, props.maxPoints) : rawPoints.value,
  );

  /** 是否渲染整个组件 ── 非数值型属性整体隐藏 */
  const visible = computed(() => {
    if (!props.propertyCode) return false;
    if (!props.data?.length) return true; // 空数据走 empty 文案
    return rawPoints.value.length / props.data.length >= props.numericRatioThreshold;
  });

  const minNum = computed(() => {
    const v = parseFloat(props.min as any);
    return isNaN(v) ? null : v;
  });
  const maxNum = computed(() => {
    const v = parseFloat(props.max as any);
    return isNaN(v) ? null : v;
  });

  /** stats 用 raw(未降采样),保证统计结果是真实数据 */
  const stats = computed(() => {
    const arr = rawPoints.value;
    if (!arr.length) return { count: 0, min: 0, max: 0, avg: 0, outOfRange: 0 };
    let sum = 0;
    let mn = arr[0].value;
    let mx = arr[0].value;
    let out = 0;
    for (const p of arr) {
      sum += p.value;
      if (p.value < mn) mn = p.value;
      if (p.value > mx) mx = p.value;
      if (
        (minNum.value !== null && p.value < minNum.value) ||
        (maxNum.value !== null && p.value > maxNum.value)
      ) {
        out++;
      }
    }
    return {
      count: arr.length,
      min: mn,
      max: mx,
      avg: sum / arr.length,
      outOfRange: out,
    };
  });

  const unitSuffix = computed(() => (props.unit ? ` ${props.unit}` : ''));

  /** 数字格式:整数原样,小数最多 2 位,避免长尾抖动 */
  function formatNum(n: number): string {
    if (!isFinite(n)) return '-';
    if (Number.isInteger(n)) return String(n);
    return n.toFixed(2);
  }

  /**
   * 根据数据时间跨度,智能选 X 轴标签格式。
   * < 5 分钟  → HH:mm:ss
   * < 1 天    → HH:mm
   * < 30 天   → MM-DD HH:mm
   * >= 30 天  → YYYY-MM-DD
   */
  function pickTimeFormat(arr: Point[]): string {
    if (arr.length < 2) return 'HH:mm:ss';
    const span = arr[arr.length - 1].tsMs - arr[0].tsMs;
    const HOUR = 3600 * 1000;
    if (span < 5 * 60 * 1000) return 'HH:mm:ss';
    if (span < 24 * HOUR) return 'HH:mm';
    if (span < 30 * 24 * HOUR) return 'MM-DD HH:mm';
    return 'YYYY-MM-DD';
  }

  /** 构建 echarts option ── markLine 画 min/max 阈值线,越界点用 itemStyle red */
  function buildOption() {
    const fmt = pickTimeFormat(points.value);
    const xData = points.value.map((p) => (p.tsMs ? dayjs(p.tsMs).format(fmt) : String(p.ts)));
    const seriesData = points.value.map((p) => {
      const out =
        (minNum.value !== null && p.value < minNum.value) ||
        (maxNum.value !== null && p.value > maxNum.value);
      return out ? { value: p.value, itemStyle: { color: '#d03b5b' }, symbolSize: 8 } : p.value;
    });

    const markLines: any[] = [];
    if (minNum.value !== null) {
      markLines.push({
        yAxis: minNum.value,
        name: 'min',
        lineStyle: { color: '#fa896b', type: 'dashed' },
      });
    }
    if (maxNum.value !== null) {
      markLines.push({
        yAxis: maxNum.value,
        name: 'max',
        lineStyle: { color: '#fa896b', type: 'dashed' },
      });
    }

    // 数据量较大时默认仅展示最后 30% 一段,用户拖 slider 看历史
    const initialStart = points.value.length > 80 ? 70 : 0;

    return {
      grid: { left: 56, right: 24, top: 18, bottom: 50 },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any[]) => {
          const p = Array.isArray(params) ? params[0] : params;
          const valueWithUnit = `${p.value}${props.unit ? ' ' + props.unit : ''}`;
          return `<b>${props.propertyName || props.propertyCode}</b><br/>${
            p.axisValueLabel
          }<br/>${valueWithUnit}`;
        },
      },
      // 鼠标滚轮 + 滑块 ── 鼠标可缩放,移动端 / 触屏也能拖
      dataZoom: [
        { type: 'inside', start: initialStart, end: 100 },
        {
          type: 'slider',
          start: initialStart,
          end: 100,
          height: 18,
          bottom: 6,
          handleSize: 14,
          showDetail: false,
          borderColor: 'transparent',
          fillerColor: 'rgba(93, 135, 255, 0.18)',
          dataBackground: {
            lineStyle: { color: '#c9d4ff', width: 1 },
            areaStyle: { color: 'rgba(93, 135, 255, 0.12)' },
          },
        },
      ],
      xAxis: {
        type: 'category',
        data: xData,
        axisLabel: {
          fontSize: 11,
          color: '#97a1b0',
          hideOverlap: true, // echarts 自动隐藏重叠标签,密集场景不糊
        },
        axisLine: { lineStyle: { color: '#eef1f7' } },
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          fontSize: 11,
          color: '#97a1b0',
          formatter: (v: number) => `${v}${props.unit ? ' ' + props.unit : ''}`,
        },
        splitLine: { lineStyle: { color: '#f4f6fb' } },
      },
      series: [
        {
          type: 'line',
          smooth: true,
          showSymbol: false,
          sampling: 'lttb', // echarts 内置抽样兜底,极端数据量再过滤
          data: seriesData,
          lineStyle: { width: 2, color: '#5d87ff' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(93, 135, 255, 0.25)' },
                { offset: 1, color: 'rgba(93, 135, 255, 0.02)' },
              ],
            },
          },
          markLine: markLines.length
            ? { silent: true, symbol: ['none', 'none'], data: markLines, label: { fontSize: 10 } }
            : undefined,
        },
      ],
    };
  }

  // ============================ 导出 ============================

  /** 触发浏览器下载 ── 通用 helper,blob / dataURL 都走它 */
  function triggerDownload(href: string, filename: string) {
    const a = document.createElement('a');
    a.href = href;
    a.download = filename;
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  /** 文件名前缀 ── 优先 propertyCode,fallback 用 propertyName(可能含中文 ok) */
  function buildFilename(ext: string): string {
    const base = props.propertyCode || props.propertyName || 'trend';
    const ts = dayjs().format('YYYYMMDD-HHmmss');
    return `${base}-${ts}.${ext}`;
  }

  function loadImage(src: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => resolve(img);
      img.onerror = () => reject(new Error('Image load failed'));
      img.src = src;
    });
  }

  /**
   * ECharts 当前全局只注册了 SVGRenderer。getDataURL({ type: 'png' }) 在 SVG 渲染器下
   * 仍可能返回 image/svg+xml,不能直接按 .png 保存,否则 macOS 预览会认为文件损坏。
   */
  async function normalizePngDataUrl(dataUrl: string): Promise<string> {
    if (!dataUrl.startsWith('data:image/svg+xml')) {
      return dataUrl;
    }
    const img = await loadImage(dataUrl);
    const width = img.naturalWidth || chartRef.value?.clientWidth || 1;
    const height = img.naturalHeight || chartRef.value?.clientHeight || props.height || 1;
    const pixelRatio = 2;
    const canvas = document.createElement('canvas');
    canvas.width = Math.max(1, Math.round(width * pixelRatio));
    canvas.height = Math.max(1, Math.round(height * pixelRatio));
    const ctx = canvas.getContext('2d');
    if (!ctx) return dataUrl;
    ctx.scale(pixelRatio, pixelRatio);
    ctx.fillStyle = '#fff';
    ctx.fillRect(0, 0, width, height);
    ctx.drawImage(img, 0, 0, width, height);
    return canvas.toDataURL('image/png');
  }

  /** PNG ── echarts.getDataURL,高清 pixelRatio=2,白底防透明 */
  async function exportPng() {
    const inst = getInstance?.();
    if (!inst) return;
    const url = (inst as any).getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff',
    });
    if (!url.startsWith('data:image/svg+xml')) {
      triggerDownload(url, buildFilename('png'));
      return;
    }
    triggerDownload(await normalizePngDataUrl(url), buildFilename('png'));
  }

  /**
   * CSV ── 用 rawPoints(原始未降采样),保证导出数据精度真实
   * BOM 头让 Excel 正确识别 UTF-8,中文 unit / propertyName 不乱码
   */
  function exportCsv() {
    const header = ['timestamp', 'value'];
    if (props.unit) header.push('unit');
    const rows = rawPoints.value.map((p) => {
      const r: (string | number)[] = [
        dayjs(p.tsMs || new Date(p.ts as any).getTime()).format('YYYY-MM-DD HH:mm:ss.SSS'),
        p.value,
      ];
      if (props.unit) r.push(props.unit);
      return r;
    });
    const csv = [header, ...rows].map((r) => r.join(',')).join('\n');
    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    triggerDownload(url, buildFilename('csv'));
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  function onExport({ key }: { key: string }) {
    if (key === 'png') {
      exportPng();
    } else if (key === 'csv') exportCsv();
  }

  // ============================ 渲染 ============================

  // 数据 / 阈值变化时重新绘图;等 nextTick 保证 DOM 已挂载再 setOptions
  watch(
    () => [points.value, minNum.value, maxNum.value, props.unit, props.propertyName],
    async () => {
      if (!visible.value || !points.value.length) return;
      await nextTick();
      setOptions(buildOption() as any);
    },
    { immediate: true, deep: true },
  );

  // 简单 resize 监听 ── 容器宽度变化(如 drawer 展开收起)时触发 echarts.resize
  const onWinResize = () => resize();
  window.addEventListener('resize', onWinResize);
  onBeforeUnmount(() => window.removeEventListener('resize', onWinResize));
</script>

<style lang="less" scoped>
  .prop-trend-chart {
    width: 100%;

    .stats-bar {
      display: flex;
      flex-wrap: wrap;
      gap: 8px 18px;
      padding: 10px 12px;
      margin-bottom: 10px;
      background: #f7f9ff;
      border: 1px solid #eef1f7;
      border-radius: 8px;
      font-size: 12px;
      color: #4a5568;

      .stat-item {
        display: inline-flex;
        align-items: baseline;
        gap: 4px;
        line-height: 1;

        .label {
          color: #97a1b0;
        }

        .num {
          color: #2a3547;
          font-weight: 600;
          font-variant-numeric: tabular-nums;

          &.mono {
            font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
          }
        }

        .sub {
          color: #97a1b0;
          font-size: 11px;
          cursor: help;
        }

        &.out {
          .label {
            color: #b4351d;
          }
          .num {
            color: #d03b5b;
            font-weight: 700;
          }
        }
      }

      /* 导出入口靠右,用小型按钮减少裸 icon 的漂浮感 */
      .export-dropdown {
        margin-left: auto;

        .export-btn {
          height: 26px;
          padding: 0 8px;
          color: #5d87ff;
          font-size: 12px;
          border-radius: 6px;
          background: #eef3ff;
          border: 1px solid #dbe6ff;
          display: inline-flex;
          align-items: center;
          gap: 4px;
          transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;

          &:hover {
            color: #5d87ff;
            background: #e7efff;
            border-color: #b9ccff;
          }
        }
      }
    }

    .chart-canvas {
      width: 100%;
    }

    .empty {
      padding: 36px 0;
      text-align: center;
      font-size: 13px;
      color: #97a1b0;
    }
  }
</style>
