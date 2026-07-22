/** Stream source types for drag-and-drop */
export type SourceType = 'channel' | 'proxy' | 'push';

export interface StreamSource {
  sourceType: SourceType;
  /** For channel: device + channel identification */
  deviceIdentification?: string;
  channelIdentification?: string;
  channelName?: string;
  hasAudio?: boolean;
  ptzCapability?: boolean;
  /** For proxy */
  proxyId?: string;
  proxyName?: string;
  /** For push */
  pushId?: string;
  /** Common */
  streamIdentification?: string;
  appId?: string;
  id?: string;
}

export type CellStatus = 'empty' | 'loading' | 'playing' | 'error';

export interface GridCell {
  index: number;
  source: StreamSource | null;
  playUrl: string;
  status: CellStatus;
  errorMsg: string;
  /** Raw stream info for URL panel */
  streamInfo: any;
}

export type LayoutType = 1 | 4 | 9 | 16 | '3-1' | '4-1';

export interface RegionTreeNode {
  key: string;
  title: string;
  value: string;
  children?: RegionTreeNode[];
  isLeaf?: boolean;
  /** For lazy-loaded channels under region */
  sourceType?: SourceType;
  sourceData?: StreamSource;
  icon?: string;
}
