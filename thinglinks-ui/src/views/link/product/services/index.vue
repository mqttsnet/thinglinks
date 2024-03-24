<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--服务列表-->
      <el-col :span="5" :xs="24">
        <el-card class="box-card" :body-style="{ padding: '10px 20px' }">
          <div slot="header" class="clearfix">
            <el-row>
              <span style="font-weight: 700;font-size: 15px;">服务列表</span>
              <el-tooltip class="item" content="刷新" effect="light" placement="top">
                <i class="el-icon-refresh" size="mini" @click="getList()" style="float: right; padding: 3px 0"></i>
              </el-tooltip>
            </el-row>
          </div>
          <div class="head-container">
            <el-row :gutter="10" class="mb8">
              <el-col :span="1.5">
                <el-tooltip class="item" effect="light" content="新增服务" placement="top">
                  <el-button v-hasPermi="['link:productServices:add']" icon="el-icon-plus" circle size="mini"
                    type="primary" @click="handleAdd">
                  </el-button>
                </el-tooltip>
              </el-col>
              <el-col :span="1.5">
                <el-tooltip class="item" effect="light" content="修改服务" placement="right">
                  <el-button v-hasPermi="['link:productServices:edit']" :disabled="single" icon="el-icon-edit" circle
                    size="mini" type="success" @click="handleUpdate">
                  </el-button>
                </el-tooltip>
              </el-col>
              <el-col :span="1.5">
                <el-tooltip class="item" effect="light" content="删除服务" placement="right">
                  <el-button v-hasPermi="['link:productServices:remove']" :disabled="multiple" icon="el-icon-delete"
                    circle size="mini" type="danger" @click="handleDelete">
                  </el-button>
                </el-tooltip>
              </el-col>
              <el-col :span="1.5">
                <el-tooltip class="item" effect="light" content="导出服务" placement="top">
                  <el-button v-hasPermi="['link:productServices:export']" icon="el-icon-download" circle size="mini"
                    type="warning" @click="handleExport">
                  </el-button>
                </el-tooltip>
              </el-col>
              <el-col :span="1.5">
                <el-tooltip class="item" effect="light" content="搜索" placement="top">
                  <el-button v-hasPermi="['link:productServices:export']" icon="el-icon-search" circle size="mini"
                    @click="searchFalg = !searchFalg">
                  </el-button>
                </el-tooltip>
              </el-col>
              <!-- <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar> -->
            </el-row>
            <el-row v-if="searchFalg">
              <div class="head-input">
                <el-input v-model="serviceName" clearable placeholder="请输入服务名称" prefix-icon="el-icon-search"
                  size="small" style="margin-bottom: 5px" />
              </div>
            </el-row>
            <el-row>
              <el-tooltip class="item" effect="light" content="Right Center 提示文字" placement="right">
                <el-tree ref="tree" :data="serviceOptions" :expand-on-click-node="false" :highlight-current="true"
                  :filter-node-method="filterNode" :props="defaultProps" default-expand-all node-key="id"
                  @node-click="handleNodeClick" show-checkbox @check="handleSelectionChange" />
              </el-tooltip>
            </el-row>
          </div>
        </el-card>

      </el-col>
      <!--属性、命令列表-->
      <el-col v-if="this.serviceId" :span="19" :xs="24">
        <el-tabs v-model="activeName" type="border-card">
          <el-tab-pane label="属性列表" name="first">
            <!--属性列表-->
            <Properties :serviceId="this.serviceId"></Properties>
          </el-tab-pane>
          <el-tab-pane label="命令列表" name="second">
            <!--命令列表-->
            <Commands :serviceId="this.serviceId"></Commands>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <!-- 添加或修改产品服务数据对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="服务编码" prop="serviceCode">
          <el-col :span="22">
            <el-input v-model="form.serviceCode" autocomplete="off" placeholder="请输入服务编码" />
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip content="属性编码。支持英文小写、数字及下划线，全部小写命名，禁止出现英文大写，多个单词用下划线，分隔长度[2,50]" effect="light"
                        placement="right">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-form-item>
        <el-form-item label="服务名称" prop="serviceName">
          <el-col :span="22">
            <el-input v-model="form.serviceName" autocomplete="off" placeholder="请输入服务名称" />
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip content="属性名称" effect="light"
              placement="right">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-form-item>
        <el-form-item label="服务描述" prop="description">
          <el-col :span="22">
            <el-input v-model="form.description" placeholder="请输入服务的描述信息" type="textarea" />
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip content="请输入服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。" effect="light" placement="right">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
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
import { queryServices, getServices, delServices, addServices, updateServices } from "@/api/link/product/productServices";
import Properties from "@/views/link/product/properties";
import Commands from "@/views/link/product/commands";

