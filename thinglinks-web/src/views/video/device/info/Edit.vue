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
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
  import { Api, save, update } from '/@/api/video/device/info';
  import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
  import { customFormSchemaRules, editFormSchema } from './info.data';

  export default defineComponent({
    name: 'VideoDeviceInfoEdit',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();

      const [registerForm, { setFieldsValue, resetFields, updateSchema, validate, resetSchema }] =
        useForm({
          name: 'VideoDeviceInfoEdit',
          labelWidth: 120,
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
            // 赋值：把嵌套的 protocolConfig.streamSource 拍平成 streamSourceXxx 字段供表单使用
            const record = { ...(data?.record || {}) };
            const src = record?.protocolConfig?.streamSource || {};
            record.streamSourceUrl = src.url;
            record.streamSourceUsername = src.username;
            record.streamSourcePath = src.streamPath;
            record.streamSourceRtpType = src.rtpType ?? '0';
            await setFieldsValue(record);
          }

          if (unref(type) !== ActionEnum.VIEW) {
            let validateApi = Api[VALIDATE_API[unref(type)]];
            await getValidateRules(validateApi, customFormSchemaRules(type)).then(async (rules) => {
              rules && rules.length > 0 && (await updateSchema(rules));
            });
          }
        },
      );

      // 把扁平 streamSourceXxx 字段聚合回 protocolConfig.streamSource，
      // 后端类型安全 DTO 期望嵌套结构，前端展示用扁平更省事。
      function packStreamSource(params: Recordable): Recordable {
        const protocols = ['RTSP', 'ONVIF'];
        const isActive = protocols.includes(String(params.accessProtocol || '').toUpperCase());
        const out = { ...params };
        if (isActive) {
          const streamSource: Recordable = {};
          if (params.streamSourceUrl) streamSource.url = params.streamSourceUrl;
          if (params.streamSourceUsername) streamSource.username = params.streamSourceUsername;
          if (params.streamSourcePath) streamSource.streamPath = params.streamSourcePath;
          if (params.streamSourceRtpType) streamSource.rtpType = params.streamSourceRtpType;
          out.protocolConfig = { ...(params.protocolConfig || {}), streamSource };
        }
        delete out.streamSourceUrl;
        delete out.streamSourceUsername;
        delete out.streamSourcePath;
        delete out.streamSourceRtpType;
        return out;
      }

      async function handleSubmit() {
        try {
          const params = packStreamSource(await validate());
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

      return { type, t, registerModel, registerForm, handleSubmit };
    },
  });
</script>
