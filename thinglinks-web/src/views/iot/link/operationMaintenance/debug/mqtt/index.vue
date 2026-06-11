<template>
  <PageWrapper content-background>
    <a-row :gutter="16">
      <!-- 左:命令参数 -->
      <a-col :xs="24" :md="10" :xl="9">
        <a-card :title="t('iot.link.operationMaintenance.debug.mqtt.params')" size="small">
          <!-- 页面用途说明(原独立页头移入,避免割裂) -->
          <div class="mqtt-desc">
            <InfoCircleOutlined />
            <span>{{ t('iot.link.operationMaintenance.debug.mqtt.subtitle') }}</span>
          </div>

          <!-- 寻址方式:醒目的全宽分段切换 —— :options 单组件(不含 a-radio-button 子组件,更稳) -->
          <a-radio-group
            v-model:value="mode"
            :options="modeOptions"
            option-type="button"
            button-style="solid"
            class="mqtt-seg"
          />

          <!-- 按设备:选产品 → 选该产品下的设备(与 ACL 设备级交互一致) -->
          <div v-if="mode === 'device'" class="mqtt-device">
            <IotProductPicker :modelValue="pickProductId" @update:model-value="onPickProduct" />
            <IotDevicePicker
              :modelValue="pickDeviceId"
              @update:model-value="onPickDevice"
              :productIdentification="pickProductId"
            />
            <div class="mqtt-hint">
              <InfoCircleOutlined />
              <span>{{ t('iot.link.operationMaintenance.debug.mqtt.deviceHint') }}</span>
            </div>
          </div>

          <a-form ref="formRef" :model="formState" layout="vertical">
            <a-form-item
              :label="t('iot.link.operationMaintenance.debug.mqtt.tenantId')"
              name="tenantId"
              :rules="[{ required: true, message: req('tenantId') }]"
            >
              <a-input v-model:value="formState.tenantId" allow-clear />
            </a-form-item>
            <a-form-item
              :label="t('iot.link.operationMaintenance.debug.mqtt.topic')"
              name="topic"
              :rules="[{ required: true, message: req('topic') }]"
            >
              <a-input
                v-model:value="formState.topic"
                :placeholder="t('iot.link.operationMaintenance.debug.mqtt.topicPh')"
                allow-clear
              />
            </a-form-item>

            <a-row :gutter="8">
              <a-col :span="14">
                <a-form-item
                  :label="t('iot.link.operationMaintenance.debug.mqtt.qos')"
                  name="qos"
                  :rules="[{ required: true, message: req('qos') }]"
                >
                  <a-select v-model:value="formState.qos">
                    <a-select-option value="0">
                      {{ t('iot.link.operationMaintenance.debug.mqtt.qos0') }}
                    </a-select-option>
                    <a-select-option value="1">
                      {{ t('iot.link.operationMaintenance.debug.mqtt.qos1') }}
                    </a-select-option>
                    <a-select-option value="2">
                      {{ t('iot.link.operationMaintenance.debug.mqtt.qos2') }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="10">
                <a-form-item
                  :label="t('iot.link.operationMaintenance.debug.mqtt.expirySeconds')"
                  name="expirySeconds"
                  :rules="[{ required: true, message: req('expirySeconds') }]"
                >
                  <a-input-number
                    v-model:value="formState.expirySeconds"
                    :min="1"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item
              :label="t('iot.link.operationMaintenance.debug.mqtt.payload')"
              name="payload"
              :rules="[{ required: true, message: req('payload') }]"
            >
              <a-textarea
                v-model:value="formState.payload"
                :rows="8"
                class="mqtt-mono"
                :placeholder="t('iot.link.operationMaintenance.debug.mqtt.payloadPh')"
              />
              <div class="mqtt-hint">
                <InfoCircleOutlined />
                <span>{{ t('iot.link.operationMaintenance.debug.mqtt.payloadHint') }}</span>
              </div>
            </a-form-item>

            <a-space>
              <a-button type="primary" :loading="sending" @click="handleSend">
                <template #icon><SendOutlined /></template>
                {{ t('iot.link.operationMaintenance.debug.mqtt.send') }}
              </a-button>
              <a-button @click="resetForm">
                {{ t('iot.link.operationMaintenance.debug.mqtt.reset') }}
              </a-button>
            </a-space>
          </a-form>
        </a-card>
      </a-col>

      <!-- 右:下发记录 -->
      <a-col :xs="24" :md="14" :xl="15">
        <a-card :title="t('iot.link.operationMaintenance.debug.mqtt.record')" size="small">
          <template #extra>
            <a-button size="small" :disabled="!records.length" @click="records = []">
              {{ t('iot.link.operationMaintenance.debug.mqtt.clear') }}
            </a-button>
          </template>
          <div class="mqtt-log">
            <div v-if="!records.length" class="mqtt-empty">
              {{ t('iot.link.operationMaintenance.debug.mqtt.empty') }}
            </div>
            <div v-for="(r, i) in records" :key="i" class="mqtt-item">
              <div class="mqtt-item-head">
                <a-tag :color="r.ok ? 'success' : 'error'">
                  {{
                    r.ok
                      ? t('iot.link.operationMaintenance.debug.mqtt.ok')
                      : t('iot.link.operationMaintenance.debug.mqtt.fail')
                  }}
                </a-tag>
                <span class="mqtt-topic">{{ r.topic }}</span>
                <a-tag color="blue">QoS{{ r.qos }}</a-tag>
                <span class="mqtt-time">{{ r.time }}</span>
                <CopyOutlined
                  class="mqtt-copy"
                  :title="t('iot.link.operationMaintenance.debug.mqtt.copy')"
                  @click="copyMsg(r.payload)"
                />
              </div>
              <pre class="mqtt-payload">{{ r.payload }}</pre>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref } from 'vue';
  import { SendOutlined, InfoCircleOutlined, CopyOutlined } from '@ant-design/icons-vue';
  import { PageWrapper } from '/@/components/Page';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import { sendMsg } from '/@/api/iot/link/operationMaintenance/mqtt/mqtt';
  import { IotProductPicker, IotDevicePicker } from '/@/components/iot/IotProductDevicePicker';

  defineOptions({ name: 'DebugMqtt' });

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const userStore = useUserStore();

  const T = (k: string) => t(`iot.link.operationMaintenance.debug.mqtt.${k}`);
  /** 必填校验文案:请输入 + 字段标签 + ! */
  const req = (field: string) =>
    t('iot.link.operationMaintenance.debug.webSocket.pleaseEnter') + T(field) + '!';

  // 寻址方式分段选项 ── 用 a-radio-group 的 :options 渲染,不写 a-radio-button 子组件
  const modeOptions = computed(() => [
    { label: T('modeManual'), value: 'manual' },
    { label: T('modeDevice'), value: 'device' },
  ]);

  // 寻址方式:manual=手动填 topic;device=选设备自动拼命令 topic。默认手动(不选设备)
  const mode = ref<'manual' | 'device'>('manual');
  // 按设备:先选产品,再选该产品下的设备(与 ACL 设备级交互一致)
  const pickProductId = ref<string>('');
  const pickDeviceId = ref<string>('');

  /** 选产品:切换产品时清空已选设备 */
  function onPickProduct(v: any): void {
    pickProductId.value = v ?? '';
    pickDeviceId.value = '';
  }
  /** 选设备:拿到设备标识后自动拼命令下行 topic(onDevicePick) */
  function onPickDevice(v: any): void {
    pickDeviceId.value = v ?? '';
    if (v) onDevicePick({ deviceIdentification: v });
  }

  /** 选中设备 → 自动拼该设备命令下行 topic;租户保持当前登录租户(默认值),不从 clientId 猜 */
  function onDevicePick(device: any): void {
    if (!device?.deviceIdentification) return;
    formState.topic = `/v1/devices/${device.deviceIdentification}/command`;
  }

  const formRef = ref();
  const sending = ref(false);
  const formState = reactive<{
    tenantId: string;
    topic: string;
    qos: string;
    expirySeconds: number;
    payload: string;
  }>({
    // 租户默认取当前登录租户,可手动改
    tenantId: userStore.getTenantId || '',
    topic: '',
    qos: '1',
    expirySeconds: 60,
    payload: '',
  });
  const records = ref<{ ok: boolean; topic: string; qos: string; payload: string; time: string }[]>(
    [],
  );

  function now(): string {
    return new Date().toLocaleTimeString('zh-CN', { hour12: false });
  }

  async function handleSend(): Promise<void> {
    try {
      await formRef.value?.validate();
    } catch {
      return;
    }
    const snapshot = { topic: formState.topic, qos: formState.qos, payload: formState.payload };
    sending.value = true;
    try {
      await sendMsg({ ...formState });
      createMessage.success(T('sendSuccess'));
      records.value.unshift({ ok: true, ...snapshot, time: now() });
    } catch (e) {
      records.value.unshift({ ok: false, ...snapshot, time: now() });
    } finally {
      sending.value = false;
    }
  }

  function copyMsg(msg: string): void {
    handleCopyTextV2(msg);
  }

  function resetForm(): void {
    formRef.value?.resetFields();
    // 重置后租户回填当前登录租户
    formState.tenantId = userStore.getTenantId || '';
  }
