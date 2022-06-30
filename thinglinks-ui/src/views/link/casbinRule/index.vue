<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="类型" prop="ptype">
        <el-select v-model="queryParams.ptype" placeholder="请选择类型" clearable size="small">
          <el-option v-for="dict in dict.type.link_device_protocol_type" :key="dict.value" :label="dict.label"
            :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="规则名称" prop="v0">
        <el-input v-model="queryParams.v0" placeholder="请输入规则名称" clearable size="small"
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="资源" prop="v1">
        <el-input v-model="queryParams.v1" placeholder="请输入资源" clearable size="small"
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="动作" prop="v2">
        <el-select v-model="queryParams.v2" placeholder="请选择动作" clearable size="small">
          <el-option v-for="dict in dict.type.link_casbinRule_v2" :key="dict.value" :label="dict.label"
            :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="策略" prop="v3">
        <el-select v-model="queryParams.v3" placeholder="请选择策略" clearable size="small">
          <el-option v-for="dict in dict.type.link_casbinRule_v3" :key="dict.value" :label="dict.label"
            :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['link:casbinRule:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['link:casbinRule:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['link:casbinRule:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['link:casbinRule:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="casbinRuleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="id" align="center" prop="id" />
      <el-table-column label="类型" align="center" prop="ptype">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_protocol_type" :value="scope.row.ptype" />
        </template>
      </el-table-column>
      <el-table-column label="规则名称" align="center" prop="v0" />
      <el-table-column label="资源" align="center" prop="v1" />
      <el-table-column label="动作" align="center" prop="v2">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_casbinRule_v2" :value="scope.row.v2" />
        </template>
      </el-table-column>
      <el-table-column label="策略" align="center" prop="v3">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_casbinRule_v3" :value="scope.row.v3" />
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-tooltip class="item" effect="light" content="修改" placement="top">
            <el-button circle size="mini" type="primary" icon="el-icon-edit" @click="handleUpdate(scope.row)"
              v-hasPermi="['link:casbinRule:edit']">
            </el-button>
          </el-tooltip>
          <el-tooltip class="item" effect="light" content="删除" placement="top">
            <el-button circle size="mini" type="primary" icon="el-icon-delete" @click="handleDelete(scope.row)"
              v-hasPermi="['link:casbinRule:remove']">
            </el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改CAS规则管理对话框 -->
    <el-dialog :title="title" :close-on-click-modal="false" :visible.sync="open" width="40%" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="22" style="width:80%">
            <el-form-item label=" 类型" prop="ptype">
              <el-select v-model="form.ptype" placeholder="请选择类型" style="width:100%">
                <el-option v-for="dict in dict.type.link_device_protocol_type" :key="dict.value" :label="dict.label"
                  :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip class="item" effect="light" content="CAS策略匹配协议，默认MQTT" placement="right-start">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22" style="width:80%">
            <el-form-item label="规则名称" prop="v0">
              <el-input :disabled="disabled" :placeholder="prompt" v-model="ruleName" class="input-with-select">
                <el-select v-model="form.v0" slot="prepend" placeholder="请选择" @change="setContene">
                  <el-option label="clientId" value="1"></el-option>
                  <el-option label="IP网段" value="2"></el-option>
                  <el-option label="all" value="3"></el-option>
                </el-select>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip class="item" effect="light" :content="contene" placement="right-start">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22" style="width:80%">
            <el-form-item label="资源" prop="v1">
              <el-input v-model="form.v1" placeholder="请输入资源" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip class="item" effect="light" content="资源支持类型如下：Topic配置（支持通配符Topic、例如拦截以test开头的Topic：test/*）"
              placement="right-start">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22" style="width:80%">
            <el-form-item label="动作" prop="v2">
              <el-select v-model="form.v2" placeholder="请选择动作" style="width:100%">
                <el-option v-for="dict in dict.type.link_casbinRule_v2" :key="dict.value" :label="dict.label"
                  :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip class="item" effect="light" content="设备触发动作" placement="right-start">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22" style="width:80%">
            <el-form-item label="策略" prop="v3">
              <el-select v-model="form.v3" placeholder="请选择策略" style="width:100%">
                <el-option v-for="dict in dict.type.link_casbinRule_v3" :key="dict.value" :label="dict.label"
                  :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip class="item" effect="light" content="执行策略" placement="right-start">
              <i class="el-icon-question" />
            </el-tooltip>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCasbinRule, getCasbinRule, delCasbinRule, addCasbinRule, updateCasbinRule } from "@/api/link/casbinRule";

export default {
  name: "CasbinRule",
  dicts: ['link_device_protocol_type', 'link_casbinRule_v2', 'link_casbinRule_v3'],
  data() {
    return {
      ruleName: "",
      prompt: "请输入规则名称",
      contene: "规则名称支持类型如下：clientId（限制clientId）、IP网段（限制ip或网段）、all（限制所有）",
      disabled: false,
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
      // CAS规则管理表格数据
      casbinRuleList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        ptype: null,
        v0: null,
        v1: null,
        v2: null,
        v3: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        ptype: [
          { required: true, message: "类型不能为空", trigger: "change" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    setContene() {
      // console.log(this.form.v0);
      if (this.form.v0 === '1') {
        this.disabled = false
        this.ruleName = ''
        this.prompt = '请输入clientId'
        this.contene = '限制该clientId'
      } else if (this.form.v0 === '2') {
        this.disabled = false
        this.ruleName = ''
        this.contene = '限制该ip或网段,示例格式：ip{192.168.0.120}、ip{192.168.0.120/24}'
        this.prompt = '请输入ip或网段信息'
      } else if (this.form.v0 === '3') {
        this.disabled = true
        this.ruleName = 'all';
        this.contene = 'all限制所有';
        this.prompt = "输入框值默认为all";
      }
    },
    /** 查询CAS规则管理列表 */
    getList() {
      this.loading = true;
      listCasbinRule(this.queryParams).then(response => {
        this.casbinRuleList = response.rows;
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
        ptype: null,
        v0: null,
        v1: null,
        v2: null,
        v3: null,
        v4: null,
        v5: null
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
      this.open = true;
      this.title = "添加CAS规则管理";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getCasbinRule(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改CAS规则管理";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateCasbinRule(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addCasbinRule(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除CAS规则管理编号为"' + ids + '"的数据项？').then(function () {
        return delCasbinRule(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => { });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('link/casbinRule/export', {
        ...this.queryParams
      }, `link_casbinRule.xlsx`)
    }
  }
};
</script>
