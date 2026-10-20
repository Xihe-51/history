package com.ailab.back.controller;

import com.ailab.back.DTO.*;
import com.ailab.back.Entity.Note;
import com.ailab.back.service.NoteService;
import com.ailab.back.utils.UserContext;
import com.ailab.back.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 笔记控制器。
 * 所有接口都要求登录（JwtAuthFilter 统一拦截），且只能操作当前登录用户自己的笔记。
 */
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/add")
    public ResponseEntity<ResultVO<NoteDetailVO>> addNote(@RequestBody NoteAddDTO dto) {
        Long userId = UserContext.getUserId();
        Note note = noteService.addNote(dto, userId);

        NoteDetailVO vo = new NoteDetailVO();
        vo.setId(note.getId());
        vo.setUserId(note.getUserId());
        vo.setType(note.getType());
        vo.setTitle(note.getTitle());
        vo.setContent(note.getContent());
        vo.setTags(note.getTags());
        vo.setCreateTime(note.getCreateTime());
        vo.setUpdateTime(note.getUpdateTime());

        return ResponseEntity.ok(new ResultVO<>(200, "新增成功", vo));
    }

    @PutMapping("/update")
    public ResponseEntity<ResultVO<Void>> updateNote(@RequestBody NoteUpdateDTO dto) {
        boolean ok = noteService.updateNote(dto, UserContext.getUserId());
        return ok
                ? ResponseEntity.ok(new ResultVO<>(200, "更新成功", null))
                : ResponseEntity.ok(new ResultVO<>(500, "笔记不存在或无权限修改", null));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResultVO<Void>> deleteNote(@PathVariable Long id) {
        boolean ok = noteService.deleteNote(id, UserContext.getUserId());
        return ok
                ? ResponseEntity.ok(new ResultVO<>(200, "删除成功", null))
                : ResponseEntity.ok(new ResultVO<>(500, "笔记不存在或无权限删除", null));
    }

    @DeleteMapping("/delete/batch")
    public ResponseEntity<ResultVO<Void>> deleteBatchNote(@RequestBody NoteBatchDeleteDTO dto) {
        boolean ok = noteService.deleteBatchNote(dto, UserContext.getUserId());
        return ok
                ? ResponseEntity.ok(new ResultVO<>(200, "批量删除成功", null))
                : ResponseEntity.ok(new ResultVO<>(500, "没有可删除的笔记", null));
    }

    @GetMapping("/page")
    public ResponseEntity<ResultVO<NotePageResultVo<NotePageItemVO>>> page(NotePageDTO dto) {
        return ResponseEntity.ok(new ResultVO<>(200, "操作成功",
                noteService.pageList(dto, UserContext.getUserId())));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ResultVO<NoteStatisticsVO>> statistics() {
        return ResponseEntity.ok(new ResultVO<>(200, "操作成功",
                noteService.noteStatistics(UserContext.getUserId())));
    }

    @GetMapping("/search")
    public ResponseEntity<ResultVO<NoteSearchResultVo<NoteSearchItemVO>>> search(NoteSearchDTO dto) {
        return ResponseEntity.ok(new ResultVO<>(200, "操作成功",
                noteService.noteSearch(dto, UserContext.getUserId())));
    }

    /**
     * 导出笔记。
     *
     * @param ids    笔记ID列表（逗号分隔，为空则导出全部）
     * @param format markdown / html / txt / pdf（pdf 返回可打印的 HTML，由前端另存为 PDF）
     */
    @GetMapping("/export")
    public ResponseEntity<?> export(@RequestParam(required = false) String ids,
                                    @RequestParam(defaultValue = "markdown") String format) {
        String fmt = format.toLowerCase();
        if (!fmt.equals("markdown") && !fmt.equals("html") && !fmt.equals("txt") && !fmt.equals("pdf")) {
            return ResponseEntity.ok(ResultVO.error("不支持的导出格式：" + format));
        }
        byte[] bytes = noteService.export(ids, fmt, UserContext.getUserId());
        String ext = switch (fmt) {
            case "html", "pdf" -> "html";
            case "txt" -> "txt";
            default -> "md";
        };
        String filename = "notes_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "." + ext;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentType(fmt.equals("html") || fmt.equals("pdf")
                ? new MediaType("text", "html", StandardCharsets.UTF_8)
                : MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /** 注意：/{id} 必须放在最后，避免吞掉 /page、/search 等固定路径 */
    @GetMapping("/{id}")
    public ResponseEntity<ResultVO<NoteDetailVO>> getNoteDetail(@PathVariable Long id) {
        NoteDetailVO detailVO = noteService.noteDetail(id, UserContext.getUserId());
        if (detailVO == null) {
            return ResponseEntity.ok(new ResultVO<>(500, "笔记不存在或无权限查看", null));
        }
        return ResponseEntity.ok(new ResultVO<>(200, "操作成功", detailVO));
    }
}
