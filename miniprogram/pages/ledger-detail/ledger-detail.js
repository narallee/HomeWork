const { get, post } = require('../../utils/request');
const { formatDate, formatAmount, showToast, showLoading, hideLoading } = require('../../utils/util');

Page({
  data: {
    ledgerId: '',
    ledger: null,
    records: [],
    page: 1,
    pageSize: 20,
    total: 0,
    hasMore: true,
    loading: false,
    inviteCode: '',
  },

  onLoad(options) {
    this.setData({ ledgerId: options.id });
    this.loadDetail();
    this.loadRecords();
  },

  async loadDetail() {
    try {
      const res = await get(`/api/ledgers/${this.data.ledgerId}`);
      if (res.code === 0) {
        this.setData({ ledger: res.data });
      }
    } catch (error) {
      showToast('加载失败');
    }
  },

  async loadRecords(loadMore = false) {
    if (this.data.loading) return;
    const page = loadMore ? this.data.page + 1 : 1;

    this.setData({ loading: true });
    try {
      const res = await get(`/api/records/ledger/${this.data.ledgerId}`, {
        page,
        pageSize: this.data.pageSize,
      });
      if (res.code === 0) {
        const { list, total } = res.data;
        const records = loadMore ? [...this.data.records, ...list] : list;
        this.setData({
          records,
          total,
          page,
          hasMore: records.length < total,
        });
      }
    } catch (error) {
      showToast('加载失败');
    } finally {
      this.setData({ loading: false });
    }
  },

  // 生成邀请码
  async generateCode() {
    try {
      showLoading('生成中...');
      const res = await post(`/api/ledgers/${this.data.ledgerId}/invite-code`);
      if (res.code === 0) {
        this.setData({ inviteCode: res.data.inviteCode });
        wx.showModal({
          title: '邀请码',
          content: `邀请码: ${res.data.inviteCode}\n分享给好友即可加入账本`,
          showCancel: true,
          cancelText: '关闭',
          confirmText: '复制',
          success(modalRes) {
            if (modalRes.confirm) {
              wx.setClipboardData({ data: res.data.inviteCode });
            }
          },
        });
      }
    } catch (error) {
      showToast('生成失败');
    } finally {
      hideLoading();
    }
  },

  goToAdd() {
    wx.navigateTo({ url: `/pages/add-record/add-record?ledgerId=${this.data.ledgerId}` });
  },

  goToEdit(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/edit-record/edit-record?recordId=${id}` });
  },

  goToStatistics() {
    wx.navigateTo({ url: `/pages/statistics/statistics?ledgerId=${this.data.ledgerId}` });
  },

  onReachBottom() {
    if (this.data.hasMore) {
      this.loadRecords(true);
    }
  },

  onPullDownRefresh() {
    Promise.all([this.loadDetail(), this.loadRecords()]).then(() => {
      wx.stopPullDownRefresh();
    });
  },

  formatDate,
  formatAmount,
});
