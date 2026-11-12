<template>
	<div v-if="isAuth(['ROOT', 'VOUCHER:SELECT'])">
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataFormRef">
			<el-form-item prop="name">
				<el-input v-model="dataForm.name" placeholder="代金券名称" size="default" clearable />
			</el-form-item>
			<el-form-item prop="tag">
				<el-input v-model="dataForm.tag" placeholder="代金券标签" size="default" clearable />
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.totalQuota" class="input" placeholder="代金券数量" size="default" clearable>
					<el-option label="无限量" value="无限量" />
					<el-option label="有限量" value="有限量" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.type" class="input" placeholder="赠送类型" size="default" clearable>
					<el-option label="用户领取" value="1" />
					<el-option label="注册赠券" value="2" />
					<el-option label="补偿券" value="3" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.limitQuota" class="input" placeholder="用户领券限制" size="default" clearable>
					<el-option label="无限制" value="无限制" />
					<el-option label="有限制" value="有限制" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.status" class="input" placeholder="代金券状态" size="default" clearable>
					<el-option label="正常" value="1" />
					<el-option label="过期" value="2" />
					<el-option label="下架" value="3" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.timeType" class="input" placeholder="有效期限" size="default" clearable>
					<el-option label="无期限" value="无期限" />
					<el-option label="有效天数" value="有效天数" />
					<el-option label="有效日期" value="有效日期" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button size="default" type="primary" @click="searchHandle()">查询</el-button>
				<el-button size="default" type="primary" @click="addHandle()">添加</el-button>
				<el-button size="default" type="danger" @click="deleteHandle()">批量删除</el-button>
			</el-form-item>
		</el-form>
		<el-table :data="dataList" border v-loading="dataListLoading" :cell-style="{ padding: '4px 0' }"
			style="width: 100%" size="default" @selection-change="selectionChangeHandle" @expand-change="expand"
			:row-key="getRowKeys" :expand-row-keys="expands">
			<el-table-column prop="content" header-align="center" align="center" type="expand">
				<template #default="scope">
					<div class="voucher-table">
						<div class="row">
							<div class="th">总数量</div>
							<div class="th">已领取</div>
							<div class="th">已使用</div>
							<div class="th">最低消费</div>
							<div class="th">领取限制</div>
							<div class="th date">有效期限</div>
						</div>
						<div class="row">
							<div class="td">{{ scope.row.totalQuota !== 0 ? scope.row.totalQuota : '无限' }}</div>
							<div class="td">{{ scope.row.takeCount }}</div>
							<div class="td">{{ scope.row.usedCount }}</div>
							<div class="td">{{ scope.row.withAmount }}元</div>
							<div class="td">{{ scope.row.limitQuota }}</div>
							<div class="td" v-if="scope.row.timeType === 0 || scope.row.timeType == null">无</div>
							<div class="td" v-if="scope.row.timeType === 1">{{ scope.row.days }}天以内</div>
							<div class="td" v-if="scope.row.timeType === 2">
								{{ scope.row.startTime }} ~ {{ scope.row.endTime }}
							</div>
						</div>
					</div>
					<div class="voucher-text-table">
						<div class="row">
							<div class="th">【描述文字】</div>
							<div class="td">{{ scope.row.remark }}</div>
						</div>
						<div class="row">
							<div class="th">【标签内容】</div>
							<div class="td">{{ scope.row.tag }}</div>
						</div>
					</div>
				</template>
			</el-table-column>
			<el-table-column type="selection" :selectable="selectable" header-align="center" align="center"
				width="50" />
			<el-table-column type="index" header-align="center" align="center" width="100" label="序号">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="uuid" header-align="center" align="center" min-width="240" label="编号" />
			<el-table-column prop="name" header-align="center" align="center" min-width="200" label="代金券名称" />
			<el-table-column prop="discount" header-align="center" align="center" min-width="80" label="面额" />
			<el-table-column prop="totalQuota" header-align="center" align="center" min-width="120" label="总数量" />
			<el-table-column prop="takeCount" header-align="center" align="center" min-width="120" label="已领取" />
			<el-table-column prop="type" header-align="center" align="center" min-width="100" label="类型" />
			<el-table-column prop="status" header-align="center" align="center" min-width="100" label="状态" />
			<el-table-column header-align="center" align="center" width="180" label="操作">
				<template #default="scope">
					<el-button type="success" link size="default" @click="updateStatus(scope.row.id, 1)"
						:disabled="scope.row.status === '正常'">
						上线
					</el-button>
					<el-button type="warning" link size="default" @click="updateStatus(scope.row.id, 3)"
						:disabled="scope.row.status === '下架'">
						下架
					</el-button>
					<el-button type="danger" link size="default"
						:disabled="!isAuth(['ROOT', 'VOUCHER:DELETE']) || scope.row.takeCount > 0"
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
		<el-dialog v-model="updateVisible" title="提示信息" width="300px" align-center>
			<div class="notice">你确定{{ operate }}这个代金券？</div>
			<template #footer>
				<span class="dialog-footer">
					<el-button size="default" @click="updateVisible = false">取消</el-button>
					<el-button type="primary" size="default" @click="confirmHandle()">确定</el-button>
				</span>
			</template>
		</el-dialog>
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
	import AddOrUpdate from './voucher-add-or-update.vue'

	defineOptions({
		name: 'Voucher'
	})

	const dataFormRef = ref(null)
	const addOrUpdateRef = ref(null)

	const dataForm = reactive({
		name: null,
		tag: null,
		totalQuota: null,
		type: null,
		limitQuota: null,
		status: null,
		timeType: null
	})

	const dataList = ref([])
	const pageIndex = ref(1)
	const pageSize = ref(10)
	const totalCount = ref(0)
	const dataListLoading = ref(false)
	const dataListSelections = ref([])
	const expands = ref([])

	const updateVisible = ref(false)
	const operate = ref(null)
	const pendingId = ref(null)
	const pendingStatus = ref(null)

	const dataRule = reactive({})

	const getRowKeys = (row) => row.id

	const loadDataList = () => {
		dataListLoading.value = true
		const data = {
			page: pageIndex.value,
			length: pageSize.value,
			name: dataForm.name,
			tag: dataForm.tag,
			totalQuota: dataForm.totalQuota,
			type: dataForm.type,
			limitQuota: dataForm.limitQuota,
			status: dataForm.status,
			timeType: dataForm.timeType
		}
		http('voucher/searchVoucherByPage', 'POST', data, true, (resp) => {
			const page = resp.page
			dataList.value = page.list
			totalCount.value = Number(page.totalCount)
			dataListLoading.value = false
		})
	}

	const searchHandle = () => {
		if (pageIndex.value !== 1) pageIndex.value = 1
		loadDataList()
	}

	const selectable = (row) => row.takeCount <= 0

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

	const expand = (row, expandedRows) => {
		expands.value = expandedRows.length > 0 ? [row.id] : []
	}

	const addHandle = () => nextTick(() => addOrUpdateRef.value?.init())

	const updateStatus = (id, status) => {
		pendingId.value = id
		pendingStatus.value = status
		operate.value = status === 1 ? '上线' : '下架'
		updateVisible.value = true
	}

	const confirmHandle = () => {
		http(
			'voucher/updateStatus',
			'POST', {
				id: pendingId.value,
				status: pendingStatus.value
			},
			true,
			(resp) => {
				if (resp.rows === 1) {
					ElMessage.success({
						message: '操作成功',
						duration: 1200
					})
					updateVisible.value = false
					loadDataList()
				} else {
					ElMessage.error({
						message: '操作失败',
						duration: 1200
					})
				}
			}
		)
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
				http('voucher/deleteVoucherByIds', 'POST', {
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

<style lang="less" scoped>
	@import url('voucher.less');
</style>