const { get } = require('../../utils/request');
const { showToast } = require('../../utils/util');

const app = getApp();

Page({
  data: {
    latitude: 39.9042,
    longitude: 116.4074,
    markers: [],
    sharing: false,
    ledgers: [],
    selectedLedgerId: '',
    connected: false,
  },

  socketTask: null,
  locationTimer: null,

  onLoad() {
    this.loadLedgers();
    this.getCurrentLocation();
  },

  onUnload() {
    this.stopSharing();
  },

  onHide() {
    this.stopSharing();
  },

  // 获取当前位置
  getCurrentLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.setData({
          latitude: res.latitude,
          longitude: res.longitude,
        });
      },
      fail: () => {
        showToast('获取位置失败，请授权');
      },
    });
  },

  // 加载账本列表（用于选择共享房间）
  async loadLedgers() {
    try {
      const res = await get('/api/ledgers');
      if (res.code === 0) {
        this.setData({ ledgers: res.data || [] });
      }
    } catch (error) {
      console.error('加载账本失败:', error);
    }
  },

  // 选择账本
  onLedgerChange(e) {
    const index = e.detail.value;
    const ledger = this.data.ledgers[index];
    this.setData({ selectedLedgerId: ledger.id });
  },

  // 开始/停止共享
  toggleSharing() {
    if (this.data.sharing) {
      this.stopSharing();
    } else {
      this.startSharing();
    }
  },

  // 开始位置共享
  startSharing() {
    if (!this.data.selectedLedgerId) {
      showToast('请先选择一个账本');
      return;
    }

    this.connectWebSocket();
  },

  // 连接WebSocket
  connectWebSocket() {
    const wsUrl = app.globalData.wsUrl;
    const token = app.globalData.token;

    this.socketTask = wx.connectSocket({
      url: wsUrl,
      success: () => {
        console.log('WebSocket连接中...');
      },
    });

    this.socketTask.onOpen(() => {
      console.log('WebSocket已连接');
      // 发送认证消息
      this.socketTask.send({
        data: JSON.stringify({ type: 'auth', token }),
      });
    });

    this.socketTask.onMessage((res) => {
      const message = JSON.parse(res.data);
      this.handleWSMessage(message);
    });

    this.socketTask.onClose(() => {
      console.log('WebSocket已断开');
      this.setData({ connected: false, sharing: false });
      this.clearLocationTimer();
    });

    this.socketTask.onError((err) => {
      console.error('WebSocket错误:', err);
      showToast('连接失败');
      this.setData({ connected: false, sharing: false });
    });
  },

  // 处理WebSocket消息
  handleWSMessage(message) {
    switch (message.type) {
      case 'auth_success':
        this.setData({ connected: true, sharing: true });
        // 加入房间
        this.socketTask.send({
          data: JSON.stringify({ type: 'join_room', ledgerId: this.data.selectedLedgerId }),
        });
        // 开始定时上报位置
        this.startLocationUpdate();
        break;

      case 'auth_failed':
        showToast('认证失败');
        this.stopSharing();
        break;

      case 'room_locations':
        // 收到房间内其他人的位置
        this.updateMarkers(message.locations);
        break;

      case 'location_update':
        // 某个用户更新了位置
        this.updateSingleMarker(message);
        break;

      case 'user_left':
        // 用户离开
        this.removeMarker(message.userId);
        break;
    }
  },

  // 开始定时上报位置
  startLocationUpdate() {
    this.sendLocation(); // 立即发送一次
    this.locationTimer = setInterval(() => {
      this.sendLocation();
    }, 5000); // 每5秒更新一次
  },

  // 发送当前位置
  sendLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.setData({ latitude: res.latitude, longitude: res.longitude });
        if (this.socketTask && this.data.connected) {
          const userInfo = app.globalData.userInfo || {};
          this.socketTask.send({
            data: JSON.stringify({
              type: 'update_location',
              lat: res.latitude,
              lng: res.longitude,
              nickName: userInfo.nickName || '我',
              avatarUrl: userInfo.avatarUrl || '',
            }),
          });
        }
      },
    });
  },

  // 更新所有标记
  updateMarkers(locations) {
    const markers = locations.map((loc, index) => ({
      id: index,
      latitude: loc.lat,
      longitude: loc.lng,
      title: loc.nickName || '队友',
      callout: {
        content: loc.nickName || '队友',
        display: 'ALWAYS',
        fontSize: 12,
        borderRadius: 4,
        padding: 4,
        bgColor: '#4A90D9',
        color: '#fff',
      },
      width: 32,
      height: 32,
    }));
    this.setData({ markers });
  },

  // 更新单个标记
  updateSingleMarker(data) {
    const { markers } = this.data;
    const existingIndex = markers.findIndex((m) => m.title === data.nickName);

    const marker = {
      id: existingIndex >= 0 ? markers[existingIndex].id : markers.length,
      latitude: data.lat,
      longitude: data.lng,
      title: data.nickName || '队友',
      callout: {
        content: data.nickName || '队友',
        display: 'ALWAYS',
        fontSize: 12,
        borderRadius: 4,
        padding: 4,
        bgColor: '#4A90D9',
        color: '#fff',
      },
      width: 32,
      height: 32,
    };

    if (existingIndex >= 0) {
      markers[existingIndex] = marker;
    } else {
      markers.push(marker);
    }
    this.setData({ markers });
  },

  // 移除标记
  removeMarker(userId) {
    // 简化处理，实际应该根据userId匹配
    showToast('有成员离开了');
  },

  // 停止共享
  stopSharing() {
    this.clearLocationTimer();
    if (this.socketTask) {
      this.socketTask.send({
        data: JSON.stringify({ type: 'leave_room' }),
        fail: () => {},
      });
      this.socketTask.close();
      this.socketTask = null;
    }
    this.setData({ sharing: false, connected: false, markers: [] });
  },

  clearLocationTimer() {
    if (this.locationTimer) {
      clearInterval(this.locationTimer);
      this.locationTimer = null;
    }
  },
});
