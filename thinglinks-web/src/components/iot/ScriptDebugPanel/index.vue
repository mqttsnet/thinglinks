<template>
  <Card :bordered="false" class="panel-card script-debug-panel">
    <template #title>
      <span class="panel-title">{{
        t('iot.rule.groovy.ruleGroovyScript.details.runningResult')
      }}</span>
    </template>
    <template #extra>
      <Tag v-if="lastStatus === 'SUCCESS'" color="success">
        <template #icon><CheckCircleOutlined /></template>
        {{ t('iot.rule.groovy.ruleGroovyScript.details.statusSuccess') }}
      </Tag>
      <Tag v-else-if="lastStatus === 'FAILED'" color="error">
        <template #icon><CloseCircleOutlined /></template>
        {{ t('iot.rule.groovy.ruleGroovyScript.details.statusFailed') }}
      </Tag>
      <Tag v-else color="default">{{
        t('iot.rule.groovy.ruleGroovyScript.details.statusUnknown')
      }}</Tag>
    </template>
    <a-tabs v-model:activeKey="activeTab" size="small">
      <!-- ============================================================
           输入参数 Tab —— 引导式:选设备 + 源 topic + 源原始报文
           ============================================================ -->
      <a-tab-pane key="input" :tab="t('iot.rule.groovy.ruleGroovyScript.details.tabs.input')">
        <a-form layout="vertical" class="debug-form">
          <a-form-item>
            <template #label>
              <span>{{ t('iot.rule.groovy.ruleGroovyScript.details.debug.deviceLabel') }}</span>
              <a-tooltip :title="t('iot.rule.groovy.ruleGroovyScript.details.debug.deviceTip')">
                <QuestionCircleOutlined class="label-help" />
              </a-tooltip>
            </template>
            <IotDevicePicker
              v-model="debugDeviceIdentification"
              :productIdentification="productIdentification || ''"
              :title="t('iot.rule.groovy.ruleGroovyScript.details.debug.devicePlaceholder')"
            />
          </a-form-item>
          <a-form-item
            :label="t('iot.rule.groovy.ruleGroovyScript.details.debug.originTopicLabel')"
          >
            <a-input
              v-model:value="originTopic"
              allow-clear
              :placeholder="
                t('iot.rule.groovy.ruleGroovyScript.details.debug.originTopicPlaceholder')
              "
            >
              <template #addonAfter>
                <a-tooltip
                  :title="t('iot.rule.groovy.ruleGroovyScript.details.debug.useTopicPattern')"
                >
                  <a class="topic-fill" @click="useTopicPattern">
                    <FormOutlined />
                  </a>
                </a-tooltip>
              </template>
            </a-input>
          </a-form-item>
          <a-form-item>
            <template #label>
              <div class="origin-body-label">
                <span>{{
                  t('iot.rule.groovy.ruleGroovyScript.details.debug.originBodyLabel')
                }}</span>
                <a-space>
                  <a-button size="small" type="link" @click="handleFormatJson">
                    <template #icon><FormOutlined /></template>
                    {{ t('iot.rule.groovy.ruleGroovyScript.details.formatJson') }}
                  </a-button>
                  <a-button size="small" type="link" @click="handleClearInput">
                    <template #icon><DeleteOutlined /></template>
                    {{ t('iot.rule.groovy.ruleGroovyScript.details.clearInput') }}
                  </a-button>
                </a-space>
              </div>
            </template>
            <codemirror
              v-model="originBody"
              :autofocus="false"
              :extensions="scriptExtensions"
              :indent-with-tab="true"
              :style="{ height: '300px' }"
              :tab-size="2"
              :placeholder="
                t('iot.rule.groovy.ruleGroovyScript.details.debug.originBodyPlaceholder')
              "
            />
          </a-form-item>
          <a-button
            type="primary"
            block
            :loading="running"
            @click="handleExecute"
            v-hasAnyPermission="['rule:groovy:ruleGroovyScript:mockDebug']"
          >
            <template #icon><CaretRightOutlined /></template>
            {{ t('iot.rule.groovy.ruleGroovyScript.details.debug.run') }}
          </a-button>
        </a-form>
      </a-tab-pane>
      <!-- ============================================================
         输出结果 Tab
         ============================================================ -->
      <a-tab-pane key="output" :tab="t('iot.rule.groovy.ruleGroovyScript.details.tabs.output')">
        <Empty
          v-if="!lastStatus"
          :description="t('iot.rule.groovy.ruleGroovyScript.details.emptyResult')"
          style="padding: 80px 0"
        />
        <template v-else>
          <div class="result-stats">
            <span>
              <ClockCircleOutlined />
              {{ t('iot.rule.groovy.ruleGroovyScript.details.latency') }}：
              <strong>{{ compileData.latencyMs ?? '-' }}</strong>
              {{ t('iot.rule.groovy.ruleGroovyScript.details.latencyMs') }}
            </span>
            <a-space>
              <a-button size="small" @click="handleCopyResult">
                <template #icon><CopyOutlined /></template>
                {{ t('iot.rule.groovy.ruleGroovyScript.details.copyResult') }}
              </a-button>
              <a-button size="small" @click="handleDownloadResult">
                <template #icon><DownloadOutlined /></template>
                {{ t('iot.rule.groovy.ruleGroovyScript.details.downloadResult') }}
              </a-button>
            </a-space>
          </div>
          <div class="result-block">
            <div class="result-label">{{
              t('iot.rule.groovy.ruleGroovyScript.details.result')
            }}</div>
            <codemirror
              v-model="contextStr"
              :extensions="scriptExtensions"
              :indent-with-tab="true"
              :style="{ height: '420px' }"
              :tab-size="2"
              :disabled="true"
            />
          </div>
          <div v-if="logStr" class="result-block">
            <div class="result-label">{{
              t('iot.rule.groovy.ruleGroovyScript.details.scriptLogs')
            }}</div>
            <codemirror
              v-model="logStr"
              :extensions="scriptExtensions"
              :indent-with-tab="true"
              :style="{ height: '160px' }"
              :tab-size="2"
              :disabled="true"
            />
          </div>
          <div v-if="exceptionStr" class="result-block">
            <div class="result-label error">{{
              t('iot.rule.groovy.ruleGroovyScript.details.abnormalInfo')
            }}</div>
            <codemirror
              v-model="exceptionStr"
              :extensions="scriptExtensions"
              :indent-with-tab="true"
              :style="{ height: '160px' }"
              :tab-size="2"
              :disabled="true"
            />
          </div>
          <div v-if="errorMessageStr" class="result-block">
            <div class="result-label error">{{
              t('iot.rule.groovy.ruleGroovyScript.details.customExceptionInfo')
            }}</div>
            <codemirror
              v-model="errorMessageStr"
              :extensions="scriptExtensions"
              :indent-with-tab="true"
              :style="{ height: '120px' }"
              :tab-size="2"
              :disabled="true"
            />
          </div>
        </template>
      </a-tab-pane>
      <!-- ============================================================
         变量检视 Tab —— 展示实际注入脚本的 binding 快照
         ============================================================ -->
      <a-tab-pane
        key="variables"
        :tab="t('iot.rule.groovy.ruleGroovyScript.details.tabs.variables')"
      >
        <Empty
          v-if="!binding"
          :description="t('iot.rule.groovy.ruleGroovyScript.details.debug.emptyBinding')"
          style="padding: 80px 0"
        />
        <template v-else>
          <div class="tab-toolbar">
            <span class="hint">{{
              t('iot.rule.groovy.ruleGroovyScript.details.debug.inspectorTip')
            }}</span>
          </div>
          <!-- 设备 / 产品未命中提示 -->
          <a-alert
            v-if="binding && deviceResolved === false"
            type="warning"
            show-icon
            banner
            class="resolve-alert"
            :message="t('iot.rule.groovy.ruleGroovyScript.details.debug.deviceNotResolved')"
          />
          <a-alert
            v-if="binding && productResolved === false"
            type="warning"
            show-icon
            banner
            class="resolve-alert"
            :message="t('iot.rule.groovy.ruleGroovyScript.details.debug.productNotResolved')"
          />

          <!-- 请求上下文 -->
          <div class="inspector-section">
            <div class="inspector-section-title">
              {{ t('iot.rule.groovy.ruleGroovyScript.details.debug.sectionRequest') }}
            </div>
            <BindingKvTable :entries="requestEntries" />
          </div>

          <!-- 设备变量 device.* -->
          <div class="inspector-section">
            <div class="inspector-section-title">
              {{ t('iot.rule.groovy.ruleGroovyScript.details.debug.sectionDevice') }}
            </div>
            <BindingKvTable :entries="deviceEntries" prefix="device." />
          </div>

          <!-- 产品变量 product.* -->
          <div class="inspector-section">
            <div class="inspector-section-title">
              {{ t('iot.rule.groovy.ruleGroovyScript.details.debug.sectionProduct') }}
            </div>
            <BindingKvTable :entries="productEntries" prefix="product." />
          </div>

          <!-- 配置变量 config.*(脚本 extend_params) -->
          <div class="inspector-section">
            <div class="inspector-section-title">
              {{ t('iot.rule.groovy.ruleGroovyScript.details.debug.sectionConfig') }}
            </div>
            <BindingKvTable :entries="configEntries" prefix="config." />
          </div>

          <!-- 物模型 productModel.*(按版本解析的服务 / 属性定义) -->
          <div class="inspector-section">
            <div class="inspector-section-title">
              {{ t('iot.rule.groovy.ruleGroovyScript.details.debug.sectionProductModel') }}
            </div>
            <BindingKvTable :entries="productModelEntries" prefix="productModel." />
          </div>
        </template>
      </a-tab-pane>
      <!-- ============================================================
         历史记录 Tab
         ============================================================ -->
      <a-tab-pane key="history" :tab="t('iot.rule.groovy.ruleGroovyScript.details.tabs.history')">
        <div class="tab-toolbar">
          <span class="hint">
            {{ t('iot.rule.groovy.ruleGroovyScript.details.historyTitle') }}
          </span>
          <a-button size="small" @click="handleClearHistory" :disabled="!history.length">
            <template #icon><DeleteOutlined /></template>
            {{ t('iot.rule.groovy.ruleGroovyScript.details.historyClear') }}
          </a-button>
        </div>
        <Empty
          v-if="!history.length"
          :description="t('iot.rule.groovy.ruleGroovyScript.details.historyEmpty')"
          style="padding: 60px 0"
        />
        <a-list v-else class="history-list" :dataSource="history" size="small">
          <template #renderItem="{ item, index }">
            <a-list-item>
              <div class="history-item">
                <div class="history-meta">
                  <Tag :color="item.status === 'SUCCESS' ? 'success' : 'error'">
                    {{ item.status }}
                  </Tag>
                  <span class="history-ts">{{ item.ts }}</span>
                  <span class="history-latency">{{ item.latencyMs }} ms</span>
                </div>
                <div class="history-fields">
                  <div class="hf-row">
                    <span class="hf-key">{{
                      t('iot.rule.groovy.ruleGroovyScript.details.debug.deviceLabel')
                    }}</span>
                    <span class="hf-val mono">{{ item.deviceIdentification }}</span>
                  </div>
                  <div v-if="item.originTopic" class="hf-row">
                    <span class="hf-key">{{
                      t('iot.rule.groovy.ruleGroovyScript.details.debug.originTopicLabel')
                    }}</span>
                    <span class="hf-val mono">{{ item.originTopic }}</span>
                  </div>
                  <div class="hf-row hf-row--body">
                    <span class="hf-key">{{
                      t('iot.rule.groovy.ruleGroovyScript.details.debug.originBodyLabel')
                    }}</span>
                    <pre class="hf-body">{{ item.originBody }}</pre>
                  </div>
                </div>
                <a-space>
                  <a-button type="link" size="small" @click="applyHistory(item)">
                    {{ t('iot.rule.groovy.ruleGroovyScript.details.historyApply') }}
                  </a-button>
                  <a-button type="link" size="small" @click="removeHistory(index)" danger>
                    <DeleteOutlined />
                  </a-button>
                </a-space>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </a-tab-pane>
    </a-tabs>
  </Card>
