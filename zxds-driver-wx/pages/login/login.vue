<template>
	<view class="login-page">
		<!-- 明亮渐变背景 -->
		<view class="bg-gradient"></view>

		<!-- 光晕装饰 -->
		<view class="glow glow-1"></view>
		<view class="glow glow-2"></view>
		<view class="glow glow-3"></view>

		<!-- 顶部品牌 -->
		<view class="header">
			<view class="brand-row">
				<view class="brand-mark">
					<text class="brand-mark-text">Z</text>
					<view class="brand-mark-glow"></view>
				</view>
				<view class="brand-info">
					<view class="brand-name">智行代驾</view>
					<view class="brand-sub">
						<text class="sub-dot"></text>
						<text>司机端</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 主标题 -->
		<view class="hero">
			<view class="hero-eyebrow">DRIVER WORKBENCH</view>
			<view class="hero-title">
				<text class="title-line">每一次</text>
				<text class="title-line title-accent">接单</text>
				<text class="title-line">都是收获</text>
			</view>
			<view class="hero-desc">专业代驾 · 安全出行 · 收入稳定</view>
		</view>

		<!-- 登录区 -->
		<view class="login-area">
			<view class="divider">
				<view class="divider-line"></view>
				<text class="divider-text">微信一键登录</text>
				<view class="divider-line"></view>
			</view>

			<button class="btn" @tap="login">
				<view class="btn-bg"></view>
				<view class="btn-wx">
					<text class="wx-icon">✆</text>
				</view>
				<text class="btn-text">立即登录</text>
				<text class="btn-arrow">→</text>
			</button>

			<view class="register-container">
				<text class="register-text">没有账号？</text>
				<text class="link" @tap="toRegisterPage">立即注册</text>
			</view>
		</view>

		<!-- 底部 -->
		<view class="footer">
			<view class="footer-line"></view>
			<view class="remark">本小程序仅限于智行代驾签约司机使用</view>
		</view>

		<up-toast ref="uToastRef"></up-toast>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'
	import {
		url,
		ajax
	} from '@/utils/common.js'

	const uToastRef = ref(null)


	function login() {
		// 获取临时登录授权码
		uni.login({
			provider: 'weixin',
			success(response) {
				const data = {
					code: response.code
				};
				// 发送ajax请求进行登录
				ajax(url.login, "POST", data, (resp) => {
					// 没有拿到token，说明司机没有注册。
					if (!resp.data.hasOwnProperty("token")) {
						uToastRef.value.show({
							message: "请先注册",
							type: "error"
						});
						return;
					}

					// 获取token realAuth archive
					const token = resp.data.token;
					const realAuth = resp.data.realAuth;
					const archive = resp.data.archive;

					// 将token和realAuth都存储到storage当中。
					uni.setStorageSync("token", token);
					uni.setStorageSync("realAuth", realAuth);

					// 删除缓存中正在执行的订单数据，进入工作台之后重新加载数据。
					uni.removeStorageSync("executeOrder");

					uToastRef.value.show({
						message: "登录成功",
						type: "success",
						complete: () => {
							// 默认的工作状态是：停止接单
							uni.setStorageSync("workStatus", "停止接单");
							// 1. 没有实名认证
							if (realAuth == 1) {
								// 跳转到填写实名认证信息的页面
								uni.redirectTo({
									url: "/identity/filling/filling?mode=create"
								})
								return;
							}
							// 2. 已经实名认证没有录入人脸的
							if (archive === false) {
								uni.showModal({
									title: "提示信息",
									content: "您还没有录入用于核实身份的面部特征信息，如果不录入就无法接单",
									confirmText: "确定",
									cancelText: "取消",
									success(modalResp) {
										if (modalResp.confirm) {
											// 跳转到录脸页面
											uni.redirectTo({
												url: "/identity/face_camera/face_camera?mode=create"
											})
										} else {
											// 不要录脸（跳转到工作台页面）
											uni.switchTab({
												url: "/pages/workbench/workbench"
											})
										}
									}
								})
								return;
							}

							// 3. 都完成了（登录、认证、人脸）
							uni.switchTab({
								url: "/pages/workbench/workbench"
							})
						}
					})
				})
			}
		})
	}

	function toRegisterPage() {
		uni.navigateTo({
			url: '/pages/register/register'
		})
	}
</script>

<style lang="less">
	@import url('login.less');
</style>
