<template>
	<view>
		<view class="message">
			<view class="header">
				<view class="desc">{{ sendTime }}</view>
				<view class="opt" @tap="deleteMsg">删除</view>
			</view>
			<view class="content">
				<view class="sender">
					<image :src="senderPhoto" mode="widthFix" class="photo" />
					<text>{{ senderName }}</text>
				</view>
				<view class="msg">{{ msg }}</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		getCurrentInstance
	} from 'vue'
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app'

	const instance = getCurrentInstance()
	const id = ref(null)
	const readFlag = ref(null)
	const refId = ref(null)
	const senderName = ref('')
	const sendTime = ref('')
	const senderPhoto = ref('')
	const msg = ref('')

	onLoad((options) => {
		id.value = options.id
	})

	onShow(() => {})

	function deleteMsg() {
		uni.showModal({
			title: '提示消息',
			content: '您确定要删除该消息吗？',
			success: (resp) => {
				if (resp.confirm) {
					instance.proxy.ajax(instance.proxy.url.deleteMessage, 'POST', {
						id: id.value
					}, () => {
						uni.showToast({
							icon: 'success',
							title: '删除成功'
						})
						setTimeout(() => uni.navigateBack(), 1500)
					})
				}
			}
		})
	}
</script>

<style lang="less">
	@import url('message.less');
</style>
