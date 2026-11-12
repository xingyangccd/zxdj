<template>
	<view class="page">
		<!-- 自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">‹</view>
			<view class="nav-title">账单明细</view>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 内容滚动区 -->
		<scroll-view scroll-y class="content-scroll">

			<!-- 基础收费 -->
			<view class="card">
				<view class="setion-title">
					<image src="../static/order/money.png" mode="widthFix"></image>
					<text>基础收费</text>
				</view>
				<view class="section-content">
					<view class="item">
						<view class="left">
							<text class="item-title">里程费（{{ realMileage }}公里）</text>
							<text
								class="item-desc">时段收费（{{ baseMileagePrice }}元{{ baseMileage }}公里，超出每公里{{ exceedMileagePrice }}元）</text>
						</view>
						<view class="right">{{ mileageFee }}</view>
					</view>
					<view class="item">
						<view class="left">
							<text class="item-title">时长费（{{ waitingMinute }}分钟）</text>
							<text class="item-desc">免费{{ baseMinute }}分钟，超出部分每分钟{{ exceedMinutePrice }}元</text>
						</view>
						<view class="right">{{ waitingFee }}</view>
					</view>
					<view class="item">
						<view class="left">
							<text class="item-title">返程费（{{ returnMileage }}公里）</text>
							<text class="item-desc">总里程超过{{ baseReturnMileage }}公里，每公里{{ exceedReturnPrice }}元</text>
						</view>
						<view class="right">{{ returnFee }}</view>
					</view>
				</view>
			</view>

			<!-- 额外收费 -->
			<view class="card">
				<view class="setion-title">
					<image src="../static/order/money.png" mode="widthFix"></image>
					<text>额外收费</text>
				</view>
				<view class="section-content">
					<view class="item">
						<view class="left">
							<text class="item-title">停车费</text>
							<text class="item-desc">如果代驾司机预付停车费，该费用将计入订单费用</text>
						</view>
						<view class="right">{{ parkingFee }}</view>
					</view>
					<view class="item">
						<view class="left">
							<text class="item-title">路桥费</text>
							<text class="item-desc">如果代驾司机预付停车费，该费用将计入订单费用</text>
						</view>
						<view class="right">{{ tollFee }}</view>
					</view>
					<view class="item">
						<view class="left">
							<text class="item-title">其他费用</text>
							<text class="item-desc">代驾过程中产生的其他费用</text>
						</view>
						<view class="right">{{ otherFee }}</view>
					</view>
				</view>
			</view>

			<!-- 奖励费 -->
			<view class="card">
				<view class="setion-title">
					<image src="../static/order/money.png" mode="widthFix"></image>
					<text>奖励费</text>
				</view>
				<view class="section-content">
					<view class="item">
						<view class="left">
							<text class="item-title">客户好处费</text>
							<text class="item-desc">代驾客户赠与的红包奖励</text>
						</view>
						<view class="right">{{ favourFee }}</view>
					</view>
					<view class="item">
						<view class="left">
							<text class="item-title">系统奖励费</text>
							<text class="item-desc">智行代驾系统激励代驾司机的奖励</text>
						</view>
						<view class="right">{{ incentiveFee }}</view>
					</view>
				</view>
			</view>

			<!-- 总金额 -->
			<view class="card">
				<view class="setion-title">
					<image src="../static/order/money.png" mode="widthFix"></image>
					<text>总金额</text>
				</view>
				<view class="section-content">
					<view class="content-container">
						<view class="left">
							<view class="content big">
								【汇总合计】
								<text class="red">¥ {{ total }} 元</text>
							</view>
							<view class="content big">
								【代缴个税】
								<text class="red">¥ {{ taxFee }} 元</text>
							</view>
							<view class="content big">
								【实际收入】
								<text class="red">¥ {{ driverIncome }} 元</text>
							</view>
						</view>
						<image :src="img" mode="widthFix" class="img"></image>
					</view>
				</view>
			</view>

			<button class="btn" @tap="sendOrderBill">发送账单</button>
			<view style="height: 60rpx;"></view>
		</scroll-view>

		<u-toast ref="uToastRef" />
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue';
	import {
		onLoad,
		onShow,
		onHide
	} from '@dcloudio/uni-app';
	import {
		ajax,
		url
	} from '@/utils/common.js';

	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight);

	const orderId = ref(null);
	const customerId = ref(null);
	const favourFee = ref('暂无');
	const incentiveFee = ref('暂无');
	const realMileage = ref('--');
	const mileageFee = ref('暂无');
	const baseMileagePrice = ref('');
	const baseMileage = ref('');
	const exceedMileagePrice = ref('');

	const waitingFee = ref('暂无');
	const baseMinute = ref('');
	const waitingMinute = ref('--');
	const exceedMinutePrice = ref('');
	const returnFee = ref('暂无');
	const baseReturnMileage = ref('');
	const exceedReturnPrice = ref('');
	const returnMileage = ref('--');
	const parkingFee = ref('暂无');
	const tollFee = ref('暂无');
	const otherFee = ref('暂无');
	const total = ref('--');
	const taxFee = ref('--');
	const driverIncome = ref('--');

	const img = ref('');

	const uToastRef = ref(null);

	function goBack() {
		uni.navigateBack();
	}

	function sendOrderBill() {
		uni.showModal({
			title: '提示信息',
			content: '是否发送代驾账单给客户',
			success(resp) {
				if (resp.confirm) {
					const data = {
						orderId: orderId.value,
						customerId: customerId.value,
						status: 6
					}
					// 发送ajax请求。
					ajax(url.updateOrderStatus, 'PUT', data, (resp) => {
						uni.setStorageSync('workStatus', '等待付款')
						// 跳转到等待乘客付款的页面
						/* uni.navigateTo({

						}) */
					})
				}
			}
		})
	}

	onLoad((options) => {
		orderId.value = options.orderId;
		customerId.value = options.customerId;

		// 准备数据
		let data = {
			orderId: orderId.value
		}

		ajax(url.searchReviewDriverOrderBill, 'GET', data, (resp) => {
			const result = resp.data.result
			if (!result) return

			favourFee.value = result.favourFee;
			incentiveFee.value = result.incentiveFee;
			realMileage.value = result.realMileage;
			mileageFee.value = result.mileageFee;
			baseMileagePrice.value = result.baseMileagePrice;
			baseMileage.value = result.baseMileage;
			exceedMileagePrice.value = result.exceedMileagePrice;
			waitingFee.value = result.waitingFee;
			baseMinute.value = result.baseMinute;
			waitingMinute.value = result.waitingMinute;
			exceedMinutePrice.value = result.exceedMinutePrice;
			returnFee.value = result.returnFee;
			baseReturnMileage.value = result.baseReturnMileage;
			exceedReturnPrice.value = result.exceedReturnPrice;
			returnMileage.value = result.returnMileage;
			parkingFee.value = result.parkingFee;
			tollFee.value = result.tollFee;
			otherFee.value = result.otherFee;
			total.value = result.total;
			driverIncome.value = result.driverIncome;
			taxFee.value = result.taxFee;
		})
	});

	onShow(() => {});
	onHide(() => {});
</script>

<style lang="less">
	@import url('order_bill.less');
</style>
