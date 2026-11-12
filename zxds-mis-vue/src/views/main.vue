<template>
	<div class="site-wrapper" :class="{ 'site-sidebar--fold': sidebarFold }" v-loading.fullscreen.lock="loading"
		element-loading-text="拼命加载中">
		<nav class="site-navbar" :class="'site-navbar--' + navbarLayoutType">
			<!-- 左侧 Logo 区 -->
			<div class="site-navbar__header">
				<h1 class="site-navbar__brand">
					<a class="site-navbar__brand-lg">智行代驾管理系统</a>
					<a class="site-navbar__brand-mini">智行</a>
				</h1>
			</div>

			<!-- 右侧内容区 -->
			<div class="site-navbar__body clearfix">
				<!-- 折叠按钮 -->
				<div class="site-navbar__switch" @click="handleSwitch">
					<SvgIcon name="zhedie" class="icon-svg" />
				</div>

				<!-- 右侧工具区 -->
				<div class="site-navbar__right">
					<!-- 铃铛 -->
					<div class="site-navbar__bell">
						<el-badge :value="0" :offset="[-2, 8]">
							<SvgIcon name="duanxin" class="icon-svg" />
						</el-badge>
					</div>

					<!-- 用户下拉 -->
					<div class="site-navbar__user">
						<el-dropdown>
							<span class="el-dropdown-link">{{ name }}</span>
							<template #dropdown>
								<el-dropdown-menu>
									<el-dropdown-item @click="updatePasswordHandle()">修改密码</el-dropdown-item>
									<el-dropdown-item @click="logout">退出</el-dropdown-item>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
					</div>
				</div>
			</div>

			<update-password ref="updatePasswordRef"></update-password>
		</nav>

		<!-- 侧边栏 -->
		<aside class="site-sidebar site-sidebar--dark">
			<div class="site-sidebar__inner">
				<el-menu :default-active="menuActiveName || 'home'" :collapse="sidebarFold" :collapse-transition="false"
					class="site-sidebar__menu" background-color="#263238" text-color="#bfcbd9"
					active-text-color="#409EFF">
					<el-menu-item index="home" @click="$router.push({ name: 'Home' })">
						<SvgIcon name="home" class="icon-svg" />
						<template #title><span>首页</span></template>
					</el-menu-item>

					<el-sub-menu index="组织管理">
						<template #title>
							<SvgIcon name="users_fill" class="icon-svg" />
							<span>组织管理</span>
						</template>
						<el-menu-item index="dept" v-if="isAuth(['ROOT', 'DEPT:SELECT'])"
							@click="$router.push({ name: 'Dept' })">
							<SvgIcon name="company_fill" class="icon-svg" />
							<template #title>部门管理</template>
						</el-menu-item>
						<el-menu-item index="role" v-if="isAuth(['ROOT', 'ROLE:SELECT'])"
							@click="$router.push({ name: 'Role' })">
							<SvgIcon name="role_fill" class="icon-svg" />
							<template #title>角色管理</template>
						</el-menu-item>
						<el-menu-item index="user" v-if="isAuth(['ROOT', 'USER:SELECT'])"
							@click="$router.push({ name: 'User' })">
							<SvgIcon name="user_fill" class="icon-svg" />
							<template #title>用户管理</template>
						</el-menu-item>
					</el-sub-menu>

					<el-sub-menu index="司机管理">
						<template #title>
							<SvgIcon name="users_fill" class="icon-svg" />
							<span>司机管理</span>
						</template>
						<el-menu-item index="driver" v-if="isAuth(['ROOT', 'DRIVER:SELECT'])"
							@click="$router.push({ name: 'Driver' })">
							<SvgIcon name="company_fill" class="icon-svg" />
							<template #title>司机信息</template>
						</el-menu-item>
						<el-menu-item index="driver_wallet" v-if="isAuth(['ROOT', 'DRIVER_WALLET:SELECT'])"
							@click="$router.push({ name: 'DriverWallet' })">
							<SvgIcon name="company_fill" class="icon-svg" />
							<template #title>司机钱包</template>
						</el-menu-item>
						<el-menu-item index="driver_fine" v-if="isAuth(['ROOT', 'DRIVER_FINE:SELECT'])"
							@click="$router.push({ name: 'DriverFine' })">
							<SvgIcon name="role_fill" class="icon-svg" />
							<template #title>司机罚款</template>
						</el-menu-item>
						<el-menu-item index="driver_lockdown" v-if="isAuth(['ROOT', 'DRIVER_LOCKDOWN:SELECT'])"
							@click="$router.push({ name: 'DriverLockdown' })">
							<SvgIcon name="user_fill" class="icon-svg" />
							<template #title>禁止接单</template>
						</el-menu-item>
					</el-sub-menu>

					<el-sub-menu index="运营管理">
						<template #title>
							<SvgIcon name="users_fill" class="icon-svg" />
							<span>运营管理</span>
						</template>
						<el-menu-item index="order" v-if="isAuth(['ROOT', 'ORDER:SELECT'])"
							@click="$router.push({ name: 'Order' })">
							<SvgIcon name="company_fill" class="icon-svg" />
							<template #title>订单信息</template>
						</el-menu-item>
						<el-menu-item index="comment" v-if="isAuth(['ROOT', 'COMMENT:SELECT'])"
							@click="$router.push({ name: 'Comment' })">
							<SvgIcon name="company_fill" class="icon-svg" />
							<template #title>订单评价</template>
						</el-menu-item>
						<el-menu-item index="voucher" v-if="isAuth(['ROOT', 'VOUCHER:SELECT'])"
							@click="$router.push({ name: 'Voucher' })">
							<SvgIcon name="company_fill" class="icon-svg" />
							<template #title>代金券</template>
						</el-menu-item>
						<el-menu-item index="monitoring" v-if="isAuth(['ROOT', 'MONITORING:SELECT'])"
							@click="$router.push({ name: 'Monitoring' })">
							<SvgIcon name="company_fill" class="icon-svg" />
							<template #title>安全监控</template>
						</el-menu-item>
					</el-sub-menu>

					<el-sub-menu index="规则管理">
						<template #title>
							<SvgIcon name="meeting_fill" class="icon-svg" />
							<span>结算规则</span>
						</template>
						<el-menu-item index="approval" v-if="isAuth(['ROOT', 'WORKFLOW:APPROVAL', 'FILE:ARCHIVE'])"
							@click="$router.push({ name: 'Approval' })">
							<SvgIcon name="warehouse_fill" class="icon-svg" />
							<template #title>取消规则</template>
						</el-menu-item>
					</el-sub-menu>

					<el-sub-menu index="账务管理">
						<template #title>
							<SvgIcon name="meeting_fill" class="icon-svg" />
							<span>代驾费</span>
						</template>
						<el-menu-item index="meeting-room" @click="$router.push({ name: 'MeetingRoom' })">
							<SvgIcon name="warehouse_fill" class="icon-svg" />
							<template #title>司机钱包</template>
						</el-menu-item>
					</el-sub-menu>
				</el-menu>
			</div>
		</aside>

		<!-- 内容区 -->
		<div class="site-content__wrapper" :style="{ 'min-height': documentClientHeight + 'px' }">
			<main class="site-content" :class="{ 'site-content--tabs': $route.meta.isTab }">
				<el-tabs v-if="$route.meta.isTab" v-model="mainTabsActiveName" :closable="true"
					@tab-click="selectedTabHandle" @tab-remove="removeTabHandle">
					<el-dropdown class="site-tabs__tools" trigger="click">
						<i class="el-icon-arrow-down el-icon--right"></i>
						<template #dropdown>
							<el-dropdown-menu>
								<el-dropdown-item @click="tabsCloseCurrentHandle">关闭当前标签页</el-dropdown-item>
								<el-dropdown-item @click="tabsCloseOtherHandle">关闭其它标签页</el-dropdown-item>
								<el-dropdown-item @click="tabsCloseAllHandle">关闭全部标签页</el-dropdown-item>
							</el-dropdown-menu>
						</template>
					</el-dropdown>
					<el-tab-pane v-for="item in mainTabs" :key="item.name + '-' + item.title" :label="item.title"
						:name="item.name" :lazy="true">
						<el-card :body-style="siteContentViewHeight">
							<iframe v-if="item.type === 'iframe'" :src="item.iframeUrl" width="100%" height="100%"
								frameborder="0" scrolling="yes"></iframe>
							<router-view v-else-if="item.name === mainTabsActiveName" />
						</el-card>
					</el-tab-pane>
				</el-tabs>
				<el-card v-else :body-style="siteContentViewHeight">
					<router-view />
				</el-card>
			</main>
		</div>
	</div>
