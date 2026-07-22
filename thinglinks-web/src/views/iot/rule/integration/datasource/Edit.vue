<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :maskClosable="false"
    :width="1000"
    @ok="handleSubmit"
  >
    <template #title>
      <div class="modal-title-row">
        <div class="title-icon">
          <component :is="currentSvg" />
        </div>
        <span>{{ t(`common.title.${type}`) }}</span>
      </div>
    </template>
    <template #footer>
      <a-button @click="handleCancel">{{ t('common.cancelText') }}</a-button>
      <a-dropdown
        v-if="currentSourceType && currentPresets.length"
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
      <a-button type="default" :loading="testing" @click="handleTestByForm">
        <template #icon><Icon icon="ant-design:thunderbolt-outlined" /></template>
        {{ t('iot.rule.integration.datasource.action.test') }}
      </a-button>
      <a-button type="primary" :loading="confirmLoading" @click="handleSubmit">
        {{ t('common.saveText') }}
      </a-button>
    </template>
    <BasicForm @register="registerForm" @field-value-change="onFieldChange" />
    <a-alert
      v-if="lastTestResult !== null"
      :message="
        lastTestResult
          ? t('iot.rule.integration.datasource.tips.testSuccess')
          : t('iot.rule.integration.datasource.tips.testFailed')
      "
      :type="lastTestResult ? 'success' : 'error'"
      show-icon
      style="margin-top: 12px"
    />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref, computed, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { detail, save, update, testConnectionByForm } from '/@/api/iot/rule/integration/dataSource';
  import type {
    DataSourceSaveVO,
    DataSourceUpdateVO,
  } from '/@/api/iot/rule/integration/model/dataSourceModel';
  import { basicEditFormSchema, policyEditFormSchema } from './datasource.data';
  import {
    getConnectionFields,
    getCredentialFields,
    getProtocolPresets,
    getRecommendedDefaults,
    parseConnection,
    parseCredential,
    assembleJson,
  } from './protocols';
  import type { ProtocolPreset } from './protocols';
  import { Icon } from '/@/components/Icon';
  import { getSourceTypeSvg, DataBridgeSvg } from '/@/components/iot/integration/svg';

  defineOptions({ name: '编辑数据源' });

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n();
  const { createMessage } = useMessage();

  const type = ref<ActionEnum>(ActionEnum.ADD);
  const confirmLoading = ref(false);
  const testing = ref(false);
  /** 上次测试结果（null=未测试 / true=通过 / false=失败）。任一字段变更后重置为 null */
  const lastTestResult = ref<boolean | null>(null);

  /** 表单当前 sourceType 值（驱动标题图标 + 动态 schema + 示例下拉项） */
  const currentSourceType = ref<string>('');

  /** 缓存当前 sourceType 涉及的字段名集合，便于提交时切片提取 */
  const currentConnectionFields = ref<string[]>([]);
  const currentCredentialFields = ref<string[]>([]);

  /**
   * 组装完整 schema：基础信息 → 连接参数 → 凭证密钥 → 默认策略与高级配置。
   *
   * <p>"基础信息" / "默认策略" / "高级配置" 三组 Divider 已分别落在
   * {@code basicEditFormSchema} 和 {@code policyEditFormSchema} 内；
   * 这里仅条件性地为"连接参数" / "凭证密钥" 两组（仅在 sourceType 选定后才有字段）插入 Divider。
   *
   * @param sourceType 当前选中的协议类型；空时只展示基础信息 + 默认策略（连接 / 凭证 schema 隐藏）
   */
  function composeSchema(sourceType?: string) {
    const connectionSchemas = getConnectionFields(sourceType);
    const credentialSchemas = getCredentialFields(sourceType);
    currentConnectionFields.value = connectionSchemas.map((s) => s.field);
    currentCredentialFields.value = credentialSchemas.map((s) => s.field);
    const connectionGroup = connectionSchemas.length
      ? [
          {
            field: 'divider-connection',
            component: 'Divider',
            label: t('iot.rule.integration.datasource.group.connection'),
            colProps: { span: 24 },
          } as any,
          ...connectionSchemas,
        ]
      : [];
    const credentialGroup = credentialSchemas.length
      ? [
          {
            field: 'divider-credential',
            component: 'Divider',
            label: t('iot.rule.integration.datasource.group.credential'),
            colProps: { span: 24 },
          } as any,
          ...credentialSchemas,
        ]
      : [];
    return [
      ...basicEditFormSchema(type),
      ...connectionGroup,
      ...credentialGroup,
      ...policyEditFormSchema(),
    ];
  }

  const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] = useForm({
    name: 'DataSourceEdit',
    labelWidth: 150,
    schemas: composeSchema(undefined),
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
  });

  /**
   * BasicForm 字段变更回调（事件名 field-value-change，不是 ant-design 的 onValuesChange）。
   * 用 template 上的 @field-value-change 接入。
   */
  async function onFieldChange(key: string, value: any) {
    // sourceType 变化 → 重组 schema + 自动填合理默认策略 + 标题图标
    if (key === 'sourceType') {
      currentSourceType.value = value;
      await nextTick();
      await resetSchema(composeSchema(value));
      // 智能预填策略默认值（仅 ADD 模式覆盖；EDIT 模式不动用户已有值）
      if (unref(type) === ActionEnum.ADD && value) {
        const recommended = getRecommendedDefaults(value);
        if (Object.keys(recommended).length > 0) {
          await setFieldsValue(recommended);
        }
      }
    }
    // 任一字段变更 → 测试结果失效，需重新测试
    lastTestResult.value = null;
  }

  /** 标题图标：根据当前 sourceType 切换；未选时显示通用 DataBridge */
  const currentSvg = computed(() => {
    return currentSourceType.value
      ? getSourceTypeSvg(currentSourceType.value)
      : DataBridgeSvg;
  });

  /** 当前 sourceType 对应的示例 preset 列表 */
  const currentPresets = computed<ProtocolPreset[]>(() => {
    return getProtocolPresets(currentSourceType.value);
  });

  const [registerModel, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    confirmLoading.value = false;
    testing.value = false;
    lastTestResult.value = null;
    currentSourceType.value = '';
    currentConnectionFields.value = [];
    currentCredentialFields.value = [];

    // 兼容两种调用方约定：BusinessCardList 传 type=EDIT，老调用方传 isUpdate=true
    const incomingType = (data?.type as ActionEnum) ?? (data?.isUpdate ? ActionEnum.EDIT : ActionEnum.ADD);
    type.value = incomingType;
    const isLoadDetail = incomingType !== ActionEnum.ADD && data?.record?.id;

    if (isLoadDetail) {
      // ⭐ 编辑时调 detail 接口取明文（含 connectionJson + credentialJson）
      const record = await detail(data.record.id);
      const sourceType = record?.sourceType ?? '';
      currentSourceType.value = sourceType;
      // 先重组 schema（含该 sourceType 的连接 / 凭证字段），再 setFieldsValue
      await resetSchema(composeSchema(sourceType));
      // 拆 JSON → 平铺字段（按 sourceType 路由到对应 ProtocolModule.parseConnection/Credential）
      const conn = parseConnection(sourceType, record?.connectionJson);
      const cred = parseCredential(sourceType, record?.credentialJson);
      await setFieldsValue({ ...record, ...conn, ...cred });
    } else {
      await resetSchema(composeSchema(undefined));
      await resetFields();
    }
  });

  /**
   * 从表单值切片出当前 sourceType 涉及的连接 / 凭证字段，组装回 JSON 字符串。
   *
   * @param values getFieldsValue() 返回的全量表单值
   */
  function buildSubmitParams(values: Record<string, any>) {
    const connObj: Record<string, any> = {};
    const credObj: Record<string, any> = {};
    for (const f of currentConnectionFields.value) {
      if (values[f] !== undefined) connObj[f] = values[f];
    }
    for (const f of currentCredentialFields.value) {
      if (values[f] !== undefined) credObj[f] = values[f];
    }
    // 删除已平铺的连接 / 凭证字段，避免后端 entity 字段冲突
    const stripped: Record<string, any> = { ...values };
    [...currentConnectionFields.value, ...currentCredentialFields.value].forEach((f) => {
      delete stripped[f];
    });
    stripped.connectionJson = assembleJson(connObj);
    stripped.credentialJson = assembleJson(credObj);
    return stripped;
  }

  /** 加载示例配置：把 preset 的 connection + credential 平铺到表单 */
  async function onPresetClick({ key }: { key: string }) {
    const preset = currentPresets.value.find((p) => p.key === key);
    if (!preset) return;
    const flat: Record<string, any> = { ...(preset.connection ?? {}), ...(preset.credential ?? {}) };
    await setFieldsValue(flat);
    createMessage.info(`${t('iot.rule.integration.datasource.action.loadExample')}: ${preset.label}`);
  }

  /** 测试连接（基于表单当前值，不写 DB） */
  async function handleTestByForm() {
    try {
      testing.value = true;
      const values = await validate();
      const params = buildSubmitParams(values);
      const ok = await testConnectionByForm(params as DataSourceSaveVO);
      lastTestResult.value = ok;
      if (ok) {
        createMessage.success(t('iot.rule.integration.datasource.tips.testSuccess'));
      } else {
        createMessage.error(t('iot.rule.integration.datasource.tips.testFailed'));
      }
    } catch (e: any) {
      lastTestResult.value = false;
      createMessage.error(
        t('iot.rule.integration.datasource.tips.testFailed') + ': ' + (e?.message ?? ''),
      );
    } finally {
      testing.value = false;
    }
  }

  /** 保存（默认 enable=false，必须测试通过后用户在列表点 toggle 启用） */
  async function handleSubmit() {
    try {
      const values = await validate();
      confirmLoading.value = true;
      const params = buildSubmitParams(values);
      if (unref(type) === ActionEnum.EDIT) {
        await update(params as DataSourceUpdateVO);
        createMessage.success(t('common.tips.editSuccess'));
      } else {
        params.id = null;
        await save(params as DataSourceSaveVO);
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

<style lang="less" scoped>
  /* SVG 默认占满容器，Modal title slot 外层 div 宽度可能是 100%，需强制约束尺寸 */
  .modal-title-row {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .title-icon {
    width: 28px;
    height: 28px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(svg) {
      width: 100%;
      height: 100%;
    }
  }
</style>
