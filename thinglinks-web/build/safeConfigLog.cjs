/** 命令行覆盖摘要只包含键名和数量，不包含配置值。 */
function formatConfigOverrideSummary(config) {
  const keys = Object.keys(config).sort();
  if (!keys.length) return '命令行参数未覆盖配置文件（0 项）';
  return `命令行参数已覆盖配置文件（${keys.length} 项；键名：${keys.join(', ')}）`;
}

module.exports = { formatConfigOverrideSummary };
