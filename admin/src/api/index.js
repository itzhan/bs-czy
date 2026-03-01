import request from '../utils/request'

// 认证
export const login = (data) => request.post('/auth/login', data)
export const getCurrentUser = () => request.get('/auth/current')

// 管理端统计
export const getStats = () => request.get('/admin/stats')

// 用户管理
export const getUsers = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, null, { params: { status } })
export const updateUserRole = (id, role) => request.put(`/admin/users/${id}/role`, null, { params: { role } })

// 问题管理
export const getQuestions = (params) => request.get('/admin/questions', { params })
export const updateQuestionStatus = (id, status) => request.put(`/admin/questions/${id}/status`, null, { params: { status } })
export const deleteQuestion = (id) => request.delete(`/admin/questions/${id}`)

// 回答管理
export const getAnswers = (params) => request.get('/admin/answers', { params })
export const updateAnswerStatus = (id, status) => request.put(`/admin/answers/${id}/status`, null, { params: { status } })
export const deleteAnswer = (id) => request.delete(`/admin/answers/${id}`)

// 评论管理
export const getComments = (params) => request.get('/admin/comments', { params })
export const deleteComment = (id) => request.delete(`/admin/comments/${id}`)

// 举报管理
export const getReports = (params) => request.get('/admin/reports', { params })
export const handleReport = (id, status, note) => request.put(`/admin/reports/${id}/handle`, null, { params: { status, note } })

// 标签管理
export const getTags = () => request.get('/admin/tags')
export const createTag = (data) => request.post('/admin/tags', data)
export const updateTag = (id, data) => request.put(`/admin/tags/${id}`, data)
export const deleteTag = (id) => request.delete(`/admin/tags/${id}`)
