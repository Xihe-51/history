package com.ailab.back.vo;

import com.ailab.back.Entity.Note;
import lombok.Data;

import java.security.Principal;
import java.util.List;
//统计信息
    //统计类型--单独类存储（考虑到复用性）
    //recentCreateItem--内部类存储（考虑到归属性）
@Data
public class NoteStatisticsVO {
    private Long totalCount;
    private List<NoteTypeStatVO>typeStats;
    private List<RecentCreateItem>recentCreate;
    @Data
    public static class RecentCreateItem {
        private String date;
        private Long count;
    }

}
