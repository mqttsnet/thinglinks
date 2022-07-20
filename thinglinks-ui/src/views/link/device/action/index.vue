<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px">
<!--      <el-form-item label="设备标识" prop="deviceIdentification">-->
<!--        <el-input-->
<!--          v-model="queryParams.deviceIdentification"-->
<!--          clearable-->
<!--          placeholder="请输入设备标识"-->
<!--          size="small"-->
<!--          @keyup.enter.native="handleQuery"-->
<!--        />-->
<!--      </el-form-item>-->
      <el-form-item label="动作类型" prop="actionType">
        <el-select v-model="queryParams.actionType" clearable placeholder="请选择动作类型" size="small">
          <el-option v-for="dict in dict.type.link_device_action_type" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态" size="small">
          <el-option v-for="dict in dict.type.business_data_status" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['link:action:add']"
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
          v-hasPermi="['link:action:edit']"
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
          v-hasPermi="['link:action:remove']"
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
          v-hasPermi="['link:action:export']"
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

    <el-table v-loading="loading" :data="actionList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="id" prop="id"/>
      <el-table-column align="center" label="设备标识" prop="deviceIdentification"/>
      <el-table-column align="center" label="动作类型" prop="actionType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_action_type" :value="scope.row.actionType"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="内容信息" prop="message"/>
      <el-table-column align="center" label="状态" prop="status"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['link:action:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['link:action:remove']"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="handleDelete(scope.row)"
          >删除
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

    <!-- 添加或修改设备动作数据对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="设备标识" prop="deviceIdentification">
          <el-input v-model="form.deviceIdentification" disabled placeholder="请输入设备标识"/>
        </el-form-item>
        <el-form-item label="动作类型" prop="actionType">
          <el-select v-model="form.actionType" placeholder="请选择动作类型">
            <el-option v-for="dict in dict.type.link_device_action_type" :key="dict.value" :label="dict.label" :value="dict.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="内容信息" prop="message">
          <el-input v-model="form.message" placeholder="请输入内容" type="textarea"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" size="small">
            <el-option v-for="dict in dict.type.business_data_status" :key="dict.value" :label="dict.label" :value="dict.value"/>
          </el-select>
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
import {listAction, getAction, delAction, addAction, updateAction} from "@/api/link/device/action";

export default {
  name: "Action",
  props: ["deviceIdentification"],
  dicts: ['link_device_action_type', "business_data_status"],
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
      // 设备动作数据表格数据
      actionList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceIdentification: null,
        actionType: null,
        message: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {}
    };
  },
  watch: {
    deviceIdentification(newval, oldval) {
      if (newval !== oldval) {
        this.queryParams.deviceIdentification = this.deviceIdentification;
        this.getList();
      }
    }
  },
  created() {
    if (this.deviceIdentification)
      this.queryParams.deviceIdentification = this.deviceIdentification;
    this.getList();
  },
  methods: {
    /** 查询设备动作数据列表 */
    getList() {
      this.loading = true;
      listAction(this.queryParams).then(response => {
        this.actionList = response.rows;
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
        deviceIdentification: null,
        actionType: null,
        message: null,
        status: "0",
        createTime: null
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
      this.form.deviceIdentification = this.deviceIdentification;
      this.open = true;
      this.title = "添加设备动作数据";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getAction(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改设备动作数据";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateAction(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addAction(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除设备动作数据编号为"' + ids + '"的数据项？').then(function () {
        return delAction(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('link/action/export', {
        ...this.queryParams
      }, `link_action.xlsx`)
    }
  }
};
</script>
<style lang="scss" scoped>

.pagination-container {
  height: 50px;
}
</style>
