<template>
	<view class="page">
		<view class="card">
			<view class="total">
				<view>总资产（CNY）</view>
				<view class="recharge" @tap="toRecharge">
					<image src="../static/wallet/recharge.png" mode="widthFix" class="icon"></image>
					<text>充值</text>
				</view>
			</view>
			<view class="balance">
				￥
				<text>{{ balance }}</text>
			</view>
			<view class="item-list">
				<view class="item">
					<view class="title">
						<image src="../static/wallet/icon-1.png" mode="widthFix" class="icon" />
						本月收入
					</view>
					<view class="amount">￥{{ incomeTotalInMonth }}</view>
				</view>
				<view class="item">
					<view class="title">
						<image src="../static/wallet/icon-2.png" mode="widthFix" class="icon" />
						本月支出
					</view>
					<view class="amount">￥{{ paymentTotalInMonth }}</view>
				</view>
				<view class="item">
					<view class="title">
						<image src="../static/wallet/icon-3.png" mode="widthFix" class="icon" />
						今日入账
					</view>
					<view class="amount">￥{{ incomeTotalInDay }}</view>
				</view>
			</view>
		</view>
		<view class="record-container">
			<view class="title">收支明细</view>
			<view class="summary">
				<view class="month" @tap="showListHandle">
					<text>{{ monthLabel }}</text>
					<image src="../static/wallet/icon-4.png" mode="widthFix" class="icon" />
				</view>
				<text class="total">合计：{{ incomeTotalInMonth - paymentTotalInMonth }}元</text>
			</view>
			<view class="record-list">
				<block v-if="recordInMonth.length > 0">
					<view class="record" v-for="one in recordInMonth" :key="one.id">
						<view class="left">
							<view class="date-time">
								<view class="date">
									<image src="../static/wallet/icon-5.png" mode="widthFix" class="date-icon" />
									<text>{{ one.date }}</text>
								</view>
								<view class="time">
									<image src="../static/wallet/icon-6.png" mode="widthFix" class="time-icon" />
									<text>{{ one.time }}</text>
								</view>
							</view>
							<view class="content">
								<view class="type">钱包{{ one.name }}</view>
								<view class="desc">{{ one.remark }}</view>
							</view>
						</view>
						<view class="right">
							<text :class="one.name == '入账' ? 'red' : 'green'">{{ one.amount }}</text>
							<text class="unit">元</text>
						</view>
					</view>
				</block>
				<block v-if="recordInMonth.length == 0">
					<view class="empty">
						<image src="../static/wallet/no-data.png" mode="widthFix" class="no-data"></image>
						<view>没有相关数据</view>
					</view>
				</block>
			</view>
			<u-select v-model="showList" :list="list" @confirm="confirmMonthHandle"></u-select>
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
		onShow,
		onHide
	} from '@dcloudio/uni-app'

	let dayjs = require('dayjs')
	const instance = getCurrentInstance()
	const uTips = ref(null)

	const showList = ref(false)
	const list = ref([])
	const id = ref(null)
	const balance = ref(0)
	const incomeTotalInDay = ref(0)
	const incomeTotalInMonth = ref(0)
	const paymentTotalInMonth = ref(0)
	const recordInMonth = ref([])
	const monthLabel = ref('')
	const monthValue = ref('')

	onShow(() => {})
	onHide(() => {})

	function toRecharge() {
		uni.navigateTo({
			url: '/wallet/recharge/recharge'
		})
	}

	function showListHandle() {
		showList.value = true
	}

	function confirmMonthHandle(resp) {
		monthLabel.value = resp[0].label
		monthValue.value = resp[0].value
	}
</script>

<style lang="less">
	@import url('wallet.less');
</style>
