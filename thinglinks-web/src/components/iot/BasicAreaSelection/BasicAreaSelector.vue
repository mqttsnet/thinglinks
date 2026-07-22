<template>
  <BasicCustomCom>
    <div class="basic-area-selector">
      <template v-if="props.disabled">
        <div class="basic-area-selector__summaryCard">
          <div class="basic-area-selector__summaryHeader">
            <!-- 已选择{{ props.modelValue?.length || 0 }}个区域 -->
            {{
              t('iot.basic.basicComp.BasicAreaSelector.selectedAreaCount', {
                value: props.modelValue?.length || 0,
              })
            }}
          </div>
          <!-- <div
          class="basic-area-selector__summaryList"
          v-if="props.modelValue && props.modelValue.length"
        >
          <div
            class="basic-area-selector__summaryItem"
            v-for="(it, idx) in props.modelValue"
            :key="it.uid || idx"
          >
            <span class="label">省份：</span>
            <span class="value">{{ it.provinceName || it.provinceCode || '-' }}</span>
            <span class="spacer">｜</span>
            <span class="label">城市：</span>
            <span class="value">{{ it.cityName || it.cityCode || '-' }}</span>
          </div>
        </div>
        <a-empty v-else description="暂无区域" /> -->
        </div>
      </template>
      <template v-else>
        <div v-for="(item, index) in areaList" :key="item.uid" class="basic-area-selector__item">
          <div class="basic-area-selector__row">
            <Select
              v-model:value="item.provinceCode"
              :options="provinceOptions"
              :placeholder="t('iot.basic.basicComp.BasicAreaSelector.selectProvince')"
              class="basic-area-selector__select"
              @change="(value) => handleProvinceChange(value, index)"
              :loading="loading"
              allow-clear
              show-search
              option-filter-prop="label"
            />
            <Select
              v-model:value="item.cityCode"
              :options="getCityOptions(item.provinceCode)"
              :placeholder="t('iot.basic.basicComp.BasicAreaSelector.selectCity')"
              class="basic-area-selector__select"
              @change="(value) => handleCityChange(value, index)"
              :loading="loading"
              :disabled="!item.provinceCode"
              allow-clear
              show-search
              option-filter-prop="label"
            />
            <div class="basic-area-selector__actions">
              <a-button type="link" danger @click="() => removeArea(index)">
                {{ t('common.delText') }}
              </a-button>
            </div>
          </div>
          <div v-if="item.cityDuplicate" class="basic-area-selector__error">
            {{ t('iot.basic.basicComp.BasicAreaSelector.cityCannotRepeat') }}
          </div>
        </div>
        <div class="basic-area-selector__footer">
          <a-button type="link" @click="addArea">
            {{ t('iot.basic.basicComp.BasicAreaSelector.addArea') }}
          </a-button>
        </div>
      </template>
    </div>
  </BasicCustomCom>
</template>

