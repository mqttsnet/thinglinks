<template>
  <div class="app-container">
    <div class="equipment_status">
      <div class="status_num">
        <!-- <img src="assets/icons/status.svg" alt=""> -->
        <i class="el-icon-help"></i>
        <p>：
          <span style="color:#71e2a3">{{ onlineCount }}</span>/
          <span style="color:#ff9292">{{ offlineCount }}</span>/
          <span style="color:#EEB422">{{ initCount }}</span>/
          <span>{{ total }}</span>
        </p>
      </div>
      <div class="status">
        <p>
          设备状态：
        </p>
        <span style="color:#71e2a3">在线/</span>
        <span style="color:#ff9292">离线/</span>
        <span style="color:#ffba00">未连接/</span>
        <span>全部</span>
      </div>
      <div class="Mqtt">
        <p v-for="dict in dict.type.link_device_connector" :key="dict.value">
          MQTT连接地址：
          <i class="el-icon-copy-document" style="cursor: pointer;" title="复制" @click="copy(dict.label)"></i>
          <span>{{ dict.label }}</span>
        </p>
      </div>
      <div class="zhengshu">
        <p>
          SSL证书：
          <a href="#" style="color:#357DF5">下载SSL证书</a>
        </p>
        <p>
          <span>二次开发：</span>
          <a href="#" style="color:#357DF5;margin-right:20px">下载Demo</a>
          <a href="https://bbs.csdn.net/forums/thingiots" style="color:#357DF5" target="_blank">开发文档</a>
        </p>
      </div>
    </div>
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="100px">
      <el-form-item label="客户端标识" prop="clientId">
        <el-input v-model="queryParams.clientId" clearable placeholder="请输入客户端标识" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="设备标识" prop="deviceIdentification">
        <el-input v-model="queryParams.deviceIdentification" clearable placeholder="请输入设备标识" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input v-model="queryParams.deviceName" clearable placeholder="请输入设备名称" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="连接实例" prop="connector">
        <el-select v-model="queryParams.connector" clearable placeholder="请选择连接实例" size="small">
          <el-option v-for="dict in dict.type.link_device_connector" :key="dict.value" :label="dict.label"
                     :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="设备状态" prop="deviceStatus">
        <el-select v-model="queryParams.deviceStatus" clearable placeholder="请选择设备状态" size="small">
          <el-option v-for="dict in dict.type.link_device_status" :key="dict.value" :label="dict.label"
                     :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="连接状态" prop="connectStatus">
        <el-select v-model="queryParams.connectStatus" clearable placeholder="请选择连接状态" size="small">
          <el-option v-for="dict in dict.type.link_device_connect_status" :key="dict.value" :label="dict.label"
                     :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="是否遗言" prop="isWill">
        <el-select v-model="queryParams.isWill" clearable placeholder="请选择是否遗言" size="small">
          <el-option v-for="dict in dict.type.link_device_is_will" :key="dict.value" :label="dict.label"
                     :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="设备标签" prop="deviceTags">
        <el-input v-model="queryParams.deviceTags" clearable placeholder="请输入设备标签" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="产品标识" prop="productIdentification">
        <el-select v-model="form.productIdentification" placeholder="请选择产品标识">
          <el-option v-for="item in productOptions" :key="item.productIdentification" :disabled="item.status === 0"
                     :label="item.productName" :value="item.productIdentification" @keyup.enter.native="handleQuery">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="产品协议类型" prop="protocolType">
        <el-select v-model="queryParams.protocolType" clearable placeholder="请选择产品协议类型" size="small">
          <el-option v-for="dict in dict.type.link_device_protocol_type" :key="dict.value" :label="dict.label"
                     :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="advancedSearch" label="设备类型" prop="deviceType">
        <el-select v-model="queryParams.deviceType" clearable placeholder="请选择设备类型" size="small">
          <el-option v-for="dict in dict.type.link_device_device_type" :key="dict.value" :label="dict.label"
                     :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button :icon="icon" size="mini" @click="advancedSearch_toggle($event)">高级搜索</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:device:add']" icon="el-icon-plus" plain size="mini" type="primary" @click="handleAdd">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:device:remove']" :disabled="multiple" icon="el-icon-delete" plain size="mini" type="danger" @click="handleDelete">删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:device:disconnect']" :disabled="multiple" icon="el-icon-loading" plain size="mini" type="danger" @click="handleDisconnect">断开连接
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['link:device:export']" icon="el-icon-download" plain size="mini" type="warning" @click="handleExport">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="deviceList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="id" prop="id"/>
      <el-table-column align="center" label="设备标识" prop="deviceIdentification" width="180"/>
      <el-table-column align="center" label="客户端标识" prop="clientId" width="180"/>
      <el-table-column align="center" label="用户名" prop="userName" width="180"/>
      <el-table-column align="center" label="密码" prop="password" width="180">
        <template slot-scope="scope">
          <div disable="disable" style="width:100%;display:flex; justify-content: center;align-items:center">
            <i class="el-icon-copy-document" style="cursor: pointer;" title="复制"
               @click="copy(deviceList[scope.$index].password)"></i>
            <span v-show="currentIndex !== scope.$index" ref="start">********</span>
            <span v-show="currentIndex === scope.$index" ref="pWord">{{ deviceList[scope.$index].password }}</span>
            <i :ind="scope.$index" class="el-icon-view" style="cursor: pointer;"
               @click="setShow(scope.$index, $event)"></i>
          </div>
        </template>
      </el-table-column>
      <el-table-column align="center" label="设备名称" prop="deviceName" width="180"/>
      <el-table-column align="center" label="设备类型" prop="deviceType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_device_type" :value="scope.row.deviceType"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="设备状态" prop="deviceStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_status" :value="scope.row.deviceStatus"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="连接状态" prop="connectStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_connect_status" :value="scope.row.connectStatus"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建者" prop="createBy"/>
      <el-table-column align="center" label="创建时间" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{
              parseTime(scope.row.createTime, "{y}-{m}-{d} {h}:{i}:{s}")
            }}</span>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="150">
        <template slot-scope="scope">
          <span style="margin-right:10px">
            <el-tooltip class="item" content="修改" effect="light" placement="top">
              <el-button v-hasPermi="['link:device:edit']" circle icon="el-icon-edit" size="mini" type="primary" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
          </span>
          <span style="margin-right:10px">
            <el-tooltip class="item" content="删除" effect="light" placement="top">
              <el-button v-hasPermi="['link:device:remove']" circle icon="el-icon-delete" size="mini" type="primary" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </span>
          <span style="margin-right:10px">
            <el-tooltip class="item" content="设备详情" effect="light" placement="top">
