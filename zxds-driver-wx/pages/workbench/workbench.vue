<template>
	<view class="page">
		<!-- 自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-title">工作台</view>
		</view>

		<!-- 审核中 -->
		<view class="review-container" v-show="reviewAuth">
			<image src="../../static/workbench/review.png" mode="widthFix"></image>
			<view class="title">您的实名资料正在审核中</view>
			<view class="desc">这期间您将无法接单，请等待资料审核通过</view>
		</view>

		<!-- 正常工作 -->
		<view class="main-content" v-show="!reviewAuth">
			<!-- 顶部数据卡片 -->
			<view class="top-container">
				<view class="notice">今日数据：凌晨00:00 ~ 本日23:59</view>
				<view class="summary-container">
					<view class="item">
						<view class="title">代驾时长</view>
						<view class="value"><text class="number">{{ hour }}</text><text class="unit">时</text></view>
					</view>
					<view class="item">
						<view class="title">今日收入</view>
						<view class="value"><text class="number">{{ income }}</text><text class="unit">元</text></view>
					</view>
					<view class="item">
						<view class="title">今日成单</view>
						<view class="value"><text class="number">{{ orders }}</text><text class="unit">单</text></view>
					</view>
				</view>
				<view class="service-container">
					<view class="item" :style="service.locationStyle">
						<image :src="service.locationIcon" mode="widthFix" class="icon icon-1"></image>
						{{ service.locationText }}
					</view>
					<view class="item" :style="service.messageStyle">
						<image :src="service.messageIcon" mode="widthFix" class="icon icon-2"></image>
						{{ service.messageText }}
					</view>
					<view class="item" :style="service.listenStyle" @tap="listenHandle">
						<image :src="service.listenIcon" mode="widthFix" class="icon icon-3"></image>
						{{ service.listenText }}
					</view>
					<view class="item" :style="service.settingStyle" @tap="settingHandle">
						<image :src="service.settingIcon" mode="widthFix" class="icon icon-4"></image>
						{{ service.settingText }}
					</view>
				</view>
			</view>

			<!-- 内容区：地图 / 新订单 / 执行订单 三选一 -->
			<view class="content-area" :class="{ 'content-area--auto': ['接客户', '到达代驾点', '开始代驾'].includes(workStatus) }">
				<!-- 停止接单：地图 -->
				<view class="map-wrapper" v-show="workStatus == '停止接单'">
					<map id="map" :longitude="longitude" :latitude="latitude" scale="15" class="map-content"
						:enable-traffic="true" :show-location="true" :enable-poi="true">
						<cover-image class="location" src="../../static/workbench/location.png"
							@tap="returnLocationHandle"></cover-image>
					</map>
				</view>

				<!-- 开始接单：新订单 -->
				<scroll-view scroll-y class="content-scroll" v-show="workStatus == '开始接单'">
					<view class="new-order">
						<view v-if="newOrder == null">
							<image src="../../static/workbench/no-order.png" mode="widthFix" class="no-order-img">
							</image>
							<view class="no-order-title">目前暂无订单</view>
						</view>
						<view v-if="newOrder != null">
							<view class="line-1">
								<view>
									<view class="sub-time">距离您</view>
									<view><text class="number">{{ newOrder.distance }}</text><text
											class="unit">公里</text></view>
								</view>
								<view>
									<view class="sub-time">全程</view>
									<view><text class="number">{{ newOrder.mileage }}</text><text class="unit">公里</text>
									</view>
								</view>
								<view>
									<view class="tag">含加价{{ newOrder.favourFee }}元</view>
									<view><text class="number">{{ newOrder.expectsFee }}</text><text
											class="unit">元</text></view>
								</view>
							</view>
							<view class="line-2">
								<view class="from"><text>{{ newOrder.from }}</text></view>
								<view class="dashed-line"></view>
								<view class="to"><text>{{ newOrder.to }}</text></view>
							</view>
							<button :class="!canAcceptOrder ? 'disabled-btn' : 'accept-btn'"
								@tap="acceptHandle">立即抢单</button>
						</view>
					</view>
					<view class="notice-container">
						<view class="title">【 抢单说明 】</view>
						<view class="desc"><text
								class="num">1.</text>当前地区可能存在多个代驾订单，但是司机端只能显示其中一条订单。这是为了保障代驾乘客订单的接单率，避免代驾司机只挑选优质订单。
						</view>
						<view class="desc"><text class="num">2.</text>某条订单如果10秒钟之内您没有抢单，系统则认定您主动放弃该订单，系统将为你推送新的订单。
						</view>
					</view>
				</scroll-view>

				<!-- 执行订单中 -->
				<scroll-view scroll-y class="content-scroll execute-order-container"
					v-show="['接客户', '到达代驾点', '开始代驾'].includes(workStatus)">

					<view class="customer-container">
						<u-avatar :src="executeOrder.photo" mode="square"></u-avatar>
						<view class="info">
							<view class="customer-name">代驾客户（{{ executeOrder.title }}）</view>
							<view class="customer-tel">Tel：{{ executeOrder.tel }}</view>
						</view>
						<view class="call" @tap="callCustomerHandle"></view>
					</view>
					<view class="address-container">
						<view class="from"><text>{{ executeOrder.startPlace }}</text></view>
						<view class="dashed-line"></view>
						<view class="to"><text>{{ executeOrder.endPlace }}</text></view>
					</view>
					<view class="order-container">
						<view>【 订单号码 】 {{ executeOrder.id }}</view>
						<view>【 下单时间 】 {{ executeOrder.createTime }}</view>
						<view>【 客户红包 】 {{ executeOrder.favourFee }}元</view>
						<view>【 代驾车型 】 {{ executeOrder.carType }}</view>
						<view>【 代驾车牌 】 {{ executeOrder.carPlate }}</view>
					</view>
					<view class="operate-container">
						<view class="item" @tap="cancelOrderHandle">
							<image src="../../static/workbench/cancel-icon.png" mode="widthFix" class="cancel-icon">
							</image>
							<text class="cancel-text">取消订单</text>
						</view>
						<view class="item" v-show="workStatus == '接客户'" @tap="arriveStartPlaceHandle">
							<image src="../../static/workbench/arrive-start-icon.png" mode="widthFix"
								class="arrive-start-icon"></image>
							<text class="arrive-start-text">到达代驾点</text>
						</view>
						<view class="item" v-show="workStatus == '到达代驾点'" @tap="startDrivingHandle">
							<image src="../../static/workbench/drive-start-icon.png" mode="widthFix"
								class="drive-start-icon"></image>
							<text class="drive-start-text">开始代驾</text>
						</view>
						<view class="item" v-show="workStatus == '开始代驾'" @tap="endDrivingHandle">
							<image src="../../static/workbench/drive-stop-icon.png" mode="widthFix"
								class="drive-stop-icon"></image>
							<text class="drive-stop-text">结束代驾</text>
						</view>
						<view class="item">
							<image src="../../static/workbench/alarm-icon.png" mode="widthFix" class="alarm-icon">
							</image>
							<text class="alarm-text">立即报警</text>
						</view>
					</view>
					<view class="other-container">
						<view class="item" @tap="showNavigationHandle">
							<image src="../../static/workbench/other-icon-1.png" mode="widthFix" class="location-icon">
							</image>
							<text class="location-text">定位导航</text>
						</view>
						<view class="item" @tap="showMoveHandle">
							<image src="../../static/workbench/other-icon-2.png" mode="widthFix" class="display-icon">
							</image>
							<text class="display-text">司乘同显</text>
						</view>
						<view class="item" @tap="showOrderHandle">
							<image src="../../static/workbench/other-icon-3.png" mode="widthFix" class="order-icon">
							</image>
							<text class="order-text">订单详情</text>
						</view>
						<view class="item" @tap="callServiceHandle">
							<image src="../../static/workbench/other-icon-4.png" mode="widthFix" class="service-icon">
							</image>
							<text class="service-text">客服热线</text>
						</view>
					</view>
				</scroll-view>
			</view>
		</view>

		<!-- 底部按钮：fixed 固定在屏幕底部 -->
		<view class="bottom-container" v-show="!reviewAuth && ['开始接单', '停止接单'].includes(workStatus)">
			<view class="btn" @tap="gohomeHandle">
				<image src="../../static/workbench/btn-icon-1.png" mode="widthFix" class="btn-icon-1"></image>
				<text>回家</text>
			</view>
			<view class="btn primary">
				<text v-show="workStatus == '停止接单'" @tap="startWorkHandle">开始接单</text>
				<text v-show="workStatus == '开始接单'" @tap="stopWorkHandle">停止接单</text>
			</view>
			<view class="btn" @tap="rechargeHandle">
				<image src="../../static/workbench/btn-icon-2.png" mode="widthFix" class="btn-icon-2"></image>
				<text>充值</text>
			</view>
		</view>

		<!-- 取消订单确认弹窗 -->
		<up-modal v-model:show="model.cancelConfirm.show" :title="model.cancelConfirm.title"
			:content="model.cancelConfirm.content" show-cancel-button @confirm="confirmCancelOrder"></up-modal>

		<!-- 取消成功提示弹窗 -->
		<up-modal v-model:show="model.cancelSuccess.show" :title="model.cancelSuccess.title"
			:content="model.cancelSuccess.content"></up-modal>
	</view>
