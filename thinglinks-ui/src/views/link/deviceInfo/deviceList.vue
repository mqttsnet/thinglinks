<template>
    <div class="wrap">
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
            <el-button type="primary" @click="drawer = !drawer; confirm()">确 定</el-button>
            <el-button @click="drawer = !drawer; cancelD()">取 消</el-button>
        </el-row>
    </div>
</template>

<script>
import { listDevice, } from "@/api/link/device/device";
export default {
    props: {
        search: {
            type: Boolean,
            default: false
        },
    },
    data() {
        return {
            // 设备档案表格数据
            search: false,
            searchValue: "",
            oldSearchValue: '',
            // did: '',
            oldDid: '',
            edgeDevicesIdentification: '',
            oldEdgeDevicesIdentification: "",
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
            dynamicTags: [],
            form: {}
        }
    },
    created() {
        this.getDeviceList();
    },
    watch: {
    },
    methods: {
        confirm() {
            if (this.search) {
                // this.searchValue = this.oldSearchValue
                // this.queryParams.did = this.oldDid
                // this.dynamicTags = [this.searchValue]
            } else {
                this.form.did = this.oldDid
                this.edgeDevicesIdentification = this.oldEdgeDevicesIdentification
                this.dynamicTags = [this.edgeDevicesIdentification]
            }
        },
        cancelD() {
            if (this.search) {
                // this.queryParams.did = this.oldDid
                // this.dynamicTags = [this.searchValue]
            } else {
                this.form.did = this.oldDid
                this.dynamicTags = [this.edgeDevicesIdentification]
            }
        },
        deviceListItem(row) {
            if (this.search) {
                // this.oldDid = row.id
                // this.oldSearchValue = row.deviceIdentification
            } else {
                this.oldDid = row.id
                this.oldEdgeDevicesIdentification = row.deviceIdentification
                // this.$emit("AddOrSet", row)
            }
        },
        /** 查询设备档案列表 */
        getDeviceList() {
            this.loading = true;
            listDevice(this.queryParamsD).then((response) => {
                this.deviceList = response.data.device.rows;
                this.deviceTotal = response.data.device.total;
                this.loading = false;
            });
        },
    }
}
</script>

<style scoped lang="scss">
</style>