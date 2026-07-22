<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t(`common.title.${type}`)"
    :maskClosable="false"
    @ok="handleSubmit"
    :keyboard="true"
    :width="900"
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
  import { save, update } from '/@/api/video/platform/info';
  import { editFormSchema } from './info.data';
  import citiesGd from '/@/utils/thinglinks/citiesGd.json';

  export default defineComponent({
    name: 'VideoPlatformInfoEdit',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();

      const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] =
        useForm({
          name: 'VideoPlatformEdit',
          labelWidth: 140,
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
            const record = { ...data?.record };
            // 根据 civilCode 反查级联选择器的值
            if (record.civilCode) {
              record.civilCodeArea = findCascaderValue(record.civilCode);
            }
            await setFieldsValue(record);
          }
        },
      );

      function findCascaderValue(code: string): string[] {
        for (const province of citiesGd) {
          if (province.value === code) return [province.value];
          if (province.children) {
            for (const city of province.children) {
              if (city.value === code) return [province.value, city.value];
              if (city.children) {
                for (const district of city.children) {
                  if (district.value === code) return [province.value, city.value, district.value];
                }
              }
            }
          }
        }
        return [];
      }

      async function handleSubmit() {
        try {
          const params = await validate();
          setProps({ confirmLoading: true });

          if (unref(type) !== ActionEnum.VIEW) {
            delete params.civilCodeArea;
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

      return { type, t, registerModel, registerForm, handleSubmit };
    },
  });
</script>
