<template>
  <PageWrapper content-background>
    <a-row :gutter="16">
      <!-- 左:命令编排 -->
      <a-col :xs="24" :md="10" :xl="9">
        <a-card :title="T('params')" size="small">
          <div class="mqtt-desc">
            <InfoCircleOutlined />
            <span>{{ T('subtitle') }}</span>
          </div>

          <!-- 寻址:手动填写 / 按设备 -->
          <a-radio-group
            v-model:value="mode"
            :options="modeOptions"
            option-type="button"
            button-style="solid"
            class="mqtt-seg"
          />

          <!-- 按设备:产品 → 设备 → 版本(默认设备绑定版本) -->
          <div v-if="mode === 'device'" class="mqtt-device">
            <IotProductPicker :modelValue="pickProductId" @update:model-value="onPickProduct" />
            <IotDevicePicker
              :modelValue="pickDeviceId"
              :productIdentification="pickProductId"
              @change="onPickDevice"
            />
            <IotProductVersionPicker
              v-model="versionNo"
              :productIdentification="pickProductId"
              allow-custom
              @change="onVersionChange"
            />
            <div class="mqtt-hint">
              <InfoCircleOutlined />
              <span>{{ T('deviceHint') }}</span>
            </div>
          </div>

          <!-- 下发内容:结构化命令 / 原始报文 -->
          <a-radio-group
            v-model:value="contentMode"
            :options="contentOptions"
            option-type="button"
            button-style="solid"
            class="mqtt-seg"
          />

          <!-- 结构化命令(按版本快照构造) -->
          <div v-if="contentMode === 'structured'" class="mqtt-structured">
            <a-alert
              v-if="!canStructured"
              type="warning"
              show-icon
              :message="T('pickDeviceVersionFirst')"
            />
            <a-spin v-else :spinning="snapshotLoading">
              <a-alert
                v-if="!snapshotLoading && services.length === 0"
                type="warning"
                show-icon
                :message="T('snapshotEmpty')"
              />
              <template v-else>
                <div class="mqtt-field">
                  <label class="mqtt-label">{{ T('service') }}</label>
                  <a-select
                    v-model:value="serviceCode"
                    :placeholder="T('servicePh')"
                    style="width: 100%"
                    show-search
                    option-filter-prop="children"
                    @change="onServiceChange"
                  >
                    <a-select-option
                      v-for="s in services"
                      :key="s.serviceCode"
                      :value="s.serviceCode"
                    >
                      {{ s.serviceName || s.serviceCode }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="mqtt-field">
                  <label class="mqtt-label">{{ T('command') }}</label>
                  <a-select
                    v-model:value="cmd"
                    :placeholder="T('commandPh')"
                    style="width: 100%"
                    show-search
                    option-filter-prop="children"
                    @change="onCommandChange"
                  >
                    <a-select-option
                      v-for="c in commands"
                      :key="c.commandCode"
                      :value="c.commandCode"
                    >
                      {{ c.commandName || c.commandCode }}
                    </a-select-option>
                  </a-select>
                </div>
                <div v-if="cmd" class="mqtt-field">
                  <label class="mqtt-label">{{ T('params') }}</label>
                  <div v-if="requestParams.length === 0" class="mqtt-empty-sm">
                    {{ T('paramsEmpty') }}
                  </div>
                  <div v-for="p in requestParams" :key="p.parameterCode" class="mqtt-param">
                    <div class="mqtt-param-label">
                      {{ p.parameterName || p.parameterCode }}
                      <span v-if="p.required === 1" class="mqtt-req">*</span>
                      <span class="mqtt-param-type">{{ p.datatype }}</span>
                      <span v-if="p.unit" class="mqtt-param-unit">{{ p.unit }}</span>
                      <span class="mqtt-param-code" v-if="p.parameterName && p.parameterCode">
                        {{ p.parameterCode }}
                      </span>
                    </div>
                    <div v-if="paramMeta(p).length" class="mqtt-param-meta">
                      <span v-for="m in paramMeta(p)" :key="m" class="mqtt-meta-item">{{ m }}</span>
                    </div>
                    <div v-if="p.parameterDescription" class="mqtt-param-desc">
                      {{ p.parameterDescription }}
                    </div>
                    <a-select
                      v-if="enumOptions(p).length"
                      v-model:value="p._value"
                      allow-clear
                      :placeholder="T('selectPh')"
                      style="width: 100%"
                    >
                      <a-select-option v-for="o in enumOptions(p)" :key="o" :value="o">
                        {{ o }}
                      </a-select-option>
                    </a-select>
                    <a-switch v-else-if="isBool(p)" v-model:checked="p._value" />
                    <a-input-number
                      v-else-if="isNumber(p)"
                      v-model:value="p._value"
                      :min="paramMin(p)"
                      :max="paramMax(p)"
                      :step="paramStep(p)"
                      :placeholder="numPh(p)"
                      style="width: 100%"
                    />
                    <a-textarea
                      v-else-if="isJson(p)"
                      v-model:value="p._value"
                      :rows="3"
                      class="mqtt-mono"
                      placeholder="{ }"
                    />
                    <a-input
                      v-else
                      v-model:value="p._value"
                      :maxlength="paramMaxLen(p)"
                      :placeholder="textPh(p)"
                      allow-clear
                    />
                  </div>
                </div>
              </template>
            </a-spin>
          </div>

          <!-- 公共参数 + 原始报文 -->
          <a-form layout="vertical" class="mqtt-form">
            <a-form-item :label="T('tenantId')">
              <a-input v-model:value="formState.tenantId" allow-clear />
            </a-form-item>
            <a-form-item :label="T('topic')">
              <a-input
                v-model:value="formState.topic"
                :placeholder="T('topicPh')"
                :disabled="contentMode === 'structured'"
                allow-clear
              />
            </a-form-item>
            <template v-if="contentMode === 'raw'">
              <a-row :gutter="8">
                <a-col :span="14">
                  <a-form-item :label="T('qos')">
                    <a-select v-model:value="formState.qos">
                      <a-select-option value="0">{{ T('qos0') }}</a-select-option>
                      <a-select-option value="1">{{ T('qos1') }}</a-select-option>
                      <a-select-option value="2">{{ T('qos2') }}</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :span="10">
                  <a-form-item :label="T('expirySeconds')">
                    <a-input-number
                      v-model:value="formState.expirySeconds"
                      :min="1"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item :label="T('payload')">
                <a-textarea
                  v-model:value="formState.payload"
                  :rows="6"
                  class="mqtt-mono"
                  :placeholder="T('payloadPh')"
                />
                <div class="mqtt-hint">
                  <InfoCircleOutlined />
                  <span>{{ T('payloadHint') }}</span>
                </div>
              </a-form-item>
            </template>

            <div class="mqtt-preview-label">{{ T('preview') }}</div>
            <pre class="mqtt-preview">{{ previewText }}</pre>

            <a-space>
              <a-button type="primary" :loading="sending" @click="handleSend">
                <template #icon><SendOutlined /></template>
                {{ T('send') }}
              </a-button>
              <a-button v-if="contentMode === 'structured'" @click="generateToRaw">
                {{ T('toRaw') }}
              </a-button>
              <a-button @click="resetForm">{{ T('reset') }}</a-button>
            </a-space>
          </a-form>
        </a-card>
      </a-col>

      <!-- 右:下发记录(device_command 真历史,按时间排序展示) -->
      <a-col :xs="24" :md="14" :xl="15">
        <a-card class="mqtt-history-card" :title="T('record')" size="small">
          <template #extra>
            <div class="mqtt-history-extra">
              <div class="mqtt-history-filters">
                <a-input
                  v-model:value="historyDeviceIdentification"
                  allow-clear
                  size="small"
                  class="mqtt-history-input mqtt-history-input-device"
                  :placeholder="T('deviceFilterPh')"
                  @press-enter="loadHistory"
                  @change="onHistoryDeviceChange"
                >
                  <template #prefix>
                    <span class="mqtt-filter-prefix">{{ T('deviceFilterPrefix') }}</span>
                  </template>
                </a-input>
                <a-input
                  v-model:value="historyTopic"
                  allow-clear
                  size="small"
                  class="mqtt-history-input mqtt-history-input-topic"
                  :placeholder="T('topicFilterPh')"
                  @press-enter="loadHistory"
                  @change="onHistoryTopicChange"
                >
                  <template #prefix>
                    <span class="mqtt-filter-prefix">{{ T('topicFilterPrefix') }}</span>
                  </template>
                </a-input>
                <a-button
                  size="small"
                  type="primary"
                  class="mqtt-history-search"
                  :title="T('search')"
                  :aria-label="T('search')"
                  @click="loadHistory"
                >
                  <template #icon><SearchOutlined /></template>
                </a-button>
              </div>
              <a-radio-group
                v-model:value="historyScope"
                size="small"
                button-style="solid"
                @change="loadHistory"
              >
                <a-radio-button v-if="hasHistoryDevice" value="device">
                  {{ T('scopeDevice') }}
                </a-radio-button>
                <a-radio-button value="all">{{ T('scopeAll') }}</a-radio-button>
              </a-radio-group>
              <a-button size="small" :loading="historyLoading" @click="loadHistory">
                <template #icon><ReloadOutlined /></template>
                {{ T('refresh') }}
              </a-button>
            </div>
          </template>
          <div class="mqtt-log">
            <div v-if="historyRows.length === 0" class="mqtt-empty">{{ T('empty') }}</div>
            <div v-for="(item, i) in historyRows" :key="historyKey(item, i)" class="mqtt-record">
              <div class="mqtt-line">
                <ArrowDownOutlined v-if="isResponseRecord(item)" class="mqtt-ic-down" />
                <ArrowUpOutlined v-else class="mqtt-ic-up" />
                <span class="mqtt-cmd">{{ cmdLabel(item) }}</span>
                <a-tag :color="recordTypeColor(item)">{{ recordTypeText(item) }}</a-tag>
                <a-tag :color="statusColor(item)">{{ statusText(item) }}</a-tag>
                <a-tag
                  v-if="hasBusinessResult(item)"
                  :color="businessStatusColor(item)"
                  :title="item.errMsg || ''"
                >
                  {{ businessStatusText(item) }}
                </a-tag>
                <span class="mqtt-time">{{ fmt(item.createdTime) }}</span>
                <EyeOutlined class="mqtt-ic-btn" :title="T('view')" @click="viewRaw(item)" />
                <ReloadOutlined
                  v-if="isIssueRecord(item)"
                  class="mqtt-ic-btn"
                  :title="T('resend')"
                  @click="resend(item)"
                />
              </div>
              <div v-if="item.topic" class="mqtt-topic-row" :title="item.topic">
                {{ item.topic }}
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 报文查看 -->
    <a-modal v-model:visible="viewVisible" :title="T('view')" :footer="null" :width="640">
      <div style="margin-bottom: 8px; text-align: right">
        <a-button size="small" @click="copyView">
          <template #icon><CopyOutlined /></template>
          {{ T('copy') }}
        </a-button>
      </div>
      <pre class="mqtt-view">{{ viewContent }}</pre>
    </a-modal>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
  import {
    SendOutlined,
    InfoCircleOutlined,
    ReloadOutlined,
    ArrowUpOutlined,
    ArrowDownOutlined,
    EyeOutlined,
    CopyOutlined,
    SearchOutlined,
  } from '@ant-design/icons-vue';
  import { PageWrapper } from '/@/components/Page';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import { formatToDateTime } from '/@/utils/dateUtil';
  import { sendMsg } from '/@/api/iot/link/operationMaintenance/mqtt/mqtt';
  import { issueCommands, debugHistory } from '/@/api/iot/link/deviceCommand/deviceCommand';
  import { detail as versionDetail } from '/@/api/iot/link/productVersion/productVersion';
  import { IotProductPicker, IotDevicePicker } from '/@/components/iot/IotProductDevicePicker';
  import IotProductVersionPicker from '/@/components/iot/IotProductVersionPicker/IotProductVersionPicker.vue';

  defineOptions({ name: 'DebugMqtt' });

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const userStore = useUserStore();
  const T = (k: string) => t(`iot.link.operationMaintenance.debug.mqtt.${k}`);

  // 寻址 manual=手动 topic / device=选设备;下发内容 structured=按版本结构化命令 / raw=原始报文
  const mode = ref<'manual' | 'device'>('manual');
  const contentMode = ref<'structured' | 'raw'>('raw');
  const modeOptions = computed(() => [
    { label: T('modeManual'), value: 'manual' },
    { label: T('modeDevice'), value: 'device' },
  ]);
  const canStructured = computed(
    () => mode.value === 'device' && !!pickDeviceId.value && !!versionNo.value,
  );
  const contentOptions = computed(() => [
    { label: T('contentStructured'), value: 'structured', disabled: !canStructured.value },
    { label: T('contentRaw'), value: 'raw' },
  ]);

  // 按设备
  const pickProductId = ref('');
  const pickDeviceId = ref('');
  const versionNo = ref('');

  // 版本快照 → 服务 / 命令 / 参数
  const snapshotLoading = ref(false);
  const services = ref<any[]>([]);
  const serviceCode = ref('');
  const cmd = ref('');
  const commands = computed<any[]>(
    () => services.value.find((s) => s.serviceCode === serviceCode.value)?.commands || [],
  );
  const requestParams = ref<any[]>([]);

  // 原始 / 公共
  const formState = reactive({
    tenantId: userStore.getTenantId || '',
    topic: '',
    qos: '1',
    expirySeconds: 60,
    payload: '',
  });
  const sending = ref(false);

  // 历史
  const records = ref<any[]>([]);
  const historyScope = ref<'device' | 'all'>('all');
  const historyDeviceIdentification = ref('');
  const historyTopic = ref('');
  const historyLoading = ref(false);
  const hasHistoryDevice = computed(() => !!pickDeviceId.value);
  let pollTimer: any = null;

  // 查看
  const viewVisible = ref(false);
  const viewContent = ref('');

  // ───────── 按设备联动 ─────────
  function onPickProduct(v: any): void {
    pickProductId.value = v ?? '';
    pickDeviceId.value = '';
    versionNo.value = '';
    historyScope.value = 'all';
    resetSnapshot();
    loadHistory();
  }
  function onPickDevice(val: any, recordsArr: any[]): void {
    const id = Array.isArray(val) ? val[0] : val;
    pickDeviceId.value = id ?? '';
    if (!id) {
      historyScope.value = 'all';
      loadHistory();
      return;
    }
    formState.topic = `/v1/devices/${id}/command`;
    const rec =
      (recordsArr || []).find((r) => String(r?.deviceIdentification) === String(id)) ||
      (recordsArr || [])[0];
    // 默认选中设备绑定的版本(可再切)
    if (rec?.boundProductVersionNo) versionNo.value = String(rec.boundProductVersionNo);
    historyScope.value = 'device';
    loadSnapshot();
    loadHistory();
  }
  function onVersionChange(v: string): void {
    versionNo.value = v || '';
    loadSnapshot();
  }

  function resetSnapshot(): void {
    services.value = [];
    serviceCode.value = '';
    cmd.value = '';
    requestParams.value = [];
  }

  async function loadSnapshot(): Promise<void> {
    if (!pickProductId.value || !versionNo.value) {
      resetSnapshot();
      return;
    }
    snapshotLoading.value = true;
    try {
      const res: any = await versionDetail(pickProductId.value, versionNo.value);
      let snap: any = {};
      try {
        snap = JSON.parse(res?.productSnapshotJson || '{}');
      } catch {
        snap = {};
      }
      services.value = Array.isArray(snap?.services) ? snap.services : [];
    } catch {
      services.value = [];
    } finally {
      snapshotLoading.value = false;
    }
  }

  function onServiceChange(): void {
    cmd.value = '';
    requestParams.value = [];
  }
  function onCommandChange(): void {
    const c = commands.value.find((x) => x.commandCode === cmd.value);
    const reqs = Array.isArray(c?.requests) ? c.requests : [];
    requestParams.value = reqs.map((p: any) => ({ ...p, _value: isBool(p) ? false : undefined }));
  }

  // ───────── datatype 驱动 ─────────
  function isNumber(p: any): boolean {
    return /^(int|integer|long|float|double|decimal|number)$/i.test(p?.datatype || '');
  }
  function isBool(p: any): boolean {
    return /^(bool|boolean)$/i.test(p?.datatype || '');
  }
  function isJson(p: any): boolean {
    return /^(json|object|array|struct)$/i.test(p?.datatype || '');
  }
  function enumOptions(p: any): string[] {
    if (!p?.enumlist) return [];
    try {
      const a = JSON.parse(p.enumlist);
      if (Array.isArray(a)) return a.map((x) => String(x));
    } catch {
      /* not json array */
    }
    return String(p.enumlist)
      .split(/[,，]/)
      .map((s) => s.trim())
      .filter(Boolean);
  }

  // ───────── 命令参数约束(来自物模型快照,用于展示 + 输入约束)─────────
  function toNum(v: any): number | undefined {
    return v === undefined || v === null || v === '' ? undefined : Number(v);
  }
  function paramMin(p: any): number | undefined {
    return toNum(p?.min ?? p?.minValue);
  }
  function paramMax(p: any): number | undefined {
    return toNum(p?.max ?? p?.maxValue);
  }
  function paramStep(p: any): number | undefined {
    return toNum(p?.step);
  }
  function paramMaxLen(p: any): number | undefined {
    return toNum(p?.maxLength ?? p?.maxlength);
  }
  // 约束摘要标签:范围 / 步进 / 最大长度 / 必填可选
  function paramMeta(p: any): string[] {
    const out: string[] = [];
    const mi = paramMin(p);
    const ma = paramMax(p);
    if (mi !== undefined && ma !== undefined) out.push(`${T('range')} ${mi} ~ ${ma}`);
    else if (mi !== undefined) out.push(`${T('range')} ≥ ${mi}`);
    else if (ma !== undefined) out.push(`${T('range')} ≤ ${ma}`);
    const st = paramStep(p);
    if (st !== undefined) out.push(`${T('step')} ${st}`);
    const ml = paramMaxLen(p);
    if (ml !== undefined) out.push(`${T('maxLen')} ${ml}`);
    out.push(p?.required === 1 ? T('requiredTag') : T('optionalTag'));
    return out;
  }
  // 数值输入框 placeholder:提示取值范围
  function numPh(p: any): string {
    const mi = paramMin(p);
    const ma = paramMax(p);
    if (mi !== undefined && ma !== undefined) return `${mi} ~ ${ma}`;
    if (mi !== undefined) return `≥ ${mi}`;
    if (ma !== undefined) return `≤ ${ma}`;
    return T('inputPh');
  }
  // 文本输入框 placeholder:提示最大长度
  function textPh(p: any): string {
    const ml = paramMaxLen(p);
    return ml !== undefined ? `${T('maxLen')} ${ml}` : T('inputPh');
  }

  function buildParams(): Record<string, any> {
    const obj: Record<string, any> = {};
    requestParams.value.forEach((p) => {
      let v = p._value;
      if (v === undefined || v === null || v === '') return;
      if (isJson(p)) {
        try {
          v = JSON.parse(v);
        } catch {
          /* keep raw string */
        }
      } else if (isNumber(p)) {
        v = Number(v);
      } else if (isBool(p)) {
        v = !!v;
      }
      obj[p.parameterCode] = v;
    });
    return obj;
  }

  function missingRequired(): string | null {
    for (const p of requestParams.value) {
      if (p.required === 1 && (p._value === undefined || p._value === null || p._value === '')) {
        return p.parameterName || p.parameterCode;
      }
    }
    return null;
  }

  // ───────── 报文预览 ─────────
  const previewText = computed(() => {
    if (contentMode.value === 'structured') {
      const body = {
        msgType: 'cloudReq',
        serviceCode: serviceCode.value,
        cmd: cmd.value,
        params: buildParams(),
      };
      const topic =
        formState.topic || `/{version}/devices/${pickDeviceId.value || '[deviceId]'}/command`;
      return `topic  ${topic}\n${JSON.stringify(body, null, 2)}`;
    }
    return `topic  ${formState.topic || '[topic]'}\n${formState.payload || ''}`;
  });

  // ───────── 发送 ─────────
  async function handleSend(): Promise<void> {
    if (contentMode.value === 'structured') {
      if (!canStructured.value) {
        createMessage.warning(T('pickDeviceVersionFirst'));
        return;
      }
      if (!serviceCode.value || !cmd.value) {
        createMessage.warning(T('commandPh'));
        return;
      }
      const miss = missingRequired();
      if (miss) {
        createMessage.warning(`${miss} ${T('paramRequired')}`);
        return;
      }
      sending.value = true;
      try {
        await issueCommands({
          serial: [
            {
              msgType: 'cloudReq',
              serviceCode: serviceCode.value,
              cmd: cmd.value,
              params: buildParams(),
              deviceIdentification: pickDeviceId.value,
              productIdentification: pickProductId.value,
              versionNo: versionNo.value,
            },
          ],
          parallel: [],
        } as any);
        createMessage.success(T('sendSuccess'));
        window.setTimeout(loadHistory, 600);
      } catch {
        /* http 拦截已提示 */
      } finally {
        sending.value = false;
      }
    } else {
      if (!formState.topic) {
        createMessage.warning(T('topicPh'));
        return;
      }
      if (!formState.payload) {
        createMessage.warning(T('payloadPh'));
        return;
      }
      sending.value = true;
      try {
        await sendMsg({ ...formState, deviceIdentification: pickDeviceId.value });
        createMessage.success(T('sendSuccess'));
        window.setTimeout(loadHistory, 600);
      } catch {
        /* http 拦截已提示 */
      } finally {
        sending.value = false;
      }
    }
  }

  // 结构化命令 → 灌进原始报文(便于改 hex / 自定义后原样发)
  function generateToRaw(): void {
    const body = {
      msgType: 'cloudReq',
      serviceCode: serviceCode.value,
      cmd: cmd.value,
      params: buildParams(),
    };
    formState.payload = JSON.stringify(body, null, 2);
    contentMode.value = 'raw';
  }

  function resetForm(): void {
    formState.topic =
      mode.value === 'device' && pickDeviceId.value
        ? `/v1/devices/${pickDeviceId.value}/command`
        : '';
    formState.qos = '1';
    formState.expirySeconds = 60;
    formState.payload = '';
    formState.tenantId = userStore.getTenantId || '';
    serviceCode.value = '';
    cmd.value = '';
    requestParams.value = [];
  }

  // ───────── 历史记录 ─────────
  /** 解析 content/remark,补出 serviceCode/cmd/versionNo/topic(原始下行还原 payload),供展示/重发 */
  function enrichRecord(r: any): any {
    try {
      if (Number(r.commandType) === 1) {
        const body = JSON.parse(r.remark || '{}');
        r.serviceCode = body?.serviceCode;
        r.cmd = body?.cmd;
        r.errCode = body?.errCode;
        r.errMsg = body?.errMsg || body?.message || body?.msg;
        try {
          const c = JSON.parse(r.content || '{}');
          if (c?.topic) r.topic = c.topic;
        } catch {
          /* response content may be the raw protocol body */
        }
      } else {
        const c = JSON.parse(r.content || '{}');
        if (c && c.serviceCode) {
          r.serviceCode = c.serviceCode;
          r.cmd = c.cmd;
          r.versionNo = c.versionNo;
          r.topic = c.topic;
        } else if (c && c.topic !== undefined) {
          r.topic = c.topic;
          r.content = c.payload;
        }
      }
    } catch {
      /* content/remark 非 JSON(纯文本/hex)→ 原样 */
    }
    return r;
  }

  async function loadHistory(): Promise<void> {
    historyLoading.value = true;
    try {
      const scope = historyScope.value === 'device' && pickDeviceId.value ? 'device' : 'all';
      if (scope !== historyScope.value) historyScope.value = scope;
      const deviceIdentification =
        historyDeviceIdentification.value.trim() || (scope === 'device' ? pickDeviceId.value : '');
      const params: any = { limit: 100 };
      if (deviceIdentification) {
        params.deviceIdentification = deviceIdentification;
      }
      const topic = historyTopic.value.trim();
      if (topic) params.topic = topic;
      const list = await debugHistory(params);
      records.value = (Array.isArray(list) ? list : []).map(enrichRecord);
    } catch {
      records.value = [];
    } finally {
      historyLoading.value = false;
    }
  }

  function onHistoryDeviceChange(): void {
    if (!historyDeviceIdentification.value.trim()) loadHistory();
  }

  function ts(r: any): number {
    const v = r?.createdTime;
    if (!v) return 0;
    const n = new Date(String(v).replace(/-/g, '/')).getTime();
    return Number.isNaN(n) ? 0 : n;
  }

  const historyRows = computed(() =>
    records.value
      .map((record, index) => ({ record, index }))
      .sort(
        (a, b) =>
          ts(b.record) - ts(a.record) ||
          Number(b.record?.id || 0) - Number(a.record?.id || 0) ||
          b.index - a.index,
      )
      .map(({ record }) => record),
  );

  function onHistoryTopicChange(): void {
    if (!historyTopic.value.trim()) loadHistory();
  }

  // ───────── 一键重发 ─────────
  async function resend(rec: any): Promise<void> {
    if (mode.value !== 'device') mode.value = 'device';
    if (rec.deviceIdentification) {
      pickDeviceId.value = rec.deviceIdentification;
      formState.topic = rec.topic || `/v1/devices/${rec.deviceIdentification}/command`;
    }
    if (rec.serviceCode && rec.cmd) {
      // 结构化:从 content(cloudReq)还原服务/命令/参数
      let parsed: any = {};
      try {
        parsed = JSON.parse(rec.content || '{}');
      } catch {
        parsed = {};
      }
      if (rec.versionNo) versionNo.value = String(rec.versionNo);
      await loadSnapshot();
      serviceCode.value = rec.serviceCode;
      cmd.value = rec.cmd;
      onCommandChange();
      const params = parsed?.params || {};
      requestParams.value.forEach((p) => {
        if (params[p.parameterCode] !== undefined) {
          p._value = isJson(p) ? JSON.stringify(params[p.parameterCode]) : params[p.parameterCode];
        }
      });
      contentMode.value = 'structured';
    } else {
      // 原始:回填 topic + payload
      formState.payload = rec.content || '';
      contentMode.value = 'raw';
    }
    createMessage.info(T('resendFilled'));
  }

  // ───────── 查看报文 ─────────
  function viewRaw(rec: any): void {
    const raw =
      Number(rec.commandType) === 1 ? rec.remark || rec.content : rec.content || rec.remark;
    viewContent.value = pretty(raw);
    viewVisible.value = true;
  }
  function pretty(s: any): string {
    if (s == null) return '';
    try {
      return JSON.stringify(JSON.parse(s), null, 2);
    } catch {
      return String(s);
    }
  }
  function copyView(): void {
    handleCopyTextV2(viewContent.value);
  }

  // ───────── 展示 helpers ─────────
  function isIssueRecord(rec: any): boolean {
    return Number(rec.commandType) === 0;
  }
  function isResponseRecord(rec: any): boolean {
    return Number(rec.commandType) === 1;
  }
  function historyKey(rec: any, index: number): string {
    return rec?.id ? String(rec.id) : `${rec?.commandType ?? 'unknown'}-${ts(rec)}-${index}`;
  }
  function recordTypeText(rec: any): string {
    if (isResponseRecord(rec)) return T('commandResponse');
    return T('issueCommand');
  }
  function recordTypeColor(rec: any): string {
    return isResponseRecord(rec) ? 'success' : 'processing';
  }
  function hasBusinessResult(rec: any): boolean {
    return isResponseRecord(rec) && rec.errCode != null;
  }
  function businessStatusText(rec: any): string {
    return Number(rec.errCode) !== 0 ? T('businessFail') : T('businessOk');
  }
  function businessStatusColor(rec: any): string {
    return Number(rec.errCode) !== 0 ? 'error' : 'success';
  }
  function cmdLabel(rec: any): string {
    if (rec.serviceCode && rec.cmd) return rec.cmd;
    if (rec.topic) return T('rawLabel');
    return '—';
  }
  function statusText(rec: any): string {
    // 这里展示链路状态:下发记录看发送是否成功,响应记录看接收/落库是否成功;业务结果由 errCode 单独展示。
    const s = Number(rec.status);
    if (Number(rec.commandType) === 1) {
      if (s === 1) return T('receiveOk');
      if (s === 2) return T('receiveFail');
      return T('receivePending');
    }
    if (s === 1) return T('sendOk');
    if (s === 2) return T('sendFail');
    return T('sendPending');
  }
  function statusColor(rec: any): string {
    const s = Number(rec.status);
    if (s === 1) return 'success';
    if (s === 2) return 'error';
    return 'warning';
  }
  function fmt(v: any): string {
    return v ? formatToDateTime(v) : '';
  }

  // manual 强制原始;失去结构化条件时回落原始
  watch(mode, (m) => {
    if (m === 'manual') {
      contentMode.value = 'raw';
      historyScope.value = 'all';
    }
  });
  watch(canStructured, (ok) => {
    if (!ok && contentMode.value === 'structured') contentMode.value = 'raw';
  });

  onMounted(() => {
    loadHistory();
    pollTimer = window.setInterval(loadHistory, 5000);
  });
  onUnmounted(() => {
    if (pollTimer) window.clearInterval(pollTimer);
  });
</script>

<style lang="less" scoped>
  .mqtt-mono {
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
  }

  .mqtt-seg {
    display: flex;
    width: 100%;
    margin-bottom: 16px;

    :deep(.ant-radio-button-wrapper) {
      flex: 1;
      text-align: center;
    }
  }

  .mqtt-device {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 16px;
    padding: 12px;
    background: #f8fafc;
    border: 1px solid #eef0f4;
    border-radius: 8px;
  }

  .mqtt-structured {
    margin-bottom: 16px;
    padding: 12px;
    background: #f8fafc;
    border: 1px solid #eef0f4;
    border-radius: 8px;
  }

  .mqtt-field {
    margin-bottom: 12px;
  }

  .mqtt-label {
    display: block;
    margin-bottom: 4px;
    font-size: 13px;
    color: #2a3547;
  }

  .mqtt-param {
    margin-bottom: 10px;
  }

  .mqtt-param-label {
    margin-bottom: 4px;
    font-size: 12px;
    color: #5a6a85;
  }

  .mqtt-req {
    color: #fa5252;
  }

  .mqtt-param-type {
    margin-left: 6px;
    padding: 0 6px;
    font-size: 11px;
    color: #8c8c8c;
    background: #f1f3f6;
    border-radius: 4px;
  }

  .mqtt-param-unit {
    margin-left: 6px;
    padding: 0 6px;
    font-size: 11px;
    color: #1c7ed6;
    background: #e7f5ff;
    border-radius: 4px;
  }

  .mqtt-param-code {
    margin-left: 6px;
    font-size: 11px;
    color: #adb5bd;
    font-family: 'JetBrains Mono', Consolas, monospace;
  }

  .mqtt-param-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 4px;
  }

  .mqtt-meta-item {
    padding: 0 6px;
    font-size: 11px;
    line-height: 18px;
    color: #5a6a85;
    background: #f8f9fb;
    border: 1px solid #e9ecef;
    border-radius: 4px;
  }

  .mqtt-param-desc {
    margin-bottom: 4px;
    font-size: 11px;
    line-height: 1.5;
    color: #8c8c8c;
  }

  .mqtt-empty-sm {
    padding: 6px 0;
    font-size: 12px;
    color: #bfbfbf;
  }

  .mqtt-desc {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 16px;
    font-size: 12px;
    color: #8c8c8c;

    :deep(svg) {
      color: #1890ff;
    }
  }

  .mqtt-hint {
    margin-top: 4px;
    font-size: 12px;
    color: #8c8c8c;
    display: flex;
    align-items: center;
    gap: 4px;

    :deep(svg) {
      color: #faad14;
    }
  }

  .mqtt-preview-label {
    margin-bottom: 4px;
    font-size: 12px;
    color: #8c8c8c;
  }

  .mqtt-preview {
    margin: 0 0 14px;
    padding: 8px 10px;
    max-height: 180px;
    overflow: auto;
    background: #fafafa;
    border-radius: 6px;
    white-space: pre-wrap;
    word-break: break-all;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    color: #595959;
  }

  .mqtt-log {
    max-height: 560px;
    overflow: auto;
  }

  .mqtt-history-card :deep(.ant-card-head-wrapper) {
    align-items: flex-start;
  }

  .mqtt-history-card :deep(.ant-card-head-title) {
    flex: 0 0 auto;
    padding-top: 4px;
  }

  .mqtt-history-card :deep(.ant-card-extra) {
    flex: 1;
    min-width: 0;
    margin-left: 16px;
  }

  .mqtt-history-extra {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    flex-wrap: wrap;
  }

  .mqtt-history-filters {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 3px;
    background: #f7f9fc;
    border: 1px solid #eef2f7;
    border-radius: 8px;
  }

  .mqtt-history-input {
    height: 28px;
    background: #fff;
    border-color: #e5ebf3;
    border-radius: 6px;
    box-shadow: none;
  }

  .mqtt-history-input-device {
    width: 210px;
  }

  .mqtt-history-input-topic {
    width: 300px;
  }

  .mqtt-filter-prefix {
    display: inline-flex;
    align-items: center;
    height: 18px;
    padding-right: 6px;
    margin-right: 2px;
    color: #009688;
    font-size: 12px;
    line-height: 18px;
    border-right: 1px solid #edf1f5;
  }

  .mqtt-history-search {
    width: 28px;
    height: 28px;
    border-radius: 6px;
    background: #009688;
    border-color: #009688;
  }

  :deep(.mqtt-history-input.ant-input-affix-wrapper) {
    padding-top: 0;
    padding-bottom: 0;
  }

  :deep(.mqtt-history-input.ant-input-affix-wrapper-focused),
  :deep(.mqtt-history-input.ant-input-affix-wrapper:hover) {
    border-color: #009688;
    box-shadow: 0 0 0 2px rgb(0 150 136 / 10%);
  }

  .mqtt-empty {
    padding: 32px 0;
    text-align: center;
    color: #bfbfbf;
    font-size: 13px;
  }

  .mqtt-record {
    padding: 8px 0;
    border-bottom: 1px dashed #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .mqtt-line {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .mqtt-ic-up {
    color: #1890ff;
  }

  .mqtt-ic-down {
    color: #52c41a;
  }

  .mqtt-cmd {
    font-weight: 500;
    color: #2a3547;
  }

  .mqtt-topic-row {
    margin-top: 6px;
    padding-left: 44px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    line-height: 18px;
    color: #8c8c8c;
    white-space: normal;
    overflow-wrap: anywhere;
    word-break: break-all;
  }

  .mqtt-time {
    margin-left: auto;
    color: #bfbfbf;
    font-size: 12px;
    white-space: nowrap;
  }

  .mqtt-ic-btn {
    cursor: pointer;
    color: #bfbfbf;
    transition: color 0.2s;

    &:hover {
      color: #1890ff;
    }
  }

  .mqtt-view {
    margin: 0;
    padding: 10px 12px;
    max-height: 480px;
    overflow: auto;
    background: #fafafa;
    border-radius: 6px;
    white-space: pre-wrap;
    word-break: break-all;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    color: #595959;
  }

  @media (max-width: 1200px) {
    .mqtt-history-extra {
      justify-content: flex-start;
    }

    .mqtt-history-filters {
      width: 100%;
    }

    .mqtt-history-input-device,
    .mqtt-history-input-topic {
      flex: 1;
      min-width: 180px;
      width: auto;
    }
  }

  @media (max-width: 768px) {
    .mqtt-history-filters {
      flex-wrap: wrap;
    }

    .mqtt-history-input-device,
    .mqtt-history-input-topic {
      width: 100%;
      min-width: 100%;
    }
  }
</style>
