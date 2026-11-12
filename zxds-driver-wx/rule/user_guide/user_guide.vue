<template>
	<view>
		<u-cell-group :border="false">
			<u-cell icon="rmb-circle-fill" :icon-style="icon" title="计费规则" :border-top="false"
				@click="goBillRule" />
			<u-cell icon="account-fill" :icon-style="icon" title="用户规则" @click="goUserRule" />
			<u-cell icon="error-circle-fill" :icon-style="icon" title="取消规则" @click="goCancelRule" />
		</u-cell-group>
		<up-notice-bar :text="uTips" />
	</view>
</template>

<script setup>
	import {
		ref,
		reactive,
		getCurrentInstance
	} from 'vue'
	import {
		onShow,
		onHide
	} from '@dcloudio/uni-app'

	const instance = getCurrentInstance()
	const uTips = ref(null)

	const icon = reactive({
		color: '#a0a0a0',
		'margin-top': '-1rpx'
	})

	onShow(() => {})
	onHide(() => {})

	function goBillRule() {
		instance.proxy.toPage('/rule/bill_rule/bill_rule')
	}

	function goUserRule() {
		instance.proxy.toPage('/rule/user_rule/user_rule')
	}

	function goCancelRule() {
		instance.proxy.toPage('/rule/cancel_rule/cancel_rule')
	}
</script>

<style lang="less">
	@import url('user_guide.less');
</style>
