<template>
	<view class="filling-page">
		<!-- 明亮渐变背景 -->
		<view class="bg-gradient"></view>

		<!-- 光晕装饰 -->
		<view class="glow glow-1"></view>
		<view class="glow glow-2"></view>

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
						<text>司机端 · 实名登记</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 主标题 -->
		<view class="hero">
			<view class="hero-eyebrow">IDENTITY VERIFICATION</view>
			<view class="hero-title">
				<text class="title-line">实名认证</text>
				<text class="title-line title-accent">安全出发</text>
			</view>
			<view class="hero-desc">准确填写信息，保障每份订单</view>
		</view>

		<!-- 提示条 -->
		<view class="notice">
			<text class="notice-icon">ⓘ</text>
			<text class="notice-text">准确填写个人信息，可享受每份代驾订单人身意外险</text>
		</view>

		<!-- 证件拍照区 -->
		<view class="card">
			<view class="card-title">证件照片</view>
			<view class="credentials-container">
				<view class="credentials" v-for="(item, index) in 6" :key="index">
					<view class="cred-cover">
						<!-- 有回显图就显示图，否则显示默认图标 + 文字 -->
						<image v-if="cardBackground[index] && cardBackground[index].startsWith('http')"
							:src="cardBackground[index]" mode="aspectFill" class="cred-img" />
						<template v-else>
							<image src="../static/filling/card.png" mode="widthFix" class="card-icon"></image>
							<text class="desc">{{ ['身份证正面','身份证背面','手持身份证','驾驶证正面','驾驶证副本','手持驾驶证'][index] }}</text>
						</template>
					</view>
					<button class="camera" @tap="onCameraTap(index)"></button>
				</view>
			</view>
		</view>

		<!-- 个人信息 -->
		<view class="card">
			<view class="card-title">个人信息</view>
			<view class="list">
				<u-cell-group border="false">
					<u-cell title="真实姓名" :value="idcard.name" :value-style="style" :arrow="false" />
					<u-cell title="性别" :value="idcard.sex" :value-style="style" :arrow="false" />
					<u-cell title="生日" :value="idcard.birthday" :value-style="style" :arrow="false" />
					<u-cell title="身份证号" :value="idcard.pid" :value-style="style" :arrow="false" />
					<u-cell title="身份证地址" :value="idcard.shortAddress" :value-style="style"
						@click="showAddressContent" />
					<u-cell title="身份证有效期" :value="idcard.expiration" :value-style="style" :arrow="false" />
				</u-cell-group>
			</view>
		</view>

		<!-- 联系方式 -->
		<view class="card">
			<view class="card-title">联系方式</view>
			<view class="list">
				<u-cell-group border="false">
					<u-cell title="手机号码" :value="contact.tel" :value-style="style"
						@click="enterContent('手机号码', 'tel')" />
					<u-cell title="电子信箱" :value="contact.shortEmail" :value-style="style"
						@click="enterContent('电子信箱', 'email')" />
					<u-cell title="收信地址" :value="contact.shortMailAddress" :value-style="style"
						@click="enterContent('收信地址', 'mailAddress')" />
					<u-cell title="紧急联系人" :value="contact.contactName" :value-style="style"
						@click="enterContent('紧急联系人', 'contactName')" />
					<u-cell title="紧急联系人电话" :value="contact.contactTel" :value-style="style"
						@click="enterContent('紧急联系人电话', 'contactTel')" />
				</u-cell-group>
			</view>
		</view>

		<!-- 驾驶证 -->
		<view class="card">
			<view class="card-title">驾驶证信息</view>
			<view class="list">
				<u-cell-group border="false">
					<u-cell title="驾驶证类型" :value="drcard.carClass" :value-style="style" :arrow="false" />
					<u-cell title="驾驶证有效期" :value="drcard.validTo" :value-style="style" :arrow="false" />
					<u-cell title="初次领证日期" :value="drcard.issueDate" :value-style="style" :arrow="false" />
				</u-cell-group>
			</view>
		</view>

		<!-- 保存按钮 -->
		<view class="btn-area">
			<button class="btn" @tap="save" :disabled="realAuth == 3">
				<view class="btn-bg"></view>
				<text class="btn-text">保存信息</text>
				<text class="btn-arrow">→</text>
			</button>
		</view>

		<!-- 底部说明 -->
		<view class="remark">您每次提交实名信息之后，都需要工作人员严格审查，请等候1~3天，这期间您将无法接单，特此声明！</view>

		<up-toast ref="uToastRef"></up-toast>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive
	} from 'vue'
	import {
		onLoad
	} from '@dcloudio/uni-app'
	import {
		url
	} from '@/utils/common.js'

	const uToastRef = ref(null)
	const mode = ref('fill')
	const style = reactive({
		color: '#ff8a3d'
	})
	const cardBackground = ref(new Array(6).fill('../../static/filling/credentials-bg.jpg'))

	// realAuth 是需要存储在 storage 当中的。
	// 这行代码表示从storage中获取realAuth它的值。
	const realAuth = ref(uni.getStorageSync('realAuth'))

	const idcard = reactive({
		pid: '',
		name: '',
		sex: '',
		address: '',
		shortAddress: '',
		birthday: '',
		expiration: '',
		idcardFront: '',
		idcardBack: '',
		idcardHolding: ''
	})

	const contact = reactive({
		tel: '',
		email: '',
		shortEmail: '',
		mailAddress: '',
		shortMailAddress: '',
		contactName: '',
		contactTel: ''
	})

	const drcard = reactive({
		issueDate: '',
		carClass: '',
		validFrom: '',
		validTo: '',
		drcardFront: '',
		drcardBack: '',
		drcardHolding: ''
	})

	/**
	 * 页面加载回调
	 * - mode=create 表示新司机的注册，弹提示
	 * - 否则查询司机实名信息并回显
	 */
	onLoad((options) => {
		// create/fill
		mode.value = options.mode

		// 未实名(realAuth == 1)：弹提示，让它去填写实名认证信息。
		if (uni.getStorageSync("realAuth") == 1) {
			uni.showModal({
				title: "提示信息",
				content: "新注册的代驾司机请填写实名认证信息，并且上传相关证件照片",
				showCancel: false
			})
			// 没必要查询，直接结束，因为这个司机还没有提交过实名认证信息。
			return;
		}

		// 已实名：查询认证信息并进行回显
		ajax(url.searchDriverAuth, "GET", null, (resp) => {
			const json = resp.data.result;

			// 身份证
			idcard.pid = json.pid
			idcard.name = json.name
			idcard.sex = json.sex
			idcard.birthday = json.birthday
			idcard.address = json.idcardAddress
			idcard.shortAddress = shorten(json.idcardAddress, 15)
			idcard.expiration = json.idcardExpiration
			idcard.idcardFront = json.idcardFront
			idcard.idcardBack = json.idcardBack
			idcard.idcardHolding = json.idcardHolding

			// 联系方式
			contact.tel = json.tel
			contact.email = json.email
			contact.shortEmail = shorten(json.email, 25)
			contact.mailAddress = json.mailAddress
			contact.shortMailAddress = shorten(json.mailAddress, 15)
			contact.contactName = json.contactName
			contact.contactTel = json.contactTel

			// 驾驶证信息
			drcard.carClass = json.drcardType
			drcard.validTo = json.drcardExpiration
			drcard.issueDate = json.drcardIssueDate
			drcard.drcardFront = json.drcardFront
			drcard.drcardBack = json.drcardBack
			drcard.drcardHolding = json.drcardHolding

			// 证件照回显
			const urlMap = [
				[0, json.idcardFrontUrl],
				[1, json.idcardBackUrl],
				[2, json.idcardHoldingUrl],
				[3, json.drcardFrontUrl],
				[4, json.drcardBackUrl],
				[5, json.drcardHoldingUrl]
			];

			urlMap.forEach(([idx, url]) => {
				if (url && url.length > 0) { // url不为空
					cardBackground.value[idx] = url;
				}
			})
		})
	})

	/**
	 * 字符串缩略：超过 max 长度时截断并加省略号
	 */
	function shorten(str, max) {
		if (!str) return ''
		return str.length > max ? str.substring(0, max) + '...' : str
	}

	function onCameraTap(index) {
		if (index === 0) scanIdcardFront()
		else if (index === 1) scanIdcardBack()
		else if (index === 2) takePhoto('idcardHolding')
		else if (index === 3) scanDrcardFront()
		else if (index === 4) takePhoto('drcardBack')
		else takePhoto('drcardHolding')
	}

	// 身份证正面识别（上传到COS，身份证正面信息识别，返回识别结果，页面上要回显证件照片以及识别的信息）
	function scanIdcardFront() {
		// 调用uni-app的方法，实现能够在相册当中选择一张图片或者拍照。
		uni.chooseImage({
			// 最多选择1张
			count: 1,
			// 选择成功之后的回调函数
			success: (chooseResponse) => {
				// chooseResponse 其实就是被选中的资源本身。
				const tempFilePath = chooseResponse.tempFilePaths[0];
				console.log("图片在相册当中的路径: ", tempFilePath);

				// 显示一个加载中
				uni.showLoading({
					title: "识别中..."
				});

				// 将图片上传到后端服务器（不是ajax请求。）
				uni.uploadFile({
					url: url.recognizeAndUpload,
					filePath: tempFilePath,
					name: "file",
					// 传文件的同时，还要提交一些参数
					formData: {
						ocrType: "1", // 1代表我现在做的是身份证正面的识别。（给后端程序参考的，因为后端代码不同的识别需要调用不同的方法）
						module: "driverAuth"
					},
					success: (uploadResponse) => {
						// 文件上传成功，后端已经处理完成了。
						uni.hideLoading();

						// uni.uploadFile() 方法的返回值是json字符串。
						// uploadResponse.data 是一个json字符串，而不是对象。
						const result = JSON.parse(uploadResponse.data);
						console.log("身份证正面识别结果：", result);
						// 身份证的正面信息获取到
						const idCardFrontInfo = result.idCardFrontInfo;
						// 把身份证信息赋值到对应的变量上，页面上的信息就显示出来了。
						// 识别结果同步到 idcard 响应式对象
						idcard.name = idCardFrontInfo.name
						idcard.sex = idCardFrontInfo.gender
						idcard.pid = idCardFrontInfo.idNumber
						idcard.address = idCardFrontInfo.address
						const fullAddress = idCardFrontInfo.address || ''
						idcard.shortAddress = fullAddress.length > 10 ? fullAddress.substring(0,
							10) + '...' : fullAddress

						// 身份证号 18 位时解析出生日（yyyy-MM-dd）
						if (idCardFrontInfo.idNumber.length === 18) {
							const pid = idCardFrontInfo.idNumber
							idcard.birthday =
								`${pid.substring(6, 10)}-${pid.substring(10, 12)}-${pid.substring(12, 14)}`
						}

						// 存图片路径（保存数据库用，后续可换取临时 URL）
						idcard.idcardFront = result.filePath

						// 把临时 URL 放到 cardBackground[0]，用于证件照卡片回显
						cardBackground.value[0] = result.tempUrl

						uni.showToast({
							title: '识别成功'
						})
					},
					fail: (err) => {
						console.log("身份证正面识别失败:", err);
					},
					complete: () => {
						// 无论成功还是失败，最后完成之后都要把正在加载中隐藏起来。
						uni.hideLoading();
					}
				})
			}
		});
	}

	/**
	 * 扫描身份证背面
	 * 流程：
	 *   1. 选图 / 拍照（最多 1 张）
	 *   2. 把图片上传给后端（后端做 OCR 识别 + 保存到 COS）
	 *   3. 后端返回识别结果（有效期 validPeriod）+ 文件路径 + 临时访问 URL
	 *   4. 把有效期格式化后写入 idcard.expiration
	 *   5. 把图片路径存到 idcard.idcardBack（保存数据库用）
	 *   6. 把临时 URL 存到 cardBackground[1]（回显用）
	 */
	function scanIdcardBack() {
		// uni-app 的方法，用于选择相册中的图片或拍照
		uni.chooseImage({
			// 最多选择 1 张
			count: 1,
			success(chooseRes) {
				// 从数组中取出图片的路径
				const tempFilePath = chooseRes.tempFilePaths[0]

				// 显示加载中
				uni.showLoading({
					title: '识别中...'
				})

				// 将文件上传给服务器：后端做 OCR 识别 + 保存到腾讯云 COS
				uni.uploadFile({
					url: url.recognizeAndUpload,
					filePath: tempFilePath,
					name: 'file',
					// ocrType=2 表示身份证背面
					// module=driverAuth 表示业务模块为司机实名
					formData: {
						ocrType: '2',
						module: 'driverAuth'
					},
					success(uploadRes) {
						uni.hideLoading()

						// uni.uploadFile 返回的是 JSON 字符串，需要手动 parse
						const result = JSON.parse(uploadRes.data)
						const idCardBackInfo = result.idCardBackInfo

						// 把有效期（如 20150312-20350312）格式化为 yyyy-MM-dd 存入响应式对象
						idcard.expiration = dateFormat(idCardBackInfo.validPeriod)

						// 存图片路径（保存数据库用，后续可换取临时 URL）
						idcard.idcardBack = result.filePath
						console.log(idcard.idcardBack)

						// Vue 3 直接赋值即可触发响应式更新（替代原来的 $set）
						// 把临时 URL 放到 cardBackground[1]，用于证件照卡片回显
						cardBackground.value[1] = result.tempUrl

						uni.showToast({
							title: '识别成功'
						})
					},
					fail: (err) => {
						console.error("背面上传失败：", err)
					},
					complete: () => {
						uni.hideLoading();
					}
				})
			}
		})
	}

	/**
	 * 日期格式化
	 * 把 8 位纯数字日期字符串（如 20150312）转成 yyyy-MM-dd
	 */
	function dateFormat(dateStr) {
		return `${dateStr.substring(0, 4)}-${dateStr.substring(4, 6)}-${dateStr.substring(6, 8)}`
	}

	/**
	 * 扫描驾驶证正面
	 * 流程：
	 *   1. 选图 / 拍照（最多 1 张）
	 *   2. 把图片上传给后端（后端做 OCR 识别 + 保存到 COS）
	 *   3. 后端返回识别结果（初次领证日期、车型、有效期等）+ 文件路径 + 临时访问 URL
	 *   4. 把识别结果同步到 drcard 响应式对象
	 *   5. 把图片路径存到 drcard.drcardFront（保存数据库用）
	 *   6. 把临时 URL 存到 cardBackground[3]（回显用）
	 */
	function scanDrcardFront() {
		// uni-app 的方法，用于选择相册中的图片或拍照
		uni.chooseImage({
			// 最多选择 1 张
			count: 1,
			success(chooseRes) {
				// 从数组中取出图片的路径
				const tempFilePath = chooseRes.tempFilePaths[0]

				// 显示加载中
				uni.showLoading({
					title: '识别中...'
				})

				// 将文件上传给服务器：后端做 OCR 识别 + 保存到腾讯云 COS
				uni.uploadFile({
					url: url.recognizeAndUpload,
					filePath: tempFilePath,
					name: 'file',
					formData: {
						ocrType: '3',
						module: 'driverAuth'
					},
					success(uploadRes) {
						// uni.uploadFile 返回的是 JSON 字符串，需要手动 parse
						const result = JSON.parse(uploadRes.data)
						const drcardFrontInfo = result.drcardFrontInfo

						// 识别结果同步到 drcard 响应式对象，日期字段做格式化
						drcard.issueDate = dateFormat(drcardFrontInfo.issueDate)
						drcard.carClass = drcardFrontInfo.carClass
						drcard.validFrom = dateFormat(drcardFrontInfo.validFrom)
						drcard.validTo = dateFormat(drcardFrontInfo.validTo)

						// 存图片路径（保存数据库用，后续可换取临时 URL）
						drcard.drcardFront = result.filePath
						console.log(drcard.drcardFront)

						// 把临时 URL 放到 cardBackground[3]，用于证件照卡片回显
						cardBackground.value[3] = result.tempUrl

						uni.showToast({
							title: '识别成功'
						})
					},
					fail(err) {
						console.error('驾驶证正面上传失败：', err)
					},
					complete() {
						uni.hideLoading()
					}
				})
			}
		})
	}

	// 跳转到拍照页面，传type参数
	function takePhoto(type) {
		uni.navigateTo({
			url: '/identity/identity_camera/identity_camera?type=' + type
		});
	}

	// 上传图片到腾讯云服务器
	function updatePhoto(type, path) {
		// 显示一个上传中
		setTimeout(() => {
			uni.showLoading({
				title: "上传中"
			});
		}, 30);

		// 文件上传
		uni.uploadFile({
			url: url.recognizeAndUpload,
			filePath: path,
			name: "file",
			formData: {
				module: "driverAuth",
				ocrType: "0", // 0代表不做OCR识别。
			},
			success(uploadResponse) {
				// 文件上传成功之后的处理.
				const result = JSON.parse(uploadResponse.data);
				if (type === "idcardHolding") {
					// 把存储桶中的图片路径赋值给响应式对象，将来保存到数据库当中。
					idcard.idcardHolding = result.filePath;
					// 回显图片
					cardBackground.value[2] = result.tempUrl;
				} else if (type === "drcardBack") {
					// 保存到数据库的图片路径
					drcard.drcardBack = result.filePath;
					// 回显图片
					cardBackground.value[4] = result.tempUrl;
				} else if (type === "drcardHolding") {
					// 保存到数据库的图片路径
					drcard.drcardHolding = result.filePath;
					// 回显图片
					cardBackground.value[5] = result.tempUrl;
				}
			},
			fail(err) {
				console.log("文件上传失败", err);
			},
			complete() {
				// 不管成功还是失败，这里都会隐藏加载进度条
				uni.hideLoading();
			}
		});
	}

	// 暴露出去，这样其他vue页面才能调用。
	defineExpose({
		updatePhoto
	})

	function showAddressContent() {
		if (!idcard.address) return;

		uni.showModal({
			title: "身份证地址",
			content: idcard.address,
			showCancel: false
		})
	}

	function enterContent(title, key) {
		// 弹窗（modal窗口）
		uni.showModal({
			title: title, // 标题
			editable: true, // 可编辑的
			content: contact[key], // 用户如果填写过了，这个文本框中需要显示用户填写的内容。
			success(response) {
				// 什么时候执行到这里？用户点击了确定或者取消，都会走这里。
				// 用户点击了取消，直接结束即可。
				if (!response.confirm) return;

				// 用户点击了确定，首先要获取到用户填写的内容。
				const val = response.content;

				// 不管是哪个cell，这个代码是一定要执行的。
				contact[key] = val;

				if (key === "email") {
					contact.shortEmail = val.length > 25 ? val.substr(0, 25) + "..." : val;
				} else if (key === "mailAddress") {
					contact.shortMailAddress = val.length > 15 ? val.substr(0, 15) + "..." : val;
				}
			}
		})
	}

	import {
		checkValidTel,
		checkValidEmail,
		checkValidAddress,
		checkValidName,
		ajax
	} from '@/utils/common.js'

	function save() {
		// 从对象中解构数据，为提交的数据做准备
		const {
			idcardFront,
			idcardBack,
			idcardHolding,
			expiration
		} = idcard
		const {
			drcardFront,
			drcardBack,
			drcardHolding,
			carClass,
			validTo,
			issueDate
		} = drcard

		// 1. 校验证件照完整
		if (!idcardFront || !idcardBack || !idcardHolding ||
			!drcardFront || !drcardBack || !drcardHolding) {
			uToastRef.value?.show({
				message: '证件上传不完整',
				type: 'error'
			})
			return
		}

		// 2. 校验联系方式（注意第一个参数是 uToastRef.value）
		const valid = checkValidTel(uToastRef.value, contact.tel, '手机号码') &&
			checkValidEmail(uToastRef.value, contact.email, '电子信箱') &&
			checkValidAddress(uToastRef.value, contact.mailAddress, '收信地址') &&
			checkValidName(uToastRef.value, contact.contactName, '联系人') &&
			checkValidTel(uToastRef.value, contact.contactTel, '联系人电话')

		if (!valid) return

		// 弹窗提示
		uni.showModal({
			title: "提示信息",
			content: "确认提交实名资料？",
			success(response) {
				// 司机点击的是取消
				if (!response.confirm) return;
				// 司机点击的是确认
				// 准备数据
				const data = {
					pid: idcard.pid,
					name: idcard.name,
					sex: idcard.sex,
					birthday: idcard.birthday,
					tel: contact.tel,
					email: contact.email,
					mailAddress: contact.mailAddress,
					contactName: contact.contactName,
					contactTel: contact.contactTel,
					idcardAddress: idcard.address,
					idcardFront,
					idcardBack,
					idcardHolding,
					idcardExpiration: expiration,
					drcardType: carClass,
					drcardExpiration: validTo,
					drcardIssueDate: issueDate,
					drcardFront,
					drcardBack,
					drcardHolding
				}
				// 发送ajax请求
				ajax(url.updateDriverAuth, "PUT", data, () => {
					uToastRef.value.show({
						message: "资料提交成功",
						type: "success",
						complete: () => {
							// 将 realAuth=3存储到storage当中。
							uni.setStorageSync("realAuth", 3);
							// 将响应式对象realAuth的值修改为3
							realAuth.value = 3;
							if (mode.value === "create") {
								// 新注册司机：跳转到采集人脸页面进行人脸采集
								uni.redirectTo({
									url: "/identity/face_camera/face_camera?mode=create"
								})
							} else {
								// 不是注册新司机，跳转到工作台。
								uni.switchTab({
									url: "/pages/workbench/workbench"
								})
							}
						}
					});
				});
			}
		})
	}
</script>

<style lang="less">
	@import url('filling.less');
</style>
