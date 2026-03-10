// index.js
Page({
  onLoad() {
    // 首页重定向到工作台
    wx.redirectTo({
      url: '/pages/workbench/workbench'
    });
  }
})
