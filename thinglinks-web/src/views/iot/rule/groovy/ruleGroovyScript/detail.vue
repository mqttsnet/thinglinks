<template>
  <PageWrapper contentFullHeight class="groovy-detail">
    <EditModal @register="registerEditModal" @success="load" />

    <!-- ===== 顶部 Header（Flexy 风格） ===== -->
    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="script-icon">
            <CodeOutlined />
          </div>
          <div class="script-meta">
            <div class="script-title">
              <span class="name-text">{{ detailData.name || '-' }}</span>
              <Tag :color="detailData.enable ? 'success' : 'default'">
                {{
                  detailData.enable
                    ? t('iot.rule.groovy.ruleGroovyScript.enabled')
                    : t('iot.rule.groovy.ruleGroovyScript.disabled')
                }}
              </Tag>
              <Tag color="blue">{{ t('iot.rule.groovy.ruleGroovyScript.card.languageBadge') }}</Tag>
            </div>
            <div class="meta-line">
              <span>
                <AppstoreOutlined />
                {{ getDictLabel('LINK_APPLICATION_SCENARIO', detailData.appId, '-') }}
              </span>
              <a-divider type="vertical" />
              <span>
                <ApartmentOutlined />
                {{ getDictLabel('RULE_GROOVY_SCRIPT_CHANNEL_CODE', detailData.channelCode, '-') }}
              </span>
              <a-divider type="vertical" />
              <span>
                <ClockCircleOutlined />
                {{ detailData.updatedTime || detailData.createdTime || '-' }}
              </span>
            </div>
          </div>
        </div>
        <a-space>
          <a-button @click="handleCopyScript">
            <template #icon><CopyOutlined /></template>
            {{ t('iot.rule.groovy.ruleGroovyScript.details.copyScript') }}
          </a-button>
          <a-button @click="handleSave" v-hasAnyPermission="['rule:groovy:ruleGroovyScript:edit']">
            <template #icon><FileDoneOutlined /></template>
            {{ t('iot.rule.groovy.ruleGroovyScript.details.save') }}
          </a-button>
          <a-button
            type="primary"
            @click="handleEdit"
            v-hasAnyPermission="['rule:groovy:ruleGroovyScript:edit']"
          >
            <template #icon><EditOutlined /></template>
            {{ t('common.title.edit') }}
          </a-button>
        </a-space>
      </div>
    </Card>

    <!-- ===== 指标卡 ===== -->
    <Row :gutter="[16, 16]" class="metric-row" align="stretch">
      <!-- 1. 产品 -->
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon biz"><TagsOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{
              t('iot.rule.groovy.ruleGroovyScript.topicInbound.product')
            }}</div>
            <div class="metric-value" :title="detailData.productIdentification">
              {{ detailData.productIdentification || '-' }}
            </div>
          </div>
        </Card>
      </Col>
      <!-- 2. 产品版本(可切换:调试 / 绑定下个版本;并标注是否为产品当前生效版本)-->
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon ver"><BranchesOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.groovy.ruleGroovyScript.topicInbound.productVersion') }}
              <a-tooltip :title="t('iot.rule.groovy.ruleGroovyScript.details.debugVersionTip')">
                <QuestionCircleOutlined class="ver-help" />
              </a-tooltip>
            </div>
            <a-select
              v-model:value="detailData.objectVersion"
              :options="versionOptions"
              :loading="versionLoading"
              size="small"
              option-label-prop="value"
              class="ver-select"
              :dropdown-match-select-width="false"
              :placeholder="t('iot.rule.groovy.ruleGroovyScript.topicInbound.versionPlaceholder')"
            >
              <template #option="{ value, statusText, isActive }">
                <span class="ver-no">{{ value }}</span>
                <a-tag v-if="statusText" :color="versionStatusColor(statusText)" class="ver-tag">
                  {{ statusText }}
                </a-tag>
                <a-tag v-if="isActive" color="orange" class="ver-tag">
                  {{ t('iot.rule.groovy.ruleGroovyScript.topicInbound.activeVersion') }}
                </a-tag>
              </template>
            </a-select>
            <div class="ver-sub">
              <span class="ver-active-text">
                {{ t('iot.rule.groovy.ruleGroovyScript.details.activeVersionLabel') }}:
                {{ productActiveVersion || '-' }}
              </span>
              <a-tag v-if="versionMatch === true" color="success" class="ver-tag">
                {{ t('iot.rule.groovy.ruleGroovyScript.details.versionInUse') }}
              </a-tag>
              <a-tag v-else-if="versionMatch === false" color="warning" class="ver-tag">
                {{ t('iot.rule.groovy.ruleGroovyScript.details.versionOutdated') }}
              </a-tag>
            </div>
          </div>
        </Card>
      </Col>
      <!-- 3. 主题模式 -->
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon id"><NodeIndexOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{
              t('iot.rule.groovy.ruleGroovyScript.topicInbound.topicPattern')
            }}</div>
            <div class="metric-value" :title="detailData.topicPattern">
              {{ detailData.topicPattern || '-' }}
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon stat"><BarChartOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{
              t('iot.rule.groovy.ruleGroovyScript.execStat.title')
            }}</div>
            <div class="metric-value">{{ execStatData.total }}</div>
            <div class="metric-stat-sub">
              <span class="ok"
                >{{ t('iot.rule.groovy.ruleGroovyScript.execStat.success') }}
                {{ execStatData.success }}</span
              >
              <span class="fail"
                >{{ t('iot.rule.groovy.ruleGroovyScript.execStat.fail') }}
                {{ execStatData.fail }}</span
              >
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- ===== 主体：左脚本编辑 + 右测试面板 ===== -->
    <Row :gutter="[16, 16]" class="main-row">
      <Col :xs="24" :lg="14">
        <Card :bordered="false" class="panel-card">
          <template #title>
            <span class="panel-title">{{
              t('iot.rule.groovy.ruleGroovyScript.details.editScript')
            }}</span>
          </template>
          <template #extra>
            <a-space>
              <span class="lang-label">
                {{ t('iot.rule.groovy.ruleGroovyScript.details.scriptingLanguage') }}：
              </span>
              <a-select v-model:value="language" style="width: 110px">
                <a-select-option value="Groovy">Groovy</a-select-option>
              </a-select>
            </a-space>
          </template>
          <codemirror
            v-model="detailData.scriptContent"
            :autofocus="false"
            :extensions="scriptExtensions"
            :indent-with-tab="true"
            :style="{ height: '560px' }"
            :tab-size="2"
            placeholder="// Groovy"
          />
        </Card>
        <Card :bordered="false" class="panel-card config-card">
          <template #title>
            <span class="panel-title">{{
              t('iot.rule.groovy.ruleGroovyScript.details.debug.configLabel')
            }}</span>
          </template>
          <template #extra>
            <a-button size="small" type="link" @click="handleFormatExtendParams">
              <template #icon><FormOutlined /></template>
              {{ t('iot.rule.groovy.ruleGroovyScript.details.formatJson') }}
            </a-button>
          </template>
          <a-textarea
            v-model:value="detailData.extendParams"
            :rows="6"
            :placeholder="t('iot.rule.groovy.ruleGroovyScript.details.extendParamsPlaceholder')"
          />
          <div class="config-hint">
            <InfoCircleOutlined />
            <span>{{ t('iot.rule.groovy.ruleGroovyScript.helpMessage.extendParams') }}</span>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :lg="10">
        <ScriptDebugPanel
          ref="debugPanelRef"
          :script-content="detailData.scriptContent"
          :product-identification="detailData.productIdentification"
          :topic-pattern="detailData.topicPattern"
          :history-key="id"
          :result-name="detailData.name"
          :extend-params="detailData.extendParams"
          :script-unique-key="scriptUniqueKey"
          :object-version="detailData.objectVersion"
        />
      </Col>
    </Row>

    <!-- ===== 元数据 ===== -->
    <Card :bordered="false" class="meta-card">
      <template #title>
        <span class="panel-title">{{
          t('iot.rule.groovy.ruleGroovyScript.details.metaTitle')
        }}</span>
      </template>
      <a-descriptions bordered :column="{ xs: 1, sm: 2, lg: 3 }" size="small">
        <a-descriptions-item :label="t('iot.rule.groovy.ruleGroovyScript.appId')">
          {{ getDictLabel('LINK_APPLICATION_SCENARIO', detailData.appId, '-') }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('iot.rule.groovy.ruleGroovyScript.channelCode')">
          {{ getDictLabel('RULE_GROOVY_SCRIPT_CHANNEL_CODE', detailData.channelCode, '-') }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('iot.rule.groovy.ruleGroovyScript.topicInbound.product')">
          {{ detailData.productIdentification || '-' }}
        </a-descriptions-item>
        <a-descriptions-item
          :label="t('iot.rule.groovy.ruleGroovyScript.topicInbound.topicPattern')"
        >
          {{ detailData.topicPattern || '-' }}
        </a-descriptions-item>
        <a-descriptions-item
          :label="t('iot.rule.groovy.ruleGroovyScript.topicInbound.productVersion')"
        >
          {{ detailData.objectVersion || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('iot.rule.groovy.ruleGroovyScript.enable')">
          <Tag :color="detailData.enable ? 'success' : 'default'">
            {{
              detailData.enable
                ? t('iot.rule.groovy.ruleGroovyScript.enabled')
                : t('iot.rule.groovy.ruleGroovyScript.disabled')
            }}
          </Tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('iot.rule.groovy.ruleGroovyScript.extendParams')" :span="3">
          {{ detailData.extendParams || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('iot.rule.groovy.ruleGroovyScript.remark')" :span="3">
          {{ detailData.remark || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('thinglinks.common.createdTime')">
          {{ detailData.createdTime || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('thinglinks.common.updatedTime')">
          {{ detailData.updatedTime || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('iot.rule.groovy.ruleGroovyScript.createdBy')">
          {{ echoMapText(detailData, 'createdBy') }}
        </a-descriptions-item>
      </a-descriptions>
    </Card>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, computed, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { Card, Row, Col, Tag, Descriptions, Select, Modal } from 'ant-design-vue';
  import {
    CodeOutlined,
    AppstoreOutlined,
    ApartmentOutlined,
    ClockCircleOutlined,
    TagsOutlined,
    NodeIndexOutlined,
    BranchesOutlined,
    FileDoneOutlined,
    CopyOutlined,
    EditOutlined,
    BarChartOutlined,
    FormOutlined,
    InfoCircleOutlined,
    QuestionCircleOutlined,
  } from '@ant-design/icons-vue';
  import { detail, update, execStat } from '/@/api/iot/rule/groovy/ruleGroovyScript';
  import { listByProduct } from '/@/api/iot/link/productVersion/productVersion';
  import { query as queryProducts } from '/@/api/iot/link/product/product';
  import { PageWrapper } from '/@/components/Page';
  import { Codemirror } from 'vue-codemirror';
  import { java } from '@codemirror/lang-java';
  import { oneDark } from '@codemirror/theme-one-dark';
  import { useDict } from '/@/components/Dict';
  import { useModal } from '/@/components/Modal';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { echoMapText } from '/@/utils/echo';
  import { ScriptDebugPanel } from '/@/components/iot/ScriptDebugPanel';
  import EditModal from './Edit.vue';

  /** ScriptDebugPanel 对外暴露的方法/状态,供顶部"执行"按钮联动。 */
  interface ScriptDebugPanelExpose {
    execute: () => void;
    running: boolean;
    lastStatus: 'SUCCESS' | 'FAILED' | '';
  }

  export default defineComponent({
    name: 'RuleGroovyScriptDetail',
    components: {
      Card,
      Row,
      Col,
      Tag,
      [Descriptions.name as string]: Descriptions,
      [Descriptions.Item.name as string]: Descriptions.Item,
      [Select.name as string]: Select,
      [Select.Option.name as string]: Select.Option,
      PageWrapper,
      Codemirror,
      ScriptDebugPanel,
      CodeOutlined,
      AppstoreOutlined,
      ApartmentOutlined,
      ClockCircleOutlined,
      TagsOutlined,
      NodeIndexOutlined,
      BranchesOutlined,
      FileDoneOutlined,
      CopyOutlined,
      EditOutlined,
      BarChartOutlined,
      FormOutlined,
      InfoCircleOutlined,
      QuestionCircleOutlined,
      EditModal,
    },
    setup() {
      const { t } = useI18n();
      const { currentRoute } = useRouter();
      const { createMessage } = useMessage();
      const { getDictLabel } = useDict();
      const [registerEditModal, { openModal: openEditModal }] = useModal();

      const id = ref('');
      const detailData = reactive<any>({});
      /** 脚本累计执行统计(总次数 / 成功 / 失败) */
      const execStatData = reactive({ total: 0, success: 0, fail: 0 });
      /** 产品可选版本(下拉)+ 产品当前生效版本 ── 详情页可切换脚本绑定版本(调试 / 绑定下个版本) */
      const versionOptions = ref<
        Array<{
          value: string;
          label: string;
          status?: number;
          statusText?: string;
          isActive: boolean;
        }>
      >([]);
      const versionLoading = ref(false);
      const productActiveVersion = ref('');
      /** 脚本绑定版本 vs 产品当前生效版本:true 生效中 / false 非当前 / null 未知 */
      const versionMatch = computed<boolean | null>(() => {
        if (!productActiveVersion.value || !detailData.objectVersion) return null;
        return String(detailData.objectVersion) === String(productActiveVersion.value);
      });
      /** 脚本唯一键(scriptType:channelCode:productIdentification:topicPattern)── 透传调试面板,使调试也计入统计 */
      const scriptUniqueKey = computed(() => {
        const { scriptType, channelCode, productIdentification, topicPattern } = detailData;
        if (!scriptType || !channelCode || !productIdentification || !topicPattern) return '';
        return [scriptType, channelCode, productIdentification, topicPattern].join(':');
      });
      const language = ref('Groovy');
      const scriptExtensions = [java(), oneDark];
      /** 调试面板实例引用,顶部"执行"按钮借此触发面板内试跑。 */
      const debugPanelRef = ref<ScriptDebugPanelExpose | null>(null);

      onMounted(() => {
        const { params } = currentRoute.value;
        id.value = (params.id as string) ?? '';
        load();
      });

      async function load() {
        if (!id.value) return;
        const res = await detail(id.value);
        Object.assign(detailData, res);
        // 拉产品可选版本 + 当前生效版本(失败不阻断详情)
        loadVersionOptions(detailData.productIdentification);
        // 执行统计:读取失败不影响详情展示
        try {
          const stat = await execStat(id.value);
          if (stat) Object.assign(execStatData, stat);
        } catch (e) {
          /* ignore stat error */
        }
      }

      /** 版本状态码 → 文案(与 Edit 页一致:1 已发布 / 2 灰度 / 3 影子) */
      function statusToText(status?: number): string {
        if (status === 1) return t('iot.rule.groovy.ruleGroovyScript.topicInbound.statusPublished');
        if (status === 2) return t('iot.rule.groovy.ruleGroovyScript.topicInbound.statusCanary');
        if (status === 3) return t('iot.rule.groovy.ruleGroovyScript.topicInbound.statusShadow');
        return '';
      }

      /** 版本状态文案 → 标签颜色 */
      function versionStatusColor(statusText: string): string {
        const tip = (k: string) => t(`iot.rule.groovy.ruleGroovyScript.topicInbound.${k}`);
        if (statusText === tip('statusPublished')) return 'green';
        if (statusText === tip('statusCanary')) return 'orange';
        if (statusText === tip('statusShadow')) return 'purple';
        return 'default';
      }

      /**
       * 拉取产品已发布版本(状态 1/2/3 可选)+ 产品当前生效版本号。
       * 供"产品版本"卡下拉切换脚本绑定版本 + 标注是否为当前生效版本。
       */
      async function loadVersionOptions(productIdentification?: string) {
        if (!productIdentification) {
          versionOptions.value = [];
          productActiveVersion.value = '';
          return;
        }
        versionLoading.value = true;
        try {
          const [list, productList] = await Promise.all([
            listByProduct(productIdentification),
            queryProducts({ productIdentification } as any),
          ]);
          const product = Array.isArray(productList) ? productList[0] : productList;
          productActiveVersion.value = product?.activeVersionNo ?? '';
          const selectable = new Set([1, 2, 3]);
          versionOptions.value = (list || [])
            .filter((v: any) => v.versionNo && selectable.has(v.versionStatus ?? -1))
            .map((v: any) => ({
              value: v.versionNo,
              label: v.versionNo,
              status: v.versionStatus,
              statusText: statusToText(v.versionStatus),
              isActive: v.versionNo === productActiveVersion.value,
            }));
          // 兜底:脚本当前绑定版本若不在可选列表(如已归档),补进去,保证下拉正确回显 / 可切回
          const cur = detailData.objectVersion;
          if (cur && !versionOptions.value.some((o) => o.value === cur)) {
            versionOptions.value.unshift({
              value: cur,
              label: cur,
              status: undefined,
              statusText: '',
              isActive: cur === productActiveVersion.value,
            });
          }
        } catch (e) {
          versionOptions.value = [];
          productActiveVersion.value = '';
        } finally {
          versionLoading.value = false;
        }
      }

      /** 保存(即生效):二次确认 → update 持久化 → 重载刷新版本生效标识 */
      function handleSave() {
        Modal.confirm({
          title: t('iot.rule.groovy.ruleGroovyScript.details.saveConfirmTitle'),
          content: t('iot.rule.groovy.ruleGroovyScript.details.saveConfirmContent', {
            version: detailData.objectVersion || '-',
          }),
          onOk: async () => {
            await update({ ...detailData });
            createMessage.success(t('iot.rule.groovy.ruleGroovyScript.details.saveSuccess'));
            await load();
          },
        });
      }

      /** 扩展参数(extend_params)JSON 格式化 */
      function handleFormatExtendParams() {
        const val = (detailData.extendParams ?? '').trim();
        if (!val) return;
        try {
          detailData.extendParams = JSON.stringify(JSON.parse(val), null, 2);
        } catch {
          createMessage.warning(t('iot.rule.groovy.ruleGroovyScript.details.inputInvalid'));
        }
      }

      function handleCopyScript() {
        const ok = copyTextToClipboard(detailData.scriptContent ?? '');
        if (ok) createMessage.success(t('iot.rule.groovy.ruleGroovyScript.details.copySuccess'));
      }

      /** 调起编辑 Modal（含唯一索引字段以外所有字段）*/
      function handleEdit() {
        openEditModal(true, { record: detailData, type: ActionEnum.EDIT });
      }

      return {
        t,
        getDictLabel,
        echoMapText,
        id,
        detailData,
        execStatData,
        scriptUniqueKey,
        language,
        scriptExtensions,
        debugPanelRef,
        handleSave,
        handleCopyScript,
        handleEdit,
        handleFormatExtendParams,
        registerEditModal,
        load,
        versionOptions,
        versionLoading,
        productActiveVersion,
        versionMatch,
        versionStatusColor,
      };
    },
  });
</script>
<style lang="less" scoped>
  /* ===== Flexy 风格通用 ===== */
  .groovy-detail {
    background: #f6f8fb;
  }

  .header-card,
  .panel-card,
  .meta-card,
  .metric-card {
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
  }

  .header-card {
    margin: 16px 16px 0;
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
  }

  .script-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 28px;
    box-shadow: 0 4px 12px rgba(93, 135, 255, 0.25);
  }

  .script-meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .script-title {
    display: flex;
    align-items: center;
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

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }

  /* ===== 指标行 ===== */
  .metric-row {
    margin: 16px;
  }

  .metric-card {
    height: 100%;
    display: flex;
    align-items: center;

    :deep(.ant-card-body) {
      padding: 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      width: 100%;
    }
  }

  .metric-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;

    &.biz {
      background: rgba(73, 190, 255, 0.1);
      color: #49beff;
    }

    &.id {
      background: rgba(19, 222, 185, 0.1);
      color: #13deb9;
    }

    &.ver {
      background: rgba(255, 174, 31, 0.1);
      color: #ffae1f;
    }

    &.stat {
      background: rgba(99, 102, 241, 0.1);
      color: #6366f1;
    }
  }

  .metric-body {
    flex: 1;
    min-width: 0;
  }

  .metric-label {
    color: #6b7280;
    font-size: 13px;
  }

  .metric-value {
    color: #2a3547;
    font-size: 15px;
    font-weight: 600;
    margin-top: 2px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }

  .metric-stat-sub {
    margin-top: 2px;
    font-size: 12px;

    .ok {
      color: #13deb9;
      margin-right: 10px;
    }

    .fail {
      color: #ff4d6d;
    }
  }

  /* ===== 产品版本卡:可切换下拉 + 生效标识 ===== */
  .ver-select {
    width: 100%;
    margin-top: 2px;
  }

  .ver-no {
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 13px;
  }

  .ver-sub {
    margin-top: 4px;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;
  }

  .ver-active-text {
    color: #6b7280;
  }

  .ver-help {
    margin-left: 4px;
    color: #9aa4b2;
    cursor: help;
  }

  .ver-tag {
    margin: 0;
    font-size: 11px;
    line-height: 1.5;
  }

  /* ===== 主体（脚本 / 测试） ===== */
  .main-row {
    margin: 0 16px 16px;
  }

  .panel-card {
    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f2f5;
      padding: 0 20px;
      min-height: 52px;
    }

    :deep(.ant-card-body) {
      padding: 16px 20px;
    }
  }

  .panel-title {
    font-size: 15px;
    font-weight: 600;
    color: #2a3547;
  }

  .lang-label {
    color: #6b7280;
    font-size: 13px;
  }

  /* ===== 扩展参数卡(脚本内容下方) ===== */
  .config-card {
    margin-top: 16px;
  }

  .config-hint {
    margin-top: 6px;
    font-size: 12px;
    color: #8c8c8c;
    display: flex;
    align-items: center;
    gap: 4px;

    :deep(svg) {
      color: #faad14;
    }
  }

  /* ===== 元数据 ===== */
  .meta-card {
    margin: 0 16px 16px;
  }
</style>
