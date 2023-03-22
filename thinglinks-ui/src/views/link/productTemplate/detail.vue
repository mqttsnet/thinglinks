<template>
  <div v-if="templateInfo" class="app-container">
    <el-row>
      <div class="chart-wrapper">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span class="dm-bold">{{ templateInfo.templateName }}</span>
            <span class="dmc-sub-title dm-pl8"> ID: {{ templateInfo.id }}</span>
          </div>
          <el-form>
            <el-row style="display: flex;justify-content: space-around;">
              <el-col :span="10">
                <el-form-item label="模板名称：">{{ templateInfo.templateName }}</el-form-item>
                <el-form-item label="应用ID：">
                  <dict-tag :options="dict.type.link_application_type" :value="templateInfo.appId" />
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="模板状态：">
                  <dict-tag :options="dict.type.business_data_status" :value="templateInfo.status" />
                </el-form-item>
                <el-form-item label="创建时间：">{{ templateInfo.createTime }}</el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="模板描述：">{{ templateInfo.remark }}</el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </div>
    </el-row>
    <el-row>
      <div class="chart-wrapper">
        <el-card class="box-card">
          <Services :template-identification="this.templateInfo.templateIdentification"></Services>
        </el-card>
      </div>
    </el-row>
  </div>
</template>

<script>
import { getProductTemplate } from "@/api/link/product/productTemplate";
import Services from "@/views/link/product/services";

export default {
  name: "template-detail",
  components: { Services },
  dicts: [
    "link_application_type",
    "business_data_status"
  ],
  data() {
    return {
      templateId: null,
      templateInfo: null
    }
  },
  created() {
    const templateId = this.$route.params && this.$route.params.templateId;
    if (templateId) {
      this.templateId = templateId
      this.getDetail()
    }
  },
  methods: {
    getDetail() {
      getProductTemplate(this.templateId).then(res => {
        this.templateInfo = res.data
      })
    }
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
