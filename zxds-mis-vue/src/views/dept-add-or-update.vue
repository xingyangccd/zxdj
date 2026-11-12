<template>
	<el-dialog :title="!dataForm.id ? '新增' : '修改'" v-if="isAuth(['ROOT', 'DEPT:INSERT', 'DEPT:UPDATE'])"
		:close-on-click-modal="false" v-model="visible" width="420px">
		<el-form :model="dataForm" ref="dataFormRef" :rules="dataRule" label-width="60px">
			<el-form-item label="部门" prop="deptName">
				<el-input v-model="dataForm.deptName" size="default" style="width: 100%" clearable />
			</el-form-item>
			<el-form-item label="电话" prop="tel">
				<el-input v-model="dataForm.tel" size="default" style="width: 100%" clearable />
			</el-form-item>
			<el-form-item label="邮箱" prop="email">
				<el-input v-model="dataForm.email" size="default" style="width: 100%" clearable />
			</el-form-item>
			<el-form-item label="备注" prop="desc">
				<el-input v-model="dataForm.desc" style="width: 100%" size="default" maxlength="20" clearable />
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
		name: 'DeptAddOrUpdate'
	})

	const emit = defineEmits(['refreshDataList'])

	const visible = ref(false)
	const dataFormRef = ref(null)

	const dataForm = reactive({
		id: null,
		deptName: null,
		tel: null,
		email: null,
		desc: null
	})

	const dataRule = reactive({
		deptName: [{
			required: true,
			pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{2,10}$',
			message: '部门名称格式错误'
		}],
		tel: [{
			required: false,
			pattern: '^1\\d{10}$|^(0\\d{2,3}\\-){0,1}[1-9]\\d{6,7}$',
			message: '电话格式错误'
		}],
		email: [{
			required: false,
			pattern: '^([a-zA-Z]|[0-9])(\\w|\\-)+@[a-zA-Z0-9]+\\.([a-zA-Z]{2,4})$',
			message: '邮箱格式错误'
		}]
	})

	const init = (id) => {
		dataForm.id = id || 0
		visible.value = true
		nextTick(() => {
			dataFormRef.value?.resetFields()
			if (id) {
				http('dept/searchById', 'POST', {
					id
				}, true, (resp) => {
					dataForm.deptName = resp.deptName
					dataForm.tel = resp.tel
					dataForm.email = resp.email
					dataForm.desc = resp.desc
				})
			}
		})
	}

	const dataFormSubmit = () => {
		dataFormRef.value.validate((valid) => {
			if (!valid) return
			const url = `dept/${!dataForm.id ? 'insert' : 'update'}`
			http(url, 'POST', dataForm, true, (resp) => {
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