export interface NotePageItemVO {
    id: number;
    userId: number;
    type: string;
    title: string;
    content: string;
    tags: string[];
    createTime: string;
    updateTime: string;
    // 添加关联字段
    relateType?: string;
    relateId?: number;
    relatedType?: string;     // 兼容后端字段
    relatedId?: number;
}

export interface NoteDetailVO {
    id: number;
    userId: number;
    type: string;
    title: string;
    content: string;
    tags: string[];
    createTime: string;
    updateTime: string;
    relateType?: string;
    relateId?: number;
    relatedType?: string;
    relatedId?: number;
}

export interface NoteStatisticsVO {
    totalCount: number;
    typeStats: { type: string; typeName: string; count: number }[];
    recentCreate?: { date: string; count: number }[];
}

// 添加笔记请求参数
export interface AddNoteParams {
    userId: number;
    title: string;
    content: string;
    type?: string;
    tags?: string[];
    relateType?: string;
    relateId?: number;
    relatedType?: string;
    relatedId?: number;
}

// 更新笔记请求参数
export interface UpdateNoteParams {
    id: number;
    title?: string;
    content?: string;
    type?: string;
    tags?: string[];
    relateType?: string;
    relateId?: number;
    relatedType?: string;
    relatedId?: number;
}