import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      redirect: '/data-management'
    },
    {
      path: '/data-management',
      name: 'DataManagement',
      component: () => import('@/views/DataManagement.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/weight-config',
      name: 'WeightConfig',
      component: () => import('@/views/WeightConfig.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/firefighter-config',
      name: 'FirefighterConfig',
      component: () => import('@/views/FirefighterConfig.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/evaluation',
      name: 'Evaluation',
      component: () => import('@/views/Evaluation.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/results',
      name: 'Results',
      component: () => import('@/views/Results.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/algorithm-management',
      name: 'AlgorithmManagement',
      component: () => import('@/views/AlgorithmManagement.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/thematic-map',
      name: 'ThematicMap',
      component: () => import('@/views/ThematicMap.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/model-management',
      name: 'ModelManagement',
      component: () => import('@/views/ModelManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    // 系统管理
    {
      path: '/system',
      name: 'System',
      redirect: '/system/user',
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'user',
          name: 'UserManagement',
          component: () => import('@/views/system/UserManagement.vue'),
          meta: { requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'role',
          name: 'RoleManagement',
          component: () => import('@/views/system/RoleManagement.vue'),
          meta: { requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'menu',
          name: 'MenuManagement',
          component: () => import('@/views/system/MenuManagement.vue'),
          meta: { requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'organization',
          name: 'OrganizationManagement',
          component: () => import('@/views/OrganizationManagement.vue'),
          meta: { requiresAuth: true, requiresAdmin: true }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 如果访问的是登录页
  if (to.path === '/login') {
    if (userStore.isLoggedIn) {
      // 已登录，跳转到首页
      next('/data-management')
    } else {
      next()
    }
    return
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    // 未登录，跳转到登录页
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    // 非管理员，跳转到首页
    next('/data-management')
    return
  }

  next()
})

export default router
