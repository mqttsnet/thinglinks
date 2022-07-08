<template>
  <div class="page">
    <div class="box">
      <div
        ref="logContainer"
        style="
          height: 500px;
          overflow-y: scroll;
          background: #000000;
          color: #aaa;
          padding: 10px;
          margin-top: 10px;
        "
      >
        <div ref="logContainerDiv" v-html="res" id="logContent" style="width:96%"></div>
      </div>
      <div class="headerbox">
        <el-button
          size="medium"
          type="primary"
          style="margin-top: 10px"
          @click="clear"
        >
          清屏
        </el-button>
        <el-button
          size="medium"
          style="margin-left: 20px; margin-top: 10px"
          @click="checkSocket()"
          type="danger"
          >{{ webSocket ? "关闭日志" : "开启日志" }}</el-button
        >
        <!-- <el-button size="medium" @click="startSocket()" type="success">开启日志</el-button> -->
      </div>
    </div>
  </div>
</template>

<script>
// 引入
import  API_CONFIG from "../../../../vue.config"
export default {
  name: "RealTime",
  data() {
    return {
      webSocket: null,
      res: "",
    };
  },
  mounted() {
    this.openSocket();
    // 使用
  },
  destroyed(){
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
      if(typeof(WebSocket) === "undefined") {
        this.$toast.fail("您的浏览器不支持WebSocket");
      }else{
        let SocketUrl = `ws://${API_CONFIG.devServer.proxy[process.env.VUE_APP_BASE_SOCKETIP].target}${process.env.VUE_APP_BASE_SOCKETIP}/broker/websocket/logging`;
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
    webSocketOnOpen(){
    },
    // 连接建立失败重连
    webSocketOnError(){
      this.res = "<div style='color: #18d035;font-size: 14px'>通道连接失败,尝试重新连接,静默等待....</div>";
      this.openSocket();
    },
    // 数据接收
    webSocketOnMessage: function (event) {
      let link = 'socket连接成功';
      let userName = "【客户】" + this.$store.state.user.name;
      if (event.data.indexOf(link) !== -1||event.data.indexOf(userName) !== -1) {
        const res = event.data.split('#####');
        let color = "#fff";
        switch (res[0]) {
          case "INFO":
            color='#18d035';
            break;
          case "DEBUG":
            color='rgb(97, 193, 231)';
            break;
          case "WARN":
            color='yellow';
            break;
          case "ERROR":
            color='red';
            break;
        }
        this.res += "<div style='color: "+color+";font-size: 14px'>"+res[1]+"</div>";
      }
      this.$refs.logContainerDiv.scrollIntoView({block: "end"});
      //   let content  = JSON.parse(event.data);
      //   let leverhtml = "";
      //   let className =
      //     "<span style='color: #229379'>" + content.className + "</span>";
      //   switch (content.level) {
      //     case "INFO":
      //       leverhtml =
      //         "<span style='color: #18d035'>" + content.level + "</span>";
      //       break;
      //     case "DEBUG":
      //       leverhtml =
      //         "<span style='color: rgb(97, 193, 231)'>" +
      //         content.level +
      //         "</span>";
      //       break;
      //     case "WARN":
      //       leverhtml =
      //         "<span style='color: yellow'>" + content.level + "</span>";
      //       break;
      //     case "ERROR":
      //       leverhtml =
      //         "<span style='color: red'>" + content.level + "</span>";
      //       break;
      //   }
      //   this.res +=
      //     "<div style='color: #18d035;font-size: 14px'>" +
      //     content.timestamp +
      //     " " +
      //     leverhtml +
      //     " --- [" +
      //     content.threadName +
      //     "] " +
      //     className +
      //     " ：" +
      //     content.body +
      //     "</div>";
      //   if (content.exception !== "") {
      //     this.res += "<div>" + content.exception + "</div>";
      //   }
      //   if (content.cause !== "") {
      //     this.res += "<div>" + content.cause + "</div>";
      //   }
      // }catch (e){
      //
      // }
    },
    // 数据发送
    webSocketSend(Data){
      this.webSocket.send(Data);
    },
    //关闭
    webSocketClose(e){
        this.webSocket.close();
    },
  }
};
</script>

<style lang="scss" scoped>
.headerbox {
  display: flex;
}
.boxtwo {
  display: flex;
}
::v-deep .el-tabs--border-card > .el-tabs__header .el-tabs__item.is-active {
  color: #fff;
  background-color: rgb(0, 150, 136);
  border-right-color: rgb(0, 150, 136);
  border-left-color: rgb(0, 150, 136);
}
::v-deep
  .el-tabs--border-card
  > .el-tabs__header
  .el-tabs__item:not(.is-disabled):hover {
  color: #909399;
}
::v-deep
  .el-tabs--border-card
  > .el-tabs__header
  .el-tabs__item.is-active:hover {
  color: #fff;
}
</style>
