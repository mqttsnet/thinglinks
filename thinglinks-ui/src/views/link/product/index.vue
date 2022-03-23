<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="产品名称" prop="productName">
        <el-input
          v-model="queryParams.productName"
          placeholder="请输入产品名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="厂商ID" prop="manufacturerId">
        <el-input
          v-model="queryParams.manufacturerId"
          placeholder="请输入厂商ID"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="厂商名称" prop="manufacturerName">
        <el-input
          v-model="queryParams.manufacturerName"
          placeholder="请输入厂商名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品型号" prop="model">
        <el-input v-model="queryParams.model" placeholder="请输入产品型号"
                  clearable
                  size="small"
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
          v-hasPermi="['link:product:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['link:product:edit']"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['link:product:remove']"
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['link:product:export']"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="id" align="center" prop="id"/>
      <el-table-column label="应用ID" align="center" prop="appId">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_application_type" :value="scope.row.appId"/>
        </template>
      </el-table-column>
      <el-table-column label="产品名称" align="center" prop="productName"/>
      <el-table-column label="厂商ID" align="center" prop="manufacturerId"/>
      <el-table-column label="厂商名称" align="center" prop="manufacturerName"/>
      <el-table-column label="产品型号" align="center" prop="model"/>
      <el-table-column label="设备类型" align="center" prop="deviceType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_product_device_type" :value="scope.row.deviceType"/>
        </template>
      </el-table-column>
      <el-table-column label="协议类型" align="center" prop="protocolType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_protocol_type" :value="scope.row.protocolType"/>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="产品描述" align="center" prop="remark"/>
      <el-table-column label="创建者" align="center" prop="createBy" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新者" align="center" prop="updateBy" />
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['link:product:edit']"
          >修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['link:product:remove']"
          >删除
          </el-button>
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

    <!-- 添加或修改产品管理对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="40%" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="11">
            <el-form-item label="应用ID" prop="appId">
              <el-select v-model="form.appId" placeholder="请选择应用ID">
                <el-option
                  v-for="dict in dict.type.link_application_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="产品模型模板" prop="templateId">
              <el-input v-model="form.templateId" placeholder="请输入产品模型模板"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线"/>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="产品标识" prop="productIdentification">
              <el-input v-model="form.productIdentification" placeholder="请输入产品标识"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="产品类型" prop="productType">
              <el-select v-model="form.productType" placeholder="请选择支持以下两种产品类型•0：普通产品，需直连设备。•1：网关产品，可挂载子设备。">
                <el-option
                  v-for="dict in dict.type.link_product_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="厂商ID" prop="manufacturerId">
              <el-input v-model="form.manufacturerId" placeholder="请输入厂商ID:支持英文大小写，数字，下划线和中划线"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="厂商名称" prop="manufacturerName">
              <el-input v-model="form.manufacturerName" placeholder="请输入厂商名称 :支持中文、英文大小写、数字、下划线和中划线"/>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="产品型号" prop="model">
              <el-input v-model="form.model" placeholder="请输入产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="数据格式" prop="dataFormat">
              <el-input v-model="form.dataFormat" placeholder="请输入数据格式，默认为JSON无需修改。"/>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="form.deviceType" placeholder="请选择设备类型:支持英文大小写、数字、下划线和中划线">
                <el-option
                  v-for="dict in dict.type.link_product_device_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="协议类型" prop="protocolType">
              <el-select v-model="form.protocolType" placeholder="请选择设备接入平台的协议类型，默认为MQTT无需修改。">
                <el-option
                  v-for="dict in dict.type.link_device_protocol_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态(字典值：启用  停用)">
                <el-option
                  v-for="dict in dict.type.sys_normal_disable"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="产品描述" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入产品描述"/>
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
import {listProduct, getProduct, delProduct, addProduct, updateProduct} from "@/api/link/product";

