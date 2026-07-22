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
  import { ActionEnum } from '/@/enums/commonEnum';
  import { save, update } from '/@/api/iot/link/group/deviceGroup';
  import { editFormSchema } from './deviceGroupEdit.data';
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
      const [register, { setFieldsValue, resetFields, validate }] = useForm({
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
            // 「上级节点」展示的是 parentName,这里要按选中的父节点回填,否则永远停在默认「根节点」误导用户。
            // 根节点新增 / 顶层节点编辑时 parent 为空,回退到「根节点」文案。
            parentName:
              parent.title ||
              parent.groupName ||
              parent.group_name ||
              t('iot.link.group.deviceGroup.editDeviceGroup.rootNode'),
          });
        },
      );

      // 提交
      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });
          const params = await validate();
          // parentName 仅用于「上级节点」展示,后端按 parentId 建树 —— 提交时一律剔除,避免把展示字段发给后端
          delete params.parentName;
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
