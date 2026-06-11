<template>
  <div style="width: 100%">
    <div :id="'container' + keyNum" class="map" v-bind="$attrs">
      <div v-if="!mapReady" class="map-loading">
        <a-spin :tip="t('component.map.loading')" />
      </div>
    </div>
    <a-auto-complete
      class="search_box"
      v-model:value="searchTerm"
      :options="suggestions"
      @search="onSearch"
      @select="onSelect"
      style="width: 300px; margin-bottom: 20px"
      :disabled="!mapReady"
    >
      <a-input v-model:value="searchTerm" :placeholder="t('component.map.searchPlaceholder')" :disabled="!mapReady" />
    </a-auto-complete>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, reactive, watch, onMounted, onBeforeUnmount } from 'vue';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useI18n } from '/@/hooks/web/useI18n';
  import AMapLoader from '@amap/amap-jsapi-loader';
  import { useDict } from '/@/components/Dict';
  import { DictEnum } from '/@/enums/commonEnum';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { initGetDictList } = useDict();

  export default defineComponent({
    name: 'AMap',
    inheritAttrs: false,
    props: {
      value: {
        type: Array as PropType<any[]>,
      },
      address: String,
      isInput: Boolean,
    },
    emits: ['change', 'addressClick', 'update:value', 'updateMap'],
    setup(props, { emit }) {
      const { createMessage } = useMessage();
      const loading = ref(false);
      const mapReady = ref(false); // 地图是否加载完成
      const attrs = useAttrs();
      const { t } = useI18n();
      const searchTerm = ref('');
      const suggestions = ref([]);

      onMounted(async () => {
        window['_AMapSecurityConfig'] = {
          securityJsCode:
            (await initGetDictList(DictEnum.TENANT_AMAP__AAPPLICATION_JS_API_KEY)).find(
              (item) => item.key === 'securityJsCode',
            )?.name ?? '', //key的安全密钥
        };
        initMap();
      });

      const mapObj = reactive({
        map: null,
        geoCoder: null,
        placeSearch: null,
        autoComplete: null,
        infoWindow: null,
        marker: null,
        form: {
          address: '',
          lng: '',
          lat: '',
        },
        areaList: [],
        address: '',
      });
      const flag = ref(false);
      const keyNum = ref(parseInt((Math.random() * 100).toString()));

      watch(
        () => [props.value, props.address, props.isInput],
        (data) => {
          if (data[2]) {
            let address = data[1];
            getLnglat(address);
          } else {
            let lnglat = data[0] || [];
            if (lnglat?.length == 2 && lnglat[0] && lnglat[1] && flag.value) {
              mapObj.form.lng = lnglat[0];
              mapObj.form.lat = lnglat[1];
              removeMarker();
              setMapMarker();
            }
          }
        },
      );

      function handleChange(value, addressComponent, formattedAddress) {
        emit('addressClick', reactive(value), addressComponent, formattedAddress);
      }

      const onSearch = (value) => {
        if (!mapReady.value) {
          return; // 地图未加载完成，不执行搜索
        }
        if (mapObj.autoComplete && value) {
          mapObj.autoComplete.search(value, (status, result) => {
            console.log(status, result);
            if (status === 'complete' && result.tips) {
              suggestions.value = result.tips.map((tip) => ({
                value: tip.name,
                label: tip.name,
              }));
            } else {
              suggestions.value = [];
            }
          });
        }
      };

      const onSelect = (value, option) => {
        if (!mapReady.value) {
          return; // 地图未加载完成，不执行选择操作
        }
        if (mapObj.placeSearch) {
          mapObj.placeSearch.search(value, (status, result) => {
            if (status === 'complete' && result.info === 'OK') {
              mapObj.map.clearMap();
              result.poiList.pois.forEach((poi) => {
                const marker = new AMap.Marker({
                  position: poi.location,
                  map: mapObj.map,
                  title: poi.name,
                });
                // 信息窗体埋点
                // marker.on('click', () => {
                //   console.log('Marker clicked:', poi.name);
                //   mapObj.infoWindow.setContent(`
                //   <div>
                //     <h4>${poi.name}</h4>
                //     <p>${poi.address}</p>
                //   </div>
                // `);
                //   mapObj.infoWindow.open(mapObj.map, marker.getPosition());
                // });
              });

              if (result.poiList.pois.length > 0) {
                mapObj.map.setCenter(result.poiList.pois[0].location);
                const location = result.poiList.pois[0].location;
                mapObj.form.lat = location.lat;
                mapObj.form.lng = location.lng;
                toGetAddress();
              }
            } else {
              console.error('Search failed: ', result);
              createMessage.error(result);
            }
          });
        }
      };

      async function initMap() {
        AMapLoader.load({
          key:
            (await initGetDictList(DictEnum.TENANT_AMAP__AAPPLICATION_JS_API_KEY)).find(
              (item) => item.key === 'key',
            )?.name ?? '',
          version: '2.0',
          plugins: [
            'AMap.ControlBar',
            'AMap.AutoComplete',
            'AMap.PlaceSearch',
            'AMap.Geocoder',
            'AMap.Marker',
            'AMap.InfoWindow',
          ],
        })
          .then((AMap) => {
            mapObj.map = new AMap.Map('container' + keyNum.value, {
              viewMode: '2D',
              zoom: 15,
              resizeEnable: true,
            });

            mapObj.placeSearch = new AMap.PlaceSearch({ map: mapObj.map });
            mapObj.geoCoder = new AMap.Geocoder();
            mapObj.autoComplete = new AMap.AutoComplete({ city: '全国' });
            mapObj.infoWindow = new AMap.InfoWindow({
              isCustom: true,
              offset: new AMap.Pixel(0, -30),
            });

            flag.value = true;
            mapReady.value = true; // 地图加载完成

            if (props.value && props.value[0] && props.value[1]) {
              mapObj.form.lng = props.value[0];
              mapObj.form.lat = props.value[1];
              setMapMarker();
            }

            mapObj.map.on('click', (e) => {
              if (!e || !e.lnglat) {
                return;
              }
              mapObj.form.lng = e.lnglat.lng;
              mapObj.form.lat = e.lnglat.lat;
              removeMarker();
              setMapMarker();
              toGetAddress();
            });
          })
          .catch((e) => {
            console.error(e);
          });
      }

      const getLnglat = (address) => {
        if (!mapReady.value) {
          return; // 地图未加载完成，不执行地理编码
        }
        if (!mapObj.geocoder) {
          mapObj.geocoder = new AMap.Geocoder();
        }
        mapObj.geocoder.getLocation(address, function (status, result) {
          if (status === 'complete' && result.info === 'OK') {
            mapObj.address = address;
            mapObj.form.address = address;
            mapObj.form.lng = result.geocodes[0].location.lng;
            mapObj.form.lat = result.geocodes[0].location.lat;
            emit('updateMap', [mapObj.form.lng, mapObj.form.lat]);
            removeMarker();
            setMapMarker();
          }
        });
      };

      function setMapMarker() {
        if (!mapReady.value) {
          return; // 地图未加载完成，不设置标记
        }
        if (mapObj.form.lng === '' && mapObj.form.lat === '') {
          return;
        }
        mapObj.marker = new AMap.Marker({
          map: mapObj.map,
          position: [mapObj.form.lng, mapObj.form.lat],
        });
        // 信息窗体埋点
        // mapObj.marker.on('click', () => {
        //   mapObj.infoWindow.setContent(`
        //           <div>
        //             <p>${mapObj.form.address}</p>
        //           </div>
        //         `);
        //   mapObj.infoWindow.open(mapObj.map, mapObj.marker.getPosition());
        // });
        mapObj.map.setCenter([mapObj.form.lng, mapObj.form.lat]);
        mapObj.map.add(mapObj.marker);
      }

      function removeMarker() {
        mapObj.map?.clearMap();
      }

      function toGetAddress() {
        const lnglat = [mapObj.form.lng, mapObj.form.lat];
        mapObj.geoCoder.getAddress(lnglat, (status, result) => {
          if (status === 'complete' && result.regeocode) {
            mapObj.address = result.regeocode.formattedAddress;
            mapObj.form.address = result.regeocode.formattedAddress;
            handleChange(
              [mapObj.form.lng, mapObj.form.lat],
              result.regeocode.addressComponent,
              result.regeocode.formattedAddress,
            );
          }
        });
      }

      function remoteMethod(query) {
        if (query !== '') {
          setTimeout(() => {
            mapObj.autoComplete.search(query, (status, result) => {
              mapObj.areaList = result.tips;
            });
          }, 500);
        } else {
          mapObj.areaList = [];
        }
      }

      function destroyMap() {
        mapObj.map && mapObj.map.destroy();
      }

      onBeforeUnmount(() => {
        console.log('地图销毁');
        destroyMap();
      });

      return {
        searchTerm,
        suggestions,
        attrs,
        loading,
        mapReady,
        t,
        handleChange,
        mapObj,
        toGetAddress,
        keyNum,
        getLnglat,
        onSearch,
        onSelect,
      };
    },
  });
</script>
<style scoped lang="less">
  .map {
    width: 100%;
    height: 400px;
    position: relative;
  }

  .map-loading {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: rgba(255, 255, 255, 0.7);
    z-index: 10;
  }

  .search_box {
    position: absolute;
    // z-index: 9;
    top: 20px;
    right: 20px;
  }
</style>
