<template>
  <div class="app-container" style="width: 500px">
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="Topic" prop="topic">
        <el-input v-model="form.topic" placeholder="请输入topic" />
      </el-form-item>
      <el-form-item label="QoS等级" prop="qos">
        <el-select v-model="form.qos" placeholder="请选择QoS等级">
          <el-option label="QoS0 至多一次" value="0" />
          <el-option label="QoS1 至少一次" value="1" />
          <el-option label="QoS2 确保只有一次" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否保留" prop="retain">
        <el-select disabled v-model="form.retain" placeholder="是否保留消息">
          <el-option label="否" value="false" />
          <el-option label="是" value="true" />
        </el-select>
      </el-form-item>
      <el-form-item label="消息" prop="message">
        <el-input v-model="form.message" placeholder="请输入内容" type="textarea" />
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm">发 送</el-button>
      <el-button @click="cancel">重 置</el-button>
    </div>
  </div>
</template>

<script>
import { sendMsg } from "@/api/broker/publish";

export default {
  name: "sendMsg",
  data() {
    return {
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        topic: [
          { required: true, message: "Topic不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.reset();
  },
  methods: {
    // 取消按钮
    cancel() {
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        topic: null,
        qos: null,
        retain: "false",
        message: null,
      };
      this.resetForm("form");
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          sendMsg(this.form).then(response => {
            this.$modal.msgSuccess("发送成功");
            this.reset();
          });
        }
      });
    },
  }
}
</script>

<style scoped>
</style>