</template>

<script lang="ts" setup>
  /**
   * ScriptDebugPanel ── 规则脚本在线调试面板(IoT 应用内通用组件)。
   *
   * <p>把"设备上行前置转换脚本"的在线调试 UI 整体封装为自包含组件:三 Tab 输入/输出/变量 +
   * 历史记录,内部自管状态、自调 {@link debugTransform} API。调用方只需通过 props 传入待调试
   * 的脚本上下文(脚本内容 / 产品标识 / 主题模式),组件即可独立运行,无需感知任何调试细节,
   * 便于在脚本详情、规则联动等多处复用。
   *
   * <p>权限点 {@code rule:groovy:ruleGroovyScript:mockDebug} 控制"运行"按钮可见性。
   *
   * @author mqttsnet
   */
  import { ref, computed, watch } from 'vue';
  import { Card, Tag, Empty } from 'ant-design-vue';
  import {
    ClockCircleOutlined,
    CaretRightOutlined,
    CopyOutlined,
    FormOutlined,
    DeleteOutlined,
    DownloadOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    QuestionCircleOutlined,
  } from '@ant-design/icons-vue';
  import { Codemirror } from 'vue-codemirror';
  import { java } from '@codemirror/lang-java';
  import { oneDark } from '@codemirror/theme-one-dark';
  import { EditorView } from 'codemirror';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { dateUtil } from '/@/utils/dateUtil';
  import { IotDevicePicker } from '/@/components/iot/IotProductDevicePicker';
  import {
    debugTransform,
    type TransformDebugBinding,
  } from '/@/api/iot/rule/groovy/transformDebug';
  import BindingKvTable from './BindingKvTable.vue';

  defineOptions({ name: 'ScriptDebugPanel' });

  /** localStorage 历史记录键前缀（按脚本上下文隔离） */
  const HISTORY_STORAGE_PREFIX = 'thinglinks.rule.groovy.history.';
  /** 历史记录最多保留条数 */
  const HISTORY_LIMIT = 10;

  /**
   * 历史记录条目
   */
  interface HistoryEntry {
    ts: string;
    status: 'SUCCESS' | 'FAILED';
    latencyMs: number;
    deviceIdentification: string;
    originTopic: string;
    originBody: string;
  }

  /**
   * 变量检视器 key-value 行
   */
  interface KvEntry {
    key: string;
    display: string;
  }

  const props = withDefaults(
    defineProps<{
      /** 待调试的脚本内容(必填,直接送给后端试跑)。 */
      scriptContent?: string;
      /** 关联产品标识 ── 透传给设备选择器,限定可选调试设备范围。 */
      productIdentification?: string;
      /** 脚本绑定的主题模式 ── 作为"源 topic"默认值 / 一键回填来源。 */
      topicPattern?: string;
      /** 历史记录隔离键(通常用脚本 ID);不传则退化为全局共享一份。 */
      historyKey?: string;
      /** 导出结果文件名前缀(通常用脚本名);为空则回退 groovy-result。 */
      resultName?: string;
      /** 脚本扩展参数 JSON ── 注入脚本 config 绑定,随调试一并送后端。 */
      extendParams?: string;
      /** 脚本唯一键 ── 透传后端,使调试运行也计入该脚本的执行统计(详情页拼好传入)。 */
      scriptUniqueKey?: string;
      /** 调试使用的产品版本号 ── 透传后端解析物模型注入脚本(可调当前 / 下个版本)。 */
      objectVersion?: string;
    }>(),
    {
      scriptContent: '',
      productIdentification: '',
      topicPattern: '',
      historyKey: '',
      resultName: '',
      extendParams: '',
      scriptUniqueKey: '',
      objectVersion: '',
    },
  );

  const { t } = useI18n();
  const { createMessage } = useMessage();

  // EditorView.lineWrapping:输出结果 / 执行日志等长行自动换行(不再横向滚动)
  const scriptExtensions = [java(), oneDark, EditorView.lineWrapping];
  // 引导式调试输入
  const debugDeviceIdentification = ref<string>('');
  const originTopic = ref<string>('');
  const originBody = ref<string>('');
  const compileData = ref<any>({});
  const running = ref(false);
  const activeTab = ref<'input' | 'output' | 'variables' | 'history'>('input');
  const history = ref<HistoryEntry[]>([]);
  // 变量检视器:实际注入脚本的绑定快照
  const binding = ref<TransformDebugBinding | null>(null);
  const deviceResolved = ref<boolean | null>(null);
  const productResolved = ref<boolean | null>(null);

  const lastStatus = computed<'SUCCESS' | 'FAILED' | ''>(() => {
    if (!compileData.value || !compileData.value.executionStatus) return '';
    return compileData.value.executionStatus === 'SUCCESS' ? 'SUCCESS' : 'FAILED';
  });
  const contextStr = computed(() => stringifyAny(compileData.value?.context));
  const exceptionStr = computed(() => stringifyAny(compileData.value?.exception));
  const errorMessageStr = computed(() => stringifyAny(compileData.value?.errorMessage));
  /** 脚本执行日志(逐行 join);空则不展示 */
  const logStr = computed(() => {
    const arr = compileData.value?.logs;
    return Array.isArray(arr) ? arr.join('\n') : '';
  });

  // ============================== 变量检视器:binding → key-value entries ==============================
  /** 请求上下文(顶部固定字段) */
  const requestEntries = computed<KvEntry[]>(() => {
    const b = binding.value;
    if (!b) return [];
    return [
      { key: 'originTopic', display: stringifyAny(b.originTopic) },
      { key: 'originBody', display: stringifyAny(b.originBody) },
      { key: 'deviceIdentification', display: stringifyAny(b.deviceIdentification) },
      { key: 'productIdentification', display: stringifyAny(b.productIdentification) },
      { key: 'clientId', display: stringifyAny(b.clientId) },
    ];
  });

  /** device.* 字段(数据驱动,后端加字段无需改 UI) */
  const deviceEntries = computed<KvEntry[]>(() => objectToEntries(binding.value?.device));
  /** product.* 字段(数据驱动) */
  const productEntries = computed<KvEntry[]>(() => objectToEntries(binding.value?.product));
  /** config.* 字段(脚本 extend_params 解析,数据驱动) */
  const configEntries = computed<KvEntry[]>(() => objectToEntries(binding.value?.config));
  /** productModel.* 字段(按版本解析的物模型,数据驱动) */
  const productModelEntries = computed<KvEntry[]>(() =>
    objectToEntries(binding.value?.productModel),
  );

  // 历史记录按 historyKey 隔离;调用方常异步填入(脚本详情拉取后才拿到 id),故 watch 同步重载。
  watch(
    () => props.historyKey,
    () => loadHistory(),
    { immediate: true },
  );

  // 源 topic 默认填脚本绑定的主题模式,方便直接试跑;仅当用户尚未输入时回填,避免覆盖手填值。
  // topicPattern 同样可能异步到达,immediate watch 兼顾构造期与后续更新。
  watch(
    () => props.topicPattern,
    (pattern) => {
      if (pattern && !originTopic.value) {
        originTopic.value = pattern;
      }
    },
    { immediate: true },
  );

  /** 一键把源 topic 填成脚本绑定的主题模式 */
  function useTopicPattern() {
    if (props.topicPattern) {
      originTopic.value = props.topicPattern;
    }
  }

  function handleFormatJson() {
    if (!originBody.value || !originBody.value.trim()) return;
    try {
      const obj = JSON.parse(originBody.value);
      originBody.value = JSON.stringify(obj, null, 2);
    } catch {
      createMessage.warning(t('iot.rule.groovy.ruleGroovyScript.details.inputInvalid'));
    }
  }

  function handleClearInput() {
    originBody.value = '';
  }

  async function handleExecute() {
    const deviceId = unwrapValue(debugDeviceIdentification.value);
    if (!deviceId) {
      createMessage.warning(t('iot.rule.groovy.ruleGroovyScript.details.debug.deviceRequired'));
      activeTab.value = 'input';
      return;
    }
    if (!originBody.value || !originBody.value.trim()) {
      createMessage.warning(t('iot.rule.groovy.ruleGroovyScript.details.debug.originBodyRequired'));
      activeTab.value = 'input';
      return;
    }
    running.value = true;
    const start = performance.now();
    try {
      const res = await debugTransform({
        scriptContent: props.scriptContent ?? '',
        deviceIdentification: deviceId,
        originTopic: originTopic.value ?? '',
        originBody: originBody.value,
        extendParams: props.extendParams ?? '',
        scriptUniqueKey: props.scriptUniqueKey ?? '',
        objectVersion: props.objectVersion ?? '',
      });
      const latency = Math.round(performance.now() - start);
      const result = res?.result ?? {};
      compileData.value = { ...result, latencyMs: latency };
      binding.value = res?.binding ?? null;
      deviceResolved.value = res?.deviceResolved ?? null;
      productResolved.value = res?.productResolved ?? null;
      activeTab.value = 'output';
      pushHistory({
        ts: dateUtil().format('YYYY-MM-DD HH:mm:ss'),
        status: result?.executionStatus === 'SUCCESS' ? 'SUCCESS' : 'FAILED',
        latencyMs: latency,
        deviceIdentification: deviceId,
        originTopic: originTopic.value ?? '',
        originBody: originBody.value ?? '',
      });
      if (result?.executionStatus === 'SUCCESS') {
        createMessage.success(t('iot.rule.groovy.ruleGroovyScript.details.runSuccessTip'));
      } else {
        createMessage.error(t('iot.rule.groovy.ruleGroovyScript.details.runFailedTip'));
      }
    } catch (e) {
      const latency = Math.round(performance.now() - start);
      compileData.value = {
        executionStatus: 'FAILED',
        errorMessage: (e as any)?.message ?? String(e),
        latencyMs: latency,
      };
      binding.value = null;
      deviceResolved.value = null;
      productResolved.value = null;
      activeTab.value = 'output';
      pushHistory({
        ts: dateUtil().format('YYYY-MM-DD HH:mm:ss'),
        status: 'FAILED',
        latencyMs: latency,
        deviceIdentification: deviceId,
        originTopic: originTopic.value ?? '',
        originBody: originBody.value ?? '',
      });
    } finally {
      running.value = false;
    }
  }

  function handleCopyResult() {
    const ok = copyTextToClipboard(contextStr.value ?? '');
    if (ok) createMessage.success(t('iot.rule.groovy.ruleGroovyScript.details.copySuccess'));
  }

  function handleDownloadResult() {
    const blob = new Blob([contextStr.value ?? ''], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${props.resultName || 'groovy-result'}-${Date.now()}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }

  // ============================== 历史记录 ==============================
  function storageKey(): string {
    return HISTORY_STORAGE_PREFIX + (props.historyKey || 'na');
  }

  function loadHistory() {
    try {
      const raw = localStorage.getItem(storageKey());
      history.value = raw ? (JSON.parse(raw) as HistoryEntry[]) : [];
    } catch {
      history.value = [];
    }
  }

  function persistHistory() {
    try {
      localStorage.setItem(storageKey(), JSON.stringify(history.value));
    } catch {
      // 写失败 silent ── 不影响功能
    }
  }

  function pushHistory(entry: HistoryEntry) {
    history.value.unshift(entry);
    if (history.value.length > HISTORY_LIMIT) {
      history.value = history.value.slice(0, HISTORY_LIMIT);
    }
    persistHistory();
  }

  function applyHistory(entry: HistoryEntry) {
    // 回填历史:设备 / 源 topic / 源报文 精确还原(三段独立存,无截断)
    debugDeviceIdentification.value = entry.deviceIdentification ?? '';
    originTopic.value = entry.originTopic ?? '';
    originBody.value = entry.originBody ?? '';
    activeTab.value = 'input';
    createMessage.success(t('iot.rule.groovy.ruleGroovyScript.details.historyApplied'));
  }

  function removeHistory(index: number) {
    history.value.splice(index, 1);
    persistHistory();
  }

  function handleClearHistory() {
    history.value = [];
    persistHistory();
  }

  // ============================== 工具 ==============================
  /** 尝试把字符串解析成 JSON(仅当以 { 或 [ 开头);失败返回 undefined。 */
  function tryParseJson(s: string): any {
    const str = s.trim();
    if (!str || (str[0] !== '{' && str[0] !== '[')) return undefined;
    try {
      return JSON.parse(str);
    } catch {
      return undefined;
    }
  }

  /** 递归把"内嵌的 JSON 字符串"还原成对象(如 payload 本身是 JSON 串),便于格式化展示。 */
  function deepParseJson(v: any): any {
    if (typeof v === 'string') {
      const p = tryParseJson(v);
      return p === undefined ? v : deepParseJson(p);
    }
    if (Array.isArray(v)) return v.map(deepParseJson);
    if (v && typeof v === 'object') {
      const o: Record<string, any> = {};
      Object.keys(v).forEach((k) => {
        o[k] = deepParseJson(v[k]);
      });
      return o;
    }
    return v;
  }

  /** 任意值 → 展示字符串:JSON 美化(2 空格)+ 自动展开内嵌 JSON 串;普通字符串原样返回。 */
  function stringifyAny(v: any): string {
    if (v === undefined || v === null) return '';
    if (typeof v === 'string') {
      const parsed = tryParseJson(v);
      if (parsed === undefined) return v;
      try {
        return JSON.stringify(deepParseJson(parsed), null, 2);
      } catch {
        return v;
      }
    }
    try {
      return JSON.stringify(deepParseJson(v), null, 2);
    } catch {
      return String(v);
    }
  }

  /** 对象 → key-value entries(数据驱动渲染,跳过 undefined) */
  function objectToEntries(obj?: Record<string, any> | null): KvEntry[] {
    if (!obj || typeof obj !== 'object') return [];
    return Object.keys(obj).map((key) => ({ key, display: stringifyAny(obj[key]) }));
  }

  /** Picker 可能给 string | string[],统一取单值 */
  function unwrapValue(v: any): string {
    if (Array.isArray(v)) return v[0] ?? '';
    return v ?? '';
  }

  /**
   * 对外暴露:供宿主页面的"顶部执行按钮"等外部触发点联动。
   *
   * <ul>
   *   <li>{@code execute} ── 触发一次试跑(与面板内"运行"按钮同一入口)</li>
   *   <li>{@code running} ── 试跑进行中(给外部按钮挂 loading)</li>
   *   <li>{@code lastStatus} ── 最近一次执行状态(SUCCESS/FAILED/'')</li>
   * </ul>
   */
  defineExpose({
    execute: handleExecute,
    running,
    lastStatus,
  });
</script>

<style lang="less" scoped>
  .panel-card {
    height: 100%;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);

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

  .tab-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .hint {
    color: #6b7280;
    font-size: 12px;
  }

  /* ===== 引导式调试表单 ===== */
  .debug-form {
    :deep(.ant-form-item) {
      margin-bottom: 16px;
    }

    .label-help {
      margin-left: 4px;
      color: #9aa4b2;
      cursor: help;
    }

    .topic-fill {
      display: inline-flex;
      color: @primary-color;
    }

    .origin-body-label {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }
  }

  /* ===== 变量检视器 ===== */
  .resolve-alert {
    margin-bottom: 12px;
    border-radius: 6px;
  }

  .inspector-section {
    margin-bottom: 16px;

    .inspector-section-title {
      font-size: 13px;
      font-weight: 600;
      color: #2a3547;
      margin-bottom: 8px;
    }
  }

  .result-stats {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    strong {
      color: #2a3547;
      margin: 0 4px;
    }
  }

  .result-block {
    margin-bottom: 12px;

    .result-label {
      font-weight: 600;
      color: #2a3547;
      font-size: 13px;
      margin-bottom: 6px;

      &.error {
        color: #fa4b4b;
      }
    }
  }

  /* 历史条数多时不撑长面板:限高 + 下滑查看(上方清空工具条在列表外,保持固定) */
  .history-list {
    max-height: 480px;
    overflow-y: auto;
  }

  .history-item {
    display: flex;
    flex-direction: column;
    gap: 6px;
    width: 100%;

    .history-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      color: #6b7280;
    }

    .history-fields {
      display: flex;
      flex-direction: column;
      gap: 4px;
      width: 100%;
    }

    .hf-row {
      display: flex;
      gap: 8px;
      font-size: 12px;
      line-height: 1.5;

      &--body {
        flex-direction: column;
        gap: 2px;
      }
    }

    .hf-key {
      flex: 0 0 auto;
      min-width: 48px;
      color: #9aa4b2;
    }

    .hf-val {
      color: #2a3547;
      word-break: break-all;

      &.mono {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      }
    }

    .hf-body {
      margin: 0;
      max-height: 160px;
      overflow: auto;
      padding: 6px 8px;
      background: #f6f8fb;
      border-radius: 6px;
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 12px;
      color: #2a3547;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
</style>
