<template>
	<view class="order-page">
		<view class="custom-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-left" @tap="goBack">
				<text class="nav-back-icon">‹</text>
			</view>
			<view class="nav-title">代驾账单</view>
			<view class="nav-right"></view>
		</view>
		<view class="bg-gradient"></view>
		<view class="glow glow-1"></view>
		<view class="glow glow-2"></view>
		<view class="glow glow-3"></view>

		<view class="page-content">
			<view class="card customer-card">
				<view class="customer-container">
					<view class="avatar-ring">
						<u-avatar :src="photo" mode="square" size="64"></u-avatar>
					</view>
					<view class="info">
						<view class="customer-name">{{ name || '无' }}<text class="tag">代驾司机</text></view>
						<view class="customer-tel">Tel：{{ tel || '--' }}</view>
					</view>
				</view>
			</view>

			<view class="card address-card">
				<view class="address-container">
					<view class="from">
						<text class="dot dot-start"></text>
						<text class="place">{{ startPlace || '获取中...' }}</text>
					</view>
					<view class="dashed-line"></view>
					<view class="to">
						<text class="dot dot-end"></text>
						<text class="place">{{ endPlace || '获取中...' }}</text>
					</view>
				</view>
			</view>

			<view class="card order-card">
				<view class="order-container">
					<view class="order-row"><text class="label">订单号码</text><text
							class="value">{{ orderId || '--' }}</text></view>
					<view class="order-row"><text class="label">下单时间</text><text
							class="value">{{ createTime || '--' }}</text></view>
					<view class="order-row"><text class="label">客户红包</text><text
							class="value">{{ favourFee === '暂无' ? '--' : favourFee + '元' }}</text></view>
					<view class="order-row"><text class="label">系统奖励</text><text
							class="value">{{ incentiveFee === '暂无' ? '--' : incentiveFee + '元' }}</text></view>
					<view class="order-row"><text class="label">代驾车型</text><text
							class="value">{{ carType || '--' }}</text></view>
					<view class="order-row"><text class="label">代驾车牌</text><text
							class="value">{{ carPlate || '--' }}</text></view>
				</view>
			</view>

			<view class="card fee-card">
				<view class="setion-title">
					<view class="title-bar"></view>
					<image src="../../static/order/money.png" mode="widthFix"></image>
					<text>基础收费</text>
				</view>
				<view class="section-content">
					<view class="item">
						<view class="left">
							<text class="item-title">里程费（{{ realMileage || '--' }}公里）</text>
							<text
								class="item-desc">时段收费（{{ baseMileagePrice || '--' }}元{{ baseMileage || '--' }}公里，超出每公里{{ exceedMileagePrice || '--' }}元）</text>
						</view>
						<view class="right">{{ mileageFee || '暂无' }}</view>
					</view>
					<view class="item">
						<view class="left">
							<text class="item-title">时长费（{{ waitingMinute }}分钟）</text>
							<text
								class="item-desc">免费{{ baseMinute || '--' }}分钟，超出部分每分钟{{ exceedMinutePrice || '--' }}元</text>
						</view>
						<view class="right">{{ waitingFee || '暂无' }}</view>
					</view>
					<view class="item">
						<view class="left">
							<text class="item-title">返程费（{{ returnMileage }}公里）</text>
							<text
								class="item-desc">总里程超过{{ baseReturnMileage || '--' }}公里，每公里{{ exceedReturnPrice || '--' }}元</text>
						</view>
						<view class="right">{{ returnFee || '暂无' }}</view>
					</view>
				</view>
			</view>

			<view class="card fee-card">
				<view class="setion-title">
					<view class="title-bar"></view>
					<image src="../../static/order/money.png" mode="widthFix"></image>
					<text>额外收费</text>
				</view>
				<view class="section-content">
					<view class="item">
						<view class="left">
							<text class="item-title">停车费</text>
							<text class="item-desc">如果代驾司机预付停车费，该费用将计入订单费用</text>
						</view>
						<view class="right">{{ parkingFee || '暂无' }}</view>
					</view>
					<view class="item">
						<view class="left">
							<text class="item-title">路桥费</text>
							<text class="item-desc">如果代驾司机预付停车费，该费用将计入订单费用</text>
						</view>
						<view class="right">{{ tollFee || '暂无' }}</view>
					</view>
					<view class="item">
						<view class="left">
							<text class="item-title">其他费用</text>
							<text class="item-desc">代驾过程中产生的其他费用</text>
						</view>
						<view class="right">{{ otherFee || '暂无' }}</view>
					</view>
				</view>
			</view>

			<view class="card fee-card total-card">
				<view class="setion-title">
					<view class="title-bar"></view>
					<image src="../../static/order/money.png" mode="widthFix"></image>
					<text>总金额</text>
				</view>
				<view class="section-content">
					<view class="content-container">
						<view class="left">
							<view class="content"><text class="label">汇总合计</text><text class="num">¥ {{ total }}
									元</text></view>
							<view class="content">
								<text class="label">减免金额</text>
								<text class="num" v-if="status == 6">¥ {{ voucher.discount }} 元</text>
								<text class="num" v-if="status != 6">¥ {{ voucherFee }} 元</text>
							</view>
							<view class="content"><text class="label">实付金额</text><text class="num red">¥ {{ realPay }}
									元</text></view>
						</view>
						<image :src="img" mode="widthFix" class="img"></image>
					</view>
				</view>
			</view>

			<view class="operate-container" v-show="status == 6">
				<button class="btn" @tap="payHandle">
					<view class="btn-bg"></view>
					<text class="btn-text">立即付款</text>
					<text class="btn-arrow">→</text>
				</button>
			</view>

			<view class="card fee-card" v-if="status >= 7">
				<view class="setion-title">
					<view class="title-bar"></view>
					<image src="../../static/order/rate.png" mode="widthFix"></image>
					<text>客户评价</text>
				</view>
				<view class="section-content">
					<view class="remark-container">
						<view class="remark-rate">
							<view class="photo"><u-avatar :src="photo" size="60" /></view>
							<view class="rate">
								<u-rate :count="customerComment.count" v-model="customerComment.value" disabled="true"
									active-color="#2074FD" size="40" />
							</view>
						</view>
						<view class="remark">{{ customerComment.remark }}</view>
					</view>
				</view>
			</view>

			<view class="card contact-card">
				<view class="contact"><text class="label">客服电话：</text><text class="tel">010-12345678</text></view>
				<view class="contact"><text class="label">服务监督电话：</text><text class="tel">010-12345679</text></view>
			</view>
		</view>

		<u-popup v-model="comment.showComment" mode="center" border-radius="14" width="550rpx" height="580rpx">
			<view class="comment-title">智行代驾服务您满意吗</view>
			<view class="comment-desc">请给司机的服务一点评价吧~</view>
			<view class="comment-rate">
				<u-rate :count="comment.count" v-model="comment.value" active-color="#FFBB2A" size="40"></u-rate>
			</view>
			<u-input v-model="comment.remark" type="textarea" :border="false" :clearable="false"
				placeholder="说说哪里好,其他顾客想知道~" :custom-style="comment.remarkStyle" />
			<u-button type="success" :custom-style="comment.btnStyle" @click="insertComment">确定</u-button>
		</u-popup>
	</view>
