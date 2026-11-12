<template>
	<view>
		<u-cell-group :border="false">
			<u-cell icon="rmb-circle-fill" :icon-style="icon" title="计费规则" :border-top="false"
				@click="toPage('../bill_rule/bill_rule')" />
			<u-cell icon="account-fill" :icon-style="icon" title="用户规则"
				@click="toPage('../user_rule/user_rule')" />
			<u-cell icon="error-circle-fill" :icon-style="icon" title="取消规则"
				@click="toPage('../cancel_rule/cancel_rule')" />
		</u-cell-group>
		<up-notice-bar ref="uTips"></up-notice-bar>
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

	function toPage(url) {
		instance.proxy.toPage(url)
	}
</script>

<style lang="less">
	@import url('user_guide.less');
</style>
