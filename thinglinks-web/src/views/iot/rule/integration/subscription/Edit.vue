<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    :width="1000"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { detail, save, update } from '/@/api/iot/rule/integration/subscriptionSource';
  import { editFormSchema } from './subscription.data';

  defineOptions({ name: '编辑订阅源' });

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n();
  const { createMessage } = useMessage();

  const type = ref<ActionEnum>(ActionEnum.ADD);

  const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] = useForm({
    name: 'SubscriptionSourceEdit',
    labelWidth: 150,
    schemas: editFormSchema(type),
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
  });

  const [registerModel, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    // ⚠ 关键顺序：先确定 type（影响 dynamicDisabled），再 resetSchema
    // 兼容两种调用方约定：BusinessCardList 传 type=EDIT，老调用方传 isUpdate=true
    const incomingType = (data?.type as ActionEnum) ?? (data?.isUpdate ? ActionEnum.EDIT : ActionEnum.ADD);
    type.value = incomingType;
    await resetSchema(editFormSchema(type));
    await resetFields();

    const isLoadDetail = incomingType !== ActionEnum.ADD && data?.record?.id;
    if (isLoadDetail) {
      const record = await detail(data.record.id);
      // mappingJson 后端可能返回原始数组，需序列化成字符串供 InputTextArea 编辑
      const mapping = record?.mappingJson;
      const patched: any = { ...record };
      if (mapping !== undefined && mapping !== null && typeof mapping !== 'string') {
        patched.mappingJson = JSON.stringify(mapping, null, 2);
      }
      await setFieldsValue(patched);
    }
  });

  async function handleSubmit() {
    try {
      const params = await validate();
      setModalProps({ confirmLoading: true });
      if (unref(type) === ActionEnum.EDIT) {
        await update(params);
        createMessage.success(t('common.tips.editSuccess'));
      } else {
        params.id = null;
        await save(params);
        createMessage.success(t('common.tips.addSuccess'));
      }
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
