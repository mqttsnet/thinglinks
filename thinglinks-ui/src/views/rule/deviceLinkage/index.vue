<template>
  <div class="wrap">
    <el-row>
      <el-button type="primary" plain size="small" @click="addRule">规则创建</el-button>
    </el-row>
    <div class="defalut">
      <div>
        <i class="el-icon-warning"></i>
        <span>暂无数据</span>
      </div>
    </div>
  </div>
</template>

<script>
// import {  } from "@/api/rule/deviceLinkage";
export default {
  name: "index.vue",
  methods: {
    addRule() {
      this.$router.push({
        name: "addRule"
      });
    }
  }
}
</script>

<style scoped lang="scss">
.wrap {
  width: 100%;
  height: 100%;
  padding: 15px 30px;
  box-sizing: border-box;

  .defalut {
    width: 100%;
    height: 500px;
    margin: 15px 0 0 0;
    display: flex;
    justify-content: center;
    align-items: center;
    border: 1px solid #EEF0F5;
    background: #EEF0F5;
    color: rgb(110, 104, 104);

    div {
      height: 20%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: space-around;

      i {
        font-size: 40px;
      }
    }
  }
}
</style>