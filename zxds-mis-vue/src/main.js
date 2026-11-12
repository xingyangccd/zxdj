import { createApp } from 'vue'
import App from './App.vue'

// SVG 图标注册
import 'virtual:svg-icons-register'

// jQuery（用于 Ajax）
import $ from 'jquery'

// 路由
import router from './router'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 消息组件
import { ElMessage } from 'element-plus'

// Cookies
import VueCookies from 'vue3-cookies'

// PDF 导出
import html2Canvas from 'html2canvas'
import JsPDF from 'jspdf'

// ECharts
import * as echarts from 'echarts'

const app = createApp(App)

// 挂载路由
app.use(router)

// 挂载 Cookies
app.use(VueCookies)

// 挂载 Element Plus
app.use(ElementPlus, { locale: zhCn })

// 注册 Element Plus 图标（全局）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// ---------- 全局属性 ----------
// PDF 导出
app.config.globalProperties.getPdf = function () {
  const title = this.htmlTitle
  html2Canvas(document.querySelector('#pdfDom'), {
    allowTaint: true,
    taintTest: false,
    useCORS: true,
    dpi: window.devicePixelRatio * 4,
    scale: 4
  }).then(function (canvas) {
    const contentWidth = canvas.width
    const contentHeight = canvas.height
    const pageHeight = (contentWidth / 592.28) * 841.89
    let leftHeight = contentHeight
    let position = 0
    const imgWidth = 595.28
    const imgHeight = (592.28 / contentWidth) * contentHeight
    const pageData = canvas.toDataURL('image/jpeg', 1.0)
    const PDF = new JsPDF('', 'pt', 'a4')
    if (leftHeight < pageHeight) {
      PDF.addImage(pageData, 'JPEG', 0, 0, imgWidth, imgHeight)
    } else {
      while (leftHeight > 0) {
        PDF.addImage(pageData, 'JPEG', 0, position, imgWidth, imgHeight)
        leftHeight -= pageHeight
        position -= 841.89
        if (leftHeight > 0) PDF.addPage()
      }
    }
    PDF.save(title + '.pdf')
  })
}

// ECharts
app.config.globalProperties.$echarts = echarts

// 后端 URL
let baseUrl = "http://localhost:8201/zxds-mis-api/";
app.config.globalProperties.$baseUrl = baseUrl

// 全局 Ajax 封装
app.config.globalProperties.$http = function (url, method, data, async, fun) {
  $.ajax({
    url: baseUrl + url,
    type: method,
    dataType: 'json',
    contentType: 'application/json',
    xhrFields: { withCredentials: true },
    headers: { token: localStorage.getItem('token') },
    async: async,
    data: data ? JSON.stringify(data) : null,
    success: function (resp) {
      if (resp.code === 200) {
        fun(resp)
      } else {
        ElMessage.error({ message: resp.msg, duration: 1200 })
      }
    },
    error: function (e) {
      if (e.status === undefined) {
        ElMessage.error({ message: '前端页面错误', duration: 1200 })
      } else if (e.status === 401) {
        router.push({ name: 'Login' })
      } else {
        ElMessage.error({ message: e.responseText, duration: 1200 })
      }
    }
  })
}

// 权限判断
app.config.globalProperties.isAuth = function (permission) {
  const permissions = localStorage.getItem('permissions')
  if (!permissions) return false
  for (const one of permission) {
    if (permissions.includes(one)) return true
  }
  return false
}

app.mount('#app')