<script lang="ts" setup>
  import { computed, onMounted, reactive, toRefs, watch, onBeforeUnmount } from 'vue';
  import { Select } from 'ant-design-vue';
  import BasicCustomCom from '../BasicCustomCom/BasicCustomCom.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { buildUUID } from '/@/utils/uuid';
  import AMapLoader from '@amap/amap-jsapi-loader';
  import { DictEnum } from '/@/enums/commonEnum';
  import type { AreaSelectorItem, DistrictSearchResult, DistrictInfo } from './typing';

  interface AreaOption {
    label: string;
    value: string;
  }

  const props = defineProps<{ modelValue: AreaSelectorItem[]; disabled?: boolean }>();
  const emit = defineEmits<{
    (e: 'update:modelValue', value: AreaSelectorItem[]): void;
    (e: 'change', value: AreaSelectorItem[]): void;
  }>();

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { initGetDictList } = useDict();

  const createEmptyAreaItem = (): AreaSelectorItem => ({
    uid: buildUUID(),
    provinceCode: undefined,
    provinceName: undefined,
    cityCode: undefined,
    cityName: undefined,
    cityDuplicate: false,
  });

  const state = reactive({
    loading: false,
    provinceOptions: [] as AreaOption[],
    cityMap: new Map<string, AreaOption[]>(),
    areaList: [createEmptyAreaItem()] as AreaSelectorItem[],
  });

  let amapLoaderPromise: Promise<typeof AMap> | null = null;
  const provinceCache = new Map<string, AreaOption[]>();

  const loading = computed(() => state.loading);
  const provinceOptions = computed(() => state.provinceOptions);
  const areaList = computed(() => state.areaList);

  const addArea = () => {
    state.areaList.push(createEmptyAreaItem());
    emitChange();
  };

  const removeArea = (index: number) => {
    state.areaList.splice(index, 1);
    if (state.areaList.length === 0) {
      state.areaList.push(createEmptyAreaItem());
    }
    markDuplicates();
    emitChange();
  };

  const getCityOptions = (provinceCode?: string) => {
    if (!provinceCode) return [];
    return state.cityMap.get(provinceCode) ?? [];
  };

  const handleProvinceChange = async (provinceCode: string | undefined, index: number) => {
    const record = state.areaList[index];
    record.provinceCode = provinceCode;
    record.cityCode = undefined;
    record.cityName = undefined;

    if (provinceCode) {
      const option = provinceOptions.value.find((item) => item.value === provinceCode);
      record.provinceName = option?.label;
      await ensureCities(provinceCode);
    } else {
      record.provinceName = undefined;
    }
    markDuplicates();
    emitChange();
  };

  const handleCityChange = async (cityCode: string | undefined, index: number) => {
    const record = state.areaList[index];
    record.cityCode = cityCode;
    record.cityName = undefined;

    if (cityCode) {
      const cityOption = getCityOptions(record.provinceCode).find(
        (item) => item.value === cityCode,
      );
      record.cityName = cityOption?.label;
    }
    markDuplicates();
    emitChange();
  };

  const emitChange = () => {
    const clonedList = state.areaList.map((item) => ({
      ...item,
      cityDuplicate: !!item.cityDuplicate,
    }));
    emit('update:modelValue', clonedList);
    emit('change', clonedList);
  };

  const ensureCities = async (provinceCode: string) => {
    if (state.cityMap.has(provinceCode)) return;
    await loadDistrictData(provinceCode);
  };

  const markDuplicates = () => {
    const cityCount = state.areaList.reduce<Record<string, number>>((acc, item) => {
      if (item.cityCode) {
        acc[item.cityCode] = (acc[item.cityCode] ?? 0) + 1;
      }
      return acc;
    }, {});

    state.areaList.forEach((item) => {
      item.cityDuplicate = !!item.cityCode && (cityCount[item.cityCode] ?? 0) > 1;
    });
  };

  const loadDistrictData = async (adcode?: string) => {
    const isCountryRequest = !adcode;
    const targetAdcode = adcode ?? '中国';

    if (isCountryRequest && state.provinceOptions.length) {
      return;
    }
    if (!isCountryRequest && state.cityMap.has(targetAdcode)) {
      return;
    }

    state.loading = true;
    try {
      const AMap = await loadAMap();
      const districtSearch = new AMap.DistrictSearch({
        level: isCountryRequest ? 'country' : 'province',
        extensions: 'all',
        showbiz: false,
        subdistrict: isCountryRequest ? 2 : 1,
      });

      const result: DistrictSearchResult = await new Promise((resolve, reject) => {
        districtSearch.search(
          targetAdcode,
          (status: string, searchResult: DistrictSearchResult) => {
            if (status === 'complete') {
              resolve(searchResult);
              return;
            }
            const errorMsg = `DistrictSearch failed: ${status}`;
            reject(new Error(errorMsg));
          },
        );
      });

      const [districtInfo] = result.districtList ?? [];
      if (!districtInfo) {
        throw new Error('Empty district list');
      }

      const districtChildren = districtInfo.districtList ?? districtInfo.districts ?? [];

      if (districtInfo.level === 'country') {
        const provinces = districtChildren.map((province: DistrictInfo) => ({
          label: province.name,
          value: province.adcode,
        }));
        state.provinceOptions = provinces;

        provinces.forEach((province) => {
          if (provinceCache.has(province.value)) {
            state.cityMap.set(province.value, provinceCache.get(province.value)!);
            return;
          }
          const provinceDetail = districtChildren.find((item) => item.adcode === province.value);
          const cityChildren = provinceDetail?.districtList ?? provinceDetail?.districts ?? [];
          const cities = cityChildren.map((city) => ({
            label: city.name,
            value: city.adcode,
          }));
          provinceCache.set(province.value, cities);
          state.cityMap.set(province.value, cities);
        });
      } else if (districtInfo.level === 'province') {
        const cityChildren = districtChildren;
        const cities = cityChildren.map((city: DistrictInfo) => ({
          label: city.name,
          value: city.adcode,
        }));
        provinceCache.set(districtInfo.adcode, cities);
        state.cityMap.set(districtInfo.adcode, cities);
      }
    } catch (error) {
      console.error('[BasicAreaSelector] loadDistrictData error:', error);
      createMessage.error(t('iot.basic.basicComp.BasicAreaSelector.loadAreaDataFailed'));
    } finally {
      state.loading = false;
    }
  };

  const loadAMap = async () => {
    if (amapLoaderPromise) {
      return amapLoaderPromise;
    }

    amapLoaderPromise = (async () => {
      const dictList = await initGetDictList(DictEnum.TENANT_AMAP__AAPPLICATION_JS_API_KEY);
      const key = dictList.find((item) => item.key === 'key')?.name ?? '';
      const securityJsCode = dictList.find((item) => item.key === 'securityJsCode')?.name ?? '';

      if (!key) {
        createMessage.error(t('iot.basic.basicComp.BasicAreaSelector.missingApiKey'));
        throw new Error('AMap key missing');
      }

      if (securityJsCode) {
        window._AMapSecurityConfig = {
          securityJsCode,
        };
      }

      return await AMapLoader.load({
        key,
        version: '2.0',
        plugins: ['AMap.DistrictSearch'],
      });
    })();

    return amapLoaderPromise;
  };

  onMounted(async () => {
    await loadDistrictData();
    await initFromModel();
  });

  onBeforeUnmount(() => {
    state.areaList = [createEmptyAreaItem()];
    state.cityMap.clear();
    state.loading = false;
  });

  const initFromModel = async () => {
    if (props.modelValue?.length) {
      state.areaList = props.modelValue.map((item) => ({
        ...item,
        uid: item.uid ?? buildUUID(),
      }));
      await Promise.all(
        state.areaList
          .filter((item) => !!item.provinceCode)
          .map((item) => ensureCities(item.provinceCode!)),
      );
      emitChange();
    } else {
      state.areaList = [createEmptyAreaItem()];
      emitChange();
    }
    markDuplicates();
  };

  watch(
    () => props.modelValue,
    async (newValue) => {
      const normalizedList = Array.isArray(newValue)
        ? newValue.length > 0
          ? newValue.map((item) => ({ ...item, uid: item.uid ?? buildUUID() }))
          : [createEmptyAreaItem()]
        : [createEmptyAreaItem()];

      const currentJson = JSON.stringify(state.areaList);
      const nextJson = JSON.stringify(normalizedList);
      if (currentJson !== nextJson) {
        state.areaList = normalizedList;
        await Promise.all(
          state.areaList
            .filter((item) => !!item.provinceCode)
            .map((item) => ensureCities(item.provinceCode!)),
        );
        markDuplicates();
        emitChange();
      }
    },
    { deep: true },
  );

  const { areaList: exposeAreaList } = toRefs(state);
  defineExpose({ areaList: exposeAreaList });
