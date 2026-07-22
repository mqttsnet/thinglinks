<template>
  <!-- 升级任务「基础信息」Tab 内容(头部公共信息已上移到容器,此处只渲染分组明细) -->
  <div class="info-flex">
    <div class="info-main">
      <!-- 任务信息 -->
      <section class="info-group">
        <div class="group-title">
          <span class="title-bar" style="background: #5d87ff"></span>
          {{ t('iot.link.ota.otaUpgradeTasks.upgradeTaskDetail') }}
        </div>
        <div class="field-grid">
          <div
            v-for="f in taskFields"
            :key="f.field"
            class="field-item"
            :class="{ 'is-full': f.full }"
          >
            <div class="field-label">{{ f.label }}</div>
            <div class="field-value">
              <span class="value-text" :class="{ mono: f.mono }">{{ f.value || '-' }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 升级包 -->
      <section class="info-group">
        <div class="group-title">
          <span class="title-bar" style="background: #fa8c16"></span>
          {{ t('iot.link.ota.otaUpgradeTasks.upgradePackageDetail') }}
        </div>
        <div class="field-grid">
          <div
            v-for="f in packageFields"
            :key="f.field"
            class="field-item"
            :class="{ 'is-full': f.full || f.field === 'fileLocation' }"
          >
            <div class="field-label">{{ f.label }}</div>
            <div class="field-value">
              <template v-if="f.field === 'fileLocation'">
                <BasicUpload
                  v-if="fileList && fileList.length > 0"
                  :value="fileList"
                  :onlyShowPreview="true"
                  :showDeleteBtn="false"
                />
                <span v-else class="value-empty">-</span>
              </template>
              <span v-else class="value-text" :class="{ mono: f.mono }">
                {{ f.value || '-' }}
              </span>
            </div>
          </div>
        </div>
      </section>

      <!-- 升级范围 -->
      <section class="info-group">
        <div class="group-title">
          <span class="title-bar" style="background: #9254de"></span>
          {{ t('iot.link.ota.otaUpgradeTasks.upgradeScope') }}
        </div>
        <div class="scope-content">
          <div v-if="upgradeScope === 0" class="scope-item">
            <span class="scope-label">
              {{ t('iot.link.ota.otaUpgradeTasks.productIdentification') }}
            </span>
            <span class="value-text mono">
              {{ upgradeTask?.otaUpgradesResult?.productIdentification || '-' }}
            </span>
          </div>
          <div v-else-if="upgradeScope === 1" class="scope-item">
            <span class="scope-label">
              {{ t('iot.link.ota.otaUpgradeTasks.directUpgrade') }}（{{
                t('iot.link.device.device.deviceIdentification')
              }}）
            </span>
            <div class="scope-tags">
              <a-tag v-for="(item, index) in targetValueList" :key="index" class="scope-tag">
                {{ item }}
              </a-tag>
              <span v-if="!targetValueList || targetValueList.length === 0" class="value-empty">
                -
              </span>
            </div>
          </div>
          <div v-else-if="upgradeScope === 2" class="scope-item">
            <span class="scope-label">
              {{ t('iot.link.ota.otaUpgradeTasks.groupUpgrade') }}（{{
                t('iot.link.ota.otaUpgradeTasks.groupId')
              }}）
            </span>
            <div class="scope-tags">
              <a-tag v-for="(item, index) in targetValueList" :key="index" class="scope-tag">
                {{ item }}
              </a-tag>
              <span v-if="!targetValueList || targetValueList.length === 0" class="value-empty">
                -
              </span>
            </div>
          </div>
          <div v-else-if="upgradeScope === 3" class="scope-item">
            <span class="scope-label">
              {{ t('iot.link.ota.otaUpgradeTasks.areaUpgrade') }}（{{
                t('iot.link.ota.otaUpgradeTasks.areaCode')
              }}）
            </span>
            <div class="scope-tags">
              <div v-if="areaList && areaList.length > 0" class="area-list">
                <div v-for="(item, index) in areaList" :key="index" class="area-item">
                  <span>{{ item.provinceName || item.provinceCode || '-' }}</span>
                  <span class="area-sep">｜</span>
                  <span>{{ item.cityName || item.cityCode || '-' }}</span>
                </div>
              </div>
              <span v-else class="value-empty">-</span>
            </div>
          </div>
          <span v-else class="value-empty">-</span>
        </div>
      </section>
    </div>

    <!-- 侧栏:升级任务 SVG 视觉锚 -->
    <div class="info-aside">
      <div class="aside-card">
        <div class="aside-thumb"><OtaTaskSvg /></div>
        <div class="aside-label">
          {{ getDictLabel('LINK_OTA_UPGRADE_METHOD', upgradeTask.upgradeMethod, '-') }}
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { ref, computed, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import { findTenantFileInfoByIds } from '/@/api/thinglinks/file/upload';
  import { BasicUpload } from '/@/components/Upload';
  import { OtaTaskSvg } from '/@/components/iot/ota/svg';
  import { getUpgradeTaskSchema, getUpgradePackageSchema } from './otaUpgradeTaskDetail.data';

  const props = defineProps<{
    taskDetail?: any;
  }>();

  const { t } = useI18n();
  const { getDictLabel } = useDict();

  const upgradeTask = ref<any>({});
  const upgradePackage = ref<any>({});
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
      return { provinceCode, cityCode, provinceName: '', cityName: '' };
    });
  });

  // 整行展示的长文本字段
  const FULL_FIELDS = ['description', 'remark', 'customInfo', 'sourceVersions'];
  // 等宽展示的标识/版本字段
  const MONO_FIELDS = ['productIdentification', 'version', 'sourceVersions', 'upgradeId'];

  // 复用 Description 的 schema(label + render)派生分组字段,避免重复定义
  function resolveFields(schema: any[], data: any) {
    return schema.map((item) => ({
      field: item.field,
      label: item.label,
      value: item.render ? item.render(data?.[item.field], data) : data?.[item.field],
      full: FULL_FIELDS.includes(item.field),
      mono: MONO_FIELDS.includes(item.field),
    }));
  }

  const taskFields = computed(() => resolveFields(getUpgradeTaskSchema(), upgradeTask.value));
  const packageFields = computed(() =>
    resolveFields(getUpgradePackageSchema(), upgradePackage.value),
  );

  const initData = async (taskData: any) => {
    if (!taskData) return;
    upgradeTask.value = Object.assign({}, upgradeTask.value, taskData);
    upgradePackage.value = upgradeTask.value?.otaUpgradesResult || {};

    const fileLocation = upgradePackage.value?.fileLocation;
    if (fileLocation) {
      try {
        const fileIds = String(fileLocation)
          .split(',')
          .filter((id: string) => id);
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

  watch(
    () => props.taskDetail,
    (newVal) => {
      if (newVal) {
        initData(newVal);
      }
    },
    { immediate: true },
  );
</script>
<style scoped lang="less">
  .info-flex {
    display: flex;
    gap: 28px;
  }

  .info-main {
    flex: 1;
    min-width: 0;
  }

  .info-aside {
    flex-shrink: 0;
    width: 200px;
  }

  .info-group {
    & + & {
      margin-top: 26px;
      padding-top: 24px;
      border-top: 1px solid #f0f2f5;
    }
  }

  .group-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
    margin-bottom: 18px;

    .title-bar {
      width: 3px;
      height: 14px;
      border-radius: 2px;
    }
  }

  .field-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
    gap: 18px 28px;
  }

  .field-item {
    min-width: 0;

    &.is-full {
      grid-column: 1 / -1;
    }
  }

  .field-label {
    font-size: 12px;
    color: #8c97a5;
    margin-bottom: 6px;
  }

  .field-value {
    font-size: 14px;
    line-height: 1.5;

    .value-text {
      color: #2a3547;
      font-weight: 500;
      word-break: break-all;

      &.mono {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 13px;
        color: #4a5568;
      }
    }
  }

  .value-empty {
    color: #b6bdc8;
  }

  /* 升级范围 */
  .scope-item {
    display: flex;
    flex-wrap: wrap;
    align-items: baseline;
    gap: 8px 12px;
  }

  .scope-label {
    font-size: 13px;
    color: #8c97a5;
    flex-shrink: 0;
  }

  .scope-tags {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  .scope-tag {
    margin: 0;
  }

  .area-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .area-item {
    padding: 5px 12px;
    background: #f7f8fa;
    border-radius: 6px;
    display: inline-flex;
    align-items: center;
    font-size: 13px;
    color: #2a3547;

    .area-sep {
      color: #c8cfdb;
      margin: 0 6px;
    }
  }

  /* 侧栏 */
  .aside-card {
    background: linear-gradient(180deg, #fafbfd 0%, #ffffff 100%);
    border: 1px solid #eef1f7;
    border-radius: 14px;
    padding: 18px 14px 16px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .aside-thumb {
    width: 110px;
    height: 110px;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(svg) {
      width: 100%;
      height: 100%;
    }
  }

  .aside-label {
    margin-top: 4px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
  }

  @media (max-width: 768px) {
    .info-flex {
      flex-direction: column;
    }

    .info-aside {
      width: 100%;
    }
  }
</style>
