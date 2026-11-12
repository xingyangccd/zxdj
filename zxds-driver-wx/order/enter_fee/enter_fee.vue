<template>
	<view class="page">
		<!-- 自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">‹</view>
			<view class="nav-title">请输入相关费用</view>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 内容滚动区 -->
		<scroll-view scroll-y class="content-scroll">
			<!-- 顶部通知栏 -->
			<view class="notice-wrap">
				<u-notice-bar :text="notice" bgColor="transparent" color="#ff8a3d"></u-notice-bar>
			</view>

			<!-- 步骤条卡片 -->
			<view class="step-card">
				<u-steps :current="currentStep">
					<u-steps-item v-for="(item, index) in numList" :key="index" :title="item.name"></u-steps-item>
				</u-steps>
			</view>

			<view class="title">【 相关费用 】</view>

			<view class="fee-card">
				<u-cell-group :border="false">
					<u-cell icon="red-packet-fill" title="路桥费" :value="tollFee + '元'" :isLink="true"
						@click="enterFee('toll')"></u-cell>
					<u-cell icon="red-packet-fill" title="停车费" :value="parkingFee + '元'" :isLink="true"
						@click="enterFee('parking')"></u-cell>
					<u-cell icon="red-packet-fill" title="其他费用" :value="otherFee + '元'" :isLink="true"
						@click="enterFee('other')"></u-cell>
				</u-cell-group>
			</view>

			<button class="btn" @tap="submit">输入好了</button>
			<view style="height: 60rpx;"></view>
		</scroll-view>

		<up-toast ref="myToast"></up-toast>
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
		checkValidFee,
		ajax,
		url
	} from '@/utils/common.js';

	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight);

	const notice = ref('请认真填写相关金额，发送账单之后则不能修改，请慎重填写费用金额，精确到小数点后两位');

	const numList = ref([{
			name: '结束代驾'
		},
		{
			name: '输入费用'
		},
		{
			name: '提交账单'
		},
		{
			name: '用户付款'
		}
	]);

	const currentStep = ref(0);
	const orderId = ref(null);
	const customerId = ref(null);
	const tollFee = ref('0.00');
	const parkingFee = ref('0.00');
	const otherFee = ref('0.00');

	const myToast = ref(null);

	function goBack() {
		uni.navigateBack();
	}

	// 司机手动输入各项费用。
	function enterFee(type) {
		if (type === 'toll') {
			uni.showModal({
				title: '路桥费',
				content: tollFee.value,
				editable: true,
				placeholderText: '输入路桥费',
				showCancel: false,
				success(resp) {
					if (checkValidFee(myToast.value, resp.content, '路桥费')) {
						console.log("输入的路桥费：", resp.content)
						tollFee.value = resp.content
					}
				}
			})
		} else if (type === 'parking') {
			uni.showModal({
				title: '停车费',
				content: parkingFee.value,
				editable: true,
				placeholderText: '请输入停车费',
				showCancel: false,
				success(resp) {
					if (checkValidFee(myToast.value, resp.content, '停车费')) {
						parkingFee.value = resp.content
					}
				}
			})
		} else if (type === 'other') {
			uni.showModal({
				title: '其他费用',
				content: otherFee.value,
				editable: true,
				placeholderText: '请输入其他费用',
				showCancel: false,
				success(resp) {
					if (checkValidFee(myToast.value, resp.content, '其他费用')) {
						otherFee.value = resp.content
					}
				}
			})
		}
	}

	function submit() {
		if (checkValidFee(myToast.value, tollFee.value, '路桥费') &&
			checkValidFee(myToast.value, parkingFee.value, '停车费') &&
			checkValidFee(myToast.value, otherFee.value, '其他费用')) {
			uni.showModal({
				title: '提示信息',
				content: '您确认要提交以上相关费用？',
				success(resp) {
					if (resp.confirm) {
						let data = {
							tollFee: tollFee.value,
							parkingFee: parkingFee.value,
							otherFee: otherFee.value,
							orderId: orderId.value
						}
						ajax(url.updateBillFee, 'PUT', data, (resp) => {
							// 更新订单 更新账单 插入分账记录
							// 跳转到账单详情页面
							uni.navigateTo({
								url: `../order_bill/order_bill?orderId=${orderId.value}&customerId=${customerId.value}`
							})
						})
					}
				}
			})
		}
	}

	onLoad((options) => {
		orderId.value = options.orderId;
		customerId.value = options.customerId;
	});
	onShow(() => {});
	onHide(() => {});
</script>

<style lang="less">
	@import url('enter_fee.less');
</style>
