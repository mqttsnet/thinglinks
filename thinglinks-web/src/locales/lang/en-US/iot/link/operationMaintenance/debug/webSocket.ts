export default {
  title: 'WebSocket Debugging',
  subtitle:
    'Debug WS uplink/downlink against the device access endpoint (via gateway) — simulate device reporting and receive platform commands',

  // Connection config
  connectConfig: 'Connection',
  tenantId: 'Tenant ID',
  clientId: 'Client ID (clientId)',
  username: 'Username',
  password: 'Password',
  wsUrl: 'WS URL (via gateway, editable)',
  wsUrlHint:
    'Defaults to the device access endpoint; editing the fields above rebuilds the URL, or edit it directly',
  resetDefault: 'Reset',
  connect: 'Connect',
  disconnect: 'Disconnect',
  connected: 'Connected',
  disconnected: 'Disconnected',
  modeManual: 'Manual',
  modeDevice: 'By Device',
  device: 'Pick Device',
  devicePh: 'Search device name…',
  deviceHint:
    'Pick a product first, then a device under it; auto-fills clientId / username / password / tenant (still editable)',

  // Send
  sendFrame: 'Send Frame',
  frameDatas: 'Data Report (datas)',
  framePing: 'Heartbeat (PING)',
  messageHint:
    'Business frame has topic/payload fields; heartbeat type is PING. Defaults to a device data-report frame, editable',
  sendBtn: 'Send',

  // Message window
  receiveWindow: 'Messages',
  clear: 'Clear',
  empty: 'No messages',
  dirSent: 'SENT',
  dirRecv: 'RECV',
  dirSys: 'SYS',
  sysConnecting: 'Connecting…',
  sysOpen: 'Connection established',
  sysClose: 'Connection closed',
  sysError: 'Connection error',
  prettyJson: 'Beautify',
  copy: 'Copy',

  // Legacy keys (still used by the mqtt debug page, keep)
  send: 'Send',
  connectionStatus: 'Connection Status',
  closeConnection: 'Close Connection',
  openConnection: 'Open Connection',
  server: 'Server Address',
  serverPlaceholder: 'Content to be sent to the server',
  message: 'Message',
  record: 'Message Record',
  receivedMsg: 'Received Message',
  setting: 'Settings',
  mqttTitle: 'MQTT command is issued',
  payload:
    '// Binary data input in hexadecimal starting with 0x, string data input original string',
  pleaseEnter: 'Please enter',
};
