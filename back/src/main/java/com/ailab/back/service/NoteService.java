package com.ailab.back.service;

import com.ailab.back.DTO.*;
import com.ailab.back.Entity.Note;
import com.ailab.back.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 笔记服务。
 * 所有方法均以 userId（当前登录用户）作为数据边界，避免越权读写。
 */
public interface NoteService extends IService<Note> {
    /** 分页查询当前用户笔记 */
    NotePageResultVo<NotePageItemVO> pageList(NotePageDTO dto, Long userId);

    /** 查询笔记详情（不存在或非本人返回 null） */
    NoteDetailVO noteDetail(Long id, Long userId);

    /** 新增笔记 */
    Note addNote(NoteAddDTO dto, Long userId);

    /** 更新笔记（仅本人） */
    boolean updateNote(NoteUpdateDTO dto, Long userId);

    /** 删除笔记（仅本人） */
    boolean deleteNote(Long id, Long userId);

    /** 批量删除笔记（仅本人的记录会被删除） */
    boolean deleteBatchNote(NoteBatchDeleteDTO dto, Long userId);

    /** 笔记统计信息 */
    NoteStatisticsVO noteStatistics(Long userId);

    /** 全文检索当前用户笔记 */
    NoteSearchResultVo<NoteSearchItemVO> noteSearch(NoteSearchDTO dto, Long userId);

    /** 导出笔记（markdown / html / txt，pdf 由前端打印生成） */
    byte[] export(String ids, String format, Long userId);
}
