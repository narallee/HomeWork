/**
 * 工具函数集合
 */

// 格式化日期
function formatDate(date, fmt = 'YYYY-MM-DD') {
  if (typeof date === 'string') date = new Date(date);
  const map = {
    YYYY: date.getFullYear(),
    MM: String(date.getMonth() + 1).padStart(2, '0'),
    DD: String(date.getDate()).padStart(2, '0'),
    HH: String(date.getHours()).padStart(2, '0'),
    mm: String(date.getMinutes()).padStart(2, '0'),
    ss: String(date.getSeconds()).padStart(2, '0'),
  };
  let result = fmt;
  Object.keys(map).forEach((key) => {
    result = result.replace(key, map[key]);
  });
  return result;
}

// 格式化金额
function formatAmount(amount) {
  return parseFloat(amount).toFixed(2);
}

// 防抖
function debounce(fn, delay = 300) {
  let timer = null;
  return function (...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}

// 显示加载
function showLoading(title = '加载中...') {
  wx.showLoading({ title, mask: true });
}

// 隐藏加载
function hideLoading() {
  wx.hideLoading();
}

// 显示提示
function showToast(title, icon = 'none') {
  wx.showToast({ title, icon, duration: 2000 });
}

module.exports = { formatDate, formatAmount, debounce, showLoading, hideLoading, showToast };
