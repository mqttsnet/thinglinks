<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    @ok="handleSubmit"
    :keyboard="true"
  >
    <BasicForm @register="registerForm" />

    <!-- VIEW 模式追加 channel_config / extend_params 自动展开（后端加字段前端无需改） -->
    <template v-if="type === ActionEnum.VIEW">
      <a-divider>{{ t('video.device.channel.channelConfig') }}</a-divider>
      <JsonAutoDescriptions
        :data="currentRecord?.channelConfig"
        i18n-prefix="video.device.channel.channelConfigFields"
      />

      <a-divider>{{ t('video.device.channel.extendParams') }}</a-divider>
      <JsonAutoDescriptions
        :data="currentRecord?.extendParams"
        i18n-prefix="video.device.channel.extendParamsFields"
      />
    </template>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
  import { Api, save, update } from '/@/api/video/device/channel';
  import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
  import { customFormSchemaRules, editFormSchema } from './channel.data';
  import JsonAutoDescriptions from '/@/components/JsonAutoDescriptions';

  export default defineComponent({
    name: 'VideoDeviceChannelEdit',
    components: { BasicModal, BasicForm, JsonAutoDescriptions },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      /** VIEW 模式下保存当前记录，供 JsonAutoDescriptions 读取 channel_config / extend_params */
      const currentRecord = ref<Record<string, any> | null>(null);
      const { createMessage } = useMessage();

      const [registerForm, { setFieldsValue, resetFields, updateSchema, validate, resetSchema }] =
        useForm({
          name: 'VideoDeviceChannelEdit',
          labelWidth: 100,
          schemas: editFormSchema(type),
          showActionButtonGroup: false,
          disabled: (_) => {
            return unref(type) === ActionEnum.VIEW;
          },
          baseColProps: { span: 11 },
          actionColOptions: {
            span: 22,
          },
        });

      const [registerModel, { setModalProps: setProps, closeModal: close }] = useModalInner(
        async (data) => {
          type.value = data?.type || ActionEnum.ADD;
          setProps({ confirmLoading: false });
          await resetSchema(editFormSchema(type));
          await resetFields();

          if (unref(type) !== ActionEnum.ADD) {
            // 赋值
            const record = { ...data?.record };
            currentRecord.value = record;
            await setFieldsValue(record);
          } else {
            currentRecord.value = null;
          }

          if (unref(type) !== ActionEnum.VIEW) {
            let validateApi = Api[VALIDATE_API[unref(type)]];
            await getValidateRules(validateApi, customFormSchemaRules(type)).then(async (rules) => {
              rules && rules.length > 0 && (await updateSchema(rules));
            });
          }
        },
      );

      async function handleSubmit() {
        try {
          const params = await validate();
          setProps({ confirmLoading: true });

          if (unref(type) !== ActionEnum.VIEW) {
            if (unref(type) === ActionEnum.EDIT) {
              await update(params);
            } else {
              params.id = null;
              await save(params);
            }
            createMessage.success(t(`common.tips.${type.value}Success`));
          }
          close();
          emit('success');
        } finally {
          setProps({ confirmLoading: false });
        }
      }

      return { type, t, registerModel, registerForm, handleSubmit, ActionEnum, currentRecord };
    },
  });
</script>
