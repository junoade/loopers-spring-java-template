package com.loopers.batch.job.rankWeekly.step.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyAggRow {
    private String yearWeek;     // e.g. 2026-W01
    private Long productId;

    private Long viewCount;
    private Long likeCount;
    private Long orderCount;
}
