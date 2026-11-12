// ============ 服务器地址 ============
export const baseUrl = "http://192.168.1.5:8201/zxds-customer"

// ============ 接口地址 ============
export const url = {
	registerNewCustomer: `${baseUrl}/customer`,
	login: `${baseUrl}/customer/token`,
	insertCustomerCar: `${baseUrl}/customer/car`,
	searchCustomerCarList: `${baseUrl}/customer/car/search`,
	deleteCustomerCarById: `${baseUrl}/customer/car`,
	createNewOrder: `${baseUrl}/order`,
	searchOrderStatus: `${baseUrl}/order/status`,
	deleteUnAcceptOrder: `${baseUrl}/order/unaccepted`,
	hasCustomerCurrentOrder: `${baseUrl}/order/customer/current`,
	searchOrderForMoveById: `${baseUrl}/order/move-info`,
	searchOrderLocationCache: `${baseUrl}/order/location/cache`,
	confirmArriveStartPlace: `${baseUrl}/order/confirm-arrive`,
	receiveBillMessage: `${baseUrl}/message/bill`,
	searchOrderById: `${baseUrl}/order`,
	createWxPayment: `${baseUrl}/order/payments`,
	updateOrderAboutPayment: `${baseUrl}/order/payment-status-sync`,
}

// ============ 腾讯地图 ============
export const tencent = {
	map: {
		referer: "智行代驾", // 开发阶段随意，但是项目上线之后，这个名字必须和小程序的名字相同。
		key: "KU2BZ-ITHKJ-LAKF2-DUAQX-RRRYQ-U2FN7"
	}
}

// ============ ajax 请求 ============
export function ajax(apiUrl, method, data, fun, load) {
	let timer = null
	if (load == true || load == undefined) {
		uni.showLoading({
			title: "执行中"
		})
		timer = setTimeout(function() {
			uni.hideLoading({
				fail: () => {}
			})
		}, 60 * 1000)
	}
	uni.request({
		url: apiUrl,
		method: method,
		header: {
			token: uni.getStorageSync("token")
		},
		data: data,
		success: function(resp) {
			if (load == true || load == undefined) {
				clearTimeout(timer)
				uni.hideLoading({
					fail: () => {}
				})
			}
			if (resp.statusCode == 401) {
				uni.redirectTo({
					url: "/pages/login/login"
				})
			} else if (resp.statusCode == 200 && resp.data.code == 200) {
				let data = resp.data
				if (data.hasOwnProperty("token")) {
					uni.setStorageSync("token", data.token)
				}
				fun(resp)
			} else {
				uni.showToast({
					icon: "none",
					title: "执行异常"
				})
				console.error(resp.data)
			}
		},
		fail: function(error) {
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

export function checkValidCarType(uToast, data, name) {
	if (data == null || data == "") {
		uToast?.show({
			message: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^[\u4e00-\u9fa5A-Za-z0-9\-\_\s]{2,20}$/.test(data)) {
		uToast?.show({
			message: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}

export function checkValidCarPlate(uToast, data, name) {
	if (data == null || data == "") {
		uToast?.show({
			message: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!
		/^([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1})$/
		.test(data)) {
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
