<template>
	<div v-if="isAuth(['ROOT', 'ORDER:SELECT'])">
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataFormRef">
			<el-form-item prop="orderId">
				<el-input v-model="dataForm.orderId" placeholder="订单编号" size="default" clearable />
			</el-form-item>
			<el-form-item prop="driverId">
				<el-input v-model="dataForm.driverId" placeholder="司机编号" size="default" clearable />
			</el-form-item>
			<el-form-item prop="customerId">
				<el-input v-model="dataForm.customerId" placeholder="客户编号" size="default" clearable />
			</el-form-item>
			<el-form-item>
				<el-date-picker v-model="dataForm.date" type="daterange" range-separator="~" start-placeholder="开始日期"
					end-placeholder="结束日期" size="default" />
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.status" class="input" placeholder="状态" size="default" clearable>
					<el-option label="等待接单" :value="1" />
					<el-option label="已接单" :value="2" />
					<el-option label="司机已到达" :value="3" />
					<el-option label="开始代驾" :value="4" />
					<el-option label="结束代驾" :value="5" />
					<el-option label="未付款" :value="6" />
					<el-option label="已付款" :value="7" />
					<el-option label="订单已结束" :value="8" />
					<el-option label="顾客撤单" :value="9" />
					<el-option label="司机撤单" :value="10" />
					<el-option label="事故关闭" :value="11" />
					<el-option label="其他" :value="12" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button size="default" type="primary" @click="searchHandle()">查询</el-button>
			</el-form-item>
		</el-form>
		<el-table :data="dataList" border v-loading="dataListLoading" :cell-style="{ padding: '4px 0' }"
			style="width: 100%" size="default" @expand-change="expand" :row-key="getRowKeys" :expand-row-keys="expands">
			<el-table-column prop="panel" header-align="center" align="center" type="expand">
				<template #default="scope">
					<div class="content-container">
						<div class="left">
							<div class="order-table">
								<div class="row">
									<div class="th">客户编号</div>
									<div class="td">{{ panel.customer.id }}</div>
									<div class="th">客户性别</div>
									<div class="td">{{ panel.customer.sex }}</div>
									<div class="th">客户电话</div>
									<div class="td">{{ panel.customer.tel }}</div>
								</div>
								<div class="row">
									<div class="th">车型</div>
									<div class="td">{{ panel.order.carType }}</div>
									<div class="th">车牌</div>
									<div class="td">{{ panel.order.carPlate }}</div>
									<div class="th">车辆隶属</div>
									<div class="td">{{ panel.order.city }}</div>
								</div>
								<div class="row">
									<div class="th">司机编号</div>
									<div class="td">{{ panel.driver.id }}</div>
									<div class="th">司机姓名</div>
									<div class="td">{{ panel.driver.name }}</div>
									<div class="th">司机电话</div>
									<div class="td">{{ panel.driver.tel }}</div>
								</div>
								<div class="row">
									<div class="th">接单时间</div>
									<div class="td">{{ panel.order.acceptTime }}</div>
									<div class="th">到达时间</div>
									<div class="td">{{ panel.order.arriveTime }}</div>
									<div class="th">代驾等时</div>
									<div class="td">{{ panel.order.waitingMinute }}</div>
								</div>
								<div class="row">
									<div class="th">开始时间</div>
									<div class="td">{{ panel.order.startTime }}</div>
									<div class="th">结束时间</div>
									<div class="td">{{ panel.order.endTime }}</div>
									<div class="th">代驾时长</div>
									<div class="td">{{ panel.order.driveMinute }}</div>
								</div>
								<div class="row">
									<div class="th">代驾里程</div>
									<div class="td">{{ panel.order.realMileage }}</div>
									<div class="th">订单费用</div>
									<div class="td">{{ panel.order.realFee }}</div>
									<div class="th">状态</div>
									<div class="td">{{ panel.order.status }}</div>
								</div>
								<div class="row">
									<div class="th">费用规则</div>
									<div class="td">{{ panel.order.chargeRule }}</div>
									<div class="th">取消规则</div>
									<div class="td">{{ panel.order.cancelRule }}</div>
									<div class="th">分账规则</div>
									<div class="td">{{ panel.order.profitsharingRule }}</div>
								</div>
							</div>
						</div>
						<div class="right">
							<div :id="'map-' + scope.row.id" :key="'map-' + scope.row.id" class="map"></div>
						</div>
					</div>
				</template>
			</el-table-column>
			<el-table-column type="index" header-align="center" align="center" width="100" label="序号">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="id" header-align="center" align="center" min-width="200" label="订单编号" />
			<el-table-column prop="startPlace" header-align="center" align="center" min-width="220"
				:show-overflow-tooltip="true" label="代驾起点" />
			<el-table-column prop="endPlace" header-align="center" align="center" min-width="220"
				:show-overflow-tooltip="true" label="代驾终点" />
			<el-table-column prop="realMileage" header-align="center" align="center" min-width="130" label="实际里程" />
			<el-table-column prop="realFee" header-align="center" align="center" min-width="130" label="实际金额" />
			<el-table-column prop="createTime" header-align="center" align="center" min-width="150" label="日期时间" />
			<el-table-column prop="status" header-align="center" align="center" min-width="100" label="状态" />
			<el-table-column header-align="center" align="center" width="200" label="操作">
				<template #default="scope">
					<el-button type="primary" link size="default" v-if="isAuth(['ROOT', 'ORDER:SELECT'])"
						:disabled="!['结束代驾', '未付款', '已付款', '订单已结束'].includes(scope.row.status)"
						@click="showOrderBill(scope.row.id)">
						账单详情
					</el-button>
					<el-button type="warning" link size="default" v-if="isAuth(['ROOT', 'ORDER:UPDATE'])"
						:disabled="!['等待接单', '已接单', '司机已到达', '开始代驾'].includes(scope.row.status)"
						@click="showUpdatePanel(scope.row.id)">
						关闭订单
					</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" :current-page="pageIndex"
			:page-sizes="[10, 20, 50]" :page-size="pageSize" :total="totalCount"
			layout="total, sizes, prev, pager, next, jumper" />
		<order-bill ref="orderBillRef" />
		<order-close ref="orderCloseRef" @refreshDataList="loadDataList" />
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
		isAuth
	} from '@/utils/auth'
	import {
		calculateCarPlateCity
	} from '../utils/car_plate.js'
	import OrderBill from './order_bill.vue'
	import OrderClose from './order-close.vue'

	import axios from 'axios'
	import dayjs from 'dayjs'
	import {
		baseUrl
	} from '../utils/http.js'
	import {
		ElMessage
	} from 'element-plus'

	// 使用相对路径导入图片。
	import driverIcon from '../assets/driver-icon.png'

	defineOptions({
		name: 'Order'
	})

	let gpsTimer = null

	const statusMap = {
		1: '等待接单',
		2: '已接单',
		3: '司机已到达',
		4: '开始代驾',
		5: '结束代驾',
		6: '未付款',
		7: '已付款',
		8: '订单已结束',
		9: '顾客撤单',
		10: '司机撤单',
		11: '事故关闭',
		12: '其他'
	}

	const dataFormRef = ref(null)
	const orderBillRef = ref(null)
	const orderCloseRef = ref(null)

	const dataForm = reactive({
		orderId: null,
		driverId: null,
		customerId: null,
		date: [],
		status: null
	})

	const dataList = ref([])
	const pageIndex = ref(1)
	const pageSize = ref(10)
	const totalCount = ref(0)
	const dataListLoading = ref(false)
	const expands = ref([])

	const panel = reactive({
		id: null,
		customer: {
			id: null,
			sex: null,
			tel: null
		},
		driver: {
			id: null,
			name: null,
			tel: null
		},
		order: {
			carPlate: null,
			carType: null,
			city: null,
			acceptTime: null,
			arriveTime: null,
			startTime: null,
			endTime: null,
			waitingMinute: null,
			driveMinute: null,
			realMileage: null,
			realFee: null,
			status: null,
			chargeRule: null,
			cancelRule: null,
			profitsharingRule: null
		}
	})

	const dataRule = reactive({
		orderId: [{
			required: false,
			pattern: '^[1-9]\\d{18}$',
			message: '订单编号格式错误'
		}],
		driverId: [{
			required: false,
			pattern: '^[1-9]\\d{18}$',
			message: '司机编号格式错误'
		}],
		customerId: [{
			required: false,
			pattern: '^[1-9]\\d{18}$',
			message: '客户编号格式错误'
		}]
	})

	const getRowKeys = (row) => row.id

	// 封装一个分页查询的函数。
	const loadDataList = async () => {
		dataListLoading.value = true

		try {
			// 组装请求需要的数据
			const data = {
				pageIndex: pageIndex.value,
				pageSize: pageSize.value,
				orderId: dataForm.orderId || null,
				driverId: dataForm.driverId || null,
				customerId: dataForm.customerId || null,
				status: dataForm.status || null
			};
			// 处理日期
			if (dataForm.date && dataForm.date.length === 2) {
				// 给data对象扩展属性
				data.startDate = dayjs(dataForm.date[0]).format('YYYY-MM-DD')
				data.endDate = dayjs(dataForm.date[1]).format('YYYY-MM-DD')
			}
			// 发送ajax请求
			const response = await axios.post(`${baseUrl}order/page`, data, {
				headers: {
					token: localStorage.getItem("token")
				}
			})

			// 解析返回的数据
			const result = response.data.result;
			const list = result.list || []

			// 遍历数据并处理
			for (const one of list) {
				// 处理状态，将数字转换为文字描述
				one.status = statusMap[one.status]
				// 处理默认值：实际里程 实际金额
				one.realMileage = one.realMileage ?? '--'
				one.realFee = one.realFee ?? '--'
			}

			dataList.value = list;
			totalCount.value = Number(result.totalCount)


		} catch (e) {
			console.error('查询订单分页数据失败', e)
			ElMessage.error('查询订单分页数据失败')
		} finally {
			dataListLoading.value = false
		}
	}

	const searchHandle = async () => {
		try {
			await dataFormRef.value.validate()
			pageIndex.value = 1
			loadDataList()
		} catch (e) {}
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
		// 不管展开还是折叠，先清理定时器。
		if (gpsTimer) {
			clearInterval(gpsTimer)
			gpsTimer = null
		}

		if (expandedRows.length > 0) {
			// 展开动作
			expands.clearInterval = [row.id]

			// 等待100毫秒，让Vue先把旧面板的DOM销毁，新面板的DOM渲染出来之后
			// 再去拉取数据并初始化地图，防止地图加载到不存在的节点上。
			setTimeout(() => {
				loadPanelData(row)
			}, 100)
		} else {
			// 折叠
			expands.value = []
		}
	}


	const showOrderBill = (id) => nextTick(() => orderBillRef.value?.init(id))

	const showUpdatePanel = (id) => nextTick(() => orderCloseRef.value?.init(id))

	onMounted(() => loadDataList())


	// 初始化地图并绘制路线/点位
	const initMapWithRealData = (row, content, result) => {
		const mapDom = document.getElementById(`map-${row.id}`)
		if (!mapDom) return

		mapDom.innerHTML = ''

		// 清理旧的定时器，防止多个定时器同时运行
		if (gpsTimer) {
			clearInterval(gpsTimer)
			gpsTimer = null
		}

		try {
			// 解析起终点坐标
			const startLoc = content.startPlaceLocation
			const endLoc = content.endPlaceLocation

			// 创建中心点
			const mapCenter = new TMap.LatLng(startLoc.latitude, startLoc.longitude)

			// 创建地图实例
			const map = new TMap.Map(mapDom, {
				center: mapCenter,
				zoom: 13,
				viewMode: '2D', // 地图固定为上北下南，禁止用户旋转或倾斜视角
				baseMap: {
					type: 'vector', // 使用矢量底图，加载快且缩放清晰
					features: ['base', 'label'] // 指定底图只渲染“道路/底面”和“文字标注”，屏蔽掉 3D 建筑物
				}
			})

			// 1. 计算最佳线路（灰色线）
			const driveLine = result.driveLine
			if (driveLine && driveLine.routes && driveLine.routes.length > 0) {
				// 将压缩坐标串转换为正常坐标点数组
				let coors = driveLine.routes[0].polyline
				let pl = []
				let kr = 1000000
				for (let i = 2; i < coors.length; i++) {
					coors[i] = Number(coors[i - 2]) + Number(coors[i]) / kr
				}
				for (let i = 0; i < coors.length; i += 2) {
					pl.push(new TMap.LatLng(coors[i], coors[i + 1]))
				}

				new TMap.MultiPolyline({
					map: map, // 指定该图层应用到map这个地图容器上
					styles: { // 定义图层样式
						style_grey: new TMap.PolylineStyle({
							color: 'rgba(190,188,188,1)',
							width: 6,
							lineCap: 'round'
						})
					},
					geometries: [{ // 指定具体坐标点以及采用什么样式
						id: 'pl_1',
						styleId: 'style_grey',
						paths: pl
					}]
				})
			}

			// 2. 创建标记图层
			const markerLayer = new TMap.MultiMarker({
				map: map,
				styles: {
					startStyle: new TMap.MarkerStyle({
						width: 24,
						height: 36,
						anchor: {
							x: 16,
							y: 32
						},
						src: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/start.png'
					}),
					endStyle: new TMap.MarkerStyle({
						width: 24,
						height: 36,
						anchor: {
							x: 16,
							y: 32
						},
						src: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/end.png'
					}),
					carStyle: new TMap.MarkerStyle({
						width: 30,
						height: 30,
						src: driverIcon,
						anchor: {
							x: 16,
							y: 32
						}
					})
				},
				geometries: [{
						id: '1',
						styleId: 'startStyle',
						position: new TMap.LatLng(startLoc.latitude, startLoc.longitude)
					},
					{
						id: '2',
						styleId: 'endStyle',
						position: new TMap.LatLng(endLoc.latitude, endLoc.longitude)
					}
				]
			})

			// 3. 状态 4: 代驾中，开启轮询
			if (content.status === 4 && result.lastGps) {
				markerLayer.add([{ // 只要拿到markerLayer实例，就能用它的carStyle样式
					id: '3',
					styleId: 'carStyle',
					position: new TMap.LatLng(result.lastGps.latitude, result.lastGps.longitude)
				}])

				// 每 15 秒获取一次最新位置
				gpsTimer = setInterval(async () => {
					try {
						const resp = await axios.get(`${baseUrl}order/last-gps`, {
							params: {
								orderId: row.id
							},
							headers: {
								token: localStorage.getItem("token")
							}
						})

						if (resp.data.result) {
							const newGps = resp.data.result
							// 在标记图层当中更新标记点。
							markerLayer.updateGeometries([{
								id: '3', // 注意id要一致。
								styleId: 'carStyle',
								position: new TMap.LatLng(newGps.latitude, newGps.longitude)
							}])
						} else {
							// 查不到数据（可能订单结束了），清理定时器并重新加载面板
							clearInterval(gpsTimer)
							gpsTimer = null
							// 重新加载面板数据
							loadPanelData(row)
						}
					} catch (e) {
						console.error('轮询最新GPS失败', e)
					}
				}, 15000)
			}

			// 4. 状态 5-8: 代驾已结束，绘制全程轨迹
			if (content.status >= 5 && content.status <= 8 && result.orderGps) {
				const paths = result.orderGps.map(gps => new TMap.LatLng(gps.latitude, gps.longitude))
				if (paths.length > 0) {
					// 创建一个新的图层，画折线。
					new TMap.MultiPolyline({
						map: map,
						styles: {
							style_blue: new TMap.PolylineStyle({
								color: '#3777FF',
								width: 6
							})
						},
						geometries: [{
							id: 'drive-polyline',
							styleId: 'style_blue',
							paths: paths
						}]
					})
				}
			}
		} catch (error) {
			console.warn('地图初始化失败', error)
		}
	}

	// 加载面板数据的函数
	const loadPanelData = async (row) => {
		try {
			// 1.获取订单的综合数据
			const resp = await axios.get(`${baseUrl}order/comprehensive-info`, {
				params: {
					orderId: row.id
				},
				headers: {
					token: localStorage.getItem("token")
				}
			})

			// result对应的就是后端的 resultMap
			const result = resp.data.result
			// content是：resultMap中content这个key对应的数据：订单的详细信息。
			const content = result.content

			// 回填客户信息
			const customerInfo = result.customerInfo || {}
			panel.customer.id = customerInfo.id ?? '--'
			panel.customer.sex = customerInfo.sex ?? '--'
			panel.customer.tel = customerInfo.tel ?? '--'

			// 回填司机信息
			const driverInfo = result.driverInfo || {}
			panel.driver.id = driverInfo.id ?? '--'
			panel.driver.name = driverInfo.name ?? '--'
			panel.driver.tel = driverInfo.tel ?? '--'

			// 回填订单信息
			panel.order.carPlate = content.carPlate ?? '--'
			panel.order.carType = content.carType ?? '--'
			panel.order.city = calculateCarPlateCity(content.carPlate) || '未知'

			panel.order.acceptTime = content.acceptTime ?? '--'
			panel.order.arriveTime = content.arriveTime ?? '--'
			panel.order.startTime = content.startTime ?? '--'
			panel.order.endTime = content.endTime ?? '--'

			panel.order.waitingMinute = content.waitingMinute != null ? `${content.waitingMinute}分钟` : '--'
			panel.order.driveMinute = content.driveMinute != null ? `${content.driveMinute}分钟` : '--'

			// 回填真实里程与费用（同步更新表格行 row 数据）
			if (content.realMileage != null) {
				panel.order.realMileage = `${content.realMileage}公里`
				row.realMileage = content.realMileage
			} else {
				panel.order.realMileage = '--'
				row.realMileage = '--'
			}

			if (content.realFee != null) {
				panel.order.realFee = `${content.realFee}元`
				row.realFee = content.realFee
			} else {
				panel.order.realFee = '--'
				row.realFee = '--'
			}

			panel.order.status = statusMap[String(content.status)] || '未知'
			row.status = statusMap[String(content.status)] || '未知'

			// 回填规则信息
			panel.order.chargeRule = result.chargeRule?.code ?? '--'
			panel.order.cancelRule = result.cancelRule?.code ?? '--'
			panel.order.profitsharingRule = result.profitSharingRule?.code ?? '--'

			// 5. 等 DOM 渲染完毕后绘制地图
			nextTick(() => {
				// 绘制地图的方法。
				initMapWithRealData(row, content, result)
			})

		} catch (e) {
			console.error('加载面板数据失败：', e)
			ElMessage.error('加载面板数据失败')
		}
	}
</script>

<style lang="less" scoped>
	@import url('order.less');
</style>