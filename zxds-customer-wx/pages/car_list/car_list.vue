<template>
	<view class="page">
		<!-- 自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">‹</view>
			<view class="nav-title">我的车辆</view>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 车辆列表 -->
		<view class="list-container">
			<view v-for="one in list" :key="one.id" class="row" @tap="choseOneHandle(one.id, one.carPlate, one.carType)"
				@longpress="removeHandle(one.id)">
				<view class="left">
					<u-icon name="info-circle-fill" color="#2074FD" size="35" class="icon"></u-icon>
					<text class="car-type">{{ one.carType }}</text>
				</view>
				<text class="car-plate">{{ one.carPlate }}</text>
			</view>

			<!-- 空状态 -->
			<image v-if="list.length === 0" src="../../static/car_list/none.jpg" mode="widthFix" class="none"></image>
		</view>

		<!-- 添加按钮 -->
		<button class="btn" @tap="addHandle">
			<view class="btn-bg"></view>
			<text class="btn-text">添加车辆</text>
			<text class="btn-arrow">→</text>
		</button>

		<up-toast ref="uToastRef"></up-toast>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'
	import {
		onShow
	} from '@dcloudio/uni-app'
	import {
		url,
		ajax
	} from '@/utils/common.js'

	const uToastRef = ref(null)

	// 状态栏高度
	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)

	const list = ref([])

	// ===== 生命周期 =====
	onShow(() => {
		// 查询车辆列表
		loadDataList();
	})

	// ===== 方法 =====

	// 返回
	function goBack() {
		uni.navigateBack()
	}

	/**
	 * 选中车辆，跳转到创建订单页
	 */
	function choseOneHandle(id, carPlate, carType) {
		uni.navigateTo({
			url: `/pages/create_order/create_order?carId=${id}&carPlate=${carPlate}&carType=${carType}`
		})
	}

	// 长按删除
	function removeHandle(id) {
		// 震动一下
		uni.vibrateShort({})

		// 提示用户确定删除车辆吗
		uni.showModal({
			title: "提示信息",
			content: '是否删除这条车辆信息',
			success(response) {
				if (!response.confirm) return;
				ajax(url.deleteCustomerCarById, "DELETE", {
					id
				}, (resp) => {
					if (resp.data.rows === 0) {
						uToastRef.value.show({
							message: "删除失败",
							type: "error"
						})
					} else {
						loadDataList();
					}
				})
			}
		})
	}

	// 添加车辆
	function addHandle() {
		uni.navigateTo({
			url: '/pages/add_car/add_car'
		})
	}

	// 封装一个函数：加载当前乘客的对应的车辆列表信息。
	const loadDataList = () => {
		list.value = [];

		// 发送ajax请求获取车辆列表信息
		ajax(url.searchCustomerCarList, "POST", {}, (response) => {
			const result = response.data.result;
			for (const one of result) {
				list.value.push({
					id: one.id,
					carType: one.carType,
					carPlate: one.carPlate
				})
			}
		})
	}
</script>

<style lang="less">
	@import url('car_list.less');
</style>
