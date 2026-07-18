<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('iot.link.engine.executionLog.triggerType')"
    :maskClosable="false"
    @ok="handleSubmit"
    :keyboard="true"
    width="1000px"
  >
    <div class="type-list">
      <a-button
        class="type-item"
        v-for="item in typeList"
        :class="{ active: conditionType == item.value }"
        :key="item.value"
        @click="handleSelect(item, $event)"
      >
        <img
          :src="
            item.value === '0'
              ? deviceProperty
              : item.value === '1'
              ? deviceDs
              : item.value === '2'
              ? deviceStatus
              : deviceStatus
          "
          alt=""
        />
        <div class="title">{{ item.label }}</div>
      </a-button>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, reactive, toRefs, unref, ref, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { ActionEnum } from '/@/enums/commonEnum';
  import deviceProperty from '/@/assets/images/iot/rule/linkage/device_property.png';
  import deviceDs from '/@/assets/images/iot/rule/linkage/device_ds.png';
  import deviceStatus from '/@/assets/images/iot/rule/linkage/device_status.png';
  import { useI18n } from '/@/hooks/web/useI18n';
  export default defineComponent({
    name: 'ConditionType',
    components: {
      BasicModal,
    },
    props: {
      options: {
        type: Array as PropType<
          {
            label: string;
            text: string;
            value: string;
            color: string;
            remark: string;
          }[]
        >,
        default: () => [],
      },
      conditionType: {
        type: Number,
        default: 0,
      },
    },
    setup(props, { emit }) {
      const { t } = useI18n();

      const type = ref<ActionEnum>(ActionEnum.ADD);
      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        type.value = data?.type || ActionEnum.ADD;
        if (unref(type) !== ActionEnum.ADD) {
          // 赋值
        } else {
          // 重置
          console.log('重置选项');
        }
      });
      const state = reactive({
        typeList: props.options,
        conditionType: Number(props.conditionType),
      });
      const handleSelect = (record: Recordable, e: Event) => {
        e?.stopPropagation();
        state.conditionType = Number(record.value);
      };
      const handleSubmit = () => {
        emit('selectTypeCard', state.conditionType);
        closeModal();
      };

      watch(
        () => props.conditionType,
        (val) => {
          state.conditionType = Number(val);
        },
      );

      return {
        t,
        handleSelect,
        registerModal,
        handleSubmit,
        deviceProperty,
        deviceDs,
        deviceStatus,
        ...toRefs(state),
      };
    },
  });
</script>
<style lang="less" scoped>
  .type-list {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    background-color: #fff;
    padding: 12px;
    // box-shadow: 0 0 5px #ccc;

    .type-item {
      min-width: 120px;
      height: 132px;
      padding: 10px;
      text-align: center;
      margin: 0 10px 10px 0;
      // border: 1px solid #ccc;
      border-radius: 6px;
      // cursor: pointer;

      &.active {
        border-color: @primary-color;
      }

      img {
        width: 80px;
        margin: 0 auto;
        margin-bottom: 6px;
      }
    }
  }
</style>
