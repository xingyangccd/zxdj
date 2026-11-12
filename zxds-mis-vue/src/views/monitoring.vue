<template>
	<div v-if="isAuth(['ROOT', 'MONITORING:SELECT'])">
		<div class="form">
			<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataFormRef">
				<el-form-item prop="orderId">
					<el-input v-model="dataForm.orderId" placeholder="订单编号" size="default" clearable />
				</el-form-item>
				<el-form-item>
					<el-date-picker v-model="dataForm.date" type="daterange" range-separator="~"
						start-placeholder="开始日期" end-placeholder="结束日期" size="default" />
				</el-form-item>
				<el-form-item>
					<el-select v-model="dataForm.safety" class="input" placeholder="内容评估" size="default" clearable>
						<el-option label="安全" value="common" />
						<el-option label="有害" value="danger" />
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-select v-model="dataForm.needReview" class="input" placeholder="人工审核" size="default" clearable>
						<el-option label="需要审核" value="true" />
						<el-option label="不需要审核" value="false" />
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-button size="default" type="primary" @click="searchHandle()">查询</el-button>
				</el-form-item>
			</el-form>
			<el-radio-group v-model="dataForm.alarm" @change="changeAlarmType">
				<el-radio-button label="全部" />
				<el-radio-button label="未报警" />
				<el-radio-button label="已报警" />
			</el-radio-group>
		</div>
		<el-table :data="dataList" border v-loading="dataListLoading" :cell-style="{ padding: '4px 0' }" size="default"
			style="width: 100%">
			<el-table-column type="index" header-align="center" align="center" width="100" label="序号">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="orderId" header-align="center" align="center" label="订单编号" min-width="200" />
			<el-table-column prop="status" header-align="center" align="center" label="代驾状态" min-width="140" />
			<el-table-column prop="records" header-align="center" align="center" label="录音数量" min-width="140" />
			<el-table-column prop="safety" header-align="center" align="center" label="安全等级" min-width="140">
				<template #default="scope">
					<el-tag :type="scope.row.safety === '安全' ? 'success' : 'danger'">
						{{ scope.row.safety }}
					</el-tag>
				</template>
			</el-table-column>
			<el-table-column prop="reviews" header-align="center" align="center" label="待审核" min-width="140" />
			<el-table-column prop="createTime" header-align="center" align="center" label="日期" min-width="140" />
			<el-table-column header-align="center" align="center" width="300" label="操作">
				<template #default="scope">
					<el-button size="small" type="primary" :disabled="!isAuth(['ROOT', 'MONITORING:UPDATE'])"
						@click="callDriver(scope.row.id)">
						呼叫司机
					</el-button>
					<el-button size="small" type="primary" :disabled="!isAuth(['ROOT', 'MONITORING:UPDATE'])"
						@click="callCustomer(scope.row.id)">
						呼叫客户
					</el-button>
					<el-button size="small" type="danger"
						:disabled="!isAuth(['ROOT', 'MONITORING:UPDATE']) || scope.row.alarm === '已报警'"
						@click="alarm(scope.row.id)">
						报警
					</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" :current-page="pageIndex"
			:page-sizes="[10, 20, 50]" :page-size="pageSize" :total="totalCount"
			layout="total, sizes, prev, pager, next, jumper" />
	</div>
</template>

<script setup>
	import {
		ref,
		reactive,
		onMounted
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

	defineOptions({
		name: 'Monitoring'
	})

	const dataFormRef = ref(null)

	const dataForm = reactive({
		orderId: null,
		date: [],
		safety: null,
		needReview: null,
		alarm: '全部'
	})

	const dataList = ref([])
	const pageIndex = ref(1)
	const pageSize = ref(10)
	const totalCount = ref(0)
	const dataListLoading = ref(false)

	const dataRule = reactive({
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
			orderId: dataForm.orderId,
			safety: dataForm.safety,
			needReview: dataForm.needReview,
			alarm: dataForm.alarm,
			date: dataForm.date && dataForm.date.length > 0 ? dataForm.date : null
		}
		http('monitoring/searchByPage', 'POST', data, true, (resp) => {
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

	const changeAlarmType = () => {
		pageIndex.value = 1
		loadDataList()
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

	const callDriver = (id) => {
		http('monitoring/callDriver', 'POST', {
			id
		}, true, (resp) => {
			if (resp.rows === 1) {
				ElMessage.success({
					message: '已呼叫司机',
					duration: 1200
				})
			}
		})
	}

	const callCustomer = (id) => {
		http('monitoring/callCustomer', 'POST', {
			id
		}, true, (resp) => {
			if (resp.rows === 1) {
				ElMessage.success({
					message: '已呼叫客户',
					duration: 1200
				})
			}
		})
	}

	const alarm = (id) => {
		ElMessageBox.confirm('确定要报警吗？', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
			.then(() => {
				http('monitoring/alarm', 'POST', {
					id
				}, true, (resp) => {
					if (resp.rows === 1) {
						ElMessage.success({
							message: '报警成功',
							duration: 1200
						})
						loadDataList()
					}
				})
			})
			.catch(() => {})
	}

	onMounted(() => loadDataList())
</script>

<style lang="less" scoped>
	.form {
		display: flex;
		justify-content: space-between;
	}
</style>