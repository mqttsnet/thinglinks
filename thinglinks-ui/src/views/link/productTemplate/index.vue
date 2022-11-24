<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="应用ID" prop="appId">
        <el-select v-model="queryParams.appId" placeholder="请选择应用ID">
          <el-option
            v-for="dict in dict.type.link_application_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="模板名称" prop="templateName">
        <el-input
          v-model="queryParams.templateName"
          placeholder="请输入产品模板名称:自定义，支持中文、英文大小写、数字、下划线和中划线"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
          >重置</el-button
        >
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
          v-hasPermi="['link:productTemplate:add']"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['link:productTemplate:edit']"
          >修改</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['link:productTemplate:remove']"
          >删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['link:productTemplate:export']"
          >导出</el-button
        >
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="templateList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="id" align="center" prop="id" />
      <el-table-column align="center" label="应用ID" prop="appId">
        <template slot-scope="scope">
          <dict-tag
            :options="dict.type.link_application_type"
            :value="scope.row.appId"
          />
        </template>
      </el-table-column>
      <el-table-column label="模板名称" align="center" prop="templateName" />
      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <dict-tag
            :options="dict.type.business_data_status"
            :value="scope.row.status"
          />
        </template>
      </el-table-column>
      <el-table-column label="模板描述" align="center" prop="remark" />
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['link:productTemplate:edit']"
            >修改</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['link:productTemplate:remove']"
            >删除</el-button
          >
          <el-button
            v-hasPermi="['link:productTemplate:detail']"
            icon="el-icon-s-operation"
            size="mini"
            type="text"
            @click="handleDetail(scope.row)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改产品模板对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="应用ID" prop="appId">
          <el-select v-model="form.appId" placeholder="请选择应用ID">
            <el-option
              v-for="dict in dict.type.link_application_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模板名称" prop="templateName">
          <el-input
            v-model="form.templateName"
            placeholder="请输入产品模板名称:自定义，支持中文、英文大小写、数字、下划线和中划线"
          />
        </el-form-item>
        <el-form-item label="模板描述" prop="remark">
          <el-input
            v-model="form.remark"
            placeholder="请输入产品模型模板描述"
            type="textarea"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 模板服务列表 -->
    <el-dialog
      :close-on-click-modal="false"
      :title="titleProp"
      :visible.sync="openProp"
      append-to-body
      width="900px"
    >
      <el-scrollbar style="height: 500px">
        <Services :templateId="this.templateId"></Services>
      </el-scrollbar>
    </el-dialog>
  </div>
</template>

<script>
import {
  listProductTemplate,
  getProductTemplate,
  delProductTemplate,
  addProductTemplate,
  updateProductTemplate,
} from "@/api/link/product/productTemplate";
import Services from "@/views/link/product/services";

export default {
  name: "Template",
  components: { Services },
  dicts: ["link_application_type", "business_data_status"],
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
      // 产品模板表格数据
      templateList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 弹出层标题
      titleProp: "",
      // 是否显示弹出层
      openProp: false,
      templateId: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        appId: null,
        templateName: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        appId: [{ required: true, message: "应用ID不能为空", trigger: "blur" }],
        templateName: [
          { required: true, message: "模板名称不能为空", trigger: "blur" },
        ],
        status: [{ required: true, message: "状态不能为空", trigger: "blur" }],
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询产品模板列表 */
    getList() {
      this.loading = true;
      listProductTemplate(this.queryParams).then((response) => {
        this.templateList = response.rows;
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
        appId: null,
        templateName: null,
        status: "0",
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
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
      this.ids = selection.map((item) => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加产品模板";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getProductTemplate(id).then((response) => {
        this.form = response.data;
        this.open = true;
        this.title = "修改产品模板";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.id != null) {
            updateProductTemplate(this.form).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addProductTemplate(this.form).then((response) => {
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
      this.$modal
        .confirm('是否确认删除产品模板编号为"' + ids + '"的数据项？')
        .then(function () {
          return delProductTemplate(ids);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download(
        "link/productTemplate/export",
        {
          ...this.queryParams,
        },
        `template_${new Date().getTime()}.xlsx`
      );
    },
    handleDetail: function (row) {
      // this.templateId = row.id;
      // this.openProp = true;
      // this.titleProp = "模板服务详情";
      const templateId = row.id;
      this.$router.push("/link/template-detail/template/" + templateId);
    },
  },
};
</script>
