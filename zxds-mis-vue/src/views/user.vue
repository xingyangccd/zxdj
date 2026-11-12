<template>
	<div v-if="isAuth(['ROOT', 'USER:SELECT'])">
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataFormRef">
			<el-form-item prop="name">
				<el-input v-model="dataForm.name" placeholder="姓名" size="default" class="input" clearable />
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.sex" class="input" placeholder="性别" size="default" clearable>
					<el-option label="男" value="男" />
					<el-option label="女" value="女" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.role" class="input" placeholder="角色" size="default" clearable>
					<el-option v-for="one in roleList" :key="one.roleName" :label="one.roleName"
						:value="one.roleName" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.deptId" class="input" placeholder="部门" size="default" clearable>
					<el-option v-for="one in deptList" :key="one.id" :label="one.deptName" :value="one.id" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.status" class="input" placeholder="状态" size="default" clearable>
					<el-option label="在职" value="1" />
					<el-option label="离职" value="2" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button size="default" type="primary" @click="searchHandle()">查询</el-button>
				<el-button size="default" type="primary" :disabled="!isAuth(['ROOT', 'USER:INSERT'])"
					@click="addHandle()">
					新增
				</el-button>
				<el-button size="default" type="danger" :disabled="!isAuth(['ROOT', 'USER:DELETE'])"
					@click="deleteHandle()">
					批量删除
				</el-button>
			</el-form-item>
		</el-form>
		<el-table :data="dataList" border v-loading="dataListLoading" @selection-change="selectionChangeHandle"
			:cell-style="{ padding: '4px 0' }" style="width: 100%" size="default">
			<el-table-column type="selection" header-align="center" align="center" width="50" />
			<el-table-column type="index" header-align="center" align="center" width="100" label="序号">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="name" header-align="center" align="center" min-width="100" label="姓名" />
			<el-table-column prop="sex" header-align="center" align="center" min-width="60" label="性别" />
			<el-table-column prop="tel" header-align="center" align="center" min-width="130" label="电话" />
			<el-table-column prop="email" header-align="center" align="center" min-width="240" label="邮箱"
				:show-overflow-tooltip="true" />
			<el-table-column prop="roles" header-align="center" align="center" min-width="150" label="角色"
				:show-overflow-tooltip="true" />
			<el-table-column prop="dept" header-align="center" align="center" min-width="120" label="部门" />
			<el-table-column prop="status" header-align="center" align="center" min-width="100" label="状态" />
			<el-table-column header-align="center" align="center" width="150" label="操作">
				<template #default="scope">
					<el-button type="primary" link size="default" v-if="isAuth(['ROOT', 'USER:UPDATE'])"
						@click="updateHandle(scope.row.id)">
						修改
					</el-button>
					<el-button type="warning" link size="default" v-if="isAuth(['ROOT', 'USER:UPDATE'])"
						:disabled="scope.row.status === '离职' || scope.row.root" @click="dimissHandle(scope.row.id)">
						离职
					</el-button>
					<el-button type="danger" link size="default" :disabled="scope.row.root"
						v-if="isAuth(['ROOT', 'USER:DELETE'])" @click="deleteHandle(scope.row.id)">
						删除
					</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" :current-page="pageIndex"
			:page-sizes="[10, 20, 50]" :page-size="pageSize" :total="totalCount"
			layout="total, sizes, prev, pager, next, jumper" />
		<add-or-update ref="addOrUpdateRef" @refreshDataList="loadDataList" />
	</div>
</template>