export default {
  name: "Product",
  dicts: ['link_application_type', 'link_product_device_type', 'link_product_type', 'link_device_protocol_type', 'sys_normal_disable'],
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
      // 产品管理表格数据
      productList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        productName: null,
        manufacturerId: null,
        manufacturerName: null,
        model: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        appId: [
          {required: true, message: "应用ID不能为空", trigger: "change"}
        ],
        productName: [
          {required: true, message: "产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线不能为空", trigger: "blur"}
        ],
        productIdentification: [
          {required: true, message: "产品标识不能为空", trigger: "blur"}
        ],
        productType: [
          {required: true, message: "支持以下两种产品类型•0：普通产品，需直连设备。•1：网关产品，可挂载子设备。不能为空", trigger: "change"}
        ],
        manufacturerId: [
          {required: true, message: "厂商ID:支持英文大小写，数字，下划线和中划线不能为空", trigger: "blur"}
        ],
        manufacturerName: [
          {required: true, message: "厂商名称 :支持中文、英文大小写、数字、下划线和中划线不能为空", trigger: "blur"}
        ],
        model: [
          {required: true, message: "产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线不能为空", trigger: "blur"}
        ],
        dataFormat: [
          {required: true, message: "数据格式，默认为JSON无需修改。不能为空", trigger: "blur"}
        ],
        deviceType: [
          {
            required: true, message: "设备类型:支持英文大小写、数字、下划线和中划线不能为空", trigger: "change" }
        ],
        protocolType: [
          {required: true, message: "设备接入平台的协议类型，默认为MQTT无需修改。不能为空", trigger: "change"}
        ],
        status: [
          {required: true, message: "状态(字典值：启用  停用)不能为空", trigger: "change"}
        ],
      }
    };
  },
    created()
    {
      this.getList();
    }
  ,
    methods: {
      /** 查询产品管理列表 */
      getList()
      {
        this.loading = true;
        listProduct(this.queryParams).then(response => {
          this.productList = response.rows;
          this.total = response.total;
          this.loading = false;
        });
      }
    ,
      // 取消按钮
      cancel()
      {
        this.open = false;
        this.reset();
      }
    ,
      // 表单重置
      reset()
      {
        this.form = {
          id: null,
          appId: null,
          templateId: null,
          productName: null,
          productIdentification: null,
          productType: null,
          manufacturerId: null,
          manufacturerName: null,
          model: null,
          dataFormat: null,
          deviceType: null,
          protocolType: null,
          status: null,
          remark: null,
          createBy: null,
          createTime: null,
          updateBy: null,
          updateTime: null
        };
        this.resetForm("form");
      }
    ,
      /** 搜索按钮操作 */
      handleQuery()
      {
        this.queryParams.pageNum = 1;
        this.getList();
      }
    ,
      /** 重置按钮操作 */
      resetQuery()
      {
        this.resetForm("queryForm");
        this.handleQuery();
      }
    ,
      // 多选框选中数据
      handleSelectionChange(selection)
      {
        this.ids = selection.map(item => item.id)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      }
    ,
      /** 新增按钮操作 */
      handleAdd()
      {
        this.reset();
        this.open = true;
        this.title = "添加产品管理";
      }
    ,
      /** 修改按钮操作 */
      handleUpdate(row)
      {
        this.reset();
        const id = row.id || this.ids
        getProduct(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改产品管理";
        });
      }
    ,
      /** 提交按钮 */
      submitForm()
      {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateProduct(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addProduct(this.form).then(response => {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              });
            }
          }
        });
      }
    ,
      /** 删除按钮操作 */
      handleDelete(row)
      {
        const ids = row.id || this.ids;
        this.$modal.confirm('是否确认删除产品管理编号为"' + ids + '"的数据项？').then(function () {
          return delProduct(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {
        });
      }
    ,
      /** 导出按钮操作 */
      handleExport()
      {
        this.download('link/product/export', {
          ...this.queryParams
        }, `link_product.xlsx`)
      }
    }
  };
</script>
