<template>
  <div class="app-container">
    <el-form
      v-show="showSearch"
      ref="queryForm"
      :inline="true"
      :model="queryParams"
      label-width="100px"
    >
      <el-form-item label="应用场景" prop="appId">
        <el-input
          v-model="queryParams.model.appId"
          clearable
          placeholder="请输入应用场景"
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="包名称" prop="packageName">
        <el-input
          v-model="queryParams.model.packageName"
          clearable
          placeholder="请输入包名称"
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="升级包类型" prop="packageType">
        <el-select
          v-model="queryParams.model.packageType"
          clearable
          placeholder="请选择升级包类型"
          size="small"
        >
          <el-option
            v-for="dict in dict.type.ota_package_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="产品标识" prop="productIdentification">
        <el-input
          v-model="queryParams.model.productIdentification"
          clearable
          placeholder="请输入产品标识"
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="升级包版本号" prop="version">
         <el-input
          v-model="queryParams.model.version"
          clearable
          placeholder="请输入升级包版本号"
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="升级包位置" prop="fileLocation">
         <el-input
          v-model="queryParams.model.fileLocation"
          clearable
          placeholder="请输入升级包版本号"
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="状态" prop="status">
        <el-select
          v-model="queryParams.model.status"
          clearable
          placeholder="请选择状态"
          size="small"
        >
          <el-option
            v-for="dict in dict.type.ota_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="升级包描述" prop="description">
        <el-input
          v-model="queryParams.model.description"
          clearable
          placeholder="请输入升级包描述"
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item
        v-if="advancedSearch"
        label="自定义信息"
        prop="customInfo"
      >
        <el-input
          v-model="queryParams.model.customInfo"
          clearable
          placeholder="请输入自定义信息"
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="描述" prop="remark">
        <el-input
          type="textarea"
          v-model="queryParams.model.remark"
          clearable
          placeholder="请输入描述"
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="创建时间" prop="createTimeRange">
        <el-date-picker
          v-model="queryParams.model.createTimeRange"
          type="daterange"
          align="right"
          unlink-panels
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button
          icon="el-icon-search"
          size="mini"
          type="primary"
          @click="handleQuery"
        >搜索
        </el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
        >重置
        </el-button
        >
        <el-button
          :icon="icon"
          size="mini"
          @click="advancedSearch_toggle($event)"
        >高级搜索
        </el-button
        >
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <!-- <el-button
          v-hasPermi="['link:device:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增
        </el-button> -->
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['link:device:remove']"
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
          v-hasPermi="['link:device:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出
        </el-button>
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="false"
      :data="otaList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column align="center" type="selection" width="50"/>
      <el-table-column align="center" label="序号">
        <template slot-scope="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="应用场景"
        prop="appId"
        width="180"
      />
      <el-table-column
        align="center"
        label="包名称"
        prop="packageName"
        width="180"
      />
      <el-table-column
        align="center"
        label="升级包类型"
        prop="packageType"
        width="180"
      >
        <template slot-scope="scope">
          <dict-tag
            :options="dict.type.ota_package_type"
            :value="scope.row.packageType"
          />
        </template>
      </el-table-column>
      <el-table-column 
        align="center" 
        label="产品标识" 
        prop="productIdentification" 
        width="180"
      />
      <el-table-column
        align="center"
        label="升级包版本号"
        prop="version"
        width="180"
      />
      <el-table-column 
        align="center"
        label="升级包位置" 
        prop="fileLocation"
      />
      <el-table-column 
        align="center"
        label="状态" 
        prop="status"
      >        
        <template slot-scope="scope">
          <dict-tag
            :options="dict.type.ota_status"
            :value="scope.row.status"
          />
        </template>
      </el-table-column>
      <el-table-column 
        align="center"
        label="升级包功能描述" 
        prop="description"
      />
      <el-table-column 
        align="center"
        label="自定义信息" 
        prop="customInfo"
      />
      <el-table-column 
        align="center"
        label="描述" 
        prop="remark"
      />
      <el-table-column 
        align="center"
        label="创建人组织" 
        prop="remark"
      />
      <el-table-column 
        align="center"
        label="创建时间" 
        prop="remark"
      />
      <el-table-column fixed="right" label="操作" width="150">
        <template slot-scope="scope">
          <span style="margin-right: 10px">
            <el-tooltip
              class="item"
              content="修改"
              effect="light"
              placement="top"
            >
              <el-button
                v-hasPermi="['link:device:edit']"
                circle
                icon="el-icon-edit"
                size="mini"
                type="primary"
                @click="handleUpdate(scope.row)"
              ></el-button>
            </el-tooltip>
          </span>
          <span style="margin-right: 10px">
            <el-tooltip
              class="item"
              content="删除"
              effect="light"
              placement="top"
            >
              <el-button
                v-hasPermi="['link:device:remove']"
                circle
                icon="el-icon-delete"
                size="mini"
                type="primary"
                @click="handleDelete(scope.row)"
              ></el-button>
            </el-tooltip>
          </span>
          <span style="margin-right: 10px">
            <el-tooltip
              class="item"
              content="设备详情"
              effect="light"
              placement="top"
            >
              <el-button
                v-hasPermi="['link:device:detail']"
                circle
                icon="el-icon-s-operation"
                size="mini"
                type="primary"
                @click="handleDeviceDetail(scope.row)"
              ></el-button>
            </el-tooltip>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :limit.sync="queryParams.size"
      :page.sync="queryParams.current"
      :total="total"
      @pagination="getList"
    />

    <!-- 添加或修改设备档案对话框 -->
    <el-dialog
      :close-on-click-modal="false"
      :title="title"
      :visible.sync="open"
      append-to-body
      width="48%"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="11">
            <el-form-item label="客户端标识" prop="clientId">
              <el-input
                v-model="form.clientId"
                placeholder="请输入客户端标识"
                @keyup.native="clientId"
              />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="用户名" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入用户名"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                placeholder="请输入密码"
                show-password
                type="password"
              />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="认证方式" prop="authMode">
              <el-select v-model="form.authMode" placeholder="请选择认证方式">
                <el-option
                  v-for="dict in dict.type.link_device_auth_mode"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input
                v-model="form.deviceName"
                placeholder="请输入设备名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="11">
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="集成应用" prop="appId">
              <el-select
                v-model="form.appId"
                placeholder="请选择集成应用"
              >
                <el-option
                  v-for="dict in dict.type.link_application_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="连接实例" prop="connector">
              <el-select v-model="form.connector" placeholder="请选择连接实例">
                <el-option
                  v-for="dict in dict.type.link_device_connector"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="设备类型" prop="deviceType">
              <el-select
                v-model="form.deviceType"
                placeholder="请选择设备类型"
              >
                <el-option
                  v-for="dict in dict.type.link_device_device_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="设备状态" prop="deviceStatus">
              <el-select
                v-model="form.deviceStatus"
                placeholder="请选择设备状态"
              >
                <el-option
                  v-for="dict in dict.type.link_device_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="加密方式" prop="encryptMethod">
              <el-select
                v-model="form.encryptMethod"
                placeholder="请选择加密方式"
              >
                <el-option
                  v-for="dict in dict.type.link_device_encrypt_method"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="加密密钥" prop="encryptKey">
              <el-input
                v-model="form.encryptKey"
                placeholder="请输入加密密钥"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="加密向量" prop="encryptVector">
              <el-input
                v-model="form.encryptVector"
                placeholder="请输入加密向量"
              />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="签名密钥" prop="signKey">
              <el-input
                v-model="form.signKey"
                placeholder="请输入签名密钥"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="软件版本" prop="swVersion">
              <el-input
                v-model="form.swVersion"
                placeholder="请输入软件版本"
              />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="固件版本" prop="fwVersion">
              <el-input
                v-model="form.fwVersion"
                placeholder="请输入固件版本"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="sdk版本" prop="deviceSdkVersion">
              <el-input
                v-model="form.deviceSdkVersion"
                placeholder="请输入sdk版本"

              />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="协议类型" prop="protocolType">
              <el-select
                v-model="form.protocolType"
                placeholder="请选择协议类型"
              >
                <el-option
                  v-for="dict in dict.type.link_device_protocol_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="设备标签" prop="deviceTags">
              <el-input
                v-model="form.deviceTags"
                placeholder="请输入设备标签"
              />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="设备描述" prop="deviceDescription">
              <el-input
                v-model="form.deviceDescription"
                placeholder="请输入设备描述"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="22">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="form.remark"
                placeholder="请输入内容"
                type="textarea"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button
          :disabled="
            check.clientId ? false : true
          "
          type="primary"
          @click="submitForm"
        >确 定
        </el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addDevice,
  deleteOta,
  disconnectDevice,
  getDevice,
  listOta,
  listStatusCount,
  updateDevice,
  validationDeviceIdentification_clientId,
  validationDeviceIdentification_deviceIdentification,
} from "@/api/link/ota/otaList.js";

