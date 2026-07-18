<template>
  <span class="detail-title-info">
    <a-switch
      :disabled="disabled"
      class="mr8"
      :checked-children="t('iot.link.engine.executionLog.enableAntiShake')"
      :un-checked-children="t('iot.link.engine.executionLog.disableAntiShake')"
      :checkedValue="0"
      :unCheckedValue="1"
      v-model:checked="antiShake"
    />
    <span v-if="antiShake == 0">
      <span style="width: 80px; display: inline-block">
        <a-input-number
          class="fdNumber"
          v-model:value="frequency.timeValue"
          :min="1"
          :max="100000"
          :step="1"
        />
      </span>
      {{ t('iot.link.engine.executionLog.secondsSend') }}
      <span style="width: 60px; display: inline-block">
        <a-input-number
          class="fdNumber"
          v-model:value="frequency.count"
          :min="1"
          :max="100"
          :step="1"
        />
      </span>
      {{ t('iot.link.engine.executionLog.processing') }}
      <a-radio-group size="small" v-model:value="occurrenceRadio" button-style="solid">
        <a-radio-button :value="1">{{ t('iot.link.engine.executionLog.first') }}</a-radio-button>
        <a-radio-button :value="2">{{ t('iot.link.engine.executionLog.last') }}</a-radio-button>
      </a-radio-group>
    </span>
  </span>
</template>

<script setup lang="ts">
  import { ref, reactive, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const props = defineProps({
    disabled: {
      type: Boolean,
      default: false,
    },
    antiShake: {
      type: Number,
      default: 1,
    },
    frequency: {
      type: Object,
      default: () => ({ timeValue: 1, count: 1 }), // Provide default values for the frequency object
    },
    occurrenceRadio: {
      type: Number,
      default: 1,
    },
  });

  const emits = defineEmits(['update:antiShake', 'update:frequency', 'update:occurrenceRadio']);

  const disabled = ref(props.disabled);
  const antiShake = ref(props.antiShake);
  const frequency = reactive(props.frequency);
  const occurrenceRadio = ref(props.occurrenceRadio);

  watch(
    () => props.disabled,
    (val: boolean) => {
      disabled.value = val;
    },
  );

  watch(
    () => props.antiShake,
    (val: number) => {
      antiShake.value = val;
    },
  );

  watch(antiShake, (val: Number) => {
    emits('update:antiShake', val);
  });

  watch(
    frequency,
    (val: Record<string, 'timeValue' | 'count'>) => {
      emits('update:frequency', { ...val }); // Emit a copy of the frequency object
    },
    { deep: true },
  );

  watch(occurrenceRadio, (val: number) => {
    emits('update:occurrenceRadio', val);
  });
</script>

<style lang="less" scoped>
  .detail-title-info {
    margin-left: 10px;
    height: 40px;
    display: flex;
    align-items: center;

    .mr8 {
      margin-left: 8px;
    }
  }
</style>
