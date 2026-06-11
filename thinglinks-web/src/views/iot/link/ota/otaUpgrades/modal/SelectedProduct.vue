<template>
  <div class="type-list">
    <div class="select-title">
      <ApartmentOutlined style="margin-right: 4px" />
      {{ t('iot.link.engine.linkage.selectedProduct') }}
    </div>
    <div class="product-item normal isSelected" v-if="selectedProduct.productIdentification">
      <div class="product-info">
        <div class="title">{{ selectedProduct.productName }}</div>
        <div class="props">
          <div class="prop">
            <div class="label">{{ t('iot.link.product.product.productType') }}</div>
            <div class="value">{{
              getDictLabel('LINK_PRODUCT_TYPE', selectedProduct.productType, '')
            }}</div>
          </div>
          <!-- <div class="prop">
            <div class="label">协议类型</div>
            <div class="value">{{ record.protocolType }}</div>
          </div> -->
          <div class="prop">
            <div class="label">{{ t('iot.link.product.product.productIdentification') }}</div>
            <div class="value">{{ selectedProduct.productIdentification }}</div>
          </div>
        </div>
        <div class="btns">
          <div class="btn">
            <img
              src="../../../../../../assets/images/iot/link/device/delete-y.png"
              alt=""
              @click="deleteProduct()"
            />
          </div>
        </div>
        <div class="product-img">
          <!-- flexy 风格内联 SVG,按 productType 自动选择普通/网关图标 -->
          <component :is="getProductTypeSvg(selectedProduct?.productType)" />
        </div>
      </div>
    </div>
    <div class="empty" v-else>
      <a-empty :description="t('iot.link.ota.otaUpgrades.triggerRule.description2')" />
    </div>
  </div>
</template>
<script lang="ts">
  import { useI18n } from '/@/hooks/web/useI18n';
  import { defineComponent, reactive, toRefs, watch } from 'vue';
  import { ApartmentOutlined } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  import { getProductTypeSvg } from '/@/components/iot/svg';
  const { getDictLabel } = useDict();

  export default defineComponent({
    name: 'SelectedProduct',
    components: {
      ApartmentOutlined,
    },
    props: {
      selectedProduct: {
        type: Object,
        default: {},
      },
    },
    setup(props, { emit }) {
      console.log(props);
      const { t } = useI18n();
      // 监听selectedProduct
      watch(
        () => props.selectedProduct,
        (data: object) => {
          console.log(data);
          state.selectedProduct = { ...props.selectedProduct };
        },
      );
      const state = reactive({
        selectedProduct: { ...props.selectedProduct },
        typeId: null,
      });
      const deleteProduct = () => {
        emit('deleteProduct');
      };
      return {
        t,
        getDictLabel,
        deleteProduct,
        getProductTypeSvg,
        ...toRefs(state),
      };
    },
  });
</script>
<style lang="less" scoped>
  .type-list {
    height: 100%;
    overflow-y: auto;

    .select-title {
      color: #1966ff;
      font-size: 16px;
      margin-bottom: 8px;
    }
  }

  .product-item {
    width: 100%;
    align-items: center;
    justify-content: space-between;
    position: relative;
    overflow: hidden;
    box-shadow: 0px 0px 4px 0px rgba(0, 0, 0, 0.15);
    border-radius: 8px;
    padding: 10px 0;
    background-color: #fff;
    background-repeat: no-repeat;
    background-position: center center;
    background-size: 104% 104%;
    transition: all 0.5s;
    border: 2px solid transparent;

    &.isSelected {
      box-shadow: 0px 0px 8px 0px rgba(34, 78, 166, 0.25);
      border: 2px solid #1966ff;

      .select-icon {
        position: absolute;
        right: -30px;
        bottom: -30px;
        z-index: 2;
        width: 60px;
        height: 60px;
        background: #1966ff;
        color: #d9dffd;
        transform: rotate(-45deg);

        .icon {
          margin-left: 20px;
          font-size: 20px;
          transform: rotate(45deg);
        }
      }
    }

    &.normal {
      background-image: url('/@/assets/images/iot/link/device/bg-normal.png');

      .status {
        background: #d9dffd;
        color: #1966ff;
      }
    }

    .product-info {
      flex: 2;
      display: flex;
      flex-direction: column;
      width: 100%;

      .status {
        width: 57px;
        height: 25px;
        border-radius: 0px 6px 6px 0px;
        font-size: 12px;
        font-family: PingFang SC-Medium, PingFang SC;
        font-weight: 500;
        line-height: 25px;
        text-align: center;
      }

      .title {
        font-size: 16px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        color: #050708;
        line-height: 19px;
        padding-left: 16px;
        margin: 14px 0 16px;
        max-width: 70%;
      }

      .props {
        padding-left: 16px;

        .prop {
          display: flex;
          align-items: center;

          // flex: 1;
          & + .prop {
            margin-top: 8px;
          }

          .label {
            font-size: 12px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            color: #9c9c9c;
            line-height: 14px;
          }

          .value {
            font-size: 12px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            color: #050708;
            line-height: 14px;
            // margin-top: 8px;
            margin-left: 8px;
          }
        }
      }

      .btns {
        display: flex;
        margin-left: 16px;
        margin-top: 14px;
        width: 138px;
        height: 28px;
        border-radius: 45px 45px 45px 45px;
        border: 2px solid #1a66ff;
        justify-content: center;
        align-items: center;

        .btn {
          width: 28px;
          text-align: center;
          position: relative;

          & + .btn::before {
            content: '';
            display: block;
            position: absolute;
            width: 1px;
            height: 7px;
            background-color: #e2e2e2;
            left: 0;
            top: 5px;
          }

          img {
            width: 15px;
            height: 15px;
            margin: 0 auto;
            cursor: pointer;
          }
        }
      }
    }

    .product-img {
      position: absolute;
      right: 10px;
      top: 10px;
      width: 30%;

      img {
        cursor: pointer;
        width: 100%;
      }
    }

    .device-btns {
      width: 158px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      border-left: 1px dashed #d4d4d4;

      .device-status {
        padding-top: 6px;
        padding-bottom: 14px;
        border-bottom: 1px dashed #d4d4d4;
        display: flex;
        align-items: center;

        .img {
          width: 18px;
          height: 18px;
          margin-right: 2px;
        }

        span {
          color: #808080;

          &.red {
            color: #fa3758;
          }

          &.green {
            color: #43cf7c;
          }
        }
      }

      .btn {
        width: 92px;
        height: 28px;
        background: #1a66ff;
        opacity: 1;
        text-align: center;
        font-size: 14px;
        line-height: 24px;
        color: #ffffff;
        border: 2px solid #1a66ff;
        border-radius: 6px;
        margin-top: 18px;

        &.plain {
          background-color: #fff;
          color: #1a66ff;
        }

        &.danger {
          background-color: #fff;
          color: #d43030;
          border: 2px solid #d43030;
        }
      }
    }
  }
</style>
