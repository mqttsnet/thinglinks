<template>
  <div class="app-container">
    <div class="device_status">
      <div class="device_attribute">
        <p>
          <span>设备名称</span>
          <span>{{ deviceInfo.deviceName }}</span>
        </p>
        <p>
          <span>设备状态</span>
          <dict-tag :options="dict.type.link_device_connect_status" :value="deviceInfo.connectStatus"/>
        </p>
      </div>
      <div class="device_attribute">
        <p>
          <span>设备标识</span>
          <span>
            <i class="el-icon-copy-document" style="cursor: pointer;" title="复制"
               @click="copy(deviceInfo.deviceIdentification)"></i>
            {{ deviceInfo.deviceIdentification }}
          </span>
        </p>
        <p>
          <span>客户端标识</span>
          <span>
            <i class="el-icon-copy-document" style="cursor: pointer;" title="复制"
               @click="copy(deviceInfo.clientId)"></i>
            {{ deviceInfo.clientId }}
          </span>
        </p>
      </div>
      <div class="device_attribute">
        <p>
          <span>用户名</span>
          <span>
            <i class="el-icon-copy-document" style="cursor: pointer;" title="复制"
               @click="copy(deviceInfo.userName)"></i>
            {{ deviceInfo.userName }}
          </span>
        </p>
        <p>
          <span>密码</span>
          <span v-if="show">
            <i class="el-icon-view" style="cursor: pointer;" @click="setShow"></i>
            ********
          </span>
          <span v-if="!show">
            <i class="el-icon-view" style="cursor: pointer;" @click="setShow"></i>
            {{ deviceInfo.password }}
          </span>
        </p>
      </div>
    </div>
    <div class="detail">
      <el-tabs v-model="activeName">
        <el-tab-pane label="基本信息" name="first">
          <div class="device_attribute">
            <p>
              <span>产品标识</span>
              <span>{{ deviceInfo.productIdentification }}</span>
            </p>
            <p>
              <span>设备类型</span>
              <span>
                <dict-tag :options="dict.type.link_device_device_type" :value="deviceInfo.deviceType"/>
              </span>
            </p>
            <p>
              <span>认证方式</span>
              <span>{{ deviceInfo.authMode }}</span>
            </p>
            <p>
              <span>产品协议类型</span>
              <span>{{ deviceInfo.protocolType }}</span>
            </p>

          </div>
          <div class="device_attribute">
            <p>
              <span>加密方式</span>
              <span>
                <dict-tag :options="dict.type.link_device_encrypt_method" :value="deviceInfo.encryptMethod"/>
              </span>
            </p>
            <p>
              <span>加密密钥</span>
              <span>{{ deviceInfo.encryptKey }}</span>
            </p>
            <p>
              <span>加密向量</span>
              <span>{{ deviceInfo.encryptVector }}</span>
            </p>
            <p>
              <span>签名密钥</span>
              <span>{{ deviceInfo.signKey }}</span>
            </p>

          </div>
          <div class="device_attribute">

            <p>
              <span>软件版本</span>
              <span>{{ deviceInfo.swVersion }}</span>
            </p>

            <p>
              <span>固件版本</span>
              <span>{{ deviceInfo.fwVersion }}</span>
            </p>

            <p>
              <span>sdk版本</span>
              <span>{{ deviceInfo.deviceSdkVersion }}</span>
            </p>


            <p>
              <span>是否遗言</span>
              <span v-text="deviceInfo.isWill == null ? '否' : deviceInfo.isWill"></span>
            </p>
          </div>

          <div class="device_attribute">
            <p>
              <span>创建者</span>
              <span>{{ deviceInfo.createBy }}</span>
            </p>
            <p>
              <span>更新者</span>
              <span>{{ deviceInfo.updateBy }}</span>
            </p>
            <p>
              <span>创建时间</span>
              <span>{{ deviceInfo.createTime }}</span>
            </p>
            <p>
              <span>更新时间</span>
              <span>{{ deviceInfo.updateTime }}</span>
            </p>
          </div>
        </el-tab-pane>
        <el-tab-pane label="Topic列表" name="second" style="width:100%;height:100%">
          <div style="width:100%;height:100%">
            <Topic :deviceIdentification="this.deviceIdentification"></Topic>
          </div>
        </el-tab-pane>
        <el-tab-pane label="设备动作" name="third" style="width:100%;height: 100%;">
          <div style="width:100%;height:100%">
            <Action :deviceIdentification="this.deviceIdentification"></Action>
          </div>
        </el-tab-pane>
        <el-tab-pane v-if="shadowShow" label="设备影子" name="fourth" style="width:100%;height: 100%;">
          <el-tabs v-model="shadowActiveName" style="width:100%;height: 100%;">
            <el-tab-pane label="列表" name="first" style="width:100%;height:100%;">
              <el-date-picker @change="timeControls" style="margin-bottom: 10px;" v-model="value1" type="datetimerange"
                              value-format="yyyy-MM-dd HH:mm:ss" range-separator="至" start-placeholder="开始日期"
                              end-placeholder="结束日期">
              </el-date-picker>
              <el-button style="position: absolute;right:20px" icon="el-icon-refresh" @click="getShadowData"
                         circle></el-button>
              <el-tabs v-model="editableTabsValue" type="card">
                <el-tab-pane v-for="(value, name, index) in ShadowData" :key="index" :label="name"
                             :name="String(index + 1)" style="width:100%;height: 100%;">
                  <el-table v-if="Array.isArray(value)" :data="value" style="width: 100%" max-height="450"
                            :fit="true">
                    <el-table-column prop="index" label="序号" style="width: 25%">
                      <template slot-scope="scope">
                        {{ scope.$index + 1 }}
                      </template>
                    </el-table-column>
                    <el-table-column v-for="(ShadowValue, ShadowName, index1) in value[0]" :key="index1"
                                     :label="ShadowName" :prop="ShadowName" style="width: 25%">
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </el-tab-pane>
            <el-tab-pane label="JSON" name="second" style="width: 100%;height:100%">
              <el-button size="medium" style="margin: 10px 0 10px 0" type="primary" @click="decoration">
                格式化
              </el-button>
              <el-input class="textJson" type="textarea" style="width:100%" :autosize="{ minRows: 5 }" resize="none"
                        :value="detailJSON" placeholder="无内容">
              </el-input>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
