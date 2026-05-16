const { get, put, del } = require('../../utils/request');
const { formatDate, showLoading, hideLoading, showToast } = require('../../utils/util');

Page({
  data: {
    recordId: '',
    record: null,
    type: 'expense',
    amount: '',
    description: '',
    categoryId: '',
    date: '',
    categories: [],
    selectedCategoryIndex: -1,
  },

  onLoad(options) {
    this.setData({ recordId: options.recordId });
    this.loadRecord();
  },

  async loadRecord() {
    // 这里简化处理，实际可以通过recordId获取记录详情
    // 由于我们的API设计中没有单独获取记录的接口，实际使用时可以从上一页传参
    // 这里先加载分类
    await this.loadCategories();
  },

  async loadCategories() {
    try {
      const res = await get('/api/categories', { type: this.data.type });
      if (res.code === 0) {
        this.setData({ categories: res.data || [] });
      }
    } catch (error) {
      console.error('加载分类失败:', error);
    }
  },

  switchType(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({ type });
    this.loadCategories();
  },

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

  // 更新记录
  async submit() {
    const { recordId, amount, type, categoryId, description, date } = this.data;

    if (!amount || parseFloat(amount) <= 0) {
      showToast('请输入有效金额');
      return;
    }

    try {
      showLoading('保存中...');
      const res = await put(`/api/records/${recordId}`, {
        amount: parseFloat(amount),
        type,
        categoryId: categoryId || undefined,
        description,
        date,
      });

      if (res.code === 0) {
        showToast('更新成功');
        setTimeout(() => wx.navigateBack(), 500);
      } else {
        showToast(res.message);
      }
    } catch (error) {
      showToast('更新失败');
    } finally {
      hideLoading();
    }
  },

  // 删除记录
  async deleteRecord() {
    const that = this;
    wx.showModal({
      title: '确认删除',
      content: '删除后不可恢复，确定要删除吗？',
      async success(modalRes) {
        if (modalRes.confirm) {
          try {
            showLoading('删除中...');
            const res = await del(`/api/records/${that.data.recordId}`);
            if (res.code === 0) {
              showToast('删除成功');
              setTimeout(() => wx.navigateBack(), 500);
            } else {
              showToast(res.message);
            }
          } catch (error) {
            showToast('删除失败');
          } finally {
            hideLoading();
          }
        }
      },
    });
  },
});
