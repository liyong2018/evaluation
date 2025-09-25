import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue')
    },
    {
      path: '/data-management',
      name: 'DataManagement',
      component: () => import('@/views/DataManagement.vue')
    },
    {
      path: '/weight-config',
      name: 'WeightConfig',
      component: () => import('@/views/WeightConfig.vue')
    },
    {
      path: '/evaluation',
      name: 'Evaluation',
      component: () => import('@/views/Evaluation.vue')
    },
    {
      path: '/results',
      name: 'Results',
      component: () => import('@/views/Results.vue')
    },
    {
      path: '/algorithm-management',
      name: 'AlgorithmManagement',
      component: () => import('@/views/AlgorithmManagement.vue')
    }
  ]
})

export default router
