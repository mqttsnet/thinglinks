<template>
  <PageWrapper ref="pageWrapper">
    <div class="upgrade-detail-info">
      <Description
        :title="t('iot.link.ota.otaUpgradeRecords.upgradeRecordsDetail')"
        :column="2"
        :data="detail"
        :schema="schema"
      />
      <div class="upgrade-progress">
        <span>{{ t('iot.link.ota.otaUpgradeRecords.progress') }}：</span>
        <a-progress :percent="detail.progress" />
      </div>
      <div class="other-info">
        <a-tabs default-active-key="1" v-model:activeKey="currentKey">
          <a-tab-pane key="1" :tab="t('iot.link.ota.otaUpgradeRecords.upgradeLog')" />
          <a-tab-pane key="2" :tab="t('iot.link.ota.otaUpgradeRecords.upgradeSuccess')" />
          <a-tab-pane key="3" :tab="t('iot.link.ota.otaUpgradeRecords.upgradeFailure')" />
        </a-tabs>
        <template v-if="![null, undefined].includes(codemirrorVal)">
          <div class="code-info">
            <a-button @click="copyCode"> {{ t('common.title.copy') }} </a-button>
            <codemirror
              v-model="codemirrorVal"
              :read-only="true"
              :extensions="contentExtensions"
              :indent-with-tab="true"
              :line-wrapping="true"
              :style="{ height: '500px' }"
              :tab-size="2"
            />
          </div>
        </template>
        <template v-else>
          <span>{{ t('iot.link.ota.otaUpgradeRecords.empty') }}</span>
        </template>
      </div>
    </div>
  </PageWrapper>
</template>
<script lang="ts" setup>
  // util
  import { computed, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { message } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  // api
  import { getSingleQuery } from '/@/api/iot/link/ota/otaUpgradeRecords';
  // components
  import { Description } from '/@/components/Description/index';
  import { PageWrapper } from '/@/components/Page';
  import { Codemirror } from 'vue-codemirror';

  import { json } from '@codemirror/lang-json';
  import { oneDark } from '@codemirror/theme-one-dark';
  // data
  import { columns } from './OtaUpgradeRecordsCardDetail.data';
  import { reactive } from 'vue';

  const route = useRoute();

  const loading = ref(false);
  const detail = ref({});
  const currentKey = ref('1');
  const routeParams = reactive({
    ...route.params,
  });

  const schema = computed(() => {
    return columns();
  });

  const contentExtensions = [json(), oneDark];
  const codemirrorVal = computed(() => {
    if (currentKey.value === '1') {
      return detail.value['logDetails'];
    } else if (currentKey.value === '2') {
      return detail.value['successDetails'];
    } else if (currentKey.value === '3') {
      return detail.value['failureDetails'];
    }
    return '';
  });
  const copyCode = () => {
    const input = document.createElement('input');
    input.setAttribute('readonly', 'readonly');
    input.setAttribute('value', codemirrorVal.value);
    document.body.appendChild(input);
    input.select();
    input.setSelectionRange(0, 9999);
    if (document.execCommand('copy')) {
      document.execCommand('copy');
      message.success(t('common.tips.copySuccess'));
    }
  };

  // 获取详细信息
  const getDetailInfo = async (timeInfo) => {
    loading.value = true;
    try {
      const detailValue = await getSingleQuery(routeParams.id);
      if (detailValue) {
        detail.value = detailValue;
      }
      loading.value = false;
      console.log(detail.value);
    } catch (error) {
      console.log(error);
      loading.value = false;
    }
  };
  getDetailInfo();
</script>
<style lang="less" scoped>
  .thinglinks-page-wrapper {
    height: 100%;
  }

  :deep(.thinglinks-page-wrapper-content) {
    height: 100%;
  }

  .mar-l-10 {
    margin-left: 10px;
  }

  .upgrade-detail-info {
    height: 100%;

    .upgrade-progress {
      padding: 10px 20px;
      background-color: #fff;
    }

    .other-info {
      height: 100%;
      margin-top: 16px;
      padding: 24px;
      background-color: #fff;

      .code-info {
        text-align: start;

        button {
          margin-bottom: 10px;
        }
      }
    }
  }

  :deep(.ant-progress-bg) {
    height: 12px !important;
  }
</style>
