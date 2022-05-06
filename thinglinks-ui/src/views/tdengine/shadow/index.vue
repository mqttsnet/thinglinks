<template>
  <div :class="$style['app-content']">
    <div>
      <span style=" font-weight: bold">设备影子数据采集</span>
      <el-tabs
        v-model="activeName"
        @tab-click="handleClick"
        type="border-card"
        style="margin:20px 0"
      >
        <el-tab-pane label="网关" name="1">
          <Sun :clientId="clientId" :startTime="startTime" :endTime="endTime"></Sun>
        </el-tab-pane>
        <el-tab-pane label="普通" name="2">
          <Lemp  :clientId="clientIdl" :startTime="startTimel" :endTime="endTimel"></Lemp>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
import Lemp from "./lemp";
import Sun from "./guanfu";
export default {
  components: {
    Lemp,
    Sun
  },
  data() {
    return {
      activeName: "1",
      tabName: {},
      clientId: null,
      endTime: null,
      startTime: null,
      clientIdl: null,
      endTimel: null,
      startTimel: null,
    };
  },
  created() {
    if (
      this.$route.query.activeNameBack &&
      this.$route.query.activeNameBack == "1"
    ) {
      this.activeName = this.$route.query.activeNameBack || "1";
      this.clientId = this.$route.query.clientIdBack;
      this.startTime = this.$route.query.startTime;
      this.endTime = this.$route.query.endTime;
    } else {
      this.activeName = this.$route.query.activeNameBack || "1";
      this.clientIdl = this.$route.query.clientIdBack;
      this.startTimel = this.$route.query.startTime;
      this.endTimel = this.$route.query.endTime;
    }
    console.log(this.clientId, "namenamename");
    console.log(this.startTime, "namenamename");
    console.log(this.endTime, "namenamename");
  },

  methods: {
    //设备接口
    deviceOptions() {
      proOptions({ type: 11 }).then(res => {
        if (res.code == 200) {
          this.prosstionoptions = res.data;
          this.qryData.clientId = "D2253IpgJo";
        }
      });
      proOptions({ type: 12 }).then(res => {
        if (res.code == 200) {
          this.prosstionoptionsLamp = res.data;
        }
      });
      this.getList();
      this.drawLine();
    },
    handleClick(tab) {
      this.tabName = tab;
    } //tab切换
  }
};
</script>
<style lang="scss" module>
.app-content {
  padding: 20px;
}
.device-table {
  table-layout: fixed;
  width: 100%;
  margin-top: 15px;
  margin-bottom: 15px;
  border-collapse: collapse;
  border: 1px solid #cfdbe6;
  //   border: none;
  font-size: 14px;
  td:nth-child(odd) {
    width: 30%;
    overflow: hidden;
    padding: 10px 15px;
    // background: #f8f8f9;
  }
  td:nth-child(even) {
    width: 10%;
    text-overflow: ellipsis;
    white-space: nowrap;
    word-break: keep-all;
    overflow: hidden;
    padding: 10px 15px;
  }
  th {
    height: 40px;
    background: rgb(230, 245, 243);
  }
  th:nth-child(odd) {
    width: 20%;
  }
  th:nth-child(even) {
    width: 10%;
  }
}
.device-table1 {
  table-layout: fixed;
  width: 100%;
  margin-top: 15px;
  margin-bottom: 15px;
  border-collapse: collapse;
  border: 1px solid #dfe6ec;
  //   border: none;
  font-size: 14px;
  // min-height: 300px;
  th {
    height: 40px !important;
    background: rgb(230, 245, 243);
  }
  .tdClass {
    width: 10%;
    overflow: hidden;
    padding: 10px 15px;
    text-align: center;
    // background: #f8f8f9;
  }
  .tdStatus {
    width: 19%;
    overflow: hidden;
    padding: 10px 15px;
    // background: #f8f8f9;
  }
  .tdZB {
    width: 40%;
    // text-overflow: ellipsis;
    // white-space: nowrap;
    // word-break: keep-all;
    overflow: hidden;
    padding: 10px 15px;
  }
  .tdZB1 {
    width: 40%;
    // text-overflow: ellipsis;
    // white-space: nowrap;
    // word-break: keep-all;
    // background: #f8f8f9;
    overflow: hidden;
    padding: 10px 15px;
  }
}
.tdZB1:hover {
  color: #56b4b0;
}
</style>
<style scoped>
.el-form-item {
  margin-bottom: 0px !important;
}
</style>
