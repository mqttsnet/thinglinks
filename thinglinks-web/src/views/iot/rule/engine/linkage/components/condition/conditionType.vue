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
        <Icon :icon="getConditionTypeIcon(item.value)" class="type-icon" />
        <div class="title">{{ item.label }}</div>
      </a-button>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, reactive, toRefs, unref, ref, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';
  export default defineComponent({
    name: 'ConditionType',
    components: {
      BasicModal,
      Icon,
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
      const getConditionTypeIcon = (value: string | number) => {
        const iconMap: Record<number, string> = {
          0: 'ant-design:control-outlined',
          1: 'ant-design:database-outlined',
          2: 'ant-design:api-outlined',
        };
        return iconMap[Number(value)] || 'ant-design:api-outlined';
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
        getConditionTypeIcon,
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

      .type-icon {
        display: block;
        margin: 14px auto 16px;
        color: @primary-color;
        font-size: 48px;
      }
    }
  }
</style>
