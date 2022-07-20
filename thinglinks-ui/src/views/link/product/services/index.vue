<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['link:productServices:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增服务
        </el-button>
      </el-col>
<!--      <el-col :span="1.5">-->
<!--        <el-button-->
<!--          v-hasPermi="['link:productServices:edit']"-->
<!--          :disabled="single"-->
<!--          icon="el-icon-edit"-->
<!--          plain-->
<!--          size="mini"-->
<!--          type="success"-->
<!--          @click="handleUpdate"-->
<!--        >修改服务-->
<!--        </el-button>-->
<!--      </el-col>-->
<!--      <el-col :span="1.5">-->
<!--        <el-button-->
<!--          v-hasPermi="['link:productServices:remove']"-->
<!--          :disabled="multiple"-->
<!--          icon="el-icon-delete"-->
<!--          plain-->
<!--          size="mini"-->
<!--          type="danger"-->
<!--          @click="handleDelete"-->
<!--        >删除服务-->
<!--        </el-button>-->
<!--      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['link:productServices:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出服务
        </el-button>
      </el-col>
<!--      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>-->
    </el-row>
    <el-row :gutter="20">
      <!--服务列表-->
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-row>
            <span>服务列表</span>
            <div style="float:right">
              <el-tooltip class="item" content="添加" effect="dark" placement="top">
                <el-button circle icon="el-icon-plus" size="mini" @click="handleAdd()" />
              </el-tooltip>
              <el-tooltip class="item" content="刷新" effect="dark" placement="top">
                <el-button circle icon="el-icon-refresh" size="mini" @click="getList()" />
              </el-tooltip>
            </div>
          </el-row>
        </div>
        <div class="head-container">
          <el-input
            v-model="serviceName"
            clearable
            placeholder="请输入服务名称"
            prefix-icon="el-icon-search"
            size="small"
            style="margin-bottom: 20px"
          />
        </div>
        <div class="head-container">
          <el-tree
            ref="tree"
            :data="serviceOptions"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            :props="defaultProps"
            default-expand-all
            node-key="id"
            @node-click="handleNodeClick"
          />
        </div>
      </el-col>
      <!--属性、命令列表-->
      <el-col v-if="this.serviceId" :span="20" :xs="24">
        <!--属性列表-->
        <Properties :serviceId="this.serviceId"></Properties>
        <!--命令列表-->
        <Commands :serviceId="this.serviceId"></Commands>
      </el-col>
    </el-row>

    <!-- 添加或修改产品服务数据对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item v-if="productId" label="产品ID" prop="productId">
          <el-input v-model="form.productId" disabled placeholder="请输入产品ID"/>
        </el-form-item>
        <el-form-item v-if="templateId" label="产品模板ID" prop="templateId">
          <el-input v-model="form.templateId" disabled placeholder="请输入产品模型模板ID"/>
        </el-form-item>
        <el-form-item label="服务名称" prop="serviceName">
          <el-col :span="22">
            <el-input v-model="form.serviceName" autocomplete="off" placeholder="请输入服务名称"/>
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip content="属性名称。支持英文小写、数字及下划线，全部小写命名，禁止出现英文大写，多个单词用下划线，分隔长度[2,50]" effect="light" placement="right">
              <i class="el-icon-question"/>
            </el-tooltip>
          </el-col>
        </el-form-item>
        <el-form-item label="服务描述" prop="description">
          <el-col :span="22">
            <el-input v-model="form.description" placeholder="请输入服务的描述信息" type="textarea"/>
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip content="请输入服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。" effect="light" placement="right">
              <i class="el-icon-question"/>
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
import {queryServices, getServices, delServices, addServices, updateServices} from "@/api/link/product/productServices";
import Properties from "@/views/link/product/properties";
import Commands from "@/views/link/product/commands";

export default {
  name: "Services",
  components: {Commands, Properties},
  props: ["productId", "templateId"],
  dicts: ["business_data_status"],
  data() {
    return {
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
      serviceName: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        serviceName: null,
        productId: null,
        templateId: null,
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
        serviceName: [
          {required: true, message: "服务名称不能为空", trigger: "blur"},
          {min: 2, max: 50, message: '服务名称长度必须介于 2 和 50 之间', trigger: 'blur'},
          {
            pattern: /^[a-z0-9_]+$/,
            message: "英文小写、数字、下划线，长度[2,50]",
            trigger: "blur"
          }
        ],
        status: [
          {required: true, message: "状态不能为空", trigger: "blur"}
        ],
      }
    };
  },
  watch: {
    productId(newval, oldval) {
      if (newval !== oldval) {
        this.queryParams.productId = newval;
        this.getList();
      }
    },
    templateId(newval, oldval) {
      if (newval !== oldval) {
        this.queryParams.templateId = newval;
        this.getList();
      }
    },
    // 根据服务名称服务列表
    serviceName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created() {
    if (this.productId)
      this.queryParams.productId = this.productId;
    if (this.templateId)
      this.queryParams.templateId = this.templateId;
    this.getList();
  },
  methods: {
    /** 查询产品服务数据列表 */
    getList() {
      this.loading = true;
      this.serviceOptions = [];
      queryServices(this.queryParams).then(res => {
        let index = 0;
        res.data.forEach(item => {
          this.serviceOptions.push({
            id: item.id,
            label: item.serviceName
          })
          if (index === 0 )
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
        serviceName: null,
        productId: null,
        templateId: null,
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.form.productId = this.productId;
      this.form.templateId = this.templateId;
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
.pagination-container {
  height: 50px;
}
>>> .el-tree-node .el-tree-node.is-checked{
  background: #F2F2F2 !important;
}
>>> .el-tree-node.is-current{
  background: transparent !important;
}

>>> .el-tree-node__content:hover{
  background: transparent !important;
}
>>> .el-tree-node:focus>.el-tree-node__content{
  background: transparent;
}
</style>
