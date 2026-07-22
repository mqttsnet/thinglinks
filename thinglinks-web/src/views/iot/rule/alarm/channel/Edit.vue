<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    @ok="handleSubmit"
    :keyboard="true"
    :bodyStyle="{ height: '420px' }"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { detail, save, update } from '../../../../../api/iot/rule/alarm/channel';
  import { CHANNEL_TYPE, editFormSchema } from './channel.data';

  export default defineComponent({
    name: '编辑告警渠道',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();

      const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] = useForm({
        name: 'AlarmChannelEdit',
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
          setProps({ confirmLoading: false });
          await resetSchema(editFormSchema(type));
          await resetFields();
          type.value = data?.type || ActionEnum.ADD;

          if (unref(type) !== ActionEnum.ADD) {
            // 赋值
            const record = await detail(data?.record?.id);
            const channelConfig = parseChannelConfig(record?.channelConfig);
            record.token = channelConfig?.token;
            record.secret = channelConfig?.secret;
            record.appId = channelConfig?.appId;
            record.appSecret = channelConfig?.appSecret;
            record.remindMode = channelConfig?.remindMode || '02';
            record.target = channelConfig?.target || '01';
            record.autoRead = channelConfig?.autoRead ?? false;
            record.url = channelConfig?.url;
            record.recipientList = normalizeRecipientList(channelConfig?.recipientList);
            record.status = String(record.status);
            record.channelType = String(record.channelType);
            await setFieldsValue(record);
          }

          // if (unref(type) !== ActionEnum.VIEW) {
          //   let validateApi = Api[VALIDATE_API[unref(type)]];
          //   await getValidateRules(validateApi, customFormSchemaRules(type)).then(async (rules) => {
          //     rules && rules.length > 0 && (await updateSchema(rules));
          //   });
          // }
        },
      );

      async function handleSubmit() {
        try {
          const params = await validate();
          setProps({ confirmLoading: true });
          const channelType = String(params.channelType);
          const channelConfig: Recordable = {};
          if (
            [CHANNEL_TYPE.DING_TALK, CHANNEL_TYPE.ENTERPRISE_WECHAT, CHANNEL_TYPE.FEISHU].includes(
              channelType,
            )
          ) {
            channelConfig.token = params.token;
          }
          if (channelType === CHANNEL_TYPE.DING_TALK) {
            channelConfig.secret = params.secret;
          }
          if (channelType === CHANNEL_TYPE.FEISHU) {
            channelConfig.appId = params.appId;
            channelConfig.appSecret = params.appSecret;
          }
          if (channelType === CHANNEL_TYPE.SITE_MESSAGE) {
            channelConfig.remindMode = params.remindMode || '02';
            channelConfig.target = params.target || '01';
            channelConfig.autoRead = params.autoRead ?? false;
            channelConfig.url = params.url || '';
            channelConfig.recipientList = normalizeRecipientList(params.recipientList);
          }
          params.channelConfig = JSON.stringify(channelConfig);
          delete params.token;
          delete params.secret;
          delete params.appId;
          delete params.appSecret;
          delete params.remindMode;
          delete params.target;
          delete params.autoRead;
          delete params.url;
          delete params.recipientList;
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

      function parseChannelConfig(channelConfig?: string) {
        try {
          return JSON.parse(channelConfig || '{}');
        } catch {
          return {};
        }
      }

      function normalizeRecipientList(value?: string | Array<string | number | Recordable>) {
        if (Array.isArray(value)) {
          return value
            .map((item) => {
              if (typeof item === 'object' && item !== null) {
                return item.value ?? item.id ?? item.key ?? item.label ?? '';
              }
              return item;
            })
            .map((item) => String(item).trim())
            .filter(Boolean);
        }
        return String(value || '')
          .split(/[,，\n]/)
          .map((item) => item.trim())
          .filter(Boolean);
      }

      return { type, t, registerModel, registerForm, handleSubmit };
    },
  });
</script>
