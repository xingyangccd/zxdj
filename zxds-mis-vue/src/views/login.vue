<template>
	<div class="page">
		<el-row type="flex" justify="center" align="middle" class="container">
			<el-col :lg="14" :xl="10" class="hidden-md-and-down">
				<el-row class="panel">
					<el-col :span="12">
						<div class="left">
							<img src="../assets/login/logo.png" class="logo" />
							<img src="../assets/login/big-1.png" class="big" />
						</div>
					</el-col>
					<el-col :span="12">
						<div class="right">
							<div class="title-container">
								<h2>ZXDS代驾管理平台</h2>
								<span>( Ver 2.0 )</span>
							</div>
							<div v-if="!qrCodeVisible">
								<div class="row">
									<el-input v-model="username" placeholder="用户名" :prefix-icon="User"
										clearable></el-input>
								</div>
								<div class="row">
									<el-input type="password" v-model="password" placeholder="密码" :prefix-icon="Lock"
										show-password clearable></el-input>
								</div>
								<div class="row">
									<el-button type="primary" class="btn" @click="login">登陆系统</el-button>
								</div>
								<div class="row">
									<a class="link" @click="changeMode">短信登陆</a>
								</div>
							</div>
							<div v-if="qrCodeVisible">
								<div class="qrCode-container">
									<img :src="qrCode" height="153" class="qrCode" />
									<img src="../assets/login/phone.png" height="148" />
								</div>
								<div class="row">
									<a class="link" @click="changeMode">用户名密码登陆</a>
								</div>
							</div>
						</div>
					</el-col>
				</el-row>
			</el-col>
		</el-row>
	</div>
</template>

<script setup>
	import {
		ref,
		onUnmounted
	} from 'vue'
	import {
		useRouter
	} from 'vue-router'
	import {
		ElMessage
	} from 'element-plus'
	import {
		User,
		Lock
	} from '@element-plus/icons-vue'
	import {
		http
	} from '@/utils/http'
	import {
		isUsername,
		isPassword
	} from '../utils/validate.js'

	defineOptions({
		name: 'Login'
	})

	const router = useRouter()

	const username = ref('')
	const password = ref('')
	const qrCodeVisible = ref(false)
	const qrCode = ref('')
	const uuid = ref(null)
	let qrCodeTimer = null
	let loginTimer = null

	const login = () => {
		if (!isUsername(username.value)) {
			ElMessage.error({
				message: '用户名格式不正确',
				duration: 1200
			})
			return
		}
		if (!isPassword(password.value)) {
			ElMessage.error({
				message: '密码格式不正确',
				duration: 1200
			})
			return
		}
		const data = {
			username: username.value,
			password: password.value
		}
		http('user/login', 'POST', data, true, (resp) => {
			if (resp.result) {
				localStorage.setItem('permissions', resp.permissions)
				localStorage.setItem('token', resp.token)
				router.push({
					name: 'Home'
				})
			} else {
				ElMessage.error({
					message: '登陆失败',
					duration: 1200
				})
			}
		})
	}

	const loadQRCode = () => {
		http('user/createQrCode', 'GET', null, true, (resp) => {
			qrCode.value = resp.pic
			uuid.value = resp.uuid
		})
	}

	const changeMode = () => {
		qrCodeVisible.value = !qrCodeVisible.value
		if (qrCodeVisible.value) {
			loadQRCode()
			qrCodeTimer = setInterval(() => {
				loadQRCode()
			}, 5 * 60 * 1000)
			loginTimer = setInterval(() => {
				http('user/wechatLogin', 'POST', {
					uuid: uuid.value
				}, true, (resp) => {
					if (resp.result) {
						clearInterval(qrCodeTimer)
						clearInterval(loginTimer)
						localStorage.setItem('permissions', resp.permissions)
						localStorage.setItem('token', resp.token)
						router.push({
							name: 'Home'
						})
					}
				})
			}, 5000)
		} else {
			clearInterval(qrCodeTimer)
			clearInterval(loginTimer)
		}
	}

	onUnmounted(() => {
		clearInterval(qrCodeTimer)
		clearInterval(loginTimer)
	})
</script>

<style lang="less" scoped>
	@import url('login.less');
</style>