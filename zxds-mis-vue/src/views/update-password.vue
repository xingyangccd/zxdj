<template>
	<el-dialog title="提示" v-model="visible" width="25%">
		<el-form :model="dataForm" :rules="dataRule" ref="dataFormRef" label-width="80px">
			<el-form-item label="原密码" prop="password">
				<el-input type="password" v-model="dataForm.password" size="default" show-password clearable />
			</el-form-item>
			<el-form-item label="新密码" prop="newPassword">
				<el-input type="password" v-model="dataForm.newPassword" size="default" show-password clearable />
			</el-form-item>
			<el-form-item label="确认密码" prop="confirmPassword">
				<el-input type="password" v-model="dataForm.confirmPassword" size="default" show-password clearable />
			</el-form-item>
		</el-form>
		<template #footer>
			<span class="dialog-footer">
				<el-button size="default" @click="visible = false">取消</el-button>
				<el-button type="primary" size="default" @click="dataFormSubmit">确定</el-button>
			</span>
		</template>
	</el-dialog>
</template>

<script setup>
	import {
		reactive,
		ref,
		nextTick
	} from 'vue'
	import {
		ElMessage
	} from 'element-plus'
	import {
		http
	} from '@/utils/http'

	defineOptions({
		name: 'UpdatePassword'
	})

	const visible = ref(false)
	const dataFormRef = ref(null)

	const dataForm = reactive({
		password: '',
		newPassword: '',
		confirmPassword: ''
	})

	// 确认密码校验器
	const validateConfirmPassword = (rule, value, callback) => {
		if (value !== dataForm.newPassword) {
			callback(new Error('两次输入的密码不一致'))
		} else {
			callback()
		}
	}

	const dataRule = reactive({
		password: [{
			required: true,
			pattern: '^[a-zA-Z0-9]{6,20}$',
			message: '密码格式错误'
		}],
		newPassword: [{
			required: true,
			pattern: '^[a-zA-Z0-9]{6,20}$',
			message: '密码格式错误'
		}],
		confirmPassword: [{
				required: true,
				pattern: '^[a-zA-Z0-9]{6,20}$',
				message: '密码格式错误'
			},
			{
				validator: validateConfirmPassword,
				trigger: 'blur'
			}
		]
	})

	// 打开对话框
	const init = () => {
		visible.value = true
		nextTick(() => {
			dataFormRef.value?.resetFields()
		})
	}

	// 提交
	const dataFormSubmit = () => {
		dataFormRef.value.validate((valid) => {
			if (valid) {
				const data = {
					oldPassword: dataForm.password,
					newPassword: dataForm.confirmPassword
				}
				http('user/updatePassword', 'POST', data, true, (resp) => {
					if (resp.rows === 1) {
						ElMessage.success({
							message: '密码修改成功',
							duration: 1200
						})
						visible.value = false
					} else {
						ElMessage.error({
							message: '密码修改失败',
							duration: 1200
						})
					}
				})
			}
		})
	}
</script>

<style scoped></style>