<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    @ok="handleSubmit"
    :keyboard="true"
  >
    <BasicForm @register="registerForm">
      <template #testConnection>
        <a-button
          type="primary"
          ghost
          :loading="testLoading"
          preIcon="ant-design:api-outlined"
          @click="handleTestConnection"
        >
          {{ t('video.media.server.testConnection') }}
        </a-button>
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
  import { Api, save, update, testConnection } from '/@/api/video/media/server';
  import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
  import { customFormSchemaRules, editFormSchema } from './server.data';

  export default defineComponent({
    name: 'VideoMediaServerEdit',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();
      const testLoading = ref(false);

      const [registerForm, { setFieldsValue, resetFields, updateSchema, validate, resetSchema }] =
        useForm({
          name: 'VideoMediaServerEdit',
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
          setProps({ confirmLoading: false });
          await resetSchema(editFormSchema(type));
          await resetFields();
          type.value = data?.type || ActionEnum.ADD;

          if (unref(type) !== ActionEnum.ADD) {
            // 赋值
            const record = { ...data?.record };
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

          if (unref(type) !== ActionEnum.VIEW) {
            if (unref(type) === ActionEnum.EDIT) {
              await update(params);
            } else {
              delete params.id;
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

      async function handleTestConnection() {
        const values = await validate();
        const { host, httpPort, secret } = values;
        if (!host || !httpPort || !secret) {
          createMessage.warning(t('video.media.server.testConnectionTip'));
          return;
        }
        testLoading.value = true;
        try {
          const result = await testConnection(host, httpPort, secret);
          if (result) {
            createMessage.success(t('video.media.server.testConnectionSuccess'));
          } else {
            createMessage.error(t('video.media.server.testConnectionFail'));
          }
        } catch (e) {
          errorMsg(t('video.media.server.testConnectionFail'));
        } finally {
          testLoading.value = false;
        }
      }

      return { type, t, testLoading, registerModel, registerForm, handleSubmit, handleTestConnection };
    },
  });
</script>
