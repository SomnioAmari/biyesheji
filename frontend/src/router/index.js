import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('../views/Register.vue'), meta: { public: true } },
  { path: '/', component: () => import('../views/Home.vue') },
  { path: '/article/:id', component: () => import('../views/ArticleDetail.vue') },
  { path: '/news-sources', component: () => import('../views/NewsSources.vue') },
  { path: '/novel/search', component: () => import('../views/NovelSearch.vue') },
  { path: '/novel/:bookId', component: () => import('../views/BookDetail.vue') },
  { path: '/novel/:bookId/:chapterId', component: () => import('../views/ChapterReader.vue') },
  { path: '/bookshelf', component: () => import('../views/Bookshelf.vue') },
  { path: '/book-sources', component: () => import('../views/BookSources.vue') },
  { path: '/profile', component: () => import('../views/Profile.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (!to.meta.public && !localStorage.getItem('token')) {
    return '/login'
  }
})

export default router
