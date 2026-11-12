<template>
	<view class="page">
		<!-- 渐变背景 + 光晕 -->
		<view class="bg-gradient"></view>
		<view class="glow glow-1"></view>
		<view class="glow glow-2"></view>

		<!-- 自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">‹</view>
			<view class="nav-title">账号与安全</view>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 菜单卡片 -->
		<view class="menu-card">
			<u-cell-group :border="false">
				<u-cell title="统一实名认证" label="保障账号与出行安全" :title-style="titleStyle" :label-style="labelStyle"
					:value="realAuth" :border-top="false" @click="toPage('/identity/filling/filling?mode=fill')" />
				<u-cell title="修改手机号" label="系统会保护您的通信隐私" :title-style="titleStyle" :label-style="labelStyle"
					:value="coverTel" @click="updateTelHandle" />
				<u-cell title="设置支付密码" label="系统会保障您钱包资金的安全" :title-style="titleStyle" :label-style="labelStyle"
					@click="setPasswordHandle" />
				<u-cell title="设置回家地址" label="系统将为你规划合理的回家路线" :title-style="titleStyle" :label-style="labelStyle"
					@click="updateHomeHandle" />
				<u-cell title="接单设置" label="系统将为你推荐符合您要求的订单" :title-style="titleStyle" :label-style="labelStyle"
					@click="toPage('/user/settings/settings')" v-show="realAuth === '已认证'" />
				<u-cell title="永久注销账号" label="操作后账号信息无法恢复，该微信帐户也无法再次注册" :title-style="titleStyle"
					:label-style="labelStyle" :border-bottom="false" @click="disableHandle" />
			</u-cell-group>
		</view>

		<!-- 数字键盘 -->
		<up-keyboard default="" ref="uKeyboardRef" mode="number" :mask="true" :mask-close-able="false"
			:dot-enabled="false" v-model:show="showKeyboard" :safe-area-inset-bottom="true" :tooltip="false"
			@change="keyboardChangeHandle" @backspace="backspaceHandle">
			<view>
				<view class="u-text-right u-padding-30">
					<view class="close" @tap="showKeyboardHandle(false)">
						<up-icon name="close" color="#333333" size="32"></up-icon>
					</view>
				</view>
				<view class="msg-input-wrapper">
					<up-message-input mode="box" :maxlength="6" :dot-fill="true" v-model="password"
						:disabled-keyboard="true" @finish="finish"></up-message-input>
				</view>
				<view class="tips">输入智行代驾钱包支付密码</view>
			</view>
		</up-keyboard>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive
	} from 'vue'
	import {
		onLoad,
		onShow,
		onHide,
		onUnload
	} from '@dcloudio/uni-app'
	import {
		toPage as toPageUtil
	} from '@/utils/common.js'

	// 状态栏高度，避开刘海
	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)

	// 键盘引用
	const uKeyboardRef = ref(null)

	// 菜单样式
	const titleStyle = reactive({
		color: '#2b1a0d',
		'font-size': '32rpx',
		'font-weight': 'bold'
	})
	const labelStyle = reactive({
		'font-size': '24rpx',
		color: '#8a7a6b',
		'font-weight': 'normal'
	})

	// 页面数据
	const realAuth = ref('')
	const tel = ref('')
	const coverTel = ref('')
	const showKeyboard = ref(false)
	const password = ref('')

	// ===== 生命周期 =====
	onLoad(() => {})

	onShow(() => {
		// TODO: 加载司机实名认证状态、手机号
	})

	onHide(() => {})

	onUnload(() => {})

	// ===== 方法 =====

	// 返回上一页
	function goBack() {
		uni.navigateBack()
	}

	// 页面跳转
	function toPage(url) {
		toPageUtil(url)
	}

	// 键盘输入变化
	function keyboardChangeHandle(val) {
		if (password.value.length < 6) {
			password.value += val
		}
	}

	// 退格
	function backspaceHandle(e) {
		password.value = password.value.slice(0, -1)
	}

	// 显示/隐藏键盘
	function showKeyboardHandle(val) {
		showKeyboard.value = val
	}

	// 密码输入完成（6位）
	function finish(val) {
		console.log('密码输入完成：', val)
		showKeyboard.value = false
		// TODO: 提交密码到后端
	}

	// 修改手机号
	function updateTelHandle() {}

	// 设置支付密码：清空 + 弹出键盘
	function setPasswordHandle() {
		console.log('点击设置支付密码')
		password.value = ''
		showKeyboard.value = true
		console.log('showKeyboard =', showKeyboard.value)
	}

	// 设置回家地址
	function updateHomeHandle() {}

	// 注销账号
	function disableHandle() {}
</script>
<style lang="less">
	@import url('account.less');
</style>
