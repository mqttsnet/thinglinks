<template>
  <el-drawer
    title="选择设备"
    custom-class="detail-drawer"
    append-to-body
    z-index="1000"
    size="680px"
    :modal="false"
    :destroy-on-close="true"
    :close-on-press-escape="true"
    :visible.sync="openSelectDevice"
    :wrapperClosable="true"
  >
    <div class="detail-drawer-con">
      <el-table :data="deviceList" style="width: 100%">
        <el-table-column label width="45">
          <template slot-scope="scope">
            <el-radio
              :label="scope.row.id"
              @change="conditionFormItemChange(scope.row)"
              v-model="deviceItem.id"
              ><span></span
            ></el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="设备名称" width="180">
        </el-table-column>
        <el-table-column
          prop="deviceIdentification"
          label="设备标识"
          width="180"
        >
        </el-table-column>
        <el-table-column prop="deviceDescription" label="描述">
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        :limit.sync="queryParams.pageSize"
        :page.sync="queryParams.pageNum"
        :total="total"
        @pagination="getList"
      />
      <div class="drawer-footer">
        <el-button type="primary" size="mini" @click="handleOk"
          >确 定</el-button
        >
      </div>
    </div>
  </el-drawer>
</template>

<script>
import { listDevice } from "@/api/link/device/device";
export default {
  name: "TLSelectDevice",
  data() {
    return {
      deviceItem: {},
      deviceList: [],
      openSelectDevice: false,
      total: 0,
      queryParams: {
        page: 1,
        pageSize: 10,
      },
    };
  },
  props: {
    // 参数
    params: {
      type: Object,
      default: {},
    },
    // 传递的产品标识
    productIdentification: {
      type: String,
      default: "",
    },
  },
  created() {},
  methods: {
    conditionFormItemChange(row) {
      this.deviceItem = row;
    },
    handleOk() {
      this.$emit("handleOk", this.deviceItem);
      this.deviceItem = {};
      this.close();
    },
    /** 查询设备档案列表 */
    getList() {
      console.log(this.productIdentification);
      this.loading = true;
      listDevice({
        productIdentification: this.productIdentification,
        ...this.queryParams,
      }).then((res) => {
        this.deviceList = res.rows;
        this.total = res.total;
        this.loading = false;
      });
    },
    async open() {
      await this.getList();
      this.openSelectDevice = true;
    },
    close() {
      this.openSelectDevice = false;
    },
  },
};
</script>

<style lang="scss">
.detail-drawer {
  padding-bottom: 50px;
  .drawer-footer {
    position: absolute;
    text-align: right;
    right: 0;
    bottom: 0;
    width: 100%;
    background-color: #f0f0f0;
    padding: 10px;
  }
  &.el-drawer {
    overflow: auto;
    height: calc(100vh - 52px);
    top: 51px;
    min-width: 800px;

    .el-drawer__header {
      padding: 14px 20px;
      font-size: 16px;
      line-height: 16px;
      color: #333;
      border-bottom: 1px solid #f0f0f0;
      position: relative;
      margin-bottom: 0;
      background-color: #f5f6f9;
    }

    .el-drawer__close-btn {
      width: 32px;
      height: 32px;
      background-color: #ff6a00;
      position: absolute;
      right: 8px;
      top: 50%;
      margin-top: -16px;
      color: #fff;
    }

    .el-drawer__body {
      padding: 20px;
    }

    .zx-page-detail-container {
      padding-bottom: 60px;
    }

    .zx-page-detail-footer {
      left: 0;
      width: 100%;
      position: absolute;
      bottom: 0;
      text-align: right;
      padding: 10px 20px;
      border-top: 1px solid #f0f0f0;
      background-color: #f5f6f9;
      z-index: 10;
    }
  }

  .detail-input {
    max-width: 300px;
  }
}
</style>