</template>
<script setup>
	// ===== 外部插件 / 工具库 =====
	const plugin = requirePlugin('WechatSI');
	import dayjs from 'dayjs';

	import {
		ref,
		reactive,
		onUnmounted
	} from 'vue'
	import {
		onLoad,
		onShow,
		onHide
	} from '@dcloudio/uni-app'
	import {
		url,
		ajax,
		refreshMessage,
		tencent
	} from '@/utils/common.js'

	import QQMapWX from '@/lib/qqmap-wx-jssdk.min.js'

	let qqmapsdk = null

	// 负责播放语音的实例对象。
	const audio = ref(null)

	// 微信同声传译插件对应的：录音管理器
	const recordManager = ref(null)
	// 录音mp3文件的数量
	const recordNum = ref(0)
	// 停止录音的标记：true表示停止录音
	const stopRecord = ref(false)

	// 状态栏高度，避开刘海
	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)

	// 审核状态：realAuth == 3 表示审核中
	const reviewAuth = ref(uni.getStorageSync('realAuth') == 3)

	// 今日数据
	const hour = ref(0)
	const income = ref(0)
	const orders = ref(0)

	// 地图坐标
	const map = ref(null)
	const latitude = ref(39.908823)
	const longitude = ref(116.397470)

	// 服务状态四个标签
	const service = reactive({
		locationIcon: '../../static/workbench/service-icon-1.png',
		locationText: '定位正常',
		locationStyle: '',
		messageIcon: '../../static/workbench/service-icon-2.png',
		messageText: '推送正常',
		messageStyle: '',
		listenIcon: '../../static/workbench/service-icon-3.png',
		listenText: '收听订单',
		listenStyle: '',
		settingIcon: '../../static/workbench/service-icon-4.png',
		settingText: '接单设置',
		settingStyle: ''
	})

	// 接单相关设置
	const settings = reactive({
		listenService: true, // 是否收听订单（语音播报）
		autoAccept: false // 是否自动抢单
	})

	// 工作状态：停止接单 / 开始接单 / 接客户 / 到达代驾点 / 开始代驾
	const workStatus = ref(uni.getStorageSync('workStatus') || '停止接单')

	// 新订单
	const newOrder = ref(null)
	const newOrderList = ref([])
	const canAcceptOrder = ref(false)
	const accepting = ref(false)
	let receiveNewOrderTimer = null
	const playFlag = ref(false)

	// 执行中的订单
	const executeOrder = reactive({
		id: '',
		photo: '',
		title: '',
		tel: '',
		customerId: '',
		startPlace: '',
		startPlaceLocation: {},
		endPlace: '',
		endPlaceLocation: {},
		favourFee: '',
		carPlate: '',
		carType: '',
		createTime: ''
	})

	// 弹窗模型
	const model = reactive({
		cancelConfirm: {
			show: false,
			title: '提示消息',
			content: '如果不满足订单免费取消规则，只能有偿取消订单，并且可能被禁止接单一段时间，您确认要取消当前订单？'
		},
		cancelSuccess: {
			show: false,
			title: '提示消息',
			content: ''
		}
	})

	// ===== 生命周期 =====
	onLoad(() => {
		// 如果实名认证信息处于审核中，直接结束
		if (reviewAuth.value) return

		// 初始化录音识别管理器
		recordManager.value = initRecordManager()

		// 初始化地图上下文
		map.value = uni.createMapContext('map')

		// 初始化qqmapsdk
		qqmapsdk = new QQMapWX({
			key: tencent.map.key
		})

		// 查询正在执行的订单，司机可能上次没结束代驾就退出了，重新进工作台要把状态恢复回来
		ajax(url.searchDriverCurrentOrder, 'GET', null, (resp) => {
			if (resp.data.result) {
				const result = resp.data.result;
				Object.assign(executeOrder, {
					id: result.id,
					photo: result.photo,
					title: result.title,
					tel: result.tel,
					customerId: result.customerId,
					startPlace: result.startPlace,
					startPlaceLocation: JSON.parse(result.startPlaceLocation),
					endPlace: result.endPlace,
					endPlaceLocation: JSON.parse(result.endPlaceLocation),
					favourFee: result.favourFee,
					carPlate: result.carPlate,
					carType: result.carType,
					createTime: result.createTime
				})

				const statusMap = {
					'2': '接客户',
					'3': '到达代驾点',
					'4': '开始代驾'
				};

				workStatus.value = statusMap[String(result.status)]

				// 缓存到本地
				uni.setStorageSync('workStatus', workStatus.value)
				uni.setStorageSync('executeOrder', executeOrder)

				// 如果工作状态是开始代驾，就要开启录音
				if (workStatus.value === '开始代驾') {
					recordManager.value.start({
						duration: 20 * 1000,
						lang: 'zh_CN'
					})
				}
			}
		})
	})

	onShow(() => {
		// 审核中不查询数据，直接返回
		if (reviewAuth.value) return;

		// 实时获取GPS定位。
		uni.$on("updateLocation", (location) => {
			if (location != null) {
				// 定位正常
				service.locationIcon = '../../static/workbench/service-icon-1.png'
				service.locationText = '定位正常'
				service.locationStyle = 'color:#46B68F'
				// 地图更新
				latitude.value = location.latitude;
				longitude.value = location.longitude;
			} else {
				// 定位异常
				service.locationIcon = '../../static/workbench/service-icon-5.png'
				service.locationText = '定位失败'
				service.locationStyle = 'color:#FF4D4D'
			}
		})

		// 获取工作状态
		let status = uni.getStorageSync("workStatus");
		// 判断工作状态，是否需要定时的轮询接单
		if (status == '开始接单') {
			if (newOrder.value == null) {
				newOrderList.value = []
				playFlag.value = false
			}
			if (!receiveNewOrderTimer) {
				receiveNewOrderTimer = createTimer()
			}
		}


		// 查询工作台数据
		ajax(url.searchWorkbenchData, "POST", null, (resp) => {
			const result = resp.data.result;

			hour.value = result.business.duration;
			income.value = result.business.income;
			orders.value = result.business.orders;

			const s = result.settings;
			// 将司机的设置存储到storage当中
			uni.setStorageSync("settings", s);

			settings.listenService = s.listenService;
			settings.autoAccept = s.autoAccept;

			changeListenService(settings.listenService);
		})
	})

	// 切换收听订单状态（语音播报开关）
	function changeListenService(bool) {
		if (bool) {
			service.listenIcon = '../../static/workbench/service-icon-3.png'
			service.listenStyle = 'color:#46B68F'
			service.listenText = '收听订单'
		} else {
			service.listenIcon = '../../static/workbench/service-icon-7.png'
			service.listenStyle = 'color:#FF4D4D'
			service.listenText = '不听订单'
		}
	}

	// 页面隐藏的时候，把事件解绑，省电。
	onHide(() => {
		uni.$off('updateLocation')
		// 清理当前播报的订单及列表
		newOrder.value = null
		newOrderList.value.length = 0

		// 停止语音播报并销毁播放器
		if (audio.value != null) {
			audio.value.stop()
			audio.value = null
		}

		// 销毁接收新订单的轮询定时器
		if (receiveNewOrderTimer) {
			clearInterval(receiveNewOrderTimer)
			receiveNewOrderTimer = null
		}

		playFlag.value = false
	})

	onUnmounted(() => {
		if (receiveNewOrderTimer) {
			clearInterval(receiveNewOrderTimer)
			receiveNewOrderTimer = null
		}
	})

	function listenHandle() {}

	function settingHandle() {}

	// 地图归位
	function returnLocationHandle() {
		map.value.moveToLocation();
	}

	// 编写的是手动抢单的核心逻辑。
	function acceptHandle() {

		// TODO：人脸识别核验

		// 以下这个判断语句达到了： :disabled 同等效果
		if (!canAcceptOrder.value || accepting.value) {
			return
		}

		// 标记一下正在抢单，避免重复点击，幂等性。
		accepting.value = true;
		uni.vibrateShort({})

		// 发送ajax请求，进行手动抢单.
		ajax(url.acceptNewOrder, 'PUT', {
			orderId: newOrder.value.orderId
		}, (resp) => {
			const result = resp.data.result;
			if (result === "接单成功") {
				uni.showToast({
					title: '接单成功'
				})

				const a = uni.createInnerAudioContext()
				audio.value = a
				a.src = '/static/voice/voice_3.mp3'
				a.play()
				a.onEnded(() => {
					audio.value = null
					// 停止接单
					ajax(url.stopWork, 'POST', null, () => {})
					// 初始化新订单和列表变量
					executeOrder.id = newOrder.value.orderId
					newOrder.value = null
					newOrderList.value.length = 0
					clearInterval(receiveNewOrderTimer)
					receiveNewOrderTimer = null
					playFlag.value = false
					accepting.value = false
					canAcceptOrder.value = false
					// 加载订单执行数据
					loadExecuteOrder()
				})
			} else {
				// 手动抢单失败
				const a = uni.createInnerAudioContext()
				audio.value = a
				a.src = '/static/voice/voice_4.mp3'
				a.play()

				playFlag.value = false
				setTimeout(() => {
					accepting.value = false
					canAcceptOrder.value = false
					if (newOrderList.value.length > 0) {
						showNewOrder()
					} else {
						newOrder.value = null
					}
				}, 1000 * 10)
			}
		})
	}

	/**
	 * 拨打客户电话
	 */
	function callCustomerHandle() {
		uni.makePhoneCall({
			phoneNumber: executeOrder.tel
		})
	}

	function cancelOrderHandle() {}

	/**
	 * 司机到达代驾起点：司机只能在距离代驾起点1公里的范围之内点击该按钮。
	 */
	function arriveStartPlaceHandle() {

		uni.showModal({
			title: '消息通知',
			content: '确认已经到达代驾点？',
			success(resp) {
				if (!resp.confirm) return

				const data = {
					orderId: executeOrder.id,
					customerId: executeOrder.customerId
				}
				ajax(url.arriveStartPlace, 'PUT', data, (resp) => {
					if (resp.data.result === 1) {
						uni.showToast({
							icon: 'success',
							title: '订单状态更新成功'
						})
						// 修改当前工作状态
						workStatus.value = '到达代驾点'
						uni.setStorageSync('workStatus', '到达代驾点')
					}
				})

				/* qqmapsdk.calculateDistance({
					mode: 'straight',
					from: {
						latitude: latitude.value,
						longitude: longitude.value
					},
					to: [{
						latitude: Number(executeOrder.startPlaceLocation.latitude),
						longitude: Number(executeOrder.startPlaceLocation.longitude)
					}],
					success: (distanceResp) => {
						const distance = distanceResp.result.elements[0].distance
						if (distance > 1000) {
							uni.showToast({
								icon: 'none',
								title: '请移动到距离代驾起点1公里以内'
							})
							return
						}

						const data = {
							orderId: executeOrder.id,
							customerId: executeOrder.customerId
						}
						ajax(url.arriveStartPlace, 'PUT', data, (resp) => {
							if (resp.data.result === 1) {
								uni.showToast({
									icon: 'success',
									title: '订单状态更新成功'
								})
								// 修改当前工作状态
								workStatus.value = '到达代驾点'
								uni.setStorageSync('workStatus', '到达代驾点')
							}
						})

					},
					fail: (err) => {
						console.error('计算距离失败', err)
						uni.showToast({
							icon: 'none',
							title: '距离计算失败，请检查定位'
						})
					}
				}) */
			}
		})
	}

	/**
	 * 司机开始代驾
	 */
	function startDrivingHandle() {
		uni.showModal({
			title: '消息通知',
			content: '您已经接到客户，现在开始代驾？',
			success(resp) {
				if (resp.confirm) {
					// 设置录音标志位（允许录音）
					stopRecord.value = false

					const data = {
						orderId: executeOrder.id,
						customerId: executeOrder.customerId
					};

					ajax(url.startDriving, 'PUT', data, (resp) => {
						if (resp.data.rows === 1) { // 成功开始代驾了。
							uni.showToast({
								icon: 'success',
								title: '订单状态更新成功'
							})

							// 更新工作状态并缓存
							workStatus.value = '开始代驾'
							uni.setStorageSync('workStatus', '开始代驾')

							// 开始录音
							recordManager.value.start({
								duration: 20 * 1000,
								lang: 'zh_CN'
							})
						}
					})
				}
			}
		})
	}

	// 停止代驾
	function endDrivingHandle() {
		uni.showModal({
			title: '消息通知',
			content: '已经到达终点，现在结束代驾？',
			success(resp) {
				if (!resp.confirm) return

				const data = {
					orderId: executeOrder.id,
					customerId: executeOrder.customerId,
					status: 5
				};

				// 停止代驾的时候更新订单状态，停止录音。
				ajax(url.updateOrderStatus, 'PUT', data, (resp) => {
					try {
						// 1.停止录音
						stopRecord.value = true
						if (recordManager.value) {
							recordManager.value.stop()
						}

						// 2.更新工作状态
						workStatus.value = '结束代驾'
						uni.setStorageSync('workStatus', '结束代驾')

						// 3.跳转到费用录入页面
						uni.navigateTo({
							url: `/order/enter_fee/enter_fee?orderId=${executeOrder.id}&customerId=${executeOrder.customerId}`
						})
					} catch (e) {
						console.error('结束代驾异常', e)
					}
				})

				/* qqmapsdk.calculateDistance({
					mode: 'straight',
					from: {
						latitude: latitude.value,
						longitude: longitude.value
					},
					to: [{
						latitude: Number(executeOrder.endPlaceLocation.latitude),
						longitude: Number(executeOrder.endPlaceLocation.longitude)
					}],
					success: (distanceResp) => {
						const distance = distanceResp.result.elements[0].distance
						if (distance > 2000) {
							uni.showToast({
								icon: 'none',
								title: '请移动到距离代驾终点2公里以内'
							})
							return
						}

						const data = {
							orderId: executeOrder.id,
							customerId: executeOrder.customerId,
							status: 5
						};

						// 停止代驾的时候更新订单状态，停止录音。
						ajax(url.updateOrderStatus, 'PUT', data, (resp) => {
							try {
								// 1.停止录音
								stopRecord.value = true
								if (recordManager.value) {
									recordManager.value.stop()
								}

								// 2.更新工作状态
								workStatus.value = '结束代驾'
								uni.setStorageSync('workStatus', '结束代驾')

								// 3.跳转到费用录入页面
								uni.navigateTo({
									url: `/order/enter_fee/enter_fee?orderId=${executeOrder.id}&customerId=${executeOrder.customerId}`
								})
							} catch (e) {
								console.error('结束代驾异常', e)
							}
						})

					},
					fail: (err) => {
						console.error('计算距离失败', err)
						uni.showToast({
							icon: 'none',
							title: '距离计算失败，请检查定位'
						})
					}
				}) */
			}
		})
	}

	/**
	 * 调用手机原生导航
	 */
	function showNavigationHandle() {
		// 导航的终点坐标和地名
		let lat = null;
		let lng = null;
		let destination = null;

		if (workStatus.value === '接客户') {
			// 去接客户，导航到代驾起点（订单的起点是终点）
			lat = Number(executeOrder.startPlaceLocation.latitude)
			lng = Number(executeOrder.startPlaceLocation.longitude)
			destination = executeOrder.startPlace
		} else {
			// 代驾中，导航到代驾终点(订单的终点是终点)
			lat = Number(executeOrder.endPlaceLocation.latitude)
			lng = Number(executeOrder.endPlaceLocation.longitude)
			destination = executeOrder.endPlace
		}

		// 调用手机原生导航APP。
		if (map.value) {
			map.value.openMapApp({
				latitude: lat,
				longitude: lng,
				destination: destination
			})
		}
	}

	/**
	 * 跳转到司乘同显页面
	 */
	function showMoveHandle() {
		uni.navigateTo({
			url: `/execution/move/move?orderId=${executeOrder.id}`
		})
	}

	function showOrderHandle() {}

	/**
	 * 拨打客服电话
	 */
	function callServiceHandle() {
		uni.makePhoneCall({
			phoneNumber: '10086'
		})
	}

	function gohomeHandle() {}

	// 开始接单的方法
	function startWorkHandle() {
		/**
		 * TODO: 检查司机是否可以接单
		 * 1. 没有被禁止接单
		 * 2. 没有未缴纳的罚款
		 * 3. 当天已经做了人脸识别
		 */

		// 假设司机当天的人脸识别通过了。
		uni.setStorageSync('verification', {
			result: true, // 人脸识别通过了
			date: dayjs().format('YYYY-MM-DD') // 当天
		})

		uni.showModal({
			title: "提示信息",
			content: "你要开始接收代驾订单信息？",
			success(resp) {
				if (resp.confirm) {
					uni.vibrateShort({})
					// 停止上一次的语音，再重新播放新的语音
					if (audio.value != null) {
						// 停止上一次的语音播报。
						audio.value.stop();
					}
					// 创建语音播报上下文对象
					const innerAudio = uni.createInnerAudioContext();
					audio.value = innerAudio
					innerAudio.src = '/static/voice/voice_1.mp3'
					innerAudio.play()

					uni.showToast({
						title: "开始接单了",
						icon: 'success',
						complete() {
							// 发送ajax请求，清空GEO缓存，消息队列，上线缓存。
							ajax(url.startWork, "POST", null, () => {})
							uni.setStorageSync("workStatus", "开始接单");
							workStatus.value = "开始接单"
							// 初始化 新订单 和 列表 变量
							newOrder.value = null
							newOrderList.value = []
							// 当前正在执行的订单是没有的，因此要恢复默认值。
							// Object.assign方法的目的:保持 executeOrder 响应式.
							Object.assign(executeOrder, {
								id: '',
								photo: '',
								title: '',
								tel: '',
								customerId: '',
								startPlace: '',
								startPlaceLocation: {},
								endPlace: '',
								endPlaceLocation: {},
								favourFee: '',
								carPlate: '',
								carType: '',
								createTime: ''
							})

							// 创建接收新订单消息的定时器，每隔5秒接收一次新订单消息。
							if (receiveNewOrderTimer == null) {
								receiveNewOrderTimer = createTimer()
							}
						}
					})
				}
			}
		})
	}

	// 停止接单
	function stopWorkHandle() {
		uni.showModal({
			title: '提示信息',
			content: '你要停止接收代驾订单信息？',
			success(resp) {
				if (resp.confirm) {
					uni.vibrateShort({})

					if (audio.value != null) {
						audio.value.stop();
					}

					const innerAudio = uni.createInnerAudioContext();
					audio.value = innerAudio
					innerAudio.src = '/static/voice/voice_2.mp3'
					innerAudio.play()

					uni.showToast({
						title: '停止接单了',
						icon: 'none',
						complete() {
							ajax(url.stopWork, 'POST', null, () => {})
							workStatus.value = '停止接单'
							uni.setStorageSync('workStatus', '停止接单')
							// 初始化新订单和列表变量
							newOrder.value = null
							newOrderList.value = []
							Object.assign(executeOrder, {
								id: '',
								photo: '',
								title: '',
								tel: '',
								customerId: '',
								startPlace: '',
								startPlaceLocation: {},
								endPlace: '',
								endPlaceLocation: {},
								favourFee: '',
								carPlate: '',
								carType: '',
								createTime: ''
							})
							// 销毁接收新订单消息的定时器
							clearInterval(receiveNewOrderTimer)
							receiveNewOrderTimer = null
							playFlag.value = false
						}
					})
				}
			}
		})
	}

	function rechargeHandle() {}

	function confirmCancelOrder() {}

	// 封装一个能够创建定时器的函数，因为这个定时器在很多地方都要使用。
	function createTimer() {
		const timer = setInterval(() => {
			ajax(url.receiveNewOrderMessage, 'GET', null, (resp) => {
				const list = resp.data.result;
				if (list.length > 0) {
					// 有新订单
					// newOrderList 是页面中老数组
					// list 是后端返回的新数组
					// 新数组拼接在前面
					newOrderList.value = list.concat(newOrderList.value);
					// 如果当前没有播放任何订单，就显示新的订单。
					if (playFlag.value == false) {
						// 显示新订单信息
						showNewOrder()
					}
				}
			}, false) // 一定要设置为false，不显示loading。悄悄的获取最新订单数据。
		}, 5000)
		return timer;
	}

	/**
	 * 播放新订单语音，并展示在页面上
	 * 实现流程：
	 * 1. 标记正在播报(playFlag = true)，播报期间不允许抢单(canAcceptOrder=false)
	 * 2. 从待播报队列 newOrderList 中取出第一条订单
	 * 3. 把订单字段同步到newOrder，页面就会立即显示这个订单
	 * 4. 如果开启了听单(settings.listenService)：
	 * 		- 用微信同声传译插件合成语音
	 * 		- 播放语音，播放结束之后允许抢单
	 * 		- 播报结束后判断人脸验证的凭证是否过期（跨天需要重新验证，每天验证一次）
	 * 5. 如果没有开启听单：直接允许抢单。
	 */
	function showNewOrder() {
		// 标记正在播报订单，防止定时器重复触发
		playFlag.value = true;
		// 播报期间不允许抢单
		canAcceptOrder.value = false;

		// 从待播报订单队列当中取出第一条订单。
		const order = newOrderList.value.shift(); // 会从数组中删除
		if (!order) {
			playFlag.value = false;
			return;
		}

		// 将订单中的数据解构
		const {
			orderId,
			distance,
			expectsFee,
			favourFee,
			from,
			to,
			mileage,
			minute
		} = order;

		// 将数据设置到 newOrder 对象上。
		newOrder.value = {
			orderId,
			distance,
			expectsFee,
			favourFee,
			from,
			to,
			mileage,
			minute
		};

		// 如果开启了听单（语音播报），就需要使用微信同声传译插件进行语音合成
		if (settings.listenService) {
			// 创建播放器对象
			const innerAudio = uni.createInnerAudioContext();
			audio.value = innerAudio

			plugin.textToSpeech({
				lang: 'zh_CN',
				tts: true,
				content: `即时订单，距离您${distance}公里，从${from}，到${to}，全程约${mileage}公里，预估代驾费${expectsFee}`,
				success: (resp) => {
					// 获取音频文件
					innerAudio.src = resp.filename
					// 播报
					innerAudio.play()

					// 播报结束之后，允许抢单
					innerAudio.onEnded(() => {
						canAcceptOrder.value = true
						// 判断人脸验证这个凭证是否仍然有效，然后自动接单/手动接单
						checkFaceAndProcessOrder(orderId)
					})
				},
				fail: (err) => {
					console.error("订单语音播报失败", err)
					playFlag.value = false;
					canAcceptOrder.value = true;
					accepting.value = false
				}
			})
		} else {
			// 没开听单，可以直接允许抢单
			canAcceptOrder.value = true
			// 判断人脸验证这个凭证是否仍然有效，然后自动接单/手动接单
			checkFaceAndProcessOrder(orderId)
		}

	}

	// 1.做人脸识别凭证的验证，看看人脸识别是否仍然有效
	// 2.当司机设置了自动接单，则执行自动接单的逻辑。
	// 3.当司机设置了手动接单，则执行手动接单的逻辑。
	function checkFaceAndProcessOrder(orderId) {
		// 1. 校验人脸凭证
		const verification = uni.getStorageSync('verification')
		const today = dayjs().format('YYYY-MM-DD')

		/* if(!verification || verification.date != today){
			// 凭证过期，或者说压根没有进行人脸认证
			playFlag.value = false
			canAcceptOrder.value = false
			uni.redirectTo({
				url: '/identity/face_camera/face_camera?mode=verification'
			})
			return
		} */


		if (settings.autoAccept) {
			// 自动抢单
			ajax(url.acceptNewOrder, 'PUT', {
				orderId
			}, (resp) => {
				const result = resp.data.result
				if (result === "接单成功") {
					// 自动抢单成功了
					uni.showToast({
						title: '接单成功'
					})

					const a = uni.createInnerAudioContext();
					audio.value = a;
					a.src = '/static/voice/voice_3.mp3';
					a.play()

					a.onEnded(() => {
						audio.value = null
						// 停止接单
						ajax(url.stopWork, 'POST', null, () => {})
						// 初始化新订单列表以及变量
						newOrder.value = null;
						newOrderList.value.length = 0
						// 即将执行的订单ID非常重要，因为要使用它发送ajax请求加载正在执行的订单详细信息。
						executeOrder.id = orderId
						// 销毁定时器
						clearInterval(receiveNewOrderTimer)
						receiveNewOrderTimer = null
						playFlag.value = false
						// 加载正在执行的订单。（发送ajax请求，根据订单ID查询订单的详细信息，展示正在执行的订单）
						loadExecuteOrder()
					})
				} else {
					// 自动抢单失败（播报语音：轮播下一个订单，下面没有订单了，递归就结束了。）
					const a = uni.createInnerAudioContext();
					audio.value = a;
					a.src = '/static/voice/voice_4.mp3'
					a.play()

					a.onEnded(() => {
						playFlag.value = false
						if (newOrderList.value.length > 0) {
							showNewOrder()
						} else {
							newOrder.value = null
						}
					})
				}
			}, false)
		} else {
			// 手动抢单（司机自己设置的手动抢单）
			// 只给司机10秒的时间去手动抢单，不抢就丢弃。
			playFlag.value = false;
			setTimeout(() => {
				// 如果司机不是正在手动抢单中，就播放下一个新订单
				if (!accepting.value) {
					// 不能接单
					canAcceptOrder.value = false
					if (newOrderList.value.length > 0) {
						showNewOrder()
					} else {
						newOrder.value = null
					}
				}
			}, 1000 * 10)
		}

	}

	// 封装一个函数，可以加载当前正在执行的订单信息，在其他位置调用即可
	function loadExecuteOrder() {
		ajax(url.searchDriverExecuteOrder, 'GET', {
			orderId: executeOrder.id
		}, (resp) => {
			const result = resp.data.result;
			Object.assign(executeOrder, {
				photo: result.photo,
				title: result.title,
				tel: result.tel,
				customerId: result.customerId,
				startPlace: result.startPlace,
				startPlaceLocation: JSON.parse(result.startPlaceLocation),
				endPlace: result.endPlace,
				endPlaceLocation: JSON.parse(result.endPlaceLocation),
				favourFee: result.favourFee,
				carPlate: result.carPlate,
				carType: result.carType,
				createTime: result.createTime
			})

			workStatus.value = '接客户'
			uni.setStorageSync('workStatus', '接客户')
			uni.setStorageSync('executeOrder', executeOrder)
		})
	}

	// 封装“初始化录音识别管理器”
	function initRecordManager() {
		// 获取 录音识别管理器
		const manager = plugin.getRecordRecognitionManager();

		// 绑定各种回调函数
		// 录音识别的过程
		manager.onRecoginze = function(resp) {
			// 这里是识别的中间结果，比较适合实时字幕效果
		}

		// 录音结束的回调：上传录音
		manager.onStop = function(resp) {
			// 如果当前是“开始代驾”状态，且没有强制停止录音，就继续录音
			if (workStatus.value === '开始代驾' && !stopRecord.value) {
				// 立即开始下一段录音。
				manager.start({
					duration: 20 * 1000,
					lang: 'zh_CN'
				})
			}

			// 核心：当停止录音的时候，我们要将上一段录音处理一下。
			const tempFilePath = resp.tempFilePath
			if (!tempFilePath) return // 防止异常发生时没有文件

			// 上传录音
			recordNum.value += 1 // 录音文件的数量追加 1
			const data = {
				//name: `${executeOrder.id}-${recordNum.value}.mp3`,
				name: `${executeOrder.id}-${Date.now()}.mp3`,
				text: resp.result
			};

			// 文件上传，不是普通的ajax请求。
			uni.uploadFile({
				url: url.uploadRecordFile,
				filePath: tempFilePath,
				name: "file",
				header: {
					token: uni.getStorageSync("token")
				},
				formData: data,
				success(uploadResp) {
					// 成功之后的响应结果uploadResp有一个data属性（data属性的结果是一个字符串）
					let resData = uploadResp.data
					if (typeof resData === 'string') {
						try {
							// 将字符串转换成对象。
							resData = JSON.parse(resData)
						} catch (e) {
							console.error('解析上传响应失败', e)
							return;
						}
					}

					// uploadResp.statusCode 获取的是HTTP响应状态码。
					// resData.code 获取的是业务码
					if (uploadResp.statusCode === 200 && resData.code === 200) {
						console.log('录音文件上传成功')
						// 重点：如果已经结束代驾，重置录音计数和停止标记
						if (stopRecord.value) { // 当结束代驾的时候，stopRecord.value是true
							recordNum.value = 0; // 计数器归0
							stopRecord.value = false; // 停止录音的标记修改为false
						}
					} else {
						// 其他的状态码处理方式
						console.error('录音文件上传失败', resData)
					}
				},
				fail(err) { // 网络没通。
					console.error('录音文件上传失败', err)
				}
			})
		}

		// 开始录音的时候
		manager.onStart = function(resp) {
			console.log('成功开启录音识别')
			if (recordNum.value === 0) {
				// 第一份录音
				uni.vibrateLong({
					complete() {
						uni.showToast({
							icon: 'none',
							title: '请提示乘客系上安全带'
						})
					}
				})
			}
		}

		// 录音发生异常
		manager.onError = function(resp) {
			console.error('录音识别故障', resp.msg)
		}

		return manager;
	}
</script>

<style lang="less">
	@import url('workbench.less');
</style>
