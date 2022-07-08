<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="服务名称:支持英文大小写、数字、下划线和中划线" prop="serviceName">
        <el-input
          v-model="queryParams.serviceName"
          placeholder="请输入服务名称:支持英文大小写、数字、下划线和中划线"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品ID" prop="productId">
        <el-input
          v-model="queryParams.productId"
          placeholder="请输入产品ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品模型模板ID" prop="templateId">
        <el-input
          v-model="queryParams.templateId"
          placeholder="请输入产品模型模板ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。" prop="description">
        <el-input
          v-model="queryParams.description"
          placeholder="请输入服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['link:services:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['link:services:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['link:services:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['link:services:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="servicesList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="服务id" align="center" prop="id" />
      <el-table-column label="服务名称:支持英文大小写、数字、下划线和中划线" align="center" prop="serviceName" />
      <el-table-column label="产品ID" align="center" prop="productId" />
      <el-table-column label="产品模型模板ID" align="center" prop="templateId" />
      <el-table-column label="状态(字典值：0启用,1停用)" align="center" prop="status" />
      <el-table-column label="服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。" align="center" prop="description" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['link:services:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['link:services:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改产品服务数据对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="服务名称:支持英文大小写、数字、下划线和中划线" prop="serviceName">
          <el-input v-model="form.serviceName" placeholder="请输入服务名称:支持英文大小写、数字、下划线和中划线" />
        </el-form-item>
        <el-form-item label="产品ID" prop="productId">
          <el-input v-model="form.productId" placeholder="请输入产品ID" />
        </el-form-item>
        <el-form-item label="产品模型模板ID" prop="templateId">
          <el-input v-model="form.templateId" placeholder="请输入产品模型模板ID" />
        </el-form-item>
        <el-form-item label="服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。" prop="description">
          <el-input v-model="form.description" placeholder="请输入服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。" />
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
import { listServices, getServices, delServices, addServices, updateServices } from "@/api/link/productServices";

export default {
  name: "Services",
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
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
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
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        serviceName: [
          { required: true, message: "服务名称:支持英文大小写、数字、下划线和中划线不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "状态(字典值：0启用,1停用)不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询产品服务数据列表 */
    getList() {
      this.loading = true;
      listServices(this.queryParams).then(response => {
        this.servicesList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
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
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加产品服务数据";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getServices(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改产品服务数据";
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
      this.$modal.confirm('是否确认删除产品服务数据编号为"' + ids + '"的数据项？').then(function() {
        return delServices(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('link/services/export', {
        ...this.queryParams
      }, `services_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
