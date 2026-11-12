import $ from 'jquery'
import {
	ElMessage
} from 'element-plus'
import router from '@/router'

export const baseUrl = 'http://localhost:8201/zxds-mis-api/'

export function http(url, method, data, async, callback) {
	$.ajax({
		url: baseUrl + url,
		type: method,
		dataType: 'json',
		contentType: 'application/json',
		xhrFields: {
			withCredentials: true
		},
		headers: {
			token: localStorage.getItem('token')
		},
		async,
		data: data ? JSON.stringify(data) : null,
		success: (resp) => {
			if (resp.code === 200) {
				callback(resp)
			} else {
				ElMessage.error({
					message: resp.msg,
					duration: 1200
				})
			}
		},
		error: (e) => {
			if (e.status === undefined) {
				ElMessage.error({
					message: '前端页面错误',
					duration: 1200
				})
			} else if (e.status === 401) {
				router.push({
					name: 'Login'
				})
			} else {
				ElMessage.error({
					message: e.responseText,
					duration: 1200
				})
			}
		}
	})
}