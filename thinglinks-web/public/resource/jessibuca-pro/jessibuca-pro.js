/**
 * Jessibuca Pro 适配层
 * 基于标准版 Jessibuca 封装，暴露为 window.JessibucaPro。
 * 标准版由 JessibucaPlayer 组件保证先于本文件加载。
 * 如后续购买正式 Pro 授权，直接替换本文件和 decoder-pro.js 即可。
 */
(function (global) {
  var BaseClass = global.Jessibuca;
  if (!BaseClass) {
    console.error('[JessibucaPro Adapter] Standard Jessibuca not found');
    return;
  }

  function JessibucaPro(options) {
    var proDefaults = {
      useWCS: true,
      useMSE: true,
      isResize: true,
      isFullResize: true,
      forceNoOffscreen: true,
      supportDblclickFullscreen: true,
    };
    var merged = Object.assign({}, proDefaults, options);
    // 适配层使用标准版 decoder
    if (merged.decoder && merged.decoder.indexOf('decoder-pro') !== -1) {
      merged.decoder = '/jessibuca/decoder.js';
    }
    return new BaseClass(merged);
  }

  // 复制静态属性（如 EVENTS）
  Object.keys(BaseClass).forEach(function (key) {
    JessibucaPro[key] = BaseClass[key];
  });

  JessibucaPro._isProAdapter = true;
  JessibucaPro.version = (BaseClass.version || 'unknown') + '-pro-adapter';

  global.JessibucaPro = JessibucaPro;
})(typeof globalThis !== 'undefined' ? globalThis : typeof window !== 'undefined' ? window : this);
