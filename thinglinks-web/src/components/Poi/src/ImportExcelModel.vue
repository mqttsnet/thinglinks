<template>
  <BasicModal
    v-bind="{ ...$attrs, ...$props }"
    :title="t('component.poi.importModalTitle')"
    @ok="handleOk"
    @cancel="handleCancel"
    :maskClosable="false"
    @register="registerModal"
  >
    <BasicForm :labelWidth="100" :showActionButtonGroup="false" @register="registerForm" />
    <slot></slot>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, h, ref, toRefs, unref } from 'vue';
  import { Button, Upload } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form/index';
  import { isFunction } from '/@/utils/is';
  import { warn } from '/@/utils/log';
  import { checkFileType } from '/@/components/Upload/src/helper';
  import { useUploadType } from '/@/components/Upload/src/useUpload';

  const { t } = useI18n();

  const schemas = (props, fileList, beforeUpload: Fn, removeFile: Fn): FormSchema[] => {
    return [
      {
        field: 'templateHref',
        component: 'Input',
        label: t('component.poi.templateHref'),
        show: () => {
          return props.templateHref;
        },
        render: () => {
          return h(
            'a',
            {
              href: props.templateHref,
              target: '_blank',
            },
            props.templateTitle,
          );
        },
      },
      {
        field: 'filename',
        component: 'Input',
        label: t('component.upload.fileName'),
        render: ({ model, field }) => {
          return h(
            Upload,
            {
              value: model[field],
              beforeUpload: beforeUpload,
              remove: removeFile,
              showUploadList: true,
              fileList,
              props,
            },
            h(Button, t('component.upload.upload')),
          );
        },
      },
    ];
  };

  export default defineComponent({
    name: 'ImportExcelModel',
    components: { BasicModal, BasicForm, [Button.name]: Button, [Upload.name]: Upload },
    props: {
      api: {
        type: Function as PropType<(arg?: Recordable) => Promise<any>>,
        default: null,
      },
      // api params
      params: {
        type: Object as PropType<Recordable>,
        default: () => ({}),
      },
      // 文件最大多少MB
      maxSize: {
        type: Number as PropType<number>,
        default: 2,
      },
      // 最大数量的文件，Infinity不限制
      maxNumber: {
        type: Number as PropType<number>,
        default: 1,
      },
      // 根据后缀，或者其他
      accept: {
        type: Array as PropType<string[]>,
        default: () => ['xls', 'xlsx'],
      },
      templateHref: {
        type: String,
      },
      templateTitle: {
        type: String,
        default: t('component.poi.templateTitle'),
      },
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const fileList = ref<any[]>([]);
      const { createMessage } = useMessage();
      const beforeUpload = (file: any) => {
        fileList.value = [...fileList.value, file];
        return false;
      };
      const removeFile = (file) => {
        const index = fileList.value.findIndex((item) => item.uid === file.uid);
        index !== -1 && fileList.value.splice(index, 1);
        return true;
      };

      const [registerForm] = useForm({
        schemas: schemas(props, fileList, beforeUpload, removeFile),
      });
      const [registerModal, { setModalProps, closeModal }] = useModalInner();

      const { accept, maxNumber, maxSize } = toRefs(props);
      const { getAccept } = useUploadType({
        acceptRef: accept,
        helpTextRef: ref<string>(''),
        maxNumberRef: maxNumber,
        maxSizeRef: maxSize,
      });

      async function handleOk() {
        const api = props.api;
        if (!api || !isFunction(api)) {
          return warn('upload api must exist and be a function');
        }

        const params = { ...props.params };
        params.file = fileList.value[0];

        if (!params.file) {
          createMessage.error(t('component.poi.pleaseSelectFile'));
          return false;
        }

        const { size } = params.file;
        const { maxSize } = props;
        const accept = unref(getAccept);
        // 设置最大值，则判断
        if (maxSize && size / 1024 / 1024 >= maxSize) {
          createMessage.error(t('component.upload.maxSizeMultiple', [maxSize]));
          return false;
        }

        // 设置类型,则判断
        if (accept.length > 0 && !checkFileType(params.file, accept)) {
          createMessage.error(t('component.upload.acceptUpload', [accept.join(',')]));
          return false;
        }

        try {
          setModalProps({ confirmLoading: true });
          const res = await api(params);
          if (!res?.data?.isSuccess) {
            createMessage.warning(res.data.msg);
          }
          emit('success', { res });
          fileList.value = [];
          closeModal();
        } catch (error) {
          console.warn(error);
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      function handleCancel() {
        fileList.value = [];
      }

      return {
        handleOk,
        handleCancel,
        registerForm,
        registerModal,
        t,
      };
    },
  });
</script>
