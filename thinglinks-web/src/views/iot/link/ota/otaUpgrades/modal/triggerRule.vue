<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('iot.link.ota.otaUpgrades.triggerRule.title')"
    :maskClosable="false"
    :destroyOnClose="true"
    :keyboard="true"
    :showOkBtn="false"
    :showCancelBtn="false"
    width="1200px"
  >
    <div class="form_box">
      <a-row :gutter="[24, 24]">
        <a-col :span="6" class="heightwrapper selected">
          <SelectedProduct :selectedProduct="selectedProduct" @deleteProduct="deleteProduct" />
        </a-col>
        <a-col :span="18" :offset="6" class="heightwrapper">
          <ProductCardList
            :isSelect="true"
            @selectProductCard="selectProductCard"
            :productIdentification="selectedProduct.productIdentification"
          />
        </a-col>
      </a-row>
    </div>
    <template #appendFooter>
      <a-button @click="handleClose">{{ t('common.cancelText') }}</a-button>
      <a-button type="primary" @click="next">{{ t('common.okText') }}</a-button>
    </template>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, toRefs, reactive, getCurrentInstance, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicForm } from '/@/components/Form/index';
  import {
    Form,
    Row,
    Col,
    Dropdown,
    Menu,
    Radio,
    Checkbox,
    TimePicker,
    Card,
    Select,
  } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import ProductCardList from '/@/components/iot/link/product/ProductCardList.vue';
  import SelectedProduct from './SelectedProduct.vue';
  import { getFullProductInfo } from '/@/api/iot/link/product/product';

  import { CloseCircleOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'ChooseProduct',
    components: {
      BasicModal,
      BasicForm,
      ProductCardList,
      CloseCircleOutlined,
      SelectedProduct,
      [Form.name]: Form,
      [Form.Item.name]: Form.Item,
      ARow: Row,
      ACol: Col,
      ADropdown: Dropdown,
      [Menu.name]: Menu,
      [Menu.Item.name]: Menu.Item,
      ARadioGroup: Radio.Group,
      ARadioButton: Radio.Button,
      ARadio: Radio,
      ACheckbox: Checkbox,
      ATimePicker: TimePicker,
      ACard: Card,
      [Select.name]: Select,
      ASelectOption: Select.Option,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { proxy } = getCurrentInstance();

      const state = reactive({
        selectedProduct: {},
      });
      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        type.value = data?.type || ActionEnum.ADD;
        if (unref(type) !== ActionEnum.ADD) {
          // 赋值
          data.productIdentification &&
            getSelectDetailByProductIdentification(data.productIdentification);
          console.log(data);
        }
      });
      const getSelectDetailByProductIdentification = async (productIdentification) => {
        console.log(productIdentification);
        const res = await getFullProductInfo(productIdentification);
        state.selectedProduct = res;
      };
      const selectProductCard = (column) => {
        if (state.selectedProduct.productIdentification == column.productIdentification) {
          createMessage.warning(t('iot.link.ota.otaUpgrades.triggerRule.description1'));
          return false;
        }
        state.selectedProduct = column;
      };
      const { createMessage } = useMessage();

      const next = () => {
        if (!state.selectedProduct.productIdentification) {
          createMessage.warning(t('iot.link.ota.otaUpgrades.triggerRule.description2'));
          return false;
        } else {
          emit('success', state.selectedProduct);
          handleClose();
        }
      };
      const handleClose = () => {
        closeModal();
        state.selectedProduct = {};
      };
      const deleteProduct = () => {
        state.selectedProduct = {};
      };

      return {
        type,
        t,
        registerModal,
        selectProductCard,
        handleClose,
        deleteProduct,
        next,
        ...toRefs(state),
      };
    },
  });
</script>
<style lang="less" scope>
  .heightwrapper {
    height: 100%;
  }

  .selected {
    position: absolute;
    width: 24%;
  }

  .form_box {
    padding: 20px;
  }

  .form_title {
    font-size: 16px;
    font-family: PingFang SC-Semibold, PingFang SC;
    font-weight: 600;
    color: #2e3033;
    line-height: 20px;
    padding-left: 9px;
    border-left: 3px solid #1a66ff;
  }

  .select-all-device {
    display: flex;
    margin: 20px 100px;
    padding: 10px 20px;
    justify-content: space-between;
    align-items: center;
    border: 2px solid #e6ebf5;
    cursor: pointer;
    font-size: 18px;
    border-radius: 4px;

    &.active {
      border-color: #1a66ff;
    }

    img {
      width: 120px;
    }
  }

  .return {
    text-align: right;
  }
</style>
../../../../../../api/iot/link/product/product
