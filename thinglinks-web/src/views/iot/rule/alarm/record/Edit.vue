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
  import { defineComponent, ref, unref, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
  import { solve } from '../../../../../api/iot/rule/alarm/record';
  import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
  import {
    customFormSchemaRules,
    editFormSchema,
    viewFormSchema,
    editresolutionFormSchema,
  } from './alarmRecord.data';

  export default defineComponent({
    name: '处理告警',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();
      const [registerForm, { setFieldsValue, resetFields, updateSchema, validate, resetSchema }] =
        useForm({
          name: 'solveForm',
          labelWidth: 100,
          schemas: editFormSchema(type),
          showActionButtonGroup: false,
          disabled: (_) => unref(type) === ActionEnum.VIEW,
          baseColProps: { span: 24 },
          actionColOptions: {
            span: 23,
          },
        });

      const [registerModel, { setModalProps: setProps, closeModal: close }] = useModalInner(
        async (data) => {
          setProps({ confirmLoading: false });
          if (data?.record && data.type === ActionEnum.EDIT) {
            const handledStatus = data.record.handledStatus;
            let schema;
            if (handledStatus == 0) {
              schema = editFormSchema();
            } else if (handledStatus == 1) {
              schema = editresolutionFormSchema();
            }
            await resetSchema(schema);
          } else if (data?.record && data.type === ActionEnum.ADD) {
            await resetSchema(viewFormSchema());
          }
          await resetFields();
          type.value = data?.type || ActionEnum.ADD;
          if (unref(type) !== ActionEnum.ADD) {
            // 赋值
            const record = { ...data?.record };
            await setFieldsValue(record);
          }
        },
      );

      async function handleSubmit() {
        try {
          const params = await validate();
          setProps({ confirmLoading: true });
          if (params.handledStatus == 0) {
            params.handledStatus = '1';
          } else {
            params.handledStatus = '2';
          }
          console.log(params);
          // 假设你有一个solve函数来处理表单数据
          // 注意：这里移除了return false，以便继续执行后续代码
          await solve(params);
          createMessage.success(t(`common.tips.${type.value}Success`));
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
../../../../../api/iot/link/alarmRecord/record
