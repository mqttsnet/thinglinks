<template>
  <PageWrapper content-background>
    <a-row :gutter="16">
      <!-- 左:连接配置 -->
      <a-col :xs="24" :md="9" :xl="8">
        <a-card
          :title="t('iot.link.operationMaintenance.debug.webSocket.connectConfig')"
          size="small"
        >
          <template #extra>
            <span class="ws-status" :class="connected ? 'is-on' : 'is-off'">
              <i class="ws-status-dot" />
              {{
                connected
                  ? t('iot.link.operationMaintenance.debug.webSocket.connected')
                  : t('iot.link.operationMaintenance.debug.webSocket.disconnected')
              }}
            </span>
          </template>

          <!-- 页面用途说明(原独立页头移入,避免割裂) -->
          <div class="ws-desc">
            <InfoCircleOutlined />
            <span>{{ t('iot.link.operationMaintenance.debug.webSocket.subtitle') }}</span>
          </div>

          <!-- 接入方式:醒目的全宽分段切换 —— 用 :options 单组件(不含 a-radio-button 子组件,更稳) -->
          <a-radio-group
            v-model:value="mode"
            :options="modeOptions"
            option-type="button"
            button-style="solid"
            :disabled="connected"
            class="ws-seg"
          />

          <!-- 按设备:选产品 → 选该产品下的设备(与 ACL 设备级交互一致) -->
          <div v-if="mode === 'device'" class="ws-device">
            <IotProductPicker
              :modelValue="pickProductId"
              @update:model-value="onPickProduct"
              :disabled="connected"
            />
            <IotDevicePicker
              :modelValue="pickDeviceId"
              @update:model-value="onPickDevice"
              :productIdentification="pickProductId"
              :disabled="connected"
            />
            <div class="ws-hint">
              <InfoCircleOutlined />
              <span>{{ t('iot.link.operationMaintenance.debug.webSocket.deviceHint') }}</span>
            </div>
          </div>

          <a-form layout="vertical">
            <a-form-item :label="t('iot.link.operationMaintenance.debug.webSocket.tenantId')">
              <a-input
                v-model:value="form.tenantId"
                :disabled="connected"
                placeholder="1"
                allow-clear
              />
            </a-form-item>
            <a-form-item :label="t('iot.link.operationMaintenance.debug.webSocket.clientId')">
              <a-input
                v-model:value="form.clientId"
                :disabled="connected"
                placeholder="3752151419551744@1"
                allow-clear
              />
            </a-form-item>
            <a-row :gutter="8">
              <a-col :span="12">
                <a-form-item :label="t('iot.link.operationMaintenance.debug.webSocket.username')">
                  <a-input v-model:value="form.username" :disabled="connected" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item :label="t('iot.link.operationMaintenance.debug.webSocket.password')">
                  <a-input-password
                    v-model:value="form.password"
                    :disabled="connected"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item :label="t('iot.link.operationMaintenance.debug.webSocket.wsUrl')">
              <a-textarea
                v-model:value="form.url"
                :rows="3"
                :disabled="connected"
                class="ws-mono"
              />
              <div class="ws-hint">
                <InfoCircleOutlined />
                <span>{{ t('iot.link.operationMaintenance.debug.webSocket.wsUrlHint') }}</span>
                <a-button type="link" size="small" :disabled="connected" @click="resetUrl">
                  {{ t('iot.link.operationMaintenance.debug.webSocket.resetDefault') }}
                </a-button>
              </div>
            </a-form-item>

            <a-button v-if="!connected" type="primary" block @click="connect">
              <template #icon><ApiOutlined /></template>
              {{ t('iot.link.operationMaintenance.debug.webSocket.connect') }}
            </a-button>
            <a-button v-else danger block @click="disconnect">
              <template #icon><PoweroffOutlined /></template>
              {{ t('iot.link.operationMaintenance.debug.webSocket.disconnect') }}
            </a-button>
          </a-form>
        </a-card>
      </a-col>

      <!-- 右:发送 + 接收 -->
      <a-col :xs="24" :md="15" :xl="16">
        <a-card :title="t('iot.link.operationMaintenance.debug.webSocket.sendFrame')" size="small">
          <template #extra>
            <a-radio-group
              v-model:value="msgType"
              :options="frameOptions"
              option-type="button"
              button-style="solid"
              size="small"
              @change="applyTemplate"
            />
          </template>

          <a-textarea v-model:value="form.message" :rows="9" class="ws-mono" />
          <div class="ws-hint">
            <InfoCircleOutlined />
            <span>{{ t('iot.link.operationMaintenance.debug.webSocket.messageHint') }}</span>
          </div>
          <a-button type="primary" block class="mt-2" :disabled="!connected" @click="send">
            <template #icon><SendOutlined /></template>
            {{ t('iot.link.operationMaintenance.debug.webSocket.sendBtn') }}
          </a-button>
        </a-card>

        <a-card
          :title="t('iot.link.operationMaintenance.debug.webSocket.receiveWindow')"
          size="small"
          class="mt-4"
        >
          <template #extra>
            <a-space>
              <a-checkbox v-model:checked="prettyOn">
                {{ t('iot.link.operationMaintenance.debug.webSocket.prettyJson') }}
              </a-checkbox>
              <a-button size="small" :disabled="!logs.length" @click="logs = []">
                {{ t('iot.link.operationMaintenance.debug.webSocket.clear') }}
              </a-button>
            </a-space>
          </template>
          <div class="ws-log">
            <div v-if="!logs.length" class="ws-empty">
              {{ t('iot.link.operationMaintenance.debug.webSocket.empty') }}
            </div>
            <div v-for="(l, i) in logs" :key="i" class="ws-log-row">
              <span class="ws-dir" :class="l.dir">{{ dirLabel(l.dir) }}</span>
              <span class="ws-time">{{ l.time }}</span>
              <pre class="ws-content">{{ formatMsg(l) }}</pre>
              <CopyOutlined
                class="ws-copy"
                :title="t('iot.link.operationMaintenance.debug.webSocket.copy')"
                @click="copyMsg(l.msg)"
              />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref, watch, onBeforeUnmount } from 'vue';
  import {
    ApiOutlined,
    PoweroffOutlined,
    SendOutlined,
    InfoCircleOutlined,
    CopyOutlined,
  } from '@ant-design/icons-vue';
  import { PageWrapper } from '/@/components/Page';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useUserStore } from '/@/store/modules/user';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import { detailBydeviceIdentification } from '/@/api/iot/link/device/device';
  import { IotProductPicker, IotDevicePicker } from '/@/components/iot/IotProductDevicePicker';

  defineOptions({ name: 'DebugWebSocket' });

  const { t } = useI18n();
  const tk = (k: string) => t(`iot.link.operationMaintenance.debug.webSocket.${k}`);
  const userStore = useUserStore();

  // 分段切换选项 ── 用 a-radio-group 的 :options 渲染,不写 a-radio-button 子组件
  const modeOptions = computed(() => [
    { label: tk('modeManual'), value: 'manual' },
    { label: tk('modeDevice'), value: 'device' },
  ]);
  const frameOptions = computed(() => [
    { label: tk('frameDatas'), value: 'datas' },
    { label: tk('framePing'), value: 'ping' },
  ]);

  const host = window.location.host;
  const wsProto = window.location.protocol === 'https:' ? 'wss' : 'ws';

  type Dir = 'sent' | 'recv' | 'sys';
  const prettyOn = ref(true);
  // 接入身份:manual=手动填;device=选设备自动填 clientId/账号。默认手动(不选设备)
  const mode = ref<'manual' | 'device'>('manual');
  // 按设备:先选产品,再选该产品下的设备(与 ACL 设备级交互一致)
  const pickProductId = ref<string>('');
  const pickDeviceId = ref<string>('');
  const form = reactive({
    // 租户默认取当前登录租户(getTenantId 是 Pinia getter,属性访问不带括号),可手动改
    tenantId: userStore.getTenantId || '1',
    clientId: '',
    username: '',
    password: '',
    url: '',
    message: '',
  });
  const msgType = ref<'datas' | 'ping'>('datas');
  const connected = ref(false);
  const logs = ref<{ dir: Dir; time: string; msg: string }[]>([]);
  let ws: WebSocket | null = null;

  /** 设备唯一标识 ── 默认取 clientId 的 @ 前缀(可在报文里改) */
  function deviceIdOf(): string {
    const at = form.clientId.indexOf('@');
    return at > 0 ? form.clientId.slice(0, at) : form.clientId || 'deviceId';
  }

  /** 经网关的设备接入端点地址 */
  function buildUrl(): string {
    const u = encodeURIComponent(form.username);
    const p = encodeURIComponent(form.password);
    return (
      `${wsProto}://${host}/api/wsBroker/anyUser/deviceOpenSocket/accessProtocol/socket/` +
      `${form.tenantId}/${form.clientId}?username=${u}&password=${p}`
    );
  }
  function resetUrl(): void {
    form.url = buildUrl();
  }

  /** 选产品:切换产品时清空已选设备 */
  function onPickProduct(v: any): void {
    pickProductId.value = v ?? '';
    pickDeviceId.value = '';
  }
  /** 选设备:拿到设备标识后回填账号(onDevicePick 内部拉详情取 clientId / 用户名 / 密码) */
  function onPickDevice(v: any): void {
    pickDeviceId.value = v ?? '';
    if (v) onDevicePick({ deviceIdentification: v });
  }

  /** 选中设备 → 调设备详情回填 clientId / 用户名 / 密码 / 租户(模拟该真实设备接入) */
  async function onDevicePick(device: any): Promise<void> {
    if (!device?.deviceIdentification) return;
    let detail: any = device;
    try {
      detail = (await detailBydeviceIdentification(device.deviceIdentification)) || device;
    } catch {
      detail = device; // 详情拉取失败,用列表记录兜底
    }
    if (detail.clientId) form.clientId = detail.clientId;
    if (detail.userName != null) form.username = detail.userName;
    if (detail.password != null) form.password = detail.password;
    // 租户保持当前登录租户(form.tenantId 默认即登录租户),不从 clientId 的 @ 后缀猜
    form.url = buildUrl();
  }

  /** 设备数据上报报文(默认):payload 为平台标准协议信封 {head, dataBody, dataSign} */
  function datasTemplate(): string {
    const did = deviceIdOf();
    const ts = Date.now();
    return JSON.stringify(
      {
        topic: `/v1/devices/${did}/datas`,
        payload: {
          // 协议头:mid 消息ID(从1自增/雪花均可)、cipherFlag 加密标志(0明文/1SM4/2AES)、timeStamp 13位毫秒
          head: { mid: 1, cipherFlag: 0, timeStamp: ts },
          // 报文体:平台标准 devices 结构(明文为 JSON 对象)
          dataBody: {
            devices: [
              {
                deviceId: did,
                services: [
                  {
                    serviceCode: 'Temperature',
                    data: { temperature: 25.6, humidity: 60 },
                    eventTime: ts,
                  },
                ],
              },
            ],
          },
          // 明文(cipherFlag=0)无需签名;加密时填 SHA256(timeStamp:signKey) 全小写
          dataSign: '',
        },
      },
      null,
      2,
    );
  }
  function applyTemplate(): void {
    form.message = msgType.value === 'ping' ? JSON.stringify({ type: 'PING' }) : datasTemplate();
  }

  // 改连接字段(未连接时)→ 自动重建地址;用户也可手动编辑地址
  watch(
    () => [form.tenantId, form.clientId, form.username, form.password],
    () => {
      if (!connected.value) form.url = buildUrl();
    },
  );

  function now(): string {
    const d = new Date();
    return (
      d.toLocaleTimeString('zh-CN', { hour12: false }) +
      '.' +
      String(d.getMilliseconds()).padStart(3, '0')
    );
  }
  function pushLog(dir: Dir, msg: string): void {
    logs.value.unshift({ dir, time: now(), msg });
  }
  function dirLabel(dir: Dir): string {
    return dir === 'sent' ? tk('dirSent') : dir === 'recv' ? tk('dirRecv') : tk('dirSys');
  }

  /** 递归把嵌套的 JSON 字符串展开 ── 下行 DOWN 报文常多层转义,展开后才好读 */
  function deepUnwrap(v: any): any {
    if (typeof v === 'string') {
      const s = v.trim();
      if ((s.startsWith('{') && s.endsWith('}')) || (s.startsWith('[') && s.endsWith(']'))) {
        try {
          return deepUnwrap(JSON.parse(s));
        } catch {
          return v;
        }
      }
      return v;
    }
    if (Array.isArray(v)) return v.map(deepUnwrap);
    if (v && typeof v === 'object') {
      const o: Record<string, any> = {};
      Object.keys(v).forEach((k) => (o[k] = deepUnwrap(v[k])));
      return o;
    }
    return v;
  }
  /** 展示用:开启美化时把 JSON(含多层转义)缩进展开;系统消息 / 非 JSON 原样 */
  function formatMsg(l: { dir: Dir; msg: string }): string {
    if (!prettyOn.value || l.dir === 'sys') return l.msg;
    try {
      return JSON.stringify(deepUnwrap(JSON.parse(l.msg)), null, 2);
    } catch {
      return l.msg;
    }
  }
  function copyMsg(msg: string): void {
    handleCopyTextV2(msg);
  }

  function connect(): void {
    if (!form.url) form.url = buildUrl();
    try {
      pushLog('sys', `${tk('sysConnecting')} ${form.url}`);
      ws = new WebSocket(form.url);
      ws.onopen = () => {
        connected.value = true;
        pushLog('sys', tk('sysOpen'));
      };
      ws.onmessage = (e: MessageEvent) => {
        pushLog('recv', typeof e.data === 'string' ? e.data : String(e.data));
      };
      ws.onclose = (e: CloseEvent) => {
        connected.value = false;
        pushLog('sys', `${tk('sysClose')} code=${e.code} ${e.reason || ''}`.trim());
      };
      ws.onerror = () => pushLog('sys', tk('sysError'));
    } catch (err) {
      pushLog('sys', `${tk('sysError')}: ${(err as Error).message}`);
    }
  }
  function disconnect(): void {
    ws?.close();
  }
  function send(): void {
    if (!ws || ws.readyState !== WebSocket.OPEN) return;
    ws.send(form.message);
    pushLog('sent', form.message);
  }

  // 初始化:填默认地址 + 默认上报报文
  resetUrl();
  applyTemplate();

  onBeforeUnmount(() => ws?.close());
