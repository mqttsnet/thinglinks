<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    @ok="handleSubmit"
    :keyboard="true"
    wrapClassName="md-edit-wrap"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { save, update } from '/@/api/iot/link/productCommand/productCommand';
  import { editFormSchema } from './productCommand.data';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    name: '编辑产品模型设备服务命令表维护',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();
      const userStore = useUserStore();
      const getUserInfo = computed(() => {
        return userStore.getUserInfo;
      });
      const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] = useForm({
        name: 'ProductCommandEdit',
        labelWidth: 100,
        schemas: editFormSchema(type),
        showActionButtonGroup: false,
        disabled: (_) => {
          return unref(type) === ActionEnum.VIEW;
        },
        baseColProps: { span: 12 },
        actionColOptions: {
          span: 24,
        },
      });

      const [registerModel, { setModalProps: setProps, closeModal: close }] = useModalInner(
        async (data) => {
          setProps({ confirmLoading: false, getContainer: data?.getContainer });
          await resetSchema(editFormSchema(type));
          await resetFields();
          type.value = data?.type || ActionEnum.ADD;

          if (unref(type) !== ActionEnum.ADD) {
            // 赋值
            const record = { ...data?.record };
            await setFieldsValue(record);
          } else {
            await setFieldsValue({
              serviceId: data?.serviceId,
              createdOrgId: getUserInfo.value?.baseEmployee?.createdOrgId,
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

      return { type, t, registerModel, registerForm, handleSubmit };
    },
  });
</script>