<!--              <router-link :to="{ name: 'deviceDetails', query: { id: scope.row.id } }">-->
<!--                <el-button circle size="mini" type="primary" icon="el-icon-s-operation"-->
<!--                           v-hasPermi="['link:device:deviceDetails']"></el-button>-->
<!--              </router-link>-->
              <el-button v-hasPermi="['link:device:detail']" circle icon="el-icon-s-operation" size="mini" type="primary" @click="handleDeviceDetail(scope.row)"></el-button>
            </el-tooltip>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total" @pagination="getList"/>

    <!-- 添加或修改设备档案对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="40%">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="11">
            <el-form-item label="客户端标识" prop="clientId">
              <el-input v-model="form.clientId" :disabled='set ? true : false' placeholder="请输入客户端标识" @keyup.native="clientId"/>
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
              <el-input v-model="form.password" placeholder="请输入密码" show-password type="password"/>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="认证方式" prop="authMode">
              <el-select v-model="form.authMode" placeholder="请选择认证方式">
                <el-option v-for="dict in dict.type.link_device_auth_mode" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="form.deviceName" placeholder="请输入设备名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="设备标识" prop="deviceIdentification">
              <el-input v-model="form.deviceIdentification" :disabled='set ? true : false' placeholder="请输入设备标识" @keyup.native="deviceIdentification"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="集成应用" prop="appId">
              <el-select v-model="form.appId" :disabled="this.appId" placeholder="请选择集成应用" @change="changeApp">
                <el-option v-for="dict in dict.type.link_application_type" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="连接实例" prop="connector">
              <el-select v-model="form.connector" placeholder="请选择连接实例">
                <el-option v-for="dict in dict.type.link_device_connector" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <mapView ref="mapView" @locationChange="locationChange" @locationFail="locationFail"></mapView>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="纬度" prop="latitude">
              <el-input v-model="form.latitude" placeholder="请输入纬度"/>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="经度" prop="longitude">
              <el-input v-model="form.longitude" placeholder="请输入经度"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="设备描述" prop="deviceDescription">
              <el-input v-model="form.deviceDescription" placeholder="请输入设备描述"/>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="设备状态" prop="deviceStatus">
              <el-select v-model="form.deviceStatus" placeholder="请选择设备状态">
                <el-option v-for="dict in dict.type.link_device_status" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="产品协议类型" prop="protocolType">
              <el-select v-model="form.protocolType" placeholder="请选择产品协议类型">
                <el-option v-for="dict in dict.type.link_device_protocol_type" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="form.deviceType" placeholder="请选择设备类型">
                <el-option v-for="dict in dict.type.link_device_device_type" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="所属产品" prop="productIdentification">
              <el-select v-model="form.productIdentification" :disabled="this.productIdentification" placeholder="请选择所属产品">
                <el-option v-for="item in productOptions" :key="item.productIdentification" :disabled="item.status === 0" :label="item.productName" :value="item.productIdentification"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="设备标签" prop="deviceTags">
              <el-input v-model="form.deviceTags" placeholder="请输入设备标签"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="22">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入内容" type="textarea"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button :disabled="(check.clientId && check.deviceIdentification) ? false : true" type="primary" @click="submitForm">确 定
        </el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listDevice,
  getDevice,
  delDevice,
  addDevice,
  updateDevice,
  disconnectDevice,
  validationDeviceIdentification_clientId,
  validationDeviceIdentification_deviceIdentification
} from "@/api/link/device/device";
import {queryProduct} from "@/api/link/product/product";
import mapView from "./mapView";

