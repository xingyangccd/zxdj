<template>
	<el-dialog :title="!dataForm.id ? '新增' : '修改'" v-if="isAuth(['ROOT', 'DRIVER_FINE:INSERT', 'DRIVER_FINE:UPDATE'])"
		:close-on-click-modal="false" v-model="visible" width="420px">
		<el-form :model="dataForm" ref="dataFormRef" :rules="dataRule" label-width="80px">
			<el-form-item label="司机编号" prop="driverId">
				<el-input v-model="dataForm.driverId" :disabled="!!dataForm.id" size="default" style="width: 100%"
					clearable />
			</el-form-item>
			<el-form-item label="订单编号" prop="orderId">
				<el-input v-model="dataForm.orderId" size="default" style="width: 100%" clearable />
			</el-form-item>
			<el-form-item label="罚款金额" prop="amount">
				<el-input v-model="dataForm.amount" size="default" style="width: 100%" clearable />
			</el-form-item>
			<el-form-item label="备注信息" prop="remark">
				<el-input v-model="dataForm.remark" size="default" style="width: 100%" clearable />
			</el-form-item>
			<el-form-item label="付款状态" prop="status">
				<el-radio-group v-model="dataForm.status">
					<el-radio-button label="未缴纳" />
					<el-radio-button label="已缴纳" />
				</el-radio-group>
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
		name: 'DriverFineAddOrUpdate'
	})

	const emit = defineEmits(['refreshDataList'])

	const visible = ref(false)
	const dataFormRef = ref(null)

	const dataForm = reactive({
		id: null,
		driverId: null,
		orderId: null,
		amount: null,
		remark: null,
		status: '未缴纳'
	})

	const dataRule = reactive({
		driverId: [{
			required: true,
			pattern: '^[1-9]\\d{17}$',
			message: '司机编号格式错误'
		}],
		orderId: [{
			required: false,
			pattern: '^[1-9]\\d{17}$',
			message: '订单编号格式错误'
		}],
		amount: [{
			required: true,
			pattern: '^[1-9]\\d*\\.\\d{1,2}$|^0\\.\\d{1,2}$|^[1-9]\\d*$',
			message: '金额格式错误'
		}],
		remark: [{
			required: true,
			message: '备注不能为空'
		}]
	})

	const init = (id) => {
		dataForm.id = id || 0
		visible.value = true
		nextTick(() => {
			dataFormRef.value?.resetFields()
			if (id) {
				http('driver_fine/searchById', 'POST', {
					id
				}, true, (resp) => {
					dataForm.driverId = resp.driverId
					dataForm.orderId = resp.orderId
					dataForm.amount = resp.amount
					dataForm.remark = resp.remark
					dataForm.status = resp.status
				})
			}
		})
	}

	const dataFormSubmit = () => {
		dataFormRef.value.validate((valid) => {
			if (!valid) return
			const url = `driver_fine/${!dataForm.id ? 'insert' : 'update'}`
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