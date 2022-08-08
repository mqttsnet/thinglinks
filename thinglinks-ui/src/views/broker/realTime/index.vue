<template>
  <div class="page">
    <div class="box">
      <div ref="logContainer" style="
          height: 500px;
          overflow-y: scroll;
          background: #000000;
          color: #aaa;
          padding: 10px;
          margin-top: 80px;
        ">
        <div ref="logContainerDiv" v-html="res" id="logContent" style="width:96%"></div>
      </div>
      <div class="headerbox">
        <el-button size="medium" type="primary" style="margin-top: 10px" @click="clear">
          清屏
        </el-button>
        <el-button size="medium" style="margin-left: 20px; margin-top: 10px" @click="checkSocket()" type="danger">{{
            webSocket ? "关闭日志" : "开启日志"
        }}</el-button>
        <!-- <el-button size="medium" @click="startSocket()" type="success">开启日志</el-button> -->
      </div>
    </div>
  </div>
</template>

<script>
// 引入
import API_CONFIG from "../../../../vue.config"
export default {
  name: "RealTime",
  data() {
    return {
      webSocket: null,
      res: "",
      flag: 1
    };
  },
  mounted() {
    this.openSocket();
    // 使用
  },
  destroyed() {
    this.webSocketClose();
  },
  methods: {
    // 清屏
    clear() {
      this.res =
        "<div style='color: #18d035;font-size: 14px'>通道连接成功,静默等待....</div>";
    },
    // 业务开启连接
    openSocket() {
      if (typeof (WebSocket) === "undefined") {
        this.$toast.fail("您的浏览器不支持WebSocket");
      } else {
        let SocketUrl = `${API_CONFIG.devServer.proxy[process.env.VUE_APP_BASE_SOCKETIP].target}/broker/websocket/logging`;
        // 实例化WebSocket
        this.webSocket = new WebSocket(SocketUrl)
        // 监听socket连接
        this.webSocket.onopen = this.webSocketOnOpen
        // 监听socket错误信息
        this.webSocket.onerror = this.webSocketOnError
        // 监听socket消息
        this.webSocket.onmessage = this.webSocketOnMessage
        // this.webSocket.onclose = this.webSocketClose
      }
    },
    //关闭日志
    checkSocket() {
      if (this.webSocket != null) {
        this.webSocketClose();
        this.res = "";
        this.webSocket = null;
      } else {
        this.openSocket();
      }
    },
    // 开启日志
    startSocket() {
      this.openSocket();
    },
    // 连接建立之后执行send方法发送数据
    webSocketOnOpen() {
    },
    // 连接建立失败重连
    webSocketOnError() {
      this.res = "<div style='color: #18d035;font-size: 14px'>通道连接失败,尝试重新连接,静默等待....</div>";
      this.openSocket();
    },
    // 数据接收
    webSocketOnMessage: function (event) {
      // console.log(event.data);
      if (event.data !== '') {
        const res = event.data.split('<br/>');
        let color = "#fff";
        res.forEach(item => {
          if (item.indexOf("INFO") !== -1) {
            color = '#18d035';
          } else if (item.indexOf("DEBUG") !== -1) {
            color = 'rgb(97, 193, 231)'; s
          } else if (item.indexOf("WARN") !== -1) {
            color = 'yellow';
          } else if (item.indexOf("ERROR") !== -1) {
            color = 'red';
          }
          this.res += "<div style='color: " + color + ";font-size: 14px'>" + item + "</div>";
        })
      }
      if (this.flag <= 2) {
        this.$refs.logContainer.scrollTop = this.$refs.logContainerDiv.scrollHeight;
        this.flag++
      }
    },
    // 数据发送
    webSocketSend(Data) {
      this.webSocket.send(Data);
    },
    //关闭
    webSocketClose(e) {
      this.webSocket.close();
    },
  }
};
</script>

<style lang="scss" scoped>
.headerbox {
  display: flex;
  justify-content: space-around;
  width: 200px;
  margin: auto;
}

.boxtwo {
  display: flex;
}

::v-deep .el-tabs--border-card>.el-tabs__header .el-tabs__item.is-active {
  color: #fff;
  background-color: rgb(0, 150, 136);
  border-right-color: rgb(0, 150, 136);
  border-left-color: rgb(0, 150, 136);
}

::v-deep .el-tabs--border-card>.el-tabs__header .el-tabs__item:not(.is-disabled):hover {
  color: #909399;
}

::v-deep .el-tabs--border-card>.el-tabs__header .el-tabs__item.is-active:hover {
  color: #fff;
}
</style>
