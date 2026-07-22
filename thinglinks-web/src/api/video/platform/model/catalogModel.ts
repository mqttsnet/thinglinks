export interface VideoPlatformCatalogPageQuery {
  platformId?: string;
  name?: string;
  gbId?: string;
  parentId?: string;
  catalogType?: number;
}

export interface VideoPlatformCatalogSaveVO {
  platformId: string;
  name: string;
  gbId?: string;
  parentId?: string;
  catalogType?: number;
  civilCode?: string;
  sortOrder?: number;
}

export interface VideoPlatformCatalogUpdateVO extends VideoPlatformCatalogSaveVO {
  id: string;
}

export interface VideoPlatformCatalogResultVO {
  id: string;
  platformId: string;
  name: string;
  gbId: string;
  parentId: string;
  catalogType: number;
  civilCode: string;
  sortOrder: number;
  createdBy: string;
  createdTime: string;
  updatedBy: string;
  updatedTime: string;
  echoMap?: Recordable;
}
