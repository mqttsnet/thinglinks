<template>
  <!--
    设备基本信息(Flexy 风格,完全对齐 product/detail/basicInfo.vue)。

    设计意图:
    - 主体分 5 组 ── 身份 / 产品&版本 / 协议&认证 / 厂商&固件 / 时间线;
      每组顶部一根色条 + 中文标题,内部用 grid 网格自适应列数
    - 侧栏:节点类型 SVG 缩略图(SubDevice/Gateway/Common),作为视觉锚点
    - 产品&版本组里 productIdentification + boundProductVersionNo 是
      灰度排查最关键的两个字段,用 snapshot chip + monospace 字体高亮
  -->
  <div class="basic-info">
    <!-- 主体:分组信息 -->
    <div class="info-main">
      <div v-for="group in groups" :key="group.key" class="info-group">
        <div class="group-title">
          <span class="title-bar" :style="{ background: group.color }"></span>
          {{ group.title }}
        </div>
        <div class="field-grid">
          <div
            v-for="field in group.fields"
            :key="field.label"
            class="field-item"
            :class="{ 'is-full': field.full }"
          >
            <div class="field-label">
              <span>{{ field.label }}</span>
              <a-tooltip v-if="field.tooltip" :title="field.tooltip">
                <QuestionCircleOutlined class="label-tip" />
              </a-tooltip>
            </div>
            <div class="field-value">
              <!-- 状态 chip(连接 / 启用 / 节点类型) -->
              <Tag v-if="field.kind === 'status'" :color="field.statusColor">
                {{ field.value || '-' }}
              </Tag>

              <!-- 版本快照 chip(产品当前版本 / 设备绑定版本) -->
              <SnapshotIdTag
                v-else-if="field.kind === 'snapshot' && field.value"
                :value="field.value"
                :color="field.snapshotColor"
              />

              <!-- 设备绑定版本为空时的友好提示 -->
              <span v-else-if="field.kind === 'snapshot' && !field.value" class="value-empty">
                <InfoCircleOutlined />
                {{ t('iot.link.device.device.boundProductVersionEmpty') }}
              </span>

              <!-- 版本绑定智能展示:一致 = 1 行 chip + ✓ / 不一致 = 2 行 + ⚠ -->
              <div v-else-if="field.kind === 'versionBinding'" class="version-binding">
                <template v-if="field.consistent">
                  <div class="version-row">
                    <SnapshotIdTag :value="field.boundValue" color="purple" />
                    <span class="hint hint-ok">
                      <CheckCircleFilled />
                      {{ t('iot.link.device.device.fieldTip.versionConsistent') }}
                    </span>
                  </div>
                </template>
                <template v-else>
                  <div class="version-row">
                    <span class="row-label">{{
                      t('iot.link.device.device.fieldTip.boundVersionLabel')
                    }}</span>
                    <SnapshotIdTag
                      v-if="field.boundValue"
                      :value="field.boundValue"
                      color="purple"
                    />
                    <span v-else class="value-empty">
                      <InfoCircleOutlined />
                      {{ t('iot.link.device.device.boundProductVersionEmpty') }}
                    </span>
                  </div>
                  <div class="version-row">
                    <span class="row-label">{{
                      t('iot.link.device.device.fieldTip.activeVersionLabel')
                    }}</span>
                    <SnapshotIdTag
                      v-if="field.activeValue"
                      :value="field.activeValue"
                      color="blue"
                    />
                    <span v-else class="value-empty">-</span>
                  </div>
                  <div class="hint hint-warn">
                    <WarningFilled />
                    {{ t('iot.link.device.device.fieldTip.versionInconsistent') }}
                  </div>
                </template>
              </div>

              <!-- 证书:仅 SSL 模式下展示富文本 -->
              <DeviceCertInfo v-else-if="field.kind === 'cert'" :serialNumber="field.value || ''" />

              <!-- 可复制字段(deviceIdentification / clientId / productIdentification 等) -->
              <CopyableText v-else-if="field.kind === 'copyable'" :text="field.value || ''" />

              <!-- 敏感字段 ── 默认脱敏 + 👁 切换明文 + 📋 复制 -->
              <SecretField v-else-if="field.kind === 'secret'" :modelValue="field.value" />

              <!-- 多标签 ── 后端逗号串拆成一排 Tag chip(对齐编辑表单的多标签输入) -->
              <template v-else-if="field.kind === 'tags'">
                <template v-if="field.tags && field.tags.length">
                  <Tag v-for="tag in field.tags" :key="tag" color="blue" class="tag-chip">
                    {{ tag }}
                  </Tag>
                </template>
                <span v-else class="value-text">-</span>
              </template>

              <!-- 默认:文本 -->
              <span v-else class="value-text" :class="{ mono: field.mono }">
                {{ field.value || '-' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 侧栏:节点类型缩略图(没有像产品那样的 icon 文件,改展示节点类型 SVG 作为视觉锚) -->
    <div class="info-aside">
      <div class="aside-card">
        <div class="aside-thumb">
          <component :is="getDeviceNodeTypeSvg(deviceDetail?.nodeType)" />
        </div>
        <div class="aside-label">
          {{ getDictLabel('LINK_DEVICE_NODE_TYPE', deviceDetail?.nodeType, '-') }}
        </div>
        <div class="aside-tip">
          {{ t('iot.link.device.device.basicInfoTip.identity') }}
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, PropType, computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { Tag } from 'ant-design-vue';
  import {
    InfoCircleOutlined,
    QuestionCircleOutlined,
    CheckCircleFilled,
    WarningFilled,
  } from '@ant-design/icons-vue';
  import { SnapshotIdTag, SecretField } from '/@/components/iot';
  import { getDeviceNodeTypeSvg } from '/@/components/iot/svg';
  import CopyableText from '/@/components/CopyableText';
  import DeviceCertInfo from '/@/components/Form/src/components/cacert/DeviceCertInfo.vue';
  import { useDict } from '/@/components/Dict';
  import { echoMapText } from '/@/utils/echo';
  import { DeviceAuthMode } from '/@/enums/link/device';
  import type { DevicePageQuery } from '/@/api/iot/link/device/model/deviceModel';

  const { getDictLabel } = useDict();

  /** 设备加密方式 ── 0 = 明文(不需要密钥 / 向量) */
  const ENCRYPT_METHOD_PLAINTEXT = 0;

  export default defineComponent({
    name: 'DeviceBasicInfo',
    components: {
      Tag,
      SnapshotIdTag,
      SecretField,
      CopyableText,
      DeviceCertInfo,
      InfoCircleOutlined,
      QuestionCircleOutlined,
      CheckCircleFilled,
      WarningFilled,
    },
    props: {
      deviceDetail: {
        type: Object as PropType<DevicePageQuery>,
        required: true,
      },
    },
    setup(props) {
      const { t } = useI18n();
      const D = (k: string) => t(`iot.link.device.device.${k}`);
      const P = (k: string) => t(`iot.link.product.product.${k}`);
      const TIP = (k: string) => t(`iot.link.device.device.fieldTip.${k}`);

      /** 节点类型 Tag 颜色 ── 0=普通 蓝 / 1=网关 紫 / 2=子设备 青 */
      const nodeTypeColor = (v: any): string => {
        const n = Number(v);
        if (n === 1) return 'purple';
        if (n === 2) return 'cyan';
        return 'blue';
      };

      /** 连接状态 Tag 颜色 ── ONLINE 绿 / OFFLINE 红 / UNCONNECTED 默认 */
      const connectStatusColor = (v: any): string => {
        const n = Number(v);
        if (n === 1) return 'success';
        if (n === 2) return 'error';
        return 'default';
      };

      /** 设备标签 ── 后端为逗号拼接字符串,拆成去空数组渲染成一排 Tag */
      const splitTags = (v?: string): string[] =>
        v
          ? String(v)
              .split(',')
              .map((s) => s.trim())
              .filter(Boolean)
          : [];

      const groups = computed(() => {
        const d = (props.deviceDetail || {}) as any;
        const prod = d.productResultVO || {};
        const isSsl = String(d.authMode ?? '') === String(DeviceAuthMode.SSL_TLS_CERTIFICATE);
        const isPlaintext = Number(d.encryptMethod ?? -1) === ENCRYPT_METHOD_PLAINTEXT;

        return [
          // ============ 设备身份 ============
          {
            key: 'identity',
            title: t('iot.link.device.device.basicInfoTip.identitySection'),
            color: '#5d87ff',
            fields: [
              { label: D('deviceName'), value: d.deviceName },
              { label: D('deviceIdentification'), value: d.deviceIdentification, kind: 'copyable' },
              { label: D('clientId'), value: d.clientId, kind: 'copyable' },
              { label: D('appId'), value: d.appId },
              {
                label: D('nodeType'),
                kind: 'status',
                value: getDictLabel('LINK_DEVICE_NODE_TYPE', d.nodeType, '-'),
                statusColor: nodeTypeColor(d.nodeType),
              },
              {
                label: D('connectStatus'),
                kind: 'status',
                value: getDictLabel('LINK_DEVICE_CONNECT_STATUS', d.connectStatus, '-'),
                statusColor: connectStatusColor(d.connectStatus),
              },
              { label: D('deviceTags'), kind: 'tags', full: true, tags: splitTags(d.deviceTags) },
            ],
          },
          // ============ 产品与版本(灰度排查核心) ============
          // 两个版本号常态下相等(设备注册时按 activeVersionNo 绑定):
          //   - 一致 → 1 个 chip + ✓ "与产品最新版本一致",大部分用户不需要再关心
          //   - 不一致(灰度 / 回滚 / 设备未升级)→ 2 行展示 + ⚠ 提示用户注意
          // 这个 versionBinding kind 把"是否一致 + 两个值"打包给模板渲染,避免用户被
          // "为什么有两个版本号"的疑问困扰
          {
            key: 'product',
            title: t('iot.link.device.device.basicInfoTip.productSection'),
            color: '#fa8c16',
            fields: [
              { label: P('productName'), value: prod.productName },
              {
                label: P('productIdentification'),
                value: prod.productIdentification,
                kind: 'copyable',
              },
              {
                label: P('productType'),
                value: getDictLabel('LINK_PRODUCT_TYPE', prod.productType, '-'),
              },
              {
                label: TIP('versionBindingLabel'),
                tooltip: TIP('versionBindingTip'),
                kind: 'versionBinding',
                full: true,
                boundValue: d.boundProductVersionNo,
                activeValue: prod.activeVersionNo,
                // 一致条件:两个值都非空且严格相等
                consistent:
                  !!d.boundProductVersionNo &&
                  !!prod.activeVersionNo &&
                  String(d.boundProductVersionNo) === String(prod.activeVersionNo),
              },
            ],
          },
          // ============ 协议与认证 ============
          // 加密密钥 / 加密向量:明文模式(0)下不渲染,避免 "-" 干扰
          // 连接实例(connector)从顶部 metric 卡下沉到这里:URL 通常较长,放卡片副信息会被截断
          {
            key: 'protocol',
            title: t('iot.link.device.device.basicInfoTip.authSection'),
            color: '#9254de',
            fields: [
              { label: P('protocolType'), value: prod.protocolType },
              { label: P('dataFormat'), value: prod.dataFormat },
              {
                label: D('connector'),
                value: d.connector,
                kind: 'copyable',
                full: true,
              },
              {
                label: D('authMode'),
                value: getDictLabel('LINK_DEVICE_AUTH_MODE', d.authMode, '-'),
              },
              { label: D('userName'), value: d.userName, kind: 'copyable' },
              ...(isSsl
                ? [
                    {
                      label: D('certSerialNumber'),
                      value: d.certSerialNumber,
                      kind: 'cert',
                      full: true,
                    },
                  ]
                : []),
              {
                label: D('encryptMethod'),
                value: getDictLabel('LINK_DEVICE_ENCRYPT_METHOD', d.encryptMethod, '-'),
              },
              { label: D('password'), value: d.password, kind: 'secret' },
              { label: D('signKey'), value: d.signKey, kind: 'secret' },
              ...(!isPlaintext
                ? [
                    { label: D('encryptKey'), value: d.encryptKey, kind: 'secret' },
                    { label: D('encryptVector'), value: d.encryptVector, kind: 'secret' },
                  ]
                : []),
            ],
          },
          // ============ 厂商与固件 ============
          {
            key: 'manufacturer',
            title: t('iot.link.device.device.basicInfoTip.manufacturerSection'),
            color: '#13c2c2',
            fields: [
              { label: P('manufacturerName'), value: prod.manufacturerName },
              { label: P('manufacturerId'), value: prod.manufacturerId },
              { label: P('model'), value: prod.model },
              { label: D('swVersion'), value: d.swVersion, mono: true },
              { label: D('fwVersion'), value: d.fwVersion, mono: true },
              { label: D('deviceSdkVersion'), value: d.deviceSdkVersion, mono: true },
            ],
          },
          // ============ 时间线(审计) ============
          {
            key: 'timeline',
            title: t('iot.link.device.device.basicInfoTip.timelineSection'),
            color: '#8c97a5',
            fields: [
              { label: D('lastHeartbeatTime'), value: d.lastHeartbeatTime },
              { label: D('createdTime'), value: d.createdTime },
              { label: D('createdBy'), value: echoMapText(d, 'createdBy') },
              { label: D('updatedTime'), value: d.updatedTime },
              { label: D('updatedBy'), value: echoMapText(d, 'updatedBy') },
            ],
          },
        ];
      });

      return {
        t,
        groups,
        getDictLabel,
        getDeviceNodeTypeSvg,
      };
    },
  });
</script>

<style lang="less" scoped>
  /* 父级 panel-card 已给固定高度 + overflow:hidden,这里不能给 .basic-info 自身 overflow:auto
     (会因父级 overflow:hidden 链路截断)。正确做法:滚动放在 .info-main 内部,
     侧栏 .info-aside 自然高度浮在右侧不滚。 ── 与 product/detail/basicInfo 同款。 */
  .basic-info {
    display: flex;
    gap: 28px;
    padding: 4px 0;
    height: 100%;
    min-height: 0;
    overflow: hidden;

    .info-main {
      flex: 1;
      min-width: 0;
      min-height: 0;
      overflow-y: auto;
      padding-right: 8px; /* 给滚动条留位置,避免压字 */
    }

    .info-aside {
      flex-shrink: 0;
      width: 220px;
      min-height: 0;
    }
  }

  .info-group {
    & + & {
      margin-top: 26px;
      padding-top: 24px;
      border-top: 1px solid #f0f2f5;
    }
  }

  .group-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
    margin-bottom: 18px;

    .title-bar {
      width: 3px;
      height: 14px;
      border-radius: 2px;
    }
  }

  /* 响应式网格:自适应列数(大屏 4 列 / 中屏 3 / 小屏 2 / 手机 1) */
  .field-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
    gap: 20px 28px;
  }

  .field-item {
    min-width: 0;

    &.is-full {
      grid-column: 1 / -1;
    }
  }

  .field-label {
    font-size: 12px;
    color: #8c97a5;
    margin-bottom: 6px;
    display: inline-flex;
    align-items: center;
    gap: 4px;

    .label-tip {
      color: #c8cfdb;
      cursor: help;
      font-size: 12px;
      transition: color 0.18s ease;

      &:hover {
        color: #5d87ff;
      }
    }
  }

  .field-value {
    font-size: 14px;
    line-height: 1.5;

    .value-text {
      color: #2a3547;
      font-weight: 500;
      word-break: break-all;

      &.mono {
        font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
        font-size: 13px;
        color: #4a5568;
      }
    }

    .value-empty {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      color: #97a1b0;
      font-size: 12px;
    }

    /* 多标签:换行时给底部留间距,避免多行标签贴在一起 */
    .tag-chip {
      margin: 0 6px 6px 0;
    }
  }

  /* 版本绑定组合块:
     - consistent → 单行 chip + 绿色 ✓ "一致"
     - 不一致 → 两行 chip(本设备绑定 / 产品最新)+ 橙色 ⚠ 提示
     用 grid 让 chip 和提示对齐,不挤成一坨 */
  .version-binding {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .version-row {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .row-label {
        font-size: 12px;
        color: #97a1b0;
        flex-shrink: 0;
      }
    }

    .hint {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      line-height: 1.4;

      &.hint-ok {
        color: #0c8a6f;
        :deep(svg) {
          color: #13deb9;
        }
      }
      &.hint-warn {
        color: #b4661d;
        :deep(svg) {
          color: #ffae1f;
        }
      }
    }
  }

  /* 侧栏视觉锚:节点类型 SVG 缩略图 */
  .aside-card {
    background: linear-gradient(180deg, #fafbfd 0%, #ffffff 100%);
    border: 1px solid #eef1f7;
    border-radius: 14px;
    padding: 18px 14px 16px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .aside-thumb {
    width: 120px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(svg) {
      width: 100%;
      height: 100%;
    }
  }

  .aside-label {
    margin-top: 4px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
  }

  .aside-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #97a1b0;
    line-height: 1.5;
  }

  @media (max-width: 768px) {
    .basic-info {
      flex-direction: column;
    }

    .info-aside {
      width: 100%;
    }
  }
</style>
