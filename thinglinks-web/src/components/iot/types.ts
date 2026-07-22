export interface StepConfig {
  title: string;
  component: any;
  props?: Record<string, any>;
  visible?: boolean;
  validator?: () => boolean | Promise<boolean>;
}

export interface DeviceInfo {
  deviceIdentification?: string;
  deviceName?: string;
  [key: string]: any;
}

export interface ProductInfo {
  productIdentification?: string;
  productName?: string;
  [key: string]: any;
}
