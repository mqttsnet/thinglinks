<template>
  <div class="app-container">
    <el-form v-if="false" v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px"
      size="small">
      <!--      <el-form-item label="服务ID" prop="serviceId">-->
      <!--        <el-input-->
      <!--          v-model="queryParams.serviceId"-->
      <!--          clearable-->
      <!--          placeholder="请输入服务ID"-->
      <!--          @keyup.enter.native="handleQuery"-->
      <!--        />-->
      <!--      </el-form-item>-->
      <!--      <el-form-item label="命令ID" prop="commandsId">-->
      <!--        <el-input-->
      <!--          v-model="queryParams.commandsId"-->
      <!--          clearable-->
      <!--          placeholder="请输入命令ID"-->
      <!--          @keyup.enter.native="handleQuery"-->
      <!--        />-->
      <!--      </el-form-item>-->
      <el-form-item label="参数名字" prop="parameterName">
        <el-input v-model="queryParams.parameterName" clearable placeholder="请输入命令中参数的名字"
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="参数描述" prop="parameterDescription">
        <el-input v-model="queryParams.parameterDescription" clearable placeholder="请输入命令中参数的描述"
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-tooltip content="新增响应参数" effect="light" placement="top">
          <el-button v-hasPermi="['link:response:add']" icon="el-icon-plus" circle size="mini" type="primary"
            @click="handleAdd"></el-button>
        </el-tooltip>
      </el-col>
      <el-col :span="1.5">
        <el-tooltip content="修改响应参数" effect="light" placement="top">
          <el-button v-hasPermi="['link:response:edit']" :disabled="single" icon="el-icon-edit" circle size="mini"
            type="success" @click="handleUpdate"></el-button>
        </el-tooltip>
      </el-col>
      <el-col :span="1.5">
        <el-tooltip content="删除响应参数" effect="light" placement="top">
          <el-button v-hasPermi="['link:response:remove']" :disabled="multiple" icon="el-icon-delete" circle size="mini"
            type="danger" @click="handleDelete"></el-button>
        </el-tooltip>
      </el-col>
      <el-col :span="1.5">
        <el-tooltip content="导出响应参数" effect="light" placement="top">
          <el-button v-hasPermi="['link:response:export']" icon="el-icon-download" circle size="mini" type="warning"
            @click="handleExport"></el-button>
        </el-tooltip>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="responseList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <!--      <el-table-column align="center" label="id" prop="id" />-->
      <!--      <el-table-column align="center" label="服务ID" prop="serviceId" />-->
      <!--      <el-table-column align="center" label="命令ID" prop="commandsId" />-->
      <el-table-column align="center" label="参数名称" prop="parameterName" />
      <el-table-column align="center" label="数据类型" prop="datatype" />
      <el-table-column align="center" label="描述" prop="parameterDescription" />
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button v-hasPermi="['link:response:edit']" icon="el-icon-edit" size="mini" type="text"
            @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['link:response:remove']" icon="el-icon-delete" size="mini" type="text"
            @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total"
      @pagination="getList" />

    <!-- 添加或修改产品模型设备响应服务命令属性对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="服务ID" prop="serviceId">
          <el-input v-model="form.serviceId" disabled placeholder="请输入服务ID" />
        </el-form-item>
        <el-form-item label="命令ID" prop="commandsId">
          <el-input v-model="form.commandsId" disabled placeholder="请输入命令ID" />
        </el-form-item>
        <el-form-item label="参数名字" prop="parameterName">
          <el-input v-model="form.parameterName" placeholder="请输入命令中参数的名字。" />
        </el-form-item>
        <el-form-item label="参数描述" prop="parameterDescription">
          <el-input v-model="form.parameterDescription" placeholder="请输入命令中参数的描述，不影响实际功能，可配置为空字符串。" type="textarea" />
        </el-form-item>
        <el-form-item label="数据类型" prop="datatype">
          <el-select v-model="form.datatype" placeholder="请选择数据类型">
            <el-option v-for="item in dict.type.link_product_datatype" :key="item.value" :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否必填" prop="required">
          <el-radio-group v-model="form.required">
            <el-radio-button v-for="item in dict.type.link_product_isRequired" :label="parseInt(item.value)">
              {{ item.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="枚举值" prop="enumlist">
          <el-input v-model="form.enumlist" placeholder="请输入枚举值，多个以英文逗号分隔" />
        </el-form-item>
        <el-form-item v-if="form.datatype === 'string' || form.datatype === 'binary'" label="长度" prop="maxlength">
          <el-col :span="22">
            <el-input v-model="form.maxlength" placeholder="请输入字符串长度" />
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip content="请输入字符串长度。输入值大于等于0、小于等于2147483647的数字。" effect="light" placement="right">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-form-item>
        <el-form-item v-if="form.datatype === 'int' || form.datatype === 'decimal'" label="取值范围" prop="min">
          <el-col :span="10">
            <el-form-item label="" prop="min">
              <el-input v-model="form.min" placeholder="请输入最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。" />
            </el-form-item>
          </el-col>
          <el-col :span="2" style="text-align: center">-</el-col>
          <el-col :span="10">
            <el-form-item label="" prop="max">
              <el-input v-model="form.max" placeholder="请输入最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。" />
            </el-form-item>
          </el-col>
        </el-form-item>
        <el-form-item v-if="form.datatype === 'int' || form.datatype === 'decimal'" label="步长" prop="step">
          <el-input v-model="form.step" placeholder="请输入步长" />
        </el-form-item>
        <el-form-item v-if="form.datatype === 'int' || form.datatype === 'decimal'" label="单位" prop="unit">
          <el-col :span="22">
            <el-input v-model="form.unit" placeholder="请输入单位。支持长度不超过50。" />
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip content="请输入单位。支持长度不超过50。取值根据参数确定，如：•温度单位：C或K•百分比单位：%•压强单位：Pa或kPa" effect="light"
              placement="right">
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
import { listResponse, getResponse, delResponse, addResponse, updateResponse } from "@/api/link/product/productResponse";

export default {
  name: "Response",
  props: {
    "serviceId": {
      type: Number,
      required: true
    },
    "commandsId": {
      type: Number,
      required: true
    }
  },
  dicts: ["link_product_datatype", "link_product_isRequired"],
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
      // 产品模型设备响应服务命令属性表格数据
      responseList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        serviceId: null,
        commandsId: null,
        parameterName: null,
        parameterDescription: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        serviceId: [
          { required: true, message: "服务ID不能为空", trigger: "blur" }
        ],
        commandsId: [
          { required: true, message: "命令ID不能为空", trigger: "blur" }
        ],
        parameterName: [
          { required: true, message: "参数名称不能为空", trigger: "blur" }
        ],
        datatype: [
          { required: true, message: "数据类型不能为空", trigger: "change" }
        ],
        maxlength: [
          { required: true, message: "字符串长度不能为空", trigger: "blur" },
          {
            pattern: /^[0-9]+$/,
            message: "请输入合法数字",
            trigger: "blur"
          }
        ],
        min: [
          { required: true, message: "最小值不能为空", trigger: "blur" },
          {
            pattern: /^[0-9]+$/,
            message: "请输入合法数字",
            trigger: "blur"
          }
        ],
        max: [
          { required: true, message: "最大值不能为空", trigger: "blur" },
          {
            pattern: /^[0-9]+$/,
            message: "请输入合法数字",
            trigger: "blur"
          }
        ],
        step: [
          {
            pattern: /^[0-9]+$/,
            message: "请输入合法数字",
            trigger: "blur"
          }
        ],
        required: [{ required: true, message: "是否必须不能为空", trigger: "blur" },],
      }
    };
  },
  watch: {
    serviceId(newval, oldval) {
      if (newval !== oldval) {
        this.queryParams.serviceId = newval;
        this.getList();
      }
    },
    commandsId(newval, oldval) {
      if (newval !== oldval) {
        this.queryParams.commandsId = newval;
        this.getList();
      }
    }
  },
  created() {
    if (this.serviceId)
      this.queryParams.serviceId = this.serviceId;
    if (this.commandsId)
      this.queryParams.commandsId = this.commandsId;
    this.getList();
  },
  methods: {
    /** 查询产品模型设备响应服务命令属性列表 */
    getList() {
      this.loading = true;
      listResponse(this.queryParams).then(response => {
        this.responseList = response.rows;
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
        commandsId: null,
        parameterName: null,
        parameterDescription: null,
        datatype: null,
        enumlist: null,
        min: 0,
        max: 65535,
        maxlength: null,
        required: 1,
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
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.form.serviceId = this.serviceId;
      this.form.commandsId = this.commandsId;
      this.open = true;
      this.title = "添加参数";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getResponse(id).then(response => {
        console.log(this.form)
        this.form = response.data;
        this.open = true;
        this.title = "修改参数";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateResponse(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addResponse(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除参数编号为"' + ids + '"的数据项？').then(function () {
        return delResponse(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => { });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('link/product/commands/response/export', {
        ...this.queryParams
      }, `response_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
<style lang="scss" scoped>
.app-container {
  padding: 0 10px;
}
</style>