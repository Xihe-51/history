package com.ailab.back.service;

import com.ailab.back.DTO.*;
import com.ailab.back.Entity.Note;
import com.ailab.back.vo.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface NoteService extends IService<Note> {
    //获取笔记列表
    NotePageResultVo<NotePageItemVO> pageList(NotePageDTO dto, Long userId);

    //获取笔记详细
    NoteDetailVO noteDetail(Long id);
    //新增
    Note addNote(NoteAddDTO dto);
    //更新
    boolean updateNote(NoteUpdateDTO dto);
    //删除
    boolean deleteNote(Long id);
    //批量删除
    boolean deleteBatchNote(NoteBatchDeleteDTO dto);
    //笔记统计信息
    NoteStatisticsVO noteStatistics(Long userId);
    //笔记搜索
    NoteSearchResultVo<NoteSearchItemVO> noteSearch(NoteSearchDTO dto, Long userId);
    //导出笔记
        //markdown/pdf/excel
    byte[] export(String ids,String format,Long userId);
//    byte[] exportExcel(String ids,Long userId);
//    byte[] exportPdf(String ids,Long userId);
}
