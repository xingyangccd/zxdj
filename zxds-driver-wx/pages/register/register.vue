<template>
	<view class="register-page">
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
						<text>司机端 · 新司机注册</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 主标题 -->
		<view class="hero">
			<view class="hero-eyebrow">DRIVER REGISTER</view>
			<view class="hero-title">
				<text class="title-line">加入我们</text>
				<text class="title-line title-accent">开启代驾</text>
				<text class="title-line">新旅程</text>
			</view>
			<view class="hero-desc">填写以下信息，完成注册</view>
		</view>

		<!-- 服务地点 -->
		<view class="location-card">
			<view class="location-left">
				<text class="location-icon">📍</text>
				<text class="location-text">代驾服务地点</text>
			</view>
			<view class="location-right">天津市</view>
		</view>

		<!-- 完善资料 -->
		<view class="form-card">
			<view class="card-title">完善资料</view>

			<view class="form-item">
				<text class="form-label">昵称</text>
				<input type="nickname" class="nickname-input" :value="nickname" placeholder="请输入昵称"
					@blur="onNicknameChange" @confirm="onNicknameChange" />
			</view>

			<view class="form-item">
				<text class="form-label">头像</text>
				<button class="avatar-wrapper" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
					<image class="avatar" :src="avatarUrl || '/static/register/default-avatar.png'" mode="aspectFill" />
				</button>
				<text class="avatar-tip">{{ avatarUrl ? '点击可更换头像' : '点击选择头像' }}</text>
			</view>
		</view>

		<!-- 基本要求 -->
		<view class="info-card">
			<view class="card-title">基本要求</view>
			<view class="list">
				<view class="item"><text class="item-index">1.</text>三年安全驾驶经验；</view>
				<view class="item"><text class="item-index">2.</text>年龄23~55周岁；</view>
				<view class="item"><text class="item-index">3.</text>无违法犯罪记录、无精神病史、无吸毒史，以及平台认为不适合代驾的其他历史证明；</view>
				<view class="item"><text class="item-index">4.</text>有熟练驾车经验；</view>
				<view class="item"><text class="item-index">5.</text>身体健康，无肢体残疾和大面积纹身；</view>
				<view class="item"><text class="item-index">6.</text>需要提供身份证、驾驶证、直系亲属联系方式，并保存前述材料的真实合法性；</view>
			</view>
		</view>

		<!-- 注册流程 -->
		<view class="info-card">
			<view class="card-title">注册流程</view>
			<view class="steps">
				<view class="step" v-for="(step, index) in steps" :key="index">
					<view class="step-num">{{ String(index + 1).padStart(2, '0') }}</view>
					<view class="step-content">
						<text class="step-title">{{ step.title }}</text>
						<text class="step-desc">{{ step.desc }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 注册按钮 -->
		<view class="btn-area">
			<button class="btn" @tap="register">
				<view class="btn-bg"></view>
				<text class="btn-text">立即注册</text>
				<text class="btn-arrow">→</text>
			</button>
		</view>

		<!-- 底部 -->
		<view class="footer">
			<view class="footer-line"></view>
			<view class="remark">本小程序仅限于智行代驾签约司机使用</view>
		</view>
		<!-- u-toast标签在uview-plus当中对应的是：up-toast -->
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
	const nickname = ref('')
	const avatarUrl = ref('')

	const steps = ref([{
			title: '在线注册',
			desc: '在小程序上完成注册'
		},
		{
			title: '基本信息提交',
			desc: '按照流程提交本人身份证、驾驶证'
		},
		{
			title: '信息审核',
			desc: '对提交信息以及证件进行审核'
		},
		{
			title: '签署合同',
			desc: '收到邮寄的合同后必须本人签署'
		}
	])

	function onNicknameChange(e) {
		nickname.value = e.detail.value
	}

	function onChooseAvatar(e) {
		avatarUrl.value = e.detail.avatarUrl
	}

	// 司机注册
	function register() {
		// 昵称不能为空，如果为空，提示错误信息
		if (!nickname.value) {
			uToastRef.value.show({
				message: "请填写昵称",
				type: "error"
			});
			return;
		}

		// 头像不能为空，如果为空，提示错误信息
		if (!avatarUrl.value) {
			uToastRef.value.show({
				message: "请选择头像",
				type: "error"
			});
			return;
		}

		// 核心：发送ajax请求，注册司机。（首先你需要获取到临时登录的code）
		uni.login({
			provider: 'weixin',
			success: (response) => {
				console.log("临时登录码：", response.code);
				// 准备数据
				const data = {
					code: response.code,
					nickname: nickname.value,
					photo: avatarUrl.value
				};

				// 发送ajax请求，完成司机注册。
				ajax(url.registerNewDriver, "POST", data, () => {
					// 注册成功了提示一下。
					uToastRef.value.show({
						message: "注册成功",
						type: "success",
						complete: () => {
							// 消息隐藏之后这个回调函数会执行。
							// 跳转到实名认证页面
							uni.redirectTo({
								// mode=create作用是什么？后面再说
								url: '/identity/filling/filling?mode=create'
							})
						}
					});
				});
			},
			fail: () => {
				uToastRef.value.show({
					message: "微信登录失败",
					type: "error"
				});
			}
		});
	}
</script>

<style lang="less">
	@import url('register.less');
</style>