</script>

<style lang="less" scoped>
  .ws-mono {
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
  }

  /* 接入方式分段控件:全宽、两段等分 */
  .ws-seg {
    display: flex;
    width: 100%;
    margin-bottom: 16px;

    :deep(.ant-radio-button-wrapper) {
      flex: 1;
      text-align: center;
    }
  }

  /* 按设备选择区:Flexy 高亮区块 */
  .ws-device {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 16px;
    padding: 12px;
    background: #f8fafc;
    border: 1px solid #eef0f4;
    border-radius: 8px;
  }

  /* 连接状态 chip:圆点 + 文案 */
  .ws-status {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;

    .ws-status-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #bfbfbf;
    }

    &.is-on {
      color: #389e0d;

      .ws-status-dot {
        background: #52c41a;
      }
    }

    &.is-off {
      color: #8c8c8c;
    }
  }

  /* 页面用途说明:轻量信息提示(蓝色 info 图标,区别于字段级琥珀提示) */
  .ws-desc {
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

  .ws-hint {
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

  .ws-log {
    max-height: 360px;
    overflow: auto;
  }

  .ws-empty {
    padding: 32px 0;
    text-align: center;
    color: #bfbfbf;
    font-size: 13px;
  }

  .ws-log-row {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 6px 0;
    border-bottom: 1px dashed #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    &:hover .ws-copy {
      opacity: 1;
    }
  }

  .ws-copy {
    flex: none;
    margin-top: 2px;
    cursor: pointer;
    color: #bfbfbf;
    opacity: 0;
    transition: all 0.2s;

    &:hover {
      color: #1890ff;
    }
  }

  .ws-dir {
    flex: none;
    min-width: 40px;
    text-align: center;
    padding: 0 6px;
    border-radius: 4px;
    font-size: 12px;
    line-height: 20px;

    &.sent {
      color: #d46b08;
      background: #fff7e6;
    }

    &.recv {
      color: #389e0d;
      background: #f6ffed;
    }

    &.sys {
      color: #8c8c8c;
      background: #fafafa;
    }
  }

  .ws-time {
    flex: none;
    color: #bfbfbf;
    font-size: 12px;
    line-height: 20px;
  }

  .ws-content {
    flex: 1;
    margin: 0;
    white-space: pre-wrap;
    word-break: break-all;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    color: #2a3547;
  }
</style>
