<template>
	<view>
		<view class="tabs">
			<up-tabs ref="uTabs" :list="list" :current="current" @change="tabsChange" :is-scroll="false"
				lineWidth="60" />
		</view>

		<scroll-view scroll-y :style="swipperStyle" @scrolltolower="onreachBottom" v-if="current==0">
			<view class="voucher" v-for="one in voucherList" :key="one.id">
				<view class="left green">
					<view class="row"><text class="amount">{{ one.discount }}</text></view>
					<view class="row">
						<text class="desc" v-if="one.withAmount > 0">满{{ one.withAmount }}元可用</text>
						<text class="desc" v-if="one.withAmount == 0">无最低消费</text>
					</view>
				</view>
				<view class="right">
					<view class="row"><text class="desc">{{ one.remark }}</text></view>
					<view class="row">
						<text class="desc" v-if="one.timeType == 1">领券后{{ one.days }}天内有效</text>
						<text class="desc" v-if="one.timeType == 2">{{ one.startTime }} ~ {{ one.endTime }}</text>
						<text class="desc" v-if="one.timeType == null">{{ one.createTime }} ~ 2099-01-01</text>
					</view>
				</view>
			</view>
		</scroll-view>

		<scroll-view scroll-y :style="swipperStyle" @scrolltolower="onreachBottom" v-if="current==1">
			<view class="voucher" v-for="one in voucherList" :key="one.id">
				<view class="left orange">
					<view class="row"><text class="amount">{{ one.discount }}</text></view>
					<view class="row">
						<text class="desc" v-if="one.withAmount > 0">满{{ one.withAmount }}元可用</text>
						<text class="desc" v-if="one.withAmount == 0">无最低消费</text>
					</view>
				</view>
				<view class="right">
					<view class="row"><text class="desc">{{ one.remark }}</text></view>
					<view class="row">
						<text class="desc" v-if="one.timeType == 1">领券后{{ one.days }}天内有效</text>
						<text class="desc" v-if="one.timeType == 2">{{ one.startTime }} ~ {{ one.endTime }}</text>
						<text class="desc" v-if="one.timeType == null">{{ one.createTime }} ~ 2099-01-01</text>
					</view>
				</view>
			</view>
		</scroll-view>

		<scroll-view scroll-y :style="swipperStyle" @scrolltolower="onreachBottom" v-if="current==2">
			<view class="voucher" v-for="one in voucherList" :key="one.id" @tap="takeVoucher(one.id, one.uuid)">
				<view class="left red">
					<view class="row"><text class="amount">{{ one.discount }}</text></view>
					<view class="row">
						<text class="desc" v-if="one.withAmount > 0">满{{ one.withAmount }}元可用</text>
						<text class="desc" v-if="one.withAmount == 0">无最低消费</text>
					</view>
				</view>
				<view class="right">
					<view class="row"><text class="desc">{{ one.remark }}</text></view>
					<view class="row">
						<text class="desc" v-if="one.timeType == 1">领券后{{ one.days }}天内有效</text>
						<text class="desc" v-if="one.timeType == 2">{{ one.startTime }} ~ {{ one.endTime }}</text>
						<text class="desc" v-if="one.timeType == null">{{ one.createTime }} ~ 2099-01-01</text>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'
	import {
		onLoad
	} from '@dcloudio/uni-app'

	const uTabs = ref(null)

	const list = ref([{
			name: '未使用',
			count: 0
		},
		{
			name: '已使用',
			count: 0
		},
		{
			name: '待领取',
			count: 0
		}
	])
	const current = ref(0)
	const swipperStyle = ref('')
	const page = ref(1)
	const length = ref(10)
	const voucherList = ref([])
	const isLastPage = ref(false)

	onLoad(() => {})

	function tabsChange(index) {
		current.value = index
	}

	function onreachBottom() {}

	function takeVoucher(id, uuid) {}
</script>

<style lang="less">
	@import url('voucher_list.less');
</style>
