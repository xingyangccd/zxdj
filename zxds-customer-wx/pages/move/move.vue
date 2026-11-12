<template>
	<view class="page">
		<!-- 自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">‹</view>
			<view class="nav-title">司乘同显</view>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 地图 -->
		<map id="map" scale="15" :longitude="longitude" :latitude="latitude" :enable-poi="true" class="map"
			:style="mapStyle" :polyline="polyline" :markers="markers" @longpress="showHandle"></map>

		<!-- 底部悬浮面板 -->
		<view class="panel" v-show="infoStatus">
			<view class="info">
				<view class="label">剩余</view>
				<view class="value">{{ distance }}公里</view>
				<view class="label">，预计</view>
				<view class="value">{{ duration }}分钟</view>
				<view class="more" @tap="moreHandle">订单详情</view>
			</view>
			<view class="opt">
				<up-button class="cancel-btn" :custom-style="cancelStyle" text="取消订单" @click="cancelHandle"></up-button>
				<up-button class="confirm-btn" v-if="status == 2 || status == 3" text="司机到达"
					@click="driverArriviedHandle"></up-button>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'
	import {
		onLoad,
		onUnload,
		onShow,
		onHide
	} from '@dcloudio/uni-app'
	import {
		tencent,
		ajax,
		url
	} from '@/utils/common.js'
	import QQMapWX from '@/lib/qqmap-wx-jssdk.min.js'

	let messageTimer = null

	let qqmapsdk = null

	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)

	const orderId = ref(null)
	const status = ref(null)
	const mode = ref(null)
	const cancelStyle = ref('')
	const map = ref(null)
	const mapStyle = ref('')
	const startLatitude = ref(0)
	const startLongitude = ref(0)
	const endLatitude = ref(0)
	const endLongitude = ref(0)
	const latitude = ref(0)
	const longitude = ref(0)
	const targetLatitude = ref(0)
	const targetLongitude = ref(0)
	const distance = ref(0)
	const duration = ref(0)
	const polyline = ref([])
	const markers = ref([])
	const timer = ref(null)
	const infoStatus = ref(true)

	function goBack() {
		uni.navigateBack()
	}

	function moreHandle() {}

	/**
	 * 乘客确认司机已到达
	 */
	function driverArriviedHandle() {
		uni.showModal({
			title: '提示信息',
			content: '确定司机已经到达代驾点？',
			success(resp) {
				if (resp.confirm) {
					const data = {
						orderId: orderId.value
					}
					ajax(url.confirmArriveStartPlace, 'PUT', data, (resp) => {
						if (resp.data.result) {
							uni.showToast({
								icon: 'success',
								title: '状态更新成功'
							})

							// 更新本地状态，准备计算去终点的路线
							status.value = 4;
							// 修改出行方式
							mode.value = 'driving';
							// 修改我们的终点
							targetLatitude.value = endLatitude.value
							targetLongitude.value = endLongitude.value
						}
					})
				}
			}
		})
	}

	function cancelHandle() {}

	function showHandle() {
		infoStatus.value = !infoStatus.value
	}

	onLoad((options) => {
		// 接收orderId
		orderId.value = options.orderId

		// 初始化腾讯地图的SDK。
		qqmapsdk = new QQMapWX({
			key: tencent.map.key
		})

		// 初始化地图上下文
		map.value = uni.createMapContext('map')

		// 动态计算地图高度，减去自定义导航栏的高度 (88rpx)
		const sysInfo = uni.getSystemInfoSync()
		const navBarHeightPx = (88 / 750) * sysInfo.windowWidth
		const mapHeight = sysInfo.windowHeight - navBarHeightPx - statusBarHeight.value
		mapStyle.value = `width:100%;height:${mapHeight}px;`
	})

	onShow(() => {
		// 1.监听实时定位的更新（监听乘客自己手机的实时定位）
		uni.$on('updateLocation', (location) => {
			// 如果状态不是 2，则应该使用当前乘客自己手机的GPS定位。
			if (location != null && status.value !== 2) {
				latitude.value = location.latitude
				longitude.value = location.longitude
			}
		})

		// 2.查询订单的详细信息并计算最佳路线
		const data = {
			orderId: orderId.value
		}

		ajax(url.searchOrderForMoveById, 'GET', data, (resp) => {
			const result = resp.data.result

			// 解析起点坐标
			const startLoc = JSON.parse(result.startPlaceLocation)
			startLatitude.value = startLoc.latitude
			startLongitude.value = startLoc.longitude

			// 解析终点坐标
			const endLoc = JSON.parse(result.endPlaceLocation)
			endLatitude.value = endLoc.latitude
			endLongitude.value = endLoc.longitude

			// 根据状态设置目标点和出行方式
			status.value = result.status
			if (status.value === 2 || status.value === 3) {
				// 状态是2或者3：目标点是  代驾订单的起点
				targetLatitude.value = startLatitude.value
				targetLongitude.value = startLongitude.value
				mode.value = 'bicycling'
			} else if (status.value === 4) {
				// 状态是4的时候：目标点是代驾订单的终点
				targetLatitude.value = endLatitude.value
				targetLongitude.value = endLongitude.value
				mode.value = 'driving'
			}

			// 执行  analyse() 分析并计算最佳线路
			analyse()

			if (timer.value) {
				clearInterval(timer.value)
			}

			// 启动定时器，每隔6秒刷新一次。
			timer.value = setInterval(() => {
				analyse()
			}, 6000)

			// 当状态是4/5/6,创建定时器，每隔5秒发送一次AJAX请求，看看MQ上有没有自己的账单通知消息。
			if (status.value == 4 || status.value == 5 || status.value == 6) {
				messageTimer = setInterval(() => {
					console.log('乘客端司乘同显页面每隔5秒发送一次AJAX请求，看看MQ中有没有当前乘客的账单通知消息')
					ajax(url.receiveBillMessage, 'GET', {}, (resp) => {
						// resp.data.result 获取的是从MQ上取出的消息。（从自己专属的消息队列当中获取消息。）
						if (resp.data.result === '您有代驾订单待支付') {
							console.log("消息：", resp.data.result)
							// 跳转到账单详情页面
							uni.redirectTo({
								url: `../order/order?orderId=${orderId.value}`
							})
						}
					})
				}, 5000)
			}

		}, false)
	})

	onHide(() => {
		console.log('乘客端司乘同显页面隐藏了')
		uni.$off('updateLocation')

		if (timer.value) {
			clearInterval(timer.value)
			timer.value = null
		}

		if (messageTimer) {
			clearInterval(messageTimer)
			messageTimer = null
		}
	})

	onUnload(() => {
		console.log('乘客端司乘同显页面隐藏了')
		uni.$off('updateLocation')

		if (timer.value) {
			clearInterval(timer.value)
			timer.value = null
		}

		if (messageTimer) {
			clearInterval(messageTimer)
			messageTimer = null
		}
	})

	/**
	 * 把腾讯地图返回的加密坐标串解析为坐标点数组
	 */
	function formatPolyline(compressed) {
		const coors = compressed
		const pl = []
		for (let i = 2; i < coors.length; i++) {
			coors[i] = coors[i - 2] + coors[i] / 1000000
		}
		for (let i = 0; i < coors.length; i++) {
			pl.push({
				latitude: coors[i],
				longitude: coors[i + 1]
			})
			i++
		}
		return pl
	}

	/**
	 * 计算最佳线路
	 */
	function calculateLine() {
		if (latitude.value === 0 || longitude.value === 0) {
			return
		}

		qqmapsdk.direction({
			mode: mode.value,
			from: {
				latitude: latitude.value,
				longitude: longitude.value
			},
			to: {
				latitude: targetLatitude.value,
				longitude: targetLongitude.value
			},
			success: (resp) => {
				// 接口报错直接返回
				if (resp.status !== 0) return

				const route = resp.result.routes[0]
				const rawDistance = route.distance
				const rawDuration = route.duration
				const rawPolyline = route.polyline

				// 距离：米 转 公里，向上取整到 0.1
				distance.value = Math.ceil((rawDistance / 1000) * 10) / 10
				duration.value = rawDuration

				// 把加密折线解析成坐标点数组
				const points = formatPolyline(rawPolyline)

				// 地图折线配置
				polyline.value = [{
					points: points, // 路线坐标点数组
					width: 6, // 线宽
					color: '#05B473', // 线色（绿）
					arrowLine: true // 显示方向箭头
				}]

				// 地图标记点：司机当前位置
				markers.value = [{
						id: 1, // 起点
						latitude: latitude.value,
						longitude: longitude.value,
						width: 30,
						height: 30,
						anchor: {
							x: 0.5,
							y: 0.5
						}, // 图标中心对准GPS坐标
						iconPath: '../../static/move/driver-icon.png'
					},
					{
						id: 2, // 终点
						latitude: targetLatitude.value,
						longitude: targetLongitude.value,
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
							latitude: latitude.value,
							longitude: longitude.value
						},
						{
							latitude: targetLatitude.value,
							longitude: targetLongitude.value
						}
					],
					padding: [80, 80, 120, 80]
				})
			},
			fail: (error) => {
				console.error('路线规划失败', error)
			}
		})
	}

	/**
	 * 分析并计算最佳线路（该方法对 calculateLine 函数进行了包装）
	 */
	function analyse() {
		if (status.value === 2) {
			// 如果状态是2，就应该查询订单的定位缓存（获取司机的位置）
			const data = {
				orderId: orderId.value
			};
			ajax(url.searchOrderLocationCache, 'GET', data, (resp) => {
				const result = resp.data.result
				// 判断是否返回了经纬度
				if (result && result.latitude && result.longitude) {
					latitude.value = result.latitude
					longitude.value = result.longitude
					calculateLine()
				}
			}, false)
		} else {
			// 其他状态不是接客户，就应该使用自己的定位作为行进路线的起点。
			calculateLine()
		}
	}
</script>

<style lang="less">
	@import url('move.less');
</style>
