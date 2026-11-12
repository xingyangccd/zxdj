<script setup>
	import {
		onLaunch,
		onShow,
		onHide
	} from '@dcloudio/uni-app'

	import {
		url,
		ajax
	} from '@/utils/common.js'

	// 仅在小程序启动时执行一次。
	onLaunch(() => {
		// #ifdef MP-WEIXIN
		if (wx.requirePrivacyAuthorize) {
			wx.requirePrivacyAuthorize({
				success: () => {
					console.log('隐私授权成功')
				},
				fail: () => {
					console.log('用户拒绝隐私协议')
				}
			})
		}
		// #endif

		// 后面会用在这个数组，用来缓存GPS定位
		// 例如：GPS定位达到20个，再向服务器发送一次请求，降低服务器的压力。
		const gps = []

		// 保存上一次上报定位的时间戳（上一次上报GPS定位的那个时间记录一下）
		let lastUpdateTime = 0;

		// 司机端一般是要求手机屏幕常亮
		uni.setKeepScreenOn({
			keepScreenOn: true
		})

		// 开启GPS定位功能
		uni.startLocationUpdate({
			success(resp) {
				console.log("开启定位成功")
			},
			fail(err) {
				console.error("开启定位失败")
				// 需要触发事件，通知应用内的其他组件定位失败了。
				// 工作台页面显示定位失败
				uni.$emit("updateLocation", null);
			}
		})

		// 绑定回调函数，定位发生变化之后，就调用这个回调函数。
		uni.onLocationChange((resp) => {
			// 获取经纬度
			const latitude = resp.latitude;
			const longitude = resp.longitude;
			// 获取司机的移动速度,后面业务要使用
			const speed = resp.speed;

			// 获取司机的当前的工作状态，例如：开始接单/开始代驾
			let workStatus = uni.getStorageSync("workStatus");
			// TODO: 测试阶段先写死，以后记得删除
			//workStatus = "开始接单";

			if (workStatus === "开始接单") {
				// 距上次上报不足10秒，不上报（每10秒至少上报一次，不管位置是否变化）
				const now = Date.now(); // 系统当前的时间戳（毫秒）
				if (now - lastUpdateTime >= 10000) {
					// 记录一下上一次上报的时间戳.
					lastUpdateTime = now;

					// 司机的设置信息也存储在storage当中
					let settings = uni.getStorageSync("settings");
					// TODO: 先暂时写死，以后注释掉
					settings = {
						orderDistance: 0,
						rangeDistance: 5,
						orientation: ""
					};

					// 准备发送ajax请求之前的数据
					const orderDistance = settings.orderDistance; // 订单的里程
					const rangeDistance = settings.rangeDistance; // 几公里范围以内的订单
					const orientation = settings.orientation; // 定向接单坐标

					// 发送ajax请求上传定位
					ajax(url.updateDriverLocation, "PUT", {
						latitude: latitude,
						longitude: longitude,
						orderDistance: orderDistance,
						rangeDistance: rangeDistance,
						orientateLongitude: orientation !== "" ? orientation.longitude : null,
						orientatelatitude: orientation !== "" ? orientation.latitude : null
					}, () => {
						console.log("定位更新成功")
					}, false)
				}
			} else if (workStatus === '接客户') {
				// 每10秒上传一次GPS定位
				const now = Date.now();
				if (now - lastUpdateTime >= 10000) {
					lastUpdateTime = now;

					// 获取正在执行的订单
					const executeOrder = uni.getStorageSync('executeOrder')
					if (executeOrder && executeOrder.id) {
						// 准备数据
						const data = {
							orderId: executeOrder.id,
							latitude: latitude,
							longitude: longitude
						}

						// 发送ajax请求，上传司机的GPS定位信息到Redis当中
						ajax(url.updateOrderLocation, 'PUT', data, () => {
							console.log('订单定位缓存更新成功')
						}, false)
					}
				}
			} else if (workStatus === "开始代驾") {
				console.log("当前收集到的GPS定位点数量：", gps.length)
				// 每凑够20个GPS定位就上传一次，减少服务器的压力。
				// 获取正在执行的订单
				const executeOrder = uni.getStorageSync('executeOrder');
				if (executeOrder && executeOrder.id) {
					// 收集GPS定位点
					gps.push({
						orderId: executeOrder.id,
						customerId: executeOrder.customerId,
						latitude: latitude,
						longitude: longitude,
						speed: speed
					})

					// 每凑够20个GPS定位点，发送一次AJAX请求。
					if (gps.length >= 20) {
						// 复制一份数据用于上传，避免上传过程中gps数组中的数据错乱
						const uploadData = [...gps]

						const data = {
							list: uploadData
						}

						ajax(url.saveOrderGps, 'POST', data, () => {
							console.log('GPS定位上传成功')
						}, false)

						// 清空本地缓存数组
						gps.length = 0
					}
				}
			}

			// 触发自定义事件，将当前的定位发出去，通知应用内其他组件位置发生变化了。
			const location = {
				latitude,
				longitude
			};

			// 触发事件（这个代码不是事件定义，也不是事件的绑定。是让事件发生。是触发事件.）
			// 定义事件在，例如工作台页面中 uni.$on("updateLocation", ...);
			// 这个和50秒没有关系，和凑够20GPS定位没有关系。通知小程序其他组件，因为其他组件中可能需要刷新地图。
			uni.$emit("updateLocation", location);

		})
	})

	onShow(() => {
		console.log("App Show")
	})

	onHide(() => {
		console.log("App Hide")
	})
</script>

<style lang="scss">
	@import 'uview-plus/index.scss';
</style>
