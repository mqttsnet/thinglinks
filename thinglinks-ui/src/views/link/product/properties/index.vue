<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="属性名称" prop="name">
        <el-input
          v-model="queryParams.name"
          clearable
          placeholder="请输入属性名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="属性描述" prop="description">
        <el-input
          v-model="queryParams.description"
          clearable
          placeholder="请输入属性描述"
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
          v-hasPermi="['link:productProperties:add']"
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
          v-hasPermi="['link:productProperties:edit']"
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
          v-hasPermi="['link:productProperties:remove']"
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
          v-hasPermi="['link:productProperties:export']"
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

    <el-table v-loading="loading" :data="propertiesList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="属性id" prop="id"/>
      <!--      <el-table-column align="center" label="服务ID" prop="serviceId"/>-->
      <el-table-column align="center" label="属性名称" prop="name"/>
      <el-table-column align="center" label="属性描述" prop="description"/>
      <el-table-column align="center" label="数据类型" prop="datatype"/>
      <el-table-column align="center" label="字符串长度" prop="maxlength"/>
      <el-table-column align="center" label="最小值" prop="min"/>
      <el-table-column align="center" label="最大值" prop="max"/>
      <el-table-column align="center" label="是否必填" prop="required">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_product_isRequired" :value="scope.row.required"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="步长" prop="step"/>
      <el-table-column align="center" label="单位" prop="unit"/>
      <el-table-column align="center" label="访问模式" prop="method"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['link:productProperties:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['link:productProperties:remove']"
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

    <!-- 添加或修改产品属性数据对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="700px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="服务ID" prop="serviceId">
          <el-input v-model="form.serviceId" disabled placeholder="请输入服务ID"/>
        </el-form-item>
        <el-form-item label="属性名称" prop="name">
          <el-col :span="22">
            <el-input v-model="form.name" autocomplete="off" placeholder="请输入指示属性名称"/>
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip content="指示属性名称。支持英文小写、数字及下划线，全部小写命名，禁止出现英文大写，多个单词用下划线，分隔长度[2,50]" effect="light"
                        placement="right">
              <i class="el-icon-question"/>
            </el-tooltip>
          </el-col>
        </el-form-item>
        <el-form-item label="数据类型" prop="datatype">
          <el-select v-model="form.datatype" placeholder="请选择数据类型" @change="changeDataType">
            <el-option v-for="item in dict.type.link_product_datatype" :key="item.value" :label="item.label"
                       :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否必填" prop="required">
          <el-select v-model="form.required" placeholder="请选择是否必填">
            <el-option v-for="item in dict.type.link_product_isRequired" :key="item.value" :label="item.label"
                       :value="parseInt(item.value)">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="属性描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入属性描述，不影响实际功能，可配置为空字符串" type="textarea"/>
        </el-form-item>
        <el-form-item label="枚举值">
          <el-input v-model="form.enumlist"
                    placeholder="请输入指示枚举值:如开关状态status可有如下取值'enumList' : ['OPEN','CLOSE'] 目前本字段是非功能性字段，仅起到描述作用。建议准确定义。"/>
        </el-form-item>
        <el-form-item v-if="this.show===1" label="字符串长度" prop="maxlength">
          <el-input v-model="form.maxlength" placeholder="请输入指示字符串长度。仅当dataType为string、DateTime时生效。"/>
        </el-form-item>
        <el-form-item v-if="this.show===2" label="最小值" prop="min">
          <el-input v-model="form.min" placeholder="请输入指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。"/>
        </el-form-item>
        <el-form-item v-if="this.show===2" label="最大值" prop="max">
          <el-input v-model="form.max" placeholder="请输入指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。"/>
        </el-form-item>
        <el-form-item v-if="this.show===2" label="步长" prop="step">
          <el-input v-model="form.step" placeholder="请输入指示步长"/>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：C或K•百分比单位：%•压强单位：Pa或kPa"/>
        </el-form-item>
        <el-form-item label="访问模式" prop="method">
          <el-col :span="22">
          <el-select v-model="form.method" placeholder="请选择访问模式">
            <el-option v-for="item in methodList" :key="item.value" :label="item.label"
                       :value="item.value">
            </el-option>
          </el-select>
          </el-col>
          <el-col :span="2" style="padding-left: 5px">
            <el-tooltip content="请输入指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE" effect="light"
                        placement="right">
              <i class="el-icon-question"/>
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
import {
  listProperties,
  getProperties,
  delProperties,
  addProperties,
  updateProperties
} from "@/api/link/product/productProperties";

export default {
  name: "Properties",
  props: ["serviceId"],
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
      // 产品属性数据表格数据
      propertiesList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 数据类型切换对应的显示
      show: null,
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
      methodList: [
        { value: "R", label: "可读" },
        { value: "RW", label: "读写" },
        { value: "RE", label: "可读上报" },
        { value: "RWE", label: "读写上报" }
      ],
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        serviceId: [
          {required: true, message: "服务ID不能为空", trigger: "blur"}
        ],
        name: [
          {required: true, message: "属性名称。不能为空", trigger: "blur"},
          {min: 2, max: 50, message: '属性名称长度必须介于 2 和 50 之间', trigger: 'blur'},
          {
            pattern: /^[a-z0-9_]+$/,
            message: "英文小写、数字、下划线，长度[2,50]",
            trigger: "blur"
          }
        ],
        datatype: [
          {required: true, message: "数据类型不能为空", trigger: "change"}
        ],
        maxlength: [
          {required: true, message: "字符串长度不能为空", trigger: "blur"},
          {
            pattern: /^[0-9]+$/,
            message: "请输入合法数字",
            trigger: "blur"
          }
        ],
        max: [
          {
            pattern: /^[0-9]+$/,
            message: "请输入合法数字",
            trigger: "blur"
          }
        ],
        min: [
          {
            pattern: /^[0-9]+$/,
            message: "请输入合法数字",
            trigger: "blur"
          }
        ],
        step: [
          {required: true, message: "请输入指示步长", trigger: "blur"},
          {
            pattern: /^[0-9]+$/,
            message: "请输入合法数字",
            trigger: "blur"
          }
        ],
        required: [{required: true, message: "是否必须不能为空", trigger: "blur"},],
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
    /** 查询产品属性数据列表 */
    getList() {
      this.loading = true;
      listProperties(this.queryParams).then(response => {
        this.propertiesList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    changeDataType(value) {
      switch (value) {
        case "string":
          this.show = 1;
          break;
        case "binary":
          this.show = 1;
          break;
        case "int":
          this.show = 2;
          break;
        case "decimal":
          this.show = 2;
          break;
        default:
          this.show = null;
          break;
      }
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
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.form.serviceId = this.serviceId;
      this.form.required = 0;
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
      this.$modal.confirm('是否确认删除产品属性数据编号为"' + ids + '"的数据项？').then(function () {
        return delProperties(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('iot/properties/export', {
        ...this.queryParams
      }, `properties_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
