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
      <template #customInfo="{ model, field }">
        <div class="editor_container">
          <FormItem :name="field" :helpMessage="'1111'">
            <template #label>
              <div>{{ t('iot.link.ota.otaUpgrades.customInfo') }}</div>
              <BasicHelp
                class="mr-1"
                :text="t('iot.link.ota.otaUpgrades.helpMessage.customInfo')"
                showIndex
                placement="left"
              />
            </template>
            <a-textarea
              :disabled="type === ActionEnum.VIEW"
              v-model:value="model[field]"
              :placeholder="t('iot.link.ota.otaUpgrades.placeholder.customInfoPlaceholder')"
            />
          </FormItem>
          <a-button
            :disabled="type === ActionEnum.VIEW"
            class="editor_btn"
            @click="openCodeModal(model[field])"
          >
            <SvgIcon name="iot-link-ota-upgradeCodeEditor" />
          </a-button>
        </div>
      </template>
    </BasicForm>
  </BasicModal>
  <triggerRule @register="registerModalProduct" @success="handleSuccess" />
  <codeEditorDefine @register="registerModalCode" @submit-editor="submitEditor" />
</template>
<script lang="ts">
  import { Form } from 'ant-design-vue';
  import { BasicHelp } from '/@/components/Basic';
  import { useModal } from '/@/components/Modal';
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum, VALIDATE_API } from '/@/enums/commonEnum';
  import { Api, save, update } from '/@/api/iot/link/ota/otaUpgrades';
  import { getValidateRules } from '/@/api/thinglinks/common/formValidateService';
  import { customFormSchemaRules, editFormSchema } from './otaUpgrades.data';
  import triggerRule from './modal/triggerRule.vue';
  import { findTenantFileInfoByIds } from '/@/api/thinglinks/file/upload';
  import codeEditorDefine from './modal/codeEditorDefine.vue';
  import SvgIcon from '/@/components/Icon/src/SvgIcon.vue';

  export default defineComponent({
    name: '编辑OTA资源',
    components: {
      BasicModal,
      BasicForm,
      triggerRule,
      FormItem: Form.Item,
      codeEditorDefine,
      SvgIcon,
      BasicHelp,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { createMessage } = useMessage();
      const [registerModalProduct, { openModal }] = useModal();
      const [registerModalCode, { openModal: openCode }] = useModal();
      const [
        registerForm,
        { getFieldsValue, setFieldsValue, updateSchema, validate, resetSchema },
      ] = useForm({
        name: 'otaUpgrades',
        labelWidth: 140,
        schemas: editFormSchema(type, {
          productIdentification: (value: string) =>
            openModal(true, { productIdentification: value, type: ActionEnum.EDIT }),
        }),
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
          type.value = data?.type || ActionEnum.ADD;
          await resetSchema(
            editFormSchema(type, {
              productIdentification: (value: string) =>
                openModal(true, { productIdentification: value, type: ActionEnum.EDIT }),
            }),
          );

          if (unref(type) !== ActionEnum.ADD) {
            // 赋值

            const record = { ...data?.record };
            record.packageType = record?.packageType;
            record.status = record?.status;
            record.fileLocation = await findTenantFileInfoByIds(
              record?.fileLocation?.split(',') ?? [],
            );
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

      async function handleSubmit() {
        try {
          const params = await validate();
          params.fileLocationInfo = params.fileLocation;
          params.fileLocation = params.fileLocation.map((item: any) => item?.id)?.join(',');
          params.customInfo =
            params.customInfo && typeof params.customInfo !== 'string'
              ? JSON.stringify(params.customInfo)
              : params.customInfo
              ? params.customInfo
              : '';
          console.log('params.customInfo', params.customInfo);
          console.log('params.customInfo', typeof params.customInfo);
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
      function handleSuccess(val) {
        setFieldsValue({ productIdentification: val?.productIdentification });
      }
      function addCustomInfo() {
        const originCustomInfo = getFieldsValue()?.customInfo || [];
        const customItem = { key: '', value: '', id: Date.now() };
        setFieldsValue({ customInfo: [...originCustomInfo, customItem] });
      }
      function removeCustomItem(item: any) {
        let originCustomInfo = getFieldsValue()?.customInfo || [];
        const index = originCustomInfo.findIndex((_v: any) => _v.id === item.id);

        if (index > -1) {
          originCustomInfo = originCustomInfo.filter((_v: any) => _v.id !== item.id);
          setFieldsValue({ customInfo: originCustomInfo });
        }
      }
      const openCodeModal = (value: string) => {
        openCode(true, { value: value });
      };
      const submitEditor = async (value) => {
        await setFieldsValue({ customInfo: value });
      };
      return {
        type,
        t,
        registerModel,
        registerForm,
        handleSubmit,
        registerModalProduct,
        handleSuccess,
        addCustomInfo,
        removeCustomItem,
        registerModalCode,
        openCodeModal,
        submitEditor,
        ActionEnum,
      };
    },
  });
</script>
<style lang="less" scoped>
  .editor_container {
    display: flex;
    align-items: center;

    .editor_btn {
      width: 50px;
      margin-left: 10px;
    }
  }

  :deep(.ant-col .ant-form-item-label) {
    width: 120px;
  }

  :deep(.ant-col .ant-form-item-control) {
    flex: 1;
  }

  :deep(.ant-row .ant-form-item) {
    flex: 1;
  }
</style>
../../../../../api/iot/link/ota/otaUpgrades
