<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    :destroyOnClose="true"
    @ok="handleSubmit"
    :keyboard="true"
    :width="'40%'"
  >
    <BasicForm @register="register" />
  </BasicModal>
</template>
<script lang="ts">
import { defineComponent, ref, unref } from 'vue';
import { Card } from 'ant-design-vue';
import { useI18n } from '/@/hooks/web/useI18n';
import { useMessage } from '/@/hooks/web/useMessage';
import { BasicForm, useForm } from '/@/components/Form';
import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
import { Api, save, update } from '/@/api/iot/link/group/deviceGroup';
import { customFormSchemaRules, editFormSchema } from './deviceGroupEdit.data';
import { BasicModal, useModalInner } from '/@/components/Modal';

export default defineComponent({
  name: 'DeviceGroupEdit',
  components: { BasicForm, [Card.name]: Card, BasicModal },
  emits: ['success'],
  setup(_, { emit }) {
    const { t } = useI18n();
    const { createMessage } = useMessage();
    const type = ref(ActionEnum.ADD);

    const groupValue = ref({});
    const [register, { setFieldsValue, resetFields, updateSchema, validate, setProps }] = useForm({
      name: 'group',
      labelWidth: 100,
      showActionButtonGroup: false,
      schemas: editFormSchema(),
      baseColProps: { span: 24 },
    });

    const [registerModal, { setModalProps, closeModal: close }] = useModalInner(
      async ({ type: actionType, parent = {}, record = {} }) => {
        type.value = actionType;
        groupValue.value = { ...record };
        await setFieldsValue({
          ...record,
          parentId: parent.id || '0',
        });
      },
    );

    // 提交
    async function handleSubmit() {
      try {
        setModalProps({ confirmLoading: true });
        const params = await validate();
        // TODO Ab 国际化
        if (params.parentName === '根节点') {
          delete params.parentName;
        }
        if (unref(type) === ActionEnum.EDIT) {
          await update({
            ...params,
            id: groupValue.value.id,
          });
        } else {
          params.id = undefined;
          const fn = unref(type) === ActionEnum.EDIT ? update : save;
          await fn(params);
        }
        createMessage.success(t(`common.tips.${type.value}Success`));

        await resetFields();
        close();
        emit('success');
      } finally {
        setModalProps({ confirmLoading: false });
      }
    }

    return {
      register,
      resetFields,
      registerModal,
      handleSubmit,
      t,
      type,
    };
  },
});
</script>
