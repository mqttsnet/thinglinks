<template>
  <!--
    设备消息趋势(Flexy 风格)。
    上/下行消息面积图,支持快捷时间(1h / 24h / 1周)+ 自定义时间范围;
    后端按时间窗自适应粒度(1m / 1h / 1d / 1M)聚合,见 getMessageData。
  -->
  <div class="msg-panel">
    <div class="panel-head">
      <div class="panel-title">
        <span class="title-bar" />
        {{ t('iot.link.dashboard.assetStats.deviceMessage') }}
      </div>
      <div class="panel-tools">
        <a-radio-group v-model:value="quickValue" button-style="solid" size="small" @change="quickClickChange">
          <a-radio-button v-for="item in quickClickOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </a-radio-button>
        </a-radio-group>
        <a-range-picker
          v-model:value="rangeTime"
          size="small"
          format="YYYY-MM-DD HH:mm:ss"
          valueFormat="YYYY-MM-DD HH:mm:ss"
          :show-time="{ format: 'HH:mm:ss' }"
          class="range-picker"
          @change="onChange"
        />
      </div>
    </div>
    <div class="panel-body">
      <dataLineChart :options="uplinkDetails" height="420px" />
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, onMounted, reactive, toRefs } from 'vue';
  import { RadioGroup, RadioButton, RangePicker } from 'ant-design-vue';
  import dataLineChart from './dataLineChart.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { assetStatsDetails } from '/@/api/iot/link/dashboard/dashboard';
  import dayjs from 'dayjs';

  export default defineComponent({
    name: 'MessageDataLine',
    components: {
      [RadioGroup.name]: RadioGroup,
      [RadioButton.name]: RadioButton,
      [RangePicker.name]: RangePicker,
      dataLineChart,
    },
    setup() {
      const { t } = useI18n();

      const state = reactive<any>({
        uplinkDetails: null,
        quickClickOptions: [
          { label: t('iot.link.dashboard.assetStats.hour'), value: 'hour' },
          { label: t('iot.link.dashboard.assetStats.day'), value: 'day' },
          { label: t('iot.link.dashboard.assetStats.week'), value: 'week' },
        ],
        quickValue: 'day',
        rangeTime: [],
      });

      const setUplinkDetails = (x: any[], y1: number[], y2: number[]): void => {
        const area = (top: string) => ({
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [{ offset: 0, color: top }, { offset: 1, color: '#ffffff' }],
            global: false,
          },
        });
        state.uplinkDetails = {
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          legend: {
            data: [t('basic.system.baseChart.upLinkMessage'), t('basic.system.baseChart.downLinkMessage')],
            top: 0,
            right: 0,
            icon: 'roundRect',
            itemWidth: 10,
            itemHeight: 10,
            textStyle: { color: '#8c97a5' },
          },
          xAxis: { type: 'category', boundaryGap: false, data: x, axisLine: { lineStyle: { color: '#eef1f7' } }, axisLabel: { color: '#97a1b0' } },
          yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f4f6fb' } }, axisLabel: { color: '#97a1b0' } },
          grid: { left: '3%', right: '2%', top: '12%', bottom: '5%', containLabel: true },
          series: [
            { name: t('basic.system.baseChart.upLinkMessage'), data: y1, type: 'line', smooth: true, symbol: 'none', color: '#ffae1f', areaStyle: area('#ffd591') },
            { name: t('basic.system.baseChart.downLinkMessage'), data: y2, type: 'line', smooth: true, symbol: 'none', color: '#539bff', areaStyle: area('#adc6ff') },
          ],
        };
      };

      const getTimeByType = (type: string) => {
        switch (type) {
          case 'hour': return dayjs().subtract(1, 'hours').valueOf();
          case 'week': return dayjs().subtract(7, 'days').valueOf();
          case 'month': return dayjs().subtract(29, 'days').valueOf();
          case 'year': return dayjs().subtract(365, 'days').valueOf();
          case 'day': return dayjs().subtract(24, 'hours').valueOf();
          default: return dayjs().startOf('day').valueOf();
        }
      };

      /** 按时间跨度自适应聚合粒度,与后端 1m/1h/1d/1M 枚举对齐 */
      const getMessageData = async (data: { startTime: number; endTime: number }) => {
        let time = '1m';
        let limit = 60;
        const distance = data.endTime - data.startTime;
        const hour = 60 * 60 * 1000;
        const days = hour * 24;
        const months = days * 30;
        const year = 365 * days;

        if (distance <= hour + 10) {
          limit = 60;
        } else if (distance > hour && distance <= days) {
          time = '1h';
          limit = 24;
        } else if (distance > days && distance < year) {
          limit = Math.abs(Math.ceil(distance / days)) + 1;
          time = '1d';
        } else if (distance >= year) {
          limit = Math.abs(Math.floor(distance / months));
          time = '1M';
        }

        try {
          const res: any = await assetStatsDetails({
            endTime: dayjs(data.endTime).format('YYYYMMDDHHmm'),
            startTime: dayjs(data.startTime).format('YYYYMMDDHHmm'),
            limit,
            time,
          });
          const up = res?.uplinkDetails || [];
          const down = res?.downlinkDetails || [];
          setUplinkDetails(up.map((i: any) => i.timeString), up.map((i: any) => i.value), down.map((i: any) => i.value));
        } catch (e) {
          console.warn('[assetStats] 设备消息趋势加载失败', e);
        }
      };

      const quickClickChange = (e?: any): void => {
        let value = state.quickValue;
        if (e?.target?.value) {
          value = e.target.value;
          state.quickValue = value;
        }
        const endTime = dayjs().valueOf();
        const startTime = getTimeByType(value);
        state.rangeTime = [
          dayjs(startTime).format('YYYY-MM-DD HH:mm:ss'),
          dayjs(endTime).format('YYYY-MM-DD HH:mm:ss'),
        ];
        getMessageData({ endTime, startTime });
      };

      const onChange = (e: any) => {
        if (e && e.length) {
          state.quickValue = null;
          getMessageData({ startTime: dayjs(e[0]).valueOf(), endTime: dayjs(e[1]).valueOf() });
        }
      };

      onMounted(() => quickClickChange());

      return { t, ...toRefs(state), quickClickChange, onChange };
    },
  });
</script>

<style lang="less" scoped>
  .msg-panel {
    background: #fff;
    border: 1px solid #eef1f7;
    border-radius: 12px;
    padding: 16px 18px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
  }

  .panel-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;

    .title-bar {
      width: 3px;
      height: 14px;
      border-radius: 2px;
      background: #ffae1f;
    }
  }

  .panel-tools {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;

    .range-picker {
      width: 300px;
    }
  }

  .panel-body {
    flex: 1;
  }
</style>
