package com.ailab.back.controller;

import com.ailab.back.DTO.*;
import com.ailab.back.Entity.Note;
import com.ailab.back.service.NoteService;
import com.ailab.back.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @PostMapping("/add")
    public ResponseEntity<?> addNote(@RequestBody NoteAddDTO noteAddDTO) {
        Note note=noteService.addNote(noteAddDTO);
        if(note!=null){
            return ResponseEntity.ok(new ResultVO<>(500,"笔记标题不能为空",null));
        }
        NoteDetailVO noteDetailVO=new NoteDetailVO();
        noteDetailVO.setId(note.getId());
        noteDetailVO.setUserId(note.getUserId());
        noteDetailVO.setType(note.getType());
        noteDetailVO.setTitle(note.getTitle());
        noteDetailVO.setContent(note.getContent());
        noteDetailVO.setTags(note.getTags());
        noteDetailVO.setCreateTime(note.getCreateTime());
        noteDetailVO.setUpdateTime(note.getUpdateTime());


        return ResponseEntity.ok(new ResultVO<>(200,"新增成功",noteDetailVO));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateNote(@RequestBody NoteUpdateDTO noteUpdateDTO) {
        boolean ok=noteService.updateNote(noteUpdateDTO);
        return ResponseEntity.ok(new ResultVO<>(200,ok?"更新成功":"更新失败",null));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?>deleteNote(@PathVariable Long id){
        boolean ok=noteService.deleteNote(id);
        return ResponseEntity.ok(new ResultVO<>(200,ok?"删除成功":"删除失败",null));
    }
    @DeleteMapping("/delete/batch")
    public ResponseEntity<?> deleteBatchNote(@RequestBody NoteBatchDeleteDTO dto){
        boolean ok=noteService.deleteBatchNote(dto);
        return ResponseEntity.ok(new ResultVO<>(200,ok? "批量删除成功":"批量删除失败" ,null));
    }
    @GetMapping("/page")
    public ResponseEntity<?>page(NotePageDTO notePageDTO){
        Long userId=1L;
        NotePageResultVo<NotePageItemVO>pageVO=noteService.pageList(notePageDTO,userId);
        return ResponseEntity.ok(new ResultVO<>(200,"操作成功",pageVO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNoteDetail(@PathVariable Long id){
        NoteDetailVO detailVO=noteService.noteDetail(id);
        if(detailVO==null){
            return ResponseEntity.ok(new ResultVO<>(200,"笔记不存在",null));
        }
        return ResponseEntity.ok(new ResultVO<>(200,"操作成功",detailVO));
    }

    @GetMapping("/search")
    public ResponseEntity<?>search(NoteSearchDTO noteSearchDTO){
        Long userId=1L;
        NoteSearchResultVo<NoteSearchItemVO>searchVO=noteService.noteSearch(noteSearchDTO,userId);
        return ResponseEntity.ok(new ResultVO<>(200,"操作成功",searchVO));
    }

    @GetMapping("/statistics")
    public ResponseEntity<?>statistics(){
        Long  userId=1L;
        NoteStatisticsVO statisticsVO=noteService.noteStatistics(userId);
        return ResponseEntity.ok(new ResultVO<>(200,"操作成功",statisticsVO));
    }

    //导出






}
