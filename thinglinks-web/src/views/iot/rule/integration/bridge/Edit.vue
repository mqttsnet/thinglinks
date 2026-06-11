<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    :width="1100"
    @ok="handleSubmit"
  >
    <template #footer>
      <a-button @click="handleCancel">{{ t('common.cancelText') }}</a-button>
      <a-dropdown
        v-if="currentPresets.length"
        :trigger="['click']"
        placement="topRight"
      >
        <a-button>
          <template #icon><Icon icon="ant-design:bulb-outlined" /></template>
          {{ t('iot.rule.integration.datasource.action.loadExample') }}
        </a-button>
        <template #overlay>
          <a-menu @click="onPresetClick">
            <a-menu-item
              v-for="p in currentPresets"
              :key="p.key"
              :title="p.description"
            >
              {{ p.label }}
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
      <a-button type="primary" :loading="confirmLoading" @click="handleSubmit">
        {{ t('common.saveText') }}
      </a-button>
    </template>
    <BasicForm @register="registerForm" @field-value-change="onFieldChange">
      <!-- 产品标识 ── 通用 IotAllOrCustomProductPicker(默认配置:4 卡片字段含字典 tag / 协议徽标 / 启用状态) -->
      <template #mc_productIdentifications="{ model, field }">
        <IotAllOrCustomProductPicker
          v-model="model[field]"
          :allValue="BridgeWildcard.ALL"
          :title="t('iot.rule.integration.bridge.matchConfig.outbound.productIdentifications.label')"
        />
      </template>
      <!-- 设备标识 ── 通用 IotAllOrCustomDevicePicker(自动按所选产品过滤 + 顶部连接状态过滤项) -->
      <template #mc_deviceIdentifications="{ model, field }">
        <IotAllOrCustomDevicePicker
          v-model="model[field]"
          :productIdentification="resolveDeviceProductFilter(model)"
          :allValue="BridgeWildcard.ALL"
          :title="t('iot.rule.integration.bridge.matchConfig.outbound.deviceIdentifications.label')"
        />
      </template>
      <!-- Topic 模式 ── 通用 ProductTopicPicker 多选(对齐 ACL,直接绑定 MQTT 主题模板字符串) -->
      <template #mc_topicPatterns="{ model, field }">
        <ProductTopicPicker
          v-model="model[field]"
          :multiple="true"
          :productIdentification="resolveProductTopicFilter(model)"
        />
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref, computed, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { detail, save, update } from '/@/api/iot/rule/integration/dataBridge';
  import { detail as dataSourceDetail } from '/@/api/iot/rule/integration/dataSource';
  import { basicEditFormSchema, overrideEditFormSchema } from './bridge.data';
  import {
    getMatchConfigSchema,
    getActionConfigSchema,
    assembleMatchConfigJson,
    assembleActionConfigJson,
    flattenMatchConfig,
    flattenActionConfig,
    allMatchFieldNames,
    allActionFieldNames,
  } from './schemas';
  import { presetToFlatByType } from './protocols';
  import { outboundPresets, inboundPresets } from './presets';
  import type { OutboundExamplePreset, InboundExamplePreset } from './dto';
  import { BridgeWildcard } from './dto';
  import { Icon } from '/@/components/Icon';
  import type {
    DataBridgeSaveVO,
    DataBridgeUpdateVO,
  } from '/@/api/iot/rule/integration/model/dataBridgeModel';
  import {
    IotAllOrCustomProductPicker,
    IotAllOrCustomDevicePicker,
  } from '/@/components/iot/IotProductDevicePicker';
  import { ProductTopicPicker } from '/@/components/iot/ProductTopicPicker';

  defineOptions({ name: '编辑桥接规则' });

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n();
  const { createMessage } = useMessage();

  const type = ref<ActionEnum>(ActionEnum.ADD);
  const confirmLoading = ref(false);

  // ============================== 设备弹窗的产品过滤参数 ==============================
  /**
   * 设备 picker 的产品过滤 ── 当用户在匹配条件里只选了 1 个具体产品时,
   * 把它作为后端过滤条件,让设备弹窗只展示该产品下的设备(避免误选)。
   * <p>多选 / 全部 / 未选 时返回空字符串,picker 全量展示。
   */
  function resolveDeviceProductFilter(model: Record<string, any>): string {
    const products = model?.mc_productIdentifications;
    if (
      Array.isArray(products) &&
      products.length === 1 &&
      products[0] !== BridgeWildcard.ALL
    ) {
      return String(products[0]);
    }
    return '';
  }

  /**
   * Topic 选择器的产品上下文 ── 用户必须先选具体产品(单选 / 多选只有 1 个)才能拉
   * 该产品的基础 topic 列表;否则按钮 disabled。语义与 device picker 一致。
   */
  function resolveProductTopicFilter(model: Record<string, any>): string {
    return resolveDeviceProductFilter(model);
  }

  /** 当前 direction（10=出站 / 20=入站） */
  const currentDirection = ref<string>('10');
  /** 当前所选数据源的 sourceType（出站时驱动 action schema） */
  const currentSourceType = ref<string>('');
  /** 当前 inbound 的 targetHandler（驱动 inbound action 子段） */
  const currentTargetHandler = ref<string>('MQTT_FORWARD');

  /**
   * 重组完整 schema：基础信息 → 匹配条件 → 动作配置 → 流控覆盖 → 高级配置。
   *
   * <p>"基础信息" / "流控覆盖" / "高级配置" 三组 Divider 落在 {@code basicEditFormSchema} 与
   * {@code overrideEditFormSchema} 内；这里只负责为"匹配条件" / "动作配置" 两组动态 schema
   * 包裹 Divider（仅当对应 schema 非空时）。
   */
  function composeSchema() {
    const matchSchemas = getMatchConfigSchema(currentDirection.value);
    const actionSchemas = getActionConfigSchema(
      currentDirection.value,
      currentSourceType.value,
      currentTargetHandler.value,
    );
    const matchGroup = matchSchemas.length
      ? [
          {
            field: 'divider-match',
            component: 'Divider',
            label: t('iot.rule.integration.bridge.group.match'),
            colProps: { span: 24 },
          } as any,
          ...matchSchemas,
        ]
      : [];
    const actionGroup = actionSchemas.length
      ? [
          {
            field: 'divider-action',
            component: 'Divider',
            label: t('iot.rule.integration.bridge.group.action'),
            colProps: { span: 24 },
          } as any,
          ...actionSchemas,
        ]
      : [];
    return [
      ...basicEditFormSchema(type),
      ...matchGroup,
      ...actionGroup,
      ...overrideEditFormSchema(),
    ];
  }

  const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] = useForm({
    name: 'DataBridgeEdit',
    labelWidth: 150,
    schemas: composeSchema(),
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
  });

  /**
   * 字段变更回调（用 BasicForm 的 field-value-change 事件，不是 ant-design 的 onValuesChange）。
   * 由 template 上的 @field-value-change 接入。
   */
  async function onFieldChange(key: string, value: any) {
    let needRebuild = false;
    if (key === 'direction') {
      currentDirection.value = value;
      // direction 切换 → targetHandler 重置为默认 MQTT_FORWARD（仅入站有意义）
      currentTargetHandler.value = 'MQTT_FORWARD';
      needRebuild = true;
    } else if (key === 'dataSourceId') {
      // 异步取所选数据源的 sourceType（仅出站时影响 action schema）
      try {
        const ds = await dataSourceDetail(value);
        currentSourceType.value = ds?.sourceType ?? '';
      } catch (e) {
        currentSourceType.value = '';
      }
      needRebuild = true;
    } else if (key === 'ac_targetHandler') {
      currentTargetHandler.value = value;
      needRebuild = true;
    }
    if (needRebuild) {
      await nextTick();
      await resetSchema(composeSchema());
    }
  }

  /** 当前可用的示例预设（按 direction 切；出站再按 sourceType 过滤） */
  const currentPresets = computed<(OutboundExamplePreset | InboundExamplePreset)[]>(() => {
    if (currentDirection.value === '20') return inboundPresets;
    if (!currentSourceType.value) return outboundPresets;
    return outboundPresets.filter(
      (p) => !p.requireSourceType || p.requireSourceType === currentSourceType.value,
    );
  });

  const [registerModel, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    confirmLoading.value = false;
    currentDirection.value = '10';
    currentSourceType.value = '';
    currentTargetHandler.value = 'MQTT_FORWARD';

    // 兼容旧调用方 isUpdate=true（→EDIT）；新调用方传 type=COPY/EDIT/ADD
    const incomingType = (data?.type as ActionEnum) ?? (data?.isUpdate ? ActionEnum.EDIT : ActionEnum.ADD);
    type.value = incomingType;
    const isCopy = incomingType === ActionEnum.COPY;
    const isEdit = incomingType === ActionEnum.EDIT;
    const needLoadDetail = (isEdit || isCopy) && data?.record?.id;

    if (needLoadDetail) {
      const record = await detail(data.record.id);
      currentDirection.value = record?.direction ?? '10';
      // 出站时拉数据源 sourceType；入站从 actionConfigJson 解析 targetHandler
      if (currentDirection.value === '10' && record?.dataSourceId) {
        try {
          const ds = await dataSourceDetail(String(record.dataSourceId));
          currentSourceType.value = ds?.sourceType ?? '';
        } catch (e) {
          currentSourceType.value = '';
        }
      } else if (currentDirection.value === '20') {
        try {
          const ac = JSON.parse(record?.actionConfigJson || '{}');
          currentTargetHandler.value = ac?.targetHandler ?? 'MQTT_FORWARD';
        } catch (e) {
          currentTargetHandler.value = 'MQTT_FORWARD';
        }
      }
      await resetSchema(composeSchema());
      const flatMatch = flattenMatchConfig(record?.matchConfigJson, currentDirection.value);
      const flatAction = flattenActionConfig(
        record?.actionConfigJson,
        currentDirection.value,
        currentSourceType.value,
      );
      // COPY 模式：清空 id + ruleCode（让用户必须改至少 ruleName，触发新建）
      const cloned = { ...record, ...flatMatch, ...flatAction };
      if (isCopy) {
        cloned.id = undefined;
        cloned.ruleCode = undefined;
        cloned.ruleName = (record?.ruleName ?? '') + ' Copy';
      }
      await setFieldsValue(cloned);
    } else {
      await resetSchema(composeSchema());
      await resetFields();
    }
  });

  /**
   * 把表单值切片：基础字段 / 流控覆盖 / mc_* (match) / ac_* (action)。
   * 然后组装回 matchConfigJson + actionConfigJson 字符串。
   */
  function buildSubmitParams(values: Record<string, any>) {
    const matchFields = allMatchFieldNames();
    const actionFields = allActionFieldNames();
    const stripped: Record<string, any> = { ...values };
    [...matchFields, ...actionFields].forEach((f) => {
      delete stripped[f];
    });
    stripped.matchConfigJson = assembleMatchConfigJson(values, currentDirection.value);
    stripped.actionConfigJson = assembleActionConfigJson(
      values,
      currentDirection.value,
      currentSourceType.value,
    );
    return stripped;
  }

  /** 加载示例配置 */
  async function onPresetClick({ key }: { key: string }) {
    const preset = currentPresets.value.find((p: any) => p.key === key);
    if (!preset) return;
    if (currentDirection.value === '20') {
      const p = preset as InboundExamplePreset;
      const flat: Record<string, any> = {};
      flat.mc_subscriptionSourceIds = p.match.subscriptionSourceIds ?? [];
      flat.mc_messageFilterType = p.match.messageFilter?.type ?? 'NONE';
      flat.mc_messageFilterExpression = p.match.messageFilter?.expression;
      flat.ac_targetHandler = p.action.targetHandler ?? 'MQTT_FORWARD';
      currentTargetHandler.value = flat.ac_targetHandler;
      flat.ac_targetProductIdentification = p.action.targetProductIdentification;
      flat.ac_targetTopicTemplate = p.action.targetTopicTemplate;
      flat.ac_fieldMapping = p.action.fieldMapping;
      flat.ac_triggerRuleId = p.action.triggerRuleId;
      await nextTick();
      await resetSchema(composeSchema());
      await setFieldsValue(flat);
    } else {
      const p = preset as OutboundExamplePreset;
      const flat: Record<string, any> = {};
      flat.mc_productIdentifications = p.match.productIdentifications ?? [];
      flat.mc_actionTypes = p.match.actionTypes ?? [];
      flat.mc_topicPatterns = p.match.topicPatterns ?? [];
      flat.mc_payloadKinds = p.match.payloadKinds ?? [];
      flat.mc_payloadFilterType = p.match.payloadFilter?.type ?? 'NONE';
      flat.mc_payloadFilterExpression = p.match.payloadFilter?.expression;
      flat.mc_timeWindowCronExpr = p.match.timeWindow?.cronExpr;
      flat.mc_deviceIdentifications = p.match.deviceFilter?.deviceIdentifications ?? [];
      flat.mc_deviceTagsAny = p.match.deviceFilter?.tagsAny ?? [];
      flat.ac_payloadFormat = p.action.payloadFormat ?? 'JSON';
      flat.ac_payloadTemplate = p.action.payloadTemplate;
      // preset 子对象 → flat 全部委托 protocols/ registry：按 currentSourceType 路由到对应模块。
      // 旧的 15 个 xxxToFlat() 函数已移除（每协议独立到 protocols/XxxBridgeAction.ts.presetToFlat）。
      const subObj = (p.action as any)[currentSourceType.value?.toLowerCase()];
      Object.assign(flat, presetToFlatByType(currentSourceType.value, subObj));
      await setFieldsValue(flat);
    }
    createMessage.info(`${t('iot.rule.integration.datasource.action.loadExample')}: ${preset.label}`);
  }

  /** 保存（默认 enable=false） */
  async function handleSubmit() {
    try {
      const values = await validate();
      confirmLoading.value = true;
      const params = buildSubmitParams(values);
      if (unref(type) === ActionEnum.EDIT) {
        await update(params as DataBridgeUpdateVO);
        createMessage.success(t('common.tips.editSuccess'));
      } else {
        params.id = null;
        await save(params as DataBridgeSaveVO);
        createMessage.success(t('common.tips.addSuccess'));
      }
      closeModal();
      emit('success');
    } finally {
      confirmLoading.value = false;
    }
  }

  function handleCancel() {
    closeModal();
  }
</script>
