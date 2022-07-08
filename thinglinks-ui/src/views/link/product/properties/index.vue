<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="指示属性名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入指示属性名称。"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="服务ID" prop="serviceId">
        <el-input
          v-model="queryParams.serviceId"
          placeholder="请输入服务ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="属性描述" prop="description">
        <el-input
          v-model="queryParams.description"
          placeholder="请输入属性描述，不影响实际功能，可配置为空字符串"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="请输入指示枚举值" prop="enumlist">
        <el-input
          v-model="queryParams.enumlist"
          placeholder="请输入指示枚举值:如开关状态status可有如下取值'enumList':['OPEN','CLOSE']目前本字段是非功能性字段，仅起到描述作用。建议准确定义。"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="指示最大值" prop="max">
        <el-input
          v-model="queryParams.max"
          placeholder="请输入指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="指示字符串长度" prop="maxlength">
        <el-input
          v-model="queryParams.maxlength"
          placeholder="请输入指示字符串长度。仅当dataType为string、DateTime时生效。"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="指示访问模式" prop="method">
        <el-input
          v-model="queryParams.method"
          placeholder="请输入指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="指示最小值" prop="min">
        <el-input
          v-model="queryParams.min"
          placeholder="请输入指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否必填" prop="required">
        <el-input
          v-model="queryParams.required"
          placeholder="请输入指示本条属性是否必填，取值为0或1，默认取值1"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="指示步长。" prop="step">
        <el-input
          v-model="queryParams.step"
          placeholder="请输入指示步长。"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="指示单位" prop="unit">
        <el-input
          v-model="queryParams.unit"
          placeholder="请输入指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：C或K•百分比单位：%•压强单位：Pa或kPa"
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
          v-hasPermi="['link:properties:add']"
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
          v-hasPermi="['link:properties:edit']"
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
          v-hasPermi="['link:properties:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['link:properties:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="propertiesList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="属性id" align="center" prop="id" />
      <el-table-column label="指示属性名称。" align="center" prop="name" />
      <el-table-column label="服务ID" align="center" prop="serviceId" />
      <el-table-column label="指示数据类型" align="center" prop="datatype" />
      <el-table-column label="属性描述" align="center" prop="description" />
      <el-table-column label="指示枚举值" align="center" prop="enumlist" />
      <el-table-column label="指示最大值" align="center" prop="max" />
      <el-table-column label="指示字符串长度" align="center" prop="maxlength" />
      <el-table-column label="指示访问模式" align="center" prop="method" />
      <el-table-column label="指示最小值" align="center" prop="min" />
      <el-table-column label="属性是否必填" align="center" prop="required" />
      <el-table-column label="指示步长。" align="center" prop="step" />
      <el-table-column label="指示单位" align="center" prop="unit" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['link:properties:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['link:properties:remove']"
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

    <!-- 添加或修改产品属性数据对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="指示属性名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入指示属性名称。" />
        </el-form-item>
        <el-form-item label="服务ID" prop="serviceId">
          <el-input v-model="form.serviceId" placeholder="请输入服务ID" />
        </el-form-item>
        <el-form-item label="属性描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入属性描述，不影响实际功能，可配置为空字符串" />
        </el-form-item>
        <el-form-item label="指示枚举值" prop="enumlist">
          <el-input v-model="form.enumlist" placeholder="请输入指示枚举值" />
        </el-form-item>
        <el-form-item label="指示最大值" prop="max">
          <el-input v-model="form.max" placeholder="请输入指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。" />
        </el-form-item>
        <el-form-item label="指示字符串长度" prop="maxlength">
          <el-input v-model="form.maxlength" placeholder="请输入指示字符串长度。仅当dataType为string、DateTime时生效。" />
        </el-form-item>
        <el-form-item label="指示访问模式" prop="method">
          <el-input v-model="form.method" placeholder="请输入指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE" />
        </el-form-item>
        <el-form-item label="指示最小值" prop="min">
          <el-input v-model="form.min" placeholder="请输入指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。" />
        </el-form-item>
        <el-form-item label="是否必填" prop="required">
          <el-input v-model="form.required" placeholder="请输入指示本条属性是否必填，取值为0或1，默认取值1" />
        </el-form-item>
        <el-form-item label="指示步长。" prop="step">
          <el-input v-model="form.step" placeholder="请输入指示步长。" />
        </el-form-item>
        <el-form-item label="指示单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：C或K•百分比单位：%•压强单位：Pa或kPa" />
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
import { listProperties, getProperties, delProperties, addProperties, updateProperties } from "@/api/link/productProperties";

export default {
  name: "Properties",
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
      // 产品属性数据表格数据
      propertiesList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        serviceId: null,
        datatype: null,
        description: null,
        enumlist: null,
        max: null,
        maxlength: null,
        method: null,
        min: null,
        required: null,
        step: null,
        unit: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "指示属性名称。不能为空", trigger: "blur" }
        ],
        serviceId: [
          { required: true, message: "服务ID不能为空", trigger: "blur" }
        ],
        datatype: [
          { required: true, message: "指示数据类型：取值范围：string、int、decimal不能为空", trigger: "change" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询产品属性数据列表 */
    getList() {
      this.loading = true;
      listProperties(this.queryParams).then(response => {
        this.propertiesList = response.rows;
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
        name: null,
        serviceId: null,
        datatype: null,
        description: null,
        enumlist: null,
        max: null,
        maxlength: null,
        method: null,
        min: null,
        required: null,
        step: null,
        unit: null,
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
      this.title = "添加产品属性数据";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getProperties(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改产品属性数据";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateProperties(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addProperties(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除产品属性数据编号为"' + ids + '"的数据项？').then(function() {
        return delProperties(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('link/properties/export', {
        ...this.queryParams
      }, `properties_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
