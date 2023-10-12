<template>
  <div class="wrap">
    <el-form>
      <el-row>
        <el-col :span="2"></el-col>
        <el-col :span="12">
          <el-form-item label="省市区" label-width="85px">
            <el-select
              v-model="provinceValue"
              placeholder="省份"
              @change="provinceE"
            >
              <el-option
                v-for="item in province"
                :key="item.value"
                :label="item.name"
                :value="item.name"
              >
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="地级市" label-width="80px">
            <el-select v-model="cityValue" placeholder="市级" @change="cityE">
              <el-option
                v-for="item in city"
                :key="item.value"
                :label="item.name"
                :value="item.name"
              >
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="区县" label-width="80px">
            <el-select v-model="areaValue" placeholder="区县" @change="areaE">
              <el-option
                v-for="item in area"
                :key="item.value"
                :label="item.name"
                :value="item.name"
              >
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="4"></el-col>
      </el-row>
      <el-row>
        <el-col :span="21">
          <el-form-item label="详细地址" label-width="75px">
            <el-select
              style="width: 97%; margin: auto"
              v-model="address"
              filterable
              remote
              reserve-keyword
              clearable
              placeholder="详细地址"
              :remote-method="remoteMethod"
              :loading="loading"
              @change="tipChange"
            >
              <el-option
                v-for="item in tips"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <span style="float: left">{{ item.name }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{
                  item.district
                }}</span>
              </el-option>
              <i slot="suffix" class="el-input__icon el-icon-date"></i>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="22">
          <div ref="asd" style="width: 95%; height: 300px; margin: auto"></div>
        </el-col>
      </el-row>
      <el-row style="margin-top: 15px">
        <el-col :span="11">
          <el-form-item label="纬度" prop="latitude" label-width="120px">
            <el-input v-model="latitude" placeholder="请输入纬度" />
          </el-form-item>
        </el-col>
        <el-col :span="11">
          <el-form-item label="经度" prop="longitude" label-width="120px">
            <el-input v-model="longitude" placeholder="请输入经度" />
          </el-form-item>
        </el-col>
        <el-col :span="2"></el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import AMapLoader from "@amap/amap-jsapi-loader";

