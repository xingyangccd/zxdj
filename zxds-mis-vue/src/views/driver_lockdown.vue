<template>
	<div v-if="isAuth(['ROOT', 'DRIVER_LOCKDOWN:SELECT'])">
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataFormRef">
			<el-form-item prop="driverId">
				<el-input v-model="dataForm.driverId" placeholder="司机编号" size="default" clearable />
			</el-form-item>
			<el-form-item prop="tel">
				<el-input v-model="dataForm.tel" placeholder="司机电话" size="default" class="input" clearable />
			</el-form-item>
			<el-form-item prop="orderId">
				<el-input v-model="dataForm.orderId" placeholder="订单编号" size="default" clearable />
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.effect" class="input" placeholder="状态" size="default" clearable>
					<el-option label="有效" value="true" />
					<el-option label="过期" value="false" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button size="default" type="primary" @click="searchHandle()">查询</el-button>
				<el-button size="default" type="primary" :disabled="!isAuth(['ROOT', 'DRIVER_LOCKDOWN:INSERT'])"
					@click="addHandle()">
					新增
				</el-button>
				<el-button size="default" type="danger" :disabled="!isAuth(['ROOT', 'DRIVER_LOCKDOWN:DELETE'])"
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
			<el-table-column prop="driverId" header-align="center" align="center" label="司机编号" min-width="200" />
			<el-table-column prop="name" header-align="center" align="center" label="司机姓名" min-width="130" />
			<el-table-column prop="orderId" header-align="center" align="center" label="订单编号" min-width="200" />
			<el-table-column prop="reason" header-align="center" align="center" label="具体原因" min-width="300" />
			<el-table-column prop="dateRange" header-align="center" align="center" label="日期范围" min-width="200" />
			<el-table-column prop="effect" header-align="center" align="center" label="状态" min-width="130" />
			<el-table-column header-align="center" align="center" width="150" label="操作">
				<template #default="scope">
					<el-button type="primary" link size="default"
						:disabled="!isAuth(['ROOT', 'DRIVER_LOCKDOWN:UPDATE']) || scope.row.effect === '过期'"
						@click="updateHandle(scope.row.id)">
						修改
					</el-button>
					<el-button type="danger" link size="default" :disabled="!isAuth(['ROOT', 'DRIVER_LOCKDOWN:DELETE'])"
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
	import AddOrUpdate from './driver_lockdown-add-or-update.vue'

	defineOptions({
		name: 'DriverLockdown'
	})

	const dataFormRef = ref(null)
	const addOrUpdateRef = ref(null)

	const dataForm = reactive({
		driverId: null,
		tel: null,
		orderId: null,
		effect: null
	})

	const dataList = ref([])
	const pageIndex = ref(1)
	const pageSize = ref(10)
	const totalCount = ref(0)
	const dataListLoading = ref(false)
	const dataListSelections = ref([])

	const dataRule = reactive({
		driverId: [{
			required: false,
			pattern: '^[1-9]\\d{17}$',
			message: '司机编号格式错误'
		}],
		tel: [{
			required: false,
			pattern: '^1\\d{10}$',
			message: '电话格式错误'
		}],
		orderId: [{
			required: false,
			pattern: '^[1-9]\\d{17}$',
			message: '订单编号格式错误'
		}]
	})

	const loadDataList = () => {
		dataListLoading.value = true
		const data = {
			page: pageIndex.value,
			length: pageSize.value,
			driverId: dataForm.driverId,
			tel: dataForm.tel,
			orderId: dataForm.orderId,
			effect: dataForm.effect
		}
		http('driver_lockdown/searchByPage', 'POST', data, true, (resp) => {
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
				if (pageIndex.value !== 1) pageIndex.value = 1
				loadDataList()
			}
		})
	}

	const selectable = (row) => row.effect !== '过期'

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

	const addHandle = () => nextTick(() => addOrUpdateRef.value?.init())

	const updateHandle = (id) => nextTick(() => addOrUpdateRef.value?.init(id))

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
				http('driver_lockdown/deleteByIds', 'POST', {
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

	onMounted(() => loadDataList())
</script>

<style></style>