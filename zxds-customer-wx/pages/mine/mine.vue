<template>
	<view>
		<view class="summary-container">
			<u-avatar :src="photo" size="large"></u-avatar>
			<view class="summary">
				<view class="name">{{ name }}</view>
				<view class="level">
					<u-icon name="integral-fill" color="#FFA600" size="35" class="icon"></u-icon>
					<text>{{ level }}</text>
				</view>
			</view>
		</view>
		<u-cell-group>
			<u-cell icon="file-text-fill" title="订单"></u-cell>
			<u-cell icon="info-circle-fill" title="罚款" :value="fine"></u-cell>
			<u-cell icon="coupon-fill" title="代金券" :value="voucher"
				@click="toPage('../voucher_list/voucher_list')"></u-cell>
			<u-cell icon="server-fill" title="在线客服"></u-cell>
			<u-cell icon="trash-fill" title="清理缓存" @click="clearHandle" />
			<u-cell icon="file-text-fill" title="用户指南" @click="toPage('../../rule/user_guide/user_guide')" />
			<u-cell icon="setting-fill" title="设置"></u-cell>
		</u-cell-group>
	</view>
</template>

<script setup>
	import {
		ref,
		getCurrentInstance
	} from 'vue'

	const instance = getCurrentInstance()
	const photo = ref('')
	const name = ref('')
	const level = ref('')
	const fine = ref('0.00元')
	const voucher = ref('0张')

	function toPage(url) {
		instance.proxy.toPage(url)
	}

	function clearHandle() {
		uni.showModal({
			title: '提示消息',
			content: '您确定要清理缓存吗？',
			success: (resp) => {
				if (resp.confirm) {
					uni.clearStorageSync()
					uni.showToast({
						icon: 'success',
						title: '清理完成'
					})
				}
			}
		})
	}
</script>

<style lang="less">
	@import url('mine.less');
</style>