import {getDevice, getDeviceShadow} from "@/api/link/device/device";
import Topic from "@/views/link/device/topic";
import Action from "@/views/link/device/action";

export default {
  name: "device-detail",
  components: {Action, Topic},
  dicts: [
    "link_device_device_type",
    "link_device_connect_status",
    "link_device_encrypt_method",
  ],
  data() {
    return {
      //密码切换
      show: true,
      activeName: "first",
      deviceId: null,
      deviceIdentification: null,
      //设备详细
      deviceInfo: {},
      //table切换
      shadowActiveName: "first",
      editableTabsValue: '1',
      //影子数据
      ShadowData: {},
      //json数据转换
      detailJSON: "",
      shadowShow: false,
      value1: [],
      // 查询子设备影子数据
      data: {
        ids: "",
        startTime: "",
        endTime: "",
      },
    }
  },
  created() {
    const deviceId = this.$route.params && this.$route.params.deviceId;
    if (deviceId) {
      this.deviceId = deviceId
      this.getDetail()
    }
  },
  watch: {
    activeName(value) {
      if (value === 'fourth') {
        this.data.ids = this.deviceId
        this.getShadowData()
      }
    },
  },
  methods: {
    timeControls() {
      this.data.startTime = this.value1[0]
      this.data.endTime = this.value1[1]
      this.getShadowData()
    },
    // 查询子设备影子数据
    getShadowData() {
      this.loading = true
      getDeviceShadow(this.data).then(res => {
        this.ShadowData = res.data
        this.detailJSON = JSON.stringify(res.data)
        this.loading = false
      })
    },
    //验证json并格式化
    decoration() {
      if (this.isJSON(this.detailJSON)) {
        const jdata = JSON.stringify(
          JSON.parse(this.detailJSON),
          null,
          4
        );
        this.detailJSON = jdata;
      } else {
        this.$toast.fail("不是正确的json格式");
      }
    },
    isJSON(str) {
      if (typeof str === "string") {
        try {
          var obj = JSON.parse(str);
          if (typeof obj === "object" && obj) {
            return true;
          } else {
            return false;
          }
        } catch (e) {
          this.$toast.fail("不是json");
          return false;
        }
      }
    },
    //设备详情
    getDetail() {
      getDevice(this.deviceId).then((response) => {
        this.deviceInfo = response.data
        this.deviceIdentification = response.data.deviceIdentification
        if (response.data.deviceType == 'COMMON') {
          this.shadowShow = true;
        } else {
          this.shadowShow = false;
        }
      })
    },
    //密码切换
    setShow() {
      this.show = !this.show
    },
    //复制
    copy(text) {
      var input = document.createElement("input");
      input.value = text;
      document.body.appendChild(input);
      input.select()
      document.execCommand("Copy");
      document.body.removeChild(input);
      this.$message({
        message: '复制成功',
        type: 'success'
      });
    },
  }
}
</script>
<style lang="scss" scoped>
.device_status {
  width: 100%;
  margin: 0 0 10px 10px;
  padding: 20px 30px;
  background: #F8F8F9;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 700;
  color: #515a6e;
}

.device_status .status {
  width: 20%;
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.device_attribute {
  width: 30%;

  p {
    width: 100%;
    display: flex;
    align-items: center;

    span:first-child {
      display: block;
      width: 20%;
    }
  }
}

.detail {
  width: 100%;
  height: 100%;
  margin: 0 0 10px 10px;
  padding: 20px 30px;
  background: #F8F8F9;
  font-size: 14px;
  font-weight: 700;
  color: #515a6e;

  .el-tabs {
    height: 100%;

    .el-tab-pane {
      width: 80%;
      height: 300px;
      display: flex;
      justify-content: space-between;

      .el-tabs {
        .el-tab-pane {
          display: block;
        }
      }

      .device_attribute {
        width: 35%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: space-around;

        p {
          width: 100%;
          display: flex;
          align-items: center;

          span:first-child {
            display: block;
            width: 30%;
          }
        }
      }
    }
  }

}

.demo-input-suffix {
  display: flex;
  align-items: center;
  margin-bottom: 10px;

  span {
    width: 30%;
    text-align: end;
  }

  .el-input,
  .el-select {
    width: 50%;
  }
}

.pagination-container {
  height: 50px;
}
</style>
