<template>
  <div v-if="productInfo" class="app-container">
    <div class="device_status">
      <div class="device_attribute">
        <p>
          <span>厂商ID</span>
          <span>{{ productInfo.manufacturerId }}</span>
        </p>
        <p>
          <span>产品状态</span>
          <span><dict-tag :options="dict.type.business_data_status" :value="productInfo.status"/></span>
        </p>
      </div>
      <div class="device_attribute">
        <p>
          <span>产品型号 </span>
          <span>{{ productInfo.model }}</span>
        </p>
        <p>
          <span></span>
          <span>
          </span>
        </p>
      </div>
      <div class="device_attribute">
        <p>
          <span>设备类型</span>
          <span>
            {{productInfo.deviceType}}
          </span>
        </p>
        <p>
          <span>设备管理数量</span>
          <span>

          </span>
        </p>
      </div>
    </div>
    <div class="detail">
      <el-tabs v-model="activeName">
        <el-tab-pane label="基本信息" name="first">
          <div class="device_attribute">
            <p>
              <span>产品名称</span>
              <span>{{ productInfo.productName }}</span>
            </p>
            <p>
              <span>创建者</span>
              <span>{{ productInfo.createBy }}</span>
            </p>
            <p>
              <span>更新者</span>
              <span>{{ productInfo.updateBy }}</span>
            </p>
            <p>
              <span>产品标识</span>
              <span>{{ productInfo.productIdentification }}</span>
            </p>
            <p>
              <span>应用ID</span>
              <span><dict-tag :options="dict.type.link_application_type" :value="productInfo.appId"/></span>
            </p>
            <p>
              <span>产品描述</span>
              <span>{{ productInfo.remark }}</span>
            </p>
          </div>
          <div class="device_attribute">
            <p>
              <span>厂商名称</span>
              <span>{{productInfo.manufacturerName}}</span>
            </p>
            <p>
              <span>创建时间</span>
              <span>{{ productInfo.createTime }}</span>
            </p>
            <p>
              <span>更新时间</span>
              <span>{{ productInfo.updateTime }}</span>
            </p>
            <p>
              <span>产品协议类型</span>
              <span>{{ productInfo.protocolType }}</span>
            </p>
            <p>
              <span>产品模板</span>
              <span><dict-tag :options="dict.type.link_application_type" :value="productInfo.templateId"/></span>
            </p>
            <p>
              <span></span>
              <span></span>
            </p>
          </div>
        </el-tab-pane>
        <el-tab-pane label="服务列表" name="second" style="width:100%;height: 100%;">
          <div style="width:100%;height:100%">
            <Services :productId="this.productId"></Services>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
import {getProduct} from "@/api/link/product/product";
import Services from "@/views/link/product/services";

export default {
  name: "product-detail",
  components: {Services},
  dicts: [
    "link_application_type",
    "link_product_device_type",
    "link_product_type",
    "link_device_protocol_type",
    "link_product_datatype",
    "link_product_isRequired",
    "business_data_status"
  ],
  data() {
    return {
      //table切换
      activeName: 'first',
      productId: null,
      productInfo: null
    }
  },
  watch: {
    activeName(value) {
      if (value === 'second') {
      }
    }
  },
  created() {
    const productId = this.$route.params && this.$route.params.productId;
    if (productId) {
      this.productId = productId
      this.getDetail()
    }
  },
  methods: {
    getDetail() {
      getProduct(this.productId).then(res => {
        this.productInfo = res.data
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.device_status {
  width: 100%;
  margin: 0 0 10px 10px;
  padding: 20px 30px;
  background: #F8F8F9;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 700;
  color: #515a6e;
}

.device_status .status {
  width: 20%;
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.device_attribute {
  width: 30%;

  p {
    width: 100%;
    display: flex;
    align-items: center;

    span:first-child {
      display: block;
      width: 20%;
    }
  }
}

.detail {
  width: 100%;
  height: 100%;
  margin: 0 0 10px 10px;
  padding: 20px 30px;
  background: #F8F8F9;
  font-size: 14px;
  font-weight: 700;
  color: #515a6e;

  .el-tabs {
    height: 100%;

    .el-tab-pane {
      width: 80%;
      height: 300px;
      display: flex;
      justify-content: space-between;

      .el-tabs {
        .el-tab-pane {
          display: block;
        }
      }

      .device_attribute {
        width: 35%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: space-around;

        p {
          width: 100%;
          display: flex;
          align-items: center;

          span:first-child {
            display: block;
            width: 30%;
          }
        }
      }
    }
  }

}

.demo-input-suffix {
  display: flex;
  align-items: center;
  margin-bottom: 10px;

  span {
    width: 30%;
    text-align: end;
  }

  .el-input,
  .el-select {
    width: 50%;
  }
}

.pagination-container {
  height: 50px;
}
</style>