</template>

<script setup>
	import {
		ref,
		onMounted,
		watch,
		nextTick
	} from 'vue'
	import {
		useRoute,
		useRouter
	} from 'vue-router'
	import {
		ElMessage
	} from 'element-plus'
	import {
		http
	} from '@/utils/http'
	import {
		isAuth
	} from '@/utils/auth'
	import {
		isURL
	} from '../utils/validate'
	import SvgIcon from '../components/SvgIcon.vue'
	import UpdatePassword from './update-password.vue'

	defineOptions({
		name: 'Main'
	})

	const route = useRoute()
	const router = useRouter()

	const navbarLayoutType = ref('')
	const sidebarFold = ref(false)
	const name = ref('')
	const photo = ref('')
	const documentClientHeight = ref(0)
	const siteContentViewHeight = ref({})
	const height = ref(null)
	const mainTabs = ref([])
	const mainTabsActiveName = ref('')
	const menuActiveName = ref('')
	const loading = ref(false)
	const updatePasswordRef = ref(null)

	const handleSwitch = () => {
		if (sidebarFold.value) {
			navbarLayoutType.value = ''
		} else {
			navbarLayoutType.value = 'fold'
		}
		sidebarFold.value = !sidebarFold.value
	}

	const resetDocumentClientHeight = () => {
		documentClientHeight.value = document.documentElement.clientHeight
		window.onresize = () => {
			documentClientHeight.value = document.documentElement.clientHeight
			loadSiteContentViewHeight()
		}
	}

	const loadSiteContentViewHeight = () => {
		let h = documentClientHeight.value - 50 - 30 - 2
		if (route.meta.isTab) {
			h -= 40
			siteContentViewHeight.value = isURL(route.meta.iframeUrl) ?
				{
					height: h + 'px'
				} :
				{
					minHeight: h + 'px'
				}
			height.value = h - 40 + 'px'
		} else {
			siteContentViewHeight.value = {
				minHeight: h + 'px'
			}
		}
	}

	const routeHandle = (r) => {
		resetDocumentClientHeight()
		loadSiteContentViewHeight()
		if (r.meta.isTab) {
			let tab = mainTabs.value.filter((item) => item.name === r.name)[0]
			if (!tab) {
				tab = {
					menuId: r.meta.menuId || r.name,
					name: r.name,
					title: r.meta.title,
					type: isURL(r.meta.iframeUrl) ? 'iframe' : 'module',
					iframeUrl: r.meta.iframeUrl || '',
					params: r.params,
					query: r.query
				}
				mainTabs.value = mainTabs.value.concat(tab)
			}
			menuActiveName.value = tab.menuId + ''
			mainTabsActiveName.value = tab.name
		}
	}

	const logout = () => {
		http('user/logout', 'GET', null, true, () => {
			localStorage.removeItem('permissions')
			localStorage.removeItem('token')
			router.push({
				name: 'Login'
			})
		})
	}

	const updatePasswordHandle = () => {
		nextTick(() => {
			updatePasswordRef.value?.init()
		})
	}

	const selectedTabHandle = (tab) => {
		const list = mainTabs.value.filter((item) => item.name === tab.paneName)
		if (list.length >= 1) {
			router.push({
				name: list[0].name,
				query: list[0].query,
				params: list[0].params
			})
		}
	}

	const removeTabHandle = (tabName) => {
		mainTabs.value = mainTabs.value.filter((item) => item.name !== tabName)
		if (mainTabs.value.length >= 1) {
			if (tabName === mainTabsActiveName.value) {
				const tab = mainTabs.value[mainTabs.value.length - 1]
				router.push({
					name: tab.name,
					query: tab.query,
					params: tab.params
				})
			}
		} else {
			menuActiveName.value = ''
			router.push({
				name: 'Home'
			})
		}
	}

	const tabsCloseCurrentHandle = () => {
		removeTabHandle(mainTabsActiveName.value)
	}

	const tabsCloseOtherHandle = () => {
		mainTabs.value = mainTabs.value.filter((item) => item.name === mainTabsActiveName.value)
	}

	const tabsCloseAllHandle = () => {
		mainTabs.value = []
		menuActiveName.value = ''
		router.push({
			name: 'Home'
		})
	}

	watch(
		() => route.path,
		() => {
			routeHandle(route)
		}
	)

	onMounted(() => {
		routeHandle(route)
		http('user/loadUserInfo', 'GET', null, true, (resp) => {
			name.value = resp.name
			photo.value = resp.photo
		})
		resetDocumentClientHeight()
		loadSiteContentViewHeight()
	})
