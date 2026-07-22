<template>
  <BasicModal v-bind="$attrs" @register="registerModel" :title="t(`common.title.${type}`)" :maskClosable="false"
    @ok="handleSubmit" :keyboard="true" :width="820">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
import { defineComponent, ref, unref, computed } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { useI18n } from '/@/hooks/web/useI18n';
import { useMessage } from '/@/hooks/web/useMessage';
import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
import { Api, save, update } from '../../../../../api/iot/rule/engine/chained/chained';
import { customFormSchemaRules, editFormSchema } from './chained.data';
import { useUserStore } from '/@/store/modules/user';

export default defineComponent({
  name: '编辑链接规则',
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

    // console.log(getUserInfo.value,'getUserInfo')

    const [registerForm, { setFieldsValue, resetFields, updateSchema, validate, resetSchema }] =
      useForm({
        name: 'ChainedEdit',
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
        setProps({ confirmLoading: false, width: 1000 });
        await resetSchema(editFormSchema(type));
        await resetFields();
        type.value = data?.type || ActionEnum.ADD;

        if (unref(type) !== ActionEnum.ADD) {
          // 赋值
          let record = { ...data?.record };
          record.type = String(record?.type);
          record.status = String(record?.status);
          await setFieldsValue(record);
        } else {
          await setFieldsValue({ createdOrgId: getUserInfo.value?.baseEmployee?.createdOrgId });
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
../../../../../api/iot/link/chained/chained