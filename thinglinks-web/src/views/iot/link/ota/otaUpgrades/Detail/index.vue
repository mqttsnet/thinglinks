<template>
  <PageWrapper class="ota-detail">
    <!-- ===== 顶部 Header(Flexy 风格,对齐设备详情)===== -->
    <a-card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="res-icon">
            <component :is="getOtaPackageTypeSvg(detail.packageType)" />
          </div>
          <div class="res-meta">
            <div class="res-title">
              <span class="name-text">{{ detail.packageName || '-' }}</span>
              <a-tag color="blue">
                {{ getDictLabel('LINK_OTA_PACKAGES_TYPE', detail.packageType, '-') }}
              </a-tag>
              <a-tag :color="detail.status === 0 ? 'success' : 'default'">
                {{ getDictLabel('LINK_OTA_PACKAGES_STATUS', detail.status, '-') }}
              </a-tag>
            </div>
            <div class="meta-line">
              <span>
                <DatabaseOutlined />
                {{ t('iot.link.ota.otaUpgrades.productIdentification') }}:
                <span class="meta-val">{{ detail.productIdentification || '-' }}</span>
              </span>
              <a-divider type="vertical" />
              <span>
                <TagOutlined />
                {{ t('iot.link.ota.otaUpgrades.version') }}:
                <span class="meta-val mono">{{ detail.version || '-' }}</span>
              </span>
              <a-divider type="vertical" />
              <span class="meta-target">
                <RocketOutlined />
                {{ t('iot.link.ota.otaUpgrades.productVersionNo') }}:
                <span class="meta-val mono">{{ detail.productVersionNo || '-' }}</span>
              </span>
            </div>
          </div>
        </div>
        <a-space>
          <a-button @click="fetchDetail">
            <template #icon><ReloadOutlined /></template>
            {{ t('common.title.refresh') }}
          </a-button>
          <a-button
            v-hasAnyPermission="['basic:link:otaUpgrades:edit']"
            type="primary"
            @click="handleEdit"
          >
            <template #icon><EditOutlined /></template>
            {{ t('common.title.edit') }}
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- ===== 详情主体(分组信息 + 侧栏缩略图,对齐设备基本信息 Tab)===== -->
    <a-card :bordered="false" class="panel-card">
      <div class="basic-info">
        <div class="info-main">
          <div v-for="group in groups" :key="group.key" class="info-group">
            <div class="group-title">
              <span class="title-bar" :style="{ background: group.color }"></span>
              {{ group.title }}
            </div>
            <div class="field-grid">
              <div
                v-for="field in group.fields"
                :key="field.label"
                class="field-item"
                :class="{ 'is-full': field.full }"
              >
                <div class="field-label">{{ field.label }}</div>
                <div class="field-value">
                  <a-tag v-if="field.kind === 'tag'" :color="field.tagColor">
                    {{ field.value || '-' }}
                  </a-tag>
                  <span
                    v-else
                    class="value-text"
                    :class="{ mono: field.mono, 'is-target': field.target }"
                  >
                    {{ field.value || '-' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 侧栏:升级包类型 SVG 缩略图,作为视觉锚 -->
        <div class="info-aside">
          <div class="aside-card">
            <div class="aside-thumb">
              <component :is="getOtaPackageTypeSvg(detail.packageType)" />
            </div>
            <div class="aside-label">
              {{ getDictLabel('LINK_OTA_PACKAGES_TYPE', detail.packageType, '-') }}
            </div>
            <div class="aside-tip">{{ t('iot.link.ota.otaUpgrades.detailAsideTip') }}</div>
          </div>
        </div>
      </div>
    </a-card>

    <EditModal @register="registerModal" @success="fetchDetail" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import {
    ReloadOutlined,
    EditOutlined,
    DatabaseOutlined,
    TagOutlined,
    RocketOutlined,
  } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import { useModal } from '/@/components/Modal';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { getOtaPackageTypeSvg } from '/@/components/iot/ota/svg';
  import { PageWrapper } from '/@/components/Page';
  import { echoMapText } from '/@/utils/echo';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { detail as getDetailApi } from '/@/api/iot/link/ota/otaUpgrades';
  import EditModal from '../Edit.vue';

  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const { setTitle } = useTabs();
  const route = useRoute();

  const detail = ref<Record<string, any>>({});
  const [registerModal, { openModal }] = useModal();

  const L = (k: string) => t(`iot.link.ota.otaUpgrades.${k}`);

  // 分组字段(对齐设备基本信息 Tab:每组一根色条 + 标题 + 自适应网格)
  const groups = computed(() => {
    const d = detail.value || {};
    return [
      {
        key: 'basic',
        title: L('group.basic'),
        color: '#5d87ff',
        fields: [
          {
            label: L('appId'),
            value: getDictLabel(DictEnum.LINK_APPLICATION_SCENARIO, d.appId, '') || d.appId,
          },
          { label: L('packageName'), value: d.packageName },
          {
            label: L('packageType'),
            kind: 'tag',
            tagColor: 'blue',
            value: getDictLabel(DictEnum.LINK_OTA_PACKAGES_TYPE, d.packageType, '-'),
          },
          {
            label: L('signMethod'),
            value: getDictLabel(DictEnum.LINK_OTA_PACKAGES_SIGN_METHOD, d.signMethod, '-'),
          },
          {
            label: L('status'),
            kind: 'tag',
            tagColor: d.status === 0 ? 'success' : 'default',
            value: getDictLabel(DictEnum.LINK_OTA_PACKAGES_STATUS, d.status, '-'),
          },
        ],
      },
      {
        key: 'product',
        title: L('group.product'),
        color: '#fa8c16',
        fields: [
          { label: L('productIdentification'), value: d.productIdentification, mono: true },
          { label: L('version'), value: d.version, mono: true },
          { label: L('productVersionNo'), value: d.productVersionNo, mono: true, target: true },
        ],
      },
      {
        key: 'extra',
        title: L('group.extra'),
        color: '#13c2c2',
        fields: [
          { label: L('description'), value: d.description, full: true },
          { label: L('remark'), value: d.remark, full: true },
        ],
      },
      {
        key: 'timeline',
        title: L('group.timeline'),
        color: '#8c97a5',
        fields: [
          { label: L('createdOrgId'), value: echoMapText(d, 'createdOrgId') },
          { label: L('createdBy'), value: echoMapText(d, 'createdBy') },
          { label: L('createdTime'), value: d.createdTime },
          { label: L('updatedBy'), value: echoMapText(d, 'updatedBy') },
          { label: L('updatedTime'), value: d.updatedTime },
        ],
      },
    ];
  });

  async function fetchDetail() {
    const id = route.params.id as string;
    if (!id) return;
    try {
      const res = await getDetailApi(id);
      if (res) {
        detail.value = res;
        setTitle(`${t('iot.link.ota.otaUpgrades.detailTitle')} - ${res.packageName || ''}`);
      }
    } catch (error) {
      console.error('获取 OTA 资源详情失败:', error);
    }
  }

  function handleEdit() {
    openModal(true, { record: { ...detail.value }, type: ActionEnum.EDIT });
  }

  fetchDetail();
</script>

<style lang="less" scoped>
  /* Flexy 风格,对齐设备详情(/views/iot/link/device/device/detail.vue) */
  .ota-detail {
    :deep(.ant-card) {
      border-radius: 14px;
      box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
    }
  }

  /* ===== Header ===== */
  .header-card {
    margin: 16px 16px 0;

    :deep(.ant-card-body) {
      padding: 20px 24px;
    }
  }

  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;
    min-width: 0;
  }

  .res-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    background: linear-gradient(135deg, #eef2ff 0%, #e8f4fd 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
    box-shadow: 0 4px 12px rgba(93, 135, 255, 0.1);
    flex-shrink: 0;
    overflow: hidden;

    :deep(svg) {
      width: 100%;
      height: 100%;
    }
  }

  .res-meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 0;
  }

  .res-title {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;

    .name-text {
      font-size: 18px;
      font-weight: 600;
      color: #2a3547;
    }
  }

  .meta-line {
    color: #6b7280;
    font-size: 13px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }

    .meta-val {
      color: #2a3547;
      font-weight: 500;

      &.mono {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      }
    }

    .meta-target {
      .anticon {
        color: #5d87ff;
      }
      .meta-val {
        color: #5d87ff;
      }
    }

    :deep(.anticon) {
      color: #97a1b0;
    }
  }

  /* ===== 详情主体 ===== */
  .panel-card {
    margin: 16px;

    :deep(.ant-card-body) {
      padding: 20px 22px;
    }
  }

  .basic-info {
    display: flex;
    gap: 28px;
  }

  .info-main {
    flex: 1;
    min-width: 0;
  }

  .info-aside {
    flex-shrink: 0;
    width: 220px;
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

  /* 响应式网格:自适应列数(大屏多列 / 小屏收窄) */
  .field-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
    gap: 20px 28px;
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

      /* 目标产品版本:版本切换的关键字段,蓝色高亮 */
      &.is-target {
        color: #5d87ff;
        font-weight: 600;
      }
    }
  }

  /* 侧栏视觉锚:升级包类型 SVG 缩略图 */
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
    width: 120px;
    height: 120px;
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

  .aside-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #97a1b0;
    line-height: 1.5;
  }

  @media (max-width: 768px) {
    .basic-info {
      flex-direction: column;
    }

    .info-aside {
      width: 100%;
    }
  }
</style>
