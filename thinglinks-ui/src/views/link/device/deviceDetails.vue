<template>
  <div class="app-container">
    <div class="equipment_status">
      <div class="equipment_attribute">
        <p>
          <span>设备名称</span>
          <span>{{ deviceInfo.deviceName }}</span>
        </p>
        <p>
          <span>设备状态</span>
          <dict-tag :options="dict.type.link_device_connect_status" :value="deviceInfo.connectStatus"/>
        </p>
      </div>
      <div class="equipment_attribute">
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
              <i style="cursor: pointer;" title="复制" class="el-icon-copy-document"
                 @click="copy(deviceInfo.clientId)"></i>
              {{ deviceInfo.clientId }}
          </span>
        </p>
      </div>
      <div class="equipment_attribute">
        <p>
          <span>用户名</span>
          <span>
                        <i style="cursor: pointer;" title="复制" class="el-icon-copy-document"
                           @click="copy(deviceInfo.userName)"></i>
                        {{ deviceInfo.userName }}
                    </span>
        </p>
        <p>
          <span>密码</span>
          <span v-if="show">
                        <i style="cursor: pointer;" class="el-icon-view" @click="setShow"></i>
                        ********
                    </span>
          <span v-if="!show">
                        <i style="cursor: pointer;" class="el-icon-view" @click="setShow"></i>
                        {{ deviceInfo.password }}
                    </span>
        </p>
      </div>
    </div>
    <div class="detail">

      <el-tabs v-model="activeName">
        <el-tab-pane label="基本信息" name="first">
          <div class="equipment_attribute">
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
          <div class="equipment_attribute">
            <p>
              <span>加密方式</span>
              <span>
                <dict-tag :options="dict.type.link_device_encrypt_method" :value="deviceInfo.encryptMethod"/>
              </span>
            </p>
            <p>
              <span>加密密钥</span>
              <span>
                    <i style="cursor: pointer;" title="复制" class="el-icon-copy-document"
                       @click="copy(deviceInfo.encryptKey)"></i>
                    {{ deviceInfo.encryptKey }}
              </span>
            </p>
            <p>
              <span>加密向量</span>
              <span>
                    <i style="cursor: pointer;" title="复制" class="el-icon-copy-document"
                       @click="copy(deviceInfo.encryptVector)"></i>
                    {{ deviceInfo.encryptVector }}
              </span>
            </p>
            <p>
              <span>签名密钥</span>
              <span>
                    <i style="cursor: pointer;" title="复制" class="el-icon-copy-document"
                       @click="copy(deviceInfo.signKey)"></i>
                    {{ deviceInfo.signKey }}
              </span>
            </p>
          </div>
          <div class="equipment_attribute">
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

          <div class="equipment_attribute">
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
          <el-tabs v-model="TopicactiveName" @tab-click="topicSwitch" style="width:100%;height:100%">
            <el-tab-pane label="基础Topic" name="first" style="width:100%;height:100%">
              <el-table v-loading="loading" :data="topicList" @selection-change="handleSelectionChange"
                        style="width:100%" max-height="500">
                <el-table-column type="selection" width="55" align="center"/>
                <el-table-column label="id" align="center" prop="id"/>
                <el-table-column label="设备标识" align="center" prop="deviceIdentification"/>
                <el-table-column label="topic" align="center" prop="topic"/>
                <el-table-column label="发布者" align="center" prop="publisher"/>
                <el-table-column label="订阅者" align="center" prop="subscriber"/>
                <el-table-column label="备注" align="center" prop="remark"/>
              </el-table>
              <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum"
                          :limit.sync="queryParams.pageSize" @pagination="getList"/>
            </el-tab-pane>
            <el-tab-pane label="自定义Topic" name="second" style="width:100%;height: 100%;">
              <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                         v-hasPermi="['link:topic:add']">新增
              </el-button>
              <el-button style="position: absolute;right:20px" icon="el-icon-refresh" @click="getList"
                         circle>
              </el-button>
              <el-table v-loading="loading" :data="topicList" @selection-change="handleSelectionChange"
                        style="width:100%;height:100%;margin-top: 20px;" max-height="500">
                <el-table-column type="selection" width="55" align="center"/>
                <el-table-column label="id" align="center" prop="id"/>
                <el-table-column label="设备标识" align="center" prop="deviceIdentification"/>
                <el-table-column label="topic" align="center" prop="topic"/>
                <el-table-column label="发布者" align="center" prop="publisher"/>
                <el-table-column label="订阅者" align="center" prop="subscriber"/>
                <el-table-column label="备注" align="center" prop="remark"/>
                <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                  <template slot-scope="scope">
                    <el-button size="mini" type="text" icon="el-icon-edit"
                               @click="handleUpdate(scope.row)" v-hasPermi="['link:topic:edit']">修改
                    </el-button>
                    <el-button size="mini" type="text" icon="el-icon-delete"
                               @click="handleDelete(scope.row)" v-hasPermi="['link:topic:remove']">删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum"
                          :limit.sync="queryParams.pageSize" @pagination="getList"/>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
        <el-tab-pane label="设备动作" name="fourth" style="width:100%;height: 100%;">
          <div style="width:100%;height:100%">
            <el-button style="position: absolute;right:20px" icon="el-icon-refresh"
                       @click="getEquipmentActionList" circle>
            </el-button>
            <el-table :data="equipmentActionList" style="width: 100%;height: 100%;margin-top: 45px;"
                      max-height="450">
              <el-table-column prop="id" label="ID">
              </el-table-column>
              <el-table-column prop="deviceIdentification" label="设备标识">
              </el-table-column>
              <el-table-column prop="message" label="信息">
              </el-table-column>
              <el-table-column prop="actionType" label="操作类型">
                <template slot-scope="scope">
                  <dict-tag :options="dict.type.link_device_action_type"
                            :value="scope.row.actionType"/>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态">
                <template slot-scope="scope">
                  <el-button type="success" size="mini" v-text="scope.row.status ? '成功' : '失败'">
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间">
              </el-table-column>
            </el-table>
            <pagination v-show="actionListTotal > 0" :total="actionListTotal"
                        :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
                        @pagination="getEquipmentActionList"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <!-- 新增||修改topic -->
    <el-dialog :title="title" :visible.sync="open" width="30%" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="20%">
        <el-form-item label="设备标识" prop="deviceIdentification">
          <el-input v-model="form.deviceIdentification" placeholder="请输入设备标识"/>
        </el-form-item>
        <el-form-item label="类型" prop="type" style="width:100%">
          <el-select v-model="form.type" placeholder="请选择类型" style="width:100%">
            <el-option label="1" value=1 />
          </el-select>
        </el-form-item>
        <el-form-item label="topic" prop="topic">
          <el-input v-model="form.topic" placeholder="请输入topic"/>
        </el-form-item>
        <el-form-item label="发布者" prop="publisher">
          <el-input v-model="form.publisher" placeholder="请输入发布者"/>
        </el-form-item>
        <el-form-item label="订阅者" prop="subscriber">
          <el-input v-model="form.subscriber" placeholder="请输入订阅者"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"/>
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
import {getDevice} from "@/api/link/device/device";
import {addTopic, listTopic, updateTopic} from "@/api/link/device/topic";
import {listAction} from "@/api/link/device/action";