</template>
<script setup>
	import {
		ref,
		reactive
	} from 'vue'
	import {
		ajax,
		url
	} from '@/utils/common.js'
	import {
		onLoad
	} from '@dcloudio/uni-app'

	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)

	const orderId = ref(null)
	const customerId = ref(null)
	const driverId = ref(null)
	const photo = ref(null)
	const name = ref(null)
	const tel = ref(null)
	const startPlace = ref(null)
	const endPlace = ref(null)
	const createTime = ref(null)
	const favourFee = ref('暂无')
	const incentiveFee = ref('暂无')
	const carPlate = ref(null)
	const carType = ref(null)
	const status = ref(null)
	const realMileage = ref(null)
	const mileageFee = ref(null)
	const baseMileagePrice = ref('')
	const baseMileage = ref('')
	const exceedMileagePrice = ref('')
	const waitingFee = ref('暂无')
	const baseMinute = ref('')
	const waitingMinute = ref('--')
	const exceedMinutePrice = ref('')
	const returnFee = ref('暂无')
	const baseReturnMileage = ref('')
	const exceedReturnPrice = ref('')
	const returnMileage = ref('--')
	const parkingFee = ref('暂无')
	const tollFee = ref('暂无')
	const otherFee = ref('暂无')
	const total = ref('--')
	const voucherFee = ref('--')
	const realPay = ref('--')
	const img = ref('')

	const voucher = reactive({
		id: null,
		discount: 0,
		voucherId: null
	})

	const comment = reactive({
		showComment: false,
		count: 5,
		value: 0,
		remark: null,
		remarkStyle: {
			'background-color': '#f8f8f8',
			padding: '20rpx',
			margin: '0 40rpx'
		},
		btnStyle: {
			margin: '40rpx 40rpx 30rpx 40rpx'
		}
	})

	const customerComment = reactive({
		count: 5,
		rate: 0,
		value: 0,
		remark: '[ 客户没有评价，系统默认为好评~ ]'
	})

	onLoad((options) => {
		orderId.value = options.orderId;

		ajax(url.searchOrderById, 'GET', {
			orderId: orderId.value
		}, (resp) => {
			let result = resp.data.result;

			driverId.value = result.driverId;
			name.value = result.name;
			photo.value = result.photo;
			tel.value = result.tel;
			startPlace.value = result.startPlace;
			endPlace.value = result.endPlace;
			createTime.value = result.createTime;
			favourFee.value = result.favourFee;
			incentiveFee.value = result.incentiveFee;
			carPlate.value = result.carPlate;
			carType.value = result.carType;

			// 订单的状态
			let statusVal = result.status;
			// 将订单状态赋值给 status
			status.value = statusVal;

			if ([5, 6, 7, 8].includes(statusVal)) {
				realMileage.value = result.realMileage;
				mileageFee.value = result.mileageFee;
				waitingFee.value = result.waitingFee;
				waitingMinute.value = result.waitingMinute;
				returnFee.value = result.returnFee;
				returnMileage.value = result.returnMileage;
				parkingFee.value = result.parkingFee;
				tollFee.value = result.tollFee;
				otherFee.value = result.otherFee;
				total.value = result.total;
				voucherFee.value = result.voucherFee;
			}

			baseMileagePrice.value = result.baseMileagePrice;
			baseMileage.value = result.baseMileage;
			exceedMileagePrice.value = result.exceedMileagePrice;
			baseMinute.value = result.baseMinute;
			exceedMinutePrice.value = result.exceedMinutePrice;
			baseReturnMileage.value = result.baseReturnMileage;
			exceedReturnPrice.value = result.exceedReturnPrice;
			realPay.value = '--';

			if ([2, 3].includes(statusVal)) {
				img.value = '../../static/order/icon-1.png';
			} else if ([4].includes(statusVal)) {
				img.value = '../../static/order/icon-2.png';
			} else if ([5, 6].includes(statusVal)) {
				img.value = '../../static/order/icon-3.png';
			} else if ([7].includes(statusVal)) {
				img.value = '../../static/order/icon-4.png';
				realPay.value = result.realPay;
			} else if ([8, 9, 10, 11, 12].includes(statusVal)) {
				img.value = '../../static/order/icon-5.png';
				realPay.value = result.realPay;
			}
		});
	})



	function goBack() {
		const pages = getCurrentPages()
		if (pages.length > 1) {
			uni.navigateBack({
				delta: 1
			})
		} else {
			uni.switchTab({
				url: '/pages/workbench/workbench'
			})
		}
	}

	function payHandle() {
		uni.showModal({
			title: '提示消息',
			content: '您确定支付该订单？',
			success(resp) {
				if (!resp.confirm) return

				const data = {
					orderId: orderId.value
				}

				// 这个请求的作用：创建微信支付单。
				ajax(url.createWxPayment, 'POST', data, (resp) => {
					// 程序执行到这里，微信支付单创建成功了。
					// 获取后端返回的组装参数。
					const {
						timeStamp,
						nonceStr,
						package: pkg,
						paySign,
						signType
					} = resp.data.result;

					// 拉起/调起小程序的微信支付。（JS代码）
					uni.requestPayment({
						timeStamp,
						nonceStr,
						package: pkg,
						paySign,
						signType,
						success() {
							console.log('付款成功')
							// 主动发起查询请求(查询是否支付成功了，如果支付成功，则更新订单、发放司机奖励、分账...)
							ajax(url.updateOrderAboutPayment, 'POST', {
								orderId: orderId.value
							}, (resp) => {
								const result = resp.data.result
								if (result === '付款成功') {
									uni.showToast({
										icon: 'success',
										title: '付款成功'
									})
									setTimeout(() => {
										uni.switchTab({
											url: '/pages/workbench/workbench'
										})
									}, 2000)
								} else {
									uni.showToast({
										icon: 'error',
										title: '付款异常，如有疑问可以联系客服'
									})
								}
							})
						},
						fail(error) {
							console.error('支付失败', error)
							uni.showToast({
								icon: 'error',
								title: '付款失败'
							})
						}
					})
				})
			}
		})
	}

	function insertComment() {}
</script>

<style lang="less">
	@import url('order.less');
</style>
