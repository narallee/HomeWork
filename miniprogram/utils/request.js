const app = getApp();

/**
 * 封装HTTP请求
 */
function request(url, options = {}) {
  const { method = 'GET', data, header = {} } = options;
  const baseUrl = app.globalData.baseUrl;
  const token = app.globalData.token;

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
        ...header,
      },
      success(res) {
        if (res.statusCode === 401) {
          // token过期，跳转登录
          app.logout();
          reject(new Error('登录已过期'));
          return;
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
        } else {
          reject(res.data);
        }
      },
      fail(err) {
        reject(err);
      },
    });
  });
}

function get(url, data) {
  return request(url, { method: 'GET', data });
}

function post(url, data) {
  return request(url, { method: 'POST', data });
}

function put(url, data) {
  return request(url, { method: 'PUT', data });
}

function del(url, data) {
  return request(url, { method: 'DELETE', data });
}

module.exports = { request, get, post, put, del };
