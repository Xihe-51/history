import request from '@/utils/request';

// 笔记列表
export const getNotePage = (params: any) => {
    return request.get('/note/page', { params });
};

// 笔记详情
export const getNoteDetail = (id: number) => {
    return request.get(`/note/${id}`);
};

// 新增
export const addNote = (data: any) => {
    return request.post('/note/add', data);
};

// 修改
export const updateNote = (data: any) => {
    return request.put('/note/update', data);
};

// 删除
export const deleteNote = (id: number) => {
    return request.delete(`/note/delete/${id}`);
};

// 统计
export const getNoteStatistics = () => {
    return request.get('/note/statistics');
};

// 搜索
export const searchNote = (params: any) => {
    return request.get('/note/search', { params });
};