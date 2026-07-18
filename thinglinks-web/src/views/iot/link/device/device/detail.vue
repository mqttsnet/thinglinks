<template>
  <PageWrapper contentFullHeight class="device-detail">
    <!-- ===== 顶部 Header(Flexy 风格,对齐产品详情) ===== -->
    <a-card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="rule-icon" :class="{ 'no-pad': !deviceDetail?.productResultVO?.icon }">
            <ImageDisplay
              v-if="deviceDetail?.productResultVO?.icon"
              :fileId="deviceDetail.productResultVO.icon"
              :imageWidth="40"
              :imageHeight="40"
              :imageStyle="{ 'max-width': '40px', 'max-height': '40px' }"
              :showBorder="false"
              :preview="false"
            />
            <component v-else :is="getDeviceNodeTypeSvg(deviceDetail?.nodeType)" />
          </div>
          <div class="rule-meta">
            <div class="rule-title">
              <span class="name-text">{{ deviceDetail?.deviceName || '-' }}</span>
              <renderQrcode
                v-if="deviceDetail?.deviceIdentification"
                :deviceIdentification="deviceDetail.deviceIdentification"
                :deviceName="deviceDetail.deviceName || '未知设备'"
              />
              <!-- 连接状态 chip -->
              <a-tag
                :color="getConnectStatusColor(deviceDetail?.connectStatus)"
                v-if="deviceDetail?.connectStatus != null"
              >
                {{ getDictLabel('LINK_DEVICE_CONNECT_STATUS', deviceDetail.connectStatus, '-') }}
              </a-tag>
              <!-- 节点类型 chip -->
              <a-tag
                :color="getNodeTypeColor(deviceDetail?.nodeType)"
                v-if="deviceDetail?.nodeType != null"
              >
                {{ getDictLabel('LINK_DEVICE_NODE_TYPE', deviceDetail.nodeType, '-') }}
              </a-tag>
              <!-- 设备启用状态 chip -->
              <a-tag
                :color="deviceDetail?.deviceStatus === 1 ? 'success' : 'default'"
                v-if="deviceDetail?.deviceStatus != null"
              >
                {{ getDictLabel('LINK_DEVICE_STATUS', deviceDetail.deviceStatus, '-') }}
              </a-tag>
            </div>
            <div class="meta-line">
              <span>
                <NumberOutlined />
                {{ t('iot.link.device.device.deviceIdentification') }}:
                {{ deviceDetail?.deviceIdentification || '-' }}
              </span>
              <a-divider v-if="deviceDetail?.appId" type="vertical" />
              <span v-if="deviceDetail?.appId">
                <AppstoreOutlined />
                {{ t('iot.link.device.device.appId') }}: {{ deviceDetail.appId }}
              </span>
              <a-divider v-if="deviceDetail?.clientId" type="vertical" />
              <span v-if="deviceDetail?.clientId" class="client-id">
                <IdcardOutlined />
                <span class="label">{{ t('iot.link.device.device.clientId') }}:</span>
                <span class="value">{{ deviceDetail.clientId }}</span>
              </span>
              <!-- 子设备:展示所属网关 -->
              <a-divider
                v-if="
                  deviceDetail?.nodeType === DeviceNodeType.SUB_DEVICE && deviceDetail?.gatewayId
                "
                type="vertical"
              />
              <span
                v-if="
                  deviceDetail?.nodeType === DeviceNodeType.SUB_DEVICE && deviceDetail?.gatewayId
                "
                class="gateway-info"
              >
                <BranchesOutlined />
                {{ t('iot.link.device.device.affiliatedGateway') }}:
                <template v-if="gatewayDeviceDetail">
                  <span class="gateway-name">{{ gatewayDeviceDetail.deviceName }}</span>
                  <span class="gateway-id" ref="textToCopy">({{ deviceDetail.gatewayId }})</span>
                </template>
                <template v-else>
                  <span class="gateway-id" ref="textToCopy">{{ deviceDetail.gatewayId }}</span>
                  <span class="gateway-deleted"
                    >({{ t('iot.link.device.device.gatewayDeleted') }})</span
                  >
                </template>
                <a-tooltip placement="top" :title="t('common.title.copy')">
                  <span class="copy_btn" @click="handleCopyText">
                    <SvgIcon name="copy" :size="12" />
                  </span>
                </a-tooltip>
              </span>
            </div>
          </div>
        </div>
        <a-space>
          <a-button @click="load">
            <template #icon><ReloadOutlined /></template>
            {{ t('common.title.refresh') }}
          </a-button>
          <a-button
            type="primary"
            danger
            :disabled="deviceDetail?.connectStatus !== DeviceConnectStatus.ONLINE"
            v-if="deviceDetail.nodeType !== DeviceNodeType.SUB_DEVICE"
            @click="handleDisconnect"
          >
            <template #icon><DisconnectOutlined /></template>
            {{ t('iot.link.device.device.disconnect') }}
          </a-button>
          <a-button type="primary" @click="handleEdit">
            <template #icon><EditOutlined /></template>
            {{ t('iot.link.device.device.updateDeviceButton') }}
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!--
      ===== 4 指标卡片(顶部,只放"用户最关注的运维指标") =====

      设计原则:
      - 卡片是首屏视觉锚,只放需要"扫一眼就能判断设备健康度"的关键指标
      - 长 URL / 可能为空的字段不放这里 ── 它们的归属应该是详情区
      - 4 个指标:连接状态 / 设备类型 / 所属产品 / 固件版本(运维最常看)
      - connector / boundProductVersionNo 下沉到基本信息 Tab,避免卡片高度不齐 + 长 URL 截断
    -->
    <a-row :gutter="16" class="metric-row">
      <!-- 1. 连接状态(必看) + 最后心跳时间(辅助判断"是不是真在线") -->
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div
            class="metric-icon connect"
            :class="{ on: deviceDetail?.connectStatus === DeviceConnectStatus.ONLINE }"
          >
            <ApiOutlined />
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.device.device.status') }}</div>
            <div class="metric-value">
              {{ getDictLabel('LINK_DEVICE_CONNECT_STATUS', deviceDetail?.connectStatus, '-') }}
            </div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.device.device.lastHeartbeatTime') }}</span>
              <a-tooltip :title="deviceDetail?.lastHeartbeatTime || ''">
                <span class="sub-val">{{ deviceDetail?.lastHeartbeatTime || '-' }}</span>
              </a-tooltip>
            </div>
          </div>
        </a-card>
      </a-col>
      <!-- 2. 节点类型 + 认证方式 -->
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon node"><AppstoreOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.device.device.nodeType') }}</div>
            <div class="metric-value">
              {{ getDictLabel('LINK_DEVICE_NODE_TYPE', deviceDetail?.nodeType, '-') }}
            </div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.device.device.authMode') }}</span>
              <span class="sub-val">
                {{ getDictLabel('LINK_DEVICE_AUTH_MODE', deviceDetail?.authMode, '-') }}
              </span>
            </div>
          </div>
        </a-card>
      </a-col>
      <!-- 3. 所属产品名 + 产品标识(可点击跳转产品详情) -->
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon product"><DatabaseOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.product.product.productName') }}</div>
            <a-tooltip :title="deviceDetail?.productResultVO?.productName || ''">
              <div class="metric-value">
                <span
                  v-if="deviceDetail?.productResultVO?.id"
                  class="product-link"
                  @click="handleGoToProductDetail"
                >
                  {{ deviceDetail?.productResultVO?.productName || '-' }}
                </span>
                <span v-else>{{ deviceDetail?.productResultVO?.productName || '-' }}</span>
              </div>
            </a-tooltip>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.product.product.productIdentification') }}</span>
              <a-tooltip :title="deviceDetail?.productIdentification || ''">
                <span class="sub-val">{{ deviceDetail?.productIdentification || '-' }}</span>
              </a-tooltip>
            </div>
          </div>
        </a-card>
      </a-col>
      <!-- 4. 版本兼容性:固件 / 软件 / SDK 三版本统一 chip 呈现(运维定位故障 + 兼容性核对常用) -->
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon version"><RocketOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.device.device.versionInfo') }}</div>
            <div class="version-grid">
              <div class="ver-row">
                <span class="ver-key">{{ t('iot.link.device.device.fwVersion') }}</span>
                <a-tooltip :title="deviceDetail?.fwVersion || ''">
                  <span class="ver-chip" :class="{ 'is-empty': !deviceDetail?.fwVersion }">
                    {{ deviceDetail?.fwVersion || '—' }}
                  </span>
                </a-tooltip>
              </div>
              <div class="ver-row">
                <span class="ver-key">{{ t('iot.link.device.device.swVersion') }}</span>
                <a-tooltip :title="deviceDetail?.swVersion || ''">
                  <span class="ver-chip" :class="{ 'is-empty': !deviceDetail?.swVersion }">
                    {{ deviceDetail?.swVersion || '—' }}
                  </span>
                </a-tooltip>
              </div>
              <div class="ver-row">
                <span class="ver-key">{{ t('iot.link.device.device.deviceSdkVersion') }}</span>
                <a-tooltip :title="deviceDetail?.deviceSdkVersion || ''">
                  <span class="ver-chip" :class="{ 'is-empty': !deviceDetail?.deviceSdkVersion }">
                    {{ deviceDetail?.deviceSdkVersion || '—' }}
                  </span>
                </a-tooltip>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- ===== Tabs 主体(Flexy panel-card) ===== -->
    <a-card
      v-if="deviceDetail?.deviceIdentification && cardTabList.length"
      :bordered="false"
      class="panel-card"
    >
      <a-tabs default-active-key="0" v-model:activeKey="currentKey" size="small">
        <a-tab-pane v-for="item in cardTabList" :tab="item.name" :key="item.key" />
      </a-tabs>
      <div class="tab-content">
        <basicInfo v-if="currentKey === '0'" :deviceDetail="deviceDetail" />
        <location
          v-else-if="currentKey === '1'"
          v-hasAnyPermission="['link:device:device:detail:location']"
          :deviceIdentification="deviceDetail.deviceIdentification"
          :deviceLocationResultVO="deviceDetail.deviceLocationResultVO"
          @success="handleSuccess"
        />
        <topic
          v-else-if="currentKey === '2'"
          :nodeType="deviceDetail?.nodeType"
          :deviceIdentification="
            deviceDetail?.nodeType === DeviceNodeType.SUB_DEVICE
              ? deviceDetail.gatewayId
              : deviceDetail.deviceIdentification
          "
          :deviceSdkVersion="deviceDetail.deviceSdkVersion"
          :productIdentification="deviceDetail.productIdentification"
          :appId="deviceDetail.appId"
          :userName="deviceDetail.userName"
        />
        <action
          v-else-if="currentKey === '3'"
          :deviceIdentification="deviceDetail.deviceIdentification"
        />
        <running
          ref="runningRef"
          v-else-if="currentKey === '4'"
          :deviceIdentification="deviceDetail.deviceIdentification"
          :boundProductVersionNo="deviceDetail.boundProductVersionNo"
          :productResultVO="deviceDetail.productResultVO"
        />
        <command
          v-else-if="currentKey === '5'"
          :deviceIdentification="deviceDetail.deviceIdentification"
        />
        <subDevice
          v-else-if="currentKey === '6'"
          :deviceIdentification="deviceDetail.deviceIdentification"
        />
        <deviceGroup
          v-else-if="currentKey === '7'"
          :deviceIdentification="deviceDetail.deviceIdentification"
        />
      </div>
    </a-card>
    <EditModal @register="registerModal" @success="handleSuccess" />
    <CopyModal @register="registerCopyModal" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, onMounted, watch, nextTick, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { detailBydeviceIdentification, disconnect } from '/@/api/iot/link/device/device';
  import { PageWrapper } from '/@/components/Page';
  import { Card, Tabs, Button, Tooltip, Tag, Space, Row, Col, Divider } from 'ant-design-vue';
  import {
    EditOutlined,
    DisconnectOutlined,
    ReloadOutlined,
    AppstoreOutlined,
    NumberOutlined,
    IdcardOutlined,
    BranchesOutlined,
    ApiOutlined,
    DatabaseOutlined,
    RocketOutlined,
  } from '@ant-design/icons-vue';
  import { getDeviceNodeTypeSvg } from '/@/components/iot/svg';
  import action from '../detail/deviceAction.vue';
  import topic from '../detail/deviceTopic.vue';
  import running from '../running/index.vue';
  import location from '../detail/deviceLocation.vue';
  import command from '../detail/deviceCommand.vue';
  import subDevice from '../detail/subDeviceList.vue';
  import deviceGroup from '../detail/deviceGroupList.vue';
  import basicInfo from '../detail/basicInfo.vue';
  import { useDict } from '/@/components/Dict';
  import EditModal from './Edit.vue';
  import { useModal } from '/@/components/Modal';
  import CopyModal from '/@/components/CopyModal/index.vue';
  import ImageDisplay from '/@/components/ImageDisplay/index.ts';
  const { getDictLabel } = useDict();
  import { ActionEnum } from '/@/enums/commonEnum';
  import { DeviceConnectStatus, DeviceNodeType } from '/@/enums/link/device';
  import type { DevicePageQuery } from '/@/api/iot/link/device/model/deviceModel';
  import SvgIcon from '/@/components/Icon/src/SvgIcon.vue';
  import renderQrcode from '../qrcode/index.vue';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common.tsx';
  import { usePermission } from '/@/hooks/web/usePermission';

  export default defineComponent({
    name: '设备详情',
    components: {
      ACard: Card,
      ATag: Tag,
      ASpace: Space,
      ARow: Row,
      ACol: Col,
      ADivider: Divider,
      [Tabs.name]: Tabs,
      [Tabs.TabPane.name]: Tabs.TabPane,
      PageWrapper,
      topic,
      action,
      command,
      subDevice,
      deviceGroup,
      EditModal,
      AButton: Button,
      EditOutlined,
      DisconnectOutlined,
      ReloadOutlined,
      AppstoreOutlined,
      NumberOutlined,
      IdcardOutlined,
      BranchesOutlined,
      ApiOutlined,
      DatabaseOutlined,
      RocketOutlined,
      location,
      running,
      SvgIcon,
      Tooltip,
      renderQrcode,
      basicInfo,
      CopyModal,
      ImageDisplay,
    },
    emits: ['success', 'register'],
    setup() {
      const textToCopy = ref(null);
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const router = useRouter();
      const { currentRoute } = router;
      const { isPermission } = usePermission();
      const { setTitle } = useTabs();

      let deviceDetail = reactive<DevicePageQuery>({});
      const [registerModal, { openModal }] = useModal();
      const [registerCopyModal] = useModal();
      // 路由 :id 段携带的是设备的 deviceIdentification(业务唯一标识,非主键 id),
      // 详情查询统一走 deviceIdentification,避免依赖内部主键
      const deviceIdentification = ref('');
      // 子设备的父网关详情:仅展示用,网关被删时容错为 null
      const gatewayDeviceDetail = ref<any>(null);
      let currentKey = ref<string | null>('0');

      const runningRef = ref(null);
      watch(currentKey, (val) => {
        if (val !== '4') {
          runningRef.value?.closeShadowSocket?.();
        }
      });

      onMounted(() => {
        const { params } = currentRoute.value;
        // 路由 params.id 历史命名遗留,实际值是设备的 deviceIdentification
        deviceIdentification.value = params.id as string;
        load();

        if (cardTabList.value?.length) {
          if (cardTabList.value.every((i) => i.key !== '0')) {
            currentKey.value = cardTabList.value?.[0].key;
          }
        } else {
          currentKey.value = null;
        }
      });

      const load = async () => {
        const res = await detailBydeviceIdentification(deviceIdentification.value);
        Object.assign(deviceDetail, res);
        setTitle(deviceDetail.deviceName);
        // 子设备额外加载父网关名(用于展示"所属网关",网关被删时优雅降级为只显示 ID)
        if (deviceDetail.nodeType === DeviceNodeType.SUB_DEVICE && deviceDetail.gatewayId) {
          try {
            gatewayDeviceDetail.value = await detailBydeviceIdentification(deviceDetail.gatewayId);
          } catch (e) {
            // 网关已被删除 / 无权限:仅展示 ID 即可,不影响当前设备详情页
            gatewayDeviceDetail.value = null;
            console.warn('[device-detail] 父网关查询失败(可能已删除)', deviceDetail.gatewayId, e);
          }
        } else {
          gatewayDeviceDetail.value = null;
        }
        await nextTick();
      };

      // 弹出编辑页面
      function handleEdit(e?: Event) {
        e?.stopPropagation();
        openModal(true, {
          record: deviceDetail,
          type: ActionEnum.EDIT,
        });
      }

      // 断开连接
      function handleDisconnect(e?: Event) {
        e?.stopPropagation();
        createConfirm({
          iconType: 'warning',
          content: '是否确定断开连接？',
          onOk: async () => {
            try {
              await disconnect(deviceDetail.deviceIdentification || '');
              createMessage.success('断开连接成功！');
              load();
            } catch (e) {}
          },
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        deviceDetail.deviceIdentification = '';
        load();
      }

      const handleCopyText = async () => {
        const text = (textToCopy.value as any)?.innerText;
        handleCopyTextV2(text || '');
      };

      // 跳转到产品详情页
      const handleGoToProductDetail = () => {
        if (!deviceDetail.productResultVO?.id) return;
        router.replace({
          name: '产品详情',
          params: { id: deviceDetail.productResultVO.id },
        });
      };

      /** 连接状态 Tag 颜色:ONLINE 绿 / OFFLINE 红 / UNCONNECTED 默认 */
      function getConnectStatusColor(connectStatus?: any): string {
        const v = Number(connectStatus);
        if (v === DeviceConnectStatus.ONLINE) return 'success';
        if (v === DeviceConnectStatus.OFFLINE) return 'error';
        return 'default';
      }

      /** 节点类型 Tag 颜色 ── 0=普通 蓝 / 1=网关 紫 / 2=子设备 青 */
      function getNodeTypeColor(nodeType?: any): string {
        const v = Number(nodeType);
        if (v === 1) return 'purple';
        if (v === 2) return 'cyan';
        return 'blue';
      }

      const cardTabList = computed(() => {
        const list = [
          {
            name: t('iot.link.device.device.tabs[0]'),
            key: '0',
            isShowAuth: isPermission(['link:device:device:detail:basicInfo']),
          },
          {
            name: t('iot.link.device.device.tabs[1]'),
            key: '1',
            isShowAuth: isPermission(['link:device:device:detail:location']),
          },
          {
            name: t('iot.link.device.device.tabs[2]'),
            key: '2',
            isShowAuth: isPermission(['link:device:device:detail:topic']),
          },
          {
            name: t('iot.link.device.device.tabs[3]'),
            key: '3',
            isShowAuth: isPermission(['link:device:device:detail:action']),
          },
          {
            name: t('iot.link.device.device.tabs[4]'),
            key: '4',
            isShowAuth: isPermission(['link:device:device:detail:shadow']),
          },
          {
            name: t('iot.link.device.device.tabs[5]'),
            key: '5',
            isShowAuth: isPermission(['link:device:device:detail:command']),
          },
          {
            name: t('iot.link.device.device.tabs[6]'),
            key: '6',
            isShowAuth:
              isPermission(['link:device:device:detail:subdevice']) &&
              deviceDetail.nodeType === DeviceNodeType.GATEWAY,
          },
          {
            name: t('iot.link.device.device.tabs[7]'),
            key: '7',
            isShowAuth: isPermission(['link:device:device:detail:shadow']),
          },
        ];
        return list.filter((i) => i.isShowAuth);
      });

      return {
        t,
        deviceDetail,
        gatewayDeviceDetail,
        currentKey,
        getDictLabel,
        registerModal,
        registerCopyModal,
        handleEdit,
        handleSuccess,
        handleCopyText,
        textToCopy,
        isPermission,
        cardTabList,
        handleDisconnect,
        runningRef,
        handleGoToProductDetail,
        getDeviceNodeTypeSvg,
        getConnectStatusColor,
        getNodeTypeColor,
        load,
        // 枚举值
        DeviceConnectStatus,
        DeviceNodeType,
      };
    },
  });
</script>
<style lang="less" scoped>
  /* Flexy 风格,完全对齐产品详情(/views/iot/link/product/detail/index.vue) */
  .device-detail {
    :deep(.ant-card) {
      border-radius: 14px;
      box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
    }
  }

  .header-card {
    margin: 16px 16px 0;

    :deep(.ant-card-body) {
      padding: 20px 24px;
    }
  }

  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;
    min-width: 0;
  }

  .rule-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    background: linear-gradient(135deg, #eef2ff 0%, #e8f4fd 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
    box-shadow: 0 4px 12px rgba(93, 135, 255, 0.1);
    flex-shrink: 0;
    overflow: hidden;

    /* SVG 自带渐变背景,撤掉外层 padding 让其充满 */
    &.no-pad {
      padding: 0;
      background: transparent;
    }

    :deep(svg) {
      width: 100%;
      height: 100%;
    }
  }

  .rule-meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 0;
  }

  .rule-title {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;

    .name-text {
      font-size: 18px;
      font-weight: 600;
      color: #2a3547;
    }
  }

  .meta-line {
    color: #6b7280;
    font-size: 13px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }

    /* 客户端标识 ── 旧版用 UserOutlined 太通用看不出是什么;改 IdcardOutlined 并显式带"客户端标识:"前缀 */
    .client-id {
      .label {
        color: #97a1b0;
      }

      .value {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        color: #2a3547;
        font-weight: 500;
      }

      :deep(.anticon) {
        color: #5d87ff;
      }
    }

    .gateway-info {
      .gateway-name {
        color: #2a3547;
        font-weight: 500;
        margin-left: 4px;
      }

      .gateway-id {
        color: #8c97a5;
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        margin-left: 2px;
      }

      .gateway-deleted {
        color: #d03b5b;
        font-weight: 500;
      }

      .copy_btn {
        cursor: pointer;
        color: #5d87ff;
        margin-left: 4px;

        &:hover {
          color: #2952cc;
        }
      }
    }
  }

  /* ===== 4 指标卡片 ===== */
  .metric-row {
    margin: 16px 16px 0 !important;
  }

  /* 4 张卡片必须等高:
     - a-row 默认 stretch col,但 ant-card 自身不撑满 col 高度 → 显式 height:100%
     - .ant-card-body 也要 100%,否则卡片高但 body 不高
     - 旧版 .sub-val 用 word-break:break-all,长 connector 文本如
       "broker.thinglinks.mqttsnet.com:11883" 会竖向堆叠撑高第 1 张卡,
       造成 4 张大小不一 ── 改 nowrap + ellipsis + tooltip 兜底 */
  .metric-card {
    height: 100%;

    :deep(.ant-card-body) {
      padding: 16px 18px;
      display: flex;
      align-items: center;
      gap: 14px;
      height: 100%;
      min-height: 88px;
    }
  }

  .metric-icon {
    width: 52px;
    height: 52px;
    border-radius: 14px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #fff;
    flex-shrink: 0;

    &.connect {
      background: linear-gradient(135deg, #9b75e6 0%, #b095f0 100%);
      box-shadow: 0 6px 14px rgba(155, 117, 230, 0.35);

      &.on {
        background: linear-gradient(135deg, #13deb9 0%, #36e6c3 100%);
        box-shadow: 0 6px 14px rgba(19, 222, 185, 0.35);
      }
    }

    &.node {
      background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
      box-shadow: 0 6px 14px rgba(93, 135, 255, 0.35);
    }

    &.product {
      background: linear-gradient(135deg, #ffae1f 0%, #ffc94a 100%);
      box-shadow: 0 6px 14px rgba(255, 174, 31, 0.35);
    }

    &.version {
      background: linear-gradient(135deg, #fa896b 0%, #ff6a4a 100%);
    }
  }

  .metric-body {
    flex: 1;
    min-width: 0;
  }

  .metric-label {
    font-size: 12px;
    color: #97a1b0;
    text-transform: uppercase;
    letter-spacing: 0.3px;
    margin-bottom: 2px;
  }

  .metric-value {
    font-size: 18px;
    font-weight: 700;
    color: #2a3547;
    font-variant-numeric: tabular-nums;
    line-height: 1.2;
    /* 单行 ellipsis,防止长产品名 / 长版本号撑高卡片;原 word-break: break-all 是元凶 */
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    &--mono {
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 14px;
      letter-spacing: 0.2px;
    }

    .product-link {
      color: #5d87ff;
      cursor: pointer;
      transition: color 0.18s ease;

      &:hover {
        color: #2952cc;
        text-decoration: underline;
      }
    }
  }

  /* 副信息行(connector / authMode / productIdentification / fwVersion):
     一行不换行 + 省略 ── 防止 connector 长 URL 撑高卡片导致 4 张大小不一 */
  .metric-sub {
    margin-top: 4px;
    font-size: 12px;
    color: #6b7280;
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 4px;
    min-width: 0;

    .sub-key {
      color: #97a1b0;
      flex-shrink: 0;
    }

    .sub-val {
      color: #2a3547;
      font-weight: 600;
      font-variant-numeric: tabular-nums;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      min-width: 0;
      flex: 1;
    }
  }

  /* 版本兼容性网格:固件 / 软件 / SDK 三版本统一 key + chip 呈现,语义一致、空值降级 */
  .version-grid {
    margin-top: 6px;
    display: flex;
    flex-direction: column;
    gap: 6px;

    .ver-row {
      display: flex;
      align-items: center;
      gap: 8px;
      min-width: 0;

      .ver-key {
        flex-shrink: 0;
        width: 56px;
        font-size: 12px;
        color: #97a1b0;
      }

      .ver-chip {
        flex: 1;
        min-width: 0;
        padding: 1px 8px;
        border-radius: 6px;
        background: #f2f4f8;
        color: #2a3547;
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 12px;
        font-weight: 600;
        letter-spacing: 0.2px;
        line-height: 18px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;

        &.is-empty {
          color: #b6bdc8;
          font-weight: 400;
          background: #f7f8fa;
        }
      }
    }
  }

  /* ===== Tabs 主体 ===== */
  .panel-card {
    margin: 16px;

    :deep(.ant-card-body) {
      padding: 0;
    }

    :deep(.ant-tabs) {
      padding: 0 16px;
    }

    :deep(.ant-tabs-nav) {
      margin: 0 0 0 0;

      &::before {
        border-bottom: 1px solid #f0f2f5;
      }
    }
  }

  .tab-content {
    padding: 16px 18px 20px;
    /* 内容区只控制 overflow-x,垂直滚动交给父级 PageWrapper(contentFullHeight 已自带),
       避免出现"内容区一个滚动条 + 外层一个"的双滚动条问题 */
    overflow-x: auto;
  }
</style>
