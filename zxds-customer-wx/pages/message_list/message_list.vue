<template>
	<view>
		<view class="message-container" v-for="one in list" :key="one.id"
			@tap="viewMessageHandle(one.id, one.readFlag, one.refId)">
			<view class="top">
				<view class="title">
					<image src="../../static/tab_bar/message_1.png" mode="widthFix" v-if="!one.readFlag"></image>
					<image src="../../static/tab_bar/message_2.png" mode="widthFix" v-if="one.readFlag"></image>
					<text>{{ one.senderName }}</text>
				</view>
				<view class="date">{{ one.sendTime }}</view>
			</view>
			<view class="content">{{ one.msg }}</view>
			<view class="bottom">
				<text>查看详情</text>
				<u-icon name="arrow-right" color="#aaa" size="28"></u-icon>
			</view>
		</view>
		<view class="empty" v-if="list.length == 0"><u-empty text="消息列表为空" mode="list"></u-empty></view>
	</view>
</template>

<script setup>
	import {
		ref,
		getCurrentInstance
	} from 'vue'
	import {
		onShow,
		onReachBottom
	} from '@dcloudio/uni-app'

	const instance = getCurrentInstance()
	const page = ref(1)
	const length = ref(50)
	const list = ref([])
	const isLastPage = ref(false)

	onShow(() => {
		loadMessageList()
	})

	onReachBottom(() => {
		if (!isLastPage.value) {
			page.value++
			loadMessageList()
		}
	})

	function loadMessageList() {
		instance.proxy.ajax(instance.proxy.url.refreshMessage, 'POST', {
			page: page.value,
			length: length.value
		}, (resp) => {
			const result = resp.data.result || []
			if (result.length == 0) {
				isLastPage.value = true
				return
			}
			list.value = result
		})
	}

	function viewMessageHandle(id, readFlag, refId) {
		if (!readFlag) {
			instance.proxy.ajax(instance.proxy.url.refreshMessage, 'POST', {
				id: id
			}, () => {
				loadMessageList()
			}, false)
		}
		if (refId) {
			uni.navigateTo({
				url: `/pages/order/order?orderId=${refId}`
			})
		}
	}
</script>

<style lang="less">
	@import url('message_list.less');
</style>
