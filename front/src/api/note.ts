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

// 批量删除
export const deleteNotes = (ids: number[]) => {
    return request.delete('/note/delete/batch', { data: { ids } });
};

// 统计
export const getNoteStatistics = () => {
    return request.get('/note/statistics');
};

// 搜索
export const searchNote = (params: any) => {
    return request.get('/note/search', { params });
};

/**
 * 下载笔记文件（markdown / html / txt）。
 * 注意：导出接口返回的是文件流，必须用 blob 方式请求，不能走普通 JSON 拦截。
 */
export async function downloadNoteExport(ids: number[] | string, format: 'markdown' | 'html' | 'txt' | 'pdf') {
    const idParam = Array.isArray(ids) ? ids.join(',') : ids;
    const res = await request.get('/note/export', {
        params: { ids: idParam || undefined, format },
        responseType: 'blob'
    });

    // 后端出错时返回的是 JSON，这里需要识别并抛错
    const contentType = String(res.headers['content-type'] || '');
    if (contentType.includes('application/json')) {
        const text = await (res.data as Blob).text();
        let msg = '导出失败';
        try { msg = JSON.parse(text).msg || msg; } catch { /* ignore */ }
        throw { code: 500, msg };
    }

    const ext = format === 'txt' ? 'txt' : format === 'markdown' ? 'md' : 'html';
    const disposition = String(res.headers['content-disposition'] || '');
    const match = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(disposition);
    const filename = match ? decodeURIComponent(match[1]) : `notes.${ext}`;

    const blob = new Blob([res.data], { type: contentType || 'application/octet-stream' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    return filename;
}
