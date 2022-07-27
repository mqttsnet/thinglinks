<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <!--      <el-form-item label="服务ID" prop="serviceId">-->
      <!--        <el-input-->
      <!--          v-model="queryParams.serviceId"-->
      <!--          clearable-->
      <!--          placeholder="请输入服务ID"-->
      <!--          @keyup.enter.native="handleQuery"-->
      <!--        />-->
      <!--      </el-form-item>-->
      <el-form-item label="命令名称" prop="name">
        <el-input v-model="queryParams.name" clearable placeholder="请输入命令名称" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="命令描述" prop="description">
        <el-input v-model="queryParams.description" clearable placeholder="请输入命令描述" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:commands:add']" icon="el-icon-plus" plain size="mini" type="primary"
          @click="handleAdd">新增命令</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:commands:edit']" :disabled="single" icon="el-icon-edit" plain size="mini"
          type="success" @click="handleUpdate">修改命令</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:commands:remove']" :disabled="multiple" icon="el-icon-delete" plain size="mini"
          type="danger" @click="handleDelete">删除命令</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:commands:export']" icon="el-icon-download" plain size="mini" type="warning"
          @click="handleExport">导出命令</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="commandsList" @selection-change="handleSelectionChange" max-height="250px">
      <el-table-column align="center" type="selection" width="55" />
      <!--      <el-table-column align="center" label="命令id" prop="id" />-->
      <!--      <el-table-column align="center" label="服务ID" prop="serviceId" />-->
      <el-table-column align="center" label="命令名称" prop="name" />
      <el-table-column align="center" label="命令描述" prop="description" />
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button v-hasPermi="['link:commands:edit']" icon="el-icon-edit" size="mini" type="text"
            @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['link:commands:remove']" icon="el-icon-delete" size="mini" type="text"
            @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total"
      @pagination="getList" />

    <!-- 添加或修改产品指令数据对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width='700px'>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="命令名字" prop="name">
          <el-col :span="23">
            <el-input v-model="form.name" placeholder="请输入命令名字" />
          </el-col>
          <el-col :span="1" style="padding-left: 5px">
            <el-tooltip content="命令名称，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。支持英文大小写、数字及下划线，长度[2,50]。"
              effect="light" placement="right">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-form-item>
        <el-form-item label="命令描述" prop="description">
          <el-col :span="23">
            <el-input v-model="form.description" type="textarea" placeholder="请输入命令描述" />
          </el-col>
        </el-form-item>
        <el-form-item v-if="form.id" label="下发参数">
          <Requests :commandsId="form.id" :serviceId="form.serviceId"></Requests>
        </el-form-item>
        <el-form-item v-if="form.id" label="响应参数">
          <Response :commandsId="form.id" :serviceId="form.serviceId"></Response>
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
import { listCommands, getCommands, delCommands, addCommands, updateCommands } from "@/api/link/product/productCommands";
import Requests from "@/views/link/product/commands/requests";
import Response from "@/views/link/product/commands/response";

export default {
  name: "Commands",
  components: { Response, Requests },
  props: {
    "serviceId": {
      type: Number,
      required: true
    }
  },
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
      // 产品指令数据表格数据
      commandsList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        serviceId: null,
        name: null,
        description: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        serviceId: [
          { required: true, message: "服务ID不能为空", trigger: "blur" }
        ],
        name: [
          { required: true, message: "命令名字不能为空", trigger: "blur" },
          {
            pattern: /^[A-Za-z0-9_]+$/,
            message: "英文大小写、数字、下划线，长度[2,50]",
            trigger: "blur"
          }
        ],
      }
    };
  },
  watch: {
    serviceId(newval, oldval) {
      if (newval !== oldval) {
        this.queryParams.serviceId = newval;
        this.getList();
      }
    }
  },
  created() {
    if (this.serviceId)
      this.queryParams.serviceId = this.serviceId;
    this.getList();
  },
  methods: {
    /** 查询产品指令数据列表 */
    getList() {
      this.loading = true;
      listCommands(this.queryParams).then(response => {
        this.commandsList = response.rows;
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
        serviceId: null,
        name: null,
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
      this.form.serviceId = this.serviceId;
      this.open = true;
      this.title = "添加命令";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getCommands(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改命令";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateCommands(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addCommands(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除产品指令数据编号为"' + ids + '"的数据项？').then(function () {
        return delCommands(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => { });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('link/product/commands/export', {
        ...this.queryParams
      }, `commands_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
<style lang="scss" scoped>
.pagination-container {
  height: 50px;
}
</style>
