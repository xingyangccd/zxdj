<template>
	<el-dialog :title="!dataForm.id ? '新增' : '修改'" v-if="isAuth(['ROOT', 'USER:INSERT', 'USER:UPDATE'])"
		:close-on-click-modal="false" v-model="visible" width="450px">
		<el-form :model="dataForm" ref="dataFormRef" :rules="dataRule" label-width="80px">
			<el-form-item label="用户名" prop="username">
				<el-input v-model="dataForm.username" size="default" clearable />
			</el-form-item>
			<el-form-item label="密码" prop="password" v-if="!dataForm.id">
				<el-input type="password" v-model="dataForm.password" size="default" show-password clearable />
			</el-form-item>
			<el-form-item label="姓名" prop="name">
				<el-input v-model="dataForm.name" size="default" clearable />
			</el-form-item>
			<el-form-item label="性别" prop="sex">
				<el-select v-model="dataForm.sex" size="default" style="width: 100%" clearable>
					<el-option label="男" value="男" />
					<el-option label="女" value="女" />
				</el-select>
			</el-form-item>
			<el-form-item label="电话" prop="tel">
				<el-input v-model="dataForm.tel" size="default" clearable />
			</el-form-item>
			<el-form-item label="邮箱" prop="email">
				<el-input v-model="dataForm.email" size="default" clearable />
			</el-form-item>
			<el-form-item label="角色" prop="role">
				<el-select v-model="dataForm.role" size="default" placeholder="选择角色" style="width: 100%" multiple
					clearable>
					<el-option v-for="one in roleList" :key="one.id" :label="one.roleName" :value="one.id"
						:disabled="one.roleName === '超级管理员'" />
				</el-select>
			</el-form-item>
			<el-form-item label="部门" prop="deptId">
				<el-select v-model="dataForm.deptId" size="default" placeholder="选择部门" style="width: 100%" clearable>
					<el-option v-for="one in deptList" :key="one.id" :label="one.deptName" :value="one.id" />
				</el-select>
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
		ref,
		reactive,
		nextTick
	} from 'vue'
	import {
		ElMessage
	} from 'element-plus'
	import {
		http
	} from '@/utils/http'
	import {
		isAuth
	} from '@/utils/auth'

	defineOptions({
		name: 'UserAddOrUpdate'
	})

	const emit = defineEmits(['refreshDataList'])

	const visible = ref(false)
	const dataFormRef = ref(null)
	const roleList = ref([])
	const deptList = ref([])

	const dataForm = reactive({
		id: null,
		username: null,
		password: null,
		name: null,
		sex: null,
		tel: null,
		email: null,
		role: null,
		deptId: null,
		status: 1
	})

	const dataRule = reactive({
		username: [{
			required: true,
			pattern: '^[a-zA-Z0-9]{5,20}$',
			message: '用户名格式错误'
		}],
		password: [{
			required: true,
			pattern: '^[a-zA-Z0-9]{6,20}$',
			message: '密码格式错误'
		}],
		name: [{
			required: true,
			pattern: '^[\u4e00-\u9fa5]{2,10}$',
			message: '姓名格式错误'
		}],
		sex: [{
			required: true,
			message: '性别不能为空'
		}],
		tel: [{
			required: true,
			pattern: '^1\\d{10}$',
			message: '电话格式错误'
		}],
		email: [{
			required: true,
			pattern: '^([a-zA-Z]|[0-9])(\\w|\\-)+@[a-zA-Z0-9]+\\.([a-zA-Z]{2,4})$',
			message: '邮箱格式错误'
		}],
		role: [{
			required: true,
			message: '角色不能为空'
		}],
		deptId: [{
			required: true,
			message: '部门不能为空'
		}]
	})

	const init = (id) => {
		dataForm.id = id || 0
		visible.value = true
		nextTick(() => {
			dataFormRef.value?.resetFields()
			http('role/searchAllRole', 'GET', null, true, (resp) => {
				roleList.value = resp.list
			})
			http('dept/searchAllDept', 'GET', null, true, (resp) => {
				deptList.value = resp.list
			})
			if (dataForm.id) {
				http('user/searchById', 'POST', {
					userId: id
				}, true, (resp) => {
					dataForm.username = resp.username
					dataForm.password = resp.password
					dataForm.name = resp.name
					dataForm.sex = resp.sex
					dataForm.tel = resp.tel
					dataForm.email = resp.email
					const temp = []
					for (const one of JSON.parse(resp.role)) {
						temp.push(one + '')
					}
					dataForm.role = temp
					dataForm.deptId = resp.deptId + ''
					dataForm.status = resp.status
				})
			}
		})
	}

	const dataFormSubmit = () => {
		dataFormRef.value.validate((valid) => {
			if (!valid) return
			const data = {
				userId: dataForm.id,
				username: dataForm.username,
				password: dataForm.password,
				name: dataForm.name,
				sex: dataForm.sex,
				tel: dataForm.tel,
				email: dataForm.email,
				role: dataForm.role,
				deptId: dataForm.deptId,
				status: dataForm.status
			}
			const url = `user/${!dataForm.id ? 'insert' : 'update'}`
			http(url, 'POST', data, true, (resp) => {
				if (resp.rows === 1) {
					ElMessage.success({
						message: '操作成功',
						duration: 1200
					})
					visible.value = false
					emit('refreshDataList')
				} else {
					ElMessage.error({
						message: '操作失败',
						duration: 1200
					})
				}
			})
		})
	}
</script>

<style lang="less" scoped></style>