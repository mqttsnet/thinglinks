<template>
  <div class="wrap">
    <div v-if="this.did">
      <el-tag :key="tag" v-for="tag in dynamicTags" closable :disable-transitions="false" @close="handleClose(tag)">
        {{ tag }}
      </el-tag>
    </div>
    <el-button v-if="!this.did" type="primary" icon="el-icon-plus" size="mini" plain @click="search = true;drawer=true">
      边设备信息
    </el-button>
    <el-drawer :modal='false' title="边设备信息" :visible.sync="drawer" @close="closeDrawer" show-close>
      <el-row>
        <el-table ref="myTable" highlight-current-row v-loading="loading" :data="deviceList"
          @row-click="deviceListItem">
          <el-table-column align="center" label="id" prop="id" />
          <el-table-column align="center" label="设备标识" prop="deviceIdentification" />
          <el-table-column align="center" label="客户端标识" prop="clientId" />
        </el-table>
        <pagination v-show="deviceTotal > 0" :limit.sync="queryParamsD.pageSize" :page.sync="queryParamsD.pageNum"
          :total="deviceTotal" @pagination="getDeviceList" />
      </el-row>
      <el-row style="padding:20px 30px">
        <el-button type="primary" @click="confirm()">确 定</el-button>
        <el-button @click="cancelD()">取 消</el-button>
      </el-row>
    </el-drawer>
  </div>
</template>

<script>
  import {
    listDevice,
  } from "@/api/link/device/device";
  export default {
    props: {
      flag: {
        type: Number,
        defalut: 1
      },
      set_did: {
        type: Number,
        default: 0
      },
      set_edgeDevicesIdentification: {
        type: String,
        default: ""
      },
    },
    data() {
      return {
        loading:false,
        falg: 1,
        // 设备档案表格数据
        did: "",
        dynamicTags: [],
        clickList: {},
        OldClickList: {},
        drawer: false,
        // 边设备查询参数
        queryParamsD: {
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
        deviceList: [],
        deviceTotal: 0,
        form: {}
      }
    },
    mounted() {
      this.getDeviceList();
      this.did = this.set_did
      this.deviceIdentification = this.set_edgeDevicesIdentification
      this.dynamicTags = [this.deviceIdentification]
    },
    watch: {},
    methods: {
      confirm() {
        this.falg = 1
        this.drawer = false
      },
      cancelD() {
        this.falg = 2
        this.drawer = false
      },
      deviceListItem(row) {
        console.log(row);
        this.clickList = {
          id: row.id,
          deviceIdentification: row.deviceIdentification
        }
      },
      //el-drawer关闭回调
      closeDrawer() {
        if (this.falg === 1) {
          this.did = this.clickList.id
          this.dynamicTags = [this.clickList.deviceIdentification]
          this.OldClickList = {
            id: this.clickList.id,
            deviceIdentification: this.clickList.deviceIdentification
          }
        } else if (this.falg === 2) {
          this.did = this.OldClickList.id
          this.dynamicTags = [this.OldClickList.deviceIdentification]
        }
        if (this.flag === 1) {
          this.$emit('searchDecive', this.did)
        } else if (this.flag === 2) {
          this.$emit('addDecive', this.did)
        } else if (this.flag === 3) {
          this.$emit('setDecive', this.did)
        }
      },
      // el-tag关闭回调
      handleClose(tag) {
        this.did = ''
        this.dynamicTags = []
      },
      /** 查询设备档案列表 */
      getDeviceList() {
        this.loading = true;
        listDevice(this.queryParamsD).then((response) => {
          this.deviceList = response.rows;
          this.deviceTotal = response.total;
          this.loading = false;
        });
      },
    }
  }
</script>

<style scoped lang="scss">
</style>
