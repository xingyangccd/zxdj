<template>
	<div v-if="isAuth(['ROOT', 'DRIVER:SELECT'])">
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
			<el-form-item prop="tel">
				<el-input v-model="dataForm.tel" placeholder="电话" size="default" class="input" clearable />
			</el-form-item>
			<el-form-item prop="pid">
				<el-input v-model="dataForm.pid" placeholder="身份证" size="default" clearable />
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.realAuth" class="input" placeholder="实名认证" size="default" clearable>
					<el-option label="未认证" :value="1" />
					<el-option label="已认证" :value="2" />
					<el-option label="审核中" :value="3" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.status" class="input" placeholder="状态" size="default" clearable>
					<el-option label="正常" :value="1" />
					<el-option label="禁用" :value="2" />
					<el-option label="减少接单" :value="3" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button size="default" type="primary" @click="searchHandle()">查询</el-button>
			</el-form-item>
		</el-form>
		<el-table :data="dataList" border v-loading="dataListLoading" :cell-style="{ padding: '4px 0' }"
			style="width: 100%" size="default" @expand-change="expand" :row-key="getRowKeys" :expand-row-keys="expands">
			<el-table-column prop="content" header-align="center" align="center" type="expand">
				<template #default="scope">
					<div class="content-container">
						<div class="left">
							<table class="content-table">
								<tbody>
									<tr>
										<th>开车驾龄</th>
										<th>生日</th>
										<th>邮箱</th>
										<th>收信地址</th>
										<th>身份证住址</th>
									</tr>
									<tr>
										<td>{{ content.year }}年</td>
										<td>{{ content.birthday }}</td>
										<td>{{ content.email }}</td>
										<td>{{ content.mailAddress }}</td>
										<td>{{ content.idcardAddress }}</td>
									</tr>
								</tbody>
							</table>
							<ul class="credentials-list">
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.idcardFront"
										:preview-src-list="content.idcardFrontList" fit="cover" />
									<p>【 身份证正面 】</p>
								</li>
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.idcardBack"
										:preview-src-list="content.idcardBackList" fit="cover" />
									<p>【 身份证背面 】</p>
								</li>
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.idcardHolding"
										:preview-src-list="content.idcardHoldingList" fit="cover" />
									<p>【 手持身份证 】</p>
								</li>
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.drcardFront"
										:preview-src-list="content.drcardFrontList" fit="cover" />
									<p>【 驾驶证正面 】</p>
								</li>
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.drcardBack"
										:preview-src-list="content.drcardBackList" fit="cover" />
									<p>【 驾驶证背面 】</p>
								</li>
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.drcardHolding"
										:preview-src-list="content.drcardHoldingList" fit="cover" />
									<p>【 手持驾驶证 】</p>
								</li>
							</ul>
						</div>
						<div class="right" v-if="scope.row.realAuth === '已认证'">
							<div class="summary-panel">
								<h3 class="title">【 代驾数据摘要 】</h3>
								<ul class="monthly-list">
									<li class="item">
										<p class="section">月订单金额</p>
										<p class="value">
											<span>￥</span>
											{{ content.total }}
										</p>
									</li>
									<li class="item">
										<p class="section">月订单分账</p>
										<p class="value">
											<span>￥</span>
											{{ content.income }}
										</p>
									</li>
									<li class="item">
										<p class="section">代扣个税</p>
										<p class="value">
											<span>￥</span>
											{{ content.tax }}
										</p>
									</li>
								</ul>
								<ul class="statistics-list">
									<li class="item">
										<p class="section">总代驾单数</p>
										<p class="value">{{ content.count }}</p>
									</li>
									<li class="item">
										<p class="section">好评总数</p>
										<p class="value">{{ content.ct1 }}</p>
									</li>
									<li class="item">
										<p class="section">差评总数</p>
										<p class="value">{{ content.ct2 }}</p>
									</li>
									<li class="item">
										<p class="section">中评总数</p>
										<p class="value">{{ content.ct3 }}</p>
									</li>
								</ul>
							</div>
							<div :id="'chart-' + scope.row.id" class="driver-chart"></div>
						</div>
					</div>
				</template>
			</el-table-column>
			<el-table-column type="index" header-align="center" align="center" width="100" label="序号">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="name" header-align="center" align="center" min-width="100" label="姓名" />
			<el-table-column prop="sex" header-align="center" align="center" min-width="60" label="性别" />
			<el-table-column prop="tel" header-align="center" align="center" min-width="130" label="电话" />
			<el-table-column prop="pid" header-align="center" align="center" min-width="190" label="身份证" />
			<el-table-column prop="contactName" header-align="center" align="center" min-width="130" label="紧急联络人" />
			<el-table-column prop="contactTel" header-align="center" align="center" min-width="130" label="联络人电话" />
			<el-table-column prop="realAuth" header-align="center" align="center" min-width="130" label="实名认证" />
			<el-table-column prop="status" header-align="center" align="center" min-width="100" label="状态" />
			<el-table-column header-align="center" align="center" width="250" label="操作">
				<template #default="scope">
					<el-button type="primary" link size="default" v-if="isAuth(['ROOT', 'DRIVER:UPDATE'])"
						@click="showApproveModel(scope.row.id)" :disabled="scope.row.realAuth !== '审核中'">
						审批认证
					</el-button>
					<el-button type="warning" link size="default" v-if="isAuth(['ROOT', 'DRIVER:UPDATE'])"
						@click="showRepealModel(scope.row.id)" :disabled="scope.row.realAuth !== '已认证'">
						撤销认证
					</el-button>
					<el-button type="primary" link size="default" v-if="isAuth(['ROOT', 'DRIVER:UPDATE'])"
						@click="showUpdatePanel(scope.row.id)">
						设置状态
					</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" :current-page="pageIndex"
			:page-sizes="[10, 20, 50]" :page-size="pageSize" :total="totalCount"
			layout="total, sizes, prev, pager, next, jumper" />
		<el-dialog v-model="approveModelVisible" title="提示信息" width="400px" align-center>
			<div class="notice">你确定已经核准该代驾司机的实名认证？</div>
			<template #footer>
				<span class="dialog-footer">
					<el-button @click="approveHandle(false)">拒绝</el-button>
					<el-button type="primary" @click="approveHandle(true)">批准</el-button>
				</span>
			</template>
		</el-dialog>
		<el-dialog v-model="repealModelVisible" title="提示信息" width="400px" align-center>
			<div class="notice">你确定取消该代驾司机的实名认证？</div>
			<template #footer>
				<span class="dialog-footer">
					<el-button @click="repealModelVisible = false">取消</el-button>
					<el-button type="primary" @click="repealHandle">确定</el-button>
				</span>
			</template>
		</el-dialog>
		<update ref="updateRef" @refreshDataList="loadDataList" />
	</div>
