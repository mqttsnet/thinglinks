<template>
  <div class="wrap">
    <div class="steps">
      <el-steps :active="stepActive" simple>
        <el-step
          title="基础信息"
          icon="el-icon-edit"
          description="基础信息描述"
        ></el-step>
        <el-step
          title="触发机制"
          icon="el-icon-setting"
          description="触发机制描述"
        ></el-step>
        <el-step
          title="执行动作"
          icon="el-icon-message-solid
"
          description="执行动作描述"
        ></el-step>
      </el-steps>
    </div>
    <div class="info" v-if="stepActive == 0">
      <el-row>
        <el-col> 基本信息 </el-col>
      </el-row>
      <el-form ref="form" label-width="100px" size="small">
        <el-row>
          <el-col :span="8">
            <el-form-item label="规则名称">
              <el-input
                v-model="ruleBasic.ruleName"
                class="detail-input"
                maxlength="60"
                placeholder="请输入规则名称"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规则标识">
              <el-input
                v-model="ruleBasic.jobIdentification"
                class="detail-input"
                maxlength="60"
                placeholder="请输入规则标识"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="应用ID">
              <el-input
                v-model="ruleBasic.appId"
                class="detail-input"
                maxlength="60"
                placeholder="请输入应用ID"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="cron表达式" prop="cronExpression">
              <el-input
                v-model="ruleBasic.cronExpression"
                placeholder="请输入cron执行表达式"
              >
                <template slot="append">
                  <el-button type="primary" @click="handleShowCron">
                    生成表达式
                    <i class="el-icon-time el-icon--right"></i>
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="错误策略" prop="misfirePolicy">
              <el-radio-group v-model="ruleBasic.misfirePolicy" size="small">
                <el-radio-button label="1">立即执行</el-radio-button>
                <el-radio-button label="2">执行一次</el-radio-button>
                <el-radio-button label="3">放弃执行</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否并发" prop="concurrent">
              <el-radio-group v-model="ruleBasic.concurrent" size="mini">
                <el-radio-button label="0">允许</el-radio-button>
                <el-radio-button label="1">禁止</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-radio-group v-model="ruleBasic.status" size="mini">
                <el-radio-button
                  v-for="dict in dict.type.sys_job_status"
                  :key="dict.value"
                  :label="dict.value"
                  >{{ dict.label }}</el-radio-button
                >
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="触发机制">
              <el-radio-group v-model="ruleBasic.triggering" size="mini">
                <el-radio-button
                  v-for="dict in options"
                  :key="dict.value"
                  :label="dict.value"
                  >{{ dict.label }}</el-radio-button
                >
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="16">
            <el-form-item label="规则描述">
              <el-input
                v-model="ruleBasic.remark"
                placeholder="请输入规则描述"
                :autosize="{ minRows: 3, maxRows: 10 }"
                maxlength="255"
                show-word-limit
                type="textarea"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="btn">
        <el-button type="primary" size="mini" @click="stepNext(1)"
          >确 定</el-button
        >
      </div>
    </div>

    <div class="info" v-if="stepActive == 1">
      <el-row>
        <el-col> 触发机制 </el-col>
      </el-row>
      <el-form>
        <div class="condition" v-if="conditionForm.length == 0">
          <span>尚未设置条件</span>
        </div>
        <div v-for="(item, index) in conditionForm" :key="index">
          <el-form-item label="条件">
            <el-select
              v-model="item.conditionType"
              placeholder="请选择"
              style="width: 150px"
              size="mini"
              @change="changeConditionType($event, index)"
            >
              <el-option
                v-for="item in conditionTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>
            <span v-show="item.conditionType == 0">
              <el-select
                v-model="item.productIdentification"
                placeholder="请选择产品"
                style="width: 160px"
                size="mini"
                @change="changeProduct($event, index)"
              >
                <el-option
                  v-for="item in productList"
                  :key="item.productName"
                  :label="item.productName"
                  :value="item.productIdentification"
                ></el-option>
              </el-select>

              <el-select
                v-model="item.deviceType"
                v-show="item.productIdentification"
                placeholder="请选择设备"
                style="width: 130px"
                size="mini"
                @change="changeDeviceType($event, index)"
              >
                <el-option :value="0" label="全部设备">全部设备</el-option>
                <el-option :value="1" label="指定设备">指定设备</el-option>
              </el-select>
              <span v-show="item.deviceType == 1">
                <el-tooltip placement="top" v-if="item.device">
                  <div slot="content">
                    设备名称：{{ item.device.deviceName || "（未设置）" }}<br />
                    设备标识码：{{
                      item.device.deviceIdentification || "（未设置）"
                    }}<br />
                    所属产品：{{ item.productIdentification || "" }}
                  </div>
                  <span>{{ item.device.deviceName || "未命名设备" }}</span>
                </el-tooltip>
                <el-link @click="showDeviceDiag(index, 1)">
                  {{ item.device ? "重新选择" : "请选择设备" }}
                </el-link>
              </span>
              <el-select
                v-show="
                  item.deviceType == 0 || (item.deviceType == 1 && item.device)
                "
                v-model="item.serviceId"
                placeholder="请选择服务"
                size="mini"
                @change="changeService($event, index, 1)"
              >
                <el-option
                  v-for="item in serviceList"
                  :key="item.id"
                  :label="item.serviceName"
                  :value="item.id"
                ></el-option>
              </el-select>
              <el-select
                v-show="item.serviceId"
                v-model="item.propertiesId"
                placeholder="请选择属性"
                size="mini"
                @change="changeProperties($event, index)"
              >
                <el-option
                  v-for="item in propertiesList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                ></el-option>
              </el-select>
              <el-select
                v-show="item.serviceId"
                v-model="item.datatype"
                placeholder="请选择"
                style="width: 120px"
                size="mini"
                @change="changeDataType($event, index)"
              >
                <el-option
                  v-for="item in datatypeList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                ></el-option>
              </el-select>
              <el-input
                v-if="item.datatype == 6"
                style="width: 60px"
                placeholder=">="
                v-model="item.min"
                clearable
                size="mini"
              >
              </el-input>
              <el-input
                v-if="item.datatype == 6"
                style="width: 60px"
                placeholder="<="
                v-model="item.max"
                clearable
                size="mini"
              >
              </el-input>
              <el-input
                v-if="item.datatype && item.datatype != 6"
                style="width: 100px"
                placeholder=""
                v-model="item.datatypeValue"
                clearable
                size="mini"
              >
              </el-input>
              <el-button size="mini" v-show="item.serviceId"
                >触发机制
                <el-divider direction="vertical"></el-divider>触发时效300秒
                <i class="el-icon-setting"></i
              ></el-button>
            </span>
            <el-link class="fr" @click="conditionForm.splice(index, 1)"
              >删除</el-link
            >
          </el-form-item>
          <el-divider></el-divider>
        </div>
        <el-row>
          <el-col>
            <el-button
              type="primary"
              icon="el-icon-plus"
              size="mini"
              plain
              @click="addAttrEnum"
              >添加条件
            </el-button>
          </el-col>
        </el-row>
      </el-form>
      <div class="btn">
        <el-button type="primary" size="mini" @click="stepActive -= 1"
          >上一步</el-button
        >
        <el-button type="primary" size="mini" @click="stepNext(2)"
          >确 定</el-button
        >
      </div>
    </div>
    <div class="info" v-if="stepActive == 2">
      <el-row>
        <el-col> 执行动作 </el-col>
      </el-row>
      <el-form>
        <div class="condition" v-if="actionForm.length == 0">
          <span>尚未设置动作</span>
        </div>
        <div v-for="(item, index) in actionForm" :key="index">
          <el-form-item label="动作">
            <el-select
              v-model="item.actionType"
              placeholder="请选择"
              style="width: 150px"
              size="mini"
              @change="changeActionType($event, index)"
            >
              <el-option
                v-for="item in actionTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>
            <span v-show="item.actionType == 1">
              <el-tooltip placement="top" v-if="item.device">
                <div slot="content">
                  设备名称：{{ item.device.deviceName || "（未设置）" }}<br />
                  设备标识码：{{
                    item.device.deviceIdentification || "（未设置）"
                  }}<br />
                  所属产品：{{ item.productIdentification || "" }}
                </div>
                <span>{{ item.device.deviceName || "未命名设备" }}</span>
              </el-tooltip>
            </span>
            <el-link @click="showDeviceDiag(index, 2)">
              {{ item.deviceId ? "重新选择" : "指定下发设备" }}
            </el-link>
            <el-select
              v-show="item.device"
              v-model="item.serviceId"
              placeholder="请选择服务"
              size="mini"
              @change="changeService($event, index, 2)"
            >
              <el-option
                v-for="item in serviceList"
                :key="item.id"
                :label="item.serviceName"
                :value="item.id"
              ></el-option>
            </el-select>
            <el-select
              v-show="item.serviceId"
              v-model="item.commandId"
              placeholder="请选择命令"
              size="mini"
              @change="changeCommandId($event, index)"
            >
              <el-option
                v-for="item in serviceList"
                :key="item.id"
                :label="item.serviceName"
                :value="item.id"
              ></el-option>
            </el-select>

            <el-link v-show="item.commandId" @click="showParamsDialog(index)"
              >参数配置</el-link
            >

            <el-link class="fr" @click="actionForm.splice(index, 1)"
              >删除</el-link
            >
          </el-form-item>
          <el-divider></el-divider>
        </div>
        <el-row>
          <el-col>
            <el-button
              type="primary"
              icon="el-icon-plus"
              size="mini"
              plain
              @click="addActionEnum"
              >添加动作
            </el-button>
          </el-col>
        </el-row>
      </el-form>
      <div class="btn">
        <el-button type="primary" size="mini" @click="stepActive -= 1"
          >上一步</el-button
        >
        <el-button type="primary" size="mini" @click="stepNext(3)"
          >确 定</el-button
        >
      </div>
    </div>
    <el-dialog
      title="Cron表达式生成器"
      :visible.sync="openCron"
      append-to-body
      destroy-on-close
      class="scrollbar"
    >
      <crontab
        @hide="openCron = false"
        @fill="crontabFill"
        :expression="expression"
      ></crontab>
    </el-dialog>

    <!-- <el-drawer title="选择设备" width="60%" :visible.sync="openSelectDevice">
      <el-table
        :data="deviceList"
        style="width: 100%"
        @current-change="handleCurrentChange"
      >
        <el-table-column label width="45">
          <template slot-scope="scope">
            <el-radio
              :label="scope.row.id"
              @change="conditionFormItemChange(scope.row)"
              v-model="deviceItem.deviceId"
              ><span></span
            ></el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="设备名称" width="180">
        </el-table-column>
        <el-table-column
          prop="deviceIdentification"
          label="设备标识"
          width="180"
        >
        </el-table-column>
        <el-table-column prop="deviceDescription" label="描述">
        </el-table-column>
      </el-table>
      <span class="drawer-footer">
        <el-button type="primary" size="mini" @click="handleOk"
          >确 定</el-button
        >
      </span>
    </el-drawer> -->
    <el-dialog
      title="下发命令参数配置"
      width="500"
      :visible.sync="paramsDialog"
    >
      <el-form v-if="paramsDialog">
        <el-form-item label="执行命令">
          <el-select
            v-model="actionForm[paramsIndex].commandId"
            placeholder="请选择"
            disabled
            size="small"
          >
            <el-option
              v-for="item in serviceList"
              :key="item.id"
              :label="item.serviceName"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="参数">
          <el-input
            v-model="actionForm[paramsIndex].params"
            style="width: 200px; margin: 0 10px"
            size="small"
            placeholder="请输入参数"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleParamsOk">确 定</el-button>
      </span>
    </el-dialog>
    <TLSelectDevice
      ref="TLSelectDevice"
      :productIdentification="productIdentification"
      :params="{}"
      @handleOk="handleOk"
    ></TLSelectDevice>
  </div>
