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
      <template #formHeader>
        <div class="topic-content">
          <div class="topic-title">
            {{ t('iot.link.productTopic.productTopic.topicModal.title') }}
          </div>
          <div>
            {{ t('iot.link.productTopic.productTopic.topicModal.content') }}
          </div>
          <div class="topic-title1">
            {{ t('iot.link.productTopic.productTopic.topicModal.title1') }}
          </div>
          <div class="list">
            <div>
              <a-tag color="orange">${app_id}</a-tag> -
              <span>{{ t('iot.link.productTopic.productTopic.topicModal.parameter1') }}</span>
            </div>
            <div>
              <a-tag color="orange">${user_name}</a-tag> -
              <span>{{ t('iot.link.productTopic.productTopic.topicModal.parameter2') }}</span>
            </div>
            <div>
              <a-tag color="orange">${device_identification}</a-tag> -
              <span>{{ t('iot.link.productTopic.productTopic.topicModal.parameter3') }}</span>
            </div>
            <div>
              <a-tag color="orange">${device_sdk_version}</a-tag> -
              <span>{{ t('iot.link.productTopic.productTopic.topicModal.parameter4') }}</span>
            </div>
            <div>
              <a-tag color="orange">${product_identification} </a-tag> -
              <span>{{ t('iot.link.productTopic.productTopic.topicModal.parameter5') }}</span>
            </div>
          </div>
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { save, update } from '/@/api/iot/link/productTopic/productTopic';
  import { editFormSchema } from './customizeTopic.data';

  export default defineComponent({
    name: '编辑产品Topic',
    components: { BasicModal, BasicForm, ATag: Tag },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const productIdentification = ref('');
      const { createMessage } = useMessage();

      const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] =
        useForm({
          name: 'ProductTopicEdit',
          labelWidth: 120,
          schemas: editFormSchema(type),
          showActionButtonGroup: false,
          disabled: (_) => {
            return unref(type) === ActionEnum.VIEW;
          },
          baseColProps: { span: 11 },
          actionColOptions: {
            span: 23,
          },
        });

      const [registerModel, { setModalProps: setProps, closeModal: close }] = useModalInner(
        async (data) => {
          setProps({ confirmLoading: false });
          await resetSchema(editFormSchema(type));
          await resetFields();
          type.value = data?.type || ActionEnum.ADD;
          productIdentification.value = data.productIdentification;

          if (unref(type) !== ActionEnum.ADD) {
            // 赋值
            const record = { ...data?.record };
            record.functionType = String(record?.functionType);
            record.publisher = String(record?.publisher);
            record.subscriber = String(record?.subscriber);
            await setFieldsValue(record);
          }
        },
      );

      async function handleSubmit() {
        try {
          const params = await validate();
          params.productIdentification = productIdentification.value;
          setProps({ confirmLoading: true });

          if (unref(type) !== ActionEnum.VIEW) {
            if (unref(type) === ActionEnum.EDIT) {
              await update(params);
            } else {
              params.id = null;
              params.topicType = 1;
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
<style scoped lang="less">
  .topic-content {
    width: 100%;
    padding: 5px 10px;
    background-color: #f5f5f5;
    border-radius: 5px;

    .topic-title {
      font-size: 16px;
      font-weight: bold;
      color: #333;
    }

    .topic-title1 {
      font-size: 14px;
      font-weight: bold;
      color: #333;
      margin-top: 7px;
    }

    .list {
      div {
        margin-top: 5px;
        margin-left: 30px;
      }
    }
  }
</style>
../../../../../api/iot/link/productTopic/productTopic
