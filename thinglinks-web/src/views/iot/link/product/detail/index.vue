<template>
  <PageWrapper contentFullHeight class="product-detail">
    <!-- ===== 顶部 Header(flexy 风格,同 bridge/detail) ===== -->
    <a-card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="rule-icon" :class="{ 'no-pad': !productDetail?.icon }">
            <ThumbUrl
              v-if="productDetail?.icon"
              :fileId="productDetail.icon"
              :imageStyle="{ 'max-width': '40px', 'max-height': '40px' }"
            />
            <component v-else :is="getProductTypeSvg(productDetail?.productType)" />
          </div>
          <div class="rule-meta">
            <div class="rule-title">
              <span class="name-text">{{ productDetail?.productName || '-' }}</span>
              <a-tag :color="productDetail?.productStatus == 0 ? 'success' : 'default'">
                {{ getDictLabel('LINK_PRODUCT_STATUS', productDetail?.productStatus, '-') }}
              </a-tag>
              <a-tag :color="getProductTypeColor(productDetail?.productType)">
                {{ getDictLabel('LINK_PRODUCT_TYPE', productDetail?.productType, '-') }}
              </a-tag>
              <!-- 当前生效版本(语义化 chip:左侧蓝色 icon + "当前" label + 完整版本号) -->
              <a-tooltip
                v-if="productDetail?.activeVersionNo"
                :title="t('iot.link.product.product.activeVersionNo')"
              >
                <span class="ver-chip ver-chip--current">
                  <CheckCircleFilled class="ver-chip-icon" />
                  <span class="ver-chip-label">{{
                    t('iot.link.product.product.versionTag.current')
                  }}</span>
                  <code class="ver-chip-no">{{ productDetail.activeVersionNo }}</code>
                </span>
              </a-tooltip>
              <!-- 灰度发布中才有值:被切换前的全量版本号(=current 时隐藏避免视觉重复) -->
              <a-tooltip
                v-if="
                  productDetail?.previousFullVersionNo &&
                  productDetail.previousFullVersionNo !== productDetail.activeVersionNo
                "
                :title="t('iot.link.product.product.previousFullVersionNo')"
              >
                <span class="ver-chip ver-chip--previous">
                  <HistoryOutlined class="ver-chip-icon" />
                  <span class="ver-chip-label">{{
                    t('iot.link.product.product.versionTag.previous')
                  }}</span>
                  <code class="ver-chip-no">{{ productDetail.previousFullVersionNo }}</code>
                </span>
              </a-tooltip>
            </div>
            <div class="meta-line">
              <span>
                <NumberOutlined />
                {{ t('iot.link.product.product.productIdentification') }}:
                {{ productDetail?.productIdentification || '-' }}
              </span>
              <a-divider type="vertical" />
              <span v-if="productDetail?.appId">
                <AppstoreOutlined />
                {{ t('iot.link.product.product.appId') }}: {{ productDetail.appId }}
              </span>
              <a-divider v-if="productDetail?.manufacturerName" type="vertical" />
              <span v-if="productDetail?.manufacturerName">
                <ShopOutlined />
                {{ productDetail.manufacturerName }}
              </span>
              <a-divider v-if="productDetail?.model" type="vertical" />
              <span v-if="productDetail?.model">
                <BarcodeOutlined />
                {{ productDetail.model }}
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
            @click="handleEdit"
            v-hasAnyPermission="['link:product:product:edit']"
          >
            <template #icon><EditOutlined /></template>
            {{ t('iot.link.product.product.updateProductButton') }}
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- ===== 4 指标卡片 ===== -->
    <a-row :gutter="16" class="metric-row">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon type"><AppstoreOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.product.product.productType') }}</div>
            <div class="metric-value">
              {{ getDictLabel('LINK_PRODUCT_TYPE', productDetail?.productType, '-') }}
            </div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.product.product.appId') }}</span>
              <span class="sub-val">{{ productDetail?.appId || '-' }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon proto"><ApiOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.product.product.protocolType') }}</div>
            <div class="metric-value">{{ productDetail?.protocolType || '-' }}</div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.product.product.dataFormat') }}</span>
              <span class="sub-val">{{ productDetail?.dataFormat || '-' }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon ver"><RocketOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.product.product.activeVersionNo') }}</div>
            <!-- 与其他 metric 卡(产品类型 / 协议类型 / 状态)样式一致:纯文本大字,不带复制按钮 -->
            <div class="metric-value metric-value--mono">
              <template v-if="productDetail?.activeVersionNo">{{
                productDetail.activeVersionNo
              }}</template>
              <span v-else class="never-pub">
                {{ t('iot.link.product.versionOverview.neverPublished') }}
              </span>
            </div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.product.product.manufacturerName') }}</span>
              <span class="sub-val">{{ productDetail?.manufacturerName || '-' }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon enable" :class="{ on: productDetail?.productStatus == 0 }">
            <PoweroffOutlined />
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.product.product.productStatus') }}</div>
            <div class="metric-value">
              {{ getDictLabel('LINK_PRODUCT_STATUS', productDetail?.productStatus, '-') }}
            </div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.product.product.deviceType') }}</span>
              <span class="sub-val">{{ productDetail?.deviceType || '-' }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- ===== Tabs 主体 ===== -->
    <a-card v-if="productDetail?.id" :bordered="false" class="panel-card">
      <a-tabs default-active-key="1" v-model:activeKey="currentKey" size="small">
        <a-tab-pane v-for="item in cardTabList" :tab="item.name" :key="item.key" />
      </a-tabs>
      <basicInfo v-if="currentKey == '0'" :productDetail="productDetail" />
      <modelDefinition v-else-if="currentKey == '1'" :id="productDetail?.id" />
      <topicList v-else-if="currentKey == '2'" :id="productDetail?.productIdentification" />
      <deviceDebug v-else-if="currentKey == '4'" :productIdentification="productDetail.productIdentification" />
      <publishRecord v-else-if="currentKey == '5'" :productIdentification="productDetail.productIdentification" />
      <versionList v-else-if="currentKey == '6'" :productDetail="productDetail" @refresh="load" />
      <changeLog v-else-if="currentKey == '7'" :productIdentification="productDetail.productIdentification" />
    </a-card>
    <EditModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, onMounted, computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { detail } from '/@/api/iot/link/product/product';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { PageWrapper } from '/@/components/Page';
  import { useRouter } from 'vue-router';
  import { Card, Descriptions, Tabs, Button } from 'ant-design-vue';
  import {
    EditOutlined,
    ReloadOutlined,
    AppstoreOutlined,
    BarcodeOutlined,
    NumberOutlined,
    PoweroffOutlined,
    RocketOutlined,
    ShopOutlined,
    ApiOutlined,
    BranchesOutlined,
    CheckCircleFilled,
    HistoryOutlined,
  } from '@ant-design/icons-vue';
  /** 卡片 cardImage / 详情 header 占位 SVG ── 与桥接规则同款 flexy 风格,见 /components/iot/svg。 */
  import { getProductTypeSvg } from '/@/components/iot/svg';
  import EditModal from '../product/Edit.vue';
  import { useModal } from '/@/components/Modal';
  import { ActionEnum } from '/@/enums/commonEnum';
  import tsImg from '/@/assets/images/iot/link/device/ts.jpg';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { useDict } from '/@/components/Dict';
  import basicInfo from './basicInfo.vue';
  import modelDefinition from './modelDefinition.vue';
  import topicList from './topicList.vue';
  import deviceDebug from './deviceDebug.vue';
  import publishRecord from './publishRecord.vue';
  import versionList from './versionList.vue';
  import changeLog from './changeLog.vue';
  import ThumbUrl from '/@/components/Upload/src/ThumbUrl.vue';
  import type { ProductResultVO } from '/@/api/iot/link/product/model/productModel';
  const { getDictLabel } = useDict();
  export default defineComponent({
    name: 'ProductDetail',
    components: {
      ACard: Card,
      [Descriptions.name]: Descriptions,
      [Descriptions.Item.name]: Descriptions.Item,
      [Tabs.name]: Tabs,
      [Tabs.TabPane.name]: Tabs.TabPane,
      PageWrapper,
      deviceDebug,
      topicList,
      modelDefinition,
      basicInfo,
      publishRecord,
      versionList,
      changeLog,
      AButton: Button,
      EditModal,
      ThumbUrl,
      // Options API 下图标组件必须显式注册,否则模板里 <XxxOutlined /> 不渲染
      EditOutlined,
      ReloadOutlined,
      AppstoreOutlined,
      BarcodeOutlined,
      NumberOutlined,
      PoweroffOutlined,
      RocketOutlined,
      ShopOutlined,
      ApiOutlined,
      BranchesOutlined,
      CheckCircleFilled,
      HistoryOutlined,
    },
    emits: ['success', 'register'],
    setup() {
      // 是否显示密码明文
      const { t } = useI18n();
      const { createMessage } = useMessage();
      const { currentRoute } = useRouter();
      const [registerModal, { openModal }] = useModal();
      const { isPermission } = usePermission();
      let productDetail = reactive<ProductResultVO>({} as ProductResultVO);
      let id = ref('');
      onMounted(() => {
        const { params, query } = currentRoute.value;
        id.value = params.id as string;
        load();
        const tabs = cardTabList.value;
        if (tabs?.length) {
          // 支持外部跳转携带 ?tab=N 直达指定页签(如版本总览页「发布管理」入口)
          const wantedTab = query?.tab as string | undefined;
          const hasWanted = !!wantedTab && tabs.some((tabItem) => tabItem.key === wantedTab);
          currentKey.value = hasWanted ? (wantedTab as string) : tabs[0].key;
        } else {
          currentKey.value = null;
        }
      });
      const load = async () => {
        const res = await detail(id.value);

        productDetail = Object.assign(productDetail, res);
      };
      // 类型显式 string | null:onMounted 里无权限 tab 时会赋 null
      let currentKey = ref<string | null>('0');
      function copyFn(text) {
        let result = copyTextToClipboard(text);
        console.log(result, 'result');
        if (result) {
          createMessage.success(t('common.tips.copySuccess'));
        } else {
          createMessage.warning(t('common.tips.copyFail'));
        }
      }

      // 弹出编辑页面
      function handleEdit(e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record: productDetail,
          type: ActionEnum.EDIT,
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        load();
      }

      /** 产品类型 tag 颜色:0=未知 / 1=网关 / 2=普通 */
      function getProductTypeColor(productType?: any): string {
        const v = Number(productType);
        if (v === 1) return 'purple';   // 网关
        if (v === 2) return 'cyan';     // 普通
        return 'default';
      }

      const cardTabList = computed(() => {
        const list = [
          {
            name: t('iot.link.product.product.tab0'),
            key: '0',
            isShowAuth: isPermission(['link:product:detail:basicInfo']),
            component: basicInfo,
          },
          {
            name: t('iot.link.product.product.tab1'),
            key: '1',
            isShowAuth: isPermission(['link:product:detail:modelDefinition']),
            component: modelDefinition,
          },
          {
            name: t('iot.link.product.product.tab2'),
            key: '2',
            isShowAuth: isPermission(['link:product:detail:topic']),
            component: topicList,
          },
          {
            name: t('iot.link.product.product.tab4'),
            key: '4',
            isShowAuth: isPermission(['link:product:detail:debug']),
            component: deviceDebug,
          },
          {
            name: t('iot.link.product.product.tab6'),
            key: '6',
            isShowAuth: isPermission(['link:product:detail:versionList']),
            component: versionList,
          },
          {
            name: t('iot.link.product.product.tab5'),
            key: '5',
            isShowAuth: isPermission(['link:product:detail:publishRecord']),
            component: publishRecord,
          },
          {
            name: t('iot.link.product.product.tab7'),
            key: '7',
            isShowAuth: isPermission(['link:product:detail:changeLog']),
            component: changeLog,
          },
        ];
        return list.filter((i) => i.isShowAuth);
      });

      return {
        t,
        copyFn,
        productDetail,
        currentKey,
        getDictLabel,
        labelStyle: {
          width: '120px',
          'font-weight': '600',
          'font-size': '14px',
        },
        contentStyle: {
          'font-weight': '600',
          'font-size': '14px',
        },
        tsImg,
        load,
        handleSuccess,
        handleEdit,
        registerModal,
        cardTabList,
        getProductTypeColor,
        getProductTypeSvg,
      };
    },
  });
</script>
<style lang="less" scoped>
  /* flexy 风格,完全对齐 bridge/detail.vue 视觉规范 */
  .product-detail {
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

    /* 产品类型 SVG 已自带渐变背景 + 圆角,撤掉外层 padding 让其充满 */
    &.no-pad {
      padding: 0;
      background: transparent;
    }

    :deep(svg) {
      width: 100%;
      height: 100%;
    }

    .placeholder-icon {
      font-size: 28px;
      color: #5d87ff;
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

  /* ─── 顶部版本号 chip(语义化:左侧色块 icon + label + 完整版本号) ─── */
  /* 比单纯版本号徽章信息量大 ── 用户一眼能看出哪个是当前生效、哪个是上一版,不再"两个数字看着懵" */
  .ver-chip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 3px 10px 3px 4px;
    border-radius: 10px;
    font-size: 12px;
    line-height: 1.5;
    transition: all 0.18s ease;

    .ver-chip-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 20px;
      height: 20px;
      border-radius: 6px;
      font-size: 11px;
      color: #fff;
      flex-shrink: 0;
    }

    .ver-chip-label {
      font-weight: 600;
      letter-spacing: 0.2px;
    }

    .ver-chip-no {
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 11.5px;
      font-variant-numeric: tabular-nums;
      letter-spacing: 0.2px;
      user-select: text;
      padding: 0;
      background: transparent;
    }

    /* 当前生效 ── 蓝主色 */
    &--current {
      background: linear-gradient(135deg, #eef2ff 0%, #f0f7ff 100%);
      border: 1px solid rgba(93, 135, 255, 0.2);
      color: #2952cc;

      .ver-chip-icon {
        background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
        box-shadow: 0 3px 8px rgba(93, 135, 255, 0.35);
      }
    }

    /* 上一版 ── 暖橙调,代表"历史 / 回滚定位" */
    &--previous {
      background: linear-gradient(135deg, #fff7e6 0%, #fff3d6 100%);
      border: 1px solid rgba(255, 174, 31, 0.22);
      color: #b25e00;

      .ver-chip-icon {
        background: linear-gradient(135deg, #ffae1f 0%, #ffc94a 100%);
        box-shadow: 0 3px 8px rgba(255, 174, 31, 0.35);
      }
    }
  }

  .meta-line {
    color: #6b7280;
    font-size: 13px;

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }

  /* ===== 4 指标卡片 ===== */
  /* 对齐说明:
   * - header-card / panel-card 用 margin: 16px(左右 16);
   * - a-row 有 :gutter="16",antd 会自动给 row 加 margin-left/right: -8px,col 加 padding 8;
   * - 所以这里 margin: 16px 8px,8 + 8(antd 抵消) = 16,跟 header/panel 卡左边对齐。
   */
  .metric-row {
    margin: 16px 8px;
  }

  /* a-col 在 a-row(flex) 下默认等高;卡片 height 100% 撑满 col,4 张卡强制同高 */
  .metric-card {
    height: 100%;
    border-radius: 14px;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    cursor: default;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 24px rgba(15, 23, 42, 0.1);
    }

    :deep(.ant-card-body) {
      height: 100%;
      padding: 18px 20px;
      display: flex;
      align-items: center;
      gap: 14px;
    }
  }

  .metric-icon {
    width: 50px;
    height: 50px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: #fff;
    flex-shrink: 0;

    /* 渐变背景 + 同色柔影,白色图标 ── 高端统计卡风格 */
    &.type {
      background: linear-gradient(135deg, #49beff 0%, #2e90e8 100%);
      box-shadow: 0 6px 14px rgba(73, 190, 255, 0.4);
    }
    &.proto {
      background: linear-gradient(135deg, #5d87ff 0%, #3d5fe0 100%);
      box-shadow: 0 6px 14px rgba(93, 135, 255, 0.4);
    }
    &.ver {
      background: linear-gradient(135deg, #52c41a 0%, #36a50d 100%);
      box-shadow: 0 6px 14px rgba(82, 196, 26, 0.4);
    }
    &.enable {
      background: linear-gradient(135deg, #b0bac9 0%, #8c97a5 100%);
      box-shadow: 0 6px 14px rgba(160, 174, 192, 0.35);

      &.on {
        background: linear-gradient(135deg, #13deb9 0%, #07b894 100%);
        box-shadow: 0 6px 14px rgba(19, 222, 185, 0.4);
      }
    }
  }

  .metric-body {
    flex: 1;
    min-width: 0;
  }

  .metric-label {
    color: #8c97a5;
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 0.3px;
  }

  .metric-value {
    color: #2a3547;
    font-size: 19px;
    font-weight: 700;
    margin-top: 3px;
    line-height: 1.25;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;

    /* 等宽 + 略小字号 ── 适配 16 位长版本号,与其他卡片(产品类型 / 协议类型 / 状态)视觉对齐 */
    &--mono {
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 15px;
      font-variant-numeric: tabular-nums;
      letter-spacing: 0.2px;
    }

    .never-pub {
      color: #a0aec0;
      font-weight: 500;
      font-size: 13px;
    }
  }

  .metric-sub {
    font-size: 12px;
    margin-top: 4px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;

    /* 副信息标签(数据格式 / 厂商名称 / 设备类型 / 应用ID)── 浅灰小字 + 浅底标签 */
    .sub-key {
      display: inline-block;
      padding: 1px 6px;
      margin-right: 6px;
      border-radius: 4px;
      background: #f0f2f5;
      color: #8c97a5;
      font-size: 11px;
    }

    .sub-val {
      color: #6b7280;
    }
  }

  /* ===== Tabs 主体 ===== */
  /* tab 容器:固定高度让子 tab(changeLog / versionList / publishRecord 等)
   * 能 height:100% 撑出 scrollbar。
   * 之前子 tab 用 calc(100vh - 320px) 失效,根因是 panel-card 自身没有高度约束 ──
   * 子组件父级是 auto,子组件 calc/height 全部退化为 min-content,scrollbar 永远不出。
   * 这里给一个稍宽松的高度(viewport - 顶部 header/metric/tab bar 共 ~280px),
   * 内部 ant-card-body flex 把 tab bar 之后的剩余空间留给当前激活的 tab。 */
  .panel-card {
    margin: 0 16px 16px;
    height: calc(100vh - 280px);
    min-height: 520px;
    display: flex;
    flex-direction: column;

    /* ───── :deep 嵌套写法的坑 ─────
     * :deep(.x) { > .y { ... } } 会编译成 [data-v-hash] .x > .y,
     * 而 antd 渲染的 .x 没有 data-v-hash,选择器匹配失败!
     * 必须把整个深层选择器链一次性放入 :deep() 才能命中。
     */
    :deep(.ant-card-body) {
      padding: 12px 20px;
      flex: 1;
      min-height: 0;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    /* a-tabs 占自然高度 ── 单独 :deep,让 .ant-tabs 这种 antd 类被正确命中 */
    :deep(.ant-card-body > .ant-tabs) {
      flex-shrink: 0;
    }

    /* 隐藏 a-tab-pane 没传 default slot 时 antd 仍渲染的空 .ant-tabs-content-holder
     * (它在 flex column 父级下会撑高,挤掉下方真实 tab 组件 ── 用户看到的"上方大块空白" bug 根因) */
    :deep(.ant-card-body > .ant-tabs > .ant-tabs-content-holder) {
      display: none !important;
    }

    /* 当前激活的 tab 子组件(basicInfo / modelDefinition / topicList / 等):
     * 占满 ant-card-body 余下空间 + 自己内部独立滚。
     * 用 :not(.ant-tabs) 排除 a-tabs 自身(它已经 flex-shrink:0,占自然高度) */
    :deep(.ant-card-body > div:not(.ant-tabs)),
    :deep(.ant-card-body > section) {
      flex: 1;
      min-height: 0;
      overflow: hidden;
    }
  }
</style>