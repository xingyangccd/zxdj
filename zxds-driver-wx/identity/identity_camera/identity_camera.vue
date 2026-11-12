<template>
	<view class="camera-page">
		<view class="bg-gradient"></view>
		<view class="glow glow-1"></view>
		<view class="glow glow-2"></view>

		<!-- 自定义导航栏，paddingTop 避开状态栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">‹</view>
			<view class="nav-title">证件拍照</view>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 相机/预览图，二选一显示 -->
		<view class="media-card">
			<camera device-position="back" flash="off" class="camera" @error="error" v-if="showCamera"></camera>
			<image mode="widthFix" class="image" :src="photoPath" v-if="showImage"></image>
		</view>

		<!-- 按钮文字由 btnText 控制 -->
		<view class="operate-container">
			<button type="primary" class="btn" @tap="clickBtn">{{ btnText }}</button>
			<button type="warn" class="btn" @tap="afresh">重拍</button>
		</view>

		<!-- 注意事项文案由 type 控制 -->
		<view class="notice-container">
			<text class="notice">注意事项</text>
			<text class="desc">
				<block v-if="type != 'drcardBack'">
					拍照的时候，必须把证件拍摄清晰并完整，否则影响识别结果。持证拍照的时候不许戴帽子、墨镜和面纱等遮挡五官的饰物，只拍摄上半身即可。手持证件，五官与证件必须同时拍摄到照片中。</block>
				<block v-if="type == 'drcardBack'">拍照的时候，必须把驾驶证背面拍摄的清晰完整，否则影响认证结果。</block>
			</text>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		getCurrentInstance
	} from 'vue'
	import {
		onLoad
	} from '@dcloudio/uni-app'

	const instance = getCurrentInstance()
	const type = ref(null)
	const photoPath = ref('')
	const btnText = ref('拍照')
	const showCamera = ref(true)
	const showImage = ref(false)
	const statusBarHeight = ref(0)

	// onLoad生命周期钩子函数。
	// 页面加载时执行一次。
	onLoad((options) => {
		// 通过 options.type 获取查询参数的值赋值给响应式对象 type
		type.value = options.type
		// 获取状态栏高度，避开刘海
		const sysInfo = uni.getSystemInfoSync()
		statusBarHeight.value = sysInfo.statusBarHeight
	})

	function error() {}

	function clickBtn() {
		if (btnText.value === "拍照") {
			// 拍照
			// 获取到相机了。
			const ctx = uni.createCameraContext();
			// 拍照
			ctx.takePhoto({
				quality: 'high',
				success(response) {
					// 获取拍照成功之后的图片路径
					photoPath.value = response.tempImagePath;
					// 更新其他响应式对象的值，这样页面才能动态
					showCamera.value = false;
					showImage.value = true;
					btnText.value = "提交";
				}
			});
		} else {
			// 提交（统一将文件上传的代码放到了filling页面当中，这里需要调用上一个页面的updatePhoto()函数）
			// 这个是获取history 栈当中所有的页面，并且返回一个数组。
			const pages = getCurrentPages();
			// 获取上一个页面
			const prevPage = pages[pages.length - 2];
			// 获取上一个页面的组件实例，然后调用组件实例上的 updatePhoto() 函数
			prevPage.$vm.updatePhoto(type.value, photoPath.value);
			// 跳回到filling页面
			uni.navigateBack({
				delta: 1
			});
		}
	}


	// 重拍
	function afresh() {
		showCamera.value = true;
		showImage.value = false;
		btnText.value = "拍照";
	}

	function goBack() {
		uni.navigateBack()
	}
</script>

<style lang="less">
	@import url('identity_camera.less');
</style>