export default {
  name: "Services",
  components: { Commands, Properties },
  props: ["productIdentification", "templateIdentification"],
  dicts: ["business_data_status"],
  data() {
    return {
      //search组件切换
      searchFalg: false,
      //tab切换
      activeName: "first",
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 产品服务数据表格数据
      servicesList: [],
      serviceOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 弹出层标题
      titleProp: "",
      // 是否显示弹出层
      openProp: false,
      serviceId: null,
      serviceCode: null,
      serviceName: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        serviceCode: null,
        serviceName: null,
        productIdentification: null,
        templateIdentification: null,
        status: null,
        description: null,
      },
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        serviceCode: [
          { required: true, message: "服务名称不能为空", trigger: "blur" },
          { min: 2, max: 50, message: '服务名称长度必须介于 2 和 50 之间', trigger: 'blur' },
          {
            pattern: /^[a-z0-9_]+$/,
            message: "英文小写、数字、下划线，长度[2,50]",
            trigger: "blur"
          }
        ],
        serviceName: [
          { required: true, message: "服务名称不能为空", trigger: "blur" },
          { min: 2, max: 50, message: '服务名称长度必须介于 2 和 50 之间', trigger: 'blur' }
        ],
        status: [
          { required: true, message: "状态不能为空", trigger: "blur" }
        ],
      }
    };
  },
  watch: {
    productId(newval, oldval) {
      if (newval !== oldval) {
        this.queryParams.productIdentification = newval;
        this.getList();
      }
    },
    templateId(newval, oldval) {
      if (newval !== oldval) {
        this.queryParams.templateIdentification = newval;
        this.getList();
      }
    },
    // 根据服务名称服务列表
    serviceName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created() {
    if (this.productIdentification)
      this.queryParams.productIdentification = this.productIdentification;
    if (this.templateIdentification)
      this.queryParams.templateIdentification = this.templateIdentification;
    this.getList();
  },
  methods: {
    /** 查询产品服务数据列表 */
    getList() {
      this.loading = true;
      this.serviceOptions = [];
      queryServices(this.queryParams).then(res => {
        console.log(res);
        let index = 0;
        res.data.forEach(item => {
          this.serviceOptions.push({
            id: item.id,
            label: item.serviceName
          })
          if (index === 0)
            this.serviceId = item.id
          index++;
        })
      });
    },
    // 筛选节点
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    // 节点单击事件
    handleNodeClick(data) {
      this.serviceId = data.id;
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
        serviceCode: null,
        serviceName: null,
        productIdentification: null,
        templateIdentification: null,
        status: "0",
        description: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(target, Selected) {
      this.ids = Selected.checkedNodes.map(item => item.id)
      this.single = Selected.checkedNodes.length !== 1
      this.multiple = !Selected.checkedNodes.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.form.productIdentification = this.productIdentification;
      this.form.templateIdentification = this.templateIdentification;
      this.open = true;
      this.title = "添加服务";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getServices(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改服务";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateServices(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addServices(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除产品服务数据编号为"' + ids + '"的数据项？').then(function () {
        return delServices(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('link/productServices/export', {
        ...this.queryParams
      }, `services_${new Date().getTime()}.xlsx`)
    },
  }
};
</script>
<style lang="scss" scoped>
.app-container {
  padding: 0 10px 10px 10px;
}

.pagination-container {
  height: 50px;
}

>>>.el-tree-node .el-tree-node.is-checked {
  background: #F2F2F2 !important;
}

>>>.el-tree-node.is-current {
  background: transparent !important;
}

>>>.el-tree-node__content:hover {
  background: transparent !important;
}

>>>.el-tree-node:focus>.el-tree-node__content {
  background: transparent;
}
</style>
