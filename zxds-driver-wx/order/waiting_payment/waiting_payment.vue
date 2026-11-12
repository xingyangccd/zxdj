<template>
	<view class="page">
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="nav-title">等待付款</view>
			<view class="nav-placeholder"></view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<view class="waiting-container">
				<image src="../static/waiting_payment/payment.png" mode="widthFix" class="payment"></image>
				<view class="title">等待顾客付款</view>
				<view class="second">{{ i }}s</view>
				<button class="btn" @tap="checkPaymentHandle">未收到付款通知</button>
			</view>

			<view class="notice-container">
				<view class="notice-title">
					<u-icon name="error-circle-fill" color="#fea802" size="18"></u-icon>
					<text class="title-text">注意事项</text>
				</view>

				<view class="desc">
					<text class="num">1.</text>
					<text class="content">如果顾客已经成功付款，但是司机端未能收到付款成功的通知消息，可以点击“未收到顾客付款通知”的链接。系统将立即向微信支付平台查询顾客的付款结果；</text>
				</view>

				<view class="desc">
					<text class="num">2.</text>
					<text
						class="content">如果顾客已经成功付款，并且您的微信上面也收到了分账通知消息，但是本页面并没有接收到用户付款成功的通知消息。您可以点击已经收款按钮，系统将立即确认付款结果；</text>
				</view>

				<view class="desc">
					<text class="num">3.</text>
					<text
						class="content">如果顾客已经成功付款，并且本页面也接收到了付款成功的通知，但是您没有收到系统分账的通知。您可以耐心等待，因为本系统将会审核您在代驾中是否存在违规，并且扣除相关罚款，再给您分账。所以在顾客付款成功之后的2小时之内，您会收到本次代驾的分账收入，请您耐心等待，如有技术问题，请拨打010-99999999</text>
				</view>
			</view>
			<view style="height: 50rpx;"></view>
		</scroll-view>

		<up-toast ref="uToastRef"></up-toast>
	</view>
</template>

<script setup>
	import {
		ref,
		onUnmounted
	} from 'vue';
	import {
		onLoad,
		onUnload,
		onShow,
		onHide
	} from '@dcloudio/uni-app';
	import {
		ajax,
		url
	} from '@/utils/common.js';

	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight);
	const uToastRef = ref(null);

	const i = ref(0);
	const orderId = ref(null);
	const timer = ref(null);
	let pollTimer = null;
	// 防止重复提交
	const isChecking = ref(false);

	function goBack() {
		uni.navigateBack();
	}

	// 底层每隔3秒会发送一次AJAX请求，获取订单最新状态。
	function pollOrderStatus() {
		if (!orderId.value || isChecking.value) return;
		isChecking.value = true;

		let data = {
			orderId: orderId.value
		};
		ajax(
			url.searchOrderStatus,
			'GET',
			data,
			function(resp) {
				isChecking.value = false;

				if (!resp.data.hasOwnProperty('result')) {
					// 清理定时器
					clearInterval(timer.value);
					clearInterval(pollTimer);

					uni.showToast({
						icon: 'none',
						title: '没有找到订单'
					});
				} else {
					let result = resp.data.result;
					if (result == 7 || result == 8) {
						// 清理定时器
						clearInterval(timer.value);
						clearInterval(pollTimer);

						uni.showToast({
							title: '客户已付款'
						});
						uni.setStorageSync('workStatus', '停止接单');
						setTimeout(function() {
							uni.switchTab({
								url: '/pages/workbench/workbench'
							});
						}, 2500);
					}
				}
			},
			false
		);
	}

	// 司机手动点击页面中的 未收到付款通知 按钮时执行的回调。
	function checkPaymentHandle() {
		if (!orderId.value || isChecking.value) return;
		isChecking.value = true;

		let data = {
			orderId: orderId.value
		};
		ajax(
			url.updateOrderAboutPayment,
			'POST',
			data,
			function(resp) {
				isChecking.value = false;
				let result = resp.data.result;

				if (result == '付款成功') {
					// 清理定时器
					clearInterval(timer.value);
					clearInterval(pollTimer);

					uni.showToast({
						title: '客户已付款'
					});
					uni.setStorageSync('workStatus', '停止接单');
					setTimeout(function() {
						uni.switchTab({
							url: '/pages/workbench/workbench'
						});
					}, 2500);
				} else {
					uni.showToast({
						icon: 'none',
						title: '未检测到成功付款'
					});
				}
			},
			false
		);
	}

	onLoad((options) => {
		orderId.value = options.orderId;

		timer.value = setInterval(() => {
			i.value++;
		}, 1000);

		pollTimer = setInterval(pollOrderStatus, 3000);
	});

	onShow(() => {
		if (!pollTimer) {
			pollTimer = setInterval(pollOrderStatus, 3000);
		}
	});

	onHide(() => {
		clearInterval(timer.value);
		clearInterval(pollTimer);
	});

	onUnmounted(() => {
		clearInterval(timer.value);
		clearInterval(pollTimer);
	});

	onUnload(() => {
		clearInterval(timer.value);
		clearInterval(pollTimer);
	});
</script>

<style lang="less">
	@import url('waiting_payment.less');
</style>
