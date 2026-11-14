import request from './request'

// 认证
export const loginApi = (data) => request.post('/auth/login', data)
export const registerApi = (data) => request.post('/auth/register', data)

// 新闻源
export const listNewsSources = () => request.get('/news-sources')
export const createNewsSource = (data) => request.post('/news-sources', data)
export const updateNewsSource = (id, data) => request.put(`/news-sources/${id}`, data)
export const deleteNewsSource = (id) => request.delete(`/news-sources/${id}`)
export const fetchNewsSource = (id) => request.post(`/news-sources/${id}/fetch`)

// 文章
export const listArticles = (params) => request.get('/articles', { params })
export const getArticle = (id) => request.get(`/articles/${id}`)
export const favoriteArticle = (id) => request.post(`/articles/${id}/favorite`)
export const unfavoriteArticle = (id) => request.delete(`/articles/${id}/favorite`)
export const listFavoriteArticles = () => request.get('/articles/favorites')

// 书源
export const listBookSources = () => request.get('/book-sources')
export const createBookSource = (data) => request.post('/book-sources', data)
export const updateBookSource = (id, data) => request.put(`/book-sources/${id}`, data)
export const deleteBookSource = (id) => request.delete(`/book-sources/${id}`)
export const importBookSources = (data) => request.post('/book-sources/import', data)
export const importBookSourcesByUrl = (data) => request.post('/book-sources/import-url', data)

// 书籍：内部书库搜索（优先）
export const searchLibrary = (keyword) => request.get('/novel/search', { params: { keyword } })
// 书籍：自定义书源搜索（兜底）
export const searchNovelSources = (keyword) => request.get('/novel/search-sources', { params: { keyword } })
// 书籍：上传 / 我的作品
export const uploadNovel = (formData) => request.post('/novel/upload', formData)
export const listMyNovels = () => request.get('/novel/mine')
export const deleteMyNovel = (bookId) => request.delete(`/novel/mine/${bookId}`)

export const getNovelDetail = (sourceId, bookUrl) =>
  request.get('/novel/detail', { params: { sourceId, bookUrl } })
export const getNovelDetailById = (bookId) => request.get(`/novel/${bookId}`)
export const getNovelCatalog = (bookId) => request.get(`/novel/${bookId}/catalog`)
export const getChapterContent = (bookId, chapterId) => request.get(`/novel/${bookId}/${chapterId}`)

// 书架
export const listBookshelf = () => request.get('/bookshelf')
export const addToBookshelf = (bookId) => request.post(`/bookshelf/${bookId}`)
export const removeFromBookshelf = (bookId) => request.delete(`/bookshelf/${bookId}`)

// 进度
export const getProgress = (targetType, targetId) =>
  request.get('/progress', { params: { targetType, targetId } })
export const reportProgress = (data) => request.put('/progress', data)

// 管理员：用户管理
export const listUsers = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, { status })
export const updateUserRole = (id, role) => request.put(`/admin/users/${id}/role`, { role })
export const resetUserPassword = (id, password) => request.put(`/admin/users/${id}/password`, { password })

// 管理员：书籍审核
export const listAdminNovels = (params) => request.get('/admin/novels', { params })
export const reviewNovel = (bookId, data) => request.post(`/admin/novels/${bookId}/review`, data)
export const deleteAdminNovel = (bookId) => request.delete(`/admin/novels/${bookId}`)
