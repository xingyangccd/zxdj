<template>
	<view>
		<u-cell-group :border="false">
			<u-cell icon="setting-fill" title="开启听单" :arrow="false" :border-top="false">
				<u-switch slot="right-icon" v-model="listenService.status" :loading="listenService.loading"
					:vibrate-short="true" active-color="#19BE6B" @change="changeListenServiceHandle"></u-switch>
			</u-cell>
			<u-cell icon="setting-fill" title="自动抢单" :arrow="false" :border-top="false">
				<u-switch slot="right-icon" v-model="autoAccept.status" :loading="autoAccept.loading"
					:vibrate-short="true" active-color="#19BE6B" @change="changeAutoAcceptHandle"></u-switch>
			</u-cell>
			<u-cell icon="setting-fill" title="定向接单" :arrow="false">
				<u-switch slot="right-icon" v-model="orientationService.status" :loading="orientationService.loading"
					:vibrate-short="true" active-color="#19BE6B" @change="changeOrientationHandle"></u-switch>
			</u-cell>
			<u-cell icon="info-circle-fill" title="接单范围" :value="rangeDistance.label"
				@tap="rangeDistanceHandle"></u-cell>
			<u-cell icon="info-circle-fill" title="代驾里程" :value="orderDistance.label"
				@tap="orderDistanceHandle"></u-cell>
		</u-cell-group>
		<u-select v-model="rangeDistance.show" :list="rangeDistance.list" :default-value="rangeDistance.default"
			@confirm="confirmRangeDistanceHandle"></u-select>
		<u-select v-model="orderDistance.show" :list="orderDistance.list" :default-value="orderDistance.default"
			@confirm="confirmOrderDistanceHandle"></u-select>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive,
		getCurrentInstance
	} from 'vue'
	import {
		onLoad,
		onShow,
		onHide,
		onUnload
	} from '@dcloudio/uni-app'

	const chooseLocation = requirePlugin('chooseLocation')
	const instance = getCurrentInstance()

	const listenService = reactive({
		status: true,
		loading: true
	})
	const autoAccept = reactive({
		status: true,
		loading: true
	})
	const orientationService = reactive({
		status: false,
		loading: true,
		localtion: {
			place: '',
			latitude: 0,
			longitude: 0
		}
	})
	const rangeDistance = reactive({
		label: '5公里',
		default: [0],
		value: 0,
		show: false,
		list: [{
				value: 5,
				label: '5公里以内'
			},
			{
				value: 3,
				label: '3公里以内'
			},
			{
				value: 1,
				label: '1公里以内'
			}
		]
	})
	const orderDistance = reactive({
		label: '5公里',
		default: [0],
		value: 0,
		show: false,
		list: [{
				value: 0,
				label: '无限制'
			},
			{
				value: 30,
				label: '30~15公里'
			},
			{
				value: 15,
				label: '10~15公里'
			},
			{
				value: 10,
				label: '5~10公里'
			},
			{
				value: 5,
				label: '5公里以内'
			}
		]
	})

	onLoad(() => {})
	onShow(() => {})
	onHide(() => {})
	onUnload(() => {})

	// ===== 以下方法保留旧项目空实现 =====
	function changeListenServiceHandle() {}

	function changeAutoAcceptHandle() {}

	function changeOrientationHandle() {}

	function rangeDistanceHandle() {}

	function orderDistanceHandle() {}

	function confirmRangeDistanceHandle(resp) {}

	function confirmOrderDistanceHandle(resp) {}
</script>

<style lang="less">
	@import url('settings.less');
</style>
