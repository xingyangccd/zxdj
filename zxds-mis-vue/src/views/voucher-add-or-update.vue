<template>
	<el-dialog :title="!dataForm.id ? '新增' : '修改'" v-if="isAuth(['ROOT', 'VOUCHER:INSERT', 'VOUCHER:UPDATE'])"
		:close-on-click-modal="false" v-model="visible" width="500px" top="8vh">
		<el-form :model="dataForm" ref="dataFormRef" :rules="dataRule" label-width="100px">
			<el-form-item label="代金券名称" prop="name">
				<el-input v-model="dataForm.name" size="default" style="width: 100%" clearable />
			</el-form-item>
			<el-form-item label="描述信息" prop="remark">
				<el-input v-model="dataForm.remark" size="default" type="textarea" rows="3" style="width: 100%"
					clearable />
			</el-form-item>
			<el-form-item label="标签" prop="tag">
				<el-input v-model="dataForm.tag" size="default" style="width: 100%" clearable />
			</el-form-item>
			<el-form-item label="总数量" prop="totalQuota">
				<el-input-number v-model="dataForm.totalQuota" :min="0" :step="100" style="width: 55%" />
				&nbsp;&nbsp;&nbsp;/&nbsp;张
			</el-form-item>
			<el-form-item label="代金券面额" prop="discount">
				<el-input-number v-model="dataForm.discount" :min="1" :max="50" style="width: 55%" />
				&nbsp;&nbsp;&nbsp;/&nbsp;元
			</el-form-item>
			<el-form-item label="最低消费额" prop="withAmount">
				<el-input-number v-model="dataForm.withAmount" :min="0" :max="50" :step="10" style="width: 55%" />
				&nbsp;&nbsp;&nbsp;/&nbsp;元
			</el-form-item>
			<el-form-item label="赠送类型" prop="type">
				<el-select v-model="dataForm.type" class="input" placeholder="赠送类型" size="default" clearable>
					<el-option label="用户领取" value="1" />
					<el-option label="注册赠券" value="2" />
					<el-option label="补偿券" value="3" />
				</el-select>
			</el-form-item>
			<el-form-item label="领取限制" prop="limitQuota">
				<el-select v-model="dataForm.limitQuota" class="input" placeholder="领取限制" size="default" clearable>
					<el-option label="无限制" value="0" />
					<el-option label="有限制" value="1" />
				</el-select>
			</el-form-item>
			<el-form-item label="有效期限" prop="timeType">
				<el-select v-model="dataForm.timeType" class="input" placeholder="有效期限" size="default" clearable
					@change="changeHandle" @clear="clearHandle">
					<el-option label="无期限" value="0" />
					<el-option label="有效天数" value="1" />
					<el-option label="有效日期" value="2" />
				</el-select>
			</el-form-item>
			<el-form-item label="有效天数" prop="days" v-if="daysVisible">
				<el-input-number v-model="dataForm.days" :min="1" style="width: 55%" />
			</el-form-item>
			<el-form-item label="有效日期" prop="date" v-if="dateVisible">
				<el-date-picker v-model="dataForm.date" type="daterange" range-separator="~" start-placeholder="开始日期"
					end-placeholder="结束日期" size="default" />
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
	import dayjs from 'dayjs'
	import {
		http
	} from '@/utils/http'
	import {
		isAuth
	} from '@/utils/auth'

	defineOptions({
		name: 'VoucherAddOrUpdate'
	})

	const emit = defineEmits(['refreshDataList'])

	const visible = ref(false)
	const dataFormRef = ref(null)
	const daysVisible = ref(false)
	const dateVisible = ref(false)

	const dataForm = reactive({
		id: null,
		name: null,
		remark: null,
		tag: null,
		totalQuota: 0,
		discount: null,
		withAmount: null,
		type: null,
		limitQuota: null,
		timeType: null,
		days: null,
		date: []
	})

	const validateDate = (rule, value, callback) => {
		if (dataForm.timeType === '2' && (!value || value.length === 0)) {
			callback(new Error('请选择有效日期'))
		} else {
			callback()
		}
	}

	const dataRule = reactive({
		name: [{
			required: true,
			message: '名称不能为空'
		}],
		type: [{
			required: true,
			message: '赠送类型不能为空'
		}],
		limitQuota: [{
			required: true,
			message: '领取限制不能为空'
		}],
		timeType: [{
			required: true,
			message: '有效期限不能为空'
		}],
		date: [{
			validator: validateDate
		}]
	})

	const changeHandle = (val) => {
		daysVisible.value = val === '1'
		dateVisible.value = val === '2'
	}

	const clearHandle = () => {
		daysVisible.value = false
		dateVisible.value = false
	}

	const init = (id) => {
		dataForm.id = id || 0
		visible.value = true
		nextTick(() => {
			dataFormRef.value?.resetFields()
			daysVisible.value = false
			dateVisible.value = false
			if (id) {
				http('voucher/searchById', 'POST', {
					id
				}, true, (resp) => {
					dataForm.name = resp.name
					dataForm.remark = resp.remark
					dataForm.tag = resp.tag
					dataForm.totalQuota = resp.totalQuota
					dataForm.discount = resp.discount
					dataForm.withAmount = resp.withAmount
					dataForm.type = resp.type + ''
					dataForm.limitQuota = resp.limitQuota + ''
					dataForm.timeType = resp.timeType + ''
					if (resp.timeType === 1) {
						daysVisible.value = true
						dataForm.days = resp.days
					} else if (resp.timeType === 2) {
						dateVisible.value = true
						dataForm.date = [resp.startTime, resp.endTime]
					}
				})
			}
		})
	}

	const dataFormSubmit = () => {
		dataFormRef.value.validate((valid) => {
			if (!valid) return
			const data = {
				...dataForm
			}
			if (data.timeType === '2' && data.date && data.date.length === 2) {
				data.startTime = dayjs(data.date[0]).format('YYYY-MM-DD')
				data.endTime = dayjs(data.date[1]).format('YYYY-MM-DD')
			}
			delete data.date
			const url = `voucher/${!dataForm.id ? 'insert' : 'update'}`
			http(url, 'POST', data, true, (resp) => {
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