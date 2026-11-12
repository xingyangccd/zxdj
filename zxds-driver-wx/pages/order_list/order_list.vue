<template>
	<view>
		<view class="order-list" v-for="one in monthList" :key="one.month">
			<view class="month">
				<text>{{ one.month }}</text>
				<image src="../../static/order_list/calendar-icon.png" mode="widthFix"></image>
			</view>
			<view class="order" v-for="order in one.orders" :key="order.id" @tap="viewOrderHandle(order.id)">
				<view class="top">
					<view class="date">{{ order.acceptTime }}</view>
					<view class="amount-status">
						<text class="amount" v-if="order.hasOwnProperty('realFee')">￥{{ order.realFee }}</text>
						<text :class="order.style">{{ order.status }}</text>
					</view>
				</view>
				<view class="content">
					<view class="from"><text>{{ order.startPlace }}</text></view>
					<view class="to"><text>{{ order.endPlace }}</text></view>
					<view class="desc"><text>订单编号：{{ order.id }}</text></view>
				</view>
			</view>
		</view>
		<up-notice-bar :text="uTips" />
	</view>
</template>

<script setup>
	import {
		ref,
		getCurrentInstance
	} from 'vue'
	import {
		onLoad,
		onReachBottom,
		onShow,
		onHide
	} from '@dcloudio/uni-app'

	let dayjs = require('dayjs')
	const instance = getCurrentInstance()
	const uTips = ref(null)

	const monthList = ref([])
	const page = ref(1)
	const length = ref(50)
	const isLastPage = ref(false)

	onLoad(() => {})
	onReachBottom(() => {})
	onShow(() => {})
	onHide(() => {})

	function viewOrderHandle(id) {
		uni.navigateTo({
			url: `/order/order/order?orderId=${id}`
		})
	}
</script>

<style lang="less">
	@import url('order_list.less');
</style>
