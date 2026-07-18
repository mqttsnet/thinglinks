<template>
  <div class="ipv-picker">
    <a-select
      v-model:value="innerValue"
      class="ipv-picker__select"
      :placeholder="resolvedPlaceholder"
      :disabled="disabled || !productIdentification"
      :loading="loading"
      :get-popup-container="(node) => node.parentNode"
      option-label-prop="label"
      show-search
      :filter-option="filterOption"
      allow-clear
      @change="onChange"
      @search="onSearch"
    >
      <a-select-option
        v-for="item in displayVersions"
        :key="item.versionNo"
        :value="item.versionNo"
        :label="item.versionNo"
      >
        <div class="ipv-opt">
          <div class="ipv-opt__head">
            <span class="ipv-opt__no">{{ item.versionNo }}</span>
            <a-tag :color="strategyColor(item)" class="ipv-opt__tag">
              {{ strategyLabel(item) }}
            </a-tag>
          </div>
          <div class="ipv-opt__meta">
            <span class="ipv-opt__time">
              <ClockCircleOutlined />
              {{
                item.publishTime
                  ? formatToDateTime(item.publishTime)
                  : t('component.iotProductVersionPicker.noPublishTime')
              }}
            </span>
            <span v-if="item.remark" class="ipv-opt__remark" :title="item.remark">{{
              item.remark
            }}</span>
          </div>
        </div>
      </a-select-option>
      <!-- 允许手动输入:键入 / 已选的自定义版本序号(不在列表)作为可提交选项 -->
      <a-select-option v-for="opt in customOptions" :key="opt" :value="opt" :label="opt">
        <span class="ipv-opt__custom">
          <EditOutlined />
          {{ t('component.iotProductVersionPicker.useCustom', { value: opt }) }}
        </span>
      </a-select-option>
    </a-select>

    <!-- 选后摘要:即使收起下拉,目标版本的状态 / 发布时间 / 备注仍可见,方便确认 -->
    <div v-if="selectedVersion" class="ipv-summary">
      <a-tag :color="strategyColor(selectedVersion)">
        {{ strategyLabel(selectedVersion) }}
      </a-tag>
      <span class="ipv-summary__item">
        {{ t('component.iotProductVersionPicker.publishTime') }}:
        {{ selectedVersion.publishTime ? formatToDateTime(selectedVersion.publishTime) : '—' }}
      </span>
      <span v-if="selectedVersion.remark" class="ipv-summary__item ipv-summary__remark">
        {{ t('component.iotProductVersionPicker.remark') }}: {{ selectedVersion.remark }}
      </span>
    </div>
    <!-- 空态提示 -->
    <div v-else-if="!productIdentification" class="ipv-tip">
      {{ t('component.iotProductVersionPicker.pickProductFirst') }}
    </div>
    <div v-else-if="!loading && displayVersions.length === 0" class="ipv-tip ipv-tip--warn">
      {{
        allowCustom
          ? t('component.iotProductVersionPicker.noVersionCustom')
          : t('component.iotProductVersionPicker.noVersion')
      }}
    </div>
  </div>
</template>

