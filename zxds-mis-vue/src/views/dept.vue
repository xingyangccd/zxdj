<template>
	<div v-if="isAuth(['ROOT', 'DEPT:SELECT'])">
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataFormRef">
			<el-form-item prop="deptName">
				<el-input v-model="dataForm.deptName" placeholder="部门名称" size="default" class="input" clearable />
			</el-form-item>
			<el-form-item>
				<el-button size="default" type="primary" @click="searchHandle()">查询</el-button>
				<el-button size="default" type="primary" :disabled="!isAuth(['ROOT', 'DEPT:INSERT'])"
					@click="addHandle()">
					新增
				</el-button>
				<el-button size="default" type="danger" :disabled="!isAuth(['ROOT', 'DEPT:DELETE'])"
					@click="deleteHandle()">
					批量删除
				</el-button>
			</el-form-item>
		</el-form>
		<el-table :data="dataList" border v-loading="dataListLoading" @selection-change="selectionChangeHandle"
			:cell-style="{ padding: '4px 0' }" size="default" style="width: 100%">
			<el-table-column type="selection" :selectable="selectable" header-align="center" align="center"
				width="50" />
			<el-table-column type="index" header-align="center" align="center" width="100" label="序号">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="deptName" header-align="center" align="center" label="部门名称" min-width="170" />
			<el-table-column prop="tel" header-align="center" align="center" label="联系电话" min-width="170" />
			<el-table-column prop="email" header-align="center" align="center" label="邮箱" min-width="270" />
			<el-table-column header-align="center" align="center" label="员工数量" min-width="140">
				<template #default="scope">
					<span>{{ scope.row.emps }}人</span>
				</template>
			</el-table-column>
			<el-table-column prop="desc" header-align="center" align="center" label="备注" min-width="400" />
			<el-table-column header-align="center" align="center" width="150" label="操作">
				<template #default="scope">
					<el-button type="primary" link size="default" :disabled="!isAuth(['ROOT', 'DEPT:UPDATE'])"
						@click="updateHandle(scope.row.id)">
						修改
					</el-button>
					<el-button type="danger" link size="default"
						:disabled="!isAuth(['ROOT', 'DEPT:DELETE']) || scope.row.emps > 0"
						@click="deleteHandle(scope.row.id)">
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
	import AddOrUpdate from './dept-add-or-update.vue'

	defineOptions({
		name: 'Dept'
	})

	const dataFormRef = ref(null)
	const addOrUpdateRef = ref(null)

	const dataForm = reactive({
		deptName: null
	})
	const dataList = ref([])
	const pageIndex = ref(1)
	const pageSize = ref(10)
	const totalCount = ref(0)
	const dataListLoading = ref(false)
	const dataListSelections = ref([])

	const dataRule = reactive({
		deptName: [{
			required: false,
			pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,10}$',
			message: '部门名称格式错误'
		}]
	})

	const loadDataList = () => {
		dataListLoading.value = true
		const data = {
			deptName: dataForm.deptName,
			page: pageIndex.value,
			length: pageSize.value
		}
		http('dept/searchDeptByPage', 'POST', data, true, (resp) => {
			const page = resp.page
			dataList.value = page.list
			totalCount.value = Number(page.totalCount)
			dataListLoading.value = false
		})
	}

	const searchHandle = () => {
		dataFormRef.value.validate((valid) => {
			if (valid) {
				dataFormRef.value.clearValidate()
				if (dataForm.deptName === '') dataForm.deptName = null
				if (pageIndex.value !== 1) pageIndex.value = 1
				loadDataList()
			}
		})
	}

	const selectable = (row) => row.emps <= 0

	const selectionChangeHandle = (val) => {
		dataListSelections.value = val
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

	const deleteHandle = (id) => {
		const ids = id ?
			[id] :
			dataListSelections.value.map((item) => item.id)
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
				http('dept/deleteDeptByIds', 'POST', {
					ids
				}, true, (resp) => {
					if (resp.rows > 0) {
						ElMessage.success({
							message: '操作成功',
							duration: 1200,
							onClose: () => loadDataList()
						})
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

	const addHandle = () => {
		nextTick(() => addOrUpdateRef.value?.init())
	}

	const updateHandle = (id) => {
		nextTick(() => addOrUpdateRef.value?.init(id))
	}

	onMounted(() => loadDataList())
</script>

<style></style>