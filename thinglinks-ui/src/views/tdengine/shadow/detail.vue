<template>
  <div :class="$style['app-content']">
    <div>
      <el-button type="text">
        <i class="el-icon-d-arrow-left" style="font-size: 20px" @click="goBack"></i>
      </el-button>
      <span style="font-size: 20px; font-weight: bold">
        {{
        topicName
        }}
      </span>
      &nbsp;&nbsp;&nbsp;
      <!-- <el-tag v-if="baseInfo.onlineStatus == 0" type="success" :hit="false" size="small">在线</el-tag>
      <el-tag v-if="baseInfo.onlineStatus == 1" type="danger" :hit="false" size="small">离线</el-tag>
      <el-tag v-if="baseInfo.onlineStatus == 2" type="warning" size="small" :hit="false">未连接</el-tag>-->
    </div>

    <!-- <el-table border highlight-current-row :data="tableData">
      <el-table-column align="center" prop="dataKey" label="指标" show-overflow-tooltip></el-table-column>
      <el-table-column align="center" prop="dataValue" label="指标值" show-overflow-tooltip></el-table-column>
    </el-table>-->
    <table :class="$style['device-table']" border="1">
      <tr>
        <th>指标</th>
        <th>指标值</th>
        <th>指标</th>
        <th>指标值</th>
      </tr>
      <tr v-for="(item,index) in tableNewData" :key="index">
        <template v-for="items in item">
          <td>{{items.dataKey}}</td>
          <td>{{items.dataValue}}</td>
        </template>
      </tr>
    </table>
  </div>
</template>
<script>
import { dataList } from "@/api/tdengine/shadow";
export default {
  data() {
    return {
      tableData: [],
      tableNewData: [],
      topicName: ""
    };
  },
  mounted() {
    this.getList();
    this.topicName = this.$route.query.row.topicName;
  },
  beforeDestroy() {
    //清理工作 避免内存泄漏
    //销毁监听事件
  },
  methods: {
    getList() {
      let params = {
        clientId: this.$route.query.clientId,
        eventTime: this.$route.query.row.eventTime
      };
      dataList(params).then(res => {
        if (res.code == 200) {
          this.tableData = res.data;
          this.getData();
        }
      });
    },
    getData() {
      const arr = [];
      let minarr = [];
      this.tableData.forEach(cate => {
        if (minarr.length === 2) {
          minarr = [];
        }
        if (minarr.length == 0) {
          arr.push(minarr);
        }
        minarr.push(cate);
      });
      this.tableNewData = arr;
    },
    goBack() {
      this.$router.push({
        path: "/shadow/Terminal",
        query: {
          activeNameBack: this.$route.query.activeName,
          clientIdBack: this.$route.query.clientId,
          startTime: this.$route.query.startTime,
          endTime: this.$route.query.endTime
        }
      });
    }
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
.greencircle {
  display: inline-block;
  vertical-align: middle;
  width: 24px;
  height: 24px;
  background: url("/assets/images/common/green_point.png") no-repeat
    50% 50%;
  background-size: 100%;
  margin-left: 8px;
}
.redcircle {
  display: inline-block;
  vertical-align: middle;
  width: 24px;
  height: 24px;
  background: url("/assets/images/common/red_point.png") no-repeat 50%
    50%;
  background-size: 100%;
  margin-left: 8px;
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
