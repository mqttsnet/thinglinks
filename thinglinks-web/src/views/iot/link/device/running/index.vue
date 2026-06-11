<template>
  <!--
    设备影子 Tab(Flexy 风格)。

    设计意图:
    - 顶部工具栏一行展示:实时指示灯 / 当前 service / 版本下拉 / 刷新
      ── 用户首屏即可看清"我现在看的是哪个版本的哪个服务,数据是不是活的"
    - 版本下拉默认 = 设备绑定版本(deviceCacheVO.boundProductVersionNo)
      切到历史已发布版本 → 弹出 banner 提示"正在查看历史影子",一键回到绑定版本
    - 左侧服务列表(从 productResultVO.services 取,无需再调一次接口)
    - 右侧属性卡片网格:大字号数值 + 单位 + 上报时间 + 操作
      空态分两种:无服务 / 该服务无属性,文案区分清晰
    - WebSocket 协议升级为 JSON `{serviceCode, versionNo}`,服务端按版本快照解析
      心跳同样发 JSON,避免后端把 serviceCode 误解释
  -->
  <div class="shadow-wrap">
    <!-- ===== 顶部工具栏 ===== -->
    <div class="shadow-toolbar">
      <div class="toolbar-left">
        <!-- 实时连接指示 -->
        <div class="live-indicator" :class="{ on: socketIsSuccess }">
          <span class="dot" />
          <span class="text">{{
            socketIsSuccess ? t('iot.link.device.device.shadow.live') : t('iot.link.device.device.shadow.offline')
          }}</span>
          <!-- 推送频率,秒值 = SHADOW_REFRESH_INTERVAL_MS / 1000,与定时器常量保持一致 -->
          <span v-if="socketIsSuccess" class="refresh-rate">
            {{ t('iot.link.device.device.shadow.refreshEvery', { n: 5 }) }}
          </span>
        </div>
        <!-- 当前服务 chip(从服务列表派生,纯展示) -->
        <span v-if="currentService" class="service-pill">
          <ApiOutlined />
          {{ currentService.serviceName }}
          <span class="code">({{ currentService.serviceCode }})</span>
        </span>
      </div>

      <div class="toolbar-right">
        <!-- 版本切换 -->
        <a-tooltip placement="topRight" :title="t('iot.link.device.device.shadow.versionTooltip')">
          <a-select
            v-model:value="selectedVersionNo"
            class="version-select"
            :options="versionOptions"
            :loading="versionLoading"
            :placeholder="t('iot.link.device.device.shadow.versionPlaceholder')"
            show-search
            option-filter-prop="label"
            option-label-prop="value"
            @change="onVersionChange"
          >
            <template #suffixIcon><BranchesOutlined /></template>
            <template #option="{ value, statusText, isBound }">
              <div class="ver-opt">
                <span class="ver-no">{{ value }}</span>
                <a-tag v-if="statusText" class="ver-tag" :color="versionStatusColor(statusText)">
                  {{ statusText }}
                </a-tag>
                <a-tag v-if="isBound" class="ver-tag" color="blue">
                  {{ t('iot.link.device.device.tag.boundVersion') }}
                </a-tag>
              </div>
            </template>
          </a-select>
        </a-tooltip>
        <a-button type="primary" ghost @click="refresh">
          <template #icon><RedoOutlined /></template>
          {{ t('common.title.refresh') }}
        </a-button>
      </div>
    </div>

    <!-- ===== 历史版本提示 banner ===== -->
    <a-alert
      v-if="isViewingHistory"
      class="history-banner"
      type="warning"
      show-icon
      :message="t('iot.link.device.device.shadow.historyBanner')"
    >
      <template #action>
        <a-button size="small" type="link" @click="backToBound">
          <template #icon><RollbackOutlined /></template>
          {{ t('iot.link.device.device.shadow.backToBound') }}
        </a-button>
      </template>
    </a-alert>

    <!-- ===== 主体:左服务列表 + 右属性网格 ===== -->
    <div class="shadow-body">
      <!-- 左:服务列表 -->
      <div class="service-panel">
        <div class="panel-title">
          <UnorderedListOutlined />
          <span>{{ t('iot.link.device.device.serviceList') }}</span>
          <a-tag color="processing" class="count-tag">{{ services.length }}</a-tag>
        </div>
        <div v-if="services.length" class="service-list">
          <div
            v-for="(item, index) in services"
            :key="item.serviceCode || index"
            class="service-item"
            :class="{ active: index === currentServiceIndex }"
            @click="changeService(index)"
          >
            <div class="line">
              <span class="name">{{ item.serviceName }}</span>
              <!-- 服务启用/停用 chip ── 与产品模型定义 tab 同款语义,始终显示(含选中态),否则选中后看不到状态 -->
              <a-tooltip
                :title="
                  Number(item.serviceStatus) === 0
                    ? t('iot.link.device.device.shadow.serviceEnabled')
                    : t('iot.link.device.device.shadow.serviceDisabled')
                "
                placement="top"
              >
                <!-- 服务状态:0=启用(ACTIVATED)绿点 / 1=停用(LOCKED)灰点,与 ProductServiceStatusEnum 对齐 -->
                <span class="status-dot" :class="{ off: Number(item.serviceStatus) !== 0 }" />
              </a-tooltip>
              <RightOutlined class="arrow" />
            </div>
            <div class="meta">
              <span class="code">{{ item.serviceCode }}</span>
            </div>
            <div v-if="item.description" class="desc">{{ item.description }}</div>
          </div>
        </div>
        <a-empty
          v-else
          class="empty-panel"
          :description="t('iot.link.device.device.shadow.emptyServices')"
          :image="Empty.PRESENTED_IMAGE_SIMPLE"
        />
      </div>

      <!-- 右:属性网格 -->
      <div class="property-panel">
        <div class="prop-grid" v-if="properties.length">
          <div class="prop-card" v-for="item in properties" :key="item.propertyCode">
            <div class="prop-head">
              <div class="prop-name">
                <span class="dot" />
                <span class="text">{{ item.propertyName }}</span>
                <!-- 读写方法小徽章 ── 卡片头部 icon-only,密集场景一眼分辨 r/w/rw -->
                <PropertyMethodBadge
                  v-if="item.method"
                  :method="item.method"
                  size="sm"
                  icon-only
                />
              </div>
              <div class="prop-actions">
                <a-tooltip
                  v-if="productPropertiesIcon[item.icon]"
                  placement="top"
                  :title="t('common.title.view') + ' Icon'"
                >
                  <EyeOutlined class="icon-btn" @click="iconPreview(productPropertiesIcon[item.icon])" />
                </a-tooltip>
                <a-tooltip v-if="item.description" placement="top" :title="item.description">
                  <QuestionCircleOutlined class="icon-btn" />
                </a-tooltip>
              </div>
            </div>
            <div class="prop-value">
              <a-tooltip
                placement="topLeft"
                :title="`${formatValue(item.propertyValue)}${item.unit ? ' ' + item.unit : ''}`"
              >
                <span class="value-text">{{ formatValue(item.propertyValue) }}</span>
                <span v-if="item.unit" class="unit">{{ item.unit }}</span>
              </a-tooltip>
            </div>
            <div class="prop-foot">
              <span class="ts">
                <ClockCircleOutlined />
                {{ item.createdTime || '-' }}
              </span>
              <a-tooltip placement="top" :title="t('common.title.details')">
                <a class="detail-link" @click="openDetailDrawer(item)">
                  <UnorderedListOutlined />
                  {{ t('common.title.details') }}
                </a>
              </a-tooltip>
            </div>
          </div>
        </div>
        <div v-else class="empty-props">
          <a-empty :description="false" :image="Empty.PRESENTED_IMAGE_SIMPLE">
            <template #description>
              <div class="empty-text">
                {{
                  services.length
                    ? t('iot.link.device.device.shadow.emptyProps')
                    : t('iot.link.device.device.shadow.selectService')
                }}
              </div>
              <div v-if="services.length" class="empty-hint">
                {{ t('iot.link.device.device.shadow.emptyPropsHint') }}
              </div>
            </template>
          </a-empty>
        </div>
      </div>
    </div>

    <runningDetail @register="runningDetailRegister" />
  </div>
