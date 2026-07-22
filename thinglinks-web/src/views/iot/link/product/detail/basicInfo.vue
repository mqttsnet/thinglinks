<template>
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
            <div class="field-label">{{ field.label }}</div>
            <div class="field-value">
              <Tag v-if="field.kind === 'status'" :color="field.statusColor">
                {{ field.value }}
              </Tag>
              <a
                v-else-if="field.kind === 'link' && field.value"
                :href="field.value"
                target="_blank"
                rel="noopener noreferrer"
                class="value-link"
              >
                {{ field.value }}
              </a>
              <SnapshotIdTag
                v-else-if="field.kind === 'snapshot'"
                :value="field.value"
              />
              <span v-else class="value-text" :class="{ mono: field.mono }">
                {{ field.value || '-' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 侧栏:产品图片(配置了 icon 才显示) -->
    <div v-if="productDetail?.icon" class="info-aside">
      <ImageDisplay
        :title="t('iot.link.product.product.productImage')"
        :fileId="productDetail.icon"
        :imageStyle="{ 'max-width': '180px', 'max-height': '180px' }"
      />
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, PropType, computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { Tag } from 'ant-design-vue';
  import ImageDisplay from '/@/components/ImageDisplay/src/ImageDisplay.vue';
  import { SnapshotIdTag } from '/@/components/iot';
  import { useDict } from '/@/components/Dict';
  import type { ProductResultVO } from '/@/api/iot/link/product/model/productModel';
  import { echoMapText } from '/@/utils/echo';

  const { getDictLabel } = useDict();

  export default defineComponent({
    name: 'ProductBasicInfo',
    components: {
      ImageDisplay,
      Tag,
      SnapshotIdTag,
    },
    props: {
      productDetail: {
        type: Object as PropType<ProductResultVO>,
        required: true,
      },
    },
    setup(props) {
      const { t } = useI18n();
      const P = (k: string) => t(`iot.link.product.product.${k}`);
      const isUrl = (v: any) => typeof v === 'string' && /^https?:\/\//i.test(v);

      /** 分组信息 ── 5 组语义分区,每组一个色条标题 + 响应式字段网格。 */
      const groups = computed(() => {
        const p = (props.productDetail || {}) as ProductResultVO;
        const enabled = String(p.productStatus) === '0';
        return [
          {
            key: 'basic',
            title: P('group.basic'),
            color: '#5d87ff',
            fields: [
              { label: P('productName'), value: p.productName },
              { label: P('productIdentification'), value: p.productIdentification, mono: true },
              { label: P('appId'), value: p.appId },
              {
                label: P('productStatus'),
                kind: 'status',
                value: getDictLabel('LINK_PRODUCT_STATUS', p.productStatus, '-'),
                statusColor: enabled ? 'green' : 'red',
              },
            ],
          },
          {
            key: 'manufacturer',
            title: P('group.manufacturer'),
            color: '#9254de',
            fields: [
              { label: P('manufacturerName'), value: p.manufacturerName },
              { label: P('manufacturerId'), value: p.manufacturerId },
              { label: P('model'), value: p.model },
              { label: P('deviceType'), value: p.deviceType },
            ],
          },
          {
            key: 'protocol',
            title: P('group.protocol'),
            color: '#13c2c2',
            fields: [
              { label: P('protocolType'), value: p.protocolType },
              { label: P('dataFormat'), value: p.dataFormat },
              // 两个版本号字段走默认文本渲染 ── 详情页顶部 metric 卡已有带复制按钮的版本徽章,
              // 这里只做参考展示,避免重复出现"复制"按钮,信息干扰。
              { label: P('activeVersionNo'), value: p.activeVersionNo },
              { label: P('previousFullVersionNo'), value: p.previousFullVersionNo },
            ],
          },
          {
            key: 'description',
            title: P('group.description'),
            color: '#fa8c16',
            fields: [
              {
                label: P('remark'),
                value: p.remark,
                kind: isUrl(p.remark) ? 'link' : 'text',
                full: true,
              },
            ],
          },
          {
            key: 'audit',
            title: P('group.audit'),
            color: '#8c97a5',
            fields: [
              { label: P('createdTime'), value: p.createdTime },
              { label: P('createdBy'), value: echoMapText(p, 'createdBy') },
              { label: P('updatedTime'), value: p.updatedTime },
            ],
          },
        ];
      });

      return { t, groups };
    },
  });
</script>

<style lang="less" scoped>
  /* 父级 panel-card 已给固定高度 + 限 overflow:hidden,这里不能给 .basic-info 自身 overflow:auto
   * (会因父级 overflow:hidden 链路截断)。正确做法:滚动放在 .info-main 内部,
   * 侧栏 .info-aside 自然高度浮在右侧不滚。 */
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
      width: 200px;
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

  /* 响应式网格:自适应列数(大屏 4 列 / 中屏 3 / 小屏 2 / 手机 1),无边框,留白分隔 */
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

    .value-link {
      color: #5d87ff;
      word-break: break-all;

      &:hover {
        text-decoration: underline;
      }
    }
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
