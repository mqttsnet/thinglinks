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
      @click="handleselect(item, $event)"
    >
      <img :src="item.id == 0 ? deviceExport : handleWarning" alt="" />
      <div class="title">{{ item.name }}</div>
    </a-button>
    <!-- </div> -->
  </div>
  <!-- </BasicModal> -->
</template>
<script lang="ts">
  import { defineComponent, reactive, toRefs } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import deviceExport from '/@/assets/images/iot/rule/linkage/device_export.png';
  import handleWarning from '/@/assets/images/iot/rule/linkage/handle_warning.png';
  export default defineComponent({
    name: 'ActionType',
    components: {},
    props: {
      BasicModal,
    },
    setup(props, { emit }) {
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
      const handleselect = (record: Recordable, e: Event) => {
        // e?.stopPropagation();
        state.typeId = record.id;
        selectTypeCard(record);
      };

      return {
        t,
        handleselect,
        handleWarning,
        deviceExport,
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

      img {
        width: 80px;
        margin: 0 auto;
        margin-bottom: 6px;
      }
    }
  }
</style>
