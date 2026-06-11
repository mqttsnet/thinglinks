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
      <div class="tips_title">{{ t('iot.link.device.device.import.tips') }}</div>
      <div class="market">{{ t('iot.link.device.device.import.description') }}</div>
      <div class="item">
        <div class="title"
          >{{ t('iot.link.device.device.import.step1[0]')
          }}<span class="tip">{{ t('iot.link.device.device.import.step1[1]') }}</span></div
        >
        <div class="description">
          <ul>
            <li
              >{{ t('iot.link.device.device.import.step1[2]') }}
              <a
                @click="
                  downloadByUrlFromPublic(fileUrl, 'Device_Records_Import_Template_v1.0.xlsx')
                "
                >Device_Records_Import_Template_v1.0.xlsx</a
              >
              {{ t('iot.link.device.device.import.step1[3]') }}</li
            >
          </ul>
        </div>
      </div>
      <div class="item">
        <div class="title">{{ t('iot.link.device.device.import.step2[0]') }}</div>
        <div class="description">
          <ul>
            <li>{{ t('iot.link.device.device.import.step2[1]') }}</li>
            <li>{{ t('iot.link.device.device.import.step2[2]') }}</li>
            <li>{{ t('iot.link.device.device.import.step2[3]') }}</li>
          </ul>
        </div>
      </div>
      <div class="item">
        <div class="title">{{ t('iot.link.device.device.import.step3[0]') }}</div>
        <div class="description">
          <span>{{ t('iot.link.device.device.import.step3[1]') }}</span>
          <ul>
            <li>{{ t('iot.link.device.device.import.step3[2]') }}</li>
            <li>{{ t('iot.link.device.device.import.step3[3]') }}</li>
            <li>{{ t('iot.link.device.device.import.step3[4]') }}</li>
          </ul>
        </div>
      </div>
      <div class="item">
        <div class="title">{{ t('iot.link.device.device.import.step4[0]') }}</div>
        <div class="description">
          <ul>
            <li>{{ t('iot.link.device.device.import.step4[1]') }}</li>
            <li>{{ t('iot.link.device.device.import.step4[2]') }}</li>
            <li>{{ t('iot.link.device.device.import.step4[3]') }}</li>
            <li>{{ t('iot.link.device.device.import.step4[4]') }}</li>
          </ul>
        </div>
      </div>
      <a-upload
        :maxCount="1"
        :beforeUpload="beforeUpload"
        :file-list="fileList"
        :accept="['.xls', '.xlsx']"
        @remove="handleRemove"
      >
        <a-button type="primary">选择文件</a-button>
      </a-upload>
    </div>
  </BasicModal>
</template>

<script setup>
  import { ref, computed, watch, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { downloadByUrlFromPublic } from '/@/utils/file/download';
  import { importDeviceFile } from '/@/api/iot/link/device/device';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { downloadByData } from '/@/utils/file/download';
  const fileUrl = ref(
    `${
      import.meta.env.BASE_URL || '/'
    }template/iot/link/device/Device_Records_Import_Template_v1.0.xlsx`,
  );
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

  // 计算确认按钮是否可用
  const canConfirm = computed(() => {
    return fileList.value.length > 0;
  });

  // 更新确认按钮状态
  const updateConfirmButtonState = () => {
    nextTick(() => {
      setModalProps({
        okButtonProps: {
          disabled: !canConfirm.value,
        },
      });
    });
  };

  // 监听文件列表变化，更新确认按钮状态
  watch(
    () => fileList.value.length,
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
    const formData = new FormData();
    fileList.value.forEach((file) => {
      formData.append('file', file);
    });
    try {
      const response = await importDeviceFile(formData);
      // 检查返回的内容类型
      const contentDisposition = response.headers['content-disposition'];
      const contentType = response.headers['content-type'];
      const res = await response.data;
      if (
        (contentDisposition && contentDisposition.includes('attachment')) ||
        contentType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      ) {
        // 导入失败：不关闭弹窗，不清空文件列表
        createMessage.error(t('common.tips.importFail'));
        downloadByData(res, 'Device_Records_Import_Template_v1.0.xlsx');
      } else {
        // 导入成功：关闭弹窗，清空文件列表
        createMessage.success(t('common.tips.importSuccess'));
        emit('reload');
        resetModal();
      }
    } catch (error) {
      // 导入失败：不关闭弹窗，不清空文件列表
      createMessage.error(t('common.tips.importFail'));
    } finally {
      changeLoading(false);
    }
  };
</script>

<style lang="less" scoped>
  .container {
    .tips_title {
      font-size: 17px;
      font-weight: 500;
    }

    .market {
      font-size: 14px;
      margin: 8px 0 10px 0;
    }

    .item {
      .title {
        color: #333;
        font-weight: 500;

        .tip {
          font-size: 12px;
          color: red;
        }
      }

      .description {
        color: #444;
        font-size: 13px;
      }
    }
  }
</style>
../../../../../api/iot/link/device/device
