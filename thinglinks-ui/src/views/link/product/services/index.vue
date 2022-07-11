<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="服务名称" prop="serviceName">
        <el-input
          v-model="queryParams.serviceName"
          clearable
          placeholder="请输入服务名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="服务描述" prop="description">
        <el-input
          v-model="queryParams.description"
          clearable
          placeholder="请输入服务的描述信息"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['link:productServices:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['link:productServices:edit']"
          :disabled="single"
          icon="el-icon-edit"
          plain
          size="mini"
          type="success"
          @click="handleUpdate"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['link:productServices:remove']"
          :disabled="multiple"
          icon="el-icon-delete"
          plain
          size="mini"
          type="danger"
          @click="handleDelete"
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['link:productServices:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="servicesList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="服务id" prop="id"/>
      <el-table-column align="center" label="产品ID" prop="productId"/>
      <el-table-column align="center" label="产品模板ID" prop="templateId"/>
      <el-table-column align="center" label="服务名称" prop="serviceName"/>
      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.business_data_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="服务描述" prop="description"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['link:productServices:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['link:productServices:remove']"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
          <el-button v-hasPermi="['link:product:detail']" icon="el-icon-s-operation" size="mini" type="text" @click="handleDetail(scope.row)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />

    <!-- 添加或修改产品服务数据对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
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
            <el-tooltip content="指示属性名称。支持英文小写、数字及下划线，全部小写命名，禁止出现英文大写，多个单词用下划线，分隔长度[2,50]" effect="light"
                        placement="right">
              <i class="el-icon-question"/>
            </el-tooltip>
          </el-col>
        </el-form-item>
        <el-form-item label="服务描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。" type="textarea"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 服务属性详情 -->
    <el-dialog :close-on-click-modal="false" :title="titleProp" :visible.sync="openProp" append-to-body width="900px">
      <el-scrollbar style="height: 500px">
        <Properties :serviceId="this.serviceId"></Properties>
      </el-scrollbar>
    </el-dialog>
  </div>
</template>

<script>
import {listServices, getServices, delServices, addServices, updateServices} from "@/api/link/product/productServices";
import Properties from "@/views/link/product/properties";

export default {
  name: "Services",
  components: {Properties},
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
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 弹出层标题
      titleProp: "",
      // 是否显示弹出层
      openProp: false,
      serviceId: null,
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
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.form.productId = this.productId;
      this.form.templateId = this.templateId;
      this.open = true;
      if (this.productId)
        this.title = "添加产品服务数据";
      if (this.templateId)
        this.title = "添加模板服务数据";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getServices(id).then(response => {
        this.form = response.data;
        this.open = true;
        if (this.productId)
          this.title = "修改产品服务数据";
        if (this.templateId)
          this.title = "修改模板服务数据";
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
      this.download('iot/services/export', {
        ...this.queryParams
      }, `services_${new Date().getTime()}.xlsx`)
    },
    handleDetail: function (row) {
      this.serviceId = row.id;
      this.openProp = true;
      this.titleProp = "服务属性列表";
    },
  }
};
</script>
<style lang="scss" scoped>
.pagination-container {
  height: 50px;
}
</style>