</script>

<style scoped lang="less">
  .basic-area-selector {
    display: flex;
    flex-direction: column;

    &__summaryCard {
      background-color: #fff;
      border: 1px solid #f0f0f0;
      border-radius: 8px;
      padding: 12px 16px;
      margin-bottom: 8px;
    }

    &__summaryHeader {
      font-size: 14px;
      font-weight: 600;
      color: #1f1f1f;
    }

    &__summaryList {
      display: flex;
      flex-direction: column;
      gap: 6px;
      max-height: 320px;
      overflow: auto;
      margin-top: 8px;
    }

    &__summaryItem {
      display: flex;
      align-items: center;
      padding: 6px 8px;
      border: 1px dashed #e5e5e5;
      border-radius: 6px;
      background: #fafafa;

      .label {
        color: #8c8c8c;
        font-size: 12px;
        margin-right: 4px;
      }

      .value {
        color: #262626;
        font-size: 13px;
        font-weight: 500;
      }

      .spacer {
        color: #bfbfbf;
        margin: 0 6px;
      }
    }

    &__item {
      display: flex;
      flex-direction: column;
      padding: 4px 0;
      gap: 4px;
    }

    &__row {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    &__select {
      width: 160px;
    }

    &__actions {
      display: flex;
      align-items: center;
    }

    &__error {
      color: #ed6f6f;
      font-size: 12px;
      margin-left: calc(160px + 8px);
      line-height: 1.4;
    }

    &__footer {
      margin-top: 12px;
    }
  }
</style>
