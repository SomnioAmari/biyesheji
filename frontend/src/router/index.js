import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('../views/Register.vue'), meta: { public: true } },
  { path: '/', component: () => import('../views/Home.vue') },
  { path: '/article/:id', component: () => import('../views/ArticleDetail.vue') },
  { path: '/settings', component: () => import('../views/Settings.vue') },
  { path: '/novel/search', component: () => import('../views/NovelSearch.vue') },
  { path: '/novel/mine', component: () => import('../views/Upload.vue') },
  { path: '/novel/:bookId', component: () => import('../views/BookDetail.vue') },
  { path: '/novel/:bookId/:chapterId', component: () => import('../views/ChapterReader.vue') },
  { path: '/bookshelf', component: () => import('../views/Bookshelf.vue') },
  { path: '/admin/review', component: () => import('../views/AdminReview.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/users', component: () => import('../views/AdminUsers.vue'), meta: { requiresAdmin: true } },
  { path: '/profile', component: () => import('../views/Profile.vue') },
  // 兼容旧地址
  { path: '/news-sources', redirect: '/settings?tab=news' },
  { path: '/book-sources', redirect: '/settings?tab=book' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) {
    return '/login'
  }
  if (to.meta.requiresAdmin) {
    const user = JSON.parse(localStorage.getItem('user') || 'null')
    if (!user || user.role !== 1) {
      return '/'
    }
  }
})

export default router
