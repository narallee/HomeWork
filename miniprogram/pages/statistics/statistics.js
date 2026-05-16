const { get } = require('../../utils/request');
const { showToast } = require('../../utils/util');

Page({
  data: {
    ledgerId: '',
    typeStat: [],
    categoryStat: [],
    memberStat: [],
    dailyStat: [],
    totalIncome: '0.00',
    totalExpense: '0.00',
    dateRange: 'month', // month / year / all
  },

  onLoad(options) {
    this.setData({ ledgerId: options.ledgerId });
    this.loadStatistics();
  },

  async loadStatistics() {
    try {
      const { ledgerId, dateRange } = this.data;
      const params = {};

      // 计算日期范围
      const now = new Date();
      if (dateRange === 'month') {
        params.startDate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;
        params.endDate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()}`;
      } else if (dateRange === 'year') {
        params.startDate = `${now.getFullYear()}-01-01`;
        params.endDate = `${now.getFullYear()}-12-31`;
      }

      const res = await get(`/api/records/statistics/${ledgerId}`, params);
      if (res.code === 0) {
        const { typeStat, categoryStat, memberStat, dailyStat } = res.data;

        // 计算总收支
        let totalIncome = '0.00';
        let totalExpense = '0.00';
        typeStat.forEach((item) => {
          if (item.type === 'income') totalIncome = parseFloat(item.total).toFixed(2);
          if (item.type === 'expense') totalExpense = parseFloat(item.total).toFixed(2);
        });

        this.setData({
          typeStat,
          categoryStat,
          memberStat,
          dailyStat,
          totalIncome,
          totalExpense,
        });
      }
    } catch (error) {
      console.error('加载统计失败:', error);
      showToast('加载失败');
    }
  },

  // 切换日期范围
  switchRange(e) {
    const range = e.currentTarget.dataset.range;
    this.setData({ dateRange: range });
    this.loadStatistics();
  },
});
