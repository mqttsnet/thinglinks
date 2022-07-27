<template>
    <div class="wrap">
        <div class="sear-map">
            <div>
                <span>省/市/区</span>
                <el-cascader style="width: 64%;" placeholder="请输入省市区信息" filterable clearable v-model="locationValue"
                    :options="options" :props="{ checkStrictly: false }">
                </el-cascader>
            </div>
            <div>
                <span>详细地址</span>
                <el-select style="width: 64%" v-model="address" filterable remote reserve-keyword clearable
                    placeholder="详细地址" :remote-method="remoteMethod" :loading="loading" @change="tipChange">
                    <el-option v-for="item in tips" :key="item.id" :label="item.name" :value="item.id">
                        <span style="float: left">{{ item.name }}</span>
                        <span style="float: right; color: #8492a6; font-size: 13px">{{
                                item.district
                        }}</span>
                    </el-option>
                    <i slot="suffix" class="el-input__icon el-icon-date"></i>
                </el-select>
            </div>
        </div>
        <div ref="asd" style="width: 95%; height: 300px;margin: auto;"></div>
    </div>
</template>

<script>
import AMapLoader from "@amap/amap-jsapi-loader";

export default {
    props: {
    },
    data() {
        return {
            address: "", //详细地址
            lonLat: "", //经纬度
            loading: false,
            tips: [],
            locationValue: [],
            Province: [],
            options: [],
            projectMapMarker: undefined,
            map: undefined,
            isMap: true,
            geocoder: null,
            center: [116.397428, 39.90923],
        };
    },
    watch: {
    },
    mounted() {
        this.initMap();
    },
    methods: {
        //地图初始化
        initMap() {
            AMapLoader.load({
                key: "e13456422e8fe93451cf2201f4db84bd", // 申请好的Web端开发者Key，首次调用 load 时必填
                version: "2.0", // 指定要加载的 JSAPI 的版本，缺省时默认为 1.4.15
                plugins: [
                    "AMap.DistrictSearch",
                    "AMap.Geocoder",
                    "AMap.AutoComplete ", //2.0版本是AMap.AutoComplete，    2.0以下是AMap.Autocomplete 
                    // 输入提示插件
                    "AMap.PlaceSearch", // POI搜索插件
                ],
            }).then(() => {
                var chartDom = this.$refs.asd;
                this.map = new window.AMap.Map(chartDom, {
                    resizeEnable: true,
                    zoom: 13, //地图显示的缩放级别
                    keyboardEnable: false,
                    center: this.center,
                });
                this.map.on("click", this.mapClick);
                this.geocoder = new window.AMap.Geocoder({
                    city: "010", //城市设为北京，默认：“全国”
                    radius: 500, //范围，默认：500
                    extensions: 'all'
                })
                let districtSearch = new window.AMap.DistrictSearch({
                    // 关键字对应的行政区级别，country表示国家
                    level: 'country',
                    //  显示下级行政区级数，1表示返回下一级行政区
                    subdistrict: 4,
                    extensions: "base",
                })
                // 搜索所有省/直辖市信息
                let _this = this
                districtSearch.search('中国', function (status, result) {
                    // 查询成功时，result即为对应的行政区信息
                    console.log(result.districtList[0].districtList);
                    _this.Province = result.districtList[0].districtList
                    result.districtList[0].districtList.map((province) => {
                        let provinceMap = new Map();
                        provinceMap.value = province.name;   //区域编号
                        provinceMap.label = province.name;     //省名称
                        provinceMap.children = [];
                        _this.options.push(provinceMap);
                        if (province.districtList) {
                            province.districtList.map((city) => {
                                let cityMap = new Map()
                                cityMap.value = city.name;   //区域编号
                                cityMap.label = city.name;     //市名称
                                cityMap.children = [];
                                provinceMap.children.push(cityMap); //添加市
                                if (city.districtList) {
                                    city.districtList.map((area) => {
                                        let areaMap = new Map()
                                        areaMap.value = area.name;   //区域编号
                                        areaMap.label = area.name;     //市名称
                                        cityMap.children.push(areaMap); //添加县
                                    })
                                }
                            })
                        }
                    })
                })
            });
        },

        //远程搜所
        remoteMethod(query) {
            if (query !== "" && this.locationValue !== []) {
                let queryValue = this.locationValue.join('') + query;
                // console.log(this.locationValue, query, queryValue);
                this.loading = true;
                const that = this;
                window.AMap.plugin("AMap.AutoComplete", function () {
                    // 实例化Autocomplete
                    const autoOptions = {
                        city: "全国",
                    };
                    const autoComplete = new window.AMap.AutoComplete(autoOptions);
                    autoComplete.search(queryValue, function (status, result) {
                        let res = result.tips || []; // 搜索成功时，result即是对应的匹配数据
                        that.tips = res.filter((item) => {
                            return item.id !== "";
                        });
                        that.loading = false;
                    });
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
                    break;
                }
            }
        },
        mapClick(e) {
            // 地图点击事件
            if (this.map !== undefined) {
                this.lonLat = e.lnglat.lng + "," + e.lnglat.lat;
                this.regeoCode(this.lonLat); //把经纬度转化为地址
            }
        },
        regeoCode(lonLatValue) {
            if (lonLatValue) {
                let lnglat = lonLatValue.split(",");
                this.$emit("locationChange", lnglat);
                if (this.projectMapMarker !== undefined) {
                    // 如果点标记已存在则先移除原点
                    this.map.remove(this.projectMapMarker);
                    this.lonLat = "";
                }
                this.projectMapMarker = new window.AMap.Marker({
                    // 定义点标记对象
                    position: new window.AMap.LngLat(lnglat[0], lnglat[1]),
                });
                console.log(this.projectMapMarker);
                // 把拿到的经纬度转化为地址信息
                this.map.add(this.projectMapMarker);// 添加点标记在地图上
                this.map.setCenter(lnglat);
                this.map.setZoom(13);
                this.projectMapMarker.setPosition(lnglat);
                this.getAddress(lnglat)
            }
        },
        // 获取地址 
        getAddress(lnglat) {
            const that = this
            that.geocoder.getAddress(lnglat, (status, result) => {
                // console.log(result);
                let resp = result.regeocode.addressComponent
                that.locationValue = [resp.province, resp.city, resp.district,]
                if (status === 'complete' && result.info === 'OK') {
                    if (result && result.regeocode) {
                        that.address = result.regeocode.formattedAddress
                        console.log(result.regeocode);
                        this.$emit("locationAddress", result.regeocode);
                    } else {
                        this.$emit("locationFail", "地址查询位置失败,请检查地址是否正确");
                    }
                }
            })
        },
    },
};
</script>

<style lang="scss" scoped>
.wrap {
    width: 100%;

    .sear-map {
        margin-bottom: 15px;
        display: flex;

        div {
            width: 50%;
            display: flex;
            align-items: center;

            span {
                display: inline-block;
                width: 90px;
                font-weight: 700;
                text-align: end;
                padding-right: 12px;
            }
        }

    }
}
</style>
