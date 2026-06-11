<template>
  <AllOrCustomPicker
    v-model="innerValue"
    :title="finalTitle"
    :allValue="allValue"
    :allowAll="allowAll"
    :pageApi="productPageApi"
    :detailApi="productDetailApi"
    :pageParams="pageParams"
    valueField="productIdentification"
    labelField="productName"
    searchField="productName"
    :searchFields="defaultSearchFields"
    :descFields="finalDescFields"
    :triggerLabels="triggerLabels"
    headerBadgeField="protocolType"
    :headerBadgeDictType="DictEnum.LINK_PRODUCT_PROTOCOL_TYPE"
    statusField="productStatus"
    :statusOnlineValues="PRODUCT_ONLINE_VALUES"
    timeField="createdTime"
    :disabled="disabled"
    @change="onChange"
    @clear="onClear"
  />
</template>

<script lang="ts" setup>
  /**
   * IotAllOrCustomProductPicker ── IoT「全部 / 自定义产品」多选包装(项目通用)
   *
   * <p>对 {@link AllOrCustomPicker} 的薄包装,把"产品分页 API + 反查 API + 卡片字段(含字典回显
   * tag 形态)+ 协议徽标 + 启用状态 + 时间 + 搜索文案 + 在线值集合"全部塞进默认值;
   * 业务侧最少一行:{@code <IotAllOrCustomProductPicker v-model="..." />}。
   *
   * <p>v-model 两态:
   * <ul>
   *   <li>{@code 'all'}(对齐 BizConstant.ALL):全部模式</li>
   *   <li>{@code (string|number)[]}:自定义多选模式</li>
   * </ul>
   *
   * <p>覆盖优先级:props > 默认值。所有自定义项均可选。
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
    type PickerSearchField,
    type PickerTriggerLabels,
    type PickerValue,
  } from '/@/components/iot/AllOrCustomPicker';
  import {
    page as productPage,
    query as queryProducts,
  } from '/@/api/iot/link/product/product';

  defineOptions({ name: 'IotAllOrCustomProductPicker' });

  const props = withDefaults(
    defineProps<{
      modelValue?: PickerValue;
      /** 通配字面值,默认对齐 AllOrCustomPicker(BizConstant.ALL='all') */
      allValue?: string;
      /** 是否允许"全部"模式 */
      allowAll?: boolean;
      /** 调用方固定参数(透传到 pageApi.extra) */
      pageParams?: Record<string, any>;
      /** 禁用 */
      disabled?: boolean;
      /** 覆盖默认 modal 标题 */
      title?: string;
      /** 覆盖默认搜索 placeholder */
      searchPlaceholder?: string;
      /** 覆盖默认触发器文案 */
      triggerLabels?: PickerTriggerLabels;
      /** 覆盖默认卡片副字段(传入则整体替换) */
      descFields?: PickerDescField[];
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

  // ============================== Picker API 适配 ==============================
  // 搜索栏拆两个独立输入框:productName(LIKE 模糊) / productIdentification(精确);
  // 多字段值由 PickerModal 收集到 searchValues map,这里整体 merge 进后端 model.
  const productPageApi = (req: any) =>
    productPage({
      model: req?.searchValues
        ? (req.searchValues as any)
        : req?.keyword
          ? { productName: req.keyword }
          : ({} as any),
      size: req?.pageSize ?? 12,
      current: req?.pageNum ?? 1,
    } as any);

  const productDetailApi = async (values: (string | number)[]) => {
    if (!values || values.length === 0) return [];
    try {
      const list = await Promise.all(
        values.map((v) =>
          queryProducts({ productIdentification: String(v) } as any)
            .then((res: any) => (Array.isArray(res) ? res[0] : res))
            .catch(() => null),
        ),
      );
      return list.filter((x) => !!x) as any[];
    } catch {
      return [];
    }
  };

  // ============================== 默认配置 ==============================
  /** 产品状态字段命中"启用"的值集合(productStatus: 0=启用 / 1=停用) */
  const PRODUCT_ONLINE_VALUES: Array<string | number> = [0, '0'];

  const defaultDescFields = computed<PickerDescField[]>(() => [
    {
      label: t('iot.link.product.product.productIdentification'),
      field: 'productIdentification',
    },
    {
      label: t('iot.link.product.product.productType'),
      field: 'productType',
      style: 'tag',
      formatter: (v) => getDictLabel('LINK_PRODUCT_TYPE', String(v ?? ''), '-'),
      tagColor: () => 'cyan',
    },
    {
      label: t('iot.link.product.product.manufacturerName'),
      field: 'manufacturerName',
    },
    {
      label: t('iot.link.product.product.appId'),
      field: 'appId',
    },
  ]);

  const finalTitle = computed<string>(
    () => props.title || t('component.iotProductPicker.title'),
  );

  const finalDescFields = computed<PickerDescField[]>(
    () => props.descFields || defaultDescFields.value,
  );

  /** 两个独立搜索字段:产品名称(LIKE) + 产品标识(精确) ── 对齐后端 ProductPageQuery */
  const defaultSearchFields = computed<PickerSearchField[]>(() => [
    {
      field: 'productName',
      placeholder: t('iot.link.product.product.productName'),
      width: 200,
    },
    {
      field: 'productIdentification',
      placeholder: t('iot.link.product.product.productIdentification'),
      width: 220,
    },
  ]);
</script>