import { otaList } from "./mock/otaList"
export default {
  props: ["appId", "productIdentification"],
  name: "Ota",
  dicts: [
    "ota_package_type",
    "ota_status",
    "link_device_status",
    "link_device_connect_status",
    "link_device_protocol_type",
    "link_device_device_type",
    "link_device_auth_mode",
    "link_device_connector",
    "link_application_type",
    "link_device_encrypt_method",
  ],
  data() {
    return {
      currentIndex: null,
      // 高级搜索切换
      advancedSearch: false,
      // 高级搜索icon
      icon: "el-icon-arrow-down",
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
      // 设备档案表格数据
      otaList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        current: 1,
        size: 10,
        model: {
          appId: null,
          packageName: null,
          packageType: null,
          productIdentification: null,
          version: null,
          fileLocation: null,
          status: null,
          description: null,
          customInfo: null,
          remark: null,
          createTimeRange: null,
        },
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        clientId: [
          {required: true, message: "客户端标识不能为空", trigger: "blur"},
        ],
        userName: [
          {required: true, message: "用户名不能为空", trigger: "blur"},
        ],
        password: [
          {required: true, message: "密码不能为空", trigger: "blur"},
        ],
        appId: [{required: true, message: "应用ID不能为空", trigger: "blur"}],
        authMode: [
          {required: true, message: "认证方式不能为空", trigger: "change"},
        ],
        deviceIdentification: [
          {required: true, message: "设备标识不能为空", trigger: "blur"},
        ],
        deviceName: [
          {required: true, message: "设备名称不能为空", trigger: "blur"},
        ],
        connector: [
          {required: true, message: "连接实例不能为空", trigger: "change"},
        ],
        deviceStatus: [
          {required: true, message: "设备状态不能为空", trigger: "change"},
        ],
        productIdentification: [
          {required: true, message: "产品标识不能为空", trigger: "blur"},
        ],
        protocolType: [
          {
            required: true,
            message: "协议类型不能为空",
            trigger: "change",
          },
        ],
        encryptMethod: [
          {
            required: true,
            message: "加密方式不能为空",
            trigger: "change",
          },
        ],
        swVersion: [
          {required: true, message: "软件版本不能为空", trigger: "blur"},
        ],
        fwVersion: [
          {required: true, message: "固件版本不能为空", trigger: "blur"},
        ],
        deviceSdkVersion: [
          {required: true, message: "sdk版本不能为空", trigger: "blur"},
        ],
        deviceType: [
          {required: true, message: "设备类型不能为空", trigger: "change"},
        ],
      },
      check: {
        clientId: false,
        deviceIdentification: false,
      },
    };
  },
  watch: {
    appId(newval, oldval) {
      if (newval !== oldval) {
        this.queryParams.appId = newval;
        this.getList();
      }
    },
    productIdentification(newval, oldval) {
      if (newval !== oldval) {
        this.queryParams.productIdentification = newval;
        this.getList();
      }
    },
  },
  created() {
    this.getList();
  },
  methods: {
    //显示隐藏
    setShow(index) {
      if (this.show) {
        this.currentIndex = index;
        this.show = false;
      } else {
        this.currentIndex = null;
        this.show = true;
      }
    },
    // 复制
    copy(shareiot) {
      var input = document.createElement("input");
      input.value = shareiot;
      document.body.appendChild(input);
      input.select();
      document.execCommand("Copy");
      document.body.removeChild(input);
      this.$message({
        message: "复制成功",
        type: "success",
      });
    },
    // 高级搜索切换显示隐藏
    advancedSearch_toggle() {
      this.advancedSearch = !this.advancedSearch;
      // 切換icon
      if (this.advancedSearch) {
        this.icon = "el-icon-arrow-up";
      } else {
        this.icon = "el-icon-arrow-down";
      }
    },
    /** 查询ots列表 */
    async getList() {
      this.loading = true;

      console.log(otaList)
      this.otaList = otaList;
      this.loading = false;
      const searchModel = Object.keys(this.queryParams.model).reduce((acc, key) => {  
        if (this.queryParams.model[key] !== null) {  
          acc[key] = this.queryParams.model[key];  
        }  
        return acc;  
      }, {});  
      const params = {
        ...this.queryParams,
        model: searchModel,
      }
      const res = await listOta(params)
      const { isSuccess = false, data } = res;
      if (isSuccess) {
        this.otaList = data.records;
        this.total = data.total;
        this.loading = false;
      }
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {};
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.current = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.check = {
        clientId: false,
        deviceIdentification: false,
      };
      this.reset();
      this.form.appId = this.appId;
      // if (this.appId)
      //   this.changeApp(this.appId);
      this.form.productIdentification = this.productIdentification;
      this.set = false;
      this.open = true;
      this.title = "添加ota";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.check = {
        clientId: true,
        deviceIdentification: true,
      };
      this.reset();
      this.set = true;
      const id = row.id || this.ids;

      this.open = true;
      this.title = "修改ota";
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.id != null) {
            this.deviceLocation.deviceIdentification =
              this.form.deviceIdentification;
            this.deviceLocation.id = this.form.id;
            this.form.deviceLocation = this.deviceLocation;
            updateDevice(this.form).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            this.deviceLocation.deviceIdentification =
              this.form.deviceIdentification;
            this.deviceLocation.id = this.form.id;
            this.form.deviceLocation = this.deviceLocation;
            addDevice(this.form).then((response) => {
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
      const ids = row.id ? [row.id] : this.ids;
      this.$modal
        .confirm('是否确认删除ota编号为"' + ids + '"的数据项？')
        .then(function () {
          return deleteOta(ids);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {
        });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download(
        "link/device/export",
        {
          ...this.queryParams,
        },
        `link_device.xlsx`
      );
    },
    handleDeviceDetail: function (row) {
      const deviceId = row.id;
      this.$router.push("/link/device-detail/device/" + deviceId);
    },
  },
};
</script>
<style lang="scss" scoped>
.equipment_status {
  width: 100%;
  margin: 0 0 10px 10px;
  padding: 20px 30px;
  background: #f8f8f9;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  font-size: 14px;
  font-weight: 700;
  color: #515a6e;
}

.equipment_status .status_num {
  width: 20%;
  display: flex;
  font-size: 18px;
  align-items: center;

  img {
    width: 30%;
  }
}

.equipment_status .status {
  width: 20%;
  display: flex;
  align-items: center;
}

.inputDeep {
  border: 0 !important;
}
</style>
