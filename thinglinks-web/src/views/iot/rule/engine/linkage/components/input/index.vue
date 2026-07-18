<template>
  <div class="type-list">
    <a-card style="padding: 10px">
      <!-- TODO 需要根据属性值及操作符匹配输入框内容以及处理多个参数的问题 -->
      <div v-for="(paramsItem,paramsIndex) in childrenItem.rightParams" :key="paramsIndex">
        <a-input v-model:value="paramsItem.value" placeholder="请输入" />
      </div>
      <!-- <div>
        <a-textarea v-model:value="value" placeholder="请输入" :rows="4" />
      </div>
      <div>
        <a-input-number id="inputNumber" v-model:value="value" :min="1" :max="10" />
      </div>
      <div>
        <a-time-picker v-model:value="strValue" valueFormat="HH:mm:ss" />
      </div>
      <div>
        <a-radio-group v-model:value="value">
          <a-radio :value="1">Option A</a-radio>
          <a-radio :value="2">Option B</a-radio>
          <a-radio :value="3">Option C</a-radio>
          <a-radio  :value="4">
            More...
            <a-input v-if="value === 4" style="width: 100px; margin-left: 10px" />
          </a-radio>
        </a-radio-group>
      </div> -->
    </a-card>
  </div>
</template>
<script lang="ts">
import { defineComponent, reactive, toRefs } from 'vue';

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

    img {
      width: 80px;
      margin: 0 auto;
      margin-bottom: 6px;
    }
  }
}
</style>
