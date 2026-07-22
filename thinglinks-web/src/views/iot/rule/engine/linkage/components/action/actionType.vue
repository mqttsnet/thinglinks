<template>
  <!-- <BasicModal v-bind="$attrs" @register="registerModal" title="动作规则" :maskClosable="false" @ok="handleSubmit"
    :keyboard="true" width="1000px"> -->
  <div class="type-list">
    <!-- <div> -->
    <a-button
      class="type-item"
      v-for="item in typeList"
      :key="item.id"
      :disabled="item.disabled"
      @click="handleselect(item)"
    >
      <Icon :icon="getActionTypeIcon(item.id)" class="type-icon" />
      <div class="title">{{ item.name }}</div>
    </a-button>
    <!-- </div> -->
  </div>
  <!-- </BasicModal> -->
</template>
<script lang="ts">
  import { defineComponent, reactive, toRefs } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { Icon } from '/@/components/Icon';
  export default defineComponent({
    name: 'ActionType',
    components: { Icon },
    props: {},
    setup(_, { emit }) {
      const { t } = useI18n();
      const state = reactive({
        typeList: [
          {
            id: 0,
            name: t('iot.link.engine.executionLog.action.deviceOutput'),
          },
          {
            id: 1,
            name: t('iot.link.engine.executionLog.action.triggerAlarm'),
          },
        ],
        typeId: null,
      });
      const selectTypeCard = (column) => {
        emit('selectTypeCard', column);
      };
      const handleselect = (record: Recordable) => {
        state.typeId = record.id;
        selectTypeCard(record);
      };
      const getActionTypeIcon = (id: number | string) => {
        if (Number(id) === 1) return 'ant-design:alert-outlined';
        return 'ant-design:cloud-upload-outlined';
      };

      return {
        t,
        handleselect,
        getActionTypeIcon,
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
    box-shadow: 0 0 5px #ccc;

    .type-item {
      width: 120px;
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
