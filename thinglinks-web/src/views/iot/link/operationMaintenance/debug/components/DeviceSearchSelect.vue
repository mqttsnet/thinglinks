<template>
  <a-select
    v-model:value="inner"
    show-search
    :filter-option="false"
    :options="options"
    :loading="loading"
    :placeholder="placeholder"
    :disabled="disabled"
    allow-clear
    style="width: 100%"
    @search="onSearch"
    @focus="onFocus"
    @change="onChange"
  >
    <template #option="{ label, online }">
      <span class="dss-dot" :class="{ on: online }"></span>
      <span>{{ label }}</span>
    </template>
  </a-select>
</template>

<script lang="ts" setup>
  /**
   * 调试中心通用设备搜索选择器 ── 输入设备名称远程搜索,选中后 emit 整条设备记录,
   * 由调用页决定怎么用(MQTT 拼 topic / WS 取 clientId+账号)。
   *
   * @author mqttsnet
   */
  import { ref, watch } from 'vue';
  import { useDebounceFn } from '@vueuse/core';
  import { page as devicePage } from '/@/api/iot/link/device/device';

  defineOptions({ name: 'DeviceSearchSelect' });

  const props = defineProps<{
    modelValue?: string;
    placeholder?: string;
    disabled?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'update:modelValue', v?: string): void;
    (e: 'pick', record: any): void;
  }>();

  const inner = ref<string | undefined>(props.modelValue);
  watch(
    () => props.modelValue,
    (v) => (inner.value = v),
  );

  const loading = ref(false);
  const options = ref<{ label: string; value: string; online: boolean; raw: any }[]>([]);

  /** connectStatus 命中"在线"的值 */
  const ONLINE_VALUES: Array<string | number> = [1, '1', 'ONLINE'];

  async function load(keyword?: string): Promise<void> {
    loading.value = true;
    try {
      const res: any = await devicePage({
        model: (keyword ? { deviceName: keyword } : {}) as any,
        size: 20,
        current: 1,
      } as any);
      options.value = (res?.records || []).map((d: any) => ({
        label: `${d.deviceName || d.deviceIdentification}（${d.deviceIdentification}）`,
        value: d.deviceIdentification,
        online: ONLINE_VALUES.includes(d.connectStatus),
        raw: d,
      }));
    } finally {
      loading.value = false;
    }
  }

  const onSearch = useDebounceFn((kw: string) => load(kw), 300);

  function onFocus(): void {
    if (!options.value.length) load();
  }
  function onChange(v?: string): void {
    emit('update:modelValue', v);
    const rec = options.value.find((o) => o.value === v)?.raw;
    if (rec) emit('pick', rec);
  }
</script>

<style lang="less" scoped>
  .dss-dot {
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 6px;
    background: #d9d9d9;

    &.on {
      background: #52c41a;
    }
  }
</style>