</script>

<style lang="less" scoped>
  .mqtt-mono {
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
  }

  /* 寻址方式分段控件:全宽、两段等分 */
  .mqtt-seg {
    display: flex;
    width: 100%;
    margin-bottom: 16px;

    :deep(.ant-radio-button-wrapper) {
      flex: 1;
      text-align: center;
    }
  }

  /* 按设备选择区:Flexy 高亮区块 */
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

  /* 页面用途说明:轻量信息提示(蓝色 info 图标,区别于字段级的琥珀提示) */
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

  .mqtt-log {
    max-height: 420px;
    overflow: auto;
  }

  .mqtt-empty {
    padding: 32px 0;
    text-align: center;
    color: #bfbfbf;
    font-size: 13px;
  }

  .mqtt-item {
    padding: 8px 0;
    border-bottom: 1px dashed #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .mqtt-item-head {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .mqtt-topic {
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 13px;
    color: #2a3547;
  }

  .mqtt-time {
    margin-left: auto;
    color: #bfbfbf;
    font-size: 12px;
  }

  .mqtt-copy {
    cursor: pointer;
    color: #bfbfbf;
    transition: color 0.2s;

    &:hover {
      color: #1890ff;
    }
  }

  .mqtt-payload {
    margin: 6px 0 0;
    padding: 6px 8px;
    background: #fafafa;
    border-radius: 4px;
    white-space: pre-wrap;
    word-break: break-all;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    color: #595959;
  }
</style>
