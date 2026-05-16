const { get } = require('../../utils/request');
const { showToast } = require('../../utils/util');

const app = getApp();

Page({
  data: {
    ledgers: [],
    loading: true,
  },

  onLoad() {
    if (!app.checkLogin()) {
      wx.redirectTo({ url: '/pages/login/login' });
      return;
    }
  },

  onShow() {
    this.loadLedgers();
  },

  async loadLedgers() {
    try {
      this.setData({ loading: true });
      const res = await get('/api/ledgers');
      if (res.code === 0) {
        this.setData({ ledgers: res.data || [] });
      }
    } catch (error) {
      console.error('加载账本失败:', error);
      showToast('加载失败');
    } finally {
      this.setData({ loading: false });
    }
  },

  // 进入账本详情
  goToLedger(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/ledger-detail/ledger-detail?id=${id}` });
  },

  // 快速添加记录
  goToAdd(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/add-record/add-record?ledgerId=${id}` });
  },

  onPullDownRefresh() {
    this.loadLedgers().then(() => wx.stopPullDownRefresh());
  },
});
