<template>
	<el-dialog title="关闭订单" v-if="isAuth(['ROOT', 'ORDER:UPDATE'])" :close-on-click-modal="false" v-model="visible"
		width="420px">
		<el-form :model="dataForm" ref="dataFormRef" :rules="dataRule" label-width="80px">
			<el-form-item label="订单状态" prop="status">
				<el-select v-model="dataForm.status" class="input" placeholder="状态" size="default" clearable>
					<el-option label="事故关闭" value="11" />
					<el-option label="其他" value="12" />
				</el-select>
			</el-form-item>
			<el-form-item label="关闭原因" prop="remark">
				<el-input v-model="dataForm.remark" rows="4" type="textarea" size="default" style="width: 100%"
					clearable />
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
		name: 'OrderClose'
	})

	const emit = defineEmits(['refreshDataList'])

	const visible = ref(false)
	const dataFormRef = ref(null)

	const dataForm = reactive({
		orderId: null,
		status: null,
		remark: null
	})

	const dataRule = reactive({
		status: [{
			required: true,
			message: '必须选择订单状态'
		}],
		remark: [{
			required: true,
			message: '必须填写备注信息'
		}]
	})

	const init = (id) => {
		dataForm.orderId = id
		visible.value = true
		nextTick(() => {
			dataFormRef.value?.resetFields()
		})
	}

	const dataFormSubmit = () => {
		dataFormRef.value.validate((valid) => {
			if (!valid) return
			http('order/closeOrder', 'POST', dataForm, true, (resp) => {
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