<script setup>
	import {
		ref,
		reactive,
		onMounted,
		nextTick
	} from 'vue'
	import {
		ElMessage,
		ElMessageBox
	} from 'element-plus'
	import {
		http
	} from '@/utils/http'
	import {
		isAuth
	} from '@/utils/auth'
	import AddOrUpdate from './user-add-or-update.vue'

	defineOptions({
		name: 'User'
	})

	const dataFormRef = ref(null)
	const addOrUpdateRef = ref(null)

	const dataForm = reactive({
		name: null,
		sex: null,
		role: null,
		deptId: null,
		status: null
	})
	const dataList = ref([])
	const roleList = ref([])
	const deptList = ref([])
	const pageIndex = ref(1)
	const pageSize = ref(10)
	const totalCount = ref(0)
	const dataListLoading = ref(false)
	const dataListSelections = ref([])

	const dataRule = reactive({
		name: [{
			required: false,
			pattern: '^[\u4e00-\u9fa5]{1,10}$',
			message: '姓名格式错误'
		}]
	})

	const loadDataList = () => {
		dataListLoading.value = true
		const data = {
			page: pageIndex.value,
			length: pageSize.value,
			name: dataForm.name === '' ? null : dataForm.name,
			sex: dataForm.sex === '' ? null : dataForm.sex,
			role: dataForm.role === '' ? null : dataForm.role,
			deptId: dataForm.deptId === '' ? null : dataForm.deptId,
			status: dataForm.status === '' ? null : dataForm.status
		}
		http('user/searchUserByPage', 'POST', data, true, (resp) => {
			const page = resp.page
			const list = page.list
			for (const one of list) {
				if (one.status === 1) one.status = '在职'
				else if (one.status === 2) one.status = '离职'
			}
			dataList.value = list
			totalCount.value = Number(page.totalCount)
			dataListLoading.value = false
		})
	}

	const loadRoleList = () => {
		http('role/searchAllRole', 'GET', null, true, (resp) => {
			roleList.value = resp.list
		})
	}

	const loadDeptList = () => {
		http('dept/searchAllDept', 'GET', null, true, (resp) => {
			deptList.value = resp.list
		})
	}

	const sizeChangeHandle = (val) => {
		pageSize.value = val
		pageIndex.value = 1
		loadDataList()
	}

	const currentChangeHandle = (val) => {
		pageIndex.value = val
		loadDataList()
	}

	const selectionChangeHandle = (val) => {
		dataListSelections.value = val
	}

	const searchHandle = () => {
		dataFormRef.value.validate((valid) => {
			if (valid) {
				dataFormRef.value.clearValidate()
				if (dataForm.name === '') dataForm.name = null
				if (pageIndex.value !== 1) pageIndex.value = 1
				loadDataList()
			}
		})
	}

	const addHandle = () => nextTick(() => addOrUpdateRef.value?.init())

	const updateHandle = (id) => nextTick(() => addOrUpdateRef.value?.init(id))

	const dimissHandle = (id) => {
		ElMessageBox.confirm('确定要将该用户设置为离职状态？', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
			.then(() => {
				http('user/dismiss', 'POST', {
					userId: id
				}, true, (resp) => {
					if (resp.rows === 1) {
						ElMessage.success({
							message: '操作成功',
							duration: 1200
						})
						loadDataList()
					} else {
						ElMessage.warning({
							message: '操作失败',
							duration: 1200
						})
					}
				})
			})
			.catch(() => {})
	}

	const deleteHandle = (id) => {
		const ids = id ? [id] : dataListSelections.value.map((item) => item.id)
		if (ids.length === 0) {
			ElMessage.warning({
				message: '没有选中记录',
				duration: 1200
			})
			return
		}
		ElMessageBox.confirm('确定要删除选中的记录？', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
			.then(() => {
				http('user/deleteUserByIds', 'POST', {
					ids
				}, true, (resp) => {
					if (resp.rows > 0) {
						ElMessage.success({
							message: '操作成功',
							duration: 1200
						})
						loadDataList()
					} else {
						ElMessage.warning({
							message: '未能删除记录',
							duration: 1200
						})
					}
				})
			})
			.catch(() => {})
	}

	onMounted(() => {
		loadDataList()
		loadRoleList()
		loadDeptList()
	})
</script>

<style lang="less" scoped></style>