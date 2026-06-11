export type PlayerType = 'jessibuca' | 'flv' | 'hls' | 'native';

export interface PlayerProps {
  url: string;
  playerType?: PlayerType;
  autoplay?: boolean;
  muted?: boolean;
  controls?: boolean;
  width?: string | number;
  height?: string | number;
}

export interface PlayerInstance {
  play: (url?: string) => void;
  pause: () => void;
  destroy: () => void;
  setVolume: (volume: number) => void;
  fullscreen: () => void;
  screenshot: (filename?: string) => void;
}
