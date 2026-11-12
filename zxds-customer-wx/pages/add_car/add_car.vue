<template>
	<view class="page">
		<!-- 自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">‹</view>
			<view class="nav-title">添加车辆</view>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 菜单卡片 -->
		<view class="menu-card">
			<u-cell-group :border="false">
				<u-cell icon="info-circle-fill" :icon-style="icon" title="车型信息" :value="carType || '请输入'"
					:border-top="false" @click="carTypeHandle" />
				<u-cell icon="info-circle-fill" :icon-style="icon" title="车牌号码" :value="carPlate || '请输入'"
					:border-bottom="false" @click="carPlateHandle" />
			</u-cell-group>
		</view>

		<!-- 保存按钮 -->
		<button class="btn" @tap="saveHandle">
			<view class="btn-bg"></view>
			<text class="btn-text">确定保存</text>
			<text class="btn-arrow">→</text>
		</button>

		<!-- 底部说明 -->
		<view class="desc">
			保存即代表您同意智行代驾的
			<text>《隐私政策》</text>
		</view>

		<up-toast ref="uToastRef"></up-toast>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive
	} from 'vue'
	import {
		url,
		ajax,
		checkValidCarType,
		checkValidCarPlate
	} from '@/utils/common.js'

	const uToastRef = ref(null)

	// 状态栏高度，避开刘海
	const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)

	// 菜单图标统一样式
	const icon = reactive({
		color: '#2074FD',
		'margin-top': '-1rpx'
	})

	const carType = ref('')
	const carPlate = ref('')

	// 返回上一页
	function goBack() {
		uni.navigateBack()
	}

	/**
	 * 编辑车型
	 */
	function carTypeHandle() {
		uni.showModal({
			title: "输入车型",
			editable: true,
			placeholderText: "例如丰田卡罗拉",
			success(response) {
				// 用户点击的是取消
				if (!response.confirm) return;
				// 用户点击确认
				const value = response.content;
				if (checkValidCarType(uToastRef.value, value, "车型")) {
					carType.value = value;
				}
			}
		})
	}

	/**
	 * 编辑车牌
	 */
	function carPlateHandle() {
		uni.showModal({
			title: "输入车牌",
			editable: true,
			placeholderText: "例如京A88888",
			success(response) {
				if (!response.confirm) return

				const value = response.content;
				if (checkValidCarPlate(uToastRef.value, value, "车牌")) {
					carPlate.value = value;
				}
			}
		})
	}

	/**
	 * 保存车辆
	 */
	function saveHandle() {
		// 校验车牌和车型
		const valid = checkValidCarPlate(uToastRef.value, carPlate.value, "车牌") && checkValidCarType(uToastRef.value,
			carType.value, "车型");
		if (!valid) return;

		// 准备数据
		const data = {
			carType: carType.value,
			carPlate: carPlate.value
		};
		// 发送ajax请求
		ajax(url.insertCustomerCar, "POST", data, () => {
			uToastRef.value.show({
				message: "添加成功",
				type: "success",
				complete: () => {
					uni.redirectTo({
						url: '/pages/car_list/car_list'
					})
				}
			});
		})
	}
</script>

<style lang="less">
	@import url('add_car.less');
</style>
