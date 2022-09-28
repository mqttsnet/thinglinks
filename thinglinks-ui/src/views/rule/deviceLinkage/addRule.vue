<template>
  <div class="wrap">
    <div class="info">
      <el-row>
        <el-col>
          基本信息
        </el-col>
      </el-row>
      <el-form ref="form" label-width="100px" size="small">
        <el-row>
          <el-col :span="6">
            <el-form-item label="规则名称">
              <el-input></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="规则标识">
              <el-input></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="应用ID">
              <el-input></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="cron表达式" prop="cronExpression">
              <el-input v-model="form.cronExpression" placeholder="请输入cron执行表达式">
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
          <el-col :span="6">
            <el-form-item label="错误策略" prop="misfirePolicy">
              <el-radio-group v-model="form.misfirePolicy" size="small">
                <el-radio-button label="1">立即执行</el-radio-button>
                <el-radio-button label="2">执行一次</el-radio-button>
                <el-radio-button label="3">放弃执行</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="是否并发" prop="concurrent">
              <el-radio-group v-model="form.concurrent" size="small">
                <el-radio-button label="0">允许</el-radio-button>
                <el-radio-button label="1">禁止</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.sys_job_status"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="规则描述">
              <el-input type="textarea"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div class="info" style="height: 380px;">
      <el-row>
        <el-col>
          触发机制
        </el-col>
      </el-row>
      <el-form>
        <el-row>
          <el-col :span="8">
            <el-form-item label="需满足">
              <el-select v-model="value" placeholder="请选择">
                <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value">
                </el-option>
              </el-select>
              <span style="margin-left: 10px;color: #606266;">以下条件</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <div class="condition">
              <span>尚未设置条件</span>
            </div>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-button type="primary" icon="el-icon-plus" size="mini" plain @click="addAttrEnum">添加条件
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <el-dialog title="Cron表达式生成器" :visible.sync="openCron" append-to-body destroy-on-close class="scrollbar">
      <crontab @hide="openCron=false" @fill="crontabFill" :expression="expression"></crontab>
    </el-dialog>
  </div>
</template>

<script>
  import Crontab from '@/components/Crontab'
  export default {
    name: "index.vue",
     components: { Crontab },
    dicts: ['sys_job_group', 'sys_job_status'],
    data() {
      return {
        form:{},
        openCron:false,
        // 传入的表达式
        expression: "",
        radioValue: "启用",
        value: "",
        options: [{
          label: "全部",
          value: 0
        }, {
          label: "任意一个",
          value: 1
        }, ]
      }
    },
    methods: {
      addAttrEnum() {
        console.log(11)
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
    }
  }
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
      background: #EEF0F5;
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

      .el-button {
        margin-top: 10px;
      }
    }
  }
</style>
