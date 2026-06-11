<template>
  <PageWrapper contentFullHeight>
    <Spin :spinning="loading">
      <!-- ==================== Hero 头部卡片 ==================== -->
      <div class="acl-detail">
        <Card :bordered="false" class="hero-card">
          <div class="hero-content">
            <div class="hero-left">
              <div class="hero-icon">
                <SafetyCertificateOutlined style="font-size: 36px; color: #fff" />
              </div>
              <div class="hero-meta">
                <div class="hero-name">
                  <span>{{ aclDetail.ruleName || '-' }}</span>
                  <Tag :color="ruleLevelColor" class="status-tag">
                    {{ getDictLabel('LINK_ACL_RULE_LEVEL', aclDetail.ruleLevel, '-') }}
                  </Tag>
                  <Tag :color="enabledColor" class="status-tag">
                    <template #icon>
                      <PoweroffOutlined />
                    </template>
                    {{
                      isAllow(aclDetail.enabled)
                        ? t('iot.link.operationMaintenance.accessControl.deviceAclRule.status.enabled')
                        : t('iot.link.operationMaintenance.accessControl.deviceAclRule.status.disabled')
                    }}
                  </Tag>
                  <Tag :color="decisionColor" class="status-tag">
                    <template #icon>
                      <component :is="isAllow(aclDetail.decision) ? CheckCircleOutlined : CloseCircleOutlined" />
                    </template>
                    {{
                      isAllow(aclDetail.decision)
                        ? t('iot.link.operationMaintenance.accessControl.deviceAclRule.allow')
                        : t('iot.link.operationMaintenance.accessControl.deviceAclRule.deny')
                    }}
                  </Tag>
                </div>
                <div class="hero-summary">
                  <span class="summary-item">
                    <DatabaseOutlined />
                    {{ aclDetail.productIdentification || '-' }}
                  </span>
                  <a-divider type="vertical" />
                  <span class="summary-item" v-if="aclDetail.deviceIdentification">
                    <ApiOutlined />
                    {{ aclDetail.deviceIdentification }}
                  </span>
                  <a-divider type="vertical" v-if="aclDetail.deviceIdentification" />
                  <span class="summary-item">
                    <ThunderboltOutlined />
                    {{ getDictLabel('LINK_ACL_RULE_ACTION_TYPE', aclDetail.actionType, '-') }}
                  </span>
                  <a-divider type="vertical" />
                  <span class="summary-item">
                    <NumberOutlined />
                    {{ t('iot.link.operationMaintenance.accessControl.deviceAclRule.priority') }}: {{ aclDetail.priority ?? '-' }}
                  </span>
                  <a-divider type="vertical" />
                  <span class="summary-item">
                    <ClockCircleOutlined />
                    {{ aclDetail.createdTime || '-' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- ==================== Hero 右侧操作区 ==================== -->
            <div class="hero-right">
              <a-button class="hero-btn" @click="handleBack">
                <template #icon><LeftOutlined /></template>
                {{ t('common.back') }}
              </a-button>
              <a-tooltip :title="t('iot.link.operationMaintenance.accessControl.deviceAclRule.testerEntry.tooltip')">
                <a-button class="hero-btn" :disabled="!aclDetail.topicPattern" @click="handleOpenTester">
                  <template #icon><ExperimentOutlined /></template>
                  {{ t('iot.link.operationMaintenance.accessControl.deviceAclRule.testerEntry.button') }}
                </a-button>
              </a-tooltip>
              <a-button
                class="hero-btn"
                type="primary"
                @click="handleEdit"
                v-hasAnyPermission="['link:accessControl:deviceAclRule:edit']"
              >
                <template #icon><EditOutlined /></template>
                {{ t('common.title.edit') }}
              </a-button>
            </div>
          </div>
        </Card>

        <!-- ACL 规则匹配测试器 -->
        <AclTopicMatcherTesterModal @register="registerTester" />

        <!-- ==================== Tab 区 ==================== -->
        <Card :bordered="false" class="tab-card">
          <a-tabs v-model:activeKey="activeTab">
            <!-- Tab 1:基础信息 -->
            <a-tab-pane key="info" :tab="t('iot.link.operationMaintenance.accessControl.deviceAclRule.detail.tabBasic')">
              <a-row :gutter="16" class="section-row">
                <!-- 左:基础属性 -->
                <a-col :span="12">
                  <Card :bordered="false" class="section-card">
                    <template #title>
                      <div class="section-title">
                        <ProfileOutlined />
                        <span>{{ t('iot.link.operationMaintenance.accessControl.deviceAclRule.section.base') }}</span>
                      </div>
                    </template>
                    <a-descriptions :column="1" bordered size="small">
                      <a-descriptions-item :label="t('iot.link.operationMaintenance.accessControl.deviceAclRule.ruleName')">
                        {{ aclDetail.ruleName || '-' }}
                      </a-descriptions-item>
                      <a-descriptions-item :label="t('iot.link.operationMaintenance.accessControl.deviceAclRule.priority')">
                        <PriorityBadge :value="aclDetail.priority" />
                      </a-descriptions-item>
                      <a-descriptions-item :label="t('iot.link.operationMaintenance.accessControl.deviceAclRule.ruleLevel')">
                        <Tag :color="ruleLevelColor">
                          {{ getDictLabel('LINK_ACL_RULE_LEVEL', aclDetail.ruleLevel, '-') }}
                        </Tag>
                      </a-descriptions-item>
                      <a-descriptions-item :label="t('iot.link.operationMaintenance.accessControl.deviceAclRule.enabled')">
                        <Tag :color="enabledColor">
                          {{
                            isAllow(aclDetail.enabled)
                              ? t('iot.link.operationMaintenance.accessControl.deviceAclRule.status.enabled')
                              : t('iot.link.operationMaintenance.accessControl.deviceAclRule.status.disabled')
                          }}
                        </Tag>
                      </a-descriptions-item>
                    </a-descriptions>
                  </Card>
                </a-col>

                <!-- 右:范围匹配 -->
                <a-col :span="12">
                  <Card :bordered="false" class="section-card">
                    <template #title>
                      <div class="section-title">
                        <FilterOutlined />
                        <span>{{ t('iot.link.operationMaintenance.accessControl.deviceAclRule.section.scope') }}</span>
                      </div>
                    </template>
                    <a-descriptions :column="1" bordered size="small">
                      <a-descriptions-item :label="t('iot.link.operationMaintenance.accessControl.deviceAclRule.productIdentification')">
                        <code class="mono-text">{{ aclDetail.productIdentification || '-' }}</code>
                      </a-descriptions-item>
                      <a-descriptions-item :label="t('iot.link.operationMaintenance.accessControl.deviceAclRule.deviceIdentification')">
                        <code class="mono-text" v-if="aclDetail.deviceIdentification">{{ aclDetail.deviceIdentification }}</code>
                        <span class="text-muted" v-else>{{
                          t('iot.link.operationMaintenance.accessControl.deviceAclRule.detail.deviceLevelHint')
                        }}</span>
                      </a-descriptions-item>
                      <a-descriptions-item :label="t('iot.link.operationMaintenance.accessControl.deviceAclRule.actionType')">
                        <Tag :color="actionTypeColor">
                          {{ getDictLabel('LINK_ACL_RULE_ACTION_TYPE', aclDetail.actionType, '-') }}
                        </Tag>
                      </a-descriptions-item>
                      <a-descriptions-item :label="t('iot.link.operationMaintenance.accessControl.deviceAclRule.topicPattern')">
                        <code class="mono-text">{{ aclDetail.topicPattern || '-' }}</code>
                      </a-descriptions-item>
                    </a-descriptions>
                  </Card>
                </a-col>
              </a-row>

              <!-- 权限决策 + IP 白名单 -->
              <Card :bordered="false" class="section-card section-row-single">
                <template #title>
                  <div class="section-title">
                    <SafetyOutlined />
                    <span>{{ t('iot.link.operationMaintenance.accessControl.deviceAclRule.section.decision') }}</span>
                  </div>
                </template>
                <a-row :gutter="16">
                  <a-col :span="8">
                    <div class="decision-display">
                      <div class="decision-label">
                        {{ t('iot.link.operationMaintenance.accessControl.deviceAclRule.decision') }}
                      </div>
                      <div class="decision-value" :class="isAllow(aclDetail.decision) ? 'allow' : 'deny'">
                        <component :is="isAllow(aclDetail.decision) ? CheckCircleOutlined : CloseCircleOutlined" />
                        {{
                          isAllow(aclDetail.decision)
                            ? t('iot.link.operationMaintenance.accessControl.deviceAclRule.allow')
                            : t('iot.link.operationMaintenance.accessControl.deviceAclRule.deny')
                        }}
                      </div>
                    </div>
                  </a-col>
                  <a-col :span="16">
                    <div class="ip-list-display">
                      <div class="ip-label">
                        <GlobalOutlined />
                        {{ t('iot.link.operationMaintenance.accessControl.deviceAclRule.ipWhitelist') }}
                      </div>
                      <div class="ip-tags" v-if="ipList.length">
                        <Tag v-for="(ip, idx) in ipList" :key="idx" color="blue">
                          {{ ip }}
                        </Tag>
                      </div>
                      <div class="text-muted" v-else>-</div>
                    </div>
                  </a-col>
                </a-row>
              </Card>

              <!-- 备注 -->
              <Card :bordered="false" class="section-card section-row-single" v-if="aclDetail.remark">
                <template #title>
                  <div class="section-title">
                    <FileTextOutlined />
                    <span>{{ t('iot.link.operationMaintenance.accessControl.deviceAclRule.remark') }}</span>
                  </div>
                </template>
                <p class="remark-text">{{ aclDetail.remark }}</p>
              </Card>
            </a-tab-pane>

            <!-- Tab 2:审计信息 -->
            <a-tab-pane key="audit" :tab="t('iot.link.operationMaintenance.accessControl.deviceAclRule.detail.tabAudit')">
              <Card :bordered="false" class="section-card">
                <a-descriptions :column="2" bordered size="small">
                  <a-descriptions-item :label="t('thinglinks.common.createBy')">
                    {{ echoMapText(aclDetail, 'createdBy') }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('thinglinks.common.createdTime')">
                    {{ aclDetail.createdTime || '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('thinglinks.common.updateBy')">
                    {{ echoMapText(aclDetail, 'updatedBy') }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('thinglinks.common.updatedTime')">
                    {{ aclDetail.updatedTime || '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('iot.link.operationMaintenance.accessControl.deviceAclRule.createdOrgId')">
                    {{ echoMapText(aclDetail, 'createdOrgId') }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('iot.link.operationMaintenance.accessControl.deviceAclRule.id')">
                    <code class="mono-text">{{ aclDetail.id || '-' }}</code>
                  </a-descriptions-item>
                </a-descriptions>
              </Card>
            </a-tab-pane>
          </a-tabs>
        </Card>
      </div>
    </Spin>

    <EditModal @register="registerEditModal" @success="loadDetail" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, onMounted, h } from 'vue';
  import { useRouter } from 'vue-router';
  import { Card, Tag, Spin } from 'ant-design-vue';
  import {
    SafetyCertificateOutlined,
    SafetyOutlined,
    ProfileOutlined,
    FilterOutlined,
    DatabaseOutlined,
    ApiOutlined,
    ThunderboltOutlined,
    NumberOutlined,
    ClockCircleOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    FileTextOutlined,
    GlobalOutlined,
    LeftOutlined,
    EditOutlined,
    ExperimentOutlined,
    PoweroffOutlined,
  } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import { useModal } from '/@/components/Modal';
  import { PageWrapper } from '/@/components/Page';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { detail } from '/@/api/iot/link/operationMaintenance/accessControl/deviceAclRule';
  import { echoMapText } from '/@/utils/echo';
  import EditModal from './Edit.vue';
  import {
    AclTopicMatcherTesterModal,
    type TestableAclRule,
  } from '/@/components/iot/AclTopicMatcherTester';

  defineOptions({ name: 'ACL规则详情' });

  const { t } = useI18n();
  const { currentRoute, push } = useRouter();
  const { getDictLabel } = useDict();
  const [registerEditModal, { openModal: openEditModal }] = useModal();
  const [registerTester, { openModal: openTesterModal }] = useModal();

  const id = ref<string>('');
  const loading = ref(false);
  const activeTab = ref('info');
  const aclDetail = reactive<Record<string, any>>({});

  // ============================== Computed ==============================
  const ruleLevelColor = computed(() => (Number(aclDetail.ruleLevel) === 1 ? 'cyan' : 'blue'));

  const enabledColor = computed(() => (isAllow(aclDetail.enabled) ? 'processing' : 'default'));

  const decisionColor = computed(() => (isAllow(aclDetail.decision) ? 'success' : 'error'));

  const actionTypeColor = computed(() => {
    const map: Record<string, string> = { '0': 'purple', '1': 'blue', '2': 'cyan', '3': 'orange' };
    return map[String(aclDetail.actionType)] || 'default';
  });

  const ipList = computed<string[]>(() =>
    (aclDetail.ipWhitelist || '')
      .split(/[,，\s]+/)
      .map((s: string) => s.trim())
      .filter(Boolean),
  );

  // ============================== Helpers ==============================
  function isAllow(v: any): boolean {
    return v === '1' || v === 1 || v === true;
  }

  /** 优先级徽章组件 ── 数字越小优先级越高,根据档位染色 */
  const PriorityBadge = (props: { value?: number | null }) => {
    const v = props.value;
    if (v === null || v === undefined) return h('span', {}, '-');
    let color = '#1890ff';
    let level = 'normal';
    if (v >= 0 && v <= 100) {
      color = '#f5222d';
      level = t('iot.link.operationMaintenance.accessControl.deviceAclRule.detail.priorityLevel.high');
    } else if (v <= 500) {
      color = '#1890ff';
      level = t('iot.link.operationMaintenance.accessControl.deviceAclRule.detail.priorityLevel.normal');
    } else {
      color = '#52c41a';
      level = t('iot.link.operationMaintenance.accessControl.deviceAclRule.detail.priorityLevel.low');
    }
    return h('span', { class: 'priority-badge' }, [
      h('span', { class: 'priority-num', style: { color } }, String(v)),
      h(Tag, { color: color, style: { marginLeft: '8px' } }, () => level),
    ]);
  };

  // ============================== Actions ==============================
  function handleBack() {
    history.length > 1 ? history.back() : push({ name: 'ACL规则' });
  }

  function handleEdit() {
    openEditModal(true, { record: { ...aclDetail }, type: ActionEnum.EDIT });
  }

  /**
   * 打开"规则测试器" ── 把当前规则的完整数据传入,Modal 内可选真实产品/设备做模拟匹配。
   * decision / enabled 在后端是字典字符串 ('1' / '0') 或数字,这里用 {@link isAllow} 统一转 boolean。
   * actionType / priority 等字段透传,占位符值由 Modal 内交互产出。
   */
  function handleOpenTester() {
    if (!aclDetail.topicPattern) return;
    const rule: TestableAclRule = {
      ruleName: aclDetail.ruleName,
      topicPattern: aclDetail.topicPattern,
      decision: aclDetail.decision == null ? undefined : isAllow(aclDetail.decision),
      actionType: aclDetail.actionType,
      priority: aclDetail.priority,
      enabled: aclDetail.enabled == null ? true : isAllow(aclDetail.enabled),
    };
    openTesterModal(true, {
      rule,
      // 预填:规则已选的产品/设备直接灌进去,用户即开即测
      presetProductIdentification: aclDetail.productIdentification,
      presetDeviceIdentification: aclDetail.deviceIdentification,
    });
  }

  async function loadDetail() {
    if (!id.value) return;
    loading.value = true;
    try {
      const res = await detail(id.value);
      Object.keys(aclDetail).forEach((k) => delete aclDetail[k]);
      Object.assign(aclDetail, res);
    } finally {
      loading.value = false;
    }
  }

  onMounted(() => {
    const { params } = currentRoute.value;
    id.value = params.id as string;
    loadDetail();
  });
</script>

<style lang="less" scoped>
  .acl-detail {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  // ============================== Hero 卡片 ==============================
  .hero-card {
    border-radius: 8px;
    overflow: hidden;
    background: linear-gradient(135deg, #1890ff 0%, #722ed1 100%);

    :deep(.ant-card-body) {
      padding: 24px 32px;
    }

    .hero-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .hero-left {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .hero-right {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-shrink: 0;

      .hero-btn {
        // 默认按钮(返回):半透明白底,适配紫色 hero
        background: rgba(255, 255, 255, 0.18);
        border-color: rgba(255, 255, 255, 0.4);
        color: #fff;
        backdrop-filter: blur(8px);
        transition: all 0.2s;

        &:hover,
        &:focus {
          background: rgba(255, 255, 255, 0.3);
          border-color: rgba(255, 255, 255, 0.6);
          color: #fff;
        }

        // 主按钮(编辑):实心白底,与 hero 对比突出
        &.ant-btn-primary {
          background: #fff;
          border-color: #fff;
          color: #1677ff;

          &:hover,
          &:focus {
            background: #f0f5ff;
            border-color: #f0f5ff;
            color: #1677ff;
          }
        }
      }
    }

    .hero-icon {
      width: 72px;
      height: 72px;
      border-radius: 16px;
      background: rgba(255, 255, 255, 0.18);
      backdrop-filter: blur(8px);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .hero-name {
      font-size: 22px;
      font-weight: 600;
      color: #fff;
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      flex-wrap: wrap;

      .status-tag {
        font-size: 12px;
        padding: 2px 8px;
        font-weight: normal;
      }
    }

    .hero-summary {
      color: rgba(255, 255, 255, 0.85);
      font-size: 13px;
      display: flex;
      align-items: center;
      flex-wrap: wrap;

      .summary-item {
        display: inline-flex;
        align-items: center;
        gap: 6px;
      }

      :deep(.ant-divider-vertical) {
        background: rgba(255, 255, 255, 0.3);
        height: 14px;
        margin: 0 12px;
      }
    }
  }

  // ============================== Tab 卡片 ==============================
  .tab-card {
    border-radius: 8px;

    :deep(.ant-card-body) {
      padding: 0 24px 24px;
    }
  }

  .section-row {
    margin-bottom: 16px;
  }

  .section-row-single {
    margin-top: 16px;
  }

  .section-card {
    border-radius: 8px;
    background: #fafafa;
    border: 1px solid #f0f0f0;
    height: 100%;

    :deep(.ant-card-head) {
      background: transparent;
      border-bottom: 1px solid #f0f0f0;
      padding: 0 16px;
      min-height: 48px;
    }

    :deep(.ant-card-body) {
      padding: 16px;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      font-weight: 600;
      color: #1f2937;

      :deep(svg) {
        color: #1890ff;
        font-size: 16px;
      }
    }
  }

  .mono-text {
    font-family: Menlo, Monaco, 'Courier New', monospace;
    font-size: 12px;
    background: #f5f5f5;
    padding: 2px 8px;
    border-radius: 3px;
    color: #d4380d;
  }

  .text-muted {
    color: #8c8c8c;
    font-style: italic;
  }

  // ============================== 决策显示 ==============================
  .decision-display {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    padding: 16px;
    background: #fff;
    border-radius: 6px;

    .decision-label {
      font-size: 12px;
      color: #8c8c8c;
      margin-bottom: 8px;
    }

    .decision-value {
      font-size: 24px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;

      &.allow {
        color: #52c41a;
      }

      &.deny {
        color: #f5222d;
      }
    }
  }

  // ============================== IP 白名单展示 ==============================
  .ip-list-display {
    background: #fff;
    border-radius: 6px;
    padding: 16px;

    .ip-label {
      font-size: 12px;
      color: #8c8c8c;
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .ip-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;

      :deep(.ant-tag) {
        font-family: Menlo, Monaco, monospace;
        font-size: 12px;
        margin: 0;
      }
    }
  }

  // ============================== 备注 ==============================
  .remark-text {
    margin: 0;
    color: #595959;
    line-height: 1.6;
    white-space: pre-wrap;
  }

  // ============================== 优先级徽章 ==============================
  :deep(.priority-badge) {
    display: inline-flex;
    align-items: center;

    .priority-num {
      font-size: 16px;
      font-weight: 600;
      font-family: Menlo, Monaco, monospace;
    }
  }

  @media (max-width: 768px) {
    .hero-card {
      :deep(.ant-card-body) {
        padding: 16px;
      }

      .hero-content {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
      }

      .hero-name {
        font-size: 18px;
      }
    }
  }
</style>
