<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="产品标识" prop="productIdentification">
        <el-input v-model="queryParams.productIdentification" placeholder="请输入产品标识" clearable size="small"
          @keyup.enter.native="handleQuery" />

      </el-form-item>
      <el-form-item label="协议名称" prop="protocolName">
        <el-input v-model="queryParams.protocolName" placeholder="请输入协议名称" clearable size="small"
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="协议标识" prop="protocolIdentification">
        <el-input v-model="queryParams.protocolIdentification" placeholder="请输入协议标识" clearable size="small"
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="协议版本" prop="protocolVersion">
        <el-input v-model="queryParams.protocolVersion" placeholder="请输入协议版本" clearable size="small"
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="协议类型 ：" prop="protocolType">
        <el-select v-model="queryParams.protocolType" placeholder="请选择协议类型" clearable size="small">
          <el-option v-for="dict in dict.type.link_device_protocol_type" :key="dict.value" :label="dict.label"
            :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="协议语言" prop="protocolVoice">
        <el-select v-model="queryParams.protocolVoice" placeholder="请选择协议语言" clearable size="small">
          <el-option v-for="dict in dict.type.link_protocol_voice" :key="dict.value" :label="dict.label"
            :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option v-for="dict in dict.type.business_data_status" :key="dict.value" :label="dict.label"
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
          v-hasPermi="['link:protocol:add']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['link:protocol:edit']">修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['link:protocol:remove']">删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['link:protocol:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="protocolList" @selection-change="handleSelectionChange"
      style=" width:99%;margin: auto;" max-height="500px">
      <el-table-column type="selection" width="55" align="center" />
      <!--      <el-table-column label="id" align="center" prop="id" />-->
      <el-table-column label="产品标识" align="center" prop="productIdentification" width="260" fixed />
      <el-table-column label="集成应用" align="center" prop="appId" width="100" fixed />
      <el-table-column label="协议名称" align="center" prop="protocolName" fixed width="240" />
      <el-table-column label="协议标识" align="center" prop="protocolIdentification" />
      <el-table-column label="协议版本" align="center" prop="protocolVersion" />
      <el-table-column label="协议类型" align="center" prop="protocolType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_protocol_type" :value="scope.row.protocolType" />
        </template>
      </el-table-column>
      <el-table-column label="协议语言" align="center" prop="protocolVoice">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_protocol_voice" :value="scope.row.protocolVoice" />
        </template>
      </el-table-column>
      <el-table-column label="类名" align="center" prop="className" />
      <el-table-column label="文件地址" align="center" prop="filePath" />
      <!--      <el-table-column label="内容" align="center" prop="content"/>-->
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.business_data_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['link:protocol:edit']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['link:protocol:remove']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改协议管理对话框 -->
    <el-dialog :title="title" :close-on-click-modal="false" :visible.sync="open" width="48%" append-to-body
      @opened="opedned" @closed="closed">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row style="display: flex;justify-content: space-around;">
          <el-col :span="11">
            <el-form-item label="产品标识" prop="productIdentification">
              <el-input v-model="form.productIdentification" placeholder="请输入产品标识" />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="集成应用" prop="appId">
              <el-select v-model="form.appId" placeholder="请选择集成应用" style="width: 100%;">
                <el-option v-for="dict in dict.type.link_application_type" :key="dict.value" :label="dict.label"
                  :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display: flex;justify-content: space-around;">
          <el-col :span="11">
            <el-form-item label="协议名称" prop="protocolName">
              <el-input v-model="form.protocolName" placeholder="请输入协议名称" />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="协议标识" prop="protocolIdentification">
              <el-input v-model="form.protocolIdentification" placeholder="请输入协议标识" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display: flex;justify-content: space-around;">
          <el-col :span="11">
            <el-form-item label="协议版本" prop="protocolVersion">
              <el-input v-model="form.protocolVersion" placeholder="请输入协议版本" />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="协议类型" prop="protocolType">
              <el-select v-model="form.protocolType" placeholder="请选择协议类型" style="width: 100%;">
                <el-option v-for="dict in dict.type.link_device_protocol_type" :key="dict.value" :label="dict.label"
                  :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display: flex;justify-content: space-around;">
          <el-col :span="11">
            <el-form-item label="协议语言" prop="protocolVoice">
              <el-select v-model="form.protocolVoice" placeholder="请选择协议语言" style="width: 100%;">
                <el-option v-for="dict in dict.type.link_protocol_voice" :key="dict.value" :label="dict.label"
                  :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="类名" prop="className">
              <el-input v-model="form.className" placeholder="请输入类名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display: flex;justify-content: space-around;">
          <el-col :span="11">
            <el-form-item label="文件地址" prop="filePath">
              <el-input v-model="form.filePath" placeholder="请输入文件地址" />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%;">
                <el-option v-for="dict in dict.type.business_data_status" :key="dict.value" :label="dict.label"
                  :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display: flex;justify-content: space-around;">
          <el-col :span="23" style="padding: 0 10px ;box-sizing: border-box;">
            <el-form-item label="脚本内容">
              <theCodeEditor ref="CodeEditor" :CodeEditor="CodeEditor" @codeValue="codeValue"></theCodeEditor>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display: flex;justify-content: space-around;">
          <el-col :span="23" style="padding: 0 10px ;box-sizing: border-box;">
            <el-form-item label="脚本入参">
              <el-input style="width: 80%;" v-model="inParameter" :autosize="{ minRows: 2, maxRows: 4 }" :rows="2"
                type="textarea" placeholder="请输入脚本入参" />
              <el-button type="info" size="mini" :disabled="this.form.content && this.inParameter ? false : true" round
                style="margin: 10px 32px;" @click="runTheCheck">
                编译运行
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display: flex;justify-content: space-around;">
          <el-col :span="23" style="padding: 0 10px ;box-sizing: border-box;">
            <el-form-item v-show="runningResults" label="运行结果" prop="remark">
              <!-- <el-input v-model="runningResults" :rows="6" type="textarea" placeholder="" /> -->
              <div style=" background: rgb(65, 54, 114);height: 100px;overflow-y: auto;
                color: #ccc;
                font-family: Fira code, Fira Mono, Consolas, Menlo, Courier, monospace;
                font-size: 14px;
                line-height: 1.5;
                padding: 5px;">
                {{runningResults}}
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display: flex;justify-content: space-around;">
          <el-col :span="23" style="padding: 0 10px ;box-sizing: border-box;">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" :autosize="{ minRows: 2, maxRows: 4 }" :rows="2" type="textarea"
                placeholder="请输入备注" />
            </el-form-item>
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
  import {
    listProtocol,
    getProtocol,
    delProtocol,
    addProtocol,
    updateProtocol,
    dynamicallyXcode
  } from "@/api/link/protocol";
  import theCodeEditor from "./TheCodeEditor/theCodeEditor"
  export default {
    name: "Protocol",
    dicts: ['link_device_protocol_type', 'link_protocol_voice', 'business_data_status', "link_application_type", ],
    components: {
      theCodeEditor
    },
    data() {
      return {
        //运行结果
        runningResults: "",
        //入参
        inParameter: "",
        //代码编辑器
        CodeEditor: "",
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
        // 协议管理表格数据
        protocolList: [],
        // 修改状态
        status: '0',
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          productIdentification: null,
          protocolName: null,
          protocolIdentification: null,
          protocolVersion: null,
          protocolType: null,
          protocolVoice: null,
          status: null,
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          appId: [{
            required: true,
            message: "应用ID不能为空",
            trigger: "blur"
          }],
          productIdentification: [{
            required: true,
            message: "产品标识不能为空",
            trigger: "blur"
          }],
          status: [{
            required: true,
            message: "状态(字典值：0启用  1停用)不能为空",
            trigger: "change"
          }],
        }
      };
    },
    created() {
      this.getList();
    },
    methods: {
      //运行校验
      runTheCheck() {
        console.log(this.form.content, this.inParameter);
        let data = {
          code: this.form.content,
          inparam: this.inParameter
        }
        // let data = {
        //   code: JSON.stringify(this.form.content),
        //   inparam: JSON.stringify(this.inParameter)
        // }
        // dynamicallyXcode(data).then(res => {
        //   console.log(res);
        // })
        this.runningResults = {
          "msg": null,
          "code": 500
        }
      },
      codeValue(e) {
        this.form.content = e
      },
      opedned() {
        if (this.form.id != null) {
          this.$refs.CodeEditor.code = this.form.content
        }
      },
      closed() {
        this.$refs.CodeEditor.code = ''
      },
      /** 查询协议管理列表 */
      getList() {
        this.loading = true;
        listProtocol(this.queryParams).then(response => {
          this.protocolList = response.rows;
          this.total = response.total;
          this.loading = false;
        });
      },
      // 取消按钮
      cancel() {
        this.open = false;
        this.reset();
        this.CodeEditor = ''
        this.form.content = ''
      },
      // 表单重置
      reset() {
        this.form = {
          id: null,
          appId: null,
          productIdentification: null,
          protocolName: null,
          protocolIdentification: null,
          protocolVersion: null,
          protocolType: null,
          protocolVoice: null,
          className: null,
          filePath: null,
          content: null,
          status: null,
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
        this.form.status = "0";
        this.status = "1";
        this.open = true;
        this.CodeEditor = ''
        this.title = "添加协议管理";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id || this.ids;
        this.status = row.status;
        getProtocol(id).then(response => {
          this.form = response.data;
          this.form.content = response.data.content
          this.open = true;
          this.title = "修改协议管理";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              this.form.content = this.$refs.CodeEditor.code
              updateProtocol(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              this.form.content = this.$refs.CodeEditor.code
              addProtocol(this.form).then(response => {
                this.$modal.msgSuccess("新增成功");
                this.$refs.CodeEditor.code = ''
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
        this.$modal.confirm('是否确认删除协议管理编号为"' + ids + '"的数据项？').then(function() {
          return delProtocol(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('link/protocol/export', {
          ...this.queryParams
        }, `link_protocol.xlsx`)
      }
    }
  };
</script>
