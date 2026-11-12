<template>
	<view class="page">
		<!-- 顶部地址条 -->
		<view class="address-container" :style="{ paddingTop: (statusBarHeight + 15) + 'px' }">
			<!-- 返回按钮 -->
			<view class="nav-back" @tap="goBack">‹</view>

			<view class="from">
				<text>{{ from.address }}</text>
			</view>
			<view class="dashed-line"></view>
			<view class="to">
				<text>{{ to.address }}</text>
			</view>
		</view>

		<!-- 全屏地图 -->
		<map id="map" :longitude="from.longitude" :latitude="from.latitude" :style="contentStyle" scale="12"
			:enable-traffic="false" :show-location="true" class="map" :polyline="polyline" :markers="markers"></map>

		<!-- 底部面板 -->
		<view class="panel">
			<view class="row">
				<view class="info">
					<view class="label">全程</view>
					<view class="value"><text>{{ distance }}</text>公里</view>
					<view class="label">，预计</view>
					<view class="value"><text>{{ duration }}</text>分钟</view>
				</view>
				<view class="opt" @tap="chooseCarHandle" v-if="!showCar">选择车辆</view>
				<view class="opt" @tap="chooseCarHandle" v-if="showCar">{{ carPlate }}</view>
			</view>
			<button class="btn" @tap="createOrderHandle">
				<view class="btn-bg"></view>
				<text class="btn-text">确定下单</text>
				<text class="btn-arrow">→</text>
			</button>
		</view>

		<!-- 等待接单弹窗 -->
		<up-popup :show="showPopup" mode="center" :round="20" :mask-close-able="false" @close="showPopup = false">
			<view class="popup-title">您的订单正在等待司机接单</view>
			<view class="count-down">
				<up-count-down ref="uCountDownRef" :time="timestamp * 1000" :autoStart="false" separator="zh"
					:show-hours="false" :show-border="true" bg-color="#DDF0FF" separator-color="#0083F3"
					border-color="#0D90FF" color="#0D90FF" font-size="32" @finish="countEndHandle"
					@change="countChangeHandle"></up-count-down>
			</view>
			<button class="btn" @tap="cancelHandle">
				<view class="btn-bg"></view>
				<text class="btn-text">取消订单</text>
			</button>
		</up-popup>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app'
	import {
		url,
		ajax,
		tencent
	} from '@/utils/common.js'
	import {
		nextTick
	} from 'vue'

	import QQMapWX from '@/lib/qqmap-wx-jssdk.min.js'

	let qqmapsdk = null;
	const map = ref(null)

	// 倒计时组件引用
	const uCountDownRef = ref(null)

	const from = ref({
		address: '',
		longitude: 0,
		latitude: 0
	})
	const to = ref({
		address: '',
		longitude: 0,
		latitude: 0
	})
	const contentStyle = ref('')
	const distance = ref(0)
	const duration = ref(0)
	const polyline = ref([])
	const markers = ref([])

	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)

	// 车辆相关
	const carId = ref(null)
	const carPlate = ref(null)
	const carType = ref(null)
	const showCar = ref(false)

	// 弹窗 & 订单
	const showPopup = ref(false)
	//const timestamp = ref(15 * 60) // 15 分钟，单位秒
	const timestamp = ref(60) // 测试的时候修改为1分钟，生产环境修改为15分钟。
	const orderId = ref(null)

	// ===== 生命周期 =====
	onLoad((options) => {
		// 地图高度 = 全屏
		const windowHeight = uni.getSystemInfoSync().windowHeight
		contentStyle.value = `height:${windowHeight}px;`

		// 从 storage 当中取出起点和终点
		from.value = uni.getStorageSync("from")
		to.value = uni.getStorageSync("to")

		// 创建腾讯地图的 SDK 对象
		qqmapsdk = new QQMapWX({
			key: tencent.map.key
		})

		// 初始化地图上下文
		map.value = uni.createMapContext("map");

		// 判断 options 里有没有车型和车牌
		if (options.carId && options.carPlate) {
			carId.value = options.carId
			carPlate.value = options.carPlate
			carType.value = options.carType
			showCar.value = true
		}

		// 是否显示倒计时
		if (options.showPopup) {
			setTimeout(() => {
				// 显示车牌
				showCar.value = true
				carPlate.value = options.carPlate

				// 显示倒计时窗口
				orderId.value = options.orderId
				//timestamp.value = 60 * 15
				timestamp.value = 60 // 测试阶段按照1分钟
				showPopup.value = true
				uCountDownRef.value.start()

			}, 2000)
		}

	})

	onShow(() => {

		// 每当页面展示的时候都会重新计算最佳路线。
		calculateLine()


		// 从车辆选择页返回时，取选中的车辆
		const carInfo = uni.getStorageSync('selectedCar')
		if (carInfo) {
			const car = JSON.parse(carInfo)
			carId.value = car.id
			carPlate.value = car.carPlate
			carType.value = car.carType
			showCar.value = true
			uni.removeStorageSync('selectedCar')
		}
	})

	// ===== 方法 =====

	// 返回上一页
	function goBack() {
		uni.navigateBack()
	}

	// 选择车辆
	function chooseCarHandle() {
		uni.navigateTo({
			url: '/pages/car_list/car_list'
		})
	}

	/**
	 * 确定下单流程：
	 * 1. 校验车型、车牌
	 * 2. 组装订单的数据
	 * 3. 发送ajax请求创建订单
	 * 4. 有合适的司机能够接单，弹窗，启动倒计时
	 * 5. 没有合适的司机能够接单，提示
	 */
	function createOrderHandle() {
		// 1. 校验：必须要选好车牌
		if (!carType.value || !carPlate.value) {
			uni.showToast({
				icon: "none",
				title: "请选择车辆"
			})
			return
		}

		// 提示一个加载进度条
		uni.showLoading({
			title: "下单中请稍后"
		})

		// 最多显示1分钟：防止后端超时导致 加载进度条一直不关闭的问题。
		setTimeout(() => uni.hideLoading({
			fail: () => {}
		}), 60000);

		// 2. 组装订单数据
		const data = {
			startPlace: from.value.address,
			startPlaceLatitude: from.value.latitude,
			startPlaceLongitude: from.value.longitude,
			endPlace: to.value.address,
			endPlaceLatitude: to.value.latitude,
			endPlaceLongitude: to.value.longitude,
			favourFee: '0.0',
			carPlate: carPlate.value,
			carType: carType.value
		};

		// 3.发送ajax 请求创建订单
		ajax(url.createNewOrder, "POST", data, (resp) => {
			uni.hideLoading({
				fail: () => {}
			})

			const result = resp.data.result;

			// 4. 没有合适的司机接单，需要提示一下。
			if (!result || result.count <= 0) {
				uni.showToast({
					icon: 'none',
					title: '没有合适接单的司机'
				})
				return
			}

			// 5. 有合适的司机可以接单
			uni.showToast({
				icon: 'success',
				title: '订单创建成功'
			})

			setTimeout(() => {
				orderId.value = result.orderId
				// 每一次倒计时弹窗弹出的时候，重置倒计时
				//timestamp.value = 60 * 15
				timestamp.value = 60 // 测试阶段修改为1分钟（生产环境15分钟）
				// 显示倒计时弹窗
				showPopup.value = true
				// 开始倒计时
				uCountDownRef.value.start() // 启动倒计时弹窗
			}, 2000)
		})

	}

	// 倒计时变化的回调
	function countChangeHandle(res) {
		console.log("倒计时：", res.seconds)
		if (res.seconds !== 0 && res.seconds % 5 === 0) {
			console.log("发送请求查询订单状态")
			searchOrderStatus()
		}
	}

	// 倒计时结束的回调（系统自动取消订单）
	function countEndHandle() {
		console.log("倒计时结束")
		// 倒计时结束的时候，取消订单
		deleteUnAcceptOrder()
	}

	// 取消订单
	function cancelHandle() {
		// 手动取消订单
		deleteUnAcceptOrder()
	}

	/**
	 * 调用腾讯地图API计算驾车路线
	 * 计算出：里程、时长、GPS坐标串
	 * 设置起点和终点的标记。
	 */
	function calculateLine() {
		qqmapsdk.direction({
			mode: "driving",
			from: {
				latitude: from.value.latitude,
				longitude: from.value.longitude
			},
			to: {
				latitude: to.value.latitude,
				longitude: to.value.longitude
			},
			success: (response) => {
				if (response.status !== 0) {
					uni.showToast({
						icon: "none",
						title: response.message
					})
					return;
				}

				// 取出第一条线路。
				const route = response.result.routes[0]
				// 距离（米）
				const rawDistance = route.distance;
				// 时长（分钟）
				const rawDuration = route.duration;
				// 获取路线的坐标串
				const rawPolyline = route.polyline;

				// 将距离米转换为公里，向上取整 0.1
				distance.value = Math.ceil((rawDistance / 1000) * 10) / 10
				duration.value = rawDuration

				// 把压缩的坐标串解析成坐标点数组
				const points = formatPolyline(rawPolyline)

				// 配置地图的折线
				// 地图折线配置
				polyline.value = [{
					points: points, // 路线坐标点数组
					width: 6, // 线宽
					color: '#05B473', // 线色（绿）
					arrowLine: true // 显示方向箭头
				}]

				// 地图标记点：起点 + 终点
				markers.value = [{
						id: 1, // 起点
						latitude: from.value.latitude,
						longitude: from.value.longitude,
						width: 25,
						height: 35,
						anchor: {
							x: 0.5,
							y: 0.5
						}, // 图标中心对准GPS坐标
						iconPath: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/start.png'
					},
					{
						id: 2, // 终点
						latitude: to.value.latitude,
						longitude: to.value.longitude,
						width: 25,
						height: 35,
						anchor: {
							x: 0.5,
							y: 0.5
						},
						iconPath: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/end.png'
					}
				]

				// 让地图自动缩放，把起点终点都显示在屏幕内
				map.value.includePoints({
					points: [{
							latitude: from.value.latitude,
							longitude: from.value.longitude
						},
						{
							latitude: to.value.latitude,
							longitude: to.value.longitude
						}
					],
					padding: [80, 80, 120, 80]
				})

			}
		})
	}


	/**
	 * 把腾讯地图返回的加密坐标串解析为坐标点数组
	 */
	function formatPolyline(coors) {
		for (let i = 2; i < coors.length; i++) {
			coors[i] = Number(coors[i - 2]) + Number(coors[i]) / 1000000;
		}
		const points = [];
		for (let i = 0; i < coors.length; i += 2) {
			points.push({
				latitude: coors[i],
				longitude: coors[i + 1]
			});
		}
		return points;
	}

	/**
	 * 查询订单状态（每5秒发送一次AJAX请求，看看是否有司机接单了）
	 */
	function searchOrderStatus() {
		const data = {
			orderId: orderId.value
		}

		ajax(url.searchOrderStatus, 'GET', data, (resp) => {
			if (resp.data.result === 2) {
				// 隐藏倒计时弹窗
				showPopup.value = false
				timestamp.value = null

				uni.showToast({
					icon: 'success',
					title: '司机已接单'
				})

				// 短震一下
				uni.vibrateShort({})

				setTimeout(() => {
					uni.redirectTo({
						// 跳转到 司乘同显 页面
						url: `/pages/move/move?orderId=${orderId.value}`
					})
				}, 3000)
			} else if (resp.data.result === 0) {
				showPopup.value = false
				timestamp.value = null

				uni.showToast({
					icon: 'success',
					title: '订单已关闭'
				})
			}
		}, false)
	}

	/**
	 * 取消订单（系统自动取消的或者乘客手动取消的）
	 */
	function deleteUnAcceptOrder() {
		showPopup.value = false
		timestamp.value = null
		const data = {
			orderId: orderId.value
		}

		ajax(url.deleteUnAcceptOrder, 'DELETE', data, (resp) => {
			const result = resp.data.result;
			if (result === '订单取消成功') {
				uni.showToast({
					icon: 'success',
					title: '订单已取消'
				})
				setTimeout(() => {
					// 取消订单成功跳转到工作台页面
					uni.switchTab({
						url: '/pages/workbench/workbench'
					})
				}, 3000)
			} else {
				// 取消失败，可能订单已经被接单了，重新查询状态。
				searchOrderStatus()
			}
		})
	}
</script>

<style lang="less">
	@import url('create_order.less');
</style>
