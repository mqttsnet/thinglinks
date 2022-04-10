<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="客户端标识" prop="clientId">
        <el-input
          v-model="queryParams.clientId"
          placeholder="请输入客户端标识"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备标识" prop="deviceIdentification">
        <el-input
          v-model="queryParams.deviceIdentification"
          placeholder="请输入设备标识"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          placeholder="请输入设备名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="连接实例" prop="connector">
        <el-select v-model="queryParams.connector" placeholder="请选择连接实例" clearable size="small">
          <el-option
            v-for="dict in dict.type.link_device_connector"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备状态" prop="deviceStatus">
        <el-select v-model="queryParams.deviceStatus" placeholder="请选择设备状态" clearable size="small">
          <el-option
            v-for="dict in dict.type.link_device_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="连接状态" prop="connectStatus">
        <el-select v-model="queryParams.connectStatus" placeholder="请选择连接状态" clearable size="small">
          <el-option
            v-for="dict in dict.type.link_device_connect_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="是否遗言" prop="isWill">
        <el-select v-model="queryParams.isWill" placeholder="请选择是否遗言" clearable size="small">
          <el-option
            v-for="dict in dict.type.link_device_is_will"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备标签" prop="deviceTags">
        <el-input
          v-model="queryParams.deviceTags"
          placeholder="请输入设备标签"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品型号" prop="productId">
        <el-input
          v-model="queryParams.productId"
          placeholder="请输入产品型号"
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
      <el-form-item label="产品协议类型" prop="protocolType">
        <el-select v-model="queryParams.protocolType" placeholder="请选择产品协议类型" clearable size="small">
          <el-option
            v-for="dict in dict.type.link_device_protocol_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-select v-model="queryParams.deviceType" placeholder="请选择设备类型" clearable size="small">
          <el-option
            v-for="dict in dict.type.link_device_device_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          v-hasPermi="['link:device:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['link:device:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['link:device:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-loading"
          size="mini"
          :disabled="multiple"
          @click="handleDisconnect"
          v-hasPermi="['link:device:disconnect']"
        >断开连接</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['link:device:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="deviceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="id" align="center" prop="id" />
      <el-table-column label="客户端标识" align="center" prop="clientId" width="180"/>
      <el-table-column label="用户名" align="center" prop="userName" width="180"/>
      <el-table-column label="密码" align="center" prop="password" width="180"/>
      <el-table-column label="认证方式" align="center" prop="authMode">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_auth_mode" :value="scope.row.authMode"/>
        </template>
      </el-table-column>
      <el-table-column label="设备标识" align="center" prop="deviceIdentification" width="180"/>
      <el-table-column label="设备名称" align="center" prop="deviceName" width="180"/>
      <el-table-column label="连接实例" align="center" prop="connector" width="180">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_connector" :value="scope.row.connector"/>
        </template>
      </el-table-column>
      <el-table-column label="设备描述" align="center" prop="deviceDescription" width="180"/>
      <el-table-column label="设备状态" align="center" prop="deviceStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_status" :value="scope.row.deviceStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="连接状态" align="center" prop="connectStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_connect_status" :value="scope.row.connectStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="是否遗言" align="center" prop="isWill">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_is_will" :value="scope.row.isWill"/>
        </template>
      </el-table-column>
      <el-table-column label="设备标签" align="center" prop="deviceTags" />
      <el-table-column label="产品型号" align="center" prop="productId" />
      <el-table-column label="厂商ID" align="center" prop="manufacturerId" />
      <el-table-column label="产品协议类型" align="center" prop="protocolType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_protocol_type" :value="scope.row.protocolType"/>
        </template>
      </el-table-column>
      <el-table-column label="设备类型" align="center" prop="deviceType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.link_device_device_type" :value="scope.row.deviceType"/>
        </template>
      </el-table-column>
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
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['link:device:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['link:device:remove']"
          >删除</el-button>
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

    <!-- 添加或修改设备档案对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="40%" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="11">
            <el-form-item label="客户端标识" prop="clientId">
              <el-input v-model="form.clientId" placeholder="请输入客户端标识" />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="用户名" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入密码" />
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
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="设备名称" prop="deviceIdentification">
              <el-input v-model="form.deviceName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="设备标识" prop="deviceIdentification">
              <el-input v-model="form.deviceIdentification" placeholder="请输入设备标识" />
            </el-form-item>
          </el-col>
        </el-row>


        <el-row>
          <el-col :span="11">
            <el-form-item label="集成应用" prop="appId">
              <el-select v-model="form.appId" placeholder="请选择集成应用">
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
            <el-form-item label="连接实例" prop="connector">
              <el-select v-model="form.connector" placeholder="请选择连接实例">
                <el-option
                  v-for="dict in dict.type.link_device_connector"
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
            <mapView
              @locationChange="locationChange"
              @locationFail="locationFail"
              ref="mapView"
            ></mapView>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="纬度" prop="latitude">
              <el-input v-model="form.latitude" placeholder="请输入纬度" />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="经度" prop="longitude">
              <el-input v-model="form.longitude" placeholder="请输入经度" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="设备描述" prop="deviceDescription">
              <el-input v-model="form.deviceDescription" placeholder="请输入设备描述" />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="设备状态" prop="deviceStatus">
              <el-select v-model="form.deviceStatus" placeholder="请选择设备状态">
                <el-option
                  v-for="dict in dict.type.link_device_status"
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
            <el-form-item label="产品型号" prop="productId">
              <el-input v-model="form.productId" placeholder="请输入产品型号" />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="厂商ID" prop="manufacturerId">
              <el-input v-model="form.manufacturerId" placeholder="请输入厂商ID" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item label="产品协议类型" prop="protocolType">
              <el-select v-model="form.protocolType" placeholder="请选择产品协议类型">
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
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="form.deviceType" placeholder="请选择设备类型">
                <el-option
                  v-for="dict in dict.type.link_device_device_type"
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
            <el-form-item label="设备标签" prop="deviceTags">
              <el-input v-model="form.deviceTags" placeholder="请输入设备标签" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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
import { listDevice, getDevice, delDevice, addDevice, updateDevice , disconnectDevice} from "@/api/link/device";

import mapView from "./mapView";

export default {
  components: {
    mapView
  },
  name: "Device",
  dicts: ['link_device_status', 'link_device_connect_status', 'link_device_protocol_type', 'link_device_device_type', 'link_device_auth_mode', 'link_device_connector', 'link_device_is_will', 'link_application_type'],
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
        productId: null,
        manufacturerId: null,
        protocolType: null,
        deviceType: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        clientId: [
          { required: true, message: "客户端标识不能为空", trigger: "blur" }
        ],
        userName: [
          { required: true, message: "用户名不能为空", trigger: "blur" }
        ],
        password: [
          { required: true, message: "密码不能为空", trigger: "blur" }
        ],
        appId: [
          { required: true, message: "应用ID不能为空", trigger: "blur" }
        ],
        authMode: [
          { required: true, message: "认证方式不能为空", trigger: "change" }
        ],
        deviceIdentification: [
          { required: true, message: "设备标识不能为空", trigger: "blur" }
        ],
        deviceName: [
          { required: true, message: "设备名称不能为空", trigger: "blur" }
        ],
        latitude: [
          { required: true, message: "纬度不能为空", trigger: "blur" }
        ],
        longitude: [
          { required: true, message: "经度不能为空", trigger: "blur" }
        ],
        connector: [
          { required: true, message: "连接实例不能为空", trigger: "change" }
        ],
        deviceStatus: [
          { required: true, message: "设备状态不能为空", trigger: "change" }
        ],
        productId: [
          { required: true, message: "产品型号不能为空", trigger: "blur" }
        ],
        manufacturerId: [
          { required: true, message: "厂商ID不能为空", trigger: "blur" }
        ],
        protocolType: [
          { required: true, message: "产品协议类型不能为空", trigger: "change" }
        ],
        deviceType: [
          { required: true, message: "设备类型不能为空", trigger: "change" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {

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
      listDevice(this.queryParams).then(response => {
        this.deviceList = response.rows;
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
        productId: null,
        manufacturerId: null,
        protocolType: null,
        deviceType: null,
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
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加设备档案";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getDevice(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改设备档案";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateDevice(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDevice(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除设备档案编号为"' + ids + '"的数据项？').then(function() {
        return delDevice(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 断开连接按钮操作 */
    handleDisconnect(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认断开连接设备档案编号为"' + ids + '"的数据项？').then(function() {
        return disconnectDevice(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("操作成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('link/device/export', {
        ...this.queryParams
      }, `link_device.xlsx`)
    }
  }
};
</script>