</template>
<script lang="ts">
  // util
  import { defineComponent, ref, toRefs, reactive, computed, onMounted, onUnmounted, provide, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  // components
  import {
    EyeOutlined,
    RedoOutlined,
    UnorderedListOutlined,
    QuestionCircleOutlined,
    ApiOutlined,
    BranchesOutlined,
    ClockCircleOutlined,
    RollbackOutlined,
    RightOutlined,
  } from '@ant-design/icons-vue';
  import { Alert, Empty, Select, Tag, Tooltip } from 'ant-design-vue';
  import runningDetail from './components/runningDetail.vue';
  import { PropertyMethodBadge } from '/@/components/iot';
  import { useDrawer } from '/@/components/Drawer';
  // api
  import { queryDeviceShadow } from '/@/api/iot/link/deviceShadow/deviceShadow';
  import { findUrlById } from '/@/api/thinglinks/file/upload.ts';
  import { listByProduct } from '/@/api/iot/link/productVersion/productVersion';

  import { useIntervalFn, useWebSocket } from '@vueuse/core';
  import { isEmpty } from 'lodash-es';
  import { createImgPreview } from '/@/components/Preview/index';
  import { getToken } from '/@/utils/auth';

  /**
   * 已发布的版本 status,与后端 ProductVersionStatusEnum 对齐:
   * 0=DRAFT(草稿,不暴露给设备影子查询);1=PUBLISHED;2=CANARY 灰度中;3=SHADOW;4=ARCHIVED 归档(不可选)。
   * 这里筛选规则:DRAFT / ARCHIVED 不让用户选,其余历史版本都允许"回看"。
   */
  const SELECTABLE_VERSION_STATUS = new Set([1, 2, 3]);

  export default defineComponent({
    name: 'Running',
    components: {
      AAlert: Alert,
      AEmpty: Empty,
      ASelect: Select,
      ATag: Tag,
      ATooltip: Tooltip,
      RedoOutlined,
      UnorderedListOutlined,
      QuestionCircleOutlined,
      ApiOutlined,
      BranchesOutlined,
      ClockCircleOutlined,
      RollbackOutlined,
      RightOutlined,
      EyeOutlined,
      runningDetail,
      PropertyMethodBadge,
    },
    props: {
      deviceIdentification: {
        type: String,
        default: '',
      },
      /**
       * 设备绑定的产品版本序号(deviceDetail.boundProductVersionNo)。
       * 影子 Tab 默认按此版本查;用户可切到其他已发布版本"回看"。
       */
      boundProductVersionNo: {
        type: String,
        default: '',
      },
      productResultVO: {
        type: Object,
        default: () => ({}),
      },
    },
    setup(props) {
      const { t } = useI18n();
      const { createMessage } = useMessage();
      const currentServiceIndex = ref(0);
      const [runningDetailRegister, { openDrawer: openDetail }] = useDrawer();

      const state = reactive({
        deviceIdentification: props.deviceIdentification,
        services: [] as any[],
        serviceCode: '',
        properties: [] as any[],
      });

      // 版本下拉 ── option 结构:label 完整 versionNo 用于搜索,statusText/isBound 给 #option 渲染 tag
      const selectedVersionNo = ref<string>(props.boundProductVersionNo || '');
      const versionOptions = ref<
        Array<{ label: string; value: string; status?: number; statusText?: string; isBound?: boolean }>
      >([]);
      const versionLoading = ref(false);

      const currentService = computed(() => state.services[currentServiceIndex.value]);
      const isViewingHistory = computed(
        () =>
          !!props.boundProductVersionNo &&
          !!selectedVersionNo.value &&
          selectedVersionNo.value !== props.boundProductVersionNo,
      );

      // WebSocket 连接:/anyone/ 路径 → 网关解 token 写 header → 后端 WebSocketAuthGuard 校验 path tenantId
      // ws 不支持自定义 header,Token / TenantId 走 URL query 由网关 getHeader 从 query fallback 取
      const socketIsSuccess = ref(false);
      const protocol = window.location.protocol;
      const host = window.location.host;
      const userStore = useUserStore();
      const tenantId = userStore.getTenantId;
      const token = getToken();
      const wsState = reactive({
        server: `${
          protocol.includes('https') ? 'wss' : 'ws'
        }://${host}/api/wsLink/anyone/deviceOpenSocket/queryDeviceShadow/${tenantId}/${
          state.deviceIdentification
        }?Token=${token}&TenantId=${tenantId}`,
      });

      // 产品属性 icon 信息缓存
      const productPropertiesIcon = ref<Record<string, string>>({});
      const iconIdInfo = ref<string[]>([]);

      const getProductPropertiesIconInfo = async (ids: string[]) => {
        if (!ids?.length) return;
        try {
          const res = await findUrlById(ids);
          if (res) {
            productPropertiesIcon.value = { ...productPropertiesIcon.value, ...res };
          }
        } catch (err) {
          console.warn(`[device-shadow] 获取属性 icon 失败, deviceId=${state.deviceIdentification}`, err);
        }
      };

      /**
       * 严格按当前 state.serviceCode 匹配 res 里的 service ── 找不到直接忽略,
       * 不 fallback 到 services[0],避免 ws 旧响应覆盖新 service 数据(race condition)
       */
      const setDeviceShadowIconInfo = (res: any) => {
        if (!res || !res.services?.length) return;
        const matched = res.services.find((s: any) => s.serviceCode === state.serviceCode);
        if (!matched) return;
        state.properties = matched.properties || [];

        const currentIconIds: string[] = res.services
          .flatMap((s: any) => s.properties || [])
          .map((p: any) => p?.icon)
          .filter(Boolean);

        if (isEmpty(iconIdInfo.value)) {
          iconIdInfo.value = currentIconIds;
          getProductPropertiesIconInfo(currentIconIds);
        } else {
          const diff = currentIconIds.filter((id) => !iconIdInfo.value.includes(id));
          if (diff.length) {
            iconIdInfo.value = currentIconIds;
            getProductPropertiesIconInfo(diff);
          }
        }
      };

      /**
       * 最新一帧 WS 影子消息 ── 通过 provide 暴露给子组件(如 runningDetail)
       * 用于实现"实时滚动趋势":detail 抽屉 inject 后 watch,新值时 append 到本地 echoList
       */
      const latestShadowMessage = ref<any>(null);

      function onMessage(_: WebSocket, event: MessageEvent) {
        const jsonStr = event.data;
        if (!jsonStr) {
          return;
        }
        try {
          const jsonResult = JSON.parse(jsonStr);
          if (jsonResult) {
            socketIsSuccess.value = true;
            latestShadowMessage.value = jsonResult;
            setDeviceShadowIconInfo(jsonResult);
          }
        } catch (e) {
          // 后端心跳可能返回空串,容错即可
        }
      }

      /**
       * 构造 WS 消息体(JSON)── 与后端 QueryDeviceShadowEndpoint 协议对齐:
       * `{ "serviceCode": "...", "versionNo": "..." }`。
       * versionNo 为空时,后端 fallback 到设备绑定版本。
       */
      const buildWsMessage = (): string => {
        return JSON.stringify({
          serviceCode: state.serviceCode || '',
          versionNo: selectedVersionNo.value || '',
        });
      };

      const wsInstance = ref<any>(null);

      /**
       * 首次进入 / 切版本时调用 ── 建立 ws 长连。
       * heartbeat 用纯 "ping",后端识别为保活不触发查询(避免每 5 秒推一份业务数据);
       * 业务数据由切服务 / refresh / load 主动 send 触发。
       */
      const handleInitWs = () => {
        if (wsInstance.value?.close) {
          wsInstance.value.close();
        }
        wsInstance.value = useWebSocket(wsState.server, {
          autoReconnect: {
            retries: 3,
            delay: 10000,
            onFailed() {
              socketIsSuccess.value = false;
              console.warn('[device-shadow] WebSocket reconnect failed after 3 retries');
            },
          },
          heartbeat: {
            message: 'ping',
            interval: 5000,
          },
          onMessage,
        });
        // 首次连接立即 send 一次业务消息触发当前服务的影子数据下行
        wsInstance.value.send(buildWsMessage());
      };

      /**
       * 影子数据刷新间隔(毫秒)。心跳只发 "ping"(后端识别为保活、不回业务数据),
       * 若只靠首帧 + 切服务时 send,页面会停在第一条上报数据;故定时 send 业务消息持续拉取最新影子。
       * useIntervalFn 绑定组件 effect scope,卸载时自动停止,无需手动清理。
       * 此处的秒值同步展示在实时指示灯旁(模板 refresh-rate),修改时两处保持一致。
       */
      const SHADOW_REFRESH_INTERVAL_MS = 5000;
      useIntervalFn(() => {
        if (socketIsSuccess.value && state.serviceCode && wsInstance.value?.send) {
          wsInstance.value.send(buildWsMessage());
        }
      }, SHADOW_REFRESH_INTERVAL_MS);

      const load = async () => {
        const res = await queryDeviceShadow({
          deviceIdentification: state.deviceIdentification,
          serviceCode: state.serviceCode,
          versionNo: selectedVersionNo.value || undefined,
        });
        setDeviceShadowIconInfo(res);
      };

      /**
       * 切服务联动:
       *   1. 清空旧 properties 给即时视觉反馈,避免用户以为"切了没反应"
       *   2. ws 不重连(避免连接抖动 + 旧响应 in-flight 覆盖新服务数据),只 send 新 serviceCode
       *   3. 同步 HTTP load 拿最新影子数据
       *   4. setDeviceShadowIconInfo 严格匹配当前 serviceCode,旧 ws 响应自动忽略
       */
      const changeService = (index: number) => {
        currentServiceIndex.value = index;
        state.serviceCode = state.services[index]?.serviceCode || '';
        state.properties = [];
        if (wsInstance.value?.send) {
          wsInstance.value.send(buildWsMessage());
        }
        load();
      };

      const refresh = async () => {
        await load();
        createMessage.success(t('iot.link.device.device.refreshSuccess'));
      };

      const iconPreview = (url: string) => {
        createImgPreview({ imageList: [url], defaultWidth: 700, rememberState: true });
      };

      const openDetailDrawer = (item: any) => {
        openDetail(true, {
          ...item,
          deviceIdentification: state.deviceIdentification,
          serviceCode: state.serviceCode,
          versionNo: selectedVersionNo.value || undefined,
        });
      };

      /**
       * 按 versionNo 拉版本快照 → 解 services → 默认选第一个服务 → ws 重连 + load 首个服务影子。
       * 首次进入 / 切版本统一走本函数(services 仅存在于 product_version.productSnapshotJson)。
       */
      const loadServicesByVersion = async (versionNo: string | undefined) => {
        const productIdentification = props.productResultVO?.productIdentification;
        if (!productIdentification || !versionNo) {
          state.services = [];
          state.serviceCode = '';
          state.properties = [];
          return;
        }
        try {
          versionLoading.value = true;
          const { detail } = await import('/@/api/iot/link/productVersion/productVersion');
          const versionDetail = await detail(productIdentification, versionNo);
          if (!versionDetail?.productSnapshotJson) {
            createMessage.warning(t('iot.link.device.device.shadow.versionSwitched'));
            state.services = [];
            state.properties = [];
            return;
          }
          const snapshot = JSON.parse(versionDetail.productSnapshotJson);
          state.services = snapshot.services || [];
          if (state.services.length) {
            // 默认选第一个服务 ── 清空 properties 立即视觉反馈,handleInitWs 内部 send 首个 serviceCode
            currentServiceIndex.value = 0;
            state.serviceCode = state.services[0]?.serviceCode || '';
            state.properties = [];
            handleInitWs();
            load();
          } else {
            state.serviceCode = '';
            state.properties = [];
          }
        } catch (e) {
          console.warn('[device-shadow] 加载版本服务列表失败', e);
          state.services = [];
          state.properties = [];
        } finally {
          versionLoading.value = false;
        }
      };

      /** 拉版本列表;过滤 DRAFT/ARCHIVED;绑定版本被回滚等极端情况下退回第一项 */
      const loadVersionOptions = async () => {
        const productIdentification = props.productResultVO?.productIdentification;
        if (!productIdentification) return;
        versionLoading.value = true;
        try {
          const list = (await listByProduct(productIdentification)) || [];
          versionOptions.value = list
            .filter((v) => v.versionNo && SELECTABLE_VERSION_STATUS.has(v.versionStatus ?? -1))
            .map((v) => ({
              value: v.versionNo!,
              label: v.versionNo!,
              status: v.versionStatus,
              statusText: statusToText(v.versionStatus),
              isBound: v.versionNo === props.boundProductVersionNo,
            }));
          if (
            selectedVersionNo.value &&
            !versionOptions.value.find((o) => o.value === selectedVersionNo.value)
          ) {
            selectedVersionNo.value = versionOptions.value[0]?.value || '';
          }
        } catch (e) {
          console.warn('[device-shadow] 拉取版本列表失败', e);
        } finally {
          versionLoading.value = false;
        }
      };

      function statusToText(status?: number): string {
        if (status === 1) return t('iot.link.device.device.shadow.statusPublished');
        if (status === 2) return t('iot.link.device.device.shadow.statusCanary');
        if (status === 3) return t('iot.link.device.device.shadow.statusShadow');
        return '';
      }

      function versionStatusColor(statusText: string): string {
        if (statusText === t('iot.link.device.device.shadow.statusPublished')) return 'green';
        if (statusText === t('iot.link.device.device.shadow.statusCanary')) return 'orange';
        if (statusText === t('iot.link.device.device.shadow.statusShadow')) return 'purple';
        return 'default';
      }

      const onVersionChange = (value: string) => {
        selectedVersionNo.value = value;
        // 统一路径:无论切到当前绑定 / 历史版本,都按 versionNo 拉对应快照
        loadServicesByVersion(value);
      };

      const backToBound = () => {
        if (!props.boundProductVersionNo) return;
        selectedVersionNo.value = props.boundProductVersionNo;
        loadServicesByVersion(props.boundProductVersionNo);
      };

      // 数值格式化:数字保留必要位、对象/null 兜底
      const formatValue = (v: any): string => {
        if (v == null || v === '') return '-';
        if (typeof v === 'number') {
          return Number.isInteger(v) ? String(v) : v.toFixed(2);
        }
        if (typeof v === 'object') {
          try {
            return JSON.stringify(v);
          } catch (e) {
            return String(v);
          }
        }
        return String(v);
      };

      onMounted(() => {
        // 首次进入:默认选中 = 设备绑定版本;直接拉该版本快照 → 启动 ws + 加载首个服务影子
        // 并行拉版本下拉列表(供用户切版本回看用)
        selectedVersionNo.value = props.boundProductVersionNo || '';
        if (selectedVersionNo.value) {
          loadServicesByVersion(selectedVersionNo.value);
        }
        loadVersionOptions();
      });

      // 父组件 detail 重新 load 后 boundProductVersionNo 可能变化 ── 同步选中 + 重拉
      watch(
        () => props.boundProductVersionNo,
        (val) => {
          if (val && val !== selectedVersionNo.value) {
            selectedVersionNo.value = val;
            loadServicesByVersion(val);
          }
        },
      );

      onUnmounted(() => {
        if (wsInstance.value?.close) {
          wsInstance.value.close();
        }
      });

      /**
       * 给 detail 抽屉等子组件共享 WS 流 ── 实现实时滚动趋势:
       *   - latestShadowMessage:每帧 onMessage 写入,子组件 watch 即可拿到新值
       *   - socketIsSuccess:用于显示连接状态(LIVE 指示器)
       */
      provide('deviceShadowStream', {
        latestShadowMessage,
        socketIsSuccess,
      });

      return {
        t,
        Empty,
        // 状态
        ...toRefs(state),
        currentServiceIndex,
        currentService,
        socketIsSuccess,
        productPropertiesIcon,
        selectedVersionNo,
        versionOptions,
        versionLoading,
        isViewingHistory,
        versionStatusColor,
        // 行为
        changeService,
        refresh,
        iconPreview,
        openDetailDrawer,
        onVersionChange,
        backToBound,
        formatValue,
        runningDetailRegister,
        wsInstance,
      };
    },
  });
</script>
<style lang="less" scoped>
  /* Flexy 风格 ── 配合 detail.vue 的 panel-card 内嵌使用 */
  .shadow-wrap {
    display: flex;
    flex-direction: column;
    gap: 16px;
    min-height: 480px;
  }

  /* ===== 顶部工具栏 ===== */
  .shadow-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 12px 16px;
    background: linear-gradient(135deg, #f7f9ff 0%, #f3f6fc 100%);
    border: 1px solid #eef1f7;
    border-radius: 12px;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
    }

    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .live-indicator {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    border-radius: 999px;
    background: #fff;
    border: 1px solid #f0d0d0;
    color: #d03b5b;
    font-size: 12px;
    font-weight: 500;

    .dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #d03b5b;
    }

    /* 推送频率后缀:竖线分隔 + 弱化,跟随指示灯颜色(连通时为绿) */
    .refresh-rate {
      padding-left: 8px;
      border-left: 1px solid currentColor;
      opacity: 0.65;
      font-weight: 400;
    }

    &.on {
      border-color: #b6efde;
      color: #0c8a6f;

      .dot {
        background: #13deb9;
        box-shadow: 0 0 0 0 rgba(19, 222, 185, 0.6);
        animation: live-pulse 1.6s infinite;
      }
    }
  }

  @keyframes live-pulse {
    0% {
      box-shadow: 0 0 0 0 rgba(19, 222, 185, 0.55);
    }
    70% {
      box-shadow: 0 0 0 8px rgba(19, 222, 185, 0);
    }
    100% {
      box-shadow: 0 0 0 0 rgba(19, 222, 185, 0);
    }
  }

  .service-pill {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    background: #fff;
    border: 1px solid #e6ebf3;
    border-radius: 999px;
    font-size: 12px;
    color: #2a3547;

    .code {
      color: #97a1b0;
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    }
  }

  .version-select {
    min-width: 280px;
  }

  /* 版本下拉 #option:版本号 + 状态 tag + 当前绑定 tag */
  .ver-opt {
    display: flex;
    align-items: center;
    gap: 6px;
    .ver-no {
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 13px;
      color: #2a3547;
    }
    .ver-tag {
      margin-right: 0;
      font-size: 11px;
      line-height: 1.4;
    }
  }

  /* ===== 历史版本 banner ===== */
  .history-banner {
    border-radius: 10px;
  }

  /* ===== 主体 ===== */
  .shadow-body {
    display: flex;
    gap: 16px;
    flex: 1;
    min-height: 420px;
  }

  /* ----- 服务面板 ----- */
  .service-panel {
    width: 260px;
    flex-shrink: 0;
    background: #fff;
    border: 1px solid #eef1f7;
    border-radius: 12px;
    padding: 14px 12px;
    display: flex;
    flex-direction: column;

    .panel-title {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 4px 12px;
      border-bottom: 1px dashed #eef1f7;
      font-size: 13px;
      font-weight: 600;
      color: #2a3547;

      :deep(.anticon) {
        color: #5d87ff;
      }

      .count-tag {
        margin-left: auto;
        margin-right: 0;
        font-weight: 600;
      }
    }

    .service-list {
      margin-top: 8px;
      overflow-y: auto;
      flex: 1;
      max-height: 540px;
      padding-right: 2px;
    }
  }

  .service-item {
    padding: 10px 12px;
    border-radius: 10px;
    border: 1px solid transparent;
    cursor: pointer;
    transition: background 0.18s ease, border-color 0.18s ease, transform 0.18s ease;

    & + .service-item {
      margin-top: 6px;
    }

    .line {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .name {
        font-size: 14px;
        font-weight: 600;
        color: #2a3547;
        word-break: break-all;
      }

      .arrow {
        color: #c8cfdb;
        font-size: 12px;
        transition: transform 0.18s ease;
        margin-left: auto;
      }

      /* 服务启用状态小圆点 ── 启用绿色脉冲,停用灰色,放 name 右侧 arrow 左侧 */
      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #13deb9;
        box-shadow: 0 0 0 0 rgba(19, 222, 185, 0.6);
        flex-shrink: 0;
        margin-left: 6px;

        &.off {
          background: #c8cfdb;
          box-shadow: none;
        }
      }
    }

    .meta {
      margin-top: 4px;
      font-size: 12px;
      color: #97a1b0;
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    }

    .desc {
      margin-top: 6px;
      font-size: 12px;
      color: #6b7280;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    &:hover {
      background: #f5f8ff;
      border-color: #dfe6f5;
    }

    &.active {
      background: linear-gradient(135deg, #eef3ff 0%, #e6efff 100%);
      border-color: #c6d6fb;

      .line .name {
        color: #1a4ce0;
      }
      .line .arrow {
        color: #5d87ff;
        transform: translateX(2px);
      }
    }
  }

  /* ----- 属性面板 ----- */
  .property-panel {
    flex: 1;
    min-width: 0;
    background: #fff;
    border: 1px solid #eef1f7;
    border-radius: 12px;
    padding: 16px;
    overflow-y: auto;
    max-height: calc(100vh - 380px);
  }

  .prop-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 14px;
  }

  .prop-card {
    background: linear-gradient(180deg, #ffffff 0%, #fafbfd 100%);
    border: 1px solid #eef1f7;
    border-radius: 12px;
    padding: 14px 16px;
    display: flex;
    flex-direction: column;
    gap: 10px;
    transition: box-shadow 0.18s ease, transform 0.18s ease, border-color 0.18s ease;

    &:hover {
      border-color: #c6d6fb;
      box-shadow: 0 8px 22px rgba(93, 135, 255, 0.12);
      transform: translateY(-1px);
    }

    .prop-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
    }

    .prop-name {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      min-width: 0;

      .dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
        flex-shrink: 0;
      }

      .text {
        font-size: 13px;
        font-weight: 600;
        color: #2a3547;
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
      }
    }

    .prop-actions {
      display: inline-flex;
      align-items: center;
      gap: 8px;

      .icon-btn {
        font-size: 14px;
        color: #97a1b0;
        cursor: pointer;
        transition: color 0.18s ease;

        &:hover {
          color: #5d87ff;
        }
      }
    }

    .prop-value {
      display: flex;
      align-items: baseline;
      gap: 6px;
      min-height: 36px;

      .value-text {
        font-size: 24px;
        font-weight: 700;
        color: #2a3547;
        font-variant-numeric: tabular-nums;
        word-break: break-all;
        line-height: 1.1;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .unit {
        font-size: 13px;
        color: #97a1b0;
        font-weight: 500;
      }
    }

    .prop-foot {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      font-size: 12px;
      color: #97a1b0;
      padding-top: 8px;
      border-top: 1px dashed #eef1f7;

      .ts {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        font-variant-numeric: tabular-nums;
      }

      .detail-link {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        color: #5d87ff;
        cursor: pointer;
        transition: color 0.18s ease;

        &:hover {
          color: #2952cc;
        }
      }
    }
  }

  .empty-panel {
    padding: 40px 12px;
  }

  .empty-props {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 360px;

    .empty-text {
      font-size: 14px;
      color: #2a3547;
      font-weight: 500;
      margin-top: 8px;
    }
    .empty-hint {
      font-size: 12px;
      color: #97a1b0;
      margin-top: 4px;
    }
  }
</style>
