<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :sm="24" :lg="12" style="padding-left: 20px">
        <h2>ThingLinks背景信息</h2>
        <h3>拓扑信息</h3>
        <p>
          设备归属于某个产品下的具体设备，例如电压表、环境监测设备等。
        </p>
        <p>
          设备可以直连LINK（这里称为普通设备），也可以作为子设备通过网关连接（这里称为网关子设备）。
        </p>
        <br>
        <h5>
          <p>
            <img src="@/assets/images/index/device-topology.png" alt="device-topology">
          </p>
        </h5>
        <p>
          <b>当前版本:</b> <span>v{{ version }}</span>
        </p>
        <p>
          <el-tag type="danger">&yen;免费开源</el-tag>
        </p>
        <p>
          <el-button
            type="primary"
            size="mini"
            icon="el-icon-cloudy"
            plain
            @click="goTarget('https://github.com/mqttsnet/thinglinks.git')"
          >访问GitHub
          </el-button
          >
          <el-button
            size="mini"
            icon="el-icon-s-home"
            plain
            @click="goTarget('http://thinglinks.mqttsnet.com')"
          >访问主页
          </el-button
          >
        </p>
      </el-col>

      <el-col :sm="24" :lg="12" style="padding-left: 20px">
        <h3>使用指引</h3>
        <p>
          使用指引帮助您快速了解ThingLinks平台的使用，包括产品、设备的创建、数据转发和命令下发等操作。
        </p>
        <br>
      </el-col>
    </el-row>
    <el-divider/>
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>联系信息</span>
          </div>
          <div class="body">
            <p>
              <i class="el-icon-s-promotion"></i> 官网：
              <el-link
                href="http://thinglinks.mqttsnet.com"
                target="_blank">https://www.mqttsnet.com
              </el-link>
            </p>
            <p>
              <i class="el-icon-chat-dot-round"></i> MQTTSNET团队邮箱：<a
              href="javascript:;"
            >/ *mqttsnet@163.com</a>
            </p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>更新日志</span>
          </div>
          <el-collapse accordion>
            <el-collapse-item title="V1.2.0 - - 2024-05-01">
              <ol>
                <li>产品模型初始化功能优化</li>
                <li>新增规则引擎-链式规则</li>
                <li>新增OTA远程升级功能</li>
                <li>设备接入支持协议加密</li>
                <li>支持MQTT5.0协议</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="V1.1.0 - - 2023-01-02">
              <ol>
                <li>设备管理页面列表布局调整</li>
                <li>支持Td时序数据库保存在线离线记录</li>
                <li>新增产品模型管理功能</li>
                <li>优化设备离线动作记录</li>
                <li>优化设备离线通知</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="V1.0.0 - 2021-10-01">
              <ol>
                <li>ThingLinks微服务系统正式发布</li>
                <li>设备管理页面功能开发</li>
                <li>支持设备注册定位功能（高德地图）</li>
                <li>新增MQTT鉴权功能</li>
                <li>MQTT集群支持</li>
                <li>优化设备注册功能</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "Index",
  data() {
    return {
      // 版本号
      version: "1.2.0",
    };
  },
  methods: {
    goTarget(href) {
      window.open(href, "_blank");
    },
  },
};
</script>

<style scoped lang="scss">
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }

  hr {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 0;
    border-top: 1px solid #eee;
  }

  .col-item {
    margin-bottom: 20px;
  }

  ul {
    padding: 0;
    margin: 0;
  }

  font-family: "open sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 13px;
  color: #676a6c;
  overflow-x: hidden;

  ul {
    list-style-type: none;
  }

  h4 {
    margin-top: 0px;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }

  .update-log {
    ol {
      display: block;
      list-style-type: decimal;
      margin-block-start: 1em;
      margin-block-end: 1em;
      margin-inline-start: 0;
      margin-inline-end: 0;
      padding-inline-start: 40px;
    }
  }
}
</style>

