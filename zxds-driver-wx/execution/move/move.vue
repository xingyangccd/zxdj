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
			:style="mapStyle" :polyline="polyline" :markers="markers" @longpress="showHandle">
			<cover-view class="information-container" v-show="infoStatus">
				<cover-image src="../static/move/close-icon.png" class="close-icon" @tap="hideHandle"></cover-image>
				<cover-view class="info">
					<cover-view class="label">剩余</cover-view>
					<cover-view class="value">{{ distance }}公里</cover-view>
					<cover-view class="label">，预计</cover-view>
					<cover-view class="value">{{ duration }}分钟</cover-view>
				</cover-view>
			</cover-view>
		</map>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'
	import {
		onLoad,
		onShow,
		onHide
	} from '@dcloudio/uni-app'

	import QQMapWX from '@/lib/qqmap-wx-jssdk.min.js'

	import {
		tencent,
		ajax,
		url
	} from '@/utils/common.js'

	let qqmapsdk = null

	// 状态栏高度，避开刘海
	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)

	// 业务状态
	const orderId = ref(null)
	const status = ref(null) // 订单状态：如果是2，则司机当前行进的终点是代驾的起点，如果是3，则司机当前行进的终点是代驾的终点。
	const mode = ref(null) // 出行模式 (bicycling / driving)

	// 地图上下文与样式
	const map = ref(null) // 存放地图上下文对象
	const mapStyle = ref('')

	// 起终点坐标
	const startLatitude = ref(0) // 代驾的起点坐标
	const startLongitude = ref(0)
	const endLatitude = ref(0) // 代驾的终点坐标
	const endLongitude = ref(0)

	// 司机当前位置
	const latitude = ref(0) // 当前司机的定位
	const longitude = ref(0)

	// 行进目标点
	const targetLatitude = ref(0) // 当前司机要行进的终点坐标
	const targetLongitude = ref(0)

	// 行驶信息
	const distance = ref(0) // 当前司机到当前行进的终点的距离
	const duration = ref(0) // 当前司机到当前行进的终点的耗时
	const polyline = ref([]) // 用来存放GPS坐标点
	const markers = ref([]) // 用来存放地图路线上显示的图表
	const timer = ref(null) // 定时器
	const infoStatus = ref(true) // 底部信息面板显示状态

	onLoad((options) => {

		// 1.获取订单id
		orderId.value = options.orderId

		// 2.初始化腾讯地图SDK
		qqmapsdk = new QQMapWX({
			key: tencent.map.key
		})

		// 3. 初始化地图上下文
		map.value = uni.createMapContext("map");

		// 动态计算地图高度，减去自定义导航栏的高度 (88rpx)
		const sysInfo = uni.getSystemInfoSync()
		const navBarHeightPx = (88 / 750) * sysInfo.windowWidth
		const mapHeight = sysInfo.windowHeight - navBarHeightPx - statusBarHeight.value
		mapStyle.value = `width:100%;height:${mapHeight}px;`
	})

	// 返回上一页
	function goBack() {
		uni.navigateBack()
	}


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
	 * 利用腾讯地图SDK计算从司机当前位置到目标点的最佳线路，在地图上渲染折线图，以及显示司机位置图标
	 */
	function caculateLine() {
		// 以下条件成立说明根本没有获取到司机的定位，就不需要计算最佳线路。
		if (latitude.value === 0 || longitude.value === 0) {
			return
		}

		// 增加对目标点的判断，防止传入无效坐标给 SDK
		if (targetLatitude.value === 0 || targetLongitude.value === 0) {
			return
		}

		qqmapsdk.direction({
			// 出行模式
			mode: mode.value,
			// 起点
			from: {
				latitude: latitude.value,
				longitude: longitude.value
			},
			// 终点
			to: {
				latitude: targetLatitude.value,
				longitude: targetLongitude.value
			},
			success: (resp) => {
				if (resp.status != 0) return

				const route = resp.result.routes[0]
				const rawDistance = route.distance;
				const rawDuration = route.duration;
				const rawPolyline = route.polyline;

				// 距离:米转换为公里，向上取整0.1
				distance.value = Math.ceil((rawDistance / 1000) * 10) / 10
				duration.value = rawDuration

				// 把压缩的坐标串转换为正常坐标串数组
				const points = formatPolyline(rawPolyline)

				// 地图折线的相关配置
				polyline.value = [{
					points: points, // 路线坐标点数组
					width: 6, // 线宽
					color: '#05B473', // 线色（绿）
					arrowLine: true // 显示方向箭头
				}]

				// 地图标记点：起点 + 终点
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
						iconPath: '../static/move/driver-icon.png'
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
				console.error("路线规划失败", error)
			}
		})

	}


	onShow(() => {
		// 1.监听实时定位
		uni.$on('updateLocation', (location) => {
			if (location != null) {
				// 司机当前的定位
				latitude.value = location.latitude;
				longitude.value = location.longitude;
			}
		})

		// 2.查询订单详情计算线路
		const data = {
			orderId: orderId.value
		};
		ajax(url.searchOrderForMoveById, 'GET', data, (resp) => {
			const result = resp.data.result

			// 解析起点坐标
			const startLoc = JSON.parse(result.startPlaceLocation)
			startLatitude.value = startLoc.latitude;
			startLongitude.value = startLoc.longitude;

			// 解析终点坐标
			const endLoc = JSON.parse(result.endPlaceLocation)
			endLatitude.value = endLoc.latitude;
			endLongitude.value = endLoc.longitude;

			// 根据状态设置目标点和出行方式
			status.value = result.status
			if (status.value === 2) {
				// 状态是2说明：前往代驾起点（上车点）
				targetLatitude.value = startLatitude.value;
				targetLongitude.value = startLongitude.value;
				mode.value = 'bicycling'
			} else if (status.value == 3 || status.value == 4) {
				targetLatitude.value = endLatitude.value;
				targetLongitude.value = endLongitude.value;
				mode.value = 'driving'
			}

			// 调用这个方法可以计算最佳线路，画折线，设置起点和终点图标。
			caculateLine()

			// 清理可能存在的旧定时器，防止重复刷新。
			if (timer.value) {
				clearInterval(timer.value)
			}

			// 每个6秒调用一次.
			timer.value = setInterval(() => {
				caculateLine()
			}, 6000)

		})
	})

	onHide(() => {
		// 解绑事件
		uni.$off('updateLocation')

		// 删除定时器
		if (timer.value) {
			clearInterval(timer.value)
			timer.value = null
		}
	})

	function hideHandle() {
		infoStatus.value = false
	}

	function showHandle() {
		infoStatus.value = true
	}
</script>

<style lang="less">
	@import url('move.less');
</style>
