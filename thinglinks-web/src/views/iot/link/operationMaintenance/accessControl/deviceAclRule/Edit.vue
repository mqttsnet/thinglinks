<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    :width="1000"
    @ok="handleSubmit"
    :keyboard="true"
  >
    <BasicForm @register="registerForm">
      <!-- 产品标识 ── 通用 IotProductPicker(默认配置已含 4 卡片字段 / 协议徽标 / 启用状态)
           ⚠ 不直接 model[field] = v:antdv Form 内部 fieldsValue 缓存与 formModel 在
           reactive 写回链路上有时不同步;走 setFieldsValue 标准 API 触发 antdv 内部同步,
           validate() 取值才可靠 -->
      <template #productIdentification="{ model, field }">
        <IotProductPicker
          :modelValue="model[field]"
          @update:modelValue="onProductIdentificationUpdate"
          :title="t('iot.link.operationMaintenance.accessControl.deviceAclRule.dialog.pickProduct')"
          :triggerLabels="productTriggerLabels"
          @change="onProductIdentificationChange"
        />
      </template>

      <!-- 设备标识 ── 通用 IotDevicePicker(无产品时自动 disabled) -->
      <template #deviceIdentification="{ model, field }">
        <IotDevicePicker
          :modelValue="model[field]"
          @update:modelValue="onDeviceIdentificationUpdate"
          :productIdentification="model.productIdentification"
          :title="t('iot.link.operationMaintenance.accessControl.deviceAclRule.dialog.pickDevice')"
          :triggerLabels="deviceTriggerLabels"
        />
      </template>

      <!-- Topic 模式 ── ProductTopicPicker(基础 + 自定义双模式) -->
      <template #topicPattern="{ model, field }">
        <ProductTopicPicker
          :modelValue="model[field]"
          @update:modelValue="onTopicPatternUpdate"
          :productIdentification="model.productIdentification"
        />
      </template>

      <!-- IP 白名单 ── tags 输入(逗号/空格分隔自动切分) -->
      <template #ipWhitelist="{ model, field }">
        <a-select
          v-model:value="ipTags"
          mode="tags"
          :placeholder="
            t('iot.link.operationMaintenance.accessControl.deviceAclRule.placeholder.ipWhitelist')
          "
          :token-separators="[',', '，', ' ']"
          :max-tag-count="6"
          style="width: 100%"
          @change="(tags) => onIpTagsChange(model, field, tags)"
          @search="(v) => (ipSearchValue = v)"
          @blur="onIpBlur(model, field)"
        >
          <template #suffixIcon>
            <GlobalOutlined />
          </template>
        </a-select>
        <div class="ip-tip">
          <InfoCircleOutlined />
          {{
            t('iot.link.operationMaintenance.accessControl.deviceAclRule.tips.ipWhitelistFormat')
          }}
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { GlobalOutlined, InfoCircleOutlined } from '@ant-design/icons-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
  import { IotProductPicker, IotDevicePicker } from '/@/components/iot/IotProductDevicePicker';
  import { ProductTopicPicker } from '/@/components/iot/ProductTopicPicker';
  import {
    Api,
    save,
    update,
  } from '/@/api/iot/link/operationMaintenance/accessControl/deviceAclRule';
  import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
  import { customFormSchemaRules, editFormSchema } from './deviceAclRule.data';

  defineOptions({ name: '编辑ACL规则' });

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n();
  const { createMessage } = useMessage();

  const type = ref<ActionEnum>(ActionEnum.ADD);

  // ============================== Picker 触发器文案(模块特有,覆盖 IotProductPicker / IotDevicePicker 默认通用文案) ==============================
  const productTriggerLabels = {
    empty: t(
      'iot.link.operationMaintenance.accessControl.deviceAclRule.placeholder.productIdentification',
    ),
    button: t('iot.link.operationMaintenance.accessControl.deviceAclRule.action.pickProduct'),
  };
  const deviceTriggerLabels = {
    empty: t(
      'iot.link.operationMaintenance.accessControl.deviceAclRule.placeholder.deviceIdentification',
    ),
    button: t('iot.link.operationMaintenance.accessControl.deviceAclRule.action.pickDevice'),
  };

  // ============================== IP 白名单 tag 输入 ==============================
  const ipTags = ref<string[]>([]);
  /**
   * 缓存 a-select 输入框里**未提交**的搜索文本 ── tags 模式下用户输入但没按
   * 回车/空格/逗号就点确认时,a-select 默认会丢掉这段文本;onBlur 时把它
   * 当成最后一个 tag 自动 commit。
   */
  const ipSearchValue = ref('');

  /** ipTags 变更时同步回 form (英文逗号分隔字符串) */
  function onIpTagsChange(model: Recordable, field: string, tags: string[]) {
    model[field] = tags.filter((s) => !!s && s.trim()).join(',');
  }

  /** 反序列化 form 值到 ipTags(逗号分隔字符串 → 数组) */
  function syncIpTagsFromValue(v: string | undefined | null) {
    ipTags.value = (v || '')
      .split(/[,，\s]+/)
      .map((s) => s.trim())
      .filter(Boolean);
  }

  /**
   * a-select 失焦时兜底:把 input 框里未确认的文本作为 tag 提交,顺带把
   * ipTags 同步回 form model ── 避免用户输了 IP 没按回车就点"确认"导致
   * required 校验失败 + 字段空保存。
   */
  function onIpBlur(model: Recordable, field: string) {
    const raw = ipSearchValue.value.trim();
    if (raw) {
      const parts = raw
        .split(/[,，\s]+/)
        .map((s) => s.trim())
        .filter(Boolean);
      if (parts.length) {
        // 去重后追加;Set 保序(ES2015+)
        ipTags.value = Array.from(new Set([...ipTags.value, ...parts]));
      }
      ipSearchValue.value = '';
    }
    // 强制同步到 form model,与 onIpTagsChange 对称
    model[field] = ipTags.value.filter((s) => !!s && s.trim()).join(',');
  }

  // ============================== 表单 ==============================
  const [
    registerForm,
    { setFieldsValue, resetFields, updateSchema, validate, resetSchema, clearValidate },
  ] = useForm({
    name: 'DeviceAclRuleEdit',
    labelWidth: 150,
    schemas: editFormSchema(type),
    showActionButtonGroup: false,
    disabled: () => unref(type) === ActionEnum.VIEW,
    baseColProps: { span: 11 },
    actionColOptions: { span: 22 },
  });

  // ============================== Picker 同步 handler ==============================
  /**
   * Picker 的 @update:modelValue 统一走 setFieldsValue 标准 API ──
   * antdv Form 内部 fieldsValue 缓存与外部 formModel reactive 写回链路偶尔不同步,
   * setFieldsValue 强制双向同步,validate() 拿值才可靠。
   */
  // setFieldsValue 是程序化赋值,不会自动重跑该字段校验 → 选/填后手动 clearValidate 清掉旧的"请输入"提示;
  // 最终提交仍以 handleSubmit 的 validate() 为准(真为空会重新报错)。
  function onProductIdentificationUpdate(v: any) {
    setFieldsValue({ productIdentification: v ?? '' });
    clearValidate('productIdentification');
  }
  /** 产品变化时清空设备 + topic,避免脏数据 */
  function onProductIdentificationChange() {
    setFieldsValue({ deviceIdentification: '', topicPattern: '' });
  }
  function onDeviceIdentificationUpdate(v: any) {
    setFieldsValue({ deviceIdentification: v ?? '' });
    clearValidate('deviceIdentification');
  }
  function onTopicPatternUpdate(v: any) {
    setFieldsValue({ topicPattern: v ?? '' });
    clearValidate('topicPattern');
  }

  const [registerModel, { setModalProps, closeModal }] = useModalInner(async (data) => {
    type.value = data?.type || ActionEnum.ADD;
    setModalProps({ confirmLoading: false });
    await resetSchema(editFormSchema(type));
    await resetFields();
    ipTags.value = [];

    if (unref(type) !== ActionEnum.ADD && data?.record) {
      const record = { ...data.record };
      await setFieldsValue(record);
      // ipWhitelist 可能为 null/undefined(后端 omit 空字段),syncIpTagsFromValue
      // 已用 v || '' 兜底,这里显式 ?? '' 让回显意图更清晰
      syncIpTagsFromValue(record.ipWhitelist ?? '');
    }

    if (unref(type) !== ActionEnum.VIEW) {
      const validateApi = Api[VALIDATE_API[unref(type)]];
      await getValidateRules(validateApi, customFormSchemaRules(type)).then(async (rules) => {
        rules && rules.length > 0 && (await updateSchema(rules));
      });
    }
  });

  async function handleSubmit() {
    // 提交前最终把 ipTags + 未提交搜索文本拼成 ipWhitelist 写回 form,
    // 防止 a-select tags 模式"输了文字没按回车就点确认"被丢弃 → validate 失败
    const pending = ipSearchValue.value.trim();
    const merged = pending
      ? Array.from(
          new Set([
            ...ipTags.value,
            ...pending
              .split(/[,，\s]+/)
              .map((s) => s.trim())
              .filter(Boolean),
          ]),
        )
      : ipTags.value;
    ipTags.value = merged;
    ipSearchValue.value = '';
    await setFieldsValue({
      ipWhitelist: merged.filter((s) => !!s && s.trim()).join(','),
    });

    try {
      const params = await validate();
      setModalProps({ confirmLoading: true });

      if (unref(type) !== ActionEnum.VIEW) {
        if (unref(type) === ActionEnum.EDIT) {
          await update(params);
        } else {
          params.id = null;
          await save(params);
        }
        createMessage.success(t(`common.tips.${type.value}Success`));
      }
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  // BasicForm 内 Divider 分组标题样式美化
  :deep(.ant-divider-horizontal.ant-divider-with-text) {
    margin: 16px 0 12px;
    color: #1f2937;
    font-weight: 600;
    font-size: 14px;

    &::before,
    &::after {
      border-block-start-color: #e6f4ff;
    }

    .ant-divider-inner-text {
      padding: 0 16px;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 8px;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 14px;
        background: @primary-color;
        border-radius: 2px;
      }

      padding-left: 18px;
    }
  }

  .ip-tip {
    margin-top: 4px;
    font-size: 12px;
    color: #8c8c8c;
    display: flex;
    align-items: center;
    gap: 4px;

    :deep(svg) {
      color: #faad14;
    }
  }
</style>
