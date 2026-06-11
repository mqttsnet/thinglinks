<template>
  <div class="card-wrap" :bordered="false">
    <div class="card-title">
      <div>{{ t('iot.link.ota.otaUpgradeTasks.upgradeTaskDetail') }}</div>
      <div class="card-id">
        {{ t('iot.link.ota.otaUpgradeTasks.taskId') }}：{{ taskDetail.id }}
        <CopyOutlined @click="handleCopyTaskId" />
      </div>
    </div>
    <Description @register="registerUpgradeTask" />
  </div>
  <div :bordered="false" class="mt-5 card-wrap">
    <div class="card-title">{{ t('iot.link.ota.otaUpgradeTasks.upgradePackageDetail') }}</div>
    <Description @register="registerUpgradePackage">
      <template #fileLocation>
        <div v-if="fileList && fileList.length > 0" class="file-list">
          <BasicUpload :value="fileList" :onlyShowPreview="true" :showDeleteBtn="false"></BasicUpload>
        </div>
        <span v-else class="empty-text">-</span>
      </template>
    </Description>
  </div>
  <div :bordered="false" class="mt-5 card-wrap">
    <div class="card-title">{{ t('iot.link.ota.otaUpgradeTasks.upgradeScope') }}</div>
    <div class="upgrade-scope-content">
      <div v-if="upgradeScope === 0" class="upgrade-scope-item">
        <span class="upgrade-scope-label"
          >{{ t('iot.link.ota.otaUpgradeTasks.productIdentification') }}：</span
        >
        <span class="upgrade-scope-value">{{
          upgradeTask?.otaUpgradesResult?.productIdentification || '-'
        }}</span>
      </div>
      <div v-else-if="upgradeScope === 1" class="upgrade-scope-item">
        <span class="upgrade-scope-label"
          >{{ t('iot.link.ota.otaUpgradeTasks.directUpgrade') }}({{
            t('iot.link.device.device.deviceIdentification')
          }})：</span
        >
        <div class="upgrade-scope-value">
          <a-tag v-for="(item, index) in targetValueList" :key="index" class="scope-tag">
            {{ item }}
          </a-tag>
          <span v-if="!targetValueList || targetValueList.length === 0" class="empty-text">-</span>
        </div>
      </div>
      <div v-else-if="upgradeScope === 2" class="upgrade-scope-item">
        <span class="upgrade-scope-label"
          >{{ t('iot.link.ota.otaUpgradeTasks.groupUpgrade') }}({{
            t('iot.link.ota.otaUpgradeTasks.groupId')
          }})：</span
        >
        <div class="upgrade-scope-value">
          <a-tag v-for="(item, index) in targetValueList" :key="index" class="scope-tag">
            {{ item }}
          </a-tag>
          <span v-if="!targetValueList || targetValueList.length === 0" class="empty-text">-</span>
        </div>
      </div>
      <div v-else-if="upgradeScope === 3" class="upgrade-scope-item">
        <span class="upgrade-scope-label"
          >{{ t('iot.link.ota.otaUpgradeTasks.areaUpgrade') }}({{
            t('iot.link.ota.otaUpgradeTasks.areaCode')
          }})：</span
        >
        <div class="upgrade-scope-value">
          <div v-if="areaList && areaList.length > 0" class="area-list">
            <div v-for="(item, index) in areaList" :key="index" class="area-item">
              <span class="area-value">{{ item.provinceName || item.provinceCode || '-' }}</span>
              <span class="area-separator">｜</span>
              <span class="area-value">{{ item.cityName || item.cityCode || '-' }}</span>
            </div>
          </div>
          <span v-else class="empty-text">-</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { ref, computed, watch } from 'vue';
  import { Description, useDescription } from '/@/components/Description/index';
  import { getUpgradeTaskSchema, getUpgradePackageSchema } from './otaUpgradeTaskDetail.data';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { findTenantFileInfoByIds } from '/@/api/thinglinks/file/upload';
  import { downloadByUrlFromPublic } from '/@/utils/file/download';
  import { DownloadOutlined, CopyOutlined } from '@ant-design/icons-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import { BasicUpload } from '/@/components/Upload';

  const props = defineProps<{
    taskDetail?: any;
  }>();

  const { t } = useI18n();
  const { createMessage } = useMessage();

  const upgradeTask = ref<any>({});
  const upgradePackage = ref({});
  const fileList = ref<any[]>([]);

  const upgradeScope = computed(() => {
    return upgradeTask.value?.upgradeScope ?? upgradeTask.value?.echoMap?.upgradeScope;
  });

  const targetValueList = computed(() => {
    return upgradeTask.value?.targetValueList || [];
  });

  const areaList = computed(() => {
    if (upgradeScope.value !== 3 || !targetValueList.value || targetValueList.value.length === 0) {
      return [];
    }
    return targetValueList.value.map((item: string) => {
      const [provinceCode, cityCode] = item.split(',');
      return {
        provinceCode,
        cityCode,
        provinceName: '', // 如果需要显示名称，需要调用区域API
        cityName: '', // 如果需要显示名称，需要调用区域API
      };
    });
  });

  const handleCopyTaskId = () => {
    handleCopyTextV2(props.taskDetail.id);
  };

  const handleDownloadFile = async (file: any) => {
    try {
      if (file?.url) {
        await downloadByUrlFromPublic(file?.url, file?.uniqueFileName);
      } else {
        createMessage.warning(t('common.tips.downloadFail'));
      }
    } catch (error) {
      console.error('下载文件失败:', error);
      createMessage.error(t('common.tips.downloadFail'));
    }
  };

  // 初始化数据
  const initData = async (taskData: any) => {
    if (!taskData) return;

    upgradeTask.value = Object.assign(upgradeTask.value, taskData);
    upgradePackage.value = upgradeTask.value?.otaUpgradesResult;

    // 获取文件信息
    const fileLocation = upgradePackage.value?.fileLocation;
    if (fileLocation) {
      try {
        const fileIds = fileLocation.split(',').filter((id: string) => id);
        if (fileIds.length > 0) {
          const files = await findTenantFileInfoByIds(fileIds);
          fileList.value = Array.isArray(files) ? files : [];
        }
      } catch (error) {
        console.error('获取文件信息失败:', error);
        fileList.value = [];
      }
    }
  };

  // 监听 props 变化，immediate: true 会在组件初始化时执行一次
  watch(
    () => props.taskDetail,
    (newVal) => {
      if (newVal) {
        initData(newVal);
      }
    },
    { immediate: true },
  );

  const [registerUpgradeTask] = useDescription({
    data: upgradeTask,
    schema: getUpgradeTaskSchema(),
    column: 2,
  });
  const [registerUpgradePackage] = useDescription({
    data: upgradePackage,
    schema: getUpgradePackageSchema(),
    column: 2,
  });
