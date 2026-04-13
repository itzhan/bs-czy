import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { noAuth: true } },
  {
    path: '/',
    component: () => import('../components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/index.vue'), meta: { title: '数据概览', icon: 'DataAnalysis' } },
      { path: 'users', name: 'Users', component: () => import('../views/user/index.vue'), meta: { title: '用户管理', icon: 'User' } },
      { path: 'questions', name: 'Questions', component: () => import('../views/question/index.vue'), meta: { title: '问题管理', icon: 'ChatDotSquare' } },
      { path: 'answers', name: 'Answers', component: () => import('../views/answer/index.vue'), meta: { title: '回答管理', icon: 'Comment' } },
      { path: 'comments', name: 'Comments', component: () => import('../views/comment/index.vue'), meta: { title: '评论管理', icon: 'ChatLineSquare' } },
      { path: 'tags', name: 'Tags', component: () => import('../views/tag/index.vue'), meta: { title: '标签管理', icon: 'Collection' } },
      { path: 'reports', name: 'Reports', component: () => import('../views/report/index.vue'), meta: { title: '举报管理', icon: 'Warning' } },
      { path: 'ai-config', name: 'AiConfig', component: () => import('../views/ai-config/index.vue'), meta: { title: 'AI配置', icon: 'Robot' } },
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  if (to.meta.noAuth) return next()
  const token = localStorage.getItem('admin_token')
  if (!token) return next('/login')
  next()
})

export default router
