<template>
    <div class="app-container">
        <div class="equipment_status">
            <div class="zhengshu">
                <p>
                    <span>设备名称</span>
                    <span>{{ deviceInfo.deviceName }}</span>
                </p>
                <p>
                    <span>设备状态</span>
                    <span style="display:flex;align-items: center;" v-if="deviceInfo.connectStatus == '离线'"><i
                            style="display:block;width:10px;height:10px;background: #ff9292;border-radius: 50%;"></i>
                        {{ deviceInfo.connectStatus }}
                    </span>
                    <span style="display:flex;align-items: center;" v-if="deviceInfo.connectStatus == '在线'"><i
                            style="display:block;width:10px;height:10px;background: #71e2a3;border-radius: 50%;"></i>
                        {{ deviceInfo.connectStatus }}
                    </span>
                    <span style="display:flex;align-items: center;" v-if="deviceInfo.connectStatus == '未连接'"><i
                            style="display:block;width:10px;height:10px;background: #ffba00;border-radius: 50%;"></i>
                        {{ deviceInfo.connectStatus }}
                    </span>
                </p>
            </div>
            <div class="zhengshu">
                <p>
                    <span>产品名称</span>
                    <span>{{ deviceInfo.password }}</span>
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
            <div class="zhengshu">
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
        <div class="detail" style="height:400px;">
            <el-tabs v-model="activeName">
                <el-tab-pane label="基本信息" name="first">
                    <div class="zhengshu">
                        <p>
                            <span>设备标识</span>
                            <span>
                                {{ deviceInfo.deviceIdentification }}
                            </span>
                        </p>
                        <p>
                            <span>创建者</span>
                            <span>
                                {{ deviceInfo.createBy }}
                            </span>
                        </p>
                        <p>
                            <span>更新者</span>
                            <span>
                                {{ deviceInfo.updateBy }}
                            </span>
                        </p>
                        <p>
                            <span>认证方式</span>
                            <span>
                                {{ deviceInfo.authMode }}
                            </span>
                        </p>
                        <p>
                            <span>产品协议类型</span>
                            <span>
                                {{ deviceInfo.protocolType }}
                            </span>
                        </p>
                    </div>
                    <div class="zhengshu">
                        <p>
                            <span>设备类型</span>
                            <span>
                                <i style="cursor: pointer;" title="复制" class="el-icon-copy-document"
                                    @click="copy(deviceInfo.userName)"></i>
                                {{ deviceInfo.userName }}
                            </span>
                        </p>
                        <p>
                            <span>创建时间</span>
                            <span>
                                {{ deviceInfo.createTime }}
                            </span>
                        </p>
                        <p>
                            <span>更新时间</span>
                            <span>
                                {{ deviceInfo.updateTime }}
                            </span>
                        </p>
                        <p>
                            <span>是否遗言</span>
                            <span v-text="deviceInfo.isWill == null ? '否' : deviceInfo.isWill">
                            </span>
                        </p>
                        <p>
                            <span>产品标识</span>
                            <span>
                                {{ deviceInfo.productIdentification }}
                            </span>
                        </p>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="Topic列表" name="second">
                    <el-tabs v-model="TopicactiveName">
                        <el-tab-pane label="基础Topic" name="first">
                            <el-table :data="TopicList" style="width: 100%">
                                <el-table-column prop="Topic" label="Topic" width="280">
                                </el-table-column>
                                <el-table-column prop="Publisher" label="Publisher(发布者)" width="180">
                                </el-table-column>
                                <el-table-column prop="Subscriber" label="Subscriber(订阅者)" width="180">
                                </el-table-column>
                                <el-table-column prop="address" label="用途" width="180">
                                </el-table-column>
                            </el-table>
                        </el-tab-pane>
                        <el-tab-pane label="自定义Topic" name="second">自定义Topic</el-tab-pane>
                    </el-tabs>
                </el-tab-pane>
                <el-tab-pane label="设备影子" name="third">
                    <el-tabs v-model="TopicactiveName">
                        <el-tab-pane label="列表" name="first">
                            <el-table :data="TopicList" style="width: 100%">
                                <el-table-column prop="Topic" label="Topic" width="280">
                                </el-table-column>
                                <el-table-column prop="Publisher" label="Publisher(发布者)" width="180">
                                </el-table-column>
                                <el-table-column prop="Subscriber" label="Subscriber(订阅者)" width="180">
                                </el-table-column>
                                <el-table-column prop="address" label="用途" width="180">
                                </el-table-column>
                            </el-table>
                        </el-tab-pane>
                        <el-tab-pane label="JSON" name="second">
                            <el-input class="textJson" type="textarea" :autosize="{ minRows: 6 }" resize="none"
                                :value="detailJSON" placeholder="无内容"></el-input>
                            <el-button size="medium" style="margin-top: 10px" type="primary" @click="decoration">
                                格式化
                            </el-button>
                        </el-tab-pane>
                    </el-tabs>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>
<script>
import {
    getDevice,
} from "@/api/link/device";
export default {
    data() {
        return {
            //table切换
            activeName: 'first',
            TopicactiveName: "first",
            //设备详细
            deviceInfo: {},
            //密码切换
            show: true,
            //Topic列表默认数据
            TopicList: [{
                Topic: '/v1/devices/{deviceId}/topo/add',
                Publisher: '边设备',
                Subscriber: "物联网平台",
                address: '边设备添加子设备'
            }],
            //json数据转换
            detailJSON: ""
        }
    },
    methods: {
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
                console.log(response);
                this.deviceInfo = response.data
                this.detailJSON = JSON.stringify(response.data)
                console.log(data);
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
        this.getDetail()
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

.zhengshu {
    width: 30%;

    p {
        width: 100%;
        display: flex;
        align-items: center;

        span:first-child {
            display: block;
            width: 20%;
        }
    }
}

.detail {
    width: 100%;
    margin: 0 0 10px 10px;
    padding: 20px 30px;
    background: #F8F8F9;
    font-size: 14px;
    font-weight: 700;
    color: #515a6e;

    .el-tab-pane {
        width: 80%;
        height: 300px;
        display: flex;
        justify-content: space-between;

        .zhengshu {
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
</style>