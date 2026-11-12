<template>
	<el-dialog title="设置状态" v-if="isAuth(['ROOT', 'DRIVER:UPDATE'])" :close-on-click-modal="false" v-model="visible"
		width="420px">
		<el-form :model="dataForm" ref="dataFormRef" label-width="60px">
			<el-form-item label="状态">
				<el-radio-group v-model="dataForm.status" size="default">
					<el-radio-button label="正常" />
					<el-radio-button label="禁用" />
					<el-radio-button label="减少接单" />
				</el-radio-group>
			</el-form-item>
		</el-form>
		<p class="desc">
			如果设置为禁用状态，该司机将立即下线，对于正在执行中的订单将无法完结和付款，请谨慎设置。
		</p>
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
		name: 'DriverUpdate'
	})

	const emit = defineEmits(['refreshDataList'])

	const visible = ref(false)
	const dataFormRef = ref(null)

	const dataForm = reactive({
		driverId: null,
		status: null
	})

	const init = (id) => {
		dataForm.driverId = id
		visible.value = true
		nextTick(() => {
			http('driver/searchDriverStatus', 'POST', {
				driverId: id
			}, true, (resp) => {
				dataForm.status = resp.status
			})
		})
	}

	const dataFormSubmit = () => {
		http('driver/updateStatus', 'POST', dataForm, true, (resp) => {
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
	}
</script>

<style lang="less" scoped>
	.desc {
		color: #999;
		padding: 0 10px;
		line-height: 1.8;
	}
</style>