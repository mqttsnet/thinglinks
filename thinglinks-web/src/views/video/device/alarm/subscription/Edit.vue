<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    @ok="handleSubmit"
    :keyboard="true"
    width="720px"
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
  import { save, update, detail } from '/@/api/video/device/notifySubscription';
  import { editFormSchema } from './subscription.data';

  export default defineComponent({
    name: 'VideoNotifySubscriptionEdit',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();

      const [registerForm, { setFieldsValue, resetFields, resetSchema, validate }] = useForm({
        name: 'NotifySubscriptionEdit',
        labelWidth: 120,
        schemas: editFormSchema(type),
        showActionButtonGroup: false,
        disabled: (_) => unref(type) === ActionEnum.VIEW,
        baseColProps: { span: 24 },
      });

      const [registerModel, { setModalProps: setProps, closeModal: close }] = useModalInner(
        async (data) => {
          setProps({ confirmLoading: false });
          await resetSchema(editFormSchema(type));
          await resetFields();
          type.value = data?.type || ActionEnum.ADD;

          if (unref(type) !== ActionEnum.ADD) {
            const record = await detail(data?.record?.id);
            // 解析 channelConfig JSON → 表单字段
            const config = JSON.parse(record?.channelConfig || '{}');
            record.configToken = config?.token;
            record.configSecret = config?.secret;
            record.configAppId = config?.appId;
            record.configAppSecret = config?.appSecret;
            // eventTypes: "ALARM,DEVICE_ONLINE" → ['ALARM','DEVICE_ONLINE']
            if (typeof record.eventTypes === 'string') {
              record.eventTypes = record.eventTypes.split(',').filter(Boolean);
            }
            await setFieldsValue(record);
          }
        },
      );

      async function handleSubmit() {
        try {
          const params = await validate();
          setProps({ confirmLoading: true });

          // 组装 channelConfig JSON
          const configObj: Record<string, string> = {};
          if (params.configToken) configObj.token = params.configToken;
          if (params.configSecret) configObj.secret = params.configSecret;
          if (params.configAppId) configObj.appId = params.configAppId;
          if (params.configAppSecret) configObj.appSecret = params.configAppSecret;
          params.channelConfig = JSON.stringify(configObj);

          // eventTypes: ['ALARM','DEVICE_ONLINE'] → "ALARM,DEVICE_ONLINE"
          if (Array.isArray(params.eventTypes)) {
            params.eventTypes = params.eventTypes.join(',');
          }

          // 清理临时字段
          delete params.configToken;
          delete params.configSecret;
          delete params.configAppId;
          delete params.configAppSecret;

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

      return { type, t, registerModel, registerForm, handleSubmit };
    },
  });
</script>
