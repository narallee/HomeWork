App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: 'https://your-domain.com', // 替换为实际后端地址
    wsUrl: 'wss://your-domain.com',     // 替换为实际WebSocket地址
  },

  onLaunch() {
    // 检查登录状态
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (token) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
    }
  },

  // 检查是否已登录
  checkLogin() {
    return !!this.globalData.token;
  },

  // 退出登录
  logout() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    wx.reLaunch({ url: '/pages/login/login' });
  },
});
