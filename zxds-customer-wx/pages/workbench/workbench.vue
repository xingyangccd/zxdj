<template>
	<view class="page">
		<!-- 全屏地图 -->
		<map id="map" :longitude="longitude" :latitude="latitude" :style="contentStyle" scale="15"
			:enable-traffic="false" :show-location="true" :enable-poi="true" class="map">
			<cover-image class="location" src="../../static/workbench/location.png"
				@tap="returnLocationHandle"></cover-image>
		</map>

		<!-- 顶部品牌（浮在地图上） -->
		<view class="header">
			<view class="brand-row">
				<view class="brand-mark">
					<text class="brand-mark-text">Z</text>
					<view class="brand-mark-glow"></view>
				</view>
				<view class="brand-info">
					<view class="brand-name">智行代驾</view>
					<view class="brand-sub">
						<text class="sub-dot"></text>
						<text>乘客端 · 叫代驾</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 底部叫车面板 -->
		<view class="panel">
			<!-- 起点 -->
			<view class="from" @tap="chooseLocationHandle('from')">
				<text>{{ from.address || '您的位置' }}</text>
			</view>

			<!-- 虚线连接 -->
			<view class="dashed-line"></view>

			<!-- 终点 -->
			<view class="to" @tap="chooseLocationHandle('to')">
				<text>{{ to.address || '输入您的目的地' }}</text>
			</view>

			<!-- 呼叫代驾按钮 -->
			<button class="btn" @tap="callDriverHandle">
				<view class="btn-bg"></view>
				<text class="btn-text">呼叫代驾</text>
				<text class="btn-arrow">→</text>
			</button>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'
	import {
		onLoad,
		onShow, // 页面展示的时候需要刷新地图（更新地图位置）
		onHide // 页面隐藏的时候需要将事件解绑。
	} from '@dcloudio/uni-app'
	import {
		tencent, // 导入腾讯位置服务相关的数据
		ajax,
		url
	} from '@/utils/common.js'

	// 腾讯地图官方提供的SDK，用来在小程序当中实现坐标转地址、地址搜索、路线规划等等地图相关服务。
	import QQMapWX from '@/lib/qqmap-wx-jssdk.min.js'

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

	const longitude = ref(117.1902)
	const latitude = ref(39.1256)
	const contentStyle = ref('')

	onLoad(() => {
		const windowHeight = uni.getSystemInfoSync().windowHeight
		contentStyle.value = `height:${windowHeight}px;`

		// 发送ajax请求查询乘客当前的订单
		ajax(url.hasCustomerCurrentOrder, 'GET', null, (resp) => {
			const result = resp.data.result

			// 1.存在未接单的订单
			if (result.hasCustomerUnAcceptOrder) {
				const json = result.unAcceptOrder
				const startPlaceLocation = JSON.parse(json.startPlaceLocation)
				const endPlaceLocation = JSON.parse(json.endPlaceLocation)

				const fromData = {
					address: json.startPlace,
					latitude: startPlaceLocation.latitude,
					longitude: startPlaceLocation.longitude
				}
				const toData = {
					address: json.endPlace,
					latitude: endPlaceLocation.latitude,
					longitude: endPlaceLocation.longitude
				}

				// 将起点和终点存储到storage当中，后面有用。
				uni.setStorageSync('from', fromData)
				uni.setStorageSync('to', toData)

				uni.showModal({
					title: '提示信息',
					content: '您有一个订单等待司机接单，现在将跳转到等待接单页面',
					showCancel: false,
					success(modalResp) {
						if (modalResp.confirm) {
							uni.navigateTo({
								url: `/pages/create_order/create_order?showPopup=true&orderId=${json.id}&carType=${json.carType}&carPlate=${json.carPlate}`
							})
						}
					}
				})
			} else if (result.hasCustomerUnFinishedOrder) {
				uni.showModal({
					title: '提示信息',
					content: '您有一个正在执行的订单，现在将跳转到司乘同显页面',
					showCancel: false,
					success(modalResp) {
						if (modalResp.confirm) {
							uni.navigateTo({
								url: `/pages/move/move?orderId=${result.unFinishedOrder}`
							})
						}
					}
				})
			}
		}, false) // 不显示loading

	})

	// 变量名不能随便写，变量名和id保持一致。
	// 这个变量map就代表上面的地图组件。
	const map = ref(null)

	let qqmapsdk = null;

	// 手动选点标记：不为空表示用户手动选过点，自动定位将不再继续覆盖用户手选的点。
	// flag是空表示：用户从来没有手动选过点，不为空表示用户已经手动选点了，就不能再自动更新了。
	const flag = ref(null)

	// 本次选点类型：from/to
	const chooseType = ref(null)

	onShow(() => {
		// 1. 获取地图上下文对象（获取一个能够操作地图组件的对象）
		map.value = uni.createMapContext("map")

		// 2. 初始化腾讯地图SDK
		// 以后调用 qqmapsdk 上的方法来操作腾讯位置服务的API。
		qqmapsdk = new QQMapWX({
			key: tencent.map.key
		});

		// 3. 监听App.vue 广播的实时定位
		// 这个代码表示事件绑定，绑定一个 updateLocation 事件，只要 updateLocation 发生，则执行回调函数。
		uni.$on("updateLocation", handleLocationChange)

		// 4. 处理地图选点插件返回的结果
		// 获取地图选点插件本身。
		const chooseLocation = requirePlugin("chooseLocation");
		// 地图选点返回的结果
		const location = chooseLocation.getLocation();
		if (location) {
			const place = location.name;
			const lat = location.latitude;
			const lng = location.longitude;

			if (chooseType.value === "from") {
				from.value.address = place;
				from.value.latitude = lat;
				from.value.longitude = lng;
			} else {
				to.value.address = place;
				to.value.latitude = lat;
				to.value.longitude = lng;
				// 当用户选择终点之后，立即跳转到创建预览订单的页面。
				// 起点和终点存储到storage当中
				uni.setStorageSync("from", from.value)
				uni.setStorageSync("to", to.value)

				// 跳转到创建预览订单页面
				uni.navigateTo({
					url: '/pages/create_order/create_order'
				})
			}

			// 标记一下用户已经手动选过点了。
			flag.value = chooseType.value;
			chooseType.value = null;
		}
	})

	// 处理定位的变化（当位置发生变化时，执行这个回调函数）
	function handleLocationChange(location) {
		// 用户手动选过点，自动定位不再继续覆盖用户选过的点。
		if (flag.value != null) return;

		// location对象中封装了最新的定位信息
		// 对象解构语法
		const {
			latitude: lat,
			longitude: lng
		} = location;
		// 更新地图中心
		latitude.value = lat;
		longitude.value = lng;
		// 更新上车点坐标
		from.value.latitude = lat;
		from.value.longitude = lng;
		// 将坐标转换成中文地址（必须使用SDK）
		qqmapsdk.reverseGeocoder({
			location: {
				latitude: lat,
				longitude: lng
			},
			success: (response) => {
				// response.result.address 可以获取到坐标转换成的中文地址
				from.value.address = response.result.address;
			},
			fail: (err) => {
				console.error("地址解析失败", err)
			}
		});
	}

	// 页面隐藏的时候解绑定位监听，避免后台浪费电量
	onHide(() => {
		uni.$off("updateLocation", handleLocationChange)
	})


	// 回到当前位置
	function returnLocationHandle() {
		// 内置的API，它可以帮助你回到当前位置。
		map.value.moveToLocation();
	}

	// 选起点 / 终点
	function chooseLocationHandle(type) {
		// 保存住当前用户点击的是from还是to！！！
		chooseType.value = type;

		const key = tencent.map.key;
		const referer = tencent.map.referer;
		const location = JSON.stringify({
			latitude: latitude.value,
			longitude: longitude.value
		})

		// 跳转到选点页面进行选点
		uni.navigateTo({
			url: `plugin://chooseLocation/index?key=${key}&referer=${referer}&location=${location}`
		})
	}

	// 呼叫代驾
	function callDriverHandle() {
		// TODO: 提交订单
	}
</script>

<style lang="less">
	@import url('workbench.less');
</style>
