<template>
  <BasicModal
    v-bind="$attrs"
    @register="register"
    :title="t(`common.title.import`)"
    :maskClosable="false"
    :destroyOnClose="true"
    :keyboard="true"
    @cancel="handleCancel"
    @ok="handleOk"
    :showOkBtn="true"
    :okText="t('common.title.import')"
  >
    <div class="container">
      <div class="warning-box">
        <a-alert
          type="warning"
          :message="`⚠️ ${t('iot.link.device.device.import.tips')}`"
          :closable="false"
        >
          <template #description>
            <div>
              {{ t('iot.link.product.product.import.warningDescription.prefix') }}
              <a-tag color="red" style="margin: 0 4px">
                {{ t('iot.link.product.product.import.warningDescription.highlight') }}
              </a-tag>
              {{ t('iot.link.product.product.import.warningDescription.suffix') }}
            </div>
          </template>
        </a-alert>
      </div>
      <div class="form-item">
        <a-form ref="formRef" :model="formData" :rules="formRules">
          <a-form-item :label="t('iot.link.product.product.appId')" name="appId">
            <a-select
              v-model:value="formData.appId"
              :placeholder="t('common.chooseText') + t('iot.link.product.product.appId')"
              :options="appIdOptions"
              :fieldNames="{ label: 'name', value: 'id' }"
              allow-clear
            />
          </a-form-item>
        </a-form>
      </div>
      <a-upload
        :maxCount="1"
        :beforeUpload="beforeUpload"
        :file-list="fileList"
        :accept="['.json']"
        @remove="handleRemove"
      >
        <a-button type="primary" :disabled="!formData.appId">选择文件</a-button>
      </a-upload>
    </div>
  </BasicModal>
</template>

<script setup>
  import { ref, reactive, computed, unref, watch, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { importProductJsonFile } from '/@/api/iot/link/product/product';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { downloadByData } from '/@/utils/file/download';
  import { useDict } from '/@/components/Dict';
  import { DictEnum } from '/@/enums/commonEnum';

  const { getDictList } = useDict();
  const emit = defineEmits(['reload']);
  const { createMessage } = useMessage();
  const { t } = useI18n();
  const [register, { closeModal, changeLoading, setModalProps }] = useModalInner(() => {
    // 模态框打开时初始化确认按钮状态
    nextTick(() => {
      updateConfirmButtonState();
    });
  });
  const fileList = ref([]);
  const formRef = ref();

  // 应用场景选项
  const appIdOptions = computed(() => getDictList(DictEnum.LINK_APPLICATION_SCENARIO));

  const formData = reactive({
    appId: undefined,
  });

  const formRules = computed(() => ({
    appId: [
      {
        required: true,
        message: t('common.chooseText') + t('iot.link.product.product.appId'),
        trigger: 'change',
      },
    ],
  }));

  // 计算确认按钮是否可用
  const canConfirm = computed(() => {
    return formData.appId && fileList.value.length > 0;
  });

  // 监听文件列表和应用场景变化，更新确认按钮状态
  const updateConfirmButtonState = () => {
    nextTick(() => {
      setModalProps({
        okButtonProps: {
          disabled: !canConfirm.value,
        },
      });
    });
  };

  // 监听应用场景和文件列表变化，更新确认按钮状态
  watch(
    () => [formData.appId, fileList.value.length],
    () => {
      updateConfirmButtonState();
    },
  );

  const handleCancel = () => {
    resetModal();
  };

  // 处理文件删除
  const handleRemove = () => {
    fileList.value = [];
    updateConfirmButtonState();
  };

  // 重置并关闭弹窗
  const resetModal = () => {
    fileList.value = [];
    formData.appId = undefined;
    closeModal();
  };

  const beforeUpload = (file) => {
    // 只选择文件，不执行上传
    fileList.value = [file];
    updateConfirmButtonState();
    return false; // 阻止自动上传
  };

  // 处理确认按钮点击
  const handleOk = async () => {
    // 验证表单
    try {
      await unref(formRef)?.validateFields(['appId']);
    } catch (e) {
      createMessage.warning(t('common.chooseText') + t('iot.link.product.product.appId'));
      return;
    }
    // 确保应用场景已选择
    if (!formData.appId) {
      createMessage.warning(t('common.chooseText') + t('iot.link.product.product.appId'));
      return;
    }
    // 确保文件已选择
    if (fileList.value.length === 0) {
      createMessage.warning('请选择文件');
      return;
    }
    // 执行上传
    await handleUpload();
  };

  const handleUpload = async () => {
    changeLoading(true);
    const file = fileList.value[0];
    if (!file || !formData.appId) {
      createMessage.warning(t('common.chooseText') + t('iot.link.product.product.appId'));
      changeLoading(false);
      return;
    }
    try {
      const params = new FormData();
      params.append('appId', formData.appId);
      params.append('file', file);
      const response = await importProductJsonFile(params);
      // 检查返回的内容类型
      const contentDisposition = response.headers?.['content-disposition'];
      const contentType = response.headers?.['content-type'];
      const res = response.data;
      if (
        (contentDisposition && contentDisposition.includes('attachment')) ||
        contentType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
        contentType === 'application/json'
      ) {
        downloadByData(res, 'Product_Import_Error.json');
      } else {
        emit('reload');
        resetModal();
      }
    } catch (error) {
      console.log(error);
    } finally {
      changeLoading(false);
    }
  };
</script>

<style lang="less" scoped>
  .container {
    .warning-box {
      margin-bottom: 20px;
    }

    .form-item {
      margin: 20px 0;
    }
  }
</style>