<script lang="ts" setup>
  /**
   * IotProductVersionPicker ── IoT 产品版本选择器(项目通用)
   *
   * <p>给定一个产品标识,按「发布策略」列出该产品下可绑定的版本,每个选项内联展示
   * 版本号 + 策略标签 + 发布时间 + 备注。典型用法:</p>
   * <ul>
   *   <li>OTA 升级包选目标影子版本:{@code :publish-strategies="[ProductPublishStrategyEnum.SHADOW]"}</li>
   *   <li>设备新增 / 编辑选绑定版本:全量 / 灰度 / 影子均可,默认即全部</li>
   * </ul>
   *
   * <p>仅列出「可绑定」版本(版本状态 已发布 / 灰度 / 影子,与后端切换校验口径一致),再按
   * {@code publishStrategies} 过滤发布策略。v-model 绑定 versionNo;产品变化时自动重拉并清掉旧选值。</p>
   *
   * @author mqttsnet
   */
  import { computed, ref, watch } from 'vue';
  import { ClockCircleOutlined, EditOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { formatToDateTime } from '/@/utils/dateUtil';
  import { listByProduct } from '/@/api/iot/link/productVersion/productVersion';
  import type { ProductVersionResultVO } from '/@/api/iot/link/productVersion/model/productVersionModel';
  import { ProductPublishStrategyEnum, ALL_PUBLISH_STRATEGIES } from '/@/enums/link/productVersion';

  defineOptions({ name: 'IotProductVersionPicker' });

  const props = withDefaults(
    defineProps<{
      /** 选中的版本序号(versionNo) */
      modelValue?: string | null;
      /** 所属产品标识;为空时禁用并提示先选产品 */
      productIdentification?: string | null;
      disabled?: boolean;
      /** 允许选择的发布策略集合(对齐后端 ProductPublishStrategyEnum:0全量 / 1灰度 / 2影子);默认三种全支持 */
      publishStrategies?: number[];
      /** 允许手动输入版本序号(键入不在列表的值也可提交);默认仅可从列表选择 */
      allowCustom?: boolean;
    }>(),
    {
      modelValue: '',
      productIdentification: '',
      disabled: false,
      publishStrategies: () => [...ALL_PUBLISH_STRATEGIES],
      allowCustom: false,
    },
  );

  const emit = defineEmits<{
    (e: 'update:modelValue', val: string): void;
    (e: 'change', val: string, record?: ProductVersionResultVO): void;
  }>();

  const { t } = useI18n();

  const loading = ref(false);
  const versions = ref<ProductVersionResultVO[]>([]);
  // 当前下拉搜索关键字 ── allowCustom 时用于派生「使用自定义版本序号」选项
  const keyword = ref('');

  const innerValue = computed<string>({
    get: () => props.modelValue || undefined,
    set: (v) => emit('update:modelValue', v || ''),
  });

  // 仅「可绑定」版本可作为目标:版本状态 已发布(1)/ 灰度(2)/ 影子(3),与后端 assertSwitchableTargetVersion 一致
  const BINDABLE_STATUSES = [1, 2, 3];

  /** 版本的发布策略;历史 / 缺省数据从版本状态兜底推导(影子状态→影子策略,灰度→灰度,其余→全量)。 */
  function effectiveStrategy(v: ProductVersionResultVO): number {
    if (v.publishStrategy != null) return v.publishStrategy;
    if (v.versionStatus === 3) return ProductPublishStrategyEnum.SHADOW;
    if (v.versionStatus === 2) return ProductPublishStrategyEnum.CANARY;
    return ProductPublishStrategyEnum.FULL;
  }

  /** 先按「可绑定状态」收口,再按发布策略过滤 */
  const displayVersions = computed<ProductVersionResultVO[]>(() => {
    const strategies = props.publishStrategies?.length
      ? props.publishStrategies
      : ALL_PUBLISH_STRATEGIES;
    return versions.value.filter(
      (v) =>
        v.versionStatus != null &&
        BINDABLE_STATUSES.includes(v.versionStatus) &&
        strategies.includes(effectiveStrategy(v)),
    );
  });

  const selectedVersion = computed<ProductVersionResultVO | undefined>(() =>
    versions.value.find((v) => v.versionNo === props.modelValue),
  );

  /** allowCustom 时的「自定义」可提交项:已选的手填值 + 正在键入的关键字(均需不在可选列表中)。
   *  保留已选值的选项,避免选完后下拉项消失导致回显丢失。 */
  const customOptions = computed<string[]>(() => {
    if (!props.allowCustom) return [];
    const set = new Set<string>();
    const inList = (val: string) => displayVersions.value.some((v) => v.versionNo === val);
    const mv = (props.modelValue || '').trim();
    const kw = keyword.value.trim();
    if (mv && !inList(mv)) set.add(mv);
    if (kw && !inList(kw)) set.add(kw);
    return [...set];
  });

  const resolvedPlaceholder = computed<string>(() =>
    props.productIdentification
      ? t('component.iotProductVersionPicker.placeholder')
      : t('component.iotProductVersionPicker.pickProductFirst'),
  );

  // 发布策略 → i18n key / 标签颜色(全量=绿 / 灰度=橙 / 影子=蓝)
  const STRATEGY_KEY: Record<number, string> = {
    [ProductPublishStrategyEnum.FULL]: 'full',
    [ProductPublishStrategyEnum.CANARY]: 'canary',
    [ProductPublishStrategyEnum.SHADOW]: 'shadow',
  };
  const STRATEGY_COLOR: Record<number, string> = {
    [ProductPublishStrategyEnum.FULL]: 'green',
    [ProductPublishStrategyEnum.CANARY]: 'orange',
    [ProductPublishStrategyEnum.SHADOW]: 'geekblue',
  };

  function strategyLabel(v: ProductVersionResultVO): string {
    const key = STRATEGY_KEY[effectiveStrategy(v)];
    return key ? t(`component.iotProductVersionPicker.strategy.${key}`) : '';
  }

  function strategyColor(v: ProductVersionResultVO): string {
    return STRATEGY_COLOR[effectiveStrategy(v)] || 'default';
  }

  /** 按 版本号 / 备注 模糊过滤 */
  function filterOption(input: string, option: any): boolean {
    const kw = String(input ?? '').trim();
    // 「使用自定义」项(value = 当前关键字)不参与过滤,始终展示
    if (props.allowCustom && kw && option?.value === kw) return true;
    const target = versions.value.find((v) => v.versionNo === option?.value);
    const haystack = `${target?.versionNo ?? ''} ${target?.remark ?? ''}`.toLowerCase();
    return haystack.includes(kw.toLowerCase());
  }

  function onSearch(val: string) {
    keyword.value = val ?? '';
  }

  function onChange(val: string) {
    emit(
      'change',
      val,
      versions.value.find((v) => v.versionNo === val),
    );
  }

  async function loadVersions(productIdentification?: string | null) {
    if (!productIdentification) {
      versions.value = [];
      return;
    }
    loading.value = true;
    try {
      const list = await listByProduct(productIdentification);
      versions.value = Array.isArray(list) ? list : [];
    } catch {
      versions.value = [];
    } finally {
      loading.value = false;
    }
    // 旧选值若不再属于当前产品的可选版本,清空,避免脏数据提交;allowCustom 时保留(可能是手填值)
    if (
      !props.allowCustom &&
      props.modelValue &&
      !displayVersions.value.some((v) => v.versionNo === props.modelValue)
    ) {
      emit('update:modelValue', '');
    }
  }

  watch(
    () => props.productIdentification,
    (val) => loadVersions(val),
    { immediate: true },
  );
</script>

<style lang="less" scoped>
  .ipv-picker {
    width: 100%;

    &__select {
      width: 100%;
    }
  }

  .ipv-opt {
    padding: 2px 0;

    &__custom {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      color: @primary-color;
      font-size: 13px;
    }

    &__head {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    &__no {
      font-weight: 600;
    }

    &__tag {
      margin-inline-end: 0;
    }

    &__meta {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-top: 2px;
      color: rgba(0, 0, 0, 0.45);
      font-size: 12px;
    }

    &__time {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      white-space: nowrap;
    }

    &__remark {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .ipv-summary {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px 14px;
    margin-top: 6px;
    color: rgba(0, 0, 0, 0.55);
    font-size: 12px;

    &__remark {
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .ipv-tip {
    margin-top: 6px;
    color: rgba(0, 0, 0, 0.4);
    font-size: 12px;

    &--warn {
      color: #d4880b;
    }
  }
</style>
