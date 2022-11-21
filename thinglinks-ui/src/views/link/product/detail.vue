<template>
  <div v-if="productInfo" class="app-container">
    <el-row>
      <div class="chart-wrapper">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span class="dm-bold">{{ productInfo.productName }}</span>
            <span class="dmc-sub-title dm-pl8 dmc-dev-amount"> ID: {{ productInfo.id }}</span>
            <span class="dmc-sub-title dm-pl8 dmc-dev-amount"> 标识: {{ productInfo.productIdentification }}</span>
            <span class="dmc-sub-title dmc-dev-amount"> 注册设备数: <span style="color: #526ECC;">0</span></span>
            <el-button v-hasPermi="['link:product:edit']" icon="el-icon-edit" plain size="mini" style="float: right;"
              type="danger" @click="handleUpdate">
              更新产品信息
            </el-button>
          </div>
          <el-form label-width="120px">
            <el-row style="display: flex;justify-content: space-around;">
              <el-col :span="10">
                <el-form-item label="产品名称：">{{ productInfo.productName }}</el-form-item>
                <el-form-item label="设备类型：">{{ productInfo.deviceType }}</el-form-item>
                <el-form-item label="厂商ID：">{{ productInfo.manufacturerId }}</el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="产品状态：">
                  <dict-tag :options="dict.type.business_data_status" :value="productInfo.status" />
                </el-form-item>
                <el-form-item label="数据格式：">{{ productInfo.dataFormat }}</el-form-item>
                <el-form-item label="厂商名称：">{{ productInfo.manufacturerName }}</el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="应用ID：">
                  <dict-tag :options="dict.type.link_application_type" :value="productInfo.appId" />
                </el-form-item>
                <el-form-item label="协议类型：">{{ productInfo.protocolType }}</el-form-item>
                <el-form-item label="创建时间：">{{ productInfo.createTime }}</el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="产品描述：">{{ productInfo.remark }}</el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </div>
    </el-row>
    <el-row>
      <div class="chart-wrapper">
        <el-card class="box-card">
          <el-tabs v-model="activeName">
            <el-tab-pane label="模型定义" name="first" style="width:100%;height: 100%;">
              <div v-if="this.productIdentification" style="width:100%;height:100%">
                <Services :productIdentification="this.productIdentification"></Services>
              </div>
            </el-tab-pane>
            <el-tab-pane label="设备调试" name="second" style="width:100%;background: #F1F6FA;">
              <img style="width: 100%;" src="@/assets/images/equipmentDebugging.jpg" alt="">
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </el-row>

    <!-- 添加或修改产品管理对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="产品名称" prop="productName">
          <el-col :span="22">
            <el-input v-model="form.productName" placeholder="请输入产品名称" />
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip class="item" content="自定义，支持中文、英文大小写、数字、下划线和中划线" effect="light" placement="right-start">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-form-item>
        <el-form-item label="厂商ID" prop="manufacturerId">
          <el-col :span="22">
            <el-input v-model="form.manufacturerId" placeholder="请输入厂商ID" />
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip class="item" content="支持英文大小写，数字，下划线和中划线" effect="light" placement="right-start">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-form-item>
        <el-form-item label="厂商名称" prop="manufacturerName">
          <el-col :span="22">
            <el-input v-model="form.manufacturerName" placeholder="请输入厂商名称" />
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip class="item" content="支持英文大小写，数字，下划线和中划线" effect="light" placement="right-start">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-form-item>
        <el-form-item label="产品描述" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入产品描述" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>
<script>
import { getProduct, updateProduct } from "@/api/link/product/product";
import Services from "@/views/link/product/services";

export default {
  name: "product-detail",
  components: { Services },
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
      productIdentification:null,
      productInfo: null,
      title: null,
      open: false,
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        productName: [
          { required: true, message: "产品名称不能为空", trigger: "blur" },
          { min: 2, max: 64, message: '产品名称长度必须介于 2 和 64 之间', trigger: 'blur' },
          {
            //pattern: /^(?!_)(?!.*?_$)(?!-)(?!.*?-$)[\u4e00-\u9fa5a-zA-Z0-9_-]+$/,
            //message: "中文、英文大小写、数字、下划线和中划线，不能以下划线中划线开头和结尾，长度[2,64]",
            pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_-]+$/,
            message: "中文、英文大小写、数字、下划线和中划线，长度[2,64]",
            trigger: "blur"
          }
        ],
        manufacturerId: [
          { required: true, message: "厂商ID不能为空", trigger: "blur" },
          { min: 2, max: 50, message: '厂商ID长度必须介于 2 和 50 之间', trigger: 'blur' },
          {
            pattern: /^[a-zA-Z0-9_-]+$/,
            message: "英文大小写、数字、下划线和中划线，长度[2,50]",
            trigger: "blur"
          }
        ],
        manufacturerName: [
          { required: true, message: "厂商名称不能为空", trigger: "blur" },
          { min: 2, max: 64, message: '厂商名称长度必须介于 2 和 64 之间', trigger: 'blur' },
          {
            pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_-]+$/,
            message: "中文、英文大小写、数字、下划线和中划线，长度[2,64]",
            trigger: "blur"
          }
        ],
      },
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
        this.productIdentification =res.data.productIdentification;
        this.productInfo = res.data
      })
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        productName: null,
        manufacturerId: null,
        manufacturerName: null,
        remark: null,
      };
      this.resetForm("form");
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      this.form = this.productInfo
      this.open = true;
      this.title = "修改产品";
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.id != null) {
            updateProduct(this.form).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getDetail();
            });
          }
        }
      });
    },
  }
}
</script>
<style lang="scss" scoped>
.chart-wrapper {
  background: #fff;
  //padding: 16px 16px 0;
  margin-bottom: 16px;
}

.box-card .el-form-item {
  margin-bottom: 0px;
}

@media (max-width: 1024px) {
  .chart-wrapper {
    padding: 8px;
  }
}

.dmc-sub-title {
  font-size: .75rem;
  color: #8a8e99;
}

.dm-font16 {
  margin-left: .5rem;
  padding-left: .5rem;
  border-left: 1px solid #dfe1e6;
}

.dm-bold {
  font-weight: 700;
}

.dm-p18 {
  padding-left: .5rem !important;
}

.dmc-dev-amount {
  margin-left: .5rem;
  padding-left: .5rem;
  border-left: 1px solid #dfe1e6;
}
</style>
