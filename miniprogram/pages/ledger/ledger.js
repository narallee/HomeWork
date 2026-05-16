const { get, post } = require('../../utils/request');
const { showLoading, hideLoading, showToast } = require('../../utils/util');

const app = getApp();

Page({
  data: {
    ledgers: [],
    showCreateModal: false,
    showJoinModal: false,
    newLedgerName: '',
    newLedgerDesc: '',
    inviteCode: '',
    loading: true,
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
      showToast('加载失败');
    } finally {
      this.setData({ loading: false });
    }
  },

  // 显示创建账本弹窗
  showCreate() {
    this.setData({ showCreateModal: true });
  },

  hideCreate() {
    this.setData({ showCreateModal: false, newLedgerName: '', newLedgerDesc: '' });
  },

  // 显示加入账本弹窗
  showJoin() {
    this.setData({ showJoinModal: true });
  },

  hideJoin() {
    this.setData({ showJoinModal: false, inviteCode: '' });
  },

  onNameInput(e) {
    this.setData({ newLedgerName: e.detail.value });
  },

  onDescInput(e) {
    this.setData({ newLedgerDesc: e.detail.value });
  },

  onCodeInput(e) {
    this.setData({ inviteCode: e.detail.value });
  },

  // 创建账本
  async createLedger() {
    const { newLedgerName, newLedgerDesc } = this.data;
    if (!newLedgerName.trim()) {
      showToast('请输入账本名称');
      return;
    }

    try {
      showLoading('创建中...');
      const res = await post('/api/ledgers', {
        name: newLedgerName.trim(),
        description: newLedgerDesc.trim(),
      });
      if (res.code === 0) {
        showToast('创建成功');
        this.hideCreate();
        this.loadLedgers();
      } else {
        showToast(res.message);
      }
    } catch (error) {
      showToast('创建失败');
    } finally {
      hideLoading();
    }
  },

  // 加入账本
  async joinLedger() {
    const { inviteCode } = this.data;
    if (!inviteCode.trim()) {
      showToast('请输入邀请码');
      return;
    }

    try {
      showLoading('加入中...');
      const res = await post('/api/ledgers/join', { inviteCode: inviteCode.trim() });
      if (res.code === 0) {
        showToast('加入成功');
        this.hideJoin();
        this.loadLedgers();
      } else {
        showToast(res.message);
      }
    } catch (error) {
      showToast('加入失败');
    } finally {
      hideLoading();
    }
  },

  goToDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/ledger-detail/ledger-detail?id=${id}` });
  },

  onPullDownRefresh() {
    this.loadLedgers().then(() => wx.stopPullDownRefresh());
  },
});
