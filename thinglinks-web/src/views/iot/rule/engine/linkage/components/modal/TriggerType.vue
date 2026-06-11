<template>
  <div class="type-list">
    <!-- <div class="type-item" :class="{ active: typeId == item.id }" v-for="item in  typeList " :key="item.id"
      @click="handleselect(item, $event)">
      <img src="/@/assets/images/index/Slice1@3x.png" alt="">
      <div class="title">{{ item.name }}</div>
    </div> -->
    <a-button  class="type-item" :class="{ active: typeId == item.id }"  v-for="item in  typeList " :key="item.id"
        @click="handleselect(item, $event)">
          <img :src="typeimg" alt="">
          <div class="title">{{ item.name }}</div>
        </a-button>
  </div>
</template>
<script lang="ts">
import { defineComponent, reactive, toRefs } from 'vue';
import typeimg from '/@/assets/images/iot/rule/linkage/device_export.png';

export default defineComponent({
  name: 'TriggerType',
  components: {},
  props: {

  },
  setup(props, { emit }) {

    const state = reactive({
      typeList: [{
        id: 1,
        name: '属性上报'
      }],
      typeId: null

    });
    const selectTypeCard = (column) => {
      emit('selectTypeCard', column);
    }
    const handleselect = (record: Recordable, e: Event) => {
      e?.stopPropagation();
      state.typeId = record.id
      selectTypeCard(record)
    }

    return {
      handleselect,
      typeimg,
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
      border-color: #009688;
      color: #009688;
    }

    img {
      width: 80px;
      margin: 0 auto;
      margin-bottom: 6px;
    }
  }
}
</style>