</script>

<style lang="scss">
	@import '../assets/scss/index.scss';

	/* ============================================================
   顶栏左侧折叠按钮
   ============================================================ */
	.site-navbar__switch {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		width: 50px;
		height: 50px;
		color: #303133;
		cursor: pointer;
		transition: background-color 0.3s;
		float: left;

		&:hover {
			background-color: #f5f7fa;
		}

		.icon-svg {
			width: 20px;
			height: 20px;
			fill: currentColor;
		}
	}

	/* ============================================================
   顶栏右侧容器
   ============================================================ */
	.site-navbar__right {
		float: right;
		height: 50px;
		display: flex;
		align-items: center;

		/* 铃铛按钮 */
		.site-navbar__bell {
			display: inline-flex;
			align-items: center;
			justify-content: center;
			width: 50px;
			height: 50px;
			color: #303133;
			cursor: pointer;
			transition: background-color 0.3s;

			&:hover {
				background-color: #f5f7fa;
			}

			.icon-svg {
				width: 20px;
				height: 20px;
				fill: currentColor;
			}

			:deep(.el-badge__content) {
				top: 6px;
				right: 10px;
			}
		}

		/* 用户下拉 */
		.site-navbar__user {
			display: inline-flex;
			align-items: center;
			height: 50px;
			padding: 0 15px;
			color: #303133;
			cursor: pointer;
			transition: background-color 0.3s;

			&:hover {
				background-color: #f5f7fa;
			}

			.el-dropdown-link {
				color: #303133;
				cursor: pointer;
				outline: none;

				&:hover {
					color: #409eff;
				}
			}
		}
	}
</style>