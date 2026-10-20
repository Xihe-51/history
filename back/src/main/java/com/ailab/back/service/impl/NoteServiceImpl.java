package com.ailab.back.service.impl;

import com.ailab.back.DTO.*;
import com.ailab.back.Entity.Note;
import com.ailab.back.mapper.NoteMapper;
import com.ailab.back.service.NoteService;
import com.ailab.back.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 笔记服务实现。
 *
 * <p>修复要点：
 * <ul>
 *   <li>userId 一律取当前登录用户，不再硬编码 1L，也不会信任前端传入的 userId；</li>
 *   <li>所有查询/修改/删除都带 owner 条件，杜绝越权读写他人笔记；</li>
 *   <li>标签筛选改为参数化绑定，修复原 JSON_CONTAINS 字符串拼接导致的 SQL 注入。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    private static final Set<String> VALID_TYPES = Set.of("study", "insight", "collect", "custom");
    private static final int MAX_TAGS = 5;
    private static final int MAX_TAG_LENGTH = 20;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_PAGE_SIZE = 200;

    /** 富文本可能包含 <script>，入库前剥掉，避免详情页 v-html 触发 XSS */
    private static final Pattern SCRIPT_PATTERN =
            Pattern.compile("(?is)<\\s*script[^>]*>.*?<\\s*/\\s*script\\s*>");
    private static final Pattern EVENT_ATTR_PATTERN =
            Pattern.compile("(?i)\\son\\w+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)");
    private static final Pattern JS_HREF_PATTERN =
            Pattern.compile("(?i)(href|src)\\s*=\\s*(\"|')\\s*javascript:[^\"']*\\2");

    @Override
    public NotePageResultVo<NotePageItemVO> pageList(NotePageDTO dto, Long userId) {
        Page<Note> page = new Page<>(normalizePageNum(dto.getPageNum()), normalizePageSize(dto.getPageSize()));

        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, userId);

        if (StringUtils.hasText(dto.getType())) {
            queryWrapper.eq(Note::getType, dto.getType());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            queryWrapper.and(w -> w.like(Note::getTitle, kw).or().like(Note::getContent, kw));
        }
        if (StringUtils.hasText(dto.getTag())) {
            // 参数化传入 JSON 片段，避免拼接 SQL
            queryWrapper.apply("JSON_CONTAINS(tags, JSON_QUOTE({0}))", dto.getTag().trim());
        }
        if (StringUtils.hasText(dto.getStartTime())) {
            queryWrapper.ge(Note::getCreateTime, parseDateStart(dto.getStartTime()));
        }
        if (StringUtils.hasText(dto.getEndTime())) {
            queryWrapper.lt(Note::getCreateTime, parseDateStart(dto.getEndTime()).plusDays(1));
        }
        queryWrapper.orderByDesc(Note::getUpdateTime);

        Page<Note> result = this.page(page, queryWrapper);

        NotePageResultVo<NotePageItemVO> voPage = new NotePageResultVo<>();
        voPage.setTotal(result.getTotal());
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setPages(result.getPages());
        voPage.setRecords(result.getRecords().stream().map(this::toPageItem).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public NoteDetailVO noteDetail(Long id, Long userId) {
        Note note = getOwnedNote(id, userId);
        if (note == null) {
            return null;
        }
        return toDetail(note);
    }

    @Override
    @Transactional
    public Note addNote(NoteAddDTO dto, Long userId) {
        validateCommon(dto.getType(), dto.getTitle(), dto.getContent(), dto.getTags());

        Note note = new Note();
        note.setUserId(userId);
        note.setType(dto.getType());
        note.setTitle(dto.getTitle().trim());
        note.setContent(sanitizeHtml(dto.getContent()));
        note.setTags(normalizeTags(dto.getTags()));
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());
        this.save(note);
        return note;
    }

    @Override
    @Transactional
    public boolean updateNote(NoteUpdateDTO dto, Long userId) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("笔记ID不能为空");
        }
        Note owned = getOwnedNote(dto.getId(), userId);
        if (owned == null) {
            return false;
        }
        if (dto.getTitle() != null && dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("笔记标题不能为空");
        }
        if (dto.getType() != null && !VALID_TYPES.contains(dto.getType())) {
            throw new IllegalArgumentException("笔记类型不合法，可选值：study/insight/collect/custom");
        }
        if (dto.getTitle() != null && dto.getTitle().trim().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("笔记标题不能超过" + MAX_TITLE_LENGTH + "个字符");
        }

        LambdaUpdateWrapper<Note> update = new LambdaUpdateWrapper<>();
        update.eq(Note::getId, dto.getId())
                .eq(Note::getUserId, userId)
                .set(dto.getType() != null, Note::getType, dto.getType())
                .set(dto.getTitle() != null, Note::getTitle, dto.getTitle() == null ? null : dto.getTitle().trim())
                .set(dto.getContent() != null, Note::getContent, sanitizeHtml(dto.getContent()))
                .set(dto.getTags() != null, Note::getTags, toJson(normalizeTags(dto.getTags())))
                .set(Note::getUpdateTime, LocalDateTime.now());

        return this.update(update);
    }

    @Override
    @Transactional
    public boolean deleteNote(Long id, Long userId) {
        return this.remove(new LambdaQueryWrapper<Note>()
                .eq(Note::getId, id)
                .eq(Note::getUserId, userId));
    }

    @Override
    @Transactional
    public boolean deleteBatchNote(NoteBatchDeleteDTO dto, Long userId) {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            return false;
        }
        return this.remove(new LambdaQueryWrapper<Note>()
                .in(Note::getId, dto.getIds())
                .eq(Note::getUserId, userId));
    }

    @Override
    public NoteStatisticsVO noteStatistics(Long userId) {
        NoteStatisticsVO vo = new NoteStatisticsVO();

        List<Note> list = this.lambdaQuery().eq(Note::getUserId, userId).list();
        vo.setTotalCount((long) list.size());

        Map<String, Long> typeMap = list.stream()
                .filter(n -> n.getType() != null)
                .collect(Collectors.groupingBy(Note::getType, Collectors.counting()));

        // 固定输出四种类型（无数据时 count=0），前端图表更好处理
        List<NoteTypeStatVO> typeStats = new ArrayList<>();
        for (String type : List.of("study", "insight", "collect", "custom")) {
            NoteTypeStatVO item = new NoteTypeStatVO();
            item.setType(type);
            item.setTypeName(getTypeName(type));
            item.setCount(typeMap.getOrDefault(type, 0L));
            typeStats.add(item);
        }
        vo.setTypeStats(typeStats);

        // 最近 7 天新增统计
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);
        Map<String, Long> dateCountMap = list.stream()
                .filter(n -> n.getCreateTime() != null)
                .filter(n -> !n.getCreateTime().toLocalDate().isBefore(start))
                .collect(Collectors.groupingBy(n -> n.getCreateTime().toLocalDate().toString(), Collectors.counting()));

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

    @Override
    public NoteSearchResultVo<NoteSearchItemVO> noteSearch(NoteSearchDTO dto, Long userId) {
        if (!StringUtils.hasText(dto.getKeyword())) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        String keyword = dto.getKeyword().trim();

        Page<Note> page = new Page<>(normalizePageNum(dto.getPageNum()), normalizePageSize(dto.getPageSize()));
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, userId)
                .and(w -> w.like(Note::getTitle, keyword).or().like(Note::getContent, keyword));
        queryWrapper.orderByDesc(Note::getUpdateTime);

        Page<Note> result = this.page(page, queryWrapper);

        NoteSearchResultVo<NoteSearchItemVO> voPage = new NoteSearchResultVo<>();
        voPage.setTotal(result.getTotal());
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setPages(result.getPages());

        voPage.setRecords(result.getRecords().stream().map(n -> {
            NoteSearchItemVO vo = new NoteSearchItemVO();
            vo.setId(n.getId());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setType(n.getType());
            vo.setCreateTime(n.getCreateTime());

            Map<String, List<String>> highlight = new HashMap<>();
            highlight.put("title", List.of(highlightKeyword(n.getTitle(), keyword)));
            highlight.put("content", List.of(highlightKeyword(stripHtml(n.getContent()), keyword)));
            vo.setHighlight(highlight);
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    /**
     * 导出笔记：markdown / txt / html。pdf 由前端用浏览器打印生成。
     * ids 为空表示导出当前用户全部笔记。
     */
    @Override
    public byte[] export(String ids, String format, Long userId) {
        List<Long> idList = parseIds(ids);
        LambdaQueryWrapper<Note> qw = new LambdaQueryWrapper<Note>().eq(Note::getUserId, userId);
        if (!idList.isEmpty()) {
            qw.in(Note::getId, idList);
        }
        qw.orderByDesc(Note::getCreateTime);
        List<Note> notes = this.list(qw);
        if (notes.isEmpty()) {
            throw new IllegalArgumentException("没有可导出的笔记");
        }

        String fmt = format == null ? "markdown" : format.toLowerCase();
        StringBuilder sb = new StringBuilder();
        switch (fmt) {
            case "html", "pdf" -> {
                sb.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">")
                        .append("<title>我的笔记</title><style>")
                        .append("body{font-family:'Microsoft YaHei',sans-serif;line-height:1.8;max-width:820px;margin:40px auto;padding:0 20px;}")
                        .append("h1{color:#8B4513;border-bottom:2px solid #8B4513;padding-bottom:8px;}")
                        .append(".meta{color:#888;font-size:13px;margin-bottom:12px;}")
                        .append(".tags span{display:inline-block;background:#f0e6d6;color:#8B4513;padding:2px 8px;border-radius:10px;font-size:12px;margin-right:6px;}")
                        .append("</style></head><body><h1>我的笔记</h1>");
                for (Note n : notes) {
                    sb.append("<article><h2>").append(escapeHtml(n.getTitle())).append("</h2>")
                            .append("<div class=\"meta\">").append(getTypeName(n.getType()));
                    if (n.getCreateTime() != null) {
                        sb.append(" · 创建于 ").append(n.getCreateTime().toString().replace('T', ' '));
                    }
                    sb.append("</div>");
                    if (n.getTags() != null && !n.getTags().isEmpty()) {
                        sb.append("<div class=\"tags\">");
                        n.getTags().forEach(t -> sb.append("<span>").append(escapeHtml(t)).append("</span>"));
                        sb.append("</div>");
                    }
                    sb.append("<div>").append(n.getContent() == null ? "" : n.getContent()).append("</div>")
                            .append("<hr/></article>");
                }
                sb.append("</body></html>");
            }
            case "txt" -> {
                for (Note n : notes) {
                    sb.append(n.getTitle()).append('\n');
                    if (n.getCreateTime() != null) {
                        sb.append(n.getCreateTime().toString().replace('T', ' ')).append('\n');
                    }
                    sb.append(stripHtml(n.getContent())).append("\n\n--------------------------------\n\n");
                }
            }
            default -> {
                for (Note n : notes) {
                    sb.append("# ").append(n.getTitle()).append("\n\n");
                    sb.append("> 类型：").append(getTypeName(n.getType()));
                    if (n.getCreateTime() != null) {
                        sb.append(" ｜ 创建时间：").append(n.getCreateTime().toString().replace('T', ' '));
                    }
                    sb.append("\n\n");
                    if (n.getTags() != null && !n.getTags().isEmpty()) {
                        sb.append("标签：").append(n.getTags().stream().map(t -> "`" + t + "`")
                                .collect(Collectors.joining(" "))).append("\n\n");
                    }
                    sb.append(htmlToMarkdown(n.getContent())).append("\n\n---\n\n");
                }
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ==================== 内部辅助 ====================

    private Note getOwnedNote(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("笔记ID不能为空");
        }
        return this.lambdaQuery().eq(Note::getId, id).eq(Note::getUserId, userId).one();
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE ? 10 : pageSize;
    }

    private LocalDate parseDateStart(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式应为 yyyy-MM-dd：" + text);
        }
    }

    private List<Long> parseIds(String ids) {
        if (!StringUtils.hasText(ids)) {
            return new ArrayList<>();
        }
        List<Long> result = new ArrayList<>();
        for (String part : ids.split(",")) {
            String s = part.trim();
            if (s.isEmpty()) {
                continue;
            }
            try {
                result.add(Long.parseLong(s));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("笔记ID列表格式错误：" + s);
            }
        }
        return result;
    }

    private void validateCommon(String type, String title, String content, List<String> tags) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("笔记类型不能为空");
        }
        if (!VALID_TYPES.contains(type)) {
            throw new IllegalArgumentException("笔记类型不合法，可选值：study/insight/collect/custom");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("笔记标题不能为空");
        }
        if (title.trim().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("笔记标题不能超过" + MAX_TITLE_LENGTH + "个字符");
        }
        if (tags != null) {
            List<String> normalized = normalizeTags(tags);
            if (normalized.size() > MAX_TAGS) {
                throw new IllegalArgumentException("单条笔记最多支持" + MAX_TAGS + "个标签");
            }
            for (String t : normalized) {
                if (t.length() > MAX_TAG_LENGTH) {
                    throw new IllegalArgumentException("单个标签不能超过" + MAX_TAG_LENGTH + "个字符：" + t);
                }
            }
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(MAX_TAGS)
                .collect(Collectors.toList());
    }

    private String toJson(List<String> tags) {
        // MyBatis-Plus 的 JacksonTypeHandler 负责写入；此处仅用于 update 包装器场景的兜底
        StringJoiner joiner = new StringJoiner("\",\"", "[\"", "\"]");
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        tags.forEach(t -> joiner.add(t.replace("\"", "\\\"")));
        return joiner.toString();
    }

    /** 去掉脚本与事件属性，保留正常富文本标签 */
    private String sanitizeHtml(String html) {
        if (html == null) {
            return "";
        }
        String cleaned = SCRIPT_PATTERN.matcher(html).replaceAll("");
        cleaned = EVENT_ATTR_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = JS_HREF_PATTERN.matcher(cleaned).replaceAll("$1=$2#$2");
        return cleaned;
    }

    private String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String htmlToMarkdown(String html) {
        if (html == null) {
            return "";
        }
        return html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n\n")
                .replaceAll("(?i)</h[1-6]>", "\n\n")
                .replaceAll("(?i)<strong>(.*?)</strong>", "**$1**")
                .replaceAll("(?i)<b>(.*?)</b>", "**$1**")
                .replaceAll("(?i)<em>(.*?)</em>", "*$1*")
                .replaceAll("(?i)<li>(.*?)</li>", "- $1\n")
                .replaceAll("(?i)<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    private String highlightKeyword(String text, String keyword) {
        String safe = escapeHtml(text);
        String k = escapeHtml(keyword);
        // 大小写不敏感替换，保留原文大小写
        return safe.replaceAll("(?i)" + Pattern.quote(k), "<em>" + k + "</em>");
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private NotePageItemVO toPageItem(Note n) {
        NotePageItemVO vo = new NotePageItemVO();
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

    private NoteDetailVO toDetail(Note n) {
        NoteDetailVO vo = new NoteDetailVO();
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

    private String getTypeName(String type) {
        if (type == null) {
            return "未知类型";
        }
        return switch (type) {
            case "study" -> "学习笔记";
            case "insight" -> "心得笔记";
            case "collect" -> "收藏笔记";
            case "custom" -> "自定义";
            default -> "未知类型";
        };
    }
}
