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

// 小说
export const searchNovel = (keyword) => request.get('/novel/search', { params: { keyword } })
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
