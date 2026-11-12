<template>
	<view class="page">
		<!-- 渐变背景 + 光晕 -->
		<view class="bg-gradient"></view>
		<view class="glow glow-1"></view>
		<view class="glow glow-2"></view>

		<!-- 自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-title">个人中心</view>
		</view>

		<!-- 顶部头像卡片 -->
		<view class="summary-container">
			<image :src="photo || '/static/register/default-avatar.png'" mode="aspectFill" class="photo"></image>
			<view class="summary">
				<view class="row">
					<text class="name">{{ name || '未登录' }}</text>
					<image :src="realAuth ? '../../static/mine/icon-1.png' : '../../static/mine/icon-2.png'"
						mode="widthFix" class="auth-icon"></image>
					<text :class="realAuth ? 'auth' : 'unauth'">{{ realAuth ? '已认证' : '未认证' }}</text>
				</view>
				<view class="row">
					<view class="attribute">
						<view class="item">代驾：{{ years }}年</view>
						<view class="item">等级：Lv{{ level }}</view>
						<view class="item">接单：{{ changeNumber(totalOrder) }}</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 升级提示 -->
		<view class="notice-container">
			<view class="notice-title">
				<image src="../../static/mine/icon-3.png" mode="widthFix"></image>
				<text>{{ levelName || '新手司机' }}</text>
			</view>
			<view class="desc">升级Lv{{ level + 1 }}等级还需接单{{ 50 - (totalOrder % 50) }}个，并且为顾客提供安全和高质量代驾服务</view>
		</view>

		<!-- 钱包 / 订单 -->
		<view class="nav-container">
			<view class="row">
				<view class="item" @tap="toPage('/wallet/recharge/recharge')">
					<image src="../../static/mine/icon-4.png" mode="widthFix"></image>
					<view class="info">
						<text class="title">钱包</text>
						<text class="desc">总额{{ balance }}元</text>
					</view>
				</view>
				<view class="border-column"></view>
				<view class="item">
					<image src="../../static/mine/icon-5.png" mode="widthFix"></image>
					<view class="info">
						<text class="title">订单</text>
						<text class="desc">本周有{{ weekOrder }}单</text>
					</view>
				</view>
			</view>
			<view class="border-row"></view>
			<view class="row">
				<view class="item">
					<image src="../../static/mine/icon-6.png" mode="widthFix"></image>
					<view class="info">
						<text class="title">顾客评价</text>
						<text class="desc">近期{{ weekComment }}条好评</text>
					</view>
				</view>
				<view class="border-column"></view>
				<view class="item">
					<image src="../../static/mine/icon-7.png" mode="widthFix"></image>
					<view class="info">
						<text class="title">我的申诉</text>
						<text class="desc">正在申诉{{ appeal }}件</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 快捷入口 -->
		<view class="tab-container">
			<view class="item">
				<view class="ball">
					<image src="../../static/mine/icon-8.png" class="ball-icon"></image>
				</view>
				<text>活动中心</text>
			</view>
			<view class="item">
				<view class="ball">
					<image src="../../static/mine/icon-9.png" class="ball-icon"></image>
				</view>
				<text>积分商城</text>
			</view>
			<view class="item">
				<view class="ball">
					<image src="../../static/mine/icon-10.png" class="ball-icon"></image>
				</view>
				<text>本月排名</text>
			</view>
			<view class="item">
				<view class="ball">
					<image src="../../static/mine/icon-11.png" class="ball-icon"></image>
				</view>
				<text>我的数据</text>
			</view>
		</view>

		<!-- 底部菜单 -->
		<view class="menu-card">
			<u-cell-group :border="false">
				<u-cell icon="account-fill" :icon-style="icon" title="账号与安全" :border-top="false"
					@click="toPage('/user/account/account')" />
				<u-cell icon="server-fill" :icon-style="icon" title="在线客服" @click="serviceHandle" />
				<u-cell icon="trash-fill" :icon-style="icon" title="清理缓存" @click="clearHandle" />
				<u-cell icon="file-text-fill" :icon-style="icon" title="用户指南" :border-bottom="false"
					@click="toPage('/rule/user_guide/user_guide')" />
			</u-cell-group>
		</view>

		<!-- 退出 -->
		<view class="exit">
			<button class="btn" @tap="logoutHandle">退出系统</button>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive
	} from 'vue'
	import {
		onShow
	} from '@dcloudio/uni-app'
	import {
		toPage as toPageUtil,
		changeNumber as changeNumberUtil,
		ajax,
		url
	} from '@/utils/common.js'

	import dayjs from 'dayjs'

	// 状态栏高度，避开刘海 / 灵动岛
	const statusBarHeight = ref(0)
	const sysInfo = uni.getSystemInfoSync()
	statusBarHeight.value = sysInfo.statusBarHeight

	// 菜单图标统一样式
	const icon = reactive({
		color: '#8A8B8F',
		'margin-top': '-1rpx'
	})

	// 页面数据
	const name = ref('')
	const photo = ref('')
	const realAuth = ref(false)
	const years = ref(0)
	const level = ref(0)
	const levelName = ref('')
	const balance = ref(0)
	const totalOrder = ref(0)
	const weekOrder = ref(0)
	const weekComment = ref(0)
	const appeal = ref(0)

	onShow(() => {
		ajax(url.searchDriverBaseInfo, "POST", null, (resp) => {
			// 司机的基本信息
			const result = resp.data.result;

			// 取出基本信息赋值给响应式对象
			name.value = result.name;
			photo.value = result.photo;
			realAuth.value = uni.getStorageSync("realAuth") == 2;
			// 计算代驾年限
			const createTime = dayjs(result.createTime, "YYYY-MM-DD");
			years.value = dayjs().diff(createTime, "years");
			// 等级和等级名称
			const lv = result.summary.level;
			level.value = lv;
			if (lv < 10) {
				levelName.value = "初级代驾";
			} else if (lv < 30) {
				levelName.value = "中级代驾";
			} else if (lv < 50) {
				levelName.value = "高级代驾";
			} else {
				levelName.value = "王牌代驾";
			}
			// 钱包、订单相关的数据
			balance.value = result.balance;
			totalOrder.value = result.summary.totalOrder;
			weekOrder.value = result.summary.weekOrder;
			weekComment.value = result.summary.weekComment;
			appeal.value = result.summary.appeal;
		})
	})

	// 数字格式化（如 1000 → 1千）
	function changeNumber(value) {
		return changeNumberUtil(value)
	}

	// 页面跳转
	function toPage(url) {
		toPageUtil(url)
	}

	// 在线客服：拨打电话
	function serviceHandle() {
		// 震动一下
		uni.vibrateShort({});
		// 拨打电话
		uni.makePhoneCall({
			phoneNumber: '10086'
		})
	}

	/**
	 * 清理缓存
	 * 1. 短暂震动一下
	 * 2. 弹窗确认
	 * 3. 遍历 storage，删除 除 token realAuth 之外的所有数据。
	 * 4. 删除本地保存的临时文件
	 * 5. 提示删除成功
	 */
	function clearHandle() {
		uni.vibrateShort({});

		uni.showModal({
			title: "提示信息",
			content: "清理本地缓存",
			success(resp) {
				if (!resp.confirm) return;
				uni.vibrateShort({});
				uni.showLoading({
					title: "执行中"
				})

				// 1. 清理除 token realAuth之外的数据
				// 返回所有的缓存key
				const cache = uni.getStorageInfoSync();
				for (const key of cache.keys) {
					if (key === "token" || key === "realAuth") continue
					uni.removeStorageSync(key);
					console.log("删除Storage缓存成功：", key);
				}

				// 2.清空保存的临时文件
				uni.getSavedFileList({
					success(resp) {
						for (const one of resp.fileList) {
							uni.removeSavedFile({
								filePath: one.filePath,
								success() {
									console.log("缓存文件删除成功:", one.filePath)
								}
							})
						}
					}
				})

				// 3.延迟提示清理完毕
				setTimeout(() => {
					uni.hideLoading();
					uni.showToast({
						title: "清理完毕"
					})
				}, 500);
			}
		})
	}

	/**
	 * 退出登录
	 * 流程：
	 *   1. 短震动提示
	 *   2. 弹窗二次确认
	 *   3. 调后端登出接口，清掉本地 token / realAuth
	 *   4. 提示"已退出"，1.5 秒后重启到登录页
	 */
	function logoutHandle() {
		// 短震动反馈
		uni.vibrateShort({})

		uni.showModal({
			title: '提示信息',
			content: '确认退出系统？',
			success: (resp) => {
				if (!resp.confirm) return

				// 调后端登出接口（POST /driver/logout）
				ajax(url.logout, 'POST', null, () => {
					// 清掉本地登录态
					uni.removeStorageSync('realAuth')
					uni.removeStorageSync('token')

					uni.showToast({
						title: '已经退出系统',
						success: () => {
							setTimeout(() => {
								// reLaunch 关闭所有页面，跳到登录页
								uni.reLaunch({
									url: '/pages/login/login'
								})
							}, 1500)
						}
					})
				})
			}
		})
	}
</script>

<style lang="less">
	@import url('mine.less');
</style>
