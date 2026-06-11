<template>
  <BasicEntityPicker
    v-model="innerValue"
    :multiple="multiple"
    :disabled="actuallyDisabled"
    :title="finalTitle"
    :pageApi="devicePageApi"
    :detailApi="deviceDetailApi"
    :pageParams="finalPageParams"
    valueField="deviceIdentification"
    labelField="deviceName"
    searchField="deviceName"
    :searchPlaceholder="finalSearchPlaceholder"
    :descFields="finalDescFields"
    :triggerLabels="finalTriggerLabels"
    headerBadgeField="nodeType"
    :headerBadgeDictType="DictEnum.LINK_DEVICE_NODE_TYPE"
    statusField="connectStatus"
    :statusOnlineValues="DEVICE_ONLINE_VALUES"
    timeField="createdTime"
    @change="onChange"
  />
</template>

<script lang="ts" setup>
  /**
   * IotDevicePicker ── IoT 设备选择器(项目通用)
   *
   * <p>对 {@link BasicEntityPicker} 的薄包装,固化设备分页 API / 卡片字段 / 在线状态徽标等配置,
   * 多处场景一行搞定:{@code <IotDevicePicker v-model="..." :productIdentification="..." />}。
   *
   * <p>关键约束:
   * <ul>
   *   <li>必须传 {@code productIdentification} prop 才能取到对应产品下的设备(空值时自动 disabled)</li>
   *   <li>支持外部强制 disabled(优先级高于"无产品自动 disabled")</li>
   *   <li>所有默认文案 / 字段 / 状态值都允许 props 覆盖</li>
   * </ul>
   *
   * @author mqttsnet
   */
  import { computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { DictEnum } from '/@/enums/commonEnum';
  import {
    BasicEntityPicker,
    type EntityDescField,
    type EntityTriggerLabels,
  } from '/@/components/iot/BasicEntityPicker';
  import { page as devicePage } from '/@/api/iot/link/device/device';

  defineOptions({ name: 'IotDevicePicker' });

  type DeviceValue = string | string[] | null | undefined;

  const props = withDefaults(
    defineProps<{
      modelValue?: DeviceValue;
      /** 关联产品标识(空值时 picker 自动 disabled,因为后端按产品过滤设备) */
      productIdentification?: string;
      multiple?: boolean;
      /** 显式 disabled,优先级高于"无产品" */
      disabled?: boolean;
      /** 覆盖默认 modal 标题 */
      title?: string;
      /** 覆盖默认搜索 placeholder */
      searchPlaceholder?: string;
      /** 覆盖默认触发器文案 */
      triggerLabels?: EntityTriggerLabels;
      /** 覆盖默认卡片副字段 */
      descFields?: EntityDescField[];
    }>(),
    {
      multiple: false,
      disabled: false,
    },
  );

  const emit = defineEmits<{
    (e: 'update:modelValue', val: DeviceValue): void;
    (e: 'change', val: DeviceValue, records: any[]): void;
  }>();

  const { t } = useI18n();

  // ============================== v-model 双绑 ==============================
  const innerValue = computed<DeviceValue>({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v),
  });

  function onChange(val: DeviceValue, records: any[]) {
    emit('change', val, records);
  }

  // ============================== 设备状态 / 默认 disabled ==============================
  /** 设备连接状态命中"在线"的值集合(connectStatus: 1/'1'/'ONLINE') */
  const DEVICE_ONLINE_VALUES: Array<string | number> = [1, '1', 'ONLINE'];

  /** 无产品标识时自动 disabled,避免后端无 productIdentification 全量查 */
  const actuallyDisabled = computed<boolean>(
    () => props.disabled || !props.productIdentification,
  );

  const finalPageParams = computed<Record<string, any>>(() => ({
    productIdentification: props.productIdentification,
  }));

  // ============================== Picker API 适配 ==============================
  const devicePageApi = (req: any) => {
    const model: Record<string, any> = {};
    if (req?.keyword) model.deviceName = req.keyword;
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

  // ============================== 默认配置(允许 props 覆盖) ==============================
  const defaultDescFields = computed<EntityDescField[]>(() => [
    {
      label: t('iot.link.device.device.deviceIdentification'),
      field: 'deviceIdentification',
    },
    { label: t('iot.link.device.device.clientId'), field: 'clientId' },
    { label: t('iot.link.device.device.deviceTags'), field: 'deviceTags' },
    {
      label: t('iot.link.device.device.connectStatus'),
      field: 'connectStatus',
      dictType: DictEnum.LINK_DEVICE_CONNECT_STATUS,
    },
  ]);

  const finalTitle = computed<string>(
    () => props.title || t('component.iotDevicePicker.title'),
  );

  const finalSearchPlaceholder = computed<string>(
    () =>
      props.searchPlaceholder || t('component.iotDevicePicker.searchPlaceholder'),
  );

  const finalTriggerLabels = computed<EntityTriggerLabels>(() => ({
    empty: props.triggerLabels?.empty || t('component.iotDevicePicker.triggerEmpty'),
    button:
      props.triggerLabels?.button || t('component.iotDevicePicker.triggerButton'),
  }));

  const finalDescFields = computed<EntityDescField[]>(
    () => props.descFields || defaultDescFields.value,
  );
</script>
