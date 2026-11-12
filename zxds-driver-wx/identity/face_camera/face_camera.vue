<template>
	<view class="face-page">
		<!-- 渐变背景 + 光晕 -->
		<view class="bg-gradient"></view>
		<view class="glow glow-1"></view>
		<view class="glow glow-2"></view>

		<!-- 自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">‹</view>
			<view class="nav-title">人脸识别</view>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 相机 / 预览图 -->
		<view class="face-container">
			<camera device-position="front" flash="off" class="camera" @error="error" v-if="showCamera">
				<cover-image src="../static/face_camera/bg.png" class="bg"></cover-image>
			</camera>
			<view class="image-container" v-if="showImage">
				<image mode="widthFix" class="photo" :src="photoPath"></image>
				<view class="cover"></view>
			</view>
		</view>

		<!-- 提示文字 -->
		<view class="desc">
			<block v-if="mode === 'verificate'">
				<image src="../static/face_camera/tips.png" mode="widthFix" class="tips"></image>
				<text>请把面部放在圆圈内</text>
				<text>拍摄脸部来确认身份</text>
			</block>
			<block v-if="mode === 'create'">
				<image src="../static/face_camera/face.png" mode="widthFix" class="face"></image>
				<text>请把完整面部放在圆圈内</text>
				<text>拍摄脸部来保存身份识别数据</text>
			</block>
		</view>

		<!-- 按钮 -->
		<view class="btn-area">
			<button class="btn" @tap="confirmHandle">
				<view class="btn-bg"></view>
				<text class="btn-text">{{ mode === 'create' ? '录入面部信息' : '身份核实' }}</text>
				<text class="btn-arrow">→</text>
			</button>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'
	import {
		onLoad,
		onHide
	} from '@dcloudio/uni-app';

	import {
		url,
		ajax
	} from "@/utils/common.js";

	// 插件：将图片转换成Base64的字符串
	import {
		pathToBase64
	} from "image-tools";

	const mode = ref('verificate')
	const photoPath = ref('')
	const showCamera = ref(true)
	const showImage = ref(false)
	const statusBarHeight = ref(0)
	const audio = ref(null)

	onLoad((options) => {
		mode.value = options.mode || 'verificate'
		const sysInfo = uni.getSystemInfoSync()
		statusBarHeight.value = sysInfo.statusBarHeight

		// 创建音乐播放器对象
		const innerAudio = uni.createInnerAudioContext();
		// 让播放器关联mp3文件
		innerAudio.src = "/static/voice/voice_5.mp3";
		// 播放
		innerAudio.play();
		// 把这个播放器保存到响应式对象当中，因为后期要在页面隐藏的时候停止播放。
		audio.value = innerAudio;
	})

	onHide(() => {
		if (audio.value) {
			// 停止语音播放
			audio.value.stop();
		}
	})

	function error() {}

	// 这个方法作用有两个：人脸采集/人脸识别
	function confirmHandle() {
		// 停止语音播放
		audio.value?.stop();

		// 调用相机进行拍照
		const ctx = uni.createCameraContext();
		ctx.takePhoto({
			quality: 'high',
			success(response) {
				photoPath.value = response.tempImagePath;
				showCamera.value = false;
				showImage.value = true;

				// 把图片转换成base64
				pathToBase64(photoPath.value).then((base64) => {
					// URL需要动态：人脸采集、人脸识别
					const apiUrl = mode.value === "create" ? url.createDriverFaceModel : url
						.verificateDriverFace;

					// 发送ajax请求
					const data = {
						photo: base64
					};

					ajax(apiUrl, "POST", data, (response) => { // 这个请求有可能是人脸识别的结果，也可能是人脸采集的结果
						const result = response.data.result;
						console.log("录入结果", result)
						if (mode.value === "create") {
							console.log("录入模式")
							// 录入模式
							if (result != null && result.length > 0) {
								console.log("录入失败")
								// result非空表示录入失败
								console.log(result);
								uni.showToast({
									icon: "none",
									title: "面部录入失败，请重新录入"
								})
								setTimeout(() => {
									showCamera.value = true;
									showImage.value = false;
								}, 2000);
							} else {
								console.log("录入成功")
								// result为空表示录入成功了
								uni.showToast({
									title: "面部录入成功"
								})
								setTimeout(() => {
									// 切换到工作台页面
									uni.switchTab({
										url: "/pages/workbench/workbench"
									})
								}, 2000);
							}
						} else {
							// 人脸识别模式
							console.log("人脸识别模式")
							// TODO: 判断人脸识别结果...
						}
					});

				});
			}
		});
	}

	function goBack() {
		uni.navigateBack()
	}
</script>

<style lang="less">
	@import url('face_camera.less');
</style>
