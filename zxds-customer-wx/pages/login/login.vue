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
						<text>乘客端</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 主标题 -->
		<view class="hero">
			<view class="hero-eyebrow">SMART DRIVING</view>
			<view class="hero-title">
				<text class="title-line">让每一次</text>
				<text class="title-line title-accent">出行</text>
				<text class="title-line">都安心</text>
			</view>
			<view class="hero-desc">专业代驾 · 安全到家 · 全程无忧</view>
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
				<text class="link" @tap="register">立即注册</text>
			</view>
		</view>

		<!-- 底部 -->
		<view class="footer">
			<view class="footer-line"></view>
			<view class="remark">本小程序仅限于智行代驾客户使用</view>
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

	/**
	 * 微信一键登录
	 * 流程：
	 *   1. 调 uni.login 拿微信临时凭证 code
	 *   2. 把 code 提交给后端换 token
	 *   3. 没拿到 token 说明还没注册，提示去注册
	 *   4. 拿到 token 存本地，提示成功并进工作台
	 */
	function login() {
		uni.login({
			provider: 'weixin',
			success: (res) => {
				const data = {
					code: res.code
				}

				ajax(url.login, 'POST', data, (resp) => {
					if (!resp.data.hasOwnProperty('token')) {
						uToastRef.value?.show({
							message: '请先注册',
							type: 'error'
						})
						return
					}

					uni.setStorageSync('token', resp.data.token)

					uToastRef.value?.show({
						message: '登录成功',
						type: 'success',
						complete: () => {
							uni.switchTab({
								url: '/pages/workbench/workbench'
							})
						}
					})
				})
			}
		})
	}

	import {
		checkValidTel
	} from '@/utils/common.js'

	/**
	 * 乘客注册
	 * 流程：
	 *   1. 弹窗输入手机号
	 *   2. 校验手机号格式
	 *   3. 拿微信临时凭证 code
	 *   4. 拿用户昵称、头像、性别
	 *   5. 提交后端注册，存 token
	 *   6. 提示成功并进工作台
	 */
	function register() {
		// 1. 弹窗输入手机号
		uni.showModal({
			title: '提示信息',
			editable: true,
			placeholderText: '输入手机号',
			success: (resp) => {
				if (!resp.confirm) return

				const tel = resp.content

				// 2. 校验手机号
				if (!checkValidTel(uToastRef.value, tel, '手机号码')) return

				// 3. 拿微信临时凭证
				uni.login({
					provider: 'weixin',
					success: (loginResp) => {
						const code = loginResp.code

						// 4. 拿用户信息
						uni.getUserProfile({
							desc: '获取用户信息',
							success: (profileResp) => {
								const info = profileResp.userInfo

								// 性别数字 → 中文
								const sexMap = {
									0: '无',
									1: '男',
									2: '女'
								}
								const sex = sexMap[info.gender] || '无'

								// 5. 提交注册
								const data = {
									code,
									nickname: info.nickName,
									photo: info.avatarUrl,
									sex,
									tel
								}

								ajax(url.registerNewCustomer, 'POST', data, (
									ajaxResp) => {
									uni.setStorageSync('token', ajaxResp.data
										.token)

									// 6. 提示成功并跳转
									uToastRef.value?.show({
										message: '注册成功',
										type: 'success',
										complete: () => {
											uni.switchTab({
												url: '/pages/workbench/workbench'
											})
										}
									})
								})
							}
						})
					}
				})
			}
		})
	}
</script>

<style lang="less">
	@import url('login.less');
</style>