export default {
  name: "HelloWorld",
  data() {
    return {
      address: "",
      loading: false,
      //纬度、经度
      latitude: "",
      longitude: "",
      //地图对象
      map: null,
      zoom: 13,
      center: [116.397428, 39.90923],
      geocoder: null,
      districtSearch: null,
      //三級聯動
      flag: "province",
      province: [],
      city: [],
      area: [],
      provinceValue: "",
      cityValue: "",
      areaValue: "",
      //搜索提示
      tips: [],
      //三级、详细信息
      deviceLocation: {},
    };
  },
  watch: {
    provinceValue(newVal) {
      console.log(111);
      console.log(this.provinceValue);
      console.log(newVal);
      if (this.isEmptyStr(newVal)) {
        return;
      }
      if (this.province.length == 0) {
        return;
      }
      if (this.provinceValue == newVal) {
        return;
      }
      this.provinceE(newVal);
    },
    // cityValue() {
    //   this.areaE()
    // },
  },
  mounted() {
    this.TheMap();
  },
  methods: {
    isEmptyStr(s) {
      if (s == undefined || s == null || s == "") {
        return true;
      }
      return false;
    },
    TheMap(lonLat) {
      this.address = "";
      this.provinceValue = "";
      this.cityValue = "";
      this.areaValue = "";
      this.latitude = "";
      this.longitude = "";
      let _this = this;
      AMapLoader.load({
        key: "a7875e62299794a32054f208842e0c34", // 申请好的Web端开发者Key，首次调用 load 时必填
        version: "2.0", // 指定要加载的 JSAPI 的版本，缺省时默认为 1.4.15
        plugins: [
          "AMap.DistrictSearch",
          "AMap.Geocoder",
          "AMap.AutoComplete ", //2.0版本是AMap.AutoComplete，    2.0以下是AMap.Autocomplete
          // 输入提示插件
          "AMap.PlaceSearch", // POI搜索插件
          "AMap.AutoComplete",
        ],
      }).then(() => {
        _this.initMap();
        _this.map.on("click", _this.mapClick);
        _this.initGeocoder();
        _this.initDistrictSearch();
        if (lonLat) {
          _this.regeoCode(lonLat);
        }
      });
    },
    initMap() {
      let _this = this;
      var chartDom = _this.$refs.asd;
      _this.map = new window.AMap.Map(chartDom, {
        resizeEnable: true,
        zoom: _this.zoom, //地图显示的缩放级别
        keyboardEnable: false,
        center: _this.center,
      });
    },
    initGeocoder() {
      this.geocoder = new window.AMap.Geocoder({
        city: "010", //城市设为北京，默认：“全国”
        radius: 500, //范围，默认：500
        extensions: "all",
      });
      console.log(this.geocoder);
    },
    initDistrictSearch() {
      let _this = this;
      this.districtSearch = new window.AMap.DistrictSearch({
        //  显示下级行政区级数，1表示返回下一级行政区
        subdistrict: 3,
        extensions: "base",
      });
      // 搜索所有省/直辖市信息
      this.districtSearch.search("中国", function (status, result) {
        console.log(result);
        console.log(result.districtList[0].districtList);
        // 查询成功时，result即为对应的行政区信息
        _this.province = result.districtList[0].districtList;
      });
    },
    initAutoComplete(queryValue) {
      const _this = this;
      const autoOptions = {
        city: "全国",
        zoom: _this.zoom,
      };
      const autoComplete = new window.AMap.AutoComplete(autoOptions);
      autoComplete.search(queryValue, function (status, result) {
        let res = result.tips || []; // 搜索成功时，result即是对应的匹配数据
        _this.tips = res.filter((item) => {
          return item.id !== "";
        });
        _this.loading = false;
      });
    },
    // 地图点击事件
    mapClick(e) {
      this.initDistrictSearch();
      if (this.map !== undefined) {
        this.lonLat = e.lnglat.lng + "," + e.lnglat.lat;
        this.regeoCode(this.lonLat);
      }
    },
    //经纬度转化为地址、添加标记点
    regeoCode(lonLat) {
      if (lonLat) {
        let lnglat = lonLat.split(",");
        this.latitude = lnglat[0];
        this.longitude = lnglat[1];
        this.$emit("locationChange", lnglat);
        this.MapMarker(lnglat);
        this.getAddress(lnglat);
      }
    },
    //添加标记点
    MapMarker(lnglat) {
      if (lnglat) {
        if (this.projectMapMarker !== undefined) {
          // 如果点标记已存在则先移除原点
          this.map.remove(this.projectMapMarker);
          this.lonLat = "";
        }
        this.projectMapMarker = new window.AMap.Marker({
          // 定义点标记对象
          position: new window.AMap.LngLat(lnglat[0], lnglat[1]),
        });
        this.map.add(this.projectMapMarker); // 添加点标记在地图上
        this.map.setCenter(lnglat);
        this.map.setZoom(16);
        this.projectMapMarker.setPosition(lnglat);
      }
    },
    // 把拿到的经纬度转化为地址信息
    getAddress(lnglat) {
      const _this = this;
      _this.geocoder.getAddress(lnglat, (status, result) => {
        console.log(result);
        let resp = result.regeocode.addressComponent;
        console.log(result);
        _this.locationValue = [resp.province, resp.city, resp.district];
        _this.provinceValue = resp.province;
        _this.cityValue = resp.city;
        _this.areaValue = resp.district;
        if (status === "complete" && result.info === "OK") {
          if (result && result.regeocode) {
            _this.address = result.regeocode.formattedAddress;
            console.log(result.regeocode.addressComponent.district, _this.area);
            _this.area.forEach((item) => {
              if (item.name === result.regeocode.addressComponent.district) {
                console.log(item);
                result.regeocode.addressComponent.district = item.adcode;
              }
            });
            console.log(result.regeocode.addressComponent.district);
            this.$emit("locationAddress", result.regeocode);
          } else {
            this.$emit("locationFail", "地址查询位置失败,请检查地址是否正确");
          }
        }
      });
    },
    //省、市、区改变触发
    provinceE(newVal) {
      this.flag = "province";
      let city = this.province.filter((item) => {
        return item.name === newVal;
      });
      this.city = city[0].districtList;
      this.setProvince(newVal);
      this.cityValue = "";
      this.areaValue = "";
      this.address = "";
    },
    cityE(newVal) {
      this.flag = "city";
      let arealist = this.city.filter((item) => {
        return item.name === newVal;
      });
      if (arealist[0] !== undefined) {
        this.area = arealist[0].districtList;
        this.setProvince(newVal);
        this.areaValue = "";
        this.address = "";
      }
    },
    areaE(newVal) {
      console.log(111);
      this.flag = "district";
      if (newVal !== "") {
        this.setProvince(newVal);
        this.address = "";
      }
    },
    //三級聯動点击动作
    setProvince(value) {
      let adcode;
      let level;
      if (this.flag === "province") {
        this.province.map((item) => {
          if (item.name === value) {
            adcode = item.adcode;
            level = item.level;
          }
        });
        this.flag = "city";
      } else if (this.flag === "city") {
        this.city.map((item) => {
          if (item.name === value) {
            adcode = item.adcode;
            level = item.level;
          }
        });
        this.flag = "district";
      } else if (this.flag === "district") {
        this.area.map((item) => {
          if (item.name === value) {
            adcode = item.adcode;
            level = item.level;
          }
        });
      }
      this.districtSearch.setLevel(level);
      this.districtSearch.setExtensions("all");
      let _this = this;
      _this.districtSearch.search(adcode, function (status, result) {
        console.log(result);
        if (result.districtList[0] !== undefined) {
          _this.map.setCenter([
            result.districtList[0].center.lng,
            result.districtList[0].center.lat,
          ]);
        }
      });
    },
    //远程搜所
    remoteMethod(query) {
      if (query !== "") {
        let queryValue =
          this.provinceValue + this.cityValue + this.areaValue + query;
        this.loading = true;
        const _this = this;
        AMap.plugin("AMap.AutoComplete", function () {
          //   // 实例化Autocomplete
          _this.initAutoComplete(queryValue);
        });
      } else {
        this.tips = [];
      }
    },
    tipChange(value) {
      //下拉选择
      for (const tip of this.tips) {
        if (value === tip.id) {
          this.lonLat = tip.location.lng + "," + tip.location.lat;
          this.regeoCode(this.lonLat);
          this.map.setCenter([tip.location.lng, tip.location.lat]);
          break;
        }
      }
    },
    //修改时触发
  },
};
</script>

<style scoped>
.el-row {
  display: flex;
  justify-content: space-evenly;
}
</style>
