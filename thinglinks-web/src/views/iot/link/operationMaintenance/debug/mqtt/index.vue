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
                    </div>
                    <a-select
                      v-if="enumOptions(p).length"
                      v-model:value="p._value"
                      allow-clear
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
                      style="width: 100%"
                    />
                    <a-textarea
                      v-else-if="isJson(p)"
                      v-model:value="p._value"
                      :rows="3"
                      class="mqtt-mono"
                      placeholder="{ }"
                    />
                    <a-input v-else v-model:value="p._value" allow-clear />
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

      <!-- 右:下发记录(device_command 真历史 + 配对时间线) -->
      <a-col :xs="24" :md="14" :xl="15">
        <a-card :title="T('record')" size="small">
          <template #extra>
            <a-space>
              <a-radio-group
                v-model:value="historyScope"
                size="small"
                button-style="solid"
                @change="loadHistory"
              >
                <a-radio-button value="device" :disabled="!pickDeviceId">
                  {{ T('scopeDevice') }}
                </a-radio-button>
                <a-radio-button value="all">{{ T('scopeAll') }}</a-radio-button>
              </a-radio-group>
              <a-button size="small" :loading="historyLoading" @click="loadHistory">
                <template #icon><ReloadOutlined /></template>
                {{ T('refresh') }}
              </a-button>
            </a-space>
          </template>
          <div class="mqtt-log">
            <div v-if="timeline.length === 0" class="mqtt-empty">{{ T('empty') }}</div>
            <div v-for="(item, i) in timeline" :key="i" class="mqtt-pair">
              <!-- 下发 -->
              <div v-if="item.down" class="mqtt-line">
                <ArrowUpOutlined class="mqtt-ic-up" />
                <span class="mqtt-cmd">{{ cmdLabel(item.down) }}</span>
                <a-tag :color="statusColor(item.down)">{{ statusText(item.down) }}</a-tag>
                <span class="mqtt-topic" :title="item.down.topic">{{ item.down.topic }}</span>
                <span class="mqtt-time">{{ fmt(item.down.createdTime) }}</span>
                <EyeOutlined class="mqtt-ic-btn" :title="T('view')" @click="viewRaw(item.down)" />
                <ReloadOutlined
                  class="mqtt-ic-btn"
                  :title="T('resend')"
                  @click="resend(item.down)"
                />
              </div>
              <!-- 响应 -->
              <div v-if="item.resp" class="mqtt-line mqtt-line--resp">
                <ArrowDownOutlined class="mqtt-ic-down" />
                <a-tag :color="statusColor(item.resp)">
                  {{ T('response') }} · {{ statusText(item.resp) }}
                </a-tag>
                <span class="mqtt-time">{{ fmt(item.resp.createdTime) }}</span>
                <EyeOutlined class="mqtt-ic-btn" :title="T('view')" @click="viewRaw(item.resp)" />
              </div>
              <div
                v-else-if="
                  item.down && Number(item.down.commandType) === 0 && item.down.serviceCode
                "
                class="mqtt-noresp"
              >
                {{ T('noResp') }}
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
  const historyLoading = ref(false);
  let pollTimer: any = null;

  // 查看
  const viewVisible = ref(false);
  const viewContent = ref('');

  // ───────── 按设备联动 ─────────
  function onPickProduct(v: any): void {
    pickProductId.value = v ?? '';
    pickDeviceId.value = '';
    versionNo.value = '';
    resetSnapshot();
  }
  function onPickDevice(val: any, recordsArr: any[]): void {
    const id = Array.isArray(val) ? val[0] : val;
    pickDeviceId.value = id ?? '';
    if (!id) return;
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

  // ───────── 历史 + 下发↔响应配对 ─────────
  /** 解析 content/remark,补出 serviceCode/cmd/versionNo/topic(原始下行还原 payload),供配对/展示/重发 */
  function enrichRecord(r: any): any {
    try {
      if (Number(r.commandType) === 1) {
        const body = JSON.parse(r.remark || '{}');
        r.serviceCode = body?.serviceCode;
        r.cmd = body?.cmd;
        r.errCode = body?.errCode;
      } else {
        const c = JSON.parse(r.content || '{}');
        if (c && c.serviceCode) {
          r.serviceCode = c.serviceCode;
          r.cmd = c.cmd;
          r.versionNo = c.versionNo;
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
      const params: any = { limit: 100 };
      if (historyScope.value === 'device' && pickDeviceId.value) {
        params.deviceIdentification = pickDeviceId.value;
      }
      const list = await debugHistory(params);
      records.value = (Array.isArray(list) ? list : []).map(enrichRecord);
    } catch {
      records.value = [];
    } finally {
      historyLoading.value = false;
    }
  }

  function ts(r: any): number {
    const v = r?.createdTime;
    if (!v) return 0;
    const n = new Date(String(v).replace(/-/g, '/')).getTime();
    return Number.isNaN(n) ? 0 : n;
  }

  // 下发(type=0)配最近一条 设备+服务+命令 相同的响应(type=1);孤立响应单列
  const timeline = computed(() => {
    const asc = [...records.value].sort((a, b) => ts(a) - ts(b));
    const usedResp = new Set<any>();
    const items: { down: any; resp: any }[] = [];
    asc.forEach((r, i) => {
      if (Number(r.commandType) !== 0) return;
      const key = `${r.deviceIdentification}|${r.serviceCode}|${r.cmd}`;
      let resp: any = null;
      for (let j = i + 1; j < asc.length; j++) {
        const c = asc[j];
        if (
          Number(c.commandType) === 1 &&
          !usedResp.has(c) &&
          `${c.deviceIdentification}|${c.serviceCode}|${c.cmd}` === key
        ) {
          resp = c;
          usedResp.add(c);
          break;
        }
      }
      items.push({ down: r, resp });
    });
    asc.forEach((r) => {
      if (Number(r.commandType) === 1 && !usedResp.has(r)) items.push({ down: null, resp: r });
    });
    return items.sort((a, b) => ts(b.down || b.resp) - ts(a.down || a.resp));
  });

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
  function cmdLabel(rec: any): string {
    if (rec.serviceCode && rec.cmd) return rec.cmd;
    if (rec.topic) return T('rawLabel');
    return '—';
  }
  function statusText(rec: any): string {
    // 响应:按设备回包 errCode(0/缺省=成功,非0=失败);下发:按落库 status
    if (Number(rec.commandType) === 1) {
      return rec.errCode != null && Number(rec.errCode) !== 0 ? T('fail') : T('ok');
    }
    const s = Number(rec.status);
    if (s === 1) return T('issued');
    if (s === 2) return T('fail');
    return T('pending');
  }
  function statusColor(rec: any): string {
    if (Number(rec.commandType) === 1) {
      return rec.errCode != null && Number(rec.errCode) !== 0 ? 'error' : 'success';
    }
    const s = Number(rec.status);
    if (s === 1) return 'processing';
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

  .mqtt-empty {
    padding: 32px 0;
    text-align: center;
    color: #bfbfbf;
    font-size: 13px;
  }

  .mqtt-pair {
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

    &--resp {
      margin-top: 6px;
      padding-left: 18px;
    }
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

  .mqtt-topic {
    max-width: 220px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    color: #8c8c8c;
  }

  .mqtt-time {
    margin-left: auto;
    color: #bfbfbf;
    font-size: 12px;
  }

  .mqtt-ic-btn {
    cursor: pointer;
    color: #bfbfbf;
    transition: color 0.2s;

    &:hover {
      color: #1890ff;
    }
  }

  .mqtt-noresp {
    margin-top: 6px;
    padding-left: 18px;
    font-size: 12px;
    color: #bfbfbf;
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
</style>
