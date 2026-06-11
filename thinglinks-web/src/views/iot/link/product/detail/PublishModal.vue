<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('iot.link.product.publish.modalTitle')"
    width="760px"
    @ok="handleSubmit"
    :confirmLoading="submitting"
    wrapClassName="publish-flexy-wrap"
  >
    <a-spin :spinning="submitting">
      <div class="flexy-body">
        <!-- ─────── 顶部产品信息条(紧凑单行) ─────── -->
        <div class="flexy-header">
          <div class="header-icon-wrap">
            <RocketOutlined class="header-icon" />
          </div>
          <div class="header-content">
            <div class="header-line">
              <span class="product-name">{{ productName ?? '—' }}</span>
              <span v-if="currentVersion" class="header-version-chip" :title="currentVersion">
                <span class="version-label">{{
                  t('iot.link.product.publish.currentVersion')
                }}</span>
                <span class="version-value">{{ formatSnapshotId(currentVersion) }}</span>
              </span>
            </div>
            <div class="header-hint">{{ t('iot.link.product.publish.hint') }}</div>
          </div>
        </div>

        <!-- ─────── 步骤 1:选择策略 ─────── -->
        <div class="flexy-card">
          <div class="step-header">
            <span class="step-no">1</span>
            <span class="step-title-text">{{ t('iot.link.product.publish.stepStrategy') }}</span>
          </div>
          <div class="strategy-grid">
            <div
              v-for="opt in strategyOptions"
              :key="opt.value"
              class="strategy-card"
              :class="[`tone-${opt.tone}`, { active: form.publishStrategy === opt.value }]"
              @click="onStrategyChange(opt.value)"
            >
              <div class="card-icon-wrap">
                <component :is="opt.icon" class="card-icon" />
              </div>
              <div class="card-title">{{ opt.label }}</div>
              <div class="card-desc">{{ opt.desc }}</div>
              <div v-if="form.publishStrategy === opt.value" class="card-check">
                <CheckOutlined />
              </div>
            </div>
          </div>
        </div>

        <!-- ─────── 步骤 2:策略配置 ─────── -->
        <div class="flexy-card">
          <div class="step-header">
            <span class="step-no">2</span>
            <span class="step-title-text">{{ t('iot.link.product.publish.stepConfig') }}</span>
          </div>

          <!-- 全量发布 提示卡 -->
          <div v-if="form.publishStrategy === 0" class="tip-card tone-blue">
            <div class="tip-icon-wrap">
              <GlobalOutlined class="tip-icon" />
            </div>
            <div class="tip-content">
              <div class="tip-title">{{ t('iot.link.product.publish.fullTipTitle') }}</div>
              <div class="tip-desc">{{ t('iot.link.product.publish.fullTipDesc') }}</div>
            </div>
          </div>

          <!-- 灰度发布 配置 -->
          <div v-else-if="form.publishStrategy === 1" class="canary-panel">
            <div class="source-tabs">
              <button
                v-for="src in canarySourceOptions"
                :key="src.value"
                class="source-tab"
                :class="{ active: canarySource === src.value }"
                @click="canarySource = src.value"
              >
                <component :is="src.icon" />
                <span>{{ src.label }}</span>
              </button>
            </div>

            <!-- 按分组 -->
            <div v-if="canarySource === 'group'" class="canary-content">
              <div class="field-label">{{ t('iot.link.product.publish.canarySourceGroup') }}</div>
              <a-select
                v-model:value="selectedGroupId"
                :placeholder="t('iot.link.product.publish.groupSelectPlaceholder')"
                :loading="groupLoading"
                :options="groupOptions"
                show-search
                :filter-option="filterGroupOption"
                allow-clear
                size="large"
                style="width: 100%"
                :getPopupContainer="(triggerNode: any) => triggerNode.parentNode"
                @change="onGroupChange"
              />
              <div class="field-extra">
                {{ t('iot.link.product.publish.canarySourceGroupTip') }}
              </div>

              <div v-if="groupFetching" class="group-preview tone-neutral">
                <LoadingOutlined /> {{ t('common.loading') }}
              </div>
              <div
                v-else-if="selectedGroupId && groupDeviceList.length === 0"
                class="group-preview tone-warning"
              >
                <InboxOutlined />
                {{ t('iot.link.product.publish.groupEmpty') }}
              </div>
              <div
                v-else-if="selectedGroupId && groupDeviceList.length > 0"
                class="group-preview tone-success"
              >
                <CheckCircleOutlined class="ok-icon" />
                <span class="preview-text">{{
                  t('iot.link.product.publish.groupDevicePreview', {
                    count: groupDeviceList.length,
                  })
                }}</span>
                <div class="device-chips">
                  <a-tag v-for="d in groupDeviceList.slice(0, 8)" :key="d" color="blue">
                    {{ d }}
                  </a-tag>
                  <a-tag v-if="groupDeviceList.length > 8">
                    +{{ groupDeviceList.length - 8 }}
                  </a-tag>
                </div>
              </div>
            </div>

            <!-- 按设备 -->
            <div v-else-if="canarySource === 'manual'" class="canary-content">
              <div class="field-label">{{ t('iot.link.product.publish.deviceWhitelist') }}</div>
              <a-textarea
                v-model:value="manualWhitelistText"
                :rows="4"
                placeholder="dev001&#10;dev002"
              />
              <div class="field-extra">
                {{ t('iot.link.product.publish.canarySourceManualTip') }}
              </div>
            </div>

            <!-- 按比例 -->
            <div v-else class="canary-content">
              <div class="field-label-row">
                <span class="field-label">{{
                  t('iot.link.product.publish.canarySourcePercent')
                }}</span>
                <div class="percent-display">
                  <span class="percent-value">{{ canaryPercent }}</span>
                  <span class="percent-unit">%</span>
                </div>
              </div>
              <div class="percent-block">
                <a-slider
                  v-model:value="canaryPercent"
                  :min="1"
                  :max="99"
                  :step="1"
                  :marks="percentMarks"
                  class="percent-slider"
                />
              </div>
              <div class="percent-quick">
                <span class="quick-label">{{
                  t('iot.link.product.publish.canaryPercentQuick')
                }}</span>
                <button
                  v-for="p in [5, 10, 25, 50, 75]"
                  :key="p"
                  class="quick-btn"
                  :class="{ active: canaryPercent === p }"
                  @click="canaryPercent = p"
                >
                  {{ p }}%
                </button>
              </div>
              <div class="field-extra">
                {{ t('iot.link.product.publish.canarySourcePercentTip') }}
              </div>
            </div>

            <!-- 汇总 -->
            <div class="canary-summary">
              <template v-if="canarySource === 'percent'">
                <span v-if="canaryPercent > 0" class="summary-ok">
                  <CheckCircleFilled />
                  {{
                    t('iot.link.product.publish.canaryPercentSummary', {
                      percent: canaryPercent,
                    })
                  }}
                </span>
                <span v-else class="summary-empty">
                  <ExclamationCircleFilled />
                  {{ t('iot.link.product.publish.canaryEmpty') }}
                </span>
              </template>
              <template v-else>
                <span v-if="canaryDeviceCount > 0" class="summary-ok">
                  <CheckCircleFilled />
                  {{ t('iot.link.product.publish.canarySummary', { count: canaryDeviceCount }) }}
                </span>
                <span v-else class="summary-empty">
                  <ExclamationCircleFilled />
                  {{ t('iot.link.product.publish.canaryEmpty') }}
                </span>
              </template>
            </div>
          </div>

          <!-- 影子发布 提示卡 -->
          <div v-else-if="form.publishStrategy === 2" class="tip-card tone-purple">
            <div class="tip-icon-wrap">
              <EyeOutlined class="tip-icon" />
            </div>
            <div class="tip-content">
              <div class="tip-title">{{ t('iot.link.product.publish.shadowTipTitle') }}</div>
              <div class="tip-desc">{{ t('iot.link.product.publish.shadowTipDesc') }}</div>
            </div>
          </div>
        </div>

        <!-- ─────── 步骤 3:发布说明 ─────── -->
        <div class="flexy-card">
          <div class="step-header">
            <span class="step-no">3</span>
            <span class="step-title-text">{{ t('iot.link.product.publish.stepRemark') }}</span>
            <span class="step-optional">{{ t('common.optional') }}</span>
          </div>
          <a-textarea
            v-model:value="form.publishRemark"
            :rows="3"
            :maxlength="500"
            showCount
            :placeholder="t('iot.link.product.publish.publishRemarkPlaceholder')"
          />
        </div>
      </div>
    </a-spin>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import {
    RocketOutlined,
    GlobalOutlined,
    BranchesOutlined,
    EyeOutlined,
    ApartmentOutlined,
    EditOutlined,
    PercentageOutlined,
    CheckCircleOutlined,
    CheckCircleFilled,
    ExclamationCircleFilled,
    LoadingOutlined,
    InboxOutlined,
    CheckOutlined,
  } from '@ant-design/icons-vue';
  import { publishVersion } from '/@/api/iot/link/productVersion/productVersion';
  import type { ProductVersionPublishVO } from '/@/api/iot/link/productVersion/model/productVersionModel';
  import { tree as fetchGroupTree } from '/@/api/iot/link/group/deviceGroup';
  import { query as queryGroupRel } from '/@/api/iot/link/group/deviceGroupRel';
  import type { DeviceGroupResultVO } from '/@/api/iot/link/group/model/deviceGroupModel';
  import { formatSnapshotId } from '/@/utils/iot/version';

  const { t } = useI18n();
  const { createMessage } = useMessage();

  const emit = defineEmits(['success']);

  const productIdentification = ref<string>('');
  const productName = ref<string | undefined>(undefined);
  const currentVersion = ref<string | undefined>(undefined);

  const submitting = ref(false);

  const form = reactive<ProductVersionPublishVO>({
    productIdentification: '',
    publishStrategy: 0,
    canaryConfigJson: undefined,
    publishRemark: undefined,
  });

  // 灰度来源:分组 / 手填 / 比例 三选一互斥
  type CanarySource = 'group' | 'manual' | 'percent';
  const canarySource = ref<CanarySource>('group');
  const manualWhitelistText = ref<string>('');

  // 分组下拉
  const groupOptions = ref<{ label: string; value: string }[]>([]);
  const groupLoading = ref(false);
  const selectedGroupId = ref<string | undefined>(undefined);
  const groupDeviceList = ref<string[]>([]);
  const groupFetching = ref(false);

  // 比例
  const canaryPercent = ref<number>(10);
  const percentMarks = {
    1: '1%',
    25: '25%',
    50: '50%',
    75: '75%',
    99: '99%',
  };

  /** 发布策略卡片配置 ── tone 决定主题色(蓝/绿/紫),与 Flexy 配色保持一致。 */
  const strategyOptions = computed(() => [
    {
      value: 0,
      tone: 'blue',
      label: t('iot.link.product.publish.strategyLabel.0'),
      desc: t('iot.link.product.publish.strategyDesc.0'),
      icon: GlobalOutlined,
    },
    {
      value: 1,
      tone: 'green',
      label: t('iot.link.product.publish.strategyLabel.1'),
      desc: t('iot.link.product.publish.strategyDesc.1'),
      icon: BranchesOutlined,
    },
    {
      value: 2,
      tone: 'purple',
      label: t('iot.link.product.publish.strategyLabel.2'),
      desc: t('iot.link.product.publish.strategyDesc.2'),
      icon: EyeOutlined,
    },
  ]);

  /** 灰度来源 tab 配置(配 source-tabs)。 */
  const canarySourceOptions = computed(() => [
    {
      value: 'group' as CanarySource,
      label: t('iot.link.product.publish.canarySourceGroup'),
      icon: ApartmentOutlined,
    },
    {
      value: 'manual' as CanarySource,
      label: t('iot.link.product.publish.canarySourceManual'),
      icon: EditOutlined,
    },
    {
      value: 'percent' as CanarySource,
      label: t('iot.link.product.publish.canarySourcePercent'),
      icon: PercentageOutlined,
    },
  ]);

  /** 灰度模式(group / manual)下,当前最终生效的设备数。percent 模式走 canaryPercent 独立分支。 */
  const canaryDeviceCount = computed(() => {
    if (canarySource.value === 'group') {
      return groupDeviceList.value.length;
    }
    if (canarySource.value === 'manual') {
      return manualWhitelistText.value
        .split(/[\n,]/)
        .map((s) => s.trim())
        .filter(Boolean).length;
    }
    return 0;
  });

  const [registerModal, { closeModal }] = useModalInner((data) => {
    productIdentification.value = data.productIdentification;
    productName.value = data.productName;
    currentVersion.value = data.currentVersion;

    form.productIdentification = data.productIdentification;
    form.publishStrategy = 0;
    form.canaryConfigJson = undefined;
    form.publishRemark = undefined;

    canarySource.value = 'group';
    manualWhitelistText.value = '';
    selectedGroupId.value = undefined;
    groupDeviceList.value = [];
    canaryPercent.value = 10;
  });

  function onStrategyChange(v: number) {
    form.publishStrategy = v;
    // 切到灰度且未加载分组,后台拉一次
    if (v === 1 && groupOptions.value.length === 0) {
      loadGroupTree();
    }
  }

  /** 拍平分组树为 select options(支持嵌套分组,用 / 分层展示)。 */
  function flattenGroup(
    nodes: DeviceGroupResultVO[] | undefined,
    prefix = '',
  ): { label: string; value: string }[] {
    if (!nodes || !nodes.length) return [];
    const out: { label: string; value: string }[] = [];
    nodes.forEach((n) => {
      const label = prefix ? `${prefix} / ${n.groupName ?? ''}` : n.groupName ?? '';
      if (n.id) {
        out.push({ label, value: String(n.id) });
      }
      const children = (n as any).children as DeviceGroupResultVO[] | undefined;
      if (children?.length) {
        out.push(...flattenGroup(children, label));
      }
    });
    return out;
  }

  async function loadGroupTree() {
    groupLoading.value = true;
    try {
      const res = await fetchGroupTree();
      const list = Array.isArray(res) ? (res as any) : res ? [res] : [];
      groupOptions.value = flattenGroup(list as DeviceGroupResultVO[]);
    } catch (e) {
      groupOptions.value = [];
    } finally {
      groupLoading.value = false;
    }
  }

  function filterGroupOption(input: string, option: any) {
    return String(option.label).toLowerCase().includes(input.toLowerCase());
  }

  async function onGroupChange(gid?: string) {
    groupDeviceList.value = [];
    if (!gid) return;
    groupFetching.value = true;
    try {
      const list = (await queryGroupRel({ groupId: gid } as any)) as any[];
      groupDeviceList.value = (list || [])
        .map((r) => r?.deviceIdentification)
        .filter((s): s is string => !!s);
    } catch {
      groupDeviceList.value = [];
      createMessage.error(t('iot.link.product.publish.groupLoadFailed'));
    } finally {
      groupFetching.value = false;
    }
  }

  /** 切来源时,把其他两侧已有数据清空,避免歧义。 */
  watch(canarySource, (cur) => {
    if (cur !== 'manual') manualWhitelistText.value = '';
    if (cur !== 'group') {
      selectedGroupId.value = undefined;
      groupDeviceList.value = [];
    }
    if (cur !== 'percent') canaryPercent.value = 10;
  });

  /**
   * 把 UI 灰度配置序列化为后端 {@code CanaryRuleMatcher} 期望的 JSON。
   *
   * - whitelist 模式(分组 / 手填):{"mode":"whitelist","deviceIdentifications":[...]}
   * - percent 模式:{"mode":"percent","canaryPercent":30}
   */
  function buildCanaryConfigJson(): string | undefined {
    if (form.publishStrategy !== 1) return undefined;
    if (canarySource.value === 'percent') {
      return JSON.stringify({ mode: 'percent', canaryPercent: canaryPercent.value });
    }
    const devices =
      canarySource.value === 'group'
        ? groupDeviceList.value
        : manualWhitelistText.value
            .split(/[\n,]/)
            .map((s) => s.trim())
            .filter(Boolean);
    return JSON.stringify({ mode: 'whitelist', deviceIdentifications: devices });
  }

  async function handleSubmit() {
    if (form.publishStrategy == null) {
      createMessage.warning(t('iot.link.product.publish.noStrategy'));
      return;
    }
    if (form.publishStrategy === 1) {
      if (canarySource.value === 'percent') {
        if (!canaryPercent.value || canaryPercent.value < 1 || canaryPercent.value > 99) {
          createMessage.warning(t('iot.link.product.publish.noCanaryPercent'));
          return;
        }
      } else if (canaryDeviceCount.value === 0) {
        createMessage.warning(t('iot.link.product.publish.noCanaryDevice'));
        return;
      }
    }
    submitting.value = true;
    try {
      form.canaryConfigJson = buildCanaryConfigJson();
      await publishVersion(form);
      createMessage.success(t('iot.link.product.publish.success'));
      closeModal();
      emit('success');
    } catch (e: any) {
      createMessage.error(
        t('iot.link.product.publish.failed') + (e?.message ? ': ' + e.message : ''),
      );
    } finally {
      submitting.value = false;
    }
  }
