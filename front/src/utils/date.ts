// utils/date.ts

/**
 * 将 ISO 时间格式（如 2026-01-23T02:43:18）转换为 yyyy-mm-dd hh:mm:ss 格式
 * @param isoDate - 带 T 分隔符的 ISO 时间字符串
 * @returns 格式化后的时间字符串，格式为 yyyy-mm-dd hh:mm:ss；若输入无效返回空字符串
 */
export function formatIsoDate(isoDate: string): string {
  // 简单校验输入是否为空
  if (!isoDate) return '';

  try {
    // 核心逻辑：替换 T 为空格，同时兼容带时区的完整 ISO 格式（如 2026-01-23T02:43:18+08:00）
    const formattedDate = isoDate.replace('T', ' ').split('+')[0];
    return formattedDate;
  } catch (error) {
    console.error('时间格式化失败：', error);
    return '';
  }
}