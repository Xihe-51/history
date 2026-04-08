package com.ailab.back.service.impl;

import com.ailab.back.DTO.*;
import com.ailab.back.Entity.Note;
import com.ailab.back.mapper.NoteMapper;
import com.ailab.back.service.NoteService;
import com.ailab.back.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    //需要自定义sql
    private final NoteMapper noteMapper;

    @Override
    public NotePageResultVo<NotePageItemVO> pageList(NotePageDTO dto, Long userId) {
        //创建分页参数对象
        Page<Note>page=new Page<>(dto.getPageNum(),dto.getPageSize());

        //创建查询包装器
        LambdaQueryWrapper<Note> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId,userId);
        //查询条件
        //必须是当前用户的笔记
        if(StringUtils.hasText(dto.getType())){
            queryWrapper.eq(Note::getType,dto.getType());
        }
        if(StringUtils.hasText(dto.getKeyword())){
            queryWrapper
                    .and(w->w.like(Note::getTitle,dto.getKeyword())
                    .or().like(Note::getContent,dto.getKeyword()) );
        }
        if(StringUtils.hasText(dto.getTag())){
            queryWrapper.apply("JSON_CONTAINS(tags, '\""+dto.getTag()+"\"')");
        }

        //调用分页方法(调用IService父类)
        Page<Note>result=this.page(page,queryWrapper);

        //返回统一响应
        NotePageResultVo<NotePageItemVO>voPage=new NotePageResultVo<>();
        voPage.setTotal(result.getTotal());
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setPages(result.getPages());

        voPage.setRecords(result.getRecords().stream().map(n->{
            NotePageItemVO vo=new NotePageItemVO();
            vo.setId(n.getId());
            vo.setUserId(n.getUserId());
            vo.setType(n.getType());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setTags(n.getTags());
            vo.setCreateTime(n.getCreateTime());
            vo.setUpdateTime(n.getUpdateTime());
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public NoteDetailVO noteDetail(Long id) {
        Note n=this.getById(id);
        if(n==null){
            return null;
        }
        NoteDetailVO vo=new NoteDetailVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setType(n.getType());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setTags(n.getTags());
        vo.setCreateTime(n.getCreateTime());
        vo.setUpdateTime(n.getUpdateTime());
        return vo;
    }

    @Override
    public Note addNote(NoteAddDTO dto) {
        Note note=new Note();
        note.setUserId(1L);
        note.setType(dto.getType());
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setTags(dto.getTags());
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());
        this.save(note);
        return note;
    }

    @Override
    public boolean updateNote(NoteUpdateDTO dto) {
        Note note=this.getById(dto.getId());
        if(note==null){
            return false;
        }
        if(dto.getType()!=null){
            note.setType(dto.getType());
        }
        if (dto.getTitle() != null) {
            note.setTitle(dto.getTitle());
        }
        if(dto.getContent() != null) {
            note.setContent(dto.getContent());
        }
        if(dto.getTags() != null) {
            note.setTags(dto.getTags());
        }
        note.setUpdateTime(LocalDateTime.now());
        return this.updateById(note);
    }

    @Override
    public boolean deleteNote(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean deleteBatchNote(NoteBatchDeleteDTO dto) {
        return this.removeBatchByIds(dto.getIds());
    }

    @Override
    public NoteStatisticsVO noteStatistics(Long userId) {
        NoteStatisticsVO vo = new NoteStatisticsVO();

        // 1. 查询用户所有笔记
        List<Note> list = this.lambdaQuery()
                .eq(Note::getUserId, userId)
                .list();

        // 2. 总数
        vo.setTotalCount((long) list.size());

        // 3. 类型统计
        Map<String, Long> typeMap = list.stream()
                .collect(Collectors.groupingBy(Note::getType, Collectors.counting()));

        List<NoteTypeStatVO> typeStats = new ArrayList<>();
        typeMap.forEach((type, count) -> {
            NoteTypeStatVO item = new NoteTypeStatVO();
            item.setType(type);
            item.setTypeName(getTypeName(type));
            item.setCount(count);
            typeStats.add(item);
        });
        vo.setTypeStats(typeStats);

        // 4. 最近 7 天新增统计
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6); // 最近 7 天

        Map<String, Long> dateCountMap = list.stream()
                .filter(n -> n.getCreateTime() != null)
                .filter(n -> !n.getCreateTime().toLocalDate().isBefore(start))
                .collect(Collectors.groupingBy(
                        n -> n.getCreateTime().toLocalDate().toString(),
                        Collectors.counting()
                ));

        List<NoteStatisticsVO.RecentCreateItem> recentList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            NoteStatisticsVO.RecentCreateItem item = new NoteStatisticsVO.RecentCreateItem();
            item.setDate(date.toString());
            item.setCount(dateCountMap.getOrDefault(date.toString(), 0L));
            recentList.add(item);
        }

        vo.setRecentCreate(recentList);

        return vo;
    }


    private String getTypeName(String type){
        return switch (type) {
            case "study" -> "学习笔记";
            case "insight" -> "心得笔记";
            case "collect" -> "收藏笔记";
            case "custom" -> "自定义";
            default -> "未知类型";
        };
    }


    @Override
    //分页查询
    public NoteSearchResultVo<NoteSearchItemVO> noteSearch(NoteSearchDTO dto, Long userId) {
        //创建分页对象
        Page<Note>page=new Page<>(dto.getPageNum(), dto.getPageSize());
        //创建查询包装器
        LambdaQueryWrapper <Note> queryWrapper=new LambdaQueryWrapper<>();
        //查询条件
        queryWrapper.eq(Note::getUserId,userId)
                .and(w->w.like(Note::getTitle,dto.getKeyword())
                        .or().like(Note::getContent,dto.getKeyword()));
        //调用查询方法
        Page<Note>result=this.page(page,queryWrapper);
        //封装返回统一响应
        NoteSearchResultVo<NoteSearchItemVO> voPage=new NoteSearchResultVo<>();
        voPage.setTotal(result.getTotal());
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setPages(result.getPages());

        voPage.setRecords(result.getRecords().stream().map(n->{
            NoteSearchItemVO vo=new NoteSearchItemVO();
            vo.setId(n.getId());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setType(n.getType());
            vo.setCreateTime(n.getCreateTime());

            Map<String, List<String>>highlight=new HashMap<>();
            highlight.put("title",Collections.singletonList(
                    n.getTitle().replace(dto.getKeyword(), "<em>"+dto.getKeyword()+"</em>")
            ));
            highlight.put("content",Collections.singletonList(
                    n.getContent().replace(dto.getKeyword(), "<em>"+dto.getKeyword()+"</em>")
            ));
            vo.setHighlight(highlight);
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }
//导出pdf
    @Override
    public byte[] export(String ids, String format, Long userId) {
        return null;
    }
}
