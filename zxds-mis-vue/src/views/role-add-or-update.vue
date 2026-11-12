<template>
	<el-dialog :title="!dataForm.id ? '新增' : '修改'" v-if="isAuth(['ROOT', 'ROLE:INSERT', 'ROLE:UPDATE'])"
		:close-on-click-modal="false" v-model="visible" custom-class="dialog" width="692px">
		<el-form :model="dataForm" ref="dataFormRef" :rules="dataRule" label-width="60px">
			<el-form-item label="角色" prop="roleName">
				<el-input v-model="dataForm.roleName" size="default" style="width: 50%" :readonly="systemic"
					clearable />
				<span class="note">（ 角色名称必须是2~10个字符之间 ）</span>
			</el-form-item>
			<el-form-item label="备注" prop="desc">
				<el-input v-model="dataForm.desc" style="width: 50%" size="default" maxlength="20" clearable />
				<span class="note">（ 备注信息必须是20个字符以内 ）</span>
			</el-form-item>
			<el-form-item label="权限" prop="permissions">
				<el-transfer v-model="dataForm.permissions" :data="permisionList" size="default"
					:titles="['权限列表', '具备权限']" filterable filter-placeholder="请输入权限" />
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
		name: 'RoleAddOrUpdate'
	})

	const emit = defineEmits(['refreshDataList'])

	const visible = ref(false)
	const systemic = ref(true)
	const dataFormRef = ref(null)
	const permisionList = ref([])
	let oldPermissions = []

	const dataForm = reactive({
		id: null,
		roleName: null,
		permissions: [],
		desc: null,
		changed: false
	})

	const dataRule = reactive({
		roleName: [{
			required: true,
			pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{2,10}$',
			message: '角色名称格式错误'
		}],
		permissions: [{
				required: true,
				trigger: 'blur',
				message: '角色没有关联权限'
			},
			{
				required: false,
				trigger: 'change',
				message: '角色没有关联权限'
			}
		]
	})

	const init = (id, sys) => {
		dataForm.id = id || 0
		systemic.value = sys
		visible.value = true
		nextTick(() => {
			dataFormRef.value?.resetFields()
			let defaultPermissions = []
			if (id) {
				http('role/searchById', 'POST', {
					id
				}, false, (resp) => {
					dataForm.roleName = resp.roleName
					dataForm.desc = resp.desc
					const temp = []
					for (const one of JSON.parse(resp.permissions)) {
						temp.push(one + '')
					}
					dataForm.permissions = temp
					oldPermissions = JSON.parse(resp.permissions)
					defaultPermissions = resp.defaultPermissions
				})
			}
			http('permission/searchAllPermission', 'GET', null, true, (resp) => {
				const temp = []
				for (const one of resp.list) {
					let disabled = false
					if (systemic.value) {
						disabled = defaultPermissions.includes(one.id)
					}
					temp.push({
						key: one.id,
						label: `${one.moduleName}（${one.actionName}）`,
						disabled
					})
				}
				permisionList.value = temp
			})
		})
	}

	const dataFormSubmit = () => {
		dataFormRef.value.validate((valid) => {
			if (!valid) return
			dataForm.permissions.sort((a, b) => a - b)
			if (dataForm.permissions.join() !== oldPermissions.join()) {
				dataForm.changed = true
			} else {
				dataForm.changed = false
			}
			const url = `role/${!dataForm.id ? 'insert' : 'update'}`
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

<style lang="less" scoped>
	.note {
		margin-left: 20px;
		color: #999;
	}
</style>