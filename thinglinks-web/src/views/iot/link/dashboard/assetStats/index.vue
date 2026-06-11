<template>
  <!--
    资产统计(Flexy 风格)。
    数据 owner:本页统一加载 assetSummary(device + product 概况),下发给 KPI 卡,避免重复请求。
    布局:
      ① KPI 卡 + 6 细分指标(GrowCard)
      ② 双栏看板:左 设备消息趋势(2/3) + 右 北向集成·数据桥接看板(1/3,数据自取 rule bridgeSummary)
  -->
  <div class="asset-stats">
    <GrowCard :device="device" :product="product" />

    <a-row :gutter="[16, 16]" class="board-row">
      <a-col :xs="24" :lg="16">
        <messageDataLine class="board-cell" />
      </a-col>
      <a-col :xs="24" :lg="8">
        <BridgeBoard class="board-cell" />
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent, onMounted, reactive, toRefs } from 'vue';
  import { Row, Col } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import GrowCard from './components/GrowCard.vue';
  import messageDataLine from './components/messageDataLine.vue';
  import BridgeBoard from './components/BridgeBoard.vue';
  import { assetSummary } from '/@/api/iot/link/dashboard/dashboard';

  export default defineComponent({
    name: 'AssetStats',
    components: {
      [Row.name]: Row,
      [Col.name]: Col,
      GrowCard,
      messageDataLine,
      BridgeBoard,
    },
    setup() {
      const { t } = useI18n();

      const state = reactive<any>({
        device: {},
        product: {},
      });

      const loadSummary = async () => {
        try {
          const res: any = await assetSummary();
          state.device = res?.deviceOverviewResultVO || {};
          state.product = res?.productOverviewResultVO || {};
        } catch (e) {
          console.warn('[assetStats] summary 加载失败', e);
        }
      };

      onMounted(loadSummary);

      return { t, ...toRefs(state) };
    },
  });
</script>

<style lang="less" scoped>
  .asset-stats {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    background: linear-gradient(180deg, #f7f9ff 0%, #f4f6fb 100%);
    min-height: 100%;
  }

  .board-row {
    /* a-row 负 margin 与 padding 配合,gutter 已处理间距 */
  }

  .board-cell {
    height: 100%;
  }
</style>
