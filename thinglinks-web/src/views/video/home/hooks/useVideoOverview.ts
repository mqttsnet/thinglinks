/**
 * 流媒体首页数据汇总 hook。
 * 并发加载：设备在线数、未处理告警数、活跃流、媒体节点数。
 * 任一接口失败时该字段走 0 兜底，不阻塞整页渲染。
 */
import { ref } from 'vue';
import {
  getAlarmUnhandledCount,
  getStreamOverview,
  getStreamList,
} from '/@/api/video/dashboard/dashboard';

export interface VideoOverview {
  onlineDevices: number;
  totalDevices: number;
  pendingAlarms: number;
  activeStreams: number;
  maxStreams: number;
  mediaNodes: number;
  mediaNodesOnline: number;
}

export function useVideoOverview() {
  const loading = ref(false);
  const overview = ref<VideoOverview>({
    onlineDevices: 0,
    totalDevices: 0,
    pendingAlarms: 0,
    activeStreams: 0,
    maxStreams: 0,
    mediaNodes: 0,
    mediaNodesOnline: 0,
  });
  const streams = ref<any[]>([]);

  async function load() {
    loading.value = true;
    const [alarmRes, overviewRes, streamRes] = await Promise.allSettled([
      getAlarmUnhandledCount(),
      getStreamOverview(),
      getStreamList(),
    ]);

    if (alarmRes.status === 'fulfilled') {
      overview.value.pendingAlarms = Number(alarmRes.value) || 0;
    }

    if (overviewRes.status === 'fulfilled') {
      const o: any = overviewRes.value || {};
      overview.value.onlineDevices = Number(o.onlineDevices ?? o.onlineDeviceCount ?? 0);
      overview.value.totalDevices = Number(o.totalDevices ?? o.totalDeviceCount ?? 0);
      overview.value.activeStreams = Number(o.activeStreams ?? o.streamCount ?? 0);
      overview.value.maxStreams = Number(o.maxStreams ?? o.maxStreamCount ?? 0);
      overview.value.mediaNodes = Number(o.mediaNodes ?? o.mediaServerCount ?? 0);
      overview.value.mediaNodesOnline = Number(o.mediaNodesOnline ?? o.onlineMediaServerCount ?? 0);
    }

    if (streamRes.status === 'fulfilled') {
      streams.value = Array.isArray(streamRes.value) ? streamRes.value : [];
      if (!overview.value.activeStreams) {
        overview.value.activeStreams = streams.value.length;
      }
    }

    loading.value = false;
  }

  return { overview, streams, loading, load };
}
