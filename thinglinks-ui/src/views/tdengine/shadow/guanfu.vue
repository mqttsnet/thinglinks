<template>
  <div>
    <el-form :model="qryData" ref="queryForm" :inline="true">
      <el-form-item label="设备" required>
        <el-select
          v-model="qryData.clientId"
          size="small"
          placeholder="请选择设备"
          clearable
          filterable
          style="width: 220px"
        >
          <el-option
            v-for="item in prosstionoptions"
            :key="item.clientId"
            :label="item.deviceName"
            :value="item.clientId"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间:" required>
        <el-date-picker
          v-model="qryData.startTime"
          size="small"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          placeholder="开始日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="结束时间:" required>
        <el-date-picker
          v-model="qryData.endTime"
          size="small"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table border highlight-current-row :data="tableData">
      <el-table-column align="center" type="index" label="序号" width="60" show-overflow-tooltip></el-table-column>
      <el-table-column align="center" prop="topicName" label="topic名称" show-overflow-tooltip></el-table-column>
      <el-table-column align="center" prop="msgSize" label="数据大小" show-overflow-tooltip></el-table-column>
      <el-table-column align="center" prop="eventTime" label="数据上报时间" show-overflow-tooltip></el-table-column>
      <el-table-column align="center" label="操作" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-search"
            @click="gotoDetailGuan(scope.row)"
          >查看</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="width:100%; height: 480px; left:2%;margin-top:20px" ref="ycechart"></div>
  </div>
</template>
<script>
import { proOptions, dataList, dataCharts } from "@/api/tdengine/shadow";
export default {
  props: {
    clientId: {
      type: String
    },
    startTime: {
      type: String
    },
    endTime: {
      type: String
    }
  },
  data() {
    return {
      qryData: {
        clientId: "D2253IpgJo",
        startTime: this.$moment()
          .subtract(1, "days")
          .format("YYYY-MM-DD HH:mm:ss"),
        endTime: this.$moment().format("YYYY-MM-DD HH:mm:ss")
      },
      prosstionoptions: [],
      ycmyChart: null,
      tableData: []
    };
  },
  created() {
    console.log(this.clientId, "namenamename");
    console.log(this.startTime, "namenamename");
    console.log(this.endTime, "namenamename");
    if (this.clientId) {
      this.qryData.clientId = this.clientId;
      this.qryData.startTime = this.startTime;
      this.qryData.endTime = this.endTime;
    }
    console.log(this.qryData.clientId, "namenamename");
    console.log(this.qryData.startTime, "namenamename");
    console.log(this.qryData.endTime, "namenamename");
    this.deviceOptions();
  },
  methods: {
    resizeChart() {
      this.ycmyChart ? this.ycmyChart.resize() : "";
      this.pdmyChart ? this.pdmyChart.resize() : "";
    },
    deviceOptions() {
      proOptions({ type: 11 }).then(res => {
        if (res.code == 200) {
          this.prosstionoptions = res.data;
          this.qryData.clientId = "D2253IpgJo";
        }
      });
      this.getList();
      this.drawLine();
    },
    getList() {
      let params = {
        clientId: this.qryData.clientId,
        startTime:
          this.qryData.startTime,
        endTime:
          this.qryData.endTime
      };
      dataList(params).then(res => {
        if (res.code == 200) {
          this.tableData = res.data;
        }
      });
    },
    drawLine() {
      let params = { clientId: this.qryData.clientId || "" };
      dataCharts(params).then(res => {
        if (res.code == 200) {
          this.ycmyChart = this.$echarts.init(this.$refs.ycechart);
          this.ycmyChart.setOption({
            xAxis: {
              type: "category",
              data: res.data.dateList,
              axisLabel: {
                interval: 0,
                rotate: 0
                // formatter: function(value) {
                //   return value.length > 6 ? value.slice(0, 3) + "..." : value;
                // }
              },
              triggerEvent: true
            },
            yAxis: [
              {
                type: "value"
              }
            ],
            series: [
              {
                data: res.data.countList,
                type: "line",
                smooth: true,
                areaStyle: {
                  normal: {
                    color: {
                      x: 0,
                      y: 0,
                      x2: 0,
                      y2: 1,
                      colorStops: [
                        {
                          offset: 0,
                          color: "rgb(230, 245, 243)"
                        },
                        {
                          offset: 0.9,
                          color: "rgb(176, 238, 235)"
                        }
                      ],
                      globalCoord: false
                    }
                  }
                },
                itemStyle: {
                  normal: {
                    lineStyle: {
                      color: "#4AB7BD"
                    },
                    color: "#56B4B0"
                  }
                }
                // markArea: {
                //   itemStyle: {
                //     color: "rgb(230, 245, 243)"
                //   },
                //   data: [
                //     [
                //       {
                //         xAxis: res.data.pdEcharts.xData[0]
                //       },
                //       {
                //         xAxis:
                //           res.data.pdEcharts.xData[
                //             res.data.pdEcharts.xData.length - 1
                //           ]
                //       }
                //     ]
                //   ]
                // }
              }
            ],
            legend: {
              data: [],
              orient: "vertical",
              right: "0%",
              top: "35%",
              itemGap: 20,
              padding: [0, 0, 0, 100]
            },
            title: {
              text: "单位：条"
            },
            tooltip: {
              trigger: "axis"
            },

            grid: {
              left: "1%",
              right: "7%",
              bottom: "3%",
              containLabel: true
            }
          });
        }
      });
    },
    handleSearch() {
      let start = this.qryData.startTime;
      let end = this.qryData.endTime;

      if (
        this.qryData.clientId &&
        this.qryData.startTime &&
        this.qryData.endTime
      ) {
        if (start < end) {
          this.getList();
          this.drawLine();
        } else {
          this.$message.error("开始时间要小于结束时间！");
        }
      } else {
        this.$message.error("有参数未选择！");
      }
    }, //查询
    resetQuery() {
      this.qryData.clientId = "";
      this.qryData.startTime = "";
      this.qryData.endTime = "";
    },
    gotoDetailGuan(row) {
      this.$router.push({
        path: "/shadow/StatisticalMonitor/Terminal/terminaldetail",
        query: {
          activeName: "1",
          row: row,
          clientId: this.qryData.clientId,
          startTime: this.qryData.startTime,
          endTime: this.qryData.endTime
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
.tdZB1:hover {
  color: #56b4b0;
}
</style>
<style scoped>
.el-form-item {
  margin-bottom: 0px !important;
}
</style>
