<template>
	<el-dialog :title="!dataForm.id ? '新增' : '修改'"
		v-if="isAuth(['ROOT', 'DRIVER_LOCKDOWN:INSERT', 'DRIVER_LOCKDOWN:UPDATE'])" :close-on-click-modal="false"
		v-model="visible" width="420px">
		<el-form :model="dataForm" ref="dataFormRef" :rules="dataRule" label-width="80px">
			<el-form-item label="司机编号" prop="driverId">
				<el-input v-model="dataForm.driverId" :disabled="!!dataForm.id" size="default" style="width: 100%"
					clearable />
			</el-form-item>
			<el-form-item label="订单编号" prop="orderId">
				<el-input v-model="dataForm.orderId" size="default" style="width: 100%" clearable />
			</el-form-item>
			<el-form-item label="具体原因" prop="reason">
				<el-input v-model="dataForm.reason" :rows="2" type="textarea" size="default" style="width: 100%"
					clearable />
			</el-form-item>
			<el-form-item label="起始日期" prop="startDate">
				<el-date-picker v-model="dataForm.startDate" type="date" placeholder="起始日期" />
			</el-form-item>
			<el-form-item label="截止日期" prop="endDate">
				<el-date-picker v-model="dataForm.endDate" type="date" placeholder="截止日期" />
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
		name: 'DriverLockdownAddOrUpdate'
	})

	const emit = defineEmits(['refreshDataList'])

	const visible = ref(false)
	const dataFormRef = ref(null)

	const dataForm = reactive({
		id: null,
		driverId: null,
		orderId: null,
		startDate: null,
		endDate: null,
		reason: null
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
		startDate: [{
			required: true,
			message: '起始日期必填'
		}],
		endDate: [{
			required: true,
			message: '截止日期必填'
		}],
		reason: [{
			required: true,
			message: '原因不能为空'
		}]
	})

	const init = (id) => {
		dataForm.id = id || 0
		visible.value = true
		nextTick(() => {
			dataFormRef.value?.resetFields()
			if (id) {
				http('driver_lockdown/searchById', 'POST', {
					id
				}, true, (resp) => {
					dataForm.driverId = resp.driverId
					dataForm.orderId = resp.orderId
					dataForm.startDate = resp.startDate
					dataForm.endDate = resp.endDate
					dataForm.reason = resp.reason
				})
			}
		})
	}

	const dataFormSubmit = () => {
		dataFormRef.value.validate((valid) => {
			if (!valid) return
			const url = `driver_lockdown/${!dataForm.id ? 'insert' : 'update'}`
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