</template>

<script>
import Crontab from "@/components/Crontab";
import {
  listProduct,
  // listDevice,
  listService,
  listProperties,
  ruleSaveBasic, //  保存基本信息
} from "@/api/rule/deviceLinkage";
import { listDevice } from "@/api/link/device/device";
export default {
  name: "index.vue",
  components: { Crontab },
  dicts: ["sys_job_group", "sys_job_status"],
  data() {
    return {
      form: {},
      ruleBasic: {}, // 规则基础信息数据集合
      detail: {
        ruleBasic: {
          id: 6,
        },
      }, // 详情
      stepActive: 1, // 步骤条
      productIdentification: "", // 选择的产品标识

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
      openCron: false,
      // 传入的表达式
      expression: "",
      radioValue: "启用",
      value: "",
      options: [
        {
          label: "全部",
          value: 0,
        },
        {
          label: "任意一个",
          value: 1,
        },
      ],

      conditionTypeOptions: [
        { label: "设备属性触发", value: 0 },
        { label: "定时触发", value: 1 },
      ],
      productValue: "",
      productList: [],

      deviceNumValue: "",
      deviceList: [], // 设备列表
      deviceItem: {}, // 选择的设备

      conditionForm: [], // 条件参数
      nowIndex: 0, // 条件参数选择的index

      serviceList: [], // 服务列表

      propertiesList: [], // 属性列表

      datatypeList: [
        {
          label: "=",
          value: 1,
        },
        {
          label: ">",
          value: 2,
        },
        {
          label: "<",
          value: 3,
        },
        {
          label: ">=",
          value: 4,
        },
        {
          label: "<=",
          value: 5,
        },
        {
          label: "设置范围",
          value: 6,
        },
      ],

      selectDevice: false,
      openSelectDevice: false,

      selectType: 1, // 选择设备弹窗时 类型  1是触发机制 2是执行动作  数据源不一样
      actionForm: [], // 命令参数
      actionTypeOptions: [
        { label: "下发命令", value: 1, tips: "" },
        // { label: "发送通知", value: 2, tips: "" },
        // {
        //   label: "上报警告",
        //   value: 3,
        //   tips: "产生的告警将统一上报到应用运维管理服务（AOM）",
        // },
        // {
        //   label: "恢复警告",
        //   value: 4,
        //   tips: "产生的告警将统一上报到应用运维管理服务（AOM）",
        // },
      ],
      paramsDialog: false, // 参数配置弹窗
      paramsIndex: 0, // 参数配置的Index
    };
  },
  created() {
    this.getProductList();
  },
  methods: {
    // step 点击下一步
    stepNext(step) {
      console.log(step);
      if (step == 1) {
        ruleSaveBasic({
          ...this.ruleBasic,
        }).then((res) => {
          this.detail.ruleBasic = res.data;
          this.stepActive = step;
        });
      } else if (step == 2) {
        // TODO 触发机制和执行动作
        this.detail.ruleBasic.id = 6; // 测试id为6的
        console.log(this.detail.ruleBasic.id);
        console.log(this.conditionForm);
      }
      this.stepActive = step;
    },

    // 添加动作
    addActionEnum() {
      let obj = {
        actionType: 1,
      };
      this.actionForm.push(obj);
    },

    // 添加条件
    addAttrEnum() {
      let obj = {
        conditionType: 0,
        productIdentification: "",
        deviceType: null,
        deviceIdentificationOpen: false,
        deviceNumValue: "",
      };
      this.conditionForm.push(obj);
    },
    /** cron表达式按钮操作 */
    handleShowCron() {
      this.expression = this.form.cronExpression;
      this.openCron = true;
    },
    /** 确定后回传值 */
    crontabFill(value) {
      this.form.cronExpression = value;
    },
    /**选择条件**/
    changeConditionType(value, index) {
      this.conditionForm[index].conditionType = value;
    },
    showParamsDialog(index) {
      this.paramsIndex = index;
      this.paramsDialog = true;
    },
    // 选择服务
    changeService(value, index, type) {
      if (type == 1) {
        this.conditionForm[index].serviceId = value;
        this.getPropertiesList(value);
      } else if (type == 2) {
        this.actionForm[index].serviceId = value;
        // TODO 此处应变更为查询命令
        this.getPropertiesList(value);
      }
    },
    handleParamsOk() {
      this.paramsDialog = false;
      console.log(this.actionForm);
    },
    // 选择命令
    changeCommandId(val) {
      console.log(val);
    },
    // 选择属性
    changeProperties(value, index) {
      this.conditionForm[index].attributeId = value;
    },
    // 选择运算符
    changeDataType(value, index) {
      this.conditionForm[index].dataType = value;
    },
    /***选择产品***/
    changeProduct(value, index) {
      console.log(value);
      this.conditionForm[index].productValue = value;
      this.productValue = value;
      this.getServiceList(value);
    },
    /** 查询产品列表 */
    getProductList() {
      this.loading = true;
      listProduct(this.queryParams).then((response) => {
        console.log(response);
        this.productList = response.data;
        this.loading = false;
      });
    },
    /***选择产品***/
    changeDeviceType(value, index) {
      if (value == 1) {
        // 可一个接口查询服务
        this.nowIndex = index;
      } else {
        this.openSelectDevice = false;
      }
    },
    conditionFormItemChange(row) {
      this.deviceItem.device = row;
    },
    // 选择设备
    handleOk(e) {
      console.log(e);
      this.openSelectDevice = false;
      if (this.selectType == 1) {
        this.$set(this.conditionForm[this.nowIndex], "device", e);
      } else if (this.selectType == 2) {
        this.$set(this.actionForm[this.nowIndex], "device", e);
        this.getServiceList("8672340058954281bd34b265586ec45e");
      }
      this.deviceItem = {};
    },
    async showDeviceDiag(index, type) {
      this.selectType = type;
      // type = 1 触发机制   2  执行动作
      if (this.selectType == 1) {
        this.productIdentification = this.conditionForm[index].productValue;
      } else if (this.selectType == 2) {
        // TODO 先固定一个产品id
        await this.getDeviceList("8672340058954281bd34b265586ec45e");
      }
      // this.openSelectDevice = true;
      this.$refs.TLSelectDevice.open();
    },
    /** 查询产品服务列表 */
    getServiceList(id) {
      this.loading = true;
      listService(id).then((response) => {
        this.serviceList = response.data;
        this.loading = false;
      });
    },
    /** 查询属性列表 */
    getPropertiesList(id) {
      //this.attributeList = [{id: 1, name: "属性名称1",}, {id: 2, name: "属性名称2",},];
      this.loading = true;
      listProperties(id).then((response) => {
        this.propertiesList = response.data;
        this.loading = false;
      });
    },
    /** 查询设备档案列表 */
    async getDeviceList(productIdentification) {
      this.loading = true;
      await listDevice({
        productIdentification,
        page: 1,
        pageSize: 100,
      }).then((response) => {
        this.deviceList = response.data;
        this.loading = false;
      });
    },
    handleCurrentChange(currentRow, oldCuurentRow) {
      //this.id
    },
  },
};
</script>

<style scoped lang="scss">
.wrap {
  width: 100%;
  height: 100%;
  padding: 15px 30px;
  box-sizing: border-box;

  .info {
    width: 100%;
    height: 100%;
    background: #f5f7fa;
    padding: 20px 50px;
    box-sizing: border-box;
    font-weight: 700;
    color: rgb(77, 74, 74);
    margin: 0 0 20px 0;
  }

  .el-form {
    padding: 20px 20px;

    .condition {
      width: 100%;
      height: 40px;
      background: white;
      padding: 20px 50px;
      display: flex;
      justify-content: center;
      align-items: center;
      color: #606266;
      font-size: 12px;
    }
    .el-select {
      padding: 0 10px;
    }
    .el-button {
      // margin-top: 10px;
    }
    .selectDevice {
      background-color: #ffffff;
      padding: 6px 8px;
      argin-left: 1rem !important;
      color: #526ecc;
      cursor: pointer;
    }
  }
}
.el-divider--horizontal {
  margin: 6px 0;
}
.el-form-item,
.condition {
  margin-bottom: 10px;
}
.steps {
  width: 80%;
  margin: 20px auto;
}
.btn {
  text-align: center;
}
</style>
