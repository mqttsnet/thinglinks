<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px">
      <!--      <el-form-item label="设备标识" prop="deviceIdentification">-->
      <!--        <el-input v-model="queryParams.deviceIdentification" clearable placeholder="请输入设备标识" size="small"-->
      <!--                  @keyup.enter.native="handleQuery" />-->
      <!--      </el-form-item>-->
      <el-form-item label="topic" prop="topic">
        <el-input v-model="queryParams.topic" clearable placeholder="请输入topic" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-tabs v-model="topicActiveName" style="width:100%;height:100%" @tab-click="topicSwitch">
      <el-tab-pane label="基础Topic" name="first" style="width:100%;height:100%">
        <el-table v-loading="loading" :data="topicList" max-height="500" style="width:100%" @selection-change="handleSelectionChange">
          <el-table-column align="center" type="selection" width="55"/>
          <el-table-column align="center" label="id" prop="id"/>
          <el-table-column align="center" label="设备标识" prop="deviceIdentification"/>
          <el-table-column align="center" label="topic" prop="topic"/>
          <el-table-column align="center" label="发布者" prop="publisher"/>
          <el-table-column align="center" label="订阅者" prop="subscriber"/>
          <el-table-column align="center" label="备注" prop="remark"/>
        </el-table>
        <pagination v-show="total > 0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total" @pagination="getList"/>
      </el-tab-pane>
      <el-tab-pane label="自定义Topic" name="second" style="width:100%;height: 100%;">
        <el-button v-hasPermi="['link:topic:add']" icon="el-icon-plus" plain size="mini" style="margin-bottom:10px"
                   type="primary" @click="handleAdd">新增
        </el-button>
        <el-button v-hasPermi="['link:topic:edit']" :disabled="single" icon="el-icon-edit" plain size="mini"
                   style="margin-bottom:10px" type="success" @click="handleUpdate">修改
        </el-button>
        <el-button v-hasPermi="['link:topic:delete']" :disabled="multiple" icon="el-icon-delete" plain size="mini"
                   style="margin-bottom:10px" type="danger" @click="handleDelete">删除
        </el-button>
        <el-button v-hasPermi="['link:topic:export']" icon="el-icon-download" plain size="mini"
                   style="margin-bottom:10px" type="warning" @click="handleExport">导出
        </el-button>
        <el-table v-loading="loading" :data="topicList" max-height="500" style="width:100%;height:100%" @selection-change="handleSelectionChange">
          <el-table-column align="center" type="selection" width="55"/>
          <el-table-column align="center" label="id" prop="id"/>
          <el-table-column align="center" label="设备标识" prop="deviceIdentification"/>
          <el-table-column align="center" label="topic" prop="topic"/>
          <el-table-column align="center" label="发布者" prop="publisher"/>
          <el-table-column align="center" label="订阅者" prop="subscriber"/>
          <el-table-column align="center" label="备注" prop="remark"/>
          <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
            <template slot-scope="scope">
              <el-button v-hasPermi="['link:topic:edit']" icon="el-icon-edit" size="mini" type="text" @click="handleUpdate(scope.row)">修改
              </el-button>
              <el-button v-hasPermi="['link:topic:remove']" icon="el-icon-delete" size="mini" type="text" @click="handleDelete(scope.row)">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total" @pagination="getList"/>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加或修改设备Topic数据对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="设备标识" prop="deviceIdentification">
          <el-input v-model="form.deviceIdentification" disabled placeholder="请输入设备标识"/>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="自定义Topic" value="1"/>
          </el-select>
        </el-form-item>
        <el-form-item label="topic" prop="topic">
          <el-input v-model="form.topic" placeholder="请输入topic"/>
        </el-form-item>
        <el-form-item label="发布者" prop="publisher">
          <el-input v-model="form.publisher" placeholder="请输入发布者"/>
        </el-form-item>
        <el-form-item label="订阅者" prop="subscriber">
          <el-input v-model="form.subscriber" placeholder="请输入订阅者"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入内容" type="textarea"/>
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
import {listTopic, getTopic, delTopic, addTopic, updateTopic} from "@/api/link/device/topic";

export default {
  name: "Topic",
  props: ["deviceIdentification"],
  dicts: [
    "link_device_device_type",
    "link_device_connect_status",
    "link_device_action_type",
  ],
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
      topicActiveName: "first",
      // 总条数
      total: 0,
      // 设备Topic数据表格数据
      topicList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceIdentification: null,
        type: null,
        topic: null,
        publisher: null,
        subscriber: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        deviceIdentification: [
          {required: true, message: "设备标识不能为空", trigger: "blur"}
        ],
        topic: [
          {required: true, message: "设备Topic不能为空", trigger: "blur"}
        ],
      }
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
    //切换topic列表
    topicSwitch() {
      if (this.topicActiveName === 'first') {
        this.add = false
        this.queryParams.type = 0
        this.getList()
      } else if (this.topicActiveName === 'second') {
        this.add = true
        this.queryParams.type = 1
        this.getList()
      }
    },
    /** 查询设备Topic数据列表 */
    getList() {
      this.loading = true;
      listTopic(this.queryParams).then(response => {
        this.topicList = response.rows;
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
        type: null,
        topic: null,
        publisher: null,
        subscriber: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
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
      this.title = "添加设备Topic数据";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getTopic(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改设备Topic数据";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateTopic(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addTopic(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除设备Topic数据编号为"' + ids + '"的数据项？').then(function () {
        return delTopic(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('link/topic/export', {
        ...this.queryParams
      }, `link_topic.xlsx`)
    }
  }
};
</script>
<style lang="scss" scoped>

.pagination-container {
  height: 50px;
}
</style>
