const { get, put } = require('../../utils/request');
const { showToast, showLoading, hideLoading } = require('../../utils/util');

const app = getApp();

Page({
  data: {
    userInfo: null,
    hasUserInfo: false,
  },

  onShow() {
    this.loadUserInfo();
  },

  async loadUserInfo() {
    try {
      const res = await get('/api/user/info');
      if (res.code === 0) {
        this.setData({ userInfo: res.data, hasUserInfo: true });
        app.globalData.userInfo = res.data;
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
    }
  },

  // 更新用户头像和昵称
  async updateProfile() {
    try {
      const profileRes = await new Promise((resolve, reject) => {
        wx.getUserProfile({
          desc: '用于完善用户信息',
          success: resolve,
          fail: reject,
        });
      });

      const { nickName, avatarUrl } = profileRes.userInfo;
      showLoading('更新中...');

      const res = await put('/api/user/info', { nickName, avatarUrl });
      if (res.code === 0) {
        this.setData({
          userInfo: { ...this.data.userInfo, nick_name: nickName, avatar_url: avatarUrl },
        });
        app.globalData.userInfo = { ...app.globalData.userInfo, nickName, avatarUrl };
        wx.setStorageSync('userInfo', app.globalData.userInfo);
        showToast('更新成功');
      }
    } catch (error) {
      if (error.errMsg && error.errMsg.includes('cancel')) return;
      showToast('更新失败');
    } finally {
      hideLoading();
    }
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success(res) {
        if (res.confirm) {
          app.logout();
        }
      },
    });
  },
});
