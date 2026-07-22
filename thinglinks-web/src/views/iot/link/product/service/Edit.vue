<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    @ok="handleSubmit"
    :keyboard="true"
    width="720px"
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
  import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
  import { Api, save, update, get } from '/@/api/iot/link/productService/productService';
  import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
  import { customFormSchemaRules, editFormSchema } from './productService.data';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    name: '编辑产品服务',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const productId = ref<string>('');
      const { createMessage } = useMessage();
      const userStore = useUserStore();
      const getUserInfo = computed(() => {
        return userStore.getUserInfo;
      });
      const [registerForm, { setFieldsValue, resetFields, updateSchema, validate, resetSchema }] =
        useForm({
          name: 'ProductServiceEdit',
          // 标签宽度 100 + 双列(span 12)布局,中文 4 字标签不会被压缩成竖排
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
          console.log(data);
          productId.value = data?.productId;
          setProps({ confirmLoading: false });
          await resetSchema(editFormSchema(type));
          await resetFields();
          type.value = data?.type || ActionEnum.ADD;

          if (unref(type) !== ActionEnum.ADD) {
            // 赋值
            const res = await get(data?.serviceId);
            await setFieldsValue({ ...res });
          } else {
            await setFieldsValue({ createdOrgId: getUserInfo.value?.baseEmployee?.createdOrgId });
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
              params.productId = productId.value;
              await update(params);
            } else {
              params.id = null;
              params.productId = productId.value;
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
../../../../../api/iot/link/productService/productService
