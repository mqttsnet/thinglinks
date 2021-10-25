<template>
  <div class="map-wrap">
    <amap
      cache-key="map"
      ref="map"
      view-mode="2D"
      async
      :zoom.sync="zoom"
      :center.sync="center"
      :pitch.sync="pitch"
      :rotation.sync="rotation"
      :show-indoor-map="false"
      is-hotspot
      @click="mapClick"
      @hotspotclick="onHotspotClick"
    >
      <!-- :label="{
            content: '标记物提示!',
            direction: 'bottom',
          }" -->
      <amap-marker
        ref="marker"
        :position="position"
        draggable
        @dragend="dragend"
      />
    </amap>
  </div>
</template>

<script>
// 组件
// import Amap from '@amap/amap-vue/lib/amap';
// import AmapMarker from '@amap/amap-vue/lib/marker';

// 函数api
// import { loadAmap } from "@amap/amap-vue"

export default {
  components: {
    // Amap,
    // AmapMarker,
  },
  data() {
    return {
      center: [116.473778, 39.990661],
      position: [116.473778, 39.990661],
      zoom: 14,
      pitch: 49, // 倾斜
      rotation: 15, // 旋转
      AMap: null,
      detailAddress:'',
    };
  },
  async mounted() {
    this.AMap = await loadAmap();
    // 首次初始化
    this.$nextTick(() => {
      this.$emit("locationInit", '');
    })
  },

  methods: {
    getAddress(city = "", location) {
      let that = this
      let geocoder = new this.AMap.Geocoder({
        city: city, //默认：“全国”
      });
      geocoder.getAddress(location, (status, result) => {
        // console.log("逆编码", status,result);
        if (status ==='complete' && result.info === "OK") {
          // result为对应的地理位置详细信息

          let province = result.regeocode.addressComponent.province
          let city =  result.regeocode.addressComponent.city
          let wholeAddress  =result.regeocode.formattedAddress
          wholeAddress= wholeAddress.replace(province,'')
          wholeAddress= wholeAddress.replace(city,'')
          that.$emit("address", wholeAddress);
        }



      });
    },
    initLocation(city = "", address) {
      this.$nextTick(() => {
        let geocoder = new this.AMap.Geocoder({
          city: city, //默认：“全国”
        });

        address = address ? city + address : city;
        geocoder.getLocation(address, (status, result) => {
          // console.log(address)
          if (status === "complete" && result.geocodes.length) {
            let lnglat = result.geocodes[0].location;
            this.position = [lnglat.lng, lnglat.lat];
            this.center = [lnglat.lng, lnglat.lat];
            // 坐标更新事件
            this.$emit("locationChange", [lnglat.lng, lnglat.lat]);
          } else {
            // console.log("根据地址查询位置失败");
            // 失败提示事件
            this.$emit("locationFail", "地址查询位置失败,请检查地址是否正确");
          }
        });
      });
    },
    dragend(e) {
      // 坐标更新事件
      this.$emit("locationChange", [e.lnglat.lng, e.lnglat.lat]);
    },
    mapClick(e) {
      if (e && e.lnglat) {
        this.position = [e.lnglat.lng, e.lnglat.lat];
        this.center = [e.lnglat.lng, e.lnglat.lat];
        // 坐标更新事件
        this.$emit("locationChange", [e.lnglat.lng, e.lnglat.lat]);
      }
    },
    onHotspotClick(e) {
      if (e && e.lnglat) {
        this.center = [e.lnglat.lng, e.lnglat.lat];
      }
    },
  },
};
</script>
<style scoped>
.map-wrap {
  width: 100%;
  height: 321px;
  margin-left: 43px;
  padding: 20px;
}
</style>
