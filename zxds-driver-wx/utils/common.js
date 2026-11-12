// ============ 服务器地址 ============
// IP地址：如果要支持真机调试的话，IP地址必须和手机的IP地址是同一个网段的。
export const baseUrl = "http://192.168.1.5:8201/zxds-driver"

// ============ 接口地址 ============
export const url = {
	registerNewDriver: `${baseUrl}/driver`,
	recognizeAndUpload: `${baseUrl}/ocr`,
	updateDriverAuth: `${baseUrl}/driver`,
	createDriverFaceModel: `${baseUrl}/driver/face-model`,
	verificateDriverFace: `${baseUrl}/driver/face-recognition`,
	login: `${baseUrl}/driver/token`,
	logout: `${baseUrl}/driver/logout`,
	searchDriverBaseInfo: `${baseUrl}/driver/base-info`,
	searchWorkbenchData: `${baseUrl}/driver/workbench-data`,
	searchDriverAuth: `${baseUrl}/driver/auth`,
	updateDriverLocation: `${baseUrl}/driver/location`,
	startWork: `${baseUrl}/driver/work/start`,
	stopWork: `${baseUrl}/driver/work/stop`,
	receiveNewOrderMessage: `${baseUrl}/message/new-orders/received`,
	acceptNewOrder: `${baseUrl}/order/driver`,
	searchDriverExecuteOrder: `${baseUrl}/order/driver/execute-order`,
	searchDriverCurrentOrder: `${baseUrl}/order/driver/current`,
	searchOrderForMoveById: `${baseUrl}/order/move-info`,
	updateOrderLocation: `${baseUrl}/driver/location/order-location`,
	arriveStartPlace: `${baseUrl}/order/arrive`,
	startDriving: `${baseUrl}/order/start-driving`,
	uploadRecordFile: `${baseUrl}/monitoring/uploadRecordFile`,
	updateOrderStatus: `${baseUrl}/order/status`,
	saveOrderGps: `${baseUrl}/order/gps`,
	updateBillFee: `${baseUrl}/order/bill`,
	searchReviewDriverOrderBill: `${baseUrl}/order/driver-orders/review`,
	searchOrderStatus: `${baseUrl}/order/status`,
	updateOrderAboutPayment: `${baseUrl}/order/payment-status-sync`,
}

// ============ 腾讯地图 ============
export const tencent = {
	map: {
		referer: "智行代驾",
		key: "KU2BZ-ITHKJ-LAKF2-DUAQX-RRRYQ-U2FN7"
	}
}

// ============ ajax ============
/**
 * 通用 ajax 请求方法
 * @param {string} apiUrl   - 请求地址（完整 URL）
 * @param {string} method   - 请求方式（'GET' / 'POST' 等）
 * @param {object} data     - 请求参数
 * @param {function} fun    - 成功回调（业务 code == 200 时触发），参数为完整响应对象 resp
 * @param {boolean} [load]  - 是否显示 loading，默认 true；传 false 时不显示
 *
 * 说明：
 *   - 自动带上本地 token（从 storage 读取）
 *   - 401 自动跳转登录页
 *   - code == 200 时，若返回体里有 token 会自动更新本地 token，然后执行 fun(resp)
 *   - 微信无法注册 / 其他异常统一 toast 提示
 */
export function ajax(apiUrl, method, data, fun, load) {
	let timer = null

	// 是否显示 loading：load 为 true 或未传时显示
	if (load == true || load == undefined) {
		uni.showLoading({
			title: "执行中"
		})
		// 60 秒超时保护，避免 loading 一直转
		timer = setTimeout(function() {
			uni.hideLoading({
				fail: () => {}
			})
		}, 60 * 1000)
	}

	uni.request({
		url: apiUrl,
		method: method,
		// 请求头带上 token，供后端鉴权
		header: {
			token: uni.getStorageSync("token")
		},
		data: data,
		success: function(resp) {
			// 请求成功回来，先关掉 loading
			if (load == true || load == undefined) {
				clearTimeout(timer)
				uni.hideLoading({
					fail: () => {}
				})
			}

			if (resp.statusCode == 401) {
				// 401：登录态失效，跳转登录页
				uni.redirectTo({
					url: "/pages/login/login"
				})
			} else if (resp.statusCode == 200 && resp.data.code == 200) {
				// 业务成功
				let data = resp.data
				// 后端如果下发了新 token，同步更新到本地
				if (data.hasOwnProperty("token")) {
					uni.setStorageSync("token", data.token)
				}
				// 执行调用方传入的成功回调
				fun(resp)
			} else if (resp.data.error == "该微信无法注册") {
				// 业务特殊错误：该微信无法注册
				uni.showToast({
					icon: "none",
					title: "该微信无法注册"
				})
			} else {
				// 其他业务异常，统一提示
				uni.showToast({
					icon: "none",
					title: "执行异常"
				})
				console.error(resp.data)
			}
		},
		fail: function(error) {
			// 请求失败（网络错误等），关掉 loading
			if (load == true || load == undefined) {
				clearTimeout(timer)
				uni.hideLoading({
					fail: () => {}
				})
			}
		}
	})
}