</script>

<style lang="less">
  /* ──────────────────────────────────────────────────────────────────────
   * Flexy 风格 ── 故意使用<b>非 scoped</b> CSS,以 .publish-flexy-wrap 做命名空间隔离。
   *
   * <b>为什么不能用 scoped + :deep()</b>:
   * Vue 3 的 scoped CSS 会把选择器编译为 [data-v-hash] .xxx,即要求有一个带
   * data-v-hash 属性的<b>祖先元素</b>。但 antd Modal 经过 <Teleport> 渲染到 body,
   * .ant-modal-wrap(挂着 publish-flexy-wrap class)及其内部所有 DOM 都<b>不在</b>
   * 当前组件 template 的 DOM 树里,自然没有 data-v-hash 祖先 ── 整套 :deep()
   * 选择器全部匹配失败,这就是用户两次反馈"样式没生效"的真凶。
   *
   * <b>当前方案</b>:整块去 scoped,所有选择器都在 .publish-flexy-wrap 命名空间下,
   * 该 wrap class 仅本 modal 使用,不会污染其他页面 / modal。
   *
   * <b>配色</b>:主蓝 #5d87ff、灰度绿 #13deb9、影子紫 #9b75e6、警告橙 #ffae1f。
   * ────────────────────────────────────────────────────────────────────── */
  .publish-flexy-wrap {
    .ant-modal-content {
      border-radius: 18px;
      overflow: hidden;
      box-shadow: 0 12px 48px rgba(15, 23, 42, 0.18);
    }

    .ant-modal-header {
      padding: 18px 24px;
      border-bottom: 1px solid #eef0f4;
      background: #fff;

      .ant-modal-title {
        font-size: 16px;
        font-weight: 700;
        color: #2a3547;
      }
    }

    .ant-modal-close-x {
      width: 48px;
      height: 48px;
      line-height: 48px;
      color: #a0aec0;
      transition: color 0.18s;

      &:hover {
        color: #5d87ff;
      }
    }

    .ant-modal-body {
      background: #f5f7fa;
      padding: 20px;
      max-height: calc(100vh - 220px);
      overflow-y: auto;
    }

    .ant-modal-footer {
      padding: 14px 24px;
      border-top: 1px solid #eef0f4;
      background: #fff;

      .ant-btn {
        height: 38px;
        padding: 0 20px;
        border-radius: 10px;
        font-weight: 500;
      }

      .ant-btn-primary {
        background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
        border: none;
        box-shadow: 0 6px 14px rgba(93, 135, 255, 0.35);

        &:hover {
          box-shadow: 0 8px 18px rgba(93, 135, 255, 0.45);
          transform: translateY(-1px);
        }
      }
    }

    /* ─── 主容器:卡片纵向堆叠 ─── */
    .flexy-body {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }

    /* ─── 通用白色卡片基线 ─── */
    .flexy-card {
      background: #fff;
      border-radius: 16px;
      padding: 20px;
      box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04), 0 2px 12px rgba(15, 23, 42, 0.03);
    }

    /* ─── 顶部产品信息条:紧凑单行(图标 / 产品名 / 版本 chip / 提示) ─── */
    .flexy-header {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 14px 18px;
      border-radius: 16px;
      background: linear-gradient(135deg, #eef2ff 0%, #f0f7ff 100%);
      box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04);

      .header-icon-wrap {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
        color: #fff;
        flex-shrink: 0;
        box-shadow: 0 6px 14px rgba(93, 135, 255, 0.35);

        .header-icon {
          font-size: 20px;
        }
      }

      .header-content {
        flex: 1;
        min-width: 0;
      }

      /* 第一行:产品名 + 版本 chip */
      .header-line {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;
        min-width: 0;

        .product-name {
          font-size: 15px;
          font-weight: 700;
          color: #2a3547;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          max-width: 280px;
        }
      }

      /* 版本 chip:浅蓝胶囊,版本号截断 + tooltip,不带复制按钮 */
      .header-version-chip {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 2px 10px;
        border-radius: 10px;
        background: rgba(93, 135, 255, 0.1);
        line-height: 1.6;

        .version-label {
          font-size: 11px;
          color: #5a6a85;
        }

        .version-value {
          font-size: 12px;
          font-weight: 600;
          color: #5d87ff;
          font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
          letter-spacing: 0.2px;
        }
      }

      /* 第二行:提示文字,字号小、行距紧凑 */
      .header-hint {
        margin-top: 4px;
        font-size: 12px;
        color: #5a6a85;
        line-height: 1.5;
      }
    }

    /* ─── 步骤标题(1/2/3 数字 chip + 标题文本) ─── */
    .step-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 18px;

      .step-no {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 26px;
        height: 26px;
        border-radius: 50%;
        background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
        color: #fff;
        font-size: 13px;
        font-weight: 700;
        box-shadow: 0 4px 10px rgba(93, 135, 255, 0.35);
      }

      .step-title-text {
        font-size: 15px;
        font-weight: 700;
        color: #2a3547;
      }

      .step-optional {
        font-size: 11px;
        color: #a0aec0;
        padding: 2px 10px;
        border-radius: 10px;
        background: #f5f7fa;
        font-weight: 500;
      }
    }

    /* ─── 策略卡片(3 列网格,tone 主题色驱动) ─── */
    .strategy-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;
    }

    .strategy-card {
      position: relative;
      padding: 20px 14px;
      border-radius: 14px;
      background: #f8fafc;
      border: 2px solid transparent;
      cursor: pointer;
      transition: all 0.2s ease;
      text-align: center;

      &:hover {
        background: #eef2ff;
        transform: translateY(-2px);
      }

      .card-icon-wrap {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 48px;
        height: 48px;
        border-radius: 14px;
        margin-bottom: 12px;
        transition: all 0.2s ease;

        .card-icon {
          font-size: 24px;
        }
      }

      .card-title {
        font-size: 14px;
        font-weight: 700;
        color: #2a3547;
        margin-bottom: 4px;
      }

      .card-desc {
        font-size: 12px;
        color: #5a6a85;
        line-height: 1.5;
      }

      .card-check {
        position: absolute;
        top: 10px;
        right: 10px;
        width: 22px;
        height: 22px;
        border-radius: 50%;
        background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
        color: #fff;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        box-shadow: 0 4px 10px rgba(93, 135, 255, 0.4);
      }

      /* tone idle */
      &.tone-blue .card-icon-wrap {
        background: rgba(93, 135, 255, 0.12);
        color: #5d87ff;
      }
      &.tone-green .card-icon-wrap {
        background: rgba(19, 222, 185, 0.14);
        color: #13deb9;
      }
      &.tone-purple .card-icon-wrap {
        background: rgba(155, 117, 230, 0.14);
        color: #9b75e6;
      }

      /* tone active */
      &.active {
        background: #fff;

        &.tone-blue {
          border-color: #5d87ff;
          box-shadow: 0 6px 20px rgba(93, 135, 255, 0.18);
          .card-icon-wrap {
            background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
            color: #fff;
            box-shadow: 0 8px 18px rgba(93, 135, 255, 0.4);
          }
        }
        &.tone-green {
          border-color: #13deb9;
          box-shadow: 0 6px 20px rgba(19, 222, 185, 0.2);
          .card-icon-wrap {
            background: linear-gradient(135deg, #13deb9 0%, #36e2c2 100%);
            color: #fff;
            box-shadow: 0 8px 18px rgba(19, 222, 185, 0.4);
          }
          .card-check {
            background: linear-gradient(135deg, #13deb9 0%, #36e2c2 100%);
            box-shadow: 0 4px 10px rgba(19, 222, 185, 0.4);
          }
        }
        &.tone-purple {
          border-color: #9b75e6;
          box-shadow: 0 6px 20px rgba(155, 117, 230, 0.2);
          .card-icon-wrap {
            background: linear-gradient(135deg, #9b75e6 0%, #ad8df0 100%);
            color: #fff;
            box-shadow: 0 8px 18px rgba(155, 117, 230, 0.4);
          }
          .card-check {
            background: linear-gradient(135deg, #9b75e6 0%, #ad8df0 100%);
            box-shadow: 0 4px 10px rgba(155, 117, 230, 0.4);
          }
        }
      }
    }

    /* ─── 提示卡(全量 / 影子) ─── */
    .tip-card {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 16px 18px;
      border-radius: 12px;

      .tip-icon-wrap {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        .tip-icon {
          font-size: 20px;
        }
      }

      .tip-content {
        flex: 1;

        .tip-title {
          font-size: 13px;
          font-weight: 700;
          margin-bottom: 4px;
          color: #2a3547;
        }

        .tip-desc {
          font-size: 12px;
          line-height: 1.6;
          color: #5a6a85;
        }
      }

      &.tone-blue {
        background: linear-gradient(135deg, #eef5ff 0%, #f5f9ff 100%);
        .tip-icon-wrap {
          background: rgba(93, 135, 255, 0.16);
          color: #5d87ff;
        }
      }

      &.tone-purple {
        background: linear-gradient(135deg, #f5f0ff 0%, #faf6ff 100%);
        .tip-icon-wrap {
          background: rgba(155, 117, 230, 0.16);
          color: #9b75e6;
        }
      }
    }

    /* ─── 灰度配置 ─── */
    .canary-panel {
      .source-tabs {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 6px;
        padding: 5px;
        border-radius: 12px;
        background: #f0f3f9;
      }

      .source-tab {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        padding: 10px 12px;
        border-radius: 10px;
        font-size: 13px;
        font-weight: 500;
        color: #5a6a85;
        background: transparent;
        border: none;
        cursor: pointer;
        transition: all 0.18s ease;

        &:hover {
          color: #5d87ff;
          background: rgba(93, 135, 255, 0.08);
        }

        &.active {
          background: #fff;
          color: #5d87ff;
          font-weight: 700;
          box-shadow: 0 3px 10px rgba(93, 135, 255, 0.18);
        }

        .anticon {
          font-size: 13px;
        }
      }

      .canary-content {
        margin-top: 16px;

        .field-label {
          font-size: 13px;
          font-weight: 600;
          color: #2a3547;
          margin-bottom: 8px;
        }

        .field-extra {
          margin-top: 8px;
          font-size: 12px;
          color: #a0aec0;
          line-height: 1.5;
        }
      }
    }

    /* ─── 比例 percent 区块(标题+大数字 / 滑块 / 快捷按钮) ─── */
    .field-label-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 14px;
    }

    .percent-display {
      display: inline-flex;
      align-items: baseline;
      gap: 2px;
      padding: 6px 14px;
      border-radius: 10px;
      background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
      color: #fff;
      box-shadow: 0 4px 12px rgba(93, 135, 255, 0.35);

      .percent-value {
        font-size: 22px;
        font-weight: 700;
        line-height: 1;
        font-variant-numeric: tabular-nums;
      }

      .percent-unit {
        font-size: 13px;
        font-weight: 600;
        opacity: 0.9;
      }
    }

    .percent-block {
      padding: 4px 8px 28px;
      background: #f8fafc;
      border-radius: 12px;
    }

    .percent-slider {
      width: 100%;
    }

    .percent-quick {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 14px;
      flex-wrap: wrap;

      .quick-label {
        font-size: 12px;
        color: #a0aec0;
        margin-right: 2px;
      }

      .quick-btn {
        padding: 4px 12px;
        border-radius: 8px;
        font-size: 12px;
        font-weight: 600;
        color: #5d87ff;
        background: rgba(93, 135, 255, 0.08);
        border: 1px solid transparent;
        cursor: pointer;
        transition: all 0.18s ease;
        font-variant-numeric: tabular-nums;

        &:hover {
          background: rgba(93, 135, 255, 0.15);
          transform: translateY(-1px);
        }

        &.active {
          background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
          color: #fff;
          box-shadow: 0 4px 10px rgba(93, 135, 255, 0.35);
        }
      }
    }

    /* ─── antd slider 品牌化 ─── */
    .ant-slider {
      margin: 12px 6px 22px;

      .ant-slider-rail {
        background: #e6ecf5;
        height: 6px;
        border-radius: 3px;
      }

      .ant-slider-track {
        background: linear-gradient(90deg, #5d87ff 0%, #49beff 100%);
        height: 6px;
        border-radius: 3px;
      }

      .ant-slider-handle {
        width: 18px;
        height: 18px;
        margin-top: -6px;
        background: #fff;
        border: 3px solid #5d87ff;
        box-shadow: 0 4px 10px rgba(93, 135, 255, 0.4);

        &::after {
          display: none;
        }

        &:focus-visible {
          box-shadow: 0 0 0 4px rgba(93, 135, 255, 0.18), 0 4px 10px rgba(93, 135, 255, 0.4);
        }
      }

      .ant-slider-dot {
        width: 8px;
        height: 8px;
        border-color: #d6dde7;

        &.ant-slider-dot-active {
          border-color: #5d87ff;
        }
      }

      .ant-slider-mark-text {
        color: #97a1b0;
        font-size: 11px;

        &.ant-slider-mark-text-active {
          color: #5d87ff;
          font-weight: 600;
        }
      }
    }

    /* ─── antd input / select / textarea 品牌化 ─── */
    .ant-input,
    .ant-input-affix-wrapper,
    .ant-select .ant-select-selector,
    .ant-input-number {
      border-radius: 10px;
      border-color: #e6ecf5;
      transition: all 0.18s ease;

      &:hover {
        border-color: #5d87ff;
      }

      &:focus,
      &-focused {
        border-color: #5d87ff;
        box-shadow: 0 0 0 3px rgba(93, 135, 255, 0.12);
      }
    }

    .ant-select-large .ant-select-selector {
      padding: 4px 14px !important;
    }

    /* ─── 分组预览 ─── */
    .group-preview {
      margin-top: 12px;
      padding: 12px 14px;
      border-radius: 10px;
      font-size: 13px;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;

      .preview-text {
        font-weight: 600;
      }

      .device-chips {
        display: flex;
        flex-wrap: wrap;
        gap: 4px;
        margin-left: 4px;
      }

      &.tone-neutral {
        background: #f5f7fa;
        color: #a0aec0;
      }

      &.tone-warning {
        background: #fff7e6;
        color: #ffae1f;
        border: 1px solid #ffe1a8;
      }

      &.tone-success {
        background: #ebfaf2;
        color: #0fb094;
        border: 1px solid #b7ecd9;

        .ok-icon {
          color: #13deb9;
          font-size: 15px;
        }
      }
    }

    /* ─── 灰度汇总(底部) ─── */
    .canary-summary {
      margin-top: 16px;
      padding: 12px 14px;
      border-radius: 10px;
      font-size: 13px;
      background: #fafbfc;

      .summary-ok {
        color: #0fb094;
        font-weight: 700;
        display: inline-flex;
        align-items: center;
        gap: 6px;

        .anticon {
          color: #13deb9;
          font-size: 15px;
        }
      }

      .summary-empty {
        color: #b58100;
        font-weight: 600;
        display: inline-flex;
        align-items: center;
        gap: 6px;

        .anticon {
          color: #ffae1f;
          font-size: 15px;
        }
      }
    }
  }
</style>
