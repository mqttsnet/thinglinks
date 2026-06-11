<template>
  <AllOrCustomPicker
    v-model="innerValue"
    :title="finalTitle"
    :allValue="allValue"
    :allowAll="allowAll"
    :pageApi="devicePageApi"
    :detailApi="deviceDetailApi"
    :pageParams="finalPageParams"
    valueField="deviceIdentification"
    labelField="deviceName"
    searchField="deviceName"
    :searchPlaceholder="finalSearchPlaceholder"
    :descFields="finalDescFields"
    :filters="finalFilters"
    :triggerLabels="triggerLabels"
    headerBadgeField="nodeType"
    :headerBadgeDictType="DictEnum.LINK_DEVICE_NODE_TYPE"
    statusField="connectStatus"
    :statusOnlineValues="DEVICE_ONLINE_VALUES"
    timeField="createdTime"
    :disabled="disabled"
    @change="onChange"
    @clear="onClear"
  />
</template>

<script lang="ts" setup>
  /**
   * IotAllOrCustomDevicePicker ── IoT「全部 / 自定义设备」多选包装(项目通用)
   *
   * <p>对 {@link AllOrCustomPicker} 的薄包装,固化设备分页 API / 卡片字段(含连接状态彩色 tag)/
   * 节点徽标 / 在线状态值集合 / 顶部连接状态过滤项 等配置。
   *
   * <p>关键设计:
   * <ul>
   *   <li>支持 {@code productIdentification} prop:传入后自动作为后端过滤参数(picker 弹窗里只展示该产品下的设备)</li>
   *   <li>同时支持调用方自带 {@code pageParams} ── 二者会合并(props 优先)</li>
   *   <li>顶部内置"在线状态"过滤项(连接状态:在线/离线 二选一);可被 {@code filters} 覆盖</li>
   * </ul>
   *
   * @author mqttsnet
   */
  import { computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { DictEnum } from '/@/enums/commonEnum';
  import { useDict } from '/@/components/Dict';
  import {
    AllOrCustomPicker,
    type PickerDescField,
    type PickerFilter,
    type PickerTriggerLabels,
    type PickerValue,
  } from '/@/components/iot/AllOrCustomPicker';
  import { page as devicePage } from '/@/api/iot/link/device/device';

  defineOptions({ name: 'IotAllOrCustomDevicePicker' });

  const props = withDefaults(
    defineProps<{
      modelValue?: PickerValue;
      /** 关联产品标识(传入后 picker 自动按此过滤设备列表) */
      productIdentification?: string;
      /** 通配字面值 */
      allValue?: string;
      /** 是否允许"全部"模式 */
      allowAll?: boolean;
      /** 调用方额外固定参数(与内部 productIdentification 合并) */
      pageParams?: Record<string, any>;
      /** 禁用 */
      disabled?: boolean;
      /** 覆盖默认 modal 标题 */
      title?: string;
      /** 覆盖默认搜索 placeholder */
      searchPlaceholder?: string;
      /** 覆盖默认触发器文案 */
      triggerLabels?: PickerTriggerLabels;
      /** 覆盖默认卡片副字段 */
      descFields?: PickerDescField[];
      /** 覆盖默认顶部过滤项(默认 = 内置连接状态) */
      filters?: PickerFilter[];
    }>(),
    {
      allowAll: true,
      disabled: false,
      pageParams: () => ({}),
      triggerLabels: () => ({}),
    },
  );

  const emit = defineEmits<{
    (e: 'update:modelValue', val: PickerValue): void;
    (e: 'change', val: PickerValue): void;
    (e: 'clear'): void;
  }>();

  const { t } = useI18n();
  const { getDictLabel } = useDict();

  // ============================== v-model 双绑 ==============================
  const innerValue = computed<PickerValue>({
    get: () => props.modelValue ?? [],
    set: (v) => emit('update:modelValue', v),
  });

  function onChange(val: PickerValue) {
    emit('change', val);
  }
  function onClear() {
    emit('clear');
  }

  // ============================== 设备状态默认值 ==============================
  /** 设备连接状态命中"在线"的值集合(connectStatus: 1/'1'/'ONLINE') */
  const DEVICE_ONLINE_VALUES: Array<string | number> = [1, '1', 'ONLINE'];

  /** 合并产品过滤 + 调用方 pageParams(productIdentification 优先,确保设备过滤生效) */
  const finalPageParams = computed<Record<string, any>>(() => {
    const merged: Record<string, any> = { ...(props.pageParams || {}) };
    if (props.productIdentification) {
      merged.productIdentification = props.productIdentification;
    }
    return merged;
  });

  // ============================== Picker API 适配 ==============================
  const devicePageApi = (req: any) => {
    const model: Record<string, any> = {};
    if (req?.keyword) model.deviceName = req.keyword;
    // 顶部过滤项:连接状态
    if (
      req?.filters?.connectStatus !== undefined &&
      req.filters.connectStatus !== null
    ) {
      model.connectStatus = req.filters.connectStatus;
    }
    // 调用方 / 产品过滤透传
    if (req?.extra?.productIdentification) {
      model.productIdentification = req.extra.productIdentification;
    }
    return devicePage({
      model: model as any,
      size: req?.pageSize ?? 12,
      current: req?.pageNum ?? 1,
    } as any);
  };

  const deviceDetailApi = async (values: (string | number)[]) => {
    if (!values || values.length === 0) return [];
    try {
      const res: any = await devicePage({
        model: { deviceIdentification: String(values[0]) } as any,
        size: values.length,
        current: 1,
      } as any);
      const records = res?.records ?? [];
      const set = new Set(values.map(String));
      return records.filter((r: any) => set.has(String(r.deviceIdentification)));
    } catch {
      return [];
    }
  };

  // ============================== 默认配置 ==============================
  const defaultDescFields = computed<PickerDescField[]>(() => [
    {
      label: t('iot.link.device.device.deviceIdentification'),
      field: 'deviceIdentification',
    },
    {
      label: t('iot.link.device.device.clientId'),
      field: 'clientId',
    },
    {
      label: t('iot.link.device.device.deviceTags'),
      field: 'deviceTags',
    },
    {
      label: t('iot.link.device.device.connectStatus'),
      field: 'connectStatus',
      style: 'tag',
      formatter: (v) => getDictLabel('LINK_DEVICE_CONNECT_STATUS', String(v ?? ''), '-'),
      tagColor: (raw) => {
        const s = String(raw ?? '');
        if (s === '1') return 'green'; // ONLINE
        if (s === '2') return 'red'; // OFFLINE
        return 'default';
      },
    },
  ]);

  /** 默认顶部过滤项 ── 连接状态(在线 / 离线) */
  const defaultFilters = computed<PickerFilter[]>(() => [
    {
      field: 'connectStatus',
      label: t('iot.link.device.device.connectStatus'),
      type: 'select',
      options: [
        {
          label: getDictLabel('LINK_DEVICE_CONNECT_STATUS', '2', '在线'),
          value: '2',
        },
        {
          label: getDictLabel('LINK_DEVICE_CONNECT_STATUS', '1', '离线'),
          value: '1',
        },
      ],
      allowClear: true,
      width: 100,
    },
  ]);

  const finalTitle = computed<string>(
    () => props.title || t('component.iotDevicePicker.title'),
  );

  const finalSearchPlaceholder = computed<string>(
    () => props.searchPlaceholder || t('component.iotDevicePicker.searchPlaceholder'),
  );

  const finalDescFields = computed<PickerDescField[]>(
    () => props.descFields || defaultDescFields.value,
  );

  const finalFilters = computed<PickerFilter[]>(
    () => props.filters || defaultFilters.value,
  );
</script>