// ============ 刷新消息 ============
export function refreshMessage(apiUrl) {
	uni.request({
		url: apiUrl,
		method: "POST",
		header: {
			token: uni.getStorageSync("token")
		},
		data: {
			identity: 'driver'
		},
		success: function(resp) {
			if (resp.statusCode == 401) {
				uni.redirectTo({
					url: "/pages/login/login"
				})
			} else if (resp.statusCode == 200 && resp.data.code == 200) {
				uni.$emit("updateMessageService", true)
				let result = resp.data.result
				if (result.lastRows > 0) {
					uni.$emit("showMessageTip", result.lastRows)
				}
			} else {
				console.error(resp.data)
				uni.$emit("updateMessageService", false)
			}
		},
		fail: function(error) {
			uni.$emit("updateMessageService", false)
		}
	})
}

// ============ 上传到 COS ============
export function uploadCos(apiUrl, path, module, fun) {
	uni.uploadFile({
		url: apiUrl,
		filePath: path,
		name: "file",
		header: {
			token: uni.getStorageSync("token")
		},
		formData: {
			module: module
		},
		success: function(resp) {
			let data = JSON.parse(resp.data)
			if (resp.statusCode == 401) {
				uni.redirectTo({
					url: "/pages/login/login"
				})
			} else if (resp.statusCode == 200 && data.code == 200) {
				fun(resp)
			} else {
				uni.showToast({
					icon: "none",
					title: data.error
				})
			}
		}
	})
}

// ============ 通用上传 ============
export function upload(apiUrl, path, data, fun) {
	uni.uploadFile({
		url: apiUrl,
		filePath: path,
		name: "file",
		header: {
			token: uni.getStorageSync("token")
		},
		formData: data,
		success: function(resp) {
			let data = JSON.parse(resp.data)
			if (resp.statusCode == 401) {
				uni.redirectTo({
					url: "/pages/login/login"
				})
			} else if (resp.statusCode == 200 && data.code == 200) {
				fun(resp)
			} else {
				uni.showToast({
					icon: "none",
					title: data.error
				})
			}
		}
	})
}

// ============ 页面跳转 ============
export function toPage(targetUrl) {
	uni.navigateTo({
		url: targetUrl
	})
}

// ============ 校验工具 ============
export function checkNull(uToast, data, name) {
	if (data == null) {
		uToast?.show({
			message: name + "不能为空",
			type: 'error'
		})
		return true
	}
	return false
}

export function checkBlank(uToast, data, name) {
	if (data == null || data == "") {
		uToast?.show({
			message: name + "不能为空",
			type: 'error'
		})
		return true
	}
	return false
}

export function checkValidName(uToast, data, name) {
	if (data == null || data == "") {
		uToast?.show({
			message: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^[\u4e00-\u9fa5]{2,15}$/.test(data)) {
		uToast?.show({
			message: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}

export function checkValidTel(uToast, data, name) {
	if (data == null || data == "") {
		uToast?.show({
			message: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^1[0-9]{10}$/.test(data)) {
		uToast?.show({
			message: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}

export function checkValidEmail(uToast, data, name) {
	if (data == null || data == "") {
		uToast?.show({
			message: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/.test(data)) {
		uToast?.show({
			message: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}

export function checkValidAddress(uToast, data, name) {
	if (data == null || data == "") {
		uToast?.show({
			message: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^[0-9a-zA-Z\u4e00-\u9fa5\-]{6,50}$/.test(data)) {
		uToast?.show({
			message: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}

export function checkValidFee(uToast, data, name) {
	if (data == null || data == "") {
		uToast?.show({
			message: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^[1-9]\d*\.\d{1,2}$|^0\.\d{1,2}$|^[1-9]\d*$/.test(data)) {
		uToast?.show({
			message: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}

// ============ 数字格式化 ============
export function changeNumber(value) {
	let newValue = ['', ''];
	let fr = 1000;
	let num = 3;
	while (value / fr >= 1) {
		fr *= 10;
		num += 1;
	}
	if (num <= 4) {
		newValue[1] = '千';
		newValue[0] = parseInt(value / 1000) + '';
	} else if (num <= 8) {
		const text1 = parseInt(num - 4) / 3 > 1 ? '千万' : '万';
		const fm = '万' === text1 ? 10000 : 10000000;
		newValue[1] = text1;
		newValue[0] = value / fm + '';
	} else if (num <= 16) {
		let text1 = (num - 8) / 3 > 1 ? '千亿' : '亿';
		text1 = (num - 8) / 4 > 1 ? '万亿' : text1;
		text1 = (num - 8) / 7 > 1 ? '千万亿' : text1;
		let fm = 1;
		if ('亿' === text1) fm = 100000000;
		else if ('千亿' === text1) fm = 100000000000;
		else if ('万亿' === text1) fm = 1000000000000;
		else if ('千万亿' === text1) fm = 1000000000000000;
		newValue[1] = text1;
		newValue[0] = parseInt(value / fm) + '';
	}
	if (value < 1000) {
		newValue[1] = '';
		newValue[0] = value + '';
	}
	let temp = Math.floor((newValue[0]) * 100) / 100
	return temp + newValue[1];
}
