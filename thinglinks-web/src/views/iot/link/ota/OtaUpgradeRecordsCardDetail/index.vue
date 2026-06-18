<template>
  <PageWrapper class="rec-detail">
    <!-- ===== 顶部 Header(Flexy 风格)===== -->
    <a-card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="res-icon">
            <OtaRecordStatusBadge :status="detail.upgradeStatus" :size="50" />
          </div>
          <div class="res-meta">
            <div class="res-title">
              <span class="name-text">{{ detail.deviceIdentification || '-' }}</span>
              <a-tag :color="statusColor">
                {{ getDictLabel('LINK_OTA_TASK_RECORD_STATUS', detail.upgradeStatus, '-') }}
              </a-tag>
            </div>
            <div class="meta-line">
              <span class="ver-flow">
                <span class="meta-val mono">{{ detail.sourceVersion || '—' }}</span>
                <ArrowRightOutlined class="ver-arrow" />
                <span class="meta-val mono is-target">{{ detail.targetVersion || '—' }}</span>
              </span>
              <a-divider type="vertical" />
              <span>
                {{ t('iot.link.ota.otaUpgradeRecords.taskId') }}:
                <span class="meta-val mono">{{ detail.taskId || '-' }}</span>
              </span>
            </div>
          </div>
        </div>
      </div>
    </a-card>

    <!-- ===== 详情主体(分组信息 + 侧栏进度)===== -->
    <a-card :bordered="false" class="panel-card">
      <div class="info-flex">
        <div class="info-main">
          <section v-for="group in groups" :key="group.key" class="info-group">
            <div class="group-title">
              <span class="title-bar" :style="{ background: group.color }"></span>
              {{ group.title }}
            </div>
            <div class="field-grid">
              <div
                v-for="f in group.fields"
                :key="f.field"
                class="field-item"
                :class="{ 'is-full': f.full }"
              >
                <div class="field-label">{{ f.label }}</div>
                <div class="field-value">
                  <span v-if="f.enableView" class="view-cell" @click="viewCommand(f)">
                    <EyeOutlined />
                    {{ t('common.title.view') }}
                  </span>
                  <span v-else class="value-text" :class="{ mono: f.mono }">{{
                    f.value || '-'
                  }}</span>
                </div>
              </div>
            </div>
          </section>
        </div>

        <!-- 侧栏:状态水晶球 + 进度 -->
        <div class="info-aside">
          <div class="aside-card">
            <div class="aside-thumb">
              <OtaRecordStatusBadge :status="detail.upgradeStatus" :size="78" />
            </div>
            <div class="aside-label">
              {{ getDictLabel('LINK_OTA_TASK_RECORD_STATUS', detail.upgradeStatus, '-') }}
            </div>
            <a-progress
              type="circle"
              :width="92"
              :percent="Number(detail.progress) || 0"
              :stroke-color="progressColor"
            />
          </div>
        </div>
      </div>
    </a-card>

    <!-- ===== 升级日志 / 成功 / 失败 ===== -->
    <a-card :bordered="false" class="panel-card log-card">
      <a-tabs v-model:activeKey="currentKey">
        <a-tab-pane key="1" :tab="t('iot.link.ota.otaUpgradeRecords.upgradeLog')" />
        <a-tab-pane key="2" :tab="t('iot.link.ota.otaUpgradeRecords.upgradeSuccess')" />
        <a-tab-pane key="3" :tab="t('iot.link.ota.otaUpgradeRecords.upgradeFailure')" />
      </a-tabs>
      <template v-if="![null, undefined, ''].includes(codemirrorVal)">
        <div class="code-info">
          <a-button size="small" @click="copyCode">{{ t('common.title.copy') }}</a-button>
          <codemirror
            v-model="codemirrorVal"
            :read-only="true"
            :extensions="contentExtensions"
            :indent-with-tab="true"
            :line-wrapping="true"
            :style="{ height: '420px' }"
            :tab-size="2"
          />
        </div>
      </template>
      <a-empty v-else :description="t('iot.link.ota.otaUpgradeRecords.empty')" />
    </a-card>

    <CopyModal @register="registerCopyModal" />
  </PageWrapper>
