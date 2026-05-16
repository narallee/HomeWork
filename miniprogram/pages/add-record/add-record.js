const { get, post } = require('../../utils/request');
const { formatDate, showLoading, hideLoading, showToast } = require('../../utils/util');

Page({
  data: {
    ledgerId: '',
    type: 'expense', // expense / income
    amount: '',
    description: '',
    categoryId: '',
    date: '',
    categories: [],
    selectedCategoryIndex: -1,
  },

  onLoad(options) {
    this.setData({
      ledgerId: options.ledgerId,
      date: formatDate(new Date()),
    });
    this.loadCategories();
  },

  async loadCategories() {
    try {
      const res = await get('/api/categories', { type: this.data.type });
      if (res.code === 0) {
        this.setData({ categories: res.data || [], selectedCategoryIndex: -1, categoryId: '' });
      }
    } catch (error) {
      console.error('加载分类失败:', error);
    }
  },

  // 切换收入/支出
  switchType(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({ type });
    this.loadCategories();
  },

  // 选择分类
  selectCategory(e) {
    const index = e.currentTarget.dataset.index;
    const category = this.data.categories[index];
    this.setData({ selectedCategoryIndex: index, categoryId: category.id });
  },

  onAmountInput(e) {
    this.setData({ amount: e.detail.value });
  },

  onDescInput(e) {
    this.setData({ description: e.detail.value });
  },

  onDateChange(e) {
    this.setData({ date: e.detail.value });
  },

  // 提交记录
  async submit() {
    const { ledgerId, amount, type, categoryId, description, date } = this.data;

    if (!amount || parseFloat(amount) <= 0) {
      showToast('请输入有效金额');
      return;
    }

    try {
      showLoading('保存中...');
      const res = await post('/api/records', {
        ledgerId,
        amount: parseFloat(amount),
        type,
        categoryId: categoryId || undefined,
        description,
        date,
      });

      if (res.code === 0) {
        showToast('添加成功');
        setTimeout(() => wx.navigateBack(), 500);
      } else {
        showToast(res.message);
      }
    } catch (error) {
      showToast('添加失败');
    } finally {
      hideLoading();
    }
  },
});
