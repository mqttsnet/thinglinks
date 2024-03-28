<template>
    <div class="app-container">
        <div class="equipment_status">
            <div class="equipment_attribute">
                <p>
                    <span>设备名称</span>
                    <span>
                        {{ deviceInfo.nodeName }}
                    </span>
                </p>
                <p>
                    <span>设备型号</span>
                    <span>
                        {{ deviceInfo.model }}
                    </span>
                </p>
            </div>
            <div class="equipment_attribute">
                <p>
                    <span>应用ID</span>
                    <span>{{ deviceInfo.appId }}</span>
                </p>
                <p>
                    <span>设备标识</span>
                    <span>
                        {{ deviceInfo.nodeId }}
                    </span>
                </p>
            </div>
            <div class="equipment_attribute">
                <p>
                    <span>厂商ID</span>
                    <span>
                        {{ deviceInfo.nodeName }}
                    </span>
                </p>
                <p>
                    <span>连接状态</span>
                    <dict-tag :options="dict.type.link_device_connect_status" :value="deviceInfo.connectStatus" />
                </p>
            </div>
        </div>
        <div class="detail">
            <el-tabs v-model="activeName">
                <el-tab-pane label="基本信息" name="first">
                    <div class="equipment_attribute">
                        <p>
                            <span>状态</span>
                            <dict-tag :options="dict.type.business_data_status" :value="deviceInfo.status" />
                        </p>
                        <p>
                            <span>是否支持设备影子</span>
                            <dict-tag :options="dict.type.link_deviceInfo_shadow_enable"
                                :value="deviceInfo.shadowEnable" />
                        </p>
                        <p>
                            <span>边设备标识</span>
                            <span>
                                {{ deviceInfo.edgeDevicesIdentification }}
                            </span>
                        </p>
                        <p>
                            <span>子设备标识</span>
                            <span>
                                {{ deviceInfo.deviceId }}
                            </span>
                        </p>
                    </div>
                    <div class="equipment_attribute">
                        <p>
                            <span>创建者</span>
                            <span>
                                {{ deviceInfo.createBy }}
                            </span>
                        </p>
                        <p>
                            <span>创建时间</span>
                            <span>
                                {{ deviceInfo.createTime }}
                            </span>
                        </p>
                        <p>
                            <span>更新者</span>
                            <span>
                                {{ deviceInfo.updateBy }}
                            </span>
                        </p>
                        <p>
                            <span>更新时间</span>
                            <span>
                                {{ deviceInfo.updateTime }}
                            </span>
                        </p>
                        <p>
                            <span>设备描述</span>
                            <span>
                                {{ deviceInfo.description }}
                            </span>
                        </p>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="设备影子" name="second" style="width:100%;height: 100%;">
                    <el-tabs v-model="shadowActiveName" style="width:100%;height: 100%;">
                        <el-tab-pane label="列表" name="first" style="width:100%;height:100%;">
                            <el-date-picker @change="timeControls" style="margin-bottom: 10px;" v-model="value1"
                                type="datetimerange" value-format="yyyy-MM-dd HH:mm:ss" range-separator="至"
                                start-placeholder="开始日期" end-placeholder="结束日期">
                            </el-date-picker>
                            <el-button style="position: absolute;right:20px" icon="el-icon-refresh"
                                @click="getShadowData" circle></el-button>
                            <el-tabs v-model="editableTabsValue" type="card">
                                <el-tab-pane v-for="(value, name, index) in ShadowData" :key="index" :label="name"
                                    :name="String(index + 1)" style="width:100%;height: 100%;">
                                    <el-table v-if="JSON.stringify(value) !== '[]'" :data="value" style="width: 100%"
                                        max-height="450" :fit="true">
                                        <el-table-column prop="index" label="序号" style="width: 25%">
                                            <template slot-scope="scope">
                                                {{ scope.$index + 1 }}
                                            </template>
                                        </el-table-column>
                                        <el-table-column v-for="(ShadowValue, ShadowName, index1) in value[0]"
                                            :key="index1" :label="ShadowName" :prop="ShadowName" style="width: 25%">
                                        </el-table-column>
                                    </el-table>
                                </el-tab-pane>
                            </el-tabs>
                        </el-tab-pane>
                        <el-tab-pane label="JSON" name="second" style="width: 100%;height:100%">
                            <el-button size="medium" style="margin: 10px 0 10px 0" type="primary" @click="decoration">
                                格式化
                            </el-button>
                            <el-input class="textJson" type="textarea" style="width:100%" :autosize="{ minRows: 5 }"
                                resize="none" :value="detailJSON" placeholder="无内容">
                            </el-input>
                        </el-tab-pane>
                    </el-tabs>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>
<script>
import {
    getDeviceInfo,
    getDeviceInfoShadow,
} from "@/api/link/deviceInfo";
export default {
    dicts: [
        "business_data_status",
        "link_device_connect_status",
        "link_deviceInfo_shadow_enable",
    ],
    data() {
        return {
            value1: [],
            //分页总条数
            total: 0,
            // 遮罩层
            loading: true,
            //table切换
            activeName: 'first',
            shadowActiveName: "first",
            editableTabsValue: '1',
            //设备详细
            deviceInfo: {
            },
            //影子数据
            ShadowData: {
            },
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
            // 查询子设备影子数据
            data: {
                ids: "",
                startTime: "",
                endTime: "",
            },
        }
    },

    watch: {
        activeName(value) {
            if (value === 'second') {
                this.data.ids = this.deviceInfo.id
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
            getDeviceInfoShadow(this.data).then(res => {
                console.log(res.data);
                this.ShadowData = res.data
                this.detailJSON = JSON.stringify(res.data)
                this.loading = false
            })
        },
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
        //设备详细获取
        getDetail() {
            getDeviceInfo(this.id).then((response) => {
                console.log(response);
                this.deviceInfo = response.data
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
        console.log(this.id);
        this.getDetail();
        // this.data.ids = this.deviceInfo.id
        // this.getShadowData()
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
            width: 35%;
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
</style>
