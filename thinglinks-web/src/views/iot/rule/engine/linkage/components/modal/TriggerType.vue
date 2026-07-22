<template>
  <div class="type-list">
    <a-button
      v-for="item in typeList"
      :key="item.id"
      class="type-item"
      :class="{ active: typeId == item.id }"
      @click="handleselect(item, $event)"
    >
      <Icon icon="ant-design:cloud-upload-outlined" class="type-icon" />
      <div class="title">{{ item.name }}</div>
    </a-button>
  </div>
</template>
<script lang="ts">
  import { defineComponent, reactive, toRefs } from 'vue';
  import { Icon } from '/@/components/Icon';

  export default defineComponent({
    name: 'TriggerType',
    components: { Icon },
    props: {},
    setup(_, { emit }) {
      const state = reactive({
        typeList: [
          {
            id: 1,
            name: '属性上报',
          },
        ],
        typeId: null,
      });
      const selectTypeCard = (column) => {
        emit('selectTypeCard', column);
      };
      const handleselect = (record: Recordable, e: Event) => {
        e?.stopPropagation();
        state.typeId = record.id;
        selectTypeCard(record);
      };

      return {
        handleselect,
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

    .type-item {
      width: 120px;
      height: 132px;
      padding: 10px;
      text-align: center;
      margin: 0 10px 10px 0;
      border-radius: 6px;
      cursor: pointer;

      &.active {
        border-color: @primary-color;
        color: @primary-color;
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
