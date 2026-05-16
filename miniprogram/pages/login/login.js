const { post } = require('../../utils/request');
const { showLoading, hideLoading, showToast } = require('../../utils/util');

const app = getApp();

Page({
  data: {
    logging: false,
  },

  onLoad() {
    // 如果已登录，直接跳转首页
    if (app.checkLogin()) {
      wx.switchTab({ url: '/pages/index/index' });
    }
  },

  // 微信一键登录
  async handleLogin() {
    if (this.data.logging) return;
    this.setData({ logging: true });

    try {
      showLoading('登录中...');

      // 获取微信登录code
      const loginRes = await new Promise((resolve, reject) => {
        wx.login({
          success: resolve,
          fail: reject,
        });
      });

      if (!loginRes.code) {
        showToast('获取登录凭证失败');
        return;
      }

      // 发送code到后端换取token
      const res = await post('/api/user/login', { code: loginRes.code });

      if (res.code === 0) {
        // 存储登录信息
        app.globalData.token = res.data.token;
        wx.setStorageSync('token', res.data.token);
        wx.setStorageSync('userId', res.data.userId);

        showToast('登录成功');
        setTimeout(() => {
          wx.switchTab({ url: '/pages/index/index' });
        }, 500);
      } else {
        showToast(res.message || '登录失败');
      }
    } catch (error) {
      console.error('登录错误:', error);
      showToast('登录失败，请重试');
    } finally {
      hideLoading();
      this.setData({ logging: false });
    }
  },
});
