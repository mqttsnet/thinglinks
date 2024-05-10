<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="属性编码" prop="propertyCode">
        <el-input v-model="queryParams.propertyCode" clearable placeholder="请输入属性编码" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="属性名称" prop="propertyName">
        <el-input v-model="queryParams.propertyName" clearable placeholder="请输入属性名称" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="属性描述" prop="description">
        <el-input v-model="queryParams.description" clearable placeholder="请输入属性描述" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:productProperties:add']" icon="el-icon-plus" plain size="mini" type="primary"
          @click="handleAdd">新增属性
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:productProperties:edit']" :disabled="single" icon="el-icon-edit" plain size="mini"
          type="success" @click="handleUpdate">修改属性
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:productProperties:remove']" :disabled="multiple" icon="el-icon-delete" plain
          size="mini" type="danger" @click="handleDelete">删除属性
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:productProperties:export']" icon="el-icon-download" plain size="mini"
          type="warning" @click="handleExport">导出属性
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="propertiesList" @selection-change="handleSelectionChange" max-height="250px">
      <el-table-column align="center" type="selection" width="55" />
      <!--      <el-table-column align="center" label="属性id" prop="id"/>-->
      <!--      <el-table-column align="center" label="服务ID" prop="serviceId"/>-->
      <el-table-column align="center" label="属性编码" prop="propertyCode" />
      <el-table-column align="center" label="属性名称" prop="propertyName" />
      <el-table-column align="center" label="数据类型" prop="datatype" />
      <el-table-column align="center" label="访问方式" prop="method" />
      <el-table-column align="center" label="属性描述" prop="description" />
      <!--      <el-table-column align="center" label="字符串长度" prop="maxlength"/>-->
      <!--      <el-table-column align="center" label="最小值" prop="min"/>-->
      <!--      <el-table-column align="center" label="最大值" prop="max"/>-->
      <!--      <el-table-column align="center" label="数据定义">-->
      <!--        <template slot-scope="scope">-->
      <!--          <span-->
      <!--            v-if="scope.row.datatype === 'int' || scope.row.datatype === 'decimal'">取值范围：{{-->
      <!--              scope.row.min-->
      <!--            }} ~ {{ scope.row.max }}</span>-->
      <!--          <span v-else-if="scope.row.datatype === 'string'">字符串长度：{{ scope.row.maxlength }}</span>-->
      <!--        </template>-->
      <!--      </el-table-column>-->
      <!--      <el-table-column align="center" label="是否必填" prop="required">-->
      <!--        <template slot-scope="scope">-->
      <!--          <dict-tag :options="dict.type.link_product_isRequired" :value="scope.row.required"/>-->
      <!--        </template>-->
      <!--      </el-table-column>-->
      <!--      <el-table-column align="center" label="步长" prop="step"/>-->
      <!--      <el-table-column align="center" label="单位" prop="unit"/>-->
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button v-hasPermi="['link:productProperties:edit']" icon="el-icon-edit" size="mini" type="text"
            @click="handleUpdate(scope.row)">修改
          </el-button>
          <el-button v-hasPermi="['link:productProperties:remove']" icon="el-icon-delete" size="mini" type="text"
            @click="handleDelete(scope.row)">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total"
      @pagination="getList" />

    <!-- 添加或修改产品属性数据对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="700px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="medium">
        <el-row>
          <div class="disply">
            <div class="small">
              <el-form-item label="属性编码" prop="propertyCode">
                <el-col :span="20">
                  <el-input v-model="form.propertyCode" autocomplete="off" placeholder="请输入指示属性编码" />
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip content="指示属性编码。支持英文小写、数字及下划线，全部小写命名，禁止出现英文大写，多个单词用下划线，分隔长度[2,50]" effect="light"
                              placement="right">
                    <i class="el-icon-question" />
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="属性名称" prop="propertyName">
                <el-col :span="20">
                  <el-input v-model="form.propertyName" autocomplete="off" placeholder="请输入指示属性名称" />
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip content="指示属性名称" effect="light"
                    placement="right">
                    <i class="el-icon-question" />
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item label="数据类型" prop="datatype">
                <el-col :span="20">
                  <el-select v-model="form.datatype" placeholder="请选择数据类型">
                    <el-option v-for="item in dict.type.link_product_datatype" :key="item.value" :label="item.label"
                      :value="item.value" />
                  </el-select>
                </el-col>
              </el-form-item>
            </div>
          </div>
        </el-row>
        <el-row>
          <el-col :span="14">
            <el-form-item label="访问权限" prop="method">
              <el-radio-group v-model="form.method" size="small">
                <el-radio-button v-for="item in methodList" :key="item.value" :label="item.value">{{ item.label }}
                </el-radio-button>
              </el-radio-group>
              <el-tooltip content="请输入指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE" effect="light" placement="right">
                <i class="el-icon-question" />
              </el-tooltip>
            </el-form-item>
          </el-col>
          <el-col :span="10">

          </el-col>
        </el-row>
        <el-row>
          <div class="disply">
            <div class="small">
              <el-form-item label="是否必填" prop="required" size="small">
                <el-radio-group v-model="form.required">
                  <el-radio-button v-for="item in dict.type.link_product_isRequired" :key="item.value"
                    :label="parseInt(item.value)">
                    {{ item.label }}</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item v-if="form.datatype === 'string' || form.datatype === 'binary'" label="长度" prop="maxlength">
                <el-col :span="20">
                  <el-input type="number" v-model="form.maxlength" placeholder="请输入字符串长度" @change="min" />
                </el-col>
                <el-col :span="1">
                  <el-tooltip content="请输入字符串长度。输入值大于等于0、小于等于2147483647的数字。" effect="light" placement="right">
                    <i class="el-icon-question" />
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
          </div>
        </el-row>
        <el-form-item v-if="form.datatype === 'enum' || form.datatype === 'string'" label="枚举项">
          <div>
            <el-button type="primary" icon="el-icon-plus" size="mini" plain @click="addAttrEnum">添加枚举项
            </el-button>
            &nbsp;
            <el-tooltip content="最多添加100项。参数值：支持整型；参数描述：支持中文、英文大小写、日文、数字，不超过20个字符；" effect="light" placement="right">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </div>
          <div v-for="(item, index) in form.enums" :key="index" class="clearfix" style="margin: 11px 0 11px -10px;">
            <el-col :span="10">
              <el-form-item :prop="'enums.' + index + '.enumValue'" :rules="rules.enumValue" label="" label-width="0px">
                <el-input v-model="item.enumValue" placeholder="编号如'0'" />
              </el-form-item>
            </el-col>
            <el-col :span="1" style="text-align: center">-</el-col>
            <el-col :span="10">
              <el-form-item :prop="'enums.' + index + '.enumText'" :rules="rules.enumText" label="" label-width="0px">
                <el-input v-model="item.enumText" placeholder="对该枚举项的描述" />
              </el-form-item>
            </el-col>
            <el-col :span="1">
              <el-button type="danger" icon="el-icon-delete" size="mini" circle @click="removeAttrEnum(item)">
              </el-button>
            </el-col>
          </div>
        </el-form-item>
        <div class="disply">
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
        </div>
        <el-row>
          <div class="disply">
            <div class="small">
              <el-form-item v-if="form.datatype === 'int' || form.datatype === 'decimal'" label="单位" prop="unit">
                <el-col :span="20">
                  <el-input v-model="form.unit" placeholder="支持长度不超过50。" />
                </el-col>
                <el-col :span="2" style="padding-left: 5px">
                  <el-tooltip content="请输入单位。支持长度不超过50。取值根据参数确定，如：•温度单位：C或K•百分比单位：%•压强单位：Pa或kPa" effect="light"
                    placement="right">
                    <i class="el-icon-question" />
                  </el-tooltip>
                </el-col>
              </el-form-item>
            </div>
            <div class="small">
              <el-form-item v-if="form.datatype === 'int' || form.datatype === 'decimal'" label="步长" prop="step">
                <el-col :span="20">
                  <el-input v-model="form.step" placeholder="请输入步长" />
                </el-col>
              </el-form-item>
            </div>
          </div>
        </el-row>
        <el-row>
          <el-col :span="23">
            <el-form-item label="属性描述" prop="description">
              <el-input v-model="form.description" placeholder="请输入属性描述，不影响实际功能，可配置为空字符串" type="textarea" />
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
  listProperties,
  getProperties,
  delProperties,
  addProperties,
  updateProperties
} from "@/api/link/product/productProperties";
//获取地图信息
export default {
  name: "Properties",
  props: {
    "serviceId": {
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
        min: null,
        max: null,
        maxlength: null,
        method: null,
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
          { required: true, message: "服务ID不能为空", trigger: "blur" }
        ],
        propertyCode: [
          { required: true, message: "属性编码不能为空", trigger: "blur" },
          { min: 2, max: 50, message: '属性编码长度必须介于 2 和 50 之间', trigger: 'blur' },
          {
            pattern: /^[a-z0-9_]+$/,
            message: "英文小写、数字、下划线，长度[2,50]",
            trigger: "blur"
          }
        ],
        propertyName: [
          { required: true, message: "属性名称不能为空", trigger: "blur" },
          { min: 2, max: 50, message: '属性名称长度必须介于 2 和 50 之间', trigger: 'blur' }
        ],
        datatype: [
          { required: true, message: "数据类型不能为空", trigger: "change" }
        ],
        maxlength: [
          { required: true, message: "长度不能为空，且只能为正整数", trigger: "blur" },
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
        enumValue: [
          { required: true, message: "枚举值不能为空", trigger: "blur" },
          {
            pattern: /^[0-9]+$/,
            message: "请输入合法数字",
            trigger: "blur"
          }
        ],
        enumText: [
          { required: true, message: "枚举描述不能为空", trigger: "blur" },
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
    min() {
      if (this.form.maxlength < 0) {
        this.form.maxlength = 0
      }
    },
    /** 查询产品属性数据列表 */
    getList() {
      this.loading = true;
      listProperties(this.queryParams).then(response => {
        this.propertiesList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    removeAttrEnum(item) {
      let index = this.form.enums.indexOf(item)
      if (index !== -1) {
        this.form.enums.splice(index, 1)
      }
    },
    addAttrEnum() {
      if (this.form.enums.length < 100) {
        this.form.enums.push({
          enumValue: '',
          enumText: ''
        });
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
        min: 0,
        max: 65535,
        maxlength: null,
        method: null,
        required: 1,
        step: null,
        unit: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        enums: []
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
      this.title = "添加属性";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getProperties(id).then(response => {
        this.form = response.data;
        this.$set(this.form, "enums", [])
        try {
          if (this.form.enumlist)
            this.$set(this.form, "enums", JSON.parse(this.form.enumlist))
        } catch {

        }
        this.open = true;
        this.title = "修改属性";
      });
    },
    /** 提交按钮 */
    submitForm() {
      if (this.form.enums && this.form.enums.length > 0) {
        this.form.enumlist = JSON.stringify(this.form.enums);
      }
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
      this.download('link/productProperties/export', {
        ...this.queryParams
      }, `properties_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
<style lang="scss" scoped>
.pagination-container {
  height: 50px;
}

.disply {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;

  .small {
    width: calc(50% - 5px);
  }
}
</style>
