<template>
  <div class="operate">
    <div class="tabs">
      <div
        v-for="item in topicTypeList"
        :key="item.id"
        :class="activeTab == item.key ? 'active tab' : 'tab'"
        @click="activeTab = item.key"
      >
        {{ item.name }}
      </div>
    </div>
    <div class="table-content">
      <BasicTopic
        v-if="activeTab == 0"
        :topicType="activeTab"
        :productIdentification="productIdentification"
      />
      <CustomizeTopic
        v-else-if="activeTab == 1"
        :topicType="activeTab"
        :productIdentification="productIdentification"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useI18n } from '/@/hooks/web/useI18n';
import BasicTopic from '/@/views/iot/link/productTopic/basicTopic/index.vue';
import CustomizeTopic from '/@/views/iot/link/productTopic/customizeTopic/index.vue';
import { useDict } from '/@/components/Dict';

const { getDictList } = useDict();

export default defineComponent({
  name: 'TopicList',
  components: {
    BasicTopic,
    CustomizeTopic,
  },
  props: {
    id: {
      type: String,
      default: '',
    },
  },
  setup(props) {
    const { t } = useI18n();
    const topicTypeList = ref<any[]>([]);
    const activeTab = ref(0);
    const productIdentification = ref(props.id);

    onMounted(() => {
      topicTypeList.value = getDictList('LINK_PRODUCT_TOPIC_TYPE');
    });

    return {
      t,
      activeTab,
      productIdentification,
      topicTypeList,
    };
  },
});
</script>

<style lang="less" scoped>
/* 父级 panel-card 已固定高度,这里 100% 撑满 + 独立滚动(内部 a-table 内容会很长) */
.operate {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .tabs {
    flex-shrink: 0;
    display: flex;

    .tab {
      min-width: 103px;
      height: 36px;
      background: #f0f2f5;
      border-radius: 6px 6px 0px 0px;
      font-size: 16px;
      font-weight: 500;
      color: #656565;
      text-align: center;
      line-height: 36px;
      cursor: pointer;

      &.active {
        background: #1a66ff;
        color: #fff;
      }

      & + .tab {
        margin-left: 12px;
      }
    }
  }

  /* 表格容器:占满 tabs 之后的剩余空间,内部独立滚 */
  .table-content {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    margin-top: 16px;
  }
}
</style>