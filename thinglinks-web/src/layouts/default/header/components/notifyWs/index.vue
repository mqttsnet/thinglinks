<template>
  <div :class="prefixCls">
    <Popover :overlayClassName="`${prefixCls}__overlay`" title="" trigger="click">
      <Badge :count="count" :numberStyle="numberStyle" dot>
        <BellOutlined />
      </Badge>
      <template #content>
        <!-- 空态:ws 未连接 / 未收到推送 / 真无消息,显式 a-empty 比之前的空白卡片更友好 -->
        <a-empty
          v-if="!listData.length"
          :image="emptyImage"
          :description="t('layout.header.tooltipNotify') || '暂无新通知'"
          class="notify-empty"
        />
        <Tabs v-else>
          <template v-for="item in listData" :key="item.key">
            <TabPane>
              <template #tab>
                {{ item.name }}
                <span style="color: #fb441b">({{ item.data.total }}) </span></template
              >
              <!-- 绑定title-click事件的通知列表中标题是"可点击"的-->
              <NoticeList :remindMode="item.key" :value="item.data" @title-click="onNoticeClick" />
              <MsgWrapper @register="registerModal" />
            </TabPane>
          </template>
        </Tabs>
      </template>
    </Popover>
  </div>
</template>
<script lang="ts">
  import { computed, defineComponent, onMounted, reactive, ref } from 'vue';
  import { Badge, Empty, Popover, Tabs } from 'ant-design-vue';
  import { BellOutlined } from '@ant-design/icons-vue';
  import { useWebSocket } from '@vueuse/core';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useUserStore } from '/@/store/modules/user';
  import MsgWrapper from '/@/views/basic/msg/extendNotice/Wrapper.vue';
  import { mark } from '/@/api/basic/msg/extendNotice';
  import { ExtendNoticeResultVO } from '/@/api/basic/msg/model/extendNoticeModel';
  import { NoticeRemindModeEnum } from '/@/enums/biz/base';
  import NoticeList from './NoticeList.vue';
  import { TabItem } from './data';
  import { useI18n } from "/@/hooks/web/useI18n";
  import { useModal } from '/@/components/Modal';
  import { getToken } from '/@/utils/auth';

  export default defineComponent({
    components: {
      MsgWrapper,
      Popover,
      BellOutlined,
      Tabs,
      TabPane: Tabs.TabPane,
      Badge,
      AEmpty: Empty,
      NoticeList,
    },
    setup() {
      const { prefixCls } = useDesign('header-notify');
      const [registerModal, { openModal }] = useModal();
      const listData = ref<TabItem[]>([]);
      const userStore = useUserStore();
      const { t } = useI18n();
      const employeeId = userStore.getUserInfo.employeeId;
      const tenantId = userStore.getTenantId;
      const host = window.location.host;
      const protocol = window.location.protocol;
      const token = getToken();
      const state = reactive({
        server: `${
          protocol.includes('https') ? 'wss' : 'ws'
        }://${host}/api/wsMsg/anyone/myMsg/${tenantId}/${employeeId}?Token=${token}&TenantId=${tenantId}`,
        sendValue: '',
        recordList: [] as { id: number; time: number; res: string }[],
      });

      function onMessage(_: WebSocket, event: MessageEvent) {
        const jsonStr = event.data;
        if (!jsonStr) {
          return;
        }
        // try-catch 防御:后端如返回非 JSON(心跳 ack / 错误字符串)JSON.parse 会抛,
        // 异常未捕获时虽然 vueuse 内部 catch 但前端会丢这次推送数据,排查困难
        let jsonResult: any;
        try {
          jsonResult = JSON.parse(jsonStr);
        } catch (e) {
          console.warn('[notify-ws] 收到非 JSON 消息,忽略', jsonStr);
          return;
        }

        if (jsonResult?.type === '2') {
          listData.value = [
            {
              key: NoticeRemindModeEnum.TO_DO,
              name: t('basic.msg.eMsg.todos'),
              data: jsonResult.data?.todoList,
            },
            {
              key: NoticeRemindModeEnum.NOTICE,
              name: t('basic.msg.eMsg.warning'),
              data: jsonResult.data?.noticeList,
            },
            {
              key: NoticeRemindModeEnum.EARLY_WARNING,
              name: t('basic.msg.eMsg.reminder'),
              data: jsonResult.data?.earlyWarningList,
            },
          ];
        } else {
          send('pull');
        }
      }

      const ws = useWebSocket(state.server, {
        autoReconnect: {
          retries: 3,
          delay: 10000,
          onFailed() {
            console.log('Failed to connect WebSocket after 3 retires');
          },
        },
        heartbeat: {
          message: 'ping',
          interval: 5000,
        },
        onMessage: onMessage,
      });

      let send = ws.send;

      // ws 首次 ready 后主动拉一次;ws 失败时也兜底走 send 缓存(vueuse useBuffer 默认 true)
      onMounted(() => {
        send('pull');
      });

      /** Popover 默认空态图(antd 自带轻量版) ── 比纯空白卡片体验好 */
      const emptyImage = Empty.PRESENTED_IMAGE_SIMPLE;

      const count = computed(() => {
        let num = 0;
        for (let i = 0; i < listData.value.length; i++) {
          num += Number(listData.value[i]?.data?.total ?? 0);
        }
        return num;
      });

      async function onNoticeClick(record: ExtendNoticeResultVO) {
        if (record.autoRead) {
          const flag = await mark([record.id]);
          if (flag) {
            send('pull');
          }
        }
        openModal(true, {
          id: record.id,
        });
      }

      return {
        prefixCls,
        registerModal,
        listData,
        count,
        onNoticeClick,
        emptyImage,
        t,
        numberStyle: {},
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-header-notify';

  .@{prefix-cls} {
    padding-top: 2px;

    &__overlay {
      max-width: 360px;

      .notify-empty {
        margin: 24px 16px;
        width: 280px;

        .ant-empty-description {
          color: #97a1b0;
          font-size: 13px;
        }
      }
    }

    .ant-tabs-nav .ant-tabs-tab {
      margin: 0;
      padding: 10px;
    }

    .ant-tabs-content {
      width: 300px;
    }

    .ant-badge {
      font-size: 18px;

      .ant-badge-multiple-words {
        padding: 0 4px;
      }

      svg {
        width: 0.9em;
      }
    }
  }
</style>
