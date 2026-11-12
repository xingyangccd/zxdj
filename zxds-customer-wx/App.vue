<script setup>
	import {
		onLaunch,
		onShow,
		onHide
	} from '@dcloudio/uni-app'

	// 这个生命周期钩子只在应用启动的时候执行一次。适合在这里开启GPS定位功能。
	onLaunch(() => {
		// 以下代码表示开启隐私授权功能（固定写法），获取乘客的隐私信息：昵称、头像、GPS定位。
		// 注意：以下代码的API是微信小程序内置的，不通用。因此不要把它编译到最终产物当中。
		// wx 就是小程序的核心对象。这个对象不通用，是微信小程序专属的.
		// uni-app中有一个uni对象，这个对象是uni-app的核心对象。这个uni对象底层封装了wx对象。
		// uni 对象更加通用，比wx更通用.
		// #ifdef MP-WEIXIN
		if (wx.requirePrivacyAuthorize) {
			wx.requirePrivacyAuthorize({
				success: () => {
					console.log('隐私授权成功')
				},
				fail: () => {
					console.log('用户拒绝隐私协议')
				}
			})
		}
		// #endif

		// 开启实时定位
		uni.startLocationUpdate({
			success() {
				console.log("开启定位成功");
			},
			fail(err) {
				console.error("开启定位失败", err);
			}
		})

		// 只要乘客的定位GPS发生任何变化，把变化的GPS定位发出去。
		// 可以依靠自定义事件来实现这个功能（Vue3当中学习过：组件之间数据传递）
		// 事件的定义，事件的绑定、事件的触发
		// 这个回调函数什么时候执行？只要乘客的GPS发生变化
		// 这个回调函数绑定一次，只要绑定上之后，以后每次发生 LocationChange 事件时，都会自动执行该回调函数。
		uni.onLocationChange((response) => {
			// 发送当前最新的location给其他的vue组件.
			// 最新位置
			const location = {
				latitude: response.latitude,
				longitude: response.longitude
			};
			// 触发事件（这个代码不是事件定义，也不是事件的绑定。是让事件发生。是触发事件.）
			// 定义事件在，例如工作台页面中 uni.$on("updateLocation", ...);
			uni.$emit("updateLocation", location);
		})

	})

	onShow(() => {
		console.log('App Show')
	})

	onHide(() => {
		console.log('App Hide')
	})
</script>

<style lang="scss">
	@import 'uview-plus/index.scss';
</style>