export default {
  dicts: [
    "link_device_connect_status",
    "link_device_action_type",
    "link_device_encrypt_method",
  ],
  data() {
    return {
      //分页总条数
      total: 0,
      actionListTotal: 0,
      //设备动作列表
      equipmentActionList: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 遮罩层
      loading: true,
      //新增按鈕
      add: false,
      //修改自定义topic
      dialogFormVisible: false,
      setTopicData: {},
      title: "",
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        deviceIdentification: [
          {required: true, message: "设备标识不能为空", trigger: "blur"}
        ],
      },
      //table切换
      activeName: 'first',
      TopicactiveName: "first",
      //设备详细
      deviceInfo: {},
      //密码切换
      show: true,
      // 设备Topic数据表格数据
      topicList: [],
      //json数据转换
      detailJSON: "",
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceIdentification: null,
        type: 0,
        topic: null,
        publisher: null,
        subscriber: null,
      },
    }
  },
  watch: {
    deviceInfo() {
      this.queryParams.deviceIdentification = this.deviceInfo.deviceIdentification
      this.getList()
    },
    activeName(value) {
      if (value === 'fourth') {
        this.getEquipmentActionList()
      }
    }
  },
  methods: {
    //分页
    turnThePage(page) {
      console.log(page);
      this.queryParams.pageNum = page
      this.getList();
    },
    //上一页
    pageUp(page) {
      console.log(page);
      this.queryParams.pageNum = page
      this.getList();
    },
    pageNext(page) {
      console.log(page);
      this.queryParams.pageNum = page
      this.getList();
    },
    //下一页
    /** 导出按钮操作 */
    handleExport() {
      this.download('link/topic/export', {
        ...this.queryParams
      }, `link_topic.xlsx`)
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加设备Topic数据";
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateTopic(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addTopic(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        deviceIdentification: null,
        type: null,
        topic: null,
        publisher: null,
        subscriber: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
      };
      this.resetForm("form");
    },
    //切换topic列表
    topicSwitch() {
      // console.log(this.TopicactiveName);
      if (this.TopicactiveName == 'first') {
        this.add = false
        this.queryParams.type = 0
        this.getList()
      } else if (this.TopicactiveName == 'second') {
        this.add = true
        this.queryParams.type = 1
        this.getList()
      }
    },
    //查询设备动作列表
    getEquipmentActionList() {
      console.log(11);
      this.loading = true;
      // console.log(this.deviceInfo);
      this.queryParams.deviceIdentification = this.deviceInfo.deviceIdentification
      listAction(this.queryParams).then(response => {
        // console.log(response);
        this.equipmentActionList = response.rows
        this.actionListTotal = response.total
        // console.log(this.equipmentActionList);
        this.loading = false;
      }).catch(response, err => {
        console.log(response, err);
      })
    },
    /** 查询设备Topic数据列表 */
    getList() {
      this.loading = true;
      listTopic(this.queryParams).then(response => {
        // console.log(this.queryParams);
        this.actionListTotal = response.total
        this.topicList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
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
    //设备详细获取
    getDetail() {
      getDevice(this.id).then((response) => {
        this.deviceInfo = response.data
        this.detailJSON = JSON.stringify(response.data)
        console.log(response.data);
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
    }
  },
  created() {
    this.id = this.$route.query.id
    this.getDetail();
  }
}
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
  justify-content: space-between;
  font-size: 14px;
  font-weight: 700;
  color: #515a6e;
}

.equipment_status .status {
  width: 20%;
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.equipment_attribute {
  width: 30%;

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

      .equipment_attribute {
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
