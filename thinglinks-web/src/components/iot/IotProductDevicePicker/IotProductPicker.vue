<template>
  <BasicEntityPicker
    v-model="innerValue"
    :multiple="multiple"
    :disabled="disabled"
    :title="finalTitle"
    :pageApi="productPageApi"
    :detailApi="productDetailApi"
    valueField="productIdentification"
    labelField="productName"
    searchField="productName"
    :searchPlaceholder="finalSearchPlaceholder"
    :filters="productFilters"
    :descFields="finalDescFields"
    :triggerLabels="finalTriggerLabels"
    headerBadgeField="protocolType"
    :headerBadgeDictType="DictEnum.LINK_PRODUCT_PROTOCOL_TYPE"
    statusField="productStatus"
    :statusOnlineValues="PRODUCT_ONLINE_VALUES"
    timeField="createdTime"
    @change="onChange"
  />
</template>

<script lang="ts" setup>
  /**
   * IotProductPicker ── IoT 产品选择器(项目通用)
   *
   * <p>对 {@link BasicEntityPicker} 的薄包装,把"产品分页 API / 反查 API / 卡片字段 /
   * 状态徽标 / 触发文案"等所有产品场景固定配置塞进默认值,业务侧调用时只需 v-model + 可选覆盖项,
   * 杜绝 ACL / 桥接 / 规则联动 / 设备命令 等多处「重复造轮子」。
   *
   * <p>覆盖优先级:props > 默认值。所有覆盖项都是可选的,允许业务侧:
   * <ul>
   *   <li>{@code title}:覆盖 modal 标题</li>
   *   <li>{@code searchPlaceholder}:覆盖搜索框占位</li>
   *   <li>{@code triggerLabels}:覆盖触发器空态/按钮文案</li>
   *   <li>{@code descFields}:整体替换卡片副字段(默认 4 项:产品标识/类型/厂商/应用)</li>
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
    type EntityFilter,
    type EntityTriggerLabels,
  } from '/@/components/iot/BasicEntityPicker';
  import {
    page as productPage,
    query as queryProducts,
  } from '/@/api/iot/link/product/product';

  defineOptions({ name: 'IotProductPicker' });

  type ProductValue = string | string[] | null | undefined;

  const props = withDefaults(
    defineProps<{
      modelValue?: ProductValue;
      multiple?: boolean;
      disabled?: boolean;
      /** 覆盖默认 modal 标题(默认走 i18n component.iotProductPicker.title) */
      title?: string;
      /** 覆盖默认搜索框 placeholder */
      searchPlaceholder?: string;
      /** 覆盖默认触发器文案 */
      triggerLabels?: EntityTriggerLabels;
      /** 覆盖默认卡片副字段(传入则整体替换;不传则用内置 4 项) */
      descFields?: EntityDescField[];
    }>(),
    {
      multiple: false,
      disabled: false,
    },
  );

  const emit = defineEmits<{
    (e: 'update:modelValue', val: ProductValue): void;
    (e: 'change', val: ProductValue, records: any[]): void;
  }>();

  const { t } = useI18n();

  // ============================== v-model 双绑 ==============================
  const innerValue = computed<ProductValue>({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v),
  });

  function onChange(val: ProductValue, records: any[]) {
    emit('change', val, records);
  }

  // ============================== Picker API 适配 ==============================
  /** 产品分页 ── BasicEntityPicker.EntityPageRequest → 项目原生 PageParams
   * <ul>
   *   <li>搜索框 keyword → model.productName(后端 like 模糊)</li>
   *   <li>顶部过滤器"产品标识" → req.filters.productIdentification → model.productIdentification(后端 eq 精确)</li>
   *   <li>两者同时填 → AND 关系组合查询</li>
   * </ul>
   */
  const productPageApi = (req: any) =>
    productPage({
      model: {
        ...(req?.keyword ? { productName: req.keyword } : {}),
        ...(req?.filters || {}),
      } as any,
      size: req?.pageSize ?? 12,
      current: req?.pageNum ?? 1,
    } as any);

  /** 顶部过滤器:产品标识(精确匹配) */
  const productFilters = computed<EntityFilter[]>(() => [
    {
      field: 'productIdentification',
      label: t('component.iotProductPicker.filterIdentificationLabel'),
      type: 'input',
      width: 200,
    },
  ]);

  /** 产品反查 ── 编辑/回显已选标识时取产品详情用于 chip / card 展示 */
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

  // ============================== 默认配置(允许 props 覆盖) ==============================
  /** 产品状态字段命中"启用"的值集合(productStatus: 0=启用 / 1=停用) */
  const PRODUCT_ONLINE_VALUES: Array<string | number> = [0, '0'];

  const defaultDescFields = computed<EntityDescField[]>(() => [
    {
      label: t('iot.link.product.product.productIdentification'),
      field: 'productIdentification',
    },
    {
      label: t('iot.link.product.product.productType'),
      field: 'productType',
      dictType: DictEnum.LINK_PRODUCT_TYPE,
    },
    {
      label: t('iot.link.product.product.manufacturerName'),
      field: 'manufacturerName',
    },
    { label: t('iot.link.product.product.appId'), field: 'appId' },
  ]);

  const finalTitle = computed<string>(
    () => props.title || t('component.iotProductPicker.title'),
  );

  const finalSearchPlaceholder = computed<string>(
    () =>
      props.searchPlaceholder || t('component.iotProductPicker.searchPlaceholder'),
  );

  const finalTriggerLabels = computed<EntityTriggerLabels>(() => ({
    empty: props.triggerLabels?.empty || t('component.iotProductPicker.triggerEmpty'),
    button:
      props.triggerLabels?.button || t('component.iotProductPicker.triggerButton'),
  }));

  const finalDescFields = computed<EntityDescField[]>(
    () => props.descFields || defaultDescFields.value,
  );
</script>