export default {
  components: { mapView },
  props:["appId","productIdentification"],
  name: "Device",
  dicts: [
    "link_device_status",
    "link_device_connect_status",
    "link_device_protocol_type",
    "link_device_device_type",
    "link_device_auth_mode",
    "link_device_connector",
    "link_device_is_will",
    "link_application_type",
  ],
  data() {
    return {
      productOptions: [],
      //密码显示隐藏
      show: true,
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
      deviceList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        clientId: null,
        deviceIdentification: null,
        deviceName: null,
        connector: null,
        deviceStatus: null,
        connectStatus: null,
        isWill: null,
        deviceTags: null,
        productIdentification: null,
        protocolType: null,
        deviceType: null,
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
        latitude: [
          {required: true, message: "纬度不能为空", trigger: "blur"},
        ],
        longitude: [
          {required: true, message: "经度不能为空", trigger: "blur"},
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
            message: "产品协议类型不能为空",
            trigger: "change",
          },
        ],
        deviceType: [
          {required: true, message: "设备类型不能为空", trigger: "change"},
        ],
      },
      onlineCount: 0,  //在线设备
      offlineCount: 0,//离线设备
      initCount: 0,//未连接设备
      set: false,//修改禁用标识
      check: {
        clientId: false,
        deviceIdentification: false
      }
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
    }
  },
  created() {
    if (this.appId)
      this.queryParams.appId = this.appId;
    if (this.productId)
      this.queryParams.productId = this.productId;
    this.getList();
  },
  methods: {
    //客户端标识校验
    clientId() {
      validationDeviceIdentification_clientId(this.form.clientId).then(res => {
        if (res.code === 200) {
          this.check.clientId = true
          this.$message({
            message: '客户端标识校验通过',
            type: 'success'
          });
        }
      })
    },
    //设备标识校验
    deviceIdentification() {
      validationDeviceIdentification_deviceIdentification(this.form.deviceIdentification).then(res => {
        if (res.code === 200) {
          this.check.deviceIdentification = true
          this.$message({
            message: '设备标识校验通过',
            type: 'success'
          });
        }
      })
    },
    //显示隐藏
    setShow(index) {
      if (this.show) {
        this.currentIndex = index
        this.show = false
      } else {
        this.currentIndex = null
        this.show = true
      }
    },
    // 复制
    copy(shareiot) {
      var input = document.createElement("input");
      input.value = shareiot;
      document.body.appendChild(input);
      input.select()
      document.execCommand("Copy");
      document.body.removeChild(input);
      this.$message({
        message: '复制成功',
        type: 'success'
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
    locationChange(e) {
      this.form.longitude = e[0];
      this.form.latitude = e[1];
    },
    locationFail(message) {
      this.$message({
        message: message,
        type: "warning",
        duration: 1500,
      });
    },
    /** 查询设备档案列表 */
    getList() {
      this.loading = true;
      listDevice(this.queryParams).then((response) => {
        this.deviceList = response.data.device.rows;
        this.onlineCount = response.data.onlineCount
        this.offlineCount = response.data.offlineCount
        this.initCount = response.data.initCount
        this.total = response.data.device.total;
        this.loading = false;
      });
    },
    // 切换应用
    changeApp(appId) {
      this.productOptions = [];
      this.getProductList(appId);
    },
    getProductList(appId) {
      let params = {
        appId: appId,
        status:0
      };
      queryProduct(params).then(res => {
        this.productOptions = res.data;
      })
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
        clientId: null,
        userName: null,
        password: null,
        appId: null,
        authMode: null,
        deviceIdentification: null,
        deviceName: null,
        latitude: null,
        longitude: null,
        connector: null,
        deviceDescription: null,
        deviceStatus: null,
        connectStatus: null,
        isWill: null,
        deviceTags: null,
        productIdentification: null,
        protocolType: null,
        deviceType: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
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
      this.ids = selection.map((item) => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.check = {
        clientId: false,
        deviceIdentification: false
      }
      this.reset();
      this.form.appId = this.appId;
      if (this.appId)
        this.changeApp(this.appId);
      this.form.productIdentification = this.productIdentification;
      this.set = false
      this.open = true;
      this.title = "添加设备档案";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.check = {
        clientId: true,
        deviceIdentification: true
      }
      this.reset();
      this.set = true;
      const id = row.id || this.ids;
      getDevice(id).then((response) => {
        this.form = response.data;
        this.open = true;
        this.title = "修改设备档案";
        this.changeApp(response.data.appId);
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.id != null) {
            updateDevice(this.form).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
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
      const ids = row.id || this.ids;
      this.$modal
        .confirm('是否确认删除设备档案编号为"' + ids + '"的数据项？')
        .then(function () {
          return delDevice(ids);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {
        });
    },
    /** 断开连接按钮操作 */
    handleDisconnect(row) {
      const ids = row.id || this.ids;
      this.$modal
        .confirm('是否确认断开连接设备档案编号为"' + ids + '"的数据项？')
        .then(function () {
          return disconnectDevice(ids);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("操作成功");
        })
        .catch(() => {
        });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download(
        "iot/device/export",
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
  background: #F8F8F9;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  font-size: 14px;
  font-weight: 700;
  color: #515a6e;

}

.equipment_status .status_num {
  width: 12%;
  display: flex;
  font-size: 18px;
  align-items: center;

  i {
    font-size: 25px;
    width: 40px;
    height: 40px;
    border: 1px #ccc dashed;
    text-align: center;
    line-height: 38px;
    border-radius: 50%;
  }

  img {
    width: 20%;
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