</script>
<style scoped lang="less">
  .upgrade-scope-content {
    padding: 8px 0;
  }

  .upgrade-scope-item {
    display: flex;
    align-items: flex-start;
    min-height: 32px;
    line-height: 32px;
  }

  .upgrade-scope-label {
    min-width: 140px;
    font-weight: 600;
    font-size: 14px;
    color: rgba(0, 0, 0, 0.85);
    flex-shrink: 0;
    line-height: 32px;
  }

  .upgrade-scope-value {
    flex: 1;
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
    line-height: 32px;
    min-height: 32px;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  .scope-tag {
    margin: 0;
    line-height: 22px;
  }

  .area-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }

  .area-item {
    padding: 6px 12px;
    background: #fafafa;
    border-radius: 4px;
    display: inline-flex;
    align-items: center;
    line-height: 20px;
    min-height: auto;
  }

  .area-value {
    color: rgba(0, 0, 0, 0.85);
    font-size: 14px;
  }

  .area-separator {
    color: rgba(0, 0, 0, 0.25);
    margin: 0 8px;
    font-size: 14px;
  }

  .empty-text {
    color: rgba(0, 0, 0, 0.25);
    font-size: 14px;
  }

  .file-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .file-item {
    display: flex;
    align-items: center;
  }

  .card-title {
    font-size: 16px;
    font-weight: 500;
    color: rgba(0, 0, 0, 0.85);
    margin: 16px 0;
    margin-top: 0;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    line-height: 1.5;

    .card-id {
      font-size: 14px;
      font-weight: 400;
      margin-top: 4px;
    }
  }

  .card-wrap {
    padding: 0 16px;
  }

  // 强制统一两个 Description 组件的 label 宽度
  :deep(.ant-descriptions-item-label) {
    min-width: 80px !important;
    max-width: 80px !important;
    width: 80px !important;
  }

  // 固定第一列的 value 宽度，确保第一列总宽度一致
  :deep(.ant-descriptions-item-content) {
    min-width: 200px !important;
    max-width: 200px !important;
    width: 200px !important;
  }

  // 固定每个 item 的总宽度，确保列对齐
  :deep(.ant-descriptions-item) {
    min-width: 200px !important;
  }
</style>
