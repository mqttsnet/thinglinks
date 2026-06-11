export interface DistrictInfo {
  adcode: string;
  name: string;
  level: string;
  citycode?: string;
  center?: string;
  districts?: DistrictInfo[];
  districtList?: DistrictInfo[];
}

export interface DistrictSearchResult {
  info: string;
  districtList: DistrictInfo[];
}

export interface AreaSelectorItem {
  uid: string;
  provinceCode?: string;
  provinceName?: string;
  cityCode?: string;
  cityName?: string;
  cityDuplicate?: boolean;
}
