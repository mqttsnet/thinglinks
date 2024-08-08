<template>
  <div class="equipment-shadow">
    <div class="shadow">
      <label>服务列表</label>
      <div class="shadow-list">
        <div v-for="(value, index) in shadowData.services" :key="index" class="shadow-item"
          @click="getShadowProperty(value)">
          <span class="shadow-name">{{ value.serviceName }}</span>
          <span class="shadow-info">{{ value.description }}</span>
        </div>
      </div>
    </div>
    <div v-loading="loading" class="shadow-property-list">
      <template>
        <div v-for="(value, index) in shadowProperty" class="shadow-property-item">
          <div class="content">
            <div class="name">
              <span>{{ value.propertyName }}</span>
              <span>
                <i class="el-icon-question" />
              </span>
            </div>
            <div class="value" />
            <div class="updateTiem">
              <span>更新时间：{{ value.updatedTime }}</span>
              <span />
            </div>
          </div>
          <div class="options">
            <span>
              <el-tooltip class="item" effect="dark" content="刷新" placement="top">
                <i class="option-icon el-icon-refresh-right" @click="getShadowData" />
              </el-tooltip>
            </span>
            <span>
              <el-tooltip class="item" effect="dark" content="详情" placement="top">
                <i class="option-icon el-icon-s-operation" />
              </el-tooltip>
            </span>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
<script>
import {
  getDeviceInfoShadow
} from '@/api/link/deviceInfo'
export default {
  name: 'DeviceShadow',
  props: {
    // 设备标识
    deviceIdentification: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loading: false,
      shadowData: {
        services: []
      },
      shadowProperty: []
    }
  },
  mounted() {
  },
  methods: {
    // 查询影子数据属性
    getShadowProperty(value = {}) {
      this.shadowProperty = value['properties'] || []
    },
    // 查询子设备影子数据
    getShadowData() {
      this.loading = true
      getDeviceInfoShadow(this.deviceIdentification).then(res => {
        const { msg = '', data = {}, code = 0 } = res
        if (code === 200 && msg === '操作成功') {
          this.shadowData = data
          this.getShadowProperty(this.shadowData.services?.[0] || {})
        } else {
          this.$message.toast('获取设备影子失败')
        }
        // this.shadowData = data1;
        // this.getShadowProperty(this.shadowData.services[0]);
        // this.loading = false;
        this.loading = false
      })
    }
  }
}
</script>
<style lang="scss" scoped>
div,
span {
  font-weight: normal;
}

.equipment-shadow {
  width: 100%;
  height: 100%;
  display: flex;
  overflow: auto;

  .shadow {
    width: 200px;
    border-right: 2px solid #d9d9d9;
    padding-right: 14px;

    label {
      font-size: 14px;
      margin-bottom: 16px;
    }

    .shadow-list {
      height: 525px;
      overflow-y: auto;

      .shadow-item {
        height: 85px;
        display: flex;
        flex-direction: column;
        background: #fff;
        border: 1px solid #c5c5c5;
        border-radius: 10px;
        padding: 12px;
        margin-top: 12px;

        .shadow-name {
          font-size: 12px;
          font-weight: bold;
          flex: 1;
        }

        .shadow-info {
          font-size: 10px;
          color: #707070;
        }
      }
    }
  }

  .shadow-property-list {
    width: calc(100% - 200px);
    height: 100%;
    padding-left: 24px;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    overflow-y: auto;

    .shadow-property-item {
      z-index: 999;
      width: 240px;
      border: 1px solid #c5c5c5;
      margin: 0 12px 12px 0;
      background: #fff;
      cursor: pointer;

      .content {
        padding: 20px;
        padding-bottom: 10px;

        .name {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .value {
          padding: 20px;
        }

        .updateTiem {
          color: #666666;
        }
      }

      .options {
        margin: 0;
        padding: 0;
        list-style: none;
        border-top: 1px solid #c5c5c5;
        display: flex;
        justify-content: space-around;

        span {
          display: block;
          width: 190px;
          height: 50px;
          text-align: center;
          line-height: 50px;

          .option-icon {
            font-size: 18px;
          }

          .option-icon:hover {
            color: rgb(64, 158, 255);
          }
        }
      }
    }

    .shadow-property-item:hover {
      transition-duration: 0.3s;
      transform: scale(1.05);
    }
  }
}
</style>
