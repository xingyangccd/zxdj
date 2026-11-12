<template>
	<view>
		<view class="tab-container">
			<up-tabs ref="uTabs" :list="list" :current="tabCurrent" @change="tabsChange" :is-scroll="false"
				lineWidth="60"></up-tabs>
		</view>
		<swiper :current="swiperCurrent" @transition="transition" @animationfinish="animationfinish"
			:style="swipperHeight">
			<swiper-item class="swiper-item">
				<scroll-view :style="swipperHeight" @scrolltolower="onreachBottom" scroll-y>
					<view class="fine-container" v-for="one in fineList" :key="one.id">
						<view class="top">
							<text class="fine-id">罚款单号：{{ one.id }}</text>
							<text class="fine-status red" v-if="one.status == 1">【 未交款 】</text>
							<text class="fine-status green" v-if="one.status == 2">【 已交款 】</text>
						</view>
						<view class="summary">
							<view class="letter orange" v-if="one.status == 1">罚</view>
							<view class="letter gray" v-if="one.status == 2">罚</view>
							<view class="content">
								<view class="remark">{{ one.remark }}</view>
								<view class="date">{{ one.createTime }}</view>
							</view>
						</view>
						<view class="bottom">
							<view class="amount">
								<image src="../static/fine_list/money-icon.png" mode="widthFix" />
								罚金面额：
								<text>{{ one.amount }}元</text>
							</view>
							<button class="pay-btn" v-if="one.status == 1"
								@tap="payHandle(one.id, one.amount)">立即支付</button>
						</view>
					</view>
				</scroll-view>
			</swiper-item>
			<swiper-item class="swiper-item">
				<scroll-view :style="swipperHeight" @scrolltolower="onreachBottom" scroll-y>
					<view class="fine-container" v-for="one in fineList" :key="one.id">
						<view class="top">
							<text class="fine-id">罚款单号：{{ one.id }}</text>
							<text class="fine-status red">【 未交款 】</text>
						</view>
						<view class="summary">
							<view class="letter orange">罚</view>
							<view class="content">
								<view class="remark">{{ one.remark }}</view>
								<view class="date">{{ one.createTime }}</view>
							</view>
						</view>
						<view class="bottom">
							<view class="amount">
								<image src="../static/fine_list/money-icon.png" mode="widthFix" />
								罚金面额：
								<text>{{ one.amount }}元</text>
							</view>
							<button class="pay-btn" @tap="payHandle(one.id, one.amount)">立即支付</button>
						</view>
					</view>
				</scroll-view>
			</swiper-item>
			<swiper-item class="swiper-item">
				<scroll-view :style="swipperHeight" @scrolltolower="onreachBottom" scroll-y>
					<view class="fine-container" v-for="one in fineList" :key="one.id">
						<view class="top">
							<text class="fine-id">罚款单号：{{ one.id }}</text>
							<text class="fine-status green">【 已交款 】</text>
						</view>
						<view class="summary">
							<view class="letter gray">罚</view>
							<view class="content">
								<view class="remark">{{ one.remark }}</view>
								<view class="date">{{ one.createTime }}</view>
							</view>
						</view>
						<view class="bottom">
							<view class="amount">
								<image src="../static/fine_list/money-icon.png" mode="widthFix" />
								罚金面额：
								<text>{{ one.amount }}元</text>
							</view>
						</view>
					</view>
				</scroll-view>
			</swiper-item>
		</swiper>
		<up-keyboard default="" ref="uKeyboard" mode="number" :mask="true" :mask-close-able="false" :dot-enabled="false"
			v-model="showKeyboard" :safe-area-inset-bottom="true" :tooltip="false" @change="keyboardChangeHandle"
			@backspace="backspaceHandle">
			<view>
				<view class="u-text-center u-padding-30 money">
					<text>¥ {{ amount }}</text>
					<view class="close" @tap="showKeyboardHandle(false)">
						<up-icon name="close" color="#333333" size="32"></up-icon>
					</view>
				</view>
				<view class="u-flex u-row-center">
					<up-message-input mode="box" :maxlength="6" :dot-fill="true" v-model="password"
						:disabled-keyboard="true" @finish="finish"></up-message-input>
				</view>
				<view class="u-text-center u-padding-top-10 u-padding-bottom-20 tips">输入智行代驾钱包支付密码</view>
			</view>
		</up-keyboard>
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
		onShow,
		onHide
	} from '@dcloudio/uni-app'

	const instance = getCurrentInstance()
	const uTabs = ref(null)
	const uKeyboard = ref(null)
	const uTips = ref('')

	const swipperHeight = ref('')
	const list = ref([{
			name: '全部罚款'
		},
		{
			name: '未缴纳'
		},
		{
			name: '已缴纳'
		}
	])
	const tabCurrent = ref(0)
	const swiperCurrent = ref(0)
	const fineList = ref([])
	const status = ref(null)
	const page = ref(1)
	const length = ref(20)
	const end = ref(false)
	const showKeyboard = ref(false)
	const amount = ref('')
	const password = ref('')
	const fineId = ref(null)

	onLoad(() => {
		const windowHeight = uni.getSystemInfoSync().windowHeight
		swipperHeight.value = 'height:' + (windowHeight - 72) + 'px'
		status.value = null
		page.value = 1
		loadFineList(instance.proxy)
	})

	onShow(() => {
		const that = instance.proxy
		uni.$on('message', () => {
			that.refreshMessage(that)
		})
		uni.$on('showMessageTip', (lastRows) => {
			uTips.value = `最新收到${lastRows}条消息通知`
		})
	})

	onHide(() => {
		uni.$off('message')
		uni.$off('showMessageTip')
	})

	// ===== tabs 与 swiper 联动 =====
	function tabsChange(index) {
		swiperCurrent.value = index
		tabCurrent.value = index
	}

	function transition(e) {
		uTabs.value?.setDx(e.detail.dx)
	}

	function animationfinish(e) {
		tabCurrent.value = e.detail.current
		uTabs.value?.setFinishCurrent(e.detail.current)
		swiperCurrent.value = e.detail.current
	}

	function onreachBottom() {}

	function loadFineList(ref) {}

	function keyboardChangeHandle(val) {}

	function backspaceHandle(e) {}

	function showKeyboardHandle(val) {
		showKeyboard.value = val
	}

	function payHandle(id, amt) {
		fineId.value = id
		amount.value = amt
		showKeyboard.value = true
	}

	function finish(val) {}
</script>

<style lang="less">
	@import url('fine_list.less');
</style>