</template>

<script setup>
	import {
		ref,
		reactive,
		onMounted,
		nextTick,
		getCurrentInstance
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
	import Update from './driver-update.vue'

	defineOptions({
		name: 'Driver'
	})

	// ⭐ ECharts 太大，继续用全局的（不 import）
	const {
		proxy
	} = getCurrentInstance()

	const dataFormRef = ref(null)
	const updateRef = ref(null)

	const dataForm = reactive({
		name: null,
		sex: null,
		tel: null,
		pid: null,
		realAuth: null,
		status: null
	})

	const dataList = ref([])
	const pageIndex = ref(1)
	const pageSize = ref(10)
	const totalCount = ref(0)
	const dataListLoading = ref(false)
	const expands = ref([])

	const content = reactive({
		year: null,
		birthday: null,
		email: null,
		mailAddress: null,
		idcardAddress: null,
		idcardFront: null,
		idcardFrontList: [],
		idcardBack: null,
		idcardBackList: [],
		idcardHolding: null,
		idcardHoldingList: [],
		drcardFront: null,
		drcardFrontList: [],
		drcardBack: null,
		drcardBackList: [],
		drcardHolding: null,
		drcardHoldingList: [],
		total: null,
		income: null,
		tax: null,
		count: null,
		ct1: null,
		ct2: null,
		ct3: null
	})

	const approveModelVisible = ref(false)
	const repealModelVisible = ref(false)
	const driverId = ref(null)

	const dataRule = reactive({
		name: [{
			required: false,
			pattern: '^[\u4e00-\u9fa5]{1,10}$',
			message: '姓名格式错误'
		}],
		tel: [{
			required: false,
			pattern: '^1\\d{10}$',
			message: '电话格式错误'
		}],
		pid: [{
			required: false,
			pattern: '^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$',
			message: '身份证格式错误'
		}]
	})

	const getRowKeys = (row) => row.id

	// 导入
	import axios from 'axios'
	import {
		baseUrl
	} from '../utils/http'

	// 封装一个分页查询的函数，在这个页面中反复调用。
	const loadDataList = async () => {
		dataListLoading.value = true;

		try {
			// 准备参数
			const params = {
				pageIndex: pageIndex.value,
				pageSize: pageSize.value,
				name: dataForm.name || null,
				sex: dataForm.sex || null,
				tel: dataForm.tel || null,
				pid: dataForm.pid || null,
				realAuth: dataForm.realAuth || null,
				status: dataForm.status || null
			}

			// 发送ajax请求
			const response = await axios.get(`${baseUrl}driver/drivers`, {
				params,
				headers: {
					token: localStorage.getItem("token")
				}
			});

			// 获取响应结果
			const result = response.data.result;
			const list = result.list;

			const statusMap = {
				"1": "正常",
				"2": "禁用",
				"3": "降低接单"
			};

			const realAuthMap = {
				"1": "未认证",
				"2": "已认证",
				"3": "审核中"
			};

			for (const one of list) {
				one.status = statusMap[String(one.status)];
				one.realAuth = realAuthMap[String(one.realAuth)];
			}

			dataList.value = list;
			totalCount.value = Number(result.totalCount);

		} catch (error) {
			console.error("查询司机记录失败", error);
			ElMessage.error("查询失败，请稍后再试")
		} finally {
			dataListLoading.value = false;
		}
	}

	// 查询按钮
	const searchHandle = async () => {
		try {
			await dataFormRef.value.validate();
		} catch {
			return;
		}
		dataFormRef.value.clearValidate();
		pageIndex.value = 1;
		loadDataList();
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


	// expands.value: 是你告诉表格“我要展开哪些行”
	// expandedRows 是表格告诉你“现在开着的是哪些行”
	const expand = async (row, expandedRows) => {
		// 所有行收起
		expands.value = [];

		// 虽然所有行收起来了，但是 expandedRows 保存的仍然是 expand 方法刚开始执行时的值。
		// expandedRows的长度如果是0，则代表用户点击的折叠操作.
		if (expandedRows.length === 0) return;
		if (!row) return;
		if (row.realAuth === "未认证") return;

		// 走到这里说明，用户是一个展开操作，并且司机已经提供了实名认证信息。
		expands.value.push(row.id);

		// 准备数据
		const data = {
			realAuth: row.realAuth === "已认证" ? 2 : 3,
			driverId: row.id
		};

		// 发送ajax请求
		const resp = await axios.post(`${baseUrl}driver/comprehensive-data`, data, {
			headers: {
				token: localStorage.getItem("token")
			}
		});

		const summaryMap = resp.data.result.summaryMap;

		content.year = summaryMap.year
		content.birthday = summaryMap.birthday
		content.email = summaryMap.email
		content.mailAddress = summaryMap.mailAddress
		content.idcardAddress = summaryMap.idcardAddress

		const imageKeys = [
			'idcardFront',
			'idcardBack',
			'idcardHolding',
			'drcardFront',
			'drcardBack',
			'drcardHolding'
		]
		for (const key of imageKeys) {
			const url = summaryMap[key]
			// 缩略图
			content[key] = url
			// 预览图列表（要求是一个数组）
			content[key + 'List'] = [url]
		}

	}

	const initChart = (id, chartData) => {
		const chartDom = document.getElementById('chart-' + id)
		if (!chartDom) return
		const chart = proxy.$echarts.init(chartDom)
		chart.setOption({
			tooltip: {
				trigger: 'axis'
			},
			legend: {
				data: ['订单数', '收入']
			},
			xAxis: {
				type: 'category',
				data: chartData.dates
			},
			yAxis: [{
					type: 'value',
					name: '订单数'
				},
				{
					type: 'value',
					name: '收入'
				}
			],
			series: [{
					name: '订单数',
					type: 'bar',
					data: chartData.counts
				},
				{
					name: '收入',
					type: 'line',
					yAxisIndex: 1,
					data: chartData.incomes
				}
			]
		})
	}

	const showApproveModel = (id) => {
		// 将id赋值给响应式对象
		driverId.value = id
		// 显示弹窗
		approveModelVisible.value = true
	}

	const showRepealModel = (id) => {
		// 将id赋值给响应式对象
		driverId.value = id
		// 显示弹窗
		repealModelVisible.value = true
	}

	const showUpdatePanel = (id) => {
		nextTick(() => updateRef.value?.init(id))
	}

	const approveHandle = async (approveId) => {
		// 准备数据
		const data = {
			realAuth: approveId ? 2 : 1,
			driverId: driverId.value
		};
		try {
			// 发送ajax请求
			const resp = await axios.put(`${baseUrl}driver/real-auth`, data, {
				headers: {
					token: localStorage.getItem("token")
				}
			})
			// 处理页面渲染
			if (resp.data.rows === 1) {
				approveModelVisible.value = false;
				ElMessage.success("数据更新成功");
				// 折叠所有面板
				expands.value = [];
				// 重新加载分页数据
				loadDataList();
			}
		} catch (err) {
			console.error("发生错误：", err);
			ElMessage.error("网络异常，稍后再试");
		}
	}

	const repealHandle = async () => {
		// 准备数据
		const data = {
			realAuth: 3,
			driverId: driverId.value
		};
		try {
			// 发送ajax请求
			const resp = await axios.put(`${baseUrl}driver/real-auth`, data, {
				headers: {
					token: localStorage.getItem("token")
				}
			})
			// 处理页面渲染
			if (resp.data.rows === 1) {
				repealModelVisible.value = false;
				ElMessage.success("数据更新成功");
				// 折叠所有面板
				expands.value = [];
				// 重新加载分页数据
				loadDataList();
			}
		} catch (err) {
			console.error("发生错误：", err);
			ElMessage.error("网络异常，稍后再试");
		}
	}

	// 页面挂载完毕之后调用方法加载第一页数据。
	onMounted(() => loadDataList())
</script>

<style lang="less" scoped>
	@import url('driver.less');

	.driver-chart {
		width: 600px;
		height: 374px;
	}
</style>