</template>
<script lang="ts" setup>
  import { computed, ref, reactive } from 'vue';
  import { useRoute } from 'vue-router';
  import { message } from 'ant-design-vue';
  import { ArrowRightOutlined, EyeOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import { useModal } from '/@/components/Modal';
  import { getSingleQuery } from '/@/api/iot/link/ota/otaUpgradeRecords';
  import { PageWrapper } from '/@/components/Page';
  import CopyModal from '/@/components/CopyModal/index.vue';
  import { OtaRecordStatusBadge } from '/@/components/iot/ota/svg';
  import { Codemirror } from 'vue-codemirror';
  import { json } from '@codemirror/lang-json';
  import { oneDark } from '@codemirror/theme-one-dark';
  import { columns } from './OtaUpgradeRecordsCardDetail.data';

  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const route = useRoute();

  const detail = ref<Record<string, any>>({});
  const currentKey = ref('1');
  const routeParams = reactive({ ...route.params });

  const [registerCopyModal, { openModal: openCopyModal }] = useModal();

  // 等宽 / 整行字段
  const MONO_FIELDS = [
    'taskId',
    'deviceIdentification',
    'sourceVersion',
    'targetVersion',
    'errorCode',
  ];
  const FULL_FIELDS = ['errorMessage', 'remark', 'commandContent'];

  // 复用 Description 的 columns(label + render)派生字段,再按语义分三组
  const fieldMap = computed<Record<string, any>>(() => {
    const map: Record<string, any> = {};
    columns().forEach((item: any) => {
      map[item.field] = {
        field: item.field,
        label: item.label,
        value: item.render
          ? item.render(detail.value?.[item.field], detail.value)
          : detail.value?.[item.field],
        raw: detail.value?.[item.field],
        enableView: item.enableView,
        mono: MONO_FIELDS.includes(item.field),
        full: FULL_FIELDS.includes(item.field),
      };
    });
    return map;
  });

  const pick = (keys: string[]) => keys.map((k) => fieldMap.value[k]).filter(Boolean);

  const groups = computed(() => [
    {
      key: 'basic',
      title: t('iot.link.ota.otaUpgradeRecords.group.basic'),
      color: '#5d87ff',
      fields: pick([
        'taskId',
        'deviceIdentification',
        'upgradeStatus',
        'sourceVersion',
        'targetVersion',
      ]),
    },
    {
      key: 'dispatch',
      title: t('iot.link.ota.otaUpgradeRecords.group.dispatch'),
      color: '#fa8c16',
      fields: pick([
        'appConfirmationStatus',
        'appConfirmationTime',
        'commandSendStatus',
        'lastCommandSendTime',
        'commandContent',
      ]),
    },
    {
      key: 'result',
      title: t('iot.link.ota.otaUpgradeRecords.group.result'),
      color: '#13c2c2',
      fields: pick([
        'errorCode',
        'errorMessage',
        'startTime',
        'endTime',
        'remark',
        'createdOrgId',
        'createdTime',
      ]),
    },
  ]);

  // 升级状态色:待升级 灰 / 升级中 蓝 / 成功 绿 / 失败 红
  const statusColor = computed(() => {
    const s = Number(detail.value?.upgradeStatus);
    if (s === 2) return 'success';
    if (s === 3) return 'error';
    if (s === 1) return 'processing';
    return 'default';
  });

  const progressColor = computed(() => {
    const s = Number(detail.value?.upgradeStatus);
    if (s === 3) return '#fa5252';
    if (s === 2) return '#13deb9';
    return '#5d87ff';
  });

  const contentExtensions = [json(), oneDark];
  const codemirrorVal = computed(() => {
    if (currentKey.value === '1') return detail.value['logDetails'];
    if (currentKey.value === '2') return detail.value['successDetails'];
    if (currentKey.value === '3') return detail.value['failureDetails'];
    return '';
  });

  const viewCommand = (f: any) => {
    const content = f?.raw;
    const contentStr =
      typeof content === 'string' || typeof content === 'number'
        ? String(content)
        : JSON.stringify(content ?? '', null, 2);
    openCopyModal(true, { title: String(f.label), value: contentStr });
  };

  const copyCode = () => {
    const input = document.createElement('input');
    input.setAttribute('readonly', 'readonly');
    input.setAttribute('value', codemirrorVal.value);
    document.body.appendChild(input);
    input.select();
    input.setSelectionRange(0, 9999);
    if (document.execCommand('copy')) {
      document.execCommand('copy');
      message.success(t('common.tips.copySuccess'));
    }
    document.body.removeChild(input);
  };

  const getDetailInfo = async () => {
    try {
      const detailValue = await getSingleQuery(routeParams.id);
      if (detailValue) {
        detail.value = detailValue;
      }
    } catch (error) {
      console.error('获取升级记录详情失败:', error);
    }
  };
  getDetailInfo();
</script>
<style lang="less" scoped>
  .rec-detail {
    :deep(.ant-card) {
      border-radius: 14px;
      box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
    }
  }

  /* ===== Header ===== */
  .header-card {
    margin: 16px 16px 0;

    :deep(.ant-card-body) {
      padding: 18px 22px;
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
    display: flex;
    align-items: center;
    flex-shrink: 0;
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
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
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

      &.is-target {
        color: #5d87ff;
      }
    }

    .ver-flow .ver-arrow {
      color: #5d87ff;
      font-size: 12px;
    }
  }

  /* ===== 详情主体 ===== */
  .panel-card {
    margin: 16px;

    :deep(.ant-card-body) {
      padding: 20px 22px;
    }
  }

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

    .view-cell {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      color: #5d87ff;
      cursor: pointer;

      &:hover {
        color: #2952cc;
      }
    }
  }

  /* 侧栏 */
  .aside-card {
    background: linear-gradient(180deg, #fafbfd 0%, #ffffff 100%);
    border: 1px solid #eef1f7;
    border-radius: 14px;
    padding: 18px 14px 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 6px;
  }

  .aside-thumb {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .aside-label {
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
    margin-bottom: 8px;
  }

  /* ===== 日志卡 ===== */
  .log-card {
    .code-info {
      text-align: start;

      button {
        margin-bottom: 10px;
      }
    